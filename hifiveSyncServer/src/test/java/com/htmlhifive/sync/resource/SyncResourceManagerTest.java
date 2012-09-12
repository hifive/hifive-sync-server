/**
 *
 */
package com.htmlhifive.sync.resource;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mockit.Expectations;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.htmlhifive.sync.sample.person.PersonResource;
import com.htmlhifive.sync.sample.scd.ScheduleResource;

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
	private final SyncResourceManager target = new SyncResourceManager();

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(SyncResourceManager.class, notNullValue());
	}

	/**
	 * {@link SyncResourceManager#locateSyncResource()}用テストメソッド.
	 */
	@Test
	public void testLocateSyncResource() {

		// Arrange：正常系
		final String dataModelName = "person";
		final Class<PersonResource> expectedClass = PersonResource.class;
		final PersonResource expected = new PersonResource();

		new Expectations() {
			Map<String, Class<? extends SyncResource<?>>> resourceMap;
			ApplicationContext context;
			{
				setField(target, "resourceMap", resourceMap);
				setField(target, context);

				resourceMap.get(dataModelName);
				result = expectedClass;

				context.getBean(expectedClass);
				result = expected;
			}
		};

		// Act
		SyncResource<?> actual = target.locateSyncResource(dataModelName);

		// Assert：結果が正しいこと
		assertThat((PersonResource) actual, is(expected));
	}

	/**
	 * {@link SyncResourceManager#locateSyncResource()}用テストメソッド.<br>
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
	 * {@link SyncResourceManager#locateSyncResource()}用テストメソッド.<br>
	 * nullが与えられた場合nullを返す.
	 */
	public void testCannotLocateSyncResourceBecauseNullInput() {

		// Act
		SyncResource<?> actual = target.locateSyncResource(null);

		// Assert：結果が正しいこと
		assertThat(actual, nullValue());
	}

	/**
	 * {@link SyncResourceManager#getAllDataModelNames()}用テストメソッド.
	 */
	@Test
	public void testGetAllDataModelNames() {

		// Arrange：正常系
		@SuppressWarnings("serial")
		final Set<String> expected = new HashSet<String>() {
			{
				add("dataModel1");
				add("dataModel2");
			}
		};

		new Expectations() {
			Map<String, Class<? extends SyncResource<?>>> resourceMap;
			{
				setField(target, "resourceMap", resourceMap);

				resourceMap.keySet();
				result = expected;
			}
		};

		// Act
		Set<String> actual = target.getAllDataModelNames();

		// Assert：結果が正しいこと
		assertThat(actual, is(expected));
	}

	/**
	 * {@link SyncResourceManager#SyncResourceManager()}用テストメソッド.<br>
	 * リソースの作成、修正に伴い結果が変わる.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testInstantiation() {

		Set<String> expectedResourceMap = new HashSet<String>() {
			private static final long serialVersionUID = 1L;
			{
				add("person");
				add("schedule");
			}
		};

		new Expectations() {
			ApplicationContext context;
			{
				setField(target, context);

				context.getBean(PersonResource.class);
				result = new PersonResource();

				context.getBean(ScheduleResource.class);
				result = new ScheduleResource();
			}
		};

		assertThat(target, notNullValue());
		assertThat(target.getAllDataModelNames(), is(expectedResourceMap));
		assertThat(
				(Class<OptimisticLockManager>) target.locateSyncResource("person").getClass()
						.getAnnotation(SyncResourceService.class).lockManager(),
				is(equalTo(OptimisticLockManager.class)));
		assertThat(
				(Class<OptimisticLockManager>) target.locateSyncResource("schedule").getClass()
						.getAnnotation(SyncResourceService.class).lockManager(),
				is(equalTo(OptimisticLockManager.class)));
	}

}
