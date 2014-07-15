package me.littlepanda.dadbear.core.schedule_with_queue;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public interface QueueProcessor {
	
	public static final String KEY_PROCESSOR = "processor";
	public static final String KEY_QUEUE_NAME = "queue_name";
	public static final String KEY_QUEUE_TYPE = "queue_type";
	public static final String KEY_MAX_RETRY = "max_retry";
	public static final String KEY_THREAD_NUMBER = "thread_number";
	
	abstract public void run();
}
