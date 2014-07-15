package me.littlepanda.dadbear.core.schedule_with_queue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.littlepanda.dadbear.core.config.ConfigurationUtil;
import me.littlepanda.dadbear.core.queue.ZookeeperQueueHelper;
import me.littlepanda.dadbear.core.util.BytesUtil;
import me.littlepanda.dadbear.core.util.ZookeeperUtil;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
class ZookeeperQueueImpl<T> implements ZookeeperQueueInterface<T> {

	private Log log = LogFactory.getLog(ZookeeperQueueImpl.class);
	
	private ZookeeperQueueHelper<T> zookeeperQueueHelper;
	
	ZookeeperQueueImpl(String queue_name, Class<T> c) {
		zookeeperQueueHelper = new ZookeeperQueueHelper<T>(queue_name, c);
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.schedule_with_queue.ZookeeperQueueInterface#offer(java.lang.Object)
	 */
	@Override
	public String offer(T t) {
		String path = zookeeperQueueHelper.offer(t);
        return path.substring(path.lastIndexOf("/") + 1, path.length());
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.schedule_with_queue.ZookeeperQueueInterface#poll()
	 */
	@Override
	public Map<String, Object> poll() {
		int retry = 0;//删除znode重试次数
		while(true){
			Map<String, Object> map = zookeeperQueueHelper.getElementAndpath(3);
			if(map == null){
				return null;
			}
			try {
				if(zookeeperQueueHelper.getZu().exists(map.get(ZookeeperQueueHelper.TEMP_PATH_KEY).toString())){
					zookeeperQueueHelper.getZu().deleteNode(map.get(ZookeeperQueueHelper.TEMP_PATH_KEY).toString());
				} else {
					return null;
				}
            } catch(RuntimeException re){
            	if(retry > 2){
					return null;
				}
				retry++;
				continue;
            }
			String path = map.get(ZookeeperQueueHelper.TEMP_PATH_KEY).toString();
			String nodeName = path.substring(path.lastIndexOf("/") + 1, path.length());
			map.put(ZookeeperQueueHelper.TEMP_PATH_KEY, nodeName);
			return map; 
		}
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.schedule_with_queue.ZookeeperQueueInterface#peek()
	 */
	@Override
	public Map<String, Object> peek() {
		Map<String, Object> map = zookeeperQueueHelper.getElementAndpath(3);
		if(map.get(ZookeeperQueueHelper.TEMP_ELEMENT_KEY) == null){
			return null;
		} else {
			String path = map.get(ZookeeperQueueHelper.TEMP_PATH_KEY).toString();
			String nodeName = path.substring(path.lastIndexOf("/") + 1, path.length());
			map.put(ZookeeperQueueHelper.TEMP_PATH_KEY, nodeName);
			return map;
		}
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.schedule_with_queue.ZookeeperQueueInterface#size()
	 */
	@Override
	public int size() {
		return zookeeperQueueHelper.size();
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.schedule_with_queue.ZookeeperQueueInterface#remove(java.lang.String)
	 */
	@Override
	public boolean remove(String node_name) {
		String path = zookeeperQueueHelper.getQueuePath() + "/" + node_name;
		if(!zookeeperQueueHelper.getZu().exists(path)){
			return true;
		} else {
			zookeeperQueueHelper.getZu().deleteNode(path);
			return true;
		}
	}
	
	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.schedule_with_queue.ZookeeperQueueInterface#getQueueElements()
	 */
	@Override
	public Map<String, T> getQueueElements() {
		Map<String, T> retVal = Maps.newHashMap();
		List<String> paths = zookeeperQueueHelper.getZu().getChildren(zookeeperQueueHelper.getQueuePath());
		for(String path : paths){
			if(zookeeperQueueHelper.getZu().exists(path)){
				String nodeName = path.substring(path.lastIndexOf("/") + 1, path.length());
				byte[] data = zookeeperQueueHelper.getZu().getData(path);
				if(null == data || 0 == data.length){
					continue;
				}
				T t = BytesUtil.bytesToClass(data, ConfigurationUtil.getInitConfig(), zookeeperQueueHelper.getQueueType());
				retVal.put(nodeName, t);
			}
		}
		return retVal;
	}
	
	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.schedule_with_queue.ZookeeperQueueInterface#getQueueElementsByLimits(int)
	 */
	@Override
	public Map<String, T> getQueueElementsByLimits(int max) {
		Map<String, T> retVal = Maps.newHashMap();
		List<String> nodes = zookeeperQueueHelper.getZu().getChildren(zookeeperQueueHelper.getQueuePath());
		if(nodes.size() == 0) {
			return Maps.newHashMap();
		}
		Collections.sort(nodes);
		List<String> limitNodes = Lists.newArrayList();
		if(nodes.size() >= max){
			limitNodes = nodes.subList(0, max);
		} else {
			limitNodes = nodes;
		}
		for(String nodename : limitNodes){
			String path = zookeeperQueueHelper.getQueuePath() + "/" + nodename;
			if(zookeeperQueueHelper.getZu().exists(path)){
				byte[] data = zookeeperQueueHelper.getZu().getData(path);
				if(null == data || 0 == data.length){
					continue;
				}
				T t = BytesUtil.bytesToClass(data, ConfigurationUtil.getInitConfig(), zookeeperQueueHelper.getQueueType());
				retVal.put(nodename, t);
			}
		}
		return retVal;
	}
	
	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.schedule_with_queue.ZookeeperQueueInterface#getElementByName(java.lang.String)
	 */
	@Override
	public T getElementByName(String node_name) {
		String path = zookeeperQueueHelper.getQueuePath() + "/" + node_name;
		if(zookeeperQueueHelper.getZu().exists(path)){
			byte[] data = zookeeperQueueHelper.getZu().getData(path);
			if(null == data || 0 == data.length){
				return null;
			}
			return BytesUtil.bytesToClass(data, ConfigurationUtil.getInitConfig(), zookeeperQueueHelper.getQueueType());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.schedule_with_queue.ZookeeperQueueInterface#exists(java.lang.String)
	 */
	@Override
	public boolean exists(String node_name) {
		String path = zookeeperQueueHelper.getQueuePath() + "/" + node_name;
		if(zookeeperQueueHelper.getZu().exists(path)) {
			return true;
		}
		return false;
	}

}
