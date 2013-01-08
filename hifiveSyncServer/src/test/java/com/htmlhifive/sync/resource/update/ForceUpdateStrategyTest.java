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
 * <H3>ForceUpdateStrategyのテストクラス.</H3>
 *
 * @author kishigam
 */
public class ForceUpdateStrategyTest {

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(ForceUpdateStrategy.class, notNullValue());
	}

	/**
	 * {@link ForceUpdateStrategy#ForceUpdateStrategy()}用テストメソッド.
	 */
	@Test
	public void testInstantiation() {
		ForceUpdateStrategy target = new ForceUpdateStrategy();
		assertThat(target, notNullValue());
	}

	/**
	 * {@link ForceUpdateStrategy#resolveConflict(ResourceItemCommonData, Object, ResourceItemCommonData, Object)}
	 * 用テストメソッド.
	 *
	 * @param itemCommon 引数のモック
	 * @param serverCommon 引数のモック
	 */
	@SuppressWarnings("serial")
	@Test
	public void testResolveConflict() {

		ForceUpdateStrategy target = new ForceUpdateStrategy();

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
			Object actual = target.<Object> resolveConflict(itemCommon, item, serverCommon, serverItem);

			assertThat(actual, is(equalTo(item)));
		} catch (ItemUpdatedException e) {

			fail();
		}
	}
}
