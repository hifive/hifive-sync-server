$(function() {
	h5.core.sync.createManager('manager', scheduleSample.data.manager, 'sync', 'scheduleSample.sync');

	var topController = {
		__name: 'TopController',
		__templates: ['template/top.ejs'],

		personController: scheduleSample.controller.personController,
		scheduleController: scheduleSample.controller.scheduleController,

		__meta: {
			personController: {
				rootElement: '#person',
			},
			scheduleController: {
				rootElement: '#schedule',
			}
		},

		__ready: function(cotnext) {
			if (!h5.env.ua.isSmartPhone) {
				this.view.prepend(this.rootElement, ('header'));
			}
			// 日付の表示
			this.showDate();

			var that = this;
			this.load().done(function() {
				if (!h5.env.ua.isSmartPhone) {
					// カラムの高さ調整
					that.adjustView();
				}

			});
		},

		load: function () {
			var dfd = h5.async.deferred();
			var promise = scheduleSample.sync.manager.sync();
			var that = this;

			promise.done(function() {
					that.$find('.column').trigger('_plot');
					dfd.resolve();
			}).fail(function(e) {
					if (e.status === 409) {
						that.$find('.column').trigger('_plot');					
					}
					dfd.resolve();
			});

			return dfd.promise();
		},


		'{window} [resize]': function(context) {
			this.adjustView();
		},

		/**
		 * オンライン・オフラインの疑似的な切り替え
		 */
		'#onlineStatus change': function(context) {
			var value = context.event.target.value;
			if (value == 'on') {
				window.navigator.__onLine = true;
			} else if (value == 'off') {
				window.navigator.__onLine = false;
			}
		},

		'#sync click': function(context) {
			var promise = scheduleSample.sync.manager.sync();

			scheduleSample.common.showIndicator(this, promise, 'データを取得中');
			
			var $column = this.$find('.column');

			promise.done(function() {
				$column.trigger('_plot');
			}).fail(function(e) {
				if (e.status == 409) {
					$column.trigger('_plot');
				} else {
					that.log.error(e);
				}
			});
		},

		'#showConflictData click': function(context) {
			this.$find('.column').trigger({
				type: '_showConflictData',
				conflictItems: this._conflictItems
			});
		},

		/**
		 * ダイアログの外を押したらダイアログを閉じる
		 */
		'{.blockOverlay} touchend': function(context) {
			$('#dialog .closebutton').trigger('click');
		},

		/**
		 * ダイアログの外を押したらダイアログを閉じる
		 */
		'{.blockOverlay} click': function(context) {
			$('#dialog .closebutton').trigger('click');
		},

		adjustView: function() {
			if (h5.env.ua.isSmartPhone) {
				return;
			}
			var top = this.$find('.wrap').offset().top;
			this.$find('.column').height($(window).height() - top - 60);
			// 各カラムの_resizeイベントをトリガ
			this.$find('.column').trigger('_resize');
		},

		showDate: function() {
			var date = new Date();
			var y = date.getFullYear();
			var m = date.getMonth() + 1;
			var d = date.getDate();
			var wn = date.getDay();
			var w = scheduleSample.common.numToWeek(wn);
			this.$find('#date').text(h5.u.str.format('{0}年{1}月{2}日({3})', y, m, d, w));
		}
	};

	h5.core.controller('body', topController);
});