/**
 *
 */
package com.htmlhifive.resourcefw.file.exception;

/**
 * @author kawaguch
 *
 */
public class UpdateConflictException extends UrlTreeSystemException {

	/**
	 *
	 */
	private static final long serialVersionUID = -1419014910965564731L;

	/**
	 *
	 */
	public UpdateConflictException() {
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public UpdateConflictException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UpdateConflictException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public UpdateConflictException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public UpdateConflictException(Throwable cause) {
		super(cause);
	}

}
