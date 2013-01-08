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
package com.htmlhifive.sync.exception;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import com.htmlhifive.sync.resource.SyncConflictType;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.resource.common.ResourceItemCommonDataId;

/**
 * <H3>ItemUpdatedExceptionのテストクラス.</H3>
 *
 * @author kishigam
 */
public class ItemUpdatedExceptionTest {

	private static ResourceItemCommonData conflictedCommonData;
	private static Object currentItem;

	private static final String message = "message";
	private static final Throwable throwable = new Throwable();
	private static final boolean enableSuppression = false;
	private static final boolean writableStackTrace = false;

	@BeforeClass
	public static void initClass() {
		conflictedCommonData = new ResourceItemCommonData(
				new ResourceItemCommonDataId("resourceName", "resourceItemId"), "targetItemId");
		currentItem = new Object();
	}

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(ItemUpdatedException.class, notNullValue());
	}

	/**
	 * {@link ItemUpdatedException#ItemUpdatedException(ResourceItemCommonData, Object)}用テストメソッド.
	 */
	@Test
	public void testInstantiation1() {
		ItemUpdatedException target = new ItemUpdatedException(conflictedCommonData, currentItem);
		assertThat(target.getCurrentItem(), is(equalTo(currentItem)));
		assertThat(target.getConflictedCommonData().getConflictType(), is(SyncConflictType.UPDATED));
	}

	/**
	 * {@link ItemUpdatedException#ItemUpdatedException(String,Throwable,boolean,boolean,ResourceItemCommonData, Object)}
	 * 用テストメソッド.
	 */
	@Test
	public void testInstantiation2() {
		ItemUpdatedException target = new ItemUpdatedException(message, throwable, enableSuppression,
				writableStackTrace, conflictedCommonData, currentItem);
		assertThat(target.getCurrentItem(), is(equalTo(currentItem)));
		assertThat(target.getConflictedCommonData().getConflictType(), is(SyncConflictType.UPDATED));
	}

	/**
	 * {@link ItemUpdatedException#ItemUpdatedException(String,Throwable,ResourceItemCommonData, Object)} 用テストメソッド.
	 */
	@Test
	public void testInstantiation3() {
		ItemUpdatedException target = new ItemUpdatedException(message, throwable, conflictedCommonData, currentItem);
		assertThat(target.getCurrentItem(), is(equalTo(currentItem)));
		assertThat(target.getConflictedCommonData().getConflictType(), is(SyncConflictType.UPDATED));
	}

	/**
	 * {@link ItemUpdatedException#ItemUpdatedException(String,ResourceItemCommonData, Object)} 用テストメソッド.
	 */
	@Test
	public void testInstantiation4() {
		ItemUpdatedException target = new ItemUpdatedException(message, conflictedCommonData, currentItem);
		assertThat(target.getCurrentItem(), is(equalTo(currentItem)));
		assertThat(target.getConflictedCommonData().getConflictType(), is(SyncConflictType.UPDATED));
	}

	/**
	 * {@link ItemUpdatedException#ItemUpdatedException(Throwable,ResourceItemCommonData, Object)} 用テストメソッド.
	 */
	@Test
	public void testInstantiation5() {
		ItemUpdatedException target = new ItemUpdatedException(throwable, conflictedCommonData, currentItem);
		assertThat(target.getCurrentItem(), is(equalTo(currentItem)));
		assertThat(target.getConflictedCommonData().getConflictType(), is(SyncConflictType.UPDATED));
	}
}
