package avrconfig;

import avrconfig.error.ErrorHandler;
import avrconfig.util.FuseBitsUpdateListener;
import avrconfig.util.TextUpdateListener;

import javafx.concurrent.Task;

import java.io.*;
import java.util.*;

public class AVRDude {
    private String avrdudePath;
    private String configPath;
    private String chip;
    private String programmer;
    private String port;
    private String baudrate = "";
    private String bitclock = "";

    private Vector<TextUpdateListener> ouls = new Vector<>(3);
    private Vector<FuseBitsUpdateListener> fbuls = new Vector<>(3);
    private Vector<TextUpdateListener> lbuls = new Vector<>(3);

    private static int fuseBitCounter = 0; // Counter for reading fuse bits


    public AVRDude(String path, String configPath, String chip, String programmer, String port) {
        this.avrdudePath = path;
        this.configPath = configPath;
        this.chip = chip;
        this.programmer = programmer;
        this.port = port;
    }


    public void addOutputUpdateEventListener(TextUpdateListener oul) {
        ouls.add(oul);
    }

    public void addFuseBitsUpdateEventListener(FuseBitsUpdateListener fbul)
    {
        fbuls.add(fbul);
    }

    public void addLocksBitsUpdateEventListener(TextUpdateListener lbul)
    {
        lbuls.add(lbul);
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

            Task updateTextArea = new Task<Void>() {
                @Override protected Void call() throws Exception {
                    while (avrdude.isAlive()) {
                        int ch;
                        while ((ch = err.read()) != -1) {
                            for(TextUpdateListener oul : ouls)
                                oul.updateText(Character.toString((char)ch));
                        }
                    }
                    return null;
                }
            };
            Thread updateTextAreaThread = new Thread(updateTextArea);
            updateTextAreaThread.setDaemon(true);
            updateTextAreaThread.start();

            if(fbuls.size() != 0) {
                BufferedReader out = new BufferedReader(new InputStreamReader(avrdude.getInputStream()));
                fuseBitCounter = 0;
                Task catchFuses1Task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        while (avrdude.isAlive()) {
                            String line;
                            while ((line = out.readLine()) != null) {
                                for(FuseBitsUpdateListener fbul : fbuls)
                                    switch(fuseBitCounter)
                                    {
                                        case(0):
                                            fbul.updateLowFuseBits(line);
                                            break;
                                        case(1):
                                            fbul.updateHighFuseBits(line);
                                            break;
                                        case(2):
                                            fbul.updateExtendedFuseBits(line);
                                            break;
                                    }

                                fuseBitCounter++;
                            }
                        }
                        return null;
                    }
                };
                Thread catchFuses1Thread = new Thread(catchFuses1Task);
                catchFuses1Thread.setDaemon(true);
                catchFuses1Thread.start();
            }

            if(lbuls.size() != 0) {
                BufferedReader out = new BufferedReader(new InputStreamReader(avrdude.getInputStream()));
                Task catchLockTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        while (avrdude.isAlive()) {
                            String line;
                            while ((line = out.readLine()) != null) {
                                for(TextUpdateListener lbul : lbuls)
                                    lbul.updateText(line);
                            }
                        }
                        return null;
                    }
                };
                Thread catchLockThread = new Thread(catchLockTask);
                catchLockThread.setDaemon(true);
                catchLockThread.start();
            }
        } catch(IOException e) {
            ErrorHandler.alert("Cannot execute avrdude.", "Please check the path to the avrdude binary.");
        }
    }
}
