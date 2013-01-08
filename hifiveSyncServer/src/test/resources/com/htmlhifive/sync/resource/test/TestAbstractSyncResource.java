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
package com.htmlhifive.sync.resource.test;

import java.util.List;
import java.util.Properties;

import com.htmlhifive.sync.resource.ResourceItemConverter;
import com.htmlhifive.sync.resource.ResourceItemWrapper;
import com.htmlhifive.sync.resource.ResourceQueryConditions;
import com.htmlhifive.sync.resource.SyncResource;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.resource.update.UpdateStrategy;
import com.htmlhifive.sync.service.SyncCommonData;
import com.htmlhifive.sync.service.upload.UploadCommonData;

/**
 * テスト用抽象{@link SyncResource}実装クラス.
 */
public abstract class TestAbstractSyncResource implements SyncResource<String> {
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
	public ResourceItemConverter<String> itemConverter() {
		return null;
	}

	@Override
	public void applyResourceConfigurations(Properties resourceConfigurations) {
	}

	@Override
	public void setUpdateStrategy(UpdateStrategy updateStrategy) {
	}
}
