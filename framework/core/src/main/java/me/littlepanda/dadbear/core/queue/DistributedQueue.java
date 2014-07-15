package me.littlepanda.dadbear.core.queue;

import java.util.Queue;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public interface DistributedQueue<T> extends Queue<T> {
	/**
	 * <p>如果使用无参构造函数，需要先调用这个方法，队列才能使用</p>
	 * @param queue_name
	 * @param clazz
	 */
	abstract public void init(String queue_name, Class<T> clazz);
}
