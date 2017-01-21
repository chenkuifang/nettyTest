package com.phonepn.server.util;

import java.net.URLClassLoader;
import java.util.ArrayList;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * 系统配置文件
 * 
 * @author CKF
 *
 */
public class Config {
	public static final String TOKEN = "xxxxx";
	public static final int MaxBuyerLimit = 500000;
	public static int Port = 8080;

	public static int MaxOnlineClient = 2000;

	public static int ThreadPool = 50;

	public static int SocketInputStreamReadNumber = 2048;

	public static String ServerAuth = "phonepn";

	public static int SoTimeout = 0;

	public static ArrayList<String> ClientIP = new ArrayList<>(5);

	public static String UserDir = "";

	public static URLClassLoader ClassLoader = null;

	private static long ServerStartTime = 0L;

	public static int BackLog = 1024;

	public synchronized long getServerStartTime() {
		return ServerStartTime;
	}

	public String init() {
		String ret = null;

		return null;
	}

	private String initClassLoader() {
		String ret = null;

		return ret;
	}

	private String parseConfigFile() {
		String ret = null;

		return ret;
	}

	private String parseConfigFile_log(JSONObject jsonCfg) {
		String ret = null;

		return ret;
	}

	private String parseConfigFile_clientIP(JSONObject jsonCfg) {
		String ret = null;

		return ret;
	}

	private String parseConfigFile_dataSource(JSONObject jsonCfg) {
		String ret = null;

		return ret;
	}

	private String parseConfigFile_dataSource2(JSONObject jsonCfg) {
		return null;
	}

	private String parseConfigFile_nadataPool(JSONObject jsonCfg) {
		return null;
	}

	public static String response(String ans) {
		return null;
	}

	public static String response(String ans, String extra) {
		return null;
	}
}