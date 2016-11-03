package avrconfig.data;

import java.io.*;
import java.util.Hashtable;

public class ConfigParser {
    File configFile;

    public ConfigParser(File configFile) throws IllegalArgumentException {
        this.configFile = configFile;
        if(!configFile.exists())
            throw new IllegalArgumentException("File does not exist.");
    }

    public Hashtable<String, String> parse(String keyword) throws FileNotFoundException, IOException {
        // Open file and create hashtable
        BufferedReader avrdudeConfigReader = new BufferedReader(new FileReader(configFile));
        Hashtable<String, String> returner = new Hashtable<>(500);

        // Get names and IDs
        boolean idFlag = false;
        boolean descFlag = false;
        String chipId = "";
        String chipName = "";
        for (String line = avrdudeConfigReader.readLine(); avrdudeConfigReader.ready(); line = avrdudeConfigReader.readLine()) {
            // Flag-based file parsing for
            if(line.startsWith(keyword))
                idFlag = true;
            if(idFlag && line.replaceAll(" ", "").startsWith("id")) {
                String tmp = line.replaceAll(" ", "").split("=")[1];
                chipId = tmp.substring(1, tmp.length() - 2);
                idFlag = false;
                descFlag = true;
            }
            if(descFlag && line.replaceAll(" ", "").startsWith("desc")) {
                String tmp = line.replaceAll(" ", "").split("=")[1];
                if(!tmp.startsWith("\"deprecated")) {
                    chipName = tmp.substring(1, tmp.length() - 2);
                }
                descFlag = false;
                returner.put(chipName, chipId);
            }
        }

        return returner;
    }
}
