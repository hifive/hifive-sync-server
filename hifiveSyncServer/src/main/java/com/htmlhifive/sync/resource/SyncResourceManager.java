package com.htmlhifive.sync.resource;

/**
 * アプリケーション内に存在するリソースを管理するサービスインターフェース.
 */
public interface SyncResourceManager {

	/**
	 * リソース名から対応するリソースを返します.<br>
	 * リソースが存在しない場合、nullを返します.
	 *
	 * @param resourceName リソース名
	 * @return リソースクラス
	 */
	SyncResource<?> locateSyncResource(String resourceName);
}