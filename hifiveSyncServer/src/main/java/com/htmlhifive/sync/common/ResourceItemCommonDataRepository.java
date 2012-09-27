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

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * リソースアイテム共通データエンティティを永続化するためのリポジトリ.<br>
 *
 * @author kishigam
 */
public interface ResourceItemCommonDataRepository extends
		JpaRepository<ResourceItemCommonData, ResourceItemCommonDataId> {

	/**
	 * リソース名が合致し、ある時刻以降に更新されたリソースの共通データを返します.<br>
	 *
	 * @param resourceName リソース名
	 * @param since データを検索する時刻(指定時刻以降の更新データを検索)
	 * @return 検索した共通データエンティティのList
	 */
	@Query("SELECT d FROM ResourceItemCommonData d WHERE d.id.resourceName = :resourceName AND d.lastModified > :since AND d.action <> 'DELETE'")
	List<ResourceItemCommonData> findModified(@Param("resourceName") String resourceName, @Param("since") long since);

	/**
	 * リソース名が合致し、そのリソースでのIDが合致する共通データを返します.<br>
	 *
	 * @param resourceName リソース名
	 * @param targetItemId 共通データの対象リソースアイテムにおけるID
	 * @return 検索した共通データエンティティ
	 */
	@Query("SELECT d FROM ResourceItemCommonData d WHERE d.id.resourceName = :resourceName AND d.targetItemId= :targetItemId")
	ResourceItemCommonData findByTargetItemId(@Param("resourceName") String resourceName,
			@Param("targetItemId") String targetItemId);
}
