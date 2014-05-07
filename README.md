file2sqlite
======================

公開してますが個人用ライブラリです。  
単に自分がclone、コピペ等して再利用する為となります。  

アカウントも消す可能性が有るので  
リポジトリをforkなりしてください  

importが使えないCSVファイル等をsqliteに取り込む   

後方互換は多分考えません  
速度は遅いです。  
取込ファイル種類に固定長ファイルも合ったので使えるように  
作成しましたが、ソース修正が必要  
（独自仕様が多い、コード修正が必要です）  

機能
------
csvファイルをsqliteに取り込む  
importコマンド不可のファイルに対応する。  

1. **ダブルコード**で**改行**を含む値「Excelで{alt}+{Enter}で記載した値」  
2. **csvヘッダ**が有る

※ 最新版のsqliteは可能かもしれません

備考
------
1. package名に予約語が有るので↓で対応

    If any of the resulting package name components are keywords (§3.9) then append underscore to them.
2. build.gradleは動きません
3. javaはjdk7以上で動きます
4. 後方互換は多分考えません
5. exec-maven-pluginで実行する
6. 固定長は動きません（一般的な仕様は無いため、個々修正します）

とりあえず試す人(郵便番号サンプル)
------
    ./shell/test.shを実行する
    ./src/test/resources/13tokyo.csvを取り込む(郵便番号)
    ./target/sqlite/{今日の日付}/post.sqliteが出来上がる

パラメータ仕様
------
    # CONFIG_XML=[入力と出力を設定するxmlファイル。classpath上に必要]
    mvn exec:java \
    -Dnet._instanceof.commons.daemon.Bootstrap.main.beanid=Bootable \
    -Dnet._instanceof.batch.file2sqlite.config.file=${CONFIG_XML} \
    -Dexec.mainClass=net._instanceof.commons.daemon.Bootstrap

    # 以下のパラメータを追加する事で「path」指定配下のディレクトリを指定可能
    # 設定する事で以下の読み取りパスとなる
    # ./src/test/resources/20140101/13tokyo.csv

    -Dnet._instanceof.batch.file2sqlite.config.src_dir=20140101

設定ファイルの仕様
------
    <config>
      <public_env>
        <!-- 文字コードを指定 -->
        <property key="CSV_CHAR_SET"      value="Windows-31J" />
        <!-- 区切り文字を指定 tsvの場合は"\t"を設定 -->
        <property key="CSV_COLUMN_TOKEN"  value="," />
        <!-- Sqliteのクラスを指定 (他のは未作成) -->
        <property key="DATA_BASE_MANAGER" value="&appname;db.impl.DataBaseManagerSqlite" />
        <!-- sqliteの一時ファイル作成ディレクトリ -->
        <property key="DB_TEMP_PATH"      value="./target/sqlite/temp" />
        <!-- sqliteファイルの出力先 -->
        <property key="DB_FILE_PATH"      value="./target/sqlite" />
        <!-- sqliteファイル名 -->
        <property key="DB_FILE_NAME"      value="&sup;.sqlite" />
        <!-- CSVの1行目がヘッダの場合はtrue -->
        <property key="CSV_HEAD_PARSE"    value="false" />
      </public_env>
      <input_source>
        <!-- class : csvの取込クラスを指定 (固定長は未サポート) -->
        <data_importer class='&appname;impl.CSVDataImporter'>
          <!--
            id         = 識別子 fileタグは複数設定可能なので一意に設定する事
            path       = csvファイルのパス
            src        = csvファイル名
            table      = sqliteのテーブル名
            csv_header = ヘッダ値が未設定の場合のヘッダ値をカンマ区切り
                         ※ CSV_HEAD_PARSE = trueの場合は無視する
          -->
          <file
            id          = '&sup;'
            path        = './src/test/resources'
            src         = '13tokyo.csv'
            table       = '&sup;'
            csv_header  = 'A ,B ,C ,D ,E ,F ,G ,H ,I ,J ,K ,L ,M ,N ,O'
          >
            <!--
             CREATE TABLE / CREATE INDEXを記載します
             idは固定します
             値はcsv_headerの順番で登録されます
            -->
            <schema>
                CREATE TABLE &sup; (
                  id    INTEGER PRIMARY KEY AUTOINCREMENT,
                  A     TEXT, /* A ：  */
                  B     TEXT, /* B ：  */
                  C     TEXT, /* C ：  */
                  D     TEXT, /* D ：  */
                  E     TEXT, /* E ：  */
                  F     TEXT, /* F ：  */
                  G     TEXT, /* G ：  */
                  H     TEXT, /* H ：  */
                  I     TEXT, /* I ：  */
                  J     TEXT, /* J ：  */
                  K     TEXT, /* K ：  */
                  L     TEXT, /* L ：  */
                  M     TEXT, /* M ：  */
                  N     TEXT, /* N ：  */
                  O     TEXT  /* O ：  */
                );
                CREATE INDEX &sup;_idx_01 on &sup;(A);
            </schema>
          </file>
        </data_importer>
      </input_source>
    </config>
