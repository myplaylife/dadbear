package me.littlepanda.dadbear.slave;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.littlepanda.dadbear.core.cluster.ClusterNodeType;
import me.littlepanda.dadbear.core.cluster.ClusterUtil;
import me.littlepanda.dadbear.core.cluster.ZnodesPath;
import me.littlepanda.dadbear.core.config.ConfigurationUtil;
import me.littlepanda.dadbear.core.config.ModuleConfigChangeWatcher;
import me.littlepanda.dadbear.core.rpc.MasterInfo;
import me.littlepanda.dadbear.core.rpc.ServiceInfo;
import me.littlepanda.dadbear.core.rpc.SlaveInfo;
import me.littlepanda.dadbear.core.service.Server;
import me.littlepanda.dadbear.core.service.ServiceFactory;
import me.littlepanda.dadbear.core.util.BytesUtil;
import me.littlepanda.dadbear.core.util.Util;
import me.littlepanda.dadbear.core.util.ZookeeperUtil;

/**
 * @author 张静波 myplaylife@icloud.com
 * 
 */
public class Slave {

	private static Log log = LogFactory.getLog(Slave.class);

	private static ZookeeperUtil zu;
	public static Configuration init_config;

	private static ConcurrentHashMap<String, Configuration> slave_configurations;

	private static String my_id = null;

	private static boolean running = true;

	// master节点当前状态
	private static String current_state = null;

	//
	private static final String SLAVE_STATE_PRESTART = "PRESTART";
	//
	private static final String SLAVE_STATE_INIT = "INIT";
	//
	private static final String SLAVE_STATE_READY = "READY";
	//
	private static final String SLAVE_STATE_OK = "OK";

	/*
	 * zookeeper集群管理路径
	 */
	private static ZnodesPath znodesPath;

	private static MasterInfo leader;

	/*
	 * 本节点运行的服务列表
	 */
	private static Map<String, Server> services = Maps.newHashMap();

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		current_state = SLAVE_STATE_PRESTART;
		log.info("Starting slave node...");

		init_config = ConfigurationUtil.getInitConfig();
		zu = ZookeeperUtil.get(init_config);
		znodesPath = ClusterUtil.getZnonesPath();

		/*
		 * 重试20次，如果依然没有leader master，就退出
		 */
		for (int i = 0; !ClusterUtil.isLeaderAlive(zu, init_config) && i < 20; i++) {
			Util.threadSleep(2000);
		}
		if (!ClusterUtil.isLeaderAlive(zu, init_config)) {
			return;
		}

		/*
		 * 获取leader master信息
		 */
		getLeaderMaster();

		current_state = SLAVE_STATE_INIT;
		log.info("Find leader master, starting init config...");

		/*
		 * 获取slave节点通用配置
		 */
		slave_configurations = ConfigurationUtil
				.initNodeConfig(ClusterNodeType.slave);
		/*
		 * 注册配置变更监视器<br/> 节点运行所需配置模块的配置变更，需要改变 slave_configurations
		 */
		for (String module : slave_configurations.keySet()) {
			zu.getData(znodesPath.getModulePath(module),
					new ModuleConfigChangeWatcher(slave_configurations,
							init_config));
		}

		current_state = SLAVE_STATE_READY;
		log.info("Initialize config complete, staring serivce now...");

		/**
		 * 启动服务
		 */
		for(String service : slave_configurations.keySet()){
			log.info(service);
			if(!service.equals(ConfigurationUtil.COMMON_MODULE) && !service.equals(ConfigurationUtil.SLAVE_MODULE)){
				log.info(slave_configurations.get(service));
				Server serv = ServiceFactory.getService(slave_configurations.get(service));
				serv.start();
				services.put(service, serv);
			}
		}
	
		/*
		 * 还需要添加服务信息
		 */
		SlaveInfo slaveInfo = SlaveInfo.newBuilder().setIp(Util.getLocalIp()).setHost(Util.getLocalHost()).setId(null).build();
		List<ServiceInfo> serviceInfos = Lists.newArrayList();
		for(String service : services.keySet()){
			ServiceInfo serviceInfo = new ServiceInfo();
			Server serv = services.get(service);
			serviceInfo.setName(service);
			serviceInfo.setHost(slaveInfo.getIp().toString());
			serviceInfo.setDescription(serv.getDescription());
			serviceInfo.setPort(serv.getPort());
			serviceInfo.setProtocol(serv.getProtocol());
			serviceInfo.setSchedule(serv.getSchedule());
			serviceInfo.setType(serv.getType());
			serviceInfo.setImplement(serv.getImplement());
			serviceInfo.setDistributeAlgo(serv.getDistributeAlgo());
			serviceInfos.add(serviceInfo);
		}
		slaveInfo.setServices(serviceInfos);
		String path = zu.createEphemeralNode(znodesPath.getSlave_register(),
				BytesUtil.classToBytes(slaveInfo, init_config), true);
		my_id = path.substring(path.lastIndexOf('/') + 1);
		current_state = SLAVE_STATE_OK;
		log.info("Slave node started.");
		try {
			while (running) {
				Thread.sleep(10000);
			}
		} catch (InterruptedException e) {
			log.error("error when waiting for program run.", e);
			throw new RuntimeException("error when waiting for program run.", e);
		} finally {
			for(Server service : services.values()){
				if(service.isAlive()){
					service.stop();
				}
				
			}
		}
	}

	/**
	 * master leader变更监视器
	 */
	static class LeaderChangedWatcher implements Watcher {
		@Override
		public void process(WatchedEvent event) {
			getLeaderMaster();
			/**
			 * TODO 重建心跳protocol client
			 */
		}
	}

	/**
	 * 获取leader master信息
	 */
	private static void getLeaderMaster() {
		leader = ClusterUtil.getLeaderMasterInfo(new LeaderChangedWatcher());
	}

	/**
	 * 获取当前状态
	 * 
	 * @return
	 */
	public static String getCurrentState() {
		return current_state;
	}

	/**
	 * 获取服务
	 */
	public static Server getService(String name) {
		return services.get(name);
	}

	public static void close(){
		running = false;
	}
}
