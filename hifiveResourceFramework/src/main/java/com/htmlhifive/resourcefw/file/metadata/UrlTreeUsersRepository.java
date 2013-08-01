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
package com.htmlhifive.resourcefw.file.metadata;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * urlTreeの構成ノードの所有者や使用者となるユーザーを永続化するJPARepository.
 *
 * @author kawaguch
 */
public interface UrlTreeUsersRepository extends JpaRepository<UrlTreeUsers, UrlTreeUsersPrimaryKey> {

	/**
	 * ユーザーIDで{@link UrlTreeUsers UrlTreeUsers}を検索します.<br/>
	 * 所属するグループごとのオブジェクトのリストを返します.
	 *
	 * @param user ユーザーID
	 * @return UrlTreeUsersのリスト
	 */
	List<UrlTreeUsers> findByPrimaryKeyUserId(String user);

	/**
	 * グループIDで{@link UrlTreeUsers UrlTreeUsers}を検索します.<br/>
	 * グループに所属するユーザーごとのオブジェクトのリストを返します.
	 *
	 * @param group グループID
	 * @return UrlTreeUsersのリスト
	 */
	List<UrlTreeUsers> findByPrimaryKeyGroupId(String group);

	/**
	 * 指定されたユーザーIDが主グループとなっている{@link UrlTreeUsers UrlTreeUsers}を検索します.
	 *
	 * @param user　ユーザーID
	 * @return UrlTreeUsersのリスト(実態としては1件のみ)
	 */
	List<UrlTreeUsers> findByPrimaryKeyUserIdAndPrimaryGroupIsTrue(String user);
}
