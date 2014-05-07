/*
 * Copyright(C) 2014 syuu256\gmail.com. All Rights Reserved.
 */
package net._instanceof.batch.file2sqlite.db.impl;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net._instanceof.batch.file2sqlite.config.ImportConfig;
import net._instanceof.batch.file2sqlite.db.DataBaseManager;
import net._instanceof.commons.datasource.wrapper.ConnectionWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DataBaseManagerSqlite implements DataBaseManager {

	private static final String DB_TEMP_PATH = "DB_TEMP_PATH";
	private static final String DB_FILE_PATH = "DB_FILE_PATH";
	private static final String DB_FILE_NAME = "DB_FILE_NAME";
	
	private static Logger log = LoggerFactory.getLogger(DataBaseManagerSqlite.class);
	
	private ImportConfig config = null;
	
    private Connection connection = null;

    private String out_dbfile  = "";

    private String temp_dbfile = "";
    
	@Override
	public DataBaseManagerSqlite initialize(final ImportConfig config) {
		
		this.config = config;
		initFilePath();
		
		return this;
	}

	private void initFilePath() {

		final Date d = new Date();
		
		final String yyyyMMdd = System.getProperty(ImportConfig.SRC_DIR_KEY, new SimpleDateFormat("yyyyMMdd").format(d));
		final String yyyyMMddHHmmssSSS = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(d);
		
		out_dbfile = config.getPublicEnv().get(DB_FILE_PATH) + "/"
				   + yyyyMMdd + "/" + config.getPublicEnv().get(DB_FILE_NAME);

		final String db_temp_path = config.getPublicEnv().get(DB_TEMP_PATH);
		if (!new File(db_temp_path).exists()) {
			new File(db_temp_path).mkdirs();
		}

		temp_dbfile = db_temp_path + "/" + yyyyMMddHHmmssSSS + "_" + yyyyMMdd + ".sqlite";
		
		if (log.isInfoEnabled()) {
			log.info("out_sqlite:" + out_dbfile);
			log.info("temp_sqlite:" + temp_dbfile);
		}
		
		final String out_dir = config.getPublicEnv().get(DB_FILE_PATH) + "/" + yyyyMMdd;
		final File f = new File(out_dir);
		
		if (!f.exists()) {
			f.mkdirs();
		}

		if (!f.isDirectory()) {
			throw new RuntimeException("non dir:" + out_dir);
		}
	}

	@Override
	public boolean isDatabaseInstance() {

		final File file = new File(out_dbfile);

		return file.exists();
	}

	@Override
	public DataBaseManagerSqlite dropDatabase() {

		if (isDatabaseInstance()) {
			final File file = new File(out_dbfile);
			file.delete();
		}

		return this;
	}

	@Override
	public DataBaseManagerSqlite connectDatabase() {

		final String jdbcString = "jdbc:sqlite:" + temp_dbfile;
		
		try {
			Class.forName("org.sqlite.JDBC");
			connection = new ConnectionWrapper(DriverManager.getConnection(jdbcString));
			connection.setAutoCommit(false);
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("sqlite error:" + jdbcString, e);
			}
			throw new RuntimeException(e);
		}

		return this;
	}

	@Override
	public Connection getConnection() {

		try {
			connection.rollback();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return connection;
	}

	@Override
	public DataBaseManager close() {

		try {
			connection.close();
		} catch (SQLException e) {
			log.error("", e);
			throw new RuntimeException(e);
		}
		
		final File from = new File(temp_dbfile);
		final File to = new File(out_dbfile);
		
		if (!from.renameTo(to)) {
			log.error("renameTo error:" + temp_dbfile + ":" + out_dbfile);
			throw new RuntimeException("renameTo error:" + temp_dbfile + ":" + out_dbfile);
		}
		return this;
	}
}
