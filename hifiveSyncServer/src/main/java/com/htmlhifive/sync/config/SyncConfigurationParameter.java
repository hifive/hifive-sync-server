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
package com.htmlhifive.sync.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.htmlhifive.resourcefw.config.MessageMetadata;

/**
 * sync機能に関わるメッセージメタデータ、動作設定パラメータの値を保持するクラス.<br/>
 * {@link Value Value} アノテーションが付与されたものは、プロパティファイル(sync-configuration.properties)の設定値を参照します.
 *
 * @author kishigam
 */
@Component
public class SyncConfigurationParameter {

	/**
	 * resource frameworkのメッセージメタデータオブジェクト.
	 */
	@Autowired
	private MessageMetadata messageMetadata;

	/**
	 * syncリクエストが上り更新であることを示すパスの値.<br/>
	 * URLのパス部分は、 "/ (リソース名) / (URL_PATH_UPLOADまたはURL_PATH_DOWNLOADの値) / (IDなど)" のようになります.
	 */
	@Value("${URL_PATH_UPLOAD}")
	public String URL_PATH_UPLOAD;

	/**
	 * syncリクエストが下り更新であることを示すパスの値.<br/>
	 * URLのパス部分は、 "/ (リソース名) / (URL_PATH_UPLOADまたはURL_PATH_DOWNLOADの値) / (IDなど)" のようになります.
	 */
	@Value("${URL_PATH_DOWNLOAD}")
	public String URL_PATH_DOWNLOAD;

	/**
	 * syncリクエストが、上り下りをHTTPメソッドで判定するためのパスの値.<br/>
	 * URLのパス部分は、 "/ (リソース名) / (URL_PATH_UPLOADまたはURL_PATH_DOWNLOADの値) / (IDなど)" のようになります.
	 */
	@Value("${URL_PATH_SYNC_BY_HTTP_METHODS}")
	public String URL_PATH_SYNC_BY_HTTP_METHODS;

	/** syncリクエスト、レスポンスに含まれる「ストレージID」メタデータのキー名.syncリクエストの発行元クライアントを識別するために使用します. */
	@Value("#{'${PREFIX_METADATA}' + '${STORAGE_ID}'}")
	public String STORAGE_ID;

	/** クライアントに返す「同期時刻」メタデータのキー名. */
	@Value("#{'${PREFIX_METADATA}' + '${SYNC_TIME}'}")
	public String SYNC_TIME;

	/** syncリクエストに含まれる「前回上り更新時刻」メタデータのキー名. */
	@Value("#{'${PREFIX_METADATA}' + '${LAST_UPLOAD_TIME}'}")
	public String LAST_UPLOAD_TIME;

	/** syncリクエストから抽出した「syncリクエスト共通データ」メタデータのキー名. */
	@Value("#{'${PREFIX_METADATA}' + '${REQUEST_COMMON_DATA}'}")
	public String REQUEST_COMMON_DATA;

	/** syncリクエストメッセージから抽出した「リソースアイテムID」メタデータのキー名. */
	@Value("#{'${PREFIX_METADATA}' + '${RESOURCE_ITEM_ID}'}")
	public String RESOURCE_ITEM_ID;

	/** syncリクエストメッセージから抽出した「同期アクション」メタデータのキー名. */
	@Value("#{'${PREFIX_METADATA}' + '${SYNC_ACTION}'}")
	public String SYNC_ACTION;

	/** syncリクエストメッセージから抽出した「リソースアイテム最終更新時刻」メタデータのキー名. */
	@Value("#{'${PREFIX_METADATA}' + '${LAST_MODIFIED}'}")
	public String LAST_MODIFIED;

	/** syncリクエストメッセージから抽出した「リソースアイテム共通データID」メタデータのキー名. */
	@Value("#{'${PREFIX_METADATA}' + '${RESOURCE_ITEM_COMMON_DATA_ID}'}")
	public String RESOURCE_ITEM_COMMON_DATA_ID;

	/** syncリクエストメッセージから抽出した「リソースアイテム共通データ」メタデータのキー名. */
	@Value("#{'${PREFIX_METADATA}' + '${RESOURCE_ITEM_COMMON_DATA}'}")
	public String RESOURCE_ITEM_COMMON_DATA;

	/** クライアントに返す「リソースアイテム」メタデータのキー名. */
	@Value("#{'${PREFIX_METADATA}' + '${RESOURCE_ITEM}'}")
	public String RESOURCE_ITEM;

	/** 競合発生時にクライアントに返す「競合種別」メタデータのキー名. */
	@Value("#{'${PREFIX_METADATA}' + '${CONFLICT_TYPE}'}")
	public String CONFLICT_TYPE;

	/** 上り更新リクエストのアクション名. */
	@Value("${ACTION_FOR_UPLOAD}")
	public String ACTION_FOR_UPLOAD;

	/** 下り更新リクエストのアクション名. */
	@Value("${ACTION_FOR_DOWNLOAD}")
	public String ACTION_FOR_DOWNLOAD;

	/** 悲観的ロックリクエストのアクション名. */
	@Value("${ACTION_FOR_GETFORUPDATE}")
	public String ACTION_FOR_GETFORUPDATE;

	/** クライアントに返す今回の同期時刻を、実際の同期実行時刻の何秒前とするか. */
	@Value("${BUFFER_TIME_FOR_DOWNLOAD}")
	public String BUFFER_TIME_FOR_DOWNLOAD;

	/** 上り更新制御タイプ. */
	@Value("${UPLOAD_CONTROL_TYPE}")
	public String UPLOAD_CONTROL_TYPE;

	/** 下り更新制御タイプ. */
	@Value("${DOWNLOAD_CONTROL_TYPE}")
	public String DOWNLOAD_CONTROL_TYPE;
}
