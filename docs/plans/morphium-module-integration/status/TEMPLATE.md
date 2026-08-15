# Zustandsdokument zur Wiedervorlage — Vorlage

> Diese Datei ist die **Vorlage** für die Zustandsdokumente nach jeder Welle.
> Kopieren nach `status/<YYYY-MM-DD>-<welle>-<thema>.md`, alle
> `<Platzhalter>` ersetzen, jeden Abschnitt ausfüllen. Ein Abschnitt, der nicht
> zutrifft, wird mit „entfällt, weil …" begründet — nicht gelöscht.
>
> Zweck: Ein Orchestrator, der Tage später mit leerem Kontext startet, muss aus
> diesem Dokument den vollständigen Zustand rekonstruieren können, **ohne
> Rückfragen an den Auftraggeber**.

---

# Welle `<M?>` — `<Thema>`

| Feld | Wert |
|---|---|
| Welle | `<M?>` |
| Thema | `<Kurzbeschreibung>` |
| Erstellt am | `<YYYY-MM-DD>` |
| Erstellt von | `<Orchestrator-Session N>` |
| Ergebnis | ✅ abgeschlossen / ⏸️ abgebrochen / 🟡 teilweise |
| Nächste Welle | `<M?>` — Vorbedingung: `<…>` |

---

## 1. Was in dieser Welle erreicht wurde

`<Drei bis sechs Sätze in Prosa. Kein Aufzählungsstakkato. Ein Leser, der das
Vorhaben nicht kennt, soll verstehen, was jetzt anders ist als vorher.>`

---

## 2. Taskbilanz

| Task | Status | Agent-Modell | Laufzeit | Vom Orchestrator verifiziert am | Commits |
|---|---|---|---|---|---|
| `<M?-T1>` | ✅/❌ | sonnet | `<min>` | `<datum>` | `<sha kurz, sha kurz>` |

**Nacharbeiten, die nötig waren:** `<welcher Task, warum, was wurde nachgezogen>`

---

## 3. Konkrete Änderungen am Code

### Geänderte und neue Dateien

```
<Ausgabe von `git diff --stat <basis>..HEAD` je betroffenem Repository,
gekürzt auf die relevanten Zeilen. Bei neuen Modulen genügt die
Verzeichnisebene plus Dateizahl.>
```

### Repositories und Branches

| Repo | Branch | Basis | Commits | Gepusht? |
|---|---|---|---|---|
| `<repo>` | `<branch>` | `<basis-ref>` | `<n>` | ja/nein — `<warum>` |

### Was ausdrücklich **nicht** geändert wurde

`<Liste der Schutzzonen mit Belegbefehl, z. B.:
`git diff --stat origin/develop -- morphium-core poppydb` → leer>`

---

## 4. Verifikationsergebnisse

| Prüfung | Befehl | Ergebnis | Beleg |
|---|---|---|---|
| Reactor-Build | `mvn -B install -DskipTests` | PASS/FAIL | `<logdatei / kernaussage>` |
| Kern-Isolation | `mvn -q -pl morphium-core dependency:tree` | PASS/FAIL | `<…>` |
| Kern-only-Build | `mvn -B install -DskipTests -DskipExtensions` | PASS/FAIL | `<…>` |
| Volltestsuite | `mvn -B verify` | `<tests/failures/errors/skipped>` | `<logdatei>` |
| Javadoc | `mvn -B javadoc:javadoc` | `<warnungen>` | `<…>` |
| MkDocs | `mkdocs build --strict` | PASS/FAIL/n.v. | `<…>` |
| Commit-Hygiene | `git log <basis>..HEAD` | PASS/FAIL | `<…>` |

**Fehlgeschlagene Tests, die schon vorher fehlgeschlagen sind:**
`<Testnamen und Belegquelle, dass sie nicht durch diese Welle verursacht sind.
Wenn keine: „keine".>`

---

## 5. Invarianten der Optionalität (I1–I5)

| Invariante | Prüfung | Ergebnis |
|---|---|---|
| I1 keine Rückwärts-Dependency | `<befehl>` | PASS/FAIL |
| I2 keine Framework-Imports im Kern | `<befehl>` | PASS/FAIL |
| I3 Kern ohne Erweiterungen baubar | `<befehl>` | PASS/FAIL |
| I4 kein fremder BOM im Parent | `<befehl>` | PASS/FAIL |
| I5 Abhängigkeitsrichtung einseitig | `<befehl>` | PASS/FAIL |

`<Ab M6: stattdessen `./scripts/verify-core-isolation.sh --all` und dessen
Ausgabe.>`

---

## 6. Pull Requests dieser Welle

| PR | Repo | Ziel | Vorgelegt am | Freigegeben am | Nummer | Status |
|---|---|---|---|---|---|---|
| `<titel>` | `<repo>` | `<branch>` | `<datum>` | `<datum>` | `#<n>` | offen/gemergt/geschlossen |

**Review-Rückmeldungen und ihr Stand:**

| Kommentar | Von | Erledigt? | Wie |
|---|---|---|---|

`<Wenn kein PR Teil dieser Welle war: „entfällt — Vorbereitungswelle ohne
PR".>`

---

## 7. Paralyse-Ereignisse und Eingriffe

| Task | Agent | Symptom | Maßnahme (P3-Stufe) | Ergebnis |
|---|---|---|---|---|

`<Wenn keine: „keine — Heartbeats liefen alle 10 Minuten, Belege: <…>".
Ein pauschales „keine" ohne Belegangabe ist nicht ausreichend.>`

---

## 8. Erkenntnisse und Planabweichungen

| Erkenntnis | Warum sie zählt | Konsequenz | Plandokument angepasst? |
|---|---|---|---|

`<Das ist der wertvollste Abschnitt für spätere Sessions. Hier gehören auch
unbequeme Dinge hinein: falsche Annahmen im Plan, unterschätzter Aufwand,
Werkzeuge, die nicht funktionierten.>`

---

## 9. Offene Punkte, die in die nächste Welle übergehen

| Punkt | Warum offen | Wer/Wann | Blockierend? |
|---|---|---|---|

---

## 10. Vorbedingungen für die nächste Welle

```
[ ] <z. B. PR #<n> ist gemergt>
[ ] <z. B. origin/develop enthält Modul X>
[ ] <z. B. Docker läuft>
[ ] STATE.md aktualisiert
[ ] Gesamtplan README.md Statustabelle aktualisiert
[ ] JF-Dokument aktualisiert
```

---

## 11. Wiederaufnahme in einem Satz

`<Ein einziger Satz, der einem frischen Orchestrator sagt, was er als
Allererstes tun soll. Beispiel: „Prüfe, ob PR #214 gemergt ist; wenn ja, starte
M3-T1, wenn nein, warte und pflege Review-Rückmeldungen ein.">`
