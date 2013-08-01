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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.net.URLEncoder;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import mockit.Expectations;
import mockit.Mocked;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.WebRequest;

import com.htmlhifive.resourcefw.config.MessageMetadata;
import com.htmlhifive.resourcefw.config.ResourceConfigurationParameter;
import com.htmlhifive.resourcefw.message.MessageSource;
import com.htmlhifive.resourcefw.message.RequestMessage;
import com.htmlhifive.resourcefw.message.RequestMessageContainer;
import com.htmlhifive.resourcefw.message.RequestMessageContext;
import com.htmlhifive.resourcefw.message.ResponseMessage;
import com.htmlhifive.resourcefw.message.ResponseMessageContainer;
import com.htmlhifive.resourcefw.message.ResponseMessageContext;
import com.htmlhifive.resourcefw.service.ResourceProcessor;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ResourceControllerTest {

	@Configuration
	static class PropConf {

		@Bean
		static PropertySourcesPlaceholderConfigurer myPropertySourcesPlaceholderConfigurer() {
			PropertySourcesPlaceholderConfigurer p = new PropertySourcesPlaceholderConfigurer();
			Resource[] resourceLocations = new Resource[] {
					new ClassPathResource("testConf/message-metadata.properties"),
					new ClassPathResource("testConf/resource-configuration.properties"), };
			p.setLocations(resourceLocations);
			return p;
		}

		@Bean
		MessageMetadata messageMetadata() {
			return new MessageMetadata();
		};

		@Bean
		ResourceConfigurationParameter resourceConfigurationParameter() {
			return new ResourceConfigurationParameter();
		};
	}

	@Autowired
	private MessageMetadata messageMetadata;

	@Autowired
	private ResourceConfigurationParameter resourceConfigurationParameter;

	@Mocked
	private final RequestContextHolder requestContextHolder = null;

	@Mocked
	private Principal principal;

	@Mocked
	private RequestAttributes requestAttributes;

	@Mocked
	private RequestMessageContext requestMessageContext;

	@Mocked
	private ResponseMessageContext responseMessageContext;

	@Mocked
	private ResourceProcessor resourceProcessor;

	@Test
	public void testRequestMessageControl(final @Mocked WebRequest webRequest) {

		final ResourceController target = new ResourceController();
		target.setMessageMetadata(messageMetadata);
		target.setResourceConfigurationParameter(resourceConfigurationParameter);

		@SuppressWarnings("serial")
		final RequestMessage reqMsg1 = new RequestMessage(messageMetadata) {
			{
				put(messageMetadata.HTTP_METHOD, "GET");
				put(messageMetadata.QUERY, new HashMap<String, Object>() {
					{
						put("id", new String[] { "1", "2" });
					}
				});
			}
		};

		final RequestMessage reqMsg2 = new RequestMessage(messageMetadata) {
			{
				put(messageMetadata.HTTP_METHOD, "POST");
			}
		};

		final RequestMessage reqMsg3 = new RequestMessage(messageMetadata) {
			{
				put(messageMetadata.ACTION, "action");
				put(messageMetadata.HTTP_METHOD, "POST");
			}
		};

		final RequestMessageContainer reqContainer = new RequestMessageContainer(true) {
			{
				addMessage(reqMsg1);
				addMessage(reqMsg2);
				addMessage(reqMsg3);
			}
		};

		final ResponseMessageContainer expected = new ResponseMessageContainer(true);

		new Expectations() {
			{
				setField(target, resourceProcessor);

				webRequest.getUserPrincipal();
				result = principal;

				requestMessageContext.put(messageMetadata.USER_PRINCIPAL, principal, MessageSource.CONTROLLER);
				requestMessageContext.get(messageMetadata.ACTION);
				result = null;
				result = null;

				requestMessageContext.get(messageMetadata.COPY);
				requestMessageContext.get(messageMetadata.MOVE);
				result = null;

				resourceProcessor.process(reqContainer);
				result = expected;
			}
		};

		ResponseMessageContainer actual = target.handle(webRequest, reqContainer);

		assertThat(actual, is(equalTo(expected)));
		assertThat((String) reqMsg1.get(messageMetadata.ACTION),
				is(equalTo(resourceConfigurationParameter.DEFAULT_ACTION_FOR_GET_BY_QUERY)));
		assertThat((String) reqMsg2.get(messageMetadata.ACTION),
				is(equalTo(resourceConfigurationParameter.DEFAULT_ACTION_FOR_POST)));
		assertThat((String) reqMsg3.get(messageMetadata.ACTION), is(equalTo("action")));
	}

	@Test
	public void testResponseMessageControl(final @Mocked WebRequest webRequest) {

		resourceConfigurationParameter.RESPONSE_WITH_ERROR_DETAIL = false;

		final ResourceController target = new ResourceController();
		target.setMessageMetadata(messageMetadata);
		target.setResourceConfigurationParameter(resourceConfigurationParameter);

		final RequestMessage reqMsg = new RequestMessage(messageMetadata) {
			{
				put(messageMetadata.ACTION, "insert");
				put(messageMetadata.HTTP_METHOD, "PUT");
			}
		};

		final RequestMessageContainer reqContainer = new RequestMessageContainer(true) {
			{
				addMessage(reqMsg);
			}
		};

		final ResponseMessage resMsg1 = new ResponseMessage(reqMsg) {
			{
				put(messageMetadata.ERROR_CAUSE, "NotFoundException");
				put(messageMetadata.ERROR_DETAIL_INFO, "Resource cannot found");
				put(messageMetadata.ERROR_STACK_TRACE, new Object());
			}
		};

		final ResponseMessageContainer expected = new ResponseMessageContainer(true) {
			{
				addMessage(resMsg1);
			}
		};

		new Expectations() {
			{
				setField(target, resourceProcessor);

				webRequest.getUserPrincipal();
				result = principal;

				requestMessageContext.put(messageMetadata.USER_PRINCIPAL, principal, MessageSource.CONTROLLER);

				resourceProcessor.process(reqContainer);
				result = expected;

				responseMessageContext.get(messageMetadata.ERROR_CAUSE);
				result = null;
				responseMessageContext.get(messageMetadata.ERROR_DETAIL_INFO);
				result = null;
				responseMessageContext.get(messageMetadata.ERROR_STACK_TRACE);
				result = null;
			}
		};

		ResponseMessageContainer actual = target.handle(webRequest, reqContainer);

		ResponseMessage responseMessage = actual.getMessages().get(0);
		assertThat(responseMessage.get(messageMetadata.ERROR_CAUSE), nullValue());
		assertThat(responseMessage.get(messageMetadata.ERROR_DETAIL_INFO), nullValue());
		assertThat(responseMessage.get(messageMetadata.ERROR_STACK_TRACE), nullValue());
	}

	@Test
	public void testPutContentDisposition(final @Mocked WebRequest webRequest) throws Exception {

		final ResourceController target = new ResourceController();
		target.setMessageMetadata(messageMetadata);
		target.setResourceConfigurationParameter(resourceConfigurationParameter);

		final RequestMessage reqMsg = new RequestMessage(messageMetadata) {
			{
				put(messageMetadata.ACTION, "findById");
				put(messageMetadata.HTTP_METHOD, "GET");
				put(messageMetadata.REQUEST_FILE_DOWNLOAD, "true");
			}
		};

		final RequestMessageContainer reqContainer = new RequestMessageContainer(false) {
			{
				addMessage(reqMsg);
			}
		};

		final ResponseMessage resMsg1 = new ResponseMessage(reqMsg) {
			{
				put(messageMetadata.RESPONSE_DOWNLOAD_FILE_NAME, "日本語 ファイル名");
			}
		};

		final ResponseMessageContainer expected = new ResponseMessageContainer(false) {
			{
				addMessage(resMsg1);
			}
		};

		new Expectations() {
			{
				setField(target, resourceProcessor);

				webRequest.getUserPrincipal();
				result = principal;

				requestMessageContext.put(messageMetadata.USER_PRINCIPAL, principal, MessageSource.CONTROLLER);

				resourceProcessor.process(reqContainer);
				result = expected;

				responseMessageContext.get(messageMetadata.RESPONSE_HEADER);
				result = null;

				responseMessageContext.get(messageMetadata.HTTP_HEADER_USER_AGENT);
				result = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.75 Safari/535.7";
			}
		};

		ResponseMessageContainer actual = target.handle(webRequest, reqContainer);
		ResponseMessage responseMessage = actual.getMessages().get(0);
		assertThat(responseMessage.get(messageMetadata.RESPONSE_HEADER), is(not(nullValue())));

		@SuppressWarnings("unchecked")
		Map<String, Object> headers = (Map<String, Object>) responseMessage.get(messageMetadata.RESPONSE_HEADER);
		assertThat(headers.get(messageMetadata.HTTP_HEADER_CONTENT_DISPOSITION), is(not(nullValue())));

		String urlEncodedFileName = URLEncoder.encode("日本語 ファイル名", "UTF8").replace("+", "%20");

		String contentDisposition = (String) headers.get(messageMetadata.HTTP_HEADER_CONTENT_DISPOSITION);
		assertThat(contentDisposition, is(equalTo("attachment;filename*=utf-8'ja'" + urlEncodedFileName)));
	}

	@Test
	public void testPutContentDispositionIE(final @Mocked WebRequest webRequest) throws Exception {

		final ResourceController target = new ResourceController();
		target.setMessageMetadata(messageMetadata);
		target.setResourceConfigurationParameter(resourceConfigurationParameter);

		final RequestMessage reqMsg = new RequestMessage(messageMetadata) {
			{
				put(messageMetadata.ACTION, "findById");
				put(messageMetadata.HTTP_METHOD, "GET");
				put(messageMetadata.REQUEST_FILE_DOWNLOAD, "true");
			}
		};

		final RequestMessageContainer reqContainer = new RequestMessageContainer(false) {
			{
				addMessage(reqMsg);
			}
		};

		final ResponseMessage resMsg1 = new ResponseMessage(reqMsg) {
			{
				put(messageMetadata.RESPONSE_DOWNLOAD_FILE_NAME, "日本語 ファイル名");
			}
		};

		final ResponseMessageContainer expected = new ResponseMessageContainer(false) {
			{
				addMessage(resMsg1);
			}
		};

		new Expectations() {
			{
				setField(target, resourceProcessor);

				webRequest.getUserPrincipal();
				result = principal;

				requestMessageContext.put(messageMetadata.USER_PRINCIPAL, principal, MessageSource.CONTROLLER);

				resourceProcessor.process(reqContainer);
				result = expected;

				responseMessageContext.get(messageMetadata.RESPONSE_HEADER);
				result = null;

				responseMessageContext.get(messageMetadata.HTTP_HEADER_USER_AGENT);
				result = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)";
			}
		};

		ResponseMessageContainer actual = target.handle(webRequest, reqContainer);
		ResponseMessage responseMessage = actual.getMessages().get(0);
		assertThat(responseMessage.get(messageMetadata.RESPONSE_HEADER), is(not(nullValue())));

		@SuppressWarnings("unchecked")
		Map<String, Object> headers = (Map<String, Object>) responseMessage.get(messageMetadata.RESPONSE_HEADER);
		assertThat(headers.get(messageMetadata.HTTP_HEADER_CONTENT_DISPOSITION), is(not(nullValue())));

		String urlEncodedFileName = URLEncoder.encode("日本語 ファイル名", "UTF8").replace("+", "%20");

		String contentDisposition = (String) headers.get(messageMetadata.HTTP_HEADER_CONTENT_DISPOSITION);
		assertThat(contentDisposition, is(equalTo("attachment;filename=\"" + urlEncodedFileName + "\"")));
	}
}
