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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.htmlhifive.sync.common.ResourceItemCommonData;
import com.htmlhifive.sync.exception.BadRequestException;
import com.htmlhifive.sync.exception.DuplicateIdException;
import com.htmlhifive.sync.exception.SyncException;
import com.htmlhifive.sync.resource.AbstractSyncResource;
import com.htmlhifive.sync.resource.ClientResolvingStrategy;
import com.htmlhifive.sync.resource.SyncResourceService;
import com.htmlhifive.sync.sample.person.Person;
import com.htmlhifive.sync.sample.person.PersonRepository;

/**
 * Scheduleリソースの情報を同期リソースとして公開するためのリソースクラス.<br>
 * 同期データを専用サービスで管理する抽象リソースクラスをこのリソース用に実装します.
 *
 * @author kishigam
 */
@SyncResourceService(resourceName = "schedule", updateStrategy = ClientResolvingStrategy.class)
public class ScheduleResource extends AbstractSyncResource<ScheduleResourceItem> {

	/**
	 * エンティティの永続化を担うリポジトリ.
	 */
	@Resource
	private ScheduleRepository repository;

	/**
	 * 関連するPersonエンティティを取得するためのリポジトリ.
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
	protected ScheduleResourceItem doGet(String targetItemId) {

		Schedule bean = findSchedule(targetItemId);

		ScheduleResourceItem item = createItem(bean);
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
	protected Map<ScheduleResourceItem, ResourceItemCommonData> doGetByQuery(
			List<ResourceItemCommonData> commonDataList, Map<String, String[]> conditions) {

		Map<ScheduleResourceItem, ResourceItemCommonData> itemMap = new HashMap<>();

		// Specificationsを用いたクエリ実行
		List<Schedule> scheduleList = repository.findAll(ScheduleSpecifications.parseConditions(commonDataList,
				conditions));

		for (Schedule schedule : scheduleList) {
			for (ResourceItemCommonData common : commonDataList) {
				if (common.getTargetItemId().equals(schedule.getScheduleId())) {
					ScheduleResourceItem item = createItem(schedule);
					itemMap.put(item, common);
				}
			}
		}

		return itemMap;
	}

	/**
	 * エンティティの持つ情報からリソースアイテムオブジェクトに値を設定します.
	 *
	 * @param bean Scheduleエンティティ
	 */
	private ScheduleResourceItem createItem(Schedule bean) {

		ScheduleResourceItem item = new ScheduleResourceItem(bean.getScheduleId());

		item.setUserIds(bean.getUserIds());
		item.setTitle(bean.getTitle());
		item.setCategory(bean.getCategory());

		// スラッシュ区切りに変換
		List<String> slashSeparatedList = new ArrayList<>();
		try {
			for (String date : bean.getDates()) {
				slashSeparatedList.add(new SimpleDateFormat("y/M/d").format(new SimpleDateFormat("yyyyMMdd")
						.parse(date)));
			}
		} catch (ParseException e) {
			throw new SyncException(e);
		}
		item.setDates(bean.getDates());
		item.setStartTime(bean.getStartTime());
		item.setFinishTime(bean.getFinishTime());
		item.setDetail(bean.getDetail());
		item.setPlace(bean.getPlace());
		item.setCreateUserName(bean.getCreateUser().getName());

		return item;
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

			throw new DuplicateIdException(newItem.getScheduleId(), doGet(newItem.getScheduleId()));
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

		// 日付文字列書式を変換してセット
		newEntity.setDates(removeDateSeparator(newItem.getDates()));

		newEntity.setStartTime(newItem.getStartTime());
		newEntity.setFinishTime(newItem.getFinishTime());
		newEntity.setDetail(newItem.getDetail());
		newEntity.setPlace(newItem.getPlace());

		// 作成者にログインユーザーであるPersonをセット
		newEntity.setCreateUser(loginUser());

		repository.save(newEntity);

		return newEntity.getScheduleId();
	}

	/**
	 * updateメソッドのリソース別独自処理.<br>
	 *
	 * @param item 更新内容を含むリソースアイテム
	 */
	@Override
	protected ScheduleResourceItem doUpdate(ScheduleResourceItem item) {

		Schedule updatingEntity = findSchedule(item.getScheduleId());

		List<Person> userIdBeans = updatingEntity.getUserBeans();
		userIdBeans.clear();
		for (String userId : item.getUserIds()) {
			userIdBeans.add(personRepository.findOne(userId));
		}
		updatingEntity.setUserBeans(userIdBeans);
		updatingEntity.setTitle(item.getTitle());
		updatingEntity.setCategory(item.getCategory());

		// 日付文字列書式を変換してセット
		updatingEntity.setDates(removeDateSeparator(item.getDates()));

		updatingEntity.setStartTime(item.getStartTime());
		updatingEntity.setFinishTime(item.getFinishTime());
		updatingEntity.setDetail(item.getDetail());
		updatingEntity.setPlace(item.getPlace());

		repository.save(updatingEntity);

		item.setCreateUserName(updatingEntity.getCreateUser().getName());
		return item;
	}

	/**
	 * 日付文字列のセパレータ(スラッシュ)を除去し、8桁文字列に変換します.
	 *
	 * @param newItem
	 * @return
	 */
	private List<String> removeDateSeparator(List<String> slashSeparatedList) {
		List<String> nonSeparatedList = new ArrayList<>();
		try {
			for (String date : slashSeparatedList) {
				nonSeparatedList
						.add(new SimpleDateFormat("yyyyMMdd").format(new SimpleDateFormat("y/M/d").parse(date)));
			}
		} catch (ParseException e) {
			throw new SyncException(e);
		}
		return nonSeparatedList;
	}

	/**
	 * deleteメソッドのリソース別独自処理. <br>
	 * エンティティをリポジトリから取得し、物理削除します.
	 *
	 * @param targetItemId リソースアイテムのID
	 */
	@Override
	protected ScheduleResourceItem doDelete(String targetItemId) {

		Schedule removingEntity = findSchedule(targetItemId);

		repository.delete(removingEntity);

		return ScheduleResourceItem.emptyItem(targetItemId);
	}

	/**
	 * このリソースのリソースアイテムのIDから1件のエンティティをリポジトリを検索して取得します. <br>
	 *
	 * @param scheduleId リソースアイテムのID
	 * @return Scheduleエンティティ
	 */
	private Schedule findSchedule(String scheduleId) {

		Schedule found = repository.findOne(scheduleId);
		if (found == null) {
			throw new BadRequestException("entity not found :" + scheduleId);
		}
		return found;
	}

	/**
	 * 認証情報からログインユーザーIDを取得します.
	 *
	 * @return ログインユーザーID.
	 */
	private Person loginUser() {

		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		String username;
		if (principal instanceof UserDetails) {
			username = ((UserDetails) principal).getUsername();
		} else {
			username = principal.toString();
		}

		return personRepository.findOne(username);
	}
}
