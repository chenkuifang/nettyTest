package com.phonepn.server.http;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONObject;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;/*
														import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
														import io.netty.handler.codec.http.cookie.Cookie;
														import io.netty.handler.codec.http.cookie.ServerCookieEncoder;*/
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;

import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;

/**
 *
 * 通道消息接收
 * 
 * @author CKF
 * @version 7.1
 * @data 2017年1月17日
 */
public class MessageReceived {

	private final StringBuilder responseContent = new StringBuilder();

	/**
	 * FullHttpRequest 表示是一个完整的请求，包括了HttpRequest和FullHttpMessage
	 */
	private FullHttpRequest request;
	private ChannelHandlerContext ctx;
	private FullHttpRequest msg;

	// post请求body的解码器
	HttpPostRequestDecoder decoder;

	public MessageReceived(ChannelHandlerContext ctx, FullHttpRequest msg) {
		this.ctx = ctx;
		this.msg = msg;
	}

	public MessageReceived(ChannelHandlerContext ctx, FullHttpRequest msg, HttpPostRequestDecoder decoder) {
		this.ctx = ctx;
		this.msg = msg;
		this.decoder = decoder;
	}

	/**
	 * 开始接收信息
	 * 
	 * @throws Exception
	 */
	public void received() throws Exception {
		// msg 实际为：DefaultHttpRequest 类
		// 判断msg是否为request类型
		if (msg instanceof HttpRequest) {
			dohttpRequest();
		} else {
			JSONObject ans = new JSONObject();
			ans.put("ans", "无效的请求方法");
			ByteBuf buf = Unpooled.copiedBuffer(ans.toString(), CharsetUtil.UTF_8);
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
			ctx.channel().writeAndFlush(response);
			ctx.channel().close();
		}
	}

	/**
	 * msg为 HttpRequest 处理http请求 <br>
	 * http请求包括get请求和post请求
	 */
	private void dohttpRequest() throws Exception {
		FullHttpRequest request = this.request = msg;
		// 判断做操作
		URI uri = new URI(request.uri());
		if (uri.getPath().equals("/favicon.ico")) {
			return;
		}

		// 空请求
		if (uri.getPath().equals("/")) {
			writeMenu(ctx);
			return;
		}

		// 获取请求的参数json
		JSONObject parms = getParameters(request);
		responseContent.setLength(0);
		responseContent.append(parms.toJSONString());
		writeResponse(ctx.channel());
	}

	/**
	 * 获取请求的参数(包括get 和 post)
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	private JSONObject getParameters(FullHttpRequest request) throws IOException {
		JSONObject paramsObject = new JSONObject();

		// 请求方法
		HttpMethod method = request.method();
		if (method == HttpMethod.GET) {
			return doGet();
		} else if (method == HttpMethod.POST) {
			return doPost();
		} else {
			paramsObject.put("ans", "无效的请求方法:" + method.name());
		}

		return paramsObject;
	}

	/**
	 * get操作（服务器通过获得参数，进行响应客户端） <br>
	 * 参数必须包含className,action
	 */
	private JSONObject doGet() {
		System.out.println("Get请求");
		JSONObject paramsObject = new JSONObject();
		paramsObject.put("method", "get");

		// 获取get参数---json
		QueryStringDecoder stringDecoder = new QueryStringDecoder(request.uri(), CharsetUtil.UTF_8);
		Map<String, List<String>> params = stringDecoder.parameters();

		if (!params.containsKey("className")) {
			paramsObject.put("ans", "参数列表必须包含className");
			return paramsObject;
		}

		if (!params.containsKey("action")) {
			paramsObject.put("ans", "参数列表必须包含action");
			return paramsObject;
		}

		Iterator<Entry<String, List<String>>> iterator = params.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, List<String>> next = iterator.next();
			System.out.println(next.getKey() + "---->" + next.getValue());
			paramsObject.put(next.getKey(), next.getValue().get(0));
		}

		// 回应客户端
		responseContent.append(paramsObject.toJSONString());
		writeResponse(ctx.channel());
		return paramsObject;
	}

	/**
	 * post操作（服务器根据获取的参数，进行数据库操作等）
	 */
	private JSONObject doPost() throws IOException {
		System.out.println("Post请求");
		JSONObject paramsObject = new JSONObject();

		paramsObject.put("method", "post");
		decoder.offer(request);// 初始化一个新的通道块

		List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();
		for (InterfaceHttpData parm : parmList) {
			Attribute data = (Attribute) parm;
			System.out.println(data.getName() + "---->" + data.getValue());
			paramsObject.put(data.getName(), data.getValue());
		}

		if (!paramsObject.containsKey("className")) {
			paramsObject.put("ans", "参数列表必须包含className");
			return paramsObject;
		}

		if (!paramsObject.containsKey("action")) {
			paramsObject.put("ans", "参数列表必须包含action");
			return paramsObject;
		}

		return paramsObject;
	}

	/**
	 * 回应给客户端
	 * 
	 * @param channel
	 */
	private void writeResponse(Channel channel) {

		responseContent.append(System.lineSeparator());
		// 回应内容
		ByteBuf buf = Unpooled.copiedBuffer(responseContent.toString(), CharsetUtil.UTF_8);

		// 创建响应对象，参数格式：版本，状态，内容
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
		// 设置响应头(HttpHeadersNames.CONTENT_TYPE)
		response.headers().set("content_type", "text/html; charset=utf-8");

		// 是否为最后响应,如果不是keep_alive,最后一条消息发送后，服务端是会主动关闭的
		boolean close = HttpUtil.isKeepAlive(request);

		if (!close) {
			response.headers().set("Content-Length", buf.readableBytes());
		}

		// Cookie cookie = null;
		// String value = request.headers().get("cookie");
		// if (value == null) {
		// // cookies = Collections.emptySet();
		// } else {
		// cookie = ClientCookieDecoder.STRICT.decode(value);
		// }
		// if (cookie != null) {
		// response.headers().add("Set-Cookie",
		// ServerCookieEncoder.STRICT.encode(cookie));
		// }

		// 通道响应写出
		ChannelFuture future = channel.writeAndFlush(response);

		// 写出操作完成后 关闭通道
		if (close) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}

	private void writeMenu(ChannelHandlerContext ctx) {
		responseContent.setLength(0);

		// create Pseudo Menu
		responseContent.append("<html>");
		responseContent.append("<head>");
		responseContent.append("<title>Netty Test Form</title>\r\n");
		responseContent.append("</head>\r\n");
		responseContent.append("<body bgcolor=white><style>td{font-size: 12pt;}</style>");

		// GET
		responseContent.append("<CENTER>GET FORM<HR WIDTH=\"75%\" NOSHADE color=\"blue\"></CENTER>");
		responseContent.append("<FORM ACTION=\"/from-get\" METHOD=\"GET\">");
		responseContent.append("<table border=\"0\">");
		responseContent.append("<tr><td>Fill with value: <br> <input type=text name=\"action\" size=10></td></tr>");
		responseContent.append("<tr><td><INPUT TYPE=\"submit\" NAME=\"Send\" VALUE=\"Send\"></INPUT></td>");
		responseContent.append("</table></FORM>\r\n");
		responseContent.append("<CENTER><HR WIDTH=\"75%\" NOSHADE color=\"blue\"></CENTER>");

		// POST
		responseContent.append("<CENTER>POST FORM<HR WIDTH=\"75%\" NOSHADE color=\"blue\"></CENTER>");
		responseContent.append("<FORM ACTION=\"/from-post\" METHOD=\"POST\">");
		responseContent.append("<table border=\"0\">");
		responseContent.append("<tr><td>Fill with value: <br> <input type=text name=\"action\" size=20>");
		responseContent.append("</td></tr>");
		responseContent.append("<tr><td><input TYPE=\"submit\" name=\"Send\" value=\"Send\"></input></td>");
		responseContent.append("</table></FORM>\r\n");
		responseContent.append("<CENTER><HR WIDTH=\"75%\" NOSHADE color=\"blue\"></CENTER>");
		responseContent.append("<CENTER><HR WIDTH=\"75%\" NOSHADE color=\"blue\"></CENTER>");

		responseContent.append("</body>");
		responseContent.append("</html>");

		ByteBuf buf = Unpooled.copiedBuffer(responseContent.toString(), CharsetUtil.UTF_8);
		// 默认响应http
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);

		response.headers().set("content_type", "text/html; charset=UTF-8");
		response.headers().set("Content-Length", buf.readableBytes());

		// 通道输出
		ctx.channel().writeAndFlush(response);
	}
}
