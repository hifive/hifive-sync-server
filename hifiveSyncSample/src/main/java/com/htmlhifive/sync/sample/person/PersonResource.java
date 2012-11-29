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
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import com.htmlhifive.sync.exception.BadRequestException;
import com.htmlhifive.sync.exception.DuplicateIdException;
import com.htmlhifive.sync.resource.AbstractSyncResource;
import com.htmlhifive.sync.resource.ResourceQuerySpecifications;
import com.htmlhifive.sync.resource.SyncResourceService;
import com.htmlhifive.sync.resource.update.ClientResolvingStrategy;

/**
 * personリソースの情報を同期リソースとして公開するためのリソースクラス.<br>
 * 同期データを専用サービスで管理する抽象リソースクラスをこのリソース用に実装します.
 */
@SyncResourceService(resourceName = "person", updateStrategy = ClientResolvingStrategy.class)
public class PersonResource extends AbstractSyncResource<Person> {

	/**
	 * エンティティの永続化を担うリポジトリ.
	 */
	@Resource
	private PersonRepository repository;

	/**
	 * 新規データの永続化に使用するEntityManager.
	 *
	 * @see {@link this#doCreate(Person)}
	 */
	@PersistenceContext
	private EntityManager entitymanager;

	/**
	 * エンティティのクエリ仕様.
	 */
	@Resource(type = PersonQuerySpecifications.class)
	private ResourceQuerySpecifications<Person> querySpec;

	/**
	 * 指定されたIDだけを持つPersonを返します.<br>
	 * Personが存在しない場合、{@link BadRequestException}をスローします.
	 *
	 * @param personId ID
	 * @return Personオブジェクト
	 * @throws BadRequestException Personが存在しない場合
	 */
	public Person getResourceItemByPersonId(final String personId) {

		// Personデータを検索(共通データ、ロックは考慮しない)
		return findPerson(personId);

	}

	/**
	 * データ取得メソッドのリソース別独自処理を行う抽象メソッド.<br>
	 * 与えられた識別子が示すリソースアイテムを返します.
	 *
	 * @param ids 各リソースアイテムの識別子(複数可)
	 * @return リソースアイテム(識別子をKeyとするMap)
	 */
	@Override
	protected Map<String, Person> doGet(String... ids) {

		Map<String, Person> itemMap = new HashMap<>();
		for (String personId : ids) {
			itemMap.put(personId, findPerson(personId));
		}

		return itemMap;
	}

	/**
	 * クエリによってリソースアイテムを取得する抽象メソッド.<br>
	 * 与えられた識別子が示すアイテムの中で、データ項目が指定された条件に合致するものを返します.
	 *
	 * @param conditions 条件Map(データ項目名,データ項目の条件)
	 * @param ids 各リソースアイテムの識別子(複数可)
	 * @return 条件に合致するリソースアイテム(アイテムの識別子をkeyとするMap)
	 */
	@Override
	protected Map<String, Person> doGetByQuery(Map<String, String[]> conditions, String... ids) {

		Map<String, Person> itemMap = new HashMap<>();

		// Specificationsを用いたクエリ実行
		List<Person> personList = repository.findAll(querySpec.parseConditions(conditions, ids));

		for (Person person : personList) {
			itemMap.put(person.getPersonId(), person);
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

		String personId = newItem.getPersonId();

		// 以下は、既に採番されたIDでcreateを行うために必要
		// リソースが一意なIDを並行性を考慮して採番するのであれば不要(saveだけ行う)
		try {

			// JPARepository#saveではキー重複の時EntityManager#mergeが呼ばれてしまう可能性があるため、EntityManager#persistを使用する
			// また、flushしないとトランザクションコミット時まで一意制約違反が遅れて発生してしまうためここでflushする
			entitymanager.persist(newItem);
			entitymanager.flush();

		} catch (PersistenceException e) {

			// キー重複
			// レスポンスに既存アイテムのIDや内容を渡すために例外オブジェクトにセット
			throw new DuplicateIdException(personId, doGet(personId).get(personId));
		}

		return personId;
	}

	/**
	 * updateメソッドのリソース別独自処理.<br>
	 *
	 * @param item 更新内容を含むリソースアイテム
	 * @return リソースアイテムの識別子
	 */
	@Override
	protected String doUpdate(Person item) {

		// itemはDTOとしてのPersonなので、永続データとして存在することを確認する
		if (!repository.exists(item.getPersonId())) {

			throw new BadRequestException("entity not found :" + item.getPersonId());
		}

		return repository.save(item).getPersonId();
	}

	/**
	 * deleteメソッドのリソース別独自処理. <br>
	 * 論理削除のため、エンティティは変更せずにIDのみ設定された空のリソースアイテムを返します.
	 *
	 * @param targetItemId リソースアイテムのID
	 */
	@Override
	protected Person doDelete(String targetItemId) {

		findPerson(targetItemId);

		Person emptyPerson = new Person();
		emptyPerson.setPersonId(targetItemId);
		return emptyPerson;
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
