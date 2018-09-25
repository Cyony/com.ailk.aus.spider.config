package com.ailk.aus.spider.shell;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ailk.aus.spider.bean.AssetInfo;
import com.ailk.aus.spider.bean.FortressInfo;
import com.ailk.aus.spider.bean.ShellInfo;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;

import expect4j.Expect4j;
import expect4j.StreamPair;
import expect4j.matches.Match;
import scala.reflect.generic.Trees.This;

import static com.ailk.aus.spider.utils.ShellUtils.*;

/**
 * 江苏移动堡垒机执行器
 * 
 * @author zhusy
 */
public class JsMobileLatcher extends ShellLatcher {

	private Logger logger = LoggerFactory.getLogger(This.class);
	private static List<String> promptRegEx;
	private static AssetInfo fortressAsset = new AssetInfo();

	@Override
	public void init(Object o) {
		if (o instanceof FortressInfo) {
			StreamPair.setCharset("GBK");
			promptRegEx = ((FortressInfo) o).getPrompts();
			fortressAsset.setIp(((FortressInfo) o).getIp());
			fortressAsset.setPort(((FortressInfo) o).getPort());
			fortressAsset.setPwd(((FortressInfo) o).getPwd());
			fortressAsset.setUserName(((FortressInfo) o).getUserName());
		} else {
			throw new RuntimeException("invalid object for init method:" + o + ",we except for a type of FortressInfo");
		}
	}

	@Override
	public String runner(AssetInfo asset, ShellInfo shell) throws Exception {
		Session session = null;
		ChannelShell channel = null;
		try {
			session = getSession(fortressAsset, times);
			channel = (ChannelShell) session.openChannel("shell");
			Expect4j expect = new Expect4j(channel.getInputStream(), channel.getOutputStream());
			channel.connect(3000);
			StringBuffer buffer = new StringBuffer();
			List<Match> lstPattern = getPattern(promptRegEx, buffer);
			fortressVerify(() -> {
				expect.expect(lstPattern);
				expect.send("1");
				expect.expect(lstPattern);
				expect.send("1");
				expect.expect(lstPattern);
				String connectStr = asset.getUserName() + "@" + asset.getIp() + ":" + asset.getPort();
				logger.debug("connect to remote host:" + connectStr);
				expect.send(connectStr + ENDCHAR);
				expect.expect(lstPattern);
			});
			expect.send(shell.getCommand() + ENDCHAR);
			expect.expect(lstPattern);
			return StringUtils.substringBeforeLast(
					StringUtils.substringAfter(buffer.toString(), shell.getCommand()).trim(), "\r\n").trim();
		} finally {
			if (channel != null) {
				channel.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}
		}

	}

}
