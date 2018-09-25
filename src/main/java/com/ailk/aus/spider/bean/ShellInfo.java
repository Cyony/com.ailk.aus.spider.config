package com.ailk.aus.spider.bean;

import java.io.Serializable;

/**
 * shell所有属性
 * 
 * @author zhusy
 */
public class ShellInfo implements Serializable {

	private static final long serialVersionUID = 5739846821361356811L;

	private int id;
	private String title;
	private String content;
	private String stragetyName;
	private int isNeedUpload;
	private String command;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getStragetyName() {
		return stragetyName;
	}

	public void setStragetyName(String stragetyName) {
		this.stragetyName = stragetyName;
	}

	public int getIsNeedUpload() {
		return isNeedUpload;
	}

	public void setIsNeedUpload(int isNeedUpload) {
		this.isNeedUpload = isNeedUpload;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	@Override
	public String toString() {
		return "ShellInfo [id=" + id + ", title=" + title + ", content=" + content + ", stragetyName=" + stragetyName
				+ ", isNeedUpload=" + isNeedUpload + ", command=" + command + "]";
	}

}
