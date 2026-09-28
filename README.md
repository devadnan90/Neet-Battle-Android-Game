# NEET Battle

Android quiz-battle app for NEET aspirants. Jetpack Compose, no Firebase, no image
assets, no icon library.

## Open karne ka tareeka

Android Studio -> File -> Open -> `NeetBattle` folder select karo.
Gradle sync apne aap chalega (pehli baar 3-5 min lagenge).
Run karo — app bundled sample questions ke saath abhi chal jaayega, backend ke bina.

## Size

| Cheez | Kyun |
|---|---|
| Koi Firebase nahi | Realtime DB + Auth ~6MB bachaya |
| Koi image/PNG nahi | Saare icons Canvas pe draw hote hain |
| Koi custom font nahi | System fonts (Roboto + Roboto Mono) |
| `material-icons-extended` nahi | Akela ~9MB add karta hai |
| Koi Retrofit/Gson/Coil nahi | OkHttp + kotlinx.serialization kaafi hai |
| R8 full mode + resource shrinking | release build me |
| Per-ABI split APK | user sirf apna variant download karta hai |

Release build banane ke liye:

```
./gradlew assembleRelease
ls -lh app/build/outputs/apk/release/
```

Expected: ~3.5-4.5 MB per ABI.

## Ghost matchmaking

Live 1v1 nahi hai. Har user ke answers timestamps ke saath record hote hain; agla user
usi paper pe khelta hai aur uske saamne pichle user ka run replay hota hai. Opponent ka
score real-time badhta dikhta hai, par actual me sirf ek REST call hai.

Iska matlab: zero socket infra, zero matchmaking queue, zero Firebase bill, aur pehle
din se hi "opponent" available (kyunki tum khud seed kar sakte ho).

Real-time baad me add karna, jab DAU justify kare.

## Backend

`data/Api.kt` me `BASE` apne Hostinger endpoint pe point karo.

Endpoint chahiye: `GET /match.php?subject=biology&n=10`

```json
{
  "match_uid": "M-88213",
  "opponent_name": "Ananya R.",
  "opponent_elo": 1290,
  "questions": [
    {
      "id": 1,
      "q_text": "...",
      "opt_a": "...", "opt_b": "...", "opt_c": "...", "opt_d": "...",
      "correct_opt": "b",
      "solution": "...",
      "chapter": "Plant Physiology",
      "topic": "Photosynthesis in Higher Plants",
      "time_limit_s": 15
    }
  ],
  "ghost": [
    { "seq": 0, "time_ms": 4200, "correct": true, "points": 172 }
  ]
}
```

Network fail ho toh app apne aap bundled sample match pe fall back kar deta hai.

## Design

Poori visual language NEET ke OMR sheet se li hai — paper stock background, ink text,
hairline rules, answer-key red. Signature element: answer options asli OMR bubbles hain
jo tap karne pe graphite se bhar jaate hain, ink-bleed ke saath.

Colors `ui/theme/Theme.kt` me hain. Type ke teen roles hain: display (heavy, tight),
body (question text), aur data (monospace, tracked out — yahi cheez ise exam document
jaisa feel deti hai).

## Aage kya

- [ ] Firebase Anonymous auth ya device-id based login
- [ ] `POST /submit.php` — answers save karo taaki ghost runs bane
- [ ] Payment gateway paywall pe hook karo
- [ ] AdMob rewarded ads (har 3rd match ke baad interstitial)
- [ ] Solo practice mode weak topics ke liye
- [ ] Streak persistence (DataStore)
