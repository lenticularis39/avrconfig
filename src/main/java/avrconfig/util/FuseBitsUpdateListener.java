package avrconfig.util;

/**
 * Created by tglozar on 4.10.17.
 */
public interface FuseBitsUpdateListener {
    void updateLowFuseBits(String fs);
    void updateHighFuseBits(String fs);
    void updateExtendedFuseBits(String fs);
}
