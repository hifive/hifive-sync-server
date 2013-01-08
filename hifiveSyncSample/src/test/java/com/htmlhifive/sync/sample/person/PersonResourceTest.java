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
package com.htmlhifive.sync.sample.person;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import mockit.Expectations;
import mockit.Mocked;

import org.junit.Test;
import org.springframework.data.jpa.domain.Specifications;

import com.htmlhifive.sync.exception.BadRequestException;
import com.htmlhifive.sync.exception.DuplicateIdException;
import com.htmlhifive.sync.exception.ItemUpdatedException;
import com.htmlhifive.sync.resource.ResourceItemConverter;
import com.htmlhifive.sync.resource.ResourceItemWrapper;
import com.htmlhifive.sync.resource.ResourceQuerySpecifications;
import com.htmlhifive.sync.resource.SyncAction;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.resource.common.ResourceItemCommonDataId;
import com.htmlhifive.sync.resource.common.ResourceItemCommonDataService;
import com.htmlhifive.sync.resource.update.UpdateStrategy;
import com.htmlhifive.sync.service.upload.UploadCommonData;

/**
 * <H3>PersonResourceのテストクラス.</H3>
 *
 * @author kishigam
 */
@SuppressWarnings({ "unchecked", "serial" })
public class PersonResourceTest {

	@Mocked
	private PersonRepository repository;

	@Mocked
	private EntityManager entityManager;

	@Mocked
	private ResourceQuerySpecifications<Person> querySpec;

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(PersonResource.class, notNullValue());
	}

	/**
	 * {@link PersonResource#PersonResource()}用テストメソッド.
	 */
	@Test
	public void testInstantiation() {
		PersonResource target = new PersonResource();
		assertThat(target, notNullValue());
	}

	/**
	 * {@link PersonResource#getResourceItemByPersonId(String)}用テストメソッド.
	 */
	@Test
	public void testGetResourceItemByPersonId() {

		// Arrange：正常系
		final PersonResource target = new PersonResource();

		final String personId = "personId";

		final Person expected = new Person();
		expected.setPersonId(personId);
		expected.setName("person");
		expected.setAge(10);
		expected.setOrganization("org");

		new Expectations() {
			{
				setField(target, repository);
				setField(target, querySpec);

				repository.findOne(personId);
				result = expected;
			}
		};

		// Act
		Person actual = target.getResourceItemByPersonId(personId);

		// Assert：結果が正しいこと
		assertThat(actual, is(expected));
	}

	/**
	 * {@link PersonResource#getResourceItemByPersonId(String)}用テストメソッド.<br>
	 * 存在しない場合 {@link BadRequestException}がスローされる.
	 */
	@Test(expected = BadRequestException.class)
	public void testGetResourceItemByPersonIdFailBecausePersonNotExists() {

		// Arrange：異常系
		final PersonResource target = new PersonResource();

		final String personId = "personId";

		new Expectations() {
			{
				setField(target, repository);
				setField(target, querySpec);

				repository.findOne(personId);
				result = null;
			}
		};

		// Act
		target.getResourceItemByPersonId(personId);

		// Assert：例外がスローされなければ失敗
		fail();
	}

	/**
	 * {@link PersonResource#doGet(String...)}用テストメソッド.
	 */
	@Test
	public void testDoGet() {

		// Arrange：正常系
		final PersonResource target = new PersonResource();

		final String personId = "personId";

		final Person expected = new Person();
		expected.setPersonId(personId);
		expected.setName("person");
		expected.setAge(10);
		expected.setOrganization("org");

		Map<String, Person> expectedMap = new HashMap<String, Person>() {
			{
				put(personId, expected);
			}
		};

		new Expectations() {
			{
				setField(target, repository);
				setField(target, querySpec);

				repository.findOne(personId);
				result = expected;
			}
		};

		// Act
		Map<String, Person> actual = target.doGet(personId);

		// Assert：結果が正しいこと
		assertThat(actual, is(expectedMap));
	}

	/**
	 * {@link PersonResource#doGet(String...)}用テストメソッド.<br>
	 * 存在しない場合 {@link BadRequestException}がスローされる.
	 */
	@Test(expected = BadRequestException.class)
	public void testDoGetFailBecausePersonNotExists() {

		// Arrange：異常系
		final PersonResource target = new PersonResource();

		final String targetItemId = "personId";

		final Person expected = new Person();
		expected.setPersonId(targetItemId);
		expected.setName("person");
		expected.setAge(10);
		expected.setOrganization("org");

		new Expectations() {
			{
				setField(target, repository);
				setField(target, querySpec);

				repository.findOne(targetItemId);
				result = null;
			}
		};

		// Act
		target.doGet(targetItemId);

		// Assert：例外がスローされなければ失敗
		fail();
	}

	/**
	 * {@link PersonResource#doGetByQuery(Map, String...)}用テストメソッド.
	 */
	@Test
	public void testDoGetByQuery() {

		// Arrange：正常系
		final PersonResource target = new PersonResource();

		final String personId1 = "person1";

		final Person person1 = new Person();
		person1.setPersonId(personId1);

		final Map<String, String[]> conditions = new HashMap<String, String[]>() {
			{
				put("personId", new String[] { personId1 });
			}
		};

		final List<Person> expectedPersonList = new ArrayList<Person>() {
			{
				add(person1);
			}
		};

		final Map<String, Person> expectedPersonMap = new HashMap<String, Person>() {
			{
				put(personId1, person1);
			}
		};

		new Expectations() {
			{
				setField(target, repository);
				setField(target, querySpec);

				Specifications<Person> specs = (Specifications<Person>) any;

				querySpec.parseConditions(conditions);
				result = specs;

				repository.findAll(specs);
				result = expectedPersonList;
			}
		};

		// Act
		Map<String, Person> actual = target.doGetByQuery(conditions);

		// Assert：結果が正しいこと
		assertThat(actual, is(expectedPersonMap));
	}

	/**
	 * {@link PersonResource#doGetByQuery(Map, String...)}用テストメソッド.<br>
	 * クエリ条件Mapがnullの時は条件なし(空のMap)と同様の結果となる.
	 */
	@Test
	public void testDoGetByQueryFailBecauseOfNullCondMap() {

		// Arrange：例外系
		final PersonResource target = new PersonResource();

		final String personId1 = "person1";
		final String personId2 = "person2";

		final Person person1 = new Person();
		person1.setPersonId(personId1);
		final Person person2 = new Person();
		person2.setPersonId(personId2);

		final List<Person> expectedPersonList = new ArrayList<Person>() {
			{
				add(person1);
				add(person2);
			}
		};

		final Map<String, Person> expectedPersonMap = new HashMap<String, Person>() {
			{
				put(personId1, person1);
				put(personId2, person2);
			}
		};

		new Expectations() {
			{
				setField(target, repository);
				setField(target, querySpec);

				Specifications<Person> specs = (Specifications<Person>) any;

				querySpec.parseConditions(null);
				result = specs;

				repository.findAll(specs);
				result = expectedPersonList;
			}
		};

		// Act
		Map<String, Person> actual = target.doGetByQuery(null);

		// Assert：結果が正しいこと
		assertThat(actual, is(expectedPersonMap));
	}

	/**
	 * {@link PersonResource#doCreate(Person)}用テストメソッド.
	 */
	@Test
	public void testDoCreate() throws Exception {

		// Arrange：正常系
		final PersonResource target = new PersonResource();

		final String personId = "100";

		final Person newItem = new Person();
		newItem.setPersonId(personId);

		new Expectations() {
			{
				setField(target, repository);
				setField(target, entityManager);
				setField(target, querySpec);

				entityManager.persist(newItem);
				entityManager.flush();
			}
		};

		// Act
		String actual = target.doCreate(newItem);

		// Assert：結果が正しいこと
		assertThat(actual, is(equalTo(personId)));
	}

	/**
	 * {@link PersonResource#doCreate(Person)}用テストメソッド. <br>
	 * キー重複が発生した場合 {@link DuplicateIdException}がスローされる.
	 */
	@Test(expected = DuplicateIdException.class)
	public void testDoCreateFailBecauseOfDuplicatePersonByPersist() throws Exception {

		// Arrange：例外系
		final PersonResource target = new PersonResource();

		final String personId = "100";

		final Person newItem = new Person();
		newItem.setPersonId(personId);

		final Person duplicated = new Person();
		duplicated.setPersonId(personId);

		new Expectations() {
			{
				setField(target, repository);
				setField(target, entityManager);
				setField(target, querySpec);

				entityManager.persist(newItem);
				result = new PersistenceException();

				repository.findOne(personId);
				result = duplicated;
			}
		};

		try {
			// Act
			target.doCreate(newItem);

			// Assert：例外でなければ失敗
			fail();
		} catch (DuplicateIdException actual) {
			assertThat(actual.getDuplicatedTargetItemId(), is(equalTo(personId)));
			assertThat((Person) actual.getCurrentItem(), is(equalTo(duplicated)));

			throw actual;
		}
	}

	/**
	 * {@link PersonResource#doCreate(Person)}用テストメソッド. <br>
	 * キー重複が(flush時に)発生した場合 {@link DuplicateIdException}がスローされる.
	 */
	@Test(expected = DuplicateIdException.class)
	public void testDoCreateFailBecauseOfDuplicatePersonByFlush() throws Exception {

		// Arrange：例外系
		final PersonResource target = new PersonResource();

		final String personId = "100";

		final Person newItem = new Person();
		newItem.setPersonId(personId);

		final Person duplicated = new Person();
		duplicated.setPersonId(personId);

		new Expectations() {
			{
				setField(target, repository);
				setField(target, entityManager);
				setField(target, querySpec);

				entityManager.persist(newItem);
				entityManager.flush();
				result = new PersistenceException();

				repository.findOne(personId);
				result = duplicated;
			}
		};

		try {
			// Act
			target.doCreate(newItem);

			// Assert：例外でなければ失敗
			fail();
		} catch (DuplicateIdException actual) {
			assertThat(actual.getDuplicatedTargetItemId(), is(equalTo(personId)));
			assertThat((Person) actual.getCurrentItem(), is(equalTo(duplicated)));

			throw actual;
		}
	}

	/**
	 * {@link PersonResource#doCreate(Person)}用テストメソッド.<br>
	 * nullが渡された場合 {@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testDoCreateFailBecauseOfNullInput() throws Exception {

		// Arrange：異常系
		final PersonResource target = new PersonResource();

		new Expectations() {
			{
				setField(target, repository);
				setField(target, entityManager);
				setField(target, querySpec);

			}
		};

		// Act
		target.doCreate(null);

		// Assert：例外でなければ失敗
		fail();
	}

	/**
	 * {@link PersonResource#doUpdate(Person)}用テストメソッド.
	 */
	@Test
	public void testDoUpdate() {

		// Arrange：正常系
		final PersonResource target = new PersonResource();

		final String personId = "id";

		final Person item = new Person();
		item.setPersonId(personId);
		item.setName("update");

		new Expectations() {
			{
				setField(target, repository);
				setField(target, querySpec);

				repository.exists(personId);
				result = true;

				repository.save(item);
				result = item;
			}
		};

		// Act
		String actual = target.doUpdate(item);

		// Assert：結果が正しいこと
		assertThat(actual, is(equalTo(personId)));
	}

	/**
	 * {@link PersonResource#update(UploadCommonData, ResourceItemCommonData, Person)} 用テストメソッド.<br>
	 * 競合が発生し解決ができない場合で、サーバ側アイテムデータが削除されている場合はIDのみのオブジェクトを返す.
	 */
	@Test
	public void testUpdateOccursConflictAndReturnDeletedItem() throws Exception {

		// Arrange：例外系
		final PersonResource target = new PersonResource();

		final long syncTime = 50;
		final UploadCommonData uploadCommon = new UploadCommonData() {
			{
				setSyncTime(syncTime);
			}
		};

		final String targetItemId = "personId1";

		final ResourceItemCommonDataId id = new ResourceItemCommonDataId("person", "1");
		final ResourceItemCommonData updatingItemCommon = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(false);
				setAction(SyncAction.UPDATE);
				setLastModified(10);
			}
		};
		final Person updatingItem = new Person() {
			{
				setPersonId(targetItemId);
				setName("update");
			}
		};

		final ResourceItemCommonData itemCommonForUpdate = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(true);
				setAction(SyncAction.DELETE);
				setLastModified(20);
			}
		};
		final Person itemForUpdate = new Person() {
			{
				setPersonId(targetItemId);
				setName("deleted");
			}
		};

		new Expectations() {
			UpdateStrategy updateStrategy;
			ResourceItemConverter<Person> defaultItemConverter;
			ResourceItemCommonDataService commonDataService;
			{
				setField(target, updateStrategy);
				setField(target, defaultItemConverter);
				setField(target, commonDataService);

				setField(target, repository);

				commonDataService.currentCommonDataForUpdate(id);
				result = itemCommonForUpdate;

				updateStrategy.resolveConflict(updatingItemCommon, updatingItem, itemCommonForUpdate, null);
				result = new ItemUpdatedException(itemCommonForUpdate, null);

				repository.findOne(targetItemId);
				result = itemForUpdate;
			}
		};

		// Act
		ResourceItemWrapper<Person> actual = target.update(uploadCommon, updatingItemCommon, updatingItem);

		// Assert：結果が正しいこと
		assertThat(actual.getItemCommonData(), is(equalTo(itemCommonForUpdate)));

		Person expectedItem = new Person() {
			{
				setPersonId(targetItemId);
			}
		};

		assertThat(actual.getItem(), is(equalTo(expectedItem)));
	}

	/**
	 * {@link PersonResource#doUpdate(Person)}用テストメソッド.<br>
	 * 更新するPersonが存在しない場合{@link BadRequestException}がスローされる.
	 */
	@Test(expected = BadRequestException.class)
	public void testDoUpdateFailBecauseOfNotFound() {

		// Arrange：正常系
		final PersonResource target = new PersonResource();

		final String personId = "id";

		final Person item = new Person();
		item.setPersonId(personId);
		item.setName("update");

		new Expectations() {
			{
				setField(target, repository);
				setField(target, querySpec);

				repository.exists(personId);
				result = false;
			}
		};

		// Act
		target.doUpdate(item);

		// Assert：例外でなければ失敗
		fail();
	}

	/**
	 * {@link PersonResource#doUpdate(Person)}用テストメソッド.<br>
	 * nullが渡された場合 {@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testDoUpdateFailBecauseOfNullInput() throws Exception {

		// Arrange：異常系
		final PersonResource target = new PersonResource();

		new Expectations() {
			{
				setField(target, repository);
				setField(target, querySpec);

			}
		};

		// Act
		target.doUpdate(null);

		// Assert：例外でなければ失敗
		fail();
	}

	/**
	 * {@link PersonResource#doDelete(String)}用テストメソッド.
	 */
	@Test
	public void testDoDelete() {

		// Arrange：正常系
		final PersonResource target = new PersonResource();

		final String personId = null;

		final Person existing = new Person() {
			{
				setPersonId(personId);
				setName("toDelete");
			}
		};

		new Expectations() {
			{
				setField(target, repository);
				setField(target, querySpec);

				repository.findOne(personId);
				result = existing;

				// 論理削除のため、repository#deleteは呼ばれない
			}
		};

		// Act
		Person actual = target.doDelete(personId);

		// Assert：結果が正しいこと
		// 論理削除のため、personIdのみセットされたアイテムが返される.
		final Person expected = new Person() {
			{
				setPersonId(personId);
			}
		};
		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * {@link PersonResource#doDelete(String)}用テストメソッド.<br>
	 * 更新するPersonが存在しない場合{@link BadRequestException}がスローされる.
	 */
	@Test(expected = BadRequestException.class)
	public void testDoDeleteFailBecauseOfNotFound() {

		// Arrange：異常系
		final PersonResource target = new PersonResource();

		final String personId = null;

		new Expectations() {
			{
				setField(target, repository);
				setField(target, querySpec);

				repository.findOne(personId);
				result = null;

				// 論理削除のため、repository#deleteは呼ばれない
			}
		};

		// Act
		target.doDelete(personId);

		// Assert：例外でなければ失敗
		fail();
	}

	/**
	 * {@link PersonResource#doDelete(String)}用テストメソッド.<br>
	 * nullが渡された場合 {@link BadRequestException}がスローされる.
	 */
	@Test(expected = BadRequestException.class)
	public void testDoDeleteFailBecauseOfNullInput() {

		// Arrange：異常系
		final PersonResource target = new PersonResource();

		new Expectations() {
			{
				setField(target, repository);
				setField(target, querySpec);
			}
		};

		// Act
		target.doDelete(null);

		// Assert：例外でなければ失敗
		fail();
	}
}
