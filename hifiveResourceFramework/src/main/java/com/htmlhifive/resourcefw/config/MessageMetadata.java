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
import org.springframework.web.context.request.NativeWebRequest;

import com.htmlhifive.resourcefw.message.AbstractMessage;
import com.htmlhifive.resourcefw.message.DirectoryMultipartFileValueHolder;

/**
 * フレームワークのリクエスト、レスポンス情報を保持する{@link AbstractMessage AbstractMessage}に保持される情報のうち、 規定のキー名(メタデータ名)を保持するクラス.<br/>
 * {@link Value Value} アノテーションが付与されたものは、プロパティファイル(message-metadata.properties)の設定値を参照します.
 *
 * @author kishigam
 */
@Component
public class MessageMetadata {

	/** アプリケーション独自HTTP Headerに付与されるプレフィックス */
	@Value("${PREFIX_HTTP_HEADER}")
	public String PREFIX_HTTP_HEADER;

	/** リクエスト、レスポンスのMessageに含まれるアプリケーションデータ以外の「メタデータ」に付与されるプレフィックス */
	@Value("${PREFIX_METADATA}")
	public String PREFIX_METADATA;

	/**
	 * HTTPメソッドを指定するためのメタデータキー名.<br/>
	 * 標準的な値を用いることが多いため、PREFIX_METADATAは付与されません.
	 */
	@Value("${HTTP_METHOD}")
	public String HTTP_METHOD;

	/** 「パス」メタデータのキー名.「オリジナルパス」からの処理状況によって変更される、パスの「現在状態」を保持します. */
	@Value("#{'${PREFIX_METADATA}' + '${REQUEST_PATH}'}")
	public String REQUEST_PATH;

	/** 「オリジナルパス」メタデータのキー名.リクエストで指定されたパスそのものを保持します. */
	@Value("#{'${PREFIX_METADATA}' + '${REQUEST_PATH_ORG}'}")
	public String REQUEST_PATH_ORG;

	/** 「アクション」メタデータのキー名.リクエストで指定するリソースに対する処理の名称.指定されない場合、HTTPメソッドごとのデフォルトが使用されます. */
	@Value("#{'${PREFIX_METADATA}' + '${ACTION}'}")
	public String ACTION;

	/** 「Accept」メタデータのキー名.HTTP HeaderにおけるAcceptと同様の役割を持つリクエストパラメータです. */
	@Value("#{'${PREFIX_METADATA}' + '${ACCEPT}'}")
	public String ACCEPT;

	/** 「クエリー」メタデータのキー名.クエリーを必要とするアクションで使用されます.書式はリソースでの解釈に依存します. */
	@Value("#{'${PREFIX_METADATA}' + '${QUERY}'}")
	public String QUERY;

	/** 「ロックトークン」メタデータのキー名.ロックの取得や開放、ロックされたリソースアイテムの参照に必要なトークンを保持します. */
	@Value("#{'${PREFIX_METADATA}' + '${LOCK_TOKEN}'}")
	public String LOCK_TOKEN;

	/** 「ユーザープリンシパル」メタデータのキー名.認証されたログインユーザ名を含むPrincipalオブジェクトを保持します. */
	@Value("#{'${PREFIX_METADATA}' + '${USER_PRINCIPAL}'}")
	public String USER_PRINCIPAL;

	/**
	 * 「リクエストContent」メタデータのキー名.Multipartデータのファイル名など、コンテンツのキー名をこのキーで参照することができます.<br>
	 * バイナリデータをBodyに含めたリクエストの場合、ファイル名をこのキーに設定してください.
	 */
	@Value("#{'${PREFIX_METADATA}' + '${REQUEST_CONTENT_KEY}'}")
	public String REQUEST_CONTENT_KEY;

	/**
	 * Streamデータなど、Bodyデータそのものを格納するキー名.<br>
	 * これを使用する場合、REQUEST_CONTENT_KEYの値としてこのキー名が保持されます.
	 */
	@Value("#{'${PREFIX_METADATA}' + '${REQUEST_CONTENT}'}")
	public String REQUEST_CONTENT;

	/**
	 * Multipartリクエストにおいて、複数ファイルをディレクトリごと送信する際に使用するディレクトリ名.<br>
	 * このデータが含まれると、REQUEST_CONTENTの値としてディレクトリ以下のファイルデータ、およびそれらの情報を持つ {@link DirectoryMultipartFileValueHolder
	 * DirectoryMultipartFileValueHolder}が保持されます.<br/>
	 */
	@Value("#{'${PREFIX_METADATA}' + '${REQUEST_MULTIPART_FILES_DIR}'}")
	public String REQUEST_MULTIPART_FILES_DIR;

	/**
	 * ファイルをダウンロードさせるHTTPヘッダ(Content-Disposition)を出力するように要求するためのメタデータのキー名.<br/>
	 * この名称のリクエストパラメータにtrueが渡されたとき、RESPONSE_FILE_NAMEメタデータに設定されたファイル名でContent-Dispositionヘッダを出力します.
	 */
	@Value("#{'${PREFIX_METADATA}' + '${REQUEST_FILE_DOWNLOAD}'}")
	public String REQUEST_FILE_DOWNLOAD;

	//TODO:追加プロパティの名称？

	/**
	 * コピー/移動先ディレクトリ
	 */
	@Value("#{'${PREFIX_METADATA}' + '${DEST_DIR}'}")
	public String DEST_DIR;

	/**
	 * 削除時の子アイテム削除可否
	 */
	@Value("#{'${PREFIX_METADATA}' + '${RECURSIVE}'}")
	public String RECURSIVE;


	/** コピー */
	@Value("${COPY}")
	public String COPY;

	/** 移動 */
	@Value("${MOVE}")
	public String MOVE;


	/** 「レスポンスStatus」メタデータのキー名.HTTP Statusに相当し、通常はフレームワークが設定しますが、リソースで設定することも可能です. */
	@Value("#{'${PREFIX_METADATA}' + '${RESPONSE_STATUS}'}")
	public String RESPONSE_STATUS;

	/** 「レスポンスBody」メタデータのキー名.リソースの処理結果はこのキー名で保持され、レスポンスとして返されます.リソースで設定することも可能です. */
	@Value("#{'${PREFIX_METADATA}' + '${RESPONSE_BODY}'}")
	public String RESPONSE_BODY;

	/** 「レスポンスHeader」メタデータのキー名.HTTP Headerに相当し、単一リクエストの場合はこのメタデータに含めたデータ(キー・値の組み合わせ)はHTTP Headerに設定されます. */
	@Value("#{'${PREFIX_METADATA}' + '${RESPONSE_HEADER}'}")
	public String RESPONSE_HEADER;

	/**
	 * 「ダウンロードファイル名」メタデータのキー名.<br/>
	 * REQUEST_FILE_DOWNLOADがtrueであった場合、HTTPレスポンスのContent-Dispositionヘッダにこのメタデータに設定された名称が使用されます.
	 */
	@Value("#{'${PREFIX_METADATA}' + '${RESPONSE_DOWNLOAD_FILE_NAME}'}")
	public String RESPONSE_DOWNLOAD_FILE_NAME;

	/** 「リソース処理ステータス」メタデータのキー名.フレームワークが内部で使用する、多重化リクエストにおける各リクエストの処理状況です. */
	@Value("#{'${PREFIX_METADATA}' + '${PROCESSING_STATUS}'}")
	public String PROCESSING_STATUS;

	/** 「エラー原因」メタデータのキー名.リソースがスローしたフレームワーク例外名を保持します. */
	@Value("#{'${PREFIX_METADATA}' + '${ERROR_CAUSE}'}")
	public String ERROR_CAUSE;

	/** 「エラー詳細情報」メタデータのキー名.リソースがフレームワーク例外をスローする場合に指定できるエラーの詳細を示す文字列です. */
	@Value("#{'${PREFIX_METADATA}' + '${ERROR_DETAIL_INFO}'}")
	public String ERROR_DETAIL_INFO;

	/** 「エラースタックトレース」メタデータのキー名.リソースがスローしたフレームワーク例外のスタックトレースインスタンスを保持します. */
	@Value("#{'${PREFIX_METADATA}' + '${ERROR_STACK_TRACE}'}")
	public String ERROR_STACK_TRACE = null;

	/**
	 * 「User-Agent」メタデータのキー名.HTTP Headerとして使用されるため、このキー名は変更できません.<br/>
	 * {@link NativeWebRequest#getHeaderNames()} の仕様に合わせて、すべて小文字にしています.
	 */
	public String HTTP_HEADER_USER_AGENT = "user-agent";

	/**
	 * 「Content-Type」メタデータのキー名.HTTP Headerとして使用されるため、このキー名は変更できません.<br/>
	 * {@link NativeWebRequest#getHeaderNames()} の仕様に合わせて、すべて小文字にしています.
	 */
	public String HTTP_HEADER_CONTENT_TYPE = "content-type";

	/**
	 * 「Content-Length」メタデータのキー名.HTTP Headerとして使用されるため、このキー名は変更できません.<br/>
	 * {@link NativeWebRequest#getHeaderNames()} の仕様に合わせて、すべて小文字にしています.
	 */
	public String HTTP_HEADER_CONTENT_LENGTH = "content-length";

	/**
	 * 「Content-Disposition」メタデータのキー名.HTTP Headerとして使用されるため、このキー名は変更できません.<br/>
	 * {@link NativeWebRequest#getHeaderNames()} の仕様に合わせて、すべて小文字にしています.
	 */
	public String HTTP_HEADER_CONTENT_DISPOSITION = "content-disposition";

	/**
	 * 「Set-Cookie」メタデータのキー名.HTTP Headerとして使用されるため、このキー名は変更できません.<br/>
	 * {@link NativeWebRequest#getHeaderNames()} の仕様に合わせて、すべて小文字にしています.
	 */
	public String HTTP_HEADER_SET_COOKIE = "set-cookie";

	/**
	 * 「Location」メタデータのキー名.HTTP Headerとして使用されるため、このキー名は変更できません.<br/>
	 * {@link NativeWebRequest#getHeaderNames()} の仕様に合わせて、すべて小文字にしています.
	 */
	public String HTTP_HEADER_LOCATION = "location";
}
