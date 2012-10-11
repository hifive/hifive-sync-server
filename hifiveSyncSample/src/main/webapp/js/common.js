$(function() {

	if (window.__resetData) {
		h5.api.storage.local.clear();
	}

	navigator.__onLine = true;

	var categoryIds = ['outmeeting', 'meeting', 'trip', 'holiday', 'others','deadline'];
	var categoryNames = ['社外打ち合わせ', '社内会議', '出張', '休み', 'その他', '締切'];
	var categoryClassNames = ['category-a', 'category-b', 'category-c', 'category-d', 'category-e', 'deadline'];

	var category = {};
	for ( var i = 0, l = categoryIds.length; i < l; i++) {
		var obj = {
			name: categoryNames[i],
			className: categoryClassNames[i]
		};
		category[categoryIds[i]] = obj;
	}

	/*
	 * DataModelマネージャを作成(DBの"スキーマ"に概ね相当)
	 * インスタンスはexpose()しておくことが多いだろう
	 */
	h5.core.data.createManager('manager', 'scheduleSample.data');

	/**
	 * grobalであるh5.core.viewでdialog.ejsをロード
	 */
	h5.core.view.load(['./template/dialog.ejs?' + new Date().getTime()]).done(function() {
		// ダイアログをbodyに追加
		$('body').append('<div id="hiddenBox" style="display:none"></div>');
		$('body #hiddenBox').append(h5.core.view.get('dialog'));
		/**
		 * 共通ダイアログコントローラ
		 */
		var dialogController = {
			__name: 'DialogController',

			/**
			 * キーダウンイベント escキーでダイアログを閉じる
			 */
			'{window} [keydown]': function(context) {
				if (this.rootElement.style.display === 'none') {
					return;
				}
				if (context.event.keyCode === 27) {
					$.unblockUI();
				}
			},

			/**
			 * 戻るボタンをクリック
			 */
			'.back click': function(context) {
				this.$find('.content>*').trigger('close');
				$.unblockUI();
			}
		};

		// ダイアログコントローラをバインド
		h5.core.controller('#dialog', dialogController);
	});

	h5.u.obj.expose('scheduleSample.common', {

		categoryIds: categoryIds,

		/**
		 * カテゴリidをキーにした、カテゴリ毎のクラス名、カテゴリの表示名
		 */
		category: category,

		/**
		 * ダイアログ表示
		 *
		 * @param {String} content ダイアログの中に表示するhtml文字列
		 * @param {Object} [css] 適応させるCSS ex {top:'20%}
		 */
		showDialog: function(content, css) {
			$('#dialog .content').html(content);
			var width = $('#dialog .content>div').outerWidth();
			// TODO $('#dialog-bar').outerHeight()がこの時点で取得できないため、直接指定している
			var height = $('#dialog .content>div').outerHeight() + 40;
			winHeight = $(window).height();
			$.blockUI({
				message: $('#dialog'),
				css: $.extend(css, {
					cursor: 'auto',
					overflowX: 'hidden',
					width: width,
					height: Math.min(height, h5.env.ua.isSmartPhone ? 9999 : winHeight),
					// コンテンツの幅から、中央表示するときのleftの位置を計算する。
					left: ($(window).innerWidth() - width) / 2,
					position: 'absolute',
					backgroundColor: 'white',
					border: '3px solid #AAA'
				})
			});
			$('.datepicker[type="text"]').datepicker({dateFormat: 'yy/mm/dd'});
		},

		/**
		 * 数字から曜日に変換
		 */
		numToWeek: function(num) {
			return ['日', '月', '火', '水', '木', '金', '土'][num];
		},

		/**
		 * 日付から曜日を取得
		 */
		dateToWeek: function(dateStr) {
			var date = new Date(dateStr);
			return ['日', '月', '火', '水', '木', '金', '土'][date.getDay()];
		},


		closeDialog: function() {
			$('#dialog .closebutton').trigger('click');
		},

		/**
		 * メッセージ付きでインジケーターを表示する
		 */
		showIndicator: function(context, promise, message) {
			// オフライン実行時だと、結果が早く帰ってきてしまい、indicatorの表示が消えないため、
			// stateを評価する。
			if (promise.state() !== 'pending') {
				return;
			}

			context.indicator({
				target: window,
				message: message,
				promises: promise
			}).show();
		},

		/**
		 * 適切な日付入力フォーム スマートフォン、iPadならtype="date"のもの。そうでないならdatepickerを使ったtype="text"のものを返す
		 *
		 * @param {String} [date] デフォルトで入力する日付の文字列
		 */
		getDateInputForm: function(date) {
			var dateStr = '';
			if (h5.env.ua.isiPhone || h5.env.ua.isiPad) {
				if (date) {
					var d = new Date(date);
					dateStr = d.getFullYear() + '-' + ("0" + (d.getMonth() + 1)).slice(-2) + '-'
							+ ("0" + d.getDate()).slice(-2);
				}
				return '<input type="date" name="date" value="' + dateStr + '" />';
			} else {
				dateStr = date || '';
				return '<input type="text" class="datepicker" name="date" value="' + dateStr
						+ '"/>';
			}
		},

		/**
		 * 基本型の配列が同一の要素からなるかを判定する
		 */
		equalArrays: function(array1, array2) {
			if (array1.length != array2.length) {
				return false;
			}

			for (var i=0; i<array1.length; i++) {
				if (array1[i] == array2[i]) {
					return false;
				}
			}
			return true;
		}
	});
});