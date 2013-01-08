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
package com.htmlhifive.sync.resource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * リソースに更新されたアイテムを要求するための問い合わせ条件を保持するクラス.
 *
 * @author kishigam
 */
public class ResourceQueryConditions implements Serializable {

	private static final long serialVersionUID = 1692199470282648825L;

	/**
	 * リソースごとにダウンロード対象アイテムを絞り込む条件. <br>
	 * 対象項目と条件(配列形式)のMapで表現されます.
	 */
	private Map<String, String[]> conditions = new HashMap<>();

	/**
	 * 前回下り更新時刻.<br>
	 * この時刻以降の更新データが取得対象になります.
	 */
	private long lastDownloadTime = 0L;

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;

		if (!(obj instanceof ResourceQueryConditions))
			return false;

		ResourceQueryConditions queryConditions = (ResourceQueryConditions) obj;

		return new EqualsBuilder().append(this.conditions, queryConditions.conditions)
				.append(this.lastDownloadTime, queryConditions.lastDownloadTime).isEquals();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return new HashCodeBuilder().append(this.conditions).append(this.lastDownloadTime).hashCode();
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * @return conditions
	 */
	public Map<String, String[]> getConditions() {
		return conditions;
	}

	/**
	 * @param conditions セットする conditions
	 */
	public void setConditions(Map<String, String[]> conditions) {
		this.conditions = conditions;
	}

	/**
	 * @return lastDownloadTime
	 */
	public long getLastDownloadTime() {
		return lastDownloadTime;
	}

	/**
	 * @param lastDownloadTime セットする lastDownloadTime
	 */
	public void setLastDownloadTime(long lastDownloadTime) {
		this.lastDownloadTime = lastDownloadTime;
	}
}
