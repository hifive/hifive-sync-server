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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.htmlhifive.sync.resource.ResourceItemWrapper;

/**
 * リソースごとのリソースアイテムリストを保持するコンテナ.
 *
 * @author kishigam
 */
public class ResourceItemsContainer {

	/**
	 * リソース名.
	 */
	private String resourceName;

	/**
	 * リソースアイテムのリスト
	 */
	private List<ResourceItemWrapper> itemList;

	/**
	 * リソース名を指定してコンテナを生成します.
	 *
	 * @param resourceName リソース名
	 */
	public ResourceItemsContainer(String resourceName) {
		this.resourceName = resourceName;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (!(obj instanceof ResourceItemsContainer))
			return false;

		ResourceItemsContainer request = (ResourceItemsContainer) obj;

		return new EqualsBuilder().append(this.resourceName, request.resourceName)
				.append(this.itemList, request.itemList).isEquals();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(this.resourceName).append(this.itemList).hashCode();
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
	 * @return items
	 */
	public List<ResourceItemWrapper> getItemsList() {
		return itemList;
	}

	/**
	 * @param itemList セットする itemList
	 */
	public void setItemsLIst(List<ResourceItemWrapper> itemList) {
		this.itemList = itemList;
	}

	/**
	 * このオブジェクトが保持するリソースアイテムリストにリソースアイテムを追加します.<br>
	 * ただし、すでに(追加しようとしているアイテムのequalsがtrueとなるようなアイテムが)リスト内に存在する場合は追加されません.
	 *
	 * @param itemWrapper リソースアイテムのラッパーオブジェクト
	 */
	public void mergeItem(ResourceItemWrapper itemWrapper) {

		if (this.itemList == null) {
			this.itemList = new ArrayList<>();
		}

		if (!this.itemList.contains(itemWrapper)) {
			this.itemList.add(itemWrapper);
		}
	}

	/**
	 * このオブジェクトが保持するリソースアイテムリストにリソースアイテムを追加します.<br>
	 * ただし、すでに(追加しようとしているアイテムのequalsがtrueとなるようなアイテムが)リスト内に存在する場合は追加されません.
	 *
	 * @param item リソースアイテム
	 */
	public void mergeItem(List<ResourceItemWrapper> items) {

		if (this.itemList == null) {
			this.itemList = new ArrayList<>();
		}

		for (ResourceItemWrapper itemWrapper : items) {
			if (!this.itemList.contains(itemWrapper)) {
				this.itemList.add(itemWrapper);
			}
		}
	}
}
