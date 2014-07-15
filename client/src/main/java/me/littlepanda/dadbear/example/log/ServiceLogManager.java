package me.littlepanda.dadbear.example.log;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.littlepanda.dadbear.client.Client;
import me.littlepanda.dadbear.core.rpc.SlaveInfo;

/**
 * @author 张静波 zhang.jb@neusoft.com
 * 服务日志管理客户端
 */
public class ServiceLogManager {
	
	private static Log log = LogFactory.getLog(ServiceLogManager.class);
	
	public static final String LOG_SERVICE_NAME = "log_service";

	/**
	 * @return 获取所有日志文件的名称。包括所有主机上所有类别日志的所有文件名
	 */
	public static Map<String, Map<String, List<String>>> getAllLogs() {
		final Map<String, Map<String, List<String>>> logs = Maps.newHashMap();
		
		List<SlaveInfo> slaves = Client.getAllSlaveInfo();
		
		if(slaves == null || slaves.size() == 0) {
			return null;
		}
		
		final CountDownLatch latch = new CountDownLatch(slaves.size());
		
		List<Runnable> runs = Lists.newArrayList();
		for(final SlaveInfo slave : slaves) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					LogManager logManager = null;
					try {
						logManager = Client.getClientByServiceNameAndSlaveId(LOG_SERVICE_NAME, slave.getId().toString());
						Map<String, List<String>> log = logManager.getAllLogs();
						logs.put(slave.getHost().toString(), log);
					} finally {
						latch.countDown();
					}
				}
			};
			runs.add(r);
		}
		
		ExecutorService es = Executors.newFixedThreadPool(slaves.size());
		for(Runnable r : runs) {
			es.execute(r);
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			log.error("Error when await threads latch down.", e);
			throw new RuntimeException("Error when await threads latch down.", e);
		}
		
		return logs;
	}
	
	/**
	 * @param host 主机名称
	 * @return 获取某个主机上的所有日志文件名称，以日志类别分类
	 */
	public static Map<String, List<String>> getLogsByHostname(String host) {
		LogManager logManager = Client.getClientByServiceNameAndHostname(LOG_SERVICE_NAME, host);
		Map<String, List<String>> logs = logManager.getAllLogs();
		return logs;
	}
	
	/**
	 * @param host 主机名称
	 * @param log_name 日志类别
	 * @param file_name 文件名称
	 * @return 获取某个日志文件的内容
	 */
	public static byte[] getLogContent(String host, String log_name, String file_name) {
		LogManager logManager = Client.getClientByServiceNameAndHostname(LOG_SERVICE_NAME, host);
		byte[] content = logManager.getLog(log_name, file_name);
		return content;
	}
	
	/**
	 * @param pattern 需要查找的字符串
	 * @param hosts 主机名称，如果为null，表示所有slave节点
	 * @param log_names 日志类别，如果为null，表示所有类别
	 * @return 包含被查找字符串的所有文件列表
	 */
	public static Map<String, Map<String, Collection<String>>> findLog(final String pattern, List<String> hosts, final List<String> log_names) {
		final Map<String, Map<String, Collection<String>>> logs = Maps.newHashMap();
		
		/*
		 * 筛选出需要的slave节点
		 */
		List<SlaveInfo> slaves = Client.getAllSlaveInfo();
		List<SlaveInfo> need_slaves = Lists.newArrayList();
		for(SlaveInfo slave : slaves) {
			if(hosts.contains(slave.getHost().toString())) {
				need_slaves.add(slave);
			}
		}
		
		if(need_slaves.size() == 0) {
			return null;
		}
		
		final CountDownLatch latch = new CountDownLatch(need_slaves.size());
		
		List<Runnable> runs = Lists.newArrayList();
		for(final SlaveInfo slave : need_slaves) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					LogManager logManager = null;
					try {
						logManager = Client.getClientByServiceNameAndSlaveId(LOG_SERVICE_NAME, slave.getId().toString());
						Map<String, Collection<String>> log = logManager.findLogs(log_names, pattern);
						logs.put(slave.getHost().toString(), log);
					} finally {
						latch.countDown();
					}
				}
			};
			runs.add(r);
		}
		
		ExecutorService es = Executors.newFixedThreadPool(need_slaves.size());
		for(Runnable r : runs) {
			es.execute(r);
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			log.error("Error when await threads latch down.", e);
			throw new RuntimeException("Error when await threads latch down.", e);
		}
		
		return logs;
	}
	
	public static void main(String[] args) {
//		Map<String, Map<String, List<String>>> logs = ServiceLogManager.getAllLogs();
//		for(String host : logs.keySet()) {
//			System.out.println("===========");
//			System.out.println(host);
//			Map<String, List<String>> log = logs.get(host);
//			for(String log_name : log.keySet()) {
//				System.out.println("=========");
//				System.out.println(log_name);
//				List<String> files = log.get(log_name);
//				for(String file : files) {
//					System.out.println(file);
//				}
//			}
//		}
		
//		Map<String, List<String>> log = getLogsByHostname("gic206");
//		for(String name : log.keySet()) {
//			System.out.println(name);
//			List<String> l = log.get(name);
//			for(String file : l) {
//				System.out.println(file);
//			}
//		}
		
		byte[] bytes_1 = getLogContent("gic206", "fileconvertor_service", "log.log");
		System.out.println("1111111");
		byte[] bytes_2 = getLogContent("gic206", "fileconvertor_service", "log.log");
		System.out.println("2222222");
//		System.out.println(new String(bytes));
		
//		Map<String, Map<String, Collection<String>>> logs = findLog("INFO", Lists.newArrayList("gic206", "gic207", "gic209", "gic231"), Lists.newArrayList("log_service", "fileconvertor_service", "client_master_protocol", "indexsynchronizer_service", "slave_master_protocol", "solrmanager_service"));
//		for(String host : logs.keySet()) {
//			System.out.println(host);
//			Map<String, Collection<String>> log = logs.get(host);
//			for(String name : log.keySet()) {
//				System.out.println(name);
//				Collection<String> files = log.get(name);
//				for(String file : files) {
//					System.out.println(file);
//				}
//			}
//		}
		
	}
}	
