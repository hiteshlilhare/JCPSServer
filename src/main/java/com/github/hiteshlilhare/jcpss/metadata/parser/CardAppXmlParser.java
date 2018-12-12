package com.github.hiteshlilhare.jcpss.metadata.parser;

import com.github.hiteshlilhare.jcpss.metadata.bean.CardAppMetaData;
import java.io.IOException;
import java.io.InputStream;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class CardAppXmlParser {

    public CardAppMetaData parseXml(InputStream in) {
        //Create a empty link of users initially
        CardAppMetaData appMetaData = new CardAppMetaData();
        try {
            //Create default handler instance
            CardAppXmlParserHandler handler = new CardAppXmlParserHandler();

            //Create parser from factory
            XMLReader parser = XMLReaderFactory.createXMLReader();

            //Register handler with parser
            parser.setContentHandler(handler);

            //Create an input source from the XML input stream
            InputSource source = new InputSource(in);

            //parse the document
            parser.parse(source);

            //populate the parsed users list in above created empty list; You can return from here also.
            appMetaData = handler.getAppMetaData();

        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {

        }
        return appMetaData;
    }

    public void parseXml(InputStream in, CardAppMetaData appMetaData) {
        try {
            //Create default handler instance
            CardAppXmlParserHandler handler = new CardAppXmlParserHandler();

            //Create parser from factory
            XMLReader parser = XMLReaderFactory.createXMLReader();

            //Register handler with parser
            parser.setContentHandler(handler);

            //Create an input source from the XML input stream
            InputSource source = new InputSource(in);

            //parse the document
            parser.parse(source);

            //populate the parsed users list in above created empty list; You can return from here also.
            handler.getAppMetaData(appMetaData);

        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {

        }

    }
}
