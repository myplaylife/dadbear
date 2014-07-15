package me.littlepanda.dadbear.core.schedule_with_queue;

import java.util.Date;
import java.util.Map;

import javax.lang.model.element.Element;

import org.codehaus.jackson.map.SerializerFactory.Config;

import me.littlepanda.dadbear.core.config.ConfigurationUtil;
import me.littlepanda.dadbear.core.queue.ZookeeperQueueHelper;
import me.littlepanda.dadbear.core.util.BytesUtil;
import me.littlepanda.dadbear.core.util.ReflectionUtils;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class QueueSchedulerHelper {
	
	private boolean pollFromQueue = true;
	
	private String queue_type;
	
	public static final String QUEUE_EXECUTING = "ZXhlY3V0aW5n";
	public static final String QUEUE_RETRY = "cmV0cnk=";
	public static final String QUEUE_ERROR = "ZXJyb3I=";
	
	private ZookeeperQueueInterface<Object> queue;
	private ZookeeperQueueInterface<Object> queue_error;
	private ZookeeperQueueInterface<ZookeeperQueueEntityWrapper> queue_executing;
	private ZookeeperQueueInterface<ZookeeperQueueEntityWrapper> queue_retry;
	
	public QueueSchedulerHelper(String queue_name, String queue_type){
		this.queue_type = queue_type;
		Class<Object> clazz = ReflectionUtils.getClass(this.queue_type);
		this.queue = new ZookeeperQueueImpl<Object>(queue_name, clazz);
		this.queue_error = new ZookeeperQueueImpl<Object>(queue_name + "_" + QUEUE_ERROR, clazz);
		this.queue_executing = new ZookeeperQueueImpl<ZookeeperQueueEntityWrapper>(queue_name + "_" + QUEUE_EXECUTING, ZookeeperQueueEntityWrapper.class);
		this.queue_retry = new ZookeeperQueueImpl<ZookeeperQueueEntityWrapper>(queue_name + "_" + QUEUE_RETRY, ZookeeperQueueEntityWrapper.class);
	}
	
	/**
	 * <p>取出执行用户操作所使用的元素</p>
	 * <p>会轮流从正确队列和重试队列中取</p>
	 * @return
	 */
	public ZookeeperQueueEntityWrapper poll(){
		ZookeeperQueueEntityWrapper wrapper = null;
		Map<String, Object> map;
		pollFromQueue = !pollFromQueue;
		if(pollFromQueue) {
			wrapper = new ZookeeperQueueEntityWrapper();
			map = queue.poll();
			if(map == null){
				return null;
			}
			String nodeName = map.get(ZookeeperQueueHelper.TEMP_PATH_KEY).toString();
			Object o = map.get(ZookeeperQueueHelper.TEMP_ELEMENT_KEY);
			wrapper.setElement(BytesUtil.classToBytes(o, ConfigurationUtil.getInitConfig()));
			wrapper.setNode_name(nodeName);
			wrapper.setRetry_times(0);
			wrapper.setElement_type(this.queue_type);
		} else {
			map = queue_retry.poll();
			if(map == null){
				return null;
			}
			wrapper = (ZookeeperQueueEntityWrapper) map.get(ZookeeperQueueHelper.TEMP_ELEMENT_KEY);
			wrapper.setRetry_times(wrapper.getRetry_times() + 1);
			wrapper.setNode_name(map.get(ZookeeperQueueHelper.TEMP_PATH_KEY).toString());
		}
		return wrapper;
	}
	
	/**
	 * <p>如果原始队列和重试队列都为空，就返回true，否则返回false。</p>
	 * @return
	 */
	public boolean isNeedSleep() {
		if(queue.size() == 0 && queue_retry.size() == 0){
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * <p>在执行用户操作前执行</p>
	 * @param wrapper
	 * @return
	 */
	public boolean pre_execute(ZookeeperQueueEntityWrapper wrapper) {
		wrapper.setStart_time(new Date());
		String nodeName = queue_executing.offer(wrapper);
		wrapper.setNode_name(nodeName);
		return true;
	}
	
	/**
	 * <p>正确执行用户操作时执行</p>
	 * @param wrapper
	 * @return
	 */
	public boolean after_execute(ZookeeperQueueEntityWrapper wrapper) {
		return queue_executing.remove(wrapper.getNode_name());
	}
	
	/**
	 * <p>执行用户操作异常时执行</p>
	 * @param wrapper
	 * @param max_retry
	 */
	public void exception(ZookeeperQueueEntityWrapper wrapper, int max_retry) {
		if(null == wrapper) {
			throw new RuntimeException("Poll element from ZookeeperQueue failed.");
		}
		if(!queue_executing.remove(wrapper.getNode_name())){
			throw new RuntimeException("Error when remove element from executing queue with " + wrapper.getNode_name());
		}
		if(wrapper.getRetry_times() >= max_retry) {
			queue_error.offer(wrapper.getElement());
		} else {
			queue_retry.offer(wrapper);
		}
	}

	/**
	 * 获取正常队列元素个数
	 * @return
	 */
	public long getCountNormal() {
		return this.queue.size();
	}

	/**
	 * 获取错误队列元素个数
	 * @return
	 */
	public long getCountError() {
		return this.queue_error.size();
	}

	/**
	 * 获取重试队列元素个数
	 * @return
	 */
	public long getCountRetry() {
		return this.queue_retry.size();
	}

	/**
	 * 获取正在执行队列个数
	 * @return
	 */
	public long getCountExecuting() {
		return this.queue_executing.size();
	}
	
	/**
	 * @return 初始状态元素列表
	 */
	public Map<String, Object> getInitStateElements(int limit) {
		return queue.getQueueElementsByLimits(limit);
	}
	public Map<String, ZookeeperQueueEntityWrapper> getRetryStateElements(int limit) {
		return queue_retry.getQueueElementsByLimits(limit);
	}
	public Map<String, ZookeeperQueueEntityWrapper> getExecutingStateElements(int limit) {
		return queue_executing.getQueueElementsByLimits(limit);
	}
	public Map<String, Object> getErrorStateElements(int limit) {
		return queue_error.getQueueElementsByLimits(limit);
	}

	/**
	 * @param id
	 * @return 根据id返回一个初始元素对象
	 */
	public Object getInitElementByName(String name) {
		return queue.getElementByName(name);
	}
	public Object getErrorElementByName(String name) {
		return queue_error.getElementByName(name);
	}
	public ZookeeperQueueEntityWrapper getRetryElementByName(String name) {
		return queue_retry.getElementByName(name);
	}
	public ZookeeperQueueEntityWrapper getExecutingElementByName(String name) {
		return queue_executing.getElementByName(name);
	}

	/**
	 * <p>将元素转换状态</p>
	 * @param name
	 */
	public boolean moveErrorToInit(String name) {
		if(queue_error.exists(name)) {
			Object o = queue_error.getElementByName(name);
			queue.offer(o);
			queue_error.remove(name);
			return true;
		} else {
			throw new RuntimeException("Can't move element " + name + " in ERROR state.It isn't exists.");
		}
	}
	public boolean moveExecutingToInit(String name) {
		if(queue_executing.exists(name)) {
			ZookeeperQueueEntityWrapper zq = queue_executing.getElementByName(name);
			Object o = BytesUtil.bytesToClass(zq.getElement(), ConfigurationUtil.getInitConfig(), ReflectionUtils.getClass(this.queue_type));
			queue.offer(o);
			queue_executing.remove(name);
			return true;
		} else {
			throw new RuntimeException("Can't move element " + name + " in EXECUTING state.It isn't exists.");
		}
	}
}
