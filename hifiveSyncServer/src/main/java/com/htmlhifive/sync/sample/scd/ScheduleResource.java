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

import javax.annotation.Resource;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.htmlhifive.sync.commondata.CommonData;
import com.htmlhifive.sync.exception.DuplicateIdException;
import com.htmlhifive.sync.exception.NotFoundException;
import com.htmlhifive.sync.resource.AbstractSyncResource;
import com.htmlhifive.sync.resource.ClientResolvingStrategy;
import com.htmlhifive.sync.resource.OptimisticLockManager;
import com.htmlhifive.sync.resource.SyncResourceService;
import com.htmlhifive.sync.sample.person.Person;
import com.htmlhifive.sync.sample.person.PersonRepository;

/**
 * scheduleデータモデルの情報を同期リソースとして公開するためのリソースクラス.<br>
 * 同期データを専用サービスで管理する抽象リソースクラスをこのリソース用に実装します.
 *
 * @author kishigam
 */
@SyncResourceService(resourceName = "schedule", lockManager = OptimisticLockManager.class, updateStrategy = ClientResolvingStrategy.class)
@Transactional(propagation = Propagation.MANDATORY)
public class ScheduleResource extends AbstractSyncResource<ScheduleResourceItem> {

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
	 * 単一データreadメソッドのリソース別独自処理を行う抽象メソッド.<br>
	 * エンティティをリポジトリから取得し、アイテムクラスのオブジェクトに設定して返します.
	 *
	 * @param targetItemId 対象リソースアイテムのID
	 * @return リソースアイテム
	 */
	@Override
	protected ScheduleResourceItem doRead(String targetItemId) {

		Schedule bean = findSchedule(targetItemId);

		ScheduleResourceItem item = new ScheduleResourceItem(bean.getScheduleId());
		item.setUserIds(bean.getUserIds());
		item.setTitle(bean.getTitle());
		item.setCategory(bean.getCategory());
		item.setDates(bean.getDates());
		item.setStartTime(bean.getStartTime());
		item.setFinishTime(bean.getFinishTime());
		item.setDetail(bean.getDetail());
		item.setPlace(bean.getPlace());

		return item;
	}

	/**
	 * クエリによってリソースアイテムを取得する抽象メソッド.<br>
	 * 指定された共通データが対応するリソースアイテムであり、かつデータ項目が指定された条件に合致するものを検索し、返します.
	 *
	 * @param commonDataList 共通データリスト
	 * @param conditions 条件Map(データ項目名,データ項目の条件)
	 * @return 条件に合致するリソースアイテム(CommonDataを値として持つMap)
	 */
	@Override
	protected Map<ScheduleResourceItem, CommonData> doReadByQuery(List<CommonData> commonDataList,
			Map<String, String[]> conditions) {

		Map<ScheduleResourceItem, CommonData> itemMap = new HashMap<>();
		for (CommonData common : commonDataList) {

			ScheduleResourceItem item = doRead(common.getTargetItemId());

			// TODO: queryの適用

			itemMap.put(item, common);
		}

		return itemMap;
	}

	/**
	 * createメソッドのリソース別独自処理. <br>
	 * エンティティを新規生成、保存し、リソースアイテムのIDを返します.
	 *
	 * @param newItem 生成内容を含むリソースアイテム
	 * @return 採番されたリソースアイテムのID
	 */
	@Override
	protected String doCreate(ScheduleResourceItem newItem) throws DuplicateIdException {

		if (repository.exists(newItem.getScheduleId())) {

			throw new DuplicateIdException(newItem.getScheduleId(), doRead(newItem.getScheduleId()));
		}

		Schedule newEntity = new Schedule();
		newEntity.setScheduleId(newItem.getScheduleId());

		List<Person> userIdBeans = new ArrayList<>();
		for (String userId : newItem.getUserIds()) {
			userIdBeans.add(personRepository.findOne(userId));
		}
		newEntity.setUserBeans(userIdBeans);
		newEntity.setTitle(newItem.getTitle());
		newEntity.setCategory(newItem.getCategory());

		// 日付リストの新規生成
		newEntity.setNewDateBeans(newItem.getDates());

		newEntity.setStartTime(newItem.getStartTime());
		newEntity.setFinishTime(newItem.getFinishTime());
		newEntity.setDetail(newItem.getDetail());
		newEntity.setPlace(newItem.getPlace());

		repository.save(newEntity);

		return newEntity.getScheduleId();
	}

	/**
	 * updateメソッドのリソース別独自処理.<br>
	 *
	 * @param item 更新内容を含むリソースアイテム
	 */
	@Override
	protected void doUpdate(ScheduleResourceItem item) {

		Schedule updatingEntity = findSchedule(item.getScheduleId());

		List<Person> userIdBeans = updatingEntity.getUserBeans();
		userIdBeans.clear();
		for (String userId : item.getUserIds()) {
			userIdBeans.add(personRepository.findOne(userId));
		}
		updatingEntity.setUserBeans(userIdBeans);
		updatingEntity.setTitle(item.getTitle());
		updatingEntity.setCategory(item.getCategory());

		// 元の日付リストの更新
		updatingEntity.setUpdatedDateBeans(item.getDates());

		updatingEntity.setStartTime(item.getStartTime());
		updatingEntity.setFinishTime(item.getFinishTime());
		updatingEntity.setDetail(item.getDetail());
		updatingEntity.setPlace(item.getPlace());

		repository.save(updatingEntity);
	}

	/**
	 * deleteメソッドのリソース別独自処理. <br>
	 * エンティティをリポジトリから取得し、物理削除します.
	 *
	 * @param targetItemId リソースアイテムのID
	 */
	@Override
	protected void doDelete(String targetItemId) {

		Schedule removingEntity = findSchedule(targetItemId);

		repository.delete(removingEntity);
	}

	/**
	 * このリソースのリソースアイテムのIDから1件のエンティティをリポジトリを検索して取得します. <br>
	 * 取得できない場合、{@link NotFoundException}をスローします.
	 *
	 * @param scheduleId リソースアイテムのID
	 * @return Scheduleエンティティ
	 */
	private Schedule findSchedule(String scheduleId) {

		Schedule found = repository.findOne(scheduleId);
		if (found == null) {
			throw new NotFoundException("entity not found :" + scheduleId);
		}
		return found;
	}
}
