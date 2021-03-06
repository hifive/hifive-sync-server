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
package com.htmlhifive.sync.resource.common;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * <H3>ResourceItemCommonDataIdのテストクラス.</H3>
 *
 * @author kishigam
 */
public class ResourceItemCommonDataIdTest {

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(ResourceItemCommonDataId.class, notNullValue());
	}

	/**
	 * {@link ResourceItemCommonDataId#ResourceItemCommonDataId()}用テストメソッド.
	 */
	@Test
	public void testInstantiation() {

		String resourceName = "resourceName";
		String resourceItemId = "resourceItemId";

		ResourceItemCommonDataId target = new ResourceItemCommonDataId(resourceName, resourceItemId);
		assertThat(target, notNullValue());
		assertThat(target.getResourceName(), is(equalTo(resourceName)));
		assertThat(target.getResourceItemId(), is(equalTo(resourceItemId)));
	}

	/**
	 * {@link ResourceItemCommonDataId#compareTo(ResourceItemCommonDataId)}用テストメソッド.
	 */
	@Test
	public void testCompareToResourceItemCommonDataId() {

		// Arrange：正常系
		ResourceItemCommonDataId target = new ResourceItemCommonDataId("b", "2");

		ResourceItemCommonDataId gt1 = new ResourceItemCommonDataId("a", "2");
		ResourceItemCommonDataId gt2 = new ResourceItemCommonDataId("b", "1");
		ResourceItemCommonDataId eq = new ResourceItemCommonDataId("b", "2");
		ResourceItemCommonDataId lt1 = new ResourceItemCommonDataId("b", "3");
		ResourceItemCommonDataId lt2 = new ResourceItemCommonDataId("c", "1");

		assertThat(target.compareTo(gt1) > 0, is(true));
		assertThat(target.compareTo(gt2) > 0, is(true));
		assertThat(target.compareTo(eq) == 0, is(true));
		assertThat(target.compareTo(lt1) < 0, is(true));
		assertThat(target.compareTo(lt2) < 0, is(true));
	}

	/**
	 * {@link ResourceItemCommonDataId#equals(Object)}用テストメソッド.
	 */
	@Test
	public void testEquals() {

		ResourceItemCommonDataId target = new ResourceItemCommonDataId("resourceName0", "resourceItemId0");

		ResourceItemCommonDataId eq = new ResourceItemCommonDataId(target.getResourceName(), target.getResourceItemId());
		assertThat(target.equals(eq), is(true));

		ResourceItemCommonDataId ne1 = new ResourceItemCommonDataId("resourceName1", target.getResourceItemId());
		assertThat(target.equals(ne1), is(false));

		ResourceItemCommonDataId ne2 = new ResourceItemCommonDataId(target.getResourceName(), "resourceItemId1");
		assertThat(target.equals(ne2), is(false));
	}
}
