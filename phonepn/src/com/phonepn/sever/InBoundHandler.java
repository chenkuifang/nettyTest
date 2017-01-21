package com.phonepn.sever;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 处理接收到数据时的时间<br>
 * ChannelInboundHandler就是用来处理我们的核心业务逻辑（处理数据的读）。
 * 
 * @author gbee
 *
 */
public class InBoundHandler extends ChannelInboundHandlerAdapter {
	private int count = 0;

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		super.handlerAdded(ctx);
		System.out.println(ctx.channel().id() + "进来了");
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		super.handlerRemoved(ctx);
		System.out.println(ctx.channel().id() + "离开了");
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// String body = (String) msg;
		// System.out.println("from client：" + body);
		//
		// // 应答
		// String anString = "客户端你好";
		// ByteBuf resp = Unpooled.copiedBuffer(anString.getBytes());
		// ctx.write(resp);

		// ByteBuf result = (ByteBuf) msg;
		// byte[] result1 = new byte[result.readableBytes()];
		// result.readBytes(result1);
		// String resultStr = new String(result1);
		// System.out.println("Client said:" + resultStr);
		//
		// result.release();
		// ctx.write(msg);

		String body = (String) msg;
		System.out.println("body" + body + ";count:" + ++count);

		String currentTime = "I am OK";
		currentTime = currentTime + System.currentTimeMillis();
		ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
		ctx.writeAndFlush(resp);

	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();// 将缓冲区内消息写到SocketChannel中,就行发送到对方
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}

}