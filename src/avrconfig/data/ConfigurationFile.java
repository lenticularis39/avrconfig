package avrconfig.data;

import avrconfig.Main;
import avrconfig.MainController;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;

public class ConfigurationFile implements Serializable {
    Hashtable<String, String> chips;
    String selectedChip;
    Hashtable<String, String> programmers;
    String selectedProgrammer;
    String execPath;
    String configPath;
    String port;
    String baudrate;

    transient String filename;
    public transient MainController s;

    public ConfigurationFile(String filename, MainController s)  {
        this.filename = filename;
        chips = s.chips;
        programmers = s.programmers;
        execPath = s.execTextField.getText();
        configPath = s.configTextField.getText();
        selectedChip = (String)s.microcontrollerChoiceBox.getValue();
        selectedProgrammer = (String)s.programmersChoiceBox.getValue();
        port = s.portTextField.getText();
        baudrate = s.baudrateTextField.getText();
        this.s = s;
    }

    public void load() {
        s.chips = this.chips;
        s.programmers = this.programmers;
        s.updateLists();
        s.execTextField.setText(this.execPath);
        s.configTextField.setText(this.configPath);
        s.microcontrollerChoiceBox.setValue(this.selectedChip);
        s.programmersChoiceBox.setValue(this.selectedProgrammer);
        s.portTextField.setText(this.port);
        s.baudrateTextField.setText(this.baudrate);
    }

    public void save() throws IOException {
        FileOutputStream o = new FileOutputStream(filename);
        ObjectOutputStream ser = new ObjectOutputStream(o);
        ser.writeObject(this);
        ser.close();
    }
}