package ch.awae.binfiles.hex;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HexRecordReaderTest {

    @Test
    public void testStreamProcessing_validSimple() throws IOException {
        List<HexRecord> data = new ArrayList<>();
        try (
                InputStream stream = this.getClass().getResourceAsStream("/ch/awae/binfiles/hex/valid_simple.hex");
                HexRecordReader reader = new HexRecordReader(stream)
        ) {
            for (int i = 0; i < 5; i++) {
                HexRecord fragment = reader.readNext();
                assertNotNull(fragment);
                data.add(fragment);
            }
            // call nr. 5 should return NULL (no more blocks in stream)
            assertNull(reader.readNext());
            // any further call should return NULL (<EOF>);
            assertNull(reader.readNext());
        }

        assertEquals(5, data.size());
        assertEquals(0x0100, data.get(0).address());
        assertEquals(0x0110, data.get(1).address());
        assertEquals(0x0120, data.get(2).address());
        assertEquals(0x0130, data.get(3).address());
        assertEquals(0x0000, data.get(4).address());

        assertEquals(0, data.get(0).type());
        assertEquals(0, data.get(1).type());
        assertEquals(0, data.get(2).type());
        assertEquals(0, data.get(3).type());
        assertEquals(1, data.get(4).type());

        assertArrayEquals(new byte[]{0x21, 0x46, 0x01, 0x36, 0x01, 0x21, 0x47, 0x01, 0x36, 0x00, 0x7E, (byte) 0xFE, 0x09, (byte) 0xD2, 0x19, 0x01}, data.get(0).data());
        assertArrayEquals(new byte[]{0x21, 0x46, 0x01, 0x7E, 0x17, (byte) 0xC2, 0x00, 0x01, (byte) 0xFF, 0x5F, 0x16, 0x00, 0x21, 0x48, 0x01, 0x19}, data.get(1).data());
        assertArrayEquals(new byte[]{0x19, 0x4E, 0x79, 0x23, 0x46, 0x23, (byte) 0x96, 0x57, 0x78, 0x23, (byte) 0x9E, (byte) 0xDA, 0x3F, 0x01, (byte) 0xB2, (byte) 0xCA}, data.get(2).data());
        assertArrayEquals(new byte[]{0x3F, 0x01, 0x56, 0x70, 0x2B, 0x5E, 0x71, 0x2B, 0x72, 0x2B, 0x73, 0x21, 0x46, 0x01, 0x34, 0x21}, data.get(3).data());
        assertArrayEquals(new byte[]{}, data.get(4).data());
    }

    @Test
    public void testStreamProcessing_validSuperfluous() throws IOException {
        List<HexRecord> data = new ArrayList<>();
        try (
                InputStream stream = this.getClass().getResourceAsStream("/ch/awae/binfiles/hex/valid_with_superfluous_data.hex");
                HexRecordReader reader = new HexRecordReader(stream)
        ) {
            for (int i = 0; i < 5; i++) {
                HexRecord fragment = reader.readNext();
                assertNotNull(fragment);
                data.add(fragment);
            }
            // call nr. 5 should return NULL (no more blocks in stream)
            assertNull(reader.readNext());
            // any further call should return NULL (<EOF>);
            assertNull(reader.readNext());
        }

        assertEquals(5, data.size());
        assertEquals(0x0100, data.get(0).address());
        assertEquals(0x0110, data.get(1).address());
        assertEquals(0x0120, data.get(2).address());
        assertEquals(0x0130, data.get(3).address());
        assertEquals(0x0000, data.get(4).address());

        assertEquals(0, data.get(0).type());
        assertEquals(0, data.get(1).type());
        assertEquals(0, data.get(2).type());
        assertEquals(0, data.get(3).type());
        assertEquals(1, data.get(4).type());

        assertArrayEquals(new byte[]{0x21, 0x46, 0x01, 0x36, 0x01, 0x21, 0x47, 0x01, 0x36, 0x00, 0x7E, (byte) 0xFE, 0x09, (byte) 0xD2, 0x19, 0x01}, data.get(0).data());
        assertArrayEquals(new byte[]{0x21, 0x46, 0x01, 0x7E, 0x17, (byte) 0xC2, 0x00, 0x01, (byte) 0xFF, 0x5F, 0x16, 0x00, 0x21, 0x48, 0x01, 0x19}, data.get(1).data());
        assertArrayEquals(new byte[]{0x19, 0x4E, 0x79, 0x23, 0x46, 0x23, (byte) 0x96, 0x57, 0x78, 0x23, (byte) 0x9E, (byte) 0xDA, 0x3F, 0x01, (byte) 0xB2, (byte) 0xCA}, data.get(2).data());
        assertArrayEquals(new byte[]{0x3F, 0x01, 0x56, 0x70, 0x2B, 0x5E, 0x71, 0x2B, 0x72, 0x2B, 0x73, 0x21, 0x46, 0x01, 0x34, 0x21}, data.get(3).data());
        assertArrayEquals(new byte[]{}, data.get(4).data());
    }

    @Test
    public void testStreamProcessing_invalidChecksum() throws IOException {
        try (
                InputStream stream = this.getClass().getResourceAsStream("/ch/awae/binfiles/hex/invalid_checksum.hex");
                HexRecordReader reader = new HexRecordReader(stream)
        ) {

            // first element must be ok
            HexRecord data = reader.readNext();
            assertNotNull(data);
            assertEquals(0x0100, data.address());
            assertEquals(0, data.type());
            assertArrayEquals(new byte[]{0x21, 0x46, 0x01, 0x36, 0x01, 0x21, 0x47, 0x01, 0x36, 0x00, 0x7E, (byte) 0xFE, 0x09, (byte) 0xD2, 0x19, 0x01}, data.data());

            // second element has bad checksum
            assertThrows(HexRecordParsingException.class, reader::readNext);

            // any further read must throw exception too
            assertThrows(HexRecordParsingException.class, reader::readNext);
        }

    }

    @Test
    public void testStreamProcessing_invalidCorrupt() throws IOException {
        try (
                InputStream stream = this.getClass().getResourceAsStream("/ch/awae/binfiles/hex/invalid_corrupt.hex");
                HexRecordReader reader = new HexRecordReader(stream)
        ) {
            // first element is corrupt
            assertThrows(HexRecordParsingException.class, reader::readNext);

            // any further read must throw exception too
            assertThrows(HexRecordParsingException.class, reader::readNext);
        }

    }

    @Test
    public void testStreamProcessing_IOException() throws IOException {
        try (
                InputStream stream = this.getClass().getResourceAsStream("/ch/awae/binfiles/hex/valid_simple.hex");
                HexRecordReader reader = new HexRecordReader(stream)
        ) {
            // read first block
            HexRecord data = reader.readNext();
            // close stream. this should cause an IO Exception at the next read call
            stream.close();
            assertThrows(IOException.class, reader::readNext);
            assertThrows(IOException.class, reader::readNext);
        }
    }

    @Test
    public void testReaderInitNullStream() {
        assertThrows(NullPointerException.class, () -> new HexRecordReader(null));
    }
}
