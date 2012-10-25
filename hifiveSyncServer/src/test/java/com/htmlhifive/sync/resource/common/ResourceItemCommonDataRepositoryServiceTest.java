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
package com.htmlhifive.sync.resource.common;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mockit.Expectations;
import mockit.Mocked;

import org.junit.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import com.htmlhifive.sync.exception.BadRequestException;
import com.htmlhifive.sync.exception.SyncException;
import com.htmlhifive.sync.resource.SyncAction;

/**
 * <H3>ResourceItemCommonDataRepositoryServiceのテストクラス.</H3>
 *
 * @author kishigam
 */
public class ResourceItemCommonDataRepositoryServiceTest {

	@Mocked
	private ResourceItemCommonDataRepository repository;

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(ResourceItemCommonDataRepositoryService.class, notNullValue());
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#ResourceItemCommonDataRepositoryService()}用テストメソッド.
	 */
	@Test
	public void testInstantiation() {
		ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();
		assertThat(target, notNullValue());
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#currentCommonData(ResourceItemCommonDataId)}用テストメソッド.
	 */
	@Test
	public void testCurrentCommonData_id() {

		// Arrange：正常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();

		final ResourceItemCommonDataId id = new ResourceItemCommonDataId("resourceName", "resourceItemId");

		final ResourceItemCommonData expected = new ResourceItemCommonData(id, "targetItemId");

		new Expectations() {
			{

				setField(target, repository);

				repository.findOne(id);
				result = expected;
			}
		};

		// Act
		ResourceItemCommonData actual = target.currentCommonData(id);

		// Assert：結果が正しいこと
		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#currentCommonData(ResourceItemCommonDataId)}用テストメソッド.<br>
	 * 存在しない場合 nullが返る.
	 */
	@Test
	public void testCurrentCommonData_id_NotFound() {

		// Arrange：異常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();

		final ResourceItemCommonDataId id = new ResourceItemCommonDataId("resourceName", "resourceItemId");

		new Expectations() {
			{
				setField(target, repository);

				repository.findOne(id);
				result = null;
			}
		};

		// Act
		ResourceItemCommonData actual = target.currentCommonData(id);

		// Assert：結果が正しいこと
		assertThat(actual, is(nullValue()));
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#currentCommonData(ResourceItemCommonDataId)}用テストメソッド.<br>
	 * nullが渡されたら{@link InvalidDataAccessApiUsageException}がスローされる.
	 */
	@Test(expected = InvalidDataAccessApiUsageException.class)
	public void testCurrentCommonData_id_FailBecauseOfNullInput() {

		// Arrange：異常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();

		new Expectations() {
			{
				setField(target, repository);

				repository.findOne(null);

				// 実際にはメッセージが指定されている
				result = new InvalidDataAccessApiUsageException("");
			}
		};

		// Act
		target.currentCommonData((ResourceItemCommonDataId) null);

		// Assert：例外でなければ失敗
		fail();
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#currentCommonDataForUpdate(ResourceItemCommonDataId)}用テストメソッド.
	 */
	@Test
	public void testCurrentCommonDataForUpdateResourceItemCommonDataId() {

		// Arrange：正常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();

		final ResourceItemCommonDataId id = new ResourceItemCommonDataId("resourceName", "resourceItemId");

		final ResourceItemCommonData expected = new ResourceItemCommonData(id, "targetItemId");

		new Expectations() {
			{
				setField(target, repository);

				repository.findOneForUpdate(id);
				result = expected;
			}
		};

		// Act
		ResourceItemCommonData actual = target.currentCommonDataForUpdate(id);

		// Assert：結果が正しいこと
		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#currentCommonDataForUpdate(ResourceItemCommonDataId)}用テストメソッド.
	 * 存在しない場合 nullが返る.
	 */
	@Test
	public void testCurrentCommonDataForUpdate_id_NotFound() {

		// Arrange：異常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();

		final ResourceItemCommonDataId id = new ResourceItemCommonDataId("resourceName", "resourceItemId");

		new Expectations() {
			{
				setField(target, repository);

				repository.findOneForUpdate(id);
				result = null;
			}
		};

		// Act
		ResourceItemCommonData actual = target.currentCommonDataForUpdate(id);

		// Assert：結果が正しいこと
		assertThat(actual, is(nullValue()));
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#currentCommonDataForUpdate(ResourceItemCommonDataId)}用テストメソッド.
	 * nullが渡されたらnullが返される.
	 */
	@Test
	public void testCurrentCommonDataForUpdate_id_NullInput() {

		// Arrange：異常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();

		new Expectations() {
			{
				setField(target, repository);

				repository.findOneForUpdate(null);
				result = null;
			}
		};

		// Act
		ResourceItemCommonData actual = target.currentCommonDataForUpdate((ResourceItemCommonDataId) null);

		// Assert：結果が正しいこと
		assertThat(actual, is(nullValue()));
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#modifiedCommonData(String, long)}用テストメソッド.
	 */
	@SuppressWarnings("serial")
	@Test
	public void testModifiedCommonData() {

		// Arrange：正常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();

		final String resourceName = "resourceName";
		final long lastDownloadTime = 0L;

		final List<ResourceItemCommonData> expected = new ArrayList<ResourceItemCommonData>() {
			{
				add(new ResourceItemCommonData(new ResourceItemCommonDataId(resourceName, "1"), "resource1"));
				add(new ResourceItemCommonData(new ResourceItemCommonDataId(resourceName, "2"), "resource2"));
			}
		};

		new Expectations() {
			{
				setField(target, repository);

				repository.findModified(resourceName, lastDownloadTime);
				result = expected;
			}
		};

		// Act
		List<ResourceItemCommonData> actual = target.modifiedCommonData(resourceName, lastDownloadTime);

		// Assert：結果が正しいこと
		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#modifiedCommonData(String, long)}用テストメソッド.<br>
	 * 検索結果が0件の場合空のリストが返される.
	 */
	@Test
	public void testModifiedCommonDataReturnsEmptyList() {

		// Arrange：例外系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();

		final String resourceName = "resourceName";
		final long lastDownloadTime = 0L;

		final List<ResourceItemCommonData> expected = Collections.emptyList();

		new Expectations() {
			{
				setField(target, repository);

				repository.findModified(resourceName, lastDownloadTime);
				result = expected;
			}
		};

		// Act
		List<ResourceItemCommonData> actual = target.modifiedCommonData(resourceName, lastDownloadTime);

		// Assert：結果が正しいこと
		assertThat(actual.size(), is(equalTo(0)));
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#modifiedCommonData(String, long)}用テストメソッド. <br>
	 * nullを渡すと、nullが返される(空のリストではない).
	 */
	@Test
	public void testModifiedCommonData_NullInput() {

		// Arrange：正常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();

		final long lastDownloadTime = 0L;

		new Expectations() {
			{
				setField(target, repository);

				repository.findModified(null, lastDownloadTime);
				result = null;
			}
		};

		// Act
		List<ResourceItemCommonData> actual = target.modifiedCommonData(null, lastDownloadTime);

		// Assert：結果が正しいこと
		assertThat(actual, is(nullValue()));
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#currentCommonData(String, String)}用テストメソッド.
	 */
	@Test
	public void testCurrentCommonData_String() {

		// Arrange：正常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();
		final String resourceName = "resourceName";
		final String targetItemId = "targetItemId";

		final ResourceItemCommonData expected = new ResourceItemCommonData(new ResourceItemCommonDataId(resourceName,
				"resourceItemId"), targetItemId);

		new Expectations() {
			{
				setField(target, repository);

				repository.findByTargetItemId(resourceName, targetItemId);
				result = expected;
			}
		};

		// Act
		ResourceItemCommonData actual = target.currentCommonData(resourceName, targetItemId);

		// Assert：結果が正しいこと
		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#currentCommonData(String, String)}用テストメソッド.<br>
	 * 存在しない場合nullが返される.
	 */
	@Test
	public void testCurrentCommonData_String_NotFound() {

		// Arrange：例外系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();
		final String resourceName = "resourceName";
		final String targetItemId = "targetItemId";

		new Expectations() {
			{
				setField(target, repository);

				repository.findByTargetItemId(resourceName, targetItemId);
				result = null;
			}
		};

		// Act
		ResourceItemCommonData actual = target.currentCommonData(resourceName, targetItemId);

		// Assert：結果が正しいこと
		assertThat(actual, is(nullValue()));
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#currentCommonData(String, String)}用テストメソッド.<br>
	 * どちらかの引数にnullが渡された場合nullが返される.
	 */
	@Test
	public void testCurrentCommonData_String_NullInput() {

		// Arrange：異常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();
		final String resourceName = "resourceName";
		final String targetItemId = "targetItemId";

		new Expectations() {
			{
				setField(target, repository);

				repository.findByTargetItemId(null, targetItemId);
				result = null;

				repository.findByTargetItemId(resourceName, null);
				result = null;
			}
		};

		// Act
		ResourceItemCommonData actual1 = target.currentCommonData(null, targetItemId);
		// Assert：結果が正しいこと
		assertThat(actual1, is(nullValue()));

		// Act
		ResourceItemCommonData actual2 = target.currentCommonData(resourceName, null);
		// Assert：結果が正しいこと
		assertThat(actual2, is(nullValue()));
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#saveNewCommonData(ResourceItemCommonData)}用テストメソッド.
	 */
	@Test
	public void testSaveNewCommonDataResourceItemCommonData() {

		// Arrange：正常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();

		final ResourceItemCommonDataId id = new ResourceItemCommonDataId("resourceName", "resourceItemId");

		final ResourceItemCommonData common = new ResourceItemCommonData(id, "targetItemId");

		new Expectations() {
			{
				setField(target, repository);

				repository.exists(id);
				result = false;

				repository.save(common);
				result = common;
			}
		};

		// Act
		ResourceItemCommonData actual = target.saveNewCommonData(common);

		// Assert：結果が正しいこと
		assertThat(actual, is(equalTo(common)));
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#saveNewCommonData(ResourceItemCommonData)}用テストメソッド.<br>
	 * 既にIDが同じデータが存在する場合、{@link BadRequestException}がスローされる.
	 */
	@Test(expected = BadRequestException.class)
	public void testSaveNewCommonDataResourceItemCommonDataFailBecauseOfDuplication() {

		// Arrange：異常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();

		final ResourceItemCommonDataId id = new ResourceItemCommonDataId("resourceName", "resourceItemId");

		final ResourceItemCommonData common = new ResourceItemCommonData(id, "targetItemId");

		new Expectations() {
			{
				setField(target, repository);

				repository.exists(id);
				result = true;
			}
		};

		// Act
		target.saveNewCommonData(common);

		// Assert：例外でなければ失敗
		fail();
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#saveNewCommonData(ResourceItemCommonData)}用テストメソッド.<br>
	 * 既にIDが同じデータが存在する場合、{@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testSaveNewCommonDataResourceItemCommonDataFailBecauseOfNullInput() {

		// Arrange：異常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();

		new Expectations() {
			{
				setField(target, repository);
			}
		};

		// Act
		target.saveNewCommonData(null);

		// Assert：例外でなければ失敗
		fail();
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#saveUpdatedCommonData(ResourceItemCommonData)}用テストメソッド.
	 */
	@Test
	public void testSaveUpdatedCommonDataResourceItemCommonData() {

		// Arrange：正常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();

		final ResourceItemCommonDataId id = new ResourceItemCommonDataId("resourceName", "resourceItemId");

		final ResourceItemCommonData newCommon = new ResourceItemCommonData(id, "targetItemId");
		newCommon.setAction(SyncAction.UPDATE);
		newCommon.setLastModified(100);

		new Expectations() {
			{
				setField(target, repository);

				repository.exists(id);
				result = true;

				repository.save(newCommon);
				result = newCommon;
			}
		};

		// Act
		ResourceItemCommonData actual = target.saveUpdatedCommonData(newCommon);

		// Assert：結果が正しいこと
		assertThat(actual, is(newCommon));
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#saveUpdatedCommonData(ResourceItemCommonData)}用テストメソッド.<br>
	 * 更新対象が存在しない場合、{@link BadRequestException}がスローされる.
	 */
	@Test(expected = BadRequestException.class)
	public void testSaveUpdatedCommonDataResourceItemCommonDataFailBecauseOfAlreadyExists() {

		// Arrange：異常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();

		final ResourceItemCommonDataId id = new ResourceItemCommonDataId("resourceName", "resourceItemId");

		final ResourceItemCommonData newCommon = new ResourceItemCommonData(id, "targetItemId");
		newCommon.setAction(SyncAction.UPDATE);
		newCommon.setLastModified(100);

		new Expectations() {
			{
				setField(target, repository);

				repository.exists(id);
				result = false;
			}
		};

		// Act
		target.saveUpdatedCommonData(newCommon);

		// Assert：例外でなければ失敗
		fail();
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#saveUpdatedCommonData(ResourceItemCommonData)}用テストメソッド.<br>
	 * nullが渡された場合、{@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testSaveUpdatedCommonDataResourceItemCommonDataFailBecauseOfNullInput() {

		// Arrange：異常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();

		new Expectations() {
			{
				setField(target, repository);
			}
		};

		// Act
		target.saveUpdatedCommonData(null);

		// Assert：例外でなければ失敗
		fail();
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#modify(String, String, String, SyncAction, long)}用テストメソッド.
	 */
	@Test
	public void testModify_Create() {

		// Arrange：正常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();

		final String resourceName = "resouceName";
		final String resourceItemId = "resourceItemId";
		final String targetItemId = "targetItemId";
		final long updateTime = 0L;
		final SyncAction action = SyncAction.CREATE;

		final ResourceItemCommonData newCommon = new ResourceItemCommonData(new ResourceItemCommonDataId(resourceName,
				resourceItemId), targetItemId);
		newCommon.setAction(action);
		newCommon.setLastModified(updateTime);

		new Expectations() {
			{
				setField(target, repository);

				repository.findOne(new ResourceItemCommonDataId(resourceName, resourceItemId));
				result = null;

				repository.save(newCommon);
				result = newCommon;
			}
		};

		// Act
		target.modify(resourceName, resourceItemId, targetItemId, action, updateTime);

		// Assert：結果が正しいこと
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#modify(String, String, String, SyncAction, long)}用テストメソッド.<br>
	 * 存在するデータと同じIDでCREATEデータを登録しようとすると{@link SyncException}がスローされる.
	 */
	@Test(expected = SyncException.class)
	public void testModify_CreateFailBecauseOfDuplication() {

		// Arrange：正常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();

		final String resourceName = "resouceName";
		final String resourceItemId = "resourceItemId";
		final String targetItemId = "targetItemId";
		final long updateTime = 0L;
		final SyncAction action = SyncAction.CREATE;

		final ResourceItemCommonData existing = new ResourceItemCommonData(new ResourceItemCommonDataId(resourceName,
				resourceItemId), targetItemId);

		new Expectations() {
			{
				setField(target, repository);

				repository.findOne(new ResourceItemCommonDataId(resourceName, resourceItemId));
				result = existing;
			}
		};

		// Act
		target.modify(resourceName, resourceItemId, targetItemId, action, updateTime);

		// Assert：結果が正しいこと
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#modify(String, String, String, SyncAction, long)}用テストメソッド.
	 */
	@Test
	public void testModify_UpdateOrDelete() {

		// Arrange：正常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();

		final String resourceName = "resouceName";
		final String resourceItemId = "resourceItemId";
		final String targetItemId = "targetItemId";
		final long updateTime = 100L;
		final SyncAction action = SyncAction.DELETE;

		final ResourceItemCommonData existing = new ResourceItemCommonData(new ResourceItemCommonDataId(resourceName,
				resourceItemId), targetItemId);
		existing.setAction(SyncAction.CREATE);
		existing.setLastModified(0);

		final ResourceItemCommonData newCommon = new ResourceItemCommonData(new ResourceItemCommonDataId(resourceName,
				resourceItemId), targetItemId);
		newCommon.setAction(action);
		newCommon.setLastModified(updateTime);

		new Expectations() {
			{
				setField(target, repository);

				repository.findOne(new ResourceItemCommonDataId(resourceName, resourceItemId));
				result = existing;

				repository.save(newCommon);
				result = newCommon;
			}
		};

		// Act
		target.modify(resourceName, resourceItemId, targetItemId, action, updateTime);

		// Assert：結果が正しいこと
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#modify(String, String, String, SyncAction, long)}用テストメソッド.
	 */
	@Test(expected = NullPointerException.class)
	public void testModify_UpdateOrDeleteFailBecauseOfNotFound() {

		// Arrange：正常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();

		final String resourceName = "resouceName";
		final String resourceItemId = "resourceItemId";
		final String targetItemId = "targetItemId";
		final long updateTime = 100L;
		final SyncAction action = SyncAction.DELETE;

		new Expectations() {
			{
				setField(target, repository);

				repository.findOne(new ResourceItemCommonDataId(resourceName, resourceItemId));
				result = null;
			}
		};

		// Act
		target.modify(resourceName, resourceItemId, targetItemId, action, updateTime);

		// Assert：例外でなければ失敗
		fail();
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#modify(String, String, String, SyncAction, long)}用テストメソッド.<br>
	 * Idデータ(リソース名、リソースアイテムID)にnullがあった場合{@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testModify_UpdateOrDeleteFailBecauseOfNullId() {

		// Arrange：正常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();

		final String resourceItemId = "resourceItemId";
		final String targetItemId = "targetItemId";
		final long updateTime = 100L;
		final SyncAction action = SyncAction.DELETE;

		new Expectations() {
			{
				setField(target, repository);

				repository.findOne(new ResourceItemCommonDataId(null, resourceItemId));
			}
		};

		// Act
		target.modify(null, resourceItemId, targetItemId, action, updateTime);

		// Assert：例外でなければ失敗
		fail();
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryService#modify(String, String, String, SyncAction, long)}用テストメソッド.<br>
	 * Idデータ(リソース名、リソースアイテムID)にnullがあった場合{@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testModify_UpdateOrDeleteFailBecauseOfNullId_2() {

		// Arrange：正常系
		final ResourceItemCommonDataRepositoryService target = new ResourceItemCommonDataRepositoryService();

		final String resourceName = "resouceName";
		final String targetItemId = "targetItemId";
		final long updateTime = 100L;
		final SyncAction action = SyncAction.DELETE;

		new Expectations() {
			{
				setField(target, repository);

				repository.findOne(new ResourceItemCommonDataId(resourceName, null));
			}
		};

		// Act
		target.modify(resourceName, null, targetItemId, action, updateTime);

		// Assert：例外でなければ失敗
		fail();
	}
}
