package com.htmlhifive.sync.service.lock;

import java.util.List;
import java.util.Map;

import com.htmlhifive.sync.resource.ResourceItemWrapper;

/**
 * クライアントからのロック取得リクエストに対するレスポンスデータクラス.
 *
 * @author kishigam
 */
public class LockResponse {

	/**
	 * ロック取得共通データ.<br>
	 * このレスポンスにおいてクライアントへ返す情報を保持しています.
	 */
	private LockCommonData lockCommonData;

	/**
	 * ロック取得結果のリソースアイテムリスト. <br>
	 * リソース別にリストを保持します.
	 */
	private Map<String, List<ResourceItemWrapper<?>>> resourceItems;

	/**
	 * このロック取得共通データを持つレスポンスデータを生成します.
	 *
	 * @param responseCommon ロック取得共通データ
	 */
	public LockResponse(LockCommonData lockCommonData) {

		this.lockCommonData = lockCommonData;
	}

	/**
	 * @return lockCommonData
	 */
	public LockCommonData getLockCommonData() {
		return lockCommonData;
	}

	/**
	 * @param lockCommonData セットする lockCommonData
	 */
	public void setLockCommonData(LockCommonData lockCommonData) {
		this.lockCommonData = lockCommonData;
	}

	/**
	 * @return resourceItems
	 */
	public Map<String, List<ResourceItemWrapper<?>>> getResourceItems() {
		return resourceItems;
	}

	/**
	 * @param resourceItems セットする resourceItems
	 */
	public void setResourceItems(Map<String, List<ResourceItemWrapper<?>>> resourceItems) {
		this.resourceItems = resourceItems;
	}
}
