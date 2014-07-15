//package me.littlepanda.dadbear.master;
//
//import java.util.Collection;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingDeque;
//import java.util.concurrent.TimeUnit;
//
//import org.apache.avro.AvroRemoteException;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import me.littlepanda.dadbear.core.rpc.ClientMasterProtocol;
//import me.littlepanda.dadbear.core.rpc.SlaveInfo;
//
///**
// * @author 张静波 myplaylife@icloud.com
// *
// */
//public class RotationClientMasterProtocol extends AbstractProtocolServer implements ClientMasterProtocol, SlaveNodeLoader {
//	
//	private static Log log = LogFactory.getLog(RotationClientMasterProtocol.class);
//	
//	private static BlockingQueue<SlaveInfo> serviceQueue = new LinkedBlockingDeque<SlaveInfo>();
//
//	/* (non-Javadoc)
//	 * @see me.littlepanda.dadbear.core.ClientMasterProtocol#getComputeNodeInfo(java.lang.CharSequence)
//	 */
//	@Override
//	public SlaveInfo getSlaveNode(CharSequence type)
//			throws AvroRemoteException {
//		if(serviceQueue.size() == 0){
//			log.error("no compute node register.");
//			throw new RuntimeException("no compute node register.");
//		}
//		SlaveInfo retVal = null;
//		try {
//			retVal = serviceQueue.poll(2, TimeUnit.SECONDS);
//			serviceQueue.put(retVal);
//		} catch (InterruptedException e) {
//			log.error("error when poll serviceQueue.", e);
//			throw new RuntimeException("error when poll serviceQueue.");
//		}
//		return retVal;
//	}
//
//	@Override
//	public void loadSlaveNodes(Collection<? extends SlaveInfo> nodes) {
//		// TODO Auto-generated method stub
//		
//	}
//}
