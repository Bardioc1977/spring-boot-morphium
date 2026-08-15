# Welle M4 — PR: `quarkus-morphium` als Morphium-Modul

| Feld | Wert |
|---|---|
| Meilenstein | M4 |
| Status | ⬜ TODO |
| Abhängig von | M3 vollständig; M2 **gemergt** |
| Arbeitsort | `morphium/`, Branch **`pr/quarkus-extension-module`** von `origin/develop` |
| Ziel-Zustandsdokument | `status/<datum>-M4-quarkus-pr.md` |
| Agenten | 4 Sonnet-Agenten + Orchestrator (PR-Text) |

---

## 🚦 Freigabepflicht

**Kein PR ohne vorherige Vorlage und explizite Freigabe.** Ablauf wie in M2:
Text formulieren → im Chat zeigen → Freigabe abwarten → `git push fork` und
`gh pr create` durch den Orchestrator.

---

## Vorbedingungen

```bash
cd morphium
git fetch origin
git log --oneline -1 origin/develop     # muss den M2-Merge enthalten
git checkout -b pr/quarkus-extension-module origin/develop
```

Der M3-T6-Trockenlaufbericht muss vorliegen (enthält Patch und Kopierliste).

Falls `origin/develop` den M2-Merge **nicht** enthält: M4 nicht starten. Ein
Quarkus-Modul, das gegen ein noch nicht gemergtes `morphium-jakarta-data` baut,
erzeugt einen PR, der beim Reviewer nicht baut.

---

## Task-Übersicht

| ID | Task | Modell | Status |
|---|---|---|---|
| M4-T1 | Vier Submodule übernehmen, Reactor anpassen | sonnet | ⬜ |
| M4-T2 | Doku-Anbindung + CHANGELOG | sonnet | ⬜ |
| M4-T3 | `release.sh` um drei Artefakte erweitern | sonnet | ⬜ |
| M4-T4 | Vollverifikation vor PR | sonnet | ⬜ |
| M4-T5 | PR-Text, Vorlage, Erstellung | **Orchestrator** | ⬜ |

---

## M4-T1 — Submodule übernehmen

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium.
Prüfe mit `git branch --show-current`, dass du auf `pr/quarkus-extension-module`
bist. Wenn nicht: abbrechen und melden.
Prüfe außerdem, dass ./morphium-jakarta-data/ existiert (M2 gemergt). Wenn nicht:
abbrechen und melden.

LIES ZUERST:
- docs/plans/morphium-module-integration/reports/M3-T6-dryrun.md
  (erprobter pom.xml-Patch + Kopierliste)
- docs/plans/morphium-module-integration/decisions/D3-reactor-strategie.md
- /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/quarkus-morphium/MIGRATION-NOTES.md

AUFGABE 1 — Modulverzeichnis anlegen:
Kopiere aus /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/quarkus-morphium
(Branch move-to-morphium) nach ./quarkus-morphium/ genau die in
MIGRATION-NOTES.md als "kommt mit" ausgewiesenen Dateien. In der Regel:
pom.xml, runtime/, deployment/, testing/, integration-tests/, docs/,
README.md, CHANGELOG.md.
NICHT kopieren: .git, target/ (in allen Modulen!), docs-for-morphium/,
MIGRATION-NOTES.md, .github/, LICENSE, CODE_OF_CONDUCT.md, CONTRIBUTING.md,
SECURITY.md, CLAUDE.md, .mcp.json, .claude/, build/, und alles Weitere, das
MIGRATION-NOTES.md als "entfällt" führt.
Prüfe nach dem Kopieren explizit, dass keine target/-Verzeichnisse und keine
.class-Dateien mitgekommen sind:
  find quarkus-morphium -name target -o -name "*.class" | head

AUFGABE 2 — ./pom.xml anpassen:
Wende den Patch aus dem M3-T6-Bericht an: <module>quarkus-morphium</module> im
Profil `extensions`. Die Reihenfolge im Profil muss die Abhängigkeiten
respektieren: morphium-jakarta-data VOR quarkus-morphium. (Maven sortiert den
Reactor selbst, aber die Lesbarkeit der Datei zählt.)
Prüfe, ob die Property-Verschiebungen aus M3-T2 („M4:"-Kommentare in
quarkus-morphium/pom.xml) jetzt umzusetzen sind:
- <quarkus.version> — Entscheidung: gemäß D1, Absicherung B6, gehört die
  Property nach morphium-parent, damit ein Quarkus-Upgrade eine Einzeiländerung
  ist. Verschiebe sie dorthin und entferne den M4-Kommentar.
  ABER: Der Quarkus-BOM-IMPORT bleibt in quarkus-morphium/pom.xml. Nur die
  Versions-Property wandert. Das ist der Unterschied zwischen „Version zentral
  pflegen" (gut) und „400 Artefakte in jedem Kern-Build auflösen" (Verstoß gegen
  Invariante I4).
Erweitere den Kommentarblock im Parent um einen Hinweis auf diese Trennung.

ABSOLUT UNVERÄNDERT: morphium-core/**, poppydb/**, morphium-jakarta-data/**.

VERIFIKATION, die du selbst ausführen musst — jedes Ergebnis protokollieren:
  git diff --stat origin/develop -- morphium-core poppydb morphium-jakarta-data
                                                    # MUSS leer sein
  find quarkus-morphium -name target | head          # MUSS leer sein
  mvn -B install -DskipTests > /tmp/m4-t1-install.log 2>&1
  mvn -B verify -pl quarkus-morphium/runtime,quarkus-morphium/deployment,quarkus-morphium/testing > /tmp/m4-t1-verify.log 2>&1
  mvn -B install -DskipTests -DskipExtensions > /tmp/m4-t1-core.log 2>&1
  mvn -q -pl morphium-core dependency:tree > /tmp/m4-t1-core-tree.txt
  grep -E 'io.quarkus|jakarta.data|testcontainers' /tmp/m4-t1-core-tree.txt
                                                    # MUSS ohne Treffer bleiben
Logdateien mit dem Read-Tool auswerten, nicht pipen. Belege aus dem
-DskipExtensions-Log, dass KEIN Quarkus-Artefakt aufgelöst wurde.
Die Volltestsuite (~90 Min) führt M4-T4 aus, nicht du.

Committe in zwei Commits:
  "feat: add quarkus-morphium extension as optional module"
  "build: register quarkus-morphium in extensions profile"

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Änderungen an `morphium-core/**`, `poppydb/**`, `morphium-jakarta-data/**`.
- Verschiebung des Quarkus-BOM-Imports nach morphium-parent (Verstoß gegen I4).
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.

BERICHTE am Ende: `git diff --stat` gegen origin/develop, den Volltext-Diff von
./pom.xml, und alle Verifikationsergebnisse.
````

---

## M4-T2 — Doku-Anbindung

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium
auf dem Branch `pr/quarkus-extension-module`.

AUFGABE 1:
Kopiere /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/quarkus-morphium/docs-for-morphium/quarkus-extension.md
(Branch move-to-morphium) nach ./docs/quarkus-extension.md. Prüfe die Datei
gegen ./mkdocs.yml: nur dort aktivierte Markdown-Extensions verwenden, interne
Links müssen auf existierende Dateien zeigen.

AUFGABE 2 — ./mkdocs.yml:
Ergänze den Eintrag "Quarkus Extension: quarkus-extension.md" im Abschnitt
"Extensions", den M2 angelegt hat. Reihenfolge: Jakarta Data, dann Quarkus
Extension. Sonst nichts ändern.

AUFGABE 3 — ./docs/index.md:
Im Abschnitt für Erweiterungsmodule den Verweis auf quarkus-extension.md
ergänzen, im Stil der vorhandenen Einträge.

AUFGABE 4 — ./CHANGELOG.md:
Neuer Eintrag im etablierten Format (lies mindestens zwei bestehende Einträge
für Ton und Struktur: "### Added" → "#### <Thema>" → Prosaabsatz).
Inhalt: quarkus-morphium als neues optionales Modul mit drei publizierten
Artefakten (quarkus-morphium, -deployment, -testing) plus einem nicht
publizierten Testmodul. Nenne den Funktionsumfang (CDI-Producer,
@ConfigMapping, @MorphiumTransactional, Health Checks, Dev Services, Dev UI,
Jakarta-Data-Repositories via Gizmo, Native-Image-Support,
MorphiumId-JSON-Serialisierung, Migrations-Runner). Nenne ausdrücklich:
- die groupId-Migration von io.quarkiverse.morphium auf de.caluga mit
  Migrationshinweis für bestehende Nutzer (Version 1.2.0 → Morphium-Version)
- dass der Kern nicht davon abhängt und `-DskipExtensions` einen
  Kern-only-Build erzeugt
- dass die Integrationstests Docker benötigen und sich ohne Docker selbst
  überspringen
- dass der Code aus Bardioc1977/quarkus-morphium stammt und dort archiviert wird
- dass die Extension NICHT im Quarkiverse liegt und daher unter de.caluga
  veröffentlicht wird

AUFGABE 5 — Antora-Build-Anbindung bewerten:
quarkus-morphium/docs/ enthält eine Antora-Doku. Das Hauptprojekt nutzt MkDocs
(deploy-docs.yml). Prüfe, was mit der Antora-Doku im Hauptprojekt passiert:
Wird sie gebaut? Von wem?
Entscheidung gemäß D4, Teilfrage 4: koexistieren, keine Konvertierung. Ein
Antora-Workflow ist NICHT Teil dieses PRs — er wäre eine Prozessänderung und
gehört, wenn überhaupt, in M6.
Deine Aufgabe: Ergänze in ./docs/quarkus-extension.md einen Hinweis, wo die
ausführliche Extension-Doku im Repository liegt (quarkus-morphium/docs/), und
notiere im Bericht, ob und wie sie veröffentlicht werden sollte — als Vorschlag
für M6, nicht als Umsetzung.

AUFGABE 6 — ./README.md und ./README.de.md:
Wie in M2: nur ergänzen, wenn eine Modulübersicht existiert. Keine neue Struktur
erfinden. README.de.md inhaltlich identisch, auf Deutsch, mit korrekten Umlauten.

VERIFIKATION, die du selbst ausführen musst:
  python3 -c "import yaml; yaml.safe_load(open('mkdocs.yml'))"
  command -v mkdocs >/dev/null && mkdocs build --strict || echo "mkdocs nicht installiert"
Jeden relativen Link prüfen (Zieldatei existiert?) und als Tabelle OK/FEHLT
berichten.

Committe in zwei Commits:
  "docs: add quarkus extension documentation"
  "docs: add changelog entry for quarkus-morphium module"

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Änderungen an `morphium-core/**`, `poppydb/**`, `morphium-jakarta-data/**`,
  `./pom.xml`, `quarkus-morphium/**`.
- Anlegen eines Antora-GitHub-Actions-Workflows.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.

BERICHTE am Ende: mkdocs.yml-Diff, CHANGELOG-Eintrag im Volltext,
Link-Prüftabelle, und deinen Vorschlag zur Antora-Veröffentlichung für M6.
````

---

## M4-T3 — `release.sh` erweitern

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium
auf dem Branch `pr/quarkus-extension-module`.

KONTEXT: ./release.sh baut das Sonatype-Central-Bundle. M2 hat es um
morphium-jakarta-data erweitert. Jetzt kommen DREI weitere Artefakte hinzu —
und eines, das ausdrücklich NICHT ins Bundle darf.
Lies zuerst
docs/plans/morphium-module-integration/decisions/D4-build-release-workflow.md.

AUFGABE:
1. Lies release.sh vollständig und sieh dir an, wie M2 morphium-jakarta-data
   eingebaut hat (Schleife/Funktion oder Copy-Paste-Block).
2. Aufzunehmende Artefakte:
   - de.caluga:quarkus-morphium-parent  (POM-only, analog morphium-parent)
   - de.caluga:quarkus-morphium         (jar + sources + javadoc)
   - de.caluga:quarkus-morphium-deployment
   - de.caluga:quarkus-morphium-testing
3. NICHT ins Bundle: quarkus-morphium/integration-tests. Das ist ein Testmodul
   ohne Veröffentlichungszweck. Stelle sicher, dass es nicht versehentlich
   erfasst wird — wenn M2 eine generische Schleife über Modulverzeichnisse
   eingeführt hat, braucht sie hier eine explizite Ausschlussliste. Prüfe das
   und setze einen Kommentar, der den Ausschluss begründet.
4. Beachte den Sonderfall POM-only: quarkus-morphium-parent hat packaging=pom,
   also nur .pom, kein .jar/-sources/-javadoc. Sieh dir an, wie der
   morphium-parent-Block im Skript das löst, und mach es analog. Die
   Verifikationsschleife darf für dieses Artefakt keine JARs erwarten —
   ein häufiger Fehler.
5. Prüfe, dass die drei JAR-Artefakte tatsächlich sources.jar und javadoc.jar
   produzieren. Die Modul-POMs müssen maven-source-plugin und
   maven-javadoc-plugin aktivieren. Wenn eines fehlt, MELDE das — Sonatype
   lehnt das Bundle sonst nach dem Upload ab. Du darfst die Modul-POMs unter
   quarkus-morphium/ dafür anpassen; das ist Teil dieses Tasks.

VERIFIKATION, die du selbst ausführen musst:
  bash -n release.sh                    # MUSS fehlerfrei sein
  shellcheck release.sh                 # falls installiert; nur DEINE neuen
                                        # Findings beheben
  mvn -B -pl quarkus-morphium/runtime,quarkus-morphium/deployment,quarkus-morphium/testing \
      package -DskipTests source:jar javadoc:jar > /tmp/m4-t3-artifacts.log 2>&1
  # danach für jedes der drei Module prüfen:
  ls quarkus-morphium/runtime/target/*.jar
  ls quarkus-morphium/deployment/target/*.jar
  ls quarkus-morphium/testing/target/*.jar
Jedes Modul MUSS ein .jar, ein -sources.jar und ein -javadoc.jar liefern.
Führe release.sh NICHT aus. Dokumentiere im Bericht für jedes der nun sieben
Bundle-Artefakte, welche Dateien das Bundle enthalten wird.

Committe als "build: include quarkus-morphium modules in release bundle".

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- AUSFÜHREN von release.sh, `mvn release:prepare`, `release:perform`.
- `git tag` in jeder Form.
- Änderungen an `morphium-core/**`, `poppydb/**`, `morphium-jakarta-data/**`.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.

BERICHTE am Ende: den release.sh-Diff, die Artefaktliste je Modul mit
Dateinamen, die Bestätigung des integration-tests-Ausschlusses, und alle
POM-Anpassungen, die für sources/javadoc nötig waren.
````

**Verifikation (Orchestrator):** Diff vollständig lesen. Besonders prüfen, dass
`integration-tests` ausgeschlossen ist und der POM-only-Sonderfall korrekt
behandelt wird.

---

## M4-T4 — Vollverifikation vor PR

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium
auf dem Branch `pr/quarkus-extension-module`.

AUFGABE: Abschließende Verifikation. Jede Prüfung als PASS/FAIL mit Belegausgabe.

A — Isolation des Kerns:
  git diff --stat origin/develop -- morphium-core poppydb morphium-jakarta-data
                                                     # MUSS leer sein
  mvn -q -pl morphium-core dependency:tree > /tmp/m4-core-tree.txt
  grep -E 'io.quarkus|jakarta.data|org.springframework|testcontainers' /tmp/m4-core-tree.txt
                                                     # keine Treffer
  mvn -B install -DskipTests -DskipExtensions > /tmp/m4-core-only.log 2>&1
  # belegen: kein Quarkus-Artefakt aufgelöst, quarkus-morphium nicht im Reactor
  grep -n 'quarkus' pom.xml
  # belegen: nur die Versions-Property, KEIN BOM-Import im Parent (Invariante I4)

B — Extension-Konformität nach der Integration:
  mvn -B -pl quarkus-morphium/runtime,quarkus-morphium/deployment install -DskipTests > /tmp/m4-ext.log 2>&1
Lies das Log und werte die Meldungen des quarkus-extension-maven-plugin aus:
Paritätsverletzungen, fehlende Deployment-Gegenstücke, Descriptor-Warnungen.
Prüfe die generierte Datei
quarkus-morphium/runtime/target/classes/META-INF/quarkus-extension.yaml:
Feld `artifact` MUSS de.caluga:quarkus-morphium::jar:<version> lauten.

C — Tests der Extension:
  mvn -B verify -pl quarkus-morphium/runtime,quarkus-morphium/deployment,quarkus-morphium/testing > /tmp/m4-ext-verify.log 2>&1
  mvn -B verify -pl quarkus-morphium/integration-tests > /tmp/m4-it.log 2>&1
Für jeden Lauf: Tests run / Failures / Errors / Skipped und die Laufzeit.
Prüfe vorher mit `docker info`, ob Docker verfügbar ist, und notiere das —
davon hängt die Interpretation der Skipped-Zahl ab.

D — Volltestsuite des Kerns:
  mvn -B verify > /tmp/m4-full-verify.log 2>&1
Dauert etwa 90 MINUTEN plus die Extension-Tests. Ausgabe NICHT pipen (Hook
unterbindet das), Logdatei mit dem Read-Tool auswerten. Werte je Modul aus:
Tests, Failures, Errors, Skipped, plus das Reactor-Summary.
Bei Fehlschlägen: prüfen, ob sie auch ohne deine Änderungen auftreten (bekannte
Flakies existieren, u.a. Messaging und ChangeStream). Testnamen nennen. Fremde
Tests NICHT reparieren.

E — Commit-Hygiene:
  git log origin/develop..HEAD --format='%H%n%an <%ae>%n%s%n%b%n---'
Keine Co-Authored-By-Zeile, kein "Generated with", keine E-Mail-Adressen im
Body, Conventional Commits. Verstöße melden, NICHT rebasen oder amenden.

F — Diff-Umfang:
  git diff --stat origin/develop
Erwartet: neue Dateien unter quarkus-morphium/, Änderungen an ./pom.xml,
./mkdocs.yml, ./docs/index.md, ./docs/quarkus-extension.md, ./CHANGELOG.md,
./release.sh. Alles Weitere ist ein Befund.
  find quarkus-morphium -name target -o -name "*.class" -o -name "*.jar" | head
MUSS leer sein — keine Build-Artefakte im Commit.
  git ls-files quarkus-morphium | wc -l
Zahl im Bericht nennen; plausibel sind einige Hundert Dateien.

Schreibe den Bericht nach
docs/plans/morphium-module-integration/reports/M4-T4-verification.md

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Reparaturen an fremdem Code, um Tests grün zu bekommen.
- `git rebase`, `git commit --amend`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.

BERICHTE am Ende: die PASS/FAIL-Tabelle A–F und alle Befunde.
````

**Paralyse-Sonderregel:** Wie M2-T4 — Logdateigröße als Fortschrittsindikator,
30 Minuten ohne Wachstum = Eskalation.

---

## M4-T5 — PR-Text und Erstellung (Orchestrator)

**Der Orchestrator führt das selbst aus.**

### PR-Text-Aufbau

```markdown
Hi Stephan,

<Kontext: Nach morphium-jakarta-data folgt hier die Quarkus-Extension. Warum sie
nicht im Quarkiverse liegt (keine Reaktion der Verantwortlichen), warum sie
deshalb unter de.caluga veröffentlicht werden soll.>

## Was dieser PR macht
<Vier Submodule, drei publizierte Artefakte, ein Testmodul. Reactor-Einbindung.
Doku. CHANGELOG. release.sh.>

## groupId-Wechsel
<io.quarkiverse.morphium → de.caluga, mit Begründung: Namespace-Ownership bei
Sonatype. artifactIds und Java-Pakete unverändert. Migrationshinweis für Nutzer.>

## Optionalität
<Kern unverändert (git diff belegt es). Kern-Dependency-Tree ohne Quarkus,
Jakarta Data, Testcontainers. -DskipExtensions baut Kern + PoppyDB.
Quarkus-BOM-Import bleibt im Modul-POM, nur die Versions-Property steht im
Parent — mit Begründung, warum diese Trennung wichtig ist.>

## Docker und Integrationstests
<integration-tests nutzt Testcontainers. Ohne Docker überspringen sich die
betroffenen Tests, der Build bleibt grün. Das Modul wird nicht nach Central
publiziert.>

## Extension-Konformität
<Verweis auf das Audit: 30 Prüfpunkte gegen die offiziellen
Quarkus-Extension-Richtlinien. Ergebnis nennen, verbleibende bewusste
Abweichungen offen benennen.>

## Verifikation
<Befehle und Ergebnisse aus M4-T4.>

## Was noch kommt
<spring-boot-morphium als separater PR. Dazu, falls gewünscht, ein
CI-Workflow-PR.>

## Offene Fragen an dich
<Antora-Doku veröffentlichen? Extension-Status preview vs. stable?
Docker-Anforderung in CI akzeptabel?>

Cheers!
```

### Ablauf

1. Text formulieren.
2. Im Chat vorlegen. **Auf Freigabe warten.**
3. Nach Freigabe:
   ```bash
   cd morphium
   git push fork pr/quarkus-extension-module
   gh pr create --repo sboesebeck/morphium \
     --base develop \
     --head Bardioc1977:pr/quarkus-extension-module \
     --title "feat: add quarkus-morphium extension as optional module" \
     --body-file /tmp/pr-quarkus-body.md
   ```
4. PR-Nummer dokumentieren, Review verfolgen.

**Hinweis zur PR-Größe:** Dieser PR umfasst über 100 Java-Dateien. Das ist für
einen Reviewer viel. Der Orchestrator soll im PR-Text ausdrücklich anbieten, den
PR bei Bedarf aufzuteilen (z. B. runtime+deployment zuerst, testing und
integration-tests nachgelagert) — und den Umfang nicht kleinreden.

---

## Abschluss der Welle M4

1. Zustandsdokument `status/<datum>-M4-quarkus-pr.md` mit PR-Nummer und Status.
2. `STATE.md`, Gesamtplan, JF-Dokument aktualisieren.
3. Wartezustand dokumentieren; M5 kann parallel laufen (hängt nur an M2, nicht
   an M4).

## Definition of Done

- [ ] Vier Submodule im Repo, Build grün
- [ ] `morphium-core/**`, `poppydb/**`, `morphium-jakarta-data/**` unverändert
- [ ] Kern-Dependency-Tree frei von Quarkus/Testcontainers
- [ ] Quarkus-BOM-Import nur im Modul-POM (I4)
- [ ] Generierte Extension-Koordinate zeigt `de.caluga`
- [ ] Keine Build-Artefakte im Commit
- [ ] Doku angebunden, CHANGELOG-Eintrag
- [ ] `release.sh` um vier Artefakte erweitert, `integration-tests`
      ausgeschlossen, POM-only-Sonderfall behandelt
- [ ] Volltestsuite gelaufen und ausgewertet
- [ ] **PR-Text vorgelegt und freigegeben**
- [ ] PR erstellt, Nummer dokumentiert
