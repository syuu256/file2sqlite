/*
 * Copyright(C) 2014 syuu256\gmail.com. All Rights Reserved.
 */
package net._instanceof.batch.file2sqlite.main;

import net._instanceof.batch.file2sqlite.config.ImportConfig;
import net._instanceof.batch.file2sqlite.db.DataBaseManager;


public interface DataImporter {

	DataImporter setDataBaseManager(final DataBaseManager dataBaseManager);
	
	DataImporter setConfig(final ImportConfig hcgConfig);
	
	DataImporter load();
	
	DataImporter createDataBaseSchema();
	
	DataImporter createInternalTable();
	
}
