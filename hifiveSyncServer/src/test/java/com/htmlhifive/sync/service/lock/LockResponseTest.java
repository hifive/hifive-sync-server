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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.htmlhifive.sync.resource.ResourceItemWrapper;

/**
 * <H3>LockResponseのテストクラス.</H3><br>
 * TODO 次期バージョンにて実装予定
 *
 * @author kishigam
 */
@SuppressWarnings("deprecation")
public class LockResponseTest {

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(LockResponse.class, notNullValue());
	}

	/**
	 * {@link LockResponse#LockResponse()}用テストメソッド.
	 */
	@SuppressWarnings("serial")
	@Test
	public void testInstantiation() {

		LockCommonData lockCommonData = new LockCommonData() {
			{
				setStorageId("storageId");
			}
		};

		LockResponse target = new LockResponse(lockCommonData);

		assertThat(target, notNullValue());
		assertThat(target.getLockCommonData(), is(equalTo(lockCommonData)));
	}

	/**
	 * {@link LockResponse#equals(Object)}用テストメソッド.
	 */
	@SuppressWarnings("serial")
	@Test
	public void testEquals() {

		final LockCommonData common = new LockCommonData() {
			{
				setStorageId("storageId");
			}
		};
		final LockCommonData anotherCommon = new LockCommonData() {
			{
				setStorageId("another storageId");
			}
		};

		final Map<String, List<ResourceItemWrapper<?>>> resourceItems = new HashMap<String, List<ResourceItemWrapper<?>>>() {
			{
				put("resource1", new ArrayList<ResourceItemWrapper<?>>());
			}
		};
		final Map<String, List<ResourceItemWrapper<?>>> anothereResourceItems = new HashMap<String, List<ResourceItemWrapper<?>>>() {
			{
				put("resource2", new ArrayList<ResourceItemWrapper<?>>());
			}
		};

		final LockResponse target = new LockResponse(common) {
			{
				setResourceItems(resourceItems);
			}
		};

		LockResponse eq = new LockResponse(target.getLockCommonData()) {
			{
				setResourceItems(target.getResourceItems());
			}
		};

		LockResponse ne1 = new LockResponse(anotherCommon) {
			{
				setResourceItems(target.getResourceItems());
			}
		};

		LockResponse ne2 = new LockResponse(target.getLockCommonData()) {
			{
				setResourceItems(anothereResourceItems);
			}
		};

		assertThat(target.equals(eq), is(true));
		assertThat(target.equals(ne1), is(false));
		assertThat(target.equals(ne2), is(false));
	}
}
