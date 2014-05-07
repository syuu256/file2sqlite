package net._instanceof.batch.file2sqlite.main;

import net._instanceof.batch.file2sqlite.config.ImportConfig;
import net._instanceof.batch.file2sqlite.config.XMLReader;
import net._instanceof.batch.file2sqlite.db.DataBaseManager;
import net._instanceof.batch.file2sqlite.db.DataBaseManagerFactory;
import net._instanceof.commons.daemon.Bootable;
import net._instanceof.commons.util.reflect.DynamicBeanFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 */
public class DataTransfer implements Bootable {

	private static Logger log = LoggerFactory.getLogger(DataTransfer.class);

	public int start() {

        final ImportConfig config = new XMLReader().getConfig();
        final DataBaseManagerFactory factory = new DataBaseManagerFactory();
        final DataBaseManager dataBaseManager = factory.createDataBaseManager(config);
        if (dataBaseManager.isDatabaseInstance()) {
            dataBaseManager.dropDatabase();
        }

        int r = 0;
        try {
			DynamicBeanFactory.create(config.getDataImporterClass(), DataImporter.class)
			    .setConfig(config)
			    .setDataBaseManager(dataBaseManager.connectDatabase())
			    .createDataBaseSchema()
			    .load()
			    .createInternalTable();
		} catch (Exception e) {
			r = -1;
	        if (log.isErrorEnabled()) {
	        	log.error("ERROR:", e);
	        }
		}
        dataBaseManager.close();
        
        return r;
	}
}
