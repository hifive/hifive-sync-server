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

/**
 * <H3>DuplicateIdExceptionのテストクラス.</H3>
 *
 * @author kishigam
 */
public class DuplicateIdExceptionTest {

	private static String duplicatedTargetItemId;
	private static Object currentItem;

	private static final String message = "message";
	private static final Throwable throwable = new Throwable();
	private static final boolean enableSuppression = false;
	private static final boolean writableStackTrace = false;

	@BeforeClass
	public static void initClass() {
		duplicatedTargetItemId = "targetItemId";
		currentItem = new Object();
	}

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(DuplicateIdException.class, notNullValue());
	}

	/**
	 * {@link DuplicateIdException#DuplicateIdException(String, Object)}用テストメソッド.
	 */
	@Test
	public void testInstantiation1() {
		DuplicateIdException target = new DuplicateIdException(duplicatedTargetItemId, currentItem);
		assertThat(target.getCurrentItem(), is(equalTo(currentItem)));
		assertThat(target.getDuplicatedTargetItemId(), is(equalTo(duplicatedTargetItemId)));
	}

	/**
	 * {@link DuplicateIdException#DuplicateIdException(String,Throwable,boolean,boolean,String, Object)} 用テストメソッド.
	 */
	@Test
	public void testInstantiation2() {
		DuplicateIdException target = new DuplicateIdException(message, throwable, enableSuppression,
				writableStackTrace, duplicatedTargetItemId, currentItem);
		assertThat(target.getCurrentItem(), is(equalTo(currentItem)));
		assertThat(target.getDuplicatedTargetItemId(), is(equalTo(duplicatedTargetItemId)));
	}

	/**
	 * {@link DuplicateIdException#DuplicateIdException(String,Throwable,String, Object)} 用テストメソッド.
	 */
	@Test
	public void testInstantiation3() {
		DuplicateIdException target = new DuplicateIdException(message, throwable, duplicatedTargetItemId, currentItem);
		assertThat(target.getCurrentItem(), is(equalTo(currentItem)));
		assertThat(target.getDuplicatedTargetItemId(), is(equalTo(duplicatedTargetItemId)));
	}

	/**
	 * {@link DuplicateIdException#DuplicateIdException(String,String, Object)} 用テストメソッド.
	 */
	@Test
	public void testInstantiation4() {
		DuplicateIdException target = new DuplicateIdException(message, duplicatedTargetItemId, currentItem);
		assertThat(target.getCurrentItem(), is(equalTo(currentItem)));
		assertThat(target.getDuplicatedTargetItemId(), is(equalTo(duplicatedTargetItemId)));
	}

	/**
	 * {@link DuplicateIdException#DuplicateIdException(Throwable,String, Object)} 用テストメソッド.
	 */
	@Test
	public void testInstantiation5() {
		DuplicateIdException target = new DuplicateIdException(throwable, duplicatedTargetItemId, currentItem);
		assertThat(target.getCurrentItem(), is(equalTo(currentItem)));
		assertThat(target.getDuplicatedTargetItemId(), is(equalTo(duplicatedTargetItemId)));
	}
}
