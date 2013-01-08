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
package com.htmlhifive.sync.resource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.resource.common.ResourceItemCommonDataId;

/**
 * <H3>ResourceItemWrapperのテストクラス.</H3>
 *
 * @author kishigam
 */
public class ResourceItemWrapperTest {

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(ResourceItemWrapper.class, notNullValue());
	}

	/**
	 * {@link ResourceItemWrapper#ResourceItemWrapper()}用テストメソッド.
	 */
	@Test
	public void testInstantiation() {
		ResourceItemWrapper<String> target = new ResourceItemWrapper<>(new ResourceItemCommonData(
				new ResourceItemCommonDataId("resourceName", "resourceItemId"), "targetItemId"), "item");

		assertThat(target, notNullValue());
	}

	/**
	 * {@link ResourceItemWrapper#compareTo(ResourceItemWrapper)}用テストメソッド.
	 */
	@Test
	public void testCompareTo() {

		// Arrange：正常系
		ResourceItemCommonDataId id = new ResourceItemCommonDataId("resourceName1", "resourceItemId1");
		ResourceItemCommonData itemCommonData = new ResourceItemCommonData(id, "targetItemId");
		String item = "item";

		ResourceItemWrapper<String> target = new ResourceItemWrapper<>(itemCommonData, item);

		ResourceItemCommonData eq = new ResourceItemCommonData(new ResourceItemCommonDataId("resourceName1",
				"resourceItemId1"), null);
		ResourceItemCommonData lt = new ResourceItemCommonData(new ResourceItemCommonDataId("resourceName0",
				"resourceItemId0"), null);
		ResourceItemCommonData gt = new ResourceItemCommonData(new ResourceItemCommonDataId("resourceName2",
				"resourceItemId2"), null);

		// Assert：結果が正しいこと
		assertThat(target.compareTo(new ResourceItemWrapper<String>(eq, null)) == 0, is(true));
		assertThat(target.compareTo(new ResourceItemWrapper<String>(lt, null)) > 0, is(true));
		assertThat(target.compareTo(new ResourceItemWrapper<String>(gt, null)) < 0, is(true));
	}

	/**
	 * {@link ResourceItemWrapper#equals(Object)}用テストメソッド.
	 */
	@Test
	public void testEquals() {

		// Arrange：正常系
		ResourceItemCommonData itemCommonData1 = new ResourceItemCommonData(new ResourceItemCommonDataId(
				"resourceName", "resourceItemId"), "targetItemId");
		String item1 = "item";

		ResourceItemWrapper<String> target = new ResourceItemWrapper<>(itemCommonData1, item1);

		ResourceItemCommonData itemCommonData2 = new ResourceItemCommonData(
				new ResourceItemCommonDataId("xxxx", "xxxx"), "xxxx");
		String item2 = "xxxx";

		ResourceItemWrapper<String> eq = new ResourceItemWrapper<>(itemCommonData1, item1);
		ResourceItemWrapper<String> ne1 = new ResourceItemWrapper<>(itemCommonData2, item1);
		ResourceItemWrapper<String> ne2 = new ResourceItemWrapper<>(itemCommonData1, item2);
		ResourceItemWrapper<String> ne3 = new ResourceItemWrapper<>(itemCommonData2, item2);

		// Assert：結果が正しいこと
		assertThat(target.equals(eq), is(true));
		assertThat(target.equals(ne1), is(false));
		assertThat(target.equals(ne2), is(false));
		assertThat(target.equals(ne3), is(false));
	}
}
