package com.ailk.aus.spider.bean;

import java.io.Serializable;
import java.util.List;

/**
 * task属性
 * 
 * @author zhusy
 */
public class TaskInfo implements Serializable {

	private static final long serialVersionUID = -7635137649980499102L;

	private int taskID;
	private String cycle;
	private String taskName;
	private List<AssetInfo> assets;
	private List<ShellInfo> shells;

	public int getTaskID() {
		return taskID;
	}

	public void setTaskID(int taskID) {
		this.taskID = taskID;
	}

	public String getCycle() {
		return cycle;
	}

	public void setCycle(String cycle) {
		this.cycle = cycle;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public List<AssetInfo> getAssets() {
		return assets;
	}

	public void setAssets(List<AssetInfo> assets) {
		this.assets = assets;
	}

	public List<ShellInfo> getShells() {
		return shells;
	}

	public void setShells(List<ShellInfo> shells) {
		this.shells = shells;
	}

}
