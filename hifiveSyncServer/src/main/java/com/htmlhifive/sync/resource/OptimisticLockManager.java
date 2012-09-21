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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.htmlhifive.sync.commondata.CommonData;

/**
 * リソース対する楽観的ロック方式での制御ロジッククラス.<br>
 * TODO: ロックエラー発生時の処理
 *
 * @author kawaguch
 */
@Service
@Transactional(propagation = Propagation.MANDATORY)
public class OptimisticLockManager implements LockManager {

	/**
	 * ロックを取得します.<br>
	 * 楽観的ロック方式では常にtrueを返し、更新の実行自体は可能です.
	 *
	 * @param requestHeader 同期リクエストヘッダ
	 * @param commonBeforUpdate 同期レスポンスヘッダ
	 * @return true.
	 */
	@Override
	public boolean lock(SyncRequestHeader requestHeader, CommonData commonBeforUpdate) {

		return true;
	}

	/**
	 * ロック取得状況に応じて、リソースの更新が実行できるか判定します.<br>
	 * 楽観的ロック方式では、更新の実行に際して最終更新日付を比較し、すでにサーバ側の更新が発生していた場合にロックエラーとなります.<br>
	 * ロックエラー発生時、リソースで採用されている更新戦略に従い、競合判定および更新を実施します.
	 *
	 * @param requestHeader 同期リクエストヘッダ
	 * @param commonBeforUpdate 同期レスポンスヘッダ
	 * @return update(/delete)できる場合true.
	 */
	@Override
	public boolean canUpdate(SyncRequestHeader requestHeader, CommonData commonBeforUpdate) {

		return requestHeader.getLastModified() >= commonBeforUpdate.getLastModified();
	}

	/**
	 * ロックを解除します.<br>
	 * 楽観的ロック方式ではロックを取得しないため、処理を実行しません.
	 *
	 * @param requestHeader 同期リクエストヘッダ
	 * @param commonBeforUpdate 同期レスポンスヘッダ
	 */
	@Override
	public void release(SyncRequestHeader requestHeader, CommonData commonBeforUpdate) {

		// ロジックなし
	}
}
