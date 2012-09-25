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
package com.htmlhifive.sync.jsonctrl.upload;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.htmlhifive.sync.exception.ConflictException;
import com.htmlhifive.sync.resource.ResourceItemWrapper;
import com.htmlhifive.sync.resource.SyncResultType;

/**
 * クライアントからの上り更新リクエストに対する競合発生時のレスポンスデータ.<br>
 * このクラスが保持するリソースアイテムリスト内には、リソースの重複およびリソースアイテムの重複はありません.
 *
 * @author kishigam
 */
public class UploadResponseOnConflict extends UploadResponse {

	/**
	 * 競合タイプ.
	 */
	private SyncResultType conflictType;

	/**
	 * 競合したリソースアイテムのリスト.<br>
	 * リソース別にリストを保持します.
	 */
	private Map<String, List<ResourceItemWrapper>> resourceItems;

	/**
	 * ConflictExceptionの情報から上り更新レスポンスを生成します.
	 *
	 * @param e ConflictExceptionインスタンス
	 */
	public UploadResponseOnConflict(ConflictException e) {

		this.conflictType = e.getConflictType();
		this.resourceItems = e.getResourceItems();
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;

		if (!(obj instanceof UploadResponseOnConflict))
			return false;

		UploadResponseOnConflict req = (UploadResponseOnConflict) obj;

		return new EqualsBuilder().append(this.conflictType, req.conflictType)
				.append(this.resourceItems, req.resourceItems).isEquals();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(this.conflictType).append(this.resourceItems).hashCode();
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * @return conflictType
	 */
	public SyncResultType getConflictType() {
		return conflictType;
	}

	/**
	 * @param conflictType セットする conflictType
	 */
	public void setConflictType(SyncResultType conflictType) {
		this.conflictType = conflictType;
	}

	/**
	 * @return resourceItems
	 */
	public Map<String, List<ResourceItemWrapper>> getResourceItems() {
		return resourceItems;
	}

	/**
	 * @param resourceItems セットする resourceItems
	 */
	public void setResourceItems(Map<String, List<ResourceItemWrapper>> resourceItems) {
		this.resourceItems = resourceItems;
	}
}
