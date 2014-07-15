package me.littlepanda.dadbear.example.solr_manager;

import java.io.IOException;

import org.apache.commons.logging.Log;

import me.littlepanda.dadbear.core.annotations.ServiceLog;
import me.littlepanda.dadbear.core.config.ConfigurationUtil;
import me.littlepanda.dadbear.core.util.ZookeeperUtil;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class SolrManagerImpl implements SolrManager {

	@ServiceLog
	private Log log; 
	
	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.example.solr_manager.SolrManager#start()
	 */
	@Override
	public void start() {
		try {
			Runtime.getRuntime().exec("service jetty-solr start");
			log.info("service jetty-solr start!");
		} catch (IOException e) {
			log.error("Error when start jetty-solr using command 'service jetty-solr start'", e);
			throw new RuntimeException("Error when start jetty-solr using command 'service jetty-solr start'", e);
		}
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.example.solr_manager.SolrManager#stop()
	 */
	@Override
	public void stop() {
		try {
			Runtime.getRuntime().exec("service jetty-solr stop");
			log.info("service jetty-solr stop!");
		} catch (IOException e) {
			log.error("Error when stop jetty-solr using command 'service jetty-solr stop'", e);
			throw new RuntimeException("Error when stop jetty-solr using command 'service jetty-solr stop'", e);
		}
	
	}
	
	public static void main(String[] args) {
		ZookeeperUtil zu = ZookeeperUtil.get(ConfigurationUtil.getInitConfig());
		System.out.println(zu.exists("/distribute-compute/queue"));
	}

}
