package com.htmlhifive.sync.service.upload;

import com.htmlhifive.sync.service.Synchronizer;

/**
 * {@link Synchronizer}における上り更新の同期制御方式を表す列挙型.
 *
 * @author kishigam
 */
public enum UploadControlType {

	/**
	 * 上り更新対象のリソースアイテムに対して「予約」を行い、更新を実行します.<br>
	 * 具体的には、"for update"操作によるロックを実行します.
	 */
	RESERVE,

	/**
	 * 上り更新対象のリソースアイテムの更新実行順をソートにより変更します.<br>
	 * 更新順が一定になるためデッドロックが回避できます.<br>
	 * 更新実行順を変更可能なアプリケーションのみ使用できます.
	 */
	AVOID_DEADLOCK,

	/**
	 * 上り更新において同期制御を行いません.
	 */
	NONE,
}
