package me.littlepanda.dadbear.example.zookeeperqueue;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import me.littlepanda.dadbear.core.config.ConfigurationUtil;
import me.littlepanda.dadbear.core.schedule_with_queue.QueueSchedulerHelper;
import me.littlepanda.dadbear.core.schedule_with_queue.ZookeeperQueueEntityWrapper;
import me.littlepanda.dadbear.core.util.BytesUtil;
import me.littlepanda.dadbear.core.util.ReflectionUtils;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class QueueObject extends Base {
	
	private QueueSchedulerHelper queueSchedulerHelper;
	
	public static final int NORMAL = 0;
	public static final int EXECUTING = 1;
	public static final int RETRY = 2;
	public static final int ERROR = 3;
	
	public QueueObject(String name, String type) {
		this.name = name;
		this.type = type;
		this.queueSchedulerHelper = new QueueSchedulerHelper(name, type);
	}
	
	private String name;
	
	private String type;
	
	/**
	 * <p>因为分布式队列每时每刻队列中元素都在不停变化，而得到所有四个队列的数据的方法之间有很小的差别，
	 *    在这极短的时间内，队列的状态都有可能产生变化，因此不单独给出获取每种状态数量的方法，而只给出
	 *    一个数量对象来表示</p>
	 */
	public CountObject getCountObject() {
		return new CountObject(this.queueSchedulerHelper.getCountNormal(), 
				this.queueSchedulerHelper.getCountError(), 
				this.queueSchedulerHelper.getCountRetry(), 
				this.queueSchedulerHelper.getCountExecuting());
	}
	
//	public List<ElementObject> getElements() {
//		List<ElementObject> elements = Lists.newArrayList();
//		elements.addAll(getElementsByState(NORMAL));
//		elements.addAll(getElementsByState(EXECUTING));
//		elements.addAll(getElementsByState(RETRY));
//		elements.addAll(getElementsByState(ERROR));
//		return elements;
//	}
	
	/**
	 * @param state
	 * @return
	 */
	public List<ElementObject> getElementsByState(int state, int limit) {
		List<ElementObject> elements = Lists.newArrayList();

		switch (state) {
		case 0:
			Map<String, Object> objects_normal = this.queueSchedulerHelper.getInitStateElements(limit);
			for(String name : objects_normal.keySet()) {
				elements.add(new ElementObject(name, type, state, objects_normal.get(name), null, this.name, 0));
			}
			return elements;
		case 1:
			Map<String, ZookeeperQueueEntityWrapper> wrapper_executing = this.queueSchedulerHelper.getExecutingStateElements(limit);
			for(String name : wrapper_executing.keySet()) {
				Object o = BytesUtil.bytesToClass(wrapper_executing.get(name).getElement(), ConfigurationUtil.getInitConfig(), ReflectionUtils.getClass(this.type));
				elements.add(new ElementObject(name, type, state, o, wrapper_executing.get(name).getStart_time(), this.name, wrapper_executing.get(name).getRetry_times()));
			}
			return elements;
		case 2:
			Map<String, ZookeeperQueueEntityWrapper> wrapper_retry = this.queueSchedulerHelper.getRetryStateElements(limit);
			for(String name : wrapper_retry.keySet()) {
				Object o = BytesUtil.bytesToClass(wrapper_retry.get(name).getElement(), ConfigurationUtil.getInitConfig(), ReflectionUtils.getClass(this.type));
				elements.add(new ElementObject(name, this.type, state, o, null, this.name, wrapper_retry.get(name).getRetry_times()));
			}
			return elements;
		case 3:
			Map<String, Object> objects_error = this.queueSchedulerHelper.getErrorStateElements(limit);
			for(String name : objects_error.keySet()) {
				elements.add(new ElementObject(name, type, state, objects_error.get(name), null, this.name, 0));
			}
			return elements;
		default:
			throw new RuntimeException("Zookeeper Queue can't be state " + state);
		}
	}

	/**
	 * <p>通过节点名称和状态获取一个元素对象</p>
	 * @param name
	 * @param state
	 * @return
	 */
	public ElementObject getElementByNameAndState(String name, int state) {
		switch (state) {
		case 0:
			Object o_normal = this.queueSchedulerHelper.getInitElementByName(name);
			return new ElementObject(name, this.type, state, o_normal, null, this.name, 0);
		case 1:
			ZookeeperQueueEntityWrapper zq_executing = this.queueSchedulerHelper.getExecutingElementByName(name);
			Object o_executing= BytesUtil.bytesToClass(zq_executing.getElement(), ConfigurationUtil.getInitConfig(), ReflectionUtils.getClass(this.type));
			return new ElementObject(name, this.type, state, o_executing, zq_executing.getStart_time(), this.name, zq_executing.getRetry_times());
		case 2:
			ZookeeperQueueEntityWrapper zq_retry = this.queueSchedulerHelper.getRetryElementByName(name);
			Object o_retry = BytesUtil.bytesToClass(zq_retry.getElement(), ConfigurationUtil.getInitConfig(), ReflectionUtils.getClass(this.type));
			return new ElementObject(name, this.type, state, o_retry, null, this.name, zq_retry.getRetry_times());
		case 3:
			Object o_error = this.queueSchedulerHelper.getErrorElementByName(name);
			return new ElementObject(name, this.type, state, o_error, null, this.name, 0);
		default:
			throw new RuntimeException("Zookeeper Queue can't be state " + state);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
}
