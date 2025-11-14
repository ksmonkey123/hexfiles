package ch.awae.binfiles.hex;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

/**
 * represents a single .hex record as defined in the Intel HEX format specification.
 * <p>
 * This class holds raw records without any knowledge of their internal structure or semantics.
 * The individual records are expected to be processed by higher level logic.
 *
 * @param type    the 1-byte type field of the record
 * @param address the 2-byte address field of the record
 * @param data    the 0-255 data bytes of the record
 * @see <a href="https://archive.org/details/IntelHEXStandard">Intel Hexadecimal Object File Format Specification</a>
 */
public record HexRecord(int type, int address, byte[] data) {

    /**
     * Creates a new HexRecord.
     *
     * @param type    The type field. Range: 0-255
     * @param address The address field. Range: 0-65536
     * @param data    The data block. Length: 0-255. May not be null
     * @implNote The provided data array is copied to ensure immutability of the newly created instance.
     */
    public HexRecord(int type, int address, byte[] data) {
        if (type < 0 || type > 255) {
            throw new IllegalArgumentException("invalid value for type field, must be between 0 and 255");
        }
        if (address < 0 || address > 65535) {
            throw new IllegalArgumentException("invalid value for address field, must be between 0 and 65535");
        }
        Objects.requireNonNull(data, "data may not be null");
        if (data.length > 255) {
            throw new IllegalArgumentException("data block too large");
        }

        this.type = type;
        this.address = address;
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * Returns a copy of the data block of this record.
     *
     * @return a <b>copy</b> of the data block.
     * @implNote A new copy is provided to ensure immutability of this instance.
     * It is therefore not recommended to call this method too frequently.
     */
    @Contract(value = " -> new", pure = true)
    @Override
    public byte @NotNull [] data() {
        return Arrays.copyOf(data, data.length);
    }

    /**
     * Calculates the checksum for this record.
     *
     * @return the valid checksum
     */
    @Contract(pure = true)
    public int calculateChecksum() {
        int sum = data.length + (address & 0xff) + ((address >>> 8) & 0xff) + type;
        for (byte x : data) {
            sum += ((int) x) & 0xff;
        }
        return (0x100 - (sum & 0xff)) & 0xff;
    }

}
