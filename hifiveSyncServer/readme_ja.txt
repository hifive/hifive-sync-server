hifiveSyncServer

----------------------------------------------

hifive sync serverの開発用プロジェクトです。
以下にビルド方法を記述します。

1.リポジトリのクローン
  $ git clone git@github.com:hifive/hifive-sync-server.git

2.Apache ivyを利用しhifiveSyncServerプロジェクトに必要なライブラリを追加
  hifiveSyncServer/ivy_build.xmlのresolveターゲットを実行します。

  -コマンドラインから
   $ cd hifiveSyncServer
   $ ant -buildfile ivy_build.xml

  -IDE(eclipse)から
   hifiveSyncServerプロジェクトをインポート -> hifiveSyncServer/ivy_build.xmlを右クリック -> 実行 -> Antビルド

3.ビルドを実行
  hifiveSyncServer/build.xmlのbuildターゲットを実行します。

  -コマンドラインから
   $ cd hifiveSyncServer
   $ ant -buildfile build.xml

  -IDE(eclipseから)
   hifiveSyncServer/build.xmlを右クリック -> 実行 -> Antビルド

  hifiveSyncServer/target/
    hifive-sync-server-(バージョン).jar
     が生成されます。

------------------------------------------------------------

APIドキュメント（JavaDocドキュメント）の生成方法:

  - build.xmlのjavadocターゲットを実行します。
    hifiveSyncServer/target/doc の下にドキュメントが生成されます。

------------------------------------------------------------
