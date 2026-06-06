# Reporter Presentation Plan

## Ziel

Das bestehende Escape-Room-Level wird nachtraeglich als begehbare Masterarbeits-
Praesentation inszeniert. Der Spieler ist ein Reporter, der den Dungeon frei
erkundet und mit NPCs ueber fruehere Ausbrueche, Studiendaten und typische
Spielerverhalten spricht.

Die Zahlen werden klar getrennt berichtet:

- Rohbestand: was technisch oder organisatorisch insgesamt vorliegt.
- Finale Auswertungsstichprobe: was fuer die bereinigte Analyse sinnvoll
  berichtet wird.

Dialogsprache ist Deutsch. Technische Feldnamen, Eventnamen und Rollenwerte
duerfen in ihrer englischen Originalschreibweise stehen.

## Bestehender Level-Kontext

- Zielmodul: `escapeRoom`
- Zielkarte: `escapeRoom/assets/levels/maroom_1.level`
- Zielklasse: bestehende `MADungeonRoom`; kein neuer Starter und keine neue
  Levelvariante
- Spawn-/Interaktionspunkte: `coll_map`, `coll_1` bis `coll_11`
- Presentation-Demo als UI-Vorlage:
  - Speaker-Dialoge
  - Multiple-Choice-Dialoge
  - TextDialog mit Inline-/Blockbildern
  - Fullscreen-Bilddialoge
  - Yes/No-Dialoge

## Datenbasis

### Finale Kennzahlen

Rohdaten:

- 30 Sessions
- 59 Teilnahmen
- 51 eindeutige technische Spieler-IDs
- 24 abgeschlossene Sessions
- 6 offene oder nicht abgeschlossene Sessions

Finale Analysebasis:

- 25 technische Sessions
- 46 surveyverknuepfte Spielerfaelle
- 21 vollstaendige Zweierteams bzw. 42 Spielerfaelle
- 24 erfolgreiche Ausbrueche
- 1 offene bzw. nicht erfolgreich abgeschlossene Session

### Insassen

Mit "Insourcen" sind Insassen gemeint: also wie viele Personen insgesamt im
Gefaengnis waren. Fuer die Praesentation kann das mit den Teilnahmen bzw.
Spielerfaellen verbunden werden, je nachdem ob der Dialog Rohdaten oder finale
Analysebasis meint.

### Rollen

Finales Player Sample:

- 23 `APPRENTICE`
- 23 `ROGUE`

Roh-Telemetriedaten:

- 25 Apprentice-Spieler
- 26 Rogue-Spieler

Fuer die finale Survey-Auswertung wird die exakt ausgeglichene Verteilung
berichtet.

### Hints

- 104 `hint_requested`-Events
- in 24 Sessions
- von 39 Spielern

Haeufigste Hint-Titel:

- `Flucht`: 21
- `Kalte Gefilde`: 16
- `Energie aus der Flasche`: 15
- `Flucht 1`: 9
- `Abenddaemmerung`: 8
- `Flucht 2`: 8
- `Morgengrauen`: 8

### Crafting und craft-nahe Proxies

Direkte `crafted`-Events liegen nicht vor, obwohl der Verb-Typ im Code existiert.
Belastbar sind daher Item-Nutzung, Drops, Skill-Casts und GUI-Events.

Craft-nahe Item-Interaktionen:

- `Axt`: 34
- `Baumstamm`: 21
- `Spitzhacke`: 15
- `Hoelzerne Bruecke`: 6

Wenn Nutzung als Proxy verwendet wird, ist die `Axt` das haeufigste craft-nahe
Item.

Skill-Erfolg und Fehlversuche:

- `Spitzhacke`: 435 fehlgeschlagen, 36 erfolgreich
- `Bruecke platzieren`: 51 fehlgeschlagen, 54 erfolgreich
- `WallbreakerSkill`: 64 fehlgeschlagen, 2 erfolgreich

### Wand, Werkzeuge und Brueckenbau

Teilweise messbar ueber:

- `Spitzhacke`
- `WallbreakerSkill`
- `Eiswand`
- `Axt`
- `Baumstamm`
- `Hoelzerne Bruecke`
- `Bruecke platzieren`

Ein sauberer Prozentzaehler fuer "Wand wurde beschaedigt" liegt nicht vor.
Brueckenplatzierung ist gut sichtbar:

- 54 erfolgreiche `Bruecke platzieren`-Casts
- 51 fehlgeschlagene `Bruecke platzieren`-Casts

### Captures und Hotspots

Positionsdaten:

- Bewegungen enthalten `pos_x` und `pos_y`
- Captures enthalten `position_x` und `position_y`
- Heatmap/Overlay-Abbildung existiert bereits

Wichtigste Capture-Hotspots:

- `(75, 31)`: 16 Festnahmen
- `(79, 56)`: 5 Festnahmen
- mehrere weitere Koordinaten mit jeweils 4 Festnahmen

Der illegale Bereich ist auswertbar. Positionsdaten liegen dort vor allem fuer
`moved`-Events vor.

### QTE und Gefangenschaft

Following-Indicator-QTE im Technical Sample:

- 2082 erfolgreiche Durchlaeufe
- 824 fehlgeschlagene Durchlaeufe
- Fehlerrate: ca. 28,36 %

Festhaltezeit kommt aus `released.time_captured_ms`, nicht direkt aus dem QTE.

Player Sample:

- insgesamt 105,20 Minuten Gefangenschaftszeit
- Median pro Spielerfall: 1,82 Minuten
- Mittelwert pro Spielerfall: 2,34 Minuten

### Verstecken, Bett und Ecken

Keine direkten Events gefunden fuer:

- `bed`
- `bett`
- `hide`
- `versteck`
- `corner`
- `ecke`

Die Aussage zu Betten, Ecken und falschen Versteckannahmen ist daher narrativ
und levelbezogen, nicht telemetry-basiert.

### Fackeln, Stamina, Labyrinth und Hidden Areas

Stamina:

- 514861 Events mit Stamina-Wert
- Mittelwert: 95,10
- Median: 102,73
- Minimum: 0
- Maximum: ca. 160
- 312 `exhausted`-Events
- 293 `recovered`-Events

Fackeln sind indirekt sichtbar ueber:

- `cast_skill`
- `changed_skill`
- `used_item`
- `dropped`

Labyrinth und Hidden Areas sind nicht sauber als eigener Eventtyp getrackt. Sie
sind indirekt ueber Positionen, Fackel-Events, Captures und vorhandene
Level-/Overlay-Bilder auswertbar.

### Push-Riddle

Messung:

- Start ueber `tries`
- Ende ueber `solved` oder `left`
- zusaetzlich `solve_time_ms` bzw. `time_in_riddle_ms`

Technical Sample:

- 25 Sessions erreicht
- 11 Sessions geloest
- sessionbasierte Erfolgsquote: 44 %

Riddle-spezifisch:

- 443 `tries`
- 415 `left`
- 22 `solved`-Events
- Median-Solve-Zeit: 763,44 s
- Bestzeit: 576,94 s
- Durchschnitt: 867,37 s

Haeufigste Fehler sind nicht als konkrete Fehlertypen semantisch kodiert.
Formal gibt es im Push-Kontext 3 `failed`-Events durch QTE, ansonsten vor allem
`left` und erneutes Betreten.

### Eisraetsel

Technical Sample:

- 20 Sessions erreicht
- 7 Sessions geloest
- sessionbasierte Erfolgsquote: 35 %

Riddle-spezifisch:

- 303 `tries`
- 289 `left`
- 14 `solved`-Events
- Median-Solve-Zeit: 152,79 s
- Bestzeit: 120,48 s
- Durchschnitt: 235,97 s

Koop-Anteil bei geloesten Sessions:

- 7 von 7 geloesten Sessions hatten beide Spieler als Solver
- damit 100 % der geloesten Faelle

## Vorhandene Graphen und Bilder

Vorhandene Evaluation-Grafiken umfassen unter anderem:

- `sample_selection_flow`
- Altersdiagramme
- Sessiondauer-Histogramm
- Hint- und Capture-Boxplots
- Eventtypen-Barplot
- Challenge-Outcomes
- Level-Heatmap mit Capture-Hotspots
- IPQ-Balkendiagramm
- Pre/Post-Future-Skills
- Scatterplots zu Stamina, Captures, Hints, Schwierigkeit/Balancing und
  Spielspass

LaTeX-Einbindung:

- `C:\Users\niklas\OneDrive - Fachhochschule Bielefeld\_Master\_3. Semester\Masterarbeit\MA_Workspace\MA\sites\evaluierung.tex`

Bildverzeichnisse:

- `C:\Users\niklas\OneDrive - Fachhochschule Bielefeld\_Master\_3. Semester\Masterarbeit\MA_Workspace\MA\pictures`
- `C:\Users\niklas\OneDrive - Fachhochschule Bielefeld\_Master\_3. Semester\Masterarbeit\MA_Workspace\db\analytics\exports\plots`

## Datenbankzugriff fuer Nachrecherche

Falls weitere Details aus der EOLI/Analytics-Datenbank gebraucht werden, liegt
der Postgres-Stand lokal in Docker.

Container:

```powershell
docker ps -a
docker start flamboyant_lalande
```

Readiness-Check:

```powershell
docker exec flamboyant_lalande pg_isready -U dungeon_master -d dungeon_analytics
```

Interaktiver `psql`-Zugriff:

```powershell
docker exec -it flamboyant_lalande psql -U dungeon_master -d dungeon_analytics
```

Connection URL fuer Skripte:

```text
postgresql://dungeon_master:password@localhost:5432/dungeon_analytics
```

Beispiel fuer bestehenden Replay-Exporter:

```powershell
python tools\export_xapi_replay.py `
  --db-url "postgresql://dungeon_master:password@localhost:5432/dungeon_analytics" `
  --docker-container flamboyant_lalande `
  --last-push-riddle-try `
  --last-push-riddle-since "2026-02-05" `
  --level MADungeonRoom `
  --output "escapeRoom/assets/replays/push_riddle_last_successful_try.json"
```

Sinnvolle zusaetzliche Grafiken:

- Push-/Eisraetsel-Zeitverteilungen
- sessionbasierte Erfolgsquoten pro Raetsel
- Top-Hints als Balkendiagramm
- Fackel-/Stamina-Verlauf ueber Zeit
- Wand-/Bruecken-Skill-Erfolg vs. Fehlversuch
- Capture-Hotspot-Zoom fuer den illegalen Bereich
- kompakte Tabelle "messbar vs. nur narrativ"

## Dialog- und Stationsplan

### `coll_map` - Grosse Karte

Rolle:

- zentrale Orientierung fuer den Reporter
- zunaechst Placeholder-Bild
- spaeter geplante freie Kartenkamera mit Zoom auf die ganze Map

Inhalt:

- offizieller Rundgang
- Ausbruchswege
- Capture-Hotspots
- Push-Riddle
- illegaler Bereich
- Labyrinth
- Eisraetsel

UI:

- Fullscreen-Bilddialog mit Placeholder
- spaeter Ersetzung durch freie Kamera-Interaktion

### `coll_1` - Guard am Eingang

Textur:

- `character/knight` oder `character/blue_knight`

Inhalt:

- Guard fragt, warum der Reporter wieder im Dungeon ist
- Reporter erklaert, dass er Ausbrueche fuer eine Forschungsnachbereitung
  dokumentiert
- Guard erlaubt Zutritt und warnt vor grauen Bereichen, Werkzeugen und
  eigenmaechtigem Herumlaufen

UI:

- Speaker-Dialog
- optional Multiple Choice fuer die Reporter-Antwort

### `coll_2` - Gefaengnisdirektor

Textur:

- Sprite: `character/knight` oder passender Office-/Boss-Ersatz
- Portrait: `office/boss.png`

Inhalt:

- Rohbestand vs. finale Analysebasis
- Insassen/Teilnahmen
- erfolgreiche und offene Sessions
- ausgeglichene Rollenverteilung im finalen Player Sample

UI:

- Multiple Choice als Statistik-Hub
- Antwortoptionen: Rohbestand, finale Stichprobe, Rollen, Ausbrueche,
  Gefangenschaft

### `coll_3` - Wizard / Hint-Giver

Textur:

- `character/wizard`

Inhalt:

- Wizard war der einzige NPC, der direkt mit Spielern geredet hat
- 104 `hint_requested`-Events
- 24 Sessions, 39 Spieler
- Top-Hints: `Flucht`, `Kalte Gefilde`, `Energie aus der Flasche`, `Flucht 1`,
  `Abenddaemmerung`, `Flucht 2`, `Morgengrauen`
- Rollenueberblick: `APPRENTICE` und `ROGUE`

Umsetzungshinweis:

- alte Hint-Giver-Logik fuer diese Praesentationsversion entfernen oder
  deaktivieren

### `coll_4` - Apprentice / Crafting

Textur:

- `character/char03`

Inhalt:

- Crafting nochmal erklaeren
- offen sagen, dass direkte `crafted`-Events nicht belastbar vorliegen
- stattdessen craft-nahe Proxies nutzen
- `Axt` als haeufigste craft-nahe Nutzung
- Fehlversuche bei `Spitzhacke`, `Bruecke platzieren`, `WallbreakerSkill`

Ton:

- Apprentice erklaert es praktisch und etwas stolz
- Reporter fragt kritisch nach, ob das echtes Crafting oder eine Proxy-Auswertung
  ist

### `coll_5` - Rogue / Push-Riddle

Textur:

- `character/rogue`

Zustand 1:

- Rogue steht vor den Felsformationen
- er kann die Steine noch nicht bewegen
- er braucht den Staerkering
- sein Apprentice sucht ihn
- Reporter soll spaeter wiederkommen

Zustand 2:

- dieses Push-Riddle-Paar ist bereits draussen
- sie berichten ueber 25 erreichte Sessions, 11 geloeste Sessions und 44 %
  sessionbasierte Erfolgsquote
- Median-Solve-Zeit 763,44 s
- Bestzeit 576,94 s
- Durchschnitt 867,37 s
- `left`/erneutes Betreten wird als typische Frustrationsspur erzaehlt

Wichtig:

- Das Push-Riddle-Paar ist ein anderes Paar als das Eisraetsel-Paar.

### `coll_6` - Wache bei Wand und Bruecke

Textur:

- `character/knight` oder `character/blue_knight`

Inhalt:

- Wache ist frustriert ueber beschaedigte Waende
- Insassen finden Wartungswerkzeuge hinter Waenden
- Brueckenbau ueber den Burggraben
- `Bruecke platzieren`: 54 erfolgreich, 51 fehlgeschlagen
- `WallbreakerSkill`: 64 fehlgeschlagen, 2 erfolgreich
- `Spitzhacke`: 435 fehlgeschlagen, 36 erfolgreich

Ton:

- genervte Instandhaltungswache

### `coll_7` - Apprentice vor illegalem Bereich

Textur:

- `character/char03`

Inhalt:

- Apprentice warnt vor grauen Boden- und Wandteilen
- Bereich ist nur fuer Wachen zugaenglich
- er versucht selbst gerade hineinzukommen

UI:

- Yes/No-Dialog: "Trotzdem weitergehen?"

Zustand:

- kann einen Praesentationszustand setzen, dass der Reporter den illegalen
  Bereich bewusst betreten hat

### `coll_8` - Wache im illegalen Bereich

Textur:

- echte Wache ueber das Wachensystem

Inhalt:

- Reporter wird im illegalen Bereich erwischt
- Wache fuehrt ihn wirklich in eine Einzelhaftzelle
- dort erklaert sie QTE- und Gefangenschaftsdaten

Daten:

- 2082 erfolgreiche Following-Indicator-QTEs
- 824 fehlgeschlagene QTEs
- Fehlerrate ca. 28,36 %
- 105,20 Minuten Gesamt-Gefangenschaftszeit im Player Sample
- Median 1,82 Minuten pro Spielerfall
- Mittelwert 2,34 Minuten pro Spielerfall

Umsetzungshinweis:

- Der Reporter soll standardmaessig kein `IllegalComponent` haben.
- Erst beim Interagieren mit der `coll_8`-Wache bzw. beim passenden Trigger
  bekommt er einmalig ein `IllegalComponent` mit einer passenden illegalen
  Reason.
- Guards erkennen Spieler ueber `IllegalComponent::isIllegal`.
- Das bestehende Guard-System fuehrt gefasste Spieler bereits zur named point
  `cell`; die Einzelhaftzelle muss daher nicht neu implementiert werden.
- Nach dieser einmaligen Szene darf der Reporter nicht erneut durch diese
  Praesentationslogik illegal werden.

### `coll_9` - Ausbrecher im Versteckraum

Textur:

- `character/rogue` oder `character/char03`

Inhalt:

- Ausbrecher glaubt, er sei gut versteckt
- Reporter erklaert, dass genau dieser Raum ein haeufiger Capture-Hotspot ist
- harte Daten zu Bett/Ecken/Verstecken gibt es nicht
- die Bett-/Ecken-Erklaerung bleibt narrativ

Daten:

- `(75, 31)` mit 16 Festnahmen
- `(79, 56)` mit 5 Festnahmen
- mehrere Koordinaten mit je 4 Festnahmen

### `coll_10` - Ausbrecher im Labyrinth

Textur:

- `character/rogue` oder `character/char03`

Inhalt:

- Ausbrecher hat zu wenige Fackeln gebaut
- er ist erschoepft, weil er nicht auf Stamina geachtet hat
- Reporter weist auf versteckte Bereiche hin, z. B. links

Daten:

- 514861 Stamina-Events
- Mittelwert 95,10
- Median 102,73
- 312 `exhausted`
- 293 `recovered`
- Fackeln nur indirekt ueber Item-/Skill-Events sichtbar

### `coll_11` - Ausbrecher am Eisraetsel

Textur:

- `character/rogue` oder `character/char03`

Inhalt:

- Ausbrecher wartet auf Kollegen
- er weiss nicht, wie er auf Eis stehen soll, ohne zu rutschen
- er erklaert, dass das Raetsel stark kooperativ gepraegt war

Daten:

- 20 Sessions erreicht
- 7 Sessions geloest
- sessionbasierte Erfolgsquote 35 %
- 303 `tries`
- 289 `left`
- 14 `solved`
- Median-Solve-Zeit 152,79 s
- Bestzeit 120,48 s
- Durchschnitt 235,97 s
- 100 % der geloesten Faelle hatten beide Spieler als Solver

Wichtig:

- Das Eisraetsel-Paar ist ein anderes Ausbrecherpaar als das Push-Riddle-Paar.

## Zustandslogik

- Reporter laeuft frei in der Open World.
- Alle `coll_*`-Stationen sind grundsaetzlich frei ansteuerbar.
- Jeder NPC bekommt einen Hauptdialog und einen kurzen Wiederholungsdialog.
- `coll_5` braucht zwei Dialogzustaende: vor und nach dem Push-Riddle-Ausbruch.
- `coll_7` kann den illegalen Bereich narrativ markieren.
- `coll_8` ist die einzige harte Wachinteraktion:
  - echte Wache
  - echtes Abfuehren in die Einzelhaftzelle
  - einmalige Illegal-Markierung fuer den Reporter
  - danach keine erneute Illegal-Markierung
- `coll_map` bleibt jederzeit erneut abrufbar.

## Platzhalter-Assets

Zunaechst benoetigt:

- Textbox-Placeholder fuer grosse Karte, z. B. `<Placeholder: grosse Karte mit
  Fluchtrouten und Capture-Hotspots>`
- Textbox-Placeholder fuer Statistiktafel, z. B. `<Placeholder:
  Rohbestand-vs-Auswertungsstichprobe>`
- Textbox-Placeholder fuer Hint-Statistik, z. B. `<Placeholder: Top-Hints als
  Balkendiagramm>`
- Textbox-Placeholder fuer Capture-Hotspot-Heatmap, z. B. `<Placeholder:
  Heatmap mit Hotspot (75,31)>`
- Textbox-Placeholder fuer Push-Riddle-Zeitdaten, z. B. `<Placeholder:
  Push-Riddle-Zeitverteilung>`
- Textbox-Placeholder fuer Eisraetsel-Zeitdaten, z. B. `<Placeholder:
  Eisraetsel-Zeitverteilung>`

Spaeter moeglich:

- freie Kartenkamera statt statischem Fullscreen-Bild
- echte Heatmap-/Overlay-Bilder aus der Evaluation
- zusaetzlich generierte Diagramme fuer fehlende Darstellungen

## Offene Punkte vor Implementierung

- Klaeren, wie die alte Wizard-Hint-Giver-Interaktion ersetzt wird.
- Pruefen, wo in `MADungeonRoom` die NPCs und Dialoginteraktionen am saubersten
  ergaenzt werden.
- Pruefen, ob der Reporter in der Praesentationsversion durch `IllegalSystem`
  standardmaessig legal bleibt oder ob `IllegalSystem` derzeit bei jedem
  Player-Tick automatisch ein leeres `IllegalComponent` erzeugt.
- Falls noetig: Praesentationslogik so bauen, dass `coll_8` genau einmal eine
  illegale Reason setzt und danach nicht erneut.
- Alle zusaetzlichen Bildinhalte zunaechst als klare Textbox-Placeholder
  formulieren, nicht als neue PNG-Assets.
