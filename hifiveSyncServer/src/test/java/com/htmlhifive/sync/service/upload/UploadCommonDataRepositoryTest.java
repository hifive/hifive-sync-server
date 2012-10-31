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
package com.htmlhifive.sync.service.upload;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import javax.annotation.Resource;
import javax.persistence.Table;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.htmlhifive.sync.resource.SyncConflictType;

/**
 * <H3>UploadCommonDataRepositoryのテストクラス.</H3>
 *
 * @author kishigam
 */
@SuppressWarnings("deprecation")
@ContextConfiguration(locations = "classpath:test-context.xml")
@TransactionConfiguration(transactionManager = "txManager")
public class UploadCommonDataRepositoryTest extends AbstractTransactionalJUnit4SpringContextTests {

	private static final String TABLE_NAME = UploadCommonData.class.getAnnotation(Table.class).name();

	@Resource
	private UploadCommonDataRepository target;

	private UploadCommonData data1;
	private UploadCommonData data2;
	private UploadCommonData data3;

	@Before
	public void setUp() {

		deleteFromTables(TABLE_NAME);

		data1 = new UploadCommonData();
		data1.setStorageId("storageId1");
		data1.setLastUploadTime(100);
		data1.setSyncTime(1000);
		data1.setConflictType(SyncConflictType.NONE);
		data1.setLockToken("token");

		data2 = new UploadCommonData();
		data2.setStorageId("storageId2");
		data2.setLastUploadTime(200);

		data3 = new UploadCommonData();
		data3.setStorageId("storageId3");
		data3.setLastUploadTime(100);

		target.save(data1);
		target.save(data2);
		target.save(data3);
	}

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(UploadCommonDataRepository.class, notNullValue());
	}

	/**
	 * {@link UploadCommonDataRepository#findOne()}用テストメソッド.<br>
	 */
	@Test
	public void testFindOne() {

		UploadCommonData actual = target.findOne("storageId1");

		assertThat(actual, is(not(equalTo(data1))));
		assertThat(actual.getStorageId(), is(equalTo(data1.getStorageId())));
		assertThat(actual.getLastUploadTime(), is(equalTo(data1.getLastUploadTime())));
		assertThat(actual.getSyncTime(), is(equalTo(0L)));
		assertThat(actual.getConflictType(), is(nullValue()));
		assertThat(actual.getLockToken(), is(nullValue()));
	}

}
