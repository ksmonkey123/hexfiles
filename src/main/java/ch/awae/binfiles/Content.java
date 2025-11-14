package ch.awae.binfiles;

import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.NoSuchElementException;

class Content {

    private final int size;
    private final byte[] content;
    private final BitSet presenceMarkers;

    public Content(int size) {
        if (size <= 0 || size > 65536) {
            throw new IllegalArgumentException("size must be between 1 and 65536");
        }
        this.size = size;
        this.content = new byte[size];
        this.presenceMarkers = new BitSet(size);
    }

    private void validateAddress(int address) {
        if (address < 0 || address >= content.length) {
            throw new IndexOutOfBoundsException();
        }
    }

    public boolean isSet(int address) {
        validateAddress(address);
        return presenceMarkers.get(address);
    }

    public void put(int address, byte value) {
        if (isSet(address)) {
            throw new IllegalStateException("value already present at address " + address);
        }
        presenceMarkers.set(address);
        content[address] = value;
    }

    public byte get(int address) {
        if (isSet(address)) {
            return content[address];
        } else {
            throw new NoSuchElementException("no value set at address " + address);
        }
    }

    public @Nullable Byte getOrNull(int address) {
        if (isSet(address)) {
            return content[address];
        } else {
            return null;
        }
    }

    public int getSize() {
        return this.size;
    }
}
