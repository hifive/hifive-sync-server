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
package com.htmlhifive.resourcefw.file.persister;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;

import com.htmlhifive.resourcefw.file.UrlTreeMetaData;
import com.htmlhifive.resourcefw.file.auth.UrlTreeContext;
import com.htmlhifive.resourcefw.file.exception.BadContentException;
import com.htmlhifive.resourcefw.file.exception.TargetNotFoundException;

/**
 * ストレージへのファイルデータ保存を担うPersisterインタフェース.
 *
 * @author kawaguch
 */
public interface ContentsPersister<T> {

	/**
	 * ファイルデータをストレージからロードします.
	 *
	 * @param metadata urlTreeメタデータオブジェクト
	 * @param ctx urlTreeコンテキストオブジェクト
	 * @return ロードされたファイルデータ
	 */
	public T load(UrlTreeMetaData<T> metadata, UrlTreeContext ctx) throws BadContentException, TargetNotFoundException;

	/**
	 * ファイルデータをストレージに保存します.
	 *
	 * @param metadata ファイルデータを含むurlTreeメタデータオブジェクト
	 * @param ctx urlTreeコンテキストオブジェクト
	 */
	public void save(UrlTreeMetaData<T> metadata, UrlTreeContext ctx) throws BadContentException;

	/**
	 * ディレクトリデータをストレージに保存(ディレクトリを作成)します.
	 *
	 * @param metadata ディレクトリデータを含むurlTreeメタデータオブジェクト
	 * @param ctx urlTreeコンテキストオブジェクト
	 */
	public void mkdir(UrlTreeMetaData<T> metadata, UrlTreeContext ctx) throws BadContentException,
			FileAlreadyExistsException;

	/**
	 * ディレクトリデータをストレージから削除します.
	 *
	 * @param metadata ディレクトリデータを含むurlTreeメタデータオブジェクト
	 * @param ctx urlTreeコンテキストオブジェクト
	 */
	public void rmdir(UrlTreeMetaData<T> metadata, UrlTreeContext ctx) throws BadContentException,
			FileNotFoundException;

	/**
	 * ファイルデータをストレージから削除します.
	 *
	 * @param metadata ディレクトリデータを含むurlTreeメタデータオブジェクト
	 * @param ctx urlTreeコンテキストオブジェクト
	 */
	public void delete(UrlTreeMetaData<T> metadata, UrlTreeContext ctx) throws BadContentException;


	/**
	 * ファイルデータをストレージ上でコピーします.
	 *
	 * @param metadata ディレクトリデータを含むurlTreeメタデータオブジェクト
	 * @param ctx urlTreeコンテキストオブジェクト
	 */
	public void copy(UrlTreeMetaData<T> metadata, String dstDir, UrlTreeContext ctx) throws BadContentException;

	/**
	 * ファイルデータをストレージ上でコピーします.
	 *
	 * @param metadata ディレクトリデータを含むurlTreeメタデータオブジェクト
	 * @param ctx urlTreeコンテキストオブジェクト
	 */
	public void move(UrlTreeMetaData<T> metadata, String dstDir, UrlTreeContext ctx) throws BadContentException;

	/**
	 * キー情報だけでロードが可能かどうかを確認し、可能な場合trueを返します.
	 *
	 * @param key ファイルデータのキー情報
	 * @return ロード可能なときtrue
	 */
	public boolean canLoad(String key, UrlTreeContext ctx) throws BadContentException;

	/**
	 * キー情報から実ファイルデータを取得し、それらからメタデータを生成して返します.
	 *
	 * @param key ファイルデータのキー情報
	 * @return キーから生成したメタデータ
	 */
	public UrlTreeMetaData<T> generateMetaDataFromReal(String key, UrlTreeContext ctx) throws BadContentException;

	/**
	 * 指定されたディレクトリのキー情報から配下のファイルのキー情報リストを返します.
	 *
	 * @param key ディレクトリのキー情報
	 * @param ctx urlTreeコンテキストオブジェクト
	 * @return キー情報のリスト
	 */
	public List<String> getChildList(String key, UrlTreeContext ctx) throws BadContentException;

	/**
	 * ストレージ上に保存されている、ファイルデータの最終更新時刻を返します.
	 *
	 * @param metadata urlTreeメタデータオブジェクト
	 * @param ctx urlTreeコンテキストオブジェクト
	 * @return ファイルデータの最終更新時刻
	 */
	public long getLastUpdatedTime(UrlTreeMetaData<T> metadata, UrlTreeContext ctx) throws BadContentException, TargetNotFoundException;

}
