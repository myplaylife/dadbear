package me.littlepanda.dadbear.example.zookeeperqueue;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class CountObject {
	
	public CountObject(long count_normal, long count_error, long count_retry, long count_executing) {
		this.countError = count_error;
		this.countExecuting = count_executing;
		this.countNormal = count_normal;
		this.countRetry = count_retry;
		this.count = this.countNormal + this.countError + this.countExecuting + this.countRetry;
	}
	
	private long countNormal;
	private long countError;
	private long countRetry;
	private long countExecuting;
	private long count;
	
	public long getCount() {
		return this.count;
	}
	
	public long getCountNormal() {
		return countNormal;
	}
	public long getCountError() {
		return countError;
	}
	public long getCountRetry() {
		return countRetry;
	}
	public long getCountExecuting() {
		return countExecuting;
	}
	
}
