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
package com.htmlhifive.sync.status;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 前回上り更新ステータスによる同期ステータス管理サービス実装.<br>
 * 前回上り更新ステータスを永続データリポジトリを使用して保持します.
 *
 * @author kishigam
 */
@Service
@Transactional(propagation = Propagation.MANDATORY)
public class LastUploadStatusService implements SyncStatusService<LastUploadStatus> {

	/**
	 * 前回上り更新状態を永続化するリポジトリ.
	 */
	@Resource
	private LastUploadStatusRepository repository;

	/**
	 * ストレージIDで指定されたクライアントについて、保存している前回上り更新ステータスを取得します.
	 *
	 * @param storageId クライアントのストレージID
	 * @return 前回上り更新ステータス
	 */
	@Override
	public LastUploadStatus currentStatus(String storageId) {

		LastUploadStatus status = repository.findOne(storageId);
		if (status == null) {
			status = new LastUploadStatus(storageId);
		}

		return status;
	}

	/**
	 * ストレージIDで指定されたクライアントについての前回上り更新ステータスを保存します.<br>
	 * まだ保存しているステータスがない場合は、新規に保存します.
	 *
	 * @param storageId クライアントのストレージID
	 * @param status 前回上り更新ステータス
	 */
	@Override
	public void updateStatus(LastUploadStatus status) {

		repository.save(status);
	}
}
