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
package com.htmlhifive.resourcefw.message;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
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

import com.htmlhifive.resourcefw.config.MessageMetadata;
import com.htmlhifive.resourcefw.config.ResourceConfigurationParameter;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class RequestMessageTest {

	@Mocked(capture = 1)
	private RequestMessageContext requestMessageContext;

	@Mocked(capture = 1)
	private ResponseMessageContext responseMessageContext;

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

	@Test
	public void testEquals() {

		RequestMessage req = new RequestMessage(messageMetadata);
		req.put("a", "b");

		ResponseMessage res = new ResponseMessage(req);
		res.put("a", "b");

		//		KeyInfoUtil.printKeyInfo(req);
		//		KeyInfoUtil.printKeyInfo(res);

		assertThat(req.equals(res), is(false));
	}

	@Test
	public void testHashCode() {

		RequestMessage req = new RequestMessage(messageMetadata);
		req.put("a", "b");

		ResponseMessage res = new ResponseMessage(req);
		res.put("a", "b");

		//		KeyInfoUtil.printKeyInfo(req);
		//		KeyInfoUtil.printKeyInfo(res);

		assertThat(req.hashCode(), is(not(equalTo(res.hashCode()))));
	}
}
