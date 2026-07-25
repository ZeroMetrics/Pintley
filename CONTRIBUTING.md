# Contributing to Pintley

Thanks for wanting to add to Pintley! The easiest and most welcome contribution is **new tiles** (prompts) — and you don't need to know how to code to do it.

## Adding tiles (the easy way — no coding needed)

Every prompt in the game lives in a plain-text file here:

```
app/src/main/assets/tiles/
├─ regular.txt
├─ action.txt
├─ rule.txt
├─ weakness.txt
├─ special.txt
├─ wild.txt
├─ democracy.txt
├─ power.txt
├─ elimination.txt
├─ would_you_rather.txt
└─ ultra_challenge.txt
```

**Each line is one tile.** To add tiles, just add lines.

Guidelines:

- **One prompt per line.**
- **Keep it short** — it has to be readable at a glance on a phone held in landscape.
- Use `Player X`, `Player Y`, `Player Z` wherever you want a random player's name dropped in (up to three per tile).
- Blank lines and lines starting with `#` are ignored (they're comments).
- No code and no quote-escaping — just type the sentence.
- Match the category's vibe. Not sure what a category is for? The ⓘ badge in-game explains each one.

Example — adding a line to `action.txt`:

```
Player X, do your best celebrity impression. Group votes — funniest gives out 3
```

### Sending your tiles in

**If you're comfortable with GitHub:** fork the repo, edit the relevant file(s), and open a Pull Request. Mention roughly which categories you added to.

**If you're not into git:** open a [**New tile suggestion**](../../issues/new?template=new_tile.yml) issue and paste your ideas — a maintainer will add them.

## Adding a whole new category (a little code)

1. Add a colour to `app/src/main/res/values/colors.xml`.
2. Add a `CategoryMeta(...)` entry in `app/src/main/java/com/example/pintly/GameData.kt` pointing at a new `tiles/<name>.txt` file.
3. Create that text file in `app/src/main/assets/tiles/`.
4. The non-Ultra category weights are meant to total roughly 100 (they're relative, so it isn't strict).

## Building

Open the project in Android Studio and hit Run, or from the command line:

```bash
./gradlew assembleDebug
```

## A note on content

Pintley is an adult party game and the tone is cheeky — that's fine. But please keep contributions in good fun: nothing hateful, and nothing that targets or endangers real people. Maintainers may edit or decline tiles.
