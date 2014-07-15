package me.littlepanda.dadbear.master;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import me.littlepanda.dadbear.core.cluster.ZnodesPath;
import me.littlepanda.dadbear.core.config.CommonConfigurationKeys;
import me.littlepanda.dadbear.core.config.ConfigInfo;
import me.littlepanda.dadbear.core.config.ConfigurationUtil;
import me.littlepanda.dadbear.core.service.ServiceConstants;
import me.littlepanda.dadbear.core.util.BytesUtil;
import me.littlepanda.dadbear.core.util.ZookeeperUtil;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class ConfigurationManage {
	
	/**
	 *  master节点在本地的集群配置缓存，这个缓存在每个master节点上都是一样的。在leader切换时，用来更新zookeeper上的集群配置
	 */
	private static TreeMultimap<String, ConfigInfo> all_config = null;
	
	private static Configuration init_conf;
	
	private static ZookeeperUtil zu;
	
	/*
	 * 配置初始化状态，标识all_configs变量的状态
	 * 	0 - 刚启动，还没有初始化
	 *  1 - 已初始化完毕
	 */
	public static final int CONFIG_LOAD_STATE_NO = 0;
	public static final int CONFIG_LOAD_STATE_YES = 1;
	public static int cluster_config_load_state = CONFIG_LOAD_STATE_NO;
	
	static {
		init_conf = ConfigurationUtil.getInitConfig();
		zu = ZookeeperUtil.get(init_conf);
	}

	/**
	 * 向zookeeper中放入集群配置
	 */
	public static void initClusterConfig(){
		
		if(cluster_config_load_state == CONFIG_LOAD_STATE_NO) {
			String[] config_list = init_conf.getStringArray(CommonConfigurationKeys.CONFIG_FILE_LIST);
			all_config = getAllConfigsFromFile(config_list);
		}
		cluster_config_load_state = CONFIG_LOAD_STATE_YES;
		//在初始化配置前，先创建集群管理根节点和配置根节点
		String rootPath = init_conf.getString(CommonConfigurationKeys.ZNODE_ROOT);
		String configRootPath = rootPath + init_conf.getString(CommonConfigurationKeys.ZNODE_CONFIG);
		
		//将本地所有配置全部写入zookeeper
		for(String module : all_config.keySet()){
			Collection<ConfigInfo> configCollection = all_config.get(module);
			String modulePath = configRootPath + "/" + module;
			zu.createPersistNode(modulePath);
			for(ConfigInfo ci : configCollection){
				String configPath = modulePath + "/" + ci.getName();
				byte[] configContent = BytesUtil.classToBytes(ci, init_conf);
				zu.createEphemeralNode(configPath, configContent);
			}
		}
	}
	
	/**
	 * 通过配置文件取得所有配置信息
	 * 这个过程只在active master节点走一遍，以后就不再走了
	 * @return
	 */
	private static TreeMultimap<String, ConfigInfo> getAllConfigsFromFile(String[] config_list){
		
		TreeMultimap<String, ConfigInfo> map = TreeMultimap.create();
		
		XMLConfiguration conf = new XMLConfiguration();
		try {
			for(String fileName : config_list){
				conf.append(new XMLConfiguration(fileName));
			}
		} catch (ConfigurationException e) {
			throw new RuntimeException("error when init configuration.", e);
		}
		ConfigurationNode root = conf.getRootNode();
		List<ConfigurationNode> modules = root.getChildren();
		
		for(ConfigurationNode module : modules){
			List<ConfigurationNode> items = module.getChildren();
			for(ConfigurationNode item : items){
				List<ConfigurationNode> itemName = item.getChildren(ConfigurationUtil.NAME);
				List<ConfigurationNode> itemValue = item.getChildren(ConfigurationUtil.VALUE);
				List<ConfigurationNode> itemType = item.getChildren(ConfigurationUtil.TYPE);
				List<ConfigurationNode> itemDesc = item.getChildren(ConfigurationUtil.DESC);
				for(int i=0; i<itemName.size(); i++){
					ConfigInfo configInfo = new ConfigInfo();
					configInfo.setName(itemName.get(i).getValue().toString());
					configInfo.setValue(itemValue.get(i).getValue().toString());
					configInfo.setType(itemType.get(i).getValue().toString());
					configInfo.setDescription(itemDesc.get(i).getValue().toString());
					map.put(module.getName(), configInfo);
				}
			}
			ConfigInfo configInfo = new ConfigInfo();
			configInfo.setName(ServiceConstants.LOG_NAME);
			configInfo.setValue(module.getName());
			configInfo.setType("String");
			configInfo.setDescription("配置模块名称");
			map.put(module.getName(), configInfo);
		}
		Iterator<String> init_keys = init_conf.getKeys();
		while(init_keys.hasNext()){
			String key = init_keys.next();
			ConfigInfo configInfo = new ConfigInfo();
			configInfo.setName(key);
			configInfo.setValue(init_conf.getString(key));
			configInfo.setType("string");
			configInfo.setDescription("初始化配置：" + key);
			map.put(ConfigurationUtil.COMMON_MODULE, configInfo);
		}
		return map;
	}
	
	/**
	 * wait master节点加载zookeeper上的配置到本地
	 * @param znodesPath
	 */
	public static synchronized Multimap<String, ConfigInfo> getAllConfigsFromZookeeper(ZnodesPath znodesPath){
		//如果是主线程，就不会再到zookeeper中加载变量
		if(cluster_config_load_state == CONFIG_LOAD_STATE_YES){
			return all_config;
		}
		TreeMultimap<String, ConfigInfo> temp = TreeMultimap.create();
		List<String> modules = zu.getChildren(znodesPath.getConfig());
		for(String module : modules){
			List<String> configs = zu.getChildren(znodesPath.getModulePath(module));
			for(String config : configs){
				ConfigInfo configInfo = BytesUtil.bytesToClass(zu.getData(znodesPath.getItemPath(module, config)), init_conf, ConfigInfo.class);
				temp.put(module, configInfo);
			}
		}
		all_config = temp;
		cluster_config_load_state = CONFIG_LOAD_STATE_YES;
		return all_config;
	}
	
	/**
	 * 将配置持久化到本地
	 */
	public static void persistConfigToFile(){
		
	}
}
