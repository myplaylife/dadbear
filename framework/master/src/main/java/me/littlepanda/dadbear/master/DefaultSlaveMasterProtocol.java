package me.littlepanda.dadbear.master;

import org.apache.avro.AvroRemoteException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import me.littlepanda.dadbear.core.annotations.Config;
import me.littlepanda.dadbear.core.annotations.ServiceLog;
import me.littlepanda.dadbear.core.rpc.SlaveMasterProtocol;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class DefaultSlaveMasterProtocol implements SlaveMasterProtocol {

	@ServiceLog
	private Log log = LogFactory.getLog(DefaultSlaveMasterProtocol.class); 
	
	@Config
	Configuration conf;
	
	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.rpc.SlaveMasterProtocol#sendHeartBeat(java.lang.CharSequence)
	 */
	@Override
	public Void sendHeartBeat(CharSequence message) throws AvroRemoteException {
		log.info("Receive hearbeat info:" + message);
		System.out.println("Receive hearbeat info:" + message);
		return null;
	}

}
