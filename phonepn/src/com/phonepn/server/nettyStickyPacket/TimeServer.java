package com.phonepn.server.nettyStickyPacket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.CharsetUtil;

public class TimeServer {
	public void bind(int port) throws Exception {
		/* 配置服务端的NIO线程组 */
		// NioEventLoopGroup类 是个线程组，包含一组NIO线程，用于网络事件的处理
		// （实际上它就是Reactor线程组）。
		// 创建的2个线程组，1个是服务端接收客户端的连接，另一个是进行SocketChannel的
		// 网络读写
		EventLoopGroup bossGroup = new NioEventLoopGroup();// 用于接收客户端连接的连接池
		EventLoopGroup WorkerGroup = new NioEventLoopGroup();// 处理I/O读写的线程池

		try {
			// ServerBootstrap 类，是启动NIO服务器的辅助启动类
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, WorkerGroup);
			b.channel(NioServerSocketChannel.class);
			b.option(ChannelOption.SO_BACKLOG, 1024);// option是Netty对TCP的封装，所以option能设置TCP

			b.childHandler(new ChildChannelHandler());

			// 绑定端口,同步等待成功
			ChannelFuture f = b.bind(port).sync();

			// 等待服务端监听端口关闭
			f.channel().closeFuture().sync();
		} finally {
			// 释放线程池资源
			bossGroup.shutdownGracefully();
			WorkerGroup.shutdownGracefully();
		}
	}

	private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
		@Override
		protected void initChannel(SocketChannel arg0) throws Exception {
			// 增加 LineBasedFrameDecoder 和StringDecoder编码器
			arg0.pipeline().addLast(new LineBasedFrameDecoder(1024));// 必须包括/r/n
																		// 或者/n分行
			// arg0.pipeline().addLast(new StringDecoder());
			// StringDecoder解码器（将接收到的的bytebuf转为string，该解码器必须和以上两种解码器中的一种使用）
			arg0.pipeline().addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));
			// 以上用stringDecoder进行了解码为字符串，所以在该handler可直接输出字符串
			arg0.pipeline().addLast(new TimeServerHandler());
		}
	}

	public static void main(String[] args) throws Exception {
		int port = 8080;
		new TimeServer().bind(port);
	}
}
