/*
 * Copyright(C) 2014 syuu256\gmail.com. All Rights Reserved.
 */
package net._instanceof.batch.file2sqlite.config;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net._instanceof.batch.file2sqlite.ImporterException;
import net._instanceof.commons.util.ResourcesUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 */
public class XMLReader {

	private static Logger log = LoggerFactory.getLogger(XMLReader.class);

    /** -D param key */
    private static final String XML_PATH_KEY = XMLReader.class.getPackage().getName() + ".file";

    /** XPath */
    protected XPath xpath;

    /** Constructor */
    public XMLReader() {
        xpath = XPathFactory.newInstance().newXPath();
    }

    private XMLReader read() {

        final ImportConfig config = ImportConfig.getInstance();
        if (config.load) {
            return this;
        }

        final Document document = getRootDocument();
        try {
            loadEnv(document, config);
            loadImporter(document, config);
        } catch (XPathExpressionException e) {
            if (log.isErrorEnabled()) {
                log.error("XMLReader", e);
            }
            throw new ImporterException(e);
        }
        config.load = true;
        return this;
    }

    private void loadEnv(final Document document, final ImportConfig config) throws XPathExpressionException {
        final NodeList nodes = evaluate("/config/public_env/property", document, NodeList.class);
        for (int i = 0; i < nodes.getLength(); i++) {
            final Element property = Element.class.cast(nodes.item(i));
            config.putPublicEnv(
                property.getAttribute("key"),
                property.getAttribute("value"));
        }
    }

    private void loadImporter(final Document document, final ImportConfig config) throws XPathExpressionException {

        final String clazz = evaluate("/config/input_source/data_importer/@class", document, String.class);
        config.setDataImporterClass(clazz);

        final NodeList nodes = evaluate("/config/input_source/data_importer/file", document, NodeList.class);
        for (int i = 0; i < nodes.getLength(); i++) {

            final Element files = Element.class.cast(nodes.item(i));

            final ImportDataMetaData importDataMetaData = new ImportDataMetaData();

            importDataMetaData.id = files.getAttribute("id");
            importDataMetaData.filePath = files.getAttribute("path");
            importDataMetaData.fileName = files.getAttribute("src");
            importDataMetaData.tableName = files.getAttribute("table");
            importDataMetaData.csvHeader = files.getAttribute("csv_header");
            importDataMetaData.schema = evaluate("./schema/text()", files, String.class);

            config.addImportDataMetaData(importDataMetaData);
        }
    }

    public ImportConfig getConfig() {
        read();
        return ImportConfig.getInstance();
    }

    private <T> T evaluate(final String path, final Object item, final Class<T> clazz) throws XPathExpressionException {

        QName qName = null;
        if (String.class == clazz)
            qName = XPathConstants.STRING;

        if (NodeList.class == clazz)
            qName = XPathConstants.NODESET;

        final Object returnValue = xpath.evaluate(path, item, qName);

        return clazz.cast(returnValue);
    }

    private Document getRootDocument() {

        Document document = null;
        try {
            final DocumentBuilderFactory documentBuilderFactory
                = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            try (final InputStream inputStream = getInputStream();) {
            	document = documentBuilder.parse(inputStream);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("getRootDocument:", e);
            }
            throw new ImporterException(e);
        }
        return document;
    }

    private InputStream getInputStream() throws IOException {
        return XMLReader.class.getResourceAsStream("/" + ResourcesUtil.getSystemValue(XML_PATH_KEY));
    }
}
