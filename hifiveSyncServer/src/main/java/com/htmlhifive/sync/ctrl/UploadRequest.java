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
package com.htmlhifive.sync.ctrl;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.htmlhifive.sync.resource.ResourceItemWrapper;

/**
 * JSON形式の上り更新リクエスト内容全体を表現するデータクラス.<br>
 * 上り更新データには、同じリソース、および同じリソースアイテムが複数個存在します. <br>
 * また、これらは順序を守って処理されなければなりません. そのため、MultiMapオブジェクトでリソース別のリストを保持します.<br>
 * リソース別のリソースアイテムをSetで保持することや、それをリソース名をキーとするHashMap等に保持することはできません.
 *
 * @author kishigam
 */
public class UploadRequest {

	/**
	 * 最終上り更新時刻.
	 */
	private long lastUploadTime;

	/**
	 * 上り更新対象のリソースアイテムリスト.<br>
	 * 上り更新リクエストデータでは、リソースアイテムのラッパーオブジェクトにリソース名を持ちます.
	 */
	private List<ResourceItemWrapper> resourceItems;

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

		return new EqualsBuilder().append(this.lastUploadTime, req.lastUploadTime)
				.append(this.resourceItems, req.resourceItems).isEquals();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(this.lastUploadTime).append(this.resourceItems).hashCode();
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * @return lastUploadTime
	 */
	public long getLastUploadTime() {
		return lastUploadTime;
	}

	/**
	 * @param lastUploadTime セットする lastUploadTime
	 */
	public void setLastUploadTime(long lastUploadTime) {
		this.lastUploadTime = lastUploadTime;
	}

	/**
	 * @return resourceItems
	 */
	public List<ResourceItemWrapper> getResourceItems() {
		return resourceItems;
	}

	/**
	 * @param resourceItems セットする resourceItems
	 */
	public void setResourceItems(List<ResourceItemWrapper> resourceItems) {
		this.resourceItems = resourceItems;
	}
}