package com.phonepn.server.http;

import com.alibaba.fastjson.JSONObject;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.util.CharsetUtil;

/**
 * 处理http请求，返回http响应（返回响应给浏览器）<br>
 * SimpleChannelInboundHandler extends ChannelInboundHandlerAdapter<br>
 * 进行了字符串编码 <br>
 * FullHttpRequest 表示是一个完整的请求，包括了HttpRequest和FullHttpMessage
 * 
 * @author CKF
 * @version 7.1
 * @data 2017年1月17日
 */
public class HttpServerrHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	// 使用如果httpData超出minsize，将存在硬盘上，否则存在内存里
	private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

	/**
	 * 解码post请求的body,需注意：最后要destroy();
	 */
	private HttpPostRequestDecoder decoder;

	/**
	 * 注释 fire <br>
	 * 引发的意思 fireChannelActive---- channelActive() 一个通道在活动，意思是通道连接成功<br>
	 * 
	 * fireChannelInactive-----channelInactive() 一个通道不活动，意思是通道关闭<br>
	 * 
	 * fireExceptionCaught-----exceptionCaught 通道在绑定操作的时候引发异常<br>
	 * 
	 * fireUserEventTriggered----userEventTriggered() 触发用户注册的事件<br>
	 * 
	 * fireChannelRead---channelRead() 通道接收一个信息<br>
	 * 
	 * fireChannelReadComplete----channelReadComplete() 通道接收完成
	 * 
	 */

	/**
	 * Please keep in mind that this method will be renamed to
	 * messageReceived(ChannelHandlerContext, I) in 5.0.
	 * 在5.0的时候该方法更名为messageReceived <br>
	 * FullHttpRequest 表示是一个完整的请求，包括了HttpRequest和FullHttpMessage
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		// System.out.println(msg.toString()); // msg 包括http 协议栈的请求行，请求头，请求内

		if (request.decoderResult().isSuccess()) {
			decoder = new HttpPostRequestDecoder(factory, request);
			new MessageReceived(ctx, request, decoder).received();
		} else {
			sendError(ctx, "请求解析出错！");
			return;
		}
	}

	/**
	 * 错误处理
	 * 
	 * @param ctx
	 */
	private static void sendError(ChannelHandlerContext ctx, String errStr) {
		JSONObject ans = new JSONObject();
		ans.put("ans", errStr);
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
				Unpooled.copiedBuffer(ans.toJSONString(), CharsetUtil.UTF_8));
		response.headers().set("content_type", "text/html; charset=utf-8");
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * 出现异常的时候
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("出现异常");
		if (decoder != null) {
			decoder.destroy();
		}
		ctx.channel().close();
	}

	/**
	 * channel关闭时触发
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (decoder != null) {
			decoder.cleanFiles();
		}
	}
}
