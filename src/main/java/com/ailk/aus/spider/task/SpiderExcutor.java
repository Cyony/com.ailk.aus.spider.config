package com.ailk.aus.spider.task;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ailk.aus.flumeplugin.core.collector.MsgGather;
import com.ailk.aus.flumeplugin.core.config.MetaData;
import com.ailk.aus.spider.bean.AssetInfo;
import com.ailk.aus.spider.bean.FortressInfo;
import com.ailk.aus.spider.bean.ShellInfo;
import com.ailk.aus.spider.bean.TaskInfo;
import com.ailk.aus.spider.fortress.LatcherReflection;
import com.ailk.aus.spider.shell.ShellLatcher;

/**
 * <h3>周期性任务调度</h3>
 * <p>
 * 通常情况下，单个周期作业要执行的主机以及指令还是比较多的，两者的笛卡尔积通常也会有三位数的，如果一个个顺序依次执行，可能这个周期
 * 还没执行完毕，下一周期又开始了，非常拖累性能。所以程序设计中引入了并发，即多个指令集同时执行。具体多少个，由参数并发度控制</br>
 * server.sources.source.parallelism=100，并发度默认为100，即默认同时执行100个指令集。此参数需要根据实际情况谨慎设置。设置过大会导致
 * 连接全部卡死，设置过小性能比较低下，建议范围在平均每台机器5到10个指令集。什么意思呢，由于程序设计的是以脚本为优先的方案，这样可以
 * 使得并发在不同的机器上，而不会导致大量指令并发在同一台机器上，那么需要满足的原则就是 5 < 主机数*指令数/parallelism <
 * 10，这样既可以保证 一定的并发，也可以保证程序稳定性，当然也不是绝对的，可以根据具体情况动态调节。
 * 上面讲了并发度，那么同一个并发集最长需要多少时间来完成呢，如果遇到连接死活连不上的情况下，我们不可能无限制地等下去，直到所有指令集
 * 都完成。这个时候参数server.sources.source.await=30就派上用场了，这个参数表示最大等待时间，默认是30s。30秒之内这批指令集没有完成的就给一个
 * 超时的报错，当然不可能这批指令集全部连不上，可以在30s内执行完毕的，当然还是会有日志输出的。
 * 同样、对单个指令执行也做了相关限制，如果一台主机，连续几次都获取不到seesion，这个时间就应该放弃了，抛出并发异常，结束这个指令，当然
 * 正常获取seesion的可以得到正确的输出。参数为server.sources.source.times=3，默认尝试次数为3次。
 * 
 * @author zhusy
 */
public class SpiderExcutor implements Job {

	private Logger logger = LoggerFactory.getLogger(SpiderExcutor.class);
	private ShellLatcher latcher = null;
	private MsgGather msgGather = null;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("--------------timing task begin to excute------------");
		JobDetail jobDetail = context.getJobDetail();
		JobDataMap jobDataMap = jobDetail.getJobDataMap();
		TaskInfo task = (TaskInfo) jobDataMap.get("task");
		MetaData metaData = (MetaData) jobDataMap.get("MetaData");
		metaData.setLogLevel(String.valueOf(new Date().getTime()));
		int times = jobDataMap.getInt("times");
		msgGather = (MsgGather) jobDataMap.get("MsgGather");
		int await = jobDataMap.getInt("await");
		int parallelism = jobDataMap.getInt("parallelism");
		FortressInfo fortressInfo = (FortressInfo) jobDataMap.get("fortress");
		String latcherClass = jobDataMap.getString("latcher");
		int capacity = (int) (task.getAssets().size() * task.getShells().size() / 0.75) + 1;
		Map<String, Future<String>> rsMap = new HashMap<>(capacity);
		try {
			Class<?> clazz = Class.forName(latcherClass);
			latcher = (ShellLatcher) LatcherReflection.newInstance(clazz, fortressInfo);
			latcher.setTimes(times);
			// 以脚本为优先,可以使得任务并发在不同的机器上
			for (ShellInfo shell : task.getShells()) {
				for (AssetInfo asset : task.getAssets()) {
					try {
						metaData.collectAddress(asset.getIp());
						metaData.collectPath(shell.getTitle());
						logger.debug("start to excutor task,ip {},command {}", asset.getIp(), shell.getCommand());
						rsMap.put(metaData.builder(), latcher.excute(asset, shell));
						if (rsMap.size() >= parallelism) {
							handleFuture(rsMap, await);
							rsMap.clear();
						}
					} catch (Exception e) {
						logger.error("ip {} this task cannot get session", asset.getIp(), e);
						if (e instanceof InterruptedException) {
							return;
						}
					}
				}
			}
			handleFuture(rsMap, await);
			logger.info("task {} excute finshed", task.getTaskName());
		} catch (ClassNotFoundException e) {
			logger.error("cannot find the latcher:{}", latcherClass, e);
		}
		logger.info("--------------timing task excute end------------");
	}

	private void handleFuture(Map<String, Future<String>> map, int await) {
		try {
			logger.info("handle batch task:" + map.size());
			latcher.awaitTermination(await);
			List<String> msgs = map.entrySet().stream().map(entry -> {
				try {
					return entry.getKey() + entry.getValue().get(1, TimeUnit.MICROSECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e2) {
					logger.error("excute error,the header is {}", entry.getKey(), e2);
				} catch (TimeoutException e3) {
					logger.error("excute timeout,the header is {}", entry.getKey(), e3);
				}
				return null;
			}).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
			if (msgs.size() > 0) {
				logger.info("add msg to file,size is:" + msgs.size());
				msgGather.addMsgToFileBySize(msgs);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
