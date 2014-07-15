package me.littlepanda.dadbear.core.service;

import org.apache.commons.configuration.Configuration;

import me.littlepanda.dadbear.core.cluster.ClusterUtil;
import me.littlepanda.dadbear.core.config.CommonConfigurationKeys;
import me.littlepanda.dadbear.core.config.ConfigurationUtil;
import me.littlepanda.dadbear.core.rpc.ServiceInfo;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class ServiceUtil {
	
	/**
	 * 获取client-master协议信息
	 * @return
	 */
	public static ServiceInfo getClientMasterServiceInfo(){
		ServiceInfo serviceInfo = new ServiceInfo();
		Configuration conf = ConfigurationUtil.getModuleConfigs(CommonConfigurationKeys.CLIENT_MASTER_PROTOCOL);
		serviceInfo.setName(CommonConfigurationKeys.CLIENT_MASTER_PROTOCOL);
		serviceInfo.setHost(ClusterUtil.getLeaderMasterInfo(null).getIp());
		serviceInfo.setDescription(conf.getString(ServiceConstants.CONFIG_DESC));
		serviceInfo.setPort(conf.getInt(ServiceConstants.CONFIG_PORT));
		serviceInfo.setProtocol(conf.getString(ServiceConstants.CONFIG_PROTOCOL));
		serviceInfo.setType(conf.getString(ServiceConstants.CONFIG_TYPE));
		serviceInfo.setImplement(conf.getString(ServiceConstants.CONFIG_IMPLEMENTATION));
		return serviceInfo;
	}
	
	/**
	 * 获取slave-master协议信息
	 * @return
	 */
	public static ServiceInfo getSlaveMasterServiceInfo(){
		ServiceInfo serviceInfo = new ServiceInfo();
		Configuration conf = ConfigurationUtil.getModuleConfigs(CommonConfigurationKeys.SLAVE_MASTER_PROTOCOL);
		serviceInfo.setName(CommonConfigurationKeys.SLAVE_MASTER_PROTOCOL);
		serviceInfo.setHost(ClusterUtil.getLeaderMasterInfo(null).getIp());
		serviceInfo.setDescription(conf.getString(ServiceConstants.CONFIG_DESC));
		serviceInfo.setPort(conf.getInt(ServiceConstants.CONFIG_PORT));
		serviceInfo.setProtocol(conf.getString(ServiceConstants.CONFIG_PROTOCOL));
		serviceInfo.setType(conf.getString(ServiceConstants.CONFIG_TYPE));
		serviceInfo.setImplement(conf.getString(ServiceConstants.CONFIG_IMPLEMENTATION));
		return serviceInfo;
	
	}
}
