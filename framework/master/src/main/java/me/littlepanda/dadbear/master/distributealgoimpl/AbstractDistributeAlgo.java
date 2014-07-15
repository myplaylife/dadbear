package me.littlepanda.dadbear.master.distributealgoimpl;

import java.util.concurrent.ConcurrentHashMap;

import me.littlepanda.dadbear.core.config.Configured;
import me.littlepanda.dadbear.master.DistributeAlgo;
import me.littlepanda.dadbear.core.rpc.ServiceInfo;

/**
 * @author 张静波 myplaylife@gmail.com
 *
 */
abstract public class AbstractDistributeAlgo extends Configured implements DistributeAlgo {
	
	protected ConcurrentHashMap<String, String> flag = new ConcurrentHashMap<String, String>();
	
	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.master.DistributeAlgo#getService(java.lang.String)
	 */
	@Override
	abstract public ServiceInfo getService(String service_name); 

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.master.DistributeAlgo#updateService(java.lang.String, java.lang.String)
	 */
	@Override
	public void updateService(String service_name, String flag) {
		String oriFlag = this.flag.get(service_name);
		if(null == oriFlag || !oriFlag.equals(flag)){
			updateServiceInfo(service_name);
			this.flag.put(service_name, flag);
		}
	}
	/**
	 * 更新服务信息
	 */
	abstract protected void updateServiceInfo(String service_name);
}
