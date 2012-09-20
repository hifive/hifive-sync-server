/**
 *
 */
package com.htmlhifive.sync.status;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Date;

import mockit.Expectations;
import mockit.Mocked;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 * <H3>LastUploadStatusServiceのテストクラス.</H3>
 *
 * @author kishigam
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
@ComponentScan(basePackages = "com.htmlhifive.sync")
public class LastUploadStatusServiceTest {

	@Mocked
	LastUploadStatusRepository repository;

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(LastUploadStatusService.class, notNullValue());
	}

	/**
	 * {@link LastUploadStatusService#LastUploadStatusService()}用テストメソッド.
	 */
	@Test
	public void testInstantiation() {
		LastUploadStatusService target = new LastUploadStatusService();
		assertThat(target, notNullValue());
	}

	/**
	 * {@link LastUploadStatusService#currentStatus(String)}用テストメソッド.
	 */
	@Test
	public void testCurrentStatus() {

		// Arrange：正常系
		final LastUploadStatusService target = new LastUploadStatusService();

		final String storageId = "storage1";

		final LastUploadStatus expectedStatus = new LastUploadStatus(storageId);
		expectedStatus.setLastUploadTime(new Date().getTime());

		new Expectations() {
			{
				setField(target, repository);

				repository.findOne(storageId);
				result = expectedStatus;
			}
		};

		// Act
		LastUploadStatus actual = target.currentStatus(storageId);

		// Assert：結果が正しいこと
		assertThat(actual, is(equalTo(expectedStatus)));
	}

	/**
	 * {@link LastUploadStatusService#currentStatus(String)}用テストメソッド.<br>
	 * 保存されているステータスがなければ新規生成して返します.
	 */
	@Test
	public void testCurrentStatusIfNewClient() {

		// Arrange：正常系
		final LastUploadStatusService target = new LastUploadStatusService();

		final String storageId = "storage1";

		// ゼロ時刻の新規ステータスエンティティ
		final LastUploadStatus expectedStatus = new LastUploadStatus(storageId);
		expectedStatus.setLastUploadTime(0L);

		new Expectations() {
			{
				setField(target, repository);

				repository.findOne(storageId);
				result = null;
			}
		};

		// Act
		LastUploadStatus actual = target.currentStatus(storageId);

		// Assert：結果が正しいこと
		assertThat(actual, is(equalTo(expectedStatus)));
	}

	/**
	 * {@link LastUploadStatusService#currentStatus(String)}用テストメソッド.<br>
	 * nullが渡された場合、NullPointerExceptionをスローする.
	 */
	@Test(expected = NullPointerException.class)
	public void testCurrentStatusFailIfNullId() {

		// Arrange：異常系
		final LastUploadStatusService target = new LastUploadStatusService();

		// Act
		target.currentStatus(null);
	}

	/**
	 * {@link LastUploadStatusService#updateStatus(LastUploadStatus)}用テストメソッド.
	 */
	@Test
	public void testUpdateStatus() {

		// Arrange：正常系
		final LastUploadStatusService target = new LastUploadStatusService();

		final String storageId = "storage1";
		final LastUploadStatus currentStatus = new LastUploadStatus(storageId);
		currentStatus.setLastUploadTime(100L);

		final long lastUploadTime = 200L;
		final LastUploadStatus expectedSavedStatus = new LastUploadStatus(storageId);
		expectedSavedStatus.setLastUploadTime(lastUploadTime);

		new Expectations() {
			{
				setField(target, repository);

				repository.findOne(storageId);
				result = currentStatus;

				repository.save(expectedSavedStatus);
				result = expectedSavedStatus;
			}
		};

		// Act
		LastUploadStatus actualCurrent = target.currentStatus(storageId);
		assertThat(actualCurrent, is(equalTo(currentStatus)));

		currentStatus.setLastUploadTime(lastUploadTime);

		target.updateStatus(currentStatus);

		// Assert：結果が正しいこと
	}

	/**
	 * {@link LastUploadStatusService#updateStatus(LastUploadStatus)}用テストメソッド.<br>
	 * 存在しない場合新規に保存される.
	 */
	@Test
	public void testUpdateStatusIfNewSave() {

		// Arrange：正常系
		final LastUploadStatusService target = new LastUploadStatusService();

		final String storageId = "storage1";

		final long lastUploadTime = 200L;
		final LastUploadStatus expectedSavedStatus = new LastUploadStatus(storageId);
		expectedSavedStatus.setLastUploadTime(lastUploadTime);

		new Expectations() {
			{
				setField(target, repository);

				repository.findOne(storageId);
				result = null;

				repository.save(expectedSavedStatus);
				result = expectedSavedStatus;
			}
		};

		// Act
		LastUploadStatus actualCurrent = target.currentStatus(storageId);
		assertThat(actualCurrent.getLastUploadTime(), is(equalTo(0L)));

		actualCurrent.setLastUploadTime(lastUploadTime);

		target.updateStatus(expectedSavedStatus);

		// Assert：結果が正しいこと
	}
}
