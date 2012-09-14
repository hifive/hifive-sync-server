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
package com.htmlhifive.sync.commondata;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 共通データエンティティを永続化するためのリポジトリ.<br>
 *
 * @author kishigam
 */
public interface CommonDataRepository extends JpaRepository<CommonDataBean, String> {

	/**
	 * データモデル名が合致し、ある時刻以降に更新されたリソースの共通データを返します.<br>
	 *
	 * @param dataModelName データモデル名
	 * @param since データを検索する時刻(指定時刻以降の更新データを検索)
	 * @return 検索した共通データエンティティのList
	 */
	@Query("SELECT d FROM CommonDataBean d WHERE d.dataModelName = :dataModelName AND d.lastModified > :since AND d.syncMethod <> 2")
	List<CommonDataBean> findModified(@Param("dataModelName") String dataModelName, @Param("since") long since);

	/**
	 * データモデル名、リソースID文字列が合致するリソースの共通データを返します.
	 *
	 * @param dataModelName データモデル名
	 * @param resourceIdStr リソースID文字列
	 * @return 共通データエンティティ
	 */
	@Query("SELECT d FROM CommonDataBean d WHERE d.dataModelName = :dataModelName AND d.resourceIdStr = :resourceIdStr")
	CommonDataBean findByResourceIdStr(@Param("dataModelName") String dataModelName,
			@Param("resourceIdStr") String resourceIdStr);
}
