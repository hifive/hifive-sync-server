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

import java.util.List;

import com.htmlhifive.resourcefw.exception.AbstractResourceException;
import com.htmlhifive.resourcefw.message.RequestMessage;
import com.htmlhifive.resourcefw.message.ResponseMessage;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.resource.update.UpdateStrategy;

/**
 * 同期可能なリソースの操作を規定したインターフェース.<br>
 * 同期のためのバージョン管理を行い、クライアントデータとサーバデータのバージョン比較によって上り/下りの更新処理を制御します.
 */
public interface SyncResource {

	/**
	 * 同期上り更新.<br/>
	 * クライアントのデータをサーバに反映します.<br/>
	 * 更新結果をリソースアイテムのようなオブジェクト、または{@link ResponseMessage RequestMessage}で返します.
	 * 上り更新するためには、クライアントのデータはサーバデータの次バージョンである必要があります.そうでない場合、IDの重複や更新の競合として検出されます.<br/>
	 *
	 * @param requestMessage リクエストメッセージ
	 * @return 上り更新結果
	 */
	Object upload(RequestMessage requestMessage) throws AbstractResourceException;

	/**
	 * 同期下り更新.<br/>
	 * 指定されたリソースアイテムのサーバデータのうち、クライアントデータのバージョン以降の更新があるものを返します.<br/>
	 * 更新結果をリソースアイテムのようなオブジェクト、または{@link ResponseMessage RequestMessage}で返します. バージョン管理は更新時刻の保持・比較によって行います.
	 *
	 * @param requestMessage リクエストメッセージ
	 * @return 下り更新結果
	 */
	Object download(RequestMessage requestMessage) throws AbstractResourceException;

	/**
	 * リソースアイテム共通データを悲観的ロックによって取得します.<br/>
	 * downloadまたはuploadの事前に行うことで、対象リソースアイテムに対する他のユーザーからのリクエストの影響を最小限にすることができます.
	 *
	 * @param requestMessage リクエストメッセージ
	 * @return リソースアイテム共通データのリスト
	 * @throws AbstractResourceException
	 */
	List<ResourceItemCommonData> getForUpdate(RequestMessage requestMessage) throws AbstractResourceException;

	Synchronizer getSynchronizer();

	void setSynchronizer(Synchronizer synchronizer);

	UpdateStrategy getUpdateStrategy();

	void setUpdateStrategy(UpdateStrategy updateStrategy);
}
