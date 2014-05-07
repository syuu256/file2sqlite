/*
 * Copyright(C) 2014 syuu256\gmail.com. All Rights Reserved.
 */
package net._instanceof.batch.file2sqlite.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net._instanceof.batch.file2sqlite.config.ImportConfig;
import net._instanceof.batch.file2sqlite.config.ImportDataMetaData;
import net._instanceof.batch.file2sqlite.db.DataBaseManager;
import net._instanceof.batch.file2sqlite.main.DataImporter;
import net._instanceof.commons.datasource.DBUtil;
import net._instanceof.commons.util.ResourcesUtil;
import net._instanceof.commons.util.csv.CSVParser;
import net._instanceof.commons.util.csv.CSVParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class CSVDataImporter implements DataImporter {


	protected static final String CSV_CHAR_SET = "CSV_CHAR_SET";

	protected static final String CSV_COLUMN_TOKEN = "CSV_COLUMN_TOKEN";

	protected static final String CSV_HEAD_PARSE = "CSV_HEAD_PARSE";

	private static Logger log = LoggerFactory.getLogger(CSVDataImporter.class);
	
	protected ImportConfig config = null;
	
	protected Map<String, ImportDataMetaData> importFiles = null;
	
	protected DataBaseManager dataBaseManager = null;

	protected String src_dir = "";
	
	@Override
	public CSVDataImporter setConfig(final ImportConfig config) {
		this.config = config;
		this.importFiles = config.getImportFiles();
        this.src_dir = ResourcesUtil.getSystemValue(ImportConfig.SRC_DIR_KEY, "");
		return this;
	}
	
	@Override
	public CSVDataImporter setDataBaseManager(final DataBaseManager dataBaseManager) {

		this.dataBaseManager = dataBaseManager;
		return this;
	}

	@Override
	public DataImporter createDataBaseSchema() {

		final Connection connection = dataBaseManager.getConnection();
		for (final ImportDataMetaData importDataMetaData : importFiles.values()) {
			String schemas[]=importDataMetaData.schema.split(";");
			for (String schema : schemas ) {
				schema = schema.trim();
				schema = schema.replaceAll("\n", "");
				if ( !schema.isEmpty() ) {
					DBUtil.executeStatement(connection, schema);
				}
			}
		}
		DBUtil.commit(connection);

		return this;
	}

	@Override
	public CSVDataImporter load() {
		try {
			return loadImpl();
		} catch (IOException | SQLException e) {
			throw new RuntimeException(e);
		}
	}

	protected CSVDataImporter _loadImpl() throws IOException, SQLException {

		for (final ImportDataMetaData importDataMetaData : importFiles.values()) {

			final String filePath = importDataMetaData.filePath + "/" + src_dir + "/" + importDataMetaData.fileName;
			if (log.isDebugEnabled()) {
				log.debug("filePath:" + filePath);
			}

			final File file = new File(filePath);
			if (!file.exists()) {
				if (log.isInfoEnabled()) {
					log.info(filePath + ":nothing");
				}
				continue;
			}

			transferTokenFile(importDataMetaData, file);
		}

		return this;
	}

	protected void transferTokenFile(final ImportDataMetaData importDataMetaData, final File file) throws IOException, SQLException {

		final String charsetName = config.getPublicEnv().get(CSV_CHAR_SET);
		final char token = config.getPublicEnv().get(CSV_COLUMN_TOKEN).charAt(0);
		final Charset charset = Charset.forName(charsetName);

		final InputStream inputStream = new FileInputStream(file);
		final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset);
		final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

		final String[] headers = importDataMetaData.csvHeader.split(",");
		final List<String> head_list = Arrays.asList(headers);

		final Connection connection = dataBaseManager.getConnection();

        final PreparedStatement preparedStatement = connection.prepareStatement(buildSql(headers, importDataMetaData.tableName));

		int i = 0;
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			i++;
			
			if (line == null || line.isEmpty()) {
				if (log.isWarnEnabled()) {
					log.warn("line null: line_number:" + i);
				}
				continue;
			}
			
			insert(line, token, head_list, preparedStatement);
			line = null;
			if (i % 10000 == 0) {
				preparedStatement.executeBatch();
				preparedStatement.clearBatch();
				if (log.isInfoEnabled()) {
					log.info(importDataMetaData.tableName + ":" + i);
				}
			}
		}
		bufferedReader.close();
		
        preparedStatement.executeBatch();
        preparedStatement.close();
        
        connection.commit();
	}

	protected void insert(final String line, final char token, final List<String> head_list, final PreparedStatement preparedStatement) throws SQLException {
		 try {
			insert(Arrays.asList(split(line, token)), head_list, preparedStatement);
		} catch (RuntimeException e) {
			if (log.isErrorEnabled()) {
				log.error("line:" + line);
			}
			throw e;
		}
	}

	protected void insert(final List<String> items, final List<String> head_list, final PreparedStatement preparedStatement) throws SQLException {

        if (head_list.size() != items.size()) {
        	String message = "/header:" + head_list.size() + "/item:" + items.size();
        	log.error(message);
            final Exception e =  new IllegalArgumentException("headers size error header:"
                    + head_list.size() + "/item:" + items.size() + "/data:" + items.toString());
            throw new RuntimeException("", e);
        }

        final Map<String, String> m = new HashMap<String, String>();

        for (int i = 0; i < items.size(); i++) {
            final String key = head_list.get(i);
            final String value = items.get(i);
            final String old = m.put(key, value);
            if (old != null) {
                throw new RuntimeException("CSVParser:headers key Unique error header");
            }
        }

        insert(m, preparedStatement, head_list.toArray(new String[head_list.size()]));
	}
	
	private String[] split(final String str, final char token) {
		
		final List<String> list = Lists.newArrayList();
		final StringBuilder sb = new StringBuilder();
		for (int i  = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (ch == token) {
				list.add(sb.toString());
				sb.setLength(0);
				continue;
			}
			sb.append(ch);
		}
		
		list.add(sb.toString());
		
		return list.toArray(new String[list.size()]);
	}
	
	protected CSVDataImporter loadImpl() throws IOException, SQLException {

		final String charsetName = config.getPublicEnv().get(CSV_CHAR_SET);
		final Charset charset = Charset.forName(charsetName);
		
		for (final ImportDataMetaData importDataMetaData : importFiles.values()) {

			final String filePath = importDataMetaData.filePath + "/" + src_dir + "/" + importDataMetaData.fileName;

			final File file = new File(filePath);
			String text = "";
			try {
				text = Files.toString(file, charset);
			} catch (IOException e) {
				if (log.isErrorEnabled()) {
					log.error("file Error:" + file, e);
				}
				throw new RuntimeException(e);
			}
			
			final String[] headers = importDataMetaData.csvHeader.split(",");

			
			final boolean isHeaderParse = Boolean.parseBoolean(config.getPublicEnv().get(CSV_HEAD_PARSE));
			
			final char token = config.getPublicEnv().get(CSV_COLUMN_TOKEN).charAt(0);
			final CSVParser csvParser = new CSVParserFactory().createCSVParser("");
			csvParser.setHeaderParse(isHeaderParse);
			csvParser.setHeaders(Arrays.asList(headers));
			csvParser.setValueSparator(token);

			List<Map<String, String>> list = null;
			try {
				list = csvParser.parse(text);
			} catch (Exception e) {
				if (log.isErrorEnabled()) {
					log.error("CSV Error:" + file, e);
				}
				throw new RuntimeException(e);
			}

			try {
				inserts(list, importDataMetaData.tableName, headers);
			} catch (SQLException e) {
				if (log.isErrorEnabled()) {
					log.error("insert Error:" + file, e);
				}
				throw new RuntimeException(e);
			}
			
			if (log.isInfoEnabled()) {
				log.info("CSV import complete!!:" + importDataMetaData.fileName);
			}
		}

		return this;
	}

    private void inserts(final List<Map<String, String>> list,
    		final String tableName, final String[] headers) throws SQLException {

		final Connection connection = dataBaseManager.getConnection();

        final PreparedStatement preparedStatement = 
        	connection.prepareStatement(buildSql(headers, tableName));;

        for (final Map<String, String> map : list) {
            insert(map, preparedStatement, headers);
        }

        preparedStatement.executeBatch();
        preparedStatement.close();
        
        connection.commit();
    }

    private void insert(final Map<String, String> map, 
    	final PreparedStatement preparedStatement, final String[] headers) throws SQLException {
        for (int i = 0; i < headers.length; i++) {
            preparedStatement.setString(i + 1, map.get(headers[i]));
        }
        preparedStatement.addBatch();
    }

    protected String buildSql(final String[] headers, final String tableName) {
    	
        final StringBuilder sb = new StringBuilder("insert into " + tableName + " (");
        for (final String header : headers) {
            sb.append(header + ",");
        }
        sb.setCharAt(sb.length() - 1, ')');
        sb.append("values(");		
        for (int i = 0; i < headers.length; i++) {
            sb.append("?,");
        }
        sb.setCharAt(sb.length() - 1, ')');
        final String sql = sb.toString();
        
        if (log.isDebugEnabled()) {
        	log.debug(sql);
        }

    	return sql;
    }

	@Override
	public DataImporter createInternalTable() {
		return this;
	}
}
