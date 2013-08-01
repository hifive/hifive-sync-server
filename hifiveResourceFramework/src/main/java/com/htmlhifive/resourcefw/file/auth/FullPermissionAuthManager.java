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
import java.util.List;

import com.htmlhifive.resourcefw.file.metadata.UrlTreeNode;

/**
 * 認可制御を行わないurltreeリソースの認可マネージャ実装.
 */
public class FullPermissionAuthManager implements UrlTreeAuthorizationManager {

	@Override
	public void init() {
	}

	public List<String> getGroupByUser(String user) {
		return new ArrayList<String>();
	}

	public List<String> getUserByGroup(String group) {
		return new ArrayList<String>();
	}

	@Override
	public boolean checkPermission(UrlTreeNode utn, AccessMode accessMode, UrlTreeContext ctx) {
		return true;
	}

	@Override
	public UrlTreeContext getContext(Principal principal) {

		return new UrlTreeContext();
	}

	@Override
	public List<String> getUsers() {
		return new ArrayList<String>();
	}

	@Override
	public List<String> getGroups() {
		return new ArrayList<String>();
	}
}
