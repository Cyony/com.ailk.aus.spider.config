package com.ailk.aus.spider.utils;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.ailk.aus.spider.sftp.SftpClient;
import com.jcraft.jsch.SftpException;

public class ShellUtilsTest {

	@Test
	public void test() throws Exception, SftpException {
		SftpClient sftp = new SftpClient("10.21.17.212", "aus", "aus123", 22);
		File file = new File("./shell" + "/" + "check.sh");
		FileUtils.writeStringToFile(file, "qq");
		FileInputStream inputStream = new FileInputStream(file);
		sftp.upload(".shell", "check.sh", inputStream);
		inputStream.close();
		System.out.println(file.delete());
		System.out.println("success");
		sftp.close();
	}

}
