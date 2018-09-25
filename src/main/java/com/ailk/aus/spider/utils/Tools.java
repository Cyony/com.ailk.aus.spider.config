package com.ailk.aus.spider.utils;

import java.util.Base64;

/**
 * spider工具类
 * 
 * @author zhusy
 */
public class Tools {

	/**
	 * @param type
	 *            加密类型
	 * @param pwd
	 *            原密码
	 * @return 解密后的密码
	 */
	public static String decode(int type, String pwd) {
		switch (type) {
		case 1:
			pwd = new String(Base64.getDecoder().decode(pwd));
			break;
		default:
			break;
		}
		return pwd;
	}

}
