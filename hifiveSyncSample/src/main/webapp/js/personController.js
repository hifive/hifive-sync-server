$(function() {

	/**
	 * personモデルのディスクリプタ
	 */
	var PERSON_DESCRIPTOR = {
			name: 'person',
			schema: {
				personId: {
					id: true,
					type: 'string'
				},

				name: {
					type: 'string'
				},

				age: {
					type: 'integer'
				},

				organization: {
					type: 'string'
				}
			}
		};

	//DataModelの登録は初期ロード時に一度行えばよい
	scheduleSample.data.manager.createModel(PERSON_DESCRIPTOR);

	var personController = {
		__name: 'PersonController',

		logic: null,

		personDataModel: scheduleSample.data.manager.models.person,

		// TODO ダイアログの処理は共通化するべき
		/**
		 * ダイアログからダイアログへ遷移したときに遷移元を保存しておく
		 */
		$fromDialog: null,

		// TODO キャッシュから取ってきてほしくないのでリクエストパラメータ指定している
		__templates: ['template/person.ejs?' + new Date().getTime()],

		$content: null,

		__init: function(context) {
			this.logic = new scheduleSample.logic.PersonLogic();
			this.logic.init();

			var that = this;

			// conflictのイベントリスナーを登録
			scheduleSample.sync.manager.addEventListener('conflict', function(event) {
				// 今回競合した分のデータを競合データリストに入れる
				// TODO: syncManagerに持たせる？
				var conflictItems = event.conflicted[that.personDataModel.name];
				if (!conflictItems) {
					return;
				}
				for (var i=0, len=conflictItems['changed'].length; i<len; i++) {
					that.logic.save(conflictItems['changed'][i].serverItem);
				}
				for (var i=0, len=conflictItems['removed'].length; i<len; i++) {
					this.personDataModel.remove(conflictItems['removed'][i].serverItem.personId);
				}
				alert('ユーザのデータが更新されていました。確認後、再度更新しなおしてください');				
			});
			
			scheduleSample.sync.manager.addEventListener('duplicateId', function(event) {
				// 今回競合した分のデータを競合データリストに入れる
				var conflictItems = event.conflicted[that.personDataModel.name];
				if (!conflictItems) {
					return;
				}
				alert('IDがすでに使用されています。他のIDを使用してください。');
				scheduleSample.common.showDialog(that.view.get('conflict', {
					conflictItem: conflictItems[0]
				}), {
					top: '0'
				});
			});

		},

		__ready: function(context) {
			this.$content = this.$find('.content');
		},

		/**
		 * TopControllerからwindow.resize時にtriggerされるイベント
		 */
		'{rootElement} _resize': function() {
			this.adjustUserLineSize();
		},

		'{rootElement} _plot': function(context) {
			this.plotPerson();
		},
		
		/**
		 * 氏名をクリック
		 */
		'ul.person li click': function(context) {
			var id = $(context.event.target).parent().find('input[name="id"]').val();
			var person = this.personDataModel.get(id);
			scheduleSample.common.showDialog(this.view.get('detail', {
				person: person
			}), {
				top: '0'
			});
		},

		/**
		 * ユーザ表示エリアのサイズ調整
		 */
		adjustUserLineSize: function() {
			var height = $(this.rootElement).height()
					- (this.$find('#personline').offset().top - $(this.rootElement).offset().top);
			this.$find('#personline').height(height);
		},

		/**
		 * ダイアログのユーザ登録ボタンをクリック
		 */
		'{#person_regist button.submit} click': function() {
			var $dialog = $('#person_regist');
			var obj = this.getDialogInput($dialog);
			this.regist(obj, obj.oldId);
		},

		getDialogInput: function($dialog) {
			var age = $dialog.find('input[name="age"]').val();
			if(age === '') {
				// ageが空の場合はnullにする
				age = null;
			} else {
				age = parseInt(age);
			}
						
			var dialogInput = {
					personId: $dialog.find('input[name="id"]').val(),
					name: $dialog.find('input[name="name"]').val(),
					age: age,
					organization: $dialog.find('input[name="organization"]').val()
			}; 
			
			var oldId = $dialog.find('input[name="oldId"]').val();
			if (oldId) {
				dialogInput.oldId = oldId;
			}
			
			return dialogInput;  
		},

		/**
		 * ユーザを登録
		 */
		regist: function(person, oldId) {
			var promise = this.logic.regist(person, oldId);

			scheduleSample.common.showIndicator(this, promise, 'データを登録中');

			var that = this;

			promise.always( function() {
					that.plotPerson();
					scheduleSample.common.closeDialog();
					alert('登録しました');
			});
		},

		/**
		 * 編集画面から変更をクリック
		 */
		'{#dialog #person_edit .submit} click': function(context) {
			// 入力データの取得
			var $dialog = $(context.event.target).closest('#dialog');
			var person = this.getDialogInput($dialog);

			scheduleSample.common.closeDialog();;

			var that = this;
			var promise = this.logic.update(person);
			
			scheduleSample.common.showIndicator(this, promise, 'データを更新中');
			
			promise.always(function() {
				that.plotPerson();
				scheduleSample.common.closeDialog();
				alert('ユーザ情報を変更しました。');
			}).fail(function(e) {
				that.log.error(e);
			});
		},

		/**
		 * ダイアログから削除ボタンをクリック
		 */
		'{#dialog #person_detail button.deletePerson} click': function(context) {
			var id = $(context.event.target).nextAll('input:first').val();
			var name = $(context.event.target).prevAll('.name:first').next('p').text();
			if (!confirm('ユーザ『' + name + '』を削除します')) {
				return;
			}

			var that = this;
			var promise = this.logic.deletePerson(id);
			
			scheduleSample.common.showIndicator(this, promise, 'データを削除中')
			
			promise.always( function() {
					that.plotPerson();
					alert('削除しました。');
					$('#dialog .closebutton').trigger('click');
			});
		},
		
		/**
		 * ユーザを登録ボタンをクリック
		 */
		'.registbutton click': function(context) {
			this.openRegistDialog();
		},

		/**
		 * 詳細表示ボタンをクリック
		 */
		'.showdetail click': function(context) {
			this.showDetail();
		},

		/**
		 * 編集画面からキャンセルをクリック
		 */
		'{#dialog .cancelEdit} click': function(context) {
			scheduleSample.common.showDialog(this.$fromDialog, {
				top: '0'
			});
		},

		/**
		 * ダイアログから再登録ボタンをクリック
		 */
		'{#dialog #person_conflict button.registPerson} click': function(context) {
			var id = $(context.event.target).nextAll('input:first').val();
			var person = this.personDataModel.get(id);
			this.$fromDialog = $('#dialog .content>*').clone();
			this.openRegistDialog(person);
		},

		/**
		 * ダイアログから編集ボタンをクリック
		 */
		'{#dialog #person_detail button.editPerson} click': function(context) {
			var id = $(context.event.target).nextAll('input:first').val();
			var person = this.personDataModel.get(id);
			this.$fromDialog = $('#dialog .content>*').clone();
			scheduleSample.common.showDialog(this.view.get('edit', {
				person : person
			}), {
				top: 0
			});
		},

		/**
		 * 登録画面を開く
		 */
		openRegistDialog: function(person) {
			scheduleSample.common.showDialog(this.view.get('regist', {
				name : person ? person.get('name') : '',
				age : person ? person.get('age') : '',
				organization : person ? person.get('organization') : '',
				oldId: person ? person.get('personId') : ''
			}), {
				top: 0
			});
		},

		/**
		 * 登録されているユーザを表示する
		 */
		plotPerson: function() {
			this.view.update(this.$content, 'personline', {
				persons: this.personDataModel.items
			});
		}
	};

	h5.u.obj.expose('scheduleSample.controller' , {
		 personController: personController
	});

});