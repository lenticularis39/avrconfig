package avrconfig.data;

import avrconfig.MainController;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeType;
import javafx.scene.control.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.lang.reflect.*;
import java.util.List;
import java.util.stream.Stream;
import java.io.*;

public class ConfigurationFile implements Serializable {
    /*Hashtable<String, String> chips;
    String selectedChip;
    Hashtable<String, String> programmers;
    String selectedProgrammer;
    String execPath;
    String configPath;
    String port;
    String baudrate;*/

    transient String filename;
    transient public MainController s;

    public ConfigurationFile(String filename, MainController s)  {
        this.filename = filename;
        /*chips = s.chips;
        programmers = s.programmers;
        execPath = s.execTextField.getText();
        configPath = s.configTextField.getText();
        selectedChip = (String)s.microcontrollerChoiceBox.getValue();
        selectedProgrammer = (String)s.programmersChoiceBox.getValue();
        port = s.portTextField.getText();
        baudrate = s.baudrateTextField.getText();*/
        this.s = s;
    }

    public void load() throws IOException {
        /*s.chips = this.chips;
        s.programmers = this.programmers;
        s.updateLists();
        s.execTextField.setText(this.execPath);
        s.configTextField.setText(this.configPath);
        s.microcontrollerChoiceBox.setValue(this.selectedChip);
        s.programmersChoiceBox.setValue(this.selectedProgrammer);
        s.portTextField.setText(this.port);
        s.baudrateTextField.setText(this.baudrate);*/
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
                    System.out.println("Detected element node!");
                    Element e = (Element)childNodes.item(i);

                    // Text fields
                    if(e.hasAttribute("id") && e.getNodeName().equals("textfield")) {
                        for(Field f : fields) {
                            if(f.getName().equals(e.getAttribute("id"))) {
                                ((TextField)f.get(s)).setText(e.getTextContent());
                            }
                        }
                    }

                    // Choice boxes
                    if(e.hasAttribute("id") && e.getNodeName().equals("choicebox")) {
                        for(Field f : fields) {
                            if(f.getName().equals(e.getAttribute("id"))) {
                                ((ChoiceBox)f.get(s)).setValue(e.getTextContent());
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


        } catch (SAXException se) {se.printStackTrace();}
          catch(ParserConfigurationException pce) {pce.printStackTrace();}
          catch(IllegalAccessException iae) {iae.printStackTrace();}
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
                if(f.getType().equals(ChoiceBox.class)) {
                    Object value = ((ChoiceBox)f.get(s)).getValue();
                    if(value != null && !value.equals("")) {
                        Element e = xml.createElement("choicebox");
                        e.setAttribute("id", f.getName());
                        e.setTextContent(value.toString());
                        rootElement.appendChild(e);
                    }
                }

                // Check boxes - if checked, store, otherwise skip
                if(f.getType().equals(CheckBox.class)) {
                    boolean value = ((CheckBox)f.get(s)).isSelected();

                    Element e = xml.createElement("choicebox");
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