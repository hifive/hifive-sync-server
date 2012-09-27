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

import com.htmlhifive.sync.common.ResourceItemCommonData;

/**
 * リソースアイテム1件の情報を保持するデータラッパー.<br>
 * サーバ上の1つのリソースアイテムに関する共通情報、およびリソースアイテムそのもののオブジェクトを保持します.
 *
 * @author kishigam
 */
public class ResourceItemWrapper<T> {

	/**
	 * リソースアイテム共通データ.
	 */
	private ResourceItemCommonData itemCommonData;

	/**
	 * このデータが表現するリソースアイテムの本体.
	 */
	private T item;

	/**
	 * アイテム共通データとアイテムを指定してデータラッパーを生成します.
	 *
	 * @param itemCommonData リソースアイテム共通データ
	 * @param item リソースアイテムオブジェクト
	 */
	public ResourceItemWrapper(ResourceItemCommonData itemCommonData, T item) {
		this.itemCommonData = itemCommonData;
		this.item = item;
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
	public T getItem() {
		return item;
	}

	/**
	 * @param item セットする item
	 */
	public void setItem(T item) {
		this.item = item;
	}
}
