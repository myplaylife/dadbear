package me.littlepanda.dadbear.core.queue;

import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;
import me.littlepanda.dadbear.core.cluster.ClusterUtil;
import me.littlepanda.dadbear.core.cluster.ZnodesPath;
import me.littlepanda.dadbear.core.config.ConfigurationUtil;
import me.littlepanda.dadbear.core.util.BytesUtil;
import me.littlepanda.dadbear.core.util.ZookeeperUtil;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class ZookeeperQueueHelper<T> {
	
	private static Log log = LogFactory.getLog(ZookeeperQueueHelper.class);
	
	private static ZookeeperUtil zu;
	private static ZnodesPath zp;
	public static final String ELEMENT_PREFIX = "element";
	public static final String TEMP_ELEMENT_KEY = "element";
	public static final String TEMP_PATH_KEY = "path";
	
	static {
		Configuration conf = ConfigurationUtil.getInitConfig();
		zu = ZookeeperUtil.get(conf);
		zp = ClusterUtil.getZnonesPath();
	}
	
	private String queuePath;
	private Class<T> queueType;
	private String elementPath;
	
	public ZookeeperQueueHelper(String queue_name, Class<T> c){
		this.queuePath = zp.getQueue() + "/" + queue_name;
		this.queueType = c;
		this.elementPath = queuePath + "/" + ELEMENT_PREFIX;
		
		if(!zu.exists(zp.getQueue())){
			zu.createPersistNode(zp.getQueue());
		}
		if(!zu.exists(queuePath)){
			//如果队列不存在，创建队列节点，并注册元素类型
			zu.createPersistNode(queuePath, c.getName().getBytes());
		} else {
			//如果队列已经被创建，检查此处给出的元素对象类型是否与已注册元素类型一致
			String type = new String(zu.getData(queuePath));
			if(!type.equals(c.getName())){
				log.error("Queue is already exist, and element type is '" + type + "', but now given type is '" + c.getName() +"'.");
				throw new RuntimeException("Queue is already exist, and element type is '" + type + "', but now given type is '" + c.getName() +"'.");
			}
		}
	}
	
	public String offer(T t) {
		byte[] bytes = BytesUtil.classToBytes(t, ConfigurationUtil.getInitConfig());
        if(bytes.length > 1024*1024){
        	log.error("Object which add to queue can't bigger than 1M.");
        	throw new RuntimeException("Object which add to queue can't bigger than 1M.");
        }
        return zu.createPersistNode(elementPath, bytes, true);
	}
	
	/**
	 * @param i 重试次数
	 * @return 获取元素和元素所在路径
	 * 	<p>返回格式：{"element": element", "path": path}</p>
	 */
	public Map<String, Object> getElementAndpath(int max_retry) {
		int retry = 1;
		while(true){
			List<String> list = zu.getChildren(queuePath);
			if (list.size() == 0) {
				return null; 
			} else {
				Integer min = new Integer(list.get(0).substring(ELEMENT_PREFIX.length()));
				String minElementName = list.get(0);
				for(String s : list){
					Integer tempValue = new Integer(s.substring(ELEMENT_PREFIX.length()));
					if(tempValue < min) {
						min = tempValue;
						minElementName = s;
					}
				}
				byte[] data = null;
				try {
					data = zu.getData(queuePath + "/" + minElementName);
				} catch(RuntimeException re) {
					//因为是分布式队列，当你去去数据的时候，数据可能已经被另一个进程删除了，所以，如果取不到数据就重试
					//也有可能因为竞争激烈，每次取的时候都被别人删除了，因此重试三次，如果三次都取不到，说明竞争国语激烈，就不取了
					if(retry > max_retry){
						return null;
					}
					retry++;
					continue;
				}
				Map<String, Object> retVal = Maps.newHashMap();
				retVal.put(TEMP_ELEMENT_KEY, BytesUtil.bytesToClass(data, ConfigurationUtil.getInitConfig(), queueType));
				retVal.put(TEMP_PATH_KEY, queuePath + "/" + minElementName);
				return retVal; 
			}
		}
	}
	
	public int size() {
		return zu.getChildren(queuePath).size();
	}
	
	public ZookeeperUtil getZu(){
		return zu;
	}
	
	public String getQueuePath(){
		return queuePath;
	}
	
	public Class<T> getQueueType(){
		return queueType;
	}
	
}
