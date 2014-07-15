package me.littlepanda.dadbear.client;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.avro.AvroRemoteException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import me.littlepanda.dadbear.core.cluster.ClusterNodeType;
import me.littlepanda.dadbear.core.cluster.ClusterUtil;
import me.littlepanda.dadbear.core.cluster.ZnodesPath;
import me.littlepanda.dadbear.core.config.ConfigurationUtil;
import me.littlepanda.dadbear.core.config.ModuleConfigChangeWatcher;
import me.littlepanda.dadbear.core.rpc.ClientMasterProtocol;
import me.littlepanda.dadbear.core.rpc.ServiceInfo;
import me.littlepanda.dadbear.core.rpc.SlaveInfo;
import me.littlepanda.dadbear.core.service.ServiceClientFactory;
import me.littlepanda.dadbear.core.service.ServiceUtil;
import me.littlepanda.dadbear.core.util.ZookeeperUtil;

/**
 * @author ferry myplaylife@icloud.com
 *
 */
public class Client {
	
	private static Log log = LogFactory.getLog(Client.class);
	
	private static ZookeeperUtil zu;
	private static ZnodesPath zp;
	private static Configuration init_conf;
	private static ConcurrentHashMap<String, Configuration> client_configurations = ConfigurationUtil.initNodeConfig(ClusterNodeType.client);
	
	static {
		init_conf = ConfigurationUtil.getInitConfig();
		zu = ZookeeperUtil.get(init_conf);
		zp = ClusterUtil.getZnonesPath();
		updateConfiguration();
	}
	
	/**
	 * <p>get service client object by service name：</p>
	 * <p>server will return a service info from one slave node , by the distributed algorithm which define in the service configuration</p>
	 * @param service_name
	 * @return
	 */
	public static<T> T getClientByServiceName(String service_name) {
		ServiceInfo serviceInfo;
		try {
			serviceInfo = getClientMasterProtocol().getServiceInfoByName(service_name);
		} catch (AvroRemoteException e) {
			log.error("error when invoke client-master-protocol.getServiceInfoByName", e);
			throw new RuntimeException("error when invoke client-master-protocol.getServiceInfoByName", e);
		}
		return ServiceClientFactory.getClientByServiceInfo(serviceInfo);
	}
	
	/**
	 * <p>根据服务名获取所有服务对象：</p> 
	 * <p>会返回所有提供该服务的slave node的服务信息</p>
	 */
	public static<T> List<T> getAllClientsByServiceName(String service_name) {
		List<ServiceInfo> serviceInfos;
		try {
			serviceInfos = getClientMasterProtocol().getAllServiceInfosByServiceName(service_name);
		} catch (AvroRemoteException e)	{
			log.error("error when invoke client-master-protocol.getAllServiceInfosByServiceName", e);
			throw new RuntimeException("error when invoke client-master-protocol.getAllServiceInfosByServiceName", e);
		}
		List<T> clients = Lists.newArrayList();
		for(ServiceInfo si : serviceInfos){
			clients.add(ServiceClientFactory.<T>getClientByServiceInfo(si));
		}
		return clients;
	}
	
	/**
	 * <p>根据服务名称和slave node的id获取服务对象：</p>
	 * <p>会返回特定slave node的服务信息</p>
	 */
	public static<T> T getClientByServiceNameAndSlaveId(String service_name, String slave_id) {
		ServiceInfo serviceInfo;
		try {
			serviceInfo = getClientMasterProtocol().getServiceInfoByNameAndSlaveId(service_name, slave_id);
		} catch (AvroRemoteException e) {
			log.error("error when invoke client-master-protocol.getServiceInfoByNameAndSlaveId", e);
			throw new RuntimeException("error when invoke client-master-protocol.getServiceInfoByNameAndSlaveId", e);
		}
		return ServiceClientFactory.getClientByServiceInfo(serviceInfo);
	}
	
	/**
	 * @param service_name 服务名称
	 * @param hostname 主机名称
	 * @return 远程服务客户端
	 */
	public static<T> T getClientByServiceNameAndHostname(String service_name, String hostname) {
		String slaveId = getSlaveidByHostname(hostname);
		ServiceInfo serviceInfo;
		try {
			serviceInfo = getClientMasterProtocol().getServiceInfoByNameAndSlaveId(service_name, slaveId);
		} catch (AvroRemoteException e) {
			log.error("error when invoke client-master-protocol.getServiceInfoByNameAndSlaveId", e);
			throw new RuntimeException("error when invoke client-master-protocol.getServiceInfoByNameAndSlaveId", e);
		}
		return ServiceClientFactory.getClientByServiceInfo(serviceInfo);
	}
	
	/**
	 * <p>获取所有slave node的信息</p>
	 * <p>TODO 获取slave节点不应该通过master节点来走，而应该直接从zookeeper上获得，这是需要修改的地方</p>
	 */
	public static List<SlaveInfo> getAllSlaveInfo() {
		try {
			return getClientMasterProtocol().getSlaves();
		} catch (AvroRemoteException e) {
			log.error("error when invoke client-master-protocol.getSlaves", e);
			throw new RuntimeException("error when invoke client-master-protocol.getSlaves", e);
		}
	}
	
	/**
	 * <p>根据slave id，获取slave node信息</p>
	 */
	public static SlaveInfo getSlaveInfoById(String slave_id) {
		try {
			return getClientMasterProtocol().getSlaveById(slave_id);
		} catch (AvroRemoteException e) {
			log.error("error when invoke client-master-protocol.getSlaveById", e);
			throw new RuntimeException("error when invoke client-master-protocol.getSlaveById", e);
		}
	}
	
	/**
	 * @param service_name 服务名称
	 * @return 所有服务信息
	 */
	public static List<ServiceInfo> getAllServiceInfos(String service_name) {
		try {
			return getClientMasterProtocol().getAllServiceInfosByServiceName(service_name);
		} catch (AvroRemoteException e) {
			log.error("error when invoke client-master-protocol.getAllServiceInfosByServiceName", e);
			throw new RuntimeException("error when invoke client-master-protocol.getAllServiceInfosByServiceName", e);
		}
	}
	
	/**
	 * 关闭一个客户单
	 * @param t
	 */
	public static<T> void close(T t){
		ServiceClientFactory.close(t);
	}
	public static<T> void closeAll(List<T> ts){
		for(T t : ts){
			ServiceClientFactory.close(t);
		}
	}
	/**
	 * <p>获取客户端-master节点协议</p>
	 * @return
	 */
	private static ClientMasterProtocol getClientMasterProtocol() {
		ServiceInfo clientMasterInfo = ServiceUtil.getClientMasterServiceInfo();
		return ServiceClientFactory.getClientByServiceInfo(clientMasterInfo);
	}
	
	/**
	 * 注册zookeeper，更新配置
	 */
	private static void updateConfiguration(){
		for (String module : client_configurations.keySet()) {
			zu.getData(zp.getModulePath(module),
					new ModuleConfigChangeWatcher(client_configurations, init_conf));
		}
	}

	/**
	 * @param hostname 主机名称
	 * @return 该主机上slave节点的id
	 */
	private static String getSlaveidByHostname(String hostname) {
		List<SlaveInfo> slaves = getAllSlaveInfo();
		for(SlaveInfo slave : slaves) {
			if(slave.getHost().toString().equals(hostname)) {
				return slave.getId().toString();
			}
		}
		return null;
	}
}
