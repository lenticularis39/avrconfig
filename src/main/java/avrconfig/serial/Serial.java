package avrconfig.serial;

import avrconfig.error.ErrorHandler;
import avrconfig.util.TextUpdateListener;
import javafx.concurrent.Task;

import javax.xml.soap.Text;
import java.io.*;
import java.util.Vector;

/**
 * Created by tglozar on 20.8.17.
 */
public class Serial implements SerialController {
    int baudrate;
    String filename;
    FileInputStream serialInputStream;
    BufferedReader serialInput;

    Task outputCatching;
    boolean taskFlag = true;

    private Vector<TextUpdateListener> outputUpdaters = new Vector<>(1);

    public Serial(String filename, int baudrate) throws IOException {
        this.baudrate = baudrate;
        this.filename = filename;

        serialInputStream = new FileInputStream(filename);
        serialInput = new BufferedReader(new InputStreamReader(serialInputStream));

        setBaudrate(baudrate);
    }

    public void setBaudrate(int baudrate) throws IOException {
        if(System.getProperty("os.name").startsWith("Windows")) {
            // Implementace pro systém BG

            Process p = Runtime.getRuntime().exec(String.format("mode %s BAUD=%d", filename, baudrate));
            try {
                if(p.waitFor() != 0)
                    ErrorHandler.info("Warning", "Failed to set baudrate.");
            } catch(InterruptedException ie) {
                ErrorHandler.alertBug(ie);
            }
        } else {
            // Implementace pro unixový systém

            Process p = Runtime.getRuntime().exec(String.format("stty -F %s %d", filename, baudrate));
            try {
                if(p.waitFor() != 0)
                    ErrorHandler.info("Warning", "Failed to set baudrate.");
            } catch(InterruptedException ie) {
                ErrorHandler.alertBug(ie);
            }
        }
    }

    public void start() {
        outputCatching = new Task() {
            @Override protected Void call() {
                while(taskFlag) {
                    try {
                        if (serialInput.ready()) {
                            String line = serialInput.readLine();
                            if(line != null)
                                for(TextUpdateListener outputUpdater : outputUpdaters)
                                    outputUpdater.updateText(line);
                        }
                    } catch(IOException ie) {}
                }
                return null;
            }
        };
        Thread t = new Thread(outputCatching);
        t.setDaemon(true);
        t.start();
    }

    public void stop() {
        taskFlag = false;
    }

    public void clearBuffer() throws IOException {
        while(serialInputStream.available() != 0)
            serialInputStream.read();
    }

    public void closeStream() throws IOException {
        serialInputStream.close();
    }

    public void addOutputUpdater(TextUpdateListener tul) {
        outputUpdaters.add(tul);
    }
}
