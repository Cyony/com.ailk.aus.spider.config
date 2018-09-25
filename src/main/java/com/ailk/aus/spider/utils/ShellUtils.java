package com.ailk.aus.spider.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.oro.text.regex.MalformedPatternException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ailk.aus.spider.bean.AssetInfo;
import com.ailk.aus.spider.bean.ShellInfo;
import com.ailk.aus.spider.sftp.SftpClient;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import expect4j.Expect4j;
import expect4j.matches.Match;
import expect4j.matches.RegExpMatch;
import expect4j.matches.TimeoutMatch;
import scala.reflect.generic.Trees.This;

/**
 * 操作脚本的工具类,提供session获取以及ssh shell客户端上传等功能
 * 
 * @author zhusy
 */
public class ShellUtils {

	private static Logger logger = LoggerFactory.getLogger(This.class);

	public static final long DEFAULTTIMEOUT = 6000;
	public static File shellDir = new File("./shell");

	public static void createAndUploadShellFiles(List<ShellInfo> shells, AssetInfo asset) throws Exception {
		SftpClient sftp = new SftpClient(asset.getIp(), asset.getUserName(), asset.getPwd(), asset.getPort());
		// create shell file
		for (ShellInfo shell : shells) {
			File file = new File(shellDir + "/" + shell.getTitle());
			FileUtils.writeStringToFile(file, shell.getContent());
			FileInputStream inputStream = new FileInputStream(file);
			sftp.upload(".shell", shell.getTitle(), inputStream);
			inputStream.close();
			file.delete();
		}
		sftp.close();
	}

	@Deprecated
	public static Session getSession(AssetInfo asset) throws JSchException {
		return getSession(asset, 3);
	}

	public static Session getSession(AssetInfo asset, int times) throws JSchException {
		Session session = null;
		boolean exception = false;
		int now = 1;
		if (times <= 1) {
			exception = true;
		}
		while ((session = connectSession(asset, exception)) == null) {
			now++;
			logger.warn(Thread.currentThread() + ",try to reconnect again:" + now);
			if (now >= times) {
				exception = true;
			}
		}
		return session;
	}

	private static Session connectSession(AssetInfo asset, boolean excepetion) throws JSchException {
		Session session = null;
		try {
			JSch jsch = new JSch();
			session = jsch.getSession(asset.getUserName(), asset.getIp(), asset.getPort());
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.setPassword(asset.getPwd());
			session.connect(3000);
		} catch (JSchException e) {
			if (session != null) {
				session.disconnect();
				session = null;
			}
			if (excepetion) {
				throw e;
			}

		}
		return session;
	}

	public static Expect4j getExpect(Session session) throws Exception {
		ChannelShell channel = (ChannelShell) session.openChannel("shell");
		Expect4j expect = new Expect4j(channel.getInputStream(), channel.getOutputStream());
		channel.connect(3000);
		return expect;
	}

	public static List<Match> getPattern(List<String> promptRegEx, StringBuffer buffer)
			throws MalformedPatternException {
		List<Match> lstPattern = new ArrayList<Match>();
		synchronized (promptRegEx) {
			for (String regexElement : promptRegEx) {
				RegExpMatch mat = new RegExpMatch(regexElement, x -> {
					buffer.append(x.getBuffer());
					// x.exp_continue();
				});
				lstPattern.add(mat);
			}
		}
		lstPattern.add(new TimeoutMatch(DEFAULTTIMEOUT, x -> {
		}));
		return lstPattern;
	}

}
