package com.guessword.service;

import com.guessword.Test;
import com.guessword.TestApp;
import com.guessword.model.User;
import com.guessword.model.Word;

import static com.guessword.Assert.assertEquals;
import static com.guessword.Assert.assertThrows;
import static com.guessword.Assert.assertTrue;

public class WordServiceTest extends TestApp {

    @Test
    void adminCanAddAWordInAnyCase() {
        Word word = app.words.addWord("smile");
        assertEquals("SMILE", word.text());
        assertEquals(21L, app.wordRepository.count());
    }

    @Test
    void invalidOrDuplicateWordsAreRejected() {
        assertThrows(AppException.class, "exactly 5 letters", () -> app.words.addWord("ABC"));
        assertThrows(AppException.class, "exactly 5 letters", () -> app.words.addWord("AB1DE"));
        assertThrows(AppException.class, "already in the word list", () -> app.words.addWord("tower"));
        assertEquals(20L, app.wordRepository.count());
    }

    @Test
    void unusedWordCanBeRemoved() {
        Word word = app.words.addWord("SMILE");
        app.words.deleteWord(word.id());
        assertTrue(app.wordRepository.findByText("SMILE").isEmpty(), "word should be gone");
    }

    @Test
    void playedWordCannotBeRemoved() {
        User user = player("PlayerOne");
        long gameId = app.games.startOrResumeGame(user);
        Word word = app.gameRepository.findById(gameId).orElseThrow().word();

        assertThrows(AppException.class, "already been played", () -> app.words.deleteWord(word.id()));
        assertTrue(app.wordRepository.findById(word.id()).isPresent(), "word should still exist");
    }
}
