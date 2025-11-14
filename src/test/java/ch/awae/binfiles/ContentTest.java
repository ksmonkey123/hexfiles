package ch.awae.binfiles;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ContentTest {

    @Test
    public void testEmptyContentHasNothingSet() {
        Content content = new Content(64);

        for (int i = 0; i < content.getSize(); i++) {
            assertFalse(content.isSet(i));
            assertNull(content.getOrNull(i), "content[" + i + "] must be null");
        }
    }

    @Test
    public void testAccessOutOfBounds() {
        Content content = new Content(64);
        assertThrows(IndexOutOfBoundsException.class, () -> content.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> content.get(64));
    }

    @Test
    public void testPutOutOfBounds() {
        Content content = new Content(64);
        assertThrows(IndexOutOfBoundsException.class, () -> content.put(-1, (byte) 1));
        assertThrows(IndexOutOfBoundsException.class, () -> content.put(64, (byte) 1));
    }

    @Test
    public void testSettingData() {
        for (int i = 0; i < 64; i++) {
            Content content = new Content(64);

            content.put(i, (byte) 1);
            assertEquals((byte) 1, content.getOrNull(i), "page[" + i + "] must be 1 after write");
            for (int j = 0; j < 64; j++) {
                if (i == j) continue;
                assertNull(content.getOrNull(j), "content[" + j + "] must still be null, when content[" + i + "] is set");
            }
        }
    }

    @Test
    public void testWritesDontCollide() {
        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < 64; j++) {
                if (i == j) continue;
                Content content = new Content(64);
                content.put(i, (byte) 1);
                content.put(j, (byte) 2);
                assertEquals((byte) 1, content.get(i), "content[" + i + "] must be 1 after write");
                assertEquals((byte) 2, content.get(j), "content[" + j + "] must be 2 after write");
            }
        }
    }

    @Test
    public void testSecondWriteCollides() {
        Content content = new Content(64);
        content.put(1, (byte) 1);
        assertThrows(IllegalStateException.class, () -> content.put(1, (byte) 2));
    }

    @Test
    public void testInitBadSize() {
        assertThrows(IllegalArgumentException.class, () -> new Content(-1));
        assertThrows(IllegalArgumentException.class, () -> new Content(0));
        assertThrows(IllegalArgumentException.class, () -> new Content(65537));
    }

}
