package ch.awae.binfiles.hex;

import ch.awae.binfiles.BinaryFile;
import ch.awae.binfiles.DataFragment;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Reader for reading {@link BinaryFile}s from an {@link InputStream}.
 * <p>
 * The reader follows the <a href="https://archive.org/details/IntelHEXStandard">Intel Hexadecimal Object File Format Specification</a>.
 * At the moment, only type 0 (data) and type 1 (end of file) records are supported.
 */
public class HexFileReader implements Closeable {

    private enum Status {ALIVE, COMPLETED, CLOSED, READER_ERROR, IO_ERROR}

    private final HexRecordReader recordReader;
    private Status status = Status.ALIVE;

    /**
     * Creates a new reader instance
     *
     * @param reader the record reader to read from. may not be null.
     */
    public HexFileReader(@NotNull HexRecordReader reader) {
        Objects.requireNonNull(reader, "reader may not be null");
        this.recordReader = reader;
    }

    /**
     * Creates a new reader instance
     *
     * @param stream the input stream to read from. may not be null.
     * @implNote a {@link HexRecordReader} is constructed internally.
     */
    public HexFileReader(@NotNull InputStream stream) {
        Objects.requireNonNull(stream, "stream may not be null");
        this.recordReader = new HexRecordReader(stream);
    }

    /**
     * Read the next full HexFile from the underlying reader / stream.
     * <p>
     * The returned file will have the smallest size that is a multiple of 2 that fits the entire contents.
     * <p>
     * If any parsing error occurs while reading a record, a {@link HexFileParsingException} is thrown.
     * Parsing errors can be:
     * <ul>
     *     <li>The underlying reader throws a {@link HexRecordParsingException}</li>
     *     <li>An unsupported record type is encountered</li>
     *     <li>The end of the stream is reached unexpectedly</li>
     * </ul>
     * If a parsing error occurs, any further reads will also cause a {@link HexFileParsingException} to be thrown.
     * <p>
     * If an {@link IOException} is thrown at any time, any further reads will also throw an {@link IOException}.
     *
     * @return the next hex record or null, if the end of the stream has been reached.
     * @throws IOException             if any I/O exception occurs in the underlying stream, or if this reader has already been closed.
     * @throws HexFileParsingException if any parsing error occurs
     **/
    public BinaryFile read() throws IOException {
        if (status == Status.COMPLETED) {
            return null;
        }
        if (status == Status.CLOSED) {
            throw new IOException("reader already closed");
        }
        if (status == Status.IO_ERROR) {
            throw new IOException("reader invalid due to previous IOException");
        }
        if (status == Status.READER_ERROR) {
            throw new HexFileParsingException("reader invalid due to previous exception");
        }
        try {
            BinaryFile result = doRead();
            if (result == null) {
                status = Status.COMPLETED;
                return null;
            }
            return result;
        } catch (IOException e) {
            status = Status.IO_ERROR;
            throw e;
        } catch (HexFileParsingException e) {
            status = Status.READER_ERROR;
            throw e;
        } catch (HexRecordParsingException e) {
            status = Status.READER_ERROR;
            throw new HexFileParsingException(e.getMessage(), e);
        }
    }

    private BinaryFile doRead() throws IOException {
        List<DataFragment> fragments = collectFileFragments();
        if (fragments == null) {
            return null;
        }

        // determine the min file size necessary to fit everything
        int minSize = 0;
        for (DataFragment fragment : fragments) {
            minSize = Math.max(minSize, fragment.getPosition() + fragment.getLength());
        }

        // calculate the smallest power of 2 to fit everything
        int fileSize = 1;
        while (fileSize < minSize) {
            fileSize *= 2;
        }

        return new BinaryFile(fileSize, fragments);
    }

    private List<DataFragment> collectFileFragments() throws IOException {
        List<DataFragment> fragments = new ArrayList<>();

        while (true) {
            HexRecord record = recordReader.readNext();
            if (record == null && fragments.isEmpty()) {
                // end of stream before file starts, simply close.
                return null;
            } else if (record == null) {
                // end of stream before file end -> ERROR
                throw new HexFileParsingException("unexpected end of stream");
            }

            int i = record.type();
            if (i == 0) {
                // data record. convert to fragment.
                fragments.add(new DataFragment(record.address(), record.data()));
            } else if (i == 1) {
                // EOF marker
                return fragments;
            } else {
                // unsupported record type
                throw new HexFileParsingException("unsupported record type: " + i);
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (status != Status.CLOSED) {
            status = Status.CLOSED;
            recordReader.close();
        }
    }

}
