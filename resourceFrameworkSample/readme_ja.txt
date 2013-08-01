hifiveResourceSample

----------------------------------------------

hifive resource framework、hifive sync serverの使用サンプルモジュール・アプリケーションのプロジェクトです。
以下にビルド方法を記述します。

1.リポジトリのクローン
  $ git clone git@github.com:hifive/hifive-sync-server.git

2.Apache ivyを利用しhifiveResourceSampleプロジェクトに必要なライブラリを追加
  hifiveResourceSample/ivy_build.xmlのresolveターゲットを実行します。

  -コマンドラインから
   $ cd hifiveResourceSample
   $ ant -buildfile ivy_build.xml

  -IDE(eclipse)から
   hifiveResourceSampleプロジェクトをインポート -> hifiveResourceSample/ivy_build.xmlを右クリック -> 実行 -> Antビルド

3.ビルドを実行
  hifiveResourceSample/build.xmlのbuildターゲットを実行します。

  -コマンドラインから
   $ cd hifiveResourceSample
   $ ant -buildfile build.xml

  -IDE(eclipseから)
   hifiveResourceSample/build.xmlを右クリック -> 実行 -> Antビルド

  hifiveResourceSample/target/
    hifive-resource-sample-(バージョン).war
     が生成されます。

------------------------------------------------------------

APIドキュメント（JavaDocドキュメント）の生成方法:

  - build.xmlのjavadocターゲットを実行します。
    hifiveResourceSample/target/doc の下にドキュメントが生成されます。

------------------------------------------------------------
