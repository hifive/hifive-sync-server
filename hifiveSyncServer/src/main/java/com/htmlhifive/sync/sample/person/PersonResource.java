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
package com.htmlhifive.sync.sample.person;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.htmlhifive.sync.common.ResourceItemCommonData;
import com.htmlhifive.sync.exception.BadRequestException;
import com.htmlhifive.sync.exception.DuplicateIdException;
import com.htmlhifive.sync.resource.AbstractSyncResource;
import com.htmlhifive.sync.resource.ClientResolvingStrategy;
import com.htmlhifive.sync.resource.SyncResourceService;

/**
 * personリソースの情報を同期リソースとして公開するためのリソースクラス.<br>
 * 同期データを専用サービスで管理する抽象リソースクラスをこのリソース用に実装します.
 */
@SyncResourceService(resourceName = "person", updateStrategy = ClientResolvingStrategy.class)
@Transactional(propagation = Propagation.MANDATORY)
public class PersonResource extends AbstractSyncResource<Person> {

	/**
	 * エンティティの永続化を担うリポジトリ.
	 */
	@Resource
	private PersonRepository repository;

	/**
	 * 単一データreadメソッドのリソース別独自処理を行う抽象メソッド.<br>
	 * エンティティをリポジトリから取得し、アイテムクラスのオブジェクトに設定して返します.
	 *
	 * @param targetItemId 対象リソースアイテムのID
	 * @return リソースアイテム
	 */
	@Override
	protected Person doGetResourceItem(String targetItemId) {

		return findPerson(targetItemId);
	}

	/**
	 * クエリによってリソースアイテムを取得する抽象メソッド.<br>
	 * 指定された共通データが対応するリソースアイテムであり、かつデータ項目が指定された条件に合致するものを検索し、返します.
	 *
	 * @param commonDataList 共通データリスト
	 * @param conditions 条件Map(データ項目名,データ項目の条件)
	 * @return 条件に合致するリソースアイテム(CommonDataを値として持つMap)
	 */
	@Override
	protected Map<Person, ResourceItemCommonData> doExecuteQuery(List<ResourceItemCommonData> commonDataList,
			Map<String, String[]> conditions) {

		Map<Person, ResourceItemCommonData> itemMap = new HashMap<>();
		for (ResourceItemCommonData common : commonDataList) {

			Person item = doGetResourceItem(common.getTargetItemId());

			// TODO: queryの適用

			itemMap.put(item, common);
		}

		return itemMap;
	}

	/**
	 * createメソッドのリソース別独自処理. <br>
	 * エンティティを新規生成、保存し、リソースアイテムのIDを返します.
	 *
	 * @param newItem 生成内容を含むリソースアイテム
	 * @return 採番されたリソースアイテムのID
	 */
	@Override
	protected String doCreate(Person newItem) throws DuplicateIdException {

		if (repository.exists(newItem.getPersonId())) {

			throw new DuplicateIdException(newItem.getPersonId(), doGetResourceItem(newItem.getPersonId()));
		}

		repository.save(newItem);

		return newItem.getPersonId();
	}

	/**
	 * updateメソッドのリソース別独自処理.<br>
	 *
	 * @param item 更新内容を含むリソースアイテム
	 */
	@Override
	protected Person doUpdate(Person item) {

		// itemはDTOとしてのPersonなので、永続データにアタッチするために取得、コピーする

		Person updatingEntity = findPerson(item.getPersonId());
		updatingEntity.setName(item.getName());
		updatingEntity.setAge(item.getAge());
		updatingEntity.setOrganization(item.getOrganization());

		return repository.save(updatingEntity);
	}

	/**
	 * deleteメソッドのリソース別独自処理. <br>
	 * エンティティをリポジトリから取得し、物理削除します.
	 *
	 * @param targetItemId リソースアイテムのID
	 */
	@Override
	protected Person doDelete(String targetItemId) {

		Person removingEntity = findPerson(targetItemId);

		repository.delete(removingEntity);

		return Person.emptyPerson(targetItemId);
	}

	/**
	 * このリソースのリソースアイテムのIDから1件のエンティティをリポジトリを検索して取得します. <br>
	 *
	 * @param personId リソースアイテムのID
	 * @return Personエンティティ
	 */
	private Person findPerson(String personId) {
		Person found = repository.findOne(personId);
		if (found == null) {
			throw new BadRequestException("entity not found :" + personId);
		}
		return found;
	}
}
