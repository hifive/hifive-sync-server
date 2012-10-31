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
package com.htmlhifive.sync.service.lock;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.htmlhifive.sync.resource.ResourceQueryConditions;

/**
 * ロック取得リクエスト内容全体を表現するデータクラス.<br>
 * TODO 次期バージョンにて実装予定
 *
 * @author kishigam
 */
@SuppressWarnings("deprecation")
@Deprecated
public class LockRequest implements Serializable {

	private static final long serialVersionUID = 365885989955291242L;

	/**
	 * ロック取得共通データ.<br>
	 * このリクエストにおいてクライアントから渡される情報を保持しています.
	 */
	private LockCommonData lockCommonData;

	/**
	 * ロック対象のクエリリストのMap.<br>
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
		if (!(obj instanceof LockRequest))
			return false;

		LockRequest request = (LockRequest) obj;

		return new EqualsBuilder().append(this.lockCommonData, request.lockCommonData)
				.append(this.queries, request.queries).isEquals();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(this.lockCommonData).append(this.queries).hashCode();
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {

		return new ToStringBuilder(this).append(this.lockCommonData).append(this.queries).toString();
	}

	/**
	 * @return lockCommonData
	 */
	public LockCommonData getLockCommonData() {
		return lockCommonData;
	}

	/**
	 * @param lockCommonData セットする lockCommonData
	 */
	public void setLockCommonData(LockCommonData lockCommonData) {
		this.lockCommonData = lockCommonData;
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
