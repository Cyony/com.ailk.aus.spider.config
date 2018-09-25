package com.ailk.aus.spider.shell;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.ailk.aus.spider.bean.AssetInfo;
import com.ailk.aus.spider.bean.FortressInfo;
import com.ailk.aus.spider.bean.ShellInfo;

public class Jstest {

	public static void main(String[] args)
			throws InterruptedException, ExecutionException, TimeoutException, Exception {
		JsMobileLatcher latcher = new JsMobileLatcher();
		FortressInfo fottress = new FortressInfo();
		fottress.setIp("10.32.222.26");
		fottress.setPort(50075);
		fottress.setUserName("yx_sjcj");
		fottress.setPwd("put9sH4d");
		fottress.setPrompts(Arrays.asList("\\$", "登陆：", "数据库", "port）"));
		ShellInfo shellInfo = new ShellInfo();
		shellInfo.setCommand(args[0]);

		AssetInfo assetInfo = new AssetInfo();
		assetInfo.setIp("10.32.222.172");
		assetInfo.setUserName("toptea");
		assetInfo.setPort(22);
		latcher.init(fottress);
		try {
			latcher.excute(assetInfo, shellInfo).get(300, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
