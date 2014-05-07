/*
 * Copyright(C) 2014 syuu256\gmail.com. All Rights Reserved.
 */
package net._instanceof.batch.file2sqlite.db;

import java.sql.Connection;

import net._instanceof.batch.file2sqlite.config.ImportConfig;

public interface DataBaseManager {

	DataBaseManager initialize(final ImportConfig config);
	
	boolean isDatabaseInstance();
	
	DataBaseManager dropDatabase();
	
	DataBaseManager connectDatabase();

	Connection getConnection();

	DataBaseManager close();
}
