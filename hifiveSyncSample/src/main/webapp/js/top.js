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

			promise.always(function() {
					that.$find('.column').trigger('_plot');
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
				window.navigator.__offLine = false;
			} else if (value == 'off') {
				window.navigator.__offLine = true;
			}
		},

		'#sync click': function(context) {
			var promise = scheduleSample.sync.manager.sync();

			scheduleSample.common.showIndicator(this, promise, 'データを取得中');

			var $column = this.$find('.column');

			promise.always(function(obj) {
				$column.trigger('_plot');
				if (obj && obj.status === 409) {
					return;
				}
				alert('同期しました')
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