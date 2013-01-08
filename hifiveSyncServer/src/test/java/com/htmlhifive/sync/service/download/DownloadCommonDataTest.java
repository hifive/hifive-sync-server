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

import org.junit.Test;

/**
 * <H3>DownloadCommonDataのテストクラス.</H3>
 *
 * @author kishigam
 */
public class DownloadCommonDataTest {

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(DownloadCommonData.class, notNullValue());
	}

	/**
	 * {@link DownloadCommonData#DownloadCommonData()}用テストメソッド.
	 */
	@Test
	public void testInstantiation() {

		String storageId = "storageId";

		DownloadCommonData target = new DownloadCommonData(storageId);

		assertThat(target, notNullValue());
		assertThat(target.getStorageId(), is(equalTo(storageId)));
	}

	/**
	 * {@link DownloadCommonData#equals(Object)}用テストメソッド.
	 */
	@Test
	public void testEquals() {

		// Arrange：正常系
		String storageId = "storageId";

		DownloadCommonData target = new DownloadCommonData(storageId);
		target.setLastDownloadTime(10);
		target.setSyncTime(100);

		DownloadCommonData eq = new DownloadCommonData(target.getStorageId());
		eq.setLastDownloadTime(target.getLastDownloadTime());
		eq.setSyncTime(target.getSyncTime());

		DownloadCommonData ne1 = new DownloadCommonData("other storage");
		ne1.setLastDownloadTime(target.getLastDownloadTime());
		ne1.setSyncTime(target.getSyncTime());

		DownloadCommonData ne2 = new DownloadCommonData(target.getStorageId());
		ne2.setLastDownloadTime(20);
		ne2.setSyncTime(target.getSyncTime());

		DownloadCommonData ne3 = new DownloadCommonData(target.getStorageId());
		ne3.setLastDownloadTime(target.getLastDownloadTime());
		ne3.setSyncTime(200);

		// Act
		// Assert：結果が正しいこと
		assertThat(target.equals(eq), is(true));
		assertThat(target.equals(ne1), is(false));
		assertThat(target.equals(ne2), is(false));
		assertThat(target.equals(ne3), is(false));
	}
}
