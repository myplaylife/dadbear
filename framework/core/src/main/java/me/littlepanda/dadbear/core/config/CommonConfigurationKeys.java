package me.littlepanda.dadbear.core.config;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class CommonConfigurationKeys {
	
	/*
	 * 初始化配置文件名称 
	 */
	public static final String DEFAULT_INIT_CONFIG_FILE_NAME = "init.properties";
	
	/*
	 * 配置文件列表
	 */
	public static final String CONFIG_FILE_LIST = "configuration.file.list";
	
	/*
	 * Zookeeper 服务器列表
	 */
	public static final String ZOOKEEPER_SERVER_LIST = "zookeeper.server";
	
	/*
	 * Zookeeper session超时时间 
	 */
	public static final String ZOOKEEPER_SESSION_TIMEOUT = "zookeeper.session_timeout";
	
	/*
	 * Zookeeper 默认监视器
	 */
	public static final String ZOOKEEPER_DEFAULT_WATCHER = "zookeeper.default_watcher";
	
	/*
	 * 默认序列化实现类
	 */
	public static final String IO_SERIALIZATIONS_KEY = "io.serialization";
	
	/*
	 * Zookeeper 中的网络视图节点
	 */
	public static final String ZNODE_ROOT = "znode.root";
	public static final String ZNODE_MASTER = "znode.master";
	public static final String ZNODE_SLAVE = "znode.slave";
	public static final String ZNODE_MASTER_ELECTION= "znode.master.election";
	public static final String ZNODE_SLAVE_REGISTER = "znode.slave.register";
	public static final String ZNODE_CONFIG = "znode.config";
	public static final String ZNODE_QUEUE = "znode.queue";
	
	/*
	 * client-master 协议实现类 和 服务端口号
	 */
	public static final String CLIENT_MASTER_PROTOCOL = "client_master_protocol";
	public static final String SLAVE_MASTER_PROTOCOL = "slave_master_protocol";
	
	/*
	 * 除了各节点的配置模块和common配置外模块，各节点还需要加载的配置模块
	 */
	public static final String EXTRA_CONFIG_MODULE = "extra.modules";
	
	/*
	 * 日志相关配置
	 */
	public static final String LOG_LEVEL = "log.level";
	public static final String LOG_APPENDER = "log.appender";
	public static final String LOG_ROOT_PATH = "log.root_path";
	public static final String LOG_MAXIMUM_FILESIZE = "log.maximumFileSize";
	public static final String LOG_BACKUPS = "log.maxSizeRollBackups";
	public static final String LOG_LAYOUT = "log.layout";
	public static final String LOG_CONVERSIONPATTERN = "log.conversionpattern";
	public static final String LOG_FILE_NAME = "log.file_name";
}
