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

import java.util.List;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * リソースアイテム共通データリポジトリの独自拡張インターフェース.
 */
public interface ResourceItemCommonDataRepositoryCustom {

	/**
	 * IDでリソースアイテムの共通データを返します.<br>
	 *
	 * @param resourceName リソース名
	 * @param since データを検索する時刻(指定時刻以降の更新データを検索)
	 * @return 検索した共通データエンティティのList
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT d FROM ResourceItemCommonData d WHERE d.id = :id")
	ResourceItemCommonData findOneForUpdate(@Param("id") ResourceItemCommonDataId id);

	/**
	 * リソース名が合致し、ある時刻以降に更新されたリソースアイテムの共通データを返します.<br>
	 *
	 * @param resourceName リソース名
	 * @param since データを検索する時刻(指定時刻以降の更新データを検索)
	 * @return 検索した共通データエンティティのList
	 */
	@Query("SELECT d FROM ResourceItemCommonData d WHERE d.id.resourceName = :resourceName AND d.lastModified > :since")
	List<ResourceItemCommonData> findModified(@Param("resourceName") String resourceName, @Param("since") long since);

	/**
	 * リソース名が合致し、ある時刻以降に更新されたリソースアイテムの共通データを返します.<br>
	 * また、リソースごとのアイテムの識別子に合致するものに限定されます.
	 *
	 * @param resourceName リソース名
	 * @param since データを検索する時刻(指定時刻以降の更新データを検索)
	 * @param targetItemIds リソースごとのアイテムの識別子
	 * @return 検索した共通データエンティティのList
	 */
	@Query("SELECT d FROM ResourceItemCommonData d WHERE d.id.resourceName = :resourceName AND d.targetItemId in :targetItemIds AND d.lastModified > :since")
	List<ResourceItemCommonData> findModified(@Param("resourceName") String resourceName, @Param("since") long since,
			@Param("targetItemIds") List<String> targetItemIds);

	/**
	 * リソース名が合致し、対象リソースアイテム識別子が合致する共通データを返します.<br>
	 *
	 * @param resourceName リソース名
	 * @param targetItemIds リソースごとのアイテムの識別子
	 * @return 検索した共通データエンティティ
	 */
	@Query("SELECT d FROM ResourceItemCommonData d WHERE d.id.resourceName = :resourceName AND d.targetItemId = :targetItemId")
	ResourceItemCommonData findByTargetItemId(@Param("resourceName") String resourceName,
			@Param("targetItemId") String targetItemId);

	/**
	 * リソース名が合致し、対象リソースアイテム識別子が合致する共通データを返します.<br>
	 *
	 * @param resourceName リソース名
	 * @param targetItemIds リソースごとのアイテムの識別子
	 * @return 検索した共通データエンティティ
	 */
	@Query("SELECT d FROM ResourceItemCommonData d WHERE d.id.resourceName = :resourceName AND d.targetItemId in :targetItemIds")
	List<ResourceItemCommonData> findByTargetItemIds(@Param("resourceName") String resourceName,
			@Param("targetItemIds") List<String> targetItemIds);

}
