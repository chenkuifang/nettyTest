package com.phonepn.server.ssl.test;

/**
 *
 * 类说明
 * 
 * @author CKF
 * @version 7.1
 * @data 2017年1月23日
 */
public class SSL {
	static final boolean SSL = System.getProperty("ssl") != null;

	public static void main(String... strings) {
		
		System.out.println(System.getProperty("ssl"));
	}

}
