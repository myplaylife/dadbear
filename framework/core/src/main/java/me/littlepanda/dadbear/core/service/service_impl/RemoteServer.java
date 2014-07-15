package me.littlepanda.dadbear.core.service.service_impl;

import java.net.InetSocketAddress;

import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.reflect.ReflectResponder;

import me.littlepanda.dadbear.core.util.ReflectionUtils;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class RemoteServer extends AbstractServer {

	private Server server;
	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.service.Service#start()
	 */
	@Override
	public void startServer(Object implement) {
		server = new NettyServer(new ReflectResponder(ReflectionUtils.getClass(getProtocol()), implement), new InetSocketAddress(getPort()));
		server.start();
	}

	/* (non-Javadoc)
	 * @see me.littlepanda.dadbear.core.service.Service#stop()
	 */
	@Override
	public void stopServer() {
		server.close();
	}

}
