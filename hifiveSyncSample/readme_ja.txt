hifive-sync-sample
----------------------------------------------
hifive syncフレームワークを使用したサンプルアプリケーション開発プロジェクトです。
以下にビルド方法を記述します。

1.リポジトリのクローン(hifive-sync-serverフレームワークと同一)
  $ git clone git@github.com:hifive/hifive-sync-server.git

2.Apache ivyを利用しhifive-sync-sampleに必要なライブラリを追加
  hifiveSyncSample/ivy_build.xmlのresolveターゲットを実行します。

  -コマンドラインから
   $ cd hifiveSyncSample
   $ ant -buildfile ivy_build.xml

  -IDE(eclipse)から
   hifiveSyncSampleプロジェクトをインポート -> hifiveSyncSample/ivy_build.xmlを右クリック -> 実行 -> Antビルド

3.サーバアプリケーションのビルドを実行
  hifiveSyncSample/build.xmlのbuildターゲットを実行します。

  -コマンドラインから
   $ cd hifiveSyncSample
   $ ant -buildfile build.xml

  -IDE(eclipseから)
   hifiveSyncSample/build.xmlを右クリック -> 実行 -> Antビルド

  hifiveSyncSample/target/
    hifive-sync-sample-1.0.0.war
     が生成されます。

4.サーバアプリケーションの起動
  eclipseからTomcatプラグインを使用してを起動します.
  また、warファイルを配備して起動ことも可能です.

5.アクセス
   localhost:8080/hifiveSyncSample/sync/index.html
  へアクセスします.

------------------------------------------------------------
