# Welle M5 — `spring-boot-morphium`: Vorbereitung und PR

| Feld | Wert |
|---|---|
| Meilenstein | M5 |
| Status | ⬜ TODO |
| Abhängig von | M2 **gemergt** (nicht von M3/M4) |
| Arbeitsort | Teil A: `spring-boot-morphium/`, Branch `move-to-morphium`<br>Teil B: `morphium/`, Branch `pr/spring-boot-module` |
| Ziel-Zustandsdokument | `status/<datum>-M5-spring-boot.md` |
| Agenten | 6 Sonnet-Agenten + Orchestrator (PR-Text) |

> Der Auftrag fasst Schritt 5 als „analog zu 3 und 4" zusammen. Diese Welle
> bündelt daher Vorbereitung **und** PR. Sie ist unabhängig von M3/M4 und kann
> parallel dazu laufen, sobald M2 gemergt ist.

---

## 🚦 Freigabepflicht

**Kein PR ohne vorherige Vorlage und explizite Freigabe.** Text formulieren → im
Chat zeigen → Freigabe abwarten → `git push fork` und `gh pr create` durch den
Orchestrator. Agenten dürfen weder pushen noch PRs erzeugen.

---

## Warum diese Welle einfacher ist als M3/M4

| Aspekt | Quarkus | Spring Boot |
|---|---|---|
| Externe Konventionen | Quarkiverse-/Extension-Richtlinien, `quarkus-extension.yaml`, Deployment-Modul, Descriptor-Plugin | keine vergleichbaren Pflichten; Auto-Configuration ist ein Muster, kein Regelwerk |
| groupId | musste von `io.quarkiverse.morphium` weg | ist bereits `de.caluga` — nichts zu migrieren |
| Modulanzahl | 4 (`runtime`, `deployment`, `testing`, `integration-tests`) | 3 (`spring-boot-morphium-autoconfigure`, `-starter`, `-test`) |
| Docker in Tests | ja (Testcontainers) | zu prüfen |
| Build-Zeit-Verarbeitung | Jandex, Gizmo, Native Image | JDK-Proxies zur Laufzeit |

Der Hauptaufwand liegt bei Doku und POM-Konvertierung, nicht bei Konformität.

---

## Task-Übersicht

| ID | Task | Teil | Modell | Status |
|---|---|---|---|---|
| M5-T1 | Bestandsaufnahme + Spring-Boot-Konventionsprüfung | A | sonnet | ⬜ |
| M5-T2 | POM-Konvertierung auf Modulstruktur | A | sonnet | ⬜ |
| M5-T3 | Javadoc + Dokumentation | A | sonnet | ⬜ |
| M5-T4 | Trockenlauf der Integration | A | sonnet | ⬜ |
| M5-T5 | Module übernehmen, Reactor, Doku, `release.sh` | B | sonnet | ⬜ |
| M5-T6 | Vollverifikation vor PR | B | sonnet | ⬜ |
| M5-T7 | PR-Text, Vorlage, Erstellung | B | **Orchestrator** | ⬜ |

> **Hinweis zu den Modulpfaden in allen folgenden Prompts:** Die `-pl`-Argumente
> nennen die heutigen Verzeichnisnamen
> (`spring-boot-morphium/spring-boot-morphium-autoconfigure` usw.). Falls M5-T2
> die Artefakte umbenennt (siehe Vorabbefund unten), ändern sich damit auch die
> Verzeichnisse. Der Orchestrator passt die Pfade in den Prompts von T4, T5 und
> T6 vor dem Start an die im M5-T2-Bericht genannten neuen Pfade an.

---

## Teil A — Vorbereitung im Bardioc1977-Repo

### Vorbedingung

```bash
cd spring-boot-morphium
git status                       # muss clean sein
git checkout -b move-to-morphium
```

---

### M5-T1 — Bestandsaufnahme und Konventionsprüfung

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/spring-boot-morphium
auf dem Branch move-to-morphium. Diese Aufgabe ist REIN ANALYTISCH — du änderst
keine Datei außer dem Bericht.

AUFGABE 1 — Bestandsaufnahme:
Verschaffe dir einen vollständigen Überblick:
- Modulstruktur (pom.xml je Modul), Koordinaten, Versionen
- alle Java-Klassen mit Zweck in einem Satz
- welche Spring-Boot-Mechanismen genutzt werden: Auto-Configuration,
  @ConfigurationProperties, ImportBeanDefinitionRegistrar, AOP, Actuator
- welche Ressourcendateien den Auto-Config-Mechanismus tragen, insbesondere
  META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
  (das ist der Boot-3-Mechanismus; ein noch vorhandenes
  META-INF/spring.factories wäre ein Boot-2-Relikt und ein Befund)
- Testabdeckung: welche Tests, welche Laufzeit, Docker nötig?

AUFGABE 2 — Konventionsprüfung Spring Boot Starter:
Es gibt keine formalen „Starter-Richtlinien", aber etablierte Konventionen. Hole
die offizielle Doku mit WebFetch und prüfe gegen sie:
- https://docs.spring.io/spring-boot/reference/features/developing-auto-configuration.html
- https://docs.spring.io/spring-boot/reference/using/build-systems.html#using.build-systems.starters

Prüfe mindestens:
 1. Namensschema: Drittanbieter-Starter heißen `<projekt>-spring-boot-starter`,
    NICHT `spring-boot-starter-<projekt>` (dieses Präfix ist für das
    Spring-Team reserviert). Prüfe die artifactIds und MELDE eine Verletzung
    ausdrücklich — sie ist unter Spring-Boot-Nutzern gut bekannt und würde in
    einem Review sofort auffallen.
 2. Aufteilung autoconfigure / starter: die Auto-Config-Klassen im
    autoconfigure-Modul, das starter-Modul praktisch leer und nur Dependencies.
 3. `@AutoConfiguration` (Boot 3) statt `@Configuration` für die Auto-Config.
 4. Registrierung über AutoConfiguration.imports, nicht spring.factories.
 5. `@ConditionalOnMissingBean` bei allen Bean-Definitionen, damit Nutzer
    überschreiben können.
 6. `@ConditionalOnClass` / `@ConditionalOnProperty`, wo die Auto-Config von
    optionalen Abhängigkeiten abhängt.
 7. `@ConfigurationProperties` mit Prefix `spring.morphium` — prüfe, ob der
    Prefix in Doku und Code identisch ist.
 8. Metadaten für die IDE-Autovervollständigung: erzeugt der Build
    `META-INF/spring-configuration-metadata.json`? Ist
    spring-boot-configuration-processor als Dependency eingebunden?
 9. Kein `spring-boot-starter-parent` als Parent-POM (das würde die
    Integration in den Morphium-Reactor blockieren). Prüfe, wie die
    Spring-Boot-Versionen stattdessen gemanagt werden — spring-boot-dependencies
    als BOM-Import wäre korrekt.
10. Actuator-Integration: gibt es HealthIndicator? Sind sie mit
    @ConditionalOnClass abgesichert, sodass Actuator optional bleibt?
11. Aktualität: Spring Boot 3.4.4 ist im POM. Prüfe, welche Version aktuell ist,
    und bewerte, ob ein Upgrade Teil dieser Welle sein sollte oder nicht.
12. Lizenz-/Metadatenfelder in den POMs für Maven Central: name, description,
    url, licenses, scm, developers.

AUFGABE 3 — Bewertung für die Integration:
- Welche Dateien kommen als Modul mit, welche entfallen (analog zu den
  MIGRATION-NOTES anderer Module)?
- Welche Abhängigkeiten müssen ins Modul-POM, welche gehören in
  morphium-parent/dependencyManagement?
- Bringt spring-boot-dependencies als BOM-Import Konflikte mit den Versionen im
  Morphium-Parent? Nenne konkrete Kandidaten (slf4j, logback, jackson).
  Wichtig: Der BOM-Import gehört ins MODUL-POM, nicht in den Parent — sonst
  würde jeder Kern-Build Spring-Koordinaten auflösen.
- Braucht das test-Modul Docker?

Schreibe den Bericht nach
/Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium-jakarta-data/docs/plans/morphium-module-integration/reports/M5-T1-spring-audit.md
Struktur: Bestandsaufnahme / Konventionstabelle (Nr, Anforderung, Status
OK-ABWEICHUNG-UNKLAR, Belegdatei:Zeile, Handlungsvorschlag) / Integrationsplan /
Risiken.
Behauptungen ohne Dateibeleg sind nicht zulässig.

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Änderungen an Produktions- oder Testcode in dieser Aufgabe.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.

BERICHTE am Ende: die Konventionstabelle als Zusammenfassung plus die drei
größten Risiken.
````

**Verifikation (Orchestrator):** Prüfpunkt 1 (Namensschema) selbst gegen
`spring-boot-morphium/*/pom.xml` nachsehen — davon hängt ab, ob artifactIds
umbenannt werden müssen, was den PR erheblich verändert. Prüfpunkt 9 ebenfalls
selbst verifizieren: ein `spring-boot-starter-parent` als Parent wäre ein
Blocker für die Reactor-Integration.

> **Vorabbefund des Orchestrators (2026-07-25, verifiziert):** Die artifactIds
> lauten heute `spring-boot-morphium-autoconfigure`,
> `spring-boot-morphium-starter` und `spring-boot-morphium-test`. Das
> `spring-boot-`-Präfix ist die Konvention des Spring-Teams; Drittanbieter
> sollen `morphium-spring-boot-starter` heißen. Der Agent wird das als
> ABWEICHUNG melden — der Orchestrator entscheidet dann, ob umbenannt wird.
> **Empfehlung:** ja, jetzt umbenennen. Das Modul hat noch keinen
> Central-Release, also gibt es keine Nutzer, die brechen; nach dem ersten
> Release wäre die Umbenennung teuer. Die Frage gehört in den PR-Text.

---

### M5-T2 — POM-Konvertierung

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/spring-boot-morphium
auf dem Branch move-to-morphium.

LIES ZUERST:
- docs/plans/morphium-module-integration/reports/M5-T1-spring-audit.md
- docs/plans/morphium-module-integration/decisions/D1-versionierung.md (Lockstep)
- docs/plans/morphium-module-integration/decisions/D3-reactor-strategie.md
  (Invarianten I1–I5)
- /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium/poppydb/pom.xml
  — VERBINDLICHE VORLAGE für ein Morphium-Modul-POM

AUFGABE — Wurzel-pom.xml auf Modulstruktur umstellen:
1. <parent> auf de.caluga:morphium-parent:<aktuelle SNAPSHOT-Version>, relativePath ../pom.xml
2. Eigene <groupId> und <version> ENTFERNEN (werden geerbt) — Lockstep gemäß D1
3. artifactId: gemäß M5-T1-Befund zu Prüfpunkt 1. Wenn eine Umbenennung nötig
   ist, benenne konsistent um und dokumentiere jede alte→neue Koordinate.
   Führe eine Umbenennung NUR durch, wenn der Bericht sie als ABWEICHUNG
   ausweist. Nicht auf Verdacht umbenennen.
   Bei einer Umbenennung ändern sich auch die Verzeichnisnamen und die
   <module>-Einträge. Verwende `git mv`, nicht kopieren-und-löschen, damit die
   Historie erhalten bleibt. Prüfe danach mit
   `grep -rn "<alter-artifactId>" --include="*.xml" --include="*.md" --include="*.yml" .`,
   dass keine Fundstelle übrig ist, und nenne im Bericht die neuen Pfade —
   die Folgetasks M5-T4/T5/T6 verwenden sie in ihren `-pl`-Argumenten.
4. Morphium-Abhängigkeiten auf ${project.version} umstellen:
   de.caluga:morphium und de.caluga:morphium-jakarta-data
5. spring-boot-dependencies BOM-Import BLEIBT im Modul-POM. Nicht in den
   Parent verschieben — Invariante I4. Setze einen Kommentar, der das begründet.
   Die reine Versions-Property (spring-boot.version) darf in den Parent wandern;
   markiere das mit einem Kommentar "M5-T5:" für die Integrationswelle.
6. Doppelte Plugin-Konfiguration entfernen, die vom Parent-pluginManagement
   abgedeckt ist — genau wie poppydb es tut. maven-source-plugin und
   maven-javadoc-plugin MÜSSEN aktiv bleiben (Central-Pflicht).
7. classgraph: spring-boot-morphium braucht classgraph explizit. Prüfe, ob das
   nach der Integration noch gilt (Morphium deklariert es ggf. als optional) und
   belasse die explizite Dependency, wenn sie nötig ist — mit Kommentar warum.
8. Submodul-POMs (autoconfigure, starter, test): <parent> zeigt weiterhin auf
   das spring-boot-morphium-Wurzel-POM. Eigene groupId/version entfernen.
9. distributionManagement (GitHub Packages) entfernen, falls vorhanden — der
   Release läuft künftig über release.sh nach Central.
10. Java-Paketnamen NICHT ändern. Keine Klasse anfassen.

ABBRUCHKRITERIUM: Falls du feststellst, dass das Wurzel-POM
spring-boot-starter-parent als <parent> nutzt, kannst du Schritt 1 nicht
umsetzen, ohne das Dependency-Management zu ersetzen. Setze dann stattdessen
spring-boot-dependencies als BOM-Import ein, prüfe den Build sorgfältig und
dokumentiere jede Version, die zuvor vom Starter-Parent kam und jetzt fehlt.

VERIFIKATION, die du selbst ausführen musst:
  mvn -B -N help:effective-pom > /tmp/m5-t2-effective.txt 2>&1
  mvn -B install -DskipTests > /tmp/m5-t2-install.log 2>&1
  mvn -B verify > /tmp/m5-t2-verify.log 2>&1
Hinweis: Der Parent liegt in diesem Repo noch nicht als ../pom.xml vor. Der Build
schlägt daher mit "Non-resolvable parent POM" fehl, SOLANGE morphium-parent nicht
im lokalen Repository installiert ist. Installiere es zuerst:
  cd /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium && mvn -B -N install
  cd /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/spring-boot-morphium
Wenn relativePath dann noch stört, entferne es NICHT — dokumentiere im Bericht,
dass der endgültige Test in M5-T4 (Trockenlauf) erfolgt.
Logs mit dem Read-Tool auswerten, nicht pipen.

Erstelle MIGRATION-NOTES.md im Repo-Wurzelverzeichnis:
- Tabelle alte Koordinate → neue Koordinate für jedes Artefakt
- Liste "kommt als Modul mit" / "entfällt bei der Integration"
- offene Punkte für die Integrationswelle (alle "M5-T5:"-Kommentare)
- Hinweis für bestehende Nutzer: was in ihrer pom.xml zu ändern ist

Committe in zwei Commits:
  "build: convert to morphium module POM structure"
  "docs: add migration notes for morphium integration"

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Änderungen an Java-Quellcode, Paketnamen, Klassennamen.
- Verschiebung des spring-boot-dependencies-BOM-Imports in den Parent.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.

BERICHTE am Ende: alle POM-Diffs im Volltext, das Buildergebnis, die
Koordinatentabelle und jede Umbenennung mit Begründung.
````

---

### M5-T3 — Javadoc und Dokumentation

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/spring-boot-morphium
auf dem Branch move-to-morphium.

KONTEXT: Nach der Integration wird dieser Code Teil des offiziellen
Morphium-Projekts und über Maven Central veröffentlicht. Der Javadoc landet auf
javadoc.io. Der Anspruch ist der eines Bibliotheks-Javadoc, nicht der einer
internen Notiz.

AUFGABE 1 — Javadoc:
Ergänze fehlenden Javadoc für alle public und protected Typen und Member in
autoconfigure/ und starter/. Für jede Klasse:
- Was tut sie, und WANN greift sie (welche @Conditional-Bedingungen)?
- Bei Auto-Configuration-Klassen: welche Beans stellt sie bereit, und wie
  überschreibt ein Nutzer sie (@ConditionalOnMissingBean)?
- Bei @ConfigurationProperties: der vollständige Property-Prefix und je Feld die
  Bedeutung, der Standardwert und die Einheit bei Zeiten/Größen.
- @param, @return, @throws vollständig, wo zutreffend.
- Bei @EnableMorphiumRepositories und dem Registrar: erklären, wie der
  Proxy-Mechanismus funktioniert und wo er sich vom Quarkus-Ansatz
  unterscheidet (JDK-Proxy zur Laufzeit vs. Bytecode-Generierung zur Bauzeit).
  Das ist die zentrale Architekturaussage dieses Moduls.
- Ein Nutzungsbeispiel im Klassen-Javadoc der Einstiegspunkte, in
  {@snippet} oder <pre>{@code ...}</pre>.

ZWINGEND: Kein Javadoc, der nur den Namen wiederholt. "Sets the morphium." zu
setMorphium ist wertlos und WIRD ZURÜCKGEWIESEN. Schreibe, was der Aufruf
bewirkt, wann er passiert und was ein Aufrufer wissen muss.
KEINE @author-Tags. KEINE Lizenzheader (Morphium nutzt keine im Quellcode).
Verhalten NICHT ändern — nur Kommentare.

AUFGABE 2 — README.md:
Aktualisiere:
- Hinweis oben: Dieses Modul wird Teil von Morphium; neue Koordinaten
- Versionierung: Lockstep mit Morphium
- Installationsbeispiel mit den neuen Koordinaten
- Vollständige Property-Referenz für spring.morphium.* — jede Property gegen den
  @ConfigurationProperties-Code verifizieren. Erfinde keine.
- Abschnitt "Verhältnis zu morphium-jakarta-data": was dort liegt, was hier
- Abschnitt "Abgrenzung zu Spring Data MongoDB": kurz und sachlich, ohne
  Marketing — was dieses Modul bietet und was es nicht ist

AUFGABE 3 — CHANGELOG.md:
Eintrag im Keep-a-Changelog-Format für die Umstellung.

AUFGABE 4 — Übersichtsseite für die Morphium-Doku:
Erstelle ./docs-for-morphium/spring-boot.md. Diese Datei wird in M5-T5 nach
morphium/docs/ kopiert und in mkdocs.yml eingetragen.
Inhalt: Einleitung, Installation, Konfigurationsreferenz, Repository-Nutzung,
Transaktionen, Health/Actuator (falls vorhanden), Abgrenzung zu Spring Data
MongoDB, Verweis auf das Modul-README.
Nur Markdown-Features, die
/Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium/mkdocs.yml
aktiviert. Orientiere Ton und Aufbau an einer bestehenden Seite unter
morphium/docs/.

BELEGPFLICHT: Für jede dokumentierte Property und jedes dokumentierte Verhalten
musst du im Bericht Datei:Zeile angeben, wo es im Code steht. Dokumentiere
NICHTS, was du nicht im Code gesehen hast.

VERIFIKATION, die du selbst ausführen musst:
  mvn -B javadoc:javadoc > /tmp/m5-t3-javadoc.log 2>&1
Log mit dem Read-Tool auswerten. ALLE Javadoc-Warnungen zu Dateien, die du
angefasst hast, beheben. Warnungen in fremden Dateien nur melden.
  mvn -B verify > /tmp/m5-t3-verify.log 2>&1
Muss unverändert grün sein.

Committe in vier Commits:
  "docs: add javadoc to spring boot autoconfiguration"
  "docs: update README for morphium module integration"
  "docs: add changelog entry"
  "docs: add spring boot overview page for morphium docs"

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Verhaltensänderungen, Refactorings, Umbenennungen, Signaturänderungen.
- Erfundene Konfigurationsoptionen oder Features.
- @author-Tags oder Lizenzheader.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.

BERICHTE am Ende: Liste der dokumentierten Klassen, die Belegtabelle
(Property → Datei:Zeile), Javadoc-Warnungsstand, Testergebnis.
````

**Verifikation (Orchestrator):** Zwei Klassen selbst lesen — die
Auto-Configuration und den Registrar. Bei leerem Javadoc („Creates a bean.")
Nacharbeit anfordern. Die Belegtabelle stichprobenartig gegen den Code prüfen.

---

### M5-T4 — Trockenlauf der Integration

**Prompt für den Sonnet-Agenten:**

````
Du führst einen TROCKENLAUF durch. Arbeite ausschließlich in /tmp/m5-dryrun.
An den echten Repositories änderst du NICHTS.

VORBEREITUNG:
  rm -rf /tmp/m5-dryrun && mkdir -p /tmp/m5-dryrun
  cd /tmp/m5-dryrun
  git clone --depth 1 -b develop /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium morphium
Wenn ./morphium-jakarta-data im Klon fehlt (M2 noch nicht gemergt), kopiere es
zusätzlich aus dem Arbeitsbaum und notiere das als Abweichung.
  cp -r /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/spring-boot-morphium /tmp/m5-dryrun/morphium/spring-boot-morphium
  rm -rf /tmp/m5-dryrun/morphium/spring-boot-morphium/.git
  find /tmp/m5-dryrun/morphium/spring-boot-morphium -name target -exec rm -rf {} + 2>/dev/null

AUFGABE:
1. Registriere spring-boot-morphium in /tmp/m5-dryrun/morphium/pom.xml —
   im Profil `extensions` gemäß D3 Variante B. Setze auch die
   Versions-Property, die MIGRATION-NOTES.md als "M5-T5:" markiert hat.
2. Baue den vollständigen Reactor:
     cd /tmp/m5-dryrun/morphium
     mvn -B install -DskipTests > /tmp/m5-dryrun-install.log 2>&1
     mvn -B verify -pl spring-boot-morphium/spring-boot-morphium-autoconfigure,spring-boot-morphium/spring-boot-morphium-starter,spring-boot-morphium/spring-boot-morphium-test > /tmp/m5-dryrun-verify.log 2>&1
3. Prüfe die Invarianten I1–I5 aus D3 mit den dort genannten Befehlen.
   Besonders:
   - Kern-Dependency-Tree ohne org.springframework:
       mvn -q -pl morphium-core dependency:tree > /tmp/m5-core-tree.txt
       grep -i spring /tmp/m5-core-tree.txt      # MUSS leer sein
   - Kern-only-Build:
       mvn -B install -DskipTests -DskipExtensions > /tmp/m5-core-only.log 2>&1
       grep -ci spring /tmp/m5-core-only.log     # belegen, dass nichts aufgelöst wird
   - kein spring-boot-dependencies-BOM-Import im Parent-POM:
       grep -n -A3 'spring-boot-dependencies' pom.xml   # MUSS leer sein
4. Versionskonflikte: Vergleiche die effektiven Versionen der von beiden Seiten
   verwalteten Artefakte (slf4j, logback, jackson, junit, assertj) VOR und NACH
   der Integration:
     mvn -q -pl spring-boot-morphium/spring-boot-morphium-autoconfigure dependency:list > /tmp/m5-sb-deps.txt
   Nenne jede Abweichung und bewerte, ob sie riskant ist. Das ist der wichtigste
   Punkt dieses Trockenlaufs: Der Morphium-Parent managt slf4j 2.0.17 und
   logback 1.5.37; spring-boot-dependencies managt eigene Versionen. Wer gewinnt,
   hängt von der Reihenfolge im dependencyManagement ab. Belege das konkret.
5. Laufzeiten messen und notieren (für die CI-Planung in M6):
   Kern-only-Build, Reactor-Build, Spring-Modul-Tests.
6. Prüfe, ob die Spring-Tests Docker benötigen:
     docker info > /dev/null 2>&1 && echo "docker verfügbar" || echo "kein docker"
   Ergebnis und Auswirkung auf die Skipped-Zahl notieren.

Schreibe den Bericht nach
/Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium-jakarta-data/docs/plans/morphium-module-integration/reports/M5-T4-dryrun.md
mit: Invariantentabelle (I1–I5, PASS/FAIL, Belegausgabe), Versionskonflikttabelle
(Artefakt, vorher, nachher, Bewertung), Laufzeittabelle, dem ERPROBTEN
pom.xml-Patch als Codeblock zum Kopieren, und einer Kopierliste (welche Dateien
gehören ins Modulverzeichnis, welche nicht).

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Jede Änderung außerhalb von /tmp/m5-dryrun, ausgenommen die Berichtsdatei.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.

BERICHTE am Ende: Invariantentabelle, Versionskonflikte, Laufzeiten und alle
Stolpersteine, die M5-T5 kennen muss.
````

**Verifikation (Orchestrator):** Punkt 4 (Versionskonflikte) selbst nachvollziehen
— das ist das reale Risiko dieser Welle. `git status` in `morphium/`,
`spring-boot-morphium/` und `quarkus-morphium/` muss unverändert sein.

---

### Abschluss Teil A

Zustandsdokument `status/<datum>-M5A-spring-vorbereitung.md`, `STATE.md` und
Gesamtplan aktualisieren. Teil B nur starten, wenn M2 gemergt ist.

---

## Teil B — PR gegen Upstream

### Vorbedingung

```bash
cd morphium
git fetch origin
git log --oneline -1 origin/develop        # M2-Merge muss enthalten sein
git checkout -b pr/spring-boot-module origin/develop
```

---

### M5-T5 — Module übernehmen, Reactor, Doku, `release.sh`

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium.
Prüfe mit `git branch --show-current`, dass du auf `pr/spring-boot-module` bist,
und dass ./morphium-jakarta-data/ existiert. Wenn eines nicht zutrifft:
abbrechen und melden.

LIES ZUERST:
- docs/plans/morphium-module-integration/reports/M5-T4-dryrun.md
- docs/plans/morphium-module-integration/decisions/D3-reactor-strategie.md
- docs/plans/morphium-module-integration/decisions/D4-build-release-workflow.md
- /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/spring-boot-morphium/MIGRATION-NOTES.md

AUFGABE 1 — Modul übernehmen:
Kopiere aus /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/spring-boot-morphium
(Branch move-to-morphium) nach ./spring-boot-morphium/ genau die in
MIGRATION-NOTES.md als "kommt mit" ausgewiesenen Dateien.
NICHT kopieren: .git, target/ (in allen Modulen), docs-for-morphium/,
MIGRATION-NOTES.md, .github/, LICENSE, CONTRIBUTING.md, SECURITY.md,
CODE_OF_CONDUCT.md, CLAUDE.md, .claude/, und alles Weitere, das
MIGRATION-NOTES.md als "entfällt" führt.
Kontrolle:
  find spring-boot-morphium -name target -o -name "*.class" -o -name "*.jar" | head
MUSS leer sein.

AUFGABE 2 — ./pom.xml:
Wende den erprobten Patch aus dem M5-T4-Bericht an: <module> im Profil
`extensions` plus die Versions-Property. Ergänze den bestehenden
Kommentarblock um eine Zeile zu diesem Modul, im vorhandenen Stil.
Der spring-boot-dependencies-BOM-Import bleibt im Modul-POM (Invariante I4).

AUFGABE 3 — Dokumentation:
- ./docs/spring-boot.md aus spring-boot-morphium/docs-for-morphium/spring-boot.md
- ./mkdocs.yml: Eintrag im Abschnitt "Extensions" (Reihenfolge: Jakarta Data,
  Quarkus Extension falls vorhanden, Spring Boot)
- ./docs/index.md: Verweis ergänzen
- ./README.md und ./README.de.md: nur ergänzen, wenn eine Modulübersicht
  existiert; keine neue Struktur erfinden; README.de.md auf Deutsch mit
  korrekten Umlauten
- ./CHANGELOG.md: Eintrag im etablierten Format (lies zwei bestehende Einträge
  für Ton und Struktur). Inhalt: spring-boot-morphium als optionales Modul,
  drei Artefakte, Funktionsumfang, Lockstep-Versionierung mit Migrationshinweis
  (1.0.0 → Morphium-Version), Kern-Unabhängigkeit und -DskipExtensions, Herkunft
  aus Bardioc1977/spring-boot-morphium mit Archivierungshinweis.

AUFGABE 4 — ./release.sh:
Nimm auf: de.caluga:spring-boot-morphium-parent (POM-only) sowie die drei
Modulartefakte (jar + sources + javadoc). Halte dich an das Muster, das M2 und
M4 etabliert haben. Ein reines Testmodul — falls das test-Modul eines ist —
gehört NICHT ins Bundle; prüfe das und begründe den Ausschluss im Kommentar.
Prüfe für jedes Bundle-Artefakt, dass sources.jar und javadoc.jar tatsächlich
entstehen; fehlt ein Plugin im Modul-POM, ergänze es (Central lehnt sonst ab).
Führe release.sh NICHT aus. `bash -n release.sh` muss fehlerfrei sein.

ABSOLUT UNVERÄNDERT: morphium-core/**, poppydb/**, morphium-jakarta-data/**,
quarkus-morphium/** (falls vorhanden).

VERIFIKATION, die du selbst ausführen musst:
  git diff --stat origin/develop -- morphium-core poppydb morphium-jakarta-data quarkus-morphium
                                                     # MUSS leer sein
  mvn -B install -DskipTests > /tmp/m5-t5-install.log 2>&1
  mvn -B verify -pl spring-boot-morphium/spring-boot-morphium-autoconfigure,spring-boot-morphium/spring-boot-morphium-starter,spring-boot-morphium/spring-boot-morphium-test > /tmp/m5-t5-verify.log 2>&1
  mvn -B install -DskipTests -DskipExtensions > /tmp/m5-t5-core.log 2>&1
  mvn -q -pl morphium-core dependency:tree > /tmp/m5-t5-core-tree.txt
  grep -i spring /tmp/m5-t5-core-tree.txt            # MUSS leer sein
  python3 -c "import yaml; yaml.safe_load(open('mkdocs.yml'))"
  bash -n release.sh
  mvn -B -pl spring-boot-morphium/spring-boot-morphium-autoconfigure,spring-boot-morphium/spring-boot-morphium-starter package -DskipTests source:jar javadoc:jar > /tmp/m5-t5-artifacts.log 2>&1
Logs mit dem Read-Tool auswerten, nicht pipen. Die Volltestsuite führt M5-T6 aus.

Committe in vier Commits:
  "feat: add spring-boot-morphium as optional module"
  "build: register spring-boot-morphium in extensions profile"
  "docs: add spring boot documentation and changelog entry"
  "build: include spring-boot-morphium in release bundle"

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- AUSFÜHREN von release.sh, `mvn release:prepare`, `release:perform`, `git tag`.
- Änderungen an morphium-core/**, poppydb/**, morphium-jakarta-data/**,
  quarkus-morphium/**.
- Verschiebung des spring-boot-dependencies-BOM-Imports in den Parent.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.

BERICHTE am Ende: `git diff --stat` gegen origin/develop, die Volltext-Diffs von
pom.xml, mkdocs.yml und release.sh, den CHANGELOG-Eintrag, die Artefaktliste je
Modul und alle Verifikationsergebnisse.
````

---

### M5-T6 — Vollverifikation vor PR

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium
auf dem Branch `pr/spring-boot-module`.

AUFGABE: Abschließende Verifikation. Jede Prüfung als PASS/FAIL mit Belegausgabe.

A — Isolation des Kerns:
  git diff --stat origin/develop -- morphium-core poppydb morphium-jakarta-data quarkus-morphium
  mvn -q -pl morphium-core dependency:tree > /tmp/m5-core-tree.txt
  grep -iE 'spring|jakarta.data|io.quarkus' /tmp/m5-core-tree.txt   # keine Treffer
  mvn -B install -DskipTests -DskipExtensions > /tmp/m5-core-only.log 2>&1
  grep -n -A3 'spring-boot-dependencies' pom.xml                     # MUSS leer sein

B — Versionsauflösung (Kernrisiko dieser Welle):
  mvn -q -pl spring-boot-morphium/spring-boot-morphium-autoconfigure dependency:list > /tmp/m5-sb-deps.txt
Vergleiche die effektiven Versionen von slf4j-api, logback-classic, jackson-*,
junit-jupiter und assertj mit dem Stand aus dem M5-T4-Trockenlaufbericht.
Jede Abweichung als Zeile in einer Tabelle: Artefakt, erwartet, effektiv,
Bewertung. Eine unerwartete Abweichung ist ein FAIL, auch wenn die Tests grün
sind — sie würde sich später als Laufzeitfehler bei Nutzern zeigen.

C — Tests des Moduls:
  mvn -B verify -pl spring-boot-morphium/spring-boot-morphium-autoconfigure,spring-boot-morphium/spring-boot-morphium-starter,spring-boot-morphium/spring-boot-morphium-test > /tmp/m5-sb-verify.log 2>&1
Tests run / Failures / Errors / Skipped und Laufzeit je Modul. Vorher
`docker info` prüfen und notieren.

D — Volltestsuite:
  mvn -B verify > /tmp/m5-full-verify.log 2>&1
Dauert etwa 90 MINUTEN. Ausgabe NICHT pipen (Hook unterbindet das), Logdatei mit
dem Read-Tool auswerten. Je Modul: Tests, Failures, Errors, Skipped, plus
Reactor-Summary. Bei Fehlschlägen prüfen, ob sie auch ohne deine Änderungen
auftreten (bekannte Flakies existieren, u.a. Messaging und ChangeStream).
Testnamen nennen. Fremde Tests NICHT reparieren.

E — Central-Tauglichkeit:
  mvn -B -pl spring-boot-morphium/spring-boot-morphium-autoconfigure,spring-boot-morphium/spring-boot-morphium-starter package -DskipTests source:jar javadoc:jar > /tmp/m5-artifacts.log 2>&1
Für jedes zu publizierende Artefakt bestätigen: .jar, -sources.jar, -javadoc.jar
vorhanden; effektives POM enthält name, description, url, licenses, scm,
developers (per `mvn help:effective-pom` prüfen).

F — Commit-Hygiene und Diff-Umfang:
  git log origin/develop..HEAD --format='%H%n%an <%ae>%n%s%n%b%n---'
Keine Co-Authored-By-Zeile, kein "Generated with", keine E-Mail-Adresse im Body,
Conventional Commits. Verstöße melden, NICHT rebasen oder amenden.
  git diff --stat origin/develop
Erwartet: neue Dateien unter spring-boot-morphium/, Änderungen an ./pom.xml,
./mkdocs.yml, ./docs/index.md, ./docs/spring-boot.md, ./CHANGELOG.md,
./release.sh, ggf. READMEs. Alles Weitere ist ein Befund.
  find spring-boot-morphium -name target -o -name "*.class" -o -name "*.jar" | head
MUSS leer sein.

Schreibe den Bericht nach
docs/plans/morphium-module-integration/reports/M5-T6-verification.md

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Reparaturen an fremdem Code, um Tests grün zu bekommen.
- `git rebase`, `git commit --amend`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.

BERICHTE am Ende: die PASS/FAIL-Tabelle A–F, die Versionstabelle aus B und alle
Befunde.
````

**Paralyse-Sonderregel:** Wie M2-T4/M4-T4 — Logdateigröße als
Fortschrittsindikator, 30 Minuten ohne Wachstum = Eskalation.

---

### M5-T7 — PR-Text und Erstellung (Orchestrator)

### PR-Text-Aufbau

```markdown
Hi Stephan,

<Kontext: drittes und letztes Erweiterungsmodul. Warum Spring Boot: Morphium
soll in beiden großen Java-Ökosystemen nutzbar sein; die gemeinsame
Jakarta-Data-Basis macht das ohne Duplikate möglich.>

## Was dieser PR macht
<Drei Submodule, Reactor-Einbindung im extensions-Profil, Doku, CHANGELOG,
release.sh.>

## Optionalität
<Kern unverändert (git diff belegt es). Kern-Dependency-Tree ohne Spring.
-DskipExtensions baut Kern + PoppyDB. spring-boot-dependencies-BOM bleibt im
Modul-POM — mit Begründung.>

## Versionsauflösung
<Ergebnis von M5-T6/B: welche Versionen slf4j/logback/jackson effektiv gewinnen
und warum das unkritisch ist. Diesen Punkt aktiv ansprechen — ein erfahrener
Maintainer fragt genau danach.>

## Konventionen
<Ergebnis des M5-T1-Audits: Namensschema der Starter, @AutoConfiguration,
AutoConfiguration.imports, @ConditionalOnMissingBean,
spring-configuration-metadata.json. Verbleibende bewusste Abweichungen offen
benennen.>

## Verifikation
<Befehle und Ergebnisse aus M5-T6.>

## Damit ist die Modularisierung komplett
<Übersicht der drei Module und was in M6 noch folgt: gemeinsamer Release-Pfad,
optionaler CI-Workflow-PR, Archivierung der Alt-Repos, Showcase-Umstellung.>

## Offene Fragen an dich
<Spring-Boot-Version aktualisieren? Actuator-Integration erwünscht?>

Cheers!
```

### Ablauf

1. Text formulieren.
2. Im Chat vorlegen. **Auf Freigabe warten.**
3. Nach Freigabe:
   ```bash
   cd morphium
   git push fork pr/spring-boot-module
   gh pr create --repo sboesebeck/morphium \
     --base develop \
     --head Bardioc1977:pr/spring-boot-module \
     --title "feat: add spring-boot-morphium as optional module" \
     --body-file /tmp/pr-spring-body.md
   ```
4. PR-Nummer dokumentieren, Review verfolgen.

---

## Abschluss der Welle M5

1. Zustandsdokument `status/<datum>-M5-spring-boot.md` mit PR-Nummer und Status.
2. `STATE.md`, Gesamtplan, JF-Dokument aktualisieren.
3. M6 erst starten, wenn M2, M4 und M5 gemergt sind.

## Definition of Done

**Teil A**
- [ ] Konventionsbericht liegt vor, Namensschema geprüft
- [ ] POMs auf Modulstruktur umgestellt, Lockstep
- [ ] Javadoc vollständig und inhaltlich substanziell
- [ ] README, CHANGELOG, `docs-for-morphium/spring-boot.md`
- [ ] `MIGRATION-NOTES.md` mit Koordinatentabelle
- [ ] Trockenlauf: Invarianten I1–I5 bestanden, Versionskonflikte bewertet

**Teil B**
- [ ] Drei Submodule im Repo, Build grün
- [ ] Kernmodule und die anderen Erweiterungsmodule unverändert
- [ ] Kern-Dependency-Tree frei von Spring
- [ ] Kein `spring-boot-dependencies`-Import im Parent (I4)
- [ ] Versionsauflösung geprüft und im PR-Text erklärt
- [ ] Doku angebunden, CHANGELOG-Eintrag
- [ ] `release.sh` erweitert, Testmodul ausgeschlossen
- [ ] Volltestsuite gelaufen und ausgewertet
- [ ] **PR-Text vorgelegt und freigegeben**
- [ ] PR erstellt, Nummer dokumentiert
