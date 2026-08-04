# Photo credits

The **Name That Bird** category uses photographs from [Wikimedia Commons](https://commons.wikimedia.org/).
Each one is bundled unmodified apart from being scaled down to roughly 1400 px on the long edge,
and each photographer is credited in the app itself when the bird's name is revealed.

The same data lives in machine-readable form at
[`app/src/main/assets/birds/credits.txt`](app/src/main/assets/birds/credits.txt), which is what the
app reads at runtime.

| Bird | Photographer | Licence | Source |
|---|---|---|---|
| Bearded Vulture | Giles Laurent | [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/) | [Commons](https://commons.wikimedia.org/wiki/File:010a_Wild_Bearded_Vulture_in_flight_at_Pfyn-Finges_(Switzerland)_Photo_by_Giles_Laurent.jpg) |
| Blue-footed Booby | Ndecam | [CC BY 2.0](https://creativecommons.org/licenses/by/2.0/) | [Commons](https://commons.wikimedia.org/wiki/File:Sula_nebouxii_-Santa_Cruz,_Galapagos_Islands,_Ecuador-8_(1).jpg) |
| Crested Auklet | F. Deines, USFWS | Public domain | [Commons](https://commons.wikimedia.org/wiki/File:Aethia_cristatella.jpg) |
| Great Potoo | Hector Bottai | [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/) | [Commons](https://commons.wikimedia.org/wiki/File:Nyctibius_grandis_-_Great_Potoo;_Apiac%C3%A1s,_Mato_Grosso,_Brazil.jpg) |
| Hamerkop | Charles J. Sharp | [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/) | [Commons](https://commons.wikimedia.org/wiki/File:Hamerkop_(Scopus_umbretta)_Botswana.jpg) |
| Kiwi | Maungatautari Ecological Island Trust | Public domain | [Commons](https://commons.wikimedia.org/wiki/File:TeTuatahianui.jpg) |
| Long-wattled Umbrellabird | Hectonichus | [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/) | [Commons](https://commons.wikimedia.org/wiki/File:Cotingidae_-_Cephalopterus_penduliger.jpg) |
| Marabou Stork | Charles J. Sharp | [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/) | [Commons](https://commons.wikimedia.org/wiki/File:Marabou_stork_(Leptoptilos_crumenifer).jpg) |
| Roseate Spoonbill | Mwanner | [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/) | [Commons](https://commons.wikimedia.org/wiki/File:Roseate_Spoonbill_-_Myakka_River_State_Park.jpg) |
| Secretarybird | Sumeet Moghe | [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/) | [Commons](https://commons.wikimedia.org/wiki/File:Secretary_bird_Mara_for_WC.jpg) |
| Shoebill | Bob Owen | [CC BY 2.0](https://creativecommons.org/licenses/by/2.0/) | [Commons](https://commons.wikimedia.org/wiki/File:Balaeniceps_rex_-Ueno_Zoo,_Tokyo,_Japan-8a.jpg) |
| Tawny Frogmouth | JJ Harrison | [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/) | [Commons](https://commons.wikimedia.org/wiki/File:Podargus_strigoides_Bonorong.jpg) |
| Three-wattled Bellbird | Noel Reynolds | [CC BY 2.0](https://creativecommons.org/licenses/by/2.0/) | [Commons](https://commons.wikimedia.org/wiki/File:Three-wattled_Bellbird_(Procnias_tricarunculata)_(5818992683).jpg) |
| Vulturine Guineafowl | Nikhil R R | [CC0](https://creativecommons.org/publicdomain/zero/1.0/) | [Commons](https://commons.wikimedia.org/wiki/File:Acryllium_vulturinum_431568323.jpg) |

## Adding a bird

1. Find a photo on Wikimedia Commons under a licence that allows commercial use and
   redistribution (public domain, CC0, CC BY, or CC BY-SA — **not** GFDL or non-commercial).
2. Download it at about 1400 px on the long edge and save it to
   `app/src/main/assets/birds/` named for the bird, with underscores for spaces:
   `Blue-footed_Booby.jpg`. That filename *is* the answer shown in the game, so spell it
   carefully.
3. Add a line to `app/src/main/assets/birds/credits.txt` and a row to the table above.

Don't crop or edit the photos. Scaling keeps the bundle a plain collection of unmodified works,
which is why the share-alike licences above don't reach the app's own MIT licence.

## The app's own code

Pintley's source is released under the [MIT License](LICENSE). The photos above are **not**
covered by it — they keep the licences listed in the table.
