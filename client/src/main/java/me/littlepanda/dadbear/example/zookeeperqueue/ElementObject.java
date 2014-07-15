package me.littlepanda.dadbear.example.zookeeperqueue;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.littlepanda.dadbear.core.schedule_with_queue.QueueSchedulerHelper;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class ElementObject extends Base implements Comparable<String>{
	
	private QueueSchedulerHelper queueSchedulerHelper;
	
	private String name;
	private String type;
	private String desc;
	private Date startTime;
	private int state;
	private String queueName;
	private String path;
	private int retryTimes;
	
	public ElementObject(String name, String type, int state, Object o, Date startTime, String queueName, int retryTimes) {
		this.name = name;
		this.type = type;
		this.state = state;
		this.desc = o.toString();
		this.startTime = startTime;
		this.queueName = queueName;
		this.path = getPath();
		queueSchedulerHelper = new QueueSchedulerHelper(this.queueName, this.type);
	}
	
	private String getPath() {
		if(QueueObject.NORMAL == this.state) {
			return zp.getQueue() + "/" + this.queueName + "/" + this.name;
		} else if(QueueObject.EXECUTING == this.state) {
			return zp.getQueue() + "/" + this.queueName + "_" + QueueSchedulerHelper.QUEUE_EXECUTING + "/" + this.name;
		} else if(QueueObject.RETRY == this.state) {
			return zp.getQueue() + "/" + this.queueName + "_" + QueueSchedulerHelper.QUEUE_RETRY + "/" + this.name;
		} else if(QueueObject.ERROR == this.state) {
			return zp.getQueue() + "/" + this.queueName + "_" + QueueSchedulerHelper.QUEUE_ERROR + "/" + this.name;
		}
		return "";
	}
	
	/**
	 * <p>将元素从当前状态转移为初始状态</p>
	 * <p>只有处于正在执行或错误状态的元素才能转移</p>
	 * @return
	 */
	public boolean moveToNormal() {
		if(this.state == QueueObject.EXECUTING) {
			return this.queueSchedulerHelper.moveExecutingToInit(this.name);
		} else if(this.state == QueueObject.ERROR) {
			return this.queueSchedulerHelper.moveErrorToInit(this.name);
		} else {
			throw new RuntimeException("Can't move element " + this.name + " to init state.It must be state in [executing, error].");
		}
	}
	
	/**
	 * <p>从队列中删除元素</p>
	 * @return
	 */
	public boolean remove() {
		if(zu.exists(path)) {
			zu.deleteNode(path);
		}
		return true;
	}
	
	public Date getStartTime() {
		return startTime;
	}
	
	public String getFormattedStartTime(String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		if(this.startTime == null) {
			return null;
		}
		return sdf.format(startTime);
	}
	
	public String toString() {
		return this.desc;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getType() {
		return this.type;
	}
	
	public int getState() {
		return this.state;
	}
	
	public String getQueueName() {
		return this.queueName;
	}
	
	public int getRetryTimes() {
		return this.retryTimes;
	}
	
	@Override
	public int compareTo(String o) {
		return this.name.compareTo(o);
	}

}
