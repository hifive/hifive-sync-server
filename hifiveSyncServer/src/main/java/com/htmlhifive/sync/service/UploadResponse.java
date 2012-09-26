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
package com.htmlhifive.sync.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.htmlhifive.sync.resource.ResourceItemWrapper;

/**
 * クライアントからの上り更新リクエストに対するレスポンスデータ.
 *
 * @author kishigam
 */
public class UploadResponse {

	/**
	 * 上り更新共通データ.<br>
	 * このレスポンスにおいてクライアントへ返す情報を保持しています.
	 */
	private UploadCommonData uploadCommonData;

	/**
	 * 競合リソースアイテムのリスト.<br>
	 * リソース別にリストを保持します.<br>
	 * このフィールドは競合発生時のみ、競合データとしてクライアントへのレスポンスに含みます.<br>
	 * nullのときレスポンスから除外するため、このフィールドのgetterメソッドに{@link JsonSerialize}を追加しています)
	 */
	private Map<String, List<ResourceItemWrapper>> resourceItems;

	/**
	 * 上り更新共通データを指定して上り更新レスポンスを生成します.
	 *
	 * @param downloadCommonData 下り更新共通データ
	 */
	public UploadResponse(UploadCommonData uploadCommonData) {
		this.uploadCommonData = uploadCommonData;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;

		if (!(obj instanceof UploadResponse))
			return false;

		UploadResponse req = (UploadResponse) obj;

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
	 * nullのときレスポンスから除外するため、{@link JsonSerialize}を追加しています).
	 *
	 * @return resourceItems
	 */
	@JsonSerialize(include = Inclusion.NON_NULL)
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
