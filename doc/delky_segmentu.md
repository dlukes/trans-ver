# Délky segmentů

Tento návod popisuje, jak využít informace o příliš dlouhých segmentech (víc
než 25 slov), které vám TransVer poskytuje. Předpokládá, že už jste program
úspěšně spustili a výsledky kontroly máte k dispozici; pokud ne a nevíte si s
ovládáním rady, podívejte se na
[oddíl o ovládání programu v úvodu](./intro.md#ovladani).

Po provedení kontroly alignace by měl TransVer dodat výstup v následující
podobě:

![Délky segmentů -- obrázek](./delky_segmentu.png "TransVer -- kontrola délek
 segmentů")

To znamená, že výstup kontroly se nezobrazuje v okně TransVeru, ale je zapsán
přímo do nového souboru .eaf. Ten se nachází ve stejné složce, jako váš
zdrojový soubor .eaf; i jméno má stejné, jen je k němu připojeno ještě aktuální
datum, aby nedošlo k přepsání vašeho původního souboru. (Jméno si samozřejmě
můžete libovolně změnit, ale **je rozumné si alespoň po nějakou dobu vedle nového
souboru ponechat i ten původní**).

Nový soubor .eaf (obsahující vrstvu `KONTROLA DÉLKY SEGMENTŮ`) si nyní otevřte
v ELANu. Přepis v Annotation Mode by měl vypadat nějak následovně:

![Délky segmentů v ELANu -- obrázek](./delky_segmentu-ELAN.png "ELAN --
 kontrola délek segmentů")

S trochou štěstí bude vrstva `KONTROLA DÉLKY SEGMENTŮ` prázdná, tj. na obrázku
zakroužkované číslo bude 0; pak není potřeba dělat nic. Pokud prázdná není, je
potřeba přepis projít a každý segment, který je vyznačen i v nové kontrolní
vrstvě, rozdělit na příslušné množství segmentů menších (viz postup rozdělování
segmentů v manuálu programu ELAN, sekce 3.8 Rozdělení segmentu). K tomu vám má
orientačně pomoci počet slov v daném segmentu, který je uvedený v anotaci
kontrolního segmentu: např. "PŘÍLIŠ DLOUHÝ SEGMENT: 30" znamená, že segment
obsahuje 30 slov.

V praxi by bylo samozřejmě příliš pracné počítat délky nově vzniklých segmentů
ručně. Mnohem smysluplnější je **rozdělení provést "od oka"** v místech, která
se nabízejí (pauzy a předěly), pak soubor .eaf uložit, znovu ho nechat
zkontrolovat TransVerem a případné zbývající dlouhé segmenty (mezi těmi nově
vytvořenými) rozdělit při tomto druhém průchodu.

Po poslední kontrole prostřednictvím TransVeru se ujistěte, že soubor .eaf
skutečně neobsahuje již žádné segmenty delší než 25 slov, a vrstvu `KONTROLA
DÉLKY SEGMENTŮ` případně smažte.
