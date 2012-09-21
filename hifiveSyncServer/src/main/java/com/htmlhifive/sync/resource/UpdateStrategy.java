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

import com.htmlhifive.sync.commondata.CommonData;
import com.htmlhifive.sync.exception.ConflictException;

/**
 * ロックエラー発生時の更新戦略、競合判定ロジックのインターフェース.<br>
 *
 * @author kishigam
 */
public interface UpdateStrategy {

	/**
	 * 引数の情報をもとに更新可否を判断します.<br>
	 * 更新可能な場合、更新に使用するエレメントを返し、そうでない場合、サーバ側エレメントを含むConflictExceptionをスローします.
	 *
	 * @param requestHeader 同期リクエストヘッダ
	 * @param clientElement リクエストされたエレメント、DELETEの場合null
	 * @param common 同期レスポンスヘッダ(リクエスト処理前のサーバ側共通データ)
	 * @param serverElement リクエスト処理前のサーバ側エレメント(DELETE済み場合null)
	 * @return 更新に用いるエレメント
	 * @throws ConflictException 競合が解決できず、更新不可の場合
	 */
	<E> E resolveConflict(SyncRequestHeader requestHeader, E clientElement, CommonData common, E serverElement)
			throws ConflictException;
}
