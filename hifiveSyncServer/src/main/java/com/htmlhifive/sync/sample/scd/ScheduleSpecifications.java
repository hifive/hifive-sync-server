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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import com.htmlhifive.sync.common.ResourceItemCommonData;

/**
 * Scheduleをリソースクエリによって検索するためのSpring Specification定義クラス.
 */
public class ScheduleSpecifications {

	/**
	 * 「指定された共通データが対応するリソースアイテムであり、かつデータ項目が指定された条件に合致する」というクエリ仕様を表現します.
	 *
	 * @param commonDataList リソースアイテム共通データ
	 * @param conditions クエリ条件
	 * @return Specificationsオブジェクト
	 */
	public static Specifications<Schedule> parseConditions(List<ResourceItemCommonData> commonDataList,
			Map<String, String[]> conditions) {

		List<Specification<Schedule>> specList = new ArrayList<>();

		List<String> targetItemIdList = new ArrayList<>();
		for (ResourceItemCommonData common : commonDataList) {
			targetItemIdList.add(common.getTargetItemId());
		}
		if (!targetItemIdList.isEmpty()) {
			specList.add(isInIds(targetItemIdList.toArray(new String[] {})));
		}

		Specification<Schedule> tempSpec;
		for (String cond : conditions.keySet()) {
			switch (cond) {
				case ("sceduleId"):
					specList.add(isInIds(conditions.get(cond)));
					break;
				case ("personIds"):
					specList.add(isInPersonIds(conditions.get(cond)));
					break;
				//				case ("date-from"):
				//					tempSpec = isGtOrEqSomeDates(conditions.get(cond)[0]);
				//					if (tempSpec != null) {
				//						specList.add(tempSpec);
				//					}
				// ・・・

				default:
					// 何もしない
			}
		}

		if (specList.isEmpty()) {
			return null;
		}

		Specifications<Schedule> specs = Specifications.where(specList.get(0));
		for (Specification<Schedule> spec : specList.subList(1, specList.size())) {

			specs = specs.and(spec);
		}

		return specs;
	}

	/**
	 * 配列に含むIDのいずれかに合致するScheduleを抽出するSpecificationを返します.
	 *
	 * @param ids IDの配列
	 * @return Specificationオブジェクト
	 */
	public static Specification<Schedule> isInIds(final String... ids) {
		return new Specification<Schedule>() {

			@Override
			public Predicate toPredicate(Root<Schedule> root, CriteriaQuery<?> cq, CriteriaBuilder builder) {

				return root.get(root.getModel().getSingularAttribute("scheduleId", String.class)).in((Object[]) ids);
			}
		};
	}

	/**
	 * 配列に含むpersonIdのいずれかがpersonIdsのいずれかに合致するScheduleを抽出するSpecificationを返します.
	 *
	 * @param personIds personIdの配列
	 * @return Specificationオブジェクト
	 */
	public static Specification<Schedule> isInPersonIds(final String[] personIds) {
		return new Specification<Schedule>() {

			@Override
			public Predicate toPredicate(Root<Schedule> root, CriteriaQuery<?> cq, CriteriaBuilder builder) {

				return ((Join<?, ?>) root.fetch("userBeans")).get("personId").in((Object[]) personIds);
			}
		};
	}

	//	/**
	//	 * 指定された日付以降の予定日を持つScheduleを抽出するSpecificationを返します.
	//	 *
	//	 * @param dateStr 日付文字列(スラッシュ区切り、「日」省略可)
	//	 * @return Specificationオブジェクト
	//	 */
	//	public static Specification<Schedule> isGtOrEqSomeDates(final String dateStr) {
	//		return new Specification<Schedule>() {
	//			@Override
	//			public Predicate toPredicate(Root<Schedule> root, CriteriaQuery<?> cq, CriteriaBuilder builder) {
	//
	//				Date fromDate = parseWithFirstDate(dateStr);
	//
	//
	//
	//
	//			}
	//		};
	//
	//	}

	private static Date parseWithFirstDate(String dateStr) {

		String[] dateArray = dateStr.split("/");

		if (dateArray.length < 1) {
			return null;
		}

		Calendar cal = Calendar.getInstance();
		cal.clear();

		cal.set(Calendar.YEAR, Integer.parseInt(dateArray[0]));
		cal.set(Calendar.MONTH,
				dateArray.length >= 2 ? Integer.parseInt(dateArray[1]) - 1 : cal.getActualMinimum(Calendar.MONTH));
		cal.set(Calendar.DATE,
				dateArray.length >= 3 ? Integer.parseInt(dateArray[2]) : cal.getActualMinimum(Calendar.DATE));

		return cal.getTime();
	}

	private static Date parseWithEndDate(String dateStr) {

		String[] dateArray = dateStr.split("/");

		if (dateArray.length < 1) {
			return null;
		}

		Calendar cal = Calendar.getInstance();
		cal.clear();

		cal.set(Calendar.YEAR, Integer.parseInt(dateArray[0]));
		cal.set(Calendar.MONTH,
				dateArray.length >= 2 ? Integer.parseInt(dateArray[1]) - 1 : cal.getActualMaximum(Calendar.MONTH));
		cal.set(Calendar.DATE,
				dateArray.length >= 3 ? Integer.parseInt(dateArray[2]) : cal.getActualMaximum(Calendar.DATE));

		return cal.getTime();
	}
}
