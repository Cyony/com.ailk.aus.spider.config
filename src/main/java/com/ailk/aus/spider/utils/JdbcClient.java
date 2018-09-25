package com.ailk.aus.spider.utils;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库连接池定义以及维护类
 * 
 * @author zhusy
 */
public class JdbcClient {

	private Logger logger = LoggerFactory.getLogger(JdbcClient.class);
	public static ComboPooledDataSource dataSource = null;
	private static Properties param = null;
	private static JdbcClient jdbc = null;

	public static JdbcClient newInstance(Properties prop) {
		if (jdbc == null) {
			param = prop;
			jdbc = new JdbcClient();
		}
		return jdbc;
	}

	private JdbcClient() {
		initialJDBCPool(param);
	}

	private void initialJDBCPool(Properties params) {
		String driver = params.getProperty("driver");
		String url = params.getProperty("url");
		String user = params.getProperty("userName");
		String password = params.getProperty("password");
		try {
			dataSource = new ComboPooledDataSource();
			dataSource.setUser(user);
			dataSource.setPassword(password);
			dataSource.setJdbcUrl(url);
			dataSource.setDriverClass(driver);
			dataSource.setInitialPoolSize(10);
			dataSource.setMinPoolSize(2);
			dataSource.setMaxPoolSize(10);
			dataSource.setMaxStatements(100);
			dataSource.setMaxIdleTime(60);
		} catch (PropertyVetoException e) {
			logger.error("初始化数据库连接池异常！", e);
		}
	}

	/**
	 * get the databases transaction
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnetcion() {
		Connection conn = null;
		if (null != dataSource) {
			try {
				conn = dataSource.getConnection();
			} catch (SQLException e) {
				logger.error("get connection error", e);
				e.printStackTrace();
			}
		}
		return conn;
	}

	/**
	 * begin the database transaction.
	 * 
	 * @param conn
	 * @throws SQLException
	 */
	public void beginTransaction(Connection conn) throws SQLException {
		if (conn != null) {
			conn.setAutoCommit(false);
		}
	}

	/**
	 * commit the database transaction
	 * 
	 * @param conn
	 * @throws SQLException
	 */
	public void commitTx(Connection conn) throws SQLException {
		if (conn != null) {
			conn.commit();
		}
	}

	/**
	 * rollbach the databases transaction
	 * 
	 * @param conn
	 * @throws SQLException
	 */
	public void rollBackBusiness(Connection conn) throws SQLException {
		if (conn != null) {
			conn.rollback();
		}
	}

	/**
	 * close the connect
	 * 
	 * @param conn
	 */
	public void closeConnect(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error("connection close error", e);
				e.printStackTrace();
			}
		}
	}

}
