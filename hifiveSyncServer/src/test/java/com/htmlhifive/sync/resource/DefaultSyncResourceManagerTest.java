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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.htmlhifive.sync.resource.test.TestSyncResourceA;
import com.htmlhifive.sync.resource.test.TestSyncResourceB;
import com.htmlhifive.sync.resource.test.TestSyncResourceC;
import com.htmlhifive.sync.resource.test.TestSyncResourceD;
import com.htmlhifive.sync.resource.update.ClientResolvingStrategy;
import com.htmlhifive.sync.resource.update.ForceUpdateStrategy;
import com.htmlhifive.sync.resource.update.UpdateStrategy;

/**
 * <H3>DefaultSyncResourceManagerのテストクラス.</H3>
 *
 * @author kishigam
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-context-DefaultSyncResourceManager.xml")
public class DefaultSyncResourceManagerTest {

	@Resource
	private DefaultSyncResourceManager target;

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(DefaultSyncResourceManager.class, notNullValue());
	}

	/**
	 * {@link DefaultSyncResourceManager#DefaultSyncResourceManager()}用テストメソッド.
	 */
	@SuppressWarnings("serial")
	@Test
	public void testInstantiation() throws Exception {
		assertThat(target, notNullValue());

		Properties testProps = new Properties() {
			{
				put("testKey", "testVal");
			}
		};

		Map<String, Class<? extends SyncResource<?>>> expectedResourceMap = new HashMap<String, Class<? extends SyncResource<?>>>() {
			{
				put("A", TestSyncResourceA.class);
				put("B", TestSyncResourceB.class);
				put("C", TestSyncResourceC.class);
				put("D", TestSyncResourceD.class);
			}
		};

		Map<String, Class<? extends UpdateStrategy>> expectedUpdateStrategyMap = new HashMap<String, Class<? extends UpdateStrategy>>() {
			{
				put("A", ClientResolvingStrategy.class);
				put("B", ClientResolvingStrategy.class);
				put("C", ForceUpdateStrategy.class);
				put("D", ClientResolvingStrategy.class);
			}
		};

		Field propsField = target.getClass().getDeclaredField("resourceConfigurations");
		propsField.setAccessible(true);
		Object actualPropsObj = propsField.get(target);

		assertThat(actualPropsObj, is(Properties.class));
		assertThat((Properties) actualPropsObj, is(equalTo(testProps)));

		Field resourceMapField = target.getClass().getDeclaredField("resourceMap");
		resourceMapField.setAccessible(true);
		Object actualResourceMapObj = resourceMapField.get(target);

		assertThat(actualResourceMapObj, is(Map.class));
		@SuppressWarnings("unchecked")
		Map<String, Class<? extends SyncResource<?>>> actualResourceMap = (Map<String, Class<? extends SyncResource<?>>>) actualResourceMapObj;
		assertThat(actualResourceMap.size(), is(equalTo(4)));
		assertThat(actualResourceMap, is(equalTo(expectedResourceMap)));

		Field updateStrategyMapField = target.getClass().getDeclaredField("updateStrategyMap");
		updateStrategyMapField.setAccessible(true);
		Object actualUpdateStrategyMapObj = updateStrategyMapField.get(target);

		assertThat(actualUpdateStrategyMapObj, is(Map.class));
		@SuppressWarnings("unchecked")
		Map<String, Class<? extends UpdateStrategy>> actualUpdateStrategyMap = (Map<String, Class<? extends UpdateStrategy>>) actualUpdateStrategyMapObj;
		assertThat(actualUpdateStrategyMap.size(), is(equalTo(4)));
		assertThat(actualUpdateStrategyMap, is(equalTo(expectedUpdateStrategyMap)));
	}

	/**
	 * {@link DefaultSyncResourceManager#allResourcNames()}用テストメソッド.
	 */
	@Test
	public void testAllResourcNames() {

		List<String> actual = target.allResourcNames();

		assertThat(actual.size(), is(equalTo(4)));
		assertThat(actual.contains("A"), is(true));
		assertThat(actual.contains("B"), is(true));
		assertThat(actual.contains("C"), is(true));
		assertThat(actual.contains("D"), is(true));
	}
}
