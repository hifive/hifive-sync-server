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
package com.htmlhifive.sync.service.download;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.htmlhifive.sync.resource.ResourceQueryConditions;

/**
 * 下り更新リクエスト内容全体を表現するデータクラス.
 *
 * @author kishigam
 */
public class DownloadRequest {

	/**
	 * 下り更新共通データ.<br>
	 * このリクエストにおいてクライアントから渡される情報を保持しています.
	 */
	private DownloadCommonData downloadCommonData;

	/**
	 * 下り更新対象を表すクエリリストのMap.<br>
	 * リソース別にクエリリストを保持します.
	 */
	private Map<String, List<ResourceQueryConditions>> queries;

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (!(obj instanceof DownloadRequest))
			return false;

		DownloadRequest request = (DownloadRequest) obj;

		return new EqualsBuilder().append(this.downloadCommonData, request.downloadCommonData)
				.append(this.queries, request.queries).isEquals();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(this.downloadCommonData).append(this.queries).hashCode();
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
	 * @param downloadCommonData セットする downloadCommonData
	 */
	public void setDownloadCommonData(DownloadCommonData downloadCommonData) {
		this.downloadCommonData = downloadCommonData;
	}

	/**
	 * @return queries
	 */
	public Map<String, List<ResourceQueryConditions>> getQueries() {
		return queries;
	}

	/**
	 * @param queries セットする queries
	 */
	public void setQueries(Map<String, List<ResourceQueryConditions>> queries) {
		this.queries = queries;
	}
}