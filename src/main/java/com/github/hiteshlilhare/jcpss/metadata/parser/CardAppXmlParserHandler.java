package com.github.hiteshlilhare.jcpss.metadata.parser;

import com.github.hiteshlilhare.jcpss.metadata.bean.Author;
import com.github.hiteshlilhare.jcpss.metadata.bean.CardAppMetaData;
import com.github.hiteshlilhare.jcpss.metadata.bean.User;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Inspired by: https://howtodoinjava.com/xml/sax-parser-read-xml-example/
 *
 * @author Hitesh
 */
public class CardAppXmlParserHandler extends DefaultHandler {

    //Meta data object corresponding to app.
    private CardAppMetaData appMetaData = new CardAppMetaData();

    //As we read any XML element we will push that in this stack
    private Stack<String> elementStack = new Stack<String>();

    //As we complete one user block in XML, we will push the CardAppMetaData 
    //instance in userList 
    private Stack<Object> objectStack = new Stack<Object>();

    public void startDocument() throws SAXException {
        //System.out.println("start of the document   : ");
    }

    public void endDocument() throws SAXException {
        //System.out.println("end of the document document     : ");
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        //Push it in element stack
        this.elementStack.push(qName);

        //If this is start of 'user' element then prepare a new CardAppMetaData
        //instance and push it in object stack
        if ("author".equalsIgnoreCase(qName)
                && this.elementStack.search("authors") == 2) {
            //New Author instance
            Author author = new Author();
            this.objectStack.push(author);
        } else if ("user".equalsIgnoreCase(qName)
                && this.elementStack.search("users") == 2) {
            //New User instance
            User user = new User();
            this.objectStack.push(user);
        }
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        //Remove last added </user> element
        this.elementStack.pop();

        //User instance has been constructed so pop it from object stack 
        //and push in userList
        if ("author".equalsIgnoreCase(qName)
                && this.elementStack.search("authors") == 1) {
            Author object = (Author) this.objectStack.pop();
            appMetaData.addAuthor(object);
        } else if ("user".equalsIgnoreCase(qName)
                && this.elementStack.search("users") == 1) {
            //New User instance
            User object = (User) this.objectStack.pop();
            appMetaData.addUser(object);
        }
    }

    /**
     * This will be called everytime parser encounter a value node
     *
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        String value = new String(ch, start, length).trim();

        if (value.length() == 0) {
            return; // ignore white space
        }

        if ("name".equalsIgnoreCase(currentElement())) {
            if (this.elementStack.search("author") == 2) {
                ((Author) this.objectStack.peek()).setName(value);
            } else {
                appMetaData.setName(value);
            }
        } else if ("web-site".equalsIgnoreCase(currentElement())) {
            if (this.elementStack.search("author") == 2) {
                ((Author) this.objectStack.peek()).setWebsite(value);
            } else {
                appMetaData.setWebsite(value);
            }
        } else if ("company".equalsIgnoreCase(currentElement())) {
            appMetaData.setCompany(value);
        } else if ("license".equalsIgnoreCase(currentElement())) {
            appMetaData.setLicense(value);
        } else if ("cap-file".equalsIgnoreCase(currentElement())) {
            appMetaData.setCapFile(value);
        } else if ("icon-file-cart".equalsIgnoreCase(currentElement())) {
            appMetaData.setCartIconFile(value);
        } else if ("icon-file-desc".equalsIgnoreCase(currentElement())) {
            appMetaData.setDescIconFile(value);
        } else if ("version".equalsIgnoreCase(currentElement())) {
            appMetaData.setVersion(value);
        } else if ("signed-by".equalsIgnoreCase(currentElement())) {
            appMetaData.setSignedBy(value);
        } else if ("release-date".equalsIgnoreCase(currentElement())) {
            appMetaData.setReleaseDate(value);
        } else if ("description".equalsIgnoreCase(currentElement())) {
            appMetaData.setDescription(value);
        } else if ("appstore-repository".equalsIgnoreCase(currentElement())) {
            appMetaData.setAppstoreRepository(value);
        } else if ("appsource-repository".equalsIgnoreCase(currentElement())) {
            appMetaData.setAppsourceRepository(value);
        } else if ("app-name".equalsIgnoreCase(currentElement())) {
            appMetaData.setAppName(value);
        } else if ("e-mail".equalsIgnoreCase(currentElement())) {
            if (this.elementStack.search("author") == 2) {
                ((Author) this.objectStack.peek()).setEmail(value);
            }
            //else if (this.elementStack.search("user") == 2) {
            //    ((User) this.objectStack.peek()).setEmail(value);
            //}
        } //else if ("raiting".equalsIgnoreCase(currentElement())) {
        //    if (this.elementStack.search("user") == 2) {
        //        try {
        //            ((User) this.objectStack.peek()).setRating(
        //                    Integer.parseInt(value));
        //        } catch (NumberFormatException e) {
        //            ((User) this.objectStack.peek()).setRating(0);
        //        }
        //    }
        //} 
        //else if ("comment".equalsIgnoreCase(currentElement())) {
        //    if (this.elementStack.search("user") == 2) {
        //        ((User) this.objectStack.peek()).setComment(value);
        //    }
        //} 
        else if ("feature".equalsIgnoreCase(currentElement())) {
            if (this.elementStack.search("features") == 2) {
                appMetaData.addFeature(value);
            }
        }
        //else if ("reporitory".equalsIgnoreCase(currentElement())) {
        //    if (this.elementStack.search("repositories") == 2) {
        //        appMetaData.addRepository(value);
        //    }
        //} 
        //else if ("discussion".equalsIgnoreCase(currentElement())) {
        //    if (this.elementStack.search("discussions") == 2) {
        //        appMetaData.addDiscussion(value);
        //    }
        //}

    }

    /**
     * Utility method for getting the current element in processing
     *
     */
    private String currentElement() {
        return this.elementStack.peek();
    }

    public CardAppMetaData getAppMetaData() {
        return appMetaData;
    }

    public void getAppMetaData(CardAppMetaData appMetaData) {
        this.appMetaData = appMetaData;
    }
}
