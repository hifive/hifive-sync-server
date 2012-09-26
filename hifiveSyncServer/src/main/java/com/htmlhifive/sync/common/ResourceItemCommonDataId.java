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
package com.htmlhifive.sync.common;

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
public class ResourceItemCommonDataId implements Serializable {

	/**
	 * シリアルバージョンUID.
	 */
	private static final long serialVersionUID = -3582140249142802398L;

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

	public ResourceItemCommonDataId(String resourceName, String resourceItemId) {
		this.resourceName = resourceName;
		this.resourceItemId = resourceItemId;
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

		return EqualsBuilder.reflectionEquals(this, ((ResourceItemCommonDataId) obj));
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return HashCodeBuilder.reflectionHashCode(this);
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