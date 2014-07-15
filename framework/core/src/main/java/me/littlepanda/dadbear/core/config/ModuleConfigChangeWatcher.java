package me.littlepanda.dadbear.core.config;

import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class ModuleConfigChangeWatcher implements Watcher {
	
	private Map<String, Configuration> conf_map = null;
	private Configuration conf;
	
	public ModuleConfigChangeWatcher(Map<String, Configuration> map, Configuration conf){
		this.conf_map = map;
		this.conf = conf;
	}

	/* (non-Javadoc)
	 * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
	 */
	@Override
	public void process(WatchedEvent event) {
		
		String path = event.getPath();
		//从事件路径中得出模块名
		String module = path.substring(path.lastIndexOf('/') + 1);
		//取得要修改的配置对象
		final Configuration changedConf = conf_map.get(module);
		
		UpdateConfig updateConfig = new UpdateConfig() {
			@Override
			public void updateConfig(ConfigInfo configInfo, String changedName) {
				changedConf.setProperty(changedName.toString(), configInfo.getValue().toString());
			}
			@Override
			public void removeConfig(String changedName) {
				changedConf.clearProperty(changedName.toString());
			}
		};
		ConfigurationUtil.updateModuleConfig(path , updateConfig, new ModuleConfigChangeWatcher(conf_map, conf), conf);
	}

}
