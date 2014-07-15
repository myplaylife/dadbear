package me.littlepanda.dadbear.example.log;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import me.littlepanda.dadbear.core.annotations.ServiceLog;
import me.littlepanda.dadbear.core.config.CommonConfigurationKeys;
import me.littlepanda.dadbear.core.config.ConfigurationUtil;
import me.littlepanda.dadbear.core.util.ShellUtil;
import me.littlepanda.dadbear.core.util.ShellUtil.ShellResult;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class LogManagerImpl implements LogManager {
	
	@ServiceLog
	Log log = org.apache.commons.logging.LogFactory.getLog(LogManagerImpl.class);
	
	private static Configuration conf;
	private static String root_path;
	private static String log_file_name;
	static {
		conf = ConfigurationUtil.getInitConfig();
		root_path = conf.getString(CommonConfigurationKeys.LOG_ROOT_PATH);
		root_path = root_path.lastIndexOf("/") == root_path.length()-1 ? root_path : root_path + "/";
		log_file_name = conf.getString(CommonConfigurationKeys.LOG_FILE_NAME);
	}
	
	private static final String find_command = "find ${path} -type f -print | xargs grep ${word} | cut -d':' -f1 | sort | uniq";
	private static final String replace_path = "\\$\\{path\\}";
	private static final String replace_word = "\\$\\{word\\}";

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.example.log.LogManager#getAllLogs()
	 */
	@Override
	public Map<String, List<String>> getAllLogs() {
		Map<String, List<String>> logs = Maps.newHashMap();
		File logPath = new File(root_path);
		if(!logPath.exists()) {
			return logs;
		}
		List<File> logNames = Lists.newArrayList(logPath.listFiles());
		for(File f : logNames) {
			if(f.isFile()) {
				continue;
			}
			String[] fileNames = f.list();
			List<String> files = null;
			for(String fileName : fileNames) {
				files = Lists.newArrayList();
				if(fileName.startsWith(log_file_name)) {
					files.add(fileName);
				}
			}
			logs.put(f.getName(), files);
		}
		return logs;
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.example.log.LogManager#getLogsByLogName(java.lang.String)
	 */
	@Override
	public List<String> getLogsByLogName(String log_name) {
		String logPath = root_path + log_name;
		File logFile = new File(logPath);
		if(!logFile.exists()) {
			log.error("Log name " + log_name + " is not exists!");
			throw new RuntimeException("Log name " + log_name + " is not exists!");
		}
		List<String> logs = Lists.newArrayList();
		for(String fileName : logFile.list()) {
			if(fileName.startsWith(log_file_name)) {
				logs.add(fileName);
			}
		}
		return logs;
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.example.log.LogManager#findLogs(java.lang.String, java.lang.String)
	 */
	@Override
	public Map<String, Collection<String>> findLogs(List<String> log_names, String pattern) {
		// find /var/log/distributedcomputing/ -type f -print | xargs grep word|cut -d':' -f1 | sort | uniq
		Multimap<String, String> temp = HashMultimap.create();
		if(log_names == null || log_names.size() == 0) {
			return temp.asMap();
		}
		String command = find_command.replaceAll(replace_path, root_path).replaceAll(replace_word, pattern);
		ShellResult sr = null;
		try {
			sr = ShellUtil.executeShell(command);
		} catch (IOException e) {
			log.error("Error when execute shell command: " + command, e);
			throw new RuntimeException("Error when execute shell command: " + command, e);
		}
		
		
		if(sr.isSuccess()) {
			List<String> results = sr.getResult();
			for(String result : results) {
				if(result.startsWith(root_path)) {
					String[] r = result.substring(root_path.length(), result.length()).split("/");
					if(log_names.contains(r[0])) {
						temp.put(r[0], r[1]);
					}
				}
			}
		} else {
			log.error("Shell Execute with command \"" + command + "\" execute failure.");
			throw new RuntimeException("Shell Execute with command \"" + command + "\" execute failure.");
		}
		
		return temp.asMap();
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.example.log.LogManager#getLog(java.lang.String, java.lang.String)
	 */
	@Override
	public byte[] getLog(String log_name, String file_name) {
		String logPath = root_path + log_name + "/" + file_name;
		File file = new File(logPath);
		if(!file.exists()) {
			return null;
		}
		byte[] bytes = null;
		try {
			bytes = FileUtils.readFileToByteArray(file);
		} catch (IOException e) {
			log.error("Error when read File " + logPath + " to ByteArray.", e);
			throw new RuntimeException("Error when read File " + logPath + " to ByteArray.", e);
		}
		return bytes;
	}

	public static void main(String[] args) {
//		Multimap<String, String> logs = new LogManagerImpl().getAllLogs();
//		for(String type : logs.keySet()) {
//			System.out.println("===========");
//			System.out.println(type);
//			Collection<String> files = logs.get(type);
//			for(String name : files) {
//				System.out.println(name);
//			}
//		}
//		System.out.println("=============");
//		List<String> logFiles = new LogManagerImpl().getLogsByLogName("client_master_protocol");
//		for(String name : logFiles) {
//			System.out.println("===========");
//			System.out.println(name);
//		}
//		System.out.println(new String(new LogManagerImpl().getLog("hahah", "log.log")));
		
		Map<String, Collection<String>> logs = new LogManagerImpl().findLogs(Lists.newArrayList("log_service"), "ha");
		for(String log : logs.keySet()) {
			System.out.println(log);
			Collection<String> files = logs.get(log);
			for(String file : files) {
				System.out.println(file);
			}
			
		}
	}
}
