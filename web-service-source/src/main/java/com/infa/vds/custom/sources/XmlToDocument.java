package com.infa.vds.custom.sources;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

public class XmlToDocument {
	InputStream m_InputStream;
	Document m_Document;
	String m_XmlString;

	XmlToDocument(InputStream xmlStream) {
		m_InputStream = xmlStream;
	}

	XmlToDocument(String xmlString) {
		m_InputStream = new ByteArrayInputStream(xmlString.getBytes());
	}
	
	XmlToDocument(File xmlFile) throws Exception {
		m_InputStream = new FileInputStream(xmlFile);
	}

	public Document getDocument() {
		return m_Document;
	}
	
	void printElements(NodeList nodes, String path, int childCount) {
		int length = nodes.getLength();
		for (int nodeIndex = 0; nodeIndex < length; nodeIndex++) {
			Node node = nodes.item(nodeIndex);
			short nodeType = node.getNodeType();
			String name = node.getNodeName();
			if (nodeType != Node.TEXT_NODE ) {
				if (node.hasChildNodes()) {
					printElements(node.getChildNodes(), path + "." + name, length);
				}
			} else {
				if (length == 1) {
					System.out.printf("%s: <%s>\n", path, node.getTextContent());
				}
			}
		}

	}

	void initialize() {
		try {

			//File fXmlFile = new File(m_InputStream);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			m_Document = dBuilder.parse(m_InputStream);

			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			m_Document.getDocumentElement().normalize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void print() {
		try {

			System.out.println("Root element :" + m_Document.getDocumentElement().getNodeName());

			NodeList nodes = m_Document.getChildNodes();
			printElements(nodes, m_Document.getNodeName(), nodes.getLength());
			System.out.println("----------------------------");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	String getXpathString(String xPathString) {
		XPath xPath = XPathFactory.newInstance().newXPath();
		String stringVal;
		try {
			stringVal = (String) xPath.compile(xPathString).evaluate(m_Document, XPathConstants.STRING);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return stringVal;
	}

}
