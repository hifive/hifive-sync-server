package com.htmlhifive.sync.service;

/**
 * 同期処理の実行モードを指定する列挙型.
 *
 * @author kishigam
 */
public enum SyncModeType {

	/**
	 * 最初に対象リソースアイテムのアクセス権を一定の順序で全て確保した後で同期するモード.
	 */
	PREPARE,

	/**
	 * リソースアイテムのアクセス権を取得せずに同期するモード.
	 */
	NOT_PREPARE,
}
