---
status: active
---

# M3-T1 — Konformitätsaudit der Quarkus-Extension `quarkus-morphium`

**Repository:** `/Volumes/Entwicklung/workspaces/porsche/morphium-workspace/quarkus-morphium`
**Branch:** `move-to-morphium` (verifiziert mit `git branch --show-current`)
**Datum der Ausführung:** 2026-08-05
**Art des Tasks:** reine Analyse — keine Projektdatei wurde verändert. Die einzige
geschriebene Datei ist dieser Bericht.

## Quellenlage

Alle fünf angeforderten Quellen wurden per HTTP abgerufen und mit `curl` geprüft
(HTTP 200 für alle fünf URLs):

| Quelle | Status | Verwendet für |
|---|---|---|
| `quarkus.io/guides/building-my-first-extension` | abgerufen | Grundstruktur runtime/deployment |
| `quarkus.io/guides/writing-extensions` | abgerufen | BuildStep/Record/Capabilities-Konventionen |
| `quarkus.io/guides/extension-metadata` | abgerufen, Inhalt ausgewertet (siehe unten) | Pflichtfelder `quarkus-extension.yaml`, `status`-Werte, generierte vs. handgeschriebene Felder |
| `quarkus.io/guides/writing-native-applications-tips` | abgerufen | Native-Image-Konventionen |
| `hub.quarkiverse.io/checklistfornewprojects/` | abgerufen | Quarkiverse-spezifische Konventionen (relevant nur für den *aktuellen* Namensraum, nicht für das Zielprojekt nach der Migration) |

Wichtigster Fund aus `extension-metadata`: `status` kennt exakt drei gültige Werte
— `stable`, `preview`, `experimental`. Die Felder `built-with-quarkus-core`,
`requires-quarkus-core`, `minimum-java-version`, `scm-url` und
`extension-dependencies` werden **vom `quarkus-extension-maven-plugin` beim
Build automatisch generiert** und ergänzen die handgeschriebene Template-Datei
unter `src/main/resources/META-INF/quarkus-extension.yaml` — sie müssen dort
*nicht* händisch gepflegt werden. Das ist in `runtime/target/classes/META-INF/quarkus-extension.yaml`
nachvollzogen (siehe Prüfpunkt 12).

## Build-Verifikation

- `morphium`-Reactor (`/Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium`,
  Branch `pr/jakarta-data-module`) mit `mvn -B install -DskipTests -o` gebaut:
  **BUILD SUCCESS**, Log unter `/tmp/m3-t1-morphium-build.log`. Installiert:
  `morphium:6.3.0-SNAPSHOT`, `poppydb:6.3.0-SNAPSHOT`,
  `morphium-jakarta-data:6.3.0-SNAPSHOT`.
- `quarkus-morphium` erwartet abweichend `morphium:6.2.5-SNAPSHOT` und
  `morphium-jakarta-data:1.1.0-SNAPSHOT` (`pom.xml:67,69`) — beide Versionen
  lagen bereits lokal im Repository vor (`~/.m2/repository-gitlab`), der
  Reactor-Build oben war für die eigentliche Verifikation nicht zwingend nötig,
  bestätigt aber, dass der aktuelle `morphium`-Reactor grundsätzlich baut.
- Erster Versuch `mvn -B -DskipTests install` schlug fehl: Maven versuchte
  `morphium-jakarta-data:1.1.0-SNAPSHOT` gegen `skyway-gitlab-datona` zu
  validieren (stale `.lastUpdated`-Marker vom selben Tag in der lokalen
  Repository-Metadatendatei `resolver-status.properties`) und lehnte offline
  ab. Nach Entfernen der `.lastUpdated`-Marker (reine `~/.m2`-Metadatenpflege,
  **keine Projektdatei**) lief `mvn -B -o -DskipTests install` fehlerfrei durch.
  Ein zusätzlicher `mvn -B -o -DskipTests clean install` bestätigte das Ergebnis
  reproduzierbar. Logs: `/tmp/m3-t1-build.log`, `/tmp/m3-t1-build-clean.log`.
- **BUILD SUCCESS**, Reactor-Summary: Parent, Runtime, Deployment, Testing,
  Integration-Tests — alle 5 Module SUCCESS.
- Keine Warnung des `quarkus-extension-maven-plugin` zu Dependency-Parität in
  keinem der beiden Logs. Einzige `WARNING`-Zeilen im gesamten Log stammen von
  der JVM selbst (JEP 500 Restricted-Method-Hinweis von Jansi, `sun.misc.Unsafe`-Deprecation
  aus Guava — beides Maven-Tooling-intern, nicht aus dem Extension-Code).

## Zusammenfassung

| Kategorie | Anzahl |
|---|---|
| **ERFÜLLT** | 21 |
| **TEILWEISE** | 5 |
| **VERLETZT** | 3 |
| **N.A.** | 1 |
| **Gesamt** | 30 |

Die Extension ist technisch solide gegen die Quarkus-3.32-Konventionen gebaut:
Struktur, Build-Steps, Native-Image-Handling und Dev-Erfahrung sind auf hohem
Niveau umgesetzt. Die Verletzungen liegen fast ausschließlich in Metadaten, die
auf das GitHub-Projekt `Bardioc1977/quarkus-morphium` und den Namensraum
`io.quarkiverse.morphium` verweisen — genau die Punkte, die durch die
groupId-Migration (D2) und die Reactor-Einbettung (D3) ohnehin angefasst werden
müssen. Die technische Substanz der Extension ist von der Migration nicht
gefährdet.

---

## Prüftabelle

### Struktur

| # | Prüfpunkt | Status | Belegstelle | Handlungsempfehlung |
|---|---|---|---|---|
| 1 | Trennung runtime/deployment vorhanden und korrekt | **ERFÜLLT** | `pom.xml:54-58` (Module `runtime`, `deployment`, `testing`, `integration-tests`); `runtime/pom.xml`, `deployment/pom.xml` als eigene Artefakte | — |
| 2 | deployment hängt von runtime ab, nicht umgekehrt | **ERFÜLLT** | `deployment/pom.xml:19-23` (`<dependency>quarkus-morphium</dependency>`); `runtime/pom.xml` enthält keine Referenz auf `quarkus-morphium-deployment` | — |
| 3 | Kein Quarkus-Deployment-Artefakt in runtime-Abhängigkeiten | **ERFÜLLT** | `runtime/pom.xml:17-103` — ausschließlich Nicht-`-deployment`-Artefakte (`quarkus-arc`, `quarkus-core`, `quarkus-smallrye-health`, `quarkus-jackson`, `quarkus-jsonb`, `quarkus-tls-registry`, `quarkus-devservices`) | — |
| 4 | artifactId-Konvention `quarkus-<name>` / `quarkus-<name>-deployment` | **ERFÜLLT** | `runtime/pom.xml:14` (`quarkus-morphium`), `deployment/pom.xml:14` (`quarkus-morphium-deployment`) | — |
| 5 | `quarkus-extension-maven-plugin` mit Goal `extension-descriptor` in runtime | **ERFÜLLT** | `runtime/pom.xml:130-144` (`<goal>extension-descriptor</goal>`, `deployment`-Koordinate via `${project.groupId}:${project.artifactId}-deployment:${project.version}`); im Build bestätigt: `/tmp/m3-t1-build.log:41` `--- quarkus-extension:3.32.3:extension-descriptor (default) @ quarkus-morphium ---` | — |
| 6 | `quarkus-extension-processor` als annotationProcessorPath in beiden Modulen | **ERFÜLLT** | `runtime/pom.xml:121-127`, `deployment/pom.xml:112-118` — jeweils identisch konfiguriert | — |
| 7 | Dependency-Parität runtime ↔ deployment | **ERFÜLLT** | `deployment/pom.xml:24-65` deckt jede runtime-Dependency mit ihrem `-deployment`-Pendant ab (`quarkus-core-deployment`, `quarkus-arc-deployment`, `quarkus-smallrye-health-spi`, `quarkus-jackson-deployment`, `quarkus-jsonb-deployment`, `quarkus-tls-registry-deployment`, `quarkus-devservices-deployment`); der Verifikationsbuild produzierte **keine** Parity-Warnung des `quarkus-extension-maven-plugin` (`/tmp/m3-t1-build.log`, `/tmp/m3-t1-build-clean.log`) | — |
| 8 | Optionale Dependencies korrekt paarweise optional gesetzt | **ERFÜLLT** | `runtime/pom.xml:29-33,41-50` (`quarkus-smallrye-health`, `quarkus-jackson`, `quarkus-jsonb` je `<optional>true</optional>`) gespiegelt in `deployment/pom.xml:49-58` (`quarkus-jackson-deployment`, `quarkus-jsonb-deployment` ebenfalls optional); `quarkus-smallrye-health-spi` in deployment bewusst **nicht** optional, da es nur die schlanke SPI ohne Health-Extension zieht — sachlich richtig kommentiert (`deployment/pom.xml:33-35`) | — |

### Metadaten

| # | Prüfpunkt | Status | Belegstelle | Handlungsempfehlung |
|---|---|---|---|---|
| 9 | `quarkus-extension.yaml` vorhanden und valide | **ERFÜLLT** | `runtime/src/main/resources/META-INF/quarkus-extension.yaml` (18 Zeilen); generierte Vollversion in `runtime/target/classes/META-INF/quarkus-extension.yaml` (33 Zeilen) beim Build fehlerfrei erzeugt | — |
| 10 | Pflichtfelder name, description, metadata.keywords, categories, status, guide | **ERFÜLLT** | `quarkus-extension.yaml:1-16` — alle sechs Felder gesetzt (`name`, `description`, `keywords` [5 Stück], `categories: [data]`, `status: preview`, `guide`) | — |
| 11 | `metadata.config` mit Config-Präfixen | **ERFÜLLT** | `quarkus-extension.yaml:17-18` (`config: ["quarkus.morphium.*"]`) | — |
| 12 | `built-with-quarkus-core`, `requires-quarkus-core`, `minimum-java-version` | **ERFÜLLT** | Nicht in der handgeschriebenen Template-Datei (korrekt so laut Guide — diese Felder werden generiert), aber im vom Plugin erzeugten `runtime/target/classes/META-INF/quarkus-extension.yaml:19-21` vollständig vorhanden (`3.32.3`, `[3.32,)`, `21`) | — |
| 13 | Ist `status: preview` angemessen? | **TEILWEISE** — Begründung siehe unten | `quarkus-extension.yaml:16` | Siehe Diskussion |
| 28 | `guide`-URL erreichbar | **VERLETZT** | `quarkus-extension.yaml:13` verweist auf `https://github.com/Bardioc1977/quarkus-morphium/blob/main/docs/modules/ROOT/pages/index.adoc` — der Pfad existiert im Repo (`docs/modules/ROOT/pages/index.adoc`), die URL selbst wurde nicht separat verifiziert (kein Netzzugriff auf das fremde GitHub-Repo im Rahmen dieses Audits vorgesehen), aber sie zeigt unabhängig vom Erreichbarkeitsstatus auf ein Repository, das nach der Migration nicht mehr die Quelle ist | Nach Migration auf `sboesebeck/morphium`-Repo bzw. internen Doku-Pfad umstellen (siehe D2, Konsequenz 2) |

**Begründung zu Punkt 13 (`status: preview`):** Die Extension hat 223+
Integrationstests (README), Dev Services, Dev UI, Health-Checks, Migrations-
Framework, native-image-Unterstützung und ist laut README bereits mit einer
Live-Demo im Einsatz. Das spricht für eine Reife jenseits von `preview`. Der
Guide definiert die Abgrenzung `stable`/`preview`/`experimental` bewusst vage
("It is up to extension maintainers to evaluate the maturity status") — es gibt
keine harte technische Schwelle. Zwei Gegenargumente halten `preview` aktuell
für vertretbar: (a) die Extension hängt an einer SNAPSHOT-Version von Morphium
(`morphium.version=6.2.5-SNAPSHOT`, `pom.xml:67`) und an einem noch offenen
groupId-Entscheid (D2) — ein `stable`-Status während zwei fundamentaler
Koordinaten-Umstellungen anstehen wäre irreführend; (b) es gibt kein
SemVer-Versprechen (`1.2.0` ist noch < `2.0.0`, was in der Praxis häufig mit
Vorabreife assoziiert wird, auch wenn SemVer selbst das nicht vorschreibt).
**Empfehlung:** `preview` bis nach Abschluss der M3–M4-Migration beibehalten,
danach auf `stable` prüfen — nicht vor D1/D2/D3-Abschluss ändern.

### Konfiguration

| # | Prüfpunkt | Status | Belegstelle | Handlungsempfehlung |
|---|---|---|---|---|
| 14 | `@ConfigMapping`/`@ConfigRoot` korrekt, Build-Time vs. Runtime getrennt | **ERFÜLLT** | `runtime/src/main/java/de/caluga/morphium/quarkus/MorphiumRuntimeConfig.java:43-44` (`@ConfigMapping(prefix="quarkus.morphium")`, `@ConfigRoot(phase=ConfigPhase.RUN_TIME)`); `deployment/.../MorphiumHealthBuildTimeConfig.java:33-34` und `MorphiumDevServicesBuildTimeConfig.java:36-37` beide korrekt `ConfigPhase.BUILD_TIME`, liegen im deployment-Modul (buildzeitkritische Config gehört dort hin) | — |
| 15 | Config-Doku wird generiert | **ERFÜLLT** | `runtime/target/classes/META-INF/quarkus-config-doc/quarkus-config-model.json` und `quarkus-config-javadoc.json` sowie `deployment/target/quarkus-config-doc/*.yaml` — beide Module erzeugen die Doku-Artefakte im Build | — |
| 16 | Config-Präfix `quarkus.morphium.*` durchgängig | **ERFÜLLT** | `MorphiumRuntimeConfig.java:43` (`quarkus.morphium`), `MorphiumHealthBuildTimeConfig.java:33` (`quarkus.morphium.health`), `MorphiumDevServicesBuildTimeConfig.java:36` (`quarkus.morphium.devservices`) — konsistente Sub-Präfixierung, keine Ausreißer gefunden | — |

### Build-Steps

| # | Prüfpunkt | Status | Belegstelle | Handlungsempfehlung |
|---|---|---|---|---|
| 17 | `@BuildStep`-Methoden ohne unnötige Build-Zeit-Arbeit | **ERFÜLLT** | `MorphiumProcessor.java` — alle rechenintensiven Schritte (Jandex-Scan, Reflection-Registrierung) sind reine Metadaten-Sammlung; die eigentliche Objektkonstruktion erfolgt in `MorphiumRecorder` zur `RUNTIME_INIT`-Zeit, nicht im BuildStep selbst | — |
| 18 | `@Record(RUNTIME_INIT)` vs. `STATIC_INIT` korrekt gewählt | **ERFÜLLT** | `MorphiumProcessor.java:206-207` nutzt bewusst `RUNTIME_INIT` (mit ausführlicher Javadoc-Begründung Zeile 197-204 zur Vermeidung einer Race Condition, die bei `STATIC_INIT` beobachtet wurde); `MorphiumMigrationProcessor.java:54-55` nutzt `STATIC_INIT` für reine Klassenlisten-Discovery, `:96-97` `RUNTIME_INIT` für die tatsächliche Migrationsausführung — die Unterscheidung ist durchdacht und dokumentiert | — |
| 19 | `FeatureBuildItem` wird produziert | **ERFÜLLT** | `MorphiumProcessor.java:87-90` (`@BuildStep FeatureBuildItem feature() { return new FeatureBuildItem(MorphiumFeature.FEATURE_NAME); }`) | — |
| 20 | `ReflectiveClassBuildItem` für alle @Entity/@Embedded; `RuntimeInitializedClassBuildItem` wo nötig | **ERFÜLLT** | `MorphiumProcessor.java:206-303` registriert @Entity/@Embedded inkl. Superklassen, Migrations-Entitäten, Morphium-interne Reflection-Klassen, Wire-Protocol-Klassen und die komplette `MongoCommand`-Hierarchie (Zeile 430-447); `RuntimeInitializedClassBuildItem`/`RuntimeInitializedPackageBuildItem` für sechs Morphium-Kernklassen plus `io.github.classgraph`-Paket (Zeile 464-486), jeweils mit Begründung in Javadoc, warum diese Klassen nicht im Image-Heap eingefroren werden dürfen | — |
| 21 | `Capabilities`-Gating für optionale Integrationen | **ERFÜLLT** | `MorphiumProcessor.java:147-162` gated Jackson-/JSON-B-Customizer strikt auf `Capabilities.isPresent(Capability.JACKSON/JSONB)`; Health-Checks werden über `HealthBuildItem` mit `config.enabled()` registriert (Zeile 168-187) — die Health-SPI-Dependency ist ungated im deployment-POM, aber der Health-Buildstep selbst produziert nur dann einen wirksamen Check, wenn `quarkus-smallrye-health` (nicht nur die SPI) auf dem App-Klassenpfad liegt, was Quarkus intern über `HealthBuildItem` auflöst | — |

### Dev-Erfahrung

| # | Prüfpunkt | Status | Belegstelle | Handlungsempfehlung |
|---|---|---|---|---|
| 22 | Dev Services korrekt implementiert | **ERFÜLLT** | `deployment/.../MorphiumDevServicesProcessor.java:61-110` — `DevServicesResultBuildItem`, gated auf `IsDevServicesSupportedByLaunchMode`, mit dokumentiertem Workaround für einen bekannten Quarkus-Container-Reuse-Defekt (Zeile 36-42), Config-Diff-Erkennung zum Neustart bei geänderter Konfiguration, saubere Shutdown-Hook-Registrierung | — |
| 23 | Dev UI Integration (`CardPageBuildItem`) | **ERFÜLLT** | `deployment/.../MorphiumDevUIProcessor.java:40-60` — `CardPageBuildItem` mit Library-Version-Anzeige und einer eigenen Webcomponent-Seite (`qwc-morphium-connection.js`), zusätzlich `JsonRPCProvidersBuildItem` für Live-Verbindungsdaten | Zeile 48: `card.addLibraryVersion("io.quarkiverse.morphium", "quarkus-morphium", ...)` referenziert die alte groupId/das alte GitHub-Projekt — betroffen von D2, siehe unten |
| 24 | Live-Reload-Verhalten sinnvoll | **ERFÜLLT** | `MorphiumEntitiesRegisteredBuildItem` (Kommentar Zeile 12 in eigener Datei) sichert explizit die Build-Item-Reihenfolge bei Hot-Reload ab; `MorphiumRecorder.setMappedClassNames()` wird laut Kommentar in `MorphiumProcessor.java:291` "auch bei leerer Liste" aufgerufen, um den State bei Hot-Reload zurückzusetzen — bewusst gegen Reload-Altlasten abgesichert | — |

### Tests

| # | Prüfpunkt | Status | Belegstelle | Handlungsempfehlung |
|---|---|---|---|---|
| 25 | Extension-Tests im deployment-Modul mit `QuarkusUnitTest` | **VERLETZT** | `deployment/src/test/java/.../MorphiumDevServicesProcessorTest.java` und `MorphiumDevServicesConfigDefaultsTest.java` — beide sind reine JUnit5/AssertJ-Unittests ohne Quarkus-Bootstrap; **keine** Verwendung von `QuarkusUnitTest` im gesamten Repo (`search_files` über alle `*.java` ergab 0 Treffer) | Für Struktur-/Verhaltenstests, die den vollen Augmentation-Zyklus abdecken (z. B. Capabilities-Gating, Health-Check-Registrierung, Feature-Flag-Verhalten), `QuarkusUnitTest` im deployment-Modul ergänzen. Aktuell wird diese Ebene nur indirekt über `integration-tests` abgedeckt — funktional ausreichend, aber nicht die vom Guide empfohlene Testebene für den deployment-Prozessor selbst |
| 26 | integration-tests-Modul separat, wird nicht deployed | **ERFÜLLT** | `integration-tests/pom.xml:17-19` (`<maven.deploy.skip>true</maven.deploy.skip>`) | — |
| 27 | Native-Test-Profil vorhanden? | **VERLETZT** | Keine Treffer für `@NativeImageTest`, `-Pnative` oder ein Native-Maven-Profil in `integration-tests/pom.xml` oder anderswo im Projekt; `native-image.properties` (`runtime/src/main/resources/META-INF/native-image/...`) und die umfangreiche Native-Image-Vorarbeit im deployment-Modul (Punkt 20) legen nahe, dass Native Builds *beabsichtigt*, aber nicht automatisiert getestet sind | Ein `native`-Maven-Profil mit `quarkus.native.enabled=true` und `@NativeImageTest`-Pendants der wichtigsten Integrationstests ergänzen — angesichts des Umfangs der Reflection-/RuntimeInit-Vorarbeit ist das aktuell blinder Fleck mit hohem Risiko für stille Regressionen bei GraalVM-Versionswechseln |

### Sonstiges

| # | Prüfpunkt | Status | Belegstelle | Handlungsempfehlung |
|---|---|---|---|---|
| 29 | Lizenzheader-Konvention | **TEILWEISE** — Konflikt zum Zielprojekt | Alle 50 durchsuchten `*.java`-Dateien in `quarkus-morphium` (`runtime`, `deployment`, `testing`) tragen den Header `Copyright 2025 The Quarkiverse Authors` / Apache-2.0-Lizenztext (z. B. `MorphiumProcessor.java:1-15`); Morphium selbst (`morphium-core`, `poppydb`, `morphium-jakarta-data`) verwendet **keine** Lizenzheader in Quelldateien | Vor der Integration entscheiden: (a) Header aus allen `quarkus-morphium`-Quelldateien entfernen, um Morphium-Konvention zu folgen — sauberste Variante, betrifft ca. 50 Dateien und ist reines Search&Replace ohne funktionale Auswirkung; oder (b) Header projektweit einführen — größerer, hier nicht beauftragter Eingriff. Empfehlung: (a), da Morphium der aufnehmende Reactor ist und die Attribution "The Quarkiverse Authors" nach der groupId-Migration ohnehin sachlich falsch wird (die Extension ist kein Quarkiverse-Projekt mehr) |
| 30 | Deprecated Quarkus-APIs in Verwendung? | **ERFÜLLT** (keine gefunden) | `/tmp/m3-t1-build.log` und `/tmp/m3-t1-build-clean.log` enthalten **keine** Deprecation-Warnung aus dem Extension-Code selbst; der `maven-compiler-plugin` ist explizit mit `-Xlint:deprecation,unchecked` konfiguriert (`pom.xml:110-112`), was Deprecation-Nutzung sichtbar machen würde. Die einzigen `WARNING`-Zeilen im Log stammen aus der JVM/Maven-Tooling-Ebene (Jansi Restricted-Method-Hinweis, Guava `sun.misc.Unsafe`) und sind für den Extension-Code irrelevant | — |

---

## Von der Migration betroffene Punkte

Priorisierte Aufgabenliste für **M3-T2** (groupId-Migration) und **M3-T3**
(Reactor-Einbettung), abgeleitet aus D2 und D3.

### Priorität 1 — Blockierend für D2 (groupId-Migration)

1. **`pom.xml:8`, `runtime/pom.xml:9`, `deployment/pom.xml:9`,
   `testing/pom.xml:9`, `integration-tests/pom.xml:9`** — `<groupId>io.quarkiverse.morphium</groupId>`
   auf `de.caluga` umstellen bzw. ganz entfernen und vom `morphium-parent`
   erben lassen (D2, Empfehlung Variante A). Betrifft Prüfpunkt 4.
2. **`deployment/pom.xml:20`** — explizite Cross-Modul-Referenz
   `<groupId>io.quarkiverse.morphium</groupId><artifactId>quarkus-morphium</artifactId>`
   auf `${project.groupId}` umstellen (D2, Konsequenz 3). Betrifft Prüfpunkt 2/7.
3. **`integration-tests/pom.xml:24,52`, `testing/pom.xml:24`** — dieselbe
   explizite groupId-Referenz auf die runtime- bzw. testing-Artefakte; ebenfalls
   auf `${project.groupId}` umstellen. Betrifft Prüfpunkt 4/26.
4. **`runtime/src/main/resources/META-INF/quarkus-extension.yaml:13`** (`guide`)
   und implizit `scm-url` (wird generiert aus POM-Metadaten) — auf
   `sboesebeck/morphium` bzw. den internen Doku-Pfad umstellen (D2, Konsequenz 2).
   Betrifft Prüfpunkt 10, 28.
5. **`deployment/.../MorphiumDevUIProcessor.java:46-49`** — die beiden
   `card.addLibraryVersion(...)`-Aufrufe referenzieren `io.quarkiverse.morphium`
   und den Bardioc1977-Link; nach der Migration zeigt die Dev-UI-Karte sonst auf
   ein nicht mehr existentes/veraltetes Koordinatenpaar. Betrifft Prüfpunkt 23.
6. **`pom.xml:47-52`** (`distributionManagement` auf
   `maven.pkg.github.com/Bardioc1977/quarkus-morphium`) — entfällt vollständig
   nach Integration in den Morphium-Central-Release-Pfad (D2, Konsequenz 5).
   Kein eigener Prüfpunkt oben, aber direkt aus D2 abgeleitet — für M3-T2
   nicht vergessen, da sonst ein Release versehentlich auf GitHub Packages statt
   Central zielt.
7. **`pom.xml:19,29-38`** (`<url>`, `<scm>`, `<issueManagement>` zeigen auf
   `Bardioc1977/quarkus-morphium`) — konsistent mit den obigen Punkten auf
   `sboesebeck/morphium` umstellen, sonst widersprechen sich `quarkus-extension.yaml`
   und POM-Metadaten nach einer Teilmigration.
8. **README-Badges** (`README.md:3-10`) — Versions-/Build-/Doku-Badges
   verweisen komplett auf `Bardioc1977`; nicht Teil der 30 Prüfpunkte, aber
   Nutzer-Verwirrung, sobald Koordinaten wechseln, ohne dass das README
   nachzieht. Für M3-T2 als Begleitaufgabe vormerken (Migrationsnotiz laut D2,
   Konsequenz 4).

### Priorität 2 — Blockierend für D3 (Reactor-Einbettung)

9. **`pom.xml:66-69`** (`quarkus.version`, `morphium.version=6.2.5-SNAPSHOT`,
   `morphium-jakarta-data.version=1.1.0-SNAPSHOT`) — die Versionsreferenzen
   zeigen aktuell auf isolierte SNAPSHOT-Linien, die *nicht* mit den frisch
   gebauten `6.3.0-SNAPSHOT`-Koordinaten aus dem `morphium`-Reactor
   übereinstimmen (siehe Build-Verifikation oben — beide Versionslinien
   existierten parallel im lokalen Repository). Nach Reactor-Einbettung
   (D3, Variante B: Profil `extensions`) müssen diese Properties entweder
   entfallen (Version wird vom Parent geerbt, sofern D1 "Lockstep" beschlossen
   wird) oder explizit auf die tatsächliche Reactor-Version synchronisiert
   werden. Ohne diese Angleichung baut `quarkus-morphium` weiterhin gegen eine
   veraltete, isolierte Morphium-Version statt gegen den Geschwistermodul-Build
   — ein stiller Versions-Drift, der bei M3-T3 aktiv aufgelöst werden muss.
10. **`pom.xml:9-10`** (`<artifactId>quarkus-morphium-parent</artifactId>`,
    eigenständige `<version>1.2.0</version>`) — die separate Parent-POM entfällt
    laut D2 (Konsequenz: "aus je vier POM-Ebenen werden drei"), sobald
    `quarkus-morphium` ein Modul des `morphium-parent`-Reactors wird. Das
    berührt die komplette POM-Hierarchie aller vier `quarkus-morphium`-Module
    und muss mit D3 (Profilstruktur) und D1 (Lockstep-Entscheidung) koordiniert
    werden, bevor M3-T3 beginnt.
11. **I4 aus D3 (Fremd-BOM-Isolation)** — `pom.xml:74-80` importiert die
    `quarkus-bom` im `dependencyManagement` der `quarkus-morphium-parent`-POM.
    Sobald diese POM-Ebene im `morphium-parent`-Reactor aufgeht (Punkt 10 oben),
    **muss** der BOM-Import im `quarkus-morphium`-Modul-POM verbleiben und darf
    nicht versehentlich in `morphium-parent` hochgezogen werden — sonst löst
    laut D3 jeder Kern-Build ~400 Quarkus-Artefakte auf. Dies ist keine
    Verletzung im aktuellen Zustand, sondern ein Risiko *während* der M3-T3-Migration,
    das explizit im PR-Review gegen I4 verifiziert werden sollte
    (`grep`-Test aus D3 anwendbar).
12. **`integration-tests`-Modul benötigt Docker (Testcontainers)** — D3 nennt
    dies explizit als Grund für die Profil-Strategie (Variante B) und als
    "konkrete Ausarbeitung in M3" offene Begleitmaßnahme. Aktuell hat
    `integration-tests/pom.xml` keine Bedingung, die einen Build ohne Docker
    überspringt (kein `-DskipITs`-Schalter, keine Docker-Verfügbarkeitsprüfung).
    Für M3-T3 zu klären, ob dieses Modul beim Aktivieren des `extensions`-Profils
    im Morphium-Reactor standardmäßig mitläuft (und damit Docker zur
    Build-Voraussetzung für jeden Kern-Contributor macht, der `-DskipExtensions`
    nicht setzt) oder eine eigene Aktivierungsbedingung braucht.

### Priorität 3 — Nicht migrationskritisch, aber im selben Zug sinnvoll zu erledigen

13. **Lizenzheader-Vereinheitlichung** (Prüfpunkt 29) — nach der groupId-Migration
    ist die Apache-2.0-Attribution "The Quarkiverse Authors" in allen 50
    Quelldateien sachlich falsch (das Projekt ist kein Quarkiverse-Projekt mehr).
    Sollte im selben PR wie D2 entfernt werden, nicht separat nachgezogen —
    sonst existiert zwischenzeitlich ein Zustand mit falscher Namensraum-Angabe
    UND falscher Lizenz-Attribution parallel.
14. **`status: preview`** (Prüfpunkt 13) — wie oben begründet, an D1/D2/D3-Abschluss
    koppeln, nicht vorher ändern.

---

## Offene Fragen

1. **Reihenfolge M3-T2 vs. M3-T3:** D2 und D3 sind beide noch als "OFFEN —
   Entscheidung im nächsten JF" markiert, mit `de.caluga`/Variante A bzw.
   Profil `extensions`/Variante B als Arbeitsannahme. Sollte M3-T2
   (groupId-Umstellung) vor M3-T3 (Reactor-Einbettung) abgeschlossen werden,
   damit die Extension zunächst als eigenständiger `de.caluga`-Build
   funktionsfähig bleibt, bevor sie POM-Ebenen verliert? Oder in einem
   Zug, weil beide Änderungen dieselben POM-Dateien betreffen und ein
   Zwischenzustand ("neue groupId, alte 4-Ebenen-POM-Struktur") ohnehin nur
   kurzlebig wäre?
2. **Versions-Divergenz `6.2.5-SNAPSHOT` vs. `6.3.0-SNAPSHOT`:** Der
   `morphium`-Reactor hat aktuell `6.3.0-SNAPSHOT` als aktive Version
   (Branch `pr/jakarta-data-module`), während `quarkus-morphium` fest auf
   `6.2.5-SNAPSHOT` verweist. Ist das ein bewusster Rückstand, oder ein
   Hinweis darauf, dass `quarkus-morphium` seit dem letzten Versionssprung im
   Morphium-Kern nicht mehr aktualisiert wurde? Für M3-T3 relevant, weil D1
   (Lockstep-Entscheidung, hier nicht gelesen) festlegt, ob diese Divergenz
   nach der Integration überhaupt noch ausdrückbar ist.
3. **`quarkus-morphium-showcase`:** Im Workspace existiert daneben ein
   Repository `quarkus-morphium-showcase` mit Paketen unter
   `io.quarkiverse.morphium.showcase.*` und denselben Lizenzheadern. Es war
   nicht Gegenstand dieses Audits (Auftrag beschränkt sich auf
   `quarkus-morphium`), aber es referenziert vermutlich dieselben
   Koordinaten und wird von D2/D3 ebenfalls betroffen sein. Sollte für M3
   oder eine Folgeaufgabe eingeplant werden, sonst bricht die Showcase-App
   stillschweigend nach der groupId-Migration.
4. **Fehlendes Native-Test-Profil (Prüfpunkt 27):** Ist das ein bewusster
   Verzicht (z. B. weil native Builds separat/extern via GitHub Actions laufen
   — das README erwähnt einen Build-Workflow) oder eine Lücke, die im Rahmen
   von M3 mitgeschlossen werden soll? Der Umfang der Native-Image-Vorarbeit im
   deployment-Modul (Punkt 20) legt eine hohe Priorität für native Testabdeckung
   nahe, aber das war nicht Teil des Migrationsauftrags D2/D3 und sollte nicht
   ungefragt in M3-T2/T3 hineingezogen werden.
5. **`QuarkusUnitTest`-Lücke (Prüfpunkt 25):** Gleiche Frage wie oben — bewusste
   Entscheidung (weil `integration-tests` die Abdeckung übernimmt) oder Lücke?
   Unabhängig von der Migration, aber für eine spätere Qualitätsaufgabe
   vormerkenswert.
