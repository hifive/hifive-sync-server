package com.htmlhifive.sync.resource.lock;

/**
 * リソースをロック状態を表す列挙型.<br>
 * TODO 次期バージョンにて実装予定
 *
 * @author kishigam
 */
@Deprecated
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
