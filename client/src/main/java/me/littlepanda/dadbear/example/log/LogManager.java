package me.littlepanda.dadbear.example.log;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public interface LogManager {

	/**
	 * @return 获取所有日志类别和日志文件名的列表
	 */
	abstract public Map<String, List<String>> getAllLogs();
	
	/**
	 * @param log_name 日志类别
	 * @return 获取某个日志类别的日志文件名称列表
	 */
	abstract public List<String> getLogsByLogName(String log_name);
	
	/**
	 * @param log_name 日志类别
	 * @param pattern 查询字符串
	 * @return 根据日志类别和关键字获取具有该关键字的日志文件名称的列表
	 */
	abstract public Map<String, Collection<String>> findLogs(List<String> log_names, String pattern);
	
	/**
	 * @param log_name 日志类别
	 * @param file_name 文件名称
	 * @return 根据日志类别和文件名称获取一个日志文件的内容
	 */
	abstract public byte[] getLog(String log_name, String file_name);
}
