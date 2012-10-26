$(function() {
	
	var personDataModel = scheduleSample.data.manager.models.person;

	/**
	 * scheduleモデルのディスクリプタ
	 */
	var SCHEDULE_DESCRIPTOR = {
			name: 'schedule',
			schema: {
				scheduleId: {
					id: true,
					type: 'string'
				},
				title: {
					//型を指定する. バリデーション時にも使われる。
					type: 'string'
				},

				category: {
					type: 'enum',
					enumValue: scheduleSample.common.categoryIds
				},

				categoryName: {
					depend: {
						on: ['category'],
						calc: function() {
							return scheduleSample.common.category[this.get('category')].className;
						}
					},
					isTransient: true
				},

				dates: {
					type: 'string[]'
				},

				place: {},

				startTime: {},

				finishTime: {},

				detail: {},

				userIds: {
					type: 'string[]'
				},

				users: {
					type: '@person[]',
					depend: {
						on: ['userIds'],
						calc: function() {
							var userIds = this.get('userIds');
							return personDataModel.get(userIds);
						}
					},
					isTransient: true
				},
				
				createUserName: {
					type: 'string',
					isTransient: true
				}
			}
		};

	//DataModelの登録は初期ロード時に一度行えばよい
	scheduleSample.data.manager.createModel(SCHEDULE_DESCRIPTOR);

	/**
	 * @name ScheduleController
	 */
	var scheduleController = {

		/**
		 * @memberOf ScheduleController
		 */
		__name: 'ScheduleController',

		logic: null,

		scheduleDataModel: scheduleSample.data.manager.models.schedule,

		personDataModel: personDataModel,
		
		// 日付をキーにしたスケジュール
		schedulesByDateKey: [],

		// スケジュールモデル内の競合状態のアイテム
		// TODO: syncManagerに持たせれば、ここのプロパティは不必要
		_conflictItems : [],

		// TODO ダイアログの処理は共通化するべき
		/**
		 * ダイアログからダイアログへ遷移したときに遷移元を保存しておく
		 */
		$fromDialog: null,

		// キャッシュから取ってきてほしくないのでリクエストパラメータ指定している
		__templates: ['template/schedule.ejs?' + new Date().getTime()],

		$content: null,
		
		__init: function(context) {
			var that = this;

			// conflictのイベントリスナーを登録
			scheduleSample.sync.manager.addEventListener('conflict', function(event) {
				// 今回競合した分のデータを競合データリストに入れる
				// TODO: syncManagerに持たせる？
				var conflictItems = event.conflicted[that.scheduleDataModel.name];
				if (!conflictItems) {
					return;
				}
				that._conflictItems = that._conflictItems.concat(conflictItems['changed']).concat(conflictItems['removed']);
				alert('スケジュールのデータが競合しています。');				
				that.showConflict();
			});
			
			this.logic = new scheduleSample.logic.ScheduleLogic();
			return this.logic.init();
		},

		__ready: function(context) {
			this.$content = this.$find('.content');
			var date = new Date();
			this.showCalender(date.getFullYear(), date.getMonth() + 1);
		},

		'{rootElement} _plot': function(context) {
			this.plotSchedule();
		},

		'{rootElement} _showConflictData': function(context) {
			// 競合ダイアログを表示する
			this.showConflict();
		},

		/**
		 * ウィンドウリサイズ時にテーブルのサイズ調整する リサイズ時にTopControllerが_resizeイベントをトリガする
		 */
		'{rootElement} _resize': function(context) {
			this.adjustTableSize();
		},

		/**
		 * 次の月へ
		 */
		'.nextMonth click': function(context) {
			context.event.preventDefault();
			this.moveCalender(1);
		},

		/**
		 * 前の月へ
		 */
		'.preMonth click': function(context) {
			this.moveCalender(-1);
		},

		/**
		 * 日付クリック
		 */
		'.date-column click': function(context) {
			context.event.preventDefault();
			var dateTime = context.event.currentTarget.getAttribute('id');
			$('.clicked-date').removeClass('clicked-date');
			$(context.event.target).closest('td').addClass('clicked-date');
			this.showScheduleByDate(dateTime);
		},

		/**
		 * ダイアログの日付入力フォームをクリック
		 */
		'.datepicker.hasDatepicker focus': function(context) {
			context.event.preventDefault();
		},

		showConflict: function() {
			if (!this._conflictItems || this._conflictItems.length == 0) {
				alert('衝突しているデータはありません');
				return;
			}
			
			scheduleSample.common.showDialog(this.view.get('conflict', {
				conflictItems: this._conflictItems
			}), {
				top: '0'
			});
		},

		getDialogInput: function($dialog) {
			var dates = [];
			$dialog.find('input[name="date"]').each(function() {
				if (this.value !== "") {
					dates.push(this.value);
				}
			});
			var userIds = [];
			$dialog.find('input[name="userIds"]').each(function() {
				userIds.push(this.value);
			});
			return  {
				category: $dialog.find('select[name="category"]').val(),
				dates: dates,
				title: $dialog.find('input[name="title"]').val(),
				detail: $dialog.find('textarea[name="detail"]').val(),
				userIds: userIds
			};
		},

		/**
		 * ダイアログのスケジュール登録ボタンをクリック
		 */
		'{#schedule_regist button.submit} click': function(context) {
			var $dialog = $('#schedule_regist');
			var obj = this.getDialogInput($dialog);

			var conflictIndex = $(context.event.target).nextAll('input[name="conflictIndex"]').val();

			if (conflictIndex && this._conflictItems && this._conflictItems.length > 0) {
				this._conflictItems.splice(conflictIndex, 1);
			}
			
			this.regist(obj);
		},

		/**
		 * スケジュールを登録
		 */
		regist: function(schedule) {
			var that = this;
			var date = schedule.dates[0];
			var promise = this.logic.regist(schedule);
			
			scheduleSample.common.showIndicator(this, promise, 'データを登録中');

			promise.always( function() {
					that.plotSchedule();
					that.showScheduleByDate(date);
					that.closeDialog();
					alert('登録しました');
			});
		},

		/**
		 * 予定編集画面から変更をクリック
		 */
		'{#dialog #schedule_edit .submit} click': function(context) {
			// 入力データの取得
			var $dialog = $(context.event.target).closest('#dialog');
			var schedule = this.getDialogInput($dialog);

			var scheduleId = $(context.event.target).nextAll('input[name="scheduleId"]').val();
			var conflictIndex = $(context.event.target).nextAll('input[name="conflictIndex"]').val();

			if (conflictIndex && this._conflictItems && this._conflictItems.length > 0) {
				this._conflictItems.splice(conflictIndex, 1);
			}

			var that = this;
			var date = schedule.dates[0];
			var promise = this.logic.update(schedule, scheduleId);
			
			scheduleSample.common.showIndicator(this, promise, 'データを更新中');
			
			promise.always(function(obj) {
				that.plotSchedule();
				that.showScheduleByDate(date);
				that.closeDialog();
				if (obj && obj.status === 409) {
					return;
				}
				alert('スケジュールを変更しました。');					
			});
		},

		/**
		 * ダイアログから予定を削除ボタンをクリック
		 */
		'{#dialog #schedule_detail button.deleteSchedule} click': function(context) {
			var scheduleId = $(context.event.target).nextAll('input:first').val();
			var title = $(context.event.target).prevAll('.title:first').next('p').text();
			var date = $(context.event.target).parent('#schedule_detail').find('.date').text();
			if (!confirm('予定『' + title + '』を削除します')) {
				return;
			}

			var that = this;
			var promise = this.logic.deleteSchedule(scheduleId);
			
			scheduleSample.common.showIndicator(this, promise, 'データを削除中');
			
			promise.always( function(obj) {
					that.plotSchedule();
					that.showScheduleByDate(date);
					if (obj && obj.status === 409) {
						return;
					}
					alert('削除しました。');
					scheduleSample.common.closeDialog();
			});
		},

		/**
		 * 予定を登録ボタンをクリック
		 */
		'.registbutton click': function(context) {
			var dateHashInput = $('#schedule-selected-date .date-hash')[0];
			var dateHash = dateHashInput ? dateHashInput.value : null;
			this.openRegistDialog(dateHash
					|| this.$find('table.calender td.current-month-date:first').attr('id').replace(/-/g, '/'));
		},

		/**
		 * 詳細表示ボタンをクリック
		 */
		'.showdetail click': function(context) {
			var dateHashInput = $('#schedule-selected-date .date-hash')[0];
			var dateHash = dateHashInput ? dateHashInput.value : null;
			this.showDetail(dateHash ? dateHash : null);
		},

		/**
		 * 予定ボックスをクリック
		 */
		'#schedule-selected-date .date-schedule-box click': function(context) {
			var scheduleId = $(context.event.target).find('input[name="scheduleId"]').val();
			var date = this.$find('#schedule-selected-date input.date-hash').val();
			var schedules = this.scheduleDataModel.get(scheduleId);
			var dateIndex = $.inArray(date, schedules.get('dates'));

			this.showDetailById(scheduleId, dateIndex);
		},

		/**
		 * 予定編集画面からキャンセルをクリック
		 */
		'{#dialog .cancelEdit} click': function(context) {
			scheduleSample.common.showDialog(this.$fromDialog, {
				top: '0'
			});
		},

		/**
		 * ダイアログから予定を編集ボタンをクリック
		 */
		'{#dialog #schedule_detail button.editSchedule} click': function(context) {
			var scheduleId = $(context.event.target).nextAll('input:first').val();
			var schedule = this.scheduleDataModel.get(scheduleId);
			this.$fromDialog = $('#dialog .content>*').clone();
			scheduleSample.common.showDialog(this.view.get('edit', {
				schedule : schedule,
				conflict: null
			}), {
				top: 0
			});
		},

		/**
		 * 競合を表示するダイアログから予定を編集ボタンをクリック
		 */
		'{#dialog #schedule_conflict button.editSchedule} click': function(context) {
			var scheduleId = $(context.event.target).nextAll('input[name="scheduleId"]').val();
			var schedule = this.scheduleDataModel.get(scheduleId);
			this.$fromDialog = $('#dialog .content>*').clone();
			var conflict = {};
			var dates =  $(context.event.target).nextAll('input[name="dates"]').val();
			if (dates && schedule.get('dates') != dates) {
				conflict.dates = dates;
			}

			var category = $(context.event.target).nextAll('input[name="category"]').val();
			if (category && schedule.get('category') != category) {
				conflict.category = category;
			}

			var title =  $(context.event.target).nextAll('input[name="title"]').val();
			if (title && schedule.get('title') != title) {
				conflict.title = title;
			}

			var detail = $(context.event.target).nextAll('input[name="detail"]').val();
			if (detail && schedule.get('detail') != detail) {
				conflict.detail = detail;
			}

			var userIds =  $(context.event.target).nextAll('input[name="userIds"]').val();
			if (userIds && scheduleSample.common.equalArrays(schedule.get('userIds'), userIds)) {
				conflict.date = userIds;
			}

			var conflictIndex = $(context.event.target).nextAll('input[name="conflictIndex"]').val();
			conflict.conflictindex = conflictIndex;
			scheduleSample.common.showDialog(this.view.get('edit', {
				schedule: schedule,
				conflict: conflict
			}), {
				top: 0
			});
		},

		/**
		 * 競合を表示するダイアログから予定を編集ボタンをクリック（ローカルで一度削除しているとき）
		 */
		'{#dialog #schedule_conflict button.registSchedule} click': function(context) {
			var scheduleId = $(context.event.target).nextAll('input[name="scheduleId"]').val();
			this.$fromDialog = $('#dialog .content>*').clone();
			
			var dates =  $(context.event.target).nextAll('input[name="dates"]').val();

			var schedule = {
					dates: dates,
					category: $(context.event.target).nextAll('input[name="category"]').val(),
					title: $(context.event.target).nextAll('input[name="title"]').val(),
					detail: $(context.event.target).nextAll('input[name="detail"]').val(),
					userIds: $(context.event.target).nextAll('input[name="userIds"]').val()
			}
			
			var conflictIndex = $(context.event.target).nextAll('input[name="conflictIndex"]').val();
			this.openRegistDialog(dates, schedule, conflictIndex);
		},

		/**
		 * カレンダーのサイズ調整
		 */
		adjustTableSize: function() {
			var $tableWrap = this.$find('.table-wrap');
			var height = $(this.rootElement).height()
					- ($tableWrap.offset().top - $(this.rootElement).offset().top);
			$tableWrap.height(height);
			var elm = $tableWrap[0];
			if (elm.clientWidth !== elm.offsetWidth
					&& this.$find('table.calender th.dummyForScrollWidth').length === 0) {
				this.$find('table.calender thead tr').append(
						$('<th class="dummyForScrollWidth"></th>'));
			} else if (elm.clientWidth === elm.offsetWidth
					&& this.$find('table.calender th.dummyForScrollWidth').length !== 0) {
				this.$find('table.calender th.dummyForScrollWidth').remove();
			}
		},

		moveCalender: function(d) {
			var curYear = parseInt(this.$find('#view_year')[0].value);
			var curMonth = parseInt(this.$find('#view_month')[0].value);
			var ym = curYear * 12 + curMonth - 1 + d;
			var y = parseInt(ym / 12);
			var m = ym % 12 + 1;
			this.showCalender(y, m);
			this.adjustTableSize();
			this.plotSchedule();
		},

		showCalender: function(y, m) {
			this.view.update(this.$content, 'calender', {
				year: y,
				month: m,
			});
		},

		/**
		 * 登録画面を開く
		 *
		 * @param {Date} [date] 予定登録画面の日付にあらかじめ入力しておく日にちの日付
		 */
		openRegistDialog: function(date, schedule, conflictIndex) {
			var userIds = [''];
			if (schedule && schedule.userIds) {
				userIds = scheduele.userIds;
			}
			
			scheduleSample.common.showDialog(this.view.get('regist', {
				category: schedule ? schedule.category : null,
				dateFrom: {
					date: date
				},
				title: schedule ? schedule.title : '',
				detail: schedule ? schedule.detail : '',
				userIds: userIds, 
				conflictIndex: conflictIndex ? conflictIndex : null
			}), {
				top: 0
			});
		},

		/**
		 * 指定された日付の予定をダイアログで表示
		 */
		showDetail: function(date, index) {
			var array = this.schedulesByDateKey[date];
			scheduleSample.common.showDialog(this.view.get('detail', {
				array: array,
				dateIndex: index || 0
			}), {
				top: '0'
			});
		},

		/**
		 * 指定されたIDの予定をダイアログで表示
		 */
		showDetailById: function(id, dateIndex) {
			var obj = this.scheduleDataModel.get(id);
			if (!obj) {
				alert('この予定は存在しません。');
				return;
			}

			var index = dateIndex || 0;
			scheduleSample.common.showDialog(this.view.get('detail', {
				array: [obj],
				dateIndex: index
			}), {
				top: '0'
			});
		},

		closeDialog: function() {
			scheduleSample.common.closeDialog();
			try {
				this.$find('.clicked-date').removeClass('clicked-date');
				var dateClass = ($('#schedule-selected-date input.date-hash').val() || date).replace(
						/\//g, '-');
				this.$find('#' + dateClass).addClass('clicked-date');
			} catch(e) {
				// 押せなかったときは何もしない
			}
		},


		/**
		 * 日付をキーとした形に変換する
		 */
		_formatSchedules: function() {
			this.schedulesByDateKey = [];

			/**
			 * サーバからロードされたデータを日付をキーにした形に変更
			 */
			for (var id in this.scheduleDataModel.items) {
				var dates = this.scheduleDataModel.items[id].get('dates');

				for ( var j = 0, len=dates.length; j<len; j++) {
					var date = dates[j];

					if (!this.schedulesByDateKey[date]) {
						this.schedulesByDateKey[date] = [];
					}
					this.schedulesByDateKey[date].push(this.scheduleDataModel.items[id]);
				}
			}
		},

		/**
		 * 現在表示されているカレンダーのスケジュールを表示する
		 */
		plotSchedule: function() {
			this._formatSchedules();

			var startDate = new Date($('table.calender td:first').attr('id').replace(/-/g, '/'));
			var endDate = new Date($('table.calender td:last').attr('id').replace(/-/g, '/'));

			this.$find('.scheduleCell').children().remove();
			for ( var date in this.schedulesByDateKey) {
				if (startDate <= new Date(date) && new Date(date) <= endDate) {
					this.refleshScheduleByDate(date);
				}
			}
		},

		/**
		 * クリックした日のスケジュール一覧を表示する
		 *
		 * @param {String} date 'yyyy-m-d'形式。
		 */
		showScheduleByDate: function(_date) {
			var date = _date.replace(/-/g, '/');
			this.$find('#schedule-selected-date .default-message').remove();
			this.$find('#schedule-selected-date ul li').remove();
			this.$find('#schedule-selected-date')[0].innerHTML = '';

			// 選択された日のスケジュールから必要な情報を取得
			var schedules = [];
			if (this.schedulesByDateKey[date]) {
				for ( var i = 0, l = this.schedulesByDateKey[date].length; i < l; i++) {
					var schedule = this.schedulesByDateKey[date][i];
					schedules.push(schedule);
				}
			}
			// 選択された日の予定を表示
			this.view.update(this.$find('#schedule-selected-date'), 'abstruct-day-schedule', {
				date: h5.u.str.format('{0}({1})', date, scheduleSample.common.numToWeek(new Date(date)
						.getDay())),
				hash: date,
				existData: !this.schedulesByDateKey[date],
				schedules: schedules
			});
		},

		/**
		 * 指定された日付のスケジュール表示を更新する
		 *
		 * @param {String} date 'yyyy/m/d'形式または'yyyy-m-d'形式。
		 */
		refleshScheduleByDate: function(date) {
			var $td = $('#' + date.replace(/\//g, '-'));
			var $cell = $td.find('.scheduleCell');
			$cell.children().remove();
			var existDeadline = false;
			if (!this.schedulesByDateKey[date] || this.schedulesByDateKey[date] === 0) {
				return;
			}
			var schedules = $(this.schedulesByDateKey[date]).filter(function() {
				if (this.get('category') !== 'deadline') {
					return true;
				}
				existDeadline = true;
			});
			var l = schedules.length;
			var originHeight = $td.children('.td_cell').height();
			var height;
			if (l !== 0) {
				height = Math.max(1, (originHeight - l * 2) / l);
				for ( var i = 0; i < l; i++) {
					var obj = schedules[i];
					var category = scheduleSample.common.category[obj.get('category')];
					var className = category.className;
					var $div = $(this.view.get('colorBar', {
						categoryClassName: className
					}));
					$div.css('height', height + 'px');
					$cell.append($div);
				}
			} else {
				var dummyHeight = originHeight - 2;
				var $div = $('<div/>');
				$div.css('height', dummyHeight + 'px');
				$div.css('position', 'relative');
				$cell.append($div);
			}
			if (existDeadline) {
				this.view.append($cell, 'deadline');
			}
		}
	};

	h5.u.obj.expose('scheduleSample.controller' , {
		 scheduleController: scheduleController
	});
});
