package me.littlepanda.dadbear.core.queue;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class ZookeeperQueue<T> extends AbstractQueue<T> implements DistributedQueue<T> {
	
	private static Log log = LogFactory.getLog(ZookeeperQueue.class);
	
	private ZookeeperQueueHelper<T> zookeeperQueueHelper;
	
	public ZookeeperQueue() { }
	
	public ZookeeperQueue(String queue_name, Class<T> clazz) {
		zookeeperQueueHelper = new ZookeeperQueueHelper<T>(queue_name, clazz);
	}

	@Override
	public void init(String queue_name, Class<T> clazz) {
		zookeeperQueueHelper = new ZookeeperQueueHelper<T>(queue_name, clazz);
	}

	/* (non-Javadoc)
	 * @see java.util.Queue#offer(java.lang.Object)
	 */
	@Override
	public boolean offer(T e) {
		zookeeperQueueHelper.offer(e);
        return true;
	}

	/* (non-Javadoc)
	 * @see java.util.Queue#poll()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T poll() {
        int retry = 0;//删除znode重试次数
		while(true){
			Map<String, Object> map = zookeeperQueueHelper.getElementAndpath(3);
			if(map.get(ZookeeperQueueHelper.TEMP_ELEMENT_KEY) == null){
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
			return (T)map.get(ZookeeperQueueHelper.TEMP_ELEMENT_KEY); 
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Queue#peek()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T peek() {
		Map<String, Object> map = zookeeperQueueHelper.getElementAndpath(3);
		if(map.get(ZookeeperQueueHelper.TEMP_ELEMENT_KEY) == null){
			return null;
		} else {
			return (T) map.get(ZookeeperQueueHelper.TEMP_ELEMENT_KEY);
		}
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return zookeeperQueueHelper.size();
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractCollection#iterator()
	 */
	@Override
	@Deprecated
	public Iterator<T> iterator() {
		throw new RuntimeException("Not implemented method!");
	}

}
