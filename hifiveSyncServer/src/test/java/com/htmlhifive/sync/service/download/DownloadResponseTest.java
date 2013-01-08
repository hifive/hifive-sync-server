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
package com.htmlhifive.sync.service.download;

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
 * <H3>DownloadResponseのテストクラス.</H3>
 *
 * @author kishigam
 */
public class DownloadResponseTest {

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(DownloadResponse.class, notNullValue());
	}

	/**
	 * {@link DownloadResponse#DownloadResponse(DownloadCommonData)}用テストメソッド.
	 */
	@Test
	public void testInstantiationDownloadCommonData() {
		DownloadCommonData downloadCommonData = new DownloadCommonData("storageId");

		DownloadResponse target = new DownloadResponse(downloadCommonData);

		assertThat(target, notNullValue());
		assertThat(target.getDownloadCommonData(), is(equalTo(downloadCommonData)));
	}

	/**
	 * {@link DownloadResponse#equals(Object)}用テストメソッド.
	 */
	@SuppressWarnings("serial")
	@Test
	public void testEquals() {

		final DownloadCommonData common = new DownloadCommonData("storageId");
		final DownloadCommonData otherCommon = new DownloadCommonData("other storageId");

		final Map<String, List<ResourceItemWrapper<?>>> resourceItems = new HashMap<String, List<ResourceItemWrapper<?>>>() {
			{
				put("resource1", new ArrayList<ResourceItemWrapper<?>>());
			}
		};
		final Map<String, List<ResourceItemWrapper<?>>> othereResourceItems = new HashMap<String, List<ResourceItemWrapper<?>>>() {
			{
				put("resource2", new ArrayList<ResourceItemWrapper<?>>());
			}
		};

		final DownloadResponse target = new DownloadResponse(common) {
			{
				setResourceItems(resourceItems);
			}
		};

		DownloadResponse eq = new DownloadResponse(target.getDownloadCommonData()) {
			{
				setResourceItems(target.getResourceItems());
			}
		};

		DownloadResponse ne1 = new DownloadResponse(otherCommon) {
			{
				setResourceItems(target.getResourceItems());
			}
		};

		DownloadResponse ne2 = new DownloadResponse(target.getDownloadCommonData()) {
			{
				setResourceItems(othereResourceItems);
			}
		};

		assertThat(target.equals(eq), is(true));
		assertThat(target.equals(ne1), is(false));
		assertThat(target.equals(ne2), is(false));
	}
}
