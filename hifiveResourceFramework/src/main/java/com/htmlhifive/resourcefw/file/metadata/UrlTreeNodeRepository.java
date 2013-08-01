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

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * urlTreeノードを永続化するためのJPARepositoryインターフェース.
 *
 * @author kawaguch
 */
@Repository
@Transactional
public interface UrlTreeNodeRepository extends JpaRepository<UrlTreeNode, UrlTreeNodePrimaryKey> {

	/**
	 * 指定した親ノード配下のノードを検索します.
	 *
	 * @param parent 親ノード
	 * @return 子ノードのリスト
	 */
	@Lock(LockModeType.PESSIMISTIC_READ)
	List<UrlTreeNode> findByParent(String parent);

	/**
	 * 指定した親ノードは以下のノードのうち、削除済みでないものを検索します.
	 *
	 * @param parent 親ノード
	 * @return 削除されていない子ノードのリスト
	 */
	@Lock(LockModeType.PESSIMISTIC_READ)
	List<UrlTreeNode> findByParentAndDeletedFalse(String parent);

	/**
	 * このノードを論理削除状態するためにフラグを更新します.
	 *
	 * @param name　キー情報
	 * @param parent 親ノード
	 */
	@Modifying
	@Query("UPDATE UrlTreeNode u SET DELETED=1 WHERE u.name = :name and u.parent = :parent")
	void logicalDeleteByPK(@Param("name") String name, @Param("parent") String parent);

	/**
	 * 指定された親ノード配下のすべての子ノードを検索します.
	 *
	 * @param parent 親ノード
	 * @return 子ノードのリスト
	 */
	@Query("SELECT u FROM UrlTreeNode u WHERE u.parent like :parent")
	List<UrlTreeNode> getAllChildrenByParent(@Param("parent") String parent);

	/**
	 * 指定されたキーに該当するノードを物理削除します.
	 *
	 * @param name キー情報
	 * @param parent 親ノード
	 */
	@Modifying
	@Query("DELETE FROM UrlTreeNode u WHERE u.name = :name and u.parent = :parent")
	void deleteByPK(@Param("name") String name, @Param("parent") String parent);

	/**
	 * 指定されたキーに該当するノードを悲観的ロックを使用して取得します.
	 *
	 * @param name キー情報
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT u FROM UrlTreeNode u WHERE u.name = :name and u.parent = :parent")
	UrlTreeNode findOneForUpdate(@Param("name") String name, @Param("parent") String parent);
}
