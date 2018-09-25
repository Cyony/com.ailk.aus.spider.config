package com.ailk.aus.spider.fortress;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

import com.ailk.aus.spider.shell.ShellLatcher;

/**
 * 类反射工具类 改善反射对性能的影响
 * 
 * @author zhusy
 */
public class LatcherReflection {

	private static final Class<?>[] EMPTY_ARRAY = new Class[] {};
	private static final Map<Class<?>, Constructor<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<Class<?>, Constructor<?>>();

	/**
	 * 根据所给类创建类构造
	 * 
	 * @param theClass
	 *            class of which an object is created
	 * @return a new object
	 */
	public static Object newInstance(Class<?> theClass, Object info) {
		Object result;
		try {
			Constructor<?> meth = (Constructor<?>) CONSTRUCTOR_CACHE.get(theClass);
			if (meth == null) {
				meth = theClass.getDeclaredConstructor(EMPTY_ARRAY);
				meth.setAccessible(true);
				CONSTRUCTOR_CACHE.put(theClass, meth);
			}
			result = meth.newInstance();
			if (info != null) {
				if (result instanceof ShellLatcher) {
					((ShellLatcher) result).init(info);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	static private ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

	public static void setContentionTracing(boolean val) {
		threadBean.setThreadContentionMonitoringEnabled(val);
	}

	/**
	 * 打印所有线程的堆栈信息
	 * 
	 * @param stream
	 *            the stream to
	 * @param title
	 *            a string title for the stack trace
	 */
	public synchronized static void printThreadInfo(PrintWriter stream, String title) {
		final int STACK_DEPTH = 20;
		boolean contention = threadBean.isThreadContentionMonitoringEnabled();
		long[] threadIds = threadBean.getAllThreadIds();
		stream.println("Process Thread Dump: " + title);
		stream.println(threadIds.length + " active threads");
		for (long tid : threadIds) {
			ThreadInfo info = threadBean.getThreadInfo(tid, STACK_DEPTH);
			if (info == null) {
				stream.println("  Inactive");
				continue;
			}
			Thread.State state = info.getThreadState();
			stream.println("  State: " + state);
			stream.println("  Blocked count: " + info.getBlockedCount());
			stream.println("  Waited count: " + info.getWaitedCount());
			if (contention) {
				stream.println("  Blocked time: " + info.getBlockedTime());
				stream.println("  Waited time: " + info.getWaitedTime());
			}
			if (state == Thread.State.WAITING) {
				stream.println("  Waiting on " + info.getLockName());
			} else if (state == Thread.State.BLOCKED) {
				stream.println("  Blocked on " + info.getLockName());
			}
			stream.println("  Stack:");
			for (StackTraceElement frame : info.getStackTrace()) {
				stream.println("    " + frame.toString());
			}
		}
		stream.flush();
	}

	private static long previousLogTime = 0;

	/**
	 * 打印当前线程info级别的日志
	 * 
	 * @param log
	 *            the logger that logs the stack trace
	 * @param title
	 *            a descriptive title for the call stacks
	 * @param minInterval
	 *            the minimum time from the last
	 */
	public static void logThreadInfo(Log log, String title, long minInterval) {
		boolean dumpStack = false;
		if (log.isInfoEnabled()) {
			synchronized (LatcherReflection.class) {
				long now = System.currentTimeMillis();
				if (System.currentTimeMillis() - previousLogTime >= minInterval * 1000) {
					previousLogTime = now;
					dumpStack = true;
				}
			}
			if (dumpStack) {
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				printThreadInfo(new PrintWriter(buffer), title);
				log.info(buffer.toString());
			}
		}
	}

	static void clearCache() {
		CONSTRUCTOR_CACHE.clear();
	}

	static int getCacheSize() {
		return CONSTRUCTOR_CACHE.size();
	}
}
