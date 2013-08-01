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
package com.htmlhifive.resourcefw.sample.resource.person;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.htmlhifive.resourcefw.exception.AbstractResourceException;
import com.htmlhifive.resourcefw.exception.NotFoundException;
import com.htmlhifive.resourcefw.message.RequestMessage;
import com.htmlhifive.resourcefw.resource.BasicResource;
import com.htmlhifive.resourcefw.resource.ResourceClass;
import com.htmlhifive.resourcefw.resource.query.ResourceQuerySpecifications;
import com.htmlhifive.sync.resource.AbstractCrudSyncResource;

/**
 * resource frameworkの汎用抽象リソースを実装したsync機能対応の人情報管理リソース.
 *
 * @author kishigam
 */
@ResourceClass(name = "syncperson")
public class PersonSyncResource extends AbstractCrudSyncResource<Person> {

	/**
	 * JPAリポジトリ
	 */
	@Autowired
	private PersonRepository personRepository;

	/**
	 * クエリの解析・実行を担うSpecificationsオブジェクト.
	 */
	@Autowired
	private ResourceQuerySpecifications<Person> querySpec;

	/**
	 * @see BasicResource#findByIdForUpdate(RequestMessage)
	 */
	@Override
	public Object findByIdForUpdate(RequestMessage requestMessage) throws AbstractResourceException {

		String personId = getId(requestMessage);

		checkCanRead(requestMessage, personId);

		Person found = personRepository.findOneForUpdate(personId);
		if (found == null) {
			throw new NotFoundException("person not found :" + personId, requestMessage);
		}
		return found;
	}

	/**
	 * @see BasicResource#findByQueryForUpdate(RequestMessage)
	 */
	@Override
	public List<?> findByQueryForUpdate(RequestMessage requestMessage) throws AbstractResourceException {

		List<Person> foundList = (List<Person>) findByQuery(requestMessage);

		// findByQueryでlockチェック済

		List<Person> resultList = new ArrayList<>();
		for (Person person : foundList) {
			Person foundForUpdate = personRepository.findOneForUpdate(person.getPersonId());
			if (foundForUpdate != null) {
				resultList.add(foundForUpdate);
			}
		}

		return resultList;
	}

	/**
	 * このリソースが使用するJPAリポジトリを返します.
	 */
	@Override
	protected JpaRepository<Person, String> getRepository() {

		return personRepository;
	}

	/**
	 * このリソースが使用するSpecification実行オブジェクトを返します.
	 */
	@Override
	protected JpaSpecificationExecutor<Person> getSpecificationExecutor() {

		// PersonRepositoryはJpaSecificationExecutorも継承している
		return personRepository;
	}

	/**
	 * クエリの解析・実行を担うSpecificationsオブジェクトを返します.
	 */
	@Override
	protected ResourceQuerySpecifications<Person> getQuerySpec() {

		return querySpec;
	}

	/**
	 * このリソースのリソースアイテムが新規生成された時に与えられるID値を返します.
	 */
	@Override
	protected String createNewId() {
		return UUID.randomUUID().toString();
	}

	/**
	 * このリソースのリソースアイテムでID値を保持するフィールド名を返します.
	 */
	@Override
	protected String getIdFieldName() {
		return "personId";
	}

}
