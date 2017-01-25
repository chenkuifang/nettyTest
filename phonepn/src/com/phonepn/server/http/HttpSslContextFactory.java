package com.phonepn.server.http;

import java.security.KeyStore;
import java.security.Security;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

/**
 *
 * SSL服务端认证类2
 * 
 * @author CKF
 * @version 7.1
 * @data 2017年1月23日
 */
public class HttpSslContextFactory {
	// private static final Logger LOGGER =
	// Logger.getLogger(HttpSslContextFactory.class);
	private static final String PROTOCOL = "SSLv3";
	/** 针对于服务器端配置 */
	private static SSLContext SSLCONTEXT = null;

	static {
		// 采用的加密算法

		String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
		if (algorithm == null) {
			algorithm = "SunX509";
		}

		SSLContext serverContext = null;
		try {
			// 访问Java密钥库，JKS是keytool创建的Java密钥库，保存密钥。

			KeyStore ks = KeyStore.getInstance("JKS");
			//ks.load(HttpsKeyStore.getKeyStoreStream(), HttpsKeyStore.getKeyStorePassword());
			
			// 创建用于管理JKS密钥库的密钥管理器。
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
			//kmf.init(ks, HttpsKeyStore.getCertificatePassword());

			// 构造SSL环境，指定SSL版本为3.0，也可以使用TLSv1，但是SSLv3更加常用。

			serverContext = SSLContext.getInstance(PROTOCOL);

			// 初始化SSL环境。第二个参数是告诉JSSE使用的可信任证书的来源，设置为null是从javax.net.ssl.trustStore中获得证书。第三个参数是JSSE生成的随机数，这个参数将影响系统的安全性，设置为null是个好选择，可以保证JSSE的安全性。

			serverContext.init(kmf.getKeyManagers(), null, null);
		} catch (Exception e) {
			//LOGGER.error("初始化客户端SSL失败", e);
			throw new Error("Failed to initialize the server SSLContext", e);
		}

		SSLCONTEXT = serverContext;
	}

	/**
	 * 获取SSLContext实例
	 *
	 * @author linfenliang
	 * @date 2012-9-11
	 * @version V1.0.0
	 * @return SSLContext
	 */
	public static SSLContext getServerContext() {

		return SSLCONTEXT;
	}
}
