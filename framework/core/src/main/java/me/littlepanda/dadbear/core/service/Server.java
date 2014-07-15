package me.littlepanda.dadbear.core.service;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public interface Server {
	/**
	 * 启动服务
	 */
	abstract public void start();
	/**
	 * 停止服务
	 */
	abstract public void stop();
	/**
	 * 服务类型
	 */
	abstract public String getType();
	/**
	 * 获取协议信息
	 */
	abstract public String getProtocol();
	abstract public String getImplement();
	abstract public int getPort();
	abstract public String getSchedule();
	abstract public String getDescription();
	abstract public String getLogName();
	/**
	 * 获取状态
	 */
	abstract public boolean isAlive();
	abstract public void setAlive(boolean alive);
	/**
	 * 调用类型 
	 */
	abstract public String getDistributeAlgo();
}
