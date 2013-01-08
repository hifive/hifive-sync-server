/**
 *
 */
package com.htmlhifive.sync.ctrl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import mockit.Expectations;
import mockit.Mocked;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.htmlhifive.sync.exception.ConflictException;
import com.htmlhifive.sync.service.Synchronizer;
import com.htmlhifive.sync.service.download.DownloadCommonData;
import com.htmlhifive.sync.service.download.DownloadRequest;
import com.htmlhifive.sync.service.download.DownloadResponse;
import com.htmlhifive.sync.service.upload.UploadCommonData;
import com.htmlhifive.sync.service.upload.UploadRequest;
import com.htmlhifive.sync.service.upload.UploadResponse;

/**
 * <H3>JsonSyncControllerのテストクラス.</H3>
 *
 * @author kishigam
 */
public class JsonSyncControllerTest {

	@Mocked
	private Synchronizer synchronizer;

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(JsonSyncController.class, notNullValue());
	}

	/**
	 * {@link JsonSyncController#JsonSyncController()}用テストメソッド.
	 */
	@Test
	public void testInstantiation() {
		JsonSyncController target = new JsonSyncController();
		assertThat(target, notNullValue());
	}

	/**
	 * {@link JsonSyncController#download(DownloadRequest)}用テストメソッド.
	 *
	 * @param request 引数のモック
	 */
	@Test
	public void testDownload(@Mocked final DownloadRequest request) {

		// Arrange：正常系
		final JsonSyncController target = new JsonSyncController();

		String storageId = "storageId";

		final DownloadCommonData requestCommon = new DownloadCommonData(storageId);

		DownloadCommonData responseCommon = new DownloadCommonData(storageId);
		final DownloadResponse expectedResponse = new DownloadResponse(responseCommon);

		new Expectations() {
			{
				setField(target, synchronizer);

				request.getDownloadCommonData();
				result = requestCommon;

				request.getDownloadCommonData();
				result = requestCommon;

				synchronizer.download(request);
				result = expectedResponse;
			}
		};

		// Act
		ResponseEntity<DownloadResponse> actual = target.download(request);

		// Assert：結果が正しいこと
		HttpHeaders headers = new HttpHeaders() {
			{
				setContentType(MediaType.APPLICATION_JSON);
			}
		};

		assertThat(actual.getBody(), is(equalTo(expectedResponse)));
		assertThat(actual.getHeaders(), is(equalTo(headers)));
		assertThat(actual.getStatusCode(), is(HttpStatus.OK));

		assertThat(actual.getBody().getDownloadCommonData().getStorageId(), is(nullValue()));
	}

	/**
	 * {@link JsonSyncController#download(DownloadRequest)}用テストメソッド.<br>
	 * 下り更新共通データがnullのときは、新規に採番したstorageIdを使用して生成する.
	 *
	 * @param request 引数のモック
	 */
	@Test
	public void testDownloadFirstAndNullCommonData(@Mocked final DownloadRequest request) {

		// Arrange：正常系
		final String storageId = "storageId";

		final JsonSyncController target = new JsonSyncController() {

			@Override
			protected String generateNewStorageId() {
				return storageId;
			}
		};

		final DownloadCommonData generatedDownloadCommon = new DownloadCommonData(null);

		final DownloadCommonData storageIdGeneratedDownloadCommon = new DownloadCommonData(storageId);

		DownloadCommonData responseCommon = new DownloadCommonData(storageId);
		final DownloadResponse expectedResponse = new DownloadResponse(responseCommon);

		new Expectations() {
			{
				setField(target, synchronizer);

				request.getDownloadCommonData();
				result = null;

				request.setDownloadCommonData(generatedDownloadCommon);

				request.getDownloadCommonData();
				result = storageIdGeneratedDownloadCommon;

				synchronizer.download(request);
				result = expectedResponse;
			}
		};

		// Act
		ResponseEntity<DownloadResponse> actual = target.download(request);

		// Assert：結果が正しいこと
		HttpHeaders headers = new HttpHeaders() {
			{
				setContentType(MediaType.APPLICATION_JSON);
			}
		};

		assertThat(actual.getBody(), is(equalTo(expectedResponse)));
		assertThat(actual.getHeaders(), is(equalTo(headers)));
		assertThat(actual.getStatusCode(), is(HttpStatus.OK));

		assertThat(actual.getBody().getDownloadCommonData().getStorageId(), is(not(nullValue())));
	}

	/**
	 * {@link JsonSyncController#download(DownloadRequest)}用テストメソッド.<br>
	 * 下り更新共通データのstorageIdがnullのときは、新規に採番したstorageIdを使用する.
	 *
	 * @param request 引数のモック
	 */
	@Test
	public void testDownloadFirstAndNullStorageId(@Mocked final DownloadRequest request) {

		// Arrange：正常系
		final String storageId = "storageId";

		final JsonSyncController target = new JsonSyncController() {

			@Override
			protected String generateNewStorageId() {
				return storageId;
			}
		};

		final DownloadCommonData requestCommon = new DownloadCommonData(null);

		final DownloadCommonData storageIdGeneratedDownloadCommon = new DownloadCommonData(storageId);

		DownloadCommonData responseCommon = new DownloadCommonData(storageId);
		final DownloadResponse expectedResponse = new DownloadResponse(responseCommon);

		new Expectations() {
			{
				setField(target, synchronizer);

				request.getDownloadCommonData();
				result = requestCommon;

				request.getDownloadCommonData();
				result = requestCommon;

				request.setDownloadCommonData(storageIdGeneratedDownloadCommon);

				synchronizer.download(request);
				result = expectedResponse;
			}
		};

		// Act
		ResponseEntity<DownloadResponse> actual = target.download(request);

		// Assert：結果が正しいこと
		HttpHeaders headers = new HttpHeaders() {
			{
				setContentType(MediaType.APPLICATION_JSON);
			}
		};

		assertThat(actual.getBody(), is(equalTo(expectedResponse)));
		assertThat(actual.getHeaders(), is(equalTo(headers)));
		assertThat(actual.getStatusCode(), is(HttpStatus.OK));

		assertThat(actual.getBody().getDownloadCommonData().getStorageId(), is(not(nullValue())));
	}

	/**
	 * {@link JsonSyncController#upload(UploadRequest)}用テストメソッド.
	 *
	 * @param request 引数のモック
	 */
	@Test
	public void testUpload(@Mocked final UploadRequest request) {

		// Arrange：正常系
		final JsonSyncController target = new JsonSyncController();

		UploadCommonData responseCommon = new UploadCommonData();

		final UploadResponse expectedResponse = new UploadResponse(responseCommon);

		new Expectations() {
			{
				setField(target, synchronizer);

				synchronizer.upload(request);
				result = expectedResponse;
			}
		};

		// Act
		ResponseEntity<UploadResponse> actual = target.upload(request);

		// Assert：結果が正しいこと
		HttpHeaders headers = new HttpHeaders() {
			{
				setContentType(MediaType.APPLICATION_JSON);
			}
		};

		assertThat(actual.getBody(), is(equalTo(expectedResponse)));
		assertThat(actual.getHeaders(), is(equalTo(headers)));
		assertThat(actual.getStatusCode(), is(HttpStatus.OK));

		assertThat(actual.getBody().getUploadCommonData().getStorageId(), is(nullValue()));
		assertThat(actual.getBody().getUploadCommonData().getConflictType(), is(nullValue()));
	}

	/**
	 * {@link JsonSyncController#upload(UploadRequest)}用テストメソッド.
	 *
	 * @param request 引数のモック
	 */
	@Test
	public void testUploadThrowsConflictException(@Mocked final UploadRequest request) {

		// Arrange：正常系
		final JsonSyncController target = new JsonSyncController();

		UploadCommonData responseCommon = new UploadCommonData();

		final UploadResponse expectedResponse = new UploadResponse(responseCommon);

		new Expectations() {
			{
				setField(target, synchronizer);

				synchronizer.upload(request);
				result = new ConflictException(expectedResponse);
			}
		};

		// Act
		ResponseEntity<UploadResponse> actual = target.upload(request);

		// Assert：結果が正しいこと
		HttpHeaders headers = new HttpHeaders() {
			{
				setContentType(MediaType.APPLICATION_JSON);
			}
		};

		assertThat(actual.getBody(), is(equalTo(expectedResponse)));
		assertThat(actual.getHeaders(), is(equalTo(headers)));
		assertThat(actual.getStatusCode(), is(HttpStatus.CONFLICT));

		assertThat(actual.getBody().getUploadCommonData().getStorageId(), is(nullValue()));
		assertThat(actual.getBody().getUploadCommonData().getConflictType(), is(nullValue()));
	}
}
