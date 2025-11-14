package ch.awae.binfiles.hex;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Reader for reading {@link HexRecord}s from an {@link InputStream}.
 * <p>
 * The reader follows the <a href="https://archive.org/details/IntelHEXStandard">Intel Hexadecimal Object File Format Specification</a>.
 */
public class HexRecordReader implements Closeable {

    enum State {VALID, COMPLETED, CLOSED, IO_ERROR, PARSING_ERROR}

    private final InputStream stream;
    private State state = State.VALID;

    /**
     * Creates a new reader instance
     *
     * @param stream the input stream to read from. may not be null.
     */
    public HexRecordReader(@NotNull InputStream stream) {
        Objects.requireNonNull(stream, "stream may not be null");
        this.stream = stream;
    }

    /**
     * closes the underlying InputStream
     *
     * @throws IOException if any I/O exception occurs
     */
    @Override
    public void close() throws IOException {
        if (state != State.CLOSED) {
            state = State.CLOSED;
            stream.close();
        }
    }

    /**
     * Read the next hex record from the underlying stream.
     * <p>
     * While "looking" for the next record, any characters will be ignored until a record start (':') is found.
     * It is guaranteed that - after returning a record - the input stream has been consumed <i>exactly</i> up to and
     * including the last byte of that record.
     * <p>
     * If any parsing error occurs while reading a record, a {@link HexRecordParsingException} is thrown.
     * Parsing errors can be:
     * <ul>
     *     <li>InputStream ending unexpectedly in the middle of a record</li>
     *     <li>A record has an invalid checksum</li>
     *     <li>Any characters within a record are invalid and cannot be parsed as hexadecimal numbers</li>
     * </ul>
     * If a parsing error occurs, any further reads will also cause a {@link HexRecordParsingException} to be thrown.
     * <p>
     * If an {@link IOException} is thrown at any time, any further reads will also throw an {@link IOException}.
     *
     * @return the next hex record or null, if the end of the stream has been reached.
     * @throws IOException               if any I/O exception occurs in the underlying stream, or if this reader has already been closed.
     * @throws HexRecordParsingException if any parsing error occurs
     **/
    public HexRecord readNext() throws IOException {
        if (state == State.CLOSED) {
            throw new IOException("reader already closed");
        }
        if (state == State.IO_ERROR) {
            throw new IOException("reader invalid due to previous IOException");
        }
        if (state == State.PARSING_ERROR) {
            throw new HexRecordParsingException("reader invalid due to previous exception");
        }
        if (state == State.COMPLETED) {
            return null;
        }
        try {
            // step 1: seek forward to next "record start marker" (:)
            while (true) {
                int c = stream.read();
                if (c == -1) {
                    // normal stream termination -> no more blocks
                    state = State.COMPLETED;
                    return null;
                }
                if (c == ':') {
                    break;
                }
            }
            // step 2: read block data
            int length = readHexEncodedByte();
            int[] rawData = readHexEncodedBytes(length + 4);

            // step 3: validate and block
            return validateAndBuildBlock(length, rawData);
        } catch (IOException e) {
            state = State.IO_ERROR;
            throw e;
        } catch (HexRecordParsingException e) {
            state = State.PARSING_ERROR;
            throw e;
        }
    }

    private static HexRecord validateAndBuildBlock(int length, int[] rawData) {
        // verify checksum
        int sum = length;
        for (int x : rawData) {
            sum += x;
        }
        sum &= 0x0000_00ff;
        if (sum != 0) {
            throw new HexRecordParsingException("bad checksum in block");
        }

        // checksum ok, construct block
        int address = (rawData[0] << 8) | rawData[1];
        int type = rawData[2];

        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = (byte) rawData[i + 3];
        }
        return new HexRecord(type, address, data);
    }

    private int[] readHexEncodedBytes(int count) throws IOException {
        int[] buffer = new int[count];
        for (int i = 0; i < count; i++) {
            buffer[i] = readHexEncodedByte();
        }
        return buffer;
    }

    private int readHexEncodedByte() throws IOException {
        int high = stream.read();
        int low = stream.read();
        if (high == -1 || low == -1) {
            throw new HexRecordParsingException("unexpected EOS");
        }

        return (hexCharToInt(high) << 4) + hexCharToInt(low);
    }

    private static int hexCharToInt(int hexChar) {
        return switch (hexChar) {
            case '0' -> 0;
            case '1' -> 1;
            case '2' -> 2;
            case '3' -> 3;
            case '4' -> 4;
            case '5' -> 5;
            case '6' -> 6;
            case '7' -> 7;
            case '8' -> 8;
            case '9' -> 9;
            case 'A' -> 10;
            case 'B' -> 11;
            case 'C' -> 12;
            case 'D' -> 13;
            case 'E' -> 14;
            case 'F' -> 15;
            default -> throw new HexRecordParsingException("unable to parse char as hex: " + ((char) hexChar));
        };
    }

}
