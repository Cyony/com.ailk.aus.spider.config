package com.ailk.aus.spider.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ailk.aus.spider.bean.AssetInfo;
import com.ailk.aus.spider.bean.ShellInfo;
import com.ailk.aus.spider.bean.TaskInfo;
import com.ailk.aus.spider.utils.Operator;
import com.ailk.aus.spider.utils.ShellUtils;
import com.ailk.aus.spider.utils.Tools;

/**
 * <h3>task 管理类</h3>
 * <p>
 * 一个flume通常包含一个或者多个周期性的作业。</br>
 * 配置信息为： server.sources.source.taskIds=1,2</br>
 * 多个作业之间以英文的逗号分隔。在初始化阶段，会根据配置的ID去获取所有作业的详细信息，比如绑定的资产和脚本。
 * 此步骤主要是为了得到作业的运行周期、作业名等信息。
 * <p>
 * 第二步是拿到所要采集的资产列表信息，对ip、用户名、端口、密码、密码加密类型进行封装。 注：密码加密类型由encryptionType
 * 字段控制，0表示不加密，1表示用base64加密。
 * <p>
 * 最后一步是为了获取作业所绑定的所有要执行的指令配置信息。对策略名、内容、上传操作等信息进行封装。 字段isNeedUpload
 * 表示当前指令是否需要上传执行: 0表示不需要上传，已经提前在用户根目录下的.shell目录生成好脚本文件；</br>
 * 1表示需要程序自动上传脚本文件到.shell目录下；</br>
 * 2表示不需要上传，可以直接执行内容。</br>
 * 注：当isNeedUpload 为0和1时，实际执行指令为：cd .shell；chmod u+x title；./title
 * （title为表中查询得出的脚本文件名） 当isNeedUpload 为2时，实际执行指令就是content内容，比如（pwd/ls等）
 * 当isNeedUpload 为1时，程序每次启动时，会先在本地生成一份脚本文件，然后覆盖性上传到远程主机对应的目录下，然后删除本地文件
 * 
 * @author zhusy
 */
public class SpiderTask {

	private static final Logger logger = LoggerFactory.getLogger(SpiderTask.class);
	private static final String TASKSQL = "select task_id as taskid,cycle,task_name as taskName from aus_spider_config_task where task_id in (%s)";
	private static final String SHELLSQL = "select a.shell_id as id,b.title,b.stragety_name as stragetyName,b.content,b.isNeedUpload from aus_spider_config_taskAndshell a left join aus_spider_config_shell b on a.shell_id = b.shell_id where a.task_id = ?";
	private static final String ASSETSQL = "select b.pwd,b.ip,b.username,b.port,b.encryptionType from aus_spider_config_taskAssert a left join aus_spider_config_assert b on a.assert_id = b.assert_id where a.task_id = ?";
	protected Operator<TaskInfo> tasker;
	protected Operator<ShellInfo> sheller;
	protected Operator<AssetInfo> asseter;

	public SpiderTask(Properties prop) {
		tasker = new Operator<>(prop);
		sheller = new Operator<>(prop);
		asseter = new Operator<>(prop);
	}

	public List<TaskInfo> getAllTask(String ids) throws Exception {
		List<TaskInfo> list = new ArrayList<>();
		list = tasker.select(String.format(TASKSQL, ids), TaskInfo.class);
		logger.info("get job success,the size is:" + list.size());
		for (TaskInfo task : list) {
			logger.info("start to format the task which name is {}", task.getTaskName());
			task.setShells(convertShell(sheller.select(SHELLSQL, ShellInfo.class, task.getTaskID())));
			logger.info("get shell list success,the size is:" + task.getShells().size());
			task.setAssets(convertAsset(asseter.select(ASSETSQL, AssetInfo.class, task.getTaskID())));
			logger.info("get asset list success,the size is:" + task.getAssets().size());
			List<ShellInfo> filterShell = task.getShells().stream().filter(s -> {
				return s.getIsNeedUpload() == 1 ? true : false;
			}).collect(Collectors.toList());
			if (filterShell.size() > 0) {
				logger.info("the shell need to upload size is:" + filterShell.size());
				for (AssetInfo asset : task.getAssets()) {
					ShellUtils.createAndUploadShellFiles(filterShell, asset);
				}
			}
		}
		return list;
	}

	/**
	 * 0：不加密 1：base64加密
	 * 
	 * @param list
	 * @return
	 */
	protected List<AssetInfo> convertAsset(List<AssetInfo> list) {
		for (AssetInfo asset : list) {
			asset.setPwd(Tools.decode(asset.getEncryptionType(), asset.getPwd()));
		}
		return list;
	}

	/**
	 * 1:上传，0：不上传，2：可直接执行
	 * 
	 * @param list
	 * @return
	 */
	protected List<ShellInfo> convertShell(List<ShellInfo> list) {
		for (ShellInfo shell : list) {
			switch (shell.getIsNeedUpload()) {
			case 0:
				shell.setCommand("cd .shell;" + "chmod u+x " + shell.getTitle() + ";" + "./" + shell.getTitle());
				break;
			case 1:
				shell.setCommand("cd .shell;" + "chmod u+x " + shell.getTitle() + ";" + "./" + shell.getTitle());
				break;
			case 2:
				shell.setCommand(shell.getContent());
			default:
				break;
			}
		}
		return list;
	}

}
