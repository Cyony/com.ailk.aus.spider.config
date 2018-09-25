package com.ailk.aus.spider.bean;

import java.io.Serializable;

/**
 * 资产相关参数
 * 
 * @author zhusy
 */
public class AssetInfo implements Serializable {

	private static final long serialVersionUID = 6451266928196055388L;
	private String ip;
	private String userName;
	private String pwd;
	private int port;
	private int encryptionType;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getEncryptionType() {
		return encryptionType;
	}

	public void setEncryptionType(int encryptionType) {
		this.encryptionType = encryptionType;
	}

	@Override
	public String toString() {
		return "AssetInfo [ip=" + ip + ", userName=" + userName + ", pwd=" + pwd + ", port=" + port
				+ ", encryptionType=" + encryptionType + "]";
	}

}
