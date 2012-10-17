package com.htmlhifive.sync.common;

import com.htmlhifive.sync.resource.SyncAction;

/**
 * リソースアイテム共通データを直接更新するためのサービス実装.<br>
 * 同期リソースを経由せずにリソースアイテムデータを更新した場合に使用することができます.<br>
 * このサービスからリソースアイテム共通データを更新することでアイテムデータと共通データの整合性が維持されます.<br>
 *
 * @author kishigam
 */
public interface ResourceItemCommonDataDirectModifier {

	/**
	 * リソースアイテム共通クラスを指定された情報で更新します.<br>
	 * 指定されたアクションが{@link SyncAction#CREATE}の場合は新規登録されます.
	 *
	 * @param resourceName リソース名
	 * @param resourceItemId リソースアイテムID
	 * @param targetItemId ターゲットアイテムID
	 * @param action アクション
	 * @param updateTime 更新時刻
	 */
	void modify(String resourceName, String resourceItemId, String targetItemId, SyncAction action, long updateTime);
}
