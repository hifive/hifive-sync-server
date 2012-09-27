package com.htmlhifive.sync.service;

/**
 * 上り更新処理における対象リソースアイテムのロック取得方法を表す列挙型.
 *
 * @author kishigam
 */
public enum UploadLockModeType {

	/**
	 * 最初に全対象リソースアイテムのロックを取得することを示します.
	 */
	PREPARE,

	/**
	 * 各リソースアイテム更新時に個別にロックを取得することを示します.
	 */
	NOT_PREPARE,
}
