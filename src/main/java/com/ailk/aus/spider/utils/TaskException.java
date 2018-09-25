package com.ailk.aus.spider.utils;

/**
 * Base class of all task exceptions.it a runtime exception
 * 
 * @author zhusy
 */
public class TaskException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new TaskException with the specified cause and a detail message
	 * of (cause==null ? null : cause.toString()) (which typically contains the
	 * class and detail message of cause). This constructor is useful for runtime
	 * exceptions that are little more than wrappers for other throwables.
	 * 
	 * @param msg
	 *            the detail msg
	 */
	public TaskException(String msg) {
		super(msg);
	}

	/**
	 * Constructs a new TaskException with the specified cause and a detail message
	 * of (cause==null ? null : cause.toString()) (which typically contains the
	 * class and detail message of cause). This constructor is useful for runtime
	 * exceptions that are little more than wrappers for other throwables.
	 * 
	 * @param msg
	 *            the detail msg
	 * @param th
	 *            the cause
	 */
	public TaskException(String msg, Throwable th) {
		super(msg, th);
	}

	/**
	 * Constructs a new TaskException with the specified cause and a detail message
	 * of (cause==null ? null : cause.toString()) (which typically contains the
	 * class and detail message of cause). This constructor is useful for runtime
	 * exceptions that are little more than wrappers for other throwables.
	 * 
	 * @param th
	 *            the cause
	 */
	public TaskException(Throwable th) {
		super(th);
	}

}
