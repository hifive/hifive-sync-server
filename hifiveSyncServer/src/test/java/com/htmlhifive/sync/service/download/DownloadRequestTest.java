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
 * <H3>DownloadRequestのテストクラス.</H3>
 *
 * @author kishigam
 */
public class DownloadRequestTest {

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(DownloadRequest.class, notNullValue());
	}

	/**
	 * {@link DownloadRequest#DownloadRequest()}用テストメソッド.
	 */
	@Test
	public void testInstantiation() {
		DownloadRequest target = new DownloadRequest();
		assertThat(target, notNullValue());
	}

	/**
	 * {@link DownloadRequest#equals(Object)}用テストメソッド.
	 */
	@SuppressWarnings("serial")
	@Test
	public void testEquals() {

		final DownloadCommonData common = new DownloadCommonData("storageId");
		final DownloadCommonData anotherCommon = new DownloadCommonData("another storageId");

		final Map<String, List<ResourceQueryConditions>> queries = new HashMap<String, List<ResourceQueryConditions>>() {
			{
				put("resource1", new ArrayList<ResourceQueryConditions>());
			}
		};

		final Map<String, List<ResourceQueryConditions>> anotherQueries = new HashMap<String, List<ResourceQueryConditions>>() {
			{
				put("resource2", new ArrayList<ResourceQueryConditions>());
			}
		};

		final DownloadRequest target = new DownloadRequest() {
			{
				setDownloadCommonData(common);
				setQueries(queries);
			}
		};

		DownloadRequest eq = new DownloadRequest() {
			{
				setDownloadCommonData(target.getDownloadCommonData());
				setQueries(target.getQueries());
			}
		};

		DownloadRequest ne1 = new DownloadRequest() {
			{
				setDownloadCommonData(anotherCommon);
				setQueries(target.getQueries());
			}
		};

		DownloadRequest ne2 = new DownloadRequest() {
			{
				setDownloadCommonData(target.getDownloadCommonData());
				setQueries(anotherQueries);
			}
		};

		assertThat(target.equals(eq), is(true));
		assertThat(target.equals(ne1), is(false));
		assertThat(target.equals(ne2), is(false));
	}
}