package me.littlepanda.dadbear.master;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import me.littlepanda.dadbear.core.config.ConfigurationUtil;
import me.littlepanda.dadbear.master.distributealgoimpl.RotationDistributeAlgo;
import me.littlepanda.dadbear.core.rpc.ServiceInfo;
import me.littlepanda.dadbear.core.service.ServiceConstants;
import me.littlepanda.dadbear.core.util.ReflectionUtils;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class DistributeAlgoServiceFactory {

	private static Log log = LogFactory.getLog(DistributeAlgoServiceFactory.class);
	/*
	 * <p>服务更新同步标记，如果服务状态有更新，这个标记的时间戳会变化</p>
	 * <p>服务状态变化指的是节点上的服务启停、参数变更、服务节点启停导致的服务增减等</p>
	 */
	private static ConcurrentHashMap<String, String> serviceUpdateFlag = new ConcurrentHashMap<String, String>();
	
	private static Map<String, Class<? extends DistributeAlgo>> distributeAlgoMap = new HashMap<String, Class<? extends DistributeAlgo>>();
	static {
		distributeAlgoMap.put(ServiceConstants.DISTRIBUTE_ALGO_TYPE_ROTATION, RotationDistributeAlgo.class);
	}
	
	/**
	 * 保存各算法实现类
	 */
	private static ConcurrentHashMap<Class<? extends DistributeAlgo>, DistributeAlgo> instances = new ConcurrentHashMap<Class<? extends DistributeAlgo>, DistributeAlgo>();
	
	/**
	 * 根据服务名称获取下一个应该提供服务的服务（节点）信息
	 * @param service_name
	 * @return
	 */
	public static ServiceInfo getServiceInfo(String service_name) {
		Configuration conf = ConfigurationUtil.getModuleConfigs(service_name);
		String distributeAlgoType = conf.getString(ServiceConstants.CONFIG_DISTRIBUTE_ALGO_TYPE);
		//如果算法类型不存在，抛异常
		if(!distributeAlgoMap.containsKey(distributeAlgoType)){
			log.error("No distributed algorithm " + distributeAlgoType);
			throw new RuntimeException("No distributed algorithm " + distributeAlgoType);
		}
		//获取算法类型对应的实现类
		Class<? extends DistributeAlgo> clazz = distributeAlgoMap.get(distributeAlgoType);
		DistributeAlgo distributeAlgo = instances.get(clazz);
		//如果实例还没有创建，就创建一个
		if(null == distributeAlgo){
			distributeAlgo = ReflectionUtils.newInstance(clazz, conf);
			instances.put(clazz, distributeAlgo);
		}
		distributeAlgo.updateService(service_name, serviceUpdateFlag.get(service_name));
		return distributeAlgo.getService(service_name);
	}
	
	/**
	 * 更新服务同步标记
	 * @param service_name
	 * @param flag
	 */
	public static void updateFlag(String service_name, String flag){
		serviceUpdateFlag.put(service_name, flag);
	}
}
