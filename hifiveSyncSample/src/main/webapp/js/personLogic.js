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
(function(){
	function PersonLogic() {
		this.personDataModel = scheduleSample.data.manager.models.person;
		this.syncManager = scheduleSample.sync.manager;
	}

	PersonLogic.prototype = {

		init: function() {
			var modelName = this.personDataModel.name;

			// 同期対象のリソースを定義
			this.syncManager.addQueries({
				modelName: modelName,
				// 指定しない(すべてのデータを取得)
				// あとで変更可能
				conditions: {}
			});
		},

		regist: function(person, oldId) {
			if(oldId) {
				// FWにid重複を解消したことを通知する
				this.syncManager.resolveDuplicate(person.personId, oldId, this.personDataModel);
				delete person.oldId;
			}
			this.personDataModel.create(person);
			return this.syncManager.sync();
		},

		update: function(person) {
			this.save(person);
			return this.syncManager.sync();
		},

		deletePerson: function(personId) {
			this.personDataModel.remove(personId);
			return this.syncManager.sync();
		},

		save: function(person) {
			var item = this.personDataModel.get(person.personId);
			delete person.personId;
			item.set(person);
		}
	};

	h5.u.obj.expose('scheduleSample.logic' , {
		 PersonLogic: PersonLogic
	});

}());