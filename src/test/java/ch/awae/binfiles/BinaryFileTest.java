package ch.awae.binfiles;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BinaryFileTest {

    @Test
    public void testEmptyFileCreation() {
        BinaryFile file = new BinaryFile();
        assertEquals(0, file.getCurrentSize());
        for (int i = 0; i < 65536; i++) {
            assertNull(file.getByte(i), "file[" + i + "] must be null");
        }
    }

    @Test
    public void testAddingFragments() {
        BinaryFile file = new BinaryFile();

        DataFragment fragment = new DataFragment(100, new byte[]{12, 13, 14, 15});
        file.addFragment(fragment);

        assertEquals((byte) 12, file.getByte(100));
        assertEquals((byte) 13, file.getByte(101));
        assertEquals((byte) 14, file.getByte(102));
        assertEquals((byte) 15, file.getByte(103));

        for (int i = 0; i < 65536; i++) {
            if (i >= 100 && i < 104) continue;
            assertNull(file.getByte(i), "file[" + i + "] must be null");
        }
    }

    @Test
    public void testAddingCollidingFragments() {
        BinaryFile file = new BinaryFile();

        DataFragment fragment1 = new DataFragment(100, new byte[]{12, 13, 14, 15});
        DataFragment fragment2 = new DataFragment(102, new byte[]{12, 13, 14, 15});
        file.addFragment(fragment1);
        assertThrows(IllegalStateException.class, () -> file.addFragment(fragment2));
    }

    @Test
    public void testAddingNonCollidingFragments() {
        BinaryFile file = new BinaryFile();

        DataFragment fragment1 = new DataFragment(100, new byte[]{12, 13, 14, 15});
        DataFragment fragment2 = new DataFragment(104, new byte[]{22, 23, 24, 25});
        file.addFragment(fragment1);
        file.addFragment(fragment2);

        assertEquals(108, file.getCurrentSize());
        assertEquals((byte) 12, file.getByte(100));
        assertEquals((byte) 13, file.getByte(101));
        assertEquals((byte) 14, file.getByte(102));
        assertEquals((byte) 15, file.getByte(103));
        assertEquals((byte) 22, file.getByte(104));
        assertEquals((byte) 23, file.getByte(105));
        assertEquals((byte) 24, file.getByte(106));
        assertEquals((byte) 25, file.getByte(107));
    }

    @Test
    public void testFragmentConstructor() {
        DataFragment fragment1 = new DataFragment(100, new byte[]{12, 13, 14, 15});
        DataFragment fragment2 = new DataFragment(104, new byte[]{22, 23, 24, 25});

        BinaryFile file = new BinaryFile(List.of(fragment1, fragment2));
        assertEquals(108, file.getCurrentSize());

        BinaryFile file2 = new BinaryFile();
        file2.addFragment(fragment2);
        file2.addFragment(fragment1);

        for (int i = 0; i < 65536; i++) {
            assertEquals(file.getByte(i), file2.getByte(i), "files must be identical at position " + i);
        }

    }

}
