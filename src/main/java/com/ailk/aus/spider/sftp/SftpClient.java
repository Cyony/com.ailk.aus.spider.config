package com.ailk.aus.spider.sftp;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * sftp客户端
 * 
 * @author zhusy
 */
public class SftpClient {

	private Logger logger = LoggerFactory.getLogger(SftpClient.class);
	private ChannelSftp sftp;
	private Session sshSession;

	/**
	 * 客户端构造函数,根据参数连接获取channel
	 * 
	 * @param server
	 *            服务器
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 * @param port
	 *            端口
	 * @throws Exception
	 */
	public SftpClient(String server, String username, String password, int port) throws Exception {
		JSch jsch = new JSch();
		sshSession = jsch.getSession(username, server, port);
		sshSession.setPassword(password);
		Properties sshConfig = new Properties();
		sshConfig.put("StrictHostKeyChecking", "no");
		sshSession.setConfig(sshConfig);
		sshSession.connect();
		Channel channel = sshSession.openChannel("sftp");
		channel.connect();
		sftp = (ChannelSftp) channel;
	}

	/**
	 * 关闭客户端(关系seesion以及和seesion相关的channel)
	 */
	public void close() {
		if (sftp != null) {
			sftp.quit();
			logger.info("sftp is closed");
		}
		if (sshSession != null && sshSession.isConnected()) {
			sshSession.disconnect();
			logger.info("sshSession is closed");
		}
	}

	/**
	 * 将输入流的数据上传到sftp作为文件
	 * 
	 * @param directory
	 *            上传到该目录
	 * @param sftpFileName
	 *            sftp端文件名
	 * @param input
	 *            输入流
	 * @throws SftpException
	 * @throws Exception
	 */
	public void upload(String directory, String sftpFileName, InputStream input) throws SftpException {
		try {
			sftp.cd(directory);
		} catch (SftpException e) {
			logger.warn("directory is not exist");
			sftp.mkdir(directory);
			sftp.cd(directory);
		}
		sftp.put(input, sftpFileName);
		logger.info("file:{} is upload successful", sftpFileName);
	}

}
