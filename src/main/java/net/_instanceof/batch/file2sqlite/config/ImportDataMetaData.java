/*
 * Copyright(C) 2014 syuu256\gmail.com. All Rights Reserved.
 */
package net._instanceof.batch.file2sqlite.config;

import net._instanceof.commons.util.ToStringUtil;

/**
 * 
 */
public class ImportDataMetaData {

	public String id = "";

	public String filePath = "";

	public String fileName = "";
	
	public String tableName = "";
	
	public String schema = "";
	
	public String csvHeader = "";
	
	@Override
	public String toString() {
		return ToStringUtil.buildToString(this);
	}
}
