/*
 * Copyright(C) 2014 syuu256\gmail.com. All Rights Reserved.
 */
package net._instanceof.batch.file2sqlite.config;

import java.util.Collections;
import java.util.Map;

import net._instanceof.commons.util.ToStringUtil;

import com.google.common.collect.Maps;

/**
 *
 */
public class ImportConfig {

    /** -D param key */
    public static final String SRC_DIR_KEY = ImportConfig.class.getPackage().getName() + ".src_dir";

	public static final String DATA_BASE_MANAGER = "DATA_BASE_MANAGER";

	private static final ImportConfig instance = new ImportConfig();

	public boolean load = false;
	
	private Map<String, String> publicEnv = Maps.newHashMap();
	
	private String dataImporterClass = "";

	private Map<String, ImportDataMetaData> importFiles = Maps.newHashMap();

	private ImportConfig() {
		
	}

	/**
	 * single
	 * @return instance
	 */
	public static ImportConfig getInstance() {
		return instance;
	}

	public Map<String, String> getPublicEnv() {

		return Collections.unmodifiableMap(publicEnv);
	}

	public void putPublicEnv(final String key, final String value) {

		publicEnv.put(key, value);
	}

	public void addImportDataMetaData(final ImportDataMetaData importDataMetaData) {

		importFiles.put(importDataMetaData.id, importDataMetaData);
	}

	public Map<String, ImportDataMetaData> getImportFiles() {

		return Collections.unmodifiableMap(importFiles);
	}

	public String getDataImporterClass() {
		return dataImporterClass;
	}

	public void setDataImporterClass(String dataImporterClass) {
		this.dataImporterClass = dataImporterClass;
	}

	@Override
	public String toString() {
		return ToStringUtil.buildToString(this);
	}
}
