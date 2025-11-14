package ch.awae.binfiles;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A representation of a binary data file with a size of up to 65536 bytes (16-bit address space).
 *
 * @since 0.1.0
 */
public class BinaryFile implements Iterable<DataFragment> {

    private final @NotNull Content content;
    private int currentSize = 0;

    /**
     * Creates a new empty file with a max size of 65536 bytes.
     */
    public BinaryFile() {
        this(65536);
    }

    /**
     * Creates a new empty file with a size limit of 65536 bytes.
     *
     * @param sizeLimit the max size of the file. must be between 1 and 65536.
     */
    BinaryFile(int sizeLimit) {
        this.content = new Content(sizeLimit);
    }

    /**
     * Creates a new file with a size limit of 65536 bytes and initializes it with the provided fragments.
     *
     * @param fragments the fragments to add
     * @throws IllegalStateException if any of the fragments are "colliding" with each other
     */
    public BinaryFile(@NotNull List<@NotNull DataFragment> fragments) {
        this(65536, fragments);
    }

    /**
     * Creates a new file with a given size limit and initializes it with the provided fragments.
     *
     * @param sizeLimit the max size of the file. must be between 1 and 65536.
     * @param fragments the fragments to add
     * @throws IllegalStateException     if any of the fragments are "colliding" with each other
     * @throws IndexOutOfBoundsException if any fragment does not fit into this file
     */
    public BinaryFile(int sizeLimit, @NotNull List<@NotNull DataFragment> fragments) {
        this(sizeLimit);
        for (DataFragment fragment : fragments) {
            this.addFragment(fragment);
        }
    }

    /**
     * Puts the data contained in the given fragment into this file.
     *
     * @param address the address of the byte to set
     * @param value   the value to set
     * @throws IllegalStateException     if there's already data present for the given address
     * @throws IndexOutOfBoundsException if the address is out of bounds for this file
     */
    public void addByte(int address, byte value) {
        this.content.put(address, value);
        currentSize = Math.max(this.currentSize, address + 1);
    }

    /**
     * Puts the data contained in the given fragment into this file.
     *
     * @param fragment the fragment to add to the file. may not be null
     * @throws IllegalStateException     if any data in the fragment collides with data already present in the file
     * @throws IndexOutOfBoundsException if the fragment does not fit into this file
     */
    public void addFragment(@NotNull DataFragment fragment) {
        Objects.requireNonNull(fragment, "fragment must not be null");
        byte[] data = fragment.getData();
        int offset = fragment.getPosition();

        for (int i = 0; i < data.length; i++) {
            this.addByte(offset + i, data[i]);
        }
    }

    /**
     * Returns the current file size.
     * <p>
     * The current size is defined as the smallest size that fits all data currently present.
     *
     * @return the file size
     */
    public int getCurrentSize() {
        return this.currentSize;
    }

    /**
     * Returns a single byte of data for a given address.
     *
     * @param address the address of the byte to read
     * @return the data at the given address or null if no data is present
     * @throws IndexOutOfBoundsException if the address is out of bounds for this file
     */
    public @Nullable Byte getByte(int address) {
        return content.getOrNull(address);
    }

    /**
     * Extracts a list of fragments covering the requested memory space.
     * <p>
     * If the space is continuous (all bytes in the space are filled), a single fragment will be returned.
     * If not, a list with the smallest number of fragments possible to cover the entire space is provided.
     * If there's no data in the space, an empty list will be returned.
     *
     * @param start  starting address of the memory space to extract
     * @param length length of the memory space to extract. must be larger than 0.
     * @return a list with 0-n fragments
     * @throws IndexOutOfBoundsException if the memory space is "invalid" (any byte outside the range of this file)
     */
    public @NotNull List<@NotNull DataFragment> getFragments(int start, int length) {
        if (length < 1) {
            throw new IllegalArgumentException("length must be greater than zero");
        }
        if (start < 0 || start + length > this.content.getSize()) {
            throw new IndexOutOfBoundsException();
        }

        List<DataFragment> fragments = new ArrayList<>();
        List<@NotNull Byte> buffer = new ArrayList<>();
        int bufferStart = start;
        for (int i = start; i < start + length; i++) {
            Byte nextByte = content.getOrNull(i);
            if (nextByte != null) {
                buffer.add(nextByte);
            } else {
                if (!buffer.isEmpty()) {
                    // "flush" buffer to new fragment.
                    fragments.add(buildFragment(bufferStart, buffer));
                    buffer.clear();
                }
                bufferStart = i + 1;
            }
        }
        // if there's still data in the buffer, "flush" it again
        if (!buffer.isEmpty()) {
            fragments.add(buildFragment(bufferStart, buffer));
        }
        return fragments;
    }

    private static DataFragment buildFragment(int start, @NotNull List<@NotNull Byte> data) {
        byte[] bytes = new byte[data.size()];
        for (int i = 0; i < data.size(); i++) {
            bytes[i] = data.get(i);
        }
        return new DataFragment(start, bytes);
    }

    /**
     * Returns the file size limit for this file.
     *
     * @return the size limit
     */
    public int getSizeLimit() {
        return this.content.getSize();
    }

    /**
     * Returns an iterator with a step size of 64.
     *
     * @return a new iterator
     * @see #iterator(int)
     */
    @Override
    public @NotNull Iterator<DataFragment> iterator() {
        return new BinaryFileIterator(this, 64);
    }

    /**
     * Returns an iterator with a custom step size.
     * <p>
     * The iterator goes over the file with the set step size.
     * If one "slice" is not representable in a single fragment, the iterator will provide multiple smaller fragments
     * for the same "slice".
     * <p>
     * Internally the iterator calls {@link #getFragments(int, int)} repeatedly and returns the resulting list items one
     * by one.
     *
     * @param stepSize the step size. must be larger than 0.
     * @return a new iterator
     */
    public @NotNull Iterator<DataFragment> iterator(int stepSize) {
        if (stepSize < 1) {
            throw new IllegalArgumentException("stepSize must be greater than zero");
        }
        return new BinaryFileIterator(this, stepSize);
    }
}
