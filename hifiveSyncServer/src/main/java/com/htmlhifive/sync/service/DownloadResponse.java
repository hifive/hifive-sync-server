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

import com.htmlhifive.sync.resource.ResourceItemWrapper;

/**
 * クライアントからの下り更新リクエストに対するレスポンスデータクラス.<br>
 * このクラスが保持するリソースアイテムリスト内には、リソースの重複およびリソースアイテムの重複はありません.
 *
 * @author kishigam
 */
public class DownloadResponse {

	/**
	 * 下り更新共通データ.<br>
	 * このレスポンスにおいてクライアントへ返す情報を保持しています.
	 */
	private DownloadCommonData downloadCommonData;

	/**
	 * 下り更新結果のリソースアイテムリスト. <br>
	 * リソース別にリストを保持します.
	 */
	private Map<String, List<ResourceItemWrapper>> resourceItems;

	/**
	 * 下り更新共通データを指定して下り更新レスポンスを生成します.
	 *
	 * @param downloadCommonData 下り更新共通データ
	 */
	public DownloadResponse(DownloadCommonData downloadCommonData) {
		this.downloadCommonData = downloadCommonData;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;

		if (!(obj instanceof DownloadResponse))
			return false;

		DownloadResponse req = (DownloadResponse) obj;

		return new EqualsBuilder().append(this.downloadCommonData, req.downloadCommonData)
				.append(this.resourceItems, req.resourceItems).isEquals();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(this.downloadCommonData).append(this.resourceItems).hashCode();
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * @return downloadCommonData
	 */
	public DownloadCommonData getDownloadCommonData() {
		return downloadCommonData;
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