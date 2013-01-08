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
(function() {

	function ScheduleLogic() {
		this.scheduleDataModel = scheduleSample.data.manager.models.schedule;
		this.personDataModel = scheduleSample.data.manager.models.person;
		this.syncManager = scheduleSample.sync.manager;
		this.loginPersonId = null;

		this.scheduleDataModel.addEventListener('resolveDuplicateId', function(event) {
			var updateItems = event.updateItems;

			if (!updateItems) {
				return;
			}

			for (var i=0, len=updateItems.length; i<len; i++) {
				var userIds = updateItems[i].userIds;
				var index = userIds.indexOf(event.oldId);
				if (index !== -1) {
					userIds[index] = event.newId;
				}
			}
		});
	}

	ScheduleLogic.prototype = {

		init: function() {
			var dfd = h5.async.deferred();

			if (h5.api.storage.local.isSupported) {
				this.loginPersonId = h5.api.storage.local.getItem('loginId');
				if (this.loginPersonId) {
					that._addQuery();
					dfd.resolve();
					return dfd.promise();
				}
			}

			var options = {
					type: 'get',
					contentType: 'application/json',
					url: this.syncManager.baseUrl + '/person',
			};

			var that = this;
			this.syncManager.ajax(options).done(function(data) {
				that.loginPersonId = data[that.personDataModel.idKey];

				// 同期対象のリソースを定義
				// TODO: リソースの順番を指定しなければいけない
				// person -> scheduleの順に指定しないと、scheduleのusersにインスタンスが伝わらない
				that._addQuery();

				if (h5.api.storage.isSupported) {
					h5.api.storage.local.setItem('personId', that.loginPersonId);
				}
				dfd.resolve();
			}).fail(function(obj){
				alert('ネットワークに接続できません');
				dfd.reject();
			});
			return dfd.promise();
		},

		_addQuery: function() {
			this.syncManager.addQueries({
				modelName: this.scheduleDataModel.name,
				conditions: {
					'userOrCreator': [this.loginPersonId]
				}
			});
		},

		regist: function(schedule, isConflict) {
			// スケジュールIDの生成
			schedule.scheduleId = this.syncManager.getGlobalItemId(this.scheduleDataModel);
			this._removeEmptyUserIds(schedule);
			this.scheduleDataModel.create(schedule);

			if(isConflict) {
				this.syncManager.setAsResolved(scheduleId, this.scheduleDataModel.name);
			}

			return this.syncManager.sync();
		},

		update: function(schedule, scheduleId, isConflict) {
			var item = this.scheduleDataModel.get(scheduleId);
			this._removeEmptyUserIds(schedule);
			item.set(schedule);

			if(isConflict) {
				this.syncManager.setAsResolved(scheduleId, this.scheduleDataModel.name);
			}

			return this.syncManager.sync();
		},

		deleteSchedule: function(scheduleId) {
			this.scheduleDataModel.remove(scheduleId);
			return this.syncManager.sync();
		},

		_removeEmptyUserIds: function(schedule) {
			for (var i=0, len=schedule.userIds.length; i<len; ) {
				if (schedule.userIds[i] === '') {
					schedule.userIds.splice(i,1);
					len--;
				} else {
					i++;
				}
			}
		}
	};

	h5.u.obj.expose('scheduleSample.logic' , {
		 ScheduleLogic: ScheduleLogic
	});
}());