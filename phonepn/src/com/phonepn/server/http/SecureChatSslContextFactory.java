package com.phonepn.server.http;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import io.netty.util.internal.SystemPropertyUtil;

/**
 *
 * 管理SSLContext的工厂类(SSL类1)
 * 
 * @author CKF
 * @version 7.1
 * @data 2017年1月17日
 */
public class SecureChatSslContextFactory {
	private static final String PROTOCOL = "TLS";
	private static final SSLContext SERVER_CONTEXT;
	private static final SSLContext CLIENT_CONTEXT;

	// key位置
	private static String CLIENT_KEY_STORE = "E:\\javassl2\\sslclientkeys";
	private static String CLIENT_TRUST_KEY_STORE = "E:\\javassl2\\sslclienttrust";
	private static String CLIENT_KEY_STORE_PASSWORD = "123456";
	private static String CLIENT_TRUST_KEY_STORE_PASSWORD = "123456";

	private static String SERVER_KEY_STORE = "E:\\javassl2\\sslserverkeys";
	private static String SERVER_TRUST_KEY_STORE = "E:\\javassl2\\sslservertrust";
	private static String SERVER_KEY_STORE_PASSWORD = "123456";
	private static String SERVER_TRUST_KEY_STORE_PASSWORD = "123456";

	static {
		String algorithm = SystemPropertyUtil.get("ssl.KeyManagerFactory.algorithm");
		if (algorithm == null) {
			algorithm = "SunX509";
		}

		SSLContext serverContext;
		SSLContext clientContext;
		try {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(SERVER_KEY_STORE), SERVER_KEY_STORE_PASSWORD.toCharArray());
			KeyStore tks = KeyStore.getInstance("JKS");
			tks.load(new FileInputStream(SERVER_TRUST_KEY_STORE), SERVER_TRUST_KEY_STORE_PASSWORD.toCharArray());

			// Set up key manager factory to use our key store
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			kmf.init(ks, SERVER_KEY_STORE_PASSWORD.toCharArray());
			tmf.init(tks);

			// Initialize the SSLContext to work with our key managers.
			serverContext = SSLContext.getInstance(PROTOCOL);
			serverContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		} catch (Exception e) {
			throw new Error("Failed to initialize the server-side SSLContext", e);
		}

		try {
			KeyStore ks2 = KeyStore.getInstance("JKS");
			ks2.load(new FileInputStream(CLIENT_KEY_STORE), CLIENT_KEY_STORE_PASSWORD.toCharArray());

			KeyStore tks2 = KeyStore.getInstance("JKS");
			tks2.load(new FileInputStream(CLIENT_TRUST_KEY_STORE), CLIENT_TRUST_KEY_STORE_PASSWORD.toCharArray());
			// Set up key manager factory to use our key store
			KeyManagerFactory kmf2 = KeyManagerFactory.getInstance(algorithm);
			TrustManagerFactory tmf2 = TrustManagerFactory.getInstance("SunX509");
			kmf2.init(ks2, CLIENT_KEY_STORE_PASSWORD.toCharArray());
			tmf2.init(tks2);
			clientContext = SSLContext.getInstance(PROTOCOL);
			clientContext.init(kmf2.getKeyManagers(), tmf2.getTrustManagers(), null);
		} catch (Exception e) {
			throw new Error("Failed to initialize the client-side SSLContext", e);
		}

		SERVER_CONTEXT = serverContext;
		CLIENT_CONTEXT = clientContext;
	}

	public static SSLContext getServerContext() {
		return SERVER_CONTEXT;
	}

	public static SSLContext getClientContext() {
		return CLIENT_CONTEXT;
	}

	private SecureChatSslContextFactory() {
		// Unused
	}
}
