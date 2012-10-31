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
package com.htmlhifive.sync.service.lock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * <H3>LockCommonDataのテストクラス.</H3><br>
 * TODO 次期バージョンにて実装予定
 *
 * @author kishigam
 */
@SuppressWarnings("deprecation")
public class LockCommonDataTest {

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(LockCommonData.class, notNullValue());
	}

	/**
	 * {@link LockCommonData#LockCommonData()}用テストメソッド.
	 */
	@Test
	public void testInstantiation() {
		LockCommonData target = new LockCommonData();
		assertThat(target, notNullValue());
	}

	/**
	 * {@link LockCommonData#equals(Object)}用テストメソッド.
	 */
	@SuppressWarnings("serial")
	@Test
	public void testEquals() {

		final LockCommonData target = new LockCommonData() {
			{
				setStorageId("storageId");
				setSyncTime(100);
				setLockToken("token");
			}
		};

		LockCommonData eq = new LockCommonData() {
			{
				setStorageId(target.getStorageId());
				setSyncTime(target.getSyncTime());
				setLockToken(target.getLockToken());
			}
		};

		LockCommonData ne1 = new LockCommonData() {
			{
				setStorageId("other storageId");
				setSyncTime(target.getSyncTime());
				setLockToken(target.getLockToken());
			}
		};

		LockCommonData ne2 = new LockCommonData() {
			{
				setStorageId(target.getStorageId());
				setSyncTime(200);
				setLockToken(target.getLockToken());
			}
		};

		LockCommonData ne3 = new LockCommonData() {
			{
				setStorageId(target.getStorageId());
				setSyncTime(target.getSyncTime());
				setLockToken("other token");
			}
		};

		// Act
		// Assert：結果が正しいこと
		assertThat(target.equals(eq), is(true));
		assertThat(target.equals(ne1), is(false));
		assertThat(target.equals(ne2), is(false));
		assertThat(target.equals(ne3), is(false));
	}
}
