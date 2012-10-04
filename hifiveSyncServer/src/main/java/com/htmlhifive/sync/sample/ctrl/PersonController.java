/*
 * Copyright (C) 2012 NS Solutions Corporation
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
package com.htmlhifive.sync.sample.ctrl;

import java.security.Principal;

import javax.annotation.Resource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.htmlhifive.sync.sample.person.Person;
import com.htmlhifive.sync.sample.person.PersonResource;

/**
 * Personデータに関するリクエストを処理するコントローラクラス.<br>
 * 以下の機能をクライアントに提供します.<br>
 * <ul>
 * <li>ログインユーザーのID取得(getUserId) ： /person
 * </ul>
 * JSONによってデータを送受信します.
 *
 * @author kishigam
 */
@Controller
public class PersonController {

	/**
	 * 人情報のリソース.
	 */
	@Resource
	private PersonResource personResource;

	/**
	 * ログインユーザーを取得、ユーザー名(ID)をレスポンスとして返します.<br>
	 *
	 * @param principal Spring Securityから渡されるプリンシパルオブジェクト
	 * @return ログインユーザーに対応するIDを含むPersonデータ(JSON形式)
	 */
	@RequestMapping(value = "/person", method = RequestMethod.GET, params = {}, headers = {
			"Content-Type=application/json", "Accept=application/json" })
	public ResponseEntity<Person> getUserId(Principal principal) {

		// Principalに設定されたログインユーザー情報を取得、レスポンスデータとしてリターン
		Person loginPerson = personResource.getResourceItemByPersonId(principal.getName());

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_JSON);

		return new ResponseEntity<>(loginPerson, responseHeaders, HttpStatus.OK);
	}
}