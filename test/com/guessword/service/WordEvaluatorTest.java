package com.guessword.service;

import com.guessword.Test;

import java.util.stream.Collectors;

import static com.guessword.Assert.assertEquals;
import static com.guessword.Assert.assertThrows;

public class WordEvaluatorTest {

    /** G = green (right spot), O = orange (wrong spot), X = grey (not in word). */
    private static String pattern(String target, String guess) {
        return WordEvaluator.evaluate(target, guess).stream()
                .map(r -> switch (r) {
                    case CORRECT -> "G";
                    case PRESENT -> "O";
                    case ABSENT -> "X";
                })
                .collect(Collectors.joining());
    }

    @Test
    void matchesTheExampleBoardFromTheSpecification() {
        assertEquals("XXXXO", pattern("TOWER", "AUDIO"));
        assertEquals("XGXGG", pattern("TOWER", "HOMER"));
        assertEquals("XGXGG", pattern("TOWER", "JOKER"));
        assertEquals("GGXGG", pattern("TOWER", "TONER"));
        assertEquals("GGGGG", pattern("TOWER", "TOWER"));
    }

    @Test
    void lettersNotInTheWordAreGrey() {
        assertEquals("XXXXX", pattern("APPLE", "TOWNS"));
    }

    @Test
    void lettersInTheWrongPlaceAreOrange() {
        assertEquals("OOOOO", pattern("STONE", "TONES"));
    }

    @Test
    void repeatedGuessLetterIsOnlyOrangeAsOftenAsItAppears() {
        // TOWER has one T: the first T in OTTER is orange, the second is grey.
        assertEquals("OOXGG", pattern("TOWER", "OTTER"));
    }

    @Test
    void greenTakesPriorityOverOrange() {
        // APPLE has two Ps: one matched exactly, one left for an orange. The second A has no match.
        assertEquals("OOGXO", pattern("APPLE", "PAPAL"));
    }

    @Test
    void repeatedTargetLetterCanGiveTwoOranges() {
        assertEquals("OXXOX", pattern("APPLE", "PXXPX"));
    }

    @Test
    void differentLengthsAreRejected() {
        assertThrows(IllegalArgumentException.class, null, () -> WordEvaluator.evaluate("WATER", "WAT"));
    }
}
