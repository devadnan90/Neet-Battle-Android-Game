
All state lives in `SharedPreferences`. There is no database and no network layer.

## Audio

`Feedback.kt` synthesizes every tone at runtime with `AudioTrack` rather than
shipping audio files, so the whole sound layer costs zero KB in the APK. Correct
answers rise in pitch with your streak. The user can mute sound and haptics from
the profile tab.

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

Create a signing key, then a `keystore.properties` in the project root based on
`keystore.properties.example`. That file and any `.jks` are gitignored and must
never be committed.
