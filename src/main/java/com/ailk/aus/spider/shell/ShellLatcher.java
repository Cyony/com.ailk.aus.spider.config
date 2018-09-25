package com.ailk.aus.spider.shell;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.ailk.aus.spider.bean.AssetInfo;
import com.ailk.aus.spider.bean.ShellInfo;
import com.ailk.aus.spider.fortress.FortressRunner;

/**
 * 通常情况下，登录到远程主机执行shell脚本，有多种方式，最简单直接的就是直接利用ssh协议登录，然后执行
 * shell。比较复杂的就是要绕过一些连接障碍，这个时候需要通过和堡垒机做一些交互。AUS目前提供两种执行器
 * 一种就是普通的直接登录，另一种则是江苏移动的4A堡垒机。</br>
 * 控制参数为：server.sources.source.latcher=com.ailk.aus.spider.shell.LinuxCommonLatcher（com.ailk.aus.spider.shell.JsMobileLatcher）</br>
 * 参数值前者为直接登录执行器，后者为江苏移动4A堡垒机执行器
 * 
 * @author zhusy
 */
public abstract class ShellLatcher {

	protected static final String ENDCHAR = "\r";

	protected int times = 3;

	private ExecutorService executor = null;

	/**
	 * 抽象执行函数,根据提供的资产以及脚本信息,登录执行shell
	 * 
	 * @param asset
	 *            资产
	 * @param shell
	 *            脚本
	 * @return
	 * @throws Exception
	 */
	public abstract String runner(AssetInfo asset, ShellInfo shell) throws Exception;

	/**
	 * 提交具体的task到线程池
	 * 
	 * @param asset
	 *            资产
	 * @param shell
	 *            脚本
	 * @return A Future represents the result of an asynchronous computation
	 * @throws Exception
	 */
	public Future<String> excute(AssetInfo asset, ShellInfo shell) throws Exception {
		if (executor == null) {
			executor = Executors.newCachedThreadPool();
		}
		return executor.submit(() -> runner(asset, shell));
	}

	/**
	 * @param verifyer
	 *            认证交互函数
	 * @throws Exception
	 */
	public void fortressVerify(FortressRunner verifyer) throws Exception {
		verifyer.action();
	}

	/**
	 * 线程池最大等待这批task完成的时间
	 * 
	 * @param seconds
	 *            等待时间,单位秒
	 * @throws InterruptedException
	 */
	public void awaitTermination(int seconds) throws InterruptedException {
		executor.shutdown();
		executor.awaitTermination(seconds, TimeUnit.SECONDS);
		executor.shutdownNow();
		executor = null;
	}

	/**
	 * 初始化函数
	 * 
	 * @param o
	 */
	public void init(Object o) {

	}

	/**
	 * 设置最大尝试获取session次数,超过这个次数则结束这个任务抛出异常
	 * 
	 * @param times
	 */
	public void setTimes(int times) {
		this.times = times;
	}

}
