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
 * <H3>UploadResponseのテストクラス.</H3>
 *
 * @author kishigam
 */
public class UploadResponseTest {

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(UploadResponse.class, notNullValue());
	}

	/**
	 * {@link UploadResponse#UploadResponse(UploadCommonData)}用テストメソッド.
	 */
	@SuppressWarnings("serial")
	@Test
	public void testInstantiationUploadCommonData() {
		UploadCommonData uploadCommonData = new UploadCommonData() {
			{
				setStorageId("storageId");
			}
		};
		UploadResponse target = new UploadResponse(uploadCommonData);
		assertThat(target, notNullValue());
		assertThat(target.getUploadCommonData(), is(equalTo(uploadCommonData)));
	}

	/**
	 * {@link UploadResponse#equals(Object)}用テストメソッド.
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

		final UploadResponse target = new UploadResponse(common) {
			{
				setResourceItems(resourceItems);
			}
		};

		UploadResponse eq = new UploadResponse(target.getUploadCommonData()) {
			{
				setResourceItems(target.getResourceItems());
			}
		};

		UploadResponse ne1 = new UploadResponse(anotherCommon) {
			{
				setResourceItems(target.getResourceItems());
			}
		};

		UploadResponse ne2 = new UploadResponse(target.getUploadCommonData()) {
			{
				setResourceItems(anothereResourceItems);
			}
		};

		assertThat(target.equals(eq), is(true));
		assertThat(target.equals(ne1), is(false));
		assertThat(target.equals(ne2), is(false));
	}
}
