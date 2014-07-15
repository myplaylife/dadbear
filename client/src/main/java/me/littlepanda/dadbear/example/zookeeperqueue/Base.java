package me.littlepanda.dadbear.example.zookeeperqueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import me.littlepanda.dadbear.core.cluster.ClusterUtil;
import me.littlepanda.dadbear.core.cluster.ZnodesPath;
import me.littlepanda.dadbear.core.config.ConfigurationUtil;
import me.littlepanda.dadbear.core.util.ZookeeperUtil;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class Base {
	
	protected static Log log = LogFactory.getLog(Base.class);
	
	protected static ZookeeperUtil zu;
	protected static ZnodesPath zp;
	
	static {
		zu = ZookeeperUtil.get(ConfigurationUtil.getInitConfig());
		zp = ClusterUtil.getZnonesPath();
	}
}
