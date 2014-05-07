/*
 * Copyright(C) 2014 syuu256\gmail.com. All Rights Reserved.
 */
package net._instanceof.batch.file2sqlite.db;

import net._instanceof.batch.file2sqlite.config.ImportConfig;
import net._instanceof.batch.file2sqlite.db.impl.DataBaseManagerSqlite;
import net._instanceof.commons.util.reflect.DynamicBeanFactory;

public class DataBaseManagerFactory {

	private static DataBaseManager dataBaseManagerinstance = null;

	public DataBaseManager createDataBaseManager(final ImportConfig hcgConfig) {

		synchronized (DataBaseManagerFactory.class) {

			if (dataBaseManagerinstance == null) {
			
				final String clazz = hcgConfig.getPublicEnv().get(ImportConfig.DATA_BASE_MANAGER);
				dataBaseManagerinstance = loadInstance(
						clazz, DataBaseManager.class, 
						DataBaseManagerSqlite.class).initialize(hcgConfig);
			}
		}
		return dataBaseManagerinstance;
	}

	private <T> T loadInstance(final String clazzName, final Class<T> clazz, final Class<? extends T> defaultImpl) {
		
		T instance = null;
		if (clazzName == null || clazzName.isEmpty()) {
			instance = DynamicBeanFactory.loadInstanceByType(defaultImpl);
		} else {
			instance = DynamicBeanFactory.create(clazzName, clazz);
		}

		return instance;
	}

}
