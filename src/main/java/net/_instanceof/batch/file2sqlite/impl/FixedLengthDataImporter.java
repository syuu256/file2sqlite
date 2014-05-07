/*
 * Copyright(C) 2014 syuu256\gmail.com. All Rights Reserved.
 */
package net._instanceof.batch.file2sqlite.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import net._instanceof.batch.file2sqlite.config.ImportDataMetaData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * 固定長ファイル用.
 * サポート無し
 */
public class FixedLengthDataImporter extends CSVDataImporter {

	private static Logger log = LoggerFactory.getLogger(FixedLengthDataImporter.class);

	private static final String LINE_TOKEN = "\r\n";
	private static final int LINE_TOKEN_SIZE = LINE_TOKEN.length();
	
	private Charset charset = null;
	
	protected CSVDataImporter loadImpl() throws IOException, SQLException {
		
		if (log.isDebugEnabled()) {
			log.debug("FixedLengthDataImporter");
		}

		charset = Charset.forName(config.getPublicEnv().get(CSV_CHAR_SET));

		for (final ImportDataMetaData importDataMetaData : importFiles.values()) {
		
			final String filePath = importDataMetaData.filePath + "/" + src_dir + "/" + importDataMetaData.fileName;
			if (log.isDebugEnabled()) {
				log.debug("filePath:" + filePath);
			}

			final File file = new File(filePath);
			if (!file.exists()) {
				log.info(filePath + ":nothing");
				continue;
			}

			if (file.length() <= 0) {
				log.info(filePath + ":0byte");
				continue;
			}

			final String[] headers = importDataMetaData.csvHeader.split(",");
			final List<String> head_list = Arrays.asList(headers);
			final int[] size_array = toIntArray(config.getPublicEnv().get("DATA_LEN." + importDataMetaData.id).split(","));
			final byte[] record_array =  new byte[sumArray(size_array)];
			if (log.isDebugEnabled()) {
				log.debug("record_array.length:" + record_array.length);
			}

			final Connection connection = dataBaseManager.getConnection();
	        final PreparedStatement preparedStatement = connection.prepareStatement(buildSql(headers, importDataMetaData.tableName));

			final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");

			for (int i = 0; ; i++) {

				final byte[] record_id_array = new byte[1];
				randomAccessFile.readFully(record_id_array);
				// 戻す
				randomAccessFile.seek(randomAccessFile.getFilePointer() - record_id_array.length);

				final String record_id = new String(record_id_array, charset);
				if ("9".equals(record_id)) break; // ENDレコード
				if (!"2".equals(record_id)) {
					randomAccessFile.seek(randomAccessFile.getFilePointer() + sumArray(size_array));
					continue;
				}

				randomAccessFile.readFully(record_array);
				if (log.isDebugEnabled()) {
					//log.debug("(" + i + ")" + new String(record_array, charset));
				}

				final List<String> list = div(size_array, record_array);

				if (log.isInfoEnabled()) {
					log.info("[" + i + "]" + list);
				}

				insert(list, head_list, preparedStatement);
				if (i % 10000 == 0) {
					preparedStatement.executeBatch();
					preparedStatement.clearBatch();
					log.info(importDataMetaData.tableName + ":" + i);
				}
			}

			randomAccessFile.close();
			
	        preparedStatement.executeBatch();
	        preparedStatement.close();
	        connection.commit();
		}
		
		return this;
	}


	private List<String> div(final int[] th_size_array, final byte[] th_byte) {
		final List<String> list = Lists.newArrayList();
		int start = 0;
		for (final int size : th_size_array) {
			final String value = new String(th_byte, start, size, charset);
			final String s = trim(value);
			if (log.isDebugEnabled()) {
				//log.debug("start:" + start + "/size:" + size + "/value:" + value + "/s:" + s);
			}
			list.add(s);
			start += size;
		}
		return list;
	}

	private static int sumArray(final int[] array) {
		int r = LINE_TOKEN_SIZE;
		for (int i : array) r += i;
		return r;
	}

	private static int[] toIntArray(final String[] array) {
		final int[] r = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			final String x = array[i].trim();
			r[i] = Integer.parseInt(x);
		}
		return r;
	}

	private static String trim(String value) {

		if (value == null || value.isEmpty()) return value;

		int beginIndex = 0;
		for (int i = 0; i < value.length(); i++) {
			final char c = value.charAt(i);
            if (c != 0x20 && c != 0x3000) {
            	break;
            }
            beginIndex++;
		}
		value = value.substring(beginIndex);
		
		int endIndex = value.length();
		for (int i = value.length() - 1; i >= 0; i--) {
			final char c = value.charAt(i);
            if (c != 0x20 && c != 0x3000) {
            	break;
            }
            endIndex = i;
		}
		value = value.substring(0, endIndex);
		
		return value;
	}
}
