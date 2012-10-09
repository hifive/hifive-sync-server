package com.htmlhifive.sync.resource;

/**
 * リソースをロック状態を表す列挙型.
 *
 * @author kishigam
 */
public enum ResourceLockStatusType {

	/**
	 * ロックなし.
	 */
	UNLOCK,

	/**
	 * 共有ロック.
	 */
	SHARED,

	/**
	 * 排他ロック.
	 */
	EXCLUSIVE,
}
