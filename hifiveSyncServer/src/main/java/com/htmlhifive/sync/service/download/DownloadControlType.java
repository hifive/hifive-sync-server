package com.htmlhifive.sync.service.download;

import com.htmlhifive.sync.service.Synchronizer;

/**
 * {@link Synchronizer}における下り更新の同期制御方式を表す列挙型.
 *
 * @author kishigam
 */
public enum DownloadControlType {

	/**
	 * 下り更新対象のリソースアイテムに対して読み取りロックを行ってから取得します.<br>
	 */
	READ_LOCK,

	/**
	 * 下り更新において同期制御を行いません.<br>
	 */
	NONE,
}
