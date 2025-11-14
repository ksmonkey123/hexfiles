package ch.awae.binfiles;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DataFragmentTest {

    @Test
    public void testConstruction() {
        byte[] buffer = new byte[100];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (byte) i;
        }

        DataFragment fragment = new DataFragment(100, buffer);

        assertEquals(100, fragment.getPosition());

        byte[] data1 = fragment.getData();
        byte[] data2 = fragment.getData();

        assertNotEquals(buffer, data1);
        assertNotEquals(buffer, data2);
        assertNotEquals(data1, data2);

        assertArrayEquals(buffer, data1);
        assertArrayEquals(buffer, data2);
        assertArrayEquals(data1, data2);
    }

    @Test
    public void testFragmentStartOutOfBounds() {
        assertThrows(IllegalArgumentException.class, () -> new DataFragment(-1, new byte[100]));
        assertThrows(IllegalArgumentException.class, () -> new DataFragment(65536, new byte[100]));
    }

    @Test
    public void testFragmentEndOutOfBounds() {
        assertDoesNotThrow(() -> new DataFragment(0, new byte[65536]));
        assertThrows(IllegalArgumentException.class, () -> new DataFragment(65000, new byte[537]));
    }

    @Test
    public void testEmptyFragment() {
        assertThrows(IllegalArgumentException.class, () -> new DataFragment(0, new byte[0]));
    }

    @Test
    public void testNullFragment() {
        //noinspection DataFlowIssue
        assertThrows(NullPointerException.class, () -> new DataFragment(0, null));
    }

    @Test
    public void testFragmentTakesInCopyOfBuffer() {
        byte[] buffer = new byte[16];
        buffer[0] = 12;
        DataFragment fragment = new DataFragment(0, buffer);
        buffer[0] = 10;
        // data in fragment should not change
        assertEquals(12, fragment.getData()[0]);
    }

}
