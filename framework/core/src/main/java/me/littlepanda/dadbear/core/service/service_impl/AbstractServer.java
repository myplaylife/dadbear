package me.littlepanda.dadbear.core.service.service_impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import me.littlepanda.dadbear.core.annotations.Config;
import me.littlepanda.dadbear.core.annotations.InitMethod;
import me.littlepanda.dadbear.core.annotations.ServiceLog;
import me.littlepanda.dadbear.core.config.CommonConfigurationKeys;
import me.littlepanda.dadbear.core.config.Configured;
import me.littlepanda.dadbear.core.service.Server;
import me.littlepanda.dadbear.core.service.ServiceConstants;
import me.littlepanda.dadbear.core.util.ReflectionUtils;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
abstract public class AbstractServer extends Configured implements Server {
	
	private static Log log = LogFactory.getLog(AbstractServer.class);
	
	protected boolean alive = false;

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.service.Service#getType()
	 */
	@Override
	public String getType() {
		return getConf().getString(ServiceConstants.CONFIG_TYPE);
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.service.Service#getProtocol()
	 */
	@Override
	public String getProtocol() {
		return getConf().getString(ServiceConstants.CONFIG_PROTOCOL);
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.service.Service#getImplement()
	 */
	@Override
	public String getImplement() {
		return getConf().getString(ServiceConstants.CONFIG_IMPLEMENTATION);
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.service.Service#getPort()
	 */
	@Override
	public int getPort() {
		if(getType().equals(ServiceConstants.SERVICE_TYPE_OSGI) || getType().equals(ServiceConstants.SERVICE_TYPE_SCHEDULE)){
			return -1;
		}
		return getConf().getInt(ServiceConstants.CONFIG_PORT);
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.service.Service#getSchedule()
	 */
	@Override
	public String getSchedule() {
		if(!getType().equals(ServiceConstants.SERVICE_TYPE_SCHEDULE)){
			return null;
		}
		return getConf().getString(ServiceConstants.CONFIG_SCHEDULE);
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.service.Service#isAlive()
	 */
	@Override
	public boolean isAlive() {
		return alive;
	}
	
	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.service.Service#setAlive(boolean)
	 */
	@Override
	public void setAlive(boolean alive) {
		this.alive = alive;
	}
	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.service.Service#getInvokeType()
	 */
	@Override
	public String getDistributeAlgo() {
		if(getType().equals(ServiceConstants.SERVICE_TYPE_SCHEDULE) || getType().equals(ServiceConstants.SERVICE_TYPE_OSGI)){
			return null;
		}
		return getConf().getString(ServiceConstants.CONFIG_DISTRIBUTE_ALGO_TYPE);
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.service.Service#start()
	 */
	@Override
	public void start() {
		final Object obj = ReflectionUtils.newInstance(getImplement(), getConf()); 
		/*
		 * 为服务设置配置对象
		 */
		List<Field> fields = ReflectionUtils.getDeclaredFields(obj.getClass());
		for(Field field : fields){
			/*
			 * 注入相应服务的配置
			 */
			if(field.isAnnotationPresent(Config.class)){
				try {
					field.setAccessible(true);
					if(!field.getType().getName().equals(Configuration.class.getName())){
						log.error("Field with annotation Config must be " + Configuration.class.getName());
						throw new RuntimeException("Field with annotation Config must be " + Configuration.class.getName());
					}
					field.set(obj, getConf());
				} catch (IllegalArgumentException e) {
					log.error("Illegal Argument Exception when set configuration object to service " + getProtocol(), e);
					throw new RuntimeException("Illegal Argument Exception when iset configuration object to service " + getProtocol(), e);
				} catch (IllegalAccessException e) {
					log.error("can't access field when set configuration object to service " + getProtocol(), e);
					throw new RuntimeException("can't access field when set configuration object to service " + getProtocol(), e);
				}
			}
			/*
			 * 日志句柄注入
			 */
			if(field.isAnnotationPresent(ServiceLog.class)){
				try {
					field.setAccessible(true);
					if(!field.getType().getName().equals(Log.class.getName())){
						log.error("Field with annotation Config must be " + Log.class.getName());
						throw new RuntimeException("Field with annotation Config must be " + Configuration.class.getName());
					}
					field.set(obj, me.littlepanda.dadbear.core.log.LogFactory.getLog(getLogName()));
				} catch (IllegalArgumentException e) {
					log.error("Illegal Argument Exception when set LOG HANDLER to service " + getProtocol(), e);
					throw new RuntimeException("Illegal Argument Exception when set LOG HANDLER to service " + getProtocol(), e);
				} catch (IllegalAccessException e) {
					log.error("can't access field when set LOG HANDLER to service " + getProtocol(), e);
					throw new RuntimeException("can't access field when set LOG HANDLER to service " + getProtocol(), e);
				}
			}
		}
		
		List<Method> methods = ReflectionUtils.getDeclaredMethods(obj.getClass());
		for(Method method : methods){
			if(method.isAnnotationPresent(InitMethod.class)){
				try {
					method.setAccessible(true);
					method.invoke(obj);
					break;
				} catch (IllegalArgumentException e) {
					log.error("Illegal Argument Exception when invoke schedule service's init method.", e);
					throw new RuntimeException("Illegal Argument Exception when invoke schedule service's init method.", e);
				} catch (IllegalAccessException e) {
					log.error("can't access method when invoke scheduler service's init method", e);
					throw new RuntimeException("can't access method when invoke scheduler service's init method", e);
				} catch (InvocationTargetException e) {
					log.error("when invoke scheduler service's init method, InvocationTargetException arise.", e);
					throw new RuntimeException("when invoke scheduler service's init method, InvocationTargetException arise.", e);
				}
			}
		}
		startServer(obj);
		setAlive(true);
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.service.Service#stop()
	 */
	@Override
	public void stop() {
		try {
			stopServer();
		} finally {
			setAlive(false);
		}
	}
	
	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.service.Service#getDecription()
	 */
	@Override
	public String getDescription(){
		return getConf().getString(ServiceConstants.CONFIG_DESC);
	}
	
	public String getLogName() {
		return getConf().getString(ServiceConstants.LOG_NAME);
	}
	
	abstract protected void startServer(Object obj);
	abstract protected void stopServer();
}
