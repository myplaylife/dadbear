package me.littlepanda.dadbear.core.cluster;

import org.apache.commons.configuration.Configuration;
import org.apache.zookeeper.Watcher;

import me.littlepanda.dadbear.core.config.CommonConfigurationKeys;
import me.littlepanda.dadbear.core.config.ConfigurationUtil;
import me.littlepanda.dadbear.core.rpc.MasterInfo;
import me.littlepanda.dadbear.core.util.BytesUtil;
import me.littlepanda.dadbear.core.util.ZookeeperUtil;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class ClusterUtil {
	
	private static final ZnodesPath znodesPath = new ZnodesPath();
	private static ZookeeperUtil zu;
	private static Configuration init_conf;
	static {
		init_conf = ConfigurationUtil.getInitConfig();
		zu = ZookeeperUtil.get(init_conf);
		initZondesPath(init_conf);
	}
	private static void initZondesPath(Configuration conf){
		znodesPath.setRoot(conf.getString(CommonConfigurationKeys.ZNODE_ROOT));
		znodesPath.setMaster(znodesPath.getRoot() + conf.getString(CommonConfigurationKeys.ZNODE_MASTER));
		znodesPath.setMaster_election(znodesPath.getMaster() + conf.getString(CommonConfigurationKeys.ZNODE_MASTER_ELECTION));
		znodesPath.setSlave(znodesPath.getRoot() + conf.getString(CommonConfigurationKeys.ZNODE_SLAVE));
		znodesPath.setSlave_register(znodesPath.getSlave() + conf.getString(CommonConfigurationKeys.ZNODE_SLAVE_REGISTER));
		znodesPath.setConfig(znodesPath.getRoot() + conf.getString(CommonConfigurationKeys.ZNODE_CONFIG));
		znodesPath.setQueue(znodesPath.getRoot() + conf.getString(CommonConfigurationKeys.ZNODE_QUEUE));
	}
	
	public static ZnodesPath getZnonesPath(){
		return znodesPath;
	}
	
	/**
	 * 判断leader是否下线了<br/>
	 * /master节点数据中有MasterInfo信息，信息描述的节点在zookeeper系统中存在，就没有下线<br/>
	 * @param zu
	 * @return
	 */
	public static boolean isLeaderAlive(ZookeeperUtil zu, Configuration conf){
		byte[] bytesLeader = zu.getData(znodesPath.getMaster());
		if(0 == bytesLeader.length){
			return false;
		}
		MasterInfo leaderInfo = BytesUtil.bytesToClass(bytesLeader, conf, MasterInfo.class);
		if(!zu.exists(znodesPath.getMaster() + "/" + leaderInfo.getId())){
			return false;
		}
		return true;
	}
	
	/**
	 * <p>获取leader masterinfo</p>
	 */
	public static MasterInfo getLeaderMasterInfo(Watcher watcher) {
		byte[] bytesLeader = zu.getData(znodesPath.getMaster(), watcher); 
		return BytesUtil.bytesToClass(bytesLeader, init_conf, MasterInfo.class);
	}
	/**
	 * 获取网络拓扑图
	 */
	
	/**
	 * 获取节点状态
	 */
}
