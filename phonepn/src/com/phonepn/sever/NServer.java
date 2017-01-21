package com.phonepn.sever;

import java.io.IOException;

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

/**
 * @author CKF
 * @date 2.17.1.13 Netty服务端
 */
public class NServer {

	public void bing(int port) throws IOException, InterruptedException {

		EventLoopGroup boosGroup = new NioEventLoopGroup();// 用于serversocketchannel的eventloop
		EventLoopGroup workerGroup = new NioEventLoopGroup();// 用于处理accept到的客户端channel
		try {
			// 服务端引导程序（Netty应用的开始）
			ServerBootstrap b = new ServerBootstrap();

			// 设置链接端和客户端的事件管理组
			b.group(boosGroup, workerGroup)
					// 该类用来生成一个通道实例
					.channel(NioServerSocketChannel.class)
					//
					.option(ChannelOption.SO_BACKLOG, 128)
					// 设置ChannelHandler用于服务通道的请求
					.childHandler(new ChildChannelHandler());

			// 等待返回与客户端绑定好的channel，通过future可以知道异步IO的状态
			ChannelFuture f = b.bind(port).sync();

			// 等待线程关闭
			f.channel().closeFuture().sync();

		} finally {

			boosGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}

	}

	/**
	 * offers an easy way to initialize a Channel once it was registered to its
	 * EventLoop <br>
	 * 提供一个注册在事件管理组的初始化方法通道方法 （配置如何接发信息)
	 * 
	 * @author gbee pipeline（传递）
	 */
	private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel arg0) throws Exception {

			// 利用netty默认解码器解决TCP粘包问题
			arg0.pipeline().addLast(new LineBasedFrameDecoder(1024));
			arg0.pipeline().addLast(new StringDecoder());
			// arg0.pipeline().addLast(new OutBoundHandler());
			arg0.pipeline().addLast(new InBoundHandler());// 最后
		}
	}

	public static void main(String... args) throws IOException, InterruptedException {
		new NServer().bing(23456);
	}

}