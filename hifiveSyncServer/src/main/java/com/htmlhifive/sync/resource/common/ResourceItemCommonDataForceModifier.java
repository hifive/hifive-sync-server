/*
 * Copyright (C) 2012-2013 NS Solutions Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.htmlhifive.sync.resource.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * リソースアイテム共通データを強制的に直接更新するサービス実装.<br>
 * 同期リソースを経由せずにリソースアイテムデータを更新した場合に使用することができます.<br>
 * このサービスからリソースアイテム共通データを更新することでアイテムデータと共通データの整合性が維持されます.<br>
 *
 * @author kishigam
 */
@Service
public class ResourceItemCommonDataForceModifier implements ResourceItemCommonDataModifier {

	/**
	 * リソースアイテム共通データのリポジトリ.
	 */
	@Autowired
	private ResourceItemCommonDataRepository repository;

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
	@Override
	public void modify(String resourceName, String resourceItemId, String targetItemId, SyncAction action,
			long updateTime) {

		ResourceItemCommonDataId id = new ResourceItemCommonDataId(resourceName, resourceItemId);

		ResourceItemCommonData common = null;

		if (action == SyncAction.CREATE) {
			common = new ResourceItemCommonData(id);
			common.setTargetItemId(targetItemId);
		} else {
			common = repository.findOne(id);
		}

		common.modify(action, updateTime);

		repository.save(common);
	}
}
