package me.littlepanda.dadbear.core.schedule_with_queue;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public interface QueueSchedulerService<T> {
	
	abstract public void run(T t) throws RuntimeException;
	
	abstract public void exception() throws RuntimeException;

}
