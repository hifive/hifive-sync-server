package com.htmlhifive.sync.resource.common;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.Table;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.htmlhifive.sync.resource.SyncAction;

/**
 * <H3>ResourceItemCommonDataRepositoryCustomのテストクラス.</H3>
 *
 * @author kishigam
 */
@ContextConfiguration(locations = "classpath:test-context.xml")
@TransactionConfiguration(transactionManager = "txManager")
public class ResourceItemCommonDataRepositoryCustomTest extends AbstractTransactionalJUnit4SpringContextTests {

	private static final String TABLE_NAME = ResourceItemCommonData.class.getAnnotation(Table.class).name();

	@Resource
	private ResourceItemCommonDataRepository target;

	private ResourceItemCommonData data1;
	private ResourceItemCommonData data2;
	private ResourceItemCommonData data3;

	@Before
	public void setUp() {

		deleteFromTables(TABLE_NAME);

		data1 = new ResourceItemCommonData(new ResourceItemCommonDataId("r1", "ri11"), "r1-1");
		data2 = new ResourceItemCommonData(new ResourceItemCommonDataId("r2", "ri21"), "r2-1");
		data3 = new ResourceItemCommonData(new ResourceItemCommonDataId("r1", "ri12"), "r1-2");

		data1.setAction(SyncAction.CREATE);
		data1.setLastModified(10);

		data2.setAction(SyncAction.UPDATE);
		data2.setLastModified(20);

		data3.setAction(SyncAction.DELETE);
		data3.setLastModified(30);

		target.save(data1);
		target.save(data2);
		target.save(data3);
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryCustom#findOneForUpdate(ResourceItemCommonDataId)}用テストメソッド.
	 */
	@Test
	public void testFindOneForUpdate() {

		ResourceItemCommonDataId id = new ResourceItemCommonDataId("r2", "ri21");

		ResourceItemCommonData expected = target.findOne(id);
		ResourceItemCommonData actual = target.findOneForUpdate(id);

		// EntityManager.getLockMode(actual)ではロックモードの取得結果が正しくない？
		// 目視で for update されていることは確認済み
		assertThat(actual, is(equalTo(expected)));
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryCustom#findModified(String, long)}用テストメソッド.
	 */
	@Test
	public void testFindModified() {

		List<ResourceItemCommonData> actual = target.findModified("r1", 15);
		assertThat(actual.size(), is(equalTo(1)));
		assertThat(actual.contains(data3), is(equalTo(true)));

		List<ResourceItemCommonData> actual2 = target.findModified("r1", 100);
		assertThat(actual2.isEmpty(), is(equalTo(true)));
	}

	/**
	 * {@link ResourceItemCommonDataRepositoryCustom#findByTargetItemId(String, String)}用テストメソッド.
	 */
	@Test
	public void testFindByTargetItemId() {

		ResourceItemCommonData actual = target.findByTargetItemId("r2", "r2-1");
		assertThat(actual, is(equalTo(data2)));

		ResourceItemCommonData actual2 = target.findByTargetItemId("r2", "r555");
		assertThat(actual2, is(nullValue()));
	}
}