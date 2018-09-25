package com.ailk.aus.spider.bean;

import java.util.List;

/**
 * 堡垒机参数,继承资产类型,拥有普通资产所有属性
 * 
 * @author zhusy
 */
public class FortressInfo extends AssetInfo {

	/**
	 * @Fields serialVersionUID : TODO
	 */

	private static final long serialVersionUID = 1L;

	private List<String> prompts;

	public List<String> getPrompts() {
		return prompts;
	}

	public void setPrompts(List<String> prompts) {
		this.prompts = prompts;
	}

	@Override
	public String toString() {
		return "FortressInfo [prompts=" + prompts + "] " + super.toString();
	}

}
