package com.ailk.aus.spider.shell;

import static com.ailk.aus.spider.utils.ShellUtils.getPattern;
import static com.ailk.aus.spider.utils.ShellUtils.getSession;

import java.util.ArrayList;
import java.util.List;

import com.ailk.aus.spider.bean.AssetInfo;
import com.ailk.aus.spider.bean.ShellInfo;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;

import expect4j.Expect4j;
import expect4j.StreamPair;
import expect4j.matches.Match;

/**
 * 思科防火墙执行器
 * 
 * @author zhusy
 */
public class CiscoFireWallLatcher extends ShellLatcher {

	private static List<String> promptRegEx;

	static {
		StreamPair.setCharset("UTF-8");
		promptRegEx = new ArrayList<>();
		promptRegEx.add("pix2>");
		promptRegEx.add("Password:");
		promptRegEx.add("<--- More --->");
		promptRegEx.add("pix2#");
	}

	@Override
	public String runner(AssetInfo asset, ShellInfo shell) throws Exception {
		Session session = null;
		ChannelShell channel = null;
		try {
			session = getSession(asset, times);
			channel = (ChannelShell) session.openChannel("shell");
			Expect4j expect = new Expect4j(channel.getInputStream(), channel.getOutputStream());
			channel.connect(3000);
			StringBuffer buffer = new StringBuffer();
			List<Match> lstPattern = getPattern(promptRegEx, buffer);
			System.out.println(expect.expect(lstPattern));
			expect.send("en" + ENDCHAR);
			System.out.println(expect.expect(lstPattern));
			expect.send(asset.getPwd() + ENDCHAR);
			System.out.println(expect.expect(lstPattern));
			expect.send(shell.getCommand() + ENDCHAR);
			System.out.println(expect.expect(lstPattern));
			return buffer.toString();
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
