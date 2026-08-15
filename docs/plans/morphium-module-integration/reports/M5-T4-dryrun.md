# M5-T4 — Trockenlauf der Integration: `spring-boot-morphium` als Morphium-Modul

| Feld | Wert |
|---|---|
| Repository | `/tmp/m5-dryrun/morphium` (Wegwerf-Klon) |
| Basis-Branch | `pr/quarkus-extension-module` (morphium-Repo) — bewusste Abweichung vom Wellenplan-Wortlaut (`develop`), da PR #266 (M2) noch nicht gemergt ist. Diese Basis enthält M2 (`morphium-jakarta-data`) und M4 (`quarkus-morphium`) bereits gestackt, exakt der Zustand, auf dem M5-T5 später aufbaut. |
| Quell-Repository für `spring-boot-morphium` | Branch `move-to-morphium` (M5-T1 bis M5-T3 bereits abgeschlossen: Modul-Umbenennung `morphium-spring-boot-*`, Property-Präfix `morphium.*`, `spring-boot.version` bereits 3.4.13, Javadoc, Doku) |
| Ausführung | Subagent (vollständige Analyse) + Orchestrator (Verifikation der kritischsten Zahlen, Berichtserstellung) |
| Datum | 2026-08-05 |

---

## 1. Kopierliste

Basis ist die bereits im `spring-boot-morphium`-Repo vorliegende `MIGRATION-NOTES.md` (aus M5-T2), die diese Liste vollständig enthält. Der rohe `cp -r`-Vorgang in diesem Trockenlauf hat **nicht** gefiltert — das ist für M5-T5 ein aktiver Auswahlschritt, kein Kopiervorgang "alles außer .git/target".

| Pfad | Kommt mit? | Begründung |
|---|---|---|
| `pom.xml` (Root) + `morphium-spring-boot-autoconfigure/`, `morphium-spring-boot-starter/`, `morphium-spring-boot-test/` (je `pom.xml` + `src/`) | **Ja** | Modul-Kernstruktur |
| `README.md` | **Ja** (bereits überarbeitet in M5-T3) | Modul-Dokumentation |
| `CHANGELOG.md` | **Ja**, Merge in Root-CHANGELOG.md in M5-T5 prüfen | Modul-spezifische Historie |
| `docs-for-morphium/spring-boot.md` | **Ja** — **Korrektur zur MIGRATION-NOTES.md**: diese behauptet fälschlich, es gäbe kein solches Verzeichnis; es existiert seit M5-T3 (nachträglich hinzugefügt, MIGRATION-NOTES.md wurde nicht nachgezogen). M5-T5 kopiert diese Datei nach `morphium/docs/spring-boot.md`, analog zu `quarkus-morphium/docs-for-morphium/quarkus-extension.md` |
| `LICENSE` | **Nein** | Reactor deckt Apache-2.0 repo-weit ab |
| `CODE_OF_CONDUCT.md` | **Nein** | Repo-weite Policy |
| `CONTRIBUTING.md` | **Nein** | Repo-weiter Workflow; modul-spezifischer Inhalt gehört ins README |
| `SECURITY.md` | **Nein** | Repo-weite Policy |
| `.github/workflows/`, `.github/ISSUE_TEMPLATE/`, `.github/PULL_REQUEST_TEMPLATE.md` | **Nein** | Eigenständiger CI-Workflow wird durch Reactor-CI ersetzt |
| `.gitignore` | **Nein** | Root-`.gitignore` deckt das ab |
| `target/` (alle Module) | **Nein** | Build-Artefakte |
| `.DS_Store` (mehrfach im Repo verstreut) | **Nein** | macOS-Metadaten — **im rohen `cp -r` dieses Trockenlaufs noch mehrfach vorhanden**, M5-T5 muss das explizit ausschließen, kein Automatismus vorhanden |

---

## 2. pom.xml-Patch (Copy-Paste-fertig für die echte Integration)

```diff
diff --git a/pom.xml b/pom.xml
index 6d5541cc5..87e95213a 100644
--- a/pom.xml
+++ b/pom.xml
@@ -87,6 +87,14 @@
          quarkus-morphium/pom.xml (see I4 and the dependencyManagement
          comment below); only the version property is centralized. -->
     <quarkus.version>3.32.3</quarkus.version>
+    <!-- Extension module property for spring-boot-morphium (M5-T5, moved
+         here from spring-boot-morphium/pom.xml per D1/B6, analogous to the
+         quarkus.version pattern above): a Spring Boot upgrade is a
+         single-line change here. The spring-boot-dependencies BOM *import*
+         itself stays in spring-boot-morphium/pom.xml (see I4 and the
+         dependencyManagement comment below); only the version property is
+         centralized. -->
+    <spring-boot.version>3.4.13</spring-boot.version>
   </properties>
   <build>
     <pluginManagement>
@@ -430,6 +438,7 @@
       <modules>
         <module>morphium-jakarta-data</module>
         <module>quarkus-morphium</module>
+        <module>spring-boot-morphium</module>
       </modules>
     </profile>
   </profiles>
```

Analog dazu wird `spring-boot-morphium/pom.xml` (Modul-Root) die lokale `spring-boot.version`-Property entfernt (jetzt vom Parent geerbt) — nur der `spring-boot-dependencies`-BOM-*Import* selbst bleibt dort (Invariante I4).

---

## 3. Verifikationsergebnisse

| # | Befehl | Ergebnis | Laufzeit |
|---|---|---|---|
| 1 | `mvn -B install -DskipTests` (voller 13-Modul-Reactor) | ✅ **BUILD SUCCESS** — alle 13 Module inkl. `Morphium Spring Boot – Parent/Autoconfigure/Starter/Test Support` | **27,0 s** |
| 2 | `mvn -B verify -pl morphium-spring-boot-autoconfigure,-starter,-test` | ✅ **BUILD SUCCESS**, **10/10 Tests grün** (2 `MorphiumAutoConfigurationTest` + 8 `MorphiumRepositoryProxyTest`) | **7,5 s** |
| 3 | `mvn -B install -DskipTests -DskipExtensions` | ✅ **BUILD SUCCESS**, Reactor-Summary zeigt nur 3 Module (Morphium Parent, Morphium, PoppyDB) | **14,6 s** |
| 4 | `mvn -q -pl morphium-core dependency:tree` + `grep -i spring` | ✅ 0 Treffer | — |
| 5 | `grep -n -A3 'spring-boot-dependencies' pom.xml` | ✅ nur der erklärende Kommentartext, kein echter `<dependency>`-Block | — |
| 6 | `mvn validate` (voller Reactor) | ✅ BUILD SUCCESS, keine zyklischen Abhängigkeiten | — |
| 7 | Docker/Testcontainers-Suche in `src/` aller drei Spring-Module | ✅ 0 Treffer für `Testcontainers\|testcontainers\|docker\|Docker` | — |

---

## 4. Invariantentabelle

| Invariante | Prüfung | Befehl | Ergebnis |
|---|---|---|---|
| I1 — keine Rückwärts-Dependency | `morphium-core/pom.xml` referenziert kein Spring/Quarkus/Jakarta-Data | `grep -E 'jakarta-data\|quarkus\|spring' morphium-core/pom.xml` | ✅ PASS (0 Treffer) |
| I2 — keine Framework-Imports im Kern | Kern-Dependency-Baum ohne Spring | `mvn -pl morphium-core dependency:tree` → `grep -i spring` | ✅ PASS (0 Treffer) |
| I3 — Kern eigenständig baubar | `mvn install -DskipTests -DskipExtensions` | Reactor-Summary zeigt nur 3 Module | ✅ PASS |
| I4 — kein fremder BOM-Import im Parent | Root-`pom.xml` importiert kein `spring-boot-dependencies` | `grep -n -A3 'spring-boot-dependencies' pom.xml` | ✅ PASS (nur Kommentartext) |
| I5 — keine zyklischen Modulabhängigkeiten | `mvn validate` über den vollen Reactor | Build Order zeigt alle 13 Module korrekt sortiert | ✅ PASS |

---

## 5. Versionskonflikte: `spring-boot-dependencies` 3.4.13 vs. Morphium-Kern

**Wichtige Korrektur gegenüber dem M5-T1-Bericht:** Der dortige Vergleich wurde gegen `spring-boot-dependencies 3.4.4` durchgeführt (Logback `1.5.18`). Da M5-T2 die Version bereits auf `3.4.13` angehoben hat, wurde in diesem Trockenlauf **erneut gegen die tatsächliche 3.4.13-POM** verifiziert (`curl` gegen `repo1.maven.org`, `logback.version=1.5.22` bestätigt) statt die veraltete Zahl weiterzutragen.

Belegt mit `mvn -pl morphium-spring-boot-autoconfigure dependency:tree -Dverbose` (nicht nur `dependency:list`, das reine Auflösungsreihenfolge ohne Managed-Herkunft zeigt):

| Artefakt | Morphium-Kern-Version | spring-boot-dependencies 3.4.13 | Effektiv gewonnen | Bewertung |
|---|---|---|---|---|
| `org.slf4j:slf4j-api` | `2.0.17` | `2.0.17` | identisch | kein Risiko |
| `ch.qos.logback:logback-core`/`-classic` | `1.5.37` | `1.5.22` | **Morphium-Kern (1.5.37)** — belegt: `"version managed from 1.5.22 ... omitted for duplicate"` | niedrig — neuere Version gewinnt, keine Downgrade-Gefahr |
| Jackson (`jackson-databind`/`-core`/`-annotations`) | nicht verwaltet | `2.18.5` | Spring (unbestritten) | kein Risiko — funktioniert exakt wie von Invariante I4 vorgesehen |
| `org.junit.jupiter:junit-jupiter-params` | `5.9.0` (explizit gepinnt in `morphium/pom.xml:226`) | `5.11.4` (über `junit-jupiter`-Aggregator) | **Morphium-Kern (5.9.0)** für `-params`, während `-api`/`-engine` bei `5.11.4` bleiben | **mittel — Versions-Split innerhalb derselben JUnit-Jupiter-Familie.** Tests laufen aktuell grün (10/10), aber ein inkonsistenter Classpath-Stand einer Testbibliothek ist eine Wartungsfalle, die bei künftigen JUnit-Upgrades zu schwer diagnostizierbaren Fehlern führen kann. |
| `org.assertj:assertj-core` | `3.27.7` | `3.26.3` | **Morphium-Kern (3.27.7)** | niedrig — neuere Version gewinnt |

**Gesamtbefund:** In allen fünf geprüften Kollisionsfällen gewinnt konsequent `morphium-parent`s `dependencyManagement`-Eintrag gegen den näher am Modul liegenden `spring-boot-dependencies`-BOM-Import. Das ist strukturell unauffällig (meist neuere Morphium-Kern-Versionen), mit **einer Ausnahme**: der JUnit-Jupiter-Versionsskew ist ein eigenständiger, vom BOM-Konflikt unabhängiger Befund, der behoben werden sollte, bevor eine spätere Welle (M6) sich darauf verlässt, dass Test-Dependencies durchgängig konsistent sind.

**Empfehlung für M5-T5 oder M6:** `morphium/pom.xml:226` (`junit-jupiter-params:5.9.0`) auf `5.11.4` anheben, um den Skew zu beseitigen — das ist eine Ein-Zeilen-Änderung im Kern-POM, betrifft aber `morphium-core`s eigene Testsuite und sollte separat verifiziert werden (nicht Teil dieses Trockenlaufs).

---

## 6. Laufzeittabelle (für eine spätere CI-Planungswelle)

| Lauf | Laufzeit | Umfang |
|---|---|---|
| Voller Reactor, `-DskipTests` | 27,0 s | 13 Module (Kern + 3 Extension-Module-Gruppen) |
| Spring-Module `verify` (autoconfigure+starter+test) | 7,5 s | 10 Tests, kein Docker |
| Kern-only, `-DskipTests -DskipExtensions` | 14,6 s | 3 Module |

Im Vergleich zu M3-T6/M4 (Quarkus-Extension: 242 Integrationstests, ~2:22 min mit Docker) ist `spring-boot-morphium` deutlich leichtgewichtiger — keine Docker-Abhängigkeit, kleinere Testsuite (10 statt 242 Tests). Für die CI-Matrix aus D3/Begleitmaßnahme 4 bedeutet das: ein `spring-boot-morphium`-Testjob ist günstig und schnell, im Gegensatz zum bereits dokumentierten teureren `quarkus-morphium/integration-tests`-Job.

---

## 7. Abschluss

`/tmp/m5-dryrun` wurde **nicht aufgeräumt** und steht für eine Folgewelle zur Verfügung. Alle Logdateien liegen unter `/tmp/m5-dryrun-*.log`, `/tmp/m5-core-*.log`, `/tmp/m5-sb-deps.txt`.

**Kein Repository unter `/Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium` oder `.../spring-boot-morphium` wurde verändert** — die gesamte Verifikation fand ausschließlich in der Wegwerf-Kopie statt.

**Offene Punkte für M5-T5/M6:**
1. **JUnit-Jupiter-Versionsskew** (`junit-jupiter-params:5.9.0` vs. `-api`/`-engine:5.11.4`) — Empfehlung: `morphium/pom.xml` auf `5.11.4` anheben, separat verifizieren.
2. **`MIGRATION-NOTES.md` im `spring-boot-morphium`-Repo ist bezüglich `docs-for-morphium/` veraltet** — sie behauptet, das Verzeichnis existiere nicht; tatsächlich wurde es in M5-T3 nachträglich angelegt. M5-T5 muss die Kopierliste aus Abschnitt 1 dieses Berichts nutzen, nicht blind der (veralteten) MIGRATION-NOTES.md folgen.
3. **Der rohe `cp -r`-Kopiervorgang entspricht nicht der finalen Kopierliste** — M5-T5 muss aktiv filtern (siehe Abschnitt 1), nicht pauschal kopieren; `.DS_Store`-Dateien sind mehrfach im Quellrepo verstreut und müssen explizit ausgeschlossen werden.
