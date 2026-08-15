# Welle M2 — PR: `morphium-jakarta-data` als Morphium-Modul

| Feld | Wert |
|---|---|
| Meilenstein | M2 |
| Status | ⬜ TODO |
| Abhängig von | M1 (vollständig verifiziert) |
| Arbeitsort | `morphium/`, Branch **`pr/jakarta-data-module`** von `origin/develop` |
| Ziel-Zustandsdokument | `status/<datum>-M2-jakarta-data-pr.md` |
| Agenten | 3 Sonnet-Agenten + Orchestrator-Eigenarbeit (PR-Text) |

---

## Ziel

Ein PR gegen `sboesebeck/morphium:develop`, der `morphium-jakarta-data` als
optionales Modul einbringt, inklusive Doku-Anbindung, CHANGELOG und
Central-Release-Fähigkeit.

---

## 🚦 Freigabepflicht

**Der PR wird nicht ohne vorherige Vorlage erstellt.** Verbindlicher Ablauf:

1. Der Orchestrator formuliert den vollständigen PR-Text (Titel + Body).
2. Er zeigt ihn dem Auftraggeber im Chat.
3. Er wartet auf **explizite Freigabe**.
4. Erst danach `git push fork pr/jakarta-data-module` und `gh pr create`.

Sub-Agenten dürfen weder pushen noch PRs erstellen. Der Orchestrator führt
`push` und `gh pr create` selbst aus, ausschließlich nach Freigabe.

---

## Vorbedingungen

```bash
cd morphium
git status --short                      # nur die bekannten untracked Dateien
git fetch origin
git checkout -b pr/jakarta-data-module origin/develop
```

Und: Der M1-Trockenlauf-Bericht `reports/M1-T5-dryrun.md` muss vorliegen und
alle Invarianten auf PASS haben. Er enthält den Patch für `morphium/pom.xml`.

---

## Task-Übersicht

| ID | Task | Modell | Status |
|---|---|---|---|
| M2-T1 | Modul ins Repo übernehmen, Reactor + Parent anpassen | sonnet | ⬜ |
| M2-T2 | Doku-Anbindung (MkDocs-Nav, index.md, CHANGELOG) | sonnet | ⬜ |
| M2-T3 | `release.sh` um das Modul erweitern | sonnet | ⬜ |
| M2-T4 | Vollverifikation vor PR | sonnet | ⬜ |
| M2-T5 | PR-Text formulieren, vorlegen, nach Freigabe erstellen | **Orchestrator** | ⬜ |

---

## M2-T1 — Modul übernehmen

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium.
Prüfe mit `git branch --show-current`, dass du auf `pr/jakarta-data-module` bist.
Wenn nicht: abbrechen und melden.

LIES ZUERST:
- /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium-jakarta-data/docs/plans/morphium-module-integration/reports/M1-T5-dryrun.md
  (enthält den erprobten Patch für pom.xml und die Kopierliste)
- /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium-jakarta-data/docs/plans/morphium-module-integration/decisions/D3-reactor-strategie.md
- /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium-jakarta-data/MIGRATION-NOTES.md
  (welche Dateien mitkommen, welche entfallen)

AUFGABE 1 — Modulverzeichnis anlegen:
Kopiere aus /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium-jakarta-data
(Branch move-to-morphium) nach ./morphium-jakarta-data/ genau die Dateien, die
MIGRATION-NOTES.md als "kommt mit" ausweist — in der Regel: pom.xml, src/,
README.md, CHANGELOG.md.
NICHT kopieren: .git, target, .github, LICENSE, CODE_OF_CONDUCT.md, SECURITY.md,
CONTRIBUTING.md, MIGRATION-NOTES.md, docs-for-morphium (letzteres verarbeitet
M2-T2).

AUFGABE 2 — ./pom.xml (morphium-parent) anpassen:
Wende den Patch aus dem Trockenlauf-Bericht an:
a) <module>morphium-jakarta-data</module> im Profil `extensions` (Aktivierung
   über <property><name>!skipExtensions</name></property>), wie in D3
   beschrieben.
b) In <properties>: <jakarta.data.version>1.0.0</jakarta.data.version>
c) In <dependencyManagement>: jakarta.data:jakarta.data-api mit
   ${jakarta.data.version}
d) Setze über das <profiles>-Element und über <modules> je einen erklärenden
   Kommentarblock, der festhält:
   - dass die Erweiterungsmodule optional sind
   - dass die Abhängigkeitsrichtung strikt Modul → Kern ist und der Kern nie von
     einem Modul abhängen darf
   - dass -DskipExtensions einen Kern-only-Build erzeugt
   - dass Framework-BOMs (Quarkus, Spring) NICHT in dieses Parent-POM gehören,
     sondern in die jeweiligen Modul-POMs
   Formuliere knapp und sachlich, Englisch, im Stil der bestehenden Kommentare
   in dieser Datei.
e) Passe das Modul-POM morphium-jakarta-data/pom.xml so an, dass es die neue
   Parent-Property nutzt, und entferne die dortigen "M2:"-Kommentare, sobald
   der jeweilige Punkt erledigt ist.

ABSOLUT UNVERÄNDERT BLEIBEN: morphium-core/** und poppydb/**. Kein Zeichen.
Wenn du glaubst, dort etwas ändern zu müssen: NICHT tun, melden, abbrechen.

VERIFIKATION, die du selbst ausführen musst — alle vier Befehle, jedes Ergebnis
protokollieren:
  git diff --stat morphium-core poppydb        # MUSS leer sein
  mvn -B install -DskipTests
  mvn -B verify -pl morphium-jakarta-data
  mvn -B install -DskipTests -DskipExtensions  # und prüfen, dass das Modul
                                               # NICHT in der Reactor-Summary steht
  mvn -q -pl morphium-core dependency:tree | grep -E 'jakarta.data|quarkus|springframework'
                                               # MUSS ohne Treffer bleiben
Der Volltest-Lauf (`mvn verify` ohne -pl) dauert ~90 Minuten — führe ihn in
diesem Task NICHT aus, das macht M2-T4.

Committe in zwei Commits:
  "feat: add morphium-jakarta-data as optional module"
  "build: register morphium-jakarta-data in extensions profile"

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Änderungen an `morphium-core/**` und `poppydb/**`.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.

BERICHTE am Ende: `git diff --stat` gegen origin/develop, den Diff von ./pom.xml
im Volltext, und die Ergebnisse aller Verifikationsbefehle.
````

**Verifikation (Orchestrator):**
```bash
cd morphium && git diff --stat origin/develop -- morphium-core poppydb   # leer
mvn -q -pl morphium-core dependency:tree 2>/dev/null | grep -cE 'jakarta.data|quarkus|springframework'  # 0
```

---

## M2-T2 — Doku-Anbindung

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium
auf dem Branch `pr/jakarta-data-module`.

AUFGABE 1:
Kopiere /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium-jakarta-data/docs-for-morphium/jakarta-data.md
(Branch move-to-morphium) nach ./docs/jakarta-data.md. Prüfe die Datei danach
gegen die MkDocs-Konfiguration: Es dürfen nur Markdown-Extensions verwendet
werden, die ./mkdocs.yml aktiviert. Interne Links müssen auf existierende Dateien
unter ./docs/ zeigen — korrigiere sie, wo nötig.

AUFGABE 2 — ./mkdocs.yml:
Ergänze den Navigationseintrag. Platzierung: Erzeuge einen neuen Abschnitt
"Extensions" NACH "Core Features" und VOR "Reference", mit dem Eintrag
"Jakarta Data: jakarta-data.md". Dieser Abschnitt nimmt in späteren Wellen die
Quarkus- und Spring-Boot-Seiten auf; setze dort einen HTML-Kommentar als
Platzhalter-Hinweis. Ändere sonst nichts an mkdocs.yml.

AUFGABE 3 — ./docs/index.md:
Ergänze einen Abschnitt für die Erweiterungsmodule mit einem Verweis auf
jakarta-data.md. Halte dich an den Stil und die Abschnittsstruktur der Datei
(sieh dir an, wie PoppyDB dort eingebunden ist). Mach deutlich, dass es sich um
optionale Module handelt.

AUFGABE 4 — ./CHANGELOG.md:
Neuer Eintrag ganz oben, im etablierten Format der Datei (lies mindestens zwei
bestehende Einträge, um Ton und Struktur zu übernehmen: "### Added" →
"#### <Thema>" → Prosaabsatz, keine Bullet-Listen für die Erklärung).
Inhalt: `morphium-jakarta-data` als neues optionales Modul. Nenne: was es
liefert (Jakarta Data 1.0 Runtime: Query-Derivation, JDQL, Pagination,
Sortierung), dass der Kern nicht davon abhängt und Nutzer von
`de.caluga:morphium` kein jakarta.data-api erhalten, dass `-DskipExtensions`
einen Kern-only-Build erzeugt, und dass die Framework-Integrationen (Quarkus,
Spring Boot) darauf aufsetzen und in Folge-PRs kommen. Erwähne, dass der Code
aus Bardioc1977/morphium-jakarta-data stammt und dort archiviert wird.
Ordne den Eintrag der kommenden Version zu — prüfe dazu die aktuelle Version in
./pom.xml (6.2.6-SNAPSHOT) und richte dich danach, wie unveröffentlichte
Einträge in dieser Datei bisher behandelt wurden.

AUFGABE 5 — ./README.md:
Prüfe, ob die Haupt-README eine Modulübersicht enthält. Wenn ja: ergänzen. Wenn
nein: NICHT eine neue Struktur erfinden — melde das im Bericht und lass die
README unverändert. Gleiches gilt für README.de.md; wenn du sie änderst, dann
inhaltlich identisch und auf Deutsch mit korrekten Umlauten.

VERIFIKATION, die du selbst ausführen musst:
  python3 -c "import yaml,sys; yaml.safe_load(open('mkdocs.yml'))"   # YAML valide
  command -v mkdocs >/dev/null && mkdocs build --strict || echo "mkdocs nicht installiert - übersprungen"
Prüfe manuell jeden von dir gesetzten oder kopierten relativen Link darauf, dass
die Zieldatei existiert. Liste im Bericht alle geprüften Links mit OK/FEHLT.

Committe in zwei Commits:
  "docs: add jakarta data module documentation"
  "docs: add changelog entry for morphium-jakarta-data module"

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Änderungen an `morphium-core/**`, `poppydb/**`, `./pom.xml`,
  `morphium-jakarta-data/**` (das hat M2-T1 erledigt).
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.

BERICHTE am Ende: den mkdocs.yml-Diff, den CHANGELOG-Eintrag im Volltext und die
Link-Prüftabelle.
````

---

## M2-T3 — `release.sh` erweitern

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium
auf dem Branch `pr/jakarta-data-module`.

KONTEXT: ./release.sh (rund 1000 Zeilen Bash) baut das Sonatype-Central-Bundle.
Es kennt heute nur die Module morphium-core und poppydb, jeweils hartcodiert.
Das neue Modul morphium-jakarta-data muss aufgenommen werden.
Lies zuerst /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium-jakarta-data/docs/plans/morphium-module-integration/decisions/D4-build-release-workflow.md,
Abschnitt "Teilfrage 1".

AUFGABE: Lies release.sh VOLLSTÄNDIG, bevor du etwas änderst. Identifiziere alle
Stellen mit Modulbezug — bekannte Kandidaten (Zeilennummern können abweichen):
- die Multi-Modul-Strukturprüfung (Schleife über `morphium-core poppydb`, ~Zeile 630)
- den Bundle-Aufbau je Modul (~ab Zeile 842)
- die Artefakt-Verifikationsschleife (~Zeile 886)
- Log-Ausgaben, die Modulnamen auflisten (~Zeile 636, 463)
Suche systematisch nach `poppydb`, `morphium-core` und `morphium-parent` im
gesamten Skript und bewerte JEDEN Treffer.

ÄNDERUNGEN:
1. Strukturprüfung um morphium-jakarta-data erweitern.
2. Einen Bundle-Block für morphium-jakarta-data ergänzen, exakt analog zum
   poppydb-Block: .pom, .jar, -sources.jar, -javadoc.jar kopieren, signieren
   (sign_file), Checksums (checksum_file).
3. Verifikationsschleife um das neue Artefakt erweitern.
4. Log-Ausgaben aktualisieren.

BEVORZUGTE UMSETZUNG: Wenn die Blöcke sich strukturell gleichen, ziehe eine
Bash-Funktion oder eine Modul-Array-Schleife vor, statt einen dritten
Copy-Paste-Block anzulegen — bei M4 und M5 kommen weitere sechs Artefakte hinzu,
und Copy-Paste skaliert hier nicht. Halte dich dabei an den Stil des Skripts
(bestehende Funktionen wie sign_file, checksum_file, log_info, log_success
zeigen die Konvention). Wenn eine Umstellung auf eine Schleife das Risiko
erhöht, weil sich Blöcke doch unterscheiden: erst analysieren, dann im Bericht
begründen, welchen Weg du gewählt hast.

WICHTIG — Ausschluss: Testmodule gehören nicht nach Maven Central. Für dieses
Modul gibt es kein Testmodul, aber baue die Erweiterung so, dass in M4 das
Verzeichnis quarkus-morphium/integration-tests leicht ausgeschlossen werden kann.

VERIFIKATION, die du selbst ausführen musst:
  bash -n release.sh                       # Syntaxprüfung, MUSS fehlerfrei sein
  shellcheck release.sh                    # falls installiert; neue Findings zu
                                           # DEINEN Zeilen beheben, bestehende
                                           # Findings NICHT anfassen
Führe release.sh NICHT aus — es würde taggen, signieren und zu Sonatype
hochladen. Prüfe stattdessen die Logik durch Lesen und dokumentiere im Bericht
für jedes der drei Artefakte, welche Dateien das Bundle nach deiner Änderung
enthalten wird.

Committe als "build: include morphium-jakarta-data in release bundle".

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- AUSFÜHREN von release.sh oder von `mvn release:prepare` / `release:perform`.
- `git tag` in jeder Form.
- Änderungen an `morphium-core/**` und `poppydb/**`.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.

BERICHTE am Ende: den vollständigen Diff von release.sh, die Liste aller
modulbezogenen Trefferstellen mit deiner Bewertung, und die erwartete
Bundle-Dateiliste je Artefakt.
````

**Verifikation (Orchestrator):** `bash -n release.sh` und den Diff selbst lesen.
`release.sh` ist ein produktives Release-Werkzeug im fremden Projekt — ein Fehler
hier fällt erst beim nächsten Release auf. Der Orchestrator liest den Diff
vollständig, nicht überfliegend.

---

## M2-T4 — Vollverifikation vor PR

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium
auf dem Branch `pr/jakarta-data-module`.

AUFGABE: Abschließende Verifikation vor der PR-Vorlage. Führe alle Prüfungen aus
und protokolliere jedes Ergebnis als PASS oder FAIL mit Belegausgabe.

A — Isolation des Kerns (die zentrale Zusage dieses PRs):
  git diff --stat origin/develop -- morphium-core poppydb    # MUSS leer sein
  grep -nE 'jakarta-data|quarkus|spring' morphium-core/pom.xml   # keine Treffer
  mvn -q -pl morphium-core dependency:tree > /tmp/m2-core-tree.txt
  grep -E 'jakarta.data|io.quarkus|org.springframework' /tmp/m2-core-tree.txt
                                                             # keine Treffer
  mvn -B install -DskipTests -DskipExtensions
  # und belegen, dass morphium-jakarta-data NICHT in der Reactor-Summary steht

B — Vollbau und Modultests:
  mvn -B install -DskipTests
  mvn -B verify -pl morphium-jakarta-data
  ls morphium-jakarta-data/target/*.jar   # jar, sources.jar, javadoc.jar

C — Volltestsuite des Kerns (Regressionsnachweis):
  Der Lauf dauert etwa 90 MINUTEN. Starte ihn mit Ausgabe in eine Datei:
    mvn -B verify > /tmp/m2-full-verify.log 2>&1
  Pipe die Ausgabe NICHT durch tail oder grep — ein Hook im Workspace
  unterbindet das. Lies die Logdatei nach Abschluss mit dem Read-Tool aus.
  Werte aus: Anzahl Tests, Failures, Errors, Skipped je Modul, sowie das
  Reactor-Summary.
  Wenn Tests fehlschlagen: Prüfe zuerst, ob sie auch auf origin/develop ohne
  deine Änderungen fehlschlagen (bekannte Flakies gibt es, z.B. im Bereich
  Messaging und ChangeStream). Belege das durch Nennung der Testnamen. NICHT
  versuchen, fremde Tests zu reparieren.

D — Javadoc-Erzeugbarkeit für Central:
  mvn -B -pl morphium-jakarta-data javadoc:jar source:jar

E — Commit-Hygiene:
  git log origin/develop..HEAD --format='%H%n%an <%ae>%n%s%n%b%n---'
  Prüfe: keine Co-Authored-By-Zeile, kein "Generated with", keine
  E-Mail-Adressen im Body, Conventional-Commits-Stil, jeder Commit hat einen
  klaren eigenen Zweck. Wenn ein Commit gegen diese Regeln verstößt: NICHT
  rebasen oder amenden — im Bericht melden, der Orchestrator entscheidet.

F — Diff-Umfang:
  git diff --stat origin/develop
  Erwartung: neue Dateien unter morphium-jakarta-data/, Änderungen an ./pom.xml,
  ./mkdocs.yml, ./docs/index.md, ./docs/jakarta-data.md, ./CHANGELOG.md,
  ./release.sh. NICHTS ANDERES. Jede weitere geänderte Datei ist ein Befund.

Schreibe den Bericht nach
/Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium-jakarta-data/docs/plans/morphium-module-integration/reports/M2-T4-verification.md

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Reparaturen an fremdem Code, um Tests grün zu bekommen.
- `git rebase`, `git commit --amend`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.

BERICHTE am Ende: die vollständige PASS/FAIL-Tabelle A–F und alle Befunde.
````

**Paralyse-Hinweis für den Orchestrator:** Dieser Task enthält einen
90-Minuten-Build. Regel P2 (10-Minuten-Heartbeat) würde hier fälschlich Paralyse
melden. Sonderregel: Solange `/tmp/m2-full-verify.log` wächst
(`wc -c` vergleichen), gilt der Agent als arbeitend. Wächst die Datei
30 Minuten nicht, ist der Build hängig → Eskalation nach P3.

---

## M2-T5 — PR-Text und Erstellung (Orchestrator)

**Diesen Task führt der Orchestrator selbst aus, kein Sub-Agent.**

### Schritt 1: PR-Text formulieren

Aufbau (bewährt über 59 gemergte PRs):

```markdown
Hi Stephan,

<1–2 Absätze: Was und Warum. Der Kontext ist hier zentral — dass die drei
Erweiterungsprojekte bisher unter Bardioc1977 lagen und nun als optionale Module
ins Hauptprojekt kommen sollen, damit sie über Maven Central verfügbar werden
und die Versionsverflechtung endet.>

## Was dieser PR macht
<Aufzählung: neues Modul, Reactor-Einbindung über Profil, Doku, CHANGELOG,
release.sh>

## Optionalität — die zentrale Zusage
<Explizit: morphium-core ist unverändert (git diff belegt es), der
Kern-Dependency-Tree enthält kein jakarta.data-api, `-DskipExtensions` baut nur
Kern und PoppyDB. Mit den konkreten Verifikationsbefehlen, damit du es selbst
nachvollziehen kannst.>

## Versionierung
<Lockstep wie bei poppydb, mit kurzer Begründung. Hinweis, dass wir bei anderer
Präferenz auf eine eigene Versionslinie umstellen können — Aufwand ist ein
<version>-Element.>

## Was noch kommt
<quarkus-morphium und spring-boot-morphium als separate PRs. Ehrlich benennen,
damit du den Gesamtumfang kennst.>

## Verifikation
<Die ausgeführten Befehle mit Ergebnissen aus dem M2-T4-Bericht.>

## Offene Fragen an dich
<Konkrete Entscheidungspunkte: groupId-Präferenz, Reactor-Strategie,
Versionierung, ob ein CI-Workflow willkommen ist.>

Cheers!
```

Regeln: Kein `Co-Authored-By`, kein „Generated with Claude Code". Freundliche
Anrede und Abschluss stehen **im PR-Body auf GitHub**, nicht nur im Chat.

### Schritt 2: Vorlage und Freigabe

Den Text vollständig im Chat zeigen. Auf explizite Freigabe warten. Bei
Änderungswünschen überarbeiten und erneut vorlegen.

### Schritt 3: Erstellen (erst nach Freigabe)

```bash
cd morphium
git push fork pr/jakarta-data-module
gh pr create --repo sboesebeck/morphium \
  --base develop \
  --head Bardioc1977:pr/jakarta-data-module \
  --title "feat: add morphium-jakarta-data as optional module" \
  --body-file /tmp/pr-jakarta-data-body.md
```

### Schritt 4: Nachverfolgung

PR-Nummer im Zustandsdokument und in `STATE.md` festhalten. Review-Kommentare
abwarten. **M3 startet erst, wenn dieser PR gemergt ist** — sonst müsste die
Quarkus-Extension gegen ein Modul gebaut werden, dessen endgültige Koordinaten
noch offen sind.

Bei Review-Feedback: Nacharbeit als neue Commits auf demselben Branch, Push nach
Freigabe. Kein force-push, kein Rebase (die Historie des PR muss für den
Reviewer stabil bleiben).

---

## Abschluss der Welle M2

1. Zustandsdokument `status/<datum>-M2-jakarta-data-pr.md` schreiben, mit
   PR-Nummer, PR-Status und allen Review-Punkten.
2. `STATE.md` aktualisieren.
3. Gesamtplan Abschnitt 5 aktualisieren.
4. JF-Dokument aktualisieren — M2 ist der erste extern sichtbare Meilenstein und
   damit für das JF relevant.
5. **Wartezustand dokumentieren.** Wenn der PR offen ist, ist die Welle
   „⏸️ BLOCKIERT — warte auf Review". Der Orchestrator legt in `STATE.md` fest,
   was in der Zwischenzeit vorgezogen werden kann: die reinen
   Vorbereitungstasks von M3 (Extension-Guideline-Audit) und M5 hängen nicht am
   Merge und können parallel laufen.

## Definition of Done

- [ ] `morphium-jakarta-data/` als Modul im Repo, Build grün
- [ ] `morphium-core/**` und `poppydb/**` bitgenau unverändert
- [ ] Alle fünf Invarianten aus D3 belegt
- [ ] `-DskipExtensions` baut Kern ohne das Modul
- [ ] MkDocs-Seite und Navigation, `docs/index.md` verlinkt
- [ ] CHANGELOG-Eintrag im Projektformat
- [ ] `release.sh` erweitert, `bash -n` fehlerfrei
- [ ] Volltestsuite gelaufen, Ergebnis dokumentiert, Abweichungen als bekannte
      Flakies belegt oder als Befund gemeldet
- [ ] Commit-Hygiene geprüft
- [ ] **PR-Text vorgelegt und freigegeben**
- [ ] PR erstellt, Nummer dokumentiert
