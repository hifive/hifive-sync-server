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

import com.htmlhifive.sync.common.ResourceItemCommonData;

/**
 * リソースアイテム1件の情報を保持するデータラッパー.<br>
 * サーバ上の1つのリソースアイテムに関する共通情報、およびリソースアイテムそのもののオブジェクトを保持します.
 *
 * @param <I> リソースアイテムの型
 */
public class ResourceItemWrapper<I> implements Comparable<ResourceItemWrapper<I>> {

	/**
	 * リソースアイテム共通データ.
	 */
	private ResourceItemCommonData itemCommonData;

	/**
	 * このデータが表現するリソースアイテムの本体.
	 */
	private I item;

	/**
	 * フレームワーク、ライブラリが使用するプライベートデフォルトコンストラクタ.
	 */
	@SuppressWarnings("unused")
	private ResourceItemWrapper() {
	}

	/**
	 * アイテム共通データとアイテムを指定してデータラッパーを生成します.
	 *
	 * @param itemCommonData リソースアイテム共通データ
	 * @param item リソースアイテムオブジェクト
	 */
	public ResourceItemWrapper(ResourceItemCommonData itemCommonData, I item) {
		this.itemCommonData = itemCommonData;
		this.item = item;
	}

	/**
	 * リソースアイテム共通データ順序で比較します.
	 */
	@Override
	public int compareTo(ResourceItemWrapper<I> o) {

		return this.itemCommonData.compareTo(o.getItemCommonData());
	}

	/**
	 * @see Object#equals(Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (!(obj instanceof ResourceItemWrapper))
			return false;

		ResourceItemWrapper<I> itemWrapper = (ResourceItemWrapper<I>) obj;

		return new EqualsBuilder().append(this.itemCommonData, itemWrapper.itemCommonData)
				.append(this.item, itemWrapper.item).isEquals();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(this.itemCommonData).append(this.item).hashCode();
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * @return itemCommonData
	 */
	public ResourceItemCommonData getItemCommonData() {
		return itemCommonData;
	}

	/**
	 * @param itemCommonData セットする itemCommonData
	 */
	public void setItemCommonData(ResourceItemCommonData itemCommonData) {
		this.itemCommonData = itemCommonData;
	}

	/**
	 * @return item
	 */
	public I getItem() {
		return item;
	}

	/**
	 * @param item セットする item
	 */
	public void setItem(I item) {
		this.item = item;
	}
}
