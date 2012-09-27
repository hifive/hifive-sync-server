package com.htmlhifive.sync.service;

/**
 * 同期操作の共通データインターフェース.<br>
 * サブクラスで、各同期操作での共通データ型を実装します.
 *
 * @author kishigam
 */
public interface SyncCommonData {

	/**
	 * この共通データが保持するストレージIDを返します.
	 */
	String getStorageId();

	/**
	 * 同期操作の実行時刻を返します.
	 */
	long getSyncTime();
}