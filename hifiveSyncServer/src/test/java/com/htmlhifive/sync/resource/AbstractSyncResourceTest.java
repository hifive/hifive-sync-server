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
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import mockit.Delegate;
import mockit.Expectations;
import mockit.FullVerificationsInOrder;
import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.junit.Test;

import com.htmlhifive.sync.exception.DuplicateIdException;
import com.htmlhifive.sync.exception.ItemUpdatedException;
import com.htmlhifive.sync.exception.SyncException;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.resource.common.ResourceItemCommonDataId;
import com.htmlhifive.sync.resource.common.ResourceItemCommonDataService;
import com.htmlhifive.sync.resource.update.UpdateStrategy;
import com.htmlhifive.sync.service.SyncCommonData;
import com.htmlhifive.sync.service.upload.UploadCommonData;

/**
 * <H3>AbstractSyncResourceのテストクラス.</H3>
 *
 * @author kishigam
 */
@SuppressWarnings("serial")
public class AbstractSyncResourceTest {

	private static final String resourceName = "test";

	/**
	 * abstractメソッドの挙動を受け持つMockオブジェクト.
	 */
	@Mocked
	MockTargetImpl mockObj;

	/**
	 * 競合発生時の競合解決を行う更新戦略オブジェクト(Mock).
	 */
	@Mocked
	private UpdateStrategy updateStrategy;

	/**
	 * このリソースが受け付けるアイテム型への変換を行うコンバータ(Mock).
	 */
	@Mocked
	private ResourceItemConverter<Object> defaultItemConverter;

	/**
	 * リソースアイテム共通データを管理するサービス(Mock).
	 */
	@Mocked
	private ResourceItemCommonDataService commonDataService;

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(AbstractSyncResource.class, notNullValue());
	}

	/**
	 * {@link AbstractSyncResource#AbstractSyncResource()}用テストメソッド.
	 */
	@Test
	public void testInstantiation() {

		AbstractSyncResource<Object> target = new TestTargetImpl();
		assertThat(target, notNullValue());
	}

	/**
	 * {@link AbstractSyncResource#get(SyncCommonData, List)}用テストメソッド.
	 */
	@Test
	public void testGet() {

		final TestTargetImpl target = new TestTargetImpl(1);

		final String targetItemIdA = "a";
		final String targetItemIdB = "b";

		// for update状態でない共通データIDのリソース名はgetメソッド内で解決される
		final ResourceItemCommonDataId inputIdB = new ResourceItemCommonDataId(null, "B");

		final ResourceItemCommonDataId idA = new ResourceItemCommonDataId(resourceName, "A");
		final ResourceItemCommonDataId idB = new ResourceItemCommonDataId(resourceName, "B");

		final ResourceItemCommonData commonA = new ResourceItemCommonData(idA, targetItemIdA) {
			{
				setForUpdate(true);
				setAction(SyncAction.UPDATE);
			}
		};

		final ResourceItemCommonData commonB = new ResourceItemCommonData(idB, targetItemIdB) {
			{
				setForUpdate(false);
				setAction(SyncAction.DELETE);
			}
		};

		final ResourceItemCommonData inputCommonB = new ResourceItemCommonData(inputIdB, targetItemIdB);

		final Object objA = new Object();
		final Object objB = new Object();

		final SyncCommonData syncCommon = createSyncCommon("storageId", 100);

		final List<ResourceItemCommonData> inputCommonDataList = new ArrayList<ResourceItemCommonData>() {
			{
				add(commonA);
				add(inputCommonB);
			}
		};

		final Map<String, Object> itemMap = new HashMap<String, Object>() {
			{
				put(targetItemIdA, objA);
				put(targetItemIdB, objB);
			}
		};

		new Expectations() {
			{
				setField(target, "mock", mockObj);
				setField(target, updateStrategy);
				setField(target, defaultItemConverter);
				setField(target, commonDataService);

				// forUpdate==falseの場合だけ実行される
				commonDataService.currentCommonData(idB);
				result = commonB;

				mockObj.doGet(new String[] { targetItemIdA, targetItemIdB });
				result = itemMap;

				// verify
				commonDataService.currentCommonData(idA);
				result = commonA;

				// verify
				commonDataService.currentCommonData(idB);
				result = commonB;
			}
		};

		// Act
		List<? extends ResourceItemWrapper<?>> actual = target.get(syncCommon, inputCommonDataList);

		// Assert：結果が正しいこと
		List<? extends ResourceItemWrapper<?>> expected = new ArrayList<ResourceItemWrapper<?>>() {
			{
				add(new ResourceItemWrapper<>(commonA, objA));
				add(new ResourceItemWrapper<>(commonB, objB));
			}
		};
		assertThat(actual.size(), is(equalTo(2)));
		assertThat(actual.contains(expected.get(0)), is(true));
		assertThat(actual.contains(expected.get(1)), is(true));
	}

	/**
	 * {@link AbstractSyncResource#get(SyncCommonData, List)}用テストメソッド.<br>
	 * verifyで2度の再取得が行われる.
	 */
	@Test
	public void testGetWithVerification() {

		// 3度まで共通データとアイテムデータの整合性検証を行う
		final TestTargetImpl target = new TestTargetImpl(3);

		final String targetItemIdA = "a";
		final String targetItemIdB = "b";

		final ResourceItemCommonDataId idA = new ResourceItemCommonDataId(resourceName, "A");
		final ResourceItemCommonDataId idB = new ResourceItemCommonDataId(resourceName, "B");

		final ResourceItemCommonData commonA = new ResourceItemCommonData(idA, targetItemIdA) {
			{
				setForUpdate(true);
				setAction(SyncAction.CREATE);
			}
		};

		final ResourceItemCommonData updatedCommonA1 = new ResourceItemCommonData(idA, targetItemIdA) {
			{
				setForUpdate(true);
				setAction(SyncAction.UPDATE);
				setLastModified(100);
			}
		};

		final ResourceItemCommonData updatedCommonA2 = new ResourceItemCommonData(idA, targetItemIdA) {
			{
				setForUpdate(true);
				setAction(SyncAction.UPDATE);
				setLastModified(200);
			}
		};

		final ResourceItemCommonData commonB = new ResourceItemCommonData(idB, targetItemIdB) {
			{
				setForUpdate(true);
				setAction(SyncAction.CREATE);
			}
		};

		final Object objA = new Object();
		final Object objB = new Object();

		final SyncCommonData syncCommon = createSyncCommon("storageId", 100);

		final List<ResourceItemCommonData> commonDataList = new ArrayList<ResourceItemCommonData>() {
			{
				add(commonA);
				add(commonB);
			}
		};

		final Map<String, Object> itemMap = new HashMap<String, Object>() {
			{
				put(targetItemIdA, objA);
				put(targetItemIdB, objB);
			}
		};

		new NonStrictExpectations() {
			{
				// 引数や戻り値(期待動作)のテスト

				setField(target, "mock", mockObj);
				setField(target, updateStrategy);
				setField(target, defaultItemConverter);
				setField(target, commonDataService);

				// どちらの順序の配列で呼び出しても同じ結果を返すようにする
				mockObj.doGet(new String[] { targetItemIdA, targetItemIdB });
				returns(itemMap, itemMap, itemMap);
				mockObj.doGet(new String[] { targetItemIdB, targetItemIdA });
				returns(itemMap, itemMap, itemMap);

				commonDataService.currentCommonData(idA);
				returns(updatedCommonA1, updatedCommonA2, updatedCommonA2);

				commonDataService.currentCommonData(idB);
				returns(commonB, commonB, commonB);
			}
		};

		// Act
		List<? extends ResourceItemWrapper<?>> actual = target.get(syncCommon, commonDataList);

		new FullVerificationsInOrder() {
			{
				// 呼び出し順序のテスト
				mockObj.doGet(new String[] { (String) any, (String) any });
				commonDataService.currentCommonData((ResourceItemCommonDataId) any);
				commonDataService.currentCommonData((ResourceItemCommonDataId) any);
				mockObj.doGet(new String[] { (String) any, (String) any });
				commonDataService.currentCommonData((ResourceItemCommonDataId) any);
				commonDataService.currentCommonData((ResourceItemCommonDataId) any);
				mockObj.doGet(new String[] { (String) any, (String) any });
				commonDataService.currentCommonData((ResourceItemCommonDataId) any);
				commonDataService.currentCommonData((ResourceItemCommonDataId) any);
			}
		};

		// Assert：結果が正しいこと
		List<? extends ResourceItemWrapper<?>> expected = new ArrayList<ResourceItemWrapper<?>>() {
			{
				add(new ResourceItemWrapper<>(updatedCommonA2, objA));
				add(new ResourceItemWrapper<>(commonB, objB));
			}
		};

		assertThat(actual.size(), is(equalTo(2)));
		assertThat(actual.contains(expected.get(0)), is(true));
		assertThat(actual.contains(expected.get(1)), is(true));
	}

	/**
	 * {@link AbstractSyncResource#get(SyncCommonData, List)}用テストメソッド.<br>
	 * 同期共通データがnullのとき{@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testGetFailBecauseOfNullSyncCommonData() {

		final TestTargetImpl target = new TestTargetImpl();

		// Act
		target.get(null, new ArrayList<ResourceItemCommonData>());

		fail();
	}

	/**
	 * {@link AbstractSyncResource#get(SyncCommonData, List)}用テストメソッド.<br>
	 * 共通データリストがnullのとき{@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testGetFailBecauseOfNullCommonDataList() {

		final TestTargetImpl target = new TestTargetImpl();

		// Act
		SyncCommonData syncCommon = createSyncCommon("", 0);

		target.get(syncCommon, null);

		fail();
	}

	/**
	 * {@link AbstractSyncResource#getByQuery(SyncCommonData, ResourceQueryConditions)}用テストメソッド.
	 */
	@Test
	public void testGetByQuery() {

		// Arrange：正常系
		final AbstractSyncResource<?> target = new TestTargetImpl(1);

		final long lastDownloadTime = 10;

		final Map<String, String[]> conditions = new HashMap<String, String[]>() {
			{
				put("id", new String[] { "a", "b" });
				put("no", new String[] { "1", "2" });
			}
		};

		final ResourceQueryConditions query = new ResourceQueryConditions() {
			{
				setLastDownloadTime(lastDownloadTime);
				setConditions(conditions);
			}
		};

		final String targetItemIdA = "a";
		final String targetItemIdB = "b";

		final ResourceItemCommonDataId idA = new ResourceItemCommonDataId(resourceName, "A");
		final ResourceItemCommonDataId idB = new ResourceItemCommonDataId(resourceName, "B");

		final ResourceItemCommonData commonA = new ResourceItemCommonData(idA, targetItemIdA) {
			{
				setForUpdate(true);
			}
		};

		final ResourceItemCommonData commonB = new ResourceItemCommonData(idB, targetItemIdB) {
			{
				setForUpdate(false);
			}
		};

		final String[] targetIds = { targetItemIdA, targetItemIdB };

		final Object objA = new Object();
		final Object objB = new Object();

		final SyncCommonData syncCommon = createSyncCommon("storageId", 100);

		final Map<String, Object> itemMap = new HashMap<String, Object>() {
			{
				put(targetItemIdA, objA);
				put(targetItemIdB, objB);
			}
		};

		new Expectations() {
			{
				setField(target, "mock", mockObj);
				setField(target, updateStrategy);
				setField(target, defaultItemConverter);
				setField(target, commonDataService);

				// ResourceItemCommonData#isForUpdateによる取得判断は行わない

				commonDataService.modifiedCommonData(resourceName, lastDownloadTime);
				result = new ArrayList<ResourceItemCommonData>() {
					{
						add(commonA);
						add(commonB);
					}
				};

				mockObj.doGetByQuery(conditions, targetIds);
				result = itemMap;

				commonDataService.currentCommonData(idA);
				result = commonA;

				commonDataService.currentCommonData(idB);
				result = commonB;
			}
		};

		// Act
		List<? extends ResourceItemWrapper<?>> actual = target.getByQuery(syncCommon, query);

		// Assert：結果が正しいこと
		List<? extends ResourceItemWrapper<?>> expected = new ArrayList<ResourceItemWrapper<?>>() {
			{
				add(new ResourceItemWrapper<>(commonA, objA));
				add(new ResourceItemWrapper<>(commonB, objB));
			}
		};

		assertThat(actual.size(), is(equalTo(2)));
		assertThat(actual.contains(expected.get(0)), is(true));
		assertThat(actual.contains(expected.get(1)), is(true));
	}

	/**
	 * {@link AbstractSyncResource#getByQuery(SyncCommonData, ResourceQueryConditions)}用テストメソッド.<br>
	 * 同期共通データがnullのとき{@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testGetByQueryFailBecauseOfNullSyncCommonData() {

		final TestTargetImpl target = new TestTargetImpl();

		// Act
		target.getByQuery(null, new ResourceQueryConditions());

		fail();
	}

	/**
	 * {@link AbstractSyncResource#getByQuery(SyncCommonData, ResourceQueryConditions)}用テストメソッド.<br>
	 * リソースクエリがnullのとき{@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testGetByQueryFailBecauseOfNullCommonDataList() {

		final TestTargetImpl target = new TestTargetImpl();

		// Act
		SyncCommonData syncCommon = createSyncCommon("", 0);

		target.getByQuery(syncCommon, null);

		fail();
	}

	/**
	 * {@link AbstractSyncResource#create(UploadCommonData, ResourceItemCommonData, Object)}用テストメソッド.
	 */
	@Test
	public void testCreate() throws Exception {

		// Arrange：正常系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		final long syncTime = 10;
		final UploadCommonData uploadCommon = createUploadCommon(syncTime);

		final String targetItemId = "newItem";

		final ResourceItemCommonDataId id = new ResourceItemCommonDataId(resourceName, "new");
		final ResourceItemCommonData requestItemCommon = new ResourceItemCommonData(id, null) {
			{
				setAction(SyncAction.CREATE);
			}
		};
		final Object newItem = new Object();

		final ResourceItemCommonData itemCommonAfterCreate = new ResourceItemCommonData(id, targetItemId) {
			{
				setAction(SyncAction.CREATE);
				setLastModified(syncTime);
			}
		};

		new Expectations() {
			{
				setField(target, "mock", mockObj);
				setField(target, updateStrategy);
				setField(target, defaultItemConverter);
				setField(target, commonDataService);

				mockObj.doCreate(newItem);
				result = targetItemId;

				commonDataService.currentCommonData(id);
				result = null;

				commonDataService.saveNewCommonData(itemCommonAfterCreate);
				result = itemCommonAfterCreate;
			}
		};

		// Act
		ResourceItemWrapper<Object> actual = target.create(uploadCommon, requestItemCommon, newItem);

		// Assert：結果が正しいこと
		ResourceItemWrapper<Object> expected = new ResourceItemWrapper<>(itemCommonAfterCreate, newItem);

		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * {@link AbstractSyncResource#create(UploadCommonData, ResourceItemCommonData, Object)}用テストメソッド.<br>
	 * キー重複が発生した場合、例外オブジェクトから情報を取得して結果を返す.
	 */
	@Test
	public void testCreateIfThrowsDuplicateIdException() throws Exception {

		// Arrange：正常系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		final long syncTime = 10;
		final UploadCommonData uploadCommon = createUploadCommon(syncTime);

		final String targetItemId = "duplicated";

		final ResourceItemCommonDataId id = new ResourceItemCommonDataId(resourceName, "new");
		final ResourceItemCommonData itemCommon = new ResourceItemCommonData(id, targetItemId) {
			{
				setAction(SyncAction.CREATE);
				setLastModified(0);
			}
		};
		final Object newItem = new Object();

		final ResourceItemCommonData duplicatedItemCommon = new ResourceItemCommonData(id, targetItemId) {
			{
				setAction(SyncAction.CREATE);
				setLastModified(5);
			}
		};
		final Object duplicatedItem = new Object();

		new Expectations() {
			{
				setField(target, "mock", mockObj);
				setField(target, updateStrategy);
				setField(target, defaultItemConverter);
				setField(target, commonDataService);

				mockObj.doCreate(newItem);
				result = new DuplicateIdException(targetItemId, duplicatedItem);

				commonDataService.currentCommonData(resourceName, targetItemId);
				result = duplicatedItemCommon;
			}
		};

		// Act
		ResourceItemWrapper<Object> actual = target.create(uploadCommon, itemCommon, newItem);

		// Assert：結果が正しいこと
		ResourceItemWrapper<Object> expected = new ResourceItemWrapper<>(duplicatedItemCommon, duplicatedItem);
		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * {@link AbstractSyncResource#create(UploadCommonData, ResourceItemCommonData, Object)}用テストメソッド.<br>
	 * 新規アイテムに対する共通データが既に存在する時は{@link SyncException}がスローされる.
	 */
	@Test(expected = SyncException.class)
	public void testCreateFailBecauseOfInconsistentCommonData() throws Exception {

		// Arrange：異常系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		final long syncTime = 10;
		final UploadCommonData uploadCommon = createUploadCommon(syncTime);

		final String targetItemId = "newItem";

		final ResourceItemCommonDataId id = new ResourceItemCommonDataId(resourceName, "new");
		final ResourceItemCommonData itemCommon = new ResourceItemCommonData(id, null) {
			{
				setAction(SyncAction.CREATE);
			}
		};
		final Object newItem = new Object();

		final ResourceItemCommonData itemCommonExisted = new ResourceItemCommonData(id, targetItemId) {
			{
				setAction(SyncAction.CREATE);
				setLastModified(5);
			}
		};

		new Expectations() {
			{
				setField(target, "mock", mockObj);
				setField(target, updateStrategy);
				setField(target, defaultItemConverter);
				setField(target, commonDataService);

				mockObj.doCreate(newItem);
				result = targetItemId;

				commonDataService.currentCommonData(id);
				result = itemCommonExisted;
			}
		};

		// Act
		target.create(uploadCommon, itemCommon, newItem);

		fail();
	}

	/**
	 * {@link AbstractSyncResource#create(UploadCommonData, ResourceItemCommonData, Object)}用テストメソッド.<br>
	 * 上り更新共通データがnullの時{@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testCreateFailBecauseOfNullUploadCommon() throws Exception {

		// Arrange：異常系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		// Act
		target.create(null, new ResourceItemCommonData(new ResourceItemCommonDataId(resourceName, ""), ""),
				new Object());

		fail();
	}

	/**
	 * {@link AbstractSyncResource#create(UploadCommonData, ResourceItemCommonData, Object)}用テストメソッド.<br>
	 * リソースアイテム共通データがnullの時{@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testCreateFailBecauseOfNullItemCommon() throws Exception {

		// Arrange：異常系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		// Act
		target.create(new UploadCommonData(), null, new Object());

		fail();
	}

	/**
	 * {@link AbstractSyncResource#create(UploadCommonData, ResourceItemCommonData, Object)}用テストメソッド.<br>
	 * アイテムデータがnullの時{@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testCreateFailBecauseOfNullItem() throws Exception {

		// Arrange：異常系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		// Act
		target.create(new UploadCommonData(), new ResourceItemCommonData(
				new ResourceItemCommonDataId(resourceName, ""), ""), null);

		fail();
	}

	/**
	 * {@link AbstractSyncResource#update(UploadCommonData, ResourceItemCommonData, Object)}用テストメソッド.
	 */
	@Test
	public void testUpdate() throws Exception {

		// Arrange：正常系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		final long syncTime = 50;
		final UploadCommonData uploadCommon = createUploadCommon(syncTime);

		final String targetItemId = "item";

		final ResourceItemCommonDataId id = new ResourceItemCommonDataId(resourceName, "update");
		final ResourceItemCommonData itemCommon = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(false);
				setAction(SyncAction.UPDATE);
				setLastModified(10);
			}
		};
		final Object item = new Object();

		final ResourceItemCommonData itemCommonForUpdate = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(true);
				setAction(SyncAction.CREATE);
				setLastModified(10);
			}
		};

		final ResourceItemCommonData itemCommonAfterUpdate = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(true);
				setAction(SyncAction.UPDATE);
				setLastModified(50);
			}
		};

		new Expectations() {
			{
				setField(target, "mock", mockObj);
				setField(target, updateStrategy);
				setField(target, defaultItemConverter);
				setField(target, commonDataService);

				commonDataService.currentCommonDataForUpdate(id);
				result = itemCommonForUpdate;

				mockObj.doUpdate(item);
				result = targetItemId;

				commonDataService.saveUpdatedCommonData(itemCommonAfterUpdate);
				result = itemCommonAfterUpdate;
			}
		};

		// Act
		ResourceItemWrapper<Object> actual = target.update(uploadCommon, itemCommon, item);

		// Assert：結果が正しいこと
		ResourceItemWrapper<Object> expected = new ResourceItemWrapper<>(itemCommonAfterUpdate, item);
		assertThat(actual, is(expected));
	}

	/**
	 * {@link AbstractSyncResource#update(UploadCommonData, ResourceItemCommonData, Object)}用テストメソッド.<br>
	 * 1リクエストに同一リソースアイテムの更新が複数含まれる場合、サーバ側共通データの最終更新時刻が更新されていても、そのリクエストでの更新であれば競合なしと判定する.
	 */
	@Test
	public void testUpdateInOneRequest() throws Exception {

		// Arrange：正常系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		final long syncTime = 50;
		final UploadCommonData uploadCommon = createUploadCommon(syncTime);

		final String targetItemId = "item";

		final ResourceItemCommonDataId id = new ResourceItemCommonDataId(resourceName, "update");
		final ResourceItemCommonData itemCommon = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(false);
				setAction(SyncAction.UPDATE);
				setLastModified(10);
			}
		};

		// 同一上り更新リクエストの前の更新が行われている
		final ResourceItemCommonData itemCommonForUpdate = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(true);
				setAction(SyncAction.UPDATE);
				setLastModified(syncTime);
			}
		};

		final ResourceItemCommonData itemCommonAfterUpdate = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(true);
				setAction(SyncAction.UPDATE);
				setLastModified(50);
			}
		};

		final Object item = new Object();

		new Expectations() {
			{
				setField(target, "mock", mockObj);
				setField(target, updateStrategy);
				setField(target, defaultItemConverter);
				setField(target, commonDataService);

				commonDataService.currentCommonDataForUpdate(id);
				result = itemCommonForUpdate;

				mockObj.doUpdate(item);
				result = targetItemId;

				commonDataService.saveUpdatedCommonData(itemCommonAfterUpdate);
				result = itemCommonAfterUpdate;
			}
		};

		// Act
		ResourceItemWrapper<Object> actual = target.update(uploadCommon, itemCommon, item);

		// Assert：結果が正しいこと
		ResourceItemWrapper<Object> expected = new ResourceItemWrapper<>(itemCommonAfterUpdate, item);
		assertThat(actual, is(expected));
	}

	/**
	 * {@link AbstractSyncResource#update(UploadCommonData, ResourceItemCommonData, Object)}用テストメソッド.<br>
	 * 競合が発生した場合でも競合解決戦略に沿って更新後データが確定した場合は更新が実行される.
	 */
	@Test
	public void testUpdateWhenConflictOccuredAndResolved() throws Exception {

		// Arrange：例外系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		final long syncTime = 50;
		final UploadCommonData uploadCommon = createUploadCommon(syncTime);

		final String targetItemId = "item";

		final ResourceItemCommonDataId id = new ResourceItemCommonDataId(resourceName, "update");
		final ResourceItemCommonData itemCommon = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(false);
				setAction(SyncAction.UPDATE);
				setLastModified(10);
			}
		};
		final Object item = new Object();

		final ResourceItemCommonData itemCommonForUpdate = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(true);
				setAction(SyncAction.DELETE);
				setLastModified(20);
			}
		};

		// サーバデータが採用されるケース
		final ResourceItemCommonData itemCommonAfterUpdate = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(true);
				setAction(SyncAction.DELETE);
				setLastModified(50);
			}
		};
		// 論理削除の結果として得られるデータ
		final Object deletedItem = new Object();

		new Expectations() {
			{
				setField(target, "mock", mockObj);
				setField(target, updateStrategy);
				setField(target, defaultItemConverter);
				setField(target, commonDataService);

				commonDataService.currentCommonDataForUpdate(id);
				result = itemCommonForUpdate;

				updateStrategy.resolveConflict(itemCommon, item, itemCommonForUpdate, null);
				result = null;

				// 削除処理実行のためのdoDelete
				mockObj.doDelete(targetItemId);
				result = deletedItem;

				commonDataService.saveUpdatedCommonData(itemCommonAfterUpdate);
				result = itemCommonAfterUpdate;
			}
		};

		// Act
		ResourceItemWrapper<Object> actual = target.update(uploadCommon, itemCommon, item);

		// Assert：結果が正しいこと
		ResourceItemWrapper<Object> expected = new ResourceItemWrapper<>(itemCommonAfterUpdate, deletedItem);
		assertThat(actual, is(expected));
	}

	/**
	 * {@link AbstractSyncResource#update(UploadCommonData, ResourceItemCommonData, Object)}用テストメソッド.<br>
	 * 競合が発生し、解決できなかった場合には例外処理が行われる.
	 */
	@Test
	public void testUpdateWhenConflictOccuredAndThrowsException() throws Exception {

		// Arrange：例外系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		final long syncTime = 50;
		final UploadCommonData uploadCommon = createUploadCommon(syncTime);

		final String targetItemId = "item";

		final ResourceItemCommonDataId id = new ResourceItemCommonDataId(resourceName, "update");
		final ResourceItemCommonData itemCommon = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(false);
				setAction(SyncAction.UPDATE);
				setLastModified(10);
			}
		};
		final Object item = new Object();

		final ResourceItemCommonData itemCommonForUpdate = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(true);
				setAction(SyncAction.UPDATE);
				setLastModified(20);
			}
		};
		final Object updatedItem = new Object();

		new Expectations() {
			{
				setField(target, "mock", mockObj);
				setField(target, updateStrategy);
				setField(target, defaultItemConverter);
				setField(target, commonDataService);

				commonDataService.currentCommonDataForUpdate(id);
				result = itemCommonForUpdate;

				mockObj.doGet(targetItemId);
				result = new HashMap<String, Object>() {
					{
						put(targetItemId, updatedItem);
					}
				};

				updateStrategy.resolveConflict(itemCommon, item, itemCommonForUpdate, updatedItem);
				result = new ItemUpdatedException(itemCommonForUpdate, updatedItem);
			}
		};

		// Act
		ResourceItemWrapper<Object> actual = target.update(uploadCommon, itemCommon, item);

		// Assert：結果が正しいこと
		ResourceItemWrapper<Object> expected = new ResourceItemWrapper<>(itemCommonForUpdate, updatedItem);
		assertThat(actual, is(expected));
	}

	/**
	 * {@link AbstractSyncResource#update(UploadCommonData, ResourceItemCommonData, Object)}用テストメソッド.<br>
	 * 上り更新共通データがnullの時 {@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testUpdateFailBecauseOfNullUploadCommon() throws Exception {

		// Arrange：異常系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		// Act
		target.update(null, new ResourceItemCommonData(new ResourceItemCommonDataId(resourceName, ""), ""),
				new Object());

		fail();
	}

	/**
	 * {@link AbstractSyncResource#update(UploadCommonData, ResourceItemCommonData, Object)}用テストメソッド.<br>
	 * リソースアイテム共通データがnullの時{@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testUpdateFailBecauseOfNullItemCommon() throws Exception {

		// Arrange：異常系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		// Act
		target.update(new UploadCommonData(), null, new Object());

		fail();
	}

	/**
	 * {@link AbstractSyncResource#update(UploadCommonData, ResourceItemCommonData, Object)}用テストメソッド.<br>
	 * アイテムデータがnullの時 {@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testUpdateFailBecauseOfNullItem() throws Exception {

		// Arrange：異常系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		// Act
		target.update(new UploadCommonData(), new ResourceItemCommonData(
				new ResourceItemCommonDataId(resourceName, ""), ""), null);

		fail();
	}

	/**
	 * {@link AbstractSyncResource#delete(UploadCommonData, ResourceItemCommonData)}用テストメソッド.
	 */
	@Test
	public void testDelete() {

		// Arrange：正常系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		final long syncTime = 50;
		final UploadCommonData uploadCommon = createUploadCommon(syncTime);

		final String targetItemId = "item";

		final ResourceItemCommonDataId id = new ResourceItemCommonDataId(resourceName, "delete");
		final ResourceItemCommonData itemCommon = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(false);
				setAction(SyncAction.DELETE);
				setLastModified(10);
			}
		};

		final ResourceItemCommonData itemCommonForUpdate = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(true);
				setAction(SyncAction.UPDATE);
				setLastModified(10);
			}
		};

		final ResourceItemCommonData itemCommonAfterDelete = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(true);
				setAction(SyncAction.DELETE);
				setLastModified(50);
			}
		};
		final Object deletedItem = new Object();

		new Expectations() {
			{
				setField(target, "mock", mockObj);
				setField(target, updateStrategy);
				setField(target, defaultItemConverter);
				setField(target, commonDataService);

				commonDataService.currentCommonDataForUpdate(id);
				result = itemCommonForUpdate;

				mockObj.doDelete(targetItemId);
				result = deletedItem;

				commonDataService.saveUpdatedCommonData(itemCommonAfterDelete);
				result = itemCommonAfterDelete;
			}
		};

		// Act
		ResourceItemWrapper<Object> actual = target.delete(uploadCommon, itemCommon);

		// Assert：結果が正しいこと
		ResourceItemWrapper<Object> expected = new ResourceItemWrapper<>(itemCommonAfterDelete, deletedItem);
		assertThat(actual, is(expected));
	}

	/**
	 * {@link AbstractSyncResource#delete(UploadCommonData, ResourceItemCommonData)}用テストメソッド.<br>
	 * 1リクエストに同一リソースアイテムの更新が複数含まれる場合、サーバ側共通データの最終更新時刻が更新されていても、そのリクエストでの更新であれば競合なしと判定する.
	 */
	@Test
	public void testDeleteInOneRequest() throws Exception {

		// Arrange：正常系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		final long syncTime = 50;
		final UploadCommonData uploadCommon = createUploadCommon(syncTime);

		final String targetItemId = "item";

		final ResourceItemCommonDataId id = new ResourceItemCommonDataId(resourceName, "delete");
		final ResourceItemCommonData itemCommon = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(false);
				setAction(SyncAction.DELETE);
				setLastModified(10);
			}
		};

		// 同一上り更新リクエストの前の更新が行われている
		final ResourceItemCommonData itemCommonForUpdate = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(true);
				setAction(SyncAction.UPDATE);
				setLastModified(syncTime);
			}
		};

		final ResourceItemCommonData itemCommonAfterDelete = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(true);
				setAction(SyncAction.DELETE);
				setLastModified(50);
			}
		};
		// 論理削除状態
		final Object deletedItem = new Object();

		new Expectations() {
			{
				setField(target, "mock", mockObj);
				setField(target, updateStrategy);
				setField(target, defaultItemConverter);
				setField(target, commonDataService);

				commonDataService.currentCommonDataForUpdate(id);
				result = itemCommonForUpdate;

				mockObj.doDelete(targetItemId);
				result = deletedItem;

				commonDataService.saveUpdatedCommonData(itemCommonAfterDelete);
				result = itemCommonAfterDelete;
			}
		};

		// Act
		ResourceItemWrapper<Object> actual = target.delete(uploadCommon, itemCommon);

		// Assert：結果が正しいこと
		ResourceItemWrapper<Object> expected = new ResourceItemWrapper<>(itemCommonAfterDelete, deletedItem);
		assertThat(actual, is(expected));
	}

	/**
	 * {@link AbstractSyncResource#delete(UploadCommonData, ResourceItemCommonData)}用テストメソッド.<br>
	 * 競合が発生した場合でも競合解決戦略に沿って更新後データが確定した場合は更新(削除)が実行される.
	 */
	@Test
	public void testDeleteWhenConflictOccuredAndResolved() throws Exception {

		// Arrange：例外系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		final long syncTime = 50;
		final UploadCommonData uploadCommon = createUploadCommon(syncTime);

		final String targetItemId = "item";

		final ResourceItemCommonDataId id = new ResourceItemCommonDataId(resourceName, "delete");
		final ResourceItemCommonData itemCommon = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(false);
				setAction(SyncAction.DELETE);
				setLastModified(10);
			}
		};

		final ResourceItemCommonData itemCommonForUpdate = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(true);
				setAction(SyncAction.UPDATE);
				setLastModified(20);
			}
		};
		// リクエスト時のサーバデータ(UPDATEアクション状態)
		final Object itemForDelete = new Object();

		// 今回のリクエストデータが採用されるケース
		final ResourceItemCommonData itemCommonAfterDelete = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(false);
				setAction(SyncAction.DELETE);
				setLastModified(50);
			}
		};

		new Expectations() {
			{
				setField(target, "mock", mockObj);
				setField(target, updateStrategy);
				setField(target, defaultItemConverter);
				setField(target, commonDataService);

				commonDataService.currentCommonDataForUpdate(id);
				result = itemCommonForUpdate;

				mockObj.doGet(targetItemId);
				result = new HashMap<String, Object>() {
					{
						put(targetItemId, itemForDelete);
					}
				};

				updateStrategy.resolveConflict(itemCommon, null, itemCommonForUpdate, itemForDelete);
				result = null;

				mockObj.doDelete(targetItemId);
				result = itemForDelete;

				commonDataService.saveUpdatedCommonData(itemCommonAfterDelete);
				result = itemCommonAfterDelete;
			}
		};

		// Act
		ResourceItemWrapper<Object> actual = target.delete(uploadCommon, itemCommon);

		// Assert：結果が正しいこと
		ResourceItemWrapper<Object> expected = new ResourceItemWrapper<>(itemCommonAfterDelete, itemForDelete);
		assertThat(actual, is(expected));
	}

	/**
	 * {@link AbstractSyncResource#delete(UploadCommonData, ResourceItemCommonData)用テストメソッド.<br>
	 * 競合が発生し、解決できなかった場合には例外処理が行われる.
	 */
	@Test
	public void testDeleteWhenConflictOccuredAndThrowsException() throws Exception {

		// Arrange：例外系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		final long syncTime = 50;
		final UploadCommonData uploadCommon = createUploadCommon(syncTime);

		final String targetItemId = "item";

		final ResourceItemCommonDataId id = new ResourceItemCommonDataId(resourceName, "delete");
		final ResourceItemCommonData itemCommon = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(false);
				setAction(SyncAction.DELETE);
				setLastModified(10);
			}
		};

		final ResourceItemCommonData itemCommonForUpdate = new ResourceItemCommonData(id, targetItemId) {
			{
				setForUpdate(true);
				setAction(SyncAction.DELETE);
				setLastModified(20);
			}
		};
		// 論理削除状態
		final Object deletedItem = new Object();

		new Expectations() {
			{
				setField(target, "mock", mockObj);
				setField(target, updateStrategy);
				setField(target, defaultItemConverter);
				setField(target, commonDataService);

				commonDataService.currentCommonDataForUpdate(id);
				result = itemCommonForUpdate;

				updateStrategy.resolveConflict(itemCommon, null, itemCommonForUpdate, null);
				result = new ItemUpdatedException(itemCommonForUpdate, deletedItem);
			}
		};

		// Act
		ResourceItemWrapper<Object> actual = target.delete(uploadCommon, itemCommon);

		// Assert：結果が正しいこと
		ResourceItemWrapper<Object> expected = new ResourceItemWrapper<>(itemCommonForUpdate, deletedItem);
		assertThat(actual, is(expected));
	}

	/**
	 * {@link AbstractSyncResource#delete(UploadCommonData, ResourceItemCommonData)}用テストメソッド.<br>
	 * 上り更新共通データがnullの時 {@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testDeleteFailBecauseOfNullUploadCommon() throws Exception {

		// Arrange：異常系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		// Act
		target.delete(null, new ResourceItemCommonData(new ResourceItemCommonDataId(resourceName, ""), ""));

		fail();
	}

	/**
	 * {@link AbstractSyncResource#delete(UploadCommonData, ResourceItemCommonData)}用テストメソッド.<br>
	 * リソースアイテム共通データがnullの時{@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testDeleteFailBecauseOfNullItemCommon() throws Exception {

		// Arrange：異常系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		// Act
		target.delete(new UploadCommonData(), null);

		fail();
	}

	/**
	 * {@link AbstractSyncResource#delete(UploadCommonData, ResourceItemCommonData)}用テストメソッド.<br>
	 * アイテムデータがnullの時 {@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testDeleteFailBecauseOfNullItem() throws Exception {

		// Arrange：異常系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		// Act
		target.delete(new UploadCommonData(), new ResourceItemCommonData(
				new ResourceItemCommonDataId(resourceName, ""), ""));

		fail();
	}

	/**
	 * {@link AbstractSyncResource#forUpdate(List)}用テストメソッド.
	 */
	@Test
	public void testForUpdate() {

		// Arrange：正常系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		final ResourceItemCommonDataId idA = new ResourceItemCommonDataId(resourceName, "A");
		final ResourceItemCommonData commonA = new ResourceItemCommonData(idA, "itemA") {
			{
				setForUpdate(false);
			}
		};

		final ResourceItemCommonDataId idB = new ResourceItemCommonDataId(resourceName, "B");
		final ResourceItemCommonData commonB = new ResourceItemCommonData(idB, "itemB") {
			{
				setForUpdate(false);
			}
		};

		final ResourceItemCommonDataId idC = new ResourceItemCommonDataId(resourceName, "C");
		final ResourceItemCommonData commonC = new ResourceItemCommonData(idC, "itemC") {
			{
				setForUpdate(false);
			}
		};

		final List<ResourceItemCommonData> itemCommonList = new ArrayList<ResourceItemCommonData>() {
			{
				add(commonA);
				add(commonB);
				add(commonC);
			}
		};

		new Expectations() {
			{
				setField(target, "mock", mockObj);
				setField(target, updateStrategy);
				setField(target, defaultItemConverter);
				setField(target, commonDataService);

				commonDataService.currentCommonDataForUpdate(idA);
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemCommonData currentCommonDataForUpdate(ResourceItemCommonDataId id) {

						assertThat(commonA.isForUpdate(), is(false));
						commonA.setForUpdate(true);
						return commonA;
					};
				};

				commonDataService.currentCommonDataForUpdate(idB);
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemCommonData currentCommonDataForUpdate(ResourceItemCommonDataId id) {

						assertThat(commonB.isForUpdate(), is(false));
						commonB.setForUpdate(true);
						return commonB;
					};
				};

				commonDataService.currentCommonDataForUpdate(idC);
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemCommonData currentCommonDataForUpdate(ResourceItemCommonDataId id) {
						assertThat(commonC.isForUpdate(), is(false));
						commonC.setForUpdate(true);
						return commonC;
					};
				};

			}
		};

		// Act
		List<ResourceItemCommonData> actual = target.forUpdate(itemCommonList);

		// Assert：結果が正しいこと
		List<ResourceItemCommonData> expected = new ArrayList<ResourceItemCommonData>() {
			{
				add(commonA);
				add(commonB);
				add(commonC);
			}
		};
		assertThat(actual, is(equalTo(expected)));
		assertThat(actual.get(0).isForUpdate(), is(true));
		assertThat(actual.get(1).isForUpdate(), is(true));
		assertThat(actual.get(2).isForUpdate(), is(true));
	}

	/**
	 * {@link AbstractSyncResource#itemConverter()}用テストメソッド.
	 */
	@Test
	public void testItemConverter() {

		// Arrange：正常系
		ResourceItemConverter<Object> resourceItemConverter = new ResourceItemConverter<Object>() {
			@Override
			public Object convertToItem(Object itemObj, Class<Object> to) {
				return null;
			}
		};

		final AbstractSyncResource<Object> target = new TestTargetImpl(resourceItemConverter);

		new Expectations() {
			{
				setField(target, "mock", mockObj);
				setField(target, updateStrategy);
				setField(target, defaultItemConverter);
				setField(target, commonDataService);
			}
		};

		// Act
		ResourceItemConverter<Object> actual = target.itemConverter();

		// Assert：結果が正しいこと
		ResourceItemConverter<Object> expected = resourceItemConverter;
		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * {@link AbstractSyncResource#itemConverter()}用テストメソッド.<br>
	 * 設定していなければデフォルトが返される.
	 */
	@Test
	public void testItemConverterDefault() {

		// Arrange：正常系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		new Expectations() {
			{
				setField(target, "mock", mockObj);
				setField(target, updateStrategy);
				setField(target, defaultItemConverter);
				setField(target, commonDataService);
			}
		};

		// Act
		ResourceItemConverter<Object> actual = target.itemConverter();

		// Assert：結果が正しいこと
		ResourceItemConverter<Object> expected = defaultItemConverter;
		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * {@link AbstractSyncResource#name()}用テストメソッド.
	 */
	@Test
	public void testName() {

		// Arrange：正常系
		AbstractSyncResource<?> target = new TestTargetImpl();

		// Act
		String actual = target.name();

		// Assert：結果が正しいこと
		String expected = resourceName;
		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * {@link AbstractSyncResource#itemType()}用テストメソッド.
	 */
	@Test
	public void testItemType() {

		// Arrange：正常系
		AbstractSyncResource<Object> target1 = new TestTargetImpl();
		AbstractSyncResource<String> target2 = new AbstractSyncResource<String>() {
			// @formatter:off
			@Override protected String doUpdate(String item) { return null; }
			@Override protected Map<String, String> doGetByQuery(Map<String, String[]> conditions, String... ids) { return null; }
			@Override protected Map<String, String> doGet(String... ids) { return null; }
			@Override protected String doDelete(String id) { return null; }
			@Override protected String doCreate(String newItem) throws DuplicateIdException { return null; }
			// @formatter:on
		};

		// Act
		// Assert：結果が正しいこと
		assertThat(target1.itemType(), is(equalTo(Object.class)));
		assertThat(target2.itemType(), is(equalTo(String.class)));
	}

	/**
	 * {@link AbstractSyncResource#applyResourceConfigurations(Properties)}用テストメソッド.
	 */
	@Test
	public void testApplyResourceConfigurations() throws Exception {

		// Arrange：正常系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		Properties resourceConfigurations = new Properties() {
			{
				put("AbstractSyncResource.countOfVerification", "2");
				put("prop", "val");
			}
		};

		// Act
		target.applyResourceConfigurations(resourceConfigurations);

		// Assert：結果が正しいこと
		Field field = target.getClass().getSuperclass().getDeclaredField("countOfVerification");
		assertThat(field, is(notNullValue()));
		field.setAccessible(true);
		assertThat(field.getInt(target), is(equalTo(2)));
	}

	/**
	 * {@link AbstractSyncResource#applyResourceConfigurations(Properties)}用テストメソッド.<br>
	 * "AbstractSyncResource.countOfVerification"を設定しなければ初期値の0となる.
	 */
	@Test
	public void testApplyResourceConfigurationsOnlyDontCare() throws Exception {

		// Arrange：正常系
		final AbstractSyncResource<Object> target = new TestTargetImpl();

		Properties resourceConfigurations = new Properties() {
			{
				put("prop", "val");
			}
		};

		// Act
		target.applyResourceConfigurations(resourceConfigurations);

		// Assert：結果が正しいこと
		Field field = target.getClass().getSuperclass().getDeclaredField("countOfVerification");
		assertThat(field, is(notNullValue()));
		field.setAccessible(true);
		assertThat(field.getInt(target), is(equalTo(0)));
	}

	/**
	 * テスト用実装クラス.<br>
	 * 共通データとアイテムデータの整合性を検証する回数をコンストラクタで指定できる.
	 */
	@SyncResourceService(resourceName = resourceName)
	private class TestTargetImpl extends AbstractSyncResource<Object> {

		private MockTargetImpl mock;

		TestTargetImpl(final int countOfVerification) {
			applyResourceConfigurations(new Properties() {
				{
					put("AbstractSyncResource.countOfVerification", String.valueOf(countOfVerification));
				}
			});
		}

		TestTargetImpl(ResourceItemConverter<Object> converter) {
			this(0);
			defaultItemConverter = converter;
		}

		TestTargetImpl() {
			this(0);
		}

		@Override
		protected Map<String, Object> doGet(String... ids) {

			return mock.doGet(ids);
		}

		@Override
		protected Map<String, Object> doGetByQuery(Map<String, String[]> conditions, String... ids) {

			return mock.doGetByQuery(conditions, ids);
		}

		@Override
		protected String doCreate(Object newItem) throws DuplicateIdException {

			return mock.doCreate(newItem);
		}

		@Override
		protected String doUpdate(Object item) {

			return mock.doUpdate(item);
		}

		@Override
		protected Object doDelete(String id) {

			return mock.doDelete(id);
		}
	}

	/**
	 * テスト用Mockクラス.<br>
	 * テスト対象クラスから委譲されて実行される.<br>
	 * 実際の動作はMockのExpectationで置き換えることができる.
	 */
	private class MockTargetImpl {

		Map<String, Object> doGet(String... ids) {
			return null;
		}

		Map<String, Object> doGetByQuery(Map<String, String[]> conditions, String... ids) {
			return null;
		}

		String doCreate(Object newItem) throws DuplicateIdException {
			return null;
		}

		String doUpdate(Object item) {
			return null;
		}

		Object doDelete(String id) {
			return null;
		}
	}

	/**
	 * SyncCommonData型として扱えるオブジェクトを返す.
	 *
	 * @param storageId ストレージID
	 * @param syncTime 同期時刻
	 * @return SyncCommonData
	 */
	private SyncCommonData createSyncCommon(String storageId, int syncTime) {
		return new SyncCommonData() {

			@Override
			public long getSyncTime() {
				return 100;
			}

			@Override
			public String getStorageId() {
				return "storageId";
			}

			@Override
			public String getLockToken() {
				return null;
			}
		};
	}

	/**
	 * UploadCommonData型のオブジェクトを返す.
	 *
	 * @param syncTime 同期時刻
	 * @return
	 */
	private UploadCommonData createUploadCommon(final long syncTime) {
		return new UploadCommonData() {
			{
				setSyncTime(syncTime);
			}
		};
	}

}
