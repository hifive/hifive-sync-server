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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;

import com.htmlhifive.resourcefw.exception.GenericResourceException;
import com.htmlhifive.resourcefw.file.UrlTreeMetaData;
import com.htmlhifive.resourcefw.file.auth.UrlTreeContext;
import com.htmlhifive.resourcefw.file.exception.BadContentException;
import com.htmlhifive.resourcefw.file.exception.TargetNotFoundException;

/**
 * ローカルファイルシステムをストレージとするContentsPersister実装.
 *
 * @author kawaguch
 */
public class LocalFileContentsPersister implements ContentsPersister<InputStream> {

	private static final Logger logger = Logger.getLogger(LocalFileContentsPersister.class);

	/**
	 * ストレージ上のベースパス.
	 */
	private String basePath;

	/**
	 * ベースパスのPathオブジェクト.
	 */
	private Path baseDir;

	/**
	 * 初期化処理としてベースパスの存在を確認し、このContentsPersisterを使用可能にします.
	 */
	@PostConstruct
	public void init() {
		FileSystem fs = FileSystems.getDefault();
		baseDir = fs.getPath(basePath);

		if (!Files.exists(baseDir) || !Files.isDirectory(baseDir)) {
			throw new IllegalArgumentException("baseDirectory is not exists or not Directory");
		}
	}

	/**
	 * ファイルデータをストレージからロードします.
	 *
	 * @param metadata urlTreeメタデータオブジェクト
	 * @param ctx urlTreeコンテキストオブジェクト
	 * @return ロードされたファイルデータ
	 */
	@Override
	public InputStream load(UrlTreeMetaData<InputStream> metadata, UrlTreeContext ctx) throws BadContentException,
			TargetNotFoundException {
		Path f = this.generateFileObj(metadata.getAbsolutePath());

		if (!Files.exists(f) || !Files.isReadable(f)) {
			throw new TargetNotFoundException("cannot read real file");
		}

		InputStream contents;
		try {
			contents = Files.newInputStream(f);
		} catch (IOException e) {
			throw new GenericResourceException(e);
		}

		String contentType = new MimetypesFileTypeMap().getContentType(f.getFileName().toString());
		metadata.setContentType(contentType);

		return contents;
	}

	/**
	 * ファイルデータをストレージに保存します.
	 *
	 * @param metadata ファイルデータを含むurlTreeメタデータオブジェクト
	 * @param ctx urlTreeコンテキストオブジェクト
	 */
	@Override
	public void save(UrlTreeMetaData<InputStream> metadata, UrlTreeContext ctx) throws BadContentException {

		String localFileName = metadata.getAbsolutePath();

		logger.debug("saving " + localFileName);
		Path f = this.generateFileObj(localFileName);

		InputStream b = metadata.getData();
		// nullの場合は書き換えない。
		if (b == null) {
			return;
		}

		try {
			Path parent = f.getParent();

			// 書き込む場所がなければ親ディレクトリを作成
			if (Files.notExists(parent)) {
				Files.createDirectories(parent);
			}
			Files.copy(b, f, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new GenericResourceException(e);
		}
	}

	/**
	 * ディレクトリデータをストレージから削除します.
	 *
	 * @param metadata ディレクトリデータを含むurlTreeメタデータオブジェクト
	 * @param ctx urlTreeコンテキストオブジェクト
	 */
	@Override
	public void delete(UrlTreeMetaData<InputStream> metadata, UrlTreeContext ctx) throws BadContentException {
		Path f = generateFileObj(metadata.getAbsolutePath());
		logger.debug("delete: " + f.getFileName());
		try {
			boolean deleted = Files.deleteIfExists(f);
			if (!deleted) {
				throw new IOException("file not exists");
			}
		} catch (IOException e) {
			// TargetNotFoundまたはBadContentでもよいか
			throw new GenericResourceException("cannot delete file", e);
		}
	}


	@Override
	public void copy(UrlTreeMetaData<InputStream> metadata, String dstDir, UrlTreeContext ctx)
			throws BadContentException {

		String srcPathName = metadata.getAbsolutePath();
		Path srcPath = this.generateFileObj(srcPathName);
		Path dstPath = this.generateFileObj(dstDir);

		logger.debug("copy: " + srcPath.toAbsolutePath() + " to " + dstPath.toAbsolutePath());

		try {
			Files.copy(srcPath, dstPath.resolve(srcPath.getFileName()), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
		} catch(IOException e) {
			throw new GenericResourceException("cannot copy file", e);
		}
	}

	@Override
	public void move(UrlTreeMetaData<InputStream> metadata, String dstDir, UrlTreeContext ctx)
			throws BadContentException {

		String srcPathName = metadata.getAbsolutePath();
		Path srcPath = this.generateFileObj(srcPathName);
		Path dstPath = this.generateFileObj(dstDir);

		logger.debug("move: " + srcPath.toAbsolutePath() + " to " + dstPath.toAbsolutePath());

		try {
			Files.move(srcPath, dstPath.resolve(srcPath.getFileName()), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch(IOException e) {
			throw new GenericResourceException("cannot copy file", e);
		}
	}

	/**
	 * ファイル名がこのPersisterで使用可能であればtrueを返します.
	 */
	private boolean isValidFileName(String fileName) {

		// nullはちろんNG
		if (fileName == null) {
			return false;
		}

		// 「.」が２個以上はNG
		if (fileName.matches("\\.{2,}")) {
			return false;
		}

		return true;
	}

	/**
	 * ファイル名(パス)からファイル(Paht)オブジェクトを生成します.
	 *
	 * @param filename ファイル名
	 * @return pathオブジェクト
	 * @throws BadContentException
	 */
	private Path generateFileObj(String filename) throws BadContentException {
		if (isValidFileName(filename)) {
			return FileSystems.getDefault().getPath(baseDir.toAbsolutePath().toString(), filename);
		} else {
			throw new BadContentException("illegal file name : " + filename);
		}
	}

	/**
	 * ディレクトリデータをストレージに保存(ディレクトリを作成)します.
	 *
	 * @param metadata ディレクトリデータを含むurlTreeメタデータオブジェクト
	 * @param ctx urlTreeコンテキストオブジェクト
	 */
	@Override
	public void mkdir(UrlTreeMetaData<InputStream> metadata, UrlTreeContext ctx) throws BadContentException,
			FileAlreadyExistsException {
		Path f = generateFileObj(metadata.getAbsolutePath());
		if (Files.exists(f)) {
			throw new FileAlreadyExistsException("file exists");
		}

		try {
			Files.createDirectories(f);
		} catch (IOException e) {
			logger.error("mkdir failure", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * ディレクトリデータをストレージから削除します.
	 *
	 * @param metadata ディレクトリデータを含むurlTreeメタデータオブジェクト
	 * @param ctx urlTreeコンテキストオブジェクト
	 */
	@Override
	public void rmdir(UrlTreeMetaData<InputStream> metadata, UrlTreeContext ctx) throws BadContentException,
			FileNotFoundException {
		Path f = generateFileObj(metadata.getAbsolutePath());
		logger.debug("called rmdir: " + f.getFileName());
		if (!Files.exists(f)) {
			throw new FileNotFoundException("file not exists");
		}

		try {
			Files.delete(f);
		} catch (IOException e) {
			logger.error("rmdir failure", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * キー情報だけでロードが可能かどうかを確認し、可能な場合trueを返します.
	 *
	 * @param key ファイルデータのキー情報
	 * @return ロード可能なときtrue
	 */
	@Override
	public boolean canLoad(String key, UrlTreeContext ctx) throws BadContentException {
		if (key == null) {
			return false;
		}

		if (!isValidFileName(key)) {
			return false;
		}

		String key2;
		if (key.equals("root")) {
			key2 = "";
		} else {
			key2 = key;
		}

		Path f = this.generateFileObj(key2);
		logger.debug(f.toString() + ": canload called");
		return Files.exists(f, LinkOption.NOFOLLOW_LINKS);
	}

	/**
	 * キー情報から実ファイルデータを取得し、それらからメタデータを生成して返します.
	 *
	 * @param key ファイルデータのキー情報
	 * @return キーから生成したメタデータ
	 */
	@Override
	public UrlTreeMetaData<InputStream> generateMetaDataFromReal(String key, UrlTreeContext ctx)
			throws BadContentException {
		if (!this.canLoad(key, ctx)) {
			throw new IllegalArgumentException("Cannot Load it. check before can it load with canLoad(key): " + key);
		}
		Path p = this.generateFileObj(key);
		File f = p.toFile();
		long lastModified = f.lastModified();

		UrlTreeMetaData<InputStream> md = new UrlTreeMetaData<>();
		md.setDirectory(Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS));
		md.setFilename(key);
		md.setOwnerId(ctx.getUserName());
		md.setGroupId(ctx.getPrimaryGroup());
		md.setCreatedTime(lastModified);
		md.setUpdatedTime(lastModified);
		md.setPermission(ctx.getDefaultPermission());
		String contentType = new MimetypesFileTypeMap().getContentType(p.getFileName().toString());
		md.setContentType(contentType);

		return md;
	}

	/**
	 * 指定されたディレクトリのキー情報から配下のファイルのキー情報リストを返します.
	 *
	 * @param key ディレクトリのキー情報
	 * @param ctx urlTreeコンテキストオブジェクト
	 * @return キー情報のリスト
	 */
	@Override
	public List<String> getChildList(String key, UrlTreeContext ctx) throws BadContentException {
		if (!this.canLoad(key, ctx)) {
			throw new IllegalArgumentException("Cannot Load it. check before can it load with canLoad(key)");
		}
		Path p = this.generateFileObj(key);
		File f = p.toFile();
		return Arrays.asList(f.list());
	}

	/**
	 * @return basePath
	 */
	public String getBasePath() {
		return basePath;
	}

	/**
	 * @param basePath セットする basePath
	 */
	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	@Override
	public long getLastUpdatedTime(UrlTreeMetaData<InputStream> metadata, UrlTreeContext ctx) throws BadContentException,
			TargetNotFoundException {
		Path f = this.generateFileObj(metadata.getAbsolutePath());

		if (!Files.exists(f) || !Files.isReadable(f)) {
			throw new TargetNotFoundException("cannot read real file");
		}

		try {
			return Files.getLastModifiedTime(f).toMillis();
		} catch (IOException e) {
			throw new GenericResourceException(e);
		}
	}

}
