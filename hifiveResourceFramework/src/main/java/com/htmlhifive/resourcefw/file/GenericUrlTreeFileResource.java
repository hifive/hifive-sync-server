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
package com.htmlhifive.resourcefw.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.web.util.UrlUtils;

import com.htmlhifive.resourcefw.config.MessageMetadata;
import com.htmlhifive.resourcefw.exception.AbstractResourceException;
import com.htmlhifive.resourcefw.exception.BadRequestException;
import com.htmlhifive.resourcefw.exception.ConflictException;
import com.htmlhifive.resourcefw.exception.ForbiddenException;
import com.htmlhifive.resourcefw.exception.GenericResourceException;
import com.htmlhifive.resourcefw.exception.GoneException;
import com.htmlhifive.resourcefw.exception.LockedException;
import com.htmlhifive.resourcefw.exception.NotFoundException;
import com.htmlhifive.resourcefw.exception.NotImplementedException;
import com.htmlhifive.resourcefw.file.UrlTreeMetaData.ResponseStatus;
import com.htmlhifive.resourcefw.file.UrlTreeMetaData.UpdateRequestType;
import com.htmlhifive.resourcefw.file.auth.UrlTreeAuthorizationManager;
import com.htmlhifive.resourcefw.file.auth.UrlTreeContext;
import com.htmlhifive.resourcefw.file.metadata.UrlTreeDTO;
import com.htmlhifive.resourcefw.file.metadata.UrlTreeMetaDataManager;
import com.htmlhifive.resourcefw.file.persister.ContentsPersister;
import com.htmlhifive.resourcefw.message.DirectoryMultipartFileValueHolder;
import com.htmlhifive.resourcefw.message.FileValueHolder;
import com.htmlhifive.resourcefw.message.RequestMessage;
import com.htmlhifive.resourcefw.message.ResponseMessage;
import com.htmlhifive.resourcefw.resource.BasicResource;
import com.htmlhifive.resourcefw.resource.ResourceActionStatus;
import com.htmlhifive.resourcefw.resource.ResourceClass;

/**
 * リソースアイテムとしてファイルとそのメタデータ(ファイル名、権限、・・＝urlTreeMetada)を管理する汎用リソース.<br>
 * {@link UrlTreeAuthorizationManager UrlTreeAuthorizationManager}を用いた権限管理、ファイルデータを保存する{@link ContentsPersister
 * Persister}を切り替えることで、 様々な環境におけるファイルストレージリソースとして機能します.
 *
 * @author kishigam
 */
@ResourceClass(name = "urltree")
public class GenericUrlTreeFileResource implements BasicResource {

	/**
	 * レスポンスボディを空にする場合の戻り値.
	 */
	protected static final String EMPTY_RETURNVALUE = "";

	/**
	 * 実処理を担うリソース本体ロジッククラス.
	 */
	@Autowired
	protected UrlTreeResource<InputStream> urlTreeResource;

	/**
	 * 認可制御を行うマネージャオブジェクト
	 */
	@Autowired
	protected UrlTreeAuthorizationManager urlTreeAuthorizationManager;

	/**
	 * このリソースおよび関連クラスの初期化処理.<br/>
	 * Springのアプリケーションコンテキスト構築後に実行します.
	 */
	@PostConstruct
	protected void init() {

		urlTreeResource.init();
	}

	/**
	 * IDでリソースアイテムを検索し、ファイルを返すアクションに対応するメソッド.<br/>
	 * ファイルは、そのファイルの情報(urlTreeMetadata)とともに検索、取得されます.<br/>
	 * urlTreeMetadataにはcontentType(MIMEタイプ)を含んでいるため、レスポンスメッセージにContent-Typeヘッダとしてそれを返す必要があります.
	 * リクエストに含まれるパラメータにより、urlTreeMetadataだけを返したり(metadataOnly設定)、
	 * リクエストにtype=dirパラメータが設定されたときにディレクトリ情報としてファイル一覧を返すことができます.<br/>
	 * これらの場合は、Content-Typeの設定は不要です.<br/>
	 * Persisterクラスの種類によっては、Persisterが扱うストレージから直接ファイルを取得するためのURLがurlTreeMetadataに設定されるので、
	 * Locationヘッダの値として使用することでクライアントがそのURLを参照できるようにします.
	 */
	@Override
	public Object findById(RequestMessage requestMessage) throws AbstractResourceException {

		UrlTreeContext ctx = createContext(requestMessage);

		String path = getId(requestMessage);

		boolean metadataOnly = requestMessage.get("metadata") != null;

		// 1件のみ前提(ディレクトリ指定は可能)
		Map<String, UrlTreeMetaData<InputStream>> urlTreeMetadataMap = urlTreeResource.doGet(metadataOnly, ctx, path);
		UrlTreeMetaData<InputStream> urlTreeMetadata = urlTreeMetadataMap.values().iterator().next();

		// ステータス判定、OK以外は例外をスロー
		checkStatus(urlTreeMetadata, requestMessage);

		// metadataOnlyならurlTreeMetadataを戻す
		if (metadataOnly) {
			// contentTypeの設定不要(デフォルト)
			return urlTreeMetadata;
		}

		// ディレクトリであればchildListをオブジェクト戻し
		if (urlTreeMetadata.isDirectory()) {
			// contentTypeの設定不要(デフォルト)
			return urlTreeMetadata.getChildList();
		}

		// metadataOnlyでないファイルの場合

		// Content-Typeやファイル名の設定が必要なため、ResponseMessageを返す
		ResponseMessage responseMessage = new ResponseMessage(requestMessage);
		MessageMetadata messageMetadata = responseMessage.getMessageMetadata();

		Map<String, Object> headers = new HashMap<>();
		responseMessage.put(messageMetadata.RESPONSE_HEADER, headers);

		// urlが設定されていたら(外部ストレージのPersisterを使用している場合等)リダイレクトレスポンスを返す
		String url = urlTreeMetadata.getUrl();
		if (StringUtils.isNotBlank(url) && UrlUtils.isValidRedirectUrl(url)) {
			// Content-Dipositionを設定する場合のファイル名
			responseMessage.put(messageMetadata.RESPONSE_DOWNLOAD_FILE_NAME, urlTreeMetadata.getName());

			responseMessage.put(messageMetadata.RESPONSE_STATUS, ResourceActionStatus.SEE_OTHER);
			headers.put(messageMetadata.HTTP_HEADER_LOCATION, url);

			return responseMessage;
		}

		// ヘッダにContent-Type、ボディにファイルデータを追加
		headers.put(messageMetadata.HTTP_HEADER_CONTENT_TYPE, urlTreeMetadata.getContentType());

		responseMessage.put(messageMetadata.RESPONSE_STATUS, ResourceActionStatus.OK);
		responseMessage.put(messageMetadata.RESPONSE_BODY, urlTreeMetadata.getData());
		// Content-Dipositionを設定する場合のファイル名
		responseMessage.put(messageMetadata.RESPONSE_DOWNLOAD_FILE_NAME, urlTreeMetadata.getName());

		return responseMessage;
	}

	/**
	 * クエリを用いてリソースアイテムを検索するアクションに対応するメソッドですが、このリソースではサポートしません.
	 */
	@Override
	public Object findByQuery(RequestMessage requestMessage) throws AbstractResourceException {

		throw new NotImplementedException("This resource is not supported findByQuery action, or cannot accept "
				+ requestMessage.getMessageMetadata().QUERY + " parameter", requestMessage);
	}

	/**
	 * リクエストに含まれるデータをurlTreeMetadataや実際のファイルストレージに反映するアクションを表すメソッド.<br/>
	 * ファイル単位のアップロードの他、ディレクトリ内のファイルを一括でアップロードするアクションに対応しています.<br/>
	 * 戻り値によって生成されるレスポンスボディは空になります.
	 */
	@Override
	public Object insertOrUpdate(RequestMessage requestMessage) throws AbstractResourceException {

		UrlTreeContext ctx = createContext(requestMessage);

		MessageMetadata messageMetadata = requestMessage.getMessageMetadata();

		// ディレクトリ一括でのinsertOrUpdateを指示するパラメータの取得と判定
		String dirFiles = (String) requestMessage.get(messageMetadata.REQUEST_MULTIPART_FILES_DIR);
		if (dirFiles != null && !dirFiles.isEmpty()) {
			return directoryInsertOrUpdate(requestMessage, ctx);
		}

		UrlTreeMetaData<InputStream> result = null;
		if (requestMessage.get("metadata") != null) {

			// metadataOnlyの場合、リクエストのContent-TypeはJSON固定
			checkContentType(requestMessage);

			UrlTreeMetaData<InputStream> item = createUrlTreeMetadata(requestMessage, UpdateRequestType.METADATA, ctx,
					false);
			result = urlTreeResource.doUpdate(item);

			checkStatus(result, requestMessage);
			return EMPTY_RETURNVALUE;
		}

		// ファイルデータを含めて更新する
		if ((boolean) exists(requestMessage, ctx).get("exists")) {
			UrlTreeMetaData<InputStream> item = createUrlTreeMetadata(requestMessage, UpdateRequestType.NORMAL, ctx,
					true);
			result = urlTreeResource.doUpdate(item);
		} else {
			// 新規ファイルの場合はurlTreeMetadataも新規生成するため、UpdateRequestType.NONEを指定する
			UrlTreeMetaData<InputStream> item = createUrlTreeMetadata(requestMessage, UpdateRequestType.NONE, ctx, true);
			result = urlTreeResource.doCreate(item);
		}

		checkStatus(result, requestMessage);

		return EMPTY_RETURNVALUE;
	}

	/**
	 * ディレクトリとその中のファイルを一括で保存します.
	 *
	 * @param requestMessage リクエストメッセージ
	 * @param ctx UrlTreeリソースのコンテキストオブジェクト
	 */
	protected Object directoryInsertOrUpdate(RequestMessage requestMessage, UrlTreeContext ctx)
			throws BadRequestException, AbstractResourceException {

		MessageMetadata messageMetadata = requestMessage.getMessageMetadata();

		String dirFileKey = (String) requestMessage.get(messageMetadata.REQUEST_CONTENT_KEY);
		DirectoryMultipartFileValueHolder dirFileValues = (DirectoryMultipartFileValueHolder) requestMessage
				.get(dirFileKey);

		// ディレクトリ内ファイルのinsertOrUpdate
		for (FileValueHolder fileValueHolder : dirFileValues.getFileValues()) {

			UrlTreeMetaData<InputStream> item = new UrlTreeMetaData<>();
			item.setUrlTreeContext(ctx);

			// 内部でパスを正規化(ルートディレクトリの指定省略、連続するパス区切りの無視)、ディレクトリパスとファイル名を分割している
			String dirPath = dirFileValues.getDirPath();
			item.setFilename(dirPath + UrlTreeMetaDataManager.PATH_SEPARATOR + fileValueHolder.getOriginalFilename());

			item.setDirectory(false);
			item.setOwnerId(ctx.getUserName());
			item.setUpdateRequestType(UpdateRequestType.NORMAL);
			item.setContentType(fileValueHolder.getContentType());
			try {
				item.setData(fileValueHolder.getInputStream());
			} catch (IOException e) {
				throw new GenericResourceException(e);
			}

			UrlTreeMetaData<InputStream> existence = urlTreeResource.doGet(true, ctx, item.getAbsolutePath()).get(
					item.getAbsolutePath());
			// OK,NOT_FOUND,DELETED以外は例外
			checkStatus(existence, requestMessage, ResponseStatus.NOT_FOUND, ResponseStatus.DELETED);

			UrlTreeMetaData<InputStream> result = null;
			if (existence != null && existence.getResponseStatus() == ResponseStatus.OK) {
				result = urlTreeResource.doUpdate(item);
			} else {
				result = urlTreeResource.doCreate(item);
			}

			// 1件でもステータスがOKでなければその例外がスローされる
			checkStatus(result, requestMessage);
		}

		return EMPTY_RETURNVALUE;
	}

	/**
	 * ディレクトリとその中のファイルを一括で保存します.
	 *
	 * @param requestMessage リクエストメッセージ
	 * @param ctx UrlTreeリソースのコンテキストオブジェクト
	 */
	protected Object directoryInsertOrUpdate(String destDir, UrlTreeMetaData<InputStream> orgDir,
			RequestMessage requestMessage, UrlTreeContext ctx) throws BadRequestException, AbstractResourceException {

		// ディレクトリ内ファイルのinsertOrUpdate
		for (UrlTreeDTO org : orgDir.getChildList()) {

			UrlTreeMetaData<InputStream> item = new UrlTreeMetaData<>();
			item.setUrlTreeContext(ctx);

			String path = destDir + UrlTreeMetaDataManager.PATH_SEPARATOR + org.getName();

			item.setFilename(path);

			//TODO:ディレクトリの場合の再帰処理
			item.setDirectory(org.isDirectory());
			item.setOwnerId(ctx.getUserName());
			item.setUpdateRequestType(UpdateRequestType.NORMAL);
			//ContentTypeは特に指定しない
			//item.setContentType(null);
			//try {
				//TODO:urlTreeDTOにgetDataがない。。。
				//item.setData(urlTreeDTO.getDdata());
			//} catch (IOException e) {
			//	throw new GenericResourceException(e);
			//}

			UrlTreeMetaData<InputStream> existence = urlTreeResource.doGet(true, ctx, item.getAbsolutePath()).get(
					item.getAbsolutePath());

			// OK,NOT_FOUND以外は例外
			checkStatus(existence, requestMessage, ResponseStatus.NOT_FOUND);

			UrlTreeMetaData<InputStream> result = null;
			if (existence != null && existence.getResponseStatus() == ResponseStatus.OK) {
				throw new ConflictException("Requested item already exists : " + path, requestMessage);
			} else {
				result = urlTreeResource.doCreate(item);
			}

			// 1件でもステータスがOKでなければその例外がスローされる
			checkStatus(result, requestMessage);

		}

		return EMPTY_RETURNVALUE;
	}

	/**
	 * IDで指定されたリソースアイテムを削除するアクションに対応するメソッド.<br/>
	 * 戻り値によって生成されるレスポンスボディは空になります.
	 */
	@Override
	public Object remove(RequestMessage requestMessage) throws AbstractResourceException {

		UrlTreeContext ctx = createContext(requestMessage);

		String path = getId(requestMessage);

		if (!(boolean) exists(requestMessage, ctx).get("exists")) {
			throw new NotFoundException("Requested item is not found : " + path, requestMessage);
		}

		MessageMetadata messageMetadata = requestMessage.getMessageMetadata();

		String recursiveValue = (String)requestMessage.get(messageMetadata.RECURSIVE);
		Boolean recursive = (recursiveValue != null && recursiveValue.equals("true"));

		UrlTreeMetaData<InputStream> result = urlTreeResource.doDelete(path, recursive, ctx);
		checkStatus(result, requestMessage);

		return EMPTY_RETURNVALUE;
	}

	/**
	 * リソースがID(ファイルやディレクトリのパス)を生成してリソースアイテムを新規生成するアクションはサポートしません.<br/>
	 * クライアントがIDを指定し、{@link GenericUrlTreeFileResource#insertOrUpdate(RequestMessage)}を実行する必要があります.<br/>
	 * 戻り値によって生成されるレスポンスボディは空になります.
	 */
	@Override
	public Object create(RequestMessage requestMessage) throws AbstractResourceException {

		// ファイルパス(ファイル名)は必ず指定されるものとする→insertやinsertOrUpdateを使う
		throw new NotImplementedException("This resource is not supported create action", requestMessage);
	}

	/**
	 * リソースアイテムの新規生成アクションを実行するメソッド.<br/>
	 * insertOrUpdateのように存在するリソースアイテムを指定した場合にupdateせず、{@link ConflictException ConflictException}をスローします.
	 * 戻り値によって生成されるレスポンスボディは空になります.
	 */
	@Override
	public Object insert(RequestMessage requestMessage) throws AbstractResourceException {

		UrlTreeContext ctx = createContext(requestMessage);

		String path = getId(requestMessage);

		if ((boolean) exists(requestMessage, ctx).get("exists")) {
			throw new ConflictException("Requested item already exists : " + path, requestMessage);
		}

		// ファイルデータを含めて更新する
		// 新規ファイルの場合はurlTreeMetadataも新規生成するため、UpdateRequestType.NONEを指定する
		UrlTreeMetaData<InputStream> item = createUrlTreeMetadata(requestMessage, UpdateRequestType.NONE, ctx, true);

		UrlTreeMetaData<InputStream> result = urlTreeResource.doCreate(item);

		checkStatus(result, requestMessage);

		return EMPTY_RETURNVALUE;
	}

	/**
	 * リソースアイテムの更新アクションを実行するメソッド.<br/>
	 * insertOrUpdateのように存在しないリソースアイテムを指定した場合にinsertせず、{@link NotFoundException NotFoundException}をスローします.
	 * 戻り値によって生成されるレスポンスボディは空になります.
	 */
	@Override
	public Object update(RequestMessage requestMessage) throws AbstractResourceException {

		UrlTreeContext ctx = createContext(requestMessage);

		String path = getId(requestMessage);

		if (!(boolean) exists(requestMessage, ctx).get("exists")) {
			throw new NotFoundException("Requested item is not found : " + path, requestMessage);
		}

		// ファイルデータを含めて更新する
		UrlTreeMetaData<InputStream> item = createUrlTreeMetadata(requestMessage, UpdateRequestType.NORMAL, ctx, true);
		UrlTreeMetaData<InputStream> result = urlTreeResource.doUpdate(item);

		checkStatus(result, requestMessage);

		return EMPTY_RETURNVALUE;
	}

	/**
	 * IDで指定されたリソースアイテムが存在するかどうかを確認するアクションに対応するメソッド.<br/>
	 * リソースアイテムが存在しても、認可やロックの状態によってアクセスできない場合は例外がスローされます.
	 *
	 * @param requestMessage
	 * @return IDと存在有無を含むMapオブジェクト
	 */
	@Override
	public Map<String, Object> exists(RequestMessage requestMessage) throws AbstractResourceException {
		UrlTreeContext ctx = createContext(requestMessage);
		return exists(requestMessage, ctx);
	}

	/**
	 * IDで指定されたリソースアイテムが存在するかどうかを確認する内部処理メソッド.
	 *
	 * @param requestMessage リクエストメッセージ
	 * @param ctx urlTreeコンテキストオブジェクト
	 * @return IDと存在有無を含むMapオブジェクト
	 * @throws AbstractResourceException リソースアイテムへのアクセスに関する例外
	 */
	protected Map<String, Object> exists(RequestMessage requestMessage, UrlTreeContext ctx)
			throws AbstractResourceException {

		String path = getId(requestMessage);

		UrlTreeMetaData<InputStream> urlTreeMetadata = urlTreeResource.doGet(true, ctx, path).get(path);

		/**
		 * NOTFOUNDやDELETEDは例外でなくexistsがfalseになるようにする
		 */
		checkStatus(urlTreeMetadata, requestMessage, ResponseStatus.NOT_FOUND, ResponseStatus.DELETED);

		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("id", getId(requestMessage));
		resultMap.put("exists", urlTreeMetadata.getResponseStatus().equals(ResponseStatus.OK));

		return resultMap;
	}

	/**
	 * IDで指定されたリソースアイテムが存在するかどうかを確認する内部処理メソッド.
	 *
	 * @param requestMessage リクエストメッセージ
	 * @param ctx urlTreeコンテキストオブジェクト
	 * @return IDと存在有無を含むMapオブジェクト
	 * @throws AbstractResourceException リソースアイテムへのアクセスに関する例外
	 */
	protected Map<String, Object> exists(String path, RequestMessage requestMessage, UrlTreeContext ctx)
			throws AbstractResourceException {

		UrlTreeMetaData<InputStream> urlTreeMetadata = urlTreeResource.doGet(true, ctx, path).get(path);

		/**
		 * NOTFOUNDやDELETEDは例外でなくexistsがfalseになるようにする
		 */
		checkStatus(urlTreeMetadata, requestMessage, ResponseStatus.NOT_FOUND, ResponseStatus.DELETED);

		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("id", path);
		resultMap.put("exists", urlTreeMetadata.getResponseStatus().equals(ResponseStatus.OK));

		return resultMap;
	}

	/**
	 * {@link GenericUrlTreeFileResource#findById(RequestMessage)}のmetadataOnly設定時と同じ動作を行います.
	 */
	@Override
	public List<String> list(RequestMessage requestMessage) throws AbstractResourceException {

		UrlTreeContext ctx = createContext(requestMessage);

		String path = getId(requestMessage);

		// 実データ不要のため、metadataOnly=true
		boolean metadataOnly = true;
		Map<String, UrlTreeMetaData<InputStream>> urlTreeMetadataMap = urlTreeResource.doGet(metadataOnly, ctx, path);

		// 1件でもステータスがOKでなければその例外がスローされる
		for (String key : urlTreeMetadataMap.keySet()) {
			checkStatus(urlTreeMetadataMap.get(key), requestMessage);
		}

		return new ArrayList<>(urlTreeMetadataMap.keySet());
	}

	/**
	 * listによって取得できるリソースアイテムの数を返すアクションを実行するメソッド.
	 */
	@Override
	public Integer count(RequestMessage requestMessage) throws AbstractResourceException {

		UrlTreeContext ctx = createContext(requestMessage);

		String path = getId(requestMessage);

		Map<String, UrlTreeMetaData<InputStream>> urlTreeMetadataMap = urlTreeResource.doGet(true, ctx, path);

		// 1件でもステータスがOKでなければその例外がスローされる
		for (String key : urlTreeMetadataMap.keySet()) {
			checkStatus(urlTreeMetadataMap.get(key), requestMessage);
		}

		return urlTreeMetadataMap.size();
	}

	/**
	 * IDで指定されたリソースアイテムのロックを取得します.
	 */
	@Override
	public Map<String, Object> lock(RequestMessage requestMessage) throws AbstractResourceException {

		return lockOrUnlock(requestMessage, UpdateRequestType.LOCK);
	}

	/**
	 * IDで指定されたリソースアイテムのロックを開放します.
	 */
	@Override
	public Map<String, Object> unlock(RequestMessage requestMessage) throws AbstractResourceException {

		return lockOrUnlock(requestMessage, UpdateRequestType.UNLOCK);
	}

	/**
	 * lock,unlockの内部実処理メソッド.<br/>
	 * UpdateRequestTypeによっていずれかの処理が実行されます.
	 *
	 * @param requestMessage リクエストメッセージ
	 * @param updateRequestType LOCKまたはUNLOCK
	 * @return IDとロックトークンを含むMapオブジェクト
	 * @throws AbstractResourceException
	 */
	protected Map<String, Object> lockOrUnlock(RequestMessage requestMessage, UpdateRequestType updateRequestType)
			throws AbstractResourceException {

		UrlTreeContext ctx = createContext(requestMessage);

		String path = getId(requestMessage);

		if (!(boolean) exists(requestMessage, ctx).get("exists")) {
			throw new NotFoundException("Requested item is not found : " + path, requestMessage);
		}

		UrlTreeMetaData<InputStream> item = createUrlTreeMetadata(requestMessage, updateRequestType, ctx, false);

		UrlTreeMetaData<InputStream> result = urlTreeResource.doUpdate(item);
		checkStatus(result, requestMessage);

		String lockToken = result.getUrlTreeContext().getLockToken();

		// body
		Map<String, Object> body = new HashMap<>();
		body.put("id", path);
		body.put("lockToken", lockToken);

		return body;
	}

	/**
	 * 悲観的ロックによるリソースアイテムの取得はサポートされません.
	 */
	@Override
	public Object findByIdForUpdate(RequestMessage requestMessage) throws AbstractResourceException {
		throw new NotImplementedException("This resource is not supported forUpdate-find action", requestMessage);
	}

	/**
	 * 悲観的ロックによるリソースアイテムの取得はサポートされません.
	 */
	@Override
	public Object findByQueryForUpdate(RequestMessage requestMessage) throws AbstractResourceException {
		throw new NotImplementedException("This resource is not supported forUpdate-find action", requestMessage);
	}

	/**
	 * リクエストメッセージから、このリソースアイテムのIDを抽出して返します.
	 */
	@Override
	public String getId(RequestMessage requestMessage) throws AbstractResourceException {
		String id = requestMessage.getPath();
		return id;
	}

	/**
	 * IDでリソースアイテムを検索し、これを複製、保存するアクションに対応するメソッド.
	 */
	@Override
	public Object copy(RequestMessage requestMessage) throws AbstractResourceException {

		UrlTreeContext ctx = createContext(requestMessage);

		String path = getId(requestMessage);

		// 1件のみ前提(ディレクトリ指定は可能)
		Map<String, UrlTreeMetaData<InputStream>> urlTreeMetadataMap = urlTreeResource.doGet(true, ctx, path);
		UrlTreeMetaData<InputStream> srcItem = urlTreeMetadataMap.values().iterator().next();

		// ステータス判定、OK以外は例外をスロー
		checkStatus(srcItem, requestMessage);

		MessageMetadata messageMetadata = requestMessage.getMessageMetadata();

		String destDir = (String) requestMessage.get(messageMetadata.DEST_DIR);
		String destPath = destDir +  UrlTreeMetaDataManager.PATH_SEPARATOR  + srcItem.getName();

		if ((boolean) exists(destPath, requestMessage, ctx).get("exists")) {
			throw new ConflictException("Requested item already exists : " + destPath, requestMessage);
		}

		UrlTreeMetaData<InputStream> result = urlTreeResource.doCopy(srcItem, destDir, ctx);

		checkStatus(result, requestMessage);

		return EMPTY_RETURNVALUE;
	}

	/**
	 * IDでリソースアイテムを検索し、これを移動するアクションに対応するメソッド.
	 */
	@Override
	public Object move(RequestMessage requestMessage) throws AbstractResourceException {

		UrlTreeContext ctx = createContext(requestMessage);

		String path = getId(requestMessage);

		// 1件のみ前提(ディレクトリ指定は可能)
		Map<String, UrlTreeMetaData<InputStream>> urlTreeMetadataMap = urlTreeResource.doGet(true, ctx, path);
		UrlTreeMetaData<InputStream> srcItem = urlTreeMetadataMap.values().iterator().next();

		// ステータス判定、OK以外は例外をスロー
		checkStatus(srcItem, requestMessage);

		MessageMetadata messageMetadata = requestMessage.getMessageMetadata();

		String destDir = (String) requestMessage.get(messageMetadata.DEST_DIR);
		String destPath = destDir +  UrlTreeMetaDataManager.PATH_SEPARATOR  + srcItem.getName();

		if ((boolean) exists(destPath, requestMessage, ctx).get("exists")) {
			throw new ConflictException("Requested item already exists : " + destPath, requestMessage);
		}

		UrlTreeMetaData<InputStream> result = urlTreeResource.doMove(srcItem, destDir, ctx);

		checkStatus(result, requestMessage);

		return EMPTY_RETURNVALUE;
	}

	/**
	 * urlTreeリソースが参照するコンテキストオブジェクトを生成します.<br/>
	 * 認可情報やロックトークンを含みます.
	 *
	 * @param requestMessage
	 * @return UrlTreeコンテキストオブジェクト
	 */
	protected UrlTreeContext createContext(RequestMessage requestMessage) {

		MessageMetadata messageMetadata = requestMessage.getMessageMetadata();

		Principal principal = (Principal) requestMessage.get(messageMetadata.USER_PRINCIPAL);
		UrlTreeContext ctx = urlTreeAuthorizationManager.getContext(principal);

		// ロックトークンの設定
		Object lockToken = requestMessage.get(messageMetadata.LOCK_TOKEN);
		if (lockToken != null) {
			ctx.setLockToken((String) lockToken);
		}

		return ctx;
	}

	/**
	 * リクエストの情報からurlTreeMetadataを生成します.<br/>
	 * リソースアイテムであるファイルに関する属性情報、実ファイルデータも含みます.
	 *
	 * @param requestMessage　リクエストメッセージ
	 * @param updateRequestType UpdateRequestType
	 * @param ctx コンテキストオブジェクト
	 * @param dataIncluded ファイルのinsert,update,insertOrUpdateのように、リクエストが対象となる実ファイルデータを含む場合true
	 * @return urlTreeメタデータオブジェクト
	 * @throws AbstractResourceException リソースアイテムアクセスに関する例外
	 */
	protected UrlTreeMetaData<InputStream> createUrlTreeMetadata(RequestMessage requestMessage,
			UpdateRequestType updateRequestType, UrlTreeContext ctx, boolean dataIncluded)
			throws AbstractResourceException {

		UrlTreeMetaData<InputStream> item = new UrlTreeMetaData<InputStream>();

		item.setUrlTreeContext(ctx);

		// 内部でディレクトリパスとファイル名を分割している
		item.setFilename(getId(requestMessage));

		// typeを判定して決定する
		item.setDirectory(isRequestForDirectory(requestMessage));
		item.setUpdateRequestType(updateRequestType);

		// その他セットが必要なものをセットする
		try {
			String ownerId = (String) requestMessage.get("ownerId");
			String groupId = (String) requestMessage.get("groupId");
			String permission = (String) requestMessage.get("permission");
			item.setOwnerId(ownerId);
			item.setGroupId(groupId);
			item.setPermission(permission);
		} catch (ClassCastException e) {
			throw new BadRequestException("Bad user authentication parameter", requestMessage);
		}

		// データを含む場合はitemに設定(ディレクトリはデータを含まない)
		if (dataIncluded && !item.isDirectory()) {
			setContent(item, requestMessage);
		}

		return item;
	}

	/**
	 * リクエストのtypeパラメータを判断し、ディレクトリを示す場合trueを返します.
	 *
	 * @param requestMessage リクエストメッセージ
	 * @return ディレクトリである場合true
	 */
	protected boolean isRequestForDirectory(RequestMessage requestMessage) {

		String type = (String) requestMessage.get("type");

		return type != null && type.equals("dir");
	}

	/**
	 * urlTreeMetadataに実ファイルデータを設定します.<br/>
	 * リクエストメッセージの中のデータを解析し、contentTypeとともにurlTreeMetadataにセットします.
	 *
	 * @param item urlTreeMetadata
	 * @param requestMessage リクエストメッセージ
	 */
	protected void setContent(UrlTreeMetaData<InputStream> item, RequestMessage requestMessage) {

		MessageMetadata metadata = requestMessage.getMessageMetadata();

		// content(data)とそのContent-TypeをRequestMessageから取得
		Object data = requestMessage.get((String) requestMessage.get(metadata.REQUEST_CONTENT_KEY));
		try {
			if (data instanceof FileValueHolder) {
				FileValueHolder fileValueHolder = (FileValueHolder) data;
				item.setContentType(fileValueHolder.getContentType());
				item.setData(fileValueHolder.getInputStream());
			} else if (data instanceof String) {
				item.setContentType(MediaType.TEXT_PLAIN_VALUE);

				String dataStr = (String) data;
				byte[] dataStrBytes = dataStr.getBytes("ISO-8859-1");
				item.setData(new ByteArrayInputStream(dataStrBytes));

			} else {
				item.setContentType((String) requestMessage.get(metadata.HTTP_HEADER_CONTENT_TYPE));
				if (data instanceof InputStream) {
					item.setData((InputStream) data);
				} else {
					item.setData(new ByteArrayInputStream((byte[]) data));
				}
			}
		} catch (IOException e) {
			throw new GenericResourceException(e);
		}
	}

	/**
	 * {@link ResponseStatus ResponseStatus}を解析し、対応する例外をスローします.
	 *
	 * @param urlTreeMetadata
	 * @param requestMessage
	 * @param ignoreStatus このメソッドで判定を行わないResponseStatus
	 * @throws AbstractResourceException　ResponseStatusに対応する例外
	 */
	protected void checkStatus(UrlTreeMetaData<InputStream> urlTreeMetadata, RequestMessage requestMessage,
			ResponseStatus... ignoreStatus) throws AbstractResourceException {

		String path = urlTreeMetadata.getName();
		ResponseStatus responseStatus = urlTreeMetadata.getResponseStatus();

		for (ResponseStatus ignore : ignoreStatus) {
			if (ignore.equals(responseStatus)) {
				return;
			}
		}

		switch (responseStatus) {
			case OK:
				break;
			case NOT_FOUND:
				throw new NotFoundException("Requested path is not found : " + path, requestMessage);
			case DELETED:
				throw new GoneException("Requested path is deleted : " + path, requestMessage);
			case LOCKED:
				throw new LockedException("Requested path is locked : " + path, requestMessage);
			case PERMISSION_DENIED:
				throw new ForbiddenException("Permission denied : " + path, requestMessage);
			case DUPLICATED:
				throw new ConflictException("Requested path is duplicated : " + path, requestMessage);
			case ILLEGAL_ARGUMENT:
			case ILLEGAL_STATE:
				throw new BadRequestException("Requested path, type or another may be wrong : path = " + path,
						requestMessage);
			default:
				break;
		}
	}

	/**
	 * リクエストのcontentTypeがJSON以外である場合にtrueを返します.<br/>
	 *
	 * @param requestMessage リクエストメッセージ
	 * @throws BadRequestException 誤ったcontentTypeの時
	 */
	protected void checkContentType(RequestMessage requestMessage) throws BadRequestException {
		try {
			MessageMetadata metadata = requestMessage.getMessageMetadata();
			String contentTypeStr = (String) requestMessage.get(metadata.HTTP_HEADER_CONTENT_TYPE);
			MediaType m = MediaType.parseMediaType(contentTypeStr);
			if (m.isCompatibleWith(MediaType.APPLICATION_JSON)) {
				return;
			}
		} catch (Exception e) {
			// do nothing and through here.
		}
		throw new BadRequestException("Wrong Content-Type Header", requestMessage);
	}

	/**
	 * @return the urlTreeResource
	 */
	protected UrlTreeResource<InputStream> getUrlTreeResource() {
		return urlTreeResource;
	}

	/**
	 * @return the urlTreeAuthorizationManager
	 */
	protected UrlTreeAuthorizationManager getUrlTreeAuthorizationManager() {
		return urlTreeAuthorizationManager;
	}

	/**
	 * insertOrUpdate, insert, updateの対象ファイルデータの追加処理結果を蓄積するヘルパークラス.
	 *
	 * @author kishigam
	 */
	protected class UploadFileDataEditHelper {

		/**
		 * 追加処理対象となる元のリクエストメッセージ
		 */
		private RequestMessage orgRequestMessage;

		/**
		 * 追加処理結果のリクエストメッセージのリスト
		 */
		private List<RequestMessage> editedMessages = new ArrayList<>();

		/**
		 * 追加処理可否(成否)
		 */
		private boolean editable = true;

		/**
		 * 追加処理対象となる元のリクエストメッセージを指定してヘルパークラスインスタンスを生成します.
		 *
		 * @param orgRequestMessage リクエストメッセージ
		 */
		public UploadFileDataEditHelper(RequestMessage orgRequestMessage) {
			this.orgRequestMessage = orgRequestMessage;
		}

		/**
		 * 追加処理が可能である場合、trueを返します.<br/>
		 * 追加処理が実行されて、事前の判定で処理不可能となった場合はfalseを返すように、{@link this#setEditable(boolean) setEditable}メソッドを使用します.
		 *
		 * @return 追加処理が可能である場合true
		 */
		public boolean isEditable() {
			return editable;
		}

		/**
		 * 追加処理可否(成否)を設定します.
		 *
		 * @param editable
		 */
		public void setEditable(boolean editable) {
			this.editable = editable;
		}

		/**
		 * 追加処理結果となるRequestMessageをこのヘルパーに追加します.<br/>
		 * 元のリクエストメッセージの情報に対して、ファイルパス、content-type(MIMEタイプ)、データを読み込むためのInputStreamを上書きしたコピーを生成します.
		 *
		 * @param path　ファイルパス
		 * @param contentType MIMEタイプを表す文字列
		 * @param data データを読み込むためのInputStream
		 */
		public void addEditedMessage(String path, String contentType, InputStream data) {
			MessageMetadata messageMetadata = orgRequestMessage.getMessageMetadata();
			RequestMessage adding = new RequestMessage(messageMetadata);

			// 元リクエストメッセージのkey,valueをシャローコピー
			for (String key : orgRequestMessage.keys()) {
				adding.put(key, orgRequestMessage.get(key));
			}

			// 引数のデータを上書き
			adding.put(messageMetadata.REQUEST_PATH, path);
			adding.put(messageMetadata.HTTP_HEADER_CONTENT_TYPE, contentType);
			adding.put(messageMetadata.REQUEST_CONTENT_KEY, messageMetadata.REQUEST_CONTENT);
			adding.put(messageMetadata.REQUEST_CONTENT, data);

			editedMessages.add(adding);
		}

		/**
		 * @return the editedMessages
		 */
		public List<RequestMessage> getEditedMessages() {
			return editedMessages;
		}

		/**
		 * @param editedMessages the editedMessages to set
		 */
		public void setEditedMessages(List<RequestMessage> editedMessages) {
			this.editedMessages = editedMessages;
		}
	}
}
