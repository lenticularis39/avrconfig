package avrconfig.data;

import avrconfig.MainController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.lang.reflect.*;
import java.io.*;

public class ConfigurationFile {

    String filename;
    public MainController s;

    public ConfigurationFile(String filename, MainController s)  {
        this.filename = filename;
        this.s = s;
    }

    private String getFirstLevelTextContent(Node node) {
        NodeList list = node.getChildNodes();
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < list.getLength(); ++i) {
            Node child = list.item(i);
            if ((child.getNodeType() == Node.TEXT_NODE) && !child.getTextContent().matches("\\s+")) {
                s.append(child.getTextContent());
            }
        }
        return s.toString();
    }

    public void load() throws IOException {
        try {
            // Load DOM document and normalize it
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document xml = builder.parse(new File(filename));
            xml.getDocumentElement().normalize();

            // Check if the correct root element exists
            Element rootElement = xml.getDocumentElement();
            if(!rootElement.getNodeName().equals("config")) throw new SAXException("Invalid config file");

            // Parse child notes into elements and check if they are in fields - if yes, set them
            List<Field> fields = Arrays.asList(MainController.class.getFields());

            NodeList childNodes = rootElement.getChildNodes();
            for(int i = 0; i < childNodes.getLength(); i++) {
                if(childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element)childNodes.item(i);

                    // Text fields
                    if(e.hasAttribute("id") && e.getNodeName().equals("textfield")) {
                        for(Field f : fields) {
                            if(f.getName().equals(e.getAttribute("id"))) {
                                ((TextField)f.get(s)).setText(getFirstLevelTextContent(e));
                            }
                        }
                    }

                    // Choice boxes
                    if(e.hasAttribute("id") && e.getNodeName().equals("choicebox")) {
                        for(Field f : fields) {
                            if(f.getName().equals(e.getAttribute("id"))) {
                                ((ChoiceBox)f.get(s)).setValue(getFirstLevelTextContent(e));

                                // Iterate over choices and set them
                                NodeList children = e.getChildNodes();
                                ObservableList<String> list = FXCollections.observableArrayList();
                                for(int j = 0; j < children.getLength(); j++) {
                                    if(children.item(j).getNodeType() == Node.ELEMENT_NODE) {
                                        list.add(children.item(j).getTextContent());
                                    }
                                }
                                ((ChoiceBox)f.get(s)).setItems(list);

                            }
                        }
                    }

                    // Check boxes
                    if(e.hasAttribute("id") && e.getNodeName().equals("checkbox")) {
                        for(Field f : fields) {
                            if(f.getName().equals(e.getAttribute("id"))) {
                                ((CheckBox)f.get(s)).setSelected(true);
                            }
                        }
                    }
                }
            }


        } catch (SAXException | ParserConfigurationException | IllegalAccessException se) {se.printStackTrace();}
    }

    public void save() throws IOException {
        try {
            // Create DOM document for the config file and its root element
            Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element rootElement = xml.createElement("config");
            xml.appendChild(rootElement);

            // Retrieve fields in the window using reflection, transform them into elements and add the elements to the document
            Field[] fields = MainController.class.getFields();
            for (Field f: fields) {
                // Text fields
                if(f.getType().equals(TextField.class)) {
                    String value = ((TextField)f.get(s)).getText();
                    if(!value.equals("")) {
                        Element e = xml.createElement("textfield");
                        e.setAttribute("id", f.getName());
                        e.setTextContent(value);
                        rootElement.appendChild(e);
                    }
                }

                // Choice boxes
                if(f.getType().equals(ChoiceBox.class) && !f.getName().matches("readFormat.*")) {
                    Object value = ((ChoiceBox)f.get(s)).getValue();
                    if(value != null && !value.equals("")) {
                        Element e = xml.createElement("choicebox");
                        e.setAttribute("id", f.getName());
                        e.appendChild(xml.createTextNode(value.toString()));

                        // Iterate over choices
                        ObservableList<Object> items = ((ChoiceBox)f.get(s)).getItems();

                        for(Object item : items) {
                            Element child = xml.createElement("choice");
                            child.appendChild(xml.createTextNode(item.toString()));
                            e.appendChild(child);
                        }

                        rootElement.appendChild(e);
                    }
                }

                // Check boxes - if checked, store, otherwise skip
                if(f.getType().equals(CheckBox.class)) {
                    boolean value = ((CheckBox)f.get(s)).isSelected();

                    Element e = xml.createElement("checkbox");
                    e.setAttribute("id", f.getName());

                    if(value) {
                        rootElement.appendChild(e);
                    }

                }
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
            StreamResult result = new StreamResult(new File(filename));

            transformer.transform(new DOMSource(xml), result);
        } catch (IllegalAccessException iae) {iae.printStackTrace();}
          catch(ParserConfigurationException pce) {pce.printStackTrace();}
          catch(TransformerConfigurationException tce) {tce.printStackTrace();}
        catch(TransformerException te) {te.printStackTrace();}
    }
}