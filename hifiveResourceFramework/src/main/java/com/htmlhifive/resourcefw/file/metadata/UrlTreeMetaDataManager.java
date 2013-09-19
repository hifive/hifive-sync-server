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
package com.htmlhifive.resourcefw.file.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.htmlhifive.resourcefw.file.UrlTreeUtil;
import com.htmlhifive.resourcefw.file.auth.AccessMode;
import com.htmlhifive.resourcefw.file.auth.AuthenticationUtil;
import com.htmlhifive.resourcefw.file.auth.UrlTreeAuthorizationManager;
import com.htmlhifive.resourcefw.file.auth.UrlTreeContext;
import com.htmlhifive.resourcefw.file.exception.DeletedException;
import com.htmlhifive.resourcefw.file.exception.FileLockedException;
import com.htmlhifive.resourcefw.file.exception.HasChildException;
import com.htmlhifive.resourcefw.file.exception.ParentNotFoundException;
import com.htmlhifive.resourcefw.file.exception.PermissionDeniedException;
import com.htmlhifive.resourcefw.file.exception.TargetAlreadyExistsException;
import com.htmlhifive.resourcefw.file.exception.TargetNotFoundException;
import com.htmlhifive.resourcefw.file.exception.TargetTypeException;
import com.htmlhifive.resourcefw.file.exception.UpdateConflictException;

/**
 * urlTree構造を扱うサービスクラス.<br/>
 * urlTree構造とは、ディレクトリのような階層構造を持つデータセットであり、構造上のノードはURL等の形式と対応付けることができます.
 *
 * @author kawaguch
 */
@Service
public class UrlTreeMetaDataManager {

	private static final Logger logger = Logger.getLogger(UrlTreeMetaDataManager.class);

	/**
	 * 階層のセパレータ.
	 */
	public static final String PATH_SEPARATOR = "/";

	/**
	 * この系のルートノードのキーとなるルート名.
	 */
	public static final String ROOT_NAME = PATH_SEPARATOR;;

	/**
	 * ノード(およびそれが対応するファイルデータ)に対するロックの有効時間のデフォルト値
	 */
	private static final long DEFAULT_LOCK_TIMEOUT_MILLIS = 30 * 60 * 1000;

	/** エントリを扱うDAO(Spring Data JPA利用) */
	@Autowired
	private UrlTreeNodeRepository urlTreeNodeRepository;

	/** ユーザの権限を管理するマネージャ. */
	@Autowired
	private UrlTreeAuthorizationManager urlTreeAuthorizationManager;

	/** ディレクトリ再帰モード */
	private boolean dirRecursiveMode = true;

	/** ロックのタイムアウト時間 */
	private long lockTimeOutMillis = DEFAULT_LOCK_TIMEOUT_MILLIS;

	/**
	 * urlTree構造の初期化処理.<br/>
	 * ルートノードを生成し、永続化します.
	 */
	public void init() {
		UrlTreeNodePrimaryKey rootPk = UrlTreeUtil.generateUrlTreeNodePrimaryKey(ROOT_NAME);
		UrlTreeNode root = urlTreeNodeRepository.findOne(rootPk);

		if (root == null) {
			root = new UrlTreeDirectory();
			root.setParent(" ");
			root.setName(ROOT_NAME);
			root.setOwnerId("root");
			root.setGroupId("all");
			root.setPermission("rwxrwxrwx");
			root.setCreatedTime(System.currentTimeMillis());
			root.setUpdatedTime(System.currentTimeMillis());
			urlTreeNodeRepository.save(root);
			logger.debug("GENERATED ROOT NODE");
		}

		urlTreeAuthorizationManager.init();
	}

	/**
	 * ノードを取得します.<br/>
	 * ディレクトリかエントリのいずれかが取得されます.
	 *
	 * @param path 対象のパス
	 * @param ctx コンテキスト
	 * @return ノード
	 * @throws TargetNotFoundException 存在なかった場合
	 * @throws PermissionDeniedException 対象のユーザに読み取り権限がない場合
	 * @throws DeletedException 削除されていた場合
	 */
	public UrlTreeDTO get(String path, UrlTreeContext ctx) throws TargetNotFoundException, PermissionDeniedException,
			DeletedException {

		//UrlTreeNode node = urlTreeNodeRepository.findOne(utnpk);
		UrlTreeNode node = this.getNode(path, true);

		if (node == null) {
			throw new TargetNotFoundException();
		} else if (node.isDeleted()) {
			throw new DeletedException();
		}

		// パーミッションのチェック。
		// 権限がない場合はPermissionDeniedExceptionが飛ぶ
		checkPermissionThrowException(node, AccessMode.READ, ctx);

		// 返す
		return this.convertToDto(node);
	}

	/**
	 * 指定したノードの直下の子ノードを取得します.
	 *
	 * @param utnpk ノードの主キーオブジェクト
	 * @param ctx コンテキスト
	 * @return 子ノードのリスト
	 * @throws TargetNotFoundException 対象が見つからない場合
	 * @throws TargetTypeException 対象が子ノードが持てないエントリの場合
	 * @throws PermissionDeniedException 対象のユーザに読み取り権限がない場合
	 */
	public List<UrlTreeDTO> getChild(UrlTreeNodePrimaryKey utnpk, UrlTreeContext ctx, boolean returnDeletedNode)
			throws TargetNotFoundException, TargetTypeException, PermissionDeniedException {

		// ノードの検索
		//		UrlTreeNodePrimaryKey pathPk = UrlTreeUtil.generateUrlTreeNodePrimaryKey(path);
		UrlTreeNode node = urlTreeNodeRepository.findOne(utnpk);

		if (node == null) {
			throw new TargetNotFoundException();
		}

		// 子ノードがないEntryの場合
		if (node instanceof UrlTreeEntry) {
			throw new TargetTypeException();
		}

		// パーミッションのチェック。
		// 権限がない場合はPermissionDeniedExceptionが飛ぶ
		checkPermissionThrowException(node, AccessMode.READ, ctx);

		// DTOのコンバート
		List<UrlTreeDTO> result = new ArrayList<>();
		List<UrlTreeNode> lsParent = urlTreeNodeRepository.findByParent(utnpk.toString());
		for (UrlTreeNode n : lsParent) {
			if (n.isDeleted() && !returnDeletedNode) {
				continue;
			} else if (!urlTreeAuthorizationManager.checkPermission(n, AccessMode.READ, ctx)) {
				continue;
			}

			result.add(this.convertToDto(n));
		}

		return result;
	}

	@Deprecated
	public List<UrlTreeDTO> getAllChild(UrlTreeNodePrimaryKey utnpk, UrlTreeContext ctx)
			throws TargetNotFoundException, TargetTypeException, PermissionDeniedException {
		return this.getChild(utnpk, ctx, false);
	}

	/**
	 * 指定したノードの子ノードの一覧を取得します.
	 *
	 * @param utnpk ノードの主キー
	 * @param ctx urlTreeコンテキストオブジェクト
	 * @return 子ノードのキーリスト
	 * @throws TargetNotFoundException
	 * @throws TargetTypeException
	 * @throws PermissionDeniedException
	 */
	public List<String> ls(UrlTreeNodePrimaryKey utnpk, UrlTreeContext ctx) throws TargetNotFoundException,
			TargetTypeException, PermissionDeniedException {

		// リストを文字列で返す
		List<UrlTreeDTO> lsParent = this.getAllChild(utnpk, ctx);
		List<String> result = new ArrayList<>();
		for (UrlTreeDTO n : lsParent) {
			result.add(n.getName());
		}

		return result;
	}

	/**
	 * ディレクトリを作成します. dirRecursiveModeの値によって動作が変わります
	 *
	 * @param path 作りたい場所
	 * @param name 作りたいディレクトリの名前
	 * @param ctx UrlTreeコンテキストオブジェクト
	 * @throws ParentNotFoundException pathの指定が不正、親ディレクトリがない
	 * @throws TargetAlreadyExistsException 指定したディレクトリは既にある
	 * @throws PermissionDeniedException 権限がない
	 * @throws TargetTypeException pathで指定されたノードはディレクトリではないので、ディレクトリが作れない
	 */
	public void mkdir(String path, String name, UrlTreeContext ctx) throws ParentNotFoundException,
			TargetAlreadyExistsException, PermissionDeniedException, TargetTypeException {
		if (dirRecursiveMode) {
			this.mkdirRecursive(path + PATH_SEPARATOR + name, ctx);
		} else {
			this.mkdirNonRecursive(path, name, ctx);
		}
	}

	/**
	 * ディレクトリを作成します.
	 *
	 * @param path 作成したい場所（上位のディレクトリ）
	 * @param name ディレクトリの名前
	 * @param ctx コンテキスト
	 * @throws ParentNotFoundException 上位ディレクトリがない場合
	 * @throws TargetTypeException 親がディレクトリではない場合
	 * @throws TargetAlreadyExistsException 対象がすでに存在する場合(エントリ、ディレクトリ問わず)
	 * @throws PermissionDeniedException 権限がない場合
	 */
	public void mkdirNonRecursive(String path, String name, UrlTreeContext ctx) throws ParentNotFoundException,
			TargetAlreadyExistsException, PermissionDeniedException, TargetTypeException {

		String normPath = UrlTreeUtil.normalizePath(path);
		if (StringUtils.isBlank(name) || StringUtils.indexOf(name, PATH_SEPARATOR) >= 0) {
			throw new IllegalArgumentException("invalid name: " + name);
		}

		// 親の存在確認
		UrlTreeNode parentNode = this.getNode(normPath, false);

		// ディレクトリそのものの存在確認
		UrlTreeNode targetNode = this.getNode(UrlTreeUtil.getChildPath(normPath, name), true);

		// 例外処理
		if (parentNode == null) {
			throw new ParentNotFoundException();
		} else if (parentNode instanceof UrlTreeEntry) {
			throw new TargetTypeException();
		} else if (targetNode != null) {
			if (!targetNode.isDeleted()) {
				throw new TargetAlreadyExistsException();
			}
		}
		// パーミッションのチェック。
		// 権限がない場合はPermissionDeniedExceptionが飛ぶ
		checkPermissionThrowException(parentNode, AccessMode.WRITE, ctx);

		long timeStamp = this.getTimeStamp();
		// 作成処理
		UrlTreeDirectory newEntry = new UrlTreeDirectory(name, normPath);
		newEntry.setCreatedTime(timeStamp);
		newEntry.setUpdatedTime(timeStamp);
		newEntry.setOwnerId(ctx.getUserName());
		newEntry.setGroupId(ctx.getPrimaryGroup());
		newEntry.setPermission(ctx.getDefaultPermission());

		// 一度削除
		if (targetNode != null && targetNode.isDeleted() && !(targetNode instanceof UrlTreeDirectory)) {
			urlTreeNodeRepository.delete(targetNode);
		}

		urlTreeNodeRepository.save(newEntry);

		// 親ディレクトリのタイムスタンプ更新
		updateParentTimeStamp(newEntry, timeStamp);
	}

	/**
	 * ディレクトリを生成します.<br/>
	 * 途中の階層のディレクトリが存在しない場合は生成します.
	 *
	 * @param path
	 * @param ctx
	 * @throws ParentNotFoundException
	 * @throws PermissionDeniedException
	 * @throws TargetTypeException
	 */
	public void mkdirRecursive(String path, UrlTreeContext ctx) throws ParentNotFoundException,
			PermissionDeniedException, TargetTypeException {
		String normPath = UrlTreeUtil.normalizePath(path);
		String[] splittedPath = normPath.split(PATH_SEPARATOR);

		for (int i = 1; i < splittedPath.length; i++) {
			StringBuilder parent = new StringBuilder();
			for (int j = 0; j < i; j++) {
				parent.append(splittedPath[j]);
				parent.append(PATH_SEPARATOR);
			}

			if (parent.lastIndexOf(PATH_SEPARATOR) >= 0) {
				parent.deleteCharAt(parent.lastIndexOf(PATH_SEPARATOR));
			}

			if (parent.length() == 0) {
				parent.append(PATH_SEPARATOR);
			}

			try {
				// 親ディレクトリを悲観的ロックする
				getNodeForUpdate(parent.toString(), false);
				this.mkdirNonRecursive(parent.toString(), splittedPath[i], ctx);
			} catch (TargetAlreadyExistsException e) {
				//ある分には構わないので無視
			}
			// それ以外の例外はそのままスロー
		}
	}

	/**
	 * ディレクトリを削除します. 中身が空でないと削除できません.
	 *
	 * @param path 削除したいディレクトリ
	 * @param ctx コンテキスト
	 * @throws TargetNotFoundException 対象がない場合
	 * @throws HasChildException 対象に中身がある場合
	 * @throws TargetTypeException 対象がディレクトリじゃない場合
	 * @throws PermissionDeniedException 権限がない場合
	 * @throws UpdateConflictException
	 */
	public void rmdirWithVersionCheck(String path, UrlTreeContext ctx, long targetVersion)
			throws TargetNotFoundException, HasChildException, TargetTypeException, PermissionDeniedException, UpdateConflictException {

		// ディレクトリそのものの存在確認
		UrlTreeNode targetNode = this.getNode(path, false);

		// 子供の存在確認
		String normPath = UrlTreeUtil.normalizePath(path);
		List<UrlTreeNode> childs = urlTreeNodeRepository.findByParentAndDeletedFalse(normPath);
		// 例外処理
		if (targetNode == null) {
			throw new TargetNotFoundException();
		} else if (targetNode instanceof UrlTreeEntry) {
			throw new TargetTypeException();
		} else if (!dirRecursiveMode && !childs.isEmpty()) {
			throw new HasChildException();
		} else if (targetNode.getName().equals(ROOT_NAME)) {
			throw new PermissionDeniedException("Do not delete root node");
		}
		// パーミッションのチェック。
		// 権限がない場合はPermissionDeniedExceptionが飛ぶ
		checkPermissionThrowException(targetNode, AccessMode.WRITE, ctx);

		// 更新時刻チェック
		// 更新対象のバージョンが現在のバージョンと違えばConflictとなる
		checkUpdatedTime(targetNode, targetVersion);

		// 更新時刻の確定
		long timeStamp = this.getTimeStamp();

		// 子供の論理削除
		urlTreeNodeRepository.logicalDeleteByParent(normPath, timeStamp);

		// 削除処理
		urlTreeNodeRepository.logicalDeleteByPK(targetNode.getName(), targetNode.getParent(), timeStamp);
		updateParentTimeStamp(targetNode, timeStamp);

	}

	public void rmdir(String path, UrlTreeContext ctx) throws TargetNotFoundException, HasChildException,
			TargetTypeException, PermissionDeniedException {
		try {
			this.rmdirWithVersionCheck(path, ctx, System.currentTimeMillis());
		} catch (UpdateConflictException e) {
			// TODO この場合どうする？（実質はバグになるはず）
			throw new PermissionDeniedException();
		}
	}

	/**
	 * 指定した配置場所に、指定したエントリを追加します. DTOにパス情報がありますが無視されます。 ownerId、groupIdは、DTOに指定したものをセットします.
	 * 指定がない場合(空白またはnullの場合)、contextのユーザ名をセットします.
	 *
	 * @param path 配置場所
	 * @param value 置きたいオブジェクト. ただし、value, ownerId, groupId以外の情報は無視されます.
	 * @param ctx コンテキスト
	 * @throws ParentNotFoundException 置きたい場所の親ディレクトリがない場合
	 * @throws TargetTypeException 置きたい場所の親に指定したものがエントリの場合
	 * @throws TargetAlreadyExistsException 置きたい場所にすでにエントリ/ディレクトリがある場合
	 * @throws PermissionDeniedException
	 */
	public void addEntry(String path, UrlTreeDTO value, UrlTreeContext ctx) throws ParentNotFoundException,
			TargetTypeException, TargetAlreadyExistsException, PermissionDeniedException {
		// 親のパス生成
		UrlTreeNodePrimaryKey tempPathPk = UrlTreeUtil.generateUrlTreeNodePrimaryKey(path);
		String parentPath = tempPathPk.getParent();

		// 親の存在確認、悲観的ロックによる排他制御
		UrlTreeNode parentNode = this.getNodeForUpdate(parentPath, false);
		// 作成対象の存在確認
		UrlTreeNode targetNode = this.getNode(path, true);

		if (dirRecursiveMode && parentNode == null) {
			this.mkdirRecursive(parentPath, ctx);
			parentNode = this.getNode(parentPath, false);
		}

		// 例外処理
		if (parentNode == null) {
			throw new ParentNotFoundException();
		} else if (parentNode instanceof UrlTreeEntry) {
			throw new TargetTypeException();
		} else if (targetNode != null) {
			// 削除済みの場合、あとで消してまた入れなおすのでいったん通す
			if (!targetNode.isDeleted()) {
				throw new TargetAlreadyExistsException();
			}
		}

		// パーミッションのチェック。
		// 権限がない場合はPermissionDeniedExceptionが飛ぶ
		checkPermissionThrowException(parentNode, AccessMode.WRITE, ctx);

		// 更新時刻の確定
		long timeStamp = this.getTimeStamp();

		UrlTreeEntry newEntry = new UrlTreeEntry();
		newEntry.setName(tempPathPk.getName());
		newEntry.setParent(parentPath);
		newEntry.setValue(value.getValue());
		newEntry.setCreatedTime(timeStamp);
		newEntry.setUpdatedTime(timeStamp);
		newEntry.setGroupId(value.getGroupId());
		newEntry.setOwnerId(value.getOwnerId());
		newEntry.setPermission(value.getPermission());

		// DTOに指定されていない要素はコンテキストから設定
		if (StringUtils.isBlank(newEntry.getOwnerId())) {
			newEntry.setOwnerId(ctx.getUserName());
		}

		if (StringUtils.isBlank(newEntry.getGroupId())) {
			newEntry.setGroupId(ctx.getPrimaryGroup());
		}

		if (StringUtils.isBlank(newEntry.getPermission())) {
			newEntry.setPermission(ctx.getDefaultPermission());
		}

		// 一応チェック　おかしい場合はIllegalArgumentException が飛ぶ
		AuthenticationUtil.convertPermissionStr(newEntry.getPermission());

		// 前に同名のディレクトリが作られていると困るので一度削除
		if (targetNode != null && targetNode.isDeleted() && !(targetNode instanceof UrlTreeEntry)) {
			urlTreeNodeRepository.delete(targetNode);
		}

		urlTreeNodeRepository.save(newEntry);
		updateParentTimeStamp(newEntry, timeStamp);
	}

	/**
	 * エントリを削除します.
	 *
	 * @param path 対象のエントリ
	 * @param ctx コンテキスト
	 * @throws TargetNotFoundException 対象がない場合
	 * @throws TargetTypeException 対象がディレクトリの場合
	 * @throws PermissionDeniedException 権限がない場合
	 * @throws FileLockedException
	 * @throws UpdateConflictException
	 */
	public void removeEntryWithVersionCheck(String path, UrlTreeContext ctx, long targetVersion)
			throws TargetNotFoundException, TargetTypeException, PermissionDeniedException, FileLockedException,
			UpdateConflictException {

		// 消去対象の存在確認
		UrlTreeNodePrimaryKey pathPk = UrlTreeUtil.generateUrlTreeNodePrimaryKey(path);
		//		UrlTreeNode targetNode = urlTreeNodeRepository.findOne(pathPk);
		UrlTreeNode targetNode = this.getNodeWithLockCheck(path, ctx);

		// 例外処理
		if (targetNode == null) {
			throw new TargetNotFoundException();
		} else if (targetNode instanceof UrlTreeDirectory) {
			throw new TargetTypeException();
		}

		// パーミッションのチェック。
		// 権限がない場合はPermissionDeniedExceptionが飛ぶ
		checkPermissionThrowException(targetNode, AccessMode.WRITE, ctx);

		// 更新時刻チェック
		// 更新対象のバージョンが現在のバージョンと違えばConflictとなる
		checkUpdatedTime(targetNode, targetVersion);

		// 更新時刻の取得
		long timeStamp = this.getTimeStamp();

		// 削除実施
		urlTreeNodeRepository.logicalDeleteByPK(pathPk.getName(), pathPk.getParent(), timeStamp);

		// 親ディレクトリのタイムスタンプ更新
		updateParentTimeStamp(targetNode, timeStamp);

	}

	public void removeEntry(String path, UrlTreeContext ctx) throws TargetNotFoundException, TargetTypeException,
			PermissionDeniedException, FileLockedException {
		try {
			this.removeEntryWithVersionCheck(path, ctx, System.currentTimeMillis());
		} catch (UpdateConflictException e) {
			// TODO この場合どうする？（実質はバグになるはず）
			throw new PermissionDeniedException();
		}
	}

	/**
	 * エントリを(明示的に)更新します.
	 *
	 * @param path パス
	 * @param value 更新後の値
	 * @param ctx コンテキスト
	 * @throws TargetNotFoundException 対象がない場合
	 * @throws TargetTypeException 対象がディレクトリの場合
	 * @throws PermissionDeniedException 権限がない場合
	 * @throws FileLockedException ロック中の場合
	 * @throws UpdateConflictException
	 */
	public void updateEntryWithVersionCheck(String path, UrlTreeDTO value, UrlTreeContext ctx, long targetVersion)
			throws TargetNotFoundException, TargetTypeException, PermissionDeniedException, FileLockedException,
			UpdateConflictException {

		// 更新対象の存在確認
		UrlTreeNode targetNode = this.getNodeWithLockCheck(path, ctx);

		// 例外処理
		if (targetNode == null) {
			throw new TargetNotFoundException();
		} else {
			boolean targetIsDirectory = targetNode instanceof UrlTreeDirectory;
			if (targetIsDirectory != value.isDirectory()) {
				throw new TargetTypeException();
			}
		}

		// パーミッションのチェック。
		// 権限がない場合はPermissionDeniedExceptionが飛ぶ
		checkPermissionThrowException(targetNode, AccessMode.WRITE, ctx);

		// 更新時刻チェック
		// 更新対象のバージョンが現在のバージョンと違えばConflictとなる
		checkUpdatedTime(targetNode, targetVersion);

		// 更新時刻の取得
		long timeStamp = this.getTimeStamp();
		targetNode.setUpdatedTime(timeStamp);

		String ownerId = value.getOwnerId();
		targetNode.setOwnerId(StringUtils.isNotBlank(ownerId) ? ownerId : targetNode.getOwnerId());
		String groupId = value.getGroupId();
		targetNode.setGroupId(StringUtils.isNotBlank(groupId) ? groupId : targetNode.getGroupId());
		String perm = value.getPermission();
		targetNode.setPermission(StringUtils.isNotBlank(perm) ? perm : targetNode.getPermission());

		if (targetNode instanceof UrlTreeEntry) {
			UrlTreeEntry targetEntry = (UrlTreeEntry) targetNode;
			targetEntry.setValue(value.getValue());
		}

		urlTreeNodeRepository.save(targetNode);

		// 親ディレクトリのタイムスタンプ更新
		updateParentTimeStamp(targetNode, timeStamp);

	}

	public void updateEntry(String path, UrlTreeDTO value, UrlTreeContext ctx) throws TargetNotFoundException,
			TargetTypeException, PermissionDeniedException, FileLockedException {
		try {
			this.updateEntryWithVersionCheck(path, value, ctx, System.currentTimeMillis());
		} catch (UpdateConflictException e) {
			// TODO この場合どうする？（実質はバグになるはず）
			throw new PermissionDeniedException();
		}
	}

	/**
	 * 対象のファイルのロックを取得します. 現在のバージョンではディレクトリに対するロックは取得できません. なおコンテキスト内にロックトークンがすでにある場合、 再利用されます.
	 * また、自分がロックを持っているファイルに対してロックを取得すると 有効期限の更新になります.
	 *
	 * @param path 取得したいファイルのパス
	 * @param ctx ユーザコンテキスト
	 * @return ロックトークン
	 * @throws TargetNotFoundException 対象がない場合
	 * @throws PermissionDeniedException 権限がない場合
	 * @throws FileLockedException すでに(他人が)ロック中の場合
	 * @throws TargetTypeException 対象がディレクトリの場合
	 */
	public synchronized String lock(String path, UrlTreeContext ctx) throws TargetNotFoundException,
			PermissionDeniedException, FileLockedException, TargetTypeException {

		UrlTreeNode targetNode = this.getNodeWithLockCheck(path, ctx);
		if (targetNode instanceof UrlTreeDirectory) {
			throw new TargetTypeException();
		}

		checkPermissionThrowException(targetNode, AccessMode.WRITE, ctx);

		String myLockToken = ctx.getLockToken();
		if (StringUtils.isBlank(myLockToken)) {
			myLockToken = UUID.randomUUID().toString();
		}

		// 取得処理
		long lockExpiredTime = System.currentTimeMillis() + lockTimeOutMillis;
		targetNode.setLockExpiredTime(lockExpiredTime);
		targetNode.setLockToken(myLockToken);

		// 更新がうまくいかないケースがあるので一度消してセーブ
		//urlTreeNodeRepository.deleteByPK(targetNode.getKey(), targetNode.getParent());
		urlTreeNodeRepository.save(targetNode);

		ctx.setLockToken(myLockToken);
		return myLockToken;
	}

	/**
	 * 対象のファイルのロックを外します.
	 *
	 * @param path
	 * @param ctx
	 * @throws TargetNotFoundException
	 * @throws FileLockedException　すでに(他人が)ロック中の場合
	 */
	public synchronized void unlock(String path, UrlTreeContext ctx) throws TargetNotFoundException,
			FileLockedException {

		UrlTreeNode targetNode = this.getNodeWithLockCheck(path, ctx);

		// 解除処理
		targetNode.setLockExpiredTime(0);
		targetNode.setLockToken(" ");

		// 更新がうまくいかないケースがあるので一度消してセーブ
		//urlTreeNodeRepository.deleteByPK(targetNode.getKey(), targetNode.getParent());
		urlTreeNodeRepository.save(targetNode);
	}

	/**
	 * 権限のチェック. 権限があればそのまま戻り、 なければPermissionDeniedExceptionを投げます.
	 *
	 * @param node 対象のノード
	 * @param accessMode 対象の操作モード
	 * @param ctx ユーザコンテキスト
	 * @throws PermissionDeniedException 権限がない場合
	 */
	private void checkPermissionThrowException(UrlTreeNode node, AccessMode accessMode, UrlTreeContext ctx)
			throws PermissionDeniedException {
		boolean b = urlTreeAuthorizationManager.checkPermission(node, accessMode, ctx);
		if (!b) {
			throw new PermissionDeniedException();
		}
	}

	/**
	 * 権限のチェック.
	 *
	 * @param dto
	 * @param accessMode
	 * @param ctx
	 * @return アクセス可否
	 */
	public boolean checkPermission(UrlTreeDTO dto, AccessMode accessMode, UrlTreeContext ctx) {
		UrlTreeNode node;
		if (dto.isDirectory()) {
			node = new UrlTreeDirectory();
		} else {
			node = new UrlTreeEntry();
		}

		node.setOwnerId(dto.getOwnerId());
		node.setGroupId(dto.getGroupId());
		node.setPermission(dto.getPermission());
		node.setParent(dto.getParent());
		node.setName(dto.getName());

		return urlTreeAuthorizationManager.checkPermission(node, accessMode, ctx);
	}

	private void checkUpdatedTime(UrlTreeNode targetNode, long requestVersion) throws UpdateConflictException {
		if (targetNode.getUpdatedTime() > requestVersion) {
			throw new UpdateConflictException();
		}
	}

	/**
	 * リポジトリから、指定したパスのノードを取得します
	 *
	 * @param path パス
	 * @param returnDeletedNode 削除済みのノードを返すか否か。削除済みのノードも必要な場合はtrue
	 * @return 対象となるノード. 存在しなければnull. returnDeletedNode=falseの場合、論理削除済みの場合もnull
	 */
	private UrlTreeNode getNode(String path, boolean returnDeletedNode) {
		UrlTreeNodePrimaryKey pathPk = UrlTreeUtil.generateUrlTreeNodePrimaryKey(path);
		UrlTreeNode node = urlTreeNodeRepository.findOne(pathPk);

		if (!returnDeletedNode) {
			if (node != null && node.isDeleted()) {
				node = null;
			}
		}

		return node;
	}

	/**
	 * リポジトリから、指定したパスのノードを悲観的ロックを使用して取得します
	 *
	 * @param path パス
	 * @param returnDeletedNode 削除済みのノードを返すか否か。削除済みのノードも必要な場合はtrue
	 * @return 対象となるノード. 存在しなければnull. returnDeletedNode=falseの場合、論理削除済みの場合もnull
	 */
	private UrlTreeNode getNodeForUpdate(String path, boolean returnDeletedNode) {
		UrlTreeNodePrimaryKey pathPk = UrlTreeUtil.generateUrlTreeNodePrimaryKey(path);
		UrlTreeNode node = urlTreeNodeRepository.findOneForUpdate(pathPk.getName(), pathPk.getParent());

		if (!returnDeletedNode) {
			if (node != null && node.isDeleted()) {
				node = null;
			}
		}

		return node;
	}

	/**
	 * ロックの有無をチェックして、対象のノードを返します. ロックされていた場合はLockedExceptionを返します
	 *
	 * @param path パス
	 * @param ctx UrlTreeコンテキストオブジェクト
	 * @return 対象のノード
	 * @throws TargetNotFoundException 対象がない場合
	 * @throws FileLockedException (他人が)ロック中の場合
	 */
	public synchronized UrlTreeNode getNodeWithLockCheck(String path, UrlTreeContext ctx)
			throws TargetNotFoundException, FileLockedException {

		// ノードの取得
		UrlTreeNode targetNode = this.getNode(path, false);
		if (targetNode == null) {
			throw new TargetNotFoundException();
		}

		// ロックチェックに必要な情報取得
		String currentLockToken = targetNode.getLockToken();
		long currentLockExpiredTime = targetNode.getLockExpiredTime();
		String myLockToken = ctx.getLockToken();

		// チェック
		boolean locked = true;
		if (StringUtils.isBlank(currentLockToken)) {
			// トークンが空またはnull＝ロックかかってない
			locked = false;
		} else if (currentLockExpiredTime < System.currentTimeMillis()) {
			// トークンあるけど、有効期限切れ
			locked = false;
		} else if (currentLockToken.equals(myLockToken)) {
			// ロック持ってるのが自分
			locked = false;
		}

		if (locked) {
			throw new FileLockedException();
		}

		// ノードを返す
		return targetNode;

	}

	/**
	 * 対象以下のノードをすべて取得します.
	 *
	 * @param target 対象ノード
	 * @return 配下ノードのリスト
	 */
	public List<UrlTreeNode> findAllChild(String target) {
		if (StringUtils.isBlank(target)) {
			return new ArrayList<>();
		}

		String normPath = UrlTreeUtil.normalizePath(target);

		if (normPath.equals(ROOT_NAME)) {
			return urlTreeNodeRepository.getAllChildrenByParent("%");
		}

		return urlTreeNodeRepository.getAllChildrenByParent(normPath + "%");
	}

	/**
	 * あるパスが指定された際に、その親ディレクトリを返します.<br/>
	 * 親ディレクトリがない場合はその親、その親もない場合はさらにその親...という風にディレクトリが存在するところまで探索します。
	 *
	 * @param searchPath パス文字列
	 * @return 親ディレクトリのUrlTreeDTOオブジェクト
	 * @throws DeletedException
	 * @throws PermissionDeniedException
	 */
	public UrlTreeDTO searchParentPath(String searchPath, UrlTreeContext ctx) throws PermissionDeniedException {
		String normPath = UrlTreeUtil.normalizePath(searchPath);
		String[] splittedPath = normPath.split(PATH_SEPARATOR);

		UrlTreeDTO currentParent = null;
		for (int i = 1; i < splittedPath.length; i++) {
			StringBuilder parent = new StringBuilder();
			for (int j = 0; j < i; j++) {
				parent.append(splittedPath[j]);
				parent.append(PATH_SEPARATOR);
			}

			if (parent.lastIndexOf(PATH_SEPARATOR) >= 0) {
				parent.deleteCharAt(parent.lastIndexOf(PATH_SEPARATOR));
			}

			if (parent.length() == 0) {
				parent.append(PATH_SEPARATOR);
			}

			try {
				currentParent = this.get(parent.toString(), ctx);
			} catch (TargetNotFoundException e) {
				return currentParent;
			} catch (DeletedException e) {
				return currentParent;
			}
		}

		return currentParent;
	}

	/**
	 * 指定されたノードのタイムスタンプを更新します.<br/>
	 * そのノードの親のタイムスタンプはすべて更新されます. そのノード「自身」は更新されませんので注意してください.
	 *
	 * @param node 更新するノード
	 * @param timeStamp 更新したいタイムスタンプ
	 */
	private void updateParentTimeStamp(UrlTreeNode node, long timeStamp) {
		List<UrlTreeNode> parentList = this.findAllParent(node);

		for (UrlTreeNode n : parentList) {
			n.setUpdatedTime(timeStamp);
		}

		// ROOTノードの更新
		UrlTreeNode n = this.getNode(ROOT_NAME, false);
		n.setUpdatedTime(timeStamp);
		parentList.add(n);

		urlTreeNodeRepository.save(parentList);
	}

	/**
	 * 指定されたノードの上位ノードをすべて取得します. 指定されたノード自身はリストに含まれず、 リストの最後は必ずルートノードになります.
	 *
	 * @param node ノード
	 * @return 上位ノードのリスト
	 */
	public List<UrlTreeNode> findAllParent(UrlTreeNode node) {
		List<UrlTreeNode> list = new ArrayList<UrlTreeNode>();
		return this.findAllParentRecursive(node, list);
	}

	/**
	 * 指定されたノードの上位ノードを再帰的に取得します.<br/>
	 * 取得されたノードはlistに追加されます.
	 *
	 * @param node ノード
	 * @param list 上位ノードが追加されるリスト
	 * @return 追加後のリスト
	 */
	private List<UrlTreeNode> findAllParentRecursive(UrlTreeNode node, List<UrlTreeNode> list) {

		// rootなら親はないのでなにもしない
		if (node == null || node.getName().equals(ROOT_NAME)) {
			return list;
		}

		UrlTreeNode parentNode = this.getNode(node.getParent(), true);
		if (parentNode != null) {
			list.add(parentNode);
		}
		this.findAllParentRecursive(parentNode, list);
		return list;
	}

	/**
	 * ノード情報を保持するDTOを生成し、返します.
	 *
	 * @param node ノード
	 * @return UrlTreeDTOオブジェクト
	 */
	private UrlTreeDTO convertToDto(UrlTreeNode node) {
		UrlTreeDTO result = UrlTreeUtil.convertToDto(node);

		// ロック開始時間対応
		long lockExpiredTime = node.getLockExpiredTime();
		if (lockExpiredTime > 0) {
			result.setLockStartTime(lockExpiredTime - lockTimeOutMillis);
		}

		return result;
	}

	/**
	 * この系で扱える全ユーザーのIDリストを返します.
	 *
	 * @return ユーザーIDのリスト
	 */
	public List<String> getUserList() {
		return urlTreeAuthorizationManager.getUsers();
	}

	/**
	 * この系扱える全グループのIDリストを返します.
	 *
	 * @return グループIDのリスト
	 */
	public List<String> getGroupList() {
		return urlTreeAuthorizationManager.getGroups();
	}

	/**
	 * @return lockTimeOutMillis
	 */
	public long getLockTimeOutMillis() {
		return lockTimeOutMillis;
	}

	/**
	 * @param lockTimeOutMillis セットする lockTimeOutMillis
	 */
	public void setLockTimeOutMillis(long lockTimeOutMillis) {
		this.lockTimeOutMillis = lockTimeOutMillis;
	}

	/**
	 * 更新時刻のセット。あとでなんかいじれるようにメソッド化
	 *
	 * @return 更新時刻
	 */
	private long getTimeStamp() {
		return System.currentTimeMillis();
	}

}
