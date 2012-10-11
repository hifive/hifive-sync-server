(function(){
	function PersonLogic() {
		this.personDataModel = scheduleSample.data.manager.models.person;
		this.syncManager = scheduleSample.sync.manager;
	}
	
	PersonLogic.prototype = {
			
		init: function() {
			var that = this;
	
			// 同期対象のリソースを定義
			this.syncManager.addQueries({
				modelName: that.personDataModel.name,
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