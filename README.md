# Guess the Word (plain Java, no Maven)

A Wordle-style game written in plain Java: Swing for the windows, JDBC for the database.
There is no Maven, Gradle or Spring. You build it with `javac` and run it with `java`.

**Requirements:** Java 17 or newer (`java -version` to check). Nothing else to install:
the only library, the H2 database jar, is already in `lib/`.

## Run it (Windows)

Double-click `run.bat`, or from a terminal in this folder:

```
run.bat        builds the first time, then starts the game
build.bat      compiles src/ into out/
test.bat       compiles and runs all tests (uses a throw-away in-memory database)
```

On macOS / Linux use `./run.sh`, `./build.sh` and `./test.sh`.

Or by hand, without the scripts:

```
javac -d out -cp lib\h2-2.2.224.jar --source-path src src\com\guessword\Main.java
java -cp "out;lib\h2-2.2.224.jar" com.guessword.Main
```

(On macOS / Linux, use `:` instead of `;` in the `-cp` value.)

**In an IDE** (Eclipse / IntelliJ / VS Code): open the folder, mark `src` as the source folder
(and `test` as a test or second source folder), add `lib/h2-2.2.224.jar` to the build path or
libraries, then run `com.guessword.Main`. To run the tests, run `com.guessword.TestRunner`.

## Accounts

| Account | How to get it |
|---------|---------------|
| Admin   | Created on first start: username `Admin`, password `Admin$123`. To use different ones, start with `java -Dadmin.username=... -Dadmin.password=... -cp ... com.guessword.Main` the first time. |
| Player  | Click **Create an account** on the login window. |

Game data is saved in `data/guessword.mv.db`. Delete the `data` folder to start over.

## Folder structure

```
guess-the-word-java/
├── lib/h2-2.2.224.jar                  embedded database (the only library)
├── build.bat  run.bat  test.bat        Windows scripts
├── build.sh   run.sh   test.sh         macOS / Linux scripts
├── src/com/guessword/
│   ├── Main.java                       entry point: opens the database, shows the login window
│   ├── AppContext.java                 creates the database, repositories and services once
│   ├── db/
│   │   ├── Database.java               JDBC connection, creates the tables, transactions
│   │   └── DataAccessException.java
│   ├── model/                          User, Role, Word, Game, GameStatus, Guess, LetterResult
│   ├── repository/                     SQL for each table (UserRepository, WordRepository,
│   │                                   GameRepository incl. report queries, GuessRepository)
│   ├── service/
│   │   ├── ValidationRules.java        username, password and word rules
│   │   ├── PasswordHasher.java         salted PBKDF2 hashing (passwords never stored as text)
│   │   ├── WordEvaluator.java          green / orange / grey scoring, handles repeated letters
│   │   ├── UserService.java            registration and login
│   │   ├── GameService.java            start/resume a game, submit a guess, 5-guess and 3-a-day limits
│   │   ├── ReportService.java          day report and user report
│   │   ├── WordService.java            admin word management
│   │   ├── DataSeeder.java             20 starter words + admin account
│   │   └── GameView, PlayerSummary, DayReport, UserReport, AppException
│   └── ui/
│       ├── LoginFrame.java             log in / open registration
│       ├── RegisterDialog.java         new player sign-up
│       ├── PlayerFrame.java            player home: words left today, start/continue, history
│       ├── GameDialog.java             the game: board, guess box, result pop-ups
│       ├── BoardPanel.java             the 5 x 5 coloured tiles
│       ├── AdminFrame.java             tabs: Day report, User report, Words
│       └── Theme, Column, Tables, Navigator, UppercaseLettersFilter
└── test/com/guessword/
    ├── TestRunner.java                 runs every @Test method (no JUnit needed)
    ├── Test.java, Assert.java          tiny test annotation and assertions
    ├── TestApp.java, MutableClock.java fresh in-memory database and a movable clock per test
    ├── service/                        WordEvaluator, ValidationRules, PasswordHasher, UserService,
    │                                   GameService, ReportService, WordService tests
    └── ui/BoardPanelTest.java          tile letters and colours, upper-case input filter
```

## Database tables

| Table      | Columns |
|------------|---------|
| `app_user` | id, username (unique), password_hash, role (ADMIN/PLAYER), created_at |
| `words`    | id, word_text (5 upper-case letters, unique) |
| `game`     | id, user_id, word_id (the word given), game_date, started_at, finished_at, status (IN_PROGRESS/WON/LOST) |
| `guess`    | id, game_id, guess_word, attempt_number, guessed_at |

## How the requirements are met

| Requirement | Where |
|---|---|
| Admin and Player users | `Role`. After login, `Navigator` opens `AdminFrame` or `PlayerFrame`. Registration only creates players. |
| Username: at least 5 letters, upper **and** lower case | `ValidationRules`, checked in `UserService` |
| Password: at least 5 characters with a letter, a digit and one of `$ % * @` | `ValidationRules` |
| 20 upper-case 5-letter words saved in the database | `DataSeeder.STARTER_WORDS` |
| Random word per game, at most 3 words per user per day | `GameService.startOrResumeGame` (also never repeats a word for a player until all have been used) |
| Guess must be 5 upper-case letters, at most 5 guesses | `GameService.submitGuess`. The guess box also upper-cases and drops non-letters as you type. |
| Green / orange / grey tiles | `WordEvaluator` + `BoardPanel` |
| Win: congratulations message, OK stops the game | `GameDialog` shows "Congratulations!", and OK closes the game |
| 5 misses: "Better luck next time", OK stops the game | `GameDialog`, which also shows the word |
| Earlier guesses shown in the order they were guessed | `guess.attempt_number`, drawn top to bottom by `BoardPanel` |
| Words given and guesses saved with date | `game` and `guess` tables |
| Day report: number of users, number of correct guesses | Admin window, **Day report** tab (plus each player's words tried and correct guesses) |
| User report: date, number of words tried, number of correct guesses | Admin window, **User report** tab |

Decisions made where the spec was open:
- The spec's special-character list is cut off after "$, %, * and". `@` is assumed to be the fourth character.
- An unfinished game counts toward the day's 3 words. Closing the game window keeps it, and the
  player can continue it from their home window.
- Usernames are unique ignoring case.
- Admins can add words, and can remove a word only if it has never been played.
