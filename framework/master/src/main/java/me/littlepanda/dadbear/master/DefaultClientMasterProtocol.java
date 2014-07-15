package me.littlepanda.dadbear.master;

import java.util.Collection;
import java.util.List;

import org.apache.avro.AvroRemoteException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import me.littlepanda.dadbear.core.annotations.Config;
import me.littlepanda.dadbear.core.annotations.ServiceLog;
import me.littlepanda.dadbear.core.rpc.ClientMasterProtocol;
import me.littlepanda.dadbear.core.rpc.ServiceInfo;
import me.littlepanda.dadbear.core.rpc.SlaveInfo;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class DefaultClientMasterProtocol implements ClientMasterProtocol { 
	
	@ServiceLog
	private Log log = LogFactory.getLog(DefaultClientMasterProtocol.class);
	
	@Config
	Configuration conf;
	
	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.rpc.ClientMasterProtocol#getServiceInfoByName(java.lang.CharSequence)
	 */
	@Override
	public ServiceInfo getServiceInfoByName(CharSequence name)
			throws AvroRemoteException {
		return DistributeAlgoServiceFactory.getServiceInfo(name.toString());
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.rpc.ClientMasterProtocol#getAllServiceInfosByServiceName(java.lang.CharSequence)
	 */
	@Override
	public List<ServiceInfo> getAllServiceInfosByServiceName(CharSequence name)
			throws AvroRemoteException {
		return Master.getAllServiceInfosByServiceName(name.toString());
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.rpc.ClientMasterProtocol#getServiceInfoByNameAndSlaveId(java.lang.CharSequence, java.lang.CharSequence)
	 */
	@Override
	public ServiceInfo getServiceInfoByNameAndSlaveId(CharSequence name,
			CharSequence slave_id) throws AvroRemoteException {
		SlaveInfo slaveInfo = Master.getSlaveBySlaveId(slave_id.toString());
		
		for(ServiceInfo server: slaveInfo.getServices()){
			if(server.getName().toString().equals(name.toString())){
				return server;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.rpc.ClientMasterProtocol#getSlaveById(java.lang.CharSequence)
	 */
	@Override
	public SlaveInfo getSlaveById(CharSequence slave_id)
			throws AvroRemoteException {
		return Master.getSlaveBySlaveId(slave_id.toString());
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.rpc.ClientMasterProtocol#getSlavesWithServiceInfo()
	 */
	@Override
	public List<SlaveInfo> getSlaves()
			throws AvroRemoteException {
		Collection<SlaveInfo> slaveInfos = Master.getSlaves();
		return Lists.newArrayList(slaveInfos.iterator());
	}

}
