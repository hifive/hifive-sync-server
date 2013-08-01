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

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * リソースアイテム共通データエンティティを永続化するためのリポジトリ.<br>
 * SpringFrameworkの標準的なリポジトリインターフェースを継承します.<br>
 *
 * @author kishigam
 */
public interface ResourceItemCommonDataRepository extends
		JpaRepository<ResourceItemCommonData, ResourceItemCommonDataId> {

	/**
	 * IDが合致するリソースアイテムの共通データを返します.<br>
	 * 悲観的ロックを実行します.
	 *
	 * @param id リソースアイテム共通データのIDオブジェクト
	 * @return 検索した共通データエンティティ
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT d FROM ResourceItemCommonData d WHERE d.id = :id")
	ResourceItemCommonData findOneForUpdate(@Param("id") ResourceItemCommonDataId id);

	/**
	 * リソース名と対象リソースアイテムのID値が合致し、ある時刻以降に更新されたリソースアイテムの共通データを返します.
	 *
	 * @param resourceName リソース名
	 * @param targetItemId 対象リソースアイテムのID値
	 * @param since データを検索する時刻(指定時刻以降の更新データを検索)
	 * @return 検索した共通データエンティティ
	 */
	@Query("SELECT d FROM ResourceItemCommonData d WHERE d.id.resourceName = :resourceName AND d.targetItemId = :targetItemId AND d.lastModified > :since")
	ResourceItemCommonData findModified(@Param("resourceName") String resourceName,
			@Param("targetItemId") String targetItemId, @Param("since") long since);

	/**
	 * リソース名と対象リソースアイテムのID値が合致し、ある時刻以降に更新されたリソースアイテムの共通データを返します.<br>
	 * 悲観的ロックを実行します.
	 *
	 * @param resourceName リソース名
	 * @param targetItemId 対象リソースアイテムのID値
	 * @param since データを検索する時刻(指定時刻以降の更新データを検索)
	 * @return 検索した共通データエンティティ
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT d FROM ResourceItemCommonData d WHERE d.id.resourceName = :resourceName AND d.targetItemId = :targetItemId AND d.lastModified > :since")
	ResourceItemCommonData findModifiedForUpdate(@Param("resourceName") String resourceName,
			@Param("targetItemId") String targetItemId, @Param("since") long since);
}
