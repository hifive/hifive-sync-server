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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * urlTreeリソースの認証・認可情報に関するユーティリティ.
 *
 * @author kawaguch
 */
public final class AuthenticationUtil {

	private AuthenticationUtil() {
		// インスタンスの生成禁止.
	}

	/**
	 * Spring Securityが認証した結果のユーザ名を返します.
	 *
	 * @return SpringSecurityが認証した結果のユーザ名.認証してない、SpringSecurityが動作していない場合は空.
	 */
	public static String getUserName(Principal principal) {

		if (principal == null) {
			return "";
		}

		if (principal instanceof Authentication) {
			Object userDetails = ((Authentication) principal).getPrincipal();
			if (userDetails instanceof UserDetails) {
				return ((UserDetails) userDetails).getUsername();
			}
			return userDetails.toString();
		} else {
			return principal.toString();
		}
	}

	/**
	 * パーミッションを表す情報をフラグ値の配列に変換します.
	 *
	 * @param permissionStr パーミッション文字列(rwxr-x---などの形式)
	 * @return パーミッションを指定した配列。要素1が対象(Owner,Group,Other)、要素2が権限(rwx)
	 */
	public static boolean[][] convertPermissionStr(String permissionStr) {
		/*
		 * パーミッション文字列の仕様 - 空白/nullはもちろんNG - 9文字でなければNG - 小文字のrwx以外が含まれているとNG - 最初から順に「所有者」「所属グループ」「それ以外」 -
		 * rはread、wはwrite、xは実行（だがxは今のところ使わない）
		 */
		if (permissionStr == null || permissionStr.length() != 9) {
			throw new IllegalArgumentException("Illegal String");
		}

		boolean[][] result = new boolean[3][3];
		char[] ar = permissionStr.toCharArray();

		for (int i = 0; i < 9; i++) {
			result[i / 3][i % 3] = (ar[i] != '-');
		}

		return result;
	}

	/**
	 * パーミッションを表すフラグ値の配列の文字列表現を返します.
	 *
	 * @param permission パーミッション配列
	 * @return パーミッションの文字列表現
	 */
	public static String convertPermissionStr(boolean[][] permission) {
		try {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 3; i++) {
				AccessMode[] ama = AccessMode.values();
				for (AccessMode am : ama) {
					int j = am.ordinal();
					sb.append(permission[i][j] ? am.getFlag() : "-");
				}
			}
			return sb.toString();

		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException();
		}
	}
}
