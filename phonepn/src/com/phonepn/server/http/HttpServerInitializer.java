package com.phonepn.server.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 *
 * 服务端channel(通道)初始化类
 * 
 * ChannelPipeline(管道)作用：负责管理和执行ChannelHandler <br>
 * 网络事件以事件流的形式在ChannelPipeline中流转，由ChannelPipeline根据ChannelHandler
 * 的执行策略调度ChannelHandler的执行
 * 
 * 
 * <br>
 * 典型的网络事件有：1.链路注册 2.链路激活 3.链路断开 4.接受到请求消息<br>
 * 5.请求消息接收并处理完毕 6.发送应答消息 7.链路发生异常 8.发生用户自定义事件
 * 
 * 
 * 系统提供的实用ChannelHandler<br>
 * 1.系统编解码框架：ByteToMessageCodec; 2.通用基于长度的半包解码器：LengthFieldBasedFrameDecoder;
 * 3.码流日志打印Handler:LoggingHandler; 4.SSL安全认证Handler:SslHandler; <br>
 * 5.链路空闲 检测Handler:IdleStateHandler;
 * 6.流量整形Handler:ChannelTrafficShapingHandler;<br>
 * 7.Base64编码器：Base64Decoder Base64Encoder;
 * 
 * 
 * 
 * 
 * @author CKF
 * @version 7.1
 * @data 2017年1月17日
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel sc) throws Exception {
		ChannelPipeline pipeline = sc.pipeline();

		/**
		 * http-request解码器<br>
		 * http服务器端对request解码
		 */
		pipeline.addLast("decoder", new HttpRequestDecoder());

		/**
		 * http-response解码器<br>
		 * http服务器端对response编码
		 */
		pipeline.addLast("encoder", new HttpResponseEncoder());
		/**
		 * 压缩 Compresses an HttpMessage and an HttpContent in gzip or deflate
		 * encoding while respecting the "Accept-Encoding" header. If there is
		 * no matching encoding, no compression is done.
		 */
		pipeline.addLast("deflater", new HttpContentCompressor());

		/**
		 * 聚合器将多个请求转为单一的FullHttpRequest或FullHttpResponse <br>
		 * 原因是http解码器在每个http消息中会生成多个消息对象 <br>
		 * 避免了channelRead多次调用
		 */
		pipeline.addLast("aggregator", new HttpObjectAggregator(65535));// 放在decoder以后

		/**
		 * 读超时,一般放在客户端，超时将关闭channle
		 */
		// pipeline.addLast("readTimeoutHandler", new ReadTimeoutHandler(50));

		/**
		 * 该处理器是对大码流的支持（文件传输）
		 */
		// pipeline.addLast("http-chunke", new ChunkedWriteHandler());

		/**
		 * 处理服务端
		 */
		pipeline.addLast("handler", new HttpServerrHandler());

		// 以下是https设置：
		// 安全的套接层 secure sockets layer(作用：在传输层对网络连接进行加密) https
		// if (isSSL) {
		// SSLEngine engine =
		// SecureChatSslContextFactory.getServerContext().createSSLEngine();
		// engine.setUseClientMode(false);  //非客户端模式
		// // engine.setNeedClientAuth(true);
		// pipeline.addLast("ssl", new SslHandler(engine));
		// }

	}

}
