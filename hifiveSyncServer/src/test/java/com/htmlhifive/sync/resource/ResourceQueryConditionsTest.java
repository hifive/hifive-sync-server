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
package com.htmlhifive.sync.resource;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * <H3>ResourceQueryConditionsのテストクラス.</H3>
 *
 * @author kishigam
 */
public class ResourceQueryConditionsTest {

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(ResourceQueryConditions.class, notNullValue());
	}

	/**
	 * {@link ResourceQueryConditions#ResourceQueryConditions()}用テストメソッド.
	 */
	@Test
	public void testInstantiation() {
		ResourceQueryConditions target = new ResourceQueryConditions();
		assertThat(target, notNullValue());
	}

	/**
	 * {@link ResourceQueryConditions#equals(Object)}用テストメソッド.
	 */
	@SuppressWarnings("serial")
	@Test
	public void testEquals() {

		final Map<String, String[]> conditions = new HashMap<String, String[]>() {
			{
				put("id", new String[] { "1", "2" });
			}
		};
		final long lastDownloadTime = 0L;

		ResourceQueryConditions target = new ResourceQueryConditions() {
			{
				setConditions(conditions);
				setLastDownloadTime(lastDownloadTime);
			}
		};

		final Map<String, String[]> conditions2 = new HashMap<String, String[]>() {
			{
				put("id", new String[] { "3" });
			}
		};
		final long lastDownloadTime2 = 100L;

		ResourceQueryConditions eq = new ResourceQueryConditions() {
			{
				setConditions(conditions);
				setLastDownloadTime(lastDownloadTime);
			}
		};
		ResourceQueryConditions ne1 = new ResourceQueryConditions() {
			{
				setConditions(conditions2);
				setLastDownloadTime(lastDownloadTime);
			}
		};
		ResourceQueryConditions ne2 = new ResourceQueryConditions() {
			{
				setConditions(conditions);
				setLastDownloadTime(lastDownloadTime2);
			}
		};
		ResourceQueryConditions ne3 = new ResourceQueryConditions() {
			{
				setConditions(conditions2);
				setLastDownloadTime(lastDownloadTime2);
			}
		};

		assertThat(target, is(equalTo(eq)));
		assertThat(target, is(not(equalTo(ne1))));
		assertThat(target, is(not(equalTo(ne2))));
		assertThat(target, is(not(equalTo(ne3))));
	}
}
