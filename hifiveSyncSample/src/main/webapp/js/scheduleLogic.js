(function() {	
	
	function ScheduleLogic() {
		this.scheduleDataModel = scheduleSample.data.manager.models.schedule;
		this.personDataModel = scheduleSample.data.manager.models.person;
		this.syncManager = scheduleSample.sync.manager;
		this.loginPersonId = null;
		
		this.syncManager.addEventListener('resolveDuplicateId', function(event) {
			var updateIds = event.updateIdLog.schedule;
			
			if (!updateIds) {
				return;
			}
			
			for (var i=0, len=updateIds.length; i<len; i++) {
				var item = this.scheduleDataModel.get(updateIds[i]);
				var userIds = item.get('userIds');
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
		
		regist: function(schedule) {
			// スケジュールIDの生成
			schedule.scheduleId = this.syncManager.getGlobalItemId(this.scheduleDataModel);	
			this._removeEmptyUserIds(schedule);
			this.scheduleDataModel.create(schedule);
	
			return this.syncManager.sync();
		},
	
		update: function(schedule, scheduleId) {
			var item = this.scheduleDataModel.get(scheduleId);
			this._removeEmptyUserIds(schedule);
			item.set(schedule);
	
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