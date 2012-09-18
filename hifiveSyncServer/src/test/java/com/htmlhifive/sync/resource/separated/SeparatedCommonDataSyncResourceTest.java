/**
 *
 */
package com.htmlhifive.sync.resource.separated;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mockit.Expectations;
import mockit.Mocked;

import org.junit.Test;

import com.htmlhifive.sync.exception.ConflictException;
import com.htmlhifive.sync.exception.DuplicateElementException;
import com.htmlhifive.sync.exception.NotFoundException;
import com.htmlhifive.sync.resource.LockManager;
import com.htmlhifive.sync.resource.OptimisticLockUpdateStrategy;
import com.htmlhifive.sync.resource.SyncMethod;
import com.htmlhifive.sync.resource.SyncProvider;
import com.htmlhifive.sync.resource.SyncRequestHeader;
import com.htmlhifive.sync.resource.SyncResourceService;
import com.htmlhifive.sync.resource.SyncResponse;
import com.htmlhifive.sync.resource.SyncResponseHeader;
import com.htmlhifive.sync.sample.person.PersonResource;
import com.htmlhifive.sync.sample.person.PersonResourceElement;

/**
 * <H3>SeparatedCommonDataSyncResourceのテストクラス.</H3>
 *
 * @author kishigam
 */
public class SeparatedCommonDataSyncResourceTest {

	/**
	 * 抽象クラステスト用のサブクラス(get,put,delete用).
	 *
	 * @author kishigam
	 */
	@SyncResourceService(syncDataModel = "test")
	private class TargetSubClass extends SeparatedCommonDataSyncResource<String, Object> {

		/**
		 * get系メソッドが返すエレメント(リソースID文字列とのMap)
		 */
		private Map<String, Object> testElementMap;

		/**
		 * リソースID文字列、エレメントを指定せずインスタンスを生成する.<br>
		 * これらが考慮されないテストの場合に使用する.
		 */
		public TargetSubClass() {

		}

		/**
		 * get可能なデータを1件保持してインスタンスを生成する.
		 *
		 * @param resourceIdStr リソースID文字列
		 * @param expectedElement リソースエレメント
		 */
		public TargetSubClass(String resourceIdStr, Object expectedElement) {

			this.testElementMap = new HashMap<>();
			this.testElementMap.put(resourceIdStr, expectedElement);
		}

		/**
		 * get可能なデータを複数件保持してインスタンスを生成する.<br>
		 * 引数配列のそれぞれの要素数は一致していることを前提とする.
		 *
		 * @param resourceIdStr リソースID文字列
		 * @param expectedElement リソースエレメント
		 * @param resourceIdStrs
		 * @param expectedElements
		 */
		public TargetSubClass(String[] resourceIdStrs, Object[] expectedElements) {

			this.testElementMap = new HashMap<>();

			for (int i = 0; i < resourceIdStrs.length; i++) {
				this.testElementMap.put(resourceIdStrs[i], expectedElements[i]);
			}
		}

		@Override
		protected Object getImpl(String resourceIdStr) {
			return this.testElementMap.get(resourceIdStr);
		}

		@Override
		protected Map<String, Object> getImpl(Set<String> resourceIdStrSet, Map<String, String[]> queryMap) {
			return this.testElementMap;
		}

		@Override
		protected void putImpl(String resourceIdStr, Object element) {
		}

		@Override
		protected void deleteImpl(String resourceIdStr) {
		}

		@Override
		protected String postImpl(Object newElement) {
			return null;
		}

		@Override
		protected String resolveResourceId(String resourceIdStr) {
			return null;
		}

		@Override
		protected String generateNewResourceIdStr(String id) {
			return null;
		}
	}

	/**
	 * 抽象クラステスト用のサブクラス(post用).
	 *
	 * @author kishigam
	 */
	@SyncResourceService(syncDataModel = "test")
	private class TargetSubClassForPOST extends SeparatedCommonDataSyncResource<String, Object> {

		/**
		 * postメソッドが扱うリソースエレメントのIDフィールドの値.
		 */
		private String elementId;

		/**
		 * リソースID文字列、エレメントを指定せずインスタンスを生成する.<br>
		 * これらが考慮されないテストの場合に使用する.
		 */
		public TargetSubClassForPOST() {

		}

		/**
		 * リソースエレメントのIDを指定してインスタンスを生成する.<br>
		 * postメソッドの動作に影響する.
		 *
		 * @param createResourceIdStr
		 */
		public TargetSubClassForPOST(String elementId) {
			this.elementId = elementId;
		}

		@Override
		protected Object getImpl(String resourceIdStr) {
			return null;
		}

		@Override
		protected Map<String, Object> getImpl(Set<String> resourceIdStrSet, Map<String, String[]> queryMap) {
			return null;
		}

		@Override
		protected void putImpl(String resourceIdStr, Object element) {
		}

		@Override
		protected void deleteImpl(String resourceIdStr) {
		}

		@Override
		protected String postImpl(Object newElement) {
			return generateNewResourceIdStr(this.elementId);
		}

		@Override
		protected String resolveResourceId(String resourceIdStr) {
			return resourceIdStr.replace("resourceIdStr : id = ", "");
		}

		@Override
		protected String generateNewResourceIdStr(String id) {
			return "resourceIdStr : id = " + id;
		}
	}

	/**
	 * 抽象クラステスト用のサブクラス(post用、キー重複例外発生).
	 *
	 * @author kishigam
	 */
	@SyncResourceService(syncDataModel = "test")
	private class TargetSubClassForDuplicatePOST extends SeparatedCommonDataSyncResource<String, Object> {

		/**
		 * postメソッドが扱うリソースエレメントのIDフィールドの値.
		 */
		private String elementId;

		private Object duplicateElement;

		/**
		 * リソースエレメントのIDを指定してインスタンスを生成する.<br>
		 * postメソッドの動作に影響する.
		 *
		 * @param createResourceIdStr
		 */
		public TargetSubClassForDuplicatePOST(String elementId, Object duplicateElement) {
			this.elementId = elementId;
			this.duplicateElement = duplicateElement;
		}

		@Override
		protected Object getImpl(String resourceIdStr) {

			assertThat(resourceIdStr, is(equalTo(generateNewResourceIdStr(this.elementId))));

			return duplicateElement;
		}

		@Override
		protected Map<String, Object> getImpl(Set<String> resourceIdStrSet, Map<String, String[]> queryMap) {
			return null;
		}

		@Override
		protected void putImpl(String resourceIdStr, Object element) {
		}

		@Override
		protected void deleteImpl(String resourceIdStr) {
		}

		@Override
		protected String postImpl(Object newElement) throws DuplicateElementException {
			String resourceIdStr = generateNewResourceIdStr(this.elementId);
			throw new DuplicateElementException(resourceIdStr, getImpl(resourceIdStr));
		}

		@Override
		protected String resolveResourceId(String resourceIdStr) {
			return resourceIdStr.replace("resourceIdStr : id = ", "");
		}

		@Override
		protected String generateNewResourceIdStr(String id) {
			return "resourceIdStr : id = " + id;
		}
	}

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(SeparatedCommonDataSyncResource.class, notNullValue());
	}

	/**
	 * {@link SeparatedCommonDataSyncResource#get(SyncRequestHeader)}用テストメソッド.
	 *
	 * @param requestHeader 引数のモック
	 */
	@Test
	public void testGetSyncRequestHeader(@Mocked final SyncRequestHeader requestHeader) {

		// Arrange：正常系
		final String resourceIdStr = "test";
		final Object expectedElement = new Object();

		final SeparatedCommonDataSyncResource<?, ?> target = new TargetSubClass(resourceIdStr, expectedElement);

		final SyncResponseHeader expectedResponseHeader = new SyncResponseHeader(resourceIdStr);
		final SyncResponse<?> expectedResponse = new SyncResponse<>(expectedResponseHeader, expectedElement);

		new Expectations() {
			SyncProvider syncProvider;
			LockManager lockManager;
			{

				setField(target, "syncProvider", syncProvider);
				setField(target, "lockManager", lockManager);

				syncProvider.getCommonData(requestHeader);
				result = expectedResponseHeader;

				lockManager.lock(requestHeader, expectedResponseHeader);
				result = true;
			}
		};

		// Act
		SyncResponse<?> actual = target.get(requestHeader);

		// Assert：結果が正しいこと
		assertEqualsHelper(actual, expectedResponse);
	}

	/**
	 * {@link SeparatedCommonDataSyncResource#get(SyncRequestHeader)}用テストメソッド.<br>
	 * SyncRequestHeaderがnullだとNullPointerExceptionがスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testCannotGetBecauseOfNullHeader() {

		// Arrange：異常系
		final SeparatedCommonDataSyncResource<?, ?> target = new TargetSubClass();

		// Act
		target.get(null);
	}

	/**
	 * {@link SeparatedCommonDataSyncResource#get(SyncRequestHeader)}用テストメソッド.<br>
	 * SyncProviderからNotFoundExceptionがスローされた場合、catchせず上げる.
	 */
	@Test(expected = NotFoundException.class)
	public void testCannotGetBecauseOfNotFoundException(@Mocked final SyncRequestHeader requestHeader) {

		// Arrange：異常系
		final SeparatedCommonDataSyncResource<?, ?> target = new TargetSubClass();

		new Expectations() {
			SyncProvider syncProvider;
			{

				setField(target, "syncProvider", syncProvider);

				syncProvider.getCommonData(requestHeader);
				result = new NotFoundException("CommonDataNotFound");
			}
		};

		// Act
		target.get(requestHeader);
	}

	//	 TODO:悲観的ロック対応
	/**
	 * {@link SeparatedCommonDataSyncResource#get(SyncRequestHeader)}用テストメソッド.<br>
	 * lockManagerでロックが取得できない場合、・・・
	 *
	 * @param requestHeader 引数のモック
	 */
	@Test
	public void testCannotGetBecauseOfLockError(@Mocked final SyncRequestHeader requestHeader) {

		// Assert：悲観的ロック未実装のため常に成功
		assertTrue(true);
	}

	/**
	 * {@link SeparatedCommonDataSyncResource#getModifiedSince(SyncRequestHeader)}用テストメソッド.
	 *
	 * @param requestHeader 引数のモック
	 */
	@Test
	public void testGetModifiedSinceSyncRequestHeader(@Mocked final SyncRequestHeader requestHeader) {

		// Arrange：正常系
		final String resourceIdStr1 = "test1";
		final Object expectedElement1 = new Object();
		final String resourceIdStr2 = "test2";
		final Object expectedElement2 = new Object();

		final SeparatedCommonDataSyncResource<?, ?> target = new TargetSubClass(new String[] { resourceIdStr1,
				resourceIdStr2 }, new Object[] { expectedElement1, expectedElement2 });

		final SyncResponseHeader expectedResponseHeader1 = new SyncResponseHeader(resourceIdStr1);
		final SyncResponseHeader expectedResponseHeader2 = new SyncResponseHeader(resourceIdStr2);

		@SuppressWarnings("serial")
		final Map<String, SyncResponseHeader> expectedResponseHeaderMap = new HashMap<String, SyncResponseHeader>() {
			{
				put(resourceIdStr1, expectedResponseHeader1);
				put(resourceIdStr2, expectedResponseHeader2);
			}
		};

		final SyncResponse<?> expectedResponse1 = new SyncResponse<>(expectedResponseHeader1, expectedElement1);
		final SyncResponse<?> expectedResponse2 = new SyncResponse<>(expectedResponseHeader2, expectedElement2);

		@SuppressWarnings("serial")
		final Set<? extends SyncResponse<?>> expectedResponseSet = new HashSet<SyncResponse<?>>() {
			{
				add(expectedResponse1);
				add(expectedResponse2);
			}
		};

		new Expectations() {
			SyncProvider syncProvider;
			LockManager lockManager;
			{

				setField(target, "syncProvider", syncProvider);
				setField(target, "lockManager", lockManager);

				syncProvider.getCommonDataModifiedSince(requestHeader);
				result = expectedResponseHeaderMap;

				lockManager.lock(requestHeader, expectedResponseHeader1);
				result = true;
				lockManager.lock(requestHeader, expectedResponseHeader2);
				result = true;
			}
		};

		// Act
		Set<? extends SyncResponse<?>> actual = target.getModifiedSince(requestHeader);

		// Assert：結果が正しいこと
		assertEqualsHelper(actual, expectedResponseSet);
	}

	/**
	 * {@link SeparatedCommonDataSyncResource#getModifiedSince(SyncRequestHeader)}用テストメソッド.<br>
	 * SyncRequestHeaderがnullだとNullPointerExceptionがスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testCannotGetModifiedSinceBecauseOfNullHeader() {

		// Arrange：異常系
		final SeparatedCommonDataSyncResource<?, ?> target = new TargetSubClass();

		// Act
		target.getModifiedSince(null);
	}

	/**
	 * {@link SeparatedCommonDataSyncResource#getModifiedSince(SyncRequestHeader)}用テストメソッド.<br>
	 * 1件でもSyncProviderからNotFoundExceptionがスローされた場合、catchせず上げる.
	 */
	@Test(expected = NotFoundException.class)
	public void testCannotGetModifiedSinceBecauseOfNotFoundException(@Mocked final SyncRequestHeader requestHeader) {

		// Arrange：異常系
		final String resourceIdStr1 = "test1";
		final Object expectedElement1 = new Object();
		final String resourceIdStr2 = "test2";

		final SeparatedCommonDataSyncResource<?, ?> target = new TargetSubClass(new String[] { resourceIdStr1,
				resourceIdStr2 }, new Object[] { expectedElement1, null });

		final SyncResponseHeader expectedResponseHeader1 = new SyncResponseHeader(resourceIdStr1);

		final SyncResponseHeader expectedResponseHeader2 = new SyncResponseHeader(resourceIdStr2);

		@SuppressWarnings("serial")
		final Map<String, SyncResponseHeader> expectedResponseHeaderMap = new HashMap<String, SyncResponseHeader>() {
			{
				put(resourceIdStr1, expectedResponseHeader1);
				put(resourceIdStr2, expectedResponseHeader2);
			}
		};

		new Expectations() {
			SyncProvider syncProvider;
			LockManager lockManager;
			{

				setField(target, "syncProvider", syncProvider);
				setField(target, "lockManager", lockManager);

				syncProvider.getCommonDataModifiedSince(requestHeader);
				result = expectedResponseHeaderMap;

				lockManager.lock(requestHeader, expectedResponseHeader1);
				result = true;
				lockManager.lock(requestHeader, expectedResponseHeader2);
				result = new NotFoundException("CommonDataNotFound");
			}
		};

		// Act
		target.getModifiedSince(requestHeader);
	}

	//	 TODO:悲観的ロック対応
	/**
	 * {@link SeparatedCommonDataSyncResource#getModifiedSince(SyncRequestHeader)}用テストメソッド.<br>
	 * lockManagerでロックが取得できない場合、・・・
	 *
	 * @param requestHeader 引数のモック
	 */
	@Test
	public void testCannotGetModifiedSinceBecauseOfLockError(@Mocked final SyncRequestHeader requestHeader) {

		// Assert：悲観的ロック未実装のため常に成功
		assertTrue(true);
	}

	/**
	 * {@link SeparatedCommonDataSyncResource#put(SyncRequestHeader, Object)}用テストメソッド.
	 *
	 * @param requestHeader 引数のモック
	 */
	@Test
	public void testPutSyncRequestHeaderObject(@Mocked final SyncRequestHeader requestHeader) throws Exception {

		// Arrange：正常系
		final String resourceIdStr = "test1";
		final Object elementBeforeUpdate = new Object();

		final SeparatedCommonDataSyncResource<?, Object> target = new TargetSubClass(resourceIdStr, elementBeforeUpdate);

		final SyncResponseHeader expectedResponseHeaderBeforeUpdate = new SyncResponseHeader(resourceIdStr);

		final SyncResponseHeader expectedResponseHeaderAfterUpdate = new SyncResponseHeader(resourceIdStr);

		final Object updateElement = new Object();
		final SyncResponse<?> expectedResponse = new SyncResponse<>(expectedResponseHeaderAfterUpdate, updateElement);

		new Expectations() {
			SyncProvider syncProvider;
			LockManager lockManager;
			OptimisticLockUpdateStrategy updateStrategy;
			{

				setField(target, "syncProvider", syncProvider);
				setField(target, "lockManager", lockManager);
				setField(target, "updateStrategy", updateStrategy);

				syncProvider.getCommonData(requestHeader);
				result = expectedResponseHeaderBeforeUpdate;

				lockManager.canUpdate(requestHeader, expectedResponseHeaderBeforeUpdate);
				result = true;

				syncProvider.saveUpdatedCommonData(requestHeader);
				result = expectedResponseHeaderAfterUpdate;

				lockManager.release(requestHeader, expectedResponseHeaderAfterUpdate);
			}
		};

		// Act
		SyncResponse<?> actual = putHelper(target, requestHeader, updateElement);

		// Assert：結果が正しいこと
		assertEqualsHelper(actual, expectedResponse);
	}

	/**
	 * {@link SeparatedCommonDataSyncResource#put(SyncRequestHeader, Object)}用テストメソッド.<br>
	 * SyncRequestHeaderがnullだとNullPointerExceptionがスローされる.
	 *
	 * @param requestHeader 引数のモック
	 */
	@Test(expected = NullPointerException.class)
	public void testCannotPutBecauseOfNullHeader() throws Exception {

		// Arrange：異常系
		final SeparatedCommonDataSyncResource<?, Object> target = new TargetSubClass();

		final Object updateElement = new Object();

		// Act
		target.put(null, updateElement);
	}

	/**
	 * {@link SeparatedCommonDataSyncResource#put(SyncRequestHeader, Object)}用テストメソッド.<br>
	 * SyncProviderからNotFoundExceptionがスローされた場合、catchせず上げる.
	 *
	 * @param requestHeader 引数のモック
	 */
	@Test(expected = NotFoundException.class)
	public void testCannotPutBecauseOfNotFoundException(@Mocked final SyncRequestHeader requestHeader) throws Exception {

		// Arrange：異常系
		final String resourceIdStr = "test1";
		final Object elementBeforeUpdate = new Object();

		final SeparatedCommonDataSyncResource<?, Object> target = new TargetSubClass(resourceIdStr, elementBeforeUpdate);

		final Object updateElement = new Object();

		new Expectations() {
			SyncProvider syncProvider;
			{

				setField(target, "syncProvider", syncProvider);

				syncProvider.getCommonData(requestHeader);
				result = new NotFoundException("CommonDataNotFound");
			}
		};

		// Act
		putHelper(target, requestHeader, updateElement);
	}

	//	 TODO:悲観的ロック対応
	/**
	 * {@link SeparatedCommonDataSyncResource#get(SyncRequestHeader)}用テストメソッド.<br>
	 * lockManagerでロックエラーとなり、更新戦略がClientResolvingのためConflictExceptionがスローされる.
	 *
	 * @param requestHeader 引数のモック
	 */
	@Test
	public void testCannotPutBecauseOfConflict(@Mocked final SyncRequestHeader requestHeader) throws Exception {

		// Arrange：例外系
		final String resourceIdStr = "test1";
		final Object elementBeforeUpdate = new Object();

		final SeparatedCommonDataSyncResource<?, Object> target = new TargetSubClass(resourceIdStr, elementBeforeUpdate);

		final SyncResponseHeader expectedResponseHeaderBeforeUpdate = new SyncResponseHeader(resourceIdStr);

		final Object updateElement = new Object();
		final SyncResponse<?> expectedConflictResponse = new SyncResponse<>(expectedResponseHeaderBeforeUpdate,
				elementBeforeUpdate);

		new Expectations() {
			SyncProvider syncProvider;
			LockManager lockManager;
			OptimisticLockUpdateStrategy updateStrategy;
			{

				setField(target, "syncProvider", syncProvider);
				setField(target, "lockManager", lockManager);
				setField(target, "updateStrategy", updateStrategy);

				syncProvider.getCommonData(requestHeader);
				result = expectedResponseHeaderBeforeUpdate;

				lockManager.canUpdate(requestHeader, expectedResponseHeaderBeforeUpdate);
				result = false;

				updateStrategy.resolveConflict(requestHeader, updateElement, expectedResponseHeaderBeforeUpdate,
						elementBeforeUpdate);
				result = new ConflictException(expectedConflictResponse);
			}
		};

		// Act
		try {
			putHelper(target, requestHeader, updateElement);

			fail();
		} catch (ConflictException actual) {

			// Assert：結果が正しいこと
			assertEqualsHelper(actual.getConflictedResponse(), expectedConflictResponse);
		}
	}

	/**
	 * {@link SeparatedCommonDataSyncResource#delete(SyncRequestHeader)}用テストメソッド.
	 *
	 * @param requestHeader 引数のモック
	 */
	@Test
	public void testDeleteSyncRequestHeader(@Mocked final SyncRequestHeader requestHeader) throws Exception {

		// Arrange：正常系
		final String resourceIdStr = "test1";
		final Object elementBeforeUpdate = new Object();

		final SeparatedCommonDataSyncResource<?, Object> target = new TargetSubClass(resourceIdStr, elementBeforeUpdate);

		final SyncResponseHeader expectedResponseHeaderBeforeDelete = new SyncResponseHeader(resourceIdStr);

		final SyncResponseHeader expectedResponseHeaderAfterDelete = new SyncResponseHeader(resourceIdStr);

		final SyncResponse<?> expectedResponse = new SyncResponse<>(expectedResponseHeaderAfterDelete, null);

		new Expectations() {
			SyncProvider syncProvider;
			LockManager lockManager;
			{

				setField(target, "syncProvider", syncProvider);
				setField(target, "lockManager", lockManager);

				syncProvider.getCommonData(requestHeader);
				result = expectedResponseHeaderBeforeDelete;

				lockManager.canUpdate(requestHeader, expectedResponseHeaderBeforeDelete);
				result = true;

				syncProvider.saveUpdatedCommonData(requestHeader);
				result = expectedResponseHeaderAfterDelete;

				lockManager.release(requestHeader, expectedResponseHeaderAfterDelete);
			}
		};

		// Act
		SyncResponse<?> actual = target.delete(requestHeader);

		// Assert：結果が正しいこと
		assertEqualsHelper(actual, expectedResponse);
	}

	/**
	 * {@link SeparatedCommonDataSyncResource#delete(SyncRequestHeader, Object)}用テストメソッド.<br>
	 * SyncRequestHeaderがnullだとNullPointerExceptionがスローされる.
	 *
	 * @param requestHeader 引数のモック
	 */
	@Test(expected = NullPointerException.class)
	public void testCannotDeleteBecauseOfNullHeader() throws Exception {

		// Arrange：異常系
		final SeparatedCommonDataSyncResource<?, Object> target = new TargetSubClass();

		// Act
		target.delete(null);
	}

	/**
	 * {@link SeparatedCommonDataSyncResource#delete(SyncRequestHeader, Object)}用テストメソッド.<br>
	 * SyncProviderからNotFoundExceptionがスローされた場合、catchせず上げる.
	 *
	 * @param requestHeader 引数のモック
	 */
	@Test(expected = NotFoundException.class)
	public void testCannotDeleteBecauseOfNotFoundException(@Mocked final SyncRequestHeader requestHeader)
			throws Exception {

		// Arrange：異常系
		final String resourceIdStr = "test1";
		final Object elementBeforeUpdate = new Object();

		final SeparatedCommonDataSyncResource<?, Object> target = new TargetSubClass(resourceIdStr, elementBeforeUpdate);

		new Expectations() {
			SyncProvider syncProvider;
			{

				setField(target, "syncProvider", syncProvider);

				syncProvider.getCommonData(requestHeader);
				result = new NotFoundException("CommonDataNotFound");
			}
		};

		// Act
		target.delete(requestHeader);
	}

	//	 TODO:悲観的ロック対応
	/**
	 * {@link SeparatedCommonDataSyncResource#delete(SyncRequestHeader)}用テストメソッド.<br>
	 * lockManagerでロックエラーとなった場合、ConflictExceptionがスローされる.
	 *
	 * @param requestHeader 引数のモック
	 */
	@Test
	public void testCannotDeleteBecauseOfConflict(@Mocked final SyncRequestHeader requestHeader) throws Exception {

		// Arrange：例外系
		final String resourceIdStr = "test1";
		final Object elementBeforeUpdate = new Object();

		final SeparatedCommonDataSyncResource<?, Object> target = new TargetSubClass(resourceIdStr, elementBeforeUpdate);

		final SyncResponseHeader expectedResponseHeaderBeforeUpdate = new SyncResponseHeader(resourceIdStr);

		final SyncResponse<?> expectedConflictResponse = new SyncResponse<>(expectedResponseHeaderBeforeUpdate,
				elementBeforeUpdate);

		new Expectations() {
			SyncProvider syncProvider;
			LockManager lockManager;
			OptimisticLockUpdateStrategy updateStrategy;
			{

				setField(target, "syncProvider", syncProvider);
				setField(target, "lockManager", lockManager);
				setField(target, "updateStrategy", updateStrategy);

				syncProvider.getCommonData(requestHeader);
				result = expectedResponseHeaderBeforeUpdate;

				lockManager.canUpdate(requestHeader, expectedResponseHeaderBeforeUpdate);
				result = false;

				updateStrategy.resolveConflict(requestHeader, null, expectedResponseHeaderBeforeUpdate,
						elementBeforeUpdate);
				result = new ConflictException(expectedConflictResponse);
			}
		};

		// Act
		try {
			target.delete(requestHeader);

			fail();
		} catch (ConflictException actual) {

			// Assert：結果が正しいこと
			assertEqualsHelper(actual.getConflictedResponse(), expectedConflictResponse);
		}
	}

	//	 TODO:悲観的ロック対応
	/**
	 * {@link SeparatedCommonDataSyncResource#delete(SyncRequestHeader)}用テストメソッド.<br>
	 * lockManagerでロックエラーとなり、更新戦略に従って更新すべきエレメントが決定した場合、put処理が行われる.
	 *
	 * @param requestHeader 引数のモック
	 */
	@Test
	public void testChangingDeleteToPutBecauseOfConflictResolved(@Mocked final SyncRequestHeader requestHeader)
			throws Exception {

		// Arrange：例外系
		final String resourceIdStr = "test1";
		final Object elementBeforeUpdate = new Object();

		final SeparatedCommonDataSyncResource<?, Object> target = new TargetSubClass(resourceIdStr, elementBeforeUpdate);

		final SyncResponseHeader expectedResponseHeaderBeforeUpdate = new SyncResponseHeader(resourceIdStr);

		final Object putElement = new Object();
		final SyncResponseHeader expectedResponseHeaderAfterUpdate = new SyncResponseHeader(resourceIdStr);
		final SyncResponse<?> expectedResponse = new SyncResponse<>(expectedResponseHeaderAfterUpdate, putElement);

		new Expectations() {
			SyncProvider syncProvider;
			LockManager lockManager;
			OptimisticLockUpdateStrategy updateStrategy;
			{

				setField(target, "syncProvider", syncProvider);
				setField(target, "lockManager", lockManager);
				setField(target, "updateStrategy", updateStrategy);

				syncProvider.getCommonData(requestHeader);
				result = expectedResponseHeaderBeforeUpdate;

				lockManager.canUpdate(requestHeader, expectedResponseHeaderBeforeUpdate);
				result = false;

				updateStrategy.resolveConflict(requestHeader, null, expectedResponseHeaderBeforeUpdate,
						elementBeforeUpdate);
				result = putElement;

				// リクエストヘッダが書き換えられる
				requestHeader.setSyncMethod(SyncMethod.PUT);

				syncProvider.saveUpdatedCommonData(requestHeader);
				result = expectedResponseHeaderAfterUpdate;

				lockManager.release(requestHeader, expectedResponseHeaderAfterUpdate);
			}
		};

		// Act
		SyncResponse<?> actual = target.delete(requestHeader);

		// Assert：結果が正しいこと
		assertEqualsHelper(actual, expectedResponse);
	}

	/**
	 * {@link SeparatedCommonDataSyncResource#post(SyncRequestHeader, Object)}用テストメソッド.
	 *
	 * @param requestHeader 引数のモック
	 */
	@Test
	public void testPostSyncRequestHeaderObject(@Mocked final SyncRequestHeader requestHeader) throws Exception {

		// Arrange：正常系
		final String createElementId = "test";
		final SeparatedCommonDataSyncResource<String, Object> target = new TargetSubClassForPOST(createElementId);

		final String expectedResourceIdStr = target.generateNewResourceIdStr(createElementId);

		final SyncResponseHeader expectedResponseHeader = new SyncResponseHeader(expectedResourceIdStr);

		final Object createElement = new Object();
		final SyncResponse<?> expectedResponse = new SyncResponse<>(expectedResponseHeader, createElement);

		new Expectations() {
			SyncProvider syncProvider;
			LockManager lockManager;
			{

				setField(target, "syncProvider", syncProvider);
				setField(target, "lockManager", lockManager);

				syncProvider.saveNewCommonData(requestHeader, expectedResourceIdStr);
				result = expectedResponseHeader;
			}
		};

		// Act
		SyncResponse<?> actual = postHelper(target, requestHeader, createElement);

		// Assert：結果が正しいこと
		assertEqualsHelper(actual, expectedResponse);
	}

	/**
	 * {@link SeparatedCommonDataSyncResource#post(SyncRequestHeader, Object)}用テストメソッド.<br>
	 * SyncRequestHeaderがnullだとNullPointerExceptionがスローされる.
	 *
	 * @param requestHeader 引数のモック
	 */
	@Test(expected = NullPointerException.class)
	public void testCannotPostBecauseOfNullHeader() throws Exception {

		// Arrange：異常系
		final SeparatedCommonDataSyncResource<?, Object> target = new TargetSubClassForPOST();

		final Object createElement = new Object();

		// Act
		target.post(null, createElement);
	}

	/**
	 * {@link SeparatedCommonDataSyncResource#post(SyncRequestHeader, Object)}用テストメソッド.<br>
	 * キー重複が発生した場合、ConflictException(case: DuplicateElementException)がスローされる.
	 *
	 * @param requestHeader 引数のモック
	 */
	@Test
	public void testCannotPostBecauseOfDuplicateElementException(@Mocked final SyncRequestHeader requestHeader) {

		// Arrange：例外系
		final String createElementId = "test";
		final Object existingElement = new String("existingElement");

		final SeparatedCommonDataSyncResource<String, Object> target = new TargetSubClassForDuplicatePOST(
				createElementId, existingElement);

		final String expectedResourceIdStr = target.generateNewResourceIdStr(createElementId);

		final SyncResponseHeader expectedExistingResponseHeader = new SyncResponseHeader(expectedResourceIdStr);

		final SyncResponse<?> expectedDuplicatedResponse = new SyncResponse<>(expectedExistingResponseHeader,
				existingElement);

		new Expectations() {
			SyncProvider syncProvider;
			{

				setField(target, "syncProvider", syncProvider);

				requestHeader.getDataModelName();
				result = any;

				syncProvider.getCommonData((String) any, expectedResourceIdStr);
				result = expectedExistingResponseHeader;

				result = true;
			}
		};

		// Act
		try {
			final Object createElement = new String("createElement");
			target.post(requestHeader, createElement);

			fail();
		} catch (ConflictException actual) {

			// Assert：結果が正しいこと
			assertThat(actual.getCause(), is(DuplicateElementException.class));

			assertEqualsHelper(actual.getConflictedResponse(), expectedDuplicatedResponse);
		}
	}

	/**
	 * {@link SeparatedCommonDataSyncResource#getElementType()}用テストメソッド.
	 */
	@Test
	public void testGetElementType() {

		// Arrange：正常系
		SeparatedCommonDataSyncResource<?, ?> target = new TargetSubClass();

		// Act
		Class<?> actual = target.getElementType();

		// Assert：結果が正しいこと
		assertEqualsHelper(actual, Object.class);
	}

	/**
	 * {@link SeparatedCommonDataSyncResource#getElementType()}用テストメソッド. PersonResourceでは、PersonResourceElementを返す.
	 */
	@Test
	public void testGetPersonResourceElementType() {

		// Arrange：正常系
		SeparatedCommonDataSyncResource<?, ?> target = new PersonResource();

		// Act
		Class<?> actual = target.getElementType();

		// Assert：結果が正しいこと
		assertEqualsHelper(actual, PersonResourceElement.class);
	}

	/**
	 * キャプチャヘルパー
	 *
	 * @param a
	 * @param b
	 */
	private <E> void assertEqualsHelper(E a, E b) {
		assertThat(a, is(equalTo(b)));
	}

	/**
	 * キャプチャヘルパー
	 *
	 * @param target
	 * @param requestHeader
	 * @param element
	 * @return
	 */
	private <I, E> SyncResponse<E> putHelper(SeparatedCommonDataSyncResource<I, E> target,
			SyncRequestHeader requestHeader, E element) throws ConflictException {

		return target.put(requestHeader, element);
	}

	/**
	 * キャプチャヘルパー
	 *
	 * @param target
	 * @param requestHeader
	 * @param element
	 * @return
	 */
	private <I, E> SyncResponse<E> postHelper(SeparatedCommonDataSyncResource<I, E> target,
			SyncRequestHeader requestHeader, E element) throws ConflictException {

		return target.post(requestHeader, element);
	}
}
