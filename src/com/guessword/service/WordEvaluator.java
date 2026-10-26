package com.guessword.service;

import com.guessword.model.LetterResult;

import java.util.Arrays;
import java.util.List;

/**
 * Scores a guess against the target word.
 * <p>
 * Greens are assigned first, then oranges are handed out only while unmatched copies of
 * that letter remain in the target. This makes repeated letters behave correctly: guessing
 * "OTTER" for "TOWER" gives one orange T and one grey T, not two oranges.
 */
public final class WordEvaluator {

    private WordEvaluator() {
    }

    public static List<LetterResult> evaluate(String target, String guess) {
        if (target.length() != guess.length()) {
            throw new IllegalArgumentException("Guess and target must be the same length");
        }
        int length = target.length();
        LetterResult[] results = new LetterResult[length];
        int[] unmatched = new int[26];

        for (int i = 0; i < length; i++) {
            if (guess.charAt(i) == target.charAt(i)) {
                results[i] = LetterResult.CORRECT;
            } else {
                unmatched[target.charAt(i) - 'A']++;
            }
        }
        for (int i = 0; i < length; i++) {
            if (results[i] != null) {
                continue;
            }
            int letter = guess.charAt(i) - 'A';
            if (unmatched[letter] > 0) {
                results[i] = LetterResult.PRESENT;
                unmatched[letter]--;
            } else {
                results[i] = LetterResult.ABSENT;
            }
        }
        return Arrays.asList(results);
    }
}
