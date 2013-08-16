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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.htmlhifive.resourcefw.exception.AbstractResourceException;
import com.htmlhifive.resourcefw.exception.GenericResourceException;
import com.htmlhifive.resourcefw.file.UrlTreeMetaData.ResponseStatus;
import com.htmlhifive.resourcefw.file.auth.AccessMode;
import com.htmlhifive.resourcefw.file.auth.UrlTreeContext;
import com.htmlhifive.resourcefw.file.exception.BadContentException;
import com.htmlhifive.resourcefw.file.exception.DeletedException;
import com.htmlhifive.resourcefw.file.exception.FileLockedException;
import com.htmlhifive.resourcefw.file.exception.HasChildException;
import com.htmlhifive.resourcefw.file.exception.ParentNotFoundException;
import com.htmlhifive.resourcefw.file.exception.PermissionDeniedException;
import com.htmlhifive.resourcefw.file.exception.TargetAlreadyExistsException;
import com.htmlhifive.resourcefw.file.exception.TargetNotFoundException;
import com.htmlhifive.resourcefw.file.exception.TargetTypeException;
import com.htmlhifive.resourcefw.file.metadata.UrlTreeDTO;
import com.htmlhifive.resourcefw.file.metadata.UrlTreeMetaDataManager;
import com.htmlhifive.resourcefw.file.metadata.UrlTreeNodePrimaryKey;
import com.htmlhifive.resourcefw.file.persister.ContentsPersister;

/**
 * urlTreeリソースの処理全般を担う本体ロジッククラス. {@link UrlTreeMetaDataManager UrlTreeMetaDataManager}でメタデータのCRUD処理を、
 * {@link ContentsPersister ContentsPersister}でストレージへのファイルアクセスを それぞれ行います.<br/>
 *
 * @author kawaguch
 * @param <T>　ファイルデータの型
 */
@Service
public class UrlTreeResource<T> {

	private static final Logger logger = Logger.getLogger(UrlTreeMetaDataManager.class);

	/** メタデータ管理マネージャ */
	@Autowired
	private UrlTreeMetaDataManager urlTreeMetaDataManager;

	/** ファイルの中身を永続化するContentsPersister */
	@Autowired
	private ContentsPersister<T> contentsPersister;

	/**
	 * メタデータStrictモード.<br/>
	 * trueで「メタデータ優先」：メタデータとファイルの状態が異なる場合はメタデータを優先します.<br/>
	 * falseで「実データ優先」：メタデータとファイルの状態が異なる場合、ファイル状態を元にメタデータを更新します.
	 */
	private boolean strictMetaData = true;

	/**
	 * 初期化処理を実行します.
	 */
	public void init() {
		urlTreeMetaDataManager.init();
	}

	/**
	 * メタデータおよびファイルを取得し、{@link UrlTreeMetaData UrlTreeMetaData}に格納して返します.<br/>
	 * metadataOnlyの場合、メタデータのみ設定されます.<br/>
	 *
	 * @param metaDataOnly メタデータのみ取得する場合true
	 * @param ctx urlTreeリソースのコンテキストオブジェクト
	 * @param idArray ID(ファイルパス)
	 * @return UrlTreeメタデータオブジェクト
	 */
	public Map<String, UrlTreeMetaData<T>> doGet(boolean metaDataOnly, UrlTreeContext ctx, String... idArray) {

		logger.debug("doget called : " + idArray[0]);
		Map<String, UrlTreeMetaData<T>> resultMap = new HashMap<>();

		for (String id : idArray) {
			// ID文字列の生成
			UrlTreeNodePrimaryKey utnpk = UrlTreeUtil.generateUrlTreeNodePrimaryKey(id);

			// メタデータオブジェクトにはあらかじめキーをセット
			UrlTreeMetaData<T> utm = new UrlTreeMetaData<>();
			utm.setParent(utnpk.getParent());
			utm.setName(utnpk.getName());

			String loadTargetId = id;
			if (id.isEmpty()) {
				id = UrlTreeMetaDataManager.ROOT_NAME;
			}

			boolean metadataLoaded = false;
			// メタデータの取得
			UrlTreeDTO utdto = null;
			try {
				utdto = urlTreeMetaDataManager.get(utnpk.toString(), ctx);
				// UrlTreeDTOをUrlTreeMetaDataに変換
				utm.set(utdto);
				utm.setResponseStatus(ResponseStatus.OK);
				String contentType = new MimetypesFileTypeMap().getContentType(utdto.getName());
				utm.setContentType(contentType);
				metadataLoaded = true;
			} catch (PermissionDeniedException e) {
				utm.setResponseStatus(ResponseStatus.PERMISSION_DENIED);
				// 権限がないのならその先は何もしない
				resultMap.put(id, utm);
				continue;
			} catch (TargetNotFoundException e) {
				utm.setResponseStatus(ResponseStatus.NOT_FOUND);
				// メタデータのの整合性確認のため先へ進む
			} catch (DeletedException e) {
				utm.setResponseStatus(ResponseStatus.DELETED);
				// メタデータの整合性確認のため先へ進む
			}

			// ファイルの所在チェック
			boolean canLoad = false;
			try {
				canLoad = contentsPersister.canLoad(loadTargetId, ctx);
			} catch (BadContentException e1) {
				utm.setResponseStatus(ResponseStatus.ILLEGAL_ARGUMENT);
			}

			// *** メタデータと実データのロード可否による分岐 ***

			// ===== 正常: メタデータとロード可否は一緒
			if (metadataLoaded == canLoad) {

				// 読めるなら中身の生成処理
				if (!metaDataOnly && canLoad) {
					try {
						utm = loadContents(loadTargetId, utm, ctx);
					} catch (PermissionDeniedException e) {
						utm.setResponseStatus(ResponseStatus.PERMISSION_DENIED);
					}
				}

				// 結果をput
				resultMap.put(id, utm);
				continue;
			}

			// =====  異常: メタデータとロード可否が食い違っている

			// けど改変はしない(strictmetadata = true)場合の動作
			if (strictMetaData) {
				if (!metadataLoaded) {
					// メタデータにものがないのなら無視
					resultMap.put(id, utm);
					continue;
				} else {
					// 実データが読めないのでエラー
					//					throw new NotFoundException("Cannot Load File Contents.");

					// 例外スローではなくresponse statusを設定して処理終了
					utm.setResponseStatus(ResponseStatus.NOT_FOUND);
				}
			}

			// 改変が必要な場合(strictmetadata = false)の場合の動作
			try {
				if (canLoad) {
					// メタデータがない場合、作成

					// 作成するメタデータの権限を親ディレクトリに合わせるため
					// 親ディレクトリの情報取得
					// ※親ディレクトリで読めないものがあればここでPermissionDenied.
					UrlTreeDTO u = urlTreeMetaDataManager.searchParentPath(loadTargetId, ctx);

					// ダミーのコンテキスト作成
					UrlTreeContext dummyContext = new UrlTreeContext();
					dummyContext.setUserName(u.getOwnerId());
					dummyContext.setPrimaryGroup(u.getGroupId());
					List<String> dummyGroupList = new ArrayList<>();
					dummyGroupList.add(u.getGroupId());
					dummyContext.setGroups(dummyGroupList);
					dummyContext.setDefaultPermission(u.getPermission());

					utm = contentsPersister.generateMetaDataFromReal(loadTargetId, dummyContext);

					if (utm.isDirectory()) {
						urlTreeMetaDataManager.mkdirRecursive(utm.getAbsolutePath(), dummyContext);
					} else {
						urlTreeMetaDataManager.addEntry(id, utm, dummyContext);
					}

					// 一応一度読み直す
					utdto = urlTreeMetaDataManager.get(utnpk.toString(), ctx);
					utm.set(utdto);

					// content-typeの判断、設定
					String contentType = new MimetypesFileTypeMap().getContentType(utdto.getName());
					utm.setContentType(contentType);

					utm.setResponseStatus(ResponseStatus.OK);
					// 中身の生成処理
					if (!metaDataOnly) {
						utm = loadContents(loadTargetId, utm, ctx);
					}
				} else {
					// メタデータがあるけど、ロードができない
					// →メタデータのほうを削除
					if (utdto.isDirectory()) {
						urlTreeMetaDataManager.rmdir(id, ctx);
					} else {
						urlTreeMetaDataManager.removeEntry(id, ctx);
					}

					utm.setResponseStatus(ResponseStatus.DELETED);
				}
			} catch (HasChildException e) {
				logger.error("MetaData Edit Failure", e);
			} catch (ParentNotFoundException e) {
				logger.error("MetaData Edit Failure", e);
			} catch (PermissionDeniedException e) {
				utm.setResponseStatus(ResponseStatus.PERMISSION_DENIED);
			} catch (TargetTypeException e) {
				logger.error("MetaData Edit Failure", e);
			} catch (TargetAlreadyExistsException e) {
				logger.error("MetaData Edit Failure", e);
			} catch (TargetNotFoundException e) {
				utm.setResponseStatus(ResponseStatus.NOT_FOUND);
			} catch (DeletedException e) {
				utm.setResponseStatus(ResponseStatus.DELETED);
			} catch (FileLockedException e) {
				utm.setResponseStatus(ResponseStatus.PERMISSION_DENIED);
			} catch (BadContentException e) {
				utm.setResponseStatus(ResponseStatus.ILLEGAL_ARGUMENT);
			}

			resultMap.put(id, utm);
		}

		return resultMap;
	}

	/**
	 * 指定されたデータをロードします.<br/>
	 *
	 * @param id　ID(ファイルパス)
	 * @param utm urlTreeMetadataオブジェクト
	 * @param ctx　urlTreeリソースのコンテキストオブジェクト
	 * @return ロードされたデータを含むurlTreeMetadataオブジェクト
	 */
	private UrlTreeMetaData<T> loadContents(String id, UrlTreeMetaData<T> utm, UrlTreeContext ctx)
			throws PermissionDeniedException {

		try {

			if (utm.isDirectory()) {
				// ディレクトリの場合、中身のリストを返す
				List<UrlTreeDTO> lsValue = this.getDirectoryList(utm, id, ctx);
				utm.setChildList(lsValue);
			} else {
				// ファイルの場合、内容を読み取る処理
				T t = contentsPersister.load(utm, ctx);
				utm.setData(t);
			}

		} catch (BadContentException bce) {
			utm.setResponseStatus(ResponseStatus.ILLEGAL_ARGUMENT);
		} catch (TargetNotFoundException tnfe) {
			utm.setResponseStatus(ResponseStatus.NOT_FOUND);
		}

		return utm;
	}

	/**
	 * 指定されたディレクトリ配下の情報を{@link UrlTreeDTO UrlTreeDTO}のリストで返します.
	 *
	 * @param utm urlTreeMetadataオブジェクト
	 * @param id　ID(ディレクトリパス)
	 * @param ctx　urlTreeリソースのコンテキストオブジェクト
	 * @return UrlTreeDTOのリスト
	 */
	private List<UrlTreeDTO> getDirectoryList(UrlTreeMetaData<T> cmd, String id, UrlTreeContext ctx)
			throws PermissionDeniedException, BadContentException {
		// ディレクトリの場合の操作

		// 戻り側都合でUrlTreeDTO型のリストとしている
		List<UrlTreeDTO> lsValue = new ArrayList<>();

		// 突き合わせ用マップ
		Map<String, UrlTreeMetaData<T>> checkMap = new HashMap<>();

		try {
			UrlTreeNodePrimaryKey utnpk = UrlTreeUtil.generateUrlTreeNodePrimaryKey(id);

			List<UrlTreeDTO> orgList = urlTreeMetaDataManager.getChild(utnpk, ctx, false);
			for (UrlTreeDTO dto : orgList) {
				//表示不可でないかパーミッションチェック
				boolean permission = urlTreeMetaDataManager.checkPermission(dto, AccessMode.READ, ctx);
				if (!permission) {
					continue;
				}

				// ContentTypeの付加
				UrlTreeMetaData<T> utm = new UrlTreeMetaData<>();
				utm.set(dto);
				String contentType = new MimetypesFileTypeMap().getContentType(dto.getName());
				utm.setContentType(contentType);

				if (strictMetaData) {
					lsValue.add(utm);
				} else {
					checkMap.put(utm.getName(), utm);
				}
			}

		} catch (TargetNotFoundException e) {
			// 前にチェックしてるのでここに来ることはないはず.
			assert false;
			throw new GenericResourceException("Illegal state : target directory's content cannot found.");
		} catch (TargetTypeException e) {
			// 前にチェックしてるのでここに来ることはないはず.
			assert false;
			throw new GenericResourceException("Illegal state : target is not directory.");
		}

		if (strictMetaData) {
			return lsValue;
		}

		List<String> lsStr = contentsPersister.getChildList(id, ctx);
		for (String str : lsStr) {
			UrlTreeMetaData<T> md = checkMap.get(str);
			String checkStr = id + UrlTreeMetaDataManager.PATH_SEPARATOR + str;

			if (md != null) {
				lsValue.add(md);
			} else {
				if (!contentsPersister.canLoad(checkStr, ctx)) {
					continue;
				}
				md = contentsPersister.generateMetaDataFromReal(checkStr, ctx);
				lsValue.add(md);
			}
		}

		return lsValue;
	}

	/**
	 * メタデータオブジェクトの内容でリソースアイテムを生成し、メタデータと実ファイルをそれぞれ永続化します.
	 *
	 * @param newItem 新規生成するメタデータオブジェクト
	 * @return 　生成されたメタデータオブジェクト
	 */
	public UrlTreeMetaData<T> doCreate(UrlTreeMetaData<T> newItem) {
		logger.debug("called Create");

		UrlTreeContext ctx = newItem.getUrlTreeContext();

		try {
			if (newItem.isDirectory()) {
				urlTreeMetaDataManager.mkdir(newItem.getParent(), newItem.getName(), ctx);
				contentsPersister.mkdir(newItem, ctx);
			} else {
				urlTreeMetaDataManager.addEntry(newItem.getAbsolutePath(), newItem, ctx);
				contentsPersister.save(newItem, ctx);
				urlTreeMetaDataManager.updateEntry(newItem.getAbsolutePath(), newItem, ctx);
			}

			newItem.setResponseStatus(ResponseStatus.OK);

		} catch (ParentNotFoundException e) {
			newItem.setResponseStatus(ResponseStatus.NOT_FOUND);
		} catch (TargetTypeException e) {
			newItem.setResponseStatus(ResponseStatus.ILLEGAL_ARGUMENT);
		} catch (TargetAlreadyExistsException e) {
			newItem.setResponseStatus(ResponseStatus.DUPLICATED);
		} catch (PermissionDeniedException e) {
			newItem.setResponseStatus(ResponseStatus.PERMISSION_DENIED);
		} catch (FileLockedException e) {
			newItem.setResponseStatus(ResponseStatus.LOCKED);
		} catch (TargetNotFoundException e) {
			// 作った後同じもので更新しているので、起きえないはずですが...
			// saveがIDを改ざんすると起きる可能性はある...。
			throw new GenericResourceException(e);
		} catch (FileAlreadyExistsException e) {
			newItem.setResponseStatus(ResponseStatus.DUPLICATED);
		} catch (BadContentException e) {
			newItem.setResponseStatus(ResponseStatus.ILLEGAL_ARGUMENT);
		}

		//		return newItem;.getAbsolutePath();
		return newItem;
	}

	/**
	 * メタデータオブジェクトの内容でリソースアイテムを更新します.
	 *
	 * @param item　更新情報を持つメタデータオブジェクト
	 * @return 更新されたメタデータオブジェクト
	 */
	public UrlTreeMetaData<T> doUpdate(UrlTreeMetaData<T> item) {
		logger.debug("called Update");

		UrlTreeContext ctx = item.getUrlTreeContext();

		UrlTreeMetaData.UpdateRequestType requestType = item.getUpdateRequestType();

		// 処理の種類による分岐
		try {
			switch (requestType) {
				case LOCK:
					urlTreeMetaDataManager.lock(item.getAbsolutePath(), ctx);
					break;
				case METADATA:
					urlTreeMetaDataManager.updateEntry(item.getAbsolutePath(), item, ctx);
					break;
				case NONE:
					break;
				case NORMAL:
					urlTreeMetaDataManager.updateEntry(item.getAbsolutePath(), item, ctx);
					contentsPersister.save(item, ctx);
					urlTreeMetaDataManager.updateEntry(item.getAbsolutePath(), item, ctx);
					break;
				case UNLOCK:
					urlTreeMetaDataManager.unlock(item.getAbsolutePath(), ctx);
				default:
					break;
			}

			item.setResponseStatus(ResponseStatus.OK);

		} catch (TargetNotFoundException e) {
			item.setResponseStatus(ResponseStatus.NOT_FOUND);
		} catch (TargetTypeException e) {
			item.setResponseStatus(ResponseStatus.ILLEGAL_ARGUMENT);
		} catch (PermissionDeniedException e) {
			item.setResponseStatus(ResponseStatus.PERMISSION_DENIED);
		} catch (FileLockedException e) {
			item.setResponseStatus(ResponseStatus.LOCKED);
		} catch (BadContentException e) {
			item.setResponseStatus(ResponseStatus.ILLEGAL_ARGUMENT);
		}

		//		return item.getAbsolutePath();
		return item;
	}

	/**
	 * 指定されたID(ファイルパス)のデータを削除します.
	 *
	 * @param id ID(ファイルパス)
	 * @param recursive 子アイテムを強制的に削除するか否か。削除する場合はture、そうでない場合はfalseを設定.
	 * @param ctx urlTreeリソースのコンテキストオブジェクト
	 * @return 削除されたファイルパスのメタデータオブジェクト
	 * @throws AbstractResourceException
	 */
	public UrlTreeMetaData<T> doDelete(String id, Boolean recursive, UrlTreeContext ctx) throws AbstractResourceException {
		logger.debug("called Delete : " + id);

		Map<String, UrlTreeMetaData<T>> resultMap = this.doGet(true, ctx, id);
		if (resultMap.size() != 1) {
			return null;
		}

		UrlTreeMetaData<T> targetMetaData = resultMap.values().iterator().next();

		if (targetMetaData == null) {
			return null;
		}

		try {

			if(recursive){
				recursiveDelete(targetMetaData, ctx);
			}else{
				if (targetMetaData.isDirectory()) {
					urlTreeMetaDataManager.rmdir(id, ctx);
					contentsPersister.rmdir(targetMetaData, ctx);
				} else {
					urlTreeMetaDataManager.removeEntry(id, ctx);
					contentsPersister.delete(targetMetaData, ctx);
				}
			}

			targetMetaData.setResponseStatus(ResponseStatus.OK);

		} catch (TargetNotFoundException e) {
			targetMetaData.setResponseStatus(ResponseStatus.NOT_FOUND);
		} catch (TargetTypeException e) {
			// 本来ここには来ないはず
			throw new GenericResourceException("Data Consistency Error");
		} catch (PermissionDeniedException e) {
			targetMetaData.setResponseStatus(ResponseStatus.PERMISSION_DENIED);
		} catch (FileLockedException e) {
			targetMetaData.setResponseStatus(ResponseStatus.LOCKED);
		} catch (HasChildException e) {
			targetMetaData.setResponseStatus(ResponseStatus.ILLEGAL_STATE);
		} catch (BadContentException e) {
			targetMetaData.setResponseStatus(ResponseStatus.ILLEGAL_ARGUMENT);
		} catch (FileNotFoundException e) {
			targetMetaData.setResponseStatus(ResponseStatus.NOT_FOUND);
		}

		return targetMetaData;
	}

	/**
	 * リソースアイテム削除再帰処理.<br>
	 * 複製元がディレクトリの場合、その子アイテムも再帰処理により削除します.
	 *
	 * @param srcItem
	 * @param ctx
	 * @throws PermissionDeniedException
	 * @throws BadContentException
	 * @throws TargetNotFoundException
	 * @throws HasChildException
	 * @throws TargetTypeException
	 * @throws FileLockedException
	 */
	@SuppressWarnings("unchecked")
	//TODO:UrlTreeMetaData<T> と URLTreeDTO について整理が必要？
	private void recursiveDelete(UrlTreeDTO srcItem, UrlTreeContext ctx) throws PermissionDeniedException,
			BadContentException, TargetNotFoundException, HasChildException, TargetTypeException, FileLockedException {

		String srcAbsolutePath = getAbsolutePath(srcItem);

		if (srcItem.isDirectory()) {

			List<UrlTreeDTO> directoryList = getDirectoryList(null, srcAbsolutePath, ctx);

			for (UrlTreeDTO nextSrcItem : directoryList) {
				recursiveDelete(nextSrcItem, ctx);
			}

			urlTreeMetaDataManager.rmdir(srcAbsolutePath, ctx);
			contentsPersister.delete((UrlTreeMetaData<T>) srcItem, ctx);

		} else {
			urlTreeMetaDataManager.removeEntry(srcAbsolutePath, ctx);
			contentsPersister.delete((UrlTreeMetaData<T>) srcItem, ctx);
		}
	}

	/**
	 * 指定のディレクトリにリソースアイテムを複製します.
	 *
	 * @param srcItem 複製元のメタデータオブジェクト
	 * @param destDir 複製先ディレクトリ
	 * @param ctx urlTreeリソースのコンテキストオブジェクト
	 * @return 複製先メタデータオブジェクト(ディレクトリの場合、それ自体のみ)
	 * @throws AbstractResourceException
	 */
	public UrlTreeMetaData<T> doCopy(UrlTreeMetaData<T> srcItem, String destDir, UrlTreeContext ctx)
			throws AbstractResourceException {
		logger.debug("called Copy");

		String destPath = destDir + UrlTreeMetaDataManager.PATH_SEPARATOR + srcItem.getName();
		UrlTreeMetaData<T> destItem = copyUrlTreeMetadata(srcItem, destPath, ctx);

		try {

			recursiveCopy(srcItem, destItem, destDir, ctx);

			destItem.setResponseStatus(ResponseStatus.OK);

		} catch (ParentNotFoundException e) {
			destItem.setResponseStatus(ResponseStatus.NOT_FOUND);
		} catch (TargetTypeException e) {
			destItem.setResponseStatus(ResponseStatus.ILLEGAL_ARGUMENT);
		} catch (TargetAlreadyExistsException e) {
			destItem.setResponseStatus(ResponseStatus.DUPLICATED);
		} catch (PermissionDeniedException e) {
			destItem.setResponseStatus(ResponseStatus.PERMISSION_DENIED);
		} catch (BadContentException e) {
			destItem.setResponseStatus(ResponseStatus.ILLEGAL_ARGUMENT);
		}

		return destItem;
	}

	/**
	 * リソースアイテム複製再帰処理.<br>
	 * 複製元がディレクトリの場合、その子アイテムも再帰処理により複製します.
	 *
	 * @param srcItem 複製元のメタデータオブジェクト
	 * @param destItem 複製先のメタデータオブジェクト
	 * @param destDir 複製先ディレクトリ
	 * @param ctx urlTreeリソースのコンテキストオブジェクト
	 * @throws ParentNotFoundException
	 * @throws TargetTypeException
	 * @throws TargetAlreadyExistsException
	 * @throws PermissionDeniedException
	 * @throws TargetNotFoundException
	 * @throws FileLockedException
	 * @throws BadContentException
	 * @throws AbstractResourceException
	 */
	@SuppressWarnings("unchecked")
	//TODO:UrlTreeMetaData<T> と URLTreeDTO について整理が必要？
	private void recursiveCopy(UrlTreeDTO srcItem, UrlTreeDTO destItem, String destDir, UrlTreeContext ctx)
			throws ParentNotFoundException, TargetAlreadyExistsException, PermissionDeniedException,
			TargetTypeException, BadContentException, AbstractResourceException {

		if (srcItem.isDirectory()) {

			String name = destItem.getName();

			urlTreeMetaDataManager.mkdir(destDir, name, ctx);
			contentsPersister.copy((UrlTreeMetaData<T>) srcItem, destDir, ctx);

			String srcAbsolutePath = getAbsolutePath(srcItem);
			List<UrlTreeDTO> directoryList = getDirectoryList(null, srcAbsolutePath, ctx);

			String nextDestDir = destDir + UrlTreeMetaDataManager.PATH_SEPARATOR + name;

			for (UrlTreeDTO nextSrcItem : directoryList) {
				String nextDestPath = nextDestDir + UrlTreeMetaDataManager.PATH_SEPARATOR + nextSrcItem.getName();
				UrlTreeMetaData<T> nextDestItem = copyUrlTreeMetadata(nextSrcItem, nextDestPath, ctx);
				recursiveCopy(nextSrcItem, nextDestItem, nextDestDir, ctx);
			}
		} else {
			String destAbsolutePath = getAbsolutePath(destItem);
			urlTreeMetaDataManager.addEntry(destAbsolutePath, destItem, ctx);
			contentsPersister.copy((UrlTreeMetaData<T>) srcItem, destDir, ctx);
		}
	}

	/**
	 * 指定のディレクトリにリソースアイテムを移動します.
	 *
	 * @param srcItem 移動元のメタデータオブジェクト
	 * @param destDir 移動先ディレクトリ
	 * @param ctx urlTreeリソースのコンテキストオブジェクト
	 * @return 移動先メタデータオブジェクト(ディレクトリの場合、それ自体のみ)
	 * @throws AbstractResourceException
	 */
	public UrlTreeMetaData<T> doMove(UrlTreeMetaData<InputStream> srcItem, String destDir, UrlTreeContext ctx)
			throws AbstractResourceException {

		String destPath = destDir + UrlTreeMetaDataManager.PATH_SEPARATOR + srcItem.getName();
		UrlTreeMetaData<T> destItem = copyUrlTreeMetadata(srcItem, destPath, ctx);

		try {

			recursiveMove(srcItem, destItem, destDir, ctx);

			destItem.setResponseStatus(ResponseStatus.OK);

		} catch (ParentNotFoundException e) {
			destItem.setResponseStatus(ResponseStatus.NOT_FOUND);
		} catch (TargetTypeException e) {
			destItem.setResponseStatus(ResponseStatus.ILLEGAL_ARGUMENT);
		} catch (TargetAlreadyExistsException e) {
			destItem.setResponseStatus(ResponseStatus.DUPLICATED);
		} catch (PermissionDeniedException e) {
			destItem.setResponseStatus(ResponseStatus.PERMISSION_DENIED);
		} catch (FileLockedException e) {
			destItem.setResponseStatus(ResponseStatus.LOCKED);
		} catch (TargetNotFoundException e) {
			throw new GenericResourceException(e);
		} catch (BadContentException e) {
			destItem.setResponseStatus(ResponseStatus.ILLEGAL_ARGUMENT);
		} catch (HasChildException e) {
			destItem.setResponseStatus(ResponseStatus.ILLEGAL_STATE);
		}

		return destItem;
	}

	/**
	 * リソースアイテム移動再帰処理.<br>
	 * 移動元がディレクトリの場合、その子アイテムも再帰処理により移動します
	 *
	 * @param srcItem 移動元のメタデータオブジェクト
	 * @param destItem 移動先のメタデータオブジェクト
	 * @param destDir 移動先ディレクトリ
	 * @param ctx urlTreeリソースのコンテキストオブジェクト
	 * @throws ParentNotFoundException
	 * @throws TargetTypeException
	 * @throws TargetAlreadyExistsException
	 * @throws PermissionDeniedException
	 * @throws TargetNotFoundException
	 * @throws FileLockedException
	 * @throws BadContentException
	 * @throws AbstractResourceException
	 * @throws HasChildException
	 */
	@SuppressWarnings("unchecked")
	//TODO:UrlTreeMetaData<T> と URLTreeDTO について整理が必要？
	private void recursiveMove(UrlTreeDTO srcItem, UrlTreeDTO destItem, String destDir, UrlTreeContext ctx)
			throws ParentNotFoundException, TargetAlreadyExistsException, PermissionDeniedException,
			TargetTypeException, BadContentException, AbstractResourceException, TargetNotFoundException,
			HasChildException, FileLockedException {

		String srcAbsolutePath = getAbsolutePath(srcItem);

		if (srcItem.isDirectory()) {

			String name = destItem.getName();

			urlTreeMetaDataManager.mkdir(destDir, name, ctx);
			contentsPersister.copy((UrlTreeMetaData<T>) srcItem, destDir, ctx);

			List<UrlTreeDTO> directoryList = getDirectoryList(null, srcAbsolutePath, ctx);

			String nextDestDir = destDir + UrlTreeMetaDataManager.PATH_SEPARATOR + name;

			for (UrlTreeDTO nextSrcItem : directoryList) {
				String nextDestPath = nextDestDir + UrlTreeMetaDataManager.PATH_SEPARATOR + nextSrcItem.getName();
				UrlTreeMetaData<T> nextDestItem = copyUrlTreeMetadata(nextSrcItem, nextDestPath, ctx);
				recursiveMove(nextSrcItem, nextDestItem, nextDestDir, ctx);
			}

			urlTreeMetaDataManager.rmdir(srcAbsolutePath, ctx);
			contentsPersister.delete((UrlTreeMetaData<T>) srcItem, ctx);

		} else {
			String destAbsolutePath = getAbsolutePath(destItem);
			urlTreeMetaDataManager.addEntry(destAbsolutePath, destItem, ctx);
			contentsPersister.copy((UrlTreeMetaData<T>) srcItem, destDir, ctx);
			urlTreeMetaDataManager.removeEntry(srcAbsolutePath, ctx);
			contentsPersister.delete((UrlTreeMetaData<T>) srcItem, ctx);
		}
	}

	/**
	 * メタデータを生成し、元となるメタデータから必要なプロパティを複写する.
	 *
	 * @param path 対象パス
	 * @param org 元となるメタデータ
	 * @param ctx コンテキストオブジェクト
	 * @return 生成したメタデータ
	 * @throws AbstractResourceException
	 */
	private UrlTreeMetaData<T> copyUrlTreeMetadata(UrlTreeDTO org, String path, UrlTreeContext ctx)
			throws AbstractResourceException {

		UrlTreeMetaData<T> item = new UrlTreeMetaData<T>();

		item.setUrlTreeContext(ctx);
		item.setFilename(path);
		item.setDirectory(org.isDirectory());
		item.setOwnerId(org.getOwnerId());
		item.setGroupId(org.getGroupId());
		item.setPermission(org.getPermission());

		return item;
	}

	//TODO:com.htmlhifive.resourcefw.file.UrlTreeMetaData.getAbsolutePath() を切り出し。他に方法はないか？
	/**
	 * メタデータの絶対パスを返します.<br/>
	 *
	 * @param urlTreeDTO
	 * @return 絶対パスを表す文字列
	 */
	private String getAbsolutePath(UrlTreeDTO urlTreeDTO) {
		String p = urlTreeDTO.getParent();
		StringBuilder returnStr = new StringBuilder();
		;
		if (StringUtils.isBlank(p) || p.equals(UrlTreeMetaDataManager.ROOT_NAME)) {
			returnStr.append(urlTreeDTO.getName());
		} else {
			returnStr.append(urlTreeDTO.getParent() + UrlTreeMetaDataManager.PATH_SEPARATOR + urlTreeDTO.getName());
		}

		if (returnStr.toString().startsWith(UrlTreeMetaDataManager.ROOT_NAME)) {
			returnStr.substring(UrlTreeMetaDataManager.ROOT_NAME.length());
		}

		return returnStr.toString();
	}

	/**
	 * @param strictMetaData セットする strictMetaData
	 */
	public void setStrictMetaData(boolean strictMetaData) {
		this.strictMetaData = strictMetaData;
	}

	/**
	 * @param urlTreeMetaDataManager the urlTreeMetaDataManager to set
	 */
	public void setUrlTreeSystem(UrlTreeMetaDataManager urlTreeSystem) {
		this.urlTreeMetaDataManager = urlTreeSystem;
	}
}
