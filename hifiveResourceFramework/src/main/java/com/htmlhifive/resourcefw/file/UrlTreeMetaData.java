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
/**
 *
 */
package com.htmlhifive.resourcefw.file;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;

import com.htmlhifive.resourcefw.file.auth.UrlTreeContext;
import com.htmlhifive.resourcefw.file.metadata.UrlTreeDTO;
import com.htmlhifive.resourcefw.file.metadata.UrlTreeMetaDataManager;
import com.htmlhifive.resourcefw.file.metadata.UrlTreeNodePrimaryKey;

/**
 * urlTreeファイルリソースの属性情報を保持するメタデータクラス.<br/>
 * {@link UrlTreeDTO UrlTreeDTO}のデータに加え、実ファイルデータとcontentType、およびそれらに関するリクエストとレスポンス(結果)の状態を受け渡すために使用します.
 *
 * @author kawaguch
 * @param <T>
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class UrlTreeMetaData<T> extends UrlTreeDTO {

	private static final long serialVersionUID = 3830498234838753406L;

	/**
	 * ファイルデータのcontentType
	 */
	private MediaType contentType;

	/**
	 * 実ファイルデータ
	 */
	@JsonIgnore
	private T data;

	/**
	 * 更新リクエストの種類を示すタイプ.<br/>
	 * 新規生成の場合、削除の場合は{@link UpdateRequestType#NONE NONE}を設定します(初期値).
	 */
	@JsonIgnore
	private UpdateRequestType updateRequestType = UpdateRequestType.NONE;

	/**
	 * UrlTreeコンテキストオブジェクト
	 */
	@JsonIgnore
	private UrlTreeContext urlTreeContext;

	/**
	 * ファイル更新の結果を示すタイプ.
	 *
	 * @see ResponseStatus
	 */
	private ResponseStatus responseStatus;

	/**
	 * {@link UrlTreeDTO UrlTreeDTO}の情報をこのファイルメタデータオブジェクトにコピーし、返します.
	 *
	 * @param utd UrlTreeDTOオブジェクト
	 * @return コピーされたurlTreeMetadataオブジェクト
	 */
	public UrlTreeMetaData<T> set(UrlTreeDTO utd) {
		BeanUtils.copyProperties(utd, this);
		return this;
	}

	/**
	 * このファイルメタデータの絶対パスを返します.<br/>
	 * ルートパスは{@link UrlTreeMetaDataManager#PATH_SEPARATOR PATH_SEPARATOR}のみであらわされます
	 *
	 * @return 絶対パスを表す文字列
	 */
	public String getAbsolutePath() {
		String p = this.getParent();
		StringBuilder returnStr = new StringBuilder();
		;
		if (StringUtils.isBlank(p) || p.equals(UrlTreeMetaDataManager.ROOT_NAME)) {
			returnStr.append(this.getName());
		} else {
			returnStr.append(this.getParent() + UrlTreeMetaDataManager.PATH_SEPARATOR + this.getName());
		}

		if (returnStr.toString().startsWith(UrlTreeMetaDataManager.ROOT_NAME)) {
			returnStr.substring(UrlTreeMetaDataManager.ROOT_NAME.length());
		}

		return returnStr.toString();
	}

	@Deprecated
	public void setAbsolutePath(String args) {
		// なにもしない。無視
	}

	/**
	 * contentTypeに対応する{@link MediaType MediaType}オブジェクトを返します.
	 *
	 * @return MediaTypeオブジェクト
	 */
	@JsonIgnore
	public MediaType getRawContentType() {
		return contentType;
	}

	/**
	 * このメタデータオブジェクトにファイル名(ファイルパス)を設定します. {@link UrlTreeNodePrimaryKey UrlTreeNodePrimaryKey}
	 * を生成し、その中で決定するparent,keyをこのオブジェクトで保持します.
	 *
	 * @param resourceName ファイル名(ファイルパス)
	 */
	public void setFilename(String resourceName) {
		UrlTreeNodePrimaryKey pk = UrlTreeUtil.generateUrlTreeNodePrimaryKey(resourceName);
		this.setParent(pk.getParent());
		this.setName(pk.getName());
	}

	/**
	 * このメタデータの表すファイルパスの親となるディレクトリのパス文字列を返します.<br/>
	 * 親がルート、あるいは自身がルートだった場合、空文字列となります.
	 */
	@Override
	public String getParent() {
		String p = super.getParent();
		// rootディレクトリの除去
		if (StringUtils.isBlank(p) || p.equals(UrlTreeMetaDataManager.ROOT_NAME) || p.equals(UrlTreeMetaDataManager.PATH_SEPARATOR)) {
			// rootまたはrootの親を表す特殊文字列("")を返す.
			p = "";
		}

		// ルートを表す文字列を除去する
		if (p.startsWith(UrlTreeMetaDataManager.ROOT_NAME)) {
			p = p.substring(UrlTreeMetaDataManager.ROOT_NAME.length());
		}

		return p;
	}

	/**
	 * @return contentType
	 */
	public String getContentType() {
		return contentType.toString();
	}

	/**
	 * @param contentTypeStr セットする contentTypeStr
	 */
	public void setContentType(String contentTypeStr) {
		this.contentType = MediaType.parseMediaType(contentTypeStr);
	}

	/**
	 * @return data
	 */
	public T getData() {
		return data;
	}

	/**
	 * @param data セットする data
	 */
	public void setData(T data) {
		this.data = data;
	}

	/**
	 * @return updateRequestType
	 */
	public UpdateRequestType getUpdateRequestType() {
		return updateRequestType;
	}

	/**
	 * @param updateRequestType セットする updateRequestType
	 */
	public void setUpdateRequestType(UpdateRequestType updateRequestType) {
		this.updateRequestType = updateRequestType;
	}

	/**
	 * @return the urlTreeContext
	 */
	public UrlTreeContext getUrlTreeContext() {
		return urlTreeContext;
	}

	/**
	 * @param urlTreeContext the urlTreeContext to set
	 */
	public void setUrlTreeContext(UrlTreeContext urlTreeContext) {
		this.urlTreeContext = urlTreeContext;
	}

	/**
	 * @return responseStatus
	 */
	public ResponseStatus getResponseStatus() {
		return responseStatus;
	}

	/**
	 * @param responseStatus セットする responseStatus
	 */
	public void setResponseStatus(ResponseStatus responseStatus) {
		this.responseStatus = responseStatus;
	}

	/* == KEY が予約語だった件対応 */
	public String getKey() {
		return this.getName();
	}
	public void setKey(String key) {
		this.setName(key);
	}

	@JsonIgnore
	@Override
	public String getName() {
		return super.getName();
	}
	/* == ここまで */

	/**
	 * 更新リクエストの種類を表すタイプの列挙型.
	 *
	 * @author kawagch
	 */
	public enum UpdateRequestType {
		NONE, NORMAL, METADATA, LOCK, UNLOCK,
	}

	/**
	 * ファイル更新の結果を示すタイプの列挙型.
	 *
	 * @author kawaguch
	 */
	public enum ResponseStatus {
		OK, NOT_FOUND, DELETED, PERMISSION_DENIED, LOCKED, DUPLICATED, NOT_MODIFIED, ILLEGAL_ARGUMENT, ILLEGAL_STATE
	}
}
