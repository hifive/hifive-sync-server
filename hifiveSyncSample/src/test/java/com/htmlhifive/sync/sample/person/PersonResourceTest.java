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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.Expectations;
import mockit.Mocked;

import org.junit.Test;
import org.springframework.data.jpa.domain.Specifications;

import com.htmlhifive.sync.exception.BadRequestException;
import com.htmlhifive.sync.exception.DuplicateIdException;
import com.htmlhifive.sync.resource.ResourceQuerySpecifications;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.resource.common.ResourceItemCommonDataId;

/**
 * <H3>
 * PersonResourceのテストクラス.</H3>
 *
 * @author kishigam
 */
@SuppressWarnings({ "unchecked", "serial" })
public class PersonResourceTest {

    @Mocked
    private PersonRepository repository;

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
    public void testGetResourceItemByPersonIdString() {

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
     * {@link PersonResource#doGet(String)}用テストメソッド.
     */
    @Test
    public void testDoGetString() {

        // Arrange：正常系
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
                result = expected;
            }
        };

        // Act
        Person actual = target.doGet(targetItemId);

        // Assert：結果が正しいこと
        assertThat(actual, is(expected));
    }

    /**
     * {@link PersonResource#doGet(String)}用テストメソッド. 存在しない場合
     * {@link BadRequestException}がスローされる.
     */
    @Test(expected = BadRequestException.class)
    public void testDoGetStringFailBecausePersonNotExists() {

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
     * {@link PersonResource#doGetByQuery(List, Map)}用テストメソッド.
     */
    @Test
    public void testDoGetByQueryListMap() {

        // Arrange：正常系
        final PersonResource target = new PersonResource();

        final String personId1 = "person1";

        final ResourceItemCommonData common1 =
                new ResourceItemCommonData(
                        new ResourceItemCommonDataId("person", "resource1"), personId1);
        final ResourceItemCommonData common2 =
                new ResourceItemCommonData(
                        new ResourceItemCommonDataId("person", "resource2"), "person2");

        final Person person1 = new Person();
        person1.setPersonId(personId1);

        final List<ResourceItemCommonData> commonDataList =
                new ArrayList<ResourceItemCommonData>() {
                    {
                        add(common1);
                        add(common2);
                    }
                };

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

        final Map<Person, ResourceItemCommonData> expectedPersonMap =
                new HashMap<Person, ResourceItemCommonData>() {
                    {
                        put(person1, common1);
                    }
                };

        new Expectations() {
            {
                setField(target, repository);
                setField(target, querySpec);

                Specifications<Person> specs = (Specifications<Person>)any;

                querySpec.parseConditions(commonDataList, conditions);
                result = specs;

                repository.findAll(specs);
                result = expectedPersonList;
            }
        };

        // Act
        Map<Person, ResourceItemCommonData> actual =
                target.doGetByQuery(commonDataList, conditions);

        // Assert：結果が正しいこと
        assertThat(actual, is(expectedPersonMap));
    }

    /**
     * {@link PersonResource#doGetByQuery(List, Map)}用テストメソッド.
     * 共通データリストがnullの時は例外をそのままスロー.
     */
    @Test(expected = Exception.class)
    public void testDoGetByQueryListMapFailBecauseOfNullCommonList() {

        // Arrange：異常系
        final PersonResource target = new PersonResource();

        final String personId1 = "person1";

        final Map<String, String[]> conditions = new HashMap<String, String[]>() {
            {
                put("personId", new String[] { personId1 });
            }
        };

        new Expectations() {
            {
                setField(target, repository);
                setField(target, querySpec);

                querySpec.parseConditions(null, conditions);
                result = new NullPointerException();
            }
        };

        // Act
        target.doGetByQuery(null, conditions);

        // Assert：例外でなければ失敗
        fail();
    }

    /**
     * {@link PersonResource#doGetByQuery(List, Map)}用テストメソッド.
     * クエリ条件Mapがnullの時は条件なし(空のMap)と同様の結果となる.
     */
    @Test
    public void testDoGetByQueryListMapFailBecauseOfNullCondMap() {

        // Arrange：例外系
        final PersonResource target = new PersonResource();

        final String personId1 = "person1";
        final String personId2 = "person2";

        final ResourceItemCommonData common1 =
                new ResourceItemCommonData(
                        new ResourceItemCommonDataId("person", "resource1"), personId1);
        final ResourceItemCommonData common2 =
                new ResourceItemCommonData(
                        new ResourceItemCommonDataId("person", "resource2"), personId2);

        final Person person1 = new Person();
        person1.setPersonId(personId1);
        final Person person2 = new Person();
        person2.setPersonId(personId2);

        final List<ResourceItemCommonData> commonDataList =
                new ArrayList<ResourceItemCommonData>() {
                    {
                        add(common1);
                        add(common2);
                    }
                };

        final List<Person> expectedPersonList = new ArrayList<Person>() {
            {
                add(person1);
                add(person2);
            }
        };

        final Map<Person, ResourceItemCommonData> expectedPersonMap =
                new HashMap<Person, ResourceItemCommonData>() {
                    {
                        put(person1, common1);
                        put(person2, common2);
                    }
                };

        new Expectations() {
            {
                setField(target, repository);
                setField(target, querySpec);

                Specifications<Person> specs = (Specifications<Person>)any;

                querySpec.parseConditions(commonDataList, null);
                result = specs;

                repository.findAll(specs);
                result = expectedPersonList;
            }
        };

        // Act
        Map<Person, ResourceItemCommonData> actual = target.doGetByQuery(commonDataList, null);

        // Assert：結果が正しいこと
        assertThat(actual, is(expectedPersonMap));
    }

    /**
     * {@link PersonResource#doCreate(Person)}用テストメソッド.
     */
    @Test
    public void testDoCreatePerson() throws Exception {

        // Arrange：正常系
        final PersonResource target = new PersonResource();

        final String personId = "100";

        final Person newItem = new Person();
        newItem.setPersonId(personId);

        new Expectations() {
            {
                setField(target, repository);
                setField(target, querySpec);

                repository.exists(personId);
                result = false;

                repository.save(newItem);
                result = newItem;
            }
        };

        // Act
        String actual = target.doCreate(newItem);

        // Assert：結果が正しいこと
        assertThat(actual, is(equalTo(personId)));
    }

    /**
     * {@link PersonResource#doCreate(Person)}用テストメソッド.
     */
    @Test(expected = DuplicateIdException.class)
    public void testDoCreateFailBecauseOfDuplicatePerson() throws Exception {

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
                setField(target, querySpec);

                repository.exists(personId);
                result = true;

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
            assertThat((Person)actual.getCurrentItem(), is(equalTo(duplicated)));

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
    public void testDoUpdatePerson() {

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
        Person actual = target.doUpdate(item);

        // Assert：結果が正しいこと
        assertThat(actual, is(equalTo(item)));
    }

    /**
     * {@link PersonResource#doUpdate(Person)}用テストメソッド.<br>
     * 更新するPersonが存在しない場合{@link BadRequestException}がスローされる.
     */
    @Test(expected = BadRequestException.class)
    public void testDoUpdatePersonFailBecauseOfNotFound() {

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
    public void testDoDeleteString() {

        // Arrange：正常系
        final PersonResource target = new PersonResource();

        final String personId = null;

        final Person existing = new Person();
        existing.setPersonId(personId);

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
        // 論理削除のため、削除対象のエンティティをそのまま返す
        assertThat(actual, is(equalTo(existing)));
    }

    /**
     * {@link PersonResource#doDelete(String)}用テストメソッド.<br>
     * 更新するPersonが存在しない場合{@link BadRequestException}がスローされる.
     */
    @Test(expected = BadRequestException.class)
    public void testDoDeleteStringFailBecauseOfNotFound() {

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
     * {@link PersonResource#doDelete(String)}用テストメソッド. nullが渡された場合
     * {@link BadRequestException}がスローされる.
     */
    @Test(expected = BadRequestException.class)
    public void testDoDeleteStringFailBecauseOfNullInput() {

        // Arrange：正常系
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
