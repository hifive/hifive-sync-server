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
package com.htmlhifive.sync.sample.scd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.htmlhifive.sync.exception.NotFoundException;
import com.htmlhifive.sync.resource.ClientResolvingStrategy;
import com.htmlhifive.sync.resource.OptimisticLockManager;
import com.htmlhifive.sync.resource.SyncResourceService;
import com.htmlhifive.sync.resource.separated.SeparatedCommonDataSyncResource;
import com.htmlhifive.sync.sample.person.PersonBean;
import com.htmlhifive.sync.sample.person.PersonRepository;

/**
 * scheduleデータモデルの情報を同期リソースとして公開するためのリソースクラス.<br>
 * 同期データを専用サービスで管理する抽象リソースクラスをこのリソース用に実装します.
 *
 * @author kishigam
 */
@SyncResourceService(syncDataModel = "schedule", lockManager = OptimisticLockManager.class, updateStrategy = ClientResolvingStrategy.class)
@Transactional(propagation = Propagation.MANDATORY)
public class ScheduleResource extends SeparatedCommonDataSyncResource<String, ScheduleResourceElement> {

	/**
	 * リソースID文字列を生成するためのprefix.
	 */
	private static final String TARGET_ID_PREFIX = ScheduleResource.class.getName();

	/**
	 * エンティティの永続化を担うリポジトリ.
	 */
	@Resource
	private ScheduleRepository repository;

	/**
	 * 関連するpersonエンティティを取得するためのリポジトリ.
	 */
	@Resource
	private PersonRepository personRepository;

	/**
	 * 単一データGETメソッドのリソース別独自処理. <br>
	 * エンティティをリポジトリから取得しエレメント(コンストラクタ）wokaesu .します.
	 *
	 * @param resourceIdStr リソースID文字列
	 * @return リソースエレメント
	 * @throws NotFoundException 指定されたIDのエンティティが存在しない場合
	 */
	@Override
	protected ScheduleResourceElement getImpl(String resourceIdStr) {

		ScheduleBean bean = findBean(resourceIdStr);

		ScheduleResourceElement element = new ScheduleResourceElement(bean.getScheduleId());
		element.setUserIds(bean.getUserIds());
		element.setTitle(bean.getTitle());
		element.setCategory(bean.getCategory());
		element.setDates(bean.getDates());
		element.setStartTime(bean.getStartTime());
		element.setFinishTime(bean.getFinishTime());
		element.setDetail(bean.getDetail());
		element.setPlace(bean.getPlace());

		return element;
	}

	/**
	 * 複数データGETメソッドのリソース別独自処理. <br>
	 * 単一データの取得をループし、Mapに格納して返します. TODO: リポジトリアクセスは一回で済ませる改善
	 *
	 * @param resourceIdStrSet リソースID文字列のSet
	 * @param queryMap クエリMap
	 * @return リソースエレメント(IDをKeyとするMap)
	 * @throws NotFoundException 指定されたIDのエンティティが存在しない場合
	 */
	@Override
	protected Map<String, ScheduleResourceElement> getImpl(Set<String> resourceIdStrSet, Map<String, String[]> queryMap) {

		Map<String, ScheduleResourceElement> elementMap = new HashMap<>();
		for (String resourceIdStr : resourceIdStrSet) {

			ScheduleResourceElement element = getImpl(resourceIdStr);

			// TODO: queryの適用

			elementMap.put(resourceIdStr, element);
		}
		return elementMap;
	}

	/**
	 * PUTメソッドのリソース別独自処理. <br>
	 * エンティティをリポジトリから取得し、更新します. 取得できない場合、{@link NotFoundException}をスローします.
	 *
	 * @param resourceIdStr リソースID文字列
	 * @param element 更新内容を含むリソースエレメント
	 */
	@Override
	protected void putImpl(String resourceIdStr, ScheduleResourceElement element) {

		ScheduleBean updatingEntity = findBean(resourceIdStr);

		List<PersonBean> userIdBeans = updatingEntity.getUserBeans();
		userIdBeans.clear();
		for (String userId : element.getUserIds()) {
			userIdBeans.add(personRepository.findOne(userId));
		}
		updatingEntity.setUserBeans(userIdBeans);
		updatingEntity.setTitle(element.getTitle());
		updatingEntity.setCategory(element.getCategory());
		// 元の日付リストの更新
		updatingEntity.setUpdatedDateBeans(element.getDates());
		updatingEntity.setStartTime(element.getStartTime());
		updatingEntity.setFinishTime(element.getFinishTime());
		updatingEntity.setDetail(element.getDetail());
		updatingEntity.setPlace(element.getPlace());

		repository.save(updatingEntity);
	}

	/**
	 * DELETEメソッドのリソース別独自処理. <br>
	 * エンティティをリポジトリから取得し、物理削除します.
	 *
	 * @param resourceIdStr リソースID文字列
	 */
	@Override
	protected void deleteImpl(String resourceIdStr) {

		ScheduleBean removingEntity = findBean(resourceIdStr);

		repository.delete(removingEntity);
	}

	/**
	 * POSTメソッドのリソース別独自処理. <br>
	 * エンティティを新規生成、保存し、採番されたリソースIDを返します.
	 *
	 * @param newElement 生成内容を含むリソースエレメント
	 * @return 採番されたリソースID文字列
	 */
	@Override
	protected String postImpl(ScheduleResourceElement newElement) {

		// 既に存在するIDの場合、IDを振りなおす
		ScheduleBean newEntity = repository.exists(newElement.getScheduleId()) ? new ScheduleBean(generateNewId())
				: new ScheduleBean(newElement.getScheduleId());

		List<PersonBean> userIdBeans = new ArrayList<>();
		for (String userId : newElement.getUserIds()) {
			userIdBeans.add(personRepository.findOne(userId));
		}
		newEntity.setUserBeans(userIdBeans);
		newEntity.setTitle(newElement.getTitle());
		newEntity.setCategory(newElement.getCategory());
		// 日付リストの新規生成
		newEntity.setNewDateBeans(newElement.getDates());
		newEntity.setStartTime(newElement.getStartTime());
		newEntity.setFinishTime(newElement.getFinishTime());
		newEntity.setDetail(newElement.getDetail());
		newEntity.setPlace(newElement.getPlace());

		repository.save(newEntity);

		return generateNewResourceIdStr(newEntity.getScheduleId());
	}

	/**
	 * リソースID文字列から1件のエンティティをリポジトリを検索して取得します. <br>
	 * エンティティのIDは{@link this#resolveResourceId(String)}で与えられます.<br>
	 * 取得できない場合、{@link NotFoundException}をスローします.
	 *
	 * @param resourceIdStr リソースID文字列
	 * @return 予定エンティティ
	 * @throws NotFoundException IDからエンティティが取得できなかったとき
	 */
	private ScheduleBean findBean(String resourceIdStr) {
		String scheduleId = resolveResourceId(resourceIdStr);

		ScheduleBean found = repository.findOne(scheduleId);
		if (found == null) {
			throw new NotFoundException("entity not found :" + scheduleId);
		}
		return found;
	}

	/**
	 * この予定データのIDを文字列で採番します.
	 *
	 * @return ID文字列
	 */
	private String generateNewId() {

		return UUID.randomUUID().toString();
	}

	/**
	 * リソースIDからprefixを除去し、エンティティのIDを返します. <br>
	 *
	 * @param resourceIdStr リソースID文字列
	 * @return エンティティのID
	 */
	@Override
	protected String resolveResourceId(String resourceIdStr) {

		// クラス名を除去し、実データのID文字列を抽出する
		return resourceIdStr.replace(TARGET_ID_PREFIX, "");
	}

	/**
	 * エンティティのIDからリソースIDを生成します.<br>
	 * リソースID文字列はリソースごとの生成ルールによって生成された文字列で、リソースエレメントを一意に識別します.
	 *
	 * @param id エンティティのID
	 * @return リソースID
	 */
	@Override
	protected String generateNewResourceIdStr(String id) {

		// クラス名＋実データのID文字列で実データIDとする
		return TARGET_ID_PREFIX + id;
	}
}
