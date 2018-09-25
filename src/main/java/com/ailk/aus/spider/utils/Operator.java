package com.ailk.aus.spider.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

/**
 * 数据库操作客户端,封装了通用的查询函数
 * 
 * @author zhusy
 * @param <T>
 *            需要封装的实体对象类型
 */
public class Operator<T> {

	private JdbcClient jdbc = null;

	/**
	 * 创建数据库操作对象
	 * 
	 * @param prop
	 *            需要参数： driver,url,user,passwd
	 */
	public Operator(Properties prop) {
		this.jdbc = JdbcClient.newInstance(prop);
	}

	/**
	 * 查询,根据传入类型,返回该类型的列表
	 * 
	 * @param sql
	 * @param clazz
	 * @return
	 * @throws SQLException
	 */
	public List<T> select(String sql, Class<T> clazz) throws SQLException {
		Connection conn = jdbc.getConnetcion();
		QueryRunner queryRunner = new QueryRunner();
		List<T> list = null;
		try {
			list = queryRunner.query(conn, sql, new BeanListHandler<>(clazz));
		} finally {
			jdbc.closeConnect(conn);
		}
		return list;
	}

	public List<T> select(String sql, Class<T> clazz, Object... objects) throws SQLException {
		Connection conn = jdbc.getConnetcion();
		QueryRunner queryRunner = new QueryRunner();
		List<T> list = null;
		try {
			list = queryRunner.query(conn, sql, new BeanListHandler<>(clazz), objects);
		} finally {
			jdbc.closeConnect(conn);
		}
		return list;
	}

}
