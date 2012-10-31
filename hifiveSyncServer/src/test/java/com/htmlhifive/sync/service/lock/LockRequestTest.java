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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.htmlhifive.sync.resource.ResourceQueryConditions;

/**
 * <H3>LockRequestのテストクラス.</H3><br>
 * TODO 次期バージョンにて実装予定
 *
 * @author kishigam
 */
@SuppressWarnings("deprecation")
public class LockRequestTest {

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(LockRequest.class, notNullValue());
	}

	/**
	 * {@link LockRequest#LockRequest()}用テストメソッド.
	 */
	@Test
	public void testInstantiation() {
		LockRequest target = new LockRequest();
		assertThat(target, notNullValue());
	}

	/**
	 * {@link LockRequest#equals(Object)}用テストメソッド.
	 */
	@SuppressWarnings("serial")
	@Test
	public void testEquals() {

		final LockCommonData common = new LockCommonData() {
			{
				setStorageId("storageId");
			}
		};

		final LockCommonData otherCommon = new LockCommonData() {
			{
				setStorageId("other storageId");
			}
		};

		final Map<String, List<ResourceQueryConditions>> queries = new HashMap<String, List<ResourceQueryConditions>>() {
			{
				put("resource1", new ArrayList<ResourceQueryConditions>());
			}
		};

		final Map<String, List<ResourceQueryConditions>> otherQueries = new HashMap<String, List<ResourceQueryConditions>>() {
			{
				put("resource2", new ArrayList<ResourceQueryConditions>());
			}
		};

		final LockRequest target = new LockRequest() {

			{
				setLockCommonData(common);
				setQueries(queries);
			}
		};

		final LockRequest eq = new LockRequest() {

			{
				setLockCommonData(common);
				setQueries(queries);
			}
		};

		final LockRequest ne1 = new LockRequest() {

			{
				setLockCommonData(otherCommon);
				setQueries(queries);
			}
		};

		final LockRequest ne2 = new LockRequest() {

			{
				setLockCommonData(common);
				setQueries(otherQueries);
			}
		};

		assertThat(target.equals(eq), is(true));
		assertThat(target.equals(ne1), is(false));
		assertThat(target.equals(ne2), is(false));
	}
}