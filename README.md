# Pintley 🍺
(Used AI a ton for this)

**Pintley** is a party drinking game for Android. Add your players, then tap through an endless, weighted-random deck of prompts, dares, votes and challenges — each colour-coded by category, with your friends' names dropped straight into the action.

<p align="center"><em>The party drinking game.</em></p>

## 📥 Download & play

**[⬇️ Download the latest Pintley APK](https://github.com/ZeroMetrics/Pintley/releases/latest/download/Pintley.apk)** (Android)

To install: open the downloaded file on your phone and allow installation from unknown sources when prompted (Android blocks sideloaded apps by default — this is normal for apps outside the Play Store). Then just open Pintley and add your players.

You can also grab it from the [Releases page](https://github.com/ZeroMetrics/Pintley/releases/latest).

## The story

Pintley is one of the first apps I ever built. I made the original version back in 2023 as a complete beginner — it worked, my friends and I actually played it, but the code was rough and there was a lot I never got around to.

Years later I came back to it, this time with the help of AI, to fix the old bugs, modernise the whole project, redesign it, and finally add the features I'd always wanted. Then I decided to **open source it** so it's out there for anyone to play, learn from, or build on.

## How to play

- Add the names of everyone playing.
- Tap the **right** side of the screen for the next card; tap the **left** to go back.
- Every card belongs to a **category** shown at the top — tap the ⓘ to see what it means.
- Player names are slotted into prompts automatically.
- Keep going until the party (or the deck) gives out.

## Features

- **Hundreds of prompts** across 11 colour-coded categories — Regular, Action, Rule, Weakness, Special, Wild, Democracy, Power, Elimination, Would You Rather… and more.
- **Add or remove players mid-game** without resetting the deck or losing your place.
- **Customise the deck** — turn categories on/off and set how often each appears (%).
- **Ultra Challenges** — a rare (~0.5%) head-to-head duel between two players, with a unique gradient look, a screen flash, a sound and a haptic buzz. Loser downs their drink.
- **Weighted randomisation** with no repeats within a game.
- Colour-coded categories, tap-to-advance with full back/forward history.
- Light sound effects and haptics.

## Built with

- **Kotlin**, Android View system + **Material 3**, ViewBinding
- Gradle 8.9 · Android Gradle Plugin 8.6.1 · Kotlin 1.9.24
- `minSdk 24` · `targetSdk 35`

## Building it yourself

**Android Studio:** clone the repo, open the project, and hit Run.

**Command line:**

```bash
./gradlew assembleDebug
# APK output: app/build/outputs/apk/debug/app-debug.apk
```

You'll need the Android SDK installed; Android Studio's bundled JDK (JBR 17+) is recommended.

## Contributing

Want to add your own tiles? It's built to be easy — all the prompts are plain-text files in [`app/src/main/assets/tiles/`](app/src/main/assets/tiles/), one per line, **no coding needed**. Add a line, open a pull request, done. See [CONTRIBUTING.md](CONTRIBUTING.md) for the details, or just open a [tile suggestion](../../issues/new?template=new_tile.yml) and someone will add it for you.

## Please drink responsibly

Pintley is meant for adults (18+/21+ depending on where you are) having fun responsibly. Know your limits, look after each other, and never pressure anyone to drink. Non-drinkers can play too — just swap the sips for something soft.

## License

Released under the [MIT License](LICENSE) — do what you like with it.

The bird photographs in the **Name That Bird** category come from Wikimedia Commons and keep their own licences. See [CREDITS.md](CREDITS.md) for the full list of photographers, licences and source links.
