package me.littlepanda.dadbear.core.config;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.Watcher;

import me.littlepanda.dadbear.core.cluster.ClusterNodeType;
import me.littlepanda.dadbear.core.cluster.ClusterUtil;
import me.littlepanda.dadbear.core.cluster.ZnodesPath;
import me.littlepanda.dadbear.core.util.BytesUtil;
import me.littlepanda.dadbear.core.util.Util;
import me.littlepanda.dadbear.core.util.ZookeeperUtil;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class ConfigurationUtil {
	
	private static Log log = LogFactory.getLog(ConfigurationUtil.class);
	
	private static Configuration init_config;
	private static ZookeeperUtil zu;
	private static ConcurrentHashMap<String, Configuration> config_maps = new ConcurrentHashMap<String, Configuration>();
	private static ZnodesPath zp;
	
	static {
		try {
			init_config = new PropertiesConfiguration("init.properties");
			zp = ClusterUtil.getZnonesPath();
			zu = ZookeeperUtil.get(init_config);
		} catch (ConfigurationException e) {
			throw new RuntimeException("error when init init.properties.", e);
		}
	}
	
	/**
	 * 配置节点名称
	 */
	public static final String COMMON_MODULE = "common";
	public static final String MASTER_MODULE = ClusterNodeType.master.toString();
	public static final String SLAVE_MODULE = ClusterNodeType.slave.toString();
	public static final String CLIENT_MODULE = ClusterNodeType.client.toString();
	
	/**
	 * ConfigInfo 类的四个属性名
	 */
	public static final String NAME = "name";
	public static final String VALUE = "value";
	public static final String TYPE = "type";
	public static final String DESC = "description";
	
	/**
	 * 初始化配置<br/>
	 * @param conf
	 */
	public static ConcurrentHashMap<String, Configuration> initNodeConfig(ClusterNodeType type){
		config_maps.clear();
		
		String moduleName = type.toString();
		//初始化common模块和节点类型名称相同的模块
		config_maps.put(COMMON_MODULE, getModuleConfigs(COMMON_MODULE));
		config_maps.put(moduleName, getModuleConfigs(moduleName));
		//初始化额外的模块
		List<Object> extraModules = config_maps.get(moduleName).getList(CommonConfigurationKeys.EXTRA_CONFIG_MODULE);
		for(Object module : extraModules){
			if(Util.empty(module.toString())){
				continue;
			}
			Configuration conf = getModuleConfigs(module.toString());
			config_maps.put(module.toString(), conf);
		}
		return config_maps;
	}
	
	
	/**
	 * 根据模块名称，取得模块配置对象
	 * @param module
	 * @return
	 */
	public static Configuration getModuleConfigs(String module){
		if(config_maps.containsKey(module)){
			return config_maps.get(module);
		}
		//建立一个空配置对象
		Configuration conf = new XMLConfiguration();
		//配置模块全路径
		String module_path =  zp.getModulePath(module);
		//其下所有配置节点
		List<String> module_config_nodes = zu.getChildren(module_path);
		//依次取得所有配置节点并添加进模块配置
		for(String config_node : module_config_nodes){
			ConfigInfo configInfo = BytesUtil.bytesToClass(zu.getData(zp.getItemPath(module, config_node)), init_config, ConfigInfo.class);
			conf.addProperty(configInfo.getName().toString(), configInfo.getValue().toString());
		}
		config_maps.put(module, conf);
		return conf;
	}
	
	/**
	 * 获取Master节点相关配置
	 * @return
	 */
	public static Configuration getMasterConfig(){
		return getModuleConfigs(MASTER_MODULE);
	}
	
	/**
	 * 获取Slave节点相关配置
	 * @return
	 */
	public static Configuration getSlaveConfig(){
		return getModuleConfigs(SLAVE_MODULE);
	}
	
	/**
	 * 获取Client节点相关配置
	 * @return
	 */
	public static Configuration getClientConfig(){
		return getModuleConfigs(CLIENT_MODULE);
	}
	
	/**
	 * 获取初始化配置，初始化配置一直存在于“init.properties”文件中
	 * @return
	 */
	public static Configuration getCommonConfig(){
		return getModuleConfigs(COMMON_MODULE);
	}
	
	/**
	 * 获取某特定服务相关配置
	 * @param serviceName
	 * @return
	 */
	public static Configuration getServiceConfig(String serviceName){
		return getModuleConfigs(serviceName);
	}
	
	public static Map<String, Configuration> getNodeConfig(){
		return config_maps;
	}
	
	/**
	 * 将配置保存到本地，只有master节点需要调用
	 */
	public void persistConfig(){
		//TODO 持久化配置到本地
	}
	
	/**
	 * 更新配置项
	 * 应该只能通过leader master修改配置项
	 * @param name
	 */
	public void updateConfigToZookeeper(String module, String name, Object value){
		if(value instanceof List){
			@SuppressWarnings("unchecked")
			List<Object> values = (List<Object>)value;
			String real_value = "";
			for(Object o : values){
				if(!(o instanceof String)){
					throw new RuntimeException("if config's type is list, it's value must be String.");
				}
				real_value += o.toString();
			}
			real_value = real_value.substring(0, real_value.length() - 1);
		}
		//TODO 当module节点状态为INIT时，不允许修改配置值
		//TODO 此方法未完成
	}


	/**
	 * 获取初始化配置，这是包含在“init.properties”文件中的配置
	 * @return
	 */
	public static Configuration getInitConfig() {
		return init_config;
	}
	
	/**
	 * 更新模块配置信息
	 *  1、如果节点不存在就在本地删除配置
	 *  2、如果节点还存在就用新值代替旧值
	 * @param path
	 * @param updateConfig
	 * @param watcher
	 */
	public static final void updateModuleConfig(String modulePath, UpdateConfig updateConfig, Watcher watcher, Configuration conf){
		//从事件路径中得出模块名
		String module = modulePath.substring(modulePath.lastIndexOf('/')+1);
		//得到要修改的配置列表，并重新注册配置变更监视器
		byte[] bytesConfigState = zu.getData(modulePath, watcher);
		ConfigState configState = BytesUtil.bytesToClass(bytesConfigState, conf, ConfigState.class);
		if(configState.getState() == ConfigInitState.INIT){
			return;
		}
		List<CharSequence> changed = configState.getChanged();
		//依次修改配置项
		for(CharSequence changedName : changed){
			String configPath = modulePath + "/" + changedName;
			if(!zu.exists(configPath)){
				updateConfig.removeConfig(changedName.toString());
			} else {
				byte[] bytesConfigInfo = zu.getData(configPath);
				ConfigInfo configInfo = BytesUtil.bytesToClass(bytesConfigInfo, conf, ConfigInfo.class);
				if(changedName.equals(configInfo.getName())){
					updateConfig.updateConfig(configInfo, changedName.toString());
				} else {
					log.info("config module " + module + " has not item named " + changedName.toString());
				}
			}
		}
	}
	
}
