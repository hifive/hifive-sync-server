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
package com.htmlhifive.resourcefw.ctrl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.junit.Test;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;

import com.htmlhifive.resourcefw.message.RequestMessageContainer;

public class ResourceHandlerMappingTest {

	@Mocked
	private HttpServletRequest request;

	private static ResourceController resourceController = new ResourceController();

	private static ResourceHandlerMapping target = new ResourceHandlerMapping("service-root", ResourceHandler.class,
			resourceController);

	@Test
	public void testLookupHandlerMethod() throws Exception {

		final String lookupPath = "/service-root/a/b/c";

		Method expectedMethod = resourceController.getClass().getMethod("handle", WebRequest.class,
				RequestMessageContainer.class);

		HandlerMethod expected = new HandlerMethod(resourceController, expectedMethod);

		new NonStrictExpectations() {
			{
				// requestに対する何らかの操作
			}
		};

		HandlerMethod actual = target.lookupHandlerMethod(lookupPath, request);

		assertThat(actual, is(equalTo(expected)));
	}
}
