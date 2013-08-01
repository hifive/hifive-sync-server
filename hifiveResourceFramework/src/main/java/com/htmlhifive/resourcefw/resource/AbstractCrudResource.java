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
package com.htmlhifive.resourcefw.resource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.htmlhifive.resourcefw.config.MessageMetadata;
import com.htmlhifive.resourcefw.exception.AbstractResourceException;
import com.htmlhifive.resourcefw.exception.BadRequestException;
import com.htmlhifive.resourcefw.exception.ConflictException;
import com.htmlhifive.resourcefw.exception.GenericResourceException;
import com.htmlhifive.resourcefw.exception.LockedException;
import com.htmlhifive.resourcefw.exception.NotFoundException;
import com.htmlhifive.resourcefw.exception.NotImplementedException;
import com.htmlhifive.resourcefw.exception.ServiceUnavailableException;
import com.htmlhifive.resourcefw.message.RequestMessage;
import com.htmlhifive.resourcefw.message.RequestMessageUtil;
import com.htmlhifive.resourcefw.resource.lock.DefaultLockManager;
import com.htmlhifive.resourcefw.resource.lock.LockManager;
import com.htmlhifive.resourcefw.resource.lock.LockType;
import com.htmlhifive.resourcefw.resource.query.ResourceQuerySpecifications;


/**
 * JPA EntityのCRUDアクションに特化したBasicResource抽象実装.<br>
 * リソース操作(アクション)の汎用実装を提供すると共に、JPA Repository、リソースアイテムやそのIDフィールドの情報など、各リソース固有の情報を取得する抽象メソッドを定義しています.<br>
 * この汎用実装を使用するためには、リソースアイテムと、永続化のためのエンティティが同じ型である必要があります.
 *
 * @author kishigam
 * @param <T>　リソースアイテム型(JPA Entity)
 */
public abstract class AbstractCrudResource<T> implements BasicResource {

	/**
	 * リソースアイテム型のクラスオブジェクト.
	 */
	private Class<T> itemType;

	/**
	 * IDフィールドの名前. デフォルトでは {@link javax.persistence.Id Id} アノテーションが
	 * ついているフィールドを探して代入します.
	 */
	private String idFieldName;

	/**
	 * リソースアイテムのLockマネージャ.
	 */
	private LockManager LockManager = new DefaultLockManager();

	/**
	 * JPA EntityManager.<br>
	 * create, insertの両アクションではキー重複を検出するために、{@link JpaRepository JpaRepository}を経由せず、直接使用します.
	 */
	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * コンストラクタ.<br>
	 * 型引数からリソースアイテム型を抽出します.
	 */
	@SuppressWarnings("unchecked")
	public AbstractCrudResource() {

		// この抽象クラスの型を取得
		ParameterizedType thisType = (ParameterizedType) this.getClass().getGenericSuperclass();

		// この抽象クラスで1つ目の型変数に指定されているのがアイテムの型
		this.itemType = (Class<T>) thisType.getActualTypeArguments()[0];
	}

	public AbstractCrudResource(String idFieldName) {
		this();
		this.idFieldName = idFieldName;
	}

	@PostConstruct
	public void init() {
		if(getIdFieldName() == null || getIdFieldName().isEmpty()) {
			idFieldName = searchIdFieldName(itemType);
		}
	}

	/**
	 * IDでリソースアイテムを検索します.<br>
	 * IDに該当するデータがない場合、listアクションを実行します.
	 *
	 * @throws LockedException
	 * @throws BadRequestException
	 * @see BasicResource#findById(RequestMessage)
	 */
	@Override
	public Object findById(RequestMessage requestMessage) throws NotFoundException, BadRequestException,
			LockedException {

		String id = getId(requestMessage);

		// idがない(リソースパス直下へのGETの場合、listアクションに切り替える)
		if (id.isEmpty()) {
			return list(requestMessage);
		}

		checkCanRead(requestMessage, id);

		T found = getRepository().findOne(id);
		if (found == null) {
			throw new NotFoundException("Resource item is not found :" + id, requestMessage);
		}
		return found;
	}

	/**
	 * クエリ({@link MessageMetadata#QUERY})メタデータを用いて条件に該当するリソースアイテムのリストを返します.<br>
	 * クエリの解析は各リソースの{@link ResourceQuerySpecifications ResourceQuerySpecifications}によって行われます.
	 *
	 * @see BasicResource#findByQuery(RequestMessage)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<T> findByQuery(RequestMessage requestMessage) throws BadRequestException, LockedException {

		Map<String, List<String>> conditions = Collections.emptyMap();

		Object queryObj = requestMessage.get(requestMessage.getMessageMetadata().QUERY);
		if (queryObj instanceof Map) {
			conditions = (Map<String, List<String>>) queryObj;
		}
		if (queryObj instanceof String) {

			String query = (String) queryObj;

			if (query != null && !query.isEmpty()) {
				try {
					conditions = (Map<String, List<String>>) new ObjectMapper().readValue(query, Map.class);
				} catch (IOException e) {
					throw new BadRequestException("Failed to parse query: " + query, e, requestMessage);
				}
			}
		}

		// Specificationsを用いたクエリ実行
		ResourceQuerySpecifications<T> querySpec = this.getQuerySpec();
		JpaSpecificationExecutor<T> specExecutor = this.getSpecificationExecutor();
		List<T> resultList;
		if(querySpec != null && specExecutor != null){
			// QuerySpecとExecutorがあれば、それらを使ってクエリを解釈
			resultList = specExecutor.findAll(querySpec.parseConditions(conditions));
		} else {
			// どちらかがなければ、クエリ無視して全件を返す。
			resultList = getRepository().findAll();
		}

		checkCanRead(requestMessage, resultList);

		return resultList;
	}

	/**
	 * 悲観ロック操作付きのfindByIdです.
	 *
	 * ただしこのクラスのデフォルト実装では、findByIdがそのまま呼ばれるので
	 * ロックは一切かかりません.
	 * 悲観ロックを行いたい場合は、子クラスで適切に実装する必要があります.
	 *
	 * @see AbstractCrudResource#findById(RequestMessage)
	 */
	@Override
	public Object findByIdForUpdate(RequestMessage requestMessage) throws AbstractResourceException {
		return this.findById(requestMessage);
	}

	/**
	 * 悲観ロック操作付きのfindByQueryです.
	 *
	 * ただしこのクラスのデフォルト実装では、findByIdがそのまま呼ばれるので
	 * ロックは一切かかりません.
	 * 悲観ロックを行いたい場合は、子クラスで適切に実装する必要があります.
	 *
	 * @see AbstractCrudResource#findByQuery(RequestMessage)
	 */
	@Override
	public Object findByQueryForUpdate(RequestMessage requestMessage) throws AbstractResourceException {
		return this.findByIdForUpdate(requestMessage);
	}



	/**
	 * IDで指定されたリソースアイテムをリクエストメッセージの情報で生成、または更新します.<br>
	 * 成功した場合、IDを返します.
	 *
	 * @throws NotFoundException
	 * @see BasicResource#insertOrUpdate(RequestMessage)
	 */
	@Override
	public String insertOrUpdate(RequestMessage requestMessage) throws BadRequestException, LockedException,
			NotFoundException {

		String id = getNotEmptyId(requestMessage);
		if (id.isEmpty()) {
			throw new BadRequestException("Resource item id cannot be empty.", requestMessage);
		}

		checkCanWrite(requestMessage, id);

		T item = RequestMessageUtil.extractObject(requestMessage, itemType, doGetIdFieldName(), id);

		getRepository().save(item);

		return id;
	}

	/**
	 * IDで指定されたリソースアイテムを削除します.<br>
	 * 成功した場合、IDを返します.
	 *
	 * @throws LockedException
	 * @throws NotFoundException
	 * @see BasicResource#remove(RequestMessage)
	 */
	@Override
	public String remove(RequestMessage requestMessage) throws LockedException, NotFoundException {

		String id = getNotEmptyId(requestMessage);

		checkCanWrite(requestMessage, id);

		T found = getRepository().findOne(id);
		if (found == null) {
			throw new NotFoundException("Resource item is not found :" + id, requestMessage);
		}

		getRepository().delete(found);

		return id;
	}

	/**
	 * IDを生成し、リクエストメッセージの情報でリソースアイテムを新規生成します.<br>
	 * 成功した場合、生成されたIDを返します.
	 *
	 * @throws BadRequestException
	 * @throws ServiceUnavailableException
	 * @see BasicResource#create(RequestMessage)
	 */
	@Override
	public String create(RequestMessage requestMessage) throws BadRequestException, ServiceUnavailableException {

		String newId = createNewId();

		// 新規なのでlockチェックしない

		T item = RequestMessageUtil.extractObject(requestMessage, itemType, doGetIdFieldName(), newId);

		// JpaRepository#saveでは一意制約違反で更新されてしまうためEntityManagerを使用する
		// ただし、厳密な一意制約違反は検知できない(JPA実装依存)
		try {
			entityManager.persist(item);
			entityManager.flush();
		} catch (PersistenceException e) {
			throw new ServiceUnavailableException("Failed to create item : " + item, e, requestMessage);
		}

		return newId;
	}

	/**
	 * リクエストメッセージの情報でリソースアイテムを新規生成します.<br>
	 * IDもリクエストメッセージに含まれている物を使用します.<br>
	 * 成功した場合、IDを返します.
	 *
	 * @throws BadRequestException
	 * @throws ServiceUnavailableException
	 * @throws LockedException
	 * @throws NotFoundException
	 * @throws ConflictException
	 * @see BasicResource#create(RequestMessage)
	 */
	@Override
	public String insert(RequestMessage requestMessage) throws BadRequestException, ServiceUnavailableException,
			LockedException, NotFoundException, ConflictException {

		String id = getNotEmptyId(requestMessage);

		// 新規なのでlockチェックしない

		if ((boolean) exists(requestMessage).get("exists")) {
			throw new BadRequestException("Resource item already exists : " + id, requestMessage);
		}

		T item = RequestMessageUtil.extractObject(requestMessage, itemType, doGetIdFieldName(), id);

		// JpaRepository#saveでは一意制約違反で更新されてしまうためEntityManagerを使用する
		// ただし、厳密な一意制約違反は検知できない(JPA実装依存)
		try {
			entityManager.persist(item);
			entityManager.flush();
		} catch (PersistenceException e) {
			throw new ConflictException("Failed to insert item : " + item, e, requestMessage);
		}

		return id;
	}

	/**
	 * IDで指定されたリソースアイテムをリクエストメッセージの情報で更新します.<br>
	 * 存在しない場合は{@link NotFoundException NotFoundException}がスローされます.<br>
	 * 成功した場合、IDを返します.
	 *
	 * @throws LockedException
	 * @throws BadRequestException
	 * @throws NotFoundException
	 * @see BasicResource#update(RequestMessage)
	 */
	@Override
	public String update(RequestMessage requestMessage) throws LockedException, NotFoundException, BadRequestException {

		String id = getNotEmptyId(requestMessage);

		checkCanWrite(requestMessage, id);

		@SuppressWarnings("unchecked")
		T item = (T) findById(requestMessage);

		T updatedItem = RequestMessageUtil.extractObject(requestMessage, itemType, doGetIdFieldName(), id, item);

		getRepository().save(updatedItem);

		return id;
	}

	/**
	 * IDで指定されたリソースアイテムが存在するかどうかを判定します.<br>
	 * IDおよび判定結果を含むMapオブジェクトを返します.
	 *
	 * @throws LockedException
	 * @throws NotFoundException
	 * @see BasicResource#exists(RequestMessage)
	 */
	@Override
	public Map<String, Object> exists(RequestMessage requestMessage) throws LockedException, NotFoundException {

		String id = getNotEmptyId(requestMessage);

		checkCanRead(requestMessage, id);

		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("id", id);
		result.put("exists", getRepository().exists(id));

		return result;
	}

	/**
	 * クエリ({@link MessageMetadata#QUERY})メタデータを用いて条件に該当するリソースアイテムのIDリストを返します.
	 *
	 * @throws LockedException
	 * @throws BadRequestException
	 * @see BasicResource#list(RequestMessage)
	 */
	@Override
	public List<String> list(RequestMessage requestMessage) throws BadRequestException, LockedException {

		List<T> itemList = (List<T>) findByQuery(requestMessage);

		List<String> idList = new ArrayList<>();
		for (T item : itemList) {
			idList.add(getIdFieldValue(item));
		}

		return idList;
	}

	/**
	 * クエリ({@link MessageMetadata#QUERY})メタデータを用いて条件に該当するリソースアイテムの数を返します.
	 *
	 * @throws LockedException
	 * @throws BadRequestException
	 * @see BasicResource#count(RequestMessage)
	 */
	@Override
	public Integer count(RequestMessage requestMessage) throws BadRequestException, LockedException {

		return findByQuery(requestMessage).size();
	}

	/**
	 * IDで指定されたリソースアイテムをロックします.<br>
	 * リソースごとのロックタイプ(排他または共有)は{@link AbstractCrudResource#getResourceLockType()}を実装することで指定します.<br>
	 * IDおよびロックトークンを含むMapオブジェクトを返します.
	 *
	 * @throws NotFoundException
	 * @throws LockedException
	 * @see BasicResource#lock(RequestMessage)
	 */
	@Override
	public Map<String, Object> lock(RequestMessage requestMessage) throws NotFoundException, LockedException {

		String id = getNotEmptyId(requestMessage);

		String lockToken = LockManager.lock(id, getResourceLockType(), requestMessage);

		Map<String, Object> body = new HashMap<>();
		body.put("id", id);
		body.put(requestMessage.getMessageMetadata().LOCK_TOKEN, lockToken);

		return body;
	}

	/**
	 * IDで指定されたリソースアイテムのロックを開放します.<br>
	 * リソースごとのロックタイプ(排他または共有)は{@link AbstractCrudResource#getResourceLockType()}を実装することで指定します.
	 * IDおよびロックトークンを含むMapオブジェクトを返します.
	 *
	 * @throws NotFoundException
	 * @throws BadRequestException
	 * @see BasicResource#unlock(RequestMessage)
	 */
	@Override
	public Map<String, Object> unlock(RequestMessage requestMessage) throws NotFoundException, BadRequestException {

		String id = getNotEmptyId(requestMessage);

		String lockToken = LockManager.unlock(id, getResourceLockType(), requestMessage);

		Map<String, Object> body = new HashMap<>();
		body.put("id", id);
		body.put(requestMessage.getMessageMetadata().LOCK_TOKEN, lockToken);

		return body;
	}

	/**
	 * リクエストメッセージから、IDを抽出して返します.<br>
	 * この抽象クラスの実装では、パス({@link MessageMetadata#REQUEST_PATH})メタデータをIDとして取得し、返します.<br>
	 */
	@Override
	public String getId(RequestMessage requestMessage) {

		return requestMessage.getPath();
	}

	/**
	 * このリソースのエンティティを永続化するJPA Repositoryを取得します.<br>
	 *
	 * @return JPA Repository
	 */
	protected abstract JpaRepository<T, String> getRepository();

	/**
	 * {@link Specifications Specifications}による検索を行うための{@link JpaSpecificationExecutor JpaSpecificationExecutor}を取得します.<br>
	 * クエリによる検索でこれらを使用しない場合、{@link NotImplementedException NotImplementedException}
	 * 等をスローし、クエリによる検索を使用するアクションを独自に実装することができます.
	 *
	 * このメソッドのデフォルトの実装ではnullを返すので、findByQueryは条件に関わりなく常に全件を返すようになります.
	 *
	 * @return JpaSpecificationExecutor
	 */
	protected JpaSpecificationExecutor<T> getSpecificationExecutor() {
		// 動作させないためにnullを返す
		return null;
	}


	/**
	 * {@link Specifications Specifications}による検索の条件を表現する{@link ResourceQuerySpecifications ResourceQuerySpecifications}
	 * を取得します.<br>
	 * クエリによる検索でこれらを使用しない場合、{@link NotImplementedException NotImplementedException}
	 * 等をスローし、クエリによる検索を使用するアクションを独自に実装することができます.
	 *
	 * このメソッドのデフォルトの実装ではnullを返すので、findByQueryは条件に関わりなく常に全件を返すようになります.
	 *
	 * @return ResourceQuerySpecifications
	 */
	protected ResourceQuerySpecifications<T> getQuerySpec() {
		// 動作させないためにnullを返す
		return null;
	}


	/**
	 * このリソースのエンティティを新規生成する際に採番されたIDを取得します.
	 * デフォルトの実装では、ランダムUUIDを返します。
	 *
	 * @return ID
	 */
	protected String createNewId() {
		return UUID.randomUUID().toString();
	}


	/**
	 * このリソースのエンティティにおけるIDフィールド名を取得します.
	 *
	 * @return フィールド名
	 */
	protected String getIdFieldName() {
		return idFieldName;
	}

	/**
	 * エラーチェック付きのgetIdFieldNameラッパー.
	 *
	 * @return getIdFieldNameの戻り値。ただし空またはnullの場合を除く
	 * @exception getIdFieldNameの戻り値が空またはnullの場合
	 */
	private final String doGetIdFieldName() {
		String idFieldName = getIdFieldName();
		if(idFieldName == null || idFieldName.isEmpty()) {
			throw new GenericResourceException("Cannot get ID field name. Please check implementation of your resource class.");
		}

		return idFieldName;
	}

	/**
	 * このリソースに対して取得できるロックの種類を取得します.
	 *
	 * @return LockType
	 */
	protected LockType getResourceLockType() {
		// デフォルト実装
		return LockType.EXCLUSIVE;
	};

	/**
	 * リソースアイテム(エンティティ)からIDフィールドの値を取得します.
	 *
	 * @param item リソースアイテム(エンティティ)
	 * @return IDの値
	 */
	protected String getIdFieldValue(T item) {

		Object idFieldValue = null;
		String idFieldName = doGetIdFieldName();

		try {
			Field idField = item.getClass().getDeclaredField(idFieldName);
			idField.setAccessible(true);
			idFieldValue = idField.get(item);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
			throw new GenericResourceException("Failed to access id field : " + item.getClass().getSimpleName() + "#"
					+ idFieldName);
		}

		return (String) idFieldValue;
	}

	/**
	 * リクエストメッセージから、IDを抽出して返します.<br>
	 * 抽出できない場合、{@link NotFoundException NotFoundException}をスローします.
	 *
	 * @param requestMessage リクエストメッセージ
	 * @return ID @
	 * @throws NotFoundException
	 */
	protected String getNotEmptyId(RequestMessage requestMessage) throws NotFoundException {
		String id = getId(requestMessage);
		if (id.isEmpty()) {
			throw new NotFoundException("Resource item has empty id.", requestMessage);
		}
		return id;
	}

	/**
	 * リクエストメッセージからロックトークンを取得し、読み取り可能でない場合{@link LockedException}をスローします.<br>
	 *
	 * @param requestMessage
	 * @param id @
	 */
	protected void checkCanRead(RequestMessage requestMessage, String id) throws LockedException {
		String lockToken = (String) requestMessage.get(requestMessage.getMessageMetadata().LOCK_TOKEN);
		if (!LockManager.canRead(lockToken, id, requestMessage)) {
			throw new LockedException("Resource item is locked : " + id, requestMessage);
		}
	}

	/**
	 * リクエストメッセージからロックトークンを取得し、指定された全てのアイテムに対して読み取り可能でない場合{@link LockedException}をスローします.<br>
	 *
	 * @param requestMessage
	 * @param id @
	 */
	protected void checkCanRead(RequestMessage requestMessage, List<T> itemList) throws LockedException {
		for (T item : itemList) {
			String lockToken = (String) requestMessage.get(requestMessage.getMessageMetadata().LOCK_TOKEN);
			if (!LockManager.canRead(lockToken, getIdFieldValue(item), requestMessage)) {
				throw new LockedException("Resource item is locked : " + getIdFieldValue(item), requestMessage);
			}
		}
	}

	/**
	 * リクエストメッセージからロックトークンを取得し、指定された全てのアイテムに対して書き込み可能でない場合{@link LockedException}をスローします.<br>
	 *
	 * @param requestMessage
	 * @param id @
	 */
	protected void checkCanWrite(RequestMessage requestMessage, String id) throws LockedException {
		String lockToken = (String) requestMessage.get(requestMessage.getMessageMetadata().LOCK_TOKEN);
		if (!LockManager.canWrite(lockToken, id, requestMessage)) {
			throw new LockedException("Resource item is locked : " + id, requestMessage);
		}
	}

	/**
	 * @return the itemType
	 */
	protected Class<T> getItemType() {
		return itemType;
	}

	/**
	 * @param itemType the itemType to set
	 */
	public void setItemType(Class<T> itemType) {
		this.itemType = itemType;
	}

	/**
	 * @return the lockManager
	 */
	protected LockManager getLockManager() {
		return LockManager;
	}

	/**
	 * @param lockManager the lockManager to set
	 */
	public void setLockManager(LockManager lockManager) {
		LockManager = lockManager;
	}

	/**
	 * このリソースでは、この操作はサポートされません。
	 */
	@Override
	public Object copy(RequestMessage requestMessage) throws AbstractResourceException {
		throw new UnsupportedOperationException("copy operation is not supported on this resources.");
	}

	/**
	 * このリソースでは、この操作はサポートされません。
	 */
	@Override
	public Object move(RequestMessage requestMessage) throws AbstractResourceException {
		throw new UnsupportedOperationException("move operation is not supported on this resources.");
	}

	/**
	 * 指定されたクラスからjavax.persistence.Idのついたフィールドを探し、
	 * その名前を返します.
	 *
	 * @param clazz フィールドを探したいクラス
	 * @return javax.persistence.Idのついたフィールドの名前. なければnull
	 */
	private static String searchIdFieldName(Class<?> clazz) {
		// 適切なユーティリティがないので一旦ここに作成

		Field[] fields = clazz.getDeclaredFields();

		for(Field f : fields) {
			if(f.isAnnotationPresent(Id.class)) {
				return f.getName();
			}
		}

		return null;
	}
}
