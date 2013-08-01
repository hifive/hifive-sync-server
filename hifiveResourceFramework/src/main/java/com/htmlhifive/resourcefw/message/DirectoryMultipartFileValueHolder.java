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
package com.htmlhifive.resourcefw.message;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

/**
 * リクエストに含まれるディレクトリ単位のファイルデータ値を持つホルダー実装クラス.<br>
 * ディレクトリ内の{@link MultipartFile MultipartFile}オブジェクトをリストで保持しており、 その情報を{@link FileValueHolder FileValueHolder}
 * インターフェースを通じて取得することができます.
 *
 * @author kishigam
 */
public class DirectoryMultipartFileValueHolder {

	/**
	 * ディレクトリパス.
	 */
	private String dirPath;

	/**
	 * ディレクトリに含むファイルデータのリスト.
	 */
	private List<FileValueHolder> fileValues;

	/**
	 * ディレクトリ名を指定して、ディレクトリ用FileValueHolderを生成します.
	 *
	 * @param dirName ディレクトリ名
	 */
	public DirectoryMultipartFileValueHolder(String dirName) {
		this.dirPath = dirName;
		this.fileValues = new ArrayList<>();
	}

	/**
	 * 指定されたFileValueHolderをこのディレクトリ用FileValueHolderに追加します.
	 *
	 * @param fileValue FileValueHolderオブジェクト
	 */
	public void addFileValues(FileValueHolder fileValue) {
		this.fileValues.add(fileValue);
	}

	/**
	 * @return the dirPath
	 */
	public String getDirPath() {
		return dirPath;
	}

	/**
	 * @param dirPath the dirPath to set
	 */
	public void setDirPath(String dirPath) {
		this.dirPath = dirPath;
	}

	/**
	 * @return the fileValues
	 */
	public List<FileValueHolder> getFileValues() {
		return fileValues;
	}

	/**
	 * @param fileValues the fileValues to set
	 */
	public void setFileValues(List<FileValueHolder> fileValues) {
		this.fileValues = fileValues;
	}
}
