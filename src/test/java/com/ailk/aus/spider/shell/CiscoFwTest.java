package com.ailk.aus.spider.shell;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.ailk.aus.spider.bean.AssetInfo;
import com.ailk.aus.spider.bean.ShellInfo;

/**
 * @ClassName: LinuxLatcherTest
 * @Description: LinuxLatcher 测试类
 * @author zhusy
 * @date 2018年4月28日 下午2:51:50
 */
public class CiscoFwTest {

	@Test
	public void testExcute() throws Exception {
		CiscoFireWallLatcher linux = new CiscoFireWallLatcher();
		Map<String, Future<String>> map = new HashMap<>();
		AssetInfo asset = new AssetInfo();
		asset.setIp("10.1.195.253");
		asset.setPort(22);
		asset.setPwd("qwer1234");
		asset.setUserName("pix");
		ShellInfo shell = new ShellInfo();
		shell.setCommand("show configuration");
		for (int i = 0; i < 1; i++) {
			map.put(i + "", linux.excute(asset, shell));
		}

		map.entrySet().stream().map(entry -> {
			try {
				return entry.getValue().get(20, TimeUnit.SECONDS);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(String.format("%s task has been timeout ,excute failed", entry.getKey()));
			}
			return null;
		}).filter(StringUtils::isNotEmpty).forEach(System.out::println);

	}

}
