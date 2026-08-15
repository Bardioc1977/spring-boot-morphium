# M2 — Review-Triage: PR #16 (Bardioc1977/morphium#16)

Quelle: Copilot, Codex (chatgpt-codex-connector), CodeRabbit — alle drei Reviews auf
Commit `cdc468ce6` (Branch `pr/jakarta-data-module` gegen `Bardioc1977/morphium:master`).

Legende Prioritaet:
- **P0** — Datenkorrektheit / falsches Ergebnis ohne Fehlermeldung (silent wrong behavior)
- **P1** — Funktionaler Bug mit Fehlermeldung oder eingeschraenktem Blast-Radius
- **P2** — Robustheit / Edge-Case, kein alltaeglicher Pfad
- **P3** — Doku / Codequalitaet, kein Laufzeitverhalten betroffen

## P0 — Silent wrong results

| # | Datei:Zeile | Befund | Quelle |
|---|---|---|---|
| 1 | `QueryExecutor.java:211` | `CONTAINS` macht Exact-Match statt Substring-Match | CodeRabbit |
| 2 | `QueryExecutor.java:76` | `Query.delete()` liefert Pre-Delete-Count statt tatsaechlich geloeschter Anzahl | CodeRabbit |
| 3 | `QueryExecutor.java:128` | Negierte Operatoren (`Not...`) auf aliasierten Feldern liefern falsche Ergebnisse | CodeRabbit |
| 4 | `MethodNameParser.java:115` | Gemischte `And`/`Or` in einem Methodennamen parsen zu einer falschen Query, ohne Fehler | CodeRabbit |
| 5 | `AbstractMorphiumRepository.java:310` | `update()` auf nicht existierender ID upserted (via `morphium.store()`) statt zu failen | Codex |
| 6 | `JdqlMethodBridge.java:202` | Cursor-Sort bei `@Query`+`CursoredPage` mit JDQL-eigenem `ORDER BY` wird aus leerer `@OrderBy`-Annotation gebaut -> falsche/wiederholte Seiten | Codex |
| 7 | `FindMethodBridge.java:193` | Dynamischer `Sort`/`Order`-Parameter bei `@Find`+`CursoredPage` wird von `CursorHelper.applySort` ueberschrieben -> Seiten wiederholen/ueberspringen sich | Codex |
| 8 | `AbstractMorphiumRepository.java:225` | Offset-Skew im Cursor-Modus bei zusaetzlichem `OFFSET` falsch angewendet | CodeRabbit |
| 9 | `JdqlMethodBridge.java:783` | AVG-Aggregatergebnis wird nicht als `double` zurueckgegeben | CodeRabbit |

## P1 — Funktionaler Bug, sichtbarer Fehlerpfad oder Spezifikations-Luecke

| # | Datei:Zeile | Befund | Quelle |
|---|---|---|---|
| 10 | `JdqlParser.java:62` | `ORDER BY` ohne `WHERE` (z.B. `@Query("ORDER BY name ASC")`) scheitert am Parser, obwohl Doku WHERE optional macht | Codex |
| 11 | `JdqlParser.java:245` | Guard gegen `HAVING` ohne `GROUP BY` ist unreachable — sollte rejecten, tut es nicht | CodeRabbit |
| 12 | `JdqlParser.java:528` | ORDER-BY-Richtungstoken (ASC/DESC) wird nicht validiert | CodeRabbit |
| 13 | `MethodNameParser.java:245` | `entityFields`-Parameter wird akzeptiert, aber nie als Autoritaet gegen echte Felder geprueft — falsche Methodennamen scheitern lautlos an anderer Stelle | CodeRabbit |
| 14 | `JdqlMethodBridge.java:364` | JDQL `LIKE` baut Regex ohne Escaping/Anchoring (inkonsistent zu `QueryExecutor.likeToRegex()`) — Regex-Injection-Risiko bei User-Input | Copilot + CodeRabbit (Duplikat) |
| 15 | `CursorHelper.java:141` | Cursor-Pagination erzwingt kein non-empty Sort-Keyset | CodeRabbit |
| 16 | `FindMethodBridge.java:128` | `PageRequest` + `Limit` kombiniert sollte abgelehnt werden, wird es nicht | CodeRabbit |
| 17 | `QueryMethodBridge.java:76-90` | `@OrderBy` (Jakarta Data 1.0: `descending`, `ignoreCase`, repeatable) wird nur teilweise unterstuetzt (nur `field[:ASC\|DESC]`) | CodeRabbit |
| 18 | `JdqlMethodBridge.java:244` | Richtungsabhaengige Cursor-Page-Flags (`isFirstPage`/`isLastPage`) fehlen | CodeRabbit |

## P2 — Edge-Case / Robustheit

| # | Datei:Zeile | Befund | Quelle |
|---|---|---|---|
| 19 | `MethodNameParser.java:257,268` | Combinator-Erkennung (`Or`/`And`) bricht bei Akronym-/Ziffer-Endung im Feldnamen (`findByURLOrStatus`) | Copilot |
| 20 | `JdqlParser.java:541` | Top-Level AND/OR-Split ignoriert String-Literale (`"name = 'A OR B'"` faelschlich gesplittet) | Copilot |
| 21 | `JdqlMethodBridge.java:698` | Sollte bei Klassenladen auf den Bridge-Classloader zurueckfallen | CodeRabbit |

## P3 — Doku / Codequalitaet (kein Laufzeitverhalten)

| # | Datei:Zeile | Befund | Quelle |
|---|---|---|---|
| 22 | `docs/jakarta-data.md:38` | Dokumentiert Framework-Integrationen, die noch nicht existieren, als verfuegbar | CodeRabbit |
| 23 | `docs/jakarta-data.md:66` | Instabile Morphium-Version in Codebeispielen | CodeRabbit |
| 24 | `morphium-jakarta-data/README.md:115` | `findAll()`-Beispiel sollte `Stream` zurueckgeben (Signatur-Praezision) | CodeRabbit |
| 25 | `MethodNameParser.java:127` | Ungenutzte `Pattern`-Variable (toter Code) | Copilot |
| 26 | `release.sh:142` | Parallele Arrays ohne Laengen-Check — bei Divergenz stille leere Strings | Copilot |
| 27 | diverse (15 CodeRabbit-Nitpicks) | Performance (In-Memory-Aggregation-Pagination), doppelte Regex-Compiles, fehlende Testfaelle | CodeRabbit |

## Empfehlung

- **P0 (9 Befunde) vor jedem Upstream-PR beheben** — das sind Faelle, in denen der Code
  ein falsches Ergebnis liefert, ohne dass der Aufrufer das merkt. Das ist der teuerste
  Fehlerklasse, weil sie in Produktion erst durch falsche Daten auffaellt.
- **P1 (9 Befunde) ebenfalls vor dem Upstream-PR** — betreffen alle Kernfunktionalitaet
  (Pagination, Sortierung, Query-Syntax), die im Modul-README/in der MkDocs-Seite als
  Feature beworben wird. Ein Feature, das laut Doku funktioniert, aber nicht funktioniert,
  ist schlechter als ein fehlendes Feature.
- **P2 (3 Befunde) vor dem PR, aber niedrigere Dringlichkeit** — echte Bugs, aber seltene
  Eingabepfade (Akronyme in Feldnamen, Anfuehrungszeichen in JDQL-Strings).
- **P3 (Rest) optional / kann im PR-Text als "known follow-up" benannt werden**, muss
  aber nicht vor dem PR behoben werden.
