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
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.Table;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

/**
 * <H3>PersonRepositoryのテストクラス.</H3>
 *
 * @author kishigam
 */
@ContextConfiguration(locations = "classpath:test-context.xml")
@TransactionConfiguration(transactionManager = "txManager")
@SuppressWarnings("serial")
public class PersonRepositoryTest extends AbstractTransactionalJUnit4SpringContextTests {

	private static final String TABLE_NAME = Person.class.getAnnotation(Table.class).name();

	@Resource
	private PersonRepository target;

	@Resource
	private PersonQuerySpecifications querySpecs;

	private Person personA;
	private Person personB;
	private Person personC;

	private String personIdA = "A";
	private String personIdB = "B";
	private String personIdC = "C";

	@Before
	public void setUp() {

		deleteFromTables(TABLE_NAME);

		personA = new Person();
		personB = new Person();
		personC = new Person();

		personA.setPersonId(personIdA);
		personA.setName("nameA");
		personA.setOrganization("org1");

		personB.setPersonId(personIdB);
		personB.setName("nameB");
		personB.setOrganization("org2");

		personC.setPersonId(personIdC);
		personC.setName("nameC");
		personC.setOrganization(personA.getOrganization());

		target.save(personA);
		target.save(personB);
		target.save(personC);
	}

	/**
	 * {@link PersonRepository#findAll(org.springframework.data.jpa.domain.Specification)} 用テストメソッド.
	 */
	@Test
	public void testFindAllBySpecOfPersonId() {

		Map<String, String[]> personCond = new HashMap<String, String[]>() {
			{
				put("personId", new String[] { personA.getPersonId(), personB.getPersonId(), personC.getPersonId() });
				put("organization", new String[] { personA.getOrganization() });
			}
		};

		List<Person> actual = target.findAll(querySpecs.parseConditions(personCond));

		assertThat(actual.size(), is(equalTo(2)));
		assertThat(actual.contains(personA), is(true));
		assertThat(actual.contains(personB), is(false));
		assertThat(actual.contains(personC), is(true));
	}

	/**
	 * {@link PersonRepository#findAll(org.springframework.data.jpa.domain.Specification)} クエリ条件が空の場合は全件検索となる.
	 */
	@Test
	public void testFindAllBySpecOfCommonDataCondOnly() {

		Map<String, String[]> personCond = new HashMap<String, String[]>() {
			{
				// empty
			}
		};

		List<Person> actual = target.findAll(querySpecs.parseConditions(personCond));

		assertThat(actual.size(), is(equalTo(3)));
		assertThat(actual.contains(personA), is(true));
		assertThat(actual.contains(personB), is(true));
		assertThat(actual.contains(personC), is(true));
	}

}
