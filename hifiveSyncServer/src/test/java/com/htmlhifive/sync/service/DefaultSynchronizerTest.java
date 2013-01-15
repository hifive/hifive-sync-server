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
package com.htmlhifive.sync.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.Delegate;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.FullVerificationsInOrder;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import mockit.VerificationsInOrder;

import org.junit.Test;

import com.htmlhifive.sync.exception.ConflictException;
import com.htmlhifive.sync.exception.NoSuchResourceException;
import com.htmlhifive.sync.resource.ResourceItemConverter;
import com.htmlhifive.sync.resource.ResourceItemWrapper;
import com.htmlhifive.sync.resource.ResourceQueryConditions;
import com.htmlhifive.sync.resource.SyncAction;
import com.htmlhifive.sync.resource.SyncConflictType;
import com.htmlhifive.sync.resource.SyncResource;
import com.htmlhifive.sync.resource.SyncResourceManager;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.resource.common.ResourceItemCommonDataId;
import com.htmlhifive.sync.service.download.DownloadCommonData;
import com.htmlhifive.sync.service.download.DownloadControlType;
import com.htmlhifive.sync.service.download.DownloadRequest;
import com.htmlhifive.sync.service.download.DownloadResponse;
import com.htmlhifive.sync.service.upload.UploadCommonData;
import com.htmlhifive.sync.service.upload.UploadCommonDataRepository;
import com.htmlhifive.sync.service.upload.UploadControlType;
import com.htmlhifive.sync.service.upload.UploadRequest;
import com.htmlhifive.sync.service.upload.UploadResponse;

/**
 * <H3>DefaultSynchronizerのテストクラス.</H3>
 *
 * @author kishigam
 */
@SuppressWarnings("serial")
public class DefaultSynchronizerTest {

	@Mocked
	private SyncConfiguration syncConfiguration;

	@Mocked
	private SyncResourceManager resourceManager;

	@Mocked
	private UploadCommonDataRepository repository;

	@Mocked
	private SyncResource<Object> resource1;

	@Mocked
	private SyncResource<Object> resource2;

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(DefaultSynchronizer.class, notNullValue());
	}

	/**
	 * {@link DefaultSynchronizer#DefaultSynchronizer()}用テストメソッド.
	 */
	@Test
	public void testInstantiation() {
		DefaultSynchronizer target = new DefaultSynchronizer();
		assertThat(target, notNullValue());
	}

	/**
	 * {@link DefaultSynchronizer#download(DownloadRequest)}用テストメソッド.<br>
	 * リソースごと、クエリごとに{@link SyncResource#getByQuery(SyncCommonData, ResourceQueryConditions)}が呼び出される.<br>
	 * 結果はリソースごとのアイテム(ラッパーオブジェクト)リストになり、重複アイテムは1つだけが保持される.
	 */
	@Test
	public void testDownload() {

		// Arrange：正常系
		final DefaultSynchronizer target = new DefaultSynchronizer();

		final DownloadCommonData requestCommon = new DownloadCommonData("storageId");

		final ResourceQueryConditions resource1QueryConditions1 = new ResourceQueryConditions() {
			{
				setLastDownloadTime(1000);
				setConditions(new HashMap<String, String[]>() {
					{
						put("id", new String[] { "a", "b" });
						put("no", new String[] { "1" });
					}
				});
			}
		};

		final ResourceQueryConditions resource2QueryConditions1 = new ResourceQueryConditions() {
			{
				setLastDownloadTime(2000);
				setConditions(new HashMap<String, String[]>() {
					{
						put("id", new String[] { "x", "y", "z" });
					}
				});
			}
		};

		final ResourceQueryConditions resource2QueryConditions2 = new ResourceQueryConditions() {
			{
				setLastDownloadTime(3000);
				setConditions(new HashMap<String, String[]>() {
					{
						put("id", new String[] { "one", "two" });
					}
				});
			}
		};

		final DownloadRequest request = new DownloadRequest() {
			{
				setDownloadCommonData(requestCommon);
				setQueries(new HashMap<String, List<ResourceQueryConditions>>() {
					{
						put("resource1", new ArrayList<ResourceQueryConditions>() {
							{
								add(resource1QueryConditions1);
							}
						});
						put("resource2", new ArrayList<ResourceQueryConditions>() {
							{
								add(resource2QueryConditions1);
								add(resource2QueryConditions2);
							}
						});
					}
				});
			}
		};

		final long syncTime = 4000;
		final int bufferTimeForDownload = 1;

		final ResourceItemWrapper<Object> item1_1 = createDownloadItemWrapper("1", "1");
		final ResourceItemWrapper<Object> item1_2 = createDownloadItemWrapper("1", "2");
		final ResourceItemWrapper<Object> item2_1 = createDownloadItemWrapper("2", "1");
		final ResourceItemWrapper<Object> item2_2 = createDownloadItemWrapper("2", "2");
		final ResourceItemWrapper<Object> item2_3 = createDownloadItemWrapper("2", "3");

		new NonStrictExpectations() {
			{
				setField(target, syncConfiguration);
				setField(target, resourceManager);
				setField(target, repository);

				syncConfiguration.generateSyncTime();
				result = syncTime;

				resourceManager.locateSyncResource("resource1");
				result = resource1;
				resourceManager.locateSyncResource("resource2");
				result = resource2;

				resource1.getByQuery(requestCommon, resource1QueryConditions1);
				result = new ArrayList<ResourceItemWrapper<?>>() {
					{
						add(item1_1);
						add(item1_2);
					}
				};

				resource2.getByQuery(requestCommon, resource2QueryConditions1);
				result = new ArrayList<ResourceItemWrapper<?>>() {
					{
						add(item2_1);
						add(item2_2);
					}
				};
				resource2.getByQuery(requestCommon, resource2QueryConditions2);
				result = new ArrayList<ResourceItemWrapper<?>>() {
					{
						add(item2_2);
						add(item2_3);
					}
				};

				syncConfiguration.downloadControl();
				result = DownloadControlType.NONE;

				syncConfiguration.bufferTimeForDownload();
				result = bufferTimeForDownload;
			}
		};

		// Act
		DownloadResponse actual = target.download(request);

		new FullVerifications() {
			{
				syncConfiguration.generateSyncTime();

				resourceManager.locateSyncResource("resource1");
				resource1.getByQuery(requestCommon, resource1QueryConditions1);

				resourceManager.locateSyncResource("resource2");
				resource2.getByQuery(requestCommon, resource2QueryConditions1);

				resourceManager.locateSyncResource("resource2");
				resource2.getByQuery(requestCommon, resource2QueryConditions2);

				syncConfiguration.downloadControl();
				syncConfiguration.bufferTimeForDownload();
			}
		};

		// Assert：結果が正しいこと
		DownloadCommonData expectedCommon = new DownloadCommonData("storageId") {
			{
				setLastDownloadTime(3000);
			}
		};

		DownloadResponse expected = new DownloadResponse(expectedCommon) {
			{
				setResourceItems(new HashMap<String, List<ResourceItemWrapper<?>>>() {
					{
						put("resource1", new ArrayList<ResourceItemWrapper<?>>() {
							{
								add(item1_1);
								add(item1_2);
							}
						});
						put("resource2", new ArrayList<ResourceItemWrapper<?>>() {
							{
								add(item2_1);
								add(item2_2);
								add(item2_3);
							}
						});
					}
				});
			}
		};

		assertThat(actual.getDownloadCommonData(), is(equalTo(expected.getDownloadCommonData())));
		assertThat(actual.getResourceItems().size(), is(equalTo(2)));

		assertThat(actual.getResourceItems().get("resource1").size(), is(equalTo(2)));
		assertThat(actual.getResourceItems().get("resource1").contains(item1_1), is(true));
		assertThat(actual.getResourceItems().get("resource1").contains(item1_2), is(true));

		assertThat(actual.getResourceItems().get("resource2").size(), is(equalTo(3)));
		assertThat(actual.getResourceItems().get("resource2").contains(item2_1), is(true));
		assertThat(actual.getResourceItems().get("resource2").contains(item2_2), is(true));
		assertThat(actual.getResourceItems().get("resource2").contains(item2_3), is(true));
	}

	/**
	 * {@link DefaultSynchronizer#download(DownloadRequest)}用テストメソッド.<br>
	 * {@link DownloadControlType#READ_LOCK}が設定されている場合、順序をソートして{@link SyncResource#forUpdate(List)}を行った後、
	 * {@link SyncResource#get(SyncCommonData, List)}により再取得する.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testDownloadOnREAD_LOCK() {

		// Arrange：正常系
		final DefaultSynchronizer target = new DefaultSynchronizer();

		final DownloadCommonData requestCommon = new DownloadCommonData("storageId");

		final ResourceQueryConditions resource1QueryConditions1 = new ResourceQueryConditions() {
			{
				setLastDownloadTime(1000);
				setConditions(new HashMap<String, String[]>() {
					{
						put("id", new String[] { "a", "b" });
						put("no", new String[] { "1" });
					}
				});
			}
		};

		final ResourceQueryConditions resource2QueryConditions1 = new ResourceQueryConditions() {
			{
				setLastDownloadTime(2000);
				setConditions(new HashMap<String, String[]>() {
					{
						put("id", new String[] { "x", "y", "z" });
					}
				});
			}
		};

		final ResourceQueryConditions resource2QueryConditions2 = new ResourceQueryConditions() {
			{
				setLastDownloadTime(3000);
				setConditions(new HashMap<String, String[]>() {
					{
						put("id", new String[] { "one", "two" });
					}
				});
			}
		};

		// 2→1の順に格納
		final DownloadRequest request = new DownloadRequest() {
			{
				setDownloadCommonData(requestCommon);
				setQueries(new HashMap<String, List<ResourceQueryConditions>>() {
					{
						put("resource2", new ArrayList<ResourceQueryConditions>() {
							{
								add(resource2QueryConditions2);
								add(resource2QueryConditions1);
							}
						});
						put("resource1", new ArrayList<ResourceQueryConditions>() {
							{
								add(resource1QueryConditions1);
							}
						});
					}
				});
			}
		};

		final long syncTime = 4000;
		final int bufferTimeForDownload = 1;

		final ResourceItemWrapper<Object> item1_1 = createDownloadItemWrapper("1", "1");
		final ResourceItemWrapper<Object> item1_2 = createDownloadItemWrapper("1", "2");
		final ResourceItemWrapper<Object> item2_1 = createDownloadItemWrapper("2", "1");
		final ResourceItemWrapper<Object> item2_2 = createDownloadItemWrapper("2", "2");

		final ArrayList<ResourceItemCommonData> forUpdateItemCommonList1 = new ArrayList<ResourceItemCommonData>() {
			{
				item1_1.getItemCommonData().setForUpdate(true);
				item1_2.getItemCommonData().setForUpdate(true);

				add(item1_1.getItemCommonData());
				add(item1_2.getItemCommonData());
			}
		};

		final ArrayList<ResourceItemCommonData> forUpdateItemCommonList2 = new ArrayList<ResourceItemCommonData>() {
			{
				item2_1.getItemCommonData().setForUpdate(true);
				item2_2.getItemCommonData().setForUpdate(true);

				add(item2_1.getItemCommonData());
				add(item2_2.getItemCommonData());
			}
		};

		new NonStrictExpectations() {
			{
				setField(target, syncConfiguration);
				setField(target, resourceManager);
				setField(target, repository);

				syncConfiguration.generateSyncTime();
				result = syncTime;

				// getByQuery

				resourceManager.locateSyncResource("resource1");
				result = resource1;
				resourceManager.locateSyncResource("resource2");
				result = resource2;

				resource1.getByQuery(requestCommon, resource1QueryConditions1);
				result = new ArrayList<ResourceItemWrapper<?>>() {
					{
						add(item1_1);
						add(item1_2);
					}
				};

				resource2.getByQuery(requestCommon, resource2QueryConditions1);
				result = new ArrayList<ResourceItemWrapper<?>>() {
					{
						add(item2_1);
						add(item2_2);
					}
				};
				resource2.getByQuery(requestCommon, resource2QueryConditions1);
				result = new ArrayList<ResourceItemWrapper<?>>() {
					{
						add(item2_2);
					}
				};

				syncConfiguration.downloadControl();
				result = DownloadControlType.READ_LOCK;

				// forUpdate(リストの中身の順番によらずresultを返す)

				resource1.forUpdate(new ArrayList<ResourceItemCommonData>() {
					{
						add(item1_1.getItemCommonData());
						add(item1_2.getItemCommonData());
					}
				});
				result = forUpdateItemCommonList1;
				resource1.forUpdate(new ArrayList<ResourceItemCommonData>() {
					{
						add(item1_2.getItemCommonData());
						add(item1_1.getItemCommonData());
					}
				});
				result = forUpdateItemCommonList1;

				resource2.forUpdate(new ArrayList<ResourceItemCommonData>() {
					{
						add(item2_1.getItemCommonData());
						add(item2_2.getItemCommonData());
					}
				});
				result = forUpdateItemCommonList2;
				resource2.forUpdate(new ArrayList<ResourceItemCommonData>() {
					{
						add(item2_2.getItemCommonData());
						add(item2_1.getItemCommonData());
					}
				});
				result = forUpdateItemCommonList2;

				// get

				resource1.get(requestCommon, forUpdateItemCommonList1);
				result = new ArrayList<ResourceItemWrapper<?>>() {
					{
						add(item1_1);
						add(item1_2);
					}
				};

				resource2.get(requestCommon, forUpdateItemCommonList2);
				result = new ArrayList<ResourceItemWrapper<?>>() {
					{
						add(item2_1);
						add(item2_2);
					}
				};

				// response

				syncConfiguration.bufferTimeForDownload();
				result = bufferTimeForDownload;
			}
		};

		// Act
		DownloadResponse actual = target.download(request);

		new VerificationsInOrder() {
			{
				syncConfiguration.generateSyncTime();

			}
		};
		new Verifications() {
			{
				resourceManager.locateSyncResource("resource2");
				resource2.getByQuery(requestCommon, (ResourceQueryConditions) any);
				resource2.getByQuery(requestCommon, (ResourceQueryConditions) any);

				resourceManager.locateSyncResource("resource1");
				resource1.getByQuery(requestCommon, resource1QueryConditions1);

			}
		};
		new VerificationsInOrder() {
			{
				syncConfiguration.downloadControl();

			}
		};
		new VerificationsInOrder() {
			{
				resourceManager.locateSyncResource("resource1");
				resource1.forUpdate((ArrayList<ResourceItemCommonData>) any);

				resourceManager.locateSyncResource("resource2");
				resource2.forUpdate((ArrayList<ResourceItemCommonData>) any);

			}
		};
		new Verifications() {
			{
				resourceManager.locateSyncResource("resource2");
				resource2.get(requestCommon, new ArrayList<ResourceItemCommonData>() {
					{
						add(item2_1.getItemCommonData());
						add(item2_2.getItemCommonData());
					}
				});

				resourceManager.locateSyncResource("resource1");
				resource1.get(requestCommon, new ArrayList<ResourceItemCommonData>() {
					{
						add(item1_1.getItemCommonData());
						add(item1_2.getItemCommonData());
					}
				});

			}
		};
		new VerificationsInOrder() {
			{
				syncConfiguration.bufferTimeForDownload();
			}
		};

		// Assert：結果が正しいこと
		DownloadCommonData expectedCommon = new DownloadCommonData("storageId") {
			{
				setLastDownloadTime(3000);
			}
		};

		DownloadResponse expected = new DownloadResponse(expectedCommon) {
			{
				setResourceItems(new HashMap<String, List<ResourceItemWrapper<?>>>() {
					{
						put("resource1", new ArrayList<ResourceItemWrapper<?>>() {
							{
								add(item1_1);
								add(item1_2);
							}
						});
						put("resource2", new ArrayList<ResourceItemWrapper<?>>() {
							{
								add(item2_1);
								add(item2_2);
							}
						});
					}
				});
			}
		};

		assertThat(actual.getDownloadCommonData(), is(equalTo(expected.getDownloadCommonData())));
		assertThat(actual.getResourceItems().size(), is(equalTo(2)));

		assertThat(actual.getResourceItems().get("resource1").size(), is(equalTo(2)));
		assertThat(actual.getResourceItems().get("resource1").contains(item1_1), is(true));
		assertThat(actual.getResourceItems().get("resource1").contains(item1_2), is(true));

		assertThat(actual.getResourceItems().get("resource2").size(), is(equalTo(2)));
		assertThat(actual.getResourceItems().get("resource2").contains(item2_1), is(true));
		assertThat(actual.getResourceItems().get("resource2").contains(item2_2), is(true));
	}

	/**
	 * {@link DefaultSynchronizer#download(DownloadRequest)}用テストメソッド.<br>
	 * クエリが空の場合、空の結果が返る.
	 */
	@Test
	public void testDownloadNothingOnREAD_LOCK() {

		// Arrange：正常系
		final DefaultSynchronizer target = new DefaultSynchronizer();

		final DownloadCommonData requestCommon = new DownloadCommonData("storageId");

		// クエリを格納しない
		final DownloadRequest request = new DownloadRequest() {
			{
				setDownloadCommonData(requestCommon);
				setQueries(new HashMap<String, List<ResourceQueryConditions>>());
			}
		};

		final long syncTime = 4000;
		final int bufferTimeForDownload = 1;

		new Expectations() {
			{
				setField(target, syncConfiguration);
				setField(target, resourceManager);
				setField(target, repository);

				syncConfiguration.generateSyncTime();
				result = syncTime;

				syncConfiguration.downloadControl();
				result = DownloadControlType.READ_LOCK;

				// response

				syncConfiguration.bufferTimeForDownload();
				result = bufferTimeForDownload;
			}
		};

		// Act
		DownloadResponse actual = target.download(request);

		// Assert：結果が正しいこと
		DownloadCommonData expectedCommon = new DownloadCommonData("storageId") {
			{
				setLastDownloadTime(3000);
			}
		};

		DownloadResponse expected = new DownloadResponse(expectedCommon) {
			{
				setResourceItems(new HashMap<String, List<ResourceItemWrapper<?>>>());
			}
		};

		assertThat(actual.getDownloadCommonData(), is(equalTo(expected.getDownloadCommonData())));
		assertThat(actual.getResourceItems().isEmpty(), is(true));
	}

	/**
	 * {@link DefaultSynchronizer#download(DownloadRequest)}用テストメソッド.<br>
	 * 下り更新共通データがnullの時{@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testDownloadFailBecauseOfNullDownloadCommonData() {

		// Arrange：正常系
		final DefaultSynchronizer target = new DefaultSynchronizer();

		final DownloadRequest request = new DownloadRequest() {
			{
				setDownloadCommonData(null);
			}
		};

		new Expectations() {
			{
				setField(target, syncConfiguration);
				setField(target, resourceManager);
				setField(target, repository);
			}
		};

		// Act
		target.download(request);

		// Assert：例外でなければ失敗
		fail();
	}

	/**
	 * {@link DefaultSynchronizer#download(DownloadRequest)}用テストメソッド.<br>
	 * リソースクエリがnullの時{@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testDownloadFailBecauseOfNullQuery() {

		// Arrange：正常系
		final DefaultSynchronizer target = new DefaultSynchronizer();

		final DownloadRequest request = new DownloadRequest() {
			{
				setDownloadCommonData(new DownloadCommonData("storageId"));
			}
		};

		new Expectations() {
			{
				setField(target, syncConfiguration);
				setField(target, resourceManager);
				setField(target, repository);
			}
		};

		// Act
		target.download(request);

		fail();
	}

	/**
	 * {@link DefaultSynchronizer#download(DownloadRequest)}用テストメソッド.<br>
	 * リソース名に対応するリソースが見つからない場合、{@link NoSuchResourceException}がスローされる.
	 */
	@Test(expected = NoSuchResourceException.class)
	public void testDownloadFailBecauseOfUnknownResource() {

		// Arrange：異常系
		final DefaultSynchronizer target = new DefaultSynchronizer();

		final DownloadCommonData requestCommon = new DownloadCommonData("storageId");

		final ResourceQueryConditions resource1QueryConditions1 = new ResourceQueryConditions() {
			{
				setLastDownloadTime(1000);
				setConditions(new HashMap<String, String[]>() {
					{
						put("id", new String[] { "a", "b" });
						put("no", new String[] { "1" });
					}
				});
			}
		};

		final DownloadRequest request = new DownloadRequest() {
			{
				setDownloadCommonData(requestCommon);
				setQueries(new HashMap<String, List<ResourceQueryConditions>>() {
					{
						put("unknown", new ArrayList<ResourceQueryConditions>() {
							{
								add(resource1QueryConditions1);
							}
						});
					}
				});
			}
		};

		final long syncTime = 4000;

		new NonStrictExpectations() {
			{
				setField(target, syncConfiguration);
				setField(target, resourceManager);
				setField(target, repository);

				syncConfiguration.generateSyncTime();
				result = syncTime;

				resourceManager.locateSyncResource("unknown");
				result = null;
			}
		};

		// Act
		target.download(request);

		fail();
	}

	/**
	 * {@link DefaultSynchronizer#upload(UploadRequest)}用テストメソッド.
	 */
	@Test
	public void testUpload() {

		// Arrange：正常系
		final DefaultSynchronizer target = new DefaultSynchronizer();

		final String storageId = "storageId";

		final UploadCommonData requestCommon = new UploadCommonData() {
			{
				setStorageId(storageId);
				setLastUploadTime(1000);
			}
		};

		final UploadCommonData responseCommon = new UploadCommonData() {
			{
				setStorageId(storageId);
			}
		};

		final ResourceItemWrapper<? extends Map<String, Object>> item1_1_1 = createUploadItemWrapper("1", "1",
				SyncAction.CREATE);
		final ResourceItemWrapper<? extends Map<String, Object>> item2_1 = createUploadItemWrapper("2", "1",
				SyncAction.UPDATE);
		// リクエスト内に同じアイテムの存在が許容される
		final ResourceItemWrapper<? extends Map<String, Object>> item1_1_2 = createUploadItemWrapper("1", "1",
				SyncAction.DELETE);

		final UploadRequest request = new UploadRequest() {
			{
				setUploadCommonData(requestCommon);
				setResourceItems(new ArrayList<ResourceItemWrapper<? extends Map<String, Object>>>() {
					{
						add(item1_1_1);
						add(item2_1);
						add(item1_1_2);
					}
				});
			}
		};

		final long syncTime = 4000;

		final ResourceItemConverter<Object> itemConverter = getTestConverter();

		new Expectations() {
			{
				setField(target, syncConfiguration);
				setField(target, resourceManager);
				setField(target, repository);

				syncConfiguration.generateSyncTime();
				result = syncTime;

				repository.findOne(storageId);
				result = responseCommon;

				syncConfiguration.uploadControl();
				result = UploadControlType.NONE;

				syncConfiguration.uploadControl();
				result = UploadControlType.NONE;

				// resource1(1)

				resourceManager.locateSyncResource("resource1");
				result = resource1;

				resource1.itemConverter();
				result = itemConverter;

				resource1.itemType();
				result = Object.class;

				// item1_1_1

				resource1.create(requestCommon, item1_1_1.getItemCommonData(),
						itemConverter.convertToItem(item1_1_1.getItem(), Object.class));
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemWrapper<Object> create(UploadCommonData uploadCommon,
							ResourceItemCommonData itemCommon, Object item) {
						itemCommon.setConflictType(SyncConflictType.NONE);

						return new ResourceItemWrapper<Object>(itemCommon, item);
					}
				};

				// resource2

				resourceManager.locateSyncResource("resource2");
				result = resource2;

				resource2.itemConverter();
				result = itemConverter;

				resource2.itemType();
				result = Object.class;

				// item2_1

				resource2.update(requestCommon, item2_1.getItemCommonData(),
						itemConverter.convertToItem(item2_1.getItem(), Object.class));
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemWrapper<Object> update(UploadCommonData uploadCommon,
							ResourceItemCommonData itemCommon, Object item) {
						itemCommon.setConflictType(SyncConflictType.NONE);

						return new ResourceItemWrapper<Object>(itemCommon, item);
					}
				};

				// resource1(2)

				resourceManager.locateSyncResource("resource1");
				result = resource1;

				// item1_1_2

				resource1.delete(requestCommon, item1_1_2.getItemCommonData());
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemWrapper<Object> delete(UploadCommonData uploadCommon, ResourceItemCommonData itemCommon) {
						itemCommon.setConflictType(SyncConflictType.NONE);

						return new ResourceItemWrapper<Object>(itemCommon, null);
					}
				};

				// result

				repository.save(responseCommon);
				result = responseCommon;
			}
		};

		// Act
		UploadResponse actual = target.upload(request);

		// Assert：結果が正しいこと
		UploadResponse expected = new UploadResponse(responseCommon);

		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * {@link DefaultSynchronizer#upload(UploadRequest)}用テストメソッド.<br>
	 * 前回上り更新時刻が0(各ストレージIDごとに初回)の場合は、当然二重送信にはならない.
	 */
	@Test
	public void testUploadFirst() {

		// Arrange：正常系
		final DefaultSynchronizer target = new DefaultSynchronizer();

		final String storageId = "storageId";

		final UploadCommonData requestCommon = new UploadCommonData() {
			{
				setStorageId(storageId);
				setLastUploadTime(0);
			}
		};

		final ResourceItemWrapper<? extends Map<String, Object>> item1 = createUploadItemWrapper("1", "1",
				SyncAction.CREATE);

		final UploadRequest request = new UploadRequest() {
			{
				setUploadCommonData(requestCommon);
				setResourceItems(new ArrayList<ResourceItemWrapper<? extends Map<String, Object>>>() {
					{
						add(item1);
					}
				});
			}
		};

		final long syncTime = 4000;

		final ResourceItemConverter<Object> itemConverter = getTestConverter();

		final UploadCommonData responseCommon = new UploadCommonData() {
			{
				setStorageId(storageId);
				setLastUploadTime(syncTime);
				setConflictType(SyncConflictType.NONE);
			}
		};

		new Expectations() {
			{
				setField(target, syncConfiguration);
				setField(target, resourceManager);
				setField(target, repository);

				syncConfiguration.generateSyncTime();
				result = syncTime;

				repository.findOne(storageId);
				result = null;

				syncConfiguration.uploadControl();
				result = UploadControlType.NONE;

				syncConfiguration.uploadControl();
				result = UploadControlType.NONE;

				// resource1(1)

				resourceManager.locateSyncResource("resource1");
				result = resource1;

				resource1.itemConverter();
				result = itemConverter;

				resource1.itemType();
				result = Object.class;

				// item1

				resource1.create(requestCommon, item1.getItemCommonData(),
						itemConverter.convertToItem(item1.getItem(), Object.class));
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemWrapper<Object> create(UploadCommonData uploadCommon,
							ResourceItemCommonData itemCommon, Object item) {
						itemCommon.setConflictType(SyncConflictType.NONE);

						return new ResourceItemWrapper<Object>(itemCommon, item);
					}
				};

				// result

				repository.save(responseCommon);
				result = responseCommon;
			}
		};

		// Act
		UploadResponse actual = target.upload(request);

		// Assert：結果が正しいこと
		UploadResponse expected = new UploadResponse(responseCommon);

		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * {@link DefaultSynchronizer#upload(UploadRequest)}用テストメソッド.<br>
	 * {@link UploadControlType#AVOID_DEADLOCK}が設定されている場合、順序をソートして実行する.
	 */
	@Test
	public void testUploadOnAVOID_DEADLOCK() {

		// Arrange：正常系
		final DefaultSynchronizer target = new DefaultSynchronizer();

		final String storageId = "storageId";

		final UploadCommonData requestCommon = new UploadCommonData() {
			{
				setStorageId(storageId);
				setLastUploadTime(1000);
			}
		};

		final UploadCommonData responseCommon = new UploadCommonData() {
			{
				setStorageId(storageId);
			}
		};

		final ResourceItemWrapper<? extends Map<String, Object>> item1_1_1 = createUploadItemWrapper("1", "1",
				SyncAction.CREATE);
		final ResourceItemWrapper<? extends Map<String, Object>> item2_1 = createUploadItemWrapper("2", "1",
				SyncAction.UPDATE);
		// リクエスト内に同じアイテムの存在が許容される
		final ResourceItemWrapper<? extends Map<String, Object>> item1_1_2 = createUploadItemWrapper("1", "1",
				SyncAction.DELETE);

		final UploadRequest request = new UploadRequest() {
			{
				setUploadCommonData(requestCommon);
				setResourceItems(new ArrayList<ResourceItemWrapper<? extends Map<String, Object>>>() {
					{
						// ソート前
						add(item1_1_1);
						add(item2_1);
						add(item1_1_2);
					}
				});
			}
		};

		final long syncTime = 4000;

		final ResourceItemConverter<Object> itemConverter = getTestConverter();

		new Expectations() {
			{
				setField(target, syncConfiguration);
				setField(target, resourceManager);
				setField(target, repository);

				syncConfiguration.generateSyncTime();
				result = syncTime;

				repository.findOne(storageId);
				result = responseCommon;

				syncConfiguration.uploadControl();
				result = UploadControlType.AVOID_DEADLOCK;

				syncConfiguration.uploadControl();
				result = UploadControlType.AVOID_DEADLOCK;

				// resource1(1)

				resourceManager.locateSyncResource("resource1");
				result = resource1;

				resource1.itemConverter();
				result = itemConverter;

				resource1.itemType();
				result = Object.class;

				// item1_1_1

				resource1.create(requestCommon, item1_1_1.getItemCommonData(),
						itemConverter.convertToItem(item1_1_1.getItem(), Object.class));
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemWrapper<Object> create(UploadCommonData uploadCommon,
							ResourceItemCommonData itemCommon, Object item) {
						itemCommon.setConflictType(SyncConflictType.NONE);

						return new ResourceItemWrapper<Object>(itemCommon, item);
					}
				};

				// resource1(2)

				resourceManager.locateSyncResource("resource1");
				result = resource1;

				// item1_1_2

				resource1.delete(requestCommon, item1_1_2.getItemCommonData());
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemWrapper<Object> delete(UploadCommonData uploadCommon, ResourceItemCommonData itemCommon) {
						itemCommon.setConflictType(SyncConflictType.NONE);

						return new ResourceItemWrapper<Object>(itemCommon, null);
					}
				};

				// resource2

				resourceManager.locateSyncResource("resource2");
				result = resource2;

				resource2.itemConverter();
				result = itemConverter;

				resource2.itemType();
				result = Object.class;

				// item2_1

				resource2.update(requestCommon, item2_1.getItemCommonData(),
						itemConverter.convertToItem(item2_1.getItem(), Object.class));
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemWrapper<Object> update(UploadCommonData uploadCommon,
							ResourceItemCommonData itemCommon, Object item) {
						itemCommon.setConflictType(SyncConflictType.NONE);

						return new ResourceItemWrapper<Object>(itemCommon, item);
					}
				};

				// result

				repository.save(responseCommon);
				result = responseCommon;
			}
		};

		// Act
		UploadResponse actual = target.upload(request);

		// Assert：結果が正しいこと
		UploadResponse expected = new UploadResponse(responseCommon);

		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * {@link DefaultSynchronizer#upload(UploadRequest)}用テストメソッド. <br>
	 * {@link UploadControlType#RESERVE}が設定されている場合、順序をソートして {@link SyncResource#forUpdate(List)}を行った後に更新を行う.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testUploadOnRESERVE() {

		// Arrange：正常系
		final DefaultSynchronizer target = new DefaultSynchronizer();

		final String storageId = "storageId";

		final UploadCommonData requestCommon = new UploadCommonData() {
			{
				setStorageId(storageId);
				setLastUploadTime(1000);
			}
		};

		final UploadCommonData responseCommon = new UploadCommonData() {
			{
				setStorageId(storageId);
			}
		};

		final ResourceItemWrapper<? extends Map<String, Object>> item1_1_1 = createUploadItemWrapper("1", "1",
				SyncAction.CREATE);
		final ResourceItemWrapper<? extends Map<String, Object>> item2_1 = createUploadItemWrapper("2", "1",
				SyncAction.UPDATE);
		// リクエスト内に同じアイテムの存在が許容される
		final ResourceItemWrapper<? extends Map<String, Object>> item1_1_2 = createUploadItemWrapper("1", "1",
				SyncAction.DELETE);

		final UploadRequest request = new UploadRequest() {
			{
				setUploadCommonData(requestCommon);
				setResourceItems(new ArrayList<ResourceItemWrapper<? extends Map<String, Object>>>() {
					{
						add(item1_1_1);
						add(item2_1);
						add(item1_1_2);
					}
				});
			}
		};

		final ArrayList<ResourceItemCommonData> forUpdateItemCommonList1 = new ArrayList<ResourceItemCommonData>() {
			{
				item1_1_1.getItemCommonData().setForUpdate(true);
				item1_1_2.getItemCommonData().setForUpdate(true);

				add(item1_1_1.getItemCommonData());
				add(item1_1_2.getItemCommonData());
			}
		};

		final ArrayList<ResourceItemCommonData> forUpdateItemCommonList2 = new ArrayList<ResourceItemCommonData>() {
			{
				item2_1.getItemCommonData().setForUpdate(true);

				add(item2_1.getItemCommonData());
			}
		};

		final long syncTime = 4000;

		final ResourceItemConverter<Object> itemConverter = getTestConverter();

		new NonStrictExpectations() {
			{
				setField(target, syncConfiguration);
				setField(target, resourceManager);
				setField(target, repository);

				syncConfiguration.generateSyncTime();
				result = syncTime;

				repository.findOne(storageId);
				result = responseCommon;

				syncConfiguration.uploadControl();
				result = UploadControlType.RESERVE;

				// forUpdate

				resourceManager.locateSyncResource("resource1");
				result = resource1;

				// リスト内がどちらの順序でも同じリストが返るという期待動作
				resource1.forUpdate(new ArrayList<ResourceItemCommonData>() {
					{
						add(item1_1_1.getItemCommonData());
						add(item1_1_2.getItemCommonData());
					}
				});
				result = forUpdateItemCommonList1;
				resource1.forUpdate(new ArrayList<ResourceItemCommonData>() {
					{
						add(item1_1_2.getItemCommonData());
						add(item1_1_1.getItemCommonData());
					}
				});
				result = forUpdateItemCommonList1;

				resourceManager.locateSyncResource("resource2");
				result = resource2;

				resource2.forUpdate(new ArrayList<ResourceItemCommonData>() {
					{
						add(item2_1.getItemCommonData());
					}
				});
				result = forUpdateItemCommonList2;

				syncConfiguration.uploadControl();
				result = UploadControlType.RESERVE;

				// resource1(1)

				resourceManager.locateSyncResource("resource1");
				result = resource1;

				resource1.itemConverter();
				result = itemConverter;

				resource1.itemType();
				result = Object.class;

				// item1_1_1

				resource1.create(requestCommon, item1_1_1.getItemCommonData(),
						itemConverter.convertToItem(item1_1_1.getItem(), Object.class));
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemWrapper<Object> create(UploadCommonData uploadCommon,
							ResourceItemCommonData itemCommon, Object item) {
						itemCommon.setConflictType(SyncConflictType.NONE);

						return new ResourceItemWrapper<Object>(itemCommon, item);
					}
				};

				// resource2

				resourceManager.locateSyncResource("resource2");
				result = resource2;

				resource2.itemConverter();
				result = itemConverter;

				resource2.itemType();
				result = Object.class;

				// item2_1

				resource2.update(requestCommon, item2_1.getItemCommonData(),
						itemConverter.convertToItem(item2_1.getItem(), Object.class));
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemWrapper<Object> update(UploadCommonData uploadCommon,
							ResourceItemCommonData itemCommon, Object item) {
						itemCommon.setConflictType(SyncConflictType.NONE);

						return new ResourceItemWrapper<Object>(itemCommon, item);
					}
				};

				// resource1(2)

				resourceManager.locateSyncResource("resource1");
				result = resource1;

				// item1_1_2

				resource1.delete(requestCommon, item1_1_2.getItemCommonData());
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemWrapper<Object> delete(UploadCommonData uploadCommon, ResourceItemCommonData itemCommon) {
						itemCommon.setConflictType(SyncConflictType.NONE);

						return new ResourceItemWrapper<Object>(itemCommon, null);
					}
				};

				// result

				repository.save(responseCommon);
				result = responseCommon;
			}
		};

		// Act
		UploadResponse actual = target.upload(request);

		new FullVerificationsInOrder() {
			{
				setField(target, syncConfiguration);
				setField(target, resourceManager);
				setField(target, repository);

				syncConfiguration.generateSyncTime();

				repository.findOne(storageId);

				syncConfiguration.uploadControl();

				// forUpdate

				resourceManager.locateSyncResource("resource1");
				resource1.forUpdate((List<ResourceItemCommonData>) any);

				resourceManager.locateSyncResource("resource2");
				resource2.forUpdate(new ArrayList<ResourceItemCommonData>() {
					{
						add(item2_1.getItemCommonData());
					}
				});

				syncConfiguration.uploadControl();

				// resource1(1)

				resourceManager.locateSyncResource("resource1");

				resource1.itemConverter();

				resource1.itemType();

				// item1_1_1

				resource1.create(requestCommon, item1_1_1.getItemCommonData(),
						itemConverter.convertToItem(item1_1_1.getItem(), Object.class));

				// resource2

				resourceManager.locateSyncResource("resource2");

				resource2.itemConverter();

				resource2.itemType();

				// item2_1

				resource2.update(requestCommon, item2_1.getItemCommonData(),
						itemConverter.convertToItem(item2_1.getItem(), Object.class));

				// resource1(2)

				resourceManager.locateSyncResource("resource1");

				// item1_1_2

				resource1.delete(requestCommon, item1_1_2.getItemCommonData());

				// result

				repository.save(responseCommon);
			}
		};

		// Assert：結果が正しいこと
		UploadResponse expected = new UploadResponse(responseCommon);

		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * {@link DefaultSynchronizer#upload(UploadRequest)}用テストメソッド.<br>
	 * 二重送信が検知された場合、処理を行わず前回のUploadCommonDataを含むレスポンスを返す.
	 */
	@Test
	public void testUploadDuplicate() {

		// Arrange：例外系
		final DefaultSynchronizer target = new DefaultSynchronizer();

		final String storageId = "storageId";

		final UploadCommonData requestCommon = new UploadCommonData() {
			{
				setStorageId(storageId);
				setLastUploadTime(1000);
			}
		};

		final UploadCommonData lastCommon = new UploadCommonData() {
			{
				setStorageId(storageId);
				setLastUploadTime(2000);
			}
		};

		final UploadRequest request = new UploadRequest() {
			{
				setUploadCommonData(requestCommon);
				setResourceItems(new ArrayList<ResourceItemWrapper<? extends Map<String, Object>>>() {
					{
						// any
					}
				});
			}
		};

		final long syncTime = 4000;

		new Expectations() {
			{
				setField(target, syncConfiguration);
				setField(target, resourceManager);
				setField(target, repository);

				syncConfiguration.generateSyncTime();
				result = syncTime;

				repository.findOne(storageId);
				result = lastCommon;
			}
		};

		// Act
		UploadResponse actual = target.upload(request);

		// Assert：結果が正しいこと
		UploadResponse expected = new UploadResponse(lastCommon);

		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * {@link DefaultSynchronizer#upload(UploadRequest)}用テストメソッド.<br>
	 * 更新競合が発生し、更新競合時の継続設定がfalseのため、処理を終え、更新競合データが含まれた例外がスローされる.
	 */
	@Test(expected = ConflictException.class)
	public void testUploadUpdatedConflictAndEnd() {

		// Arrange：正常系
		final DefaultSynchronizer target = new DefaultSynchronizer();

		final String storageId = "storageId";

		final UploadCommonData requestCommon = new UploadCommonData() {
			{
				setStorageId(storageId);
				setLastUploadTime(1000);
			}
		};

		final UploadCommonData responseCommon = new UploadCommonData() {
			{
				setStorageId(storageId);
			}
		};

		final ResourceItemWrapper<? extends Map<String, Object>> item1_0 = createUploadItemWrapper("1", "0",
				SyncAction.UPDATE);
		final ResourceItemWrapper<? extends Map<String, Object>> item1 = createUploadItemWrapper("1", "1",
				SyncAction.UPDATE);
		final ResourceItemWrapper<? extends Map<String, Object>> item2 = createUploadItemWrapper("2", "1",
				SyncAction.UPDATE);

		final UploadRequest request = new UploadRequest() {
			{
				setUploadCommonData(requestCommon);
				setResourceItems(new ArrayList<ResourceItemWrapper<? extends Map<String, Object>>>() {
					{
						add(item1_0);
						add(item1);
						add(item2);
					}
				});
			}
		};

		final long syncTime = 4000;

		final ResourceItemConverter<Object> itemConverter = getTestConverter();

		final Object convertedItem1_0 = itemConverter.convertToItem(item1_0.getItem(), Object.class);
		final Object convertedItem1 = itemConverter.convertToItem(item1.getItem(), Object.class);

		new Expectations() {
			{
				setField(target, syncConfiguration);
				setField(target, resourceManager);
				setField(target, repository);

				syncConfiguration.generateSyncTime();
				result = syncTime;

				repository.findOne(storageId);
				result = responseCommon;

				syncConfiguration.uploadControl();
				result = UploadControlType.NONE;

				syncConfiguration.uploadControl();
				result = UploadControlType.NONE;

				// resource1(0)

				resourceManager.locateSyncResource("resource1");
				result = resource1;

				resource1.itemConverter();
				result = itemConverter;

				resource1.itemType();
				result = Object.class;

				// item1_0

				resource1.update(requestCommon, item1_0.getItemCommonData(), convertedItem1_0);
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemWrapper<Object> update(UploadCommonData uploadCommon,
							ResourceItemCommonData itemCommon, Object item) {
						itemCommon.setConflictType(SyncConflictType.NONE);

						return new ResourceItemWrapper<Object>(itemCommon, item);
					}
				};

				// resource1(1)

				resourceManager.locateSyncResource("resource1");
				result = resource1;

				resource1.itemConverter();
				result = itemConverter;

				resource1.itemType();
				result = Object.class;

				// item1

				resource1.update(requestCommon, item1.getItemCommonData(), convertedItem1);
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemWrapper<Object> update(UploadCommonData uploadCommon,
							ResourceItemCommonData itemCommon, Object item) {
						itemCommon.setConflictType(SyncConflictType.UPDATED);

						return new ResourceItemWrapper<Object>(itemCommon, item);
					}
				};

				syncConfiguration.isContinueOnConflictOfUpdated();
				result = false;
			}
		};

		// Act
		try {
			target.upload(request);

			fail();
		} catch (ConflictException e) {

			// Assert：結果が正しいこと
			UploadCommonData expectedResponseCommonData = new UploadCommonData() {
				{
					setConflictType(SyncConflictType.UPDATED);
				}
			};

			final HashMap<String, List<ResourceItemWrapper<?>>> expectedResourceItems = new HashMap<String, List<ResourceItemWrapper<?>>>() {
				{
					put("resource1", new ArrayList<ResourceItemWrapper<?>>() {
						{
							add(new ResourceItemWrapper<>(item1.getItemCommonData(), convertedItem1));
						}
					});
				}
			};

			UploadResponse expected = new UploadResponse(expectedResponseCommonData) {
				{
					setResourceItems(expectedResourceItems);
				}
			};

			assertThat(e.getResponse(), is(equalTo(expected)));

			throw e;
		}
	}

	/**
	 * {@link DefaultSynchronizer#upload(UploadRequest)}用テストメソッド.<br>
	 * 更新競合が発生し、更新競合時の継続設定がtrueのため、全ての更新競合データが含まれた例外がスローされる.
	 */
	@Test(expected = ConflictException.class)
	public void testUploadUpdatedConflictAndContinue() {

		// Arrange：正常系
		final DefaultSynchronizer target = new DefaultSynchronizer();

		final String storageId = "storageId";

		final UploadCommonData requestCommon = new UploadCommonData() {
			{
				setStorageId(storageId);
				setLastUploadTime(1000);
			}
		};

		final UploadCommonData responseCommon = new UploadCommonData() {
			{
				setStorageId(storageId);
			}
		};

		final ResourceItemWrapper<? extends Map<String, Object>> item1 = createUploadItemWrapper("1", "1",
				SyncAction.UPDATE);
		final ResourceItemWrapper<? extends Map<String, Object>> item2 = createUploadItemWrapper("2", "1",
				SyncAction.UPDATE);
		final ResourceItemWrapper<? extends Map<String, Object>> item2_2 = createUploadItemWrapper("2", "2",
				SyncAction.UPDATE);

		final UploadRequest request = new UploadRequest() {
			{
				setUploadCommonData(requestCommon);
				setResourceItems(new ArrayList<ResourceItemWrapper<? extends Map<String, Object>>>() {
					{
						add(item1);
						add(item2);
						add(item2_2);
					}
				});
			}
		};

		final long syncTime = 4000;

		final ResourceItemConverter<Object> itemConverter = getTestConverter();

		final Object convertedItem1 = itemConverter.convertToItem(item1.getItem(), Object.class);
		final Object convertedItem2 = itemConverter.convertToItem(item2.getItem(), Object.class);
		final Object convertedItem2_2 = itemConverter.convertToItem(item2_2.getItem(), Object.class);

		new Expectations() {
			{
				setField(target, syncConfiguration);
				setField(target, resourceManager);
				setField(target, repository);

				syncConfiguration.generateSyncTime();
				result = syncTime;

				repository.findOne(storageId);
				result = responseCommon;

				syncConfiguration.uploadControl();
				result = UploadControlType.NONE;

				syncConfiguration.uploadControl();
				result = UploadControlType.NONE;

				// resource1(1)

				resourceManager.locateSyncResource("resource1");
				result = resource1;

				resource1.itemConverter();
				result = itemConverter;

				resource1.itemType();
				result = Object.class;

				// item1

				resource1.update(requestCommon, item1.getItemCommonData(), convertedItem1);
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemWrapper<Object> update(UploadCommonData uploadCommon,
							ResourceItemCommonData itemCommon, Object item) {
						itemCommon.setConflictType(SyncConflictType.UPDATED);

						return new ResourceItemWrapper<Object>(itemCommon, item);
					}
				};

				syncConfiguration.isContinueOnConflictOfUpdated();
				result = true;

				// resource2(1)

				resourceManager.locateSyncResource("resource2");
				result = resource2;

				resource2.itemConverter();
				result = itemConverter;

				resource2.itemType();
				result = Object.class;

				// item2

				resource2.update(requestCommon, item2.getItemCommonData(), convertedItem2);
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemWrapper<Object> update(UploadCommonData uploadCommon,
							ResourceItemCommonData itemCommon, Object item) {
						itemCommon.setConflictType(SyncConflictType.UPDATED);

						return new ResourceItemWrapper<Object>(itemCommon, item);
					}
				};

				syncConfiguration.isContinueOnConflictOfUpdated();
				result = true;

				// resource2(2)

				resourceManager.locateSyncResource("resource2");
				result = resource2;

				resource2.itemConverter();
				result = itemConverter;

				resource2.itemType();
				result = Object.class;

				// item2_2

				resource2.update(requestCommon, item2_2.getItemCommonData(), convertedItem2_2);
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemWrapper<Object> update(UploadCommonData uploadCommon,
							ResourceItemCommonData itemCommon, Object item) {
						itemCommon.setConflictType(SyncConflictType.NONE);

						return new ResourceItemWrapper<Object>(itemCommon, item);
					}
				};

				syncConfiguration.isContinueOnConflictOfUpdated();
				result = true;
			}
		};

		// Act
		try {
			target.upload(request);

			fail();
		} catch (ConflictException e) {

			// Assert：結果が正しいこと
			UploadCommonData expectedResponseCommonData = new UploadCommonData() {
				{
					setConflictType(SyncConflictType.UPDATED);
				}
			};

			final HashMap<String, List<ResourceItemWrapper<?>>> expectedResourceItems = new HashMap<String, List<ResourceItemWrapper<?>>>() {
				{
					put("resource1", new ArrayList<ResourceItemWrapper<?>>() {
						{
							add(new ResourceItemWrapper<>(item1.getItemCommonData(), convertedItem1));
						}
					});
					put("resource2", new ArrayList<ResourceItemWrapper<?>>() {
						{
							add(new ResourceItemWrapper<>(item2.getItemCommonData(), convertedItem2));
						}
					});
				}
			};

			UploadResponse expected = new UploadResponse(expectedResponseCommonData) {
				{
					setResourceItems(expectedResourceItems);
				}
			};

			assertThat(e.getResponse(), is(equalTo(expected)));

			throw e;
		}
	}

	/**
	 * {@link DefaultSynchronizer#upload(UploadRequest)}用テストメソッド.<br>
	 * キー重複競合が発生し、キー重複競合時の継続設定がfalseのため、処理を終え、キー重複競合データが含まれた例外がスローされる.
	 */
	@Test(expected = ConflictException.class)
	public void testUploadDuplicatedIdConflictAndEnd() {

		// Arrange：正常系
		final DefaultSynchronizer target = new DefaultSynchronizer();

		final String storageId = "storageId";

		final UploadCommonData requestCommon = new UploadCommonData() {
			{
				setStorageId(storageId);
				setLastUploadTime(1000);
			}
		};

		final UploadCommonData responseCommon = new UploadCommonData() {
			{
				setStorageId(storageId);
			}
		};

		final ResourceItemWrapper<? extends Map<String, Object>> item1_0 = createUploadItemWrapper("1", "0",
				SyncAction.UPDATE);
		final ResourceItemWrapper<? extends Map<String, Object>> item1 = createUploadItemWrapper("1", "1",
				SyncAction.CREATE);
		final ResourceItemWrapper<? extends Map<String, Object>> item2 = createUploadItemWrapper("2", "1",
				SyncAction.CREATE);

		final UploadRequest request = new UploadRequest() {
			{
				setUploadCommonData(requestCommon);
				setResourceItems(new ArrayList<ResourceItemWrapper<? extends Map<String, Object>>>() {
					{
						add(item1_0);
						add(item1);
						add(item2);
					}
				});
			}
		};

		final long syncTime = 4000;

		final ResourceItemConverter<Object> itemConverter = getTestConverter();

		final Object convertedItem1_0 = itemConverter.convertToItem(item1_0.getItem(), Object.class);
		final Object convertedItem1 = itemConverter.convertToItem(item1.getItem(), Object.class);

		new Expectations() {
			{
				setField(target, syncConfiguration);
				setField(target, resourceManager);
				setField(target, repository);

				syncConfiguration.generateSyncTime();
				result = syncTime;

				repository.findOne(storageId);
				result = responseCommon;

				syncConfiguration.uploadControl();
				result = UploadControlType.NONE;

				syncConfiguration.uploadControl();
				result = UploadControlType.NONE;

				// resource1(0)

				resourceManager.locateSyncResource("resource1");
				result = resource1;

				resource1.itemConverter();
				result = itemConverter;

				resource1.itemType();
				result = Object.class;

				// item1_0

				resource1.update(requestCommon, item1_0.getItemCommonData(), convertedItem1_0);
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemWrapper<Object> update(UploadCommonData uploadCommon,
							ResourceItemCommonData itemCommon, Object item) {
						itemCommon.setConflictType(SyncConflictType.UPDATED);

						return new ResourceItemWrapper<Object>(itemCommon, item);
					}
				};

				syncConfiguration.isContinueOnConflictOfUpdated();
				result = true;

				// resource1(1)

				resourceManager.locateSyncResource("resource1");
				result = resource1;

				resource1.itemConverter();
				result = itemConverter;

				resource1.itemType();
				result = Object.class;

				// item1

				resource1.create(requestCommon, item1.getItemCommonData(), convertedItem1);
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemWrapper<Object> create(UploadCommonData uploadCommon,
							ResourceItemCommonData itemCommon, Object item) {
						itemCommon.setConflictType(SyncConflictType.DUPLICATE_ID);

						return new ResourceItemWrapper<Object>(itemCommon, item);
					}
				};

				syncConfiguration.isContinueOnConflictOfDuplicateId();
				result = false;
			}
		};

		// Act
		try {
			target.upload(request);

			fail();
		} catch (ConflictException e) {

			// Assert：結果が正しいこと
			UploadCommonData expectedResponseCommonData = new UploadCommonData() {
				{
					setConflictType(SyncConflictType.DUPLICATE_ID);
				}
			};

			final HashMap<String, List<ResourceItemWrapper<?>>> expectedResourceItems = new HashMap<String, List<ResourceItemWrapper<?>>>() {
				{
					put("resource1", new ArrayList<ResourceItemWrapper<?>>() {
						{
							add(new ResourceItemWrapper<>(item1.getItemCommonData(), convertedItem1));
						}
					});
				}
			};

			UploadResponse expected = new UploadResponse(expectedResponseCommonData) {
				{
					setResourceItems(expectedResourceItems);
				}
			};

			assertThat(e.getResponse(), is(equalTo(expected)));

			throw e;
		}
	}

	/**
	 * {@link DefaultSynchronizer#upload(UploadRequest)}用テストメソッド.<br>
	 * キー重複競合が発生し、キー重複競合競合時の継続設定がtrueのため、全てのキー重複競合データが含まれた例外がスローされる.
	 */
	@Test(expected = ConflictException.class)
	public void testUploadDuplicatedIdConflictAndContinue() {

		// Arrange：正常系
		final DefaultSynchronizer target = new DefaultSynchronizer();

		final String storageId = "storageId";

		final UploadCommonData requestCommon = new UploadCommonData() {
			{
				setStorageId(storageId);
				setLastUploadTime(1000);
			}
		};

		final UploadCommonData responseCommon = new UploadCommonData() {
			{
				setStorageId(storageId);
			}
		};

		final ResourceItemWrapper<? extends Map<String, Object>> item1 = createUploadItemWrapper("1", "1",
				SyncAction.CREATE);
		final ResourceItemWrapper<? extends Map<String, Object>> item2 = createUploadItemWrapper("2", "1",
				SyncAction.CREATE);

		final UploadRequest request = new UploadRequest() {
			{
				setUploadCommonData(requestCommon);
				setResourceItems(new ArrayList<ResourceItemWrapper<? extends Map<String, Object>>>() {
					{
						add(item1);
						add(item2);
					}
				});
			}
		};

		final long syncTime = 4000;

		final ResourceItemConverter<Object> itemConverter = getTestConverter();

		final Object convertedItem1 = itemConverter.convertToItem(item1.getItem(), Object.class);
		final Object convertedItem2 = itemConverter.convertToItem(item2.getItem(), Object.class);

		new Expectations() {
			{
				setField(target, syncConfiguration);
				setField(target, resourceManager);
				setField(target, repository);

				syncConfiguration.generateSyncTime();
				result = syncTime;

				repository.findOne(storageId);
				result = responseCommon;

				syncConfiguration.uploadControl();
				result = UploadControlType.NONE;

				syncConfiguration.uploadControl();
				result = UploadControlType.NONE;

				// resource1(1)

				resourceManager.locateSyncResource("resource1");
				result = resource1;

				resource1.itemConverter();
				result = itemConverter;

				resource1.itemType();
				result = Object.class;

				// item1

				resource1.create(requestCommon, item1.getItemCommonData(), convertedItem1);
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemWrapper<Object> create(UploadCommonData uploadCommon,
							ResourceItemCommonData itemCommon, Object item) {
						itemCommon.setConflictType(SyncConflictType.DUPLICATE_ID);

						return new ResourceItemWrapper<Object>(itemCommon, item);
					}
				};

				syncConfiguration.isContinueOnConflictOfDuplicateId();
				result = true;

				// resource2

				resourceManager.locateSyncResource("resource2");
				result = resource2;

				resource2.itemConverter();
				result = itemConverter;

				resource2.itemType();
				result = Object.class;

				// item2

				resource2.create(requestCommon, item2.getItemCommonData(), convertedItem2);
				result = new Delegate() {
					@SuppressWarnings("unused")
					ResourceItemWrapper<Object> create(UploadCommonData uploadCommon,
							ResourceItemCommonData itemCommon, Object item) {
						itemCommon.setConflictType(SyncConflictType.DUPLICATE_ID);

						return new ResourceItemWrapper<Object>(itemCommon, item);
					}
				};

				syncConfiguration.isContinueOnConflictOfDuplicateId();
				result = true;
			}
		};

		// Act
		try {
			target.upload(request);

			fail();
		} catch (ConflictException e) {

			// Assert：結果が正しいこと
			UploadCommonData expectedResponseCommonData = new UploadCommonData() {
				{
					setConflictType(SyncConflictType.DUPLICATE_ID);
				}
			};

			final HashMap<String, List<ResourceItemWrapper<?>>> expectedResourceItems = new HashMap<String, List<ResourceItemWrapper<?>>>() {
				{
					put("resource1", new ArrayList<ResourceItemWrapper<?>>() {
						{
							add(new ResourceItemWrapper<>(item1.getItemCommonData(), convertedItem1));
						}
					});
					put("resource2", new ArrayList<ResourceItemWrapper<?>>() {
						{
							add(new ResourceItemWrapper<>(item2.getItemCommonData(), convertedItem2));
						}
					});
				}
			};

			UploadResponse expected = new UploadResponse(expectedResponseCommonData) {
				{
					setResourceItems(expectedResourceItems);
				}
			};

			assertThat(e.getResponse(), is(equalTo(expected)));

			throw e;
		}
	}

	/**
	 * {@link DefaultSynchronizer#upload(UploadRequest)}用テストメソッド.<br>
	 * リソース名に対応するリソースが見つからない場合、{@link NoSuchResourceException}がスローされる.
	 */
	@Test(expected = NoSuchResourceException.class)
	public void testUploadFailBecauseOfUnknownResource() {

		// Arrange：異常系
		final DefaultSynchronizer target = new DefaultSynchronizer();

		final String storageId = "storageId";

		final UploadCommonData requestCommon = new UploadCommonData() {
			{
				setStorageId(storageId);
				setLastUploadTime(1000);
			}
		};

		final UploadCommonData responseCommon = new UploadCommonData() {
			{
				setStorageId(storageId);
			}
		};

		final ResourceItemWrapper<? extends Map<String, Object>> item1_1_1 = createUploadItemWrapper("unknown", "1",
				SyncAction.CREATE);

		final UploadRequest request = new UploadRequest() {
			{
				setUploadCommonData(requestCommon);
				setResourceItems(new ArrayList<ResourceItemWrapper<? extends Map<String, Object>>>() {
					{
						add(item1_1_1);
					}
				});
			}
		};

		final long syncTime = 4000;

		new Expectations() {
			{
				setField(target, syncConfiguration);
				setField(target, resourceManager);
				setField(target, repository);

				syncConfiguration.generateSyncTime();
				result = syncTime;

				repository.findOne(storageId);
				result = responseCommon;

				syncConfiguration.uploadControl();
				result = UploadControlType.NONE;

				syncConfiguration.uploadControl();
				result = UploadControlType.NONE;

				// resource1(1)

				resourceManager.locateSyncResource("resourceunknown");
				result = null;
			}
		};

		// Act
		target.upload(request);

		fail();
	}

	/**
	 * 下り更新の結果を構成するアイテムデータのラッパーオブジェクトを生成する.
	 *
	 * @param resourceNameNum リソース名の末尾に付加する番号
	 * @param resourceItemIdNum リソースアイテムIDの末尾に付加する番号
	 * @return ラッパーオブジェクト(テスト用)
	 */
	private ResourceItemWrapper<Object> createDownloadItemWrapper(String resourceNameNum, String resourceItemIdNum) {

		String resourceName = "resource" + resourceNameNum;
		String resourceItemId = "resourceItemId" + resourceItemIdNum;
		ResourceItemCommonDataId id = new ResourceItemCommonDataId(resourceName, resourceItemId);

		String targetItemId = "targetItemId" + resourceItemIdNum;

		return new ResourceItemWrapper<>(new ResourceItemCommonData(id, targetItemId), new Object());
	}

	/**
	 * 上り更新の各更新内容を構成するアイテムデータのラッパーオブジェクトを生成する.
	 *
	 * @param resourceNameNum リソース名の末尾に付加する番号
	 * @param resourceItemIdNum リソースアイテムIDの末尾に付加する番号
	 * @param action リソースアイテム共通データのアクション
	 * @return ラッパーオブジェクト(テスト用)
	 */
	private ResourceItemWrapper<? extends Map<String, Object>> createUploadItemWrapper(String resourceNameNum,
			String resourceItemIdNum, final SyncAction action) {

		String resourceName = "resource" + resourceNameNum;
		String resourceItemId = "resourceItemId" + resourceItemIdNum;
		ResourceItemCommonDataId id = new ResourceItemCommonDataId(resourceName, resourceItemId);

		String targetItemId = "targetItemId" + resourceItemIdNum;

		ResourceItemCommonData itemCommonData = new ResourceItemCommonData(id, targetItemId) {
			{
				setAction(action);
			}
		};

		HashMap<String, Object> item = new HashMap<String, Object>() {
			{
				// リソースアイテムのフィールドとその値のペアを表現
				put("id", new Object());
				put("no", new Object());
			}
		};

		return new ResourceItemWrapper<>(itemCommonData, item);
	}

	/**
	 * テスト用のダミーコンバータ.<br>
	 *
	 * @param itemMap リソースアイテムデータ(HashMap)
	 * @return 変換後のオブジェクト
	 */
	private ResourceItemConverter<Object> getTestConverter() {

		return new ResourceItemConverter<Object>() {

			@Override
			public Object convertToItem(Object itemObj, Class<Object> to) {

				@SuppressWarnings("unchecked")
				Map<String, Object> itemMap = (Map<String, Object>) itemObj;

				return itemMap.get("id");
			}
		};
	}
}
