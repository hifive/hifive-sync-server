/**
 *
 */
package com.htmlhifive.sync.resource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Map;

import mockit.Expectations;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.htmlhifive.sync.sample.person.PersonResource;

/**
 * <H3>SyncResourceManagerのテストクラス.</H3>
 *
 * @author kishigam
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class SyncResourceManagerTest {

	/**
	 * コンストラクタ以外のテストに使用するターゲットクラスインスタンス.
	 */
	private final SyncResourceManager target = new SyncResourceManagerImpl();

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(SyncResourceManagerImpl.class, notNullValue());
	}

	/**
	 * {@link SyncResourceManagerImpl#locateSyncResource()}用テストメソッド.
	 */
	@Test
	public void testLocateSyncResource() {

		// Arrange：正常系
		final String dataModelName = "person";
		final Class<PersonResource> expectedResourceClass = PersonResource.class;

		final Class<? extends LockManager> expectedLockManagerClass = OptimisticLockManager.class;
		final LockManager expectedLockManager = new OptimisticLockManager();

		final Class<? extends UpdateStrategy> expectedUpdateStrategyClass = ClientResolvingStrategy.class;
		final UpdateStrategy expectedUpdateStrategy = new ClientResolvingStrategy();

		final PersonResource expectedResource = new PersonResource();
		expectedResource.setLockManager(expectedLockManager);
		expectedResource.setUpdateStrategy(expectedUpdateStrategy);

		new Expectations() {
			Map<String, Class<? extends SyncResource<?>>> resourceMap;
			Map<String, Class<? extends LockManager>> lockManagerMap;
			Map<String, Class<? extends UpdateStrategy>> updateStrategyMap;
			ApplicationContext context;
			{
				setField(target, "resourceMap", resourceMap);
				setField(target, "lockManagerMap", lockManagerMap);
				setField(target, "updateStrategyMap", updateStrategyMap);
				setField(target, context);

				resourceMap.get(dataModelName);
				result = expectedResourceClass;

				context.getBean(expectedResourceClass);
				result = expectedResource;

				lockManagerMap.get(dataModelName);
				result = expectedLockManagerClass;

				context.getBean(expectedLockManagerClass);
				result = expectedLockManager;

				updateStrategyMap.get(dataModelName);
				result = expectedUpdateStrategyClass;

				context.getBean(expectedUpdateStrategyClass);
				result = expectedUpdateStrategy;
			}
		};

		// Act
		SyncResource<?> actual = target.locateSyncResource(dataModelName);

		// Assert：結果が正しいこと
		assertThat((PersonResource) actual, is(expectedResource));
	}

	/**
	 * {@link SyncResourceManagerImpl#locateSyncResource()}用テストメソッド.<br>
	 * データモデルに対するリソースが見つからない場合nullを返す.
	 */
	@Test
	public void testCannotLocateSyncResourceBecauseNotFound() {

		// Arrange：例外系
		final String notFoundDataModelName = "not found";

		new Expectations() {
			Map<String, Class<? extends SyncResource<?>>> resourceMap;
			{
				setField(target, "resourceMap", resourceMap);

				resourceMap.get(notFoundDataModelName);
				result = null;
			}
		};

		// Act
		SyncResource<?> actual = target.locateSyncResource(notFoundDataModelName);

		// Assert：結果が正しいこと
		assertThat(actual, nullValue());
	}

	/**
	 * {@link SyncResourceManagerImpl#locateSyncResource()}用テストメソッド.<br>
	 * nullが与えられた場合nullを返す.
	 */
	public void testCannotLocateSyncResourceBecauseNullInput() {

		// Act
		SyncResource<?> actual = target.locateSyncResource(null);

		// Assert：結果が正しいこと
		assertThat(actual, nullValue());
	}
}
