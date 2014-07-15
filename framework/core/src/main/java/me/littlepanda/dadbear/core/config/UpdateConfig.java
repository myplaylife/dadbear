package me.littlepanda.dadbear.core.config;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public interface UpdateConfig {
	/**
	 * 删除一个配置
	 * @param changedName
	 */
	public void removeConfig(String changedName);
	/**
	 * 更新一个配置，不管是增、改
	 */
	public void updateConfig(ConfigInfo configInfo, String changedName);
}
