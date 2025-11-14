package ch.awae.binfiles;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * A representation of a binary data file with a size of up to 65536 bytes (16-bit address space).
 *
 * @since 0.1.0
 */
public class BinaryFile {

    private final @NotNull Content content;
    private int currentSize = 0;

    /**
     * Creates a new empty file with a max size of 65536 bytes.
     */
    public BinaryFile() {
        this(65536);
    }

    /**
     * Creates a new empty file with a max size of 65536 bytes.
     *
     * @param size the max size of the file. must be between 1 and 65536.
     * @throws IllegalArgumentException if the size is invalid.
     */
    BinaryFile(int size) {
        this.content = new Content(size);
    }

    /**
     * Creates a new file with a max size of 65536 bytes and initializes it with the provided fragments.
     *
     * @param fragments the fragments to add
     * @throws IllegalStateException if any of the fragments are "colliding" with each other
     */
    public BinaryFile(@NotNull List<@NotNull DataFragment> fragments) {
        this(65536, fragments);
    }

    /**
     * Creates a new file with a given size and initializes it with the provided fragments.
     *
     * @param size      the max size of the file. must be between 1 and 65536.
     * @param fragments the fragments to add
     * @throws IllegalArgumentException  if the size is invalid
     * @throws IllegalStateException     if any of the fragments are "colliding" with each other
     * @throws IndexOutOfBoundsException if any fragment does not fit into this file
     */
    public BinaryFile(int size, @NotNull List<@NotNull DataFragment> fragments) {
        this(size);
        for (DataFragment fragment : fragments) {
            this.addFragment(fragment);
        }
    }

    private void setByte(int address, byte value) {
        this.content.put(address, value);
        currentSize = Math.max(this.currentSize, address + 1);
    }

    /**
     * Puts the data contained in the given fragment into this file.
     *
     * @param fragment the fragment to add to the file. may not be null
     * @throws IllegalStateException     if any data in the fragment collides with data already present in the file
     * @throws NullPointerException      if the fragment is null
     * @throws IndexOutOfBoundsException if the fragment does not fit into this file
     */
    public void addFragment(@NotNull DataFragment fragment) {
        Objects.requireNonNull(fragment, "fragment must not be null");
        byte[] data = fragment.getData();
        int offset = fragment.getPosition();

        for (int i = 0; i < data.length; i++) {
            this.setByte(offset + i, data[i]);
        }
    }

    /**
     * returns the current file size.
     * <p>
     * The current size is defined as the smallest size that fits all data currently present.
     *
     * @return the file size
     */
    public int getCurrentSize() {
        return this.currentSize;
    }

    /**
     * returns a single byte of data for a given address.
     *
     * @param address the address of the byte to read
     * @return the data at the given address or null if no data is present
     * @throws IndexOutOfBoundsException if the address is out of bounds for this file
     */
    public @Nullable Byte getByte(int address) {
        return content.getOrNull(address);
    }

}
