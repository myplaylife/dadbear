package me.littlepanda.dadbear.core.util;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import me.littlepanda.dadbear.core.config.CommonConfigurationKeys;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class ZookeeperUtil {
	
	private static Log log = LogFactory.getLog(ZookeeperUtil.class);
	
	private static ConcurrentHashMap<String, ZookeeperUtil> cache = new ConcurrentHashMap<String, ZookeeperUtil>();
	
	private ZooKeeper zk;
	
	public static ZookeeperUtil get(Configuration conf){
		List<Object> zookeeperServerlist = conf.getList(CommonConfigurationKeys.ZOOKEEPER_SERVER_LIST);
		int session_timeout = conf.getInt(CommonConfigurationKeys.ZOOKEEPER_SESSION_TIMEOUT);
		String defaultWatcher = conf.getString(CommonConfigurationKeys.ZOOKEEPER_DEFAULT_WATCHER);
		
		//zu对象缓存的键值由“服务器列表”、“超时时间”和“默认监视器”组成。
		String cacheKey = zookeeperServerlist.toString() + ":" + String.valueOf(session_timeout) + ":" + defaultWatcher;
		
		ZookeeperUtil zku = cache.get(cacheKey);
		if(null != zku){
			return zku;
		}
		
		String server = "";
		for(Object s : zookeeperServerlist){
			server = server + s.toString() + ",";
		}
		server = server.substring(0, server.length() - 1);
		Watcher watcher = ReflectionUtils.newInstance(defaultWatcher, conf);
		
		zku = new ZookeeperUtil(server, session_timeout, watcher);
		cache.put(cacheKey, zku);
		return zku;
	}
	
	private ZookeeperUtil(String server, int session_timeout, Watcher watcher){
		
		try {
			zk = new ZooKeeper(server, session_timeout, watcher);
		} catch (IOException e) {
			throw new RuntimeException("error when create Zookeeper instance", e);
		}
		
	}
	
	/**
	 * 创建持久节点
	 */
	public String createPersistNode(String path){
		return createPersistNode(path, new byte[0]);
	}
	public String createPersistNode(String path, byte[] data){
		return createPersistNode(path, data, false);
	}
	public String createPersistNode(String path, byte[] data, boolean isSequence){
		String rev;
		try {
	        if(isSequence){
	        	rev = zk.create(path, data, Ids.OPEN_ACL_UNSAFE,
	        			CreateMode.PERSISTENT_SEQUENTIAL);
	        } else {
	        	rev = zk.create(path, data, Ids.OPEN_ACL_UNSAFE,
	        			CreateMode.PERSISTENT);
	        }
	        return rev;
		} catch (KeeperException e) {
			throw new RuntimeException("error when create zookeeper persist node.", e);
		} catch (InterruptedException e) {
			throw new RuntimeException("error when create zookeeper persist node.", e);
		}
	}
	/**
	 * 创建临时节点
	 */
	public String createEphemeralNode(String path){
		return createEphemeralNode(path, new byte[0]);
	}
	public String createEphemeralNode(String path, byte[] data){
		return createEphemeralNode(path, data, false);
	}
	public String createEphemeralNode(String path, byte[] data, boolean isSequence){
		String rev;
		try {
	        if(isSequence){
	        	rev = zk.create(path, data, Ids.OPEN_ACL_UNSAFE,
	        			CreateMode.EPHEMERAL_SEQUENTIAL);
	        } else {
	        	rev = zk.create(path, data, Ids.OPEN_ACL_UNSAFE,
	        			CreateMode.EPHEMERAL);
	        }
	        return rev;
		} catch (KeeperException e) {
			throw new RuntimeException("error when create zookeeper ephemeral node.", e);
		} catch (InterruptedException e) {
			throw new RuntimeException("error when create zookeeper ephemeral node.", e);
		}
	}
	
	/**
	 * 判断路径是否存在
	 * @param path
	 * @return
	 */
	public boolean exists(String path){
		return exists(path, null);
	}
	public boolean exists(String path, Watcher watcher){
		Stat s = null;
		try {
			if(null == watcher){
				s = zk.exists(path, false);
			} else {
				s = zk.exists(path, watcher);
			}
		} catch (KeeperException e) {
			throw new RuntimeException("error when judge a path exists.", e);
		} catch (InterruptedException e) {
			throw new RuntimeException("error when judge a path exists.", e);
		}
		if(null == s){
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * 获取所有子节点
	 * @param path
	 * @return
	 */
	public List<String> getChildren(String path){
		if(this.exists(path)) {
			return getChildren(path, null);
		} else {
			log.error(path + " is not exists.");
			throw new RuntimeException(path + " is not exists.");
		}
	}
	public List<String> getChildren(String path, Watcher watcher){
		try {
			if(null == watcher){
				return zk.getChildren(path, false);
			} else {
				return zk.getChildren(path, watcher);
			}
		} catch (KeeperException e) {
			throw new RuntimeException("error when get a path children.", e);
		} catch (InterruptedException e) {
			throw new RuntimeException("error when get a path children.", e);
		}
	}
	
	/**
	 * 获取某节点的数据
	 * @return
	 */
	public byte[] getData(String path){
		return getData(path, null);
	}
	public byte[] getData(String path, Watcher watcher){
		try {
			if(null == watcher){
					return zk.getData(path, false, null);
			} else {
				return zk.getData(path, watcher, null);
			}
		} catch (KeeperException e) {
			throw new RuntimeException("error when get a path data.", e);
		} catch (InterruptedException e) {
			throw new RuntimeException("error when get a path data.", e);
		}
	}
	
	/**
	 * 设置某节点的数据
	 * @param path
	 * @param data
	 */
	public void setData(String path, byte[] data){
		try {
			zk.setData(path, data, -1);
		} catch (KeeperException e) {
			throw new RuntimeException("error when get a path data.", e);
		} catch (InterruptedException e) {
			throw new RuntimeException("error when get a path data.", e);
		}
	}
	
	/**
	 * 删除节点
	 * @param path
	 */
	public void deleteNode(String path){
		try {
			zk.delete(path, -1);
		} catch (InterruptedException e) {
			throw new RuntimeException("error when delete a path.", e);
		} catch (KeeperException e) {
			throw new RuntimeException("error when delete a path.", e);
		}
	}
	public ZooKeeper getZk(){
		return zk;
	}
	
}
