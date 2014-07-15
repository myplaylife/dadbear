package me.littlepanda.dadbear.core.service;

import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;
import me.littlepanda.dadbear.core.schedule_with_queue.QueueProcessor;
import me.littlepanda.dadbear.core.service.service_impl.OsgiServer;
import me.littlepanda.dadbear.core.service.service_impl.RemoteAvdlServer;
import me.littlepanda.dadbear.core.service.service_impl.RemoteOsgiServer;
import me.littlepanda.dadbear.core.service.service_impl.RemoteServer;
import me.littlepanda.dadbear.core.service.service_impl.SchedulerServer;
import me.littlepanda.dadbear.core.util.ReflectionUtils;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class ServiceFactory {
	
	private static Log log = LogFactory.getLog(ServiceFactory.class);
	
//	private static Map<String, Server> cache = Maps.newHashMap();
	
	private static Map<String, Class<? extends Server>> type_service = Maps.newHashMap();
	
	static {
		type_service.put(ServiceConstants.SERVICE_TYPE_OSGI, OsgiServer.class);
		type_service.put(ServiceConstants.SERVICE_TYPE_OSGI_REMOTE, RemoteOsgiServer.class);
		type_service.put(ServiceConstants.SERVICE_TYPE_REMOTE, RemoteServer.class);
		type_service.put(ServiceConstants.SERVICE_TYPE_REMOTE_AVDL, RemoteAvdlServer.class);
		type_service.put(ServiceConstants.SERVICE_TYPE_SCHEDULE, SchedulerServer.class);
	}
	/**
	 * 获取服务
	 */
	public static Server getService(Configuration conf) {
//		String key = conf.getString(ServiceConstants.CONFIG_PROTOCOL) + ":" 
//				+ conf.getString(ServiceConstants.CONFIG_PORT) + ":"
//				+ conf.getString(QueueProcessor.KEY_PROCESSOR);
//		if(cache.containsKey(key)){
//			return cache.get(key);
//		}
		String type = conf.getString(ServiceConstants.CONFIG_TYPE);
		Class<? extends Server> clazz = type_service.get(type);
		Server service = ReflectionUtils.newInstance(clazz, conf);
//		cache.put(key, service);
		return service;
	}
}
