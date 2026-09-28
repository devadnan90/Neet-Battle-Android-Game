# NEET Battle

An Android quiz-battle app for NEET aspirants. Jetpack Compose, no Firebase, no backend, no image assets, no icon library. Under 5 MB.

Package: `com.orynexlab.neetbattleneetgame`

## How it plays

Every match is 10 questions with a per-question timer. You are racing a recorded opponent run on the same paper, so faster correct answers score higher. Scoring is 100 base points plus up to 100 more for speed.

Four subjects, four difficulty levels, a once-a-day challenge, XP and levels, badges, a revision deck of your own mistakes, and a chapter-wise syllabus map.

## Opening the project

Android Studio, then File, Open, and select the `NeetBattle` folder. Gradle sync runs automatically and takes a few minutes the first time. The app is fully playable straight after install with no backend and no internet.

## Question bank

1,000 questions ship as JSON in `app/src/main/assets`, 250 per subject:

| File | Subject | ID range |
|---|---|---|
| `questions_botany.json` | Botany | 3,000,001+ |
| `questions_zoology.json` | Zoology | 4,000,001+ |
| `questions_physics.json` | Physics | 1,000,001+ |
| `questions_chemistry.json` | Chemistry | 2,000,001+ |

Each question carries four options, the correct key, a written explanation, a chapter, a topic, and a difficulty tier.

Options are shuffled at serve time in `QuestionBank.shuffleOptions()`. This matters: without it, any bias in how a bank was authored (most answers sitting at B, say) becomes a pattern players can exploit without knowing the material. Shuffling on serve makes the bank's own answer balance irrelevant.

To grow the bank, `tools/gen_bank.py` generates and independently verifies new questions, and `tools/xlsx_to_bank.py` converts a spreadsheet into these JSON files.

## Ghost opponents

There is no live multiplayer. An opponent is a recorded run: a list of `(question index, time taken, correct, points)`. The UI replays it against the clock, so the opponent's score climbs in real time while you play, but nothing leaves the device.

This buys a lot. No sockets, no matchmaking queue, no Firebase bill, and an opponent available from the very first install. Opponents are labelled as bots in the UI rather than presented as real people.

The daily challenge seeds both its questions and its ghost from the date, so every device derives the identical paper and the identical target score with no server involved. See `Daily.kt`.

## Architecture

```
data/
  Models.kt        Question, Subject, Level, Badge, XP curve
  QuestionBank.kt  Asset loading, non-repeating draw, option shuffle, ghost builder
  Progress.kt      XP, coins, streak, badges, mistakes, topic stats (SharedPreferences)
  Daily.kt         Date-seeded daily challenge
game/
  MatchEngine.kt   Match state machine, timer loop, scoring, power-ups
  Feedback.kt      Audio and haptics
ui/
  theme/Theme.kt   Palette and type scale
  comp/            Brutal block, pills, motion helpers, bottom nav
  screens/         Splash, Login, Home, Matchmaking, Match, Result,
                   Practice, Chapters, Leaderboard, Profile
```

All state lives in `SharedPreferences`. There is no database and no network layer.

## Audio

`Feedback.kt` synthesizes every tone at runtime with `AudioTrack` rather than shipping audio files, so the whole sound layer costs zero KB in the APK. Correct answers rise in pitch with your streak. The user can mute sound and haptics from the profile tab.

## Size

| Decision | Saving |
|---|---|
| No Firebase | Realtime DB and Auth would add roughly 6 MB |
| No image or PNG assets | Icons are drawn on Canvas |
| No custom fonts | System Roboto and Roboto Mono |
| No `material-icons-extended` | That library alone adds roughly 9 MB |
| No Retrofit, Gson or Coil | Nothing needs them |
| Synthesized audio | No sound files |
| R8 full mode and resource shrinking | Release builds only |
| `resourceConfigurations = ["en"]` | Drops library translations |

Question assets add about 460 KB.

## Release build

Create a signing key, then a `keystore.properties` in the project root based on `keystore.properties.example`. That file and any `.jks` are gitignored and must never be committed.

```
keytool -genkeypair -v -keystore ~/neetbattle-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 -alias neetbattle
```

```
./gradlew clean bundleRelease
ls -lh app/build/outputs/bundle/release/app-release.aab
```

Play Store takes the AAB and handles ABI, density and language splitting itself.

Back up the keystore in more than one place. Losing it means the app can never be updated again.

Always run one full match on a release build before uploading. R8 is only active there, and a stripped serialization class will not show up in debug.

## Design

Neo-brutalist dark. Deep void background with drifting colour glows, saturated blocks with thick black borders, and hard offset shadows that a block physically travels into when pressed. Colours and the type scale live in `ui/theme/Theme.kt`.

The core component is `Brutal` in `ui/comp/Brutal.kt`. Everything tappable is built from it, which is what keeps the press feedback consistent across every screen.

## Not built yet

- [ ] Backend for a real leaderboard and real ghost runs from other players
- [ ] Google sign-in (the login screen currently just takes a name)
- [ ] Friend challenge via shareable link
- [ ] AdMob rewarded ads tied to the power-up economy
- [ ] Shareable result card for social
- [ ] Exam countdown and syllabus pacing
