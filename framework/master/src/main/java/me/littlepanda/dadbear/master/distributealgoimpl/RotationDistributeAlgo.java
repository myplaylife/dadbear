package me.littlepanda.dadbear.master.distributealgoimpl;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import me.littlepanda.dadbear.master.Master;
import me.littlepanda.dadbear.core.rpc.ServiceInfo;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class RotationDistributeAlgo extends AbstractDistributeAlgo {

	private static Log log = LogFactory.getLog(RotationDistributeAlgo.class);
	
	private ConcurrentHashMap<String, BlockingDeque<ServiceInfo>> serviceInfos = new ConcurrentHashMap<String, BlockingDeque<ServiceInfo>>();

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.master.DistributeAlgo#getService(java.lang.String)
	 */
	@Override
	public ServiceInfo getService(String service_name) {
		BlockingDeque<ServiceInfo> queue = serviceInfos.get(service_name);
		if(queue.size() < 1){
			log.error("No service " + service_name + " to use.");
			throw new RuntimeException("No service " + service_name + " to use.");
		}
		try {
			ServiceInfo serviceInfo = queue.take();
			queue.put(serviceInfo);
			return serviceInfo;
		} catch (InterruptedException e) {
			log.error("error when operate service queue in RotationDistributeAlgo.", e);
			throw new RuntimeException("error when operate service queue in RotationDistributeAlgo.", e);
		}
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.master.AbstractDistributeAlgo#updateServiceInfo(java.lang.String)
	 */
	@Override
	protected void updateServiceInfo(String service_name) {
		BlockingDeque<ServiceInfo> queue = new LinkedBlockingDeque<ServiceInfo>();
		queue.addAll(Master.getAllServiceInfosByServiceName(service_name));
		serviceInfos.remove(service_name);
		serviceInfos.put(service_name, queue);
	}

}
