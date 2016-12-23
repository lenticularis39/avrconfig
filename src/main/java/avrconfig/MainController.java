package avrconfig;

import java.io.*;
import java.util.*;

import avrconfig.data.ConfigParser;
import avrconfig.data.ConfigurationFile;
import avrconfig.error.ErrorHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.*;

public class MainController {
    public String configurationFile = System.getProperty("user.home") + "/.config/avrconfig/config.xml";

    @FXML
    public Parent root;

    // Setup page
    @FXML
    public TextField execTextField;
    @FXML
    public TextField configTextField;
    @FXML
    public TextField portTextField;
    @FXML
    public TextField baudrateTextField;
    @FXML
    public ChoiceBox microcontrollerChoiceBox;
    @FXML
    public ChoiceBox programmersChoiceBox;

    // Flash page
    @FXML
    public TextField readFlashTextField;
    @FXML
    public TextField writeFlashTextField;
    @FXML
    public Text flashOutput;
    @FXML
    public ChoiceBox readFormatChoiceBox;
    @FXML
    public CheckBox overrideSignatureCheckBox;
    @FXML
    public CheckBox notEraseCheckbox;

    // EEPROM page
    @FXML
    public Text EEPROMOutput;
    @FXML
    public ChoiceBox readFormatChoiceBoxEEPROM;
    @FXML
    public CheckBox overrideSignatureCheckBoxEEPROM;
    @FXML
    public TextField readEEPROMTextField;
    @FXML
    public TextField writeEEPROMTextField;

    // Classic fuses page
    @FXML
    public Text classicFusesOutput;
    @FXML
    public TextField lowFuseTextField;
    @FXML
    public TextField highFuseTextField;
    @FXML
    public TextField extendedFuseTextField;
    @FXML
    public CheckBox overrideSignatureCheckBoxClassicF;

    // Lock bits page
    @FXML
    public Text lockBitsOutput;
    @FXML
    public CheckBox BLB12;
    @FXML
    public CheckBox BLB11;
    @FXML
    public CheckBox BLB02;
    @FXML
    public CheckBox BLB01;
    @FXML
    public CheckBox LB2;
    @FXML
    public CheckBox LB1;
    @FXML
    public CheckBox overrideSignatureCheckLock;

    // Other page
    @FXML
    public Text otherOutput;
    @FXML
    public CheckBox overrideSignatureCheckOther;

    FileChooser fileChooser;
    public Hashtable<String, String> chips;
    public Hashtable<String, String> programmers;

    public MainController() {
        super();

        fileChooser = new FileChooser();
        chips = new Hashtable<>();
        programmers = new Hashtable<>();
    }

    @FXML
    public void initialize()  {
        // Set up saving configuration file on closing
        MainController _this = this;

        Task setSave = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e) {
                    ErrorHandler.alertBug(e);
                }
                root.getScene().getWindow().setOnHiding(event -> {
                    ConfigurationFile save = new ConfigurationFile(configurationFile, _this);
                    try {
                        save.save();
                    } catch(IOException e) {
                        // Cannot write to file
                        ErrorHandler.alert("Cannot save configuration file.", "AVRConfig couldn't write into the configuration file. " +
                                "Please check if you have sufficient permissions.");
                    }
                });

                return null;
            }
        };
        Thread t = new Thread(setSave);
        t.setDaemon(true);
        t.start();

        try {
            ConfigurationFile loader = new ConfigurationFile(configurationFile, this);
            loader.load();
        } catch (FileNotFoundException e) {
            // The file does not exist, a new file will be created
            if(configurationFile.equals(System.getProperty("user.home") + "/.config/avrconfig/config.xml")) {
                // If the configuration file should be in the default location, check if it exists, if not, create it
                if (!new File(System.getProperty("user.home") + "/.config").isDirectory()) {
                    // Config directory does not exist, create it and avrconfig subfolder
                    System.out.println("Creating directory " + System.getProperty("user.home") + "/.config");
                    new File(System.getProperty("user.home") + "/.config").mkdir();

                    System.out.println("Creating directory " + System.getProperty("user.home") + "/.config/avrconfig");
                    new File(System.getProperty("user.home") + "/.config/avrconfig").mkdir();
                } else if (!new File(System.getProperty("user.home") + "/.config/avrconfig").isDirectory()) {
                    // Config directory exists, but avrconfig subdirectory does not exist
                    System.out.println("Creating directory " + System.getProperty("user.home") + "/.config/avrconfig");
                    new File(System.getProperty("user.home") + "/.config/avrconfig").mkdir();
                }
            }
            System.out.println("Creating file " + configurationFile);
        }
        catch(IOException ie) {
            ErrorHandler.alert("Cannot load configuration file.", "config.xml exists, but cannot be read. " +
                    "The filesystem is no longer available or corrupted.");
        }

        // Set read formats
        ObservableList<String> readFormats = FXCollections.observableArrayList();
        readFormats.add("Intel HEX");
        readFormats.add("Motorola S-record");
        readFormats.add("Raw binary");
        readFormats.add("ELF");
        readFormats.add("Decimal");
        readFormats.add("Hexadecimal");
        readFormats.add("Binary");
        readFormats.add("Octal");
        readFormatChoiceBox.setItems(readFormats);
        readFormatChoiceBox.setValue("Intel HEX");
        readFormatChoiceBoxEEPROM.setItems(readFormats);
        readFormatChoiceBoxEEPROM.setValue("Intel HEX");
    }


    public void updateLists() {
        ObservableList<String> chipsDescriptions = FXCollections.observableArrayList();
        chipsDescriptions.addAll(Collections.list(chips.keys()));
        chipsDescriptions.sort((String x, String y) -> x.compareTo(y));
        microcontrollerChoiceBox.setItems(chipsDescriptions);

        ObservableList<String> programmersDescriptions = FXCollections.observableArrayList();
        programmersDescriptions.addAll(Collections.list(programmers.elements()));
        programmersDescriptions.sort((String x, String y) -> x.compareTo(y));
        programmersChoiceBox.setItems(programmersDescriptions);
    }

    public void execButtonClicked() {
        File f = fileChooser.showOpenDialog(root.getScene().getWindow());
        if(f != null)
            execTextField.setText(f.getPath());
    }

    public void configButtonClicked() {
        File f = fileChooser.showOpenDialog(root.getScene().getWindow());
        if(f != null)
            configTextField.setText(f.getPath());
    }

    public void readFlashButtonClicked() {
        File f = fileChooser.showSaveDialog(root.getScene().getWindow());
        if(f != null)
            readFlashTextField.setText(f.getPath());
    }

    public void writeFlashButtonClicked() {
        File f = fileChooser.showOpenDialog(root.getScene().getWindow());
        if(f != null)
            writeFlashTextField.setText(f.getPath());
    }

    public void readEEPROMButtonClicked() {
        File f = fileChooser.showSaveDialog(root.getScene().getWindow());
        if(f != null)
            readEEPROMTextField.setText(f.getPath());
    }

    public void writeEEPROMButtonClicked() {
        File f = fileChooser.showOpenDialog(root.getScene().getWindow());
        if(f != null)
            writeEEPROMTextField.setText(f.getPath());
    }

    public void setupButtonClicked() {
        // First check if the files exist
        File avrdudeExe = new File(execTextField.getText());
        File avrdudeConfig = new File(configTextField.getText());

        if (!avrdudeExe.exists() || !avrdudeExe.canExecute()) {
            ErrorHandler.alert("Cannot find or execute avrdude.", "The file " + execTextField.getText() +
                    " either does not exist, or cannot be executed.");
            return;
        } else if (!avrdudeConfig.exists()) {
            ErrorHandler.alert("Cannot find avrdude configuration file.", "The file " + configTextField.getText() + " has not been found.");
            return;
        }

        try {
            ConfigParser cp = new ConfigParser(avrdudeConfig);
            chips = cp.parse("part");
            programmers = cp.parse("programmer");
        } catch(IOException e) {
            ErrorHandler.alert("Cannot read avrdude configuration file.", "The filesystem is no longer available or corrupted (or the folder does not exist).");
        }

        updateLists();
    }

    public AVRDude getAvrDude(Text text) {
        AVRDude avrDude = new AVRDude(execTextField.getText(), configTextField.getText(), (String)microcontrollerChoiceBox.getValue(), (String)programmersChoiceBox.getValue(), portTextField.getText(), text);
        if(!baudrateTextField.getText().equals("")) avrDude.setBaudrate(Integer.parseInt(baudrateTextField.getText()));
        return avrDude;
    }

    public AVRDude getAvrDudeFUSES1(Text text) {
        ArrayList<TextField> tf = new ArrayList<>();
        tf.add(lowFuseTextField);
        tf.add(highFuseTextField);
        tf.add(extendedFuseTextField);
        AVRDude avrDude = new AVRDude(execTextField.getText(), configTextField.getText(), (String)microcontrollerChoiceBox.getValue(), (String)programmersChoiceBox.getValue(), portTextField.getText(), text, tf);
        if(!baudrateTextField.getText().equals("")) avrDude.setBaudrate(Integer.parseInt(baudrateTextField.getText()));
        return avrDude;
    }

    public AVRDude getAvrDudeLOCK(Text text) {
        Vector<CheckBox> cb = new Vector<>();
        cb.add(BLB12);
        cb.add(BLB11);
        cb.add(BLB02);
        cb.add(BLB01);
        cb.add(LB2);
        cb.add(LB1);
        AVRDude avrDude = new AVRDude(execTextField.getText(), configTextField.getText(), (String)microcontrollerChoiceBox.getValue(), (String)programmersChoiceBox.getValue(), portTextField.getText(), text, cb);
        if(!baudrateTextField.getText().equals("")) avrDude.setBaudrate(Integer.parseInt(baudrateTextField.getText()));
        return avrDude;
    }

    public void readFlash() {
        AVRDude avrDude = getAvrDude(flashOutput);
        File hexFile = new File(readFlashTextField.getText());

        ArrayList<String> parameters = new ArrayList<>();

        if(overrideSignatureCheckBox.isSelected()) {
            parameters.add("-F");
        }

        String readType;
        switch((String)readFormatChoiceBox.getValue()) {
            case("Intel HEX"): {
                readType = "i";
                break;
            }
            case("Motorola S-record"): {
                readType = "s";
                break;
            }
            case("Raw binary"): {
                readType = "r";
                break;
            }
            case("ELF"): {
                readType = "e";
                break;
            }
            case("Decimal"): {
                readType = "d";
                break;
            }
            case("Hexadecimal"): {
                readType = "h";
                break;
            }
            case("Binary"): {
                readType = "b";
                break;
            }
            case("Octal"): {
                readType = "o";
                break;
            }
            default: {
                readType = "r";
                break;
            }
        }

        parameters.add("-U");
        parameters.add("flash:r:" + hexFile.getName() + ":" + readType);

        avrDude.run(parameters, hexFile.getParentFile());
    }

    public void readEEPROM() {
        AVRDude avrDude = getAvrDude(EEPROMOutput);
        File hexFile = new File(readEEPROMTextField.getText());

        ArrayList<String> parameters = new ArrayList<>();

        if(overrideSignatureCheckBoxEEPROM.isSelected()) {
            parameters.add("-F");
        }

        String readType;
        switch((String)readFormatChoiceBoxEEPROM.getValue()) {
            case("Intel HEX"): {
                readType = "i";
                break;
            }
            case("Motorola S-record"): {
                readType = "s";
                break;
            }
            case("Raw binary"): {
                readType = "r";
                break;
            }
            case("ELF"): {
                readType = "e";
                break;
            }
            case("Decimal"): {
                readType = "d";
                break;
            }
            case("Hexadecimal"): {
                readType = "h";
                break;
            }
            case("Binary"): {
                readType = "b";
                break;
            }
            case("Octal"): {
                readType = "o";
                break;
            }
            default: {
                readType = "r";
                break;
            }
        }

        parameters.add("-U");
        parameters.add("eeprom:r:" + hexFile.getName() + ":" + readType);

        avrDude.run(parameters, hexFile.getParentFile());
    }

    public void writeEEPROM() {
        AVRDude avrDude = getAvrDude(EEPROMOutput);
        ArrayList<String> parameters = new ArrayList<>();
        File hexFile = new File(writeEEPROMTextField.getText());

        if(overrideSignatureCheckBoxEEPROM.isSelected()) {
            parameters.add("-F");
        }
        parameters.add("-U");
        parameters.add("eeprom:w:" + hexFile.getName());

        avrDude.run(parameters, hexFile.getParentFile());
    }

    public void writeFlash(){
        AVRDude avrDude = getAvrDude(flashOutput);
        ArrayList<String> parameters = new ArrayList<>();
        File hexFile = new File(writeFlashTextField.getText());

        if(overrideSignatureCheckBox.isSelected()) {
            parameters.add("-F");
        }
        if(notEraseCheckbox.isSelected()) {
            parameters.add("-e");
        }

        parameters.add("-U");
        parameters.add("flash:w:" + hexFile.getName());

        avrDude.run(parameters, hexFile.getParentFile());
    }

    public void writeFusesButtonClicked() {
        AVRDude avrDude = getAvrDude(classicFusesOutput);
        ArrayList<String> parameters = new ArrayList<>();

        if(overrideSignatureCheckBoxClassicF.isSelected()) {
            parameters.add("-F");
        }

        if(!lowFuseTextField.getText().equals("")) {
            parameters.add("-U");
            parameters.add("lfuse:w:" + lowFuseTextField.getText() + ":m");
        }

        if(!highFuseTextField.getText().equals("")) {
            parameters.add("-U");
            parameters.add("hfuse:w:" + highFuseTextField.getText() + ":m");
        }

        if(!extendedFuseTextField.getText().equals("")) {
            parameters.add("-U");
            parameters.add("efuse:w:" + extendedFuseTextField.getText() + ":m");
        }

        avrDude.run(parameters);
    }

    public void readFusesButtonClicked() {
        AVRDude avrDude = getAvrDudeFUSES1(classicFusesOutput);
        ArrayList<String> parameters = new ArrayList<>();

        if(overrideSignatureCheckBoxClassicF.isSelected()) {
            parameters.add("-F");
        }

        parameters.add("-U");
        parameters.add("lfuse:r:-:h");

        parameters.add("-U");
        parameters.add("hfuse:r:-:h");

        parameters.add("-U");
        parameters.add("efuse:r:-:h");

        avrDude.run(parameters);
    }

    public void readLockBitsButtonClicked() {
        AVRDude avrDude = getAvrDudeLOCK(lockBitsOutput);
        ArrayList<String> parameters = new ArrayList<>();
        if(overrideSignatureCheckLock.isSelected()) {
            parameters.add("-F");
        }

        parameters.add("-U");
        parameters.add("lock:r:-:d");

        avrDude.run(parameters);
    }

    public void writeLockBitsButtonClicked() {
        AVRDude avrDude = getAvrDude(lockBitsOutput);
        ArrayList<String> parameters = new ArrayList<>();
        if(overrideSignatureCheckLock.isSelected()) {
            parameters.add("-F");
        }

        StringBuilder locks = new StringBuilder();
        locks.append("0b");
        ArrayList<CheckBox> cb = new ArrayList<>();
        cb.add(BLB12);
        cb.add(BLB11);
        cb.add(BLB02);
        cb.add(BLB01);
        cb.add(LB2);
        cb.add(LB1);
        for(CheckBox c : cb) {
            locks.append(c.isSelected() ? 0 : 1);
        }

        parameters.add("-U");
        parameters.add("lock:w:" + locks.toString() + ":m");

        avrDude.run(parameters);
    }

    public void checkConnectionButtonClicked() {
        AVRDude avrDude = getAvrDudeFUSES1(otherOutput);
        ArrayList<String> parameters = new ArrayList<>();
        if(overrideSignatureCheckOther.isSelected()) {
            parameters.add("-F");
        }
        avrDude.run(parameters);
    }

    public void eraseButtonClicked() {
        AVRDude avrDude = getAvrDude(otherOutput);
        ArrayList<String> parameters = new ArrayList<>();

        parameters.add("-e");
        if (overrideSignatureCheckOther.isSelected()) {
            parameters.add("-F");
        }

        avrDude.run(parameters);
    }
}
