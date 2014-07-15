package me.littlepanda.dadbear.core.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author 张静波 myplaylife@icloud.com 
 *
 */
public class Util {
	/**
	 * @return 获取本地hostname
	 * @throws UnknownHostException
	 */
	public static String getLocalHost() {
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			throw new RuntimeException("error when Util.getLocalHost().", e);
		}
		return addr.getHostName();
	}
	
	/**
	 * @return 获取本地ip
	 */
	public static String getLocalIp(){
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			throw new RuntimeException("error when Util.getLocalHost().", e);
		}
		return addr.getHostAddress();
	}
	
	/**
	 * 线程休眠
	 * @param millis
	 */
	public static void threadSleep(long millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new RuntimeException("error when thread sleep.", e);
		}
	}
	
	/**
	 * 判断字符串是否为空
	 * @param str
	 * @return
	 */
	public static boolean empty(String str){
		if(null == str || "".equals(str)){
			return true;
		}
		return false;
	}
	
	/**
	 * <p>根据ip获取hostname</p>
	 * <p>如果本地hosts文件中有远程ip和hostname的对应关系，则返回hostname，否则还是返回ip</p>
	 * @param ip
	 * @return
	 */
	public static String getHostNameByIp(String ip){
		InetAddress addr;
		try {
			addr = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			throw new RuntimeException("error when Util.getLocalHost().", e);
		}
		return addr.getHostName();
	}
	public static void main(String[] args) throws UnknownHostException {
		InetAddress ia = InetAddress.getLocalHost();
		System.out.println(ia.getHostAddress());
		System.out.println(ia.getHostName());
	}
}
