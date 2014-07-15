package me.littlepanda.dadbear.core.util;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import me.littlepanda.dadbear.core.config.Configured;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class DefaultWatcher extends Configured implements Watcher {

	@Override
	public void process(WatchedEvent event) {
		
	}
	
	public String toString(){
		return DefaultWatcher.class.getName();
	}
}
