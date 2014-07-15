package me.littlepanda.dadbear.core.schedule_with_queue;

import java.util.Map;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
interface ZookeeperQueueInterface<T> {
	
	/**
	 * @param t 要加入队列的元素
	 * @return Zookeeper节点名称
	 */
	abstract String offer(T t);
	
	/**
	 * @return 队列长度
	 */
	abstract int size();
	
	/**
	 * 从头部取出一个元素并删除
	 * @return {path:路径; element:元素}
	 */
	abstract Map<String, Object> poll();
	
	/**
	 * 从头部取出一个元素，但不删除之
	 * @return
	 */
	abstract Map<String, Object> peek();
	
	/**
	 * 删除队列中，zookeeper节点名称为${node_name}的元素
	 * @param node_name zookeeper节点名称
	 * @return
	 */
	abstract boolean remove(String node_name);
	
	/**
	 * <p>获取队列中的所有元素</>
	 * @return {${元素名称} : ${元素内容})
	 */
	abstract Map<String, T> getQueueElements();
	
	/**
	 * <p>限制每次取元素的数量</p>
	 * @return
	 */
	abstract Map<String, T> getQueueElementsByLimits(int max);
	
	/**
	 * <p>根据节点名称，获取一个节点上的对象</p>
	 * @param node_name
	 * @return
	 */
	abstract T getElementByName(String node_name);
	
	/**
	 * <p>判断节点是否存在</p>
	 * @param node_name
	 * @return
	 */
	abstract boolean exists(String node_name);
	
}
