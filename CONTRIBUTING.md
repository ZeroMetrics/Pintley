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
├─ ghosts.txt
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

### Optional tags

Most tiles need none of these, but a line can start with one or more tags:

| Tag | What it does |
| --- | --- |
| `[requires: Power]` | The tile only appears **after** a Power has already come up. Stops cards like "All Powers are out of play" showing when nobody has one. List several (`[requires: Rule, Power]`) to mean "any one of these". |
| `[clears: Power]` | The tile **removes** that category from the *What's in play* panel — use it on cards that cancel lasting effects. |
| `[transient]` | A one-off. Keeps the tile **out** of the *What's in play* panel (use for cards that just move things around or make people drink now). |
| `[ghosts: 2]` | Gives 2 ghosts to every player named on the card. Also `[ghosts: clear]` (named players lose theirs), `[ghosts: clearall]` (everyone does), `[ghosts: transfer]` (one ghost moves from the first named player to the second) and `[ghosts: swap]` (the two named players exchange counts). |
| `[haunted]` | The tile needs someone who **already has ghosts**. It's held back until somebody does, and `Player X` is then picked from the haunted players (`Player Y` and `Player Z` can still be anyone). Use it on any card that takes ghosts away, moves them, or makes someone drink them — otherwise it lands on a player with none and does nothing. |

### Ghost-adjusted drinks

Wrap a number in `{braces}` when it's a number of **drinks or sips the named player takes** — the app adds that player's ghosts to it automatically, so `drink {2}` shows as `drink 4` for someone with 2 ghosts.

```
Player X, drink {2}
Player X, tell an embarrassing story or drink {3}
```

Only tag it when **the player named on the card is the one drinking**. Leave the number plain if:

- it isn't drinks — `pick someone to do 20 push-ups`, `immune to the next 5 cards`, `sing 30 seconds`
- someone else drinks it — `give out 2 drinks`, `if anyone laughs, they drink 2`
- **it's shots** — ghosts never affect shots

The bonus always comes from the **first** player named on the card, so on a tile that names two people make sure `Player X` is the one drinking.

`{0}` is a handy trick for "drink one per ghost" cards — nothing plus their ghosts is just their ghost count. Pair it with `[haunted]` so it can't come up as `drink 0`.

```
[requires: Power][clears: Power] All Powers are out of play
[haunted][transient] Player X, drink {0}, one for every ghost on your back
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
