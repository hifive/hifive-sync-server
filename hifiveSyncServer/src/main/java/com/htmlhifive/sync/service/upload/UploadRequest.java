/*
 * Copyright (C) 2012 NS Solutions Corporation
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
package com.htmlhifive.sync.service.upload;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.htmlhifive.sync.resource.ResourceItemWrapper;

/**
 * 上り更新リクエスト内容全体を表現するデータクラス.
 *
 * @author kishigam
 */
public class UploadRequest {

	/**
	 * 上り更新共通データ.<br>
	 * このリクエストにおいてクライアントから渡される情報を保持しています.
	 */
	private UploadCommonData uploadCommonData;

	/**
	 * 上り更新対象を表すリソースアイテムリストのMap.<br>
	 */
	private List<? extends ResourceItemWrapper<?>> resourceItems;

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;

		if (!(obj instanceof UploadRequest))
			return false;

		UploadRequest req = (UploadRequest) obj;

		return new EqualsBuilder().append(this.uploadCommonData, req.uploadCommonData)
				.append(this.resourceItems, req.resourceItems).isEquals();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(this.uploadCommonData).append(this.resourceItems).hashCode();
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * @return uploadCommonData
	 */
	public UploadCommonData getUploadCommonData() {
		return uploadCommonData;
	}

	/**
	 * @param uploadCommonData セットする uploadCommonData
	 */
	public void setUploadCommonData(UploadCommonData uploadCommonData) {
		this.uploadCommonData = uploadCommonData;
	}

	/**
	 * @return resourceItems
	 */
	public List<? extends ResourceItemWrapper<?>> getResourceItems() {
		return resourceItems;
	}

	/**
	 * @param resourceItems セットする resourceItems
	 */
	public void setResourceItems(List<? extends ResourceItemWrapper<?>> resourceItems) {
		this.resourceItems = resourceItems;
	}
}