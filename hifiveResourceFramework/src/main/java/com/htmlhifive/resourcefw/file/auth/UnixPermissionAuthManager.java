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
package com.htmlhifive.resourcefw.file.auth;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import com.htmlhifive.resourcefw.file.metadata.UrlTreeNode;
import com.htmlhifive.resourcefw.file.metadata.UrlTreeUsers;
import com.htmlhifive.resourcefw.file.metadata.UrlTreeUsersPrimaryKey;
import com.htmlhifive.resourcefw.file.metadata.UrlTreeUsersRepository;

/**
 * UNIXライクな権限管理を行うurlTreeリソースの認可マネージャ実装.
 *
 * @author kawaguch
 */
public class UnixPermissionAuthManager implements UrlTreeAuthorizationManager {

	// ====== 定数 ==========
	//  ユーザ系
	private static final int OWNER = 0;

	private static final int GROUP = 1;

	private static final int OTHER = 2;

	private static final String SPECIAL_USER_NAME = "root";

	/**
	 * 認可情報を永続化するリポジトリ.
	 */
	@Resource
	private UrlTreeUsersRepository urlTreeUsersRepository;

	/**
	 * ルートユーザを追加し、この認可マネージャを初期化します.
	 */
	@Override
	public void init() {
		this.addUserPair("root", "root");
	}

	/**
	 * 指定されたユーザーID、グループIDを持つユーザーデータを永続化します.
	 *
	 * @param userId　ユーザID
	 * @param groupId グループID
	 */
	public void addUserPair(String userId, String groupId) {
		UrlTreeUsers utu = new UrlTreeUsers();
		UrlTreeUsersPrimaryKey utup = new UrlTreeUsersPrimaryKey();
		utup.setUserId(userId);
		utup.setGroupId(groupId);
		utu.setPrimaryKey(utup);
		urlTreeUsersRepository.save(utu);
	}

	/**
	 * 指定されたユーザIDの所属グループIDのリストを返します.
	 *
	 * @param user ユーザーID
	 * @return　グループIDのリスト
	 */
	public List<String> getGroupByUser(String user) {
		List<UrlTreeUsers> repoResult = urlTreeUsersRepository.findByPrimaryKeyUserId(user);
		List<String> result = new ArrayList<>();

		for (UrlTreeUsers u : repoResult) {
			result.add(u.getPrimaryKey().getGroupId());
		}

		return result;
	}

	/**
	 * 指定されたグループに所属するユーザIDのリストを返します.
	 *
	 * @param group グループ
	 * @return 所属ユーザIDのリスト
	 */
	public List<String> getUserByGroup(String group) {
		List<UrlTreeUsers> repoResult = urlTreeUsersRepository.findByPrimaryKeyGroupId(group);
		List<String> result = new ArrayList<>();

		for (UrlTreeUsers u : repoResult) {
			result.add(u.getPrimaryKey().getUserId());
		}

		return result;
	}

	/**
	 * @see UrlTreeAuthorizationManager#checkPermission(UrlTreeNode, AccessMode, UrlTreeContext)
	 */
	@Override
	public boolean checkPermission(UrlTreeNode utn, AccessMode accessMode, UrlTreeContext ctx) {

		String nodeOwner = utn.getOwnerId();
		String nodeGroup = utn.getGroupId();
		List<String> userGroups = ctx.getGroups();

		// 特権ユーザ対応
		if (ctx.getUserName().equals(SPECIAL_USER_NAME)) {
			return true;
		}

		boolean isOwner = ctx.getUserName().equals(nodeOwner);
		boolean isInGroup = userGroups.contains(nodeGroup);

		// パーミッション文字列の解析
		boolean[][] permission = AuthenticationUtil.convertPermissionStr(utn.getPermission());
		int ai = accessMode.ordinal();

		if (isOwner) {
			return permission[OWNER][ai];
		}

		if (isInGroup) {
			return permission[GROUP][ai];
		}

		return permission[OTHER][ai];
	}

	/**
	 * @see UrlTreeAuthorizationManager#getContext(Principal)
	 */
	@Override
	public UrlTreeContext getContext(Principal principal) {

		UrlTreeContext ctx = new UrlTreeContext();
		String userName = AuthenticationUtil.getUserName(principal);
		ctx.setUserName(userName);

		List<UrlTreeUsers> user = urlTreeUsersRepository.findByPrimaryKeyUserId(userName);

		// ユーザが見つからなければ作成
		if(user == null || user.size() < 1) {
			UrlTreeUsers utu = new UrlTreeUsers();
			UrlTreeUsersPrimaryKey utupk = new UrlTreeUsersPrimaryKey();
			utupk.setUserId(userName);
			utupk.setGroupId("nogroup");
			utu.setPrimaryKey(utupk);
			utu.setPrimaryGroup(true);

			urlTreeUsersRepository.save(utu);
		}

		List<String> groupList = this.getGroupByUser(userName);
		ctx.setGroups(groupList);
		List<UrlTreeUsers> primaryList = urlTreeUsersRepository.findByPrimaryKeyUserIdAndPrimaryGroupIsTrue(userName);
		if (!primaryList.isEmpty()) {
			ctx.setPrimaryGroup(primaryList.get(0).getPrimaryKey().getGroupId());
		}

		return ctx;
	}

	/**
	 * リポジトリで管理されている全ユーザーのIDリストを返します.
	 *
	 * @return 全ユーザーのリスト
	 */
	@Override
	public List<String> getUsers() {
		List<UrlTreeUsers> allList = urlTreeUsersRepository.findAll();

		Set<String> set = new HashSet<>();
		for (UrlTreeUsers utu : allList) {
			set.add(utu.getPrimaryKey().getUserId());
		}

		List<String> result = new ArrayList<>();

		result.addAll(set);
		return result;
	}

	/**
	 * リポジトリで管理されている全グループのIDリストを返します.
	 */
	@Override
	public List<String> getGroups() {
		List<UrlTreeUsers> allList = urlTreeUsersRepository.findAll();

		Set<String> set = new HashSet<>();
		for (UrlTreeUsers utu : allList) {
			set.add(utu.getPrimaryKey().getGroupId());
		}

		List<String> result = new ArrayList<>();

		result.addAll(set);
		return result;
	}
}
