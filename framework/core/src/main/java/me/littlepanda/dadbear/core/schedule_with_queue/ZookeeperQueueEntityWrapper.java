package me.littlepanda.dadbear.core.schedule_with_queue;

import java.util.Date;

import org.apache.avro.reflect.Nullable;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class ZookeeperQueueEntityWrapper {
	
	public int retry_times;
	
	@Nullable
	public Date start_time;
	
	public byte[] element;
	
	public String node_name;
	
	public String element_type;
	
	public String getElement_type() {
		return element_type;
	}

	public void setElement_type(String element_type) {
		this.element_type = element_type;
	}

	public String getNode_name() {
		return node_name;
	}

	public void setNode_name(String node_name) {
		this.node_name = node_name;
	}

	public int getRetry_times() {
		return retry_times;
	}

	public void setRetry_times(int retry_times) {
		this.retry_times = retry_times;
	}

	public Date getStart_time() {
		return start_time;
	}

	public void setStart_time(Date start_time) {
		this.start_time = start_time;
	}

	public byte[] getElement() {
		return element;
	}

	public void setElement(byte[] element) {
		this.element = element;
	}

}
