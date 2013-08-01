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
package com.htmlhifive.resourcefw.sample.ctrl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * resource frameworkの管理対象外Controller(Handler)サンプル.<br/>
 * SpringMVCによるRequestMappinのデフォルト挙動を使用できます.
 *
 * @author kishigam
 */
@Controller
@RequestMapping(value = "/other")
public class OtherController {

	@Autowired
	ApplicationContext context;

	@RequestMapping("other1")
	public ResponseEntity<Map<String, String>> other1(
			@RequestParam(value = "q", required = false, defaultValue = "defaultQ") final String q) throws Exception {

		@SuppressWarnings("serial")
		HashMap<String, String> body = new HashMap<String, String>() {
			{
				put("q", q);
			}
		};

		return new ResponseEntity<Map<String, String>>(body, HttpStatus.OK);
	}
}
