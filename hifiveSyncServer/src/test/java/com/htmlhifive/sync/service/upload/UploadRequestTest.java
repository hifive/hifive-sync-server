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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.htmlhifive.sync.resource.ResourceItemWrapper;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.resource.common.ResourceItemCommonDataId;

/**
 * <H3>UploadRequestのテストクラス.</H3>
 *
 * @author kishigam
 */
public class UploadRequestTest {

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(UploadRequest.class, notNullValue());
	}

	/**
	 * {@link UploadRequest#UploadRequest()}用テストメソッド.
	 */
	@Test
	public void testInstantiation() {
		UploadRequest target = new UploadRequest();
		assertThat(target, notNullValue());
	}

	/**
	 * {@link UploadRequest#equals(Object)}用テストメソッド.
	 */
	@SuppressWarnings("serial")
	@Test
	public void testEquals() {

		final UploadCommonData common = new UploadCommonData() {
			{
				setStorageId("storageId");
			}
		};
		final UploadCommonData anotherCommon = new UploadCommonData() {
			{
				setStorageId("another storageId");
			}
		};

		final List<ResourceItemWrapper<? extends Map<String, Object>>> queries = new ArrayList<ResourceItemWrapper<? extends Map<String, Object>>>() {
			{
				ResourceItemCommonData resourceItemCommonData = new ResourceItemCommonData(
						new ResourceItemCommonDataId("resourceName1", "resourceItemId1"), "targetItemId1");

				add(new ResourceItemWrapper<>(resourceItemCommonData, new HashMap<String, Object>()));
			}
		};

		final List<ResourceItemWrapper<? extends Map<String, Object>>> anotherQueries = new ArrayList<ResourceItemWrapper<? extends Map<String, Object>>>() {
			{
				ResourceItemCommonData resourceItemCommonData = new ResourceItemCommonData(
						new ResourceItemCommonDataId("resourceName2", "resourceItemId2"), "targetItemId2");

				add(new ResourceItemWrapper<>(resourceItemCommonData, new HashMap<String, Object>()));
			}
		};

		final UploadRequest target = new UploadRequest() {
			{
				setUploadCommonData(common);
				setResourceItems(queries);
			}
		};

		UploadRequest eq = new UploadRequest() {
			{
				setUploadCommonData(target.getUploadCommonData());
				setResourceItems(queries);
			}
		};

		UploadRequest ne1 = new UploadRequest() {
			{
				setUploadCommonData(anotherCommon);
				setResourceItems(queries);
			}
		};

		UploadRequest ne2 = new UploadRequest() {
			{
				setUploadCommonData(target.getUploadCommonData());
				setResourceItems(anotherQueries);
			}
		};

		assertThat(target.equals(eq), is(true));
		assertThat(target.equals(ne1), is(false));
		assertThat(target.equals(ne2), is(false));
	}
}
