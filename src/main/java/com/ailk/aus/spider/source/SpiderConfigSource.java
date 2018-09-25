package com.ailk.aus.spider.source;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.flume.Context;
import org.apache.flume.conf.Configurables;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ai.common.warning.ErrInfo;
import com.ailk.aus.flumeplugin.core.distribute.HighAvailable;
import com.ailk.aus.flumeplugin.core.source.BasicSource;
import com.ailk.aus.spider.bean.FortressInfo;
import com.ailk.aus.spider.bean.TaskInfo;
import com.ailk.aus.spider.task.SpiderExcutor;
import com.ailk.aus.spider.task.SpiderTask;
import com.ailk.aus.spider.utils.TaskException;
import com.ailk.aus.spider.utils.Tools;

/**
 * spider config产品flume source 入口
 * <p>
 * SpiderConfig产品主要针对主机设备、防火墙、路由器、交换机、数据库和中间件等平台，通过策略库对这些资产设备进行合规性检查，并对数据标签化后用于后续分析。最终能够提供资产的账号、口令、配置、服务等全方面安全合规基线分析
 * <p>
 * 一个spider-config任务的生命周期主要有三个阶段</br>
 * 1、任务初始化</br>
 * 这个阶段主要工作是：根据本地配置文件的数据库url地址，查询当前采集任务所要执行的Task。然后遍历Task,关联出当前task所绑定的资产信息以及脚本信息。最后根据资产的ip，关联出他的用户名以及密码信息，再根据不同的编码器对密码进行解密。</br>
 * 2、周期调度期</br>
 * 在所有初始化工作做完后，根据需要生成脚本文件，利用sftp协议将脚本文件上传到各个主机。然后启动周期性任务，调度触发后，首先进入堡垒机管理模块，做堡垒机适配交互；然后进入shell执行器，和目标服务器做交互式采集。</br>
 * 3、数据输出</br>
 * 将获取到的数据标签化，打上批次、策略、资产等标签。最后输出到kafka 集群。</br>
 * 
 * @author zhusy
 */
@HighAvailable
public class SpiderConfigSource extends BasicSource {

	private static final Logger logger = LoggerFactory.getLogger(SpiderConfigSource.class);
	private SchedulerFactory sf = new StdSchedulerFactory();
	private Scheduler sched;
	private Properties prop = new Properties();
	private int parallelism;
	private int await;
	private int times;
	private String taskIds;
	private String latcher;
	private static List<String> fortressLatchers = new ArrayList<>();
	private FortressInfo fortressInfo = null;
	private List<TaskInfo> taskInfoList = null;

	static {
		fortressLatchers.add("com.ailk.aus.spider.shell.JsMobileLatcher");
	}

	@Override
	public void isMaster() {
		try {
			if (taskInfoList == null) {
				SpiderTask st = new SpiderTask(prop);
				taskInfoList = st.getAllTask(taskIds);
			}
			addjob(taskInfoList);
		} catch (Exception e) {
			logger.error("start task failed", e);
			ErrInfo.add("error", e.getMessage());
			m_amcReport.Warning("start task failed:" + e.toString());
			throw new TaskException(e);
		}
	}

	private void addjob(List<TaskInfo> taskInfoList) throws SchedulerException, ParseException {
		sched = sf.getScheduler();
		for (TaskInfo taskInfo : taskInfoList) {
			JobDetail jobDetail = new JobDetail(String.valueOf(taskInfo.getTaskID()), "spider", SpiderExcutor.class);
			jobDetail.getJobDataMap().put("task", taskInfo);
			jobDetail.getJobDataMap().put("MetaData", metaData);
			jobDetail.getJobDataMap().put("MsgGather", msgGather);
			jobDetail.getJobDataMap().put("fortress", fortressInfo);
			jobDetail.getJobDataMap().put("parallelism", parallelism);
			jobDetail.getJobDataMap().put("times", times);
			jobDetail.getJobDataMap().put("await", await);
			jobDetail.getJobDataMap().put("latcher", latcher);
			CronTrigger cronTrigger = new CronTrigger(String.valueOf(taskInfo.getTaskID()), "spider");
			CronExpression cexp = new CronExpression(taskInfo.getCycle());
			cronTrigger.setCronExpression(cexp);
			sched.scheduleJob(jobDetail, cronTrigger);
			logger.info("the sched add one job:{}", taskInfo.getTaskName());
		}
		sched.start();
	}

	@Override
	protected void shutdown() {
		if (sched != null) {
			logger.info("SpiderConfigSource stopping...");
			try {
				sched.shutdown(false);
			} catch (SchedulerException e) {
				logger.error("SchedulerException", e);
			}
		}
	}

	@Override
	public void configure(Context context) {
		Configurables.ensureRequiredNonNull(context, "driver", "url", "password", "userName", "taskIds");
		this.prop.put("driver", context.getString("driver"));
		this.prop.put("url", context.getString("url"));
		this.prop.put("password", Tools.decode(1, context.getString("password")));
		this.prop.put("userName", context.getString("userName"));

		this.parallelism = context.getInteger("parallelism", 100);
		this.await = context.getInteger("await", 30);
		this.times = context.getInteger("times", 3);
		this.taskIds = context.getString("taskIds");
		this.latcher = context.getString("latcher");
		if (fortressLatchers.contains(latcher)) {
			fortressInfo = new FortressInfo();
			fortressInfo.setPwd(Tools.decode(1, context.getString("fortress.pwd")));
			fortressInfo.setIp(context.getString("fortress.ip"));
			fortressInfo.setPort(context.getInteger("fortress.port"));
			fortressInfo.setUserName(context.getString("fortress.user"));
			fortressInfo.setPrompts(Arrays.asList(context.getString("fortress.prompts").split(",")));
		}
		super.configure(context);
	}

}
