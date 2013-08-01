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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

/**
 * リクエストに含まれるファイルデータ値のホルダー実装クラス.<br>
 * {@link MultipartFile MultipartFile}オブジェクトを内包しており、 その情報を{@link FileValueHolder FileValueHolder}
 * インターフェースを通じて取得することができます.
 *
 * @author kishigam
 */
public class MultipartFileValueHolder implements FileValueHolder {

	/**
	 * Springが扱うMultipartファイルデータのラッパー.<br/>
	 * このホルダークラスの保持するファイル情報の取得元です.
	 */
	private MultipartFile fileValue;

	/**
	 * {@link MultipartFile MultipartFile}を持つホルダーオブジェクトを生成します.
	 *
	 * @param fileValue
	 */
	public MultipartFileValueHolder(MultipartFile fileValue) {
		this.fileValue = fileValue;
	}

	/**
	 * @see MultipartFile#getName()
	 */
	@Override
	public String getName() {
		return fileValue.getName();
	}

	/**
	 * @see MultipartFile#getContentType()
	 */
	public String getContentType() {
		return fileValue.getContentType();
	}

	/**
	 * @see MultipartFile#getSize()
	 */
	@Override
	public long getSize() {
		return fileValue.getSize();
	}

	/**
	 * @see MultipartFile#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		return fileValue.getInputStream();
	}

	/**
	 * @see MultipartFile#transferTo(File)
	 */
	@Override
	public void transferTo(File dest) throws IOException, IllegalStateException {
		fileValue.transferTo(dest);
	}

	/**
	 * @see MultipartFile#getOriginalFilename()
	 */
	@Override
	public String getOriginalFilename() {
		return fileValue.getOriginalFilename();
	}

	/**
	 * @see MultipartFile#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return fileValue.isEmpty();
	}

	/**
	 * @see MultipartFile#getBytes()
	 */
	@Override
	public byte[] getBytes() throws IOException {
		return fileValue.getBytes();
	}
}
