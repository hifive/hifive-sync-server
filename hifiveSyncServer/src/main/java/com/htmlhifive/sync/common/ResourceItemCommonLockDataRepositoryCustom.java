/*
 * Copyright (C) 2012 NS Solutions Corporation
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
package com.htmlhifive.sync.common;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;

import com.htmlhifive.sync.resource.ResourceLockModeType;

/**
 * リソースアイテム共通ロックデータリポジトリの独自拡張インターフェース.
 */
public interface ResourceItemCommonLockDataRepositoryCustom {

	// 	 メソッド名からSpringFrameworkがクエリーを生成
	/**
	 * 指定された項目に合致するリソースアイテム共通ロックデータを取得します.<br>
	 * 悲観的WRITEロックを必要とします.
	 *
	 * @param resourceName リソース名
	 * @param targetItemId 共通データの対象リソースアイテムにおけるID
	 * @return 検索した共通データエンティティ
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	ResourceItemCommonLockData findByIdAndLockMode(ResourceItemCommonDataId id, ResourceLockModeType lockMode);
}
