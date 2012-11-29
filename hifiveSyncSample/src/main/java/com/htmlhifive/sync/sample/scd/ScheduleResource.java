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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.htmlhifive.sync.exception.BadRequestException;
import com.htmlhifive.sync.exception.DuplicateIdException;
import com.htmlhifive.sync.resource.AbstractSyncResource;
import com.htmlhifive.sync.resource.ResourceQuerySpecifications;
import com.htmlhifive.sync.resource.SyncResourceService;
import com.htmlhifive.sync.resource.update.ClientResolvingStrategy;
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
	 * 新規データの永続化に使用するEntityManager.
	 *
	 * @see {@link this#doCreate(ScheduleResourceItem)}
	 */
	@PersistenceContext
	private EntityManager entitymanager;

	/**
	 * エンティティのクエリ仕様.
	 */
	@Resource(type = ScheduleQuerySpecifications.class)
	private ResourceQuerySpecifications<Schedule> querySpec;

	/**
	 * 関連するPersonエンティティを取得するためのリポジトリ.
	 */
	@Resource
	private PersonRepository personRepository;

	/**
	 * データ取得メソッドのリソース別独自処理を行う抽象メソッド.<br>
	 * 与えられた識別子が示すリソースアイテムを返します.
	 *
	 * @param ids リソースアイテムの識別子(複数可)
	 * @return リソースアイテム(識別子をkeyとするMap)
	 */
	@Override
	protected Map<String, ScheduleResourceItem> doGet(String... ids) {

		Map<String, ScheduleResourceItem> itemMap = new HashMap<>();
		for (String scheduleId : ids) {

			Schedule scheduleEntity = findSchedule(scheduleId);
			itemMap.put(scheduleId, entityToItem(scheduleEntity));
		}

		return itemMap;
	}

	/**
	 * クエリによってリソースアイテムを取得する抽象メソッド.<br>
	 * 与えられた識別子が示すアイテムの中で、データ項目が指定された条件に合致するものを返します.
	 *
	 * @param conditions 条件Map(データ項目名,データ項目の条件)
	 * @param ids リソースアイテムの識別子(複数可)
	 * @return 条件に合致するリソースアイテム(アイテム識別子をKeyとするMap)
	 */
	@Override
	protected Map<String, ScheduleResourceItem> doGetByQuery(Map<String, String[]> conditions, String... ids) {

		Map<String, ScheduleResourceItem> itemMap = new HashMap<>();

		// Specificationsを用いたクエリ実行
		List<Schedule> scheduleList = repository.findAll(querySpec.parseConditions(conditions, ids));

		for (Schedule schedule : scheduleList) {
			ScheduleResourceItem item = entityToItem(schedule);
			itemMap.put(item.getScheduleId(), item);
		}

		return itemMap;
	}

	/**
	 * エンティティの持つ情報からリソースアイテムオブジェクトに値を設定します.
	 *
	 * @param bean Scheduleエンティティ
	 */
	private ScheduleResourceItem entityToItem(Schedule bean) {

		ScheduleDateFormatConverter formatConverter = new ScheduleDateFormatConverter(
				ScheduleDateFormatConverter.FORMAT_INT8, ScheduleDateFormatConverter.FORMAT_SLASH_SEPARATION);

		ScheduleResourceItem item = new ScheduleResourceItem(bean.getScheduleId());

		List<String> userIds = new ArrayList<>();
		for (Person user : bean.getUserBeans()) {
			userIds.add(user.getPersonId());
		}
		item.setUserIds(userIds);

		item.setTitle(bean.getTitle());
		item.setCategory(bean.getCategory());

		List<String> dates = new ArrayList<>();
		for (ScheduleDate date : bean.getDateBeans()) {
			dates.add(formatConverter.convertFormat(String.valueOf(date.getDate())));
		}
		item.setDates(dates);

		item.setStartTime(bean.getStartTime());
		item.setFinishTime(bean.getFinishTime());
		item.setDetail(bean.getDetail());
		item.setPlace(bean.getPlace());

		String createUserName = bean.getCreateUser() == null ? "" : bean.getCreateUser().getName();
		item.setCreateUserName(createUserName);

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

		String scheduleId = newItem.getScheduleId();

		Schedule newEntity = new Schedule(scheduleId);

		// 作成者にログインユーザーであるPersonをセット
		newEntity.setCreateUser(loginUser());

		itemToEntity(newEntity, newItem);

		// 以下は、既に採番されたIDでcreateを行うために必要
		// リソースが一意なIDを並行性を考慮して採番するのであれば不要
		try {
			// JPARepository#saveではキー重複の時EntityManager#mergeが呼ばれてしまう可能性があるため、EntityManager#persistを使用する
			// また、flushしないとトランザクションコミット時まで一意制約違反が遅れて発生してしまうためここでflushする
			entitymanager.persist(newEntity);
			entitymanager.flush();

		} catch (PersistenceException e) {

			// キー重複
			// レスポンスに既存アイテムのIDや内容を渡すために例外オブジェクトにセット
			throw new DuplicateIdException(scheduleId, doGet(scheduleId).get(scheduleId));
		}

		return newEntity.getScheduleId();
	}

	/**
	 * updateメソッドのリソース別独自処理.<br>
	 *
	 * @param item 更新内容を含むリソースアイテム
	 * @return リソースアイテムの識別子
	 */
	@Override
	protected String doUpdate(ScheduleResourceItem item) {

		Schedule updatingEntity = findSchedule(item.getScheduleId());

		itemToEntity(updatingEntity, item);

		repository.save(updatingEntity);

		return item.getScheduleId();
	}

	/**
	 * リソースアイテムの内容をScheduleエンティティに設定します.<br>
	 *
	 * @param entity Scheduleオブジェクト(Entity)
	 * @param item Scheduleリソースアイテムオブジェクト
	 */
	private void itemToEntity(Schedule entity, ScheduleResourceItem item) {

		// userBeansの関連の更新
		entity.setUserBeans(itemUsersToEntityUsers(entity.getUserBeans(), item.getUserIds()));

		entity.setTitle(item.getTitle());
		entity.setCategory(item.getCategory());

		// DateBeansの関連の更新
		entity.setDateBeans(itemDatesToEntityDates(entity, item.getDates()));

		entity.setStartTime(item.getStartTime());
		entity.setFinishTime(item.getFinishTime());
		entity.setDetail(item.getDetail());
		entity.setPlace(item.getPlace());
	}

	/**
	 * リソースアイテムのユーザーリストの内容をエンティティのユーザーリストに反映します.<br>
	 * エンティティのユーザーリストに既に含むものはそのままとし、足りないユーザーのPersonエンティティを取得して加えます.
	 *
	 * @param entityUserBeans エンティティのユーザーリスト(Personエンティティのリスト)
	 * @param itemUserIds リソースアイテムのユーザーリスト(IDのリスト)
	 * @return 反映後のユーザーリスト(Personエンティティのリスト)
	 */
	private List<Person> itemUsersToEntityUsers(List<Person> entityUserBeans, List<String> itemUserIds) {

		List<Person> resultUserList = new ArrayList<>();

		// 足りないユーザーを抽出するためリストのコピーを作成
		List<String> userListInItem = new ArrayList<>(itemUserIds);

		// 既存のエンティティリストに含まれるものはそのまま新しいリストに含め、コピーから削除
		for (Person existingPerson : entityUserBeans) {
			if (itemUserIds.contains(existingPerson.getPersonId())) {
				resultUserList.add(existingPerson);
				userListInItem.remove(existingPerson.getPersonId());
			}
		}

		// コピーに残ったIDは既存のエンティティリストに含まないため、リポジトリから取得して新しいリストに含める
		for (String newPersonId : userListInItem) {
			resultUserList.add(findRelatedPerson(newPersonId));
		}

		return resultUserList;
	}

	/**
	 * リソースアイテムの予定日付リストの内容をエンティティの予定日付リストに反映します.<br>
	 * エンティティのユーザーリストに既に含むものはそのままとし、足りない日付のScheduleDateエンティティを取得して加えます.
	 *
	 * @param entity Scheduleエンティティ
	 * @param itemDates
	 * @return
	 */
	private List<ScheduleDate> itemDatesToEntityDates(Schedule entity, List<String> itemDates) {

		ScheduleDateFormatConverter formatConverter = new ScheduleDateFormatConverter(
				ScheduleDateFormatConverter.FORMAT_SLASH_SEPARATION, ScheduleDateFormatConverter.FORMAT_INT8);

		List<ScheduleDate> resultDatesList = new ArrayList<>();

		Iterator<String> dateItr = itemDates.iterator();
		for (ScheduleDate dateBean : entity.getDateBeans()) {

			// 日付のマッチングは行わず、取得順に再設定する
			if (dateItr.hasNext()) {
				ScheduleDate oldDateBean = dateBean;
				oldDateBean.setDate(Integer.parseInt(formatConverter.convertFormat(dateItr.next())));
				resultDatesList.add(oldDateBean);
			}
		}

		// 元より多い分は新規
		while (dateItr.hasNext()) {
			resultDatesList.add(new ScheduleDate(entity,
					Integer.parseInt(formatConverter.convertFormat(dateItr.next()))));
		}
		return resultDatesList;
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

	/**
	 * deleteメソッドのリソース別独自処理. <br>
	 * 論理削除のため、エンティティは変更せずにIDのみ設定された空のリソースアイテムを返します.
	 *
	 * @param targetItemId リソースアイテムのID
	 * @return 削除されたアイテムを表すリソースアイテムオブジェクト
	 */
	@Override
	protected ScheduleResourceItem doDelete(String targetItemId) {

		findSchedule(targetItemId);
		return new ScheduleResourceItem(targetItemId);
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
	 * このリソースのリソースアイテムと関連するPersonのIDから1件のエンティティをリポジトリを検索して取得します. <br>
	 *
	 * @param personId PersonのID
	 * @return Personエンティティ
	 */
	private Person findRelatedPerson(String personId) {
		Person found = personRepository.findOne(personId);
		if (found == null) {
			throw new BadRequestException("entity not found :" + personId);
		}
		return found;
	}
}
