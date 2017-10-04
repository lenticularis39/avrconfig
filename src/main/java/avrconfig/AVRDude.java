package avrconfig;

import avrconfig.error.ErrorHandler;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

public class AVRDude {
    enum CatchingMode {
        NORMAL, FUSES1, FUSES2, LOCK
    }
    public CatchingMode avrdudeMode;
    String avrdudePath;
    String configPath;
    String chip;
    String programmer;
    String port;
    String baudrate = "";
    String bitclock = "";
    Text output;
    ArrayList<TextField> tf;
    Vector<CheckBox> cb;

    static int count; // Counter for reading fuse bits

    public AVRDude(String path, String configPath, String chip, String programmer, String port, Text ta) {
        this.avrdudePath = path;
        this.configPath = configPath;
        this.chip = chip;
        this.programmer = programmer;
        this.port = port;
        output = ta;

        avrdudeMode = CatchingMode.NORMAL;
    }

    public AVRDude(String path, String configPath, String chip, String programmer, String port, Text ta, ArrayList<TextField> tf) {
        this.avrdudePath = path;
        this.configPath = configPath;
        this.chip = chip;
        this.programmer = programmer;
        this.port = port;
        this.tf = tf;
        output = ta;

        avrdudeMode = CatchingMode.FUSES1;
    }

    public AVRDude(String path, String configPath, String chip, String programmer, String port, Text ta, Vector<CheckBox> cb) {
        this.avrdudePath = path;
        this.configPath = configPath;
        this.chip = chip;
        this.programmer = programmer;
        this.port = port;
        this.cb = cb;
        output = ta;

        avrdudeMode = CatchingMode.LOCK;
    }

    public void setBaudrate(String baudrate) {
        this.baudrate = baudrate;
    }

    public void setBitclock(String bitclock) { this.bitclock = bitclock; }

    public void run(ArrayList<String> parameters) {
        run(parameters, new File(avrdudePath).getParentFile());
    }

    public void run(ArrayList<String> parameters, File envDir) {
        Runtime r = Runtime.getRuntime();

        ArrayList<String> call = new ArrayList<>();
        call.add(avrdudePath);
        call.add("-C");
        call.add(configPath);
        call.add("-c");
        call.add(programmer);
        call.add("-p");
        call.add(chip);
        if(!port.equals("")) {
            parameters.add("-P");
            parameters.add(port);
        }
        if(!baudrate.equals("")) {
            parameters.add("-b");
            parameters.add(new Integer(baudrate).toString());
        }
        if(!bitclock.equals("")) {
            parameters.add("-B");
            parameters.add(new Float(bitclock).toString());
        }

        call.addAll(parameters);

        try {
            Process avrdude = r.exec(Arrays.copyOf(call.toArray(), call.toArray().length, String[].class), null, envDir);

            InputStreamReader err = new InputStreamReader(avrdude.getErrorStream());
            output.setText("");
            Task updateTextArea = new Task<Void>() {
                @Override protected Void call() throws Exception {
                    while (avrdude.isAlive()) {
                        int ch;
                        while ((ch = err.read()) != -1) {
                            Platform.runLater(new Vypis(Character.toString((char)ch), false));
                        }
                    }
                    return null;
                }
            };
            Thread updateTextAreaThread = new Thread(updateTextArea);
            updateTextAreaThread.setDaemon(true);
            updateTextAreaThread.start();

            if(avrdudeMode == CatchingMode.FUSES1) {
                BufferedReader out = new BufferedReader(new InputStreamReader(avrdude.getInputStream()));
                count = 0;
                Task updateTextArea2 = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        while (avrdude.isAlive()) {
                            String line;
                            while ((line = out.readLine()) != null) {
                                Platform.runLater(new ChytacFusu(line, tf));
                            }
                        }
                        return null;
                    }
                };
                Thread updateTextAreaThread2 = new Thread(updateTextArea2);
                updateTextAreaThread2.setDaemon(true);
                updateTextAreaThread2.start();
            }

            if(avrdudeMode == CatchingMode.LOCK) {
                BufferedReader out = new BufferedReader(new InputStreamReader(avrdude.getInputStream()));
                Task updateTextArea2 = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        while (avrdude.isAlive()) {
                            String line;
                            while ((line = out.readLine()) != null) {
                                Platform.runLater(new ChytacLocku(line, cb));
                            }
                        }
                        return null;
                    }
                };
                Thread updateTextAreaThread2 = new Thread(updateTextArea2);
                updateTextAreaThread2.setDaemon(true);
                updateTextAreaThread2.start();
            }
        } catch(IOException e) {
            ErrorHandler.alert("Cannot execute avrdude.", "Please check the path to the avrdude binary.");
        }
    }

    private class Vypis implements Runnable {
        String line;
        boolean eol;

        public Vypis(String line, boolean eol) {
            this.line = line; this.eol = eol;
        }

        @Override
        public void run() {
            if(eol)
                output.setText(output.getText() + "\n" + line);
            else
                output.setText(output.getText()  + line);
        }
    }

    private class ChytacFusu extends Vypis {
        ArrayList<TextField> tf;

        public ChytacFusu(String line, ArrayList<TextField> tf) {
            super(line, true);
            this.tf = tf;
        }

        @Override
        public void run() {
            if(count < 3) {
                tf.get(count).setText(line);
                count++;
            }
            super.run();
        }
    }

    private class ChytacLocku extends Vypis {
        Vector<CheckBox> cb;

        public ChytacLocku(String line, Vector<CheckBox> cb) {
            super(line, true);
            this.cb = cb;
        }

        @Override
        public void run() {
            byte locks = Byte.parseByte(line);
            for(int i = 0; i <= 5; i++) {
                cb.get(i).setSelected(!((locks & (0b00100000 >> i)) > 0));
            }
            super.run();
        }
    }
}
