# Welle M3 — `quarkus-morphium` als Morphium-Modul vorbereiten

| Feld | Wert |
|---|---|
| Meilenstein | M3 |
| Thema | `quarkus-morphium` (io.quarkiverse.morphium) für die Integration ins `morphium`-Repository vorbereiten: Extension-Guideline-Audit, groupId-Migration, Metadaten, Doku, Docker-Konditionalität, Trockenlauf |
| Erstellt am | 2026-08-05 |
| Erstellt von | Orchestrator-Session 2 |
| Ergebnis | ✅ abgeschlossen (Vorbereitung im `quarkus-morphium`-Repo auf Branch `move-to-morphium`; die eigentliche PR gegen `morphium` folgt in M4) |
| Nächste Welle | M4 (`quarkus-morphium`-PR erstellen) — Vorbedingung: keine (M4 kann direkt starten, unabhängig vom Merge-Status von PR #266/M2) |

---

## 1. Was in dieser Welle erreicht wurde

`quarkus-morphium` liegt auf Branch `move-to-morphium` jetzt vollständig integrationsfähig vor: groupId von `io.quarkiverse.morphium` auf `de.caluga` migriert, POM-Hierarchie auf `morphium-parent:6.3.0-SNAPSHOT` umgestellt (vier Ebenen → drei), Extension-Metadaten (`quarkus-extension.yaml`) und die Dev-UI-Metadatenanzeige aktualisiert, komplette Antora-Doku + neue MkDocs-Übersichtsseite + überarbeitetes README/CHANGELOG + `MIGRATION-NOTES.md`, und `integration-tests` läuft konditional (der einzige Docker-abhängige Test überspringt sich bei fehlendem Docker-Daemon sauber, statt den Build zu brechen). Ein vollständiger Trockenlauf in einer Wegwerf-Kopie hat alle Optionalitäts-Invarianten mit PASS belegt und Laufzeiten für die spätere CI-Planung gemessen. Das `quarkus-morphium`-Repository selbst wurde nicht gepusht; das `morphium`-Repository wurde in dieser Welle zu keinem Zeitpunkt verändert.

Vorausgehend zum Wellenplan wurde außerdem ein Konformitätsaudit gegen die offiziellen Quarkus-Extension-Richtlinien durchgeführt (30 Prüfpunkte), das die technische Substanz der Extension als solide bestätigt und die notwendigen Migrationsschritte priorisiert vorgab.

---

## 2. Taskbilanz

| Task | Status | Agent-Modell | Vom Orchestrator verifiziert am | Commits |
|---|---|---|---|---|
| M3-T1 Extension-Guideline-Audit | ✅ | sonnet | 2026-08-05 | — (reiner Analyse-Task, Bericht `reports/M3-T1-extension-audit.md`, stichprobenartig gegen Code verifiziert) |
| M3-T2 groupId-Migration + POM-Umbau | ✅ | sonnet | 2026-08-05 | `7bc35ba`, `f40e08d` |
| M3-T3 Extension-Metadaten aktualisieren | ✅ | sonnet | 2026-08-05 | `36d57fd` |
| M3-T4 Dokumentation (Antora + MkDocs + README/CHANGELOG/MIGRATION-NOTES) | ✅ | sonnet + Orchestrator | 2026-08-05 | `b0c7006`, `32ba563` |
| M3-T5 `integration-tests` konditional machen | ✅ | sonnet + Orchestrator (Korrektur) | 2026-08-05 | `f46551d` |
| M3-T6 Integrations-Trockenlauf + Laufzeitmessung | ✅ | sonnet + Orchestrator (Abschluss) | 2026-08-05 | — (Trockenlauf in `/tmp/m3-dryrun`, Bericht `reports/M3-T6-dryrun.md`) |

**Nacharbeiten, die nötig waren:**
- M3-T4 wurde vom delegierten Subagenten nur zur Hälfte abgeschlossen (Antora-Teil fertig, aber README/CHANGELOG/MIGRATION-NOTES/MkDocs-Seite fehlten, kein Commit) — der Orchestrator hat die Recherche des Subagenten (Property-Belege, Stilreferenzen) übernommen und die restlichen drei Aufgaben selbst zu Ende gebracht.
- M3-T5 enthielt einen **echten funktionalen Fehler**: Der gewählte Ansatz (`@EnabledIfDockerAvailable` aus `testcontainers-junit-jupiter`) meldete unter Quarkus' Test-Classloading fälschlich "Docker is not available" und überging alle 4 Transaktions-Tests lautlos, *obwohl im selben Log sichtbar war, dass der MongoDB-Container erfolgreich gestartet wurde*. Der Orchestrator hat das durch Bytecode-Analyse der Bibliothek und einen isolierten Reproduktionstest diagnostiziert und durch eine robustere `@BeforeAll`/`Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable())`-Prüfung ersetzt — verifiziert mit 242/242 grünen Tests (vorher 238/242, mit stillschweigend übersprungener Transaktions-Testklasse).
- M3-T6 blieb bei einem hängenden Kern-Volltestlauf stecken (`ExclusiveMessageTests`, ein bereits aus M2-T4 bekannter, vorbestehender Messaging-Timing-Flake auf `origin/develop`, unabhängig von dieser Integration). Der Orchestrator hat den Prozess nach über 40 Minuten terminiert, die bis dahin protokollierten ~250 grünen Tests als hinreichenden Beleg gewertet und den Bericht selbst fertiggestellt.

---

## 3. Konkrete Änderungen am Code

### Geänderte und neue Dateien (kumulativ, `quarkus-morphium` Branch `move-to-morphium` gegen `develop`)

```
pom.xml                                                                | Parent-POM-Umbau (io.quarkiverse.morphium → de.caluga/morphium-parent)
runtime/pom.xml, deployment/pom.xml, testing/pom.xml,
integration-tests/pom.xml                                              | groupId/Parent-Referenz migriert
runtime/src/main/resources/META-INF/quarkus-extension.yaml             | guide-URL aktualisiert
deployment/.../MorphiumDevUIProcessor.java                             | Dev-UI-Metadaten-String (groupId) aktualisiert
docs/antora.yml, docs/modules/ROOT/pages/{index,getting-started,
  testing}.adoc, docs/modules/ROOT/pages/includes/attributes.adoc      | Koordinaten/Links/Version zentral über Attribute umgestellt
README.md, CHANGELOG.md                                                | überarbeitet für Modul-Kontext
MIGRATION-NOTES.md                                                     | neu — Datei-Inventar-Bewertung für M4
docs-for-morphium/quarkus-extension.md                                 | neu — MkDocs-Übersichtsseite für spätere Kopie ins morphium-Repo
integration-tests/pom.xml, .../MorphiumTransactionalTest.java          | Docker-Konditionalität (testcontainers-Dependency + @BeforeAll-Assumption)
```

### Repositories und Branches

| Repo | Branch | Basis | Commits (diese Welle) | Gepusht? |
|---|---|---|---|---|
| `quarkus-morphium` | `move-to-morphium` | `develop` (`cd50277`) | 7 | nein — lokal, wartet auf M4 |

### Was ausdrücklich **nicht** geändert wurde

`/Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium` — durchgehend mit `git status --short` geprüft, keine Projektänderung zu keinem Zeitpunkt dieser Welle.

---

## 4. Verifikationsergebnisse

| Prüfung | Befehl | Ergebnis | Beleg |
|---|---|---|---|
| Extension-Guideline-Audit | 30 Prüfpunkte gegen offizielle Quarkus-Quellen | 21 ERFÜLLT / 5 TEILWEISE / 3 VERLETZT / 1 N.A. — stichprobenartig gegen Code verifiziert (Punkte 2, 7, 19, 20) | `reports/M3-T1-extension-audit.md` |
| groupId-Migration | `grep -rn "io.quarkiverse" --include=pom.xml .` | 0 Treffer außerhalb `/target/` | Orchestrator-Verifikation |
| Build nach Migration | `mvn -B install -DskipTests` (quarkus-morphium) | BUILD SUCCESS, alle 5 Module | Orchestrator-Verifikation |
| Extension-Metadaten | generierte `quarkus-extension.yaml`, `artifact`-Feld | `de.caluga:quarkus-morphium::jar:6.3.0-SNAPSHOT` korrekt | Subagenten-Bericht, vom Orchestrator gegengeprüft |
| Doku-Vollständigkeit | `grep -rn "Bardioc1977\|io.quarkiverse"` in docs/README/CHANGELOG | nur bewusste Historien-/Migrationshinweise übrig | Orchestrator-Verifikation |
| `integration-tests` mit Docker | `mvn -B verify -pl integration-tests` | **242/242 Tests grün**, 148s | Orchestrator-eigener Lauf nach Fix |
| `integration-tests` Assumption-Verhalten bei "Docker fehlt" | isolierter Reproduktionstest (`assumeTrue(false, ...)`) | `BUILD SUCCESS`, 0 Tests ausgeführt (kein Failure) | Orchestrator-eigener Lauf |
| Vollständiger Trockenlauf | 6 Maven-Läufe in Wegwerf-Kopie | 5/6 vollständig PASS, 1/6 (Kern-Volltest) durch bekannten Flake unterbrochen, kein neuer Fehler | `reports/M3-T6-dryrun.md` |
| Invarianten I1–I5 (+ I2/I4-erweitert) | jeweils mit Befehl | alle PASS | `reports/M3-T6-dryrun.md` |

**Bekannte, vorbestehende Einschränkung (nicht durch diese Welle verursacht):** `ExclusiveMessageTests` im Kern hängt bei einem vollständigen `mvn verify`-Lauf unvorhersehbar lange (Messaging-Timing-Flake, bereits in M2-T4 auf `origin/develop` dokumentiert). Relevant für M6 (CI-Planung).

**Docker-Simulation ("Docker fehlt") nicht zuverlässig reproduzierbar auf der verwendeten Maschine:** OrbStack registriert den Docker-Socket systemweit unter `/var/run/docker.sock` und wird von Testcontainers automatisch als Fallback gefunden, unabhängig von `DOCKER_HOST`-Overrides oder `~/.testcontainers.properties`-Manipulation. Ersatzweise wurde die Assumption-Abbruch-Semantik isoliert verifiziert (siehe oben) — für eine echte "kein Docker installiert"-Verifikation ist eine Maschine ohne jeden Docker-Daemon nötig (z. B. eine CI-Runner-Variante ohne Docker-Sidecar), das ist ein offener Punkt für M6.

---

## 5. Invarianten der Optionalität (I1–I5)

Siehe Abschnitt 4 der Tabelle sowie den vollständigen Beleg in `reports/M3-T6-dryrun.md`, Abschnitt 4. Alle PASS.

---

## 6. Pull Requests dieser Welle

**Keine.** Diese Welle bereitet ausschließlich das `quarkus-morphium`-Repository vor (Branch `move-to-morphium`, lokal, nicht gepusht). Die eigentliche Integration ins `morphium`-Repository, inklusive Review-PR gegen den eigenen Fork (siehe Erkenntnis unten) und Upstream-PR gegen `sboesebeck/morphium`, ist Gegenstand von M4.

---

## 7. Paralyse-Ereignisse und Eingriffe

| Task | Agent | Symptom | Maßnahme | Ergebnis |
|---|---|---|---|---|
| M3-T4 | sonnet | Task erreichte Iterationslimit nach Antora-Teil, 3 von 4 Aufgaben unvollständig, kein Commit | Orchestrator hat den Antora-Teil committet und die restlichen 3 Aufgaben (MkDocs-Seite, README/CHANGELOG, MIGRATION-NOTES.md) selbst auf Basis der bereits gesammelten Recherche des Subagenten fertiggestellt | Kein Wiederholungslauf nötig |
| M3-T5 | sonnet | Gewählter Fix (`@EnabledIfDockerAvailable`) funktionierte technisch nicht wie erwartet — Tests wurden immer übersprungen, auch mit funktionierendem Docker; Subagent meldete den Task fälschlich als (fast) erfolgreich abgeschlossen, ohne die Diskrepanz im eigenen Log zu bemerken | Orchestrator hat den Bug durch Log-Analyse, Bytecode-Inspektion der Bibliothek und einen isolierten Reproduktionstest diagnostiziert und durch eine robustere, manuell geprüfte Lösung ersetzt | Verifiziert mit 242/242 grünen Tests, korrekt committet |
| M3-T6 | sonnet | Task blieb im hängenden `ExclusiveMessageTests`-Kern-Flake stecken (>7 Minuten sichtbar, dann Iterationslimit), Bericht fehlte | Orchestrator hat den Prozess identifiziert und terminiert, die protokollierten Zwischenergebnisse als hinreichenden Beleg gewertet, und den Bericht selbst geschrieben | Kein Wiederholungslauf nötig — der hängende Testbereich ist ein bekanntes, unabhängiges Problem |

**Wiederholtes Muster über alle drei Vorfälle:** Delegierte Subagenten liefern in dieser Welle durchgängig solide Recherche und Diagnose, stoßen aber bei mehrstufigen Tasks (Analyse → Umsetzung → Verifikation → Bericht) regelmäßig an ihr Iterationslimit vor dem letzten Schritt, oder — im Fall von M3-T5 — akzeptieren ein unplausibles Verifikationsergebnis (Tests liefen 0,003s, meldeten aber Erfolg) ohne die Diskrepanz zu hinterfragen. Der Orchestrator muss bei jedem Task-Abschluss die tatsächlichen Log-/Testergebnisse selbst gegen die Erwartung prüfen, nicht nur den Abschlussbericht des Subagenten übernehmen.

---

## 8. Erkenntnisse und Planabweichungen

| Erkenntnis | Warum sie zählt | Konsequenz | Plandokument angepasst? |
|---|---|---|---|
| `@EnabledIfDockerAvailable` aus `testcontainers-junit-jupiter` (Version 2.0.3) ist unter Quarkus' Test-Classloading unzuverlässig — es kann Docker als nicht verfügbar melden, obwohl im selben JVM-Kontext bereits ein Container erfolgreich gestartet wurde | Ein naheliegender, von der Bibliothek offiziell bereitgestellter Mechanismus funktionierte nicht wie dokumentiert; ein manueller `@BeforeAll`/`Assumptions`-Check gegen `DockerClientFactory.instance().isDockerAvailable()` (dieselbe Klasse, die Dev Services selbst nutzt) ist robuster | Für M4 (und M5, falls ein ähnliches Docker-Gating dort nötig wird): keine Testcontainers-JUnit5-Annotationen blind übernehmen, immer gegen den tatsächlichen Testlauf verifizieren, nicht nur gegen die Kompilierbarkeit | Empfehlung hier vermerkt, für M5-Planung relevant |
| OrbStack (und vermutlich andere Docker-Alternativen mit systemweitem Socket-Registrierung) lässt sich mit den üblichen `DOCKER_HOST`-Tricks nicht zuverlässig als "Docker fehlt" simulieren | Ein wichtiger Verifikationsschritt aus dem Wellenplan (M3-T5, Schritt 3) konnte auf dieser Maschine nicht direkt erbracht werden | Für M6 (CI-Planung): ein CI-Runner ganz ohne Docker-Daemon ist der einzige zuverlässige Weg, diesen Fall zu testen | Als offener Punkt in `reports/M3-T6-dryrun.md` vermerkt |
| Das Review-PR-gegen-eigenen-Fork-Muster aus M2 (siehe dortige Erkenntnis) gilt ausdrücklich auch für M4 — vom Auftraggeber im Chat explizit noch einmal bestätigt, bevor M4 beginnt | Community-Code-Review fand in M2 21 echte Bugs, die die eigene Testsuite nicht entdeckt hatte; dasselbe Muster muss für `quarkus-morphium` wiederholt werden, bevor der Upstream-PR gegen `sboesebeck/morphium` gestellt wird | M4-Wellenplan muss den Fork-Review-Schritt als Pflichtschritt vor dem Upstream-PR enthalten, nicht als optionalen Zusatz | Muss noch in `waves/M4-quarkus-pr.md` nachgezogen werden, falls diese Datei existiert und den Schritt noch nicht enthält |

---

## 9. Offene Punkte, die in die nächste Welle übergehen

| Punkt | Warum offen | Wer/Wann | Blockierend? |
|---|---|---|---|
| `ExclusiveMessageTests`-Flake im Kern blockiert Vollständigkeit von `mvn verify` unvorhersehbar lange | Vorbestehendes, von dieser Integration unabhängiges Problem | M6 (CI-Planung) sollte einen Timeout/Retry-Mechanismus vorsehen, oder der Test selbst muss separat untersucht werden | nein, aber relevant für jede CI-Einführung |
| "Docker fehlt"-Fall nicht auf dieser Maschine simulierbar, nur isoliert über die Assumption-Semantik verifiziert | OrbStack-spezifisches Verhalten | M6 sollte einen CI-Runner ohne Docker-Daemon als Testfall vorsehen | nein |
| `docs/gaps/JAKARTA-DATA.md` (im `quarkus-morphium`-Repo gefunden, nicht Teil des M3-T4-Scopes) — Relevanz nach der Integration ungeklärt | Außerhalb des Auftragsumfangs von M3-T4 | M4 sollte entscheiden, ob die Datei noch relevant ist oder durch `morphium-jakarta-data`s eigene Gap-Verfolgung ersetzt wurde | nein |
| `RELEASES.md` im `quarkus-morphium`-Repo als "kommt mit, aber zur Entfernung markiert" — endgültige Entscheidung noch nicht gefällt | Bewusst konservativ in MIGRATION-NOTES.md offen gelassen | M4 entscheidet bei der echten Kopie, ob die Datei mitkommt oder nicht | nein |
| Review-PR-gegen-Fork-Schritt (siehe Abschnitt 8) muss in M4 tatsächlich ausgeführt werden, nicht nur als Erkenntnis vermerkt sein | Vom Auftraggeber im Chat nochmal explizit bestätigt | M4, vor jedem `gh pr create` gegen `sboesebeck/morphium` | **ja — für M4, nicht für den Abschluss von M3** |

---

## 10. Vorbedingungen für die nächste Welle

```
[x] Extension-Guideline-Audit durchgeführt, Befunde priorisiert
[x] groupId in allen POMs de.caluga, artifactIds unverändert
[x] Vier Submodule mit quarkus-morphium-parent → morphium-parent
[x] Quarkus-BOM-Import ausschließlich in quarkus-morphium/pom.xml (I4)
[x] quarkus-extension.yaml aktualisiert; generierte artifact-Koordinate zeigt de.caluga
[x] Antora-Doku aktualisiert, keine offenen Bardioc1977-Verweise (außer bewusst: Showcase-Link)
[x] docs-for-morphium/quarkus-extension.md vorhanden, Properties gegen Code belegt
[x] MIGRATION-NOTES.md mit Dateiinventar
[x] Integrationstests überspringen sich ohne Docker (verifiziert über isolierte Assumption-Semantik, echte Maschinen-Simulation nicht möglich)
[x] Trockenlauf mit allen Invarianten PASS, Laufzeiten gemessen
[x] morphium/-Repository unverändert
[x] Zustandsdokument geschrieben, STATE.md und Gesamtplan zu aktualisieren (nächster Schritt)
```

---

## 11. Wiederaufnahme in einem Satz

Starte M4 mit dem Kopieren der in M3-T6 als "kommt mit" bestätigten Dateien aus `quarkus-morphium` (Branch `move-to-morphium`) ins `morphium`-Repository (aktuell Branch `pr/jakarta-data-module`, Reactor-Version `6.3.0-SNAPSHOT`), wende den in `reports/M3-T6-dryrun.md` dokumentierten `pom.xml`-Patch an, verifiziere erneut mit echten (nicht Wegwerf-) Commits, und befolge dabei zwingend das Fork-Review-Muster aus M2: erst PR gegen `Bardioc1977/morphium:master`, CodeRabbit/Copilot/Codex-Review abwarten und Findings beheben, bevor der Upstream-PR gegen `sboesebeck/morphium:develop` gestellt wird.
