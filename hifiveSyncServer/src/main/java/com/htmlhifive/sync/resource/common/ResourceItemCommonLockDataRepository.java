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
package com.htmlhifive.sync.resource.common;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * リソースアイテム共通ロックデータエンティティを永続化するためのリポジトリ.<br>
 * SpringFrameworkの標準的なリポジトリ、独自拡張メソッドを定義した{@link ResourceItemCommonLockDataRepositoryCustom}インターフェースを継承します.<br>
 * TODO 次期バージョンにて実装予定
 *
 * @author kishigam
 */
@SuppressWarnings("deprecation")
@Deprecated
public interface ResourceItemCommonLockDataRepository extends
		JpaRepository<ResourceItemCommonLockData, ResourceItemCommonDataId> {
}