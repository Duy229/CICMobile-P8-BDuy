package org.P8Service;

import com.common.apiutil.CommonException;
import com.common.apiutil.nfc.Nfc;
import net.sf.scuba.smartcards.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class P8CardService extends CardService {
    private static final Logger LOGGER = Logger.getLogger("P8CardService");
    private final Nfc nfc;
    private int apduCount;

    public P8CardService(Nfc nfc) {
        this.nfc = nfc;
        this.apduCount = 0;
    }

    @Override
    public void open() throws CardServiceException {
        byte[] nfcData;

        if (!this.isOpen()) {
            try {
                nfc.open();
                nfcData = nfc.activate(5000);
                if (nfcData == null) {
                    nfc.close();
                    throw new RuntimeException("Không tìm thấy thẻ NFC");
                }
            } catch (CommonException e) {
                try {
                    nfc.close();
                } catch (CommonException ex) {
                    throw new RuntimeException(ex);
                }
                throw new CardServiceException("Kết nối thất bại");
            }
        }
    }

    @Override
    public ResponseAPDU transmit(CommandAPDU commandAPDU) throws CardServiceException {
        try {
            if (!isOpen()) {
                throw new CardServiceException("Not connected");
            } else {
                byte[] responseBytes = nfc.transmit(commandAPDU.getBytes(), commandAPDU.getBytes().length);
                if (responseBytes != null && responseBytes.length >= 2) {
                    ResponseAPDU ourResponseAPDU = new ResponseAPDU(responseBytes);
                    this.notifyExchangedAPDU(new APDUEvent(this, "NfcP8", ++this.apduCount, commandAPDU, ourResponseAPDU));
                    return ourResponseAPDU;
                } else {
                    throw new CardServiceException("Failed response");
                }
            }
        } catch (Exception e) {
            throw new CardServiceException(e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            this.nfc.close();
            this.state = 0;
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean isOpen() {
        if (this.nfc.getOpenFlag()) {
            this.state = 1;
            return true;
        } else {
            this.state = 0;
            return false;
        }
    }

    @Override
    public byte[] getATR() {
        try {
            return nfc.get_uid();
        } catch (CommonException e) {
            LOGGER.log(Level.WARNING, "Failed to get ATR", e);
            return null;
        }
    }

    @Override
    public boolean isConnectionLost(Exception e) {
        if (e == null) {
            return false;
        } else {
            String exceptionClassName = e.getClass().getName();
            if (exceptionClassName.contains("TagLostException")) {
                return true;
            } else {
                String message = e.getMessage();
                if (message == null) {
                    message = "";
                }
                return message.toLowerCase().contains("tag was lost");
            }
        }
    }
}
