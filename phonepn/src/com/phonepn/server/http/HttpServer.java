package com.phonepn.server.http;

import com.phonepn.server.util.Config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 *
 * HTTP协议栈请求服务端
 * 
 * @author CKF
 * @version 7.1
 * @data 2017年1月17日
 */
public class HttpServer {
	public static boolean isSSL;

	public HttpServer(int port) throws Exception {
		bind(port);
	}

	// 入口函数
	public static void main(String... args) throws Exception {
		int port;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		} else {
			port = 8080;
		}
		if (args.length > 1) {
			isSSL = true;
		}
		new HttpServer(port);
	}

	/**
	 * 绑定客户端
	 * 
	 * @param host
	 * @param port
	 * @throws Exception
	 */
	public void bind(int port) throws Exception {
		// 自定义Task和定时任务都交由EventLoopGroup执行，统一调度，提升I/O线程的处理和性能
		// 创建两个线程池
		NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
		NioEventLoopGroup workerGroup = new NioEventLoopGroup(4);

		try {

			// NIO启动+配置
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);// 指定服务端channel类型
			b.option(ChannelOption.SO_BACKLOG, Config.BackLog);

			// 初始化服务器端handler
			b.childHandler(new HttpServerInitializer());

			// 绑定端口,同步等待成功,成功后返回channel
			Channel channel = b.bind(port).sync().channel();
			// b.bind(port).sync()返回 ChanelFuture,可增加监听器，进行连接监听
			System.out.println("Open your browser and navigate to http://localhost:" + port + '/');

			channel.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

}
