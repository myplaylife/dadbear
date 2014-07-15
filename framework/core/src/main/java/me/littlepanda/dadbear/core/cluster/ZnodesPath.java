package me.littlepanda.dadbear.core.cluster;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class ZnodesPath {
	
	/**
	 * 根节点路径
	 */
	private String root;
	
	/**
	 * 配置根路径
	 */
	private String config;
	
	/**
	 * master节点根路径
	 */
	private String master;
	
	/**
	 * slave节点根路径
	 */
	private String slave;
	
	/**
	 * master节点路径
	 */
	private String master_election;
	
	/**
	 * slave节点路径
	 */
	private String slave_register;
	/**
	 * 全局队列根节点
	 */
	private String queue;
	
	/**
	 * 取得配置模块所在zookeeper路径
	 * @param module
	 * @return
	 */
	public String getModulePath(String module){
		return config + "/" + module;
	}
	
	/**
	 * 取得配置项所在zookeeper路径
	 * @param module
	 * @param item
	 * @return
	 */
	public String getItemPath(String module, String item){
		return config + "/" + module + "/" + item;
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public String getMaster() {
		return master;
	}

	public void setMaster(String master) {
		this.master = master;
	}

	public String getSlave() {
		return slave;
	}

	public void setSlave(String slave) {
		this.slave = slave;
	}

	public String getMaster_election() {
		return master_election;
	}

	public void setMaster_election(String master_election) {
		this.master_election = master_election;
	}

	public String getSlave_register() {
		return slave_register;
	}

	public void setSlave_register(String slave_register) {
		this.slave_register = slave_register;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

}
