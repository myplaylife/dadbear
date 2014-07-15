package me.littlepanda.dadbear.master;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import me.littlepanda.dadbear.core.cluster.ClusterNodeType;
import me.littlepanda.dadbear.core.cluster.ClusterUtil;
import me.littlepanda.dadbear.core.cluster.ZnodesPath;
import me.littlepanda.dadbear.core.config.CommonConfigurationKeys;
import me.littlepanda.dadbear.core.config.ConfigInfo;
import me.littlepanda.dadbear.core.config.ConfigInitState;
import me.littlepanda.dadbear.core.config.ConfigState;
import me.littlepanda.dadbear.core.config.ConfigurationUtil;
import me.littlepanda.dadbear.core.config.ModuleConfigChangeWatcher;
import me.littlepanda.dadbear.core.config.UpdateConfig;
import me.littlepanda.dadbear.core.rpc.MasterInfo;
import me.littlepanda.dadbear.core.rpc.ServiceInfo;
import me.littlepanda.dadbear.core.rpc.SlaveInfo;
import me.littlepanda.dadbear.core.service.Server;
import me.littlepanda.dadbear.core.service.ServiceConstants;
import me.littlepanda.dadbear.core.service.ServiceFactory;
import me.littlepanda.dadbear.core.util.BytesUtil;
import me.littlepanda.dadbear.core.util.Util;
import me.littlepanda.dadbear.core.util.ZookeeperUtil;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class Master implements Watcher {
	
	private static Log log = LogFactory.getLog(Master.class);
	
	private static boolean is_leader = false;
	private static String my_id = null;
	
	private static ZookeeperUtil zu;

	/*
	 * zookeeper集群管理路径 
	 */
	private static ZnodesPath znodesPath;
	
	/*
	 * 节点是否停止运行
	 */
	private static boolean running = true;
	
	/*
	 * 服务与slave节点的映射视图 
	 */
	private static Multimap<String, SlaveInfo> service_slave_maps = TreeMultimap.create();
	/*
	 * slave 节点列表
	 */
	private static Map<String, SlaveInfo> slaves = Maps.newConcurrentMap();
	
	/*
	 * 所有本地服务信息
	 */
	private static Map<String, Server> local_services = Maps.newConcurrentMap();
	
	/*
	 * master节点的配置模块
	 */
	private static ConcurrentHashMap<String, Configuration> master_configurations;
	
	/*
	 * 初始化配置，这个配置只从“init.properties”文件中来
	 */
	private static Configuration init_conf;
	
	//master节点当前状态
	private static String current_state = null;
	
	//正在创建zoopeer中的持久化节点
	private static final String MASTER_STATE_PRESTART = "PRESTART";
	//正在选举leader时
	private static final String MASTER_STATE_ALTERNATIVE = "ALTERNATIVE";
	//正在向zookeeper上传配置时
	private static final String MASTER_STATE_CONFIGURING = "CONFIGURING";
	//非leader节点一直处于这个状态
	private static final String MASTER_STATE_WAITING = "WAITING";
	//正在启动服务时
	private static final String MASTER_STATE_STARTING = "STARTING";
	//服务启动完毕，可以对外提供服务时
	private static final String MASTER_STATE_OK = "OK";
	
	private static final String MASTER_STATE_FAILURE = "FAILURE";
	
	/**
	 * 获取当前Master节点的状态
	 * @return
	 */
	public static String getCurrentState(){
		return current_state;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		 * 首先需要选举leader master节点
		 * 然后leader master初始化集群配置，而wait master等待加载集群配置
		 */
		current_state = MASTER_STATE_PRESTART;
		log.info("Master state chagned to prestart...");
		//init_conf是从本地的“init.properties”文件中加载的，不是从集群配置中加载的
		init_conf = ConfigurationUtil.getInitConfig();
		zu = ZookeeperUtil.get(init_conf);
		znodesPath = ClusterUtil.getZnonesPath();
		if(!zu.exists(znodesPath.getRoot())){
			zu.createPersistNode(znodesPath.getRoot());
		}
		if(!zu.exists(znodesPath.getMaster())){
			zu.createPersistNode(znodesPath.getMaster());
		}
		if(!zu.exists(znodesPath.getSlave())){
			zu.createPersistNode(znodesPath.getSlave());
		}
		if(!zu.exists(znodesPath.getConfig())){
			zu.createPersistNode(znodesPath.getConfig());
		}
		/*
		 * 向zookeeper注册leader选举监视器，并创建本节点在zookeeper中的映射
		 */
		zu.getChildren(znodesPath.getMaster(), new Master());
		current_state = MASTER_STATE_ALTERNATIVE;
		log.info("Master state chagned to alternative, choose leader now...");
		
		String path = zu.createEphemeralNode(znodesPath.getMaster_election(), new byte[0], true);
		my_id = path.substring(znodesPath.getMaster().length() + 1, path.length());
		
		try {
			while(running){
				Thread.sleep(10000);
			}
		} catch (InterruptedException e) {
			log.error("error when waiting for program run.", e);
			throw new RuntimeException("error when waiting for program run.");
		} finally {
			for(Server service : local_services.values()){
				if(service.isAlive()){
					service.stop();
				}
				
			}
		}
	}

	/**
	 * <p>master节点监视器，主要负责leader master的选举，以及选举的后续动作</p>
	 * <p>    1、如果是leader，那么需要把本身的配置信息放到zookeeper上</p>
	 * <p>    2、如果不是leader，需要注册config节点的getData事件，加载由leader上传到zookeeper的配置信息。 并在leader下线时，重新选举leader，并上传配置</p>
	 */
	public void process(WatchedEvent event) {
		/*
		 * 如果主节点掉了
		 * 重新进行集群配置初始化过程，先把/config节点数据设为空，是为了在重新初始化完集群配置前，所有节点都不进行本地的初始化
		 */
		if(!ClusterUtil.isLeaderAlive(zu, init_conf)){
			//如果leader down，启动新一轮集群配置更新流程
			zu.setData(znodesPath.getConfig(), new byte[0]);
		} else if(ConfigurationManage.cluster_config_load_state == ConfigurationManage.CONFIG_LOAD_STATE_YES){
			//如果leader没down，说明新进来一个备用master。如果集群配置加载完了，说明不是第一次加载，直接退出
			zu.getChildren(znodesPath.getMaster(), new Master());
			return;
		}
		
		Object[] alters = zu.getChildren(znodesPath.getMaster(), new Master()).toArray();
		Arrays.sort(alters);
		if(my_id.equals(alters[0])){
			is_leader = true;
		}
		if(is_leader){
			//删除所有配置模块，重新建立
			List<String> modulePath = zu.getChildren(znodesPath.getConfig());
			for(String module : modulePath){
				zu.deleteNode(znodesPath.getModulePath(module));
			}
		}
		
		current_state = MASTER_STATE_CONFIGURING;
		if(is_leader){
			log.info("Master state chagned to configuring, init cluster configuration now...");
		} else {
			log.info("Master state chagned to configuring, waiting for leader to init cluster configuration now...");
		}
		
		if(is_leader) {
			//设置config根路径状态为配置“正在初始化”
			ConfigState configStateINIT = ConfigState.newBuilder().setState(ConfigInitState.INIT).build();
			byte[] bytesConfigStateINIT = BytesUtil.classToBytes(configStateINIT, init_conf);
			zu.setData(znodesPath.getConfig(), bytesConfigStateINIT);
			
			//初始化配置，将所有配置加载到zookeeper
			ConfigurationManage.initClusterConfig();
			
			//初始化集群配置后，将跟配置节点和各模块配置节点的状态均置为OK
			ConfigState configStateOK = ConfigState.newBuilder().setState(ConfigInitState.OK).build();
			byte[] bytesConfigStateOK =  BytesUtil.classToBytes(configStateOK, init_conf);
			List<String> configModules = zu.getChildren(znodesPath.getConfig());
			for(String module : configModules){
				zu.setData(znodesPath.getModulePath(module), bytesConfigStateOK);
			}
			zu.setData(znodesPath.getConfig(), bytesConfigStateOK);
		}
		
		/*
		 * 非leader节点会首先走到这里，这时需要一起等待leader初始化集群配置结束
		 */
		byte[] bytesConfigState = zu.getData(znodesPath.getConfig());
		while(bytesConfigState.length == 0){
			Util.threadSleep(2000);
			bytesConfigState = zu.getData(znodesPath.getConfig());
		}
		while(bytesConfigState.length > 0){
			bytesConfigState = zu.getData(znodesPath.getConfig());
			ConfigState configState = BytesUtil.bytesToClass(bytesConfigState, init_conf, ConfigState.class);
			if(configState.getState() != ConfigInitState.OK){
				Util.threadSleep(2000);
				continue;
			} else {
				break;
			}
		}
		
		master_configurations = ConfigurationUtil.initNodeConfig(ClusterNodeType.master);
		ConfigurationManage.getAllConfigsFromZookeeper(znodesPath);
		/*
		 * 注册配置变更监视器
		 *  1、节点运行所需配置模块的配置变更，需要改变 master_configurations
		 *  2、所有模块配置变更都需要变更 ConfigurationManage.all_config对象
		 */
		for(String module : master_configurations.keySet()){
			zu.getData(znodesPath.getModulePath(module), new ModuleConfigChangeWatcher(master_configurations, init_conf));
		}
		Multimap<String, ConfigInfo> allConfig = ConfigurationManage.getAllConfigsFromZookeeper(znodesPath);
		for(String module : allConfig.keySet()){
			zu.getData(znodesPath.getModulePath(module), new AllConfigChangeWatcher());
		}
		
		/*
		 * 注册slave node监视器
		 */
		retriveSlaves();

		if(is_leader){
			current_state = MASTER_STATE_STARTING;
			log.info("Starting service now...");
			/*
			 * 在这里需要加载master服务 
			 *   1、client-master-protocol
			 *   2、slave-master-protocol（heartbeat）
			 */
			for(String service : master_configurations.keySet()){
				if(!service.equals(ConfigurationUtil.COMMON_MODULE) && !service.equals(ConfigurationUtil.MASTER_MODULE)){
					Server serv = ServiceFactory.getService(master_configurations.get(service));
					serv.start();
					local_services.put(service, serv);
				}
			}
		}
		
		/*
		 * 将节点信息保存在zookeeper中
		 * 	leader保存在master自己的临时节点，和master根节点上
		 *  非leader保存在自己的临时节点上 
		 */
		MasterInfo masterInfo = new MasterInfo();
		masterInfo.setId(my_id);
		masterInfo.setIp(Util.getLocalIp());
		masterInfo.setHost(Util.getLocalHost());
		masterInfo.setClientMasterPort(master_configurations.get(CommonConfigurationKeys.CLIENT_MASTER_PROTOCOL).getInt(ServiceConstants.CONFIG_PORT));
		masterInfo.setSlaveMasterPort(master_configurations.get(CommonConfigurationKeys.SLAVE_MASTER_PROTOCOL).getInt(ServiceConstants.CONFIG_PORT));
		masterInfo.setIsLeader(is_leader);
		byte[] bytesMasterInfo = BytesUtil.classToBytes(masterInfo, init_conf);
		zu.setData(znodesPath.getMaster() + "/" + my_id, bytesMasterInfo);
		if(is_leader){
			zu.setData(znodesPath.getMaster(), bytesMasterInfo);
			current_state = MASTER_STATE_OK;
			log.info("Leader master node stated.");
		} else {
			current_state = MASTER_STATE_WAITING;
			log.info("This Master is not leader, now is waiting for leader master down...");
		}
	}
	
	/**
	 * master节点更新本地的集群配置缓存
	 */
	class AllConfigChangeWatcher implements Watcher {
		@Override
		public void process(WatchedEvent event) {
			
			String path = event.getPath();
			//从事件路径中得出模块名
			String module = path.substring(path.lastIndexOf('/') + 1);
			//取得要修改的配置对象，此Collection为Set类型
			final Collection<ConfigInfo> configInfos = ConfigurationManage.getAllConfigsFromZookeeper(znodesPath).get(module);
			final Iterator<ConfigInfo> configInfos_iterator = configInfos.iterator();
			
			UpdateConfig updateConfig = new UpdateConfig() {
				@Override
				public void updateConfig(ConfigInfo configInfo, String changedName) {
					while(configInfos_iterator.hasNext()){
						ConfigInfo ci = configInfos_iterator.next();
						if(ci.getName().toString().equals(changedName)){
							configInfos_iterator.remove();
							break;
						}
					}
					configInfos.add(configInfo);
				}
				@Override
				public void removeConfig(String changedName) {
					while(configInfos_iterator.hasNext()){
						ConfigInfo ci = configInfos_iterator.next();
						if(ci.getName().toString().equals(changedName)){
							configInfos.remove(ci);
						}
					}
				}
			};
			
			ConfigurationUtil.updateModuleConfig(path, updateConfig, new AllConfigChangeWatcher(), init_conf);
		}
	}
	
	
	/**
	 * 用来监视slave节点的值改变事件，不处理其他事件
	 */
	class SlaveNodeWatcher implements Watcher{
		public void process(WatchedEvent event) {
			if(event.getType() == EventType.NodeDataChanged) {
				log.info("slave node changed, retrive slave nodes.");
				String path = event.getPath();
				byte[] bytesSlaveInfo = zu.getData(event.getPath(), new SlaveNodeWatcher());
				String slave_id = path.substring(path.lastIndexOf('/') + 1);
				SlaveInfo slaveInfo = BytesUtil.bytesToClass(bytesSlaveInfo, init_conf, SlaveInfo.class);
				slaveInfo.setId(slave_id);
				if(!slaveInfo.equals(slaves.get(slave_id))){
					slaves.put(slave_id, slaveInfo);
					//先删除该节点的所有服务对应关系信息，再添加新的
					removeSlaveFromServiceSlaveMaps(slaveInfo.getId().toString());
					for(ServiceInfo s : slaveInfo.getServices()){
						service_slave_maps.put(s.getName().toString(), slaveInfo);
						DistributeAlgoServiceFactory.updateFlag(s.getName().toString(), UUID.randomUUID().toString());
					}
				}
			}
		}
	}
	
	/**
	 * 监视slave节点的创建和删除事件
	 */
	class SlaveAddWatcher implements Watcher {

		@Override
		public synchronized void process(WatchedEvent event) {
			List<String> slaveNodes = zu.getChildren(znodesPath.getSlave(), new SlaveAddWatcher());
			List<String> now = Lists.newArrayList();
			now.addAll(slaves.keySet());
			
			if(slaveNodes.size() > now.size()){
				//节点创建事件
				if(slaveNodes.removeAll(now) || slaveNodes.size() == 1){
					String createId = slaveNodes.get(0);
					byte[] bytesCreateId = zu.getData(znodesPath.getSlave() + "/" + createId, new SlaveNodeWatcher());
					SlaveInfo slaveInfo = BytesUtil.bytesToClass(bytesCreateId, init_conf, SlaveInfo.class);
					slaveInfo.setId(createId);
					slaves.put(createId, slaveInfo);
					for(ServiceInfo s : slaveInfo.getServices()){
						service_slave_maps.put(s.getName().toString(), slaveInfo);
						DistributeAlgoServiceFactory.updateFlag(s.getName().toString(), UUID.randomUUID().toString());
					}
					return;
				} 
			} else if(slaveNodes.size() < now.size()){
				//节点删除事件
				if(now.removeAll(slaveNodes) || now.size() == 1){
					String delId = now.get(0);
					removeSlaveFromServiceSlaveMaps(delId);
					slaves.remove(now.get(0));
					return;
				}
			} else if(slaveNodes.size() == now.size()){
				return;
			}
//			retriveSlaves();
		}
		
	}
	
	/**
	 * 获取所有slaves信息
	 */
	private synchronized void retriveSlaves(){
		synchronized (slaves) {
			synchronized (service_slave_maps) {
				slaves.clear();
				service_slave_maps.clear();
				List<String> slaveNodes = zu.getChildren(znodesPath.getSlave(), new SlaveAddWatcher());
				for(String slaveId : slaveNodes){
					byte[] bytesSlaveInfo = zu.getData(znodesPath.getSlave() + "/" + slaveId, new SlaveNodeWatcher());
					SlaveInfo slaveInfo = BytesUtil.bytesToClass(bytesSlaveInfo, init_conf, SlaveInfo.class);
					slaveInfo.setId(slaveId);
					slaves.put(slaveId, slaveInfo);
					for(ServiceInfo s : slaveInfo.getServices()){
						service_slave_maps.put(s.getName().toString(), slaveInfo);
					}
				}
				for(String serviceName : service_slave_maps.keySet()){
					DistributeAlgoServiceFactory.updateFlag(serviceName, UUID.randomUUID().toString());
				}
			}
		}
	}

	/**
	 * 根据slave node ip，删除“service-slave”对应关系map中的slave节点信息
	 * @param delIp
	 */
	private void removeSlaveFromServiceSlaveMaps(String delNodeId) {
		Map<String, SlaveInfo> temp = Maps.newHashMap();
		for(String service : service_slave_maps.keySet()){
			for(SlaveInfo slaveInfo : service_slave_maps.get(service)){
				if(delNodeId.equals(slaveInfo.getId())){
					temp.put(service, slaveInfo);
				}
			}
		}
		for(String service : temp.keySet()){
			service_slave_maps.remove(service, temp.get(service));
			DistributeAlgoServiceFactory.updateFlag(service, UUID.randomUUID().toString());
		}
	}
	/**
	 * 通过服务名获取slave节点列表
	 * @param name
	 * @return
	 */
	public static Collection<SlaveInfo> getSlavesByService(String name){
		return service_slave_maps.get(name);
	}

	/**
	 * 通过slave节点id返回slave节点信息
	 * @param slave_id
	 * @return
	 */
	public static SlaveInfo getSlaveBySlaveId(String slave_id){
		return slaves.get(slave_id);
	}
	
	/**
	 * 获取所有slave节点信息
	 */
	public static List<SlaveInfo> getSlaves(){
		return Lists.newArrayList(slaves.values());
	}
	
	/**
	 * 通过服务名获取所有服务节点信息
	 * @param service_name
	 * @return
	 */
	public static List<ServiceInfo> getAllServiceInfosByServiceName(String service_name) {
		Collection<SlaveInfo> slaves = getSlavesByService(service_name);
		List<ServiceInfo> servers = Lists.newArrayList();
		for(SlaveInfo slaveInfo : slaves){
			for(ServiceInfo server : slaveInfo.getServices()){
				if(server.getName().toString().equals(service_name)){
					servers.add(server);
				}
			}
		}
		return servers;
	}
	
	public static void close(){
		running = false;
	}
	
}
