package ch.awae.binfiles;

import org.jetbrains.annotations.NotNull;
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

    @Test
    public void testEmptyFragmentExtraction() {
        BinaryFile file = new BinaryFile();

        List<@NotNull DataFragment> fragments = file.getFragments(32, 64);
        assertNotNull(fragments);
        assertEquals(0, fragments.size());
    }

    @Test
    public void testDenseFragmentExtraction() {
        DataFragment fragment1 = new DataFragment(100, new byte[]{12, 13, 14, 15});
        DataFragment fragment2 = new DataFragment(104, new byte[]{22, 23, 24, 25});

        BinaryFile file = new BinaryFile(List.of(fragment1, fragment2));

        List<@NotNull DataFragment> fragments = file.getFragments(102, 4);
        assertNotNull(fragments);
        assertEquals(1, fragments.size());

        DataFragment fragment = fragments.getFirst();
        assertEquals(102, fragment.getPosition());
        byte[] data = fragment.getData();
        assertEquals(4, data.length);

        assertEquals(14, data[0]);
        assertEquals(15, data[1]);
        assertEquals(22, data[2]);
        assertEquals(23, data[3]);
    }

    @Test
    public void testSparseFragmentExtraction() {
        DataFragment fragment1 = new DataFragment(100, new byte[]{12, 13, 14, 15});
        DataFragment fragment2 = new DataFragment(105, new byte[]{23, 24, 25});

        BinaryFile file = new BinaryFile(List.of(fragment1, fragment2));

        List<@NotNull DataFragment> fragments = file.getFragments(102, 4);
        assertNotNull(fragments);
        assertEquals(2, fragments.size());

        DataFragment f1 = fragments.getFirst();
        assertEquals(102, f1.getPosition());
        byte[] d1 = f1.getData();
        assertEquals(2, d1.length);
        assertEquals(14, d1[0]);
        assertEquals(15, d1[1]);

        DataFragment f2 = fragments.get(1);
        assertEquals(105, f2.getPosition());
        byte[] d2 = f2.getData();
        assertEquals(1, d2.length);
        assertEquals(23, d2[0]);
    }

}
