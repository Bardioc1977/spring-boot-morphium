# Welle M1 — `morphium-jakarta-data` als Morphium-Modul vorbereiten

| Feld | Wert |
|---|---|
| Meilenstein | M1 |
| Status | ⬜ TODO |
| Abhängig von | — |
| Arbeitsort | `morphium-jakarta-data/`, Branch **`move-to-morphium`** |
| Ziel-Zustandsdokument | `status/<datum>-M1-jakarta-data-vorbereitung.md` |
| Agenten | 5 Sonnet-Agenten, teils parallel |

---

## Ziel

Der Branch `move-to-morphium` in `Bardioc1977/morphium-jakarta-data` enthält den
Stand, der 1:1 als Verzeichnis `morphium-jakarta-data/` in das
`morphium`-Repository kopiert werden kann: Modul-POM statt Standalone-POM,
vollständige Javadoc, Modul-README nach Morphium-Konvention, MkDocs-Seite,
CHANGELOG-Eintrag.

**M1 verändert das `morphium`-Repository nicht.** Das passiert erst in M2.

---

## Vorbedingungen (Orchestrator prüft, bevor der erste Agent startet)

```bash
cd morphium-jakarta-data
git status --short                 # muss leer sein
git fetch origin
git log --oneline -3 origin/develop
```

Branch anlegen (Orchestrator, nicht Agent):

```bash
cd morphium-jakarta-data
git checkout -b move-to-morphium origin/develop
```

Basis ist `develop` (`1.1.0-SNAPSHOT`, aktueller Stand), nicht `main`.

---

## Task-Übersicht

| ID | Task | Modell | Parallel mit | Status |
|---|---|---|---|---|
| M1-T1 | POM-Umbau zum Morphium-Modul | sonnet | — | ⬜ |
| M1-T2 | Javadoc für alle 15 Hauptklassen | sonnet | M1-T3, M1-T4 | ⬜ |
| M1-T3 | Modul-README + CHANGELOG | sonnet | M1-T2, M1-T4 | ⬜ |
| M1-T4 | MkDocs-Doku-Seite `docs/jakarta-data.md` | sonnet | M1-T2, M1-T3 | ⬜ |
| M1-T5 | Integrations-Trockenlauf + Invariantenprüfung | sonnet | — (nach T1–T4) | ⬜ |

Reihenfolge: T1 zuerst (die anderen referenzieren die Koordinaten). Dann T2, T3,
T4 parallel. T5 zum Schluss.

---

## M1-T1 — POM-Umbau zum Morphium-Modul

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium-jakarta-data
auf dem Branch `move-to-morphium`. Prüfe mit `git branch --show-current`, dass du
auf diesem Branch bist; wenn nicht, brich ab und melde das.

AUFGABE: Baue `pom.xml` von einem Standalone-POM zu einem Modul-POM des
Morphium-Multi-Modul-Projekts um.

REFERENZ: Die Datei
/Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium/poppydb/pom.xml
ist die verbindliche Vorlage. Lies sie zuerst vollständig. Ebenso
/Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium/pom.xml
(der Parent), um zu sehen, was in `pluginManagement` und `dependencyManagement`
bereits vorhanden ist.

ZIELZUSTAND von pom.xml:
1. `<parent>` auf de.caluga:morphium-parent:6.2.6-SNAPSHOT.
2. KEIN eigenes `<version>`-Element, KEIN eigenes `<groupId>`-Element (beides
   wird geerbt). Setze unmittelbar darüber diesen Kommentar:
   <!-- ENTSCHEIDUNG-OFFEN D1: Lockstep mit Morphium-Version. Bei Variante A
        (eigene Versionslinie) hier <version> und <morphium.version> ergänzen. -->
3. `<artifactId>morphium-jakarta-data</artifactId>`, `<packaging>jar</packaging>`.
4. `<name>` und `<description>` beibehalten bzw. präzisieren.
5. Entferne aus dem POM alles, was der Parent bereits liefert:
   - maven.compiler.* Properties (Parent setzt Java 21)
   - project.build.sourceEncoding
   - `<licenses>`, `<url>` (erbt vom Parent)
   - Plugin-VERSIONEN (Parent hat sie in pluginManagement)
6. Abhängigkeit auf den Kern:
   <dependency><groupId>de.caluga</groupId><artifactId>morphium</artifactId>
   <version>${project.version}</version></dependency>
   Genau so wie poppydb es macht.
7. `jakarta.data-api`: Version über eine Property `${jakarta.data.version}`.
   WICHTIG: Diese Property gehört NICHT in dieses POM, sondern in
   `morphium-parent`. Da du den Parent nicht ändern darfst (siehe VERBOTEN),
   deklariere die Property vorläufig lokal in diesem POM und setze den
   Kommentar:
   <!-- M2: nach morphium-parent verschieben (properties + dependencyManagement) -->
8. `slf4j-api`, `junit-jupiter`, `assertj-core`, `logback-classic` (test):
   Versionen entfernen, wo der Parent sie in dependencyManagement führt — prüfe
   das im Parent-POM nach. Wo der Parent sie NICHT führt (z.B. junit-jupiter
   Aggregat-Artefakt), Version beibehalten und einen Kommentar
   "<!-- M2: prüfen ob nach morphium-parent -->" setzen.
9. `<build>`: Plugin-Liste analog poppydb, also nur `<artifactId>`-Referenzen
   ohne Version für maven-compiler-plugin, maven-surefire-plugin,
   maven-source-plugin, maven-javadoc-plugin, maven-jar-plugin. `sourceDirectory`
   und `testSourceDirectory` wie bei poppydb setzen.
   Das maven-javadoc-plugin und maven-source-plugin MÜSSEN aktiviert sein —
   Maven Central lehnt Artefakte ohne sources.jar und javadoc.jar ab.

VERIFIKATION, die du selbst ausführen musst:
Da der Parent 6.2.6-SNAPSHOT lokal installiert sein muss, führe zuerst aus:
  cd /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium
  mvn -q -N install
  mvn -q -pl morphium-core -am install -DskipTests
Danach:
  cd /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium-jakarta-data
  mvn -B verify
Der Build muss durchlaufen und die 3 Testklassen müssen grün sein.
Prüfe zusätzlich, dass target/ ein *-sources.jar und ein *-javadoc.jar enthält.

Wenn `mvn -q -N install` oder der Core-Build fehlschlägt: NICHT im morphium-Repo
etwas ändern. Melde den Fehler und brich ab.

Committe am Ende mit einer Message im Conventional-Commits-Stil, z.B.
"build: convert to morphium-parent module POM".

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Änderungen an `morphium/morphium-core/**`, `morphium/poppydb/**` und
  `morphium/pom.xml`.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
- Änderungen an Java-Quellcode. Dieser Task ist ausschließlich POM-Arbeit.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.

BERICHTE am Ende: den vollständigen Inhalt der neuen pom.xml, das Ergebnis von
`mvn -B verify` (letzte 30 Zeilen), und eine Liste aller Properties/Dependencies,
die du mit "M2:"-Kommentaren markiert hast — die braucht die nächste Welle.
````

**Verifikation (Orchestrator führt selbst aus):**
```bash
cd morphium-jakarta-data && git log --oneline -1 && \
grep -c "<version>" pom.xml && \
grep -q "morphium-parent" pom.xml && echo "PARENT OK" && \
mvn -B verify -q && ls target/*.jar
```
Erwartung: Parent vorhanden, kein eigenes `<version>` direkt unter `<project>`,
Build grün, drei JARs (`*.jar`, `*-sources.jar`, `*-javadoc.jar`).

---

## M1-T2 — Javadoc für alle Hauptklassen

Der Auftrag verlangt ausdrücklich „eine ordentliche Dokumentation im Code".
15 Klassen in `src/main/java/de/caluga/morphium/data/`.

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium-jakarta-data
auf dem Branch `move-to-morphium`.

AUFGABE: Vollständige Javadoc für die 15 Klassen in
src/main/java/de/caluga/morphium/data/:
AbstractMorphiumRepository, CursorHelper, FindMethodBridge, JdqlMethodBridge,
JdqlParser, JdqlQuery, MethodNameParser, MorphiumPage, MorphiumRepository,
QueryDescriptor, QueryExecutor, QueryMethodBridge, QueryResultHelper,
RepositoryMetadata, SortMapper.

ANFORDERUNGEN pro Klasse:
- Klassen-Javadoc: Was tut die Klasse, welche Rolle hat sie in der
  Jakarta-Data-Verarbeitungskette, und — wichtig — wie hängt sie mit den
  Nachbarklassen zusammen. Ein Leser soll die Kette
  Methodenaufruf → Parser → Descriptor → Executor → ResultHelper verstehen
  können, ohne den Code zu lesen.
- Alle public und protected Methoden: @param, @return, @throws.
- Bei AbstractMorphiumRepository ist `setMorphium(Morphium)` der zentrale
  Erweiterungspunkt: Dokumentiere explizit, dass Framework-Adapter (Quarkus via
  @Inject + @PostConstruct, Spring via Setter) diese Methode überschreiben bzw.
  aufrufen, um ihre Injektion zu überbrücken. Das ist die wichtigste
  Designentscheidung des Moduls und muss im Javadoc stehen.
- Bei MethodNameParser und JdqlParser: die unterstützte Grammatik bzw. die
  erkannten Schlüsselwörter mit je einem Kurzbeispiel in einem
  <pre>{@code ...}</pre>-Block.
- Nutzungsbeispiele in Klassen-Javadoc dort, wo es dem Verständnis dient
  (mindestens bei AbstractMorphiumRepository und MorphiumRepository).

STIL: Beachte den Stil des umliegenden Morphium-Projekts. Sieh dir dazu
Javadoc in /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium/morphium-core/src/main/java/de/caluga/morphium/query/Query.java
an. Englisch, sachlich, keine Marketingsprache, kein "This class is responsible
for..."-Füllstil. Keine Autoren-Tags mit Namen oder E-Mail-Adressen.

STRIKTES NICHT-ZIEL: Ändere KEINE Programmlogik. Nur Javadoc-Kommentare
hinzufügen. Keine Umbenennungen, keine Refactorings, keine
Sichtbarkeitsänderungen, keine neuen Methoden, keine Formatierungsänderungen an
bestehendem Code. Wenn du beim Lesen einen Bug findest: NICHT beheben, sondern
im Abschlussbericht melden.

VERIFIKATION, die du selbst ausführen musst:
  mvn -B verify
  mvn -B javadoc:javadoc
Beide müssen ohne Fehler durchlaufen. `javadoc:javadoc` darf keine WARNINGS zu
fehlenden @param/@return in den 15 Klassen mehr ausgeben — prüfe die Ausgabe
darauf. Zusätzlich:
  git diff --stat
Der Diff darf ausschließlich Kommentarzeilen enthalten. Prüfe mit
  git diff -U0 | grep '^[+-]' | grep -v '^[+-][[:space:]]*\(/\*\*\|\*\|\*/\)' | grep -v '^[+-][+-][+-]'
dass hier NICHTS ausgegeben wird außer ggf. reinen Leerzeilen. Wenn doch, hast du
Code geändert — rückgängig machen.

Committe in maximal 3 Commits, z.B. "docs: add javadoc to repository core classes".

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Änderungen an `morphium/**` (ein anderes Repository).
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.

BERICHTE am Ende: Liste der 15 Klassen mit je einem Satz, was du dokumentiert
hast; die Ausgabe von `git diff --stat`; und alle beim Lesen gefundenen, NICHT
behobenen Auffälligkeiten im Code.
````

**Verifikation (Orchestrator):**
```bash
cd morphium-jakarta-data && mvn -B javadoc:javadoc -q 2>&1 | grep -ci warning
git diff --stat move-to-morphium~1
```
Zusätzlich: Der Orchestrator liest **stichprobenartig zwei Klassen** selbst und
bewertet die Javadoc-Qualität inhaltlich. Ein Sonnet-Agent kann formal korrekte,
inhaltlich leere Javadoc erzeugen („Gets the morphium. @return the morphium") —
das ist der wahrscheinlichste Qualitätsmangel dieses Tasks und muss manuell
geprüft werden.

---

## M1-T3 — Modul-README und CHANGELOG

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium-jakarta-data
auf dem Branch `move-to-morphium`.

KONTEXT: Dieses Repository wird als Verzeichnis `morphium-jakarta-data/` in das
Morphium-Hauptrepository (sboesebeck/morphium) integriert und danach archiviert.
Die Artefakt-Koordinaten werden `de.caluga:morphium-jakarta-data:<morphium-version>`
(derzeit 6.2.6-SNAPSHOT), NICHT mehr 1.1.0-SNAPSHOT.

AUFGABE 1 — README.md neu schreiben, als MODUL-README:
- Kein Standalone-Projekt-Auftritt mehr. Entferne die GitHub-Actions-Badge
  (der Workflow verschwindet) und die License-Badge (Lizenz kommt vom
  Hauptprojekt).
- Kopfzeile klarstellen: Dies ist ein optionales Modul von Morphium.
- Abschnitt "Was dieses Modul ist und was nicht": Es ist die
  framework-agnostische Jakarta-Data-Laufzeitschicht. Anwendungen binden es in
  der Regel NICHT direkt ein, sondern über quarkus-morphium oder
  spring-boot-morphium. Es existiert als eigenes Modul, um die Duplizierung
  dieser ~2400 Zeilen zwischen den Framework-Adaptern zu vermeiden. Wer eine
  eigene Framework-Integration baut (Micronaut, Helidon, Jakarta EE, plain
  Java), ist der direkte Zielnutzer.
- Abschnitt "Optionalität": Morphium-Kern hängt NICHT von diesem Modul ab. Wer
  nur `de.caluga:morphium` einbindet, bekommt kein jakarta.data-api.
- Maven-Koordinaten mit der neuen groupId/Version.
- Feature-Liste beibehalten und erweitern (aus der bestehenden README).
- Architektur-Abschnitt: die bestehende Klassenübersicht beibehalten, aber um
  einen Verarbeitungsketten-Abschnitt ergänzen (Repository-Methodenaufruf →
  MethodNameParser/JdqlParser → QueryDescriptor → QueryExecutor →
  QueryResultHelper → Rückgabetyp).
- Abschnitt "Eigene Framework-Integration bauen": beschreibe konkret, dass
  `AbstractMorphiumRepository` zu erweitern und `setMorphium(Morphium)`
  aufzurufen ist, mit einem minimalen Codebeispiel für plain Java.
- Abschnitt "Building": nach der Integration ist es
  `mvn -pl morphium-jakarta-data -am verify` im Morphium-Repo. Die alte
  Anleitung mit git clone von zwei Repos entfernen.
- Requirements-Tabelle: Java 21+, Morphium (gleiche Version), Jakarta Data 1.0.
- Verweise auf Bardioc1977-URLs durch sboesebeck/morphium-URLs ersetzen.

STIL-REFERENZ: /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium/docs/poppydb.md
und die README des Hauptprojekts. Englisch. Sachlich.

AUFGABE 2 — CHANGELOG.md:
Ergänze einen Abschnitt für die Integration. Beachte das Format des
Hauptprojekts (/Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium/CHANGELOG.md:
"Keep a Changelog", Überschriften "### Added/Changed/Fixed", darunter
"#### <Thema>" mit Prosaabsatz — sieh dir dort mindestens zwei Einträge an).
Inhalt des neuen Abschnitts:
- Changed: Modul des Morphium-Multi-Modul-Projekts; Koordinaten von
  de.caluga:morphium-jakarta-data:1.1.0 auf die Morphium-Version umgestellt;
  Migrationshinweis alt → neu für bestehende Nutzer.
- Der Abschnitt "## [Unreleased] - 1.0.0-SNAPSHOT" ist veraltet (das Modul ist
  bei 1.1.0-SNAPSHOT) — korrigiere das.

AUFGABE 3 — Repo-Meta-Dateien bewerten (nur bewerten, nicht löschen):
Erstelle die Datei `MIGRATION-NOTES.md` mit einer Tabelle, welche Dateien im
Wurzelverzeichnis beim Kopieren in das Morphium-Repo MITKOMMEN und welche
ENTFALLEN, mit Begründung. Zu bewerten: LICENSE, CODE_OF_CONDUCT.md,
CONTRIBUTING.md, SECURITY.md, .github/workflows/build.yml,
.github/ISSUE_TEMPLATE/*, .github/PULL_REQUEST_TEMPLATE.md, .gitignore,
CHANGELOG.md, README.md. Faustregel: Was das Hauptprojekt auf Repo-Ebene schon
hat, entfällt im Modulverzeichnis. Lösche in diesem Task nichts — die Datei ist
die Arbeitsgrundlage für M2.

STRIKTES NICHT-ZIEL: Kein Java-Code, kein POM.

VERIFIKATION, die du selbst ausführen musst:
- Alle Markdown-Links prüfen: für jeden relativen Link muss die Zieldatei
  existieren; für jeden Link auf github.com/Bardioc1977 muss begründet sein,
  warum er noch dort zeigt (in der Regel: gar nicht, also ersetzen).
- `grep -rn "Bardioc1977" README.md CHANGELOG.md` — es darf höchstens in einem
  bewusst gesetzten Historien-Hinweis vorkommen.
- `grep -rn "1.1.0-SNAPSHOT\|1.0.0-SNAPSHOT" README.md` — darf nur in
  Migrationshinweisen vorkommen.

Committe getrennt: "docs: rewrite README as morphium module documentation",
"docs: add changelog entry for module integration",
"docs: add migration notes for repository file inventory".

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Änderungen an `morphium/**` (ein anderes Repository).
- Löschen von Dateien.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.

BERICHTE am Ende: den vollständigen Inhalt von MIGRATION-NOTES.md und die
Überschriftenstruktur der neuen README.
````

**Verifikation (Orchestrator):** README selbst lesen und bewerten. Prüfen:
```bash
cd morphium-jakarta-data && grep -c "Bardioc1977" README.md CHANGELOG.md && ls MIGRATION-NOTES.md
```

---

## M1-T4 — MkDocs-Dokumentationsseite

Morphium dokumentiert über MkDocs Material. Das neue Modul braucht eine Seite in
dieser Navigation. Sie wird in M1 **vorbereitet** und in M2 ins Morphium-Repo
übernommen.

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium-jakarta-data
auf dem Branch `move-to-morphium`.

AUFGABE: Erstelle die Datei `docs-for-morphium/jakarta-data.md`. Diese Datei wird
in der nächsten Welle als `docs/jakarta-data.md` in das Morphium-Hauptrepository
kopiert und dort in die MkDocs-Navigation aufgenommen. Sie muss also für sich
stehen und im Stil der dortigen Doku sein.

STIL- UND STRUKTUR-REFERENZ — lies diese Dateien zuerst:
- /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium/docs/poppydb.md
  (das strukturelle Vorbild: ein optionales Modul dokumentieren)
- /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium/docs/index.md
- /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium/mkdocs.yml
  (welche Markdown-Extensions verfügbar sind: admonition, pymdownx.superfences,
  pymdownx.tabbed, tables, toc — nutze sie, aber nur diese)

INHALT (Zielumfang: gründlich, nicht knapp — das ist die Referenzdoku des Moduls):
1. Was Jakarta Data ist, in drei Sätzen, für einen Leser der es nicht kennt.
2. Wozu dieses Modul dient und dass es ein optionales Modul ist. Klar sagen:
   Anwendungen nutzen normalerweise quarkus-morphium oder
   spring-boot-morphium; direkt relevant ist dieses Modul für eigene
   Framework-Integrationen.
3. Die Abhängigkeitsrichtung explizit: Modul → Kern, niemals umgekehrt. Wer nur
   `de.caluga:morphium` einbindet, erhält kein jakarta.data-api. Nutze dafür
   einen `!!! note`-Admonition-Block.
4. Maven-Koordinaten.
5. Repository-Interfaces: CrudRepository, MorphiumRepository — mit vollständigem
   Codebeispiel einer Entity und eines Repository-Interface.
6. Query-Derivation: Tabelle der unterstützten Schlüsselwörter (findBy, countBy,
   existsBy, deleteBy, And, Or, Between, In, Like, GreaterThan, LessThan, Not,
   OrderBy) mit je einem Methodennamen-Beispiel und dem resultierenden
   Morphium-Query. Verifiziere jedes Schlüsselwort gegen den echten Code in
   src/main/java/de/caluga/morphium/data/MethodNameParser.java — erfinde
   NICHTS, was der Parser nicht unterstützt. Wenn du ein Schlüsselwort im Code
   nicht findest, nimm es nicht in die Tabelle auf.
7. JDQL via @Query: Grammatikumfang, gegen JdqlParser.java verifiziert, mit
   Beispielen.
8. @Find / @Delete mit @By-Parameterbindung.
9. Pagination: Page, CursoredPage, PageRequest — mit Beispiel und einem Hinweis,
   wann Cursor-Pagination der Offset-Pagination vorzuziehen ist.
10. Sortierung: Sort, Order, @OrderBy.
11. Rückgabetypen-Tabelle: welche Rückgabetypen unterstützt QueryResultHelper
    (List, Optional, Stream, CompletionStage, Page, CursoredPage, Skalare,
    Arrays ...) — verifiziert gegen QueryResultHelper.java.
12. Abschnitt "Eigene Framework-Integration": AbstractMorphiumRepository
    erweitern, setMorphium() aufrufen. Mit vollständigem, kompilierbarem
    plain-Java-Beispiel.
13. Abschnitt "Grenzen": was Jakarta Data hier NICHT abdeckt und wann man auf
    die Morphium-Query-API zurückfallen sollte (MorphiumRepository.query()).

WICHTIGSTE REGEL: Jedes technische Detail muss gegen den Quellcode in
src/main/java/de/caluga/morphium/data/ verifiziert sein. Erfinde keine Features.
Wenn du unsicher bist, ob etwas unterstützt wird, lies die Implementierung. Wenn
du es dann noch nicht weißt, lass es weg und notiere die offene Frage im
Abschlussbericht.

VERIFIKATION, die du selbst ausführen musst:
- Für jedes Schlüsselwort in der Derivation-Tabelle: zeige im Abschlussbericht
  die Codestelle (Datei:Zeile) in MethodNameParser.java, die es belegt.
- Für jeden Rückgabetyp: die Codestelle in QueryResultHelper.java.
- Alle Java-Codebeispiele müssen syntaktisch korrekt sein. Prüfe die
  verwendeten Klassennamen und Methodensignaturen gegen den echten Code.

Committe als "docs: add jakarta data documentation page for morphium mkdocs".

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Änderungen an `morphium/**` (ein anderes Repository) — auch nicht an dessen
  mkdocs.yml. Der Navigationseintrag wird in der nächsten Welle gesetzt.
- Änderungen an Java-Code.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.

BERICHTE am Ende: die Belegtabelle (Feature → Datei:Zeile), alle offenen Fragen,
und den vorgeschlagenen mkdocs.yml-Navigationseintrag als YAML-Schnipsel.
````

**Verifikation (Orchestrator):** Die Belegtabelle stichprobenartig gegenprüfen —
zwei Schlüsselwörter selbst im `MethodNameParser` nachlesen. Halluzinierte
Features sind bei Dokumentationstasks das Hauptrisiko.

---

## M1-T5 — Integrations-Trockenlauf

Beweist, dass der M2-Schritt funktionieren wird, **ohne** das Morphium-Repo zu
verändern.

**Prompt für den Sonnet-Agenten:**

````
AUFGABE: Trockenlauf der Integration von morphium-jakarta-data als Modul in
Morphium — in einer WEGWERF-KOPIE, nicht im echten Repository.

VORGEHEN:
1. Arbeitskopie anlegen:
   rm -rf /tmp/m1-dryrun && mkdir -p /tmp/m1-dryrun
   cp -R /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium /tmp/m1-dryrun/morphium
   cd /tmp/m1-dryrun/morphium && git checkout develop 2>/dev/null || true
   Hinweis: Falls der checkout wegen lokaler Änderungen fehlschlägt, arbeite auf
   dem aktuell ausgecheckten Branch weiter und notiere das im Bericht.
2. Modul hineinkopieren (ohne .git, ohne target, ohne Repo-Meta-Dateien):
   Kopiere aus /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium-jakarta-data
   nach /tmp/m1-dryrun/morphium/morphium-jakarta-data/ nur: pom.xml, src/,
   README.md, CHANGELOG.md. NICHT: .git, target, .github, LICENSE,
   CODE_OF_CONDUCT.md, SECURITY.md, CONTRIBUTING.md, MIGRATION-NOTES.md,
   docs-for-morphium.
3. In /tmp/m1-dryrun/morphium/pom.xml:
   a) <module>morphium-jakarta-data</module> so ergänzen, wie es die
      Vorzugsvariante aus
      /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium-jakarta-data/docs/plans/morphium-module-integration/decisions/D3-reactor-strategie.md
      vorsieht (Profil `extensions`, per Default aktiv über
      <name>!skipExtensions</name>). Lies dieses Dokument zuerst.
   b) Die Property <jakarta.data.version> in <properties> ergänzen und
      jakarta.data-api in <dependencyManagement> aufnehmen; die entsprechenden
      Stellen im Modul-POM (mit "M2:"-Kommentar markiert) darauf umstellen.
4. Vollständige Verifikation ausführen und JEDES Ergebnis protokollieren:
   cd /tmp/m1-dryrun/morphium
   mvn -B -q install -DskipTests                     # Reaktor baut alles
   mvn -B verify -pl morphium-jakarta-data           # Modultests grün
   mvn -B -q install -DskipTests -DskipExtensions    # Kern ohne Erweiterungen
   mvn -q -pl morphium-core dependency:tree > /tmp/m1-dryrun/core-tree.txt
   mvn -q -pl morphium-jakarta-data dependency:tree > /tmp/m1-dryrun/mjd-tree.txt

5. INVARIANTEN prüfen und jede einzeln mit Befehl und Ergebnis belegen
   (Definitionen in decisions/D3-reactor-strategie.md, Abschnitt "Invarianten"):
   I1: grep -E 'jakarta-data|quarkus|spring' morphium-core/pom.xml → keine Treffer
   I2: core-tree.txt enthält NICHT jakarta.data, io.quarkus, org.springframework
   I3: der -DskipExtensions-Build lief erfolgreich UND hat
       morphium-jakarta-data NICHT gebaut (prüfe die Reactor-Summary-Ausgabe)
   I4: morphium/pom.xml importiert keine Quarkus- oder Spring-BOM
   I5: mvn validate meldet keinen Zyklus
   Wenn eine Invariante verletzt ist: NICHT versuchen, sie durch Änderungen am
   Kern zu reparieren. Melden und abbrechen.

6. Erstelle den Bericht als Datei
   /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium-jakarta-data/docs/plans/morphium-module-integration/reports/M1-T5-dryrun.md
   mit: allen ausgeführten Befehlen und ihrem Ergebnis (PASS/FAIL), der
   Invariantentabelle, dem exakten Diff, der an morphium/pom.xml nötig war
   (als Copy-Paste-fertiger Patch für M2), und einer Liste aller Dateien, die
   in M2 kopiert werden müssen.

7. Räume /tmp/m1-dryrun NICHT auf — der Orchestrator prüft nach.

STRIKTES NICHT-ZIEL: Keine Änderung an
/Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium (dem echten
Repository). Nur /tmp/m1-dryrun und die eine Berichtsdatei unter docs/.

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Jede Änderung unter /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.

BERICHTE am Ende: die Invariantentabelle und den morphium/pom.xml-Patch.
````

**Verifikation (Orchestrator):**
```bash
cd /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium && git status --short
```
Muss **unverändert** sein (bis auf die schon vorher vorhandenen untracked
Dateien). Dann den Bericht lesen und die Invariantentabelle prüfen.

---

## Abschluss der Welle M1

Der Orchestrator führt aus, wenn T1–T5 verifiziert sind:

1. **Gesamtverifikation:**
   ```bash
   cd morphium-jakarta-data && git log --oneline move-to-morphium ^origin/develop && mvn -B verify
   ```
2. **Zustandsdokument** `status/<datum>-M1-jakarta-data-vorbereitung.md` nach der
   Vorlage in `status/TEMPLATE.md` schreiben.
3. **`status/STATE.md`** aktualisieren: aktuelle Welle → M2, M1 auf ✅.
4. **Gesamtplan `README.md`** Abschnitt 5 aktualisieren.
5. **JF-Dokument** `docs/jf/2026-07-morphium-modularisierung.md` aktualisieren.
6. Dem Auftraggeber den Stand berichten, inklusive der Frage, ob der Branch
   `move-to-morphium` nach `Bardioc1977/morphium-jakarta-data` gepusht werden
   soll — **Push nur nach Freigabe.**

## Definition of Done

- [ ] Branch `move-to-morphium` existiert lokal mit allen Commits
- [ ] `pom.xml` ist ein Modul-POM mit `morphium-parent` als Parent
- [ ] `mvn -B verify` grün; `sources.jar` und `javadoc.jar` werden erzeugt
- [ ] Alle 15 Hauptklassen haben inhaltlich substanzielle Javadoc (Orchestrator
      hat zwei stichprobenartig gelesen und bewertet)
- [ ] `javadoc:javadoc` ohne Warnungen zu fehlenden Tags
- [ ] README als Modul-README, keine offenen Bardioc1977-Verweise
- [ ] CHANGELOG-Eintrag im Format des Hauptprojekts
- [ ] `MIGRATION-NOTES.md` mit Dateiinventar für M2
- [ ] `docs-for-morphium/jakarta-data.md` vorhanden, Features gegen Code belegt
- [ ] Trockenlauf-Bericht mit allen fünf Invarianten auf PASS
- [ ] `morphium/`-Repository unverändert
- [ ] Zustandsdokument geschrieben, `STATE.md` und Gesamtplan aktualisiert
