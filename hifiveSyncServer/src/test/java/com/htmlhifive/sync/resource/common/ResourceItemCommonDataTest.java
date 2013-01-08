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
package com.htmlhifive.sync.resource.common;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import mockit.Mocked;

import org.junit.Test;

import com.htmlhifive.sync.resource.SyncAction;
import com.htmlhifive.sync.resource.SyncConflictType;

/**
 * <H3>ResourceItemCommonDataのテストクラス.</H3>
 *
 * @author kishigam
 */
public class ResourceItemCommonDataTest {

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(ResourceItemCommonData.class, notNullValue());
	}

	/**
	 * {@link ResourceItemCommonData#ResourceItemCommonData()}用テストメソッド.
	 */
	@Test
	public void testInstantiation() {
		String targetItemId = "targetItemId1";
		ResourceItemCommonDataId id = new ResourceItemCommonDataId("resourceName1", "resourceItemId1");

		ResourceItemCommonData target = new ResourceItemCommonData(id, targetItemId);
		assertThat(target, notNullValue());
		assertThat(target.getId(), is(equalTo(id)));
		assertThat(target.getTargetItemId(), is(equalTo(target.getTargetItemId())));
	}

	/**
	 * {@link ResourceItemCommonData#compareTo(ResourceItemCommonData)}用テストメソッド.
	 */
	@Test
	public void testCompareToResourceItemCommonData() {

		// Arrange：正常系
		ResourceItemCommonData target = new ResourceItemCommonData(new ResourceItemCommonDataId("b", "2"), "target");

		ResourceItemCommonData gt1 = new ResourceItemCommonData(new ResourceItemCommonDataId("a", "2"), "gt1");
		ResourceItemCommonData gt2 = new ResourceItemCommonData(new ResourceItemCommonDataId("b", "1"), "gt2");
		ResourceItemCommonData eq = new ResourceItemCommonData(new ResourceItemCommonDataId("b", "2"), "eq");
		ResourceItemCommonData lt1 = new ResourceItemCommonData(new ResourceItemCommonDataId("b", "3"), "lt1");
		ResourceItemCommonData lt2 = new ResourceItemCommonData(new ResourceItemCommonDataId("c", "1"), "lt2");

		assertThat(target.compareTo(gt1) > 0, is(true));
		assertThat(target.compareTo(gt2) > 0, is(true));
		assertThat(target.compareTo(eq) == 0, is(true));
		assertThat(target.compareTo(lt1) < 0, is(true));
		assertThat(target.compareTo(lt2) < 0, is(true));
	}

	/**
	 * {@link ResourceItemCommonData#equals(Object)}用テストメソッド.
	 */
	@Test
	public void testEquals() {

		ResourceItemCommonData target = new ResourceItemCommonData(new ResourceItemCommonDataId("resourceName0",
				"resourceItemId0"), "targetItemId0");
		target.setAction(SyncAction.CREATE);
		target.setLastModified(0);

		ResourceItemCommonData eq = new ResourceItemCommonData(new ResourceItemCommonDataId(target.getId()
				.getResourceName(), target.getId().getResourceItemId()), target.getTargetItemId());
		eq.setAction(target.getAction());
		eq.setLastModified(target.getLastModified());
		eq.setConflictType(SyncConflictType.UPDATED);
		assertThat(target.equals(eq), is(true));

		eq.setConflictType(target.getConflictType());
		eq.setForUpdate(false);
		assertThat(target.equals(eq), is(true));

		eq.setForUpdate(target.isForUpdate());
		assertThat(target.equals(eq), is(true));

		ResourceItemCommonData ne1 = new ResourceItemCommonData(new ResourceItemCommonDataId("resourceName1", target
				.getId().getResourceItemId()), target.getTargetItemId());
		ne1.setAction(target.getAction());
		ne1.setLastModified(target.getLastModified());
		assertThat(target.equals(ne1), is(false));

		ResourceItemCommonData ne2 = new ResourceItemCommonData(new ResourceItemCommonDataId(target.getId()
				.getResourceName(), "resourceItemId1"), target.getTargetItemId());
		ne2.setAction(target.getAction());
		ne2.setLastModified(target.getLastModified());
		assertThat(target.equals(ne2), is(false));

		ResourceItemCommonData ne3 = new ResourceItemCommonData(new ResourceItemCommonDataId(target.getId()
				.getResourceName(), target.getId().getResourceItemId()), "targetItemId1");
		ne3.setAction(target.getAction());
		ne3.setLastModified(target.getLastModified());
		assertThat(target.equals(ne3), is(false));

		ResourceItemCommonData ne4 = new ResourceItemCommonData(new ResourceItemCommonDataId(target.getId()
				.getResourceName(), target.getId().getResourceItemId()), target.getTargetItemId());
		eq.setAction(SyncAction.UPDATE);
		eq.setLastModified(target.getLastModified());
		assertThat(target.equals(ne4), is(false));

		ResourceItemCommonData ne5 = new ResourceItemCommonData(new ResourceItemCommonDataId(target.getId()
				.getResourceName(), target.getId().getResourceItemId()), target.getTargetItemId());
		eq.setAction(target.getAction());
		eq.setLastModified(1);
		assertThat(target.equals(ne5), is(false));
	}

	/**
	 * {@link ResourceItemCommonData#modify(SyncAction, long)}用テストメソッド.
	 *
	 * @param actionToModify 引数のモック
	 */
	@Test
	public void testModifySyncActionlong(@Mocked final SyncAction actionToModify) {

		// Arrange：正常系
		ResourceItemCommonData target = new ResourceItemCommonData(new ResourceItemCommonDataId("resourceName",
				"resourceItemId"), "targetItemId");
		target.setAction(SyncAction.CREATE);
		target.setLastModified(0);

		SyncAction expectedAction = SyncAction.UPDATE;
		long expectedLastModified = 1;

		// Act
		target.modify(expectedAction, expectedLastModified);

		// Assert：結果が正しいこと
		assertThat(target.getAction(), is(equalTo(expectedAction)));
		assertThat(target.getLastModified(), is(equalTo(expectedLastModified)));
	}
}
