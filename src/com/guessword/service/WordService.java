package com.guessword.service;

import com.guessword.model.Word;
import com.guessword.repository.WordRepository;

import java.util.List;
import java.util.Locale;

/** Admin configuration of the word list. */
public class WordService {

    private final WordRepository words;

    public WordService(WordRepository words) {
        this.words = words;
    }

    public List<Word> listWords() {
        return words.findAll();
    }

    /** Words are always stored upper-case, so admins may type them in any case. */
    public Word addWord(String raw) {
        String text = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
        if (!ValidationRules.isValidWord(text)) {
            throw new AppException("A word must be exactly 5 letters (A-Z).");
        }
        if (words.findByText(text).isPresent()) {
            throw new AppException("'" + text + "' is already in the word list.");
        }
        return words.insert(text);
    }

    /** Words that have already been played are kept so the saved game history stays intact. */
    public void deleteWord(long id) {
        Word word = words.findById(id).orElseThrow(() -> new AppException("Word not found."));
        if (words.isUsedInAnyGame(id)) {
            throw new AppException("'" + word.text() + "' has already been played and can't be removed.");
        }
        words.delete(id);
    }
}
