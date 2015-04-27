# TransVer

## [EN]

A transcription verifier for the current spoken corpus project at the Czech
National Corpus Institute (http://www.korpus.cz). Probably not useful outside this
project, so the rest of the description will be in Czech. Contact me via github
should you need details in English :)

## [CS]

TransVer slouží k verifikaci správnosti transkripce v rámci aktuálně budovaného
mluveného korpusu ÚČNK (http://www.korpus.cz). Vstupem mu je soubor `.eaf`
obsahující přepis sondy v programu [ELAN](tla.mpi.nl/tools/tla-tools/elan/)
podle platných interních pravidel ÚČNK.

Zpětnou vazbu posílejte na e-mailovou adresu uvedenou
[v mém profilu na githubu](http://github.com/dafydd-lukes/), nebo použijte
přímo
[rozhraní githubu pro hlášení problémů](http://github.com/dafydd-lukes/trans-ver/issues).

## Instalace

Stáhněte si soubor `trans-ver-<č. verze programu>-standalone.jar` z následující
adresy: https://trnka.korpus.cz/~lukes/trans-ver-0.5.0-standalone.jar

## Použití

Pokud je váš systém (Windows, Linux, MacOSX) správně nastaven, měl by jít
program spustit pouhým dvojkliknutím na soubor `trans-ver-<č. verze
programu>-standalone.jar` (pokud ne, viz sekce [Řešení problémů](#problemy)).

V případě potřeby (např. kvůli sledování chybových hlášek) lze TransVer spustit
i z příkazového řádku (pokud máte správně nastavenou cestu k programu `java`),
a to následovně:

    $ java -jar trans-ver-<č. verze programu>-standalone.jar

**Dokumentace** k programu včetně **návodu k použití**, který obsahuje i mnohé
konkrétní příklady, je dostupná [zde](./doc/intro.md).

## Řešení problémů <a name="problemy"></a>

1. Mnoho problémů by mělo jít vyřešit instalací nejnovější verze Javy (JRE 7 či
   8), dostupné z http://www.java.com/download.

2. Pokud v systému **Windows nefunguje spouštění dvojklikem** na soubor
   `trans-ver-<č. verze programu>-standalone.jar`:

    - klikněte na soubor pravým tlačítkem
    - z menu zvolte *Otevřít v programu* → *Zvolit výchozí program...*
    - jako výchozí program zvolte prostředí Java (Java Runtime)
    - zaškrtněte možnost *K otevírání souborů tohoto typu vždy používejte
      vybraný program*

<!-- ## Options -->

<!-- FIXME: listing of options this app accepts. -->

<!-- ## Examples -->

<!-- ... -->

<!-- ### Bugs -->

<!-- ... -->

## License

Copyright © 2013--2015 David Lukeš

Distributed under the GNU General Public License v3.
