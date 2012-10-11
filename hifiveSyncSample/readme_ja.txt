﻿hifive-sync-sample
----------------------------------------------
hifive syncフレームワークを使用したサンプルアプリケーション(スケジュール管理アプリ)です。
以下にビルド方法を記述します。


1.リポジトリのクローン(hifive-sync-serverフレームワークと同一)

  $ git clone git@github.com:hifive/hifive-sync-server.git


2.Apache ivyを利用し、hifive-sync-sampleに必要なライブラリを追加

  hifiveSyncSample/ivy_build.xmlのresolveターゲットを実行します。

  -コマンドラインから
   $ cd hifiveSyncSample
   $ ant -buildfile ivy_build.xml

  -IDE(eclipse)から
   hifiveSyncSampleプロジェクトをインポート -> hifiveSyncSample/ivy_build.xmlを右クリック -> 実行 -> Antビルド


3.データベースファイル(H2)のURLを指定

    hifiveSyncSample/src/main/resources/META-INF/database.properties
  の「db.url」にDBの場所と名前を指定してください。
    db.url=jdbc:h2:file:///[ファイルパス("\"ではなく"/")]/[ファイル名]
  と設定すると、
    [ファイル名].h2.db
  という名前のファイルが作成、参照されます.

  例：
      db.url=jdbc:h2:file:///C:/xxx/hifive-sync-server/sync
    と設定すると、C:/xxx/hifive-sync-server/ に sync.h2.db が作成され、参照される


4.サーバアプリケーションのビルドを実行

  hifiveSyncSample/build.xmlのbuildターゲットを実行します。

  -コマンドラインから
   $ cd hifiveSyncSample
   $ ant -buildfile build.xml

  -IDE(eclipseから)
   hifiveSyncSample/build.xmlを右クリック -> 実行 -> Antビルド

  hifiveSyncSample/target/
    hifive-sync-sample-1.0.0.war
     が生成されます。


5.サーバアプリケーションの起動

  eclipseからTomcatプラグイン、あるいはwarファイルをサーバに配備してアプリケーションを起動します。


6.アクセス

   http://[hostname]:[port]/hifiveSyncSample/

  へアクセスします。
  認証ダイアログが表示されますので、ユーザー名「hifive」パスワード「hifive」でログインします。


7.hifiveユーザーを登録

  「ユーザーを登録」ボタンを押し、ID：hifiveのユーザーを登録してください(他のデータは任意)。
  (サンプルアプリケーションのため認証後にユーザー登録していますが、これ以降はこのユーザーがスケジュールの作成者になります)


8.アプリケーションを使用

  ユーザーやユーザーのスケジュールを登録、変更できます。

------------------------------------------------------------
