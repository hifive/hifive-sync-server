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
package com.htmlhifive.resourcefw.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import mockit.Expectations;
import mockit.Mocked;

import org.junit.Before;
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

import com.htmlhifive.resourcefw.config.MessageMetadata;
import com.htmlhifive.resourcefw.config.ResourceConfigurationParameter;
import com.htmlhifive.resourcefw.message.MessageSource;
import com.htmlhifive.resourcefw.message.RequestMessage;
import com.htmlhifive.resourcefw.message.RequestMessageContainer;
import com.htmlhifive.resourcefw.message.RequestMessageContext;
import com.htmlhifive.resourcefw.message.ResponseMessage;
import com.htmlhifive.resourcefw.message.ResponseMessageContainer;
import com.htmlhifive.resourcefw.message.ResponseMessageContext;
import com.htmlhifive.resourcefw.resource.ResourceActionStatus;
import com.htmlhifive.resourcefw.resource.ResourceMethodInvoker;
import com.htmlhifive.resourcefw.service.processing.AlwaysTerminatingStrategy;
import com.htmlhifive.resourcefw.util.KeyInfoUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class DefaultResourceProcessorTest {

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
	private ResourceManager resourceManager;

	@Mocked
	private RequestMessageContext requestMessageContext;

	@Mocked
	private ResponseMessageContext responseMessageContext;

	private DefaultResourceProcessor target;

	@Before
	public void setUpClass() {
		target = new DefaultResourceProcessor();
		target.setMessageMetadata(messageMetadata);
		target.setResourceConfigurationParameter(resourceConfigurationParameter);
		target.setProcessContinuationStrategy(new AlwaysTerminatingStrategy());
	}

	/**
	 * 正常系(多重化リクエスト)
	 *
	 * @throws Exception
	 */
	@Test
	public void testProcess() throws Exception {

		final RequestMessage reqMsg1 = new RequestMessage(messageMetadata) {
			{
				put(messageMetadata.ACTION, "findById");
				put(messageMetadata.REQUEST_PATH, "/resname1/1/");
			}
		};

		final RequestMessage reqMsg2 = new RequestMessage(messageMetadata) {
			{
				put(messageMetadata.ACTION, "action");
				put(messageMetadata.REQUEST_PATH, "/resname2");
			}
		};

		final RequestMessageContainer reqContainer = new RequestMessageContainer(true) {
			{
				addMessage(reqMsg1);
				addMessage(reqMsg2);
			}
		};

		final Object item1 = new Object();
		final ResponseMessage resMsg1 = new ResponseMessage(reqMsg1) {
			{
				put(messageMetadata.RESPONSE_STATUS, ResourceActionStatus.OK, MessageSource.PROCESSOR);
				put(messageMetadata.RESPONSE_BODY, item1, MessageSource.PROCESSOR);
				put(messageMetadata.PROCESSING_STATUS, ResourceProcessingStatus.CONTINUE, MessageSource.PROCESSOR);
			}
		};

		final Object item2 = new Object();
		final ResponseMessage resMsg2 = new ResponseMessage(reqMsg2) {
			{
				put(messageMetadata.RESPONSE_STATUS, ResourceActionStatus.OK, MessageSource.PROCESSOR);
				put(messageMetadata.RESPONSE_BODY, item2, MessageSource.PROCESSOR);
				put(messageMetadata.PROCESSING_STATUS, ResourceProcessingStatus.CONTINUE, MessageSource.PROCESSOR);
			}
		};

		final ResponseMessageContainer expected = new ResponseMessageContainer(true) {
			{
				addMessage(resMsg1);
				addMessage(resMsg2);
			}
		};

		new Expectations() {
			ResourceMethodInvoker invoker1;
			ResourceMethodInvoker invoker2;
			{
				setField(target, resourceManager);

				resourceManager.getResourceMethodByName("resname1", "findById", reqMsg1);
				result = invoker1;

				invoker1.invoke(reqMsg1);
				result = item1;

				resourceManager.getResourceMethodByName("resname2", "action", reqMsg2);
				result = invoker2;

				invoker2.invoke(reqMsg2);
				result = item2;
			}
		};

		ResponseMessageContainer actual = target.process(reqContainer);
		KeyInfoUtil.printKeyInfo(actual);
		KeyInfoUtil.printKeyInfo(expected);

		assertThat(actual.getMessages().size(), is(equalTo(expected.getMessages().size())));
		for (ResponseMessage msg : expected.getMessages()) {
			assertThat(actual.getMessages().contains(msg), is(true));
		}
	}
}
