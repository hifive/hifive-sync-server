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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import com.htmlhifive.sync.resource.ResourceQuerySpecifications;
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
     * 日付書式変換のためのフォーマット文字列(整数8桁文字列)
     */
    private static final String FORMAT_STR_INT8 = "yyyyMMdd";

    /**
     * 日付書式変換のためのフォーマット文字列(スラッシュ区切りゼロ埋めなし)
     */
    private static final String FORMAT_STR_SLASH_SEPARATION = "y/M/d";

    /**
     * エンティティの永続化を担うリポジトリ.
     */
    @Resource
    private ScheduleRepository repository;

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
     * 単一データreadメソッドのリソース別独自処理を行う抽象メソッド.<br>
     * エンティティをリポジトリから取得し、アイテムクラスのオブジェクトに設定して返します.
     *
     * @param targetItemId
     *            対象リソースアイテムのID
     * @return リソースアイテム
     */
    @Override
    protected ScheduleResourceItem doGet(String targetItemId) {

        Schedule bean = findSchedule(targetItemId);

        ScheduleResourceItem item = entityToItem(bean);
        return item;
    }

    /**
     * クエリによってリソースアイテムを取得する抽象メソッド.<br>
     * 指定された共通データが対応するリソースアイテムであり、かつデータ項目が指定された条件に合致するものを検索し、返します.
     *
     * @param commonDataList
     *            共通データリスト
     * @param conditions
     *            条件Map(データ項目名,データ項目の条件)
     * @return 条件に合致するリソースアイテム(CommonDataを値として持つMap)
     */
    @Override
    protected Map<ScheduleResourceItem, ResourceItemCommonData> doGetByQuery(
            List<ResourceItemCommonData> commonDataList,
            Map<String, String[]> conditions) {

        Map<ScheduleResourceItem, ResourceItemCommonData> itemMap = new HashMap<>();

        // Specificationsを用いたクエリ実行
        List<Schedule> scheduleList =
                repository.findAll(querySpec.parseConditions(commonDataList, conditions));

        for (Schedule schedule : scheduleList) {
            for (ResourceItemCommonData common : commonDataList) {
                if (common.getTargetItemId().equals(schedule.getScheduleId())) {
                    ScheduleResourceItem item = entityToItem(schedule);
                    itemMap.put(item, common);
                }
            }
        }

        return itemMap;
    }

    /**
     * エンティティの持つ情報からリソースアイテムオブジェクトに値を設定します.
     *
     * @param bean
     *            Scheduleエンティティ
     */
    private ScheduleResourceItem entityToItem(Schedule bean) {

        DateFormat slashSeparation = new SimpleDateFormat(FORMAT_STR_SLASH_SEPARATION);
        DateFormat int8 = new SimpleDateFormat(FORMAT_STR_INT8);

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
            dates.add(convertDateFormat(int8, slashSeparation, String.valueOf(date.getDate())));
        }
        item.setDates(dates);

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
     * @param newItem
     *            生成内容を含むリソースアイテム
     * @return 採番されたリソースアイテムのID
     */
    @Override
    protected String doCreate(ScheduleResourceItem newItem) throws DuplicateIdException {

        if (repository.exists(newItem.getScheduleId())) {

            throw new DuplicateIdException(newItem.getScheduleId(), doGet(newItem.getScheduleId()));
        }

        Schedule newEntity = new Schedule(newItem.getScheduleId());

        itemToEntity(newEntity, newItem);

        // 作成者にログインユーザーであるPersonをセット
        newEntity.setCreateUser(loginUser());

        repository.save(newEntity);

        return newEntity.getScheduleId();
    }

    /**
     * updateメソッドのリソース別独自処理.<br>
     *
     * @param item
     *            更新内容を含むリソースアイテム
     * @return 更新されたリソースアイテム
     */
    @Override
    protected ScheduleResourceItem doUpdate(ScheduleResourceItem item) {

        Schedule updatingEntity = findSchedule(item.getScheduleId());

        itemToEntity(updatingEntity, item);

        repository.save(updatingEntity);

        item.setCreateUserName(updatingEntity.getCreateUser().getName());
        return item;
    }

    /**
     * リソースアイテムの内容をScheduleエンティティに設定します.<br>
     *
     * @param entity
     *            Scheduleオブジェクト(Entity)
     * @param item
     *            Scheduleリソースアイテムオブジェクト
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
     * @param entityUserBeans
     *            エンティティのユーザーリスト(Personエンティティのリスト)
     * @param itemUserIds
     *            リソースアイテムのユーザーリスト(IDのリスト)
     * @return 反映後のユーザーリスト(Personエンティティのリスト)
     */
    private List<Person> itemUsersToEntityUsers(
            List<Person> entityUserBeans,
            List<String> itemUserIds) {

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
     * @param entity
     *            Scheduleエンティティ
     * @param itemDates
     * @return
     */
    private List<ScheduleDate> itemDatesToEntityDates(Schedule entity, List<String> itemDates) {

        DateFormat slashSeparation = new SimpleDateFormat(FORMAT_STR_SLASH_SEPARATION);
        DateFormat int8 = new SimpleDateFormat(FORMAT_STR_INT8);

        List<ScheduleDate> resultDatesList = new ArrayList<>();

        Iterator<String> dateItr = itemDates.iterator();
        for (ScheduleDate dateBean : entity.getDateBeans()) {

            // 日付のマッチングは行わず、取得順に再設定する
            if (dateItr.hasNext()) {
                ScheduleDate oldDateBean = dateBean;
                oldDateBean.setDate(Integer.parseInt(convertDateFormat(
                        slashSeparation, int8, dateItr.next())));
                resultDatesList.add(oldDateBean);
            }
        }

        // 元より多い分は新規
        while (dateItr.hasNext()) {
            resultDatesList.add(new ScheduleDate(entity, Integer.parseInt(convertDateFormat(
                    slashSeparation, int8, dateItr.next()))));
        }
        return resultDatesList;
    }

    /**
     * 日付フォーマットを変換します.
     *
     * @param from
     *            変換前日付フォーマット
     * @param to
     *            変換後日付フォーマット
     * @param dateStr
     *            変換前日付文字列
     * @return 変換後日付文字列
     */
    private String convertDateFormat(DateFormat from, DateFormat to, String dateStr) {

        String resultStr;
        try {
            resultStr = to.format(from.parse(dateStr));
        } catch (ParseException e) {
            throw new SyncException(e);
        }

        return resultStr;
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
            username = ((UserDetails)principal).getUsername();
        } else {
            username = principal.toString();
        }

        return personRepository.findOne(username);
    }

    /**
     * deleteメソッドのリソース別独自処理. <br>
     * 論理削除のため、エンティティは変更せずにIDのみ設定された空のリソースアイテムを返します.
     *
     * @param targetItemId
     *            リソースアイテムのID
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
     * @param scheduleId
     *            リソースアイテムのID
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
     * @param personId
     *            PersonのID
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
