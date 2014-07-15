package me.littlepanda.dadbear.core.service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.Requestor;
import org.apache.avro.ipc.reflect.ReflectRequestor;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;
import me.littlepanda.dadbear.core.rpc.ServiceInfo;
import me.littlepanda.dadbear.core.util.ReflectionUtils;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class ServiceClientFactory {
	
	private static Log log = LogFactory.getLog(ServiceClientFactory.class);
	
	private static Map<ServiceInfo, ProtocolWrapper> cache = Maps.newHashMap();
	
	/**
	 * 根据服务信息获取客户端对象
	 * @param serviceInfo
	 * @return
	 */
	public static<T> T getClientByServiceInfo(ServiceInfo serviceInfo) {
		String type = serviceInfo.getType().toString();
		if(ServiceConstants.SERVICE_TYPE_REMOTE_AVDL.equals(type)){
			return getAvdlClient(serviceInfo);
		} else if(ServiceConstants.SERVICE_TYPE_REMOTE.equals(type)){
			return getRemoteClient(serviceInfo);
		} else {
			log.error("No implement service type:");
			throw new RuntimeException("No implement service type:" + type);
		}
	}
	
	/**
	 * 获取avdl形式的服务客户端
	 * @param serviceInfo
	 * @return
	 */
	private static<T> T getAvdlClient(ServiceInfo serviceInfo){
		T protocol = getProtocolFromCache(serviceInfo);
		if(protocol != null){
			return protocol;
		}
		NettyTransceiver client = null;
		try {
			client =  new NettyTransceiver(new InetSocketAddress(serviceInfo.getHost().toString(), serviceInfo.getPort()));
			protocol = SpecificRequestor.getClient(ReflectionUtils.<T>getClass(serviceInfo.getProtocol().toString()), client);
		} catch (IOException e) {
			log.error("error when create Avdl client.", e);
			throw new RuntimeException("error when create Avdl client.", e);
		}
		cache.put(serviceInfo, new ProtocolWrapper(protocol));
		return protocol;
	}
	
	/**
	 * 获取pojo形式的服务客户端
	 * @param serviceInfo
	 * @return
	 */
	private static<T> T getRemoteClient(ServiceInfo serviceInfo) {
		T protocol = getProtocolFromCache(serviceInfo);
		if(protocol != null){
			return protocol;
		}
		NettyTransceiver client = null;
		try {
			client =  new NettyTransceiver(new InetSocketAddress(serviceInfo.getHost().toString(), serviceInfo.getPort()));
			protocol = ReflectRequestor.getClient(ReflectionUtils.<T>getClass(serviceInfo.getProtocol().toString()), client);
		} catch (IOException e) {
			log.error("error when create Remote client.", e);
			throw new RuntimeException("error when create Remote client.", e);
		} 
		cache.put(serviceInfo, new ProtocolWrapper(protocol));
		return protocol;
	}

	/**
	 * 关闭一个客户端
	 * @param t
	 */
	public static<T> void close(T t){
		List<Field> fields = ReflectionUtils.getDeclaredFieldsIncludingInherited(t.getClass());
		for(Field field : fields){
			/**
			 * TODO 这里需要有一个得到远程句柄的更可靠的办法
			 */
			if(field.getName().equals("h")){
				try {
					field.setAccessible(true);
					Requestor requestor = (Requestor)field.get(t);
					requestor.getTransceiver().close();
				} catch (IllegalArgumentException e) {
					log.error("Error when close connection remote protocol " + t.getClass().toString(), e);
					throw new RuntimeException("Error when close connection remote protocol " + t.getClass().toString(), e);
				} catch (IllegalAccessException e) {
					log.error("Error when close connection remote protocol " + t.getClass().toString(), e);
					throw new RuntimeException("Error when close connection remote protocol " + t.getClass().toString(), e);			
				} catch (IOException e) {
					log.error("Error when close connection remote protocol " + t.getClass().toString(), e);
					throw new RuntimeException("Error when close connection remote protocol " + t.getClass().toString(), e);	
				}
			}
		}
	}
	
	/**
	 * 试着从缓存中获取客户端对象
	 * @param serviceInfo
	 * @return
	 */
	private static<T> T getProtocolFromCache(ServiceInfo serviceInfo){
		ProtocolWrapper pw = cache.get(serviceInfo);
		if(null == pw){
			return null;
		}
		return (T)pw.getProtocol();
	}
}
