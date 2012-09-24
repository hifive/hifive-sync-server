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
package com.htmlhifive.sync.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.htmlhifive.sync.resource.SyncResultType;
import com.htmlhifive.sync.service.ResourceItemsContainer;

/**
 * 同期(上り更新)において競合が発生したことを示す例外.<br>
 * サーバ側で管理しているリソースエレメントの内容を含む同期レスポンスオブジェクトを保持しており、クライアントに返す情報の生成に使用することができます.
 *
 * @author kishigam
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "conflicted")
public class ConflictException extends RuntimeException {

	/**
	 * シリアルバージョンUID.
	 */
	private static final long serialVersionUID = 2402614340391069636L;

	/**
	 * 競合タイプ.
	 */
	private SyncResultType conflictType;

	/**
	 * 競合データのリソースアイテムリスト
	 */
	private List<ResourceItemsContainer> resourceItems;

	/**
	 * 指定された情報をもとに例外オブジェクトを生成します.
	 *
	 * @param conflictType 競合タイプ
	 * @param resourceItems 競合したリソースアイテムリスト(コンテナ)
	 */
	public ConflictException(SyncResultType conflictType, List<ResourceItemsContainer> resourceItems) {
		super();
		this.conflictType = conflictType;
		this.resourceItems = resourceItems;
	}

	/**
	 * 指定された情報をもとに例外オブジェクトを生成します.
	 *
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 * @param conflictType 競合タイプ
	 * @param resourceItems 競合したリソースアイテムリスト(コンテナ)
	 * @see RuntimeException
	 */
	public ConflictException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
			SyncResultType conflictType, List<ResourceItemsContainer> resourceItems) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.conflictType = conflictType;
		this.resourceItems = resourceItems;
	}

	/**
	 * 指定された情報をもとに例外オブジェクトを生成します.
	 *
	 * @param message
	 * @param cause
	 * @param conflictType 競合タイプ
	 * @param resourceItems 競合したリソースアイテムリスト(コンテナ)
	 * @see RuntimeException
	 */
	public ConflictException(String message, Throwable cause, SyncResultType conflictType,
			List<ResourceItemsContainer> resourceItems) {
		super(message, cause);
		this.conflictType = conflictType;
		this.resourceItems = resourceItems;
	}

	/**
	 * 指定された情報をもとに例外オブジェクトを生成します.
	 *
	 * @param message
	 * @param conflictType 競合タイプ
	 * @param resourceItems 競合したリソースアイテムリスト(コンテナ)
	 * @see RuntimeException
	 */
	public ConflictException(String message, SyncResultType conflictType, List<ResourceItemsContainer> resourceItems) {
		super(message);
		this.conflictType = conflictType;
		this.resourceItems = resourceItems;
	}

	/**
	 * 指定された情報をもとに例外オブジェクトを生成します.
	 *
	 * @param cause
	 * @param conflictType 競合タイプ
	 * @param resourceItems 競合したリソースアイテムリスト(コンテナ)
	 * @see RuntimeException
	 */
	public ConflictException(Throwable cause, SyncResultType conflictType, List<ResourceItemsContainer> resourceItems) {
		super(cause);
		this.conflictType = conflictType;
		this.resourceItems = resourceItems;
	}

	/**
	 * @return conflictType
	 */
	public SyncResultType getConflictType() {
		return conflictType;
	}

	/**
	 * @return resourceItems
	 */
	public List<ResourceItemsContainer> getResourceItems() {
		return resourceItems;
	}
}
