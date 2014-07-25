# Alignace vrstev ort a fon

*Upozornění*: detailní popis pravidel a značek, kterými se v přepisu na stopě
fon zachycuje alignace s vrstvou ort, je dostupný
[zde](https://trnka.korpus.cz/mluvka2/wiki/doku.php?id=alignace) (po přihlášení
do databáze Mluvka2), včetně vysvětlení, proč je alignace nutná, a
příkladů. Pokud s alignací ještě nejste obeznámeni, přečtěte si nejprve prosím
výše uvedený dokument.

Tento návod popisuje, jak využít informace o chybách v alignaci vrstev ort a
fon, které vám TransVer poskytuje. Předpokládá, že už jste program úspěšně
spustili a výsledky kontroly máte k dispozici; pokud ne a nevíte si s ovládáním
rady, podívejte se na [oddíl o ovládání programu v úvodu](./intro.md#ovladani).

Po provedení kontroly alignace by měl TransVer dodat výstup v následující
podobě:

![Alignace -- obrázek](./alignace.png "TransVer -- kontrola alignace")

Každý segment, v němž si počet slov na vrstvách ort a fon neodpovídá, je
shrnut v tabulce, která obsahuje v prvním sloupci slova na vrstvě ort a v
druhém sloupci slova na vrstvě fon. Připomínáme, že mezi dvěma slovy na vrstvě
fon může být kromě mezery (jako na vrstvě ort) i svislítko `|`, které značí, že
obě slova foneticky splývají do jednoho přízvukového taktu.

Mějme např. následující chybovou tabulku:

    Segment č. 36 v této vrstvě; čas: 00:05:16.120.
    |        ort |        fon |
    ===========================
    |      [no]  |      [nó]  |
    |         .  |         .  |
    |       teď  |       teť| |
    |        se  |        se  |
    | například  | napříklat  |
    |   strašně  |   strašňe  |
    |      těší  |      ťeší  |
    |        že  |   žebudou  |
    |     budou  |       mít  |
    |       mít  |    jehňata |
    |    jehňata |            |

Z ní je patrné, že až do slova "těší" je vše v pořádku (slova na obou vrstvách
si odpovídají). Pak ale máme na vrstvě ort pouhé "že", kdežto na vrstvě fon je
přepis "žebudou". To jsou jasně slova dvě (pouze foneticky splývají v jeden
takt), jen se při přepisu zapomnělo na doplnění svislítka, které tento fakt
zachytí způsobem srozumitelným i pro počítač. Stačí tedy otevřít přepis v
programu ELAN a do příslušného segmentu na stopě fon doplnit mezi "že" a
"budou" svislítko: `že|budou`.

Ideální je v tuto chvíli **soubor .eaf v ELANu uložit a znovu spustit kontrolu
v TransVeru**: právě opravená chyba se již nezobrazí a můžete pokračovat těmi
zbývajícími. Toto se vyplatí zejména v případě, pokud máte vícero chyb v jednom
segmentu, protože když nastane větší posun mezi vrstvou ort a fon, nemusí být
úplně lehké vyčíst z tabulky způsob, jak problémy ke konci segmentu
napravit. Ale pokud je chyb málo, stačí pochopitelně spustit kontrolu jen
jednou a opravit je všechny naráz.

Povšimněte si též, že před každou tabulkou jsou uvedeny dvě informace, podle
nichž lze segment v programu ELAN jednoduše najít. Jedná se o:

1. **Číslo** segmentu **v rámci dané vrstvy**, podle něhož lze segment v ELANu
   identifikovat v **Transcription Mode** (viz níže popis způsobu, jak si v
   Transcription Mode zobrazit jen jednu vrstvu).
2. **Čas začátku** segmentu, který se hodí při hledání segmentu v **Annotation
   Mode**.

Pokud chcete, aby čísla segmentů uváděná TransVerem odpovídala číslům
zobrazovaným v Transcription Mode v ELANu, musíte si ELAN nastavit tak, aby
zobrazoval pouze relevantní vrstvu. To učiníte tak, že v Transcription Mode v
rámečku `Settings` po levé straně kliknete na tlačítko `Configure...`. Vyskočí
dialog, v němž klikněte na tlačítko `Select tiers...`, a mezi nabízenými
vrstvami si vyberte pouze tu, kterou zrovna opravujete.

Na závěr -- po opravení všech nahlášených chyb -- pro jistotu spusťte TransVer
ještě naposledy jednou, abyste se ujistili, že vám žádná nesrovnalost v
alignaci opravdu neunikla. Alignaci totiž zaznamenáváme kvůli dalšímu
počítačovému zpracování přepisů a má smysl pouze v případě, že je bezchybná.

# Další typy chyb a jejich řešení -- příklady

## Přebytečné svislítko

Občas se může stát, že svislítko omylem vložíte hned vedle mezery,
např. původní ortografický přepis *[ne] no [až]* převedete na fonetickou
transkripci *[ne] no| [aš]*.

    |   ort |                   fon |
    =================================
    | [ne]  |                 [ne]  |
    |   no  |                   no| |
    |  [až] | SLOVOBEZFONREALIZACE  |
    |       |                  [aš] |

Takovýto zápis normálně znamená, že po slovu *no* následuje slovo, o němž
bezpečně víme, že ho mluvčí zamýšlel, ale nemá žádnou zvukovou (fonetickou)
realizaci (na což vás program upozorní tím, že do tabulky umístí pomocný
řetězec `SLOVOBEZFONREALIZACE`).¹ V tomto případě je to ovšem omyl, protože na
vrstvě ort mezi slovy *no* a *až* žádné slovo navíc není. Stačí tedy zbloudilé
svislítko umazat a chyba zmizí.

## Posunutá hranice závorky

Chybou je i případ, kde je hranice kulatých `()` či hranatých závorek `[]`
umístěna na obou vrstvách jinde (závorky složené `{}`jsou pouze na vrstvě fon,
takže roli v tomto ohledu nehrají). Např. zde:

    Segment č. 134 v této vrstvě; čas: 00:18:48.400.
    |      ort |       fon |
    ========================
    |      ty  |      {ti| |
    | nebudou  | nebədou}  |
    |   tikat  |     ťika  |
    |      ty  |      [ti| |
    | [druhý]  |   druhí]  |
    |      ..  |       ..  |
    |      ne  |       né  |
    |       .. |        .. |

Na ortu je v překryvu pouze *[druhý]*, na fonu *[ti|druhí]*; je tedy potřeba
upravit ort na *[ty druhý]*.

## Porušení symetrie u pauz (., ..) či hezitací (@)

    Segment č. 89 v této vrstvě; čas: 00:08:17.508.
    |       ort |       fon |
    =========================
    |     jako  |     jako  |
    |  [jestli  |     [esi| |
    |      tam  |      tam  |
    | náhodou]  | náhodou]  |
    |     něco  |     ňeco  |
    |   nevidí  |   neviďí  |
    |       ..  |        .  |

Vrstva ort má pauzu, vrstva fon pouze předěl -- je potřeba zápis
sjednotit. Podobně s hezitacemi:

    Segment č. 13 v této vrstvě; čas: 00:03:50.110.
    |      ort |      fon |
    =======================
    |      ve  |      ve| |
    | čtvrtek  | štvrtek  |
    |       s  |      ss| |
    |        @ |        ǝ |

Na vrstvě ort je značena hezitace *@*, na vrstvě fon místo ní *ǝ*. Toto je
potřeba sjednotit -- buď nechat na ortu pouze *s* a fon přepsat jako *ssǝ*, nebo
zachovat hezitaci na ortu (*s @*) a doplnit ji i na fonu (podle uvážení a zvuku
buď *ssǝ @* nebo *ss @*.

Podobný problém může nastat, když je porušena symetrie u dalších speciálních
značek sdílených oběma vrstvami fon a ort -- např. záměna navazovacího znaku
*+* za znak přerušené výpovědi *-* apod.

## Znak sdílení hlásky (_) uvnitř slova

Sdílení hlásky značíme pouze na hranici dvou slov (nedává smysl, aby jedno
slovo hlásku "sdílelo" samo se sebou). TransVer tedy ohlásí chybu, pokud
bezprostředně po znaku pro sdílení hlásky `_` nenajde hranici slova (svislítko
`|` nebo mezeru):

    Segment č. 287 v této vrstvě; čas: 00:27:08.371.
    |      ort |                   fon |
    ====================================
    |      no  |                   no  |
    |       .  |                    .  |
    |     tak  |                  tak| |
    |      to  |                   tə| |
    |      je  |                    e  |
    |   dobrý  |                dobrí  |
    |     tak  |                  tak| |
    |       v  |                    f| |
    |   pátek  |                padeg  |
    | [můžeme  |             [mužem_e| |
    |     jet  | SLOVOBEZFONREALIZACE  |
    |   spolu] |                  spo] |

Ve slově *mužem_e* je `_` uprostřed slova, přitom se asi editor snažil
naznačit, že koncové *e* je sdílené i následujícím slovesem *jet*. Řešení je
jednoduché: umístíme `|`, kam patří, tj. ihned za `_`.

Podobně TransVer zahlásí chybu také v případě, že je `_` umístěno na začátek
druhého slov místo na konec prvního, např. `jag|_diš` místo `ja_|gdiš`.

¹ Více o zápisu slov bez fonetické realizace viz bod 2
[zde](https://trnka.korpus.cz/mluvka2/wiki/doku.php?id=alignace#nektere_slozite_pripady_a_jejich_reseni).
