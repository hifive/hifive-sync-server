/**
 *
 */
package com.htmlhifive.sync.resource.common;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import mockit.Mocked;

import org.junit.Test;

import com.htmlhifive.sync.resource.SyncAction;
import com.htmlhifive.sync.resource.SyncConflictType;

/**
 * <H3>ResourceItemCommonDataのテストクラス.</H3>
 *
 * @author kishigam
 */
public class ResourceItemCommonDataTest {

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(ResourceItemCommonData.class, notNullValue());
	}

	/**
	 * {@link ResourceItemCommonData#ResourceItemCommonData()}用テストメソッド.
	 */
	@Test
	public void testInstantiation() {
		String targetItemId = "targetItemId1";
		ResourceItemCommonDataId id = new ResourceItemCommonDataId("resourceName1", "resourceItemId1");

		ResourceItemCommonData target = new ResourceItemCommonData(id, targetItemId);
		assertThat(target, notNullValue());
		assertThat(target.getId(), is(equalTo(id)));
		assertThat(target.getTargetItemId(), is(equalTo(target.getTargetItemId())));
	}

	/**
	 * {@link ResourceItemCommonData#compareTo(ResourceItemCommonData)}用テストメソッド.
	 */
	@Test
	public void testCompareToResourceItemCommonData() {

		// Arrange：正常系
		ResourceItemCommonData target = new ResourceItemCommonData(new ResourceItemCommonDataId("b", "2"), "target");

		ResourceItemCommonData gt1 = new ResourceItemCommonData(new ResourceItemCommonDataId("a", "2"), "gt1");
		ResourceItemCommonData gt2 = new ResourceItemCommonData(new ResourceItemCommonDataId("b", "1"), "gt2");
		ResourceItemCommonData eq = new ResourceItemCommonData(new ResourceItemCommonDataId("b", "2"), "eq");
		ResourceItemCommonData lt1 = new ResourceItemCommonData(new ResourceItemCommonDataId("b", "3"), "lt1");
		ResourceItemCommonData lt2 = new ResourceItemCommonData(new ResourceItemCommonDataId("c", "1"), "lt2");

		assertThat(target.compareTo(gt1) > 0, is(true));
		assertThat(target.compareTo(gt2) > 0, is(true));
		assertThat(target.compareTo(eq) == 0, is(true));
		assertThat(target.compareTo(lt1) < 0, is(true));
		assertThat(target.compareTo(lt2) < 0, is(true));
	}

	/**
	 * {@link ResourceItemCommonData#equals(Object)}用テストメソッド.
	 */
	@Test
	public void testEqualsObject() {

		ResourceItemCommonData target = new ResourceItemCommonData(new ResourceItemCommonDataId("resourceName0",
				"resourceItemId0"), "targetItemId0");
		target.setAction(SyncAction.CREATE);
		target.setLastModified(0);

		ResourceItemCommonData eq = new ResourceItemCommonData(new ResourceItemCommonDataId(target.getId()
				.getResourceName(), target.getId().getResourceItemId()), target.getTargetItemId());
		eq.setAction(target.getAction());
		eq.setLastModified(target.getLastModified());
		eq.setConflictType(SyncConflictType.UPDATED);
		assertThat(target, is(equalTo(eq)));

		eq.setConflictType(target.getConflictType());
		eq.setForUpdate(false);
		assertThat(target, is(equalTo(eq)));

		eq.setForUpdate(target.isForUpdate());
		eq.setKeepLock(false);
		assertThat(target, is(equalTo(eq)));

		ResourceItemCommonData ne1 = new ResourceItemCommonData(new ResourceItemCommonDataId("resourceName1", target
				.getId().getResourceItemId()), target.getTargetItemId());
		ne1.setAction(target.getAction());
		ne1.setLastModified(target.getLastModified());
		assertThat(target, is(not(equalTo(ne1))));

		ResourceItemCommonData ne2 = new ResourceItemCommonData(new ResourceItemCommonDataId(target.getId()
				.getResourceName(), "resourceItemId1"), target.getTargetItemId());
		ne2.setAction(target.getAction());
		ne2.setLastModified(target.getLastModified());
		assertThat(target, is(not(equalTo(ne2))));

		ResourceItemCommonData ne3 = new ResourceItemCommonData(new ResourceItemCommonDataId(target.getId()
				.getResourceName(), target.getId().getResourceItemId()), "targetItemId1");
		ne3.setAction(target.getAction());
		ne3.setLastModified(target.getLastModified());
		assertThat(target, is(not(equalTo(ne3))));

		ResourceItemCommonData ne4 = new ResourceItemCommonData(new ResourceItemCommonDataId(target.getId()
				.getResourceName(), target.getId().getResourceItemId()), target.getTargetItemId());
		eq.setAction(SyncAction.UPDATE);
		eq.setLastModified(target.getLastModified());
		assertThat(target, is(not(equalTo(ne4))));

		ResourceItemCommonData ne5 = new ResourceItemCommonData(new ResourceItemCommonDataId(target.getId()
				.getResourceName(), target.getId().getResourceItemId()), target.getTargetItemId());
		eq.setAction(target.getAction());
		eq.setLastModified(1);
		assertThat(target, is(not(equalTo(ne5))));
	}

	/**
	 * {@link ResourceItemCommonData#modify(SyncAction, long)}用テストメソッド.
	 *
	 * @param actionToModify 引数のモック
	 */
	@Test
	public void testModifySyncActionlong(@Mocked final SyncAction actionToModify) {

		// Arrange：正常系
		ResourceItemCommonData target = new ResourceItemCommonData(new ResourceItemCommonDataId("resourceName",
				"resourceItemId"), "targetItemId");
		target.setAction(SyncAction.CREATE);
		target.setLastModified(0);

		SyncAction expectedAction = SyncAction.UPDATE;
		long expectedLastModified = 1;

		// Act
		target.modify(expectedAction, expectedLastModified);

		// Assert：結果が正しいこと
		assertThat(target.getAction(), is(equalTo(expectedAction)));
		assertThat(target.getLastModified(), is(equalTo(expectedLastModified)));
	}
}