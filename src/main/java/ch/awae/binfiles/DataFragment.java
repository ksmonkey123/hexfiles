package ch.awae.binfiles;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

/**
 * A continuous block of binary data with an absolute position in the address space.
 *
 * @since 0.1.0
 */
@SuppressWarnings("ClassCanBeRecord")
public class DataFragment {

    private final int position;
    private final byte @NotNull [] data;

    /**
     * Create a new DataFragment.
     * <p>
     * The entire address range the fragment occupies must fit into a 16-bit address space (0..65535)
     *
     * @param position the <i>starting position</i> of the fragment. The starting position is defined as the address of
     *                 the first byte.
     * @param data     the data contained in the fragment. may not be null and must contain at least 1 element.
     * @throws IllegalArgumentException if any byte of the fragment lies outside the 16-bit address space (0..65535) or if {@code data} is empty.
     * @throws NullPointerException     if {@code data} is null.
     */
    public DataFragment(int position, byte @NotNull [] data) {
        Objects.requireNonNull(data, "data may not be null");
        validatePosition(position);
        validatePosition(position + data.length - 1);
        if (data.length == 0) {
            throw new IllegalArgumentException("must contain at least 1 byte of data");
        }

        this.position = position;
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * Returns the position of this data fragment.
     * <p>
     * The position is defined as the address of the first byte of the fragment.
     *
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Returns a <b>copy</b> of the data contained in the fragment.
     * <p>
     * Please note, that every invocation returns a fresh copy!
     *
     * @return a copy of the fragments data
     */
    public byte @NotNull [] getData() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public String toString() {
        return ("DataFragment(position=%d, length=%d)".formatted(this.position, this.data.length));
    }

    private static void validatePosition(int position) {
        if (position < 0 || position >= 65536) {
            throw new IllegalArgumentException("address out of bounds: " + position);
        }
    }

}
