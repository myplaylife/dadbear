package me.littlepanda.dadbear.core.service;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class ServiceConstants {
	/**
	 * <p>服务类型：</p>
	 * <p>avdl远程服务，只有框架本身使用这种方式，如果了解avro编程，用户也是可以使用的。</p>
	 * <p>这种方式能提供更灵活的序列化方式</p>
	 */
	public static final String SERVICE_TYPE_REMOTE_AVDL = "remote_avdl";
	/**
	 * <p>服务类型 - 远程服务</p>
	 */
	public static final String SERVICE_TYPE_REMOTE = "remote";
	/**
	 * <p>服务类型 - 定时服务</p>
	 */
	public static final String SERVICE_TYPE_SCHEDULE = "scheduler";
	/**
	 * <p>服务类型 - osgi普通服务</p>
	 */
	public static final String SERVICE_TYPE_OSGI = "osgi";
	/**
	 * <p>服务类型 - osgi远程服务</p>
	 */
	public static final String SERVICE_TYPE_OSGI_REMOTE = "remote_osgi";
	
	/**
	 * <p>配置项 - 协议</p>
	 */
	public static final String CONFIG_PROTOCOL = "protocol";
	/**
	 * <p>配置项 - 实现类 </p>
	 */
	public static final String CONFIG_IMPLEMENTATION = "implementation";
	/**
	 * <p>配置项 - 端口号</p>
	 */
	public static final String CONFIG_PORT = "port";
	/**
	 * <p>配置项 - 调度串</p>
	 */
	public static final String CONFIG_SCHEDULE = "schedule";
	/**
	 * <p>配置项 - 服务类型</p>
	 */
	public static final String CONFIG_TYPE = "type";
	/**
	 * <p>配置项 - 调用类型</p>
	 */
	public static final String CONFIG_DISTRIBUTE_ALGO_TYPE = "distribute_algo";
	/**
	 * <p>配置项 - 描述</p>
	 */
	public static final String CONFIG_DESC = "description";
	/**
	 * <p>分发算法类型 - 循环</p>
	 */
	public static final String DISTRIBUTE_ALGO_TYPE_ROTATION = "rotation";
	
	/**
	 * <p>日志名称</p>
	 */
	public static final String LOG_NAME = "log_name";
	
} 
