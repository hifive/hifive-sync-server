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
package com.htmlhifive.resourcefw.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * フレームワークの動作設定パラメータの値を保持するクラス.<br/>
 * {@link Value Value} アノテーションが付与されたものは、プロパティファイル(resource-configuration.properties)の設定値を参照します.
 *
 * @author kishigam
 */
@Component
public class ResourceConfigurationParameter {

	/** サービスルートパス(フレームワーク管理下で動作させるURLの最上位パス) */
	@Value("${SERVICE_ROOT_PATH}")
	public String SERVICE_ROOT_PATH;

	/** リソースでエラーが発生した際のレスポンスに例外情報を含めるかどうか */
	@Value("${RESPONSE_WITH_ERROR_DETAIL}")
	public boolean RESPONSE_WITH_ERROR_DETAIL;

	/** Multipart形式のファイルアップロードにおけるファイルサイズ上限. */
	@Value("${MULTIPART_MAX_UPLOAD_SIZE}")
	public int MULTIPART_MAX_UPLOAD_SIZE;

	/** GETリクエストのデフォルトアクション. */
	@Value("${DEFAULT_ACTION_FOR_GET_BY_ID}")
	public String DEFAULT_ACTION_FOR_GET_BY_ID;

	/** GETリクエストのデフォルトアクション.queryメタデータが指定されている場合はこちらが使用されます. */
	@Value("${DEFAULT_ACTION_FOR_GET_BY_QUERY}")
	public String DEFAULT_ACTION_FOR_GET_BY_QUERY;

	/** POSTリクエストのデフォルトアクション. */
	@Value("${DEFAULT_ACTION_FOR_POST}")
	public String DEFAULT_ACTION_FOR_POST;

	/** POSTリクエストのデフォルトアクション.copyパラメータが指定されている場合はこちらが使用されます. */
	@Value("${DEFAULT_ACTION_FOR_COPY}")
	public String DEFAULT_ACTION_FOR_COPY;

	/** POSTリクエストのデフォルトアクション.moveパラメータが指定されている場合はこちらが使用されます. */
	@Value("${DEFAULT_ACTION_FOR_MOVE}")
	public String DEFAULT_ACTION_FOR_MOVE;

	/** PUTリクエストのデフォルトアクション. */
	@Value("${DEFAULT_ACTION_FOR_PUT}")
	public String DEFAULT_ACTION_FOR_PUT;

	/** DELETEリクエストのデフォルトアクション. */
	@Value("${DEFAULT_ACTION_FOR_DELETE}")
	public String DEFAULT_ACTION_FOR_DELETE;
}
