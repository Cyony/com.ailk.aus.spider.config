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
public class LinuxLatcherTest {

	@Test
	public void testExcute() throws Exception {
		LinuxCommonLatcher linux = new LinuxCommonLatcher();
		Map<String, Future<String>> map = new HashMap<>();
		AssetInfo asset = new AssetInfo();
		asset.setIp("10.21.17.212");
		asset.setPort(22);
		asset.setPwd("aus123");
		asset.setUserName("aus");
		ShellInfo shell = new ShellInfo();
		shell.setCommand("hostname");
		for (int i = 0; i < 5; i++) {
			map.put(i + "", linux.excute(asset, shell));
		}

		map.entrySet().stream().map(entry -> {
			try {
				return entry.getValue().get(1, TimeUnit.MICROSECONDS);
			} catch (Exception e) {
				System.out.println(String.format("%s task has been timeout ,excute failed", entry.getKey()));
			}
			return null;
		}).filter(StringUtils::isNotEmpty).forEach(System.out::println);

	}

}
