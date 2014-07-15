package me.littlepanda.dadbear.core.service;

/**
 * @author 张静波 myplaylife@icloud.com
 * <p>因为NettyTransceiver是远程对象，无法直接保存在Map中，因此做了一个包装再放入。</p>
 */
public class ProtocolWrapper {
	
	private Object protocol;
	
	public ProtocolWrapper(Object protocol) {
		this.protocol = protocol;
	}

	public Object getProtocol() {
		return this.protocol;
	}	
}	
