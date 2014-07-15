package me.littlepanda.dadbear.master;

import me.littlepanda.dadbear.core.rpc.ServiceInfo;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public interface DistributeAlgo {

	/**
	 * 通过服务名，获得一个服务信息
	 * @param service_name
	 * @return
	 */
	abstract public ServiceInfo getService(String service_name);
	
	
	/**
	 * <p>更新服务状态:</p>
	 * <p>实现类中应该保存当前获取服务的时间戳，如果时间戳与传递进来的不一致，就应该更新服务信息。</p>
	 * <p>服务信息来自Master节点保存的“服务于slave节点对应关系”</p>
	 */
	abstract public void updateService(String service_name, String flag);
}
