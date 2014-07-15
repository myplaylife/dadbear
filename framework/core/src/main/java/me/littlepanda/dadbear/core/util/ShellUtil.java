package me.littlepanda.dadbear.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

/**
 * @author 张静波 myplaylife@icloud.com
 * <p>shell指令执行</p>
 */
public class ShellUtil {
	
	private static Log log = LogFactory.getLog(ShellUtil.class);

	public static ShellResult executeShell(String shellCommand) throws IOException {
		boolean success = false;
		List<String> result = Lists.newArrayList();
		String pid_num = "";
		
		BufferedReader bufferedReader = null;
		try {
			Process pid = null;
			String[] cmd = { "/bin/sh", "-c", shellCommand };
			// 执行Shell命令
			pid = Runtime.getRuntime().exec(cmd);
			if (pid != null) {
				pid_num = pid.toString();
				//bufferedReader用于读取Shell的输出内容 
				bufferedReader = new BufferedReader(new InputStreamReader(pid.getInputStream()), 1024);
				pid.waitFor();
			}
			String line = null;
			// 读取Shell的输出内容，并添加到stringBuffer中
			while (bufferedReader != null
					&& (line = bufferedReader.readLine()) != null) {
				result.add(line);
			}
		} catch (Exception e) {
			log.error("", e);
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
			success = true;
		}
		ShellResult sr = new ShellUtil().new ShellResult(pid_num, success, result);
		return sr;
	}
	
	public class ShellResult {
		
		private boolean success = false;
		private List<String> result = Lists.newArrayList();
		private String pid;
		
		public ShellResult(String pid, boolean success, List<String> result) {
			this.pid = pid;
			this.success = success;
			this.result = result;
		}
		
		public String getPid() {
			return pid;
		}
		
		public boolean isSuccess() {
			return success;
		}

		public List<String> getResult() {
			return result;
		}
		
	}
	public static void main(String[] args) throws IOException {
		ShellResult sr = executeShell("netstat -apn | grep 8100");
		if(sr.isSuccess()) {
			List<String> result = sr.getResult();
			for(String str : result) {
				System.out.println(str);
			}
		}
	}
}
