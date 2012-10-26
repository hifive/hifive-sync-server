package com.htmlhifive.sync.resource.test;

import java.util.List;
import java.util.Properties;

import com.htmlhifive.sync.resource.ResourceItemConverter;
import com.htmlhifive.sync.resource.ResourceItemWrapper;
import com.htmlhifive.sync.resource.ResourceQueryConditions;
import com.htmlhifive.sync.resource.SyncResource;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.resource.lock.LockStrategy;
import com.htmlhifive.sync.resource.lock.ResourceLockStatusType;
import com.htmlhifive.sync.resource.update.UpdateStrategy;
import com.htmlhifive.sync.service.SyncCommonData;
import com.htmlhifive.sync.service.lock.LockCommonData;
import com.htmlhifive.sync.service.upload.UploadCommonData;

/**
 * テスト用抽象{@link SyncResource}実装クラス.
 */
@SuppressWarnings("deprecation")
public abstract class TestAbstractSyncResourceAnother implements SyncResource<String> {
	@Override
	public List<ResourceItemWrapper<String>> get(SyncCommonData syncCommon,
			List<ResourceItemCommonData> itemCommonDataList) {
		return null;
	}

	@Override
	public List<ResourceItemWrapper<String>> getByQuery(SyncCommonData syncCommon, ResourceQueryConditions query) {
		return null;
	}

	@Override
	public ResourceItemWrapper<String> create(UploadCommonData uploadCommon, ResourceItemCommonData itemCommon,
			String item) {
		return null;
	}

	@Override
	public ResourceItemWrapper<String> update(UploadCommonData uploadCommon, ResourceItemCommonData itemCommon,
			String item) {
		return null;
	}

	@Override
	public ResourceItemWrapper<String> delete(UploadCommonData uploadCommon, ResourceItemCommonData itemCommon) {
		return null;
	}

	@Override
	public List<ResourceItemWrapper<String>> lock(LockCommonData lockCommon,
			List<ResourceItemCommonData> itemCommonDataList) {
		return null;
	}

	@Override
	public void releaseLock(LockCommonData lockCommon, List<ResourceItemCommonData> itemCommonList) {
	}

	@Override
	public List<ResourceItemCommonData> lockedItemsList(LockCommonData lockCommonData) {
		return null;
	}

	@Override
	public List<ResourceItemCommonData> forUpdate(List<ResourceItemCommonData> itemCommonList) {
		return null;
	}

	@Override
	public String name() {
		return null;
	}

	@Override
	public Class<String> itemType() {
		return null;
	}

	@Override
	public ResourceLockStatusType requiredLockStatus() {
		return null;
	}

	@Override
	public ResourceItemConverter<String> itemConverter() {
		return null;
	}

	@Override
	public void applyResourceConfigurations(Properties resourceConfigurations) {
	}

	@Override
	public void setRequiredLockStatus(ResourceLockStatusType requiredLockStatus) {
	}

	@Override
	public void setLockStrategy(LockStrategy lockManager) {
	}

	@Override
	public void setUpdateStrategy(UpdateStrategy updateStrategy) {
	}
}
