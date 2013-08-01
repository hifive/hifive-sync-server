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

import static org.apache.log4j.Logger.getLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.ClassUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodProcessor;
import org.springframework.web.servlet.mvc.method.annotation.HttpEntityMethodProcessor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.util.WebUtils;

import com.htmlhifive.resourcefw.config.MessageMetadata;
import com.htmlhifive.resourcefw.config.ResourceWebMvcConfigurer;
import com.htmlhifive.resourcefw.exception.GenericResourceException;
import com.htmlhifive.resourcefw.resource.ResourceActionStatus;
import com.htmlhifive.resourcefw.service.ResourceProcessingStatus;

/**
 * HTTPリクエスト、レスポンスととメッセージおよびそのコンテナの相互変換を行うMethodProcessor実装.<br>
 * SpringMVCの{@link HandlerMethodArgumentResolver HandlerMethodArgumentResolver}および
 * {@link HandlerMethodReturnValueHandler HandlerMethodReturnValueHandler}を実装しており、{@link RequestMappingHandlerAdapter
 * RequestMappingHandlerAdapter} に対して追加することでController(Handler)に対して{@link RequestMessageContainer
 * RequestMessageContainer}を渡し、{@link ResponseMessageContainer ResponseMessageContainer}を受け取ることができます.
 *
 * @author kishigam
 */
public class MessageContainerMethodProcessor extends AbstractMessageConverterMethodProcessor {

	private static final Logger LOGGER = getLogger(MessageContainerMethodProcessor.class);

	/**
	 * HTTPヘッダのContent-Typeに含めるデフォルトのCharset文字列
	 */
	private static final String CHARSET_STR = "utf-8";

	/**
	 * {@link WebMvcConfigurationSupport WebMvcConfigurationSupport}と同等のロジックにより、JSONを用いたHTTPリクエスト、レスポンスが可能であるか判断します.
	 *
	 * @see WebMvcConfigurationSupport
	 */
	private static final boolean jackson2Present = ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper",
			WebMvcConfigurationSupport.class.getClassLoader())
			&& ClassUtils.isPresent("com.fasterxml.jackson.core.JsonGenerator",
					WebMvcConfigurationSupport.class.getClassLoader());

	/**
	 * {@link WebMvcConfigurationSupport WebMvcConfigurationSupport}と同等のロジックにより、JSONを用いたHTTPリクエスト、レスポンスが可能であるか判断します.
	 *
	 * @see WebMvcConfigurationSupport
	 */
	private static final boolean jacksonPresent = ClassUtils.isPresent("org.codehaus.jackson.map.ObjectMapper",
			WebMvcConfigurationSupport.class.getClassLoader())
			&& ClassUtils.isPresent("org.codehaus.jackson.JsonGenerator",
					WebMvcConfigurationSupport.class.getClassLoader());

	/**
	 * メッセージメタデータオブジェクト
	 */
	private MessageMetadata messageMetadata;

	/**
	 * HttpMessageConverterとメッセージメタデータオブジェクトからインスタンスを生成します.
	 *
	 * @param messageConverters
	 * @param messageMetadata
	 * @see AbstractMessageConverterMethodProcessor
	 * @see ResourceWebMvcConfigurer
	 */
	public MessageContainerMethodProcessor(List<HttpMessageConverter<?>> messageConverters,
			MessageMetadata messageMetadata) {
		super(messageConverters);
		this.messageMetadata = messageMetadata;
	}

	/**
	 * HttpMessageConverterとContentNegotiationManager、メッセージメタデータオブジェクトからインスタンスを生成します.
	 *
	 * @param messageConverters
	 * @param messageMetadata
	 * @param contentNegotiationManager
	 * @see AbstractMessageConverterMethodProcessor
	 * @see ResourceWebMvcConfigurer
	 */
	public MessageContainerMethodProcessor(List<HttpMessageConverter<?>> messageConverters,
			ContentNegotiationManager contentNegotiationManager, MessageMetadata messageMetadata) {

		super(messageConverters, contentNegotiationManager);
		this.messageMetadata = messageMetadata;
	}

	/**
	 * このArgumentResolverが使用できるかどうかを判定します.<br>
	 * {@link RequestMessageContainer RequestMessageContainer}を引数にとる場合、trueを返します.
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {

		return RequestMessageContainer.class.isAssignableFrom(parameter.getParameterType());
	}

	/**
	 * このReturnValueHandlerが使用できるかどうかを判定します.<br>
	 * {@link ResponseMessageContainer ResponseMessageContainer}を引数にとる場合、trueを返します.
	 */
	@Override
	public boolean supportsReturnType(MethodParameter returnType) {

		return ResponseMessageContainer.class.isAssignableFrom(returnType.getParameterType());
	}

	/**
	 * HTTPリクエストから、このArgumentResolverがサポートする引数の型のインスタンスを生成し、返します.<br>
	 * HTTPリクエストの各情報を、優先度に基づきRequestMessageあるいはそのコンテキスト情報にputし、コンテナに詰めて返します.
	 *
	 * @see AbstractMessageConverterMethodProcessor#resolveArgument(MethodParameter, ModelAndViewContainer,
	 *      NativeWebRequest, WebDataBinderFactory)
	 */
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

		// MessageContext領域の生成
		webRequest.setAttribute(RequestMessageContext.REQUEST_MESSAGE_CONTEXT_ATTRIBUTE, new HashMap<>(),
				NativeWebRequest.SCOPE_REQUEST);
		webRequest.setAttribute(ResponseMessageContext.RESPONSE_MESSAGE_CONTEXT_ATTRIBUTE, new HashMap<>(),
				NativeWebRequest.SCOPE_REQUEST);

		// Bodyデータの解析、Message、MessageContainerの生成
		RequestMessageContainer container = (RequestMessageContainer) readWithMessageConverters(webRequest, parameter,
				parameter.getGenericParameterType());

		HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

		// MessageContextDataの上書き
		// Cookie
		readCookieValue(container, servletRequest);
		// HTTP method(Verb)
		readHttpMethodValue(container, servletRequest);
		// Header
		readHttpHeaderValue(container, webRequest);

		// Bodyより優先度が高い情報の反映・上書き
		// URL Path, Param
		readUrlPathAndParamValue(container, webRequest);

		return container;
	}

	/**
	 * HTTPリクエストボディの情報を、{@link HttpMessageConverter HttpMessageConverter}を用いて変換します.<br>
	 * Content-Typeを基に、Spring標準のコンバータに処理を振り分けます.
	 *
	 * @see AbstractMessageConverterMethodArgumentResolver
	 */
	@Override
	protected Object readWithMessageConverters(NativeWebRequest webRequest, MethodParameter methodParam, Type targetType)
			throws IOException, HttpMediaTypeNotSupportedException {

		ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
		MediaType contentType = inputMessage.getHeaders().getContentType();

		if (contentType == null) {
			LOGGER.debug("[resourcefw] read [RequestMessageContainer] as byte array, or empty body.");
			return readNoneOrUnknown(inputMessage);
		}

		if (MediaType.APPLICATION_FORM_URLENCODED.includes(contentType)) {
			LOGGER.debug("[resourcefw] read [RequestMessageContainer] as \"" + contentType + "\" using ["
					+ "FormHttpMessageConverter" + "]");
			return readAsFormMessage(inputMessage);
		}

		if (MediaType.MULTIPART_FORM_DATA.includes(contentType)) {
			LOGGER.debug("[resourcefw] read [RequestMessageContainer] as \"" + contentType + "\"");
			return readAsMultipartMessage(webRequest);
		}

		if (MediaType.APPLICATION_JSON.includes(contentType)) {

			AbstractHttpMessageConverter<Object> converter = null;

			if (jackson2Present) {
				LOGGER.debug("[resourcefw] read [RequestMessageContainer] as \"" + contentType + "\" using ["
						+ "MappingJackson2HttpMessageConverter" + "]");

				converter = new MappingJackson2HttpMessageConverter();
			} else if (jacksonPresent) {
				LOGGER.debug("[resourcefw] read [RequestMessageContainer] as \"" + contentType + "\" using ["
						+ "MappingJacksonHttpMessageConverter" + "]");

				converter = new MappingJacksonHttpMessageConverter();
			} else {
				throw new GenericResourceException("No jackson library found.");
			}

			return readAsJson(inputMessage, converter);
		}

		// other content type
		LOGGER.debug("[resourcefw] read [RequestMessageContainer] as \"InputStream\"");
		return readAsInputStream(inputMessage, contentType);
	}

	/**
	 * ボディ情報をバイト列で取得し、メッセージにputします.<br>
	 * コンテナは単一リクエストの情報を保持します.
	 *
	 * @param inputMessage リクエスト情報のラッパー
	 * @return RequestMessageContainer
	 * @throws IOException
	 */
	private RequestMessageContainer readNoneOrUnknown(ServletServerHttpRequest inputMessage) throws IOException {
		ByteArrayHttpMessageConverter converter = new ByteArrayHttpMessageConverter();
		byte[] byteArray = converter.read(byte[].class, inputMessage);

		RequestMessage message = new RequestMessage(messageMetadata);
		if (byteArray.length > 0) {
			message.put(messageMetadata.REQUEST_CONTENT_KEY, messageMetadata.REQUEST_CONTENT, MessageSource.BODY);
			message.put(messageMetadata.REQUEST_CONTENT, byteArray, MessageSource.BODY);
		}

		return createRequestMessageContainer(false, message);
	}

	/**
	 * ボディ情報をフォームデータとして読み取り、メッセージにputします.<br>
	 * コンテナは単一リクエストの情報を保持します.
	 *
	 * @param inputMessage リクエスト情報のラッパー
	 * @return RequestMessageContainer
	 * @throws IOException
	 */
	private RequestMessageContainer readAsFormMessage(ServletServerHttpRequest inputMessage) throws IOException {

		FormHttpMessageConverter converter = new AllEncompassingFormHttpMessageConverter();

		// 以下ではAntのjavacタスクでコンパイルできない
		//		MultiValueMap<String, String> valueMap = converter.read(
		//				(Class<? extends MultiValueMap<String, ?>>) MultiValueMap.class, inputMessage);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		MultiValueMap<String, String> valueMap = converter.read(
				(Class<? extends MultiValueMap<String, ?>>) new LinkedMultiValueMap().getClass(), inputMessage);

		RequestMessage message = new RequestMessage(messageMetadata);
		for (String key : valueMap.keySet()) {
			List<String> values = valueMap.get(key);
			message.put(key, values.size() == 1 ? values.get(0) : values, MessageSource.BODY);
		}

		// REQUEST_CONTENTを含んでいる場合、REQUEST_CONTENT_KEYを設定
		if (valueMap.containsKey(messageMetadata.REQUEST_CONTENT)) {
			message.put(messageMetadata.REQUEST_CONTENT_KEY, messageMetadata.REQUEST_CONTENT, MessageSource.BODY);
		}

		return createRequestMessageContainer(false, message);
	}

	/**
	 * ボディ情報をMultipartデータとして読み取り、メッセージにputします.<br>
	 * ファイルデータは{@link MultipartFileValueHolder MultipartFileValueHolder}でラップされます.<br>
	 * コンテナは単一リクエストの情報を保持します.
	 *
	 * @param webRequest リクエスト情報のラッパー
	 * @return RequestMessageContainer
	 */
	private RequestMessageContainer readAsMultipartMessage(NativeWebRequest webRequest) {

		HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
		MultipartHttpServletRequest multipartRequest = WebUtils.getNativeRequest(servletRequest,
				MultipartHttpServletRequest.class);

		RequestMessage message = new RequestMessage(messageMetadata);

		// form data
		Map<String, String[]> parameterMap = webRequest.getParameterMap();
		for (String key : parameterMap.keySet()) {
			String[] paramArray = parameterMap.get(key);
			message.put(key, paramArray.length == 1 ? paramArray[0] : paramArray, MessageSource.BODY);
		}

		// File
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();

		String dirPath = (String) message.get(messageMetadata.REQUEST_MULTIPART_FILES_DIR);
		if (dirPath != null && !dirPath.isEmpty()) {

			// ディレクトリ内ファイル一括対応 ： 多重化リクエスト化すべきだが、現状はディレクトリと全ファイルの情報を持つValueHolderを使って単一リクエストとして扱っている
			DirectoryMultipartFileValueHolder dirValueHolder = new DirectoryMultipartFileValueHolder(dirPath);

			for (String key : fileMap.keySet()) {
				MultipartFile multipartFile = fileMap.get(key);
				dirValueHolder.addFileValues(new MultipartFileValueHolder(multipartFile));
			}

			message.put(messageMetadata.REQUEST_CONTENT_KEY, messageMetadata.REQUEST_CONTENT, MessageSource.BODY);
			message.put(messageMetadata.REQUEST_CONTENT, dirValueHolder, MessageSource.BODY);

		} else {

			for (String key : fileMap.keySet()) {
				MultipartFile multipartFile = fileMap.get(key);
				MultipartFileValueHolder valueHolder = new MultipartFileValueHolder(multipartFile);
				message.put(messageMetadata.REQUEST_CONTENT_KEY, key, MessageSource.BODY);
				message.put(key, valueHolder, MessageSource.BODY);
			}
		}

		return createRequestMessageContainer(false, message);
	}

	/**
	 * ボディ情報をJSONデータとして読み取り、メッセージにputします.<br>
	 * コンテナは単一リクエストあるいは多重化リクエストの情報を保持します.<br>
	 *
	 * @param inputMessage リクエスト情報のラッパー
	 * @param converter jacksonコンバータ
	 * @return RequestMessageContainer
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private RequestMessageContainer readAsJson(ServletServerHttpRequest inputMessage,
			AbstractHttpMessageConverter<Object> converter) throws IOException {

		Object converted = converter.read(Object.class, inputMessage);

		List<Map<String, Object>> messageObjList;
		boolean multiplexed;
		// 変換結果がMapであれば単一リクエスト、Listであれば多重化リクエスト
		if (converted instanceof Map) {
			multiplexed = false;
			messageObjList = new ArrayList<>();
			messageObjList.add((Map<String, Object>) converted);
		} else {
			multiplexed = true;
			messageObjList = (List<Map<String, Object>>) converted;
		}

		List<RequestMessage> messages = new ArrayList<>();
		for (Map<String, Object> messageObj : messageObjList) {
			RequestMessage message = new RequestMessage(messageMetadata);
			for (String key : messageObj.keySet()) {
				message.put(key, messageObj.get(key), MessageSource.BODY);
			}
			messages.add(message);
		}

		return createRequestMessageContainer(multiplexed, messages.toArray(new RequestMessage[messages.size()]));
	}

	/**
	 * ボディ情報を指定されたMIMEタイプのストリームとしてメッセージにputします.<br>
	 * MIMEタイプはメッセージのContent-Typeとしてメッセージに設定され、リソースではこれを参照してストリームを扱うことができます.<br>
	 * コンテナは単一リクエストの情報を保持します.
	 *
	 * @param inputMessage リクエスト情報のラッパー
	 * @param contentType MIMEタイプ
	 * @return RequestMessageContainer
	 * @throws IOException
	 */
	private RequestMessageContainer readAsInputStream(ServletServerHttpRequest inputMessage, MediaType contentType)
			throws IOException {

		RequestMessage message = new RequestMessage(messageMetadata);
		message.put(messageMetadata.REQUEST_CONTENT_KEY, messageMetadata.REQUEST_CONTENT, MessageSource.BODY);
		message.put(messageMetadata.REQUEST_CONTENT, inputMessage.getBody(), MessageSource.BODY);

		return createRequestMessageContainer(false, message);
	}

	/**
	 * 全てのリクエストメッセージを含むコンテナを生成し、返します.
	 *
	 * @param multiplexed 多重化リクエストの場合true
	 * @param messages リクエストメッセージ
	 * @return コンテナ
	 */
	private RequestMessageContainer createRequestMessageContainer(boolean multiplexed, RequestMessage... messages) {

		RequestMessageContainer container = new RequestMessageContainer(multiplexed);
		for (RequestMessage message : messages) {
			container.addMessage(message);
		}
		return container;
	}

	/**
	 * HTTP Cookieの値を読み取り、コンテキスト情報としてコンテナ内の各メッセージから参照可能にします.
	 *
	 * @param container コンテナ
	 * @param servletRequest Cookie情報を含むHTTPリクエストのラッパー
	 */
	private void readCookieValue(RequestMessageContainer container, HttpServletRequest servletRequest) {

		Cookie cookies[] = servletRequest.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				container.putContextData(cookie.getName(), cookie.getValue(), MessageSource.COOKIE);
			}
		}
	}

	/**
	 * HTTP method(GET,PUT,POST,DELETE)の値を読み取り、コンテキスト情報としてコンテナ内の各メッセージから参照可能にします.<br>
	 * メッセージでは、キー{@link MessageMetadata#HTTP_METHOD}で参照できます.
	 *
	 * @param container コンテナ
	 * @param servletRequest HTTP methodを含むHTTPリクエストのラッパー
	 */
	private void readHttpMethodValue(RequestMessageContainer container, HttpServletRequest servletRequest) {
		container.putContextData(messageMetadata.HTTP_METHOD, servletRequest.getMethod().toString(),
				MessageSource.HTTP_METHOD);
	}

	/**
	 * HTTP Headerの値を読み取り、コンテキスト情報としてコンテナ内の各メッセージから参照可能にします.<br>
	 * {@link MessageMetadata#PREFIX_HTTP_HEADER}で始まるヘッダは、アプリ固有ヘッダとしてメッセージにおけるメタデータのプレフィックスを付与します.<br>
	 * そうでないヘッダは無視されます.
	 *
	 * @param container コンテナ
	 * @param webRequest Header情報を含むHTTPリクエストのラッパー
	 */
	private void readHttpHeaderValue(RequestMessageContainer container, NativeWebRequest webRequest) {

		// NativeWebRequestから取得できるヘッダフィールド名は小文字になっている
		String lowerCasePrefix = messageMetadata.PREFIX_HTTP_HEADER.toLowerCase();

		for (Iterator<String> iterator = webRequest.getHeaderNames(); iterator.hasNext();) {
			String headerName = iterator.next();

			String[] headerValues = webRequest.getHeaderValues(headerName);

			// アプリ固有ヘッダはプレフィックスを変換、標準のヘッダはそのまま
			if (headerName.startsWith(lowerCasePrefix)) {
				headerName = Pattern.compile(messageMetadata.PREFIX_HTTP_HEADER, Pattern.CASE_INSENSITIVE)
						.matcher(headerName).replaceFirst(messageMetadata.PREFIX_METADATA);
			}

			container.putContextData(headerName, headerValues.length == 1 ? headerValues[0] : headerValues,
					MessageSource.HEADER);
		}
	}

	/**
	 * URLパス,URLパラメータの値を読み取り、メッセージから参照可能にします.<br>
	 * 多重化リクエストにおいてはコンテキスト情報としてコンテナ内の各メッセージから参照可能にします.<br>
	 * 単一リクエストにおいては、ボディデータよりも高い優先度であるため、メッセージに上書き設定します.
	 *
	 * @param container コンテナ
	 * @param webRequest URLパス,URLパラメータ情報を含むHTTPリクエストのラッパー
	 */
	private void readUrlPathAndParamValue(RequestMessageContainer container, NativeWebRequest webRequest) {

		String orgPath = (String) webRequest.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
				NativeWebRequest.SCOPE_REQUEST);

		container.putContextData(messageMetadata.REQUEST_PATH_ORG, orgPath, MessageSource.URL_PATH);

		String matchedPath = (String) webRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE,
				NativeWebRequest.SCOPE_REQUEST);

		String remainingPath = orgPath.replaceFirst(matchedPath, "");
		Map<String, String[]> parameterMap = webRequest.getParameterMap();

		// 多重化リクエストの場合、個々のメッセージ(Body)で指定されていない場合のデフォルト値になるため、ContextDataとしてセットする
		if (container.isMultiplexed()) {

			// Path
			container.putContextData(messageMetadata.REQUEST_PATH, remainingPath, MessageSource.URL_PATH);

			// Param
			for (String key : parameterMap.keySet()) {
				String[] value = parameterMap.get(key);
				container.putContextData(key, value.length == 1 ? value[0] : value, MessageSource.URL_PARAM);
			}
		} else {
			// 多重化リクエストでなければ、messageにセット(上書き)される
			RequestMessage message = container.getMessages().get(0);

			// Path
			message.put(messageMetadata.REQUEST_PATH, remainingPath, MessageSource.URL_PATH);

			// Param
			for (String key : parameterMap.keySet()) {
				String[] value = parameterMap.get(key);
				message.put(key, value.length == 1 ? value[0] : value, MessageSource.URL_PARAM);
			}
		}
	}

	/**
	 * このReturnValueHandlerがサポートする戻り値の型のインスタンスからHTTPレスポンスを生成します.<br>
	 * ResponseMessageあるいはそのコンテキスト情報をResponseEntityに設定します.
	 *
	 * @see ResponseEntity
	 * @see AbstractMessageConverterMethodProcessor#handleReturnValue(Object, MethodParameter, ModelAndViewContainer,
	 *      NativeWebRequest)
	 */
	@Override
	public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest) throws Exception {

		mavContainer.setRequestHandled(true);

		if (returnValue == null) {
			return;
		}

		ResponseMessageContainer container = (ResponseMessageContainer) returnValue;

		ResponseEntity<?> convertedResponseEntity = null;
		if (container.isMultiplexed()) {
			convertedResponseEntity = convertToMultiplexedResponseEntity(container);
		} else {
			convertedResponseEntity = convertToResponseEntity(container.getMessages().get(0));
		}

		doHandleHttpEntityReturnValue(convertedResponseEntity, returnType, webRequest);

		// MessageContext領域の削除
		webRequest.removeAttribute(RequestMessageContext.REQUEST_MESSAGE_CONTEXT_ATTRIBUTE,
				NativeWebRequest.SCOPE_REQUEST);
		webRequest.removeAttribute(ResponseMessageContext.RESPONSE_MESSAGE_CONTEXT_ATTRIBUTE,
				NativeWebRequest.SCOPE_REQUEST);
	}

	/**
	 * 多重化リクエストに対するレスポンスメッセージからHTTPレスポンスを生成します.<br>
	 * 原則として、コンテキスト情報からHTTPレスポンスヘッダへ、各メッセージの情報はHTTPレスポンスボディに設定します.<br>
	 * 各メッセージはそのメッセージ固有のヘッダ、ボディを持っていますが、それらは全てHTTPレスポンスボディに含まれます.
	 *
	 * @param container レスポンスメッセージのコンテナ
	 * @return ResponseEntity
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	protected ResponseEntity<?> convertToMultiplexedResponseEntity(ResponseMessageContainer container)
			throws IOException {

		ResourceActionStatus statusOfAll = ResourceActionStatus.OK;

		HttpHeaders httpHeaders = new HttpHeaders();

		// RESPONSE_STATUS(初期値)
		statusOfAll = ResourceActionStatus.OK;

		// RESPONSE_HEADER
		// Http headerはContextの"header"から取得
		Object headerObj = container.getContextData(messageMetadata.RESPONSE_HEADER);
		if (headerObj != null) {
			writeHttpHeaders(httpHeaders, (Map<String, Object>) headerObj);
		}

		// 多重化リクエストのContent-TypeはJSON固定
		// MessageMetadata.ACCEPTの指定によってmultipart/form-data、他にも対応できるが、現状は対応していない
		MediaType json = MediaType.APPLICATION_JSON;
		httpHeaders.setContentType(new MediaType(json.getType(), json.getSubtype(), Charset.forName(CHARSET_STR)));

		// RESPONSE_BODY
		List<Map<String, Object>> list = new ArrayList<>();
		for (ResponseMessage message : container.getMessages()) {

			Map<String, Object> map = new HashMap<>();

			// RESPONSE_STATUS(個別)
			// Bodyデータとして残らないように、取得したらmesageから消す
			ResourceActionStatus status = (ResourceActionStatus) message.remove(messageMetadata.RESPONSE_STATUS);
			map.put(messageMetadata.RESPONSE_STATUS, status);

			// RESPONSE_HEADER(個別)
			// HTTPHeaderと重複して全レスポンスメッセージに含まれてしまう。仕様とするか、ContextDataを参照しないgetを用意する？
			Object headerForMessageObj = message.get(messageMetadata.RESPONSE_HEADER);
			if (headerForMessageObj != null) {
				map.put(messageMetadata.RESPONSE_HEADER, headerForMessageObj);
			}

			// RESPONSE_BODY(個別)
			Object responseBody = convertResponseBodyData(message);
			if (responseBody != null) {
				map.put(messageMetadata.RESPONSE_BODY, responseBody);
			}

			// 多重化リクエストの個々リクエスト処理が例外によりBREAKした場合、そのstatusをHttpStatusに設定する
			// そうでなければ(CONTINUE)、途中に例外がスローされていたとしてもOKが設定される
			// Bodyデータとして残らないように、取得したらmesageから消す
			if ((ResourceProcessingStatus) message.remove(messageMetadata.PROCESSING_STATUS) == ResourceProcessingStatus.TERMINATE) {
				statusOfAll = status;
			}

			list.add(map);
		}

		return new ResponseEntity<>(list, httpHeaders, statusOfAll.getHttpStatus());
	}

	/**
	 * 単一リクエストに対するレスポンスメッセージからHTTPレスポンスを生成します.<br>
	 * 原則としてメッセージのヘッダ情報はHTTPレスポンスヘッダに、ボディ情報はHTTPレスポンスボディに設定されます.
	 *
	 * @param message レスポンスメッセージ
	 * @return ResponseEntity
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	protected ResponseEntity<?> convertToResponseEntity(ResponseMessage message) throws IOException {

		HttpHeaders httpHeaders = new HttpHeaders();

		// 多重化リクエストでなければ不要
		message.remove(messageMetadata.PROCESSING_STATUS);

		// RESPONSE_STATUS
		// Bodyデータとして残らないように、取得したらmesageから消す
		ResourceActionStatus status = (ResourceActionStatus) message.remove(messageMetadata.RESPONSE_STATUS);

		// RESPONSE_HEADER
		// Bodyデータとして残らないように、取得したらmesageから消す
		Object headerObj = message.remove(messageMetadata.RESPONSE_HEADER);

		if (headerObj != null) {
			// Content-Typeが含まれていればここでセットされる
			writeHttpHeaders(httpHeaders, (Map<String, Object>) headerObj);
		}

		// Content-Typeが含まれていない場合、MessageMetadata.ACCEPTがあればそれを使用する
		if (httpHeaders.getContentType() == null) {
			String accept = (String) message.get(messageMetadata.ACCEPT);
			if (accept != null) {
				httpHeaders.setContentType(MediaType.valueOf(accept));
			}
		}

		// RESPONSE_BODY
		Object responseBody = convertResponseBodyData(message);
		if (responseBody == null) {
			return new ResponseEntity<>(httpHeaders, status.getHttpStatus());
		}
		return new ResponseEntity<>(responseBody, httpHeaders, status.getHttpStatus());
	}

	/**
	 * レスポンスメッセージからボディの情報を抽出し、HTTPレスポンスボディに設定するオブジェクトに変換して返します.
	 *
	 * @param message レスポンスメッセージ
	 * @return 抽出、変換後のボディ情報オブジェクト
	 */
	private Object convertResponseBodyData(ResponseMessage message) throws IOException {

		Object bodyObj = message.get(messageMetadata.RESPONSE_BODY);

		if (bodyObj != null) {

			// FileやStreamは読み込み、byte[]に書き出し
			if (bodyObj instanceof File) {
				return FileUtils.readFileToByteArray((File) bodyObj);
			}
			if (bodyObj instanceof InputStream) {
				return FileCopyUtils.copyToByteArray((InputStream) bodyObj);
			}

			// Stringで、空文字列の時はBodyを空にする
			if (bodyObj instanceof String && ((String) bodyObj).isEmpty()) {
				return null;
			}

			// それ以外(Beanなどを想定)はそのまま
			return bodyObj;
		}

		// RESPONSE_BODYがなければ、メタデータを除く全てを含むObject(Map)をセット
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> messageDataWithoutHeader = getMessageDataWithoutHeader(message);
		if (!messageDataWithoutHeader.isEmpty()) {
			map.put(messageMetadata.RESPONSE_BODY, messageDataWithoutHeader);
			return map;
		}

		// Bodyが空ならなにもセットしない
		return null;
	}

	/**
	 * Map情報をHTTPHeadersオブジェクトを返します.<br>
	 * Mapのキーがヘッダフィールド名、値がヘッダフィールドの値に対応します.
	 *
	 * @param httpHeaders
	 * @param header
	 */
	private void writeHttpHeaders(HttpHeaders httpHeaders, Map<String, Object> header) {

		for (String headerNameMetadataKey : header.keySet()) {
			Object headerValue = header.get(headerNameMetadataKey);

			// メタデータプレフィックスが付いていたらHTTPヘッダプレフィックスに変換
			String httpHeaderFieldName = headerNameMetadataKey.replaceFirst(messageMetadata.PREFIX_METADATA,
					messageMetadata.PREFIX_HTTP_HEADER);

			if (headerValue.getClass().isArray()) {
				for (String value : (String[]) headerValue) {

					httpHeaders.add(httpHeaderFieldName, value);
				}
			} else {
				httpHeaders.add(httpHeaderFieldName, (String) headerValue);
			}
		}
	}

	/**
	 * レスポンスメッセージから、メタデータでない(キーが{@link MessageMetadata#PREFIX_METADATA　PREFIX_METADATA}で始まっていない)全てのデータをMapで返します.
	 *
	 * @param message レスポンスメッセ―ジ
	 * @return Map
	 */
	private Map<String, Object> getMessageDataWithoutHeader(ResponseMessage message) {

		Map<String, Object> map = new HashMap<>();
		for (String key : message.keys()) {
			if (!key.startsWith(messageMetadata.PREFIX_METADATA)) {
				map.put(key, message.get(key));
			}
		}

		return map;
	}

	/**
	 * HttpEntityMethodProcessorと同様のロジックでResponseEntityオブジェクトからHTTPレスポンスを生成します.
	 *
	 * @see HttpEntityMethodProcessor#handleReturnValue(Object, MethodParameter, ModelAndViewContainer,
	 *      NativeWebRequest)
	 */
	private void doHandleHttpEntityReturnValue(ResponseEntity<?> returnValue, MethodParameter returnType,
			NativeWebRequest webRequest) throws Exception {

		ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
		ServletServerHttpResponse outputMessage = createOutputMessage(webRequest);

		outputMessage.setStatusCode(returnValue.getStatusCode());

		HttpHeaders entityHeaders = returnValue.getHeaders();
		if (!entityHeaders.isEmpty()) {
			outputMessage.getHeaders().putAll(entityHeaders);
		}

		Object body = returnValue.getBody();
		if (body != null) {
			writeWithMessageConverters(body, returnType, inputMessage, outputMessage);
		} else {
			outputMessage.getBody();
		}
	}
}
