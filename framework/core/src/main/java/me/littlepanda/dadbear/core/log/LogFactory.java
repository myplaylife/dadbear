package me.littlepanda.dadbear.core.log;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.log4j.PropertyConfigurator;

import com.google.common.collect.Maps;
import me.littlepanda.dadbear.core.config.CommonConfigurationKeys;
import me.littlepanda.dadbear.core.config.ConfigurationUtil;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class LogFactory {
	
	private static final String LOGGER = "log4j.logger.${name}";
	private static final String APPENDER = "log4j.appender.${name}";
	private static final String FILE = "log4j.appender.${name}.File";
	private static final String MAXIMUM_FILESIZE = "log4j.appender.${name}.MaxFileSize";
	private static final String MAXSIZE_BACKUPS = "log4j.appender.${name}.MaxBackupIndex";
	private static final String LAYOUT = "log4j.appender.${name}.layout";
	private static final String CONVERSION_PATTERN = "log4j.appender.${name}.layout.ConversionPattern";
	
	private static final String PATTERN = "\\$\\{name\\}";
	
	private static String level;
	private static String appender;
	private static String maximum_filesize;
	private static String root_path;
	private static String maxsize_backups;
	private static String layout;
	private static String conversion_pattern;
	private static String file_name;
	
	private static Map<String, Log> cache = Maps.newHashMap();
	
	static {
		Configuration conf = ConfigurationUtil.getInitConfig();
		level = conf.getString(CommonConfigurationKeys.LOG_LEVEL);
		appender = conf.getString(CommonConfigurationKeys.LOG_APPENDER);
		maximum_filesize = conf.getString(CommonConfigurationKeys.LOG_MAXIMUM_FILESIZE);
		root_path = conf.getString(CommonConfigurationKeys.LOG_ROOT_PATH);
		maxsize_backups = conf.getString(CommonConfigurationKeys.LOG_BACKUPS);
		layout = conf.getString(CommonConfigurationKeys.LOG_LAYOUT);
		conversion_pattern = conf.getString(CommonConfigurationKeys.LOG_CONVERSIONPATTERN);
		file_name = conf.getString(CommonConfigurationKeys.LOG_FILE_NAME);
	}
	
	public static Log getLog(String name) {
		/**
		 * 为每个服务做一个logger
		 */
		String logger = LOGGER.replaceAll(PATTERN, name);
		String appender = APPENDER.replaceAll(PATTERN, name);
		String file = FILE.replaceAll(PATTERN, name);
		String maximum_filesize = MAXIMUM_FILESIZE.replaceAll(PATTERN, name);
		String maxsize_backups = MAXSIZE_BACKUPS.replaceAll(PATTERN, name);
		String layout = LAYOUT.replaceAll(PATTERN, name);
		String conversion_pattern = CONVERSION_PATTERN.replaceAll(PATTERN, name);

		/**
		 * 拼装logger参数
		 */
		Properties pro = new Properties();
		pro.put(logger, LogFactory.level + "," + name);
		pro.put(appender, LogFactory.appender);
		pro.put(file, root_path.lastIndexOf("/") == root_path.length()-1 ? root_path + name + "/" + file_name : root_path + "/" + name + "/" + file_name);
		pro.put(maximum_filesize, LogFactory.maximum_filesize);
		pro.put(maxsize_backups, LogFactory.maxsize_backups);
		pro.put(layout, LogFactory.layout);
		pro.put(conversion_pattern, LogFactory.conversion_pattern);
		PropertyConfigurator.configure(pro);
		
		Log log = org.apache.commons.logging.LogFactory.getLog(name);
		
		cache.put(name, log);
		
		return log;
	}
	public static void main(String[] args) {
//		String a = "hello, ${name}!";
//		String name = "zhang";
//		String c = a.replaceAll("\\$\\{name\\}", name);
//		System.out.println(c);
		
		Log log = getLog("hahah");
		log.error("heiheiheiheiehi");
		
	}
}
