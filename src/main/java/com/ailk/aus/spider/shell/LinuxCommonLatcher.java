package com.ailk.aus.spider.shell;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.ailk.aus.spider.bean.AssetInfo;
import com.ailk.aus.spider.bean.ShellInfo;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import static com.ailk.aus.spider.utils.ShellUtils.*;

/**
 * Linux系统shell执行引擎
 * 
 * @author zhusy
 */
public class LinuxCommonLatcher extends ShellLatcher {

	@Override
	public String runner(AssetInfo asset, ShellInfo shell) throws Exception {
		Session session = null;
		ChannelExec openChannel = null;
		try {
			session = getSession(asset, times);
			openChannel = (ChannelExec) session.openChannel("exec");
			openChannel.setCommand(shell.getCommand());
			openChannel.connect();
			InputStream in = openChannel.getInputStream();
			return IOUtils.toString(in);
		} finally {
			if (openChannel != null) {
				openChannel.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}
		}
	}

}
