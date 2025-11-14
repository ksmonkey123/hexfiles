package ch.awae.binfiles;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BinaryFileIteratorTest {

    @Test
    public void testStandardIterator() {
        BinaryFile file = buildFile();

        // evaluate
        List<@NotNull DataFragment> fragments = new ArrayList<>();
        for (@NotNull DataFragment frag : file) {
            fragments.add(frag);
        }

        assertEquals(5, fragments.size());
        assertEquals(64, fragments.get(0).getPosition());
        assertEquals(64, fragments.get(0).getData().length);
        assertEquals(128, fragments.get(1).getPosition());
        assertEquals(32, fragments.get(1).getData().length);
        assertEquals(170, fragments.get(2).getPosition());
        assertEquals(2, fragments.get(2).getData().length);
        assertEquals(191, fragments.get(3).getPosition());
        assertEquals(1, fragments.get(3).getData().length);
        assertEquals(255, fragments.get(4).getPosition());
        assertEquals(1, fragments.get(4).getData().length);
    }

    private static @NotNull BinaryFile buildFile() {
        BinaryFile file = new BinaryFile(256);

        // "block 1" stays empty
        // fill "block 2" completely
        for (int i = 0; i < 64; i++) {
            file.addByte(64 + i, (byte) i);
        }

        // fill "block 3" with 3 disjoint sections
        for (int i = 0; i < 32; i++) {
            file.addByte(128 + i, (byte) i);
        }
        file.addByte(170, (byte) 1);
        file.addByte(171, (byte) 2);
        file.addByte(191, (byte) 3);

        // fill "block 4" at end only
        file.addByte(255, (byte) 1);
        return file;
    }

}
