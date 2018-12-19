package com.etherblood.connect4.transpositions;

import com.etherblood.connect4.Util;
import static com.etherblood.connect4.Util.Long.toFlag;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Philipp
 */
public class LongTranspositionTableTest {

    @Test
    public void storeAndLoad() {
        long seed = System.currentTimeMillis();
        System.out.println("seed: " + seed);
        Random random = new Random(seed);
        TableEntry expected = new TableEntry();
        expected.depth = random.nextInt(64);
        expected.move = toFlag(random.nextInt(64));
        expected.score = random.nextInt(1 << 14);
        expected.type = random.nextInt(4);
        expected.stateId = random.nextLong();

        LongTranspositionTable table = new LongTranspositionTable(10);
        table.storeRaw(expected.stateId, expected.type, expected.score, expected.depth, expected.move);
        TableEntry actual = table.load(expected.stateId);
        assertEquals(expected.depth, actual.depth);
        assertEquals(expected.move, actual.move);
        assertEquals(expected.score, actual.score);
        assertEquals(expected.type, actual.type);
        assertEquals(expected.stateId, actual.stateId);
    }

}
