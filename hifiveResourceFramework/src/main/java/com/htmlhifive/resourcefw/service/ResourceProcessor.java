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
package com.htmlhifive.resourcefw.service;

import com.htmlhifive.resourcefw.config.MessageMetadata;
import com.htmlhifive.resourcefw.config.ResourceConfigurationParameter;
import com.htmlhifive.resourcefw.message.RequestMessageContainer;
import com.htmlhifive.resourcefw.message.ResponseMessageContainer;
import com.htmlhifive.resourcefw.service.processing.ProcessContinuationStrategy;

/**
 * リソースアクションのリクエストを処理し、レスポンスを生成するプロセッサインターフェース.<br>
 *
 * @author kishigam
 * @see DefaultResourceProcessor
 */
public interface ResourceProcessor {

	/**
	 * リソースへのリクエストを実行します.<br/>
	 * コンテナに含まれる全てのリクエストを適切なリソースとそのアクションに振り分けます.<br/>
	 * 結果を取りまとめ、{@link ResponseMessageContainer}に格納して返します.
	 *
	 * @param requestMessages リクエストメッセージコンテナ
	 * @return レスポンスメッセージコンテナ
	 */
	ResponseMessageContainer process(RequestMessageContainer requestMessages);

	/**
	 * リソースクラスへの参照を管理するマネージャを設定します.
	 *
	 * @param resourceManager
	 */
	void setResourceManager(ResourceManager resourceManager);

	/**
	 * リソースが例外をスローしたときのプロセス継続判定を行うストラテジーオブジェクトを設定します.
	 *
	 * @param processContinuationStrategy
	 */
	void setProcessContinuationStrategy(ProcessContinuationStrategy processContinuationStrategy);

	/**
	 * メッセージメタデータオブジェクトを設定します.
	 *
	 * @param messageMetadata
	 */
	void setMessageMetadata(MessageMetadata messageMetadata);

	/**
	 * フレームワーク動作設定パラメータオブジェクトを設定します.
	 *
	 * @param resourceConfigurationParameter
	 */
	void setResourceConfigurationParameter(ResourceConfigurationParameter resourceConfigurationParameter);
}
