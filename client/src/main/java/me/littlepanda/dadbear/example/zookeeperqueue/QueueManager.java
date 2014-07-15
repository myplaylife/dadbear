package me.littlepanda.dadbear.example.zookeeperqueue;

import java.util.List;
import java.util.Map;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.littlepanda.dadbear.client.Client;
import me.littlepanda.dadbear.core.rpc.SlaveInfo;
import me.littlepanda.dadbear.core.schedule_with_queue.QueueSchedulerHelper;

/**
 * @author 张静波 myplaylife@icloud.com
 *         <p>
 *         只操作已存在的队列
 *         </p>
 */
public class QueueManager extends Base {
	
	private static Map<String, QueueObject> cache = Maps.newHashMap();
	
	static {
		refreshQueue();
	}

	/**
	 * @return 队列名称与队列管理对象的映射集合
	 */
	public static Map<String, QueueObject> getQueueList() {
		return cache;
	}
	
	/**
	 * <p>获取主机列表</p>
	 * @return
	 */
	public static List<String> getHosts() {
		List<SlaveInfo> slaves = Client.getAllSlaveInfo();
		List<String> hosts = Lists.newArrayList();
		for(SlaveInfo slave : slaves) {
			hosts.add(slave.getHost().toString());
		}
		return hosts;
	}

	public static List<String> getLogList(String hostname) {
		return null;
	}

	public static byte[] getLogByHostnameAndName(String hostname, String name) {
		return null;
	}
	
	/**
	 * @return 所有队列的名称
	 */
	private static List<String> getQueueNameList() {
		List<String> queueNames = zu.getChildren(zp.getQueue(), new QueueChangeWatcher());
		List<String> removeList = Lists.newArrayList();
		for (String id : queueNames) {
			if (id.endsWith(QueueSchedulerHelper.QUEUE_ERROR)
					|| id.endsWith(QueueSchedulerHelper.QUEUE_EXECUTING)
					|| id.endsWith(QueueSchedulerHelper.QUEUE_RETRY)) {
				removeList.add(id);
			}
		}
		queueNames.removeAll(removeList);
		return queueNames;
	}

	private static QueueObject getQueueByName(String name) {
		String queueType = new String(zu.getData(zp.getQueue() + "/" + name));
		return new QueueObject(name, queueType);
	}
	
	static class QueueChangeWatcher implements Watcher{

		@Override
		public synchronized void process(WatchedEvent event) {
			refreshQueue();
		}
	}
	
	private synchronized static void refreshQueue() {
		List<String> names = getQueueNameList();
		cache.clear();
		for (String name : names) {
			cache.put(name, getQueueByName(name));
		}
	}

}
