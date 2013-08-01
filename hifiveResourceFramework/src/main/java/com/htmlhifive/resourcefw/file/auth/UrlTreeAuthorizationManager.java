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
import java.util.List;

import org.springframework.stereotype.Service;

import com.htmlhifive.resourcefw.file.metadata.UrlTreeNode;

/**
 * 権限管理を行うurlTreeリソースの認可マネージャインターフェース.
 *
 * @author kawaguch
 */
@Service
public interface UrlTreeAuthorizationManager {

	/**
	 * 指定された情報をもとに、{@link UrlTreeNode UrlTreeNode}へのアクセス権限有無を返します.
	 *
	 * @param utn 対象urlTreeノード
	 * @param accessMode 権限有無を確認する対象のアクセスモード
	 * @param ctx urlTreeコンテキストオブジェクト
	 * @return 権限があるときtrue
	 */
	public boolean checkPermission(UrlTreeNode utn, AccessMode accessMode, UrlTreeContext ctx);

	/**
	 * urlTreeコンテキストオブジェクトを生成して返します.<br/>
	 * このコンテキストは認可マネージャを使用する時に必要です.
	 *
	 * @param principal ユーザープリンシパルオブジェクト
	 * @return コンテキストオブジェクト
	 */
	public UrlTreeContext getContext(Principal principal);

	/**
	 * 認可マネージャの初期化処理を実行します.
	 */
	public void init();

	/**
	 * このマネージャが管理する全ユーザーのIDリストを返します.
	 *
	 * @return ユーザーIDのリスト
	 */
	public List<String> getUsers();

	/**
	 * このマネージャが管理する全グループのIDリストを返します.
	 *
	 * @return グループIDのリスト
	 */
	public List<String> getGroups();
}