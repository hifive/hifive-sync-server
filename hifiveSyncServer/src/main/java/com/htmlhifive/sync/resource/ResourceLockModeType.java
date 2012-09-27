package com.htmlhifive.sync.resource;

/**
 * リソースをロックするモードを表す列挙型.
 *
 * @author kishigam
 */
public enum ResourceLockModeType {

	/**
	 * 共有ロックモード.
	 */
	SHARED,

	/**
	 * 排他ロックモード.
	 */
	EXCLUSIVE,
}
