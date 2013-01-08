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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.htmlhifive.sync.resource.SyncConflictType;

/**
 * <H3>UploadCommonDataのテストクラス.</H3>
 *
 * @author kishigam
 */
public class UploadCommonDataTest {

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(UploadCommonData.class, notNullValue());
	}

	/**
	 * {@link UploadCommonData#UploadCommonData()}用テストメソッド.
	 */
	@Test
	public void testInstantiation() {
		UploadCommonData target = new UploadCommonData();
		assertThat(target, notNullValue());
	}

	/**
	 * {@link UploadCommonData#equals(Object)}用テストメソッド.
	 */
	@SuppressWarnings("serial")
	@Test
	public void testEquals() {

		final String storageId = "storageId";

		final UploadCommonData target = new UploadCommonData() {
			{
				setStorageId(storageId);
				setLastUploadTime(10);
				setSyncTime(100);
				setConflictType(SyncConflictType.NONE);
			}
		};

		UploadCommonData eq = new UploadCommonData() {
			{
				setStorageId(target.getStorageId());
				setLastUploadTime(target.getLastUploadTime());
				setSyncTime(target.getSyncTime());
				setConflictType(target.getConflictType());
			}
		};

		UploadCommonData ne1 = new UploadCommonData() {
			{
				setStorageId("other storageId");
				setLastUploadTime(target.getLastUploadTime());
				setSyncTime(target.getSyncTime());
				setConflictType(target.getConflictType());
			}
		};

		UploadCommonData ne2 = new UploadCommonData() {
			{
				setStorageId(target.getStorageId());
				setLastUploadTime(20);
				setSyncTime(target.getSyncTime());
				setConflictType(target.getConflictType());
			}
		};

		UploadCommonData ne3 = new UploadCommonData() {
			{
				setStorageId(target.getStorageId());
				setLastUploadTime(target.getLastUploadTime());
				setSyncTime(30);
				setConflictType(target.getConflictType());
			}
		};

		UploadCommonData ne4 = new UploadCommonData() {
			{
				setStorageId(target.getStorageId());
				setLastUploadTime(target.getLastUploadTime());
				setSyncTime(target.getSyncTime());
				setConflictType(SyncConflictType.UPDATED);
			}
		};

		// Act
		// Assert：結果が正しいこと
		assertThat(target.equals(eq), is(true));
		assertThat(target.equals(ne1), is(false));
		assertThat(target.equals(ne2), is(false));
		assertThat(target.equals(ne3), is(false));
		assertThat(target.equals(ne4), is(false));
	}

	/**
	 * {@link UploadCommonData#isLaterUploadThan(UploadCommonData)}用テストメソッド.
	 */
	@Test
	public void testIsLaterUploadThan() {

		// Arrange：正常系
		UploadCommonData last = new UploadCommonData();
		final UploadCommonData current = new UploadCommonData();

		// 二重送信
		last.setLastUploadTime(200);
		current.setLastUploadTime(100);
		assertThat(last.isLaterUploadThan(current), is(true));

		// 通常の上り更新リクエスト
		last.setLastUploadTime(100);
		current.setLastUploadTime(100);
		assertThat(last.isLaterUploadThan(current), is(false));

		// 起こりえない状態
		last.setLastUploadTime(100);
		current.setLastUploadTime(200);
		assertThat(last.isLaterUploadThan(current), is(false));
	}
}
