hifiveResourceFramework

----------------------------------------------

hifive resource frameworkの開発用プロジェクトです。
以下にビルド方法を記述します。

1.リポジトリのクローン
  $ git clone git@github.com:hifive/hifive-sync-server.git

2.Apache ivyを利用しhifiveResourceFrameworkプロジェクトに必要なライブラリを追加
  hifiveResourceFramework/ivy_build.xmlのresolveターゲットを実行します。

  -コマンドラインから
   $ cd hifiveResourceFramework
   $ ant -buildfile ivy_build.xml

  -IDE(eclipse)から
   hifiveResourceFrameworkプロジェクトをインポート -> hifiveResourceFramework/ivy_build.xmlを右クリック -> 実行 -> Antビルド

3.ビルドを実行
  hifiveResourceFramework/build.xmlのbuildターゲットを実行します。

  -コマンドラインから
   $ cd hifiveResourceFramework
   $ ant -buildfile build.xml

  -IDE(eclipseから)
   hifiveResourceFramework/build.xmlを右クリック -> 実行 -> Antビルド

  hifiveResourceFramework/target/
    hifive-resource-framework-(バージョン).jar
     が生成されます。

------------------------------------------------------------

APIドキュメント（JavaDocドキュメント）の生成方法:

  - build.xmlのjavadocターゲットを実行します。
    hifiveResourceFramework/target/doc の下にドキュメントが生成されます。

------------------------------------------------------------
