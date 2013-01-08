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
package com.htmlhifive.sync.resource.update;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.htmlhifive.sync.exception.ItemUpdatedException;
import com.htmlhifive.sync.resource.SyncAction;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.resource.common.ResourceItemCommonDataId;

/**
 * <H3>ClientResolvingStrategyのテストクラス.</H3>
 *
 * @author kishigam
 */
public class ClientResolvingStrategyTest {

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(ClientResolvingStrategy.class, notNullValue());
	}

	/**
	 * {@link ClientResolvingStrategy#ClientResolvingStrategy()}用テストメソッド.
	 */
	@Test
	public void testInstantiation() {
		ClientResolvingStrategy target = new ClientResolvingStrategy();
		assertThat(target, notNullValue());
	}

	/**
	 * {@link ClientResolvingStrategy#resolveConflict(ResourceItemCommonData, Object, ResourceItemCommonData, Object)}
	 * 用テストメソッド.
	 */
	@SuppressWarnings("serial")
	@Test
	public void testResolveConflict() {

		ClientResolvingStrategy target = new ClientResolvingStrategy();

		ResourceItemCommonDataId id = new ResourceItemCommonDataId("resourceName", "resourceItemId");

		ResourceItemCommonData itemCommon = new ResourceItemCommonData(id, "targetItemId") {
			{
				setAction(SyncAction.UPDATE);
				setLastModified(100);
			}
		};
		Object item = new Object();

		ResourceItemCommonData serverCommon = new ResourceItemCommonData(id, "targetItemId") {
			{
				setAction(SyncAction.DELETE);
				setLastModified(200);
			}
		};
		Object serverItem = new Object();

		try {
			target.<Object> resolveConflict(itemCommon, item, serverCommon, serverItem);

			fail();
		} catch (ItemUpdatedException e) {

			assertThat(e.getConflictedCommonData(), is(equalTo(serverCommon)));
			assertThat(e.getCurrentItem(), is(equalTo(serverItem)));
		}
	}
}
