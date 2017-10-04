package avrconfig.util;

import java.util.EventListener;

/**
 * Created by tglozar on 4.10.17.
 */
public interface TextUpdateListener extends EventListener {
    void updateText(String newText);
}
