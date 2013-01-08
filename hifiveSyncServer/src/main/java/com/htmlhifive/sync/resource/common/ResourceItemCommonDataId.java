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
package com.htmlhifive.sync.resource.common;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * {@link ResourceItemCommonData 共通データクラス}のIDクラス.
 *
 * @author kishigam
 */
@Embeddable
public class ResourceItemCommonDataId implements Serializable, Comparable<ResourceItemCommonDataId> {

	private static final long serialVersionUID = -4867979788750226686L;

	/**
	 * リソース名.<br>
	 */
	private String resourceName;

	/**
	 * リソースアイテムのID.
	 */
	private String resourceItemId;

	/**
	 * アプリケーションからは使用できないプライベートデフォルトコンストラクタ.<br>
	 * 永続化マネージャが使用します.
	 */
	@SuppressWarnings("unused")
	private ResourceItemCommonDataId() {
	}

	/**
	 * リソース名とリソースアイテムIDからリソースアイテム共通データIDオブジェクトを生成します.
	 *
	 * @param resourceName リソース名
	 * @param resourceItemId リソースアイテムID
	 */
	public ResourceItemCommonDataId(String resourceName, String resourceItemId) {
		this.resourceName = resourceName;
		this.resourceItemId = resourceItemId;
	}

	/**
	 * リソース名、リソースアイテムIDの順序で比較します.
	 *
	 * @param o 比較対象
	 * @return 比較結果
	 */
	@Override
	public int compareTo(ResourceItemCommonDataId o) {

		int compare1 = this.resourceName.compareTo(o.resourceName);

		return compare1 != 0 ? compare1 : this.resourceItemId.compareTo(o.resourceItemId);
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (!(obj instanceof ResourceItemCommonDataId))
			return false;

		ResourceItemCommonDataId id = (ResourceItemCommonDataId) obj;

		return new EqualsBuilder().append(this.resourceName, id.resourceName)
				.append(this.resourceItemId, id.resourceItemId).isEquals();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(this.resourceName).append(this.resourceItemId).hashCode();
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * @return resourceName
	 */
	public String getResourceName() {
		return resourceName;
	}

	/**
	 * @return resourceItemId
	 */
	public String getResourceItemId() {
		return resourceItemId;
	}
}