package util;

import java.io.OutputStream;
import org.slf4j.Logger;

//modified from Log4j implementation
public class LoggingOutputStream extends OutputStream {

    /**
     * Default number of bytes in the buffer.
     */
    private static final int DEFAULT_BUFFER_LENGTH = 2048;

    /**
     * Indicates stream state.
     */
    private boolean hasBeenClosed = false;

    /**
     * Internal buffer where data is stored.
     */
    private byte[] buf;

    /**
     * The number of valid bytes in the buffer.
     */
    private int count;

    /**
     * Remembers the size of the buffer.
     */
    private int curBufLength;

    /**
     * The logger to write to.
     */
    private Logger log;

    /**
     * Creates the Logging instance to flush to the given logger.
     */
    public LoggingOutputStream(Logger log) {
        log.debug("LoggingOutputStream attached to {}\nThis class has not been tested");
        this.log = log;
        curBufLength = DEFAULT_BUFFER_LENGTH;
        buf = new byte[curBufLength];
        count = 0;
    }

    /**
     * Writes the specified byte to this output stream.
     *
     */
    @Override
    public void write(final int b) {
        if (hasBeenClosed) {
            log.error("Message written after LoggingOutputStream has closed ");
        }
        // don't log nulls
        if (b == 0) {
            return;
        }
        //  writing past the buffer
        if (count == curBufLength) {
            // grow the buffer
            final int newBufLength = curBufLength +
                    DEFAULT_BUFFER_LENGTH;
            final byte[] newBuf = new byte[newBufLength];
            System.arraycopy(buf, 0, newBuf, 0, curBufLength);
            buf = newBuf;
            curBufLength = newBufLength;
        }

        buf[count] = (byte) b;
        count++;
    }

    /**
     * Flushes this output stream and forces any buffered output
     * bytes to be written out.
     */
    @Override
    public void flush() {
        if (count == 0) {
            return;
        }
        final byte[] bytes = new byte[count];
        System.arraycopy(buf, 0, bytes, 0, count);
        String str = new String(bytes);
        log.debug(str);
        count = 0;
    }

    /**
     * Closes this output stream and releases any system resources
     * associated with this stream.
     */
    @Override
    public void close() {
        flush();
        hasBeenClosed = true;
    }
}