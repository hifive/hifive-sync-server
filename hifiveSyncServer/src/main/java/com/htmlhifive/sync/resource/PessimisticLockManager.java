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

import com.htmlhifive.sync.exception.PessimisticLockException;

/**
 * リソース対する悲観的ロック方式での制御ロジッククラス.<br>
 * TODO:未実装
 */
@Deprecated
@Service
@Transactional(propagation = Propagation.MANDATORY)
public class PessimisticLockManager implements LockManager {

	// @Resource
	// private SyncProvider commonDataService;

	/**
	 * ロックを取得します.<br>
	 * 仮実装として、PessimisticLockExceptionをスローします.
	 *
	 * @param requestHeader 同期リクエストヘッダ
	 * @param responseHeaderBeforUpdate 同期レスポンスヘッダ
	 * @return
	 */
	@Override
	public boolean lock(SyncRequestHeader requestHeader, SyncResponseHeader responseHeaderBeforUpdate)
			throws PessimisticLockException {

		throw new PessimisticLockException();
	}

	/**
	 * ロック取得状況に応じて、リソースの更新が実行できるか判定します.<br>
	 * 仮実装として、PessimisticLockExceptionをスローします.
	 *
	 * @param requestHeader 同期リクエストヘッダ
	 * @param responseHeaderBeforUpdate 同期レスポンスヘッダ
	 * @return
	 */
	@Override
	public boolean canUpdate(SyncRequestHeader requestHeader, SyncResponseHeader responseHeaderBeforUpdate)
			throws PessimisticLockException {

		throw new PessimisticLockException();
	}

	/**
	 * ロックを解除します.<br>
	 * 仮実装として、空実装とします.
	 *
	 * @param requestHeader 同期リクエストヘッダ
	 * @param responseHeaderBeforUpdate 同期レスポンスヘッダ
	 */
	@Override
	public void release(SyncRequestHeader requestHeader, SyncResponseHeader responseHeaderBeforUpdate) {

	}
}
