# Welle M6 — Konsolidierung: Release, CI, Stilllegung

| Feld | Wert |
|---|---|
| Meilenstein | M6 (der „eine mehr") |
| Status | ⬜ TODO |
| Abhängig von | M2, M4 **und** M5 gemergt |
| Arbeitsorte | `morphium/`, `quarkus-morphium-showcase/`, GitHub (Bardioc1977) |
| Ziel-Zustandsdokument | `status/<datum>-M6-konsolidierung.md` |
| Agenten | 5 Sonnet-Agenten + Orchestrator (PRs, GitHub-Administration) |

---

## Warum es diese Welle gibt

Die fünf Schritte des Auftrags enden mit dem letzten PR. Danach ist der Code
integriert, aber das Vorhaben ist nicht fertig:

1. **Release-Pfad** — `release.sh` ist dreimal additiv erweitert worden, aber nie
   im Zusammenhang geprüft. Ein Release, das erst beim echten Sonatype-Upload
   scheitert, ist teuer.
2. **CI** — Upstream hat heute **keinen** Build-Workflow (verifiziert: nur
   `deploy-docs.yml` und `sync-wiki.yml`). Mit drei zusätzlichen Modulen und
   Framework-Abhängigkeiten wird das von einem latenten zu einem realen Risiko.
   Insbesondere kann die Optionalität ohne automatisierten Check still erodieren.
3. **Showcase** — `quarkus-morphium-showcase` zeigt auf Koordinaten und einen
   `repository-dispatch`-Trigger, die es nach der Stilllegung nicht mehr gibt.
   Ohne Umstellung ist das Repo kaputt.
4. **Alt-Repos** — vier Bardioc1977-Repositories müssen kontrolliert stillgelegt
   werden, ohne die Herkunftsnachweise der Beiträge zu vernichten.
5. **Doppelte Dokumentation** — vier Repos dokumentieren teils überlappend; nach
   der Integration braucht es eine Quelle der Wahrheit.

---

## 🚦 Freigabepflichten in dieser Welle

Diese Welle enthält mehrere **nach außen sichtbare, schwer umkehrbare**
Aktionen. Für jede gilt Vorlagepflicht:

| Aktion | Freigabe nötig | Umkehrbar? |
|---|---|---|
| CI-Workflow-PR gegen Upstream | ✅ ja, Text vorlegen | ja (PR schließen) |
| Release-Trockenlauf ohne Upload | nein | ja |
| **Echter Sonatype-Upload** | ✅ ja, ausdrücklich | **nein** |
| Archivierung eines GitHub-Repos | ✅ ja | ja (Unarchivieren möglich) |
| Löschen eines Repos | **niemals** | nein |
| Showcase-PR (eigenes Repo) | ✅ ja, Text vorlegen | ja |
| Deaktivieren von Workflows in Alt-Repos | ✅ ja | ja |

Der echte Release nach Maven Central ist **nicht Teil dieser Welle** — er ist
Sache des Hauptmaintainers. M6 stellt nur sicher, dass er funktioniert, wenn er
angestoßen wird.

---

## Task-Übersicht

| ID | Task | Modell | Status |
|---|---|---|---|
| M6-T1 | `scripts/verify-core-isolation.sh` | sonnet | ⬜ |
| M6-T2 | Release-Trockenlauf (ohne Upload) | sonnet | ⬜ |
| M6-T3 | `build.yml` für Upstream entwerfen | sonnet | ⬜ |
| M6-T4 | Dokumentationskonsolidierung | sonnet | ⬜ |
| M6-T5 | Showcase auf neue Koordinaten umstellen | sonnet | ⬜ |
| M6-T6 | Stilllegung der Alt-Repos | **Orchestrator** | ⬜ |
| M6-T7 | CI-PR und Showcase-PR vorlegen und stellen | **Orchestrator** | ⬜ |

---

## M6-T1 — Invariantenprüfskript

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium.
Lege einen Branch an: `git checkout -b pr/verify-core-isolation origin/develop`
(vorher `git fetch origin`). Prüfe, dass alle drei Erweiterungsmodule vorhanden
sind: morphium-jakarta-data, quarkus-morphium, spring-boot-morphium. Fehlt eines:
abbrechen und melden.

LIES ZUERST:
docs/plans/morphium-module-integration/decisions/D3-reactor-strategie.md
— dort sind die Invarianten I1–I5 samt Prüfbefehlen definiert.

AUFGABE: Erstelle ./scripts/verify-core-isolation.sh.
Das Skript prüft maschinell, dass die Kern-Optionalität nicht erodiert ist. Es
ist die Absicherung gegen den Fall, dass jemand später versehentlich eine
Rückwärtsabhängigkeit einführt.

Anforderungen:
- POSIX-kompatible bash, `set -euo pipefail`
- Aufrufbar ohne Argumente aus dem Repo-Wurzelverzeichnis; prüft selbst, dass es
  dort läuft (pom.xml mit artifactId morphium-parent vorhanden)
- Exit 0, wenn alle Invarianten halten; Exit 1 bei der ersten Verletzung
  (mit `--all` alle Prüfungen durchlaufen und am Ende sammeln)
- Ausgabe: pro Prüfung eine Zeile "PASS/FAIL <Kürzel> <Beschreibung>", bei FAIL
  zusätzlich die verletzende Fundstelle. Keine Farben, wenn nicht TTY.
- `--help` mit Erklärung, was jede Invariante bedeutet und warum sie zählt

Zu prüfende Invarianten (Details in D3):
 I1  Kein Kernmodul (morphium-core, poppydb) deklariert eine Abhängigkeit auf
     ein Erweiterungsmodul. Prüfung über `mvn dependency:tree` je Kernmodul,
     Suche nach den Erweiterungs-artifactIds.
 I2  Kein Kernmodul importiert Klassen aus Framework-Paketen. Prüfung über grep
     auf `import io.quarkus`, `import org.springframework`, `import jakarta.data`
     in morphium-core/src und poppydb/src.
 I3  Der Kern baut ohne die Erweiterungen: `mvn -q install -DskipTests
     -DskipExtensions` MUSS erfolgreich sein UND es darf kein Quarkus-,
     Spring- oder Jakarta-Data-Artefakt aufgelöst werden.
 I4  Das Parent-POM importiert keinen fremden BOM (quarkus-bom,
     spring-boot-dependencies). Versions-Properties sind erlaubt, BOM-Importe
     nicht. Das ist die subtilste Invariante — prüfe im <dependencyManagement>
     des Parents auf <type>pom</type> mit <scope>import</scope> und melde jedes
     Artefakt, das nicht de.caluga oder ein bereits vorher vorhandener Import ist.
 I5  Jedes Erweiterungsmodul hängt nur auf de.caluga:morphium (und ggf.
     de.caluga:morphium-jakarta-data), nie umgekehrt.

WICHTIG zur Laufzeit: I3 ist der teuerste Check. Baue das Skript so, dass I1, I2,
I4, I5 in Sekunden laufen und I3 mit `--quick` übersprungen werden kann. Das
macht es als Pre-Commit- und CI-Check brauchbar. Dokumentiere das in --help.

Ergänze außerdem einen kurzen Abschnitt in ./docs/contributing.md (oder der
äquivalenten Datei — suche sie, erfinde keine neue), der erklärt: warum die
Erweiterungsmodule optional sind und dass dieses Skript das prüft.
Wenn keine passende Datei existiert, melde das und lege NICHTS an.

VERIFIKATION, die du selbst ausführen musst:
  bash -n scripts/verify-core-isolation.sh
  shellcheck scripts/verify-core-isolation.sh    # falls installiert
  ./scripts/verify-core-isolation.sh --quick
  ./scripts/verify-core-isolation.sh --all
Beide Läufe MÜSSEN im aktuellen, korrekten Zustand PASS liefern.
NEGATIVTEST — zwingend: Führe eine Verletzung künstlich ein (z. B. temporär eine
Dependency auf morphium-jakarta-data in morphium-core/pom.xml), prüfe, dass das
Skript FAIL meldet, und MACHE DIE ÄNDERUNG RÜCKGÄNGIG (`git checkout --
morphium-core/pom.xml`). Belege beides im Bericht. Ein Prüfskript, das nie
etwas findet, ist wertlos — dieser Test ist nicht optional.
Bestätige zum Schluss mit `git status`, dass keine Testverletzung übrig ist.

Committe als "build: add core isolation verification script".

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Änderungen an Produktionscode oder POMs, die über den Negativtest hinausgehen.
- Anlegen einer neuen contributing-Datei, wenn keine existiert.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.

BERICHTE am Ende: das Skript im Volltext, die Ausgaben beider Läufe, den
Negativtest mit Ausgabe, und die Bestätigung, dass `git status` clean ist.
````

**Verifikation (Orchestrator):** Den Negativtest selbst wiederholen. Ein Skript,
das im FAIL-Fall nicht anschlägt, ist schlimmer als keines — es erzeugt
Scheinsicherheit.

---

## M6-T2 — Release-Trockenlauf

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium
auf `origin/develop` (nur lesend bauen, kein Branch nötig — aber `git status`
muss vorher clean sein; wenn nicht: abbrechen und melden).

ZIEL: Belegen, dass der Release-Pfad nach drei additiven Erweiterungen von
release.sh vollständig und Central-tauglich ist — OHNE etwas zu veröffentlichen.

ABSOLUT VERBOTEN in dieser Aufgabe: release.sh ausführen, `mvn release:prepare`,
`mvn release:perform`, `mvn deploy`, `git tag`, irgendein HTTP-Upload an
central.sonatype.com. Du analysierst und baust lokal, du veröffentlichst nicht.

AUFGABE 1 — Statische Analyse von release.sh:
Lies das Skript vollständig. Erstelle eine Tabelle aller Artefakte, die im
Bundle landen: groupId, artifactId, packaging, welche Dateien (.pom, .jar,
-sources.jar, -javadoc.jar), Signatur- und Checksum-Behandlung.
Erwartet werden (Stand nach M2/M4/M5):
  de.caluga:morphium-parent (pom)
  de.caluga:morphium
  de.caluga:poppydb
  de.caluga:morphium-jakarta-data
  de.caluga:quarkus-morphium-parent (pom)
  de.caluga:quarkus-morphium
  de.caluga:quarkus-morphium-deployment
  de.caluga:quarkus-morphium-testing
  de.caluga:<spring-parent> (pom)
  de.caluga:<spring-autoconfigure>
  de.caluga:<spring-starter>
Die drei Spring-artifactIds sind ZWINGEND gegen die POMs zu verifizieren, nicht
zu raten: M5-T2 hat sie möglicherweise umbenannt (Konvention
`morphium-spring-boot-starter` statt `spring-boot-morphium-starter`). Lies
spring-boot-morphium/pom.xml und die Submodul-POMs und verwende, was dort steht.
Prüfe explizit, dass NICHT im Bundle landen: quarkus-morphium/integration-tests
und das Spring-Testmodul.
Nenne jede Diskrepanz zwischen dieser Erwartung und dem Skript.

AUFGABE 2 — Artefakte tatsächlich bauen:
  mvn -B clean install -DskipTests > /tmp/m6-release-build.log 2>&1
  mvn -B package -DskipTests source:jar javadoc:jar > /tmp/m6-release-artifacts.log 2>&1
Prüfe für JEDES zu publizierende Artefakt im jeweiligen target/-Verzeichnis, dass
.jar, -sources.jar und -javadoc.jar existieren. Tabelle: Artefakt, jar, sources,
javadoc, Größe. Ein fehlendes sources/javadoc ist ein FAIL — Sonatype lehnt das
Bundle nach dem Upload ab, und das merkt man dann erst im Release.

AUFGABE 3 — POM-Metadaten je Artefakt:
Für jedes zu publizierende Modul:
  mvn -q -pl <modul> help:effective-pom > /tmp/m6-epom-<modul>.xml
Prüfe die Central-Pflichtfelder: name, description, url, licenses (mit name und
url), scm (connection, developerConnection, url), developers (mit name).
Tabelle: Modul × Feld, vorhanden/fehlt, geerbt/eigen. Fehlende Felder sind
FAIL mit konkretem Behebungsvorschlag.
Achte besonders auf `description` und `name` — die werden je Modul erwartet und
NICHT sinnvoll vom Parent geerbt. Ein Modul-POM ohne eigenes <name> und
<description> ist ein häufiger Ablehnungsgrund.

AUFGABE 4 — GPG-Fähigkeit prüfen, nicht nutzen:
  gpg --list-secret-keys 2>&1 | head -20
Nur feststellen, ob ein Schlüssel vorhanden ist, und ob release.sh die
Schlüsselauswahl korrekt behandelt. NICHTS signieren, keinen Schlüssel anlegen,
keine Passphrase abfragen.

AUFGABE 5 — Bewertung:
Was müsste am Skript noch geändert werden, damit ein Release der elf Artefakte
in einem Durchlauf klappt? Konkrete Änderungsvorschläge mit Zeilennummern.
Nimm KEINE Änderungen vor — nur Vorschläge. Falls Änderungen zwingend sind,
markiere sie als BLOCKER, damit der Orchestrator entscheidet.

Schreibe den Bericht nach
docs/plans/morphium-module-integration/reports/M6-T2-release-dryrun.md

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- AUSFÜHREN von release.sh, `mvn release:*`, `mvn deploy`, `git tag`.
- Jeder Netzwerk-Upload an Sonatype, Nexus, GitHub Packages.
- GPG-Signieren oder Schlüsselerzeugung.
- Änderungen an release.sh oder POMs in dieser Aufgabe.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.

BERICHTE am Ende: Artefakttabelle, Metadatentabelle, alle Diskrepanzen und alle
BLOCKER.
````

**Verifikation (Orchestrator):** Die Metadatentabelle ist der wichtigste Teil.
Ein Modul ohne eigenes `<name>`/`<description>` selbst nachprüfen. Falls BLOCKER
gemeldet werden, entscheiden, ob ein zusätzlicher kleiner PR nötig ist.

---

## M6-T3 — `build.yml` für Upstream

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium.
Lege an: `git checkout -b pr/ci-workflow origin/develop` (nach `git fetch origin`).

KONTEXT: Upstream sboesebeck/morphium hat KEINEN Build-Workflow — nur
deploy-docs.yml und sync-wiki.yml. Auf fork/develop existiert ein erprobtes
build.yml (Commit 21ca5ab9), das bewusst fork-only war. Es soll jetzt als
Vorschlag für Upstream aufbereitet werden.
Lies zuerst
docs/plans/morphium-module-integration/decisions/D4-build-release-workflow.md,
Teilfrage 2.

AUFGABE 1 — Vorlage sichten:
  git show fork/develop:.github/workflows/build.yml
(Falls das Remote `fork` nicht existiert, prüfe mit `git remote -v` und melde.)
Analysiere: Trigger, Jobs, Java-Setup, Caching, Testaufrufe, Artefakt-Upload.
Identifiziere alles Fork-Spezifische — insbesondere: Klonen von Morphium oder
morphium-jakarta-data aus Quelle, Deploy nach GitHub Packages unter Bardioc1977,
repository-dispatch an das Showcase-Repo, Branch-Filter auf fork-Branches.
Nichts davon darf in den Upstream-Vorschlag.

AUFGABE 2 — .github/workflows/build.yml entwerfen:
Dreistufige Matrix gemäß D4:
  job "isolation" — ./scripts/verify-core-isolation.sh --all
      läuft zuerst, dauert Sekunden, bricht früh ab
  job "core"      — mvn -B verify -DskipExtensions -Dgroups=core
  job "full"      — mvn -B verify   (vollständig, inkl. Erweiterungen)
Anforderungen:
- Trigger: pull_request auf develop und master; push auf develop; workflow_dispatch
- Der `full`-Job dauert ~90 Minuten. Er darf PRs nicht blockieren: entweder
  `schedule` (nachts) plus push-auf-develop, oder als eigener Job mit
  `continue-on-error: false` aber nur bei push/schedule, nicht bei pull_request.
  Entscheide begründet und dokumentiere es als Kommentar im Workflow.
- Java 21 (temurin), Maven-Cache über actions/setup-java
- Alle Actions auf gepinnte Major-Versionen (nicht @master)
- timeout-minutes je Job setzen — sonst hängt ein Runner bis zum
  GitHub-Standardlimit
- Testberichte als Artefakt hochladen (if: always()), damit Fehlschläge
  auswertbar sind
- Docker: der full-Job braucht Docker für die Quarkus-Integrationstests.
  ubuntu-latest hat Docker. Prüfe und kommentiere, ob die Tests sich ohne Docker
  selbst überspringen (M3-T5 hat das eingerichtet) — der core-Job darf Docker
  NICHT brauchen.
- Concurrency-Gruppe, die vorherige Läufe desselben PR abbricht

AUFGABE 3 — .github/workflows/ prüfen:
Kollidiert der neue Workflow mit deploy-docs.yml oder sync-wiki.yml (gleiche
Trigger, gleiche Namen, Concurrency)? Melde jede Interaktion.

AUFGABE 4 — Begründungsdokument:
Schreibe docs/plans/morphium-module-integration/reports/M6-T3-ci-proposal.md:
was der Workflow prüft, welche Laufzeiten zu erwarten sind (nutze die Messwerte
aus den M3-T6-, M5-T4- und M6-T2-Berichten), was er KOSTET (GitHub-Actions-
Minuten), und welche Alternativen verworfen wurden. Dieses Dokument ist die
Grundlage des PR-Textes.

VERIFIKATION, die du selbst ausführen musst:
  python3 -c "import yaml,sys; yaml.safe_load(open('.github/workflows/build.yml'))"
  command -v actionlint >/dev/null && actionlint .github/workflows/build.yml || echo "actionlint nicht installiert"
Prüfe außerdem lokal, dass die drei Kommandos so funktionieren, wie der Workflow
sie aufruft:
  ./scripts/verify-core-isolation.sh --all
  mvn -B verify -DskipExtensions -Dgroups=core > /tmp/m6-t3-core.log 2>&1
Den full-Lauf NICHT hier ausführen — der ist in M4-T4/M5-T6 belegt.
Logs mit dem Read-Tool auswerten, nicht pipen.

Committe als "ci: add build workflow with core isolation check".

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- `gh workflow run`, `gh workflow enable/disable` — kein Auslösen von Workflows.
- Änderungen an deploy-docs.yml oder sync-wiki.yml.
- Übernahme fork-spezifischer Workflow-Teile.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.

BERICHTE am Ende: build.yml im Volltext, die Liste der weggelassenen
fork-spezifischen Teile, die Laufzeitschätzung je Job und die Kostenabschätzung.
````

---

## M6-T4 — Dokumentationskonsolidierung

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium.
Lege an: `git checkout -b pr/docs-consolidation origin/develop`.

KONTEXT: Nach drei Modulintegrationen gibt es Doppelungen und Lücken. Diese
Aufgabe räumt auf — OHNE inhaltlich umzuschreiben, was funktioniert.

AUFGABE 1 — Inventur:
Erfasse alle Dokumentationsquellen im Repo:
- docs/ (MkDocs, mit nav in mkdocs.yml)
- README.md, README.de.md, CHANGELOG.md im Wurzelverzeichnis
- README.md und CHANGELOG.md je Erweiterungsmodul
- quarkus-morphium/docs/ (Antora/AsciiDoc)
Tabelle: Datei, Umfang, Zielgruppe, Überschneidung mit anderen Dateien.

AUFGABE 2 — Überschneidungen benennen und auflösen:
Typische Fälle:
- Installationsanleitungen mit veralteten Koordinaten (Bardioc1977, alte
  Versionen, io.quarkiverse) — ALLE finden:
    grep -rn "Bardioc1977\|io.quarkiverse.morphium" --include="*.md" --include="*.adoc" --include="*.yml" .
  Jede Fundstelle bewerten: korrigieren, oder als bewusster historischer
  Verweis stehen lassen (z. B. in CHANGELOG-Einträgen — die dürfen NICHT
  umgeschrieben werden, sie dokumentieren die Vergangenheit).
- Property-Referenzen, die zwischen Modul-README und docs/-Seite abweichen —
  Abweichungen melden, die Modul-README als Quelle der Wahrheit behandeln
- Die drei Modul-CHANGELOGs vs. das Haupt-CHANGELOG: prüfe, ob die
  Modul-CHANGELOGs nach der Integration noch Sinn haben. Vorschlag machen,
  aber nichts löschen, ohne es zu begründen.

AUFGABE 3 — Übersichtsseite für die Modularchitektur:
Erstelle docs/module-overview.md:
- Diagramm (Mermaid, falls mkdocs.yml es aktiviert — sonst ASCII) der
  Abhängigkeitsrichtungen: morphium ← morphium-jakarta-data ←
  {quarkus-morphium, spring-boot-morphium}; morphium ← poppydb
- Tabelle: Modul, Koordinate, Zweck, wann brauche ich es
- Abschnitt "Warum die Erweiterungen optional sind" — die drei Bedeutungen von
  Optionalität und der Hinweis auf scripts/verify-core-isolation.sh
- Abschnitt "Versionierung" — Lockstep, mit Begründung in zwei Sätzen
- Abschnitt "Für Nutzer der alten Standalone-Artefakte" — Migrationstabelle
  alte → neue Koordinaten für alle Module, aus den MIGRATION-NOTES.md-Dateien
  der drei Vorbereitungswellen zusammengeführt
Trage die Seite in mkdocs.yml ein — sinnvoll als erste Seite des Abschnitts
"Extensions".

AUFGABE 4 — docs/index.md und README:
Sicherstellen, dass beide auf docs/module-overview.md verweisen und die
Modulübersicht konsistent ist. README.de.md inhaltlich gleichwertig, auf
Deutsch, mit korrekten Umlauten.

AUFGABE 5 — Linkprüfung über die gesamte Doku:
Prüfe jeden relativen Markdown-Link in docs/ auf ein existierendes Ziel.
Tabelle: Quelle:Zeile, Ziel, OK/FEHLT. Alle FEHLT-Einträge beheben, die durch
die Integration entstanden sind; ältere Bruchstellen nur melden.

VERIFIKATION, die du selbst ausführen musst:
  python3 -c "import yaml; yaml.safe_load(open('mkdocs.yml'))"
  command -v mkdocs >/dev/null && mkdocs build --strict || echo "mkdocs nicht installiert"
  grep -rn "Bardioc1977\|io.quarkiverse.morphium" --include="*.md" docs/ README.md README.de.md
Nach deiner Arbeit darf dieser grep nur noch bewusst belassene historische
Verweise finden — liste sie einzeln mit Begründung auf.

Committe in drei Commits:
  "docs: add module overview page"
  "docs: fix stale coordinates and links after module integration"
  "docs: consolidate module documentation"

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Löschen von Dokumentationsdateien ohne Auftrag — Vorschläge ja, Vollzug nein.
- Umschreiben bestehender CHANGELOG-Einträge.
- Änderungen an Code oder POMs.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.

BERICHTE am Ende: Inventurtabelle, Linkprüftabelle, die Liste der korrigierten
Koordinaten, die Liste der bewusst belassenen historischen Verweise, und deine
Vorschläge zu den Modul-CHANGELOGs.
````

---

## M6-T5 — Showcase umstellen

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/quarkus-morphium-showcase.
Prüfe `git status` (muss clean sein) und lege an:
`git checkout -b chore/morphium-module-coordinates`

KONTEXT: Das Showcase-Repo wird NICHT in Morphium integriert — es ist eine
Demo-Anwendung, kein Modul. Aber es zeigt auf Koordinaten und einen
CI-Trigger, die nach der Stilllegung von Bardioc1977/quarkus-morphium nicht mehr
existieren. Ohne diese Umstellung ist das Repo kaputt.

AUFGABE 1 — Inventur der Abhängigkeiten nach außen:
  grep -rn "io.quarkiverse.morphium\|Bardioc1977\|repository-dispatch\|GitHub Packages\|maven.pkg.github.com" \
    --include="*.xml" --include="*.yml" --include="*.yaml" --include="*.md" --include="*.properties" .
Tabelle: Datei:Zeile, Fundstelle, was daran nach der Stilllegung bricht.

AUFGABE 2 — pom.xml umstellen:
- de.caluga:quarkus-morphium statt io.quarkiverse.morphium:quarkus-morphium
  (artifactId bleibt, groupId wechselt — verifiziere die neuen Koordinaten gegen
  /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium/quarkus-morphium/runtime/pom.xml)
- Version auf die Morphium-Version (Lockstep). Verwende die aktuelle
  SNAPSHOT-Version, solange kein Release auf Central liegt, und setze einen
  Kommentar, dass beim ersten Central-Release auf die Release-Version
  umzustellen ist.
- <repositories>/<pluginRepositories> für GitHub Packages entfernen, falls
  vorhanden — nach dem Central-Release nicht mehr nötig. Falls noch SNAPSHOTs
  gebraucht werden, dokumentiere im README, dass ein lokales `mvn install` von
  Morphium nötig ist.
- Keine Anwendungslogik ändern.

AUFGABE 3 — .github/workflows:
- Den `repository-dispatch`-Empfänger-Trigger entfernen oder auf
  workflow_dispatch umstellen — das sendende Repo verschwindet.
- Schritte, die Morphium oder quarkus-morphium aus Quelle klonen und bauen,
  entfernen, sobald die Artefakte über Central verfügbar sind. Solange nur
  SNAPSHOTs existieren, den Klon-Schritt auf das Upstream-Repo
  sboesebeck/morphium umstellen (nicht Bardioc1977) und kommentieren, dass er
  nach dem ersten Release entfallen kann.
- Deploy nach GitHub Packages unter Bardioc1977 entfernen.

AUFGABE 4 — README.md:
Koordinaten aktualisieren, Verweis auf sboesebeck/morphium und die
Extension-Doku, Hinweis auf die Herkunft (die Extension war zuvor ein
eigenständiges Repo).

VERIFIKATION, die du selbst ausführen musst:
  mvn -B clean package -DskipTests > /tmp/m6-t5-build.log 2>&1
Der Build MUSS durchlaufen. Voraussetzung: die neuen Artefakte liegen im
lokalen Repository. Installiere sie vorher:
  cd /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium && mvn -B install -DskipTests
  cd /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/quarkus-morphium-showcase
Falls der Build an fehlenden Artefakten scheitert, notiere genau welche — das ist
ein Befund über die Vollständigkeit der Integration, kein Fehler deiner Arbeit.
  mvn -B verify > /tmp/m6-t5-verify.log 2>&1
Falls die Anwendung Tests hat, müssen sie grün sein. Docker-Bedarf prüfen und
notieren.
  python3 -c "import yaml,glob; [yaml.safe_load(open(f)) for f in glob.glob('.github/workflows/*.yml')]"
Logs mit dem Read-Tool auswerten, nicht pipen.

Committe in drei Commits:
  "build: switch to de.caluga coordinates for quarkus-morphium"
  "ci: remove dependency on archived source repositories"
  "docs: update README for new module coordinates"

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- `gh workflow run/enable/disable`, `gh repo edit`, `gh repo archive`.
- Änderungen an der Anwendungslogik oder an den Qute-Templates.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.

BERICHTE am Ende: die Inventurtabelle aus Aufgabe 1 mit Status je Zeile, alle
Diffs, das Buildergebnis und jeden Punkt, der erst nach dem ersten
Central-Release erledigt werden kann.
````

---

## M6-T6 — Stilllegung der Alt-Repos (Orchestrator)

**Nur der Orchestrator. Kein Agent führt GitHub-Administration aus.**

Betroffen sind vier Repositories unter `Bardioc1977`:
`morphium-jakarta-data`, `quarkus-morphium`, `spring-boot-morphium` und `morphium`
(der Fork — **Sonderfall, siehe unten**).

### Vorbedingung

Alle drei Modul-PRs sind **gemergt**. Solange ein PR offen ist, wird nichts
archiviert — der Branch im Fork muss erreichbar bleiben.

### Ablauf je Repo (Reihenfolge einhalten)

1. **Vorlegen.** Dem Nutzer die Liste der zu archivierenden Repos zeigen,
   inklusive der Feststellung, was jeweils verloren geht (nichts außer
   Schreibzugriff) und was erhalten bleibt (Issues, PRs, Historie, Tags).
   **Auf Freigabe warten.**
2. **Letztes Tag setzen**, damit der Stand referenzierbar bleibt:
   `git tag -a archived-<datum> -m "Final state before integration into sboesebeck/morphium"`
   und pushen. Erst nach Freigabe.
3. **README-Kopf** mit Wegweiser: Verweis auf `sboesebeck/morphium`, die neuen
   Koordinaten, eine Migrationstabelle alt → neu, und den Hinweis, dass die
   Entwicklung dort weitergeht. Als eigener Commit.
4. **Workflows deaktivieren**: alle Trigger auf `workflow_dispatch` reduzieren,
   damit keine Läufe mehr starten. Alternativ über
   `gh workflow disable <name> --repo Bardioc1977/<repo>`.
5. **Offene Dependabot-PRs schließen** mit kurzem Kommentar, der auf das neue
   Repo verweist.
6. **Archivieren**: `gh repo archive Bardioc1977/<repo>`.
   **Niemals `gh repo delete`.** Issues, PR-Diskussionen und Historie sind der
   Herkunftsnachweis der Beiträge — 59 gemergte PRs hängen daran.

### Sonderfall `Bardioc1977/morphium` (der Fork)

Dieses Repo wird **nicht archiviert**. Es ist das Push-Ziel des
triangulären Workflows und bleibt für künftige Beiträge nötig. Zu tun:
- `fork/develop` mit `origin/develop` synchronisieren, nachdem alle drei PRs
  gemergt sind
- prüfen, ob die fork-only Commits (`21ca5ab9`: `build.yml` +
  `MultiNodeElectionTest`-Polling) nach M6-T3 noch gebraucht werden — wenn der
  CI-PR gemergt wird, ist der `build.yml`-Teil obsolet
- die gemergten PR-Branches löschen

### Sonderfall `quarkus-morphium-showcase`

Bleibt bestehen und aktiv. Nur die Umstellung aus M6-T5 wird als PR eingebracht
(M6-T7). Nicht archivieren.

### Dokumentation

Für jedes Repo festhalten: Datum, gesetztes Tag, Archivierungsstatus. In
`status/<datum>-M6-konsolidierung.md`.

---

## M6-T7 — PRs vorlegen und stellen (Orchestrator)

Drei separate PRs, jeder mit eigener Freigabe. Bewusst getrennt, weil sie
unterschiedliche Zustimmungen brauchen.

### PR A — Isolationsskript (klein, unstrittig)

Branch `pr/verify-core-isolation`, Ziel `sboesebeck/morphium:develop`.

```markdown
Hi Stephan,

<Kurz: Nach der Integration der drei Erweiterungsmodule braucht es eine
maschinelle Absicherung, dass der Kern optional bleibt. Das Skript prüft fünf
Invarianten in Sekunden und kann lokal wie in CI laufen.>

## Was es prüft
<Die fünf Invarianten in je einem Satz.>

## Warum
<Ohne automatisierten Check erodiert Optionalität still: eine versehentliche
Dependency im Kern fällt niemandem auf, bis ein Nutzer sich über 40 MB
transitive Quarkus-Abhängigkeiten beschwert.>

## Verifikation
<Negativtest: künstlich eingeführte Verletzung wird erkannt. Ausgabe zeigen.>

Cheers!
```

### PR B — CI-Workflow (Prozessänderung, braucht eigene Zustimmung)

Branch `pr/ci-workflow`, Ziel `sboesebeck/morphium:develop`.
Grundlage: `reports/M6-T3-ci-proposal.md`.

Im Text ausdrücklich adressieren: dass dies eine Prozessänderung ist, dass die
Volltestsuite ~90 Minuten dauert und deshalb nicht bei jedem PR läuft, was es an
Actions-Minuten kostet, und dass der Vorschlag gerne abgelehnt oder verkleinert
werden kann. **Nicht drängen.** Es ist Stephans Projekt.

### PR C — Doku-Konsolidierung

Branch `pr/docs-consolidation`, Ziel `sboesebeck/develop`.
Im Text: was zusammengeführt wurde, welche veralteten Koordinaten korrigiert
wurden, welche historischen Verweise bewusst stehen bleiben.

### PR D — Showcase (eigenes Repo, aber trotzdem Vorlagepflicht)

Branch `chore/morphium-module-coordinates` im Showcase-Repo. Ziel ist der
Default-Branch `main` dieses Repos.
Auch hier: Text vorlegen, Freigabe abwarten. Die Regel gilt nicht nur für
Upstream-PRs — der Nutzer hat keine PRs ohne Vorlage gewünscht.

### Ablauf je PR

1. Text formulieren, im Chat vorlegen, **auf Freigabe warten**.
2. Nach Freigabe: `git push fork <branch>` bzw. `git push origin <branch>` beim
   Showcase, dann `gh pr create` mit `--body-file`.
3. PR-Nummer dokumentieren.

---

## Reihenfolge innerhalb der Welle

```
M6-T1 (Skript) ──┬─→ M6-T3 (build.yml nutzt das Skript)
                 │
M6-T2 (Release)  │      unabhängig, kann parallel
M6-T4 (Doku)     │      unabhängig, kann parallel
M6-T5 (Showcase) │      unabhängig, kann parallel
                 │
                 └─→ M6-T7 (PRs A, B, C, D)
                          │
                          └─→ M6-T6 (Archivierung — ERST nach Merge aller PRs)
```

T1, T2, T4 und T5 können parallel laufen (vier Sonnet-Agenten gleichzeitig). T3
braucht T1. T6 ist der letzte Schritt überhaupt.

---

## Abschluss der Welle M6 — und des Vorhabens

1. Zustandsdokument `status/<datum>-M6-konsolidierung.md` mit allen PR-Nummern,
   Archivierungsdaten und offenen Punkten.
2. `STATE.md` auf „Vorhaben abgeschlossen" setzen, mit einer Liste dessen, was
   bewusst offen bleibt (z. B. Quarkiverse-Aufnahme, echter Central-Release).
3. Gesamtplan `README.md`: Statustabelle vollständig, Rückblick ergänzen.
4. JF-Dokument `docs/jf/2026-07-morphium-modularisierung.md` auf den
   Endzustand aktualisieren — was erreicht wurde, was gelernt wurde, was noch
   aussteht.
5. Die vier D-Entscheidungen von „OFFEN" auf den tatsächlich getroffenen Stand
   setzen, mit Datum und Begründung, falls das JF anders entschieden hat als
   empfohlen.

## Definition of Done

- [ ] `scripts/verify-core-isolation.sh` existiert, Negativtest belegt
- [ ] Release-Trockenlauf: alle elf Artefakte mit sources/javadoc und
      vollständigen POM-Metadaten, keine BLOCKER offen
- [ ] `build.yml` entworfen, Begründungsdokument liegt vor
- [ ] Doku konsolidiert, keine veralteten Koordinaten außer bewussten
      historischen Verweisen
- [ ] Showcase baut gegen die neuen Koordinaten
- [ ] PRs A–D vorgelegt, freigegeben, gestellt, Nummern dokumentiert
- [ ] Alt-Repos nach Merge archiviert (nicht gelöscht), Tags gesetzt,
      READMEs als Wegweiser
- [ ] Fork synchronisiert, obsolete fork-only Commits bewertet
- [ ] `STATE.md`, Gesamtplan, JF-Dokument und D1–D4 auf Endstand
