package avrconfig.serial;

import java.io.IOException;

/**
 * Created by tglozar on 27.8.17.
 */
public interface SerialController {
    void start() throws IOException;
    void stop() throws IOException;
    void setBaudrate(int baudrate) throws IOException;
    void clearBuffer() throws IOException;
    void closeStream() throws IOException;
}
