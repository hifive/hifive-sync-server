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
package com.htmlhifive.resourcefw.resource;

import com.htmlhifive.resourcefw.exception.AbstractResourceException;
import com.htmlhifive.resourcefw.message.RequestMessage;

/**
 * フレームワークが管理するリソースの標準的なメソッド(アクション)を規定したインターフェース.<br/>
 *
 * @author kishigam
 */
public interface BasicResource extends Resource {

	/** {@link #getId(RequestMessage)}で取得されたIDに該当するアイテムを返すアクション. */
	Object findById(RequestMessage requestMessage) throws AbstractResourceException;

	/** requestMessageに含まれる「クエリー」メタデータ該当する全アイテムを返すアクション. */
	Object findByQuery(RequestMessage requestMessage) throws AbstractResourceException;

	/** {@link #getId(RequestMessage)}で取得されたIDに該当するアイテムを保存するアクション.既に存在する場合は更新されます. */
	Object insertOrUpdate(RequestMessage requestMessage) throws AbstractResourceException;

	/** {@link #getId(RequestMessage)}で取得されたIDに該当するアイテムを削除し、そのアイテムを返すアクション. */
	Object remove(RequestMessage requestMessage) throws AbstractResourceException;

	/** 新たに生成したIDでアイテムを新規保存し、生成されたIDを返すアクション.既に存在する場合はServiceUnavailable例外になります. */
	Object create(RequestMessage requestMessage) throws AbstractResourceException;

	/** {@link #getId(RequestMessage)}で取得されたIDに該当するアイテムを保存するアクション.既に存在する場合はServiceUnavailable例外になります. */
	Object insert(RequestMessage requestMessage) throws AbstractResourceException;

	/** {@link #getId(RequestMessage)}で取得されたIDに該当するアイテムを更新するアクション.存在しない場合はNOTFOUND例外になります. */
	Object update(RequestMessage requestMessage) throws AbstractResourceException;

	/** {@link #getId(RequestMessage)}で取得されたIDに該当するアイテムが存在するときtrueを返すアクション. */
	Object exists(RequestMessage requestMessage) throws AbstractResourceException;

	/** requestMessageに含まれる「クエリー」メタデータに該当する全アイテムのIDリストを返すアクション. */
	Object list(RequestMessage requestMessage) throws AbstractResourceException;

	/** requestMessageに含まれる「クエリー」メタデータに該当する全アイテム件数を返すアクション. */
	Object count(RequestMessage requestMessage) throws AbstractResourceException;

	/** {@link #getId(RequestMessage)}で取得されたIDに該当するアイテムのロックを取得するアクション. */
	Object lock(RequestMessage requestMessage) throws AbstractResourceException;

	/** {@link #getId(RequestMessage)}で取得されたIDに該当するアイテムのロックを開放するアクション. */
	Object unlock(RequestMessage requestMessage) throws AbstractResourceException;

	/** {@link #getId(RequestMessage)}で取得されたIDに該当するアイテムを悲観的ロックによって取得します(フレームワーク内部利用を想定). */
	Object findByIdForUpdate(RequestMessage requestMessage) throws AbstractResourceException;

	/** requestMessageに含まれるクエリー文字列に該当する全アイテムを悲観的ロックによって取得します(フレームワーク内部利用を想定). */
	Object findByQueryForUpdate(RequestMessage requestMessage) throws AbstractResourceException;

	/** requestMessageから、そのリクエストが対象とするIDを抽出し、返します.多くの場合、「パス」メタデータから導出します. */
	String getId(RequestMessage requestMessage) throws AbstractResourceException;

	/** {@link #getId(RequestMessage)}で取得されたIDに該当するアイテムを複製し指定の親ディレクトリに保存するアクション.同名のアイテムが既に存在する場合はServiceUnavailable例外になります.  */
	Object copy(RequestMessage requestMessage) throws AbstractResourceException;

	/** {@link #getId(RequestMessage)}で取得されたIDに該当するアイテムを指定の親ディレクトリに移動するアクション.同名のアイテムが既に存在する場合はServiceUnavailable例外になります.  */
	Object move(RequestMessage requestMessage) throws AbstractResourceException;
}
