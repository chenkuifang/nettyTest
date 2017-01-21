package com.phonepn.server.nettyStickyPacket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class TimeServerHandler extends ChannelInboundHandlerAdapter {
	private int counter;

	// 用于网络的读写操作
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		String body = (String) msg;
		System.out.println("客户端信息 : " + body + "the countor is:" + (++counter));
		
		String currentTime = getAns(body);
		// 增加/n 进行消息分割
		currentTime += System.getProperty("line.separator");
		ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
		ctx.writeAndFlush(resp);

		// 当客户端和服务端建立tcp成功之后，Netty的NIO线程会调用channelActive
		// 发送查询时间的指令给服务端。
		// 调用ChannelHandlerContext的writeAndFlush方法，将请求消息发送给服务端
		// 当服务端应答时，channelRead方法被调用
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		ctx.close();
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		super.handlerAdded(ctx);
		System.out.println(ctx.channel().id() + "来了");
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		super.handlerRemoved(ctx);
		System.out.println(ctx.channel().id() + "回去了");
	}

	private String getAns(String body) {
		if (body != null) {
			body = body.trim();
		} else {
			return "无效的body";
		}
		if (body.equals("1")) {
			return "一";
		} else if (body.equals("2")) {
			return "二";
		} else if (body.equals("3")) {
			return "三";
		}else{
			return "无效的body";
		}
		
	}

}
