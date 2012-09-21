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
package com.htmlhifive.sync.resource;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.htmlhifive.sync.commondata.CommonData;

/**
 * リソースへの同期リクエストに対する同期レスポンスのデータクラス.<br>
 *
 * @author kishigam
 * @param <E> エレメントのデータ型
 */
public class SyncResponse<E> {

	/**
	 * 同期レスポンスの共通データ.
	 */
	private CommonData common;

	/**
	 * 同期レスポンスのリソースエレメント.
	 */
	private E element;

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (!(obj instanceof SyncResponse))
			return false;

		SyncResponse<?> res = (SyncResponse<?>) obj;

		return new EqualsBuilder().append(this.common, res.common).append(this.element, res.element).isEquals();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(this.common).append(this.element).hashCode();
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * 同期レスポンスオブジェクトを生成します.
	 *
	 * @param common 共通データ
	 * @param element リソースエレメント
	 */
	public SyncResponse(CommonData common, E element) {
		this.common = common;
		this.element = element;
	}

	/**
	 * @return common
	 */
	public CommonData getCommon() {
		return common;
	}

	/**
	 * @param common セットする common
	 */
	public void setCommon(CommonData common) {
		this.common = common;
	}

	/**
	 * @return element
	 */
	public E getElement() {
		return element;
	}

	/**
	 * @param element セットする element
	 */
	public void setElement(E element) {
		this.element = element;
	}
}
