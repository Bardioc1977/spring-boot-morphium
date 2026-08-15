---
status: active
---

# M5-T1 — Bestandsaufnahme + Spring-Boot-Konventionsprüfung: `spring-boot-morphium`

Repository: `/Volumes/Entwicklung/workspaces/porsche/morphium-workspace/spring-boot-morphium`,
Branch `move-to-morphium` (HEAD `86b8adf`, 7 Commits, kein Tag, keine
Central-Veröffentlichung — `origin` zeigt auf
`git@github.com:Bardioc1977/spring-boot-morphium.git`).

Diese Aufgabe ist rein analytisch. Keine Datei außer diesem Bericht wurde
geändert. Alle Behauptungen sind mit Datei:Zeile belegt.

---

## 1. Bestandsaufnahme

### 1.1 Modulstruktur

Vier POMs, drei Submodule (`spring-boot-morphium/pom.xml:23-27`):

| Modul | artifactId | Packaging | Parent |
|---|---|---|---|
| Wurzel | `spring-boot-morphium-parent` (`pom.xml:8`) | `pom` (`pom.xml:10`) | keiner (`pom.xml:2-10` — kein `<parent>`-Element) |
| autoconfigure | `spring-boot-morphium-autoconfigure` (`spring-boot-morphium-autoconfigure/pom.xml:13`) | jar (Default) | `spring-boot-morphium-parent` (`.../pom.xml:7-11`) |
| starter | `spring-boot-morphium-starter` (`spring-boot-morphium-starter/pom.xml:13`) | jar (Default) | `spring-boot-morphium-parent` (`.../pom.xml:7-11`) |
| test | `spring-boot-morphium-test` (`spring-boot-morphium-test/pom.xml:13`) | jar (Default) | `spring-boot-morphium-parent` (`.../pom.xml:7-11`) |

Koordinaten: `groupId de.caluga`, `version 1.0.0-SNAPSHOT` überall
(`pom.xml:7-9`, jeweils via `<parent>` in den drei Submodulen geerbt).

Versions-Properties (`pom.xml:29-38`):
`spring-boot.version=3.4.4`, `morphium.version=6.2.4`,
`morphium-jakarta-data.version=1.1.0`, `jakarta.data.version=1.0.0`,
Java-Release 21 (`pom.xml:30-32`).

`dependencyManagement` im Wurzel-POM (`pom.xml:40-70`):
- `spring-boot-dependencies` als **BOM-Import** (`pom.xml:42-48`, `<scope>import</scope>`, `<type>pom</type>`) — **kein** `<parent>spring-boot-starter-parent</parent>`. Bestätigt: kein `<parent>`-Element im Wurzel-POM überhaupt (`pom.xml:2-10`).
- `de.caluga:morphium:${morphium.version}` (`pom.xml:49-53`)
- `de.caluga:morphium-jakarta-data:${morphium-jakarta-data.version}` (`pom.xml:54-58`)
- `jakarta.data:jakarta.data-api:${jakarta.data.version}` (`pom.xml:59-63`)
- `io.github.classgraph:classgraph:4.8.184` (`pom.xml:64-68`)

Kein `<licenses>`/`<url>`/`<description>` auf Submodul-Ebene nötig (wird geerbt),
aber siehe Prüfpunkt 12 unten für Details.

### 1.2 Java-Klassen (13 Produktions-, 5 Test-Klassen)

**autoconfigure-Modul, `src/main/java/de/caluga/morphium/spring/autoconfigure/`:**

| Klasse | Zweck |
|---|---|
| `MorphiumAutoConfiguration.java` | Erstellt den `Morphium`-Bean aus `MorphiumProperties`, inkl. Entity-Pre-Scan (ClassGraph) und Verbindungs-Retry-Logik. |
| `MorphiumHealthAutoConfiguration.java` | Registriert einen `HealthIndicator` für Actuator, der den Morphium-Verbindungsstatus meldet. |
| `MorphiumProperties.java` | `@ConfigurationProperties`-Bindung für `spring.morphium.*` (Hosts, Datenbank, Auth, Cache, SSL, Index-Check). |
| `MorphiumRepositoryRegistrar.java` | `ImportBeanDefinitionRegistrar`, scannt Pakete nach `@Repository`-Interfaces und registriert je eine `MorphiumRepositoryFactoryBean`. |
| `MorphiumRepositoryFactoryBean.java` | Spring `FactoryBean`, erzeugt pro Repository-Interface einen JDK-Dynamic-Proxy. |
| `MorphiumRepositoryInvocationHandler.java` | `InvocationHandler` des Proxies; dispatcht CRUD-/Query-Aufrufe an die geteilte `morphium-jakarta-data`-Query-Engine (`QueryMethodBridge`, `JdqlMethodBridge`, `FindMethodBridge`). |
| `MorphiumTransactionAspect.java` | AspectJ-`@Around`-Advice, der Methoden/Klassen mit `@MorphiumTransactional` in eine Morphium-Transaktion einbettet. |
| `MorphiumTransactional.java` | Marker-Annotation für transaktionale Methoden/Klassen. |
| `EnableMorphiumRepositories.java` | Aktivierungs-Annotation, importiert `MorphiumRepositoryRegistrar` via `@Import`. |

**autoconfigure-Modul, `src/test/java/...` (Testcode, nicht geändert):**

| Klasse | Zweck |
|---|---|
| `MorphiumAutoConfigurationTest.java` | Prüft, dass der `Morphium`-Bean korrekt mit InMemDriver erzeugt wird. |
| `MorphiumRepositoryProxyTest.java` | Prüft CRUD/Query-Verhalten des generierten Repository-Proxies End-to-End. |
| `TestApplication.java` | Minimale `@SpringBootApplication` als Testkontext. |
| `TestEntity.java` | Test-Entity mit `@Entity`/`@Id`. |
| `TestEntityRepository.java` | Test-Repository-Interface (`@Repository extends MorphiumRepository<TestEntity, MorphiumId>`). |

**test-Modul, `src/main/java/de/caluga/morphium/spring/test/`:**

| Klasse | Zweck |
|---|---|
| `MorphiumTest.java` | Composite-Testannotation (`@SpringBootTest` + `@TestPropertySource`), konfiguriert InMemDriver ohne echte MongoDB. |

**starter-Modul:** keine Java-Klassen (bestätigt via Dateisuche, `search_files` liefert 0 Treffer unter `spring-boot-morphium-starter/src`).

### 1.3 Genutzte Spring-Boot-Mechanismen

| Mechanismus | Fundstelle |
|---|---|
| `@AutoConfiguration` (Boot 3) | `MorphiumAutoConfiguration.java:20`, `MorphiumHealthAutoConfiguration.java:12` |
| `@ConfigurationProperties` | `MorphiumProperties.java:7` (Prefix `spring.morphium`) |
| `@EnableConfigurationProperties` | `MorphiumAutoConfiguration.java:22` |
| `ImportBeanDefinitionRegistrar` | `MorphiumRepositoryRegistrar.java:29` (`implements ImportBeanDefinitionRegistrar`) |
| AOP (AspectJ) | `MorphiumTransactionAspect.java:11-15` (`@Aspect`, `@Component`, `@Around`) |
| Actuator | `MorphiumHealthAutoConfiguration.java:5-19` (`HealthIndicator`, `@ConditionalOnEnabledHealthIndicator("morphium")`) |
| `FactoryBean` (klassisches Spring, kein Boot-spezifisches Feature) | `MorphiumRepositoryFactoryBean.java:21` |
| `@Import` | `EnableMorphiumRepositories.java:20` |

### 1.4 Ressourcendateien für Auto-Configuration

- `spring-boot-morphium-autoconfigure/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` — **Boot-3-Mechanismus**, korrekt verwendet. Inhalt (2 Zeilen):
  ```
  de.caluga.morphium.spring.autoconfigure.MorphiumAutoConfiguration
  de.caluga.morphium.spring.autoconfigure.MorphiumHealthAutoConfiguration
  ```
- **Kein** `META-INF/spring.factories` im Repository (`search_files` über das gesamte Repo nach `spring.factories` liefert 0 Treffer) — kein Boot-2-Relikt vorhanden. Positiver Befund.
- Kein `META-INF/spring-configuration-metadata.json` und kein
  `META-INF/spring-autoconfigure-metadata.properties` im Quellbaum vorhanden
  (`search_files` nach `spring-configuration-metadata` und
  `configuration-processor` im gesamten Repo: 0 Treffer). Auch nicht im
  bereits gebauten `target/classes` (nur die `.imports`-Datei liegt dort,
  siehe `spring-boot-morphium-autoconfigure/target/classes/META-INF/spring/`).
  Näheres zur Konsequenz siehe Prüfpunkt 8 unten.

### 1.5 Testabdeckung

- `MorphiumAutoConfigurationTest.java` und `MorphiumRepositoryProxyTest.java` — beide `@SpringBootTest(classes = TestApplication.class)` mit `@ActiveProfiles("test")`.
- Testkonfiguration `application-test.properties:1-3`:
  ```
  spring.morphium.database=test
  spring.morphium.driver-name=InMemDriver
  spring.morphium.hosts=localhost:27017
  ```
  → Tests laufen mit Morphiums **InMemDriver**, keine echte MongoDB-Instanz nötig.
- **Kein Docker-Bedarf.** Kein Testcontainers-Import, kein `docker`-Aufruf im
  gesamten Repo außer einem Kommentar
  (`CONTRIBUTING.md:53`: „All tests (uses InMemDriver, no Docker needed)"),
  der genau das bestätigt. `search_files` nach `Testcontainers|testcontainers|docker|Docker` im gesamten Repo liefert nur diesen einen Treffer.
- Das `spring-boot-morphium-test`-Modul selbst enthält keine eigenen Tests
  (nur die Produktionsklasse `MorphiumTest.java`, ein Hilfsmittel für
  *Nutzer* des Starters).
- CI (`.github/workflows/build.yml:38-39`) führt `mvn -B verify` aus,
  ebenfalls ohne Docker-Schritt.

---

## 2. Konventionsprüfung (Spring Boot Starter)

Offizielle Doku via `curl` geladen und geprüft (HTTP 200 bestätigt):
- `https://docs.spring.io/spring-boot/reference/features/developing-auto-configuration.html` (2707 Zeilen HTML, textextrahiert nach `/tmp/autoconf.txt`)
- `https://docs.spring.io/spring-boot/reference/using/build-systems.html` (2657 Zeilen HTML, textextrahiert nach `/tmp/buildsystems.txt`)

Zusätzlich für Prüfpunkt 11 die Maven-Central-Metadaten von
`spring-boot-dependencies` via `repo1.maven.org/maven2/.../maven-metadata.xml`
abgerufen (290 Versionen, aktuellste GA-Reihen `3.4.x`, `3.5.x`, `4.0.x`, `4.1.0`).

| Nr | Anforderung | Status | Belegdatei:Zeile | Handlungsvorschlag |
|---|---|---|---|---|
| 1 | Drittanbieter-Starter heißen `<projekt>-spring-boot-starter`, nicht `spring-boot-starter-<projekt>` | **ABWEICHUNG** | Doku: `/tmp/buildsystems.txt:447-449` „third party starters should not start with spring-boot ... typically starts with the name of the project ... would typically be named thirdpartyproject-spring-boot-starter"; ebenso `/tmp/autoconf.txt:662-669` „Naming ... Do not start your module names with spring-boot". Code: `spring-boot-morphium-starter/pom.xml:13` (`spring-boot-morphium-starter`), `spring-boot-morphium-autoconfigure/pom.xml:13` (`spring-boot-morphium-autoconfigure`), `pom.xml:8` (`spring-boot-morphium-parent`) | Alle drei artifactIds umbenennen zu `morphium-spring-boot-starter`, `morphium-spring-boot-autoconfigure`, `morphium-spring-boot-parent` (Muster: Projektname zuerst). Bestätigt den Vorabbefund des Orchestrators; die Empfehlung „jetzt umbenennen, solange kein Central-Release existiert" ist durch die Doku-Quelle direkt gedeckt (kein Nutzer betroffen, siehe Historie: 7 Commits, kein Tag, kein Release). |
| 2 | Aufteilung autoconfigure (Logik) / starter (praktisch leer, nur Dependencies) | **OK** | `spring-boot-morphium-starter/pom.xml:17-35` enthält ausschließlich `<dependencies>`, keine Java-Klassen (bestätigt durch leere Dateisuche unter `spring-boot-morphium-starter/src`). Auto-Config-Klassen liegen vollständig in `spring-boot-morphium-autoconfigure/src/main/java/...` (9 Klassen, siehe 1.2). Deckt sich mit Doku: `/tmp/autoconf.txt:757-759` „The starter is really an empty jar." | Keiner — Struktur ist bereits konventionsgemäß. |
| 3 | `@AutoConfiguration` (Boot 3) statt `@Configuration` | **OK** | `MorphiumAutoConfiguration.java:20`, `MorphiumHealthAutoConfiguration.java:12` beide `@AutoConfiguration` (Letztere sogar mit `after = MorphiumAutoConfiguration.class`, Zeile 12). Doku bestätigt dies als das erwartete Muster: `/tmp/autoconf.txt:378-379`. | Keiner. |
| 4 | Registrierung über `AutoConfiguration.imports`, nicht `spring.factories` | **OK** | `spring-boot-morphium-autoconfigure/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports:1-2` listet beide Auto-Config-Klassen. Kein `spring.factories` im Repo (0 Treffer, siehe 1.4). Doku: `/tmp/autoconf.txt:383-388`. | Keiner. |
| 5 | `@ConditionalOnMissingBean` bei allen Bean-Definitionen | **ABWEICHUNG (Teilbefund)** | `MorphiumAutoConfiguration.java:27-28` hat `@ConditionalOnMissingBean` auf dem einzigen `@Bean`-Methode (`morphium(...)`). Der `HealthIndicator`-Bean in `MorphiumHealthAutoConfiguration.java:17-19` hat dagegen **kein** `@ConditionalOnMissingBean`, sondern nur `@ConditionalOnEnabledHealthIndicator("morphium")` (Zeile 18) — ein Actuator-spezifisches Conditional, das die Überschreibbarkeit durch einen eigenen `HealthIndicator`-Bean nicht abdeckt (Spring würde bei zwei `HealthIndicator`-Beans mit demselben Actuator-Namen ggf. kollidieren, statt dass der Nutzer-Bean automatisch gewinnt). | `@ConditionalOnMissingBean(name = "morphiumHealthIndicator")` (oder ohne `name`, je nach gewünschtem Override-Verhalten) zu Zeile 17 ergänzen — geringfügige, aber laut Doku (`/tmp/autoconf.txt:381-382, 412`) erwartete Absicherung. Kein Blocker für die Integration, aber sollte im PR-Text als bekannte kleine Lücke erwähnt werden. |
| 6 | `@ConditionalOnClass` / `@ConditionalOnProperty` bei optionalen Abhängigkeiten | **OK** | `MorphiumAutoConfiguration.java:21` (`@ConditionalOnClass(Morphium.class)`), `MorphiumHealthAutoConfiguration.java:9,13-14` (`@ConditionalOnClass(HealthIndicator.class)` + `@ConditionalOnBean(Morphium.class)`), `MorphiumTransactionAspect.java:13-14` (`@ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")` + `@ConditionalOnBean(Morphium.class)`). Alle drei optionalen Abhängigkeiten (AOP, Actuator) sind in `spring-boot-morphium-autoconfigure/pom.xml:21-30` bewusst `<optional>true</optional>` deklariert (`spring-boot-starter-aop` Zeile 22-25, `spring-boot-actuator-autoconfigure` Zeile 26-30). | Keiner. Kein `@ConditionalOnProperty` im Code gefunden — nicht nötig, da es keine „Feature ein/aus"-Property gibt (Actuator/AOP-Absicherung läuft komplett über `@ConditionalOnClass`/`@ConditionalOnBean`). |
| 7 | `@ConfigurationProperties(prefix = "spring.morphium")`, Prefix identisch in Doku und Code | **OK** | Code: `MorphiumProperties.java:7` (`@ConfigurationProperties(prefix = "spring.morphium")`). Doku (README): `README.md:20,63-66,159-179` — alle Beispiele und die Konfigurationsreferenz-Tabelle nutzen konsequent `spring.morphium.*`. Testkonfiguration: `application-test.properties:1-3` nutzt ebenfalls `spring.morphium.*`. `MorphiumTest.java:23-27` (Composite-Annotation) setzt dieselben Property-Keys. | Keiner. Anmerkung: der Prefix `spring.*` verletzt allerdings die separate Empfehlung „do not include your keys in the namespaces that Spring Boot uses (such as server, management, spring...)" (`/tmp/autoconf.txt:672`) — siehe eigene Zeile unten, da das eine andere Dimension der Prüfung ist als „Doku==Code". |
| 7b | (Zusatzbefund, nicht in der Nummerierung des Auftrags, aber aus derselben Doku-Quelle) Konfigurationsschlüssel sollen NICHT im Namensraum `spring` liegen | **ABWEICHUNG** | Doku: `/tmp/autoconf.txt:671-674` „do not include your keys in the namespaces that Spring Boot uses (such as server, management, spring, and so on)... prefix all your keys with a namespace that you own (for example acme)". Code: `MorphiumProperties.java:7` nutzt exakt `spring.morphium`, also den reservierten `spring`-Namensraum. | Formal eine Abweichung von der Namensraum-Empfehlung. In der Praxis nutzen aber auch andere Drittanbieter-Starter teils `spring.*`-Subnamen (uneinheitliche Community-Praxis) und eine Umbenennung des Prefixes wäre ein **Breaking Change** für jede zukünftige Nutzer-Config. Da noch kein Central-Release existiert, wäre jetzt der günstigste Zeitpunkt für einen Wechsel zu z. B. `morphium.*` — das ist aber eine Produktentscheidung mit Doku-/Code-Auswirkung auf sehr viele Stellen (README, `MorphiumTest`, alle Beispiele) und sollte separat vom Orchestrator entschieden werden, nicht in dieser Analyseaufgabe vorentschieden werden. |
| 8 | `META-INF/spring-configuration-metadata.json` wird erzeugt; `spring-boot-configuration-processor` als Dependency | **ABWEICHUNG** | Kein `spring-boot-configuration-processor` in `spring-boot-morphium-autoconfigure/pom.xml:16-54` (vollständige `<dependencies>`-Liste enthält nur `spring-boot-autoconfigure`, `spring-boot-starter-aop`, `spring-boot-actuator-autoconfigure`, `de.caluga:morphium`, `classgraph`, `morphium-jakarta-data`, `jakarta.data-api`, `spring-boot-starter-test`). `search_files` im gesamten Repo nach `spring-boot-configuration-processor` und `spring-configuration-metadata`: 0 Treffer. Kein `META-INF/spring-configuration-metadata.json` im bereits gebauten `target/classes` (nur `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` vorhanden). Doku fordert das ausdrücklich: `/tmp/autoconf.txt:724-726` „Make sure to trigger meta-data generation so that IDE assistance is available for your keys as well." | `spring-boot-configuration-processor` als `<optional>true</optional>`-Dependency (Annotation-Processor-Scope) zu `spring-boot-morphium-autoconfigure/pom.xml` ergänzen. Ohne dieses Artefakt bekommen Nutzer der `spring.morphium.*`-Properties keine IDE-Autovervollständigung/-Validierung in `application.properties`/`.yml`. Sollte Teil der M5-T2-POM-Konvertierung werden. |
| 9 | Kein `spring-boot-starter-parent` als Parent-POM; `spring-boot-dependencies` als BOM-Import | **OK** | `pom.xml:2-10` — Wurzel-POM hat **kein** `<parent>`-Element überhaupt (weder `spring-boot-starter-parent` noch sonst einer). `pom.xml:42-48` importiert `spring-boot-dependencies` korrekt via `<scope>import</scope>`/`<type>pom</type>` im `<dependencyManagement>`. Deckt sich exakt mit der vom Orchestrator vorab bestätigten Feststellung und mit der Doku-Empfehlung selbst (`/tmp/buildsystems.txt:379-382`: BOM-Nutzung als Standardweg, wenn kein Parent-POM verwendet wird). | Keiner in Bezug auf das Muster selbst. Für die Integration in den Morphium-Reactor (Prüfpunkt in Aufgabe 3) muss der BOM-Import beim Umhängen auf `morphium-parent` als `<parent>` im **Modul**-POM verbleiben, nicht in `morphium-parent` wandern — siehe Integrationsplan unten. |
| 10 | Actuator-Integration mit `HealthIndicator`, `@ConditionalOnClass`-abgesichert | **OK** | `MorphiumHealthAutoConfiguration.java:15-19` — `HealthIndicator`-Bean, Klasse mit `@ConditionalOnClass(HealthIndicator.class)` (Zeile 9) und `@ConditionalOnBean(Morphium.class)` (Zeile 14) abgesichert; `spring-boot-actuator-autoconfigure` ist `<optional>true</optional>` in `spring-boot-morphium-autoconfigure/pom.xml:26-30`. Actuator bleibt damit optional — ohne Actuator im Classpath backt Spring Boot automatisch ab. README dokumentiert das Verhalten konsistent: `README.md:200-220`. | Keiner strukturell. Siehe aber Prüfpunkt 5 oben (`@ConditionalOnMissingBean` fehlt am konkreten `@Bean`). |
| 11 | Aktualität der Spring-Boot-Version | **ABWEICHUNG (veraltet, kein Upgrade-Zwang in dieser Welle)** | Code: `pom.xml:34` (`spring-boot.version=3.4.4`). Maven-Central-Metadaten (`repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies/maven-metadata.xml`, abgerufen per curl, 290 Versionen gelistet): aktuellste `3.4.x`-Version ist `3.4.13`; aktuellste GA-Linie insgesamt ist `4.1.0` (mit einer aktiven `4.0.x`-Reihe bis `4.0.7` und `3.5.x` bis `3.5.16`). `3.4.4` liegt also mindestens 9 Patch-Releases hinter der eigenen Minor-Linie und zwei Major-Versionen hinter der aktuellen Spring-Boot-Hauptlinie zurück. | Kein Upgrade auf `4.x` in dieser Welle empfehlen — das wäre ein separates, potenziell breaking Unterfangen (Spring Boot 4 setzt i.d.R. Baseline-Anhebungen bei Java/Framework voraus) und liegt außerhalb des Migrations-Scopes. Ein Patch-Upgrade innerhalb der 3.4.x-Linie (`3.4.4` → `3.4.13`) ist dagegen risikoarm (reine Bugfix-Releases derselben Minor-Version) und sollte im Rahmen der M5-Integration mitgenommen werden, um nicht mit einer bereits 9 Patches alten Abhängigkeit in den Reactor zu starten. Ein Sprung auf `3.5.x` oder `4.x` ist eine eigene Entscheidung, keine dieser Aufgabe. |
| 12 | Metadaten für Maven Central (name, description, url, licenses, scm, developers) | **ABWEICHUNG (unvollständig)** | Vorhanden im Wurzel-POM: `<name>` (`pom.xml:12`), `<description>` (`pom.xml:13`), `<url>` (`pom.xml:14`), `<licenses>` (`pom.xml:16-21`, Apache 2.0). **Fehlend** im Wurzel-POM: `<scm>` und `<developers>` (vollständige Durchsicht von `pom.xml:1-92` bestätigt: keine dieser beiden Sektionen existiert). Zum Vergleich: `morphium/pom.xml:20-32` hat beide Sektionen vorbildlich ausgefüllt (`<scm>` Zeile 20-25, `<developers>` Zeile 26-32). Submodul-POMs haben je nur ein eigenes `<name>` (`spring-boot-morphium-autoconfigure/pom.xml:14`, `spring-boot-morphium-starter/pom.xml:14-15` inkl. eigener `<description>`, `spring-boot-morphium-test/pom.xml:14`) und erben Lizenz/URL vom Parent — das ist für Maven-Central-Zwecke ausreichend (nur das Wurzel-POM/BOM-Root braucht die vollständigen Metadaten je Publikationseinheit; bei Integration in den Morphium-Reactor entfällt diese Sorge sowieso, da das Modul dann unter `morphium-parent`s Metadaten released wird — siehe `morphium/pom.xml:8-32`). | Da die Integration in den Morphium-Reactor bevorsteht (M5-T2 hängt das Modul unter `morphium-parent` als `<parent>`), werden `scm`/`developers` künftig ohnehin vom `morphium-parent` (`morphium/pom.xml:20-32`) geerbt. Kein eigenständiger Central-Release dieses Repos ist geplant — die fehlenden Felder sind daher **kein Blocker** für die Integration, sondern nur relevant, falls das Repo jemals eigenständig auf Central veröffentlicht werden sollte (aktuell nicht der Fall, kein Tag, keine `distributionManagement`-Sektion im POM gefunden). |

---

## 3. Integrationsplan (Aufgabe 3)

### 3.1 Dateien: kommt mit vs. entfällt (analog zu `quarkus-morphium/MIGRATION-NOTES.md`)

Angewendet wird dieselbe Regel wie in `quarkus-morphium/MIGRATION-NOTES.md:12-22`
(„Rule of thumb"): ein Repo-Wurzel-File wandert nur mit, wenn es
**modul-spezifisch** ist und der Ziel-Repository-Root (`morphium/`) kein
äquivalentes Repo-weites File dafür schon besitzt.

| Datei/Verzeichnis | Kommt mit? | Begründung |
|---|---|---|
| `pom.xml` (Wurzel), `spring-boot-morphium-autoconfigure/`, `spring-boot-morphium-starter/`, `spring-boot-morphium-test/` (jeweils `pom.xml` + `src/`) | **Kommt mit** | Modul-Kernstruktur, entspricht `runtime`/`deployment`/... bei `quarkus-morphium/MIGRATION-NOTES.md:52-54`. Umbenennung der artifactIds gemäß Prüfpunkt 1 wird in M5-T2 behandelt, nicht hier. |
| `README.md` | **Kommt mit (überarbeitet in M5-T3)** | Modul-Dokumentation, analog `quarkus-morphium/MIGRATION-NOTES.md:48`. |
| `CHANGELOG.md` | **Kommt mit, Merge später prüfen** | Analog `quarkus-morphium/MIGRATION-NOTES.md:47` — modul-spezifische Historie, potenziell später in die Haupt-`CHANGELOG.md` zu integrieren (Entscheidung liegt bei M5-T5/späterer Welle). |
| `LICENSE` | **Entfällt** | Morphium-Reactor deckt Apache-2.0 bereits repo-weit über `morphium/pom.xml:13-19` ab. Analog `quarkus-morphium/MIGRATION-NOTES.md:28`. |
| `CODE_OF_CONDUCT.md` | **Entfällt** | Repo-weite Policy, analog `quarkus-morphium/MIGRATION-NOTES.md:29`. |
| `CONTRIBUTING.md` | **Entfällt** | Repo-weiter Contribution-Workflow, analog `quarkus-morphium/MIGRATION-NOTES.md:30`. Modul-spezifischer Inhalt (`CONTRIBUTING.md:38-44`: „mvn test uses InMemDriver, no Docker needed", Build-Reihenfolge morphium→morphium-jakarta-data→dieses Modul) ist wertvoll und sollte ins Modul-README wandern, nicht als eigene Datei kopiert werden. |
| `SECURITY.md` | **Entfällt** | Repo-weite Policy, analog `quarkus-morphium/MIGRATION-NOTES.md:31`. |
| `.github/workflows/build.yml` | **Entfällt** | Baut dieses Repo als eigenständiges Maven-Projekt inkl. `git clone` von morphium/morphium-jakarta-data (`build.yml:26-36`) — nach der Integration baut die Haupt-CI des Reactors das Modul mit; ein eigenständiger Klon-Workflow wäre nach der Integration aktiv falsch. Analog `quarkus-morphium/MIGRATION-NOTES.md:34`. |
| `.github/ISSUE_TEMPLATE/*`, `.github/PULL_REQUEST_TEMPLATE.md` | **Entfällt** | Nur auf Repo-Root wirksam (GitHub liest `.github/` nicht aus Unterverzeichnissen); analog `quarkus-morphium/MIGRATION-NOTES.md:37-38`. |
| `.gitignore` | **Entfällt** | Root-`.gitignore` des Morphium-Reactors deckt `target/`, `.idea/` etc. bereits ab; analog `quarkus-morphium/MIGRATION-NOTES.md:43`. |
| `target/` (in allen drei Submodulen) | **Entfällt** | Build-Artefakte, niemals versioniert; wird beim Kopieren explizit ausgeschlossen (wie bereits in `quarkus-morphium/MIGRATION-NOTES.md:496` als Kontrollschritt vorgesehen). |
| `.DS_Store` (Wurzel, `.github/`, `spring-boot-morphium-test/`, `spring-boot-morphium-autoconfigure/`) | **Entfällt** | macOS-Metadaten, nie relevanter Inhalt. |

Es existiert **kein** Antora-`docs/`-Verzeichnis, keine `docs-for-morphium/`,
keine `RELEASES.md` in diesem Repository (anders als bei `quarkus-morphium`)
— diese Zeilen aus der Vorlage entfallen ersatzlos, da nichts Entsprechendes
vorhanden ist. `.mcp.json`/`CLAUDE.md` sind ebenfalls nicht vorhanden
(bestätigt: keine solchen Dateien in der Dateisuche des Repos aufgetaucht).

### 3.2 Abhängigkeiten: Modul-POM vs. `morphium-parent`/`dependencyManagement`

Gemäß der bereits im `morphium/pom.xml:37-59` dokumentierten Invariante **I4**
(„Foreign BOMs (Quarkus/Spring) are NOT imported here, only in the respective
extension module's own POM") und I2 („core dependency tree stays free of
jakarta.data/io.quarkus/org.springframework"):

- **Bleibt im Modul-POM** (`spring-boot-morphium/pom.xml` nach der Integration):
  - `spring-boot-dependencies`-BOM-Import (`pom.xml:42-48`) — MUSS im Modul bleiben, s.u.
  - Alle konkreten Versions-Properties, die nur für dieses Modul gelten:
    `spring-boot.version` (`pom.xml:34`). `morphium.version` und
    `morphium-jakarta-data.version` (`pom.xml:35-36`) entfallen nach der
    Integration ohnehin, weil das Modul dann `${project.version}` der
    Reactor-Version für `de.caluga:morphium`/`de.caluga:morphium-jakarta-data`
    verwenden wird (Lockstep-Versionierung, analog zur bereits etablierten
    Praxis bei `morphium-jakarta-data`/`quarkus-morphium`).
  - `io.github.classgraph:classgraph:4.8.184` — bereits **auch** im
    Morphium-Kern-Parent verwaltet (`morphium/pom.xml:321-325`, identische
    Version `4.8.184`). Die explizite Dependency-Deklaration in
    `spring-boot-morphium-autoconfigure/pom.xml:35-38` kann bleiben (Nutzung
    ist modulspezifisch, siehe `MorphiumAutoConfiguration.java:53-81`
    Entity-Pre-Scan), die Versionsverwaltung übernimmt künftig automatisch
    `morphium-parent`'s bereits vorhandener `dependencyManagement`-Eintrag.
- **Könnte in `morphium-parent`/`dependencyManagement` wandern** (nur die
  Versions-**Property**, nicht der BOM-Import — analog zum bestehenden Muster
  bei `quarkus.version` in `morphium/pom.xml:84-89`):
  - Eine künftige `spring-boot.version`-Property in `morphium-parent`
    (wie es `morphium/pom.xml:89` bereits für `quarkus.version` tut) wäre
    konsistent mit der bestehenden Praxis, ist aber eine Entscheidung für
    M5-T2/T5, nicht Gegenstand dieser Analyse.
  - `jakarta.data.version` ist in `morphium/pom.xml:83` bereits als
    Reactor-weite Property vorhanden (`1.0.0`) — identisch zum hier
    verwendeten Wert (`pom.xml:37` dieses Repos, ebenfalls `1.0.0`). Nach der
    Integration entfällt die doppelte Property-Deklaration.

### 3.3 BOM-Konflikte: `spring-boot-dependencies` vs. `morphium-parent`

Konkreter Versionsvergleich (Belege: `repo1.maven.org` für
`spring-boot-dependencies-3.4.4.pom`, `morphium/pom.xml` für den
Morphium-Kern):

| Artefakt | Morphium-Kern-Version | spring-boot-dependencies 3.4.4 | Konflikt? |
|---|---|---|---|
| `org.slf4j:slf4j-api` | `2.0.17` (`morphium/pom.xml:235-238`) | `2.0.17` (`slf4j.version` in `spring-boot-dependencies-3.4.4.pom`, per curl geprüft) | **Kein Konflikt** — identische Version. Zufällig deckungsgleich, kein strukturelles Sicherheitsnetz. |
| `ch.qos.logback:logback-classic`/`logback-core` | `1.5.37` (`morphium/pom.xml:240-248`) | `1.5.18` (`logback.version` in `spring-boot-dependencies-3.4.4.pom`, per curl geprüft) | **Potenzieller Konflikt** — Morphium-Kern verwaltet eine neuere Logback-Version (`1.5.37`) als das Spring-Boot-BOM (`1.5.18`). Da der BOM-Import ausschließlich im Modul-POM (`spring-boot-morphium/pom.xml`) liegt und nicht im Parent, betrifft das ausschließlich diesen einen Modul-Build — welche Version tatsächlich gewinnt, hängt von der Reihenfolge im `dependencyManagement` bzw. Maven's "nearest definition wins"-Regel ab, sobald das Modul unter `morphium-parent` gehängt wird und beide `dependencyManagement`-Blöcke wirksam sind. **Dieser Punkt braucht einen echten Build-Test (M5-T4-Trockenlauf), keine reine POM-Lektüre** — die tatsächlich aufgelöste Version lässt sich ohne `mvn dependency:tree` nicht sicher vorhersagen. |
| Jackson (`com.fasterxml.jackson:jackson-bom`) | **Nicht vom Morphium-Kern verwaltet** — `search_files` über `morphium/pom.xml` und das gesamte `morphium/`-Verzeichnis nach `jackson` liefert nur Treffer in `quarkus-morphium/*` (Quarkus-eigene Jackson-Extension-Artefakte), keinen Treffer im Morphium-Kern-`dependencyManagement` | `2.18.3` (`jackson-bom.version` in `spring-boot-dependencies-3.4.4.pom`, per curl geprüft) | **Kein Konflikt möglich** — Morphium-Kern hat keine eigene Jackson-Version, die kollidieren könnte. Das Spring-Boot-BOM darf hier ungestört seine eigene Version auflösen, exakt wie von Invariante I4 vorgesehen. |

**Wichtiger Befund, der die Aufgabenstellung bestätigt:** Der BOM-Import
gehört zwingend ins **Modul-POM** (`spring-boot-morphium/pom.xml`), nicht in
`morphium-parent`. Würde `spring-boot-dependencies` in
`morphium-parent/dependencyManagement` importiert, würde jeder
Kern-Build (`morphium-core`, `poppydb`) versuchen, ~250+ Spring-Boot-verwaltete
Koordinaten aufzulösen, obwohl der Kern gar keine Spring-Abhängigkeit hat
— genau die in `morphium/pom.xml:40-59` (Invarianten I2/I4) bereits
dokumentierte Gefahr. Diese Analyse bestätigt, dass die aktuelle Platzierung
des BOM-Imports im Modul-POM (`pom.xml:42-48` dieses Repos) bereits korrekt
ist und bei der Integration unverändert an dieser Stelle bleiben muss.

### 3.4 Docker-Bedarf des test-Moduls

**Nein.** Siehe 1.5: alle Tests laufen mit Morphiums InMemDriver
(`application-test.properties:1-3`, `MorphiumTest.java:23-27`). Kein
Testcontainers-Import, keine `docker`-Befehle im gesamten Repository außer
dem bestätigenden Kommentar in `CONTRIBUTING.md:53`. Das steht im deutlichen
Gegensatz zu `quarkus-morphium`, dessen `integration-tests`-Modul laut
Wellenplan (`morphium-jakarta-data/docs/plans/.../waves/M5-spring-boot.md:33`)
Testcontainers benötigt — dieser Unterschied ist einer der Gründe, warum die
M5-Welle laut demselben Plandokument als „einfacher" eingeschätzt wird.

---

## 4. Risiken (Top 3)

1. **Logback-Versionskonflikt zwischen `spring-boot-dependencies` (1.5.18) und
   Morphium-Kern (1.5.37) ist nicht durch reine POM-Analyse auflösbar.**
   Beleg: `spring-boot-dependencies-3.4.4.pom` (`logback.version=1.5.18`, per
   curl geprüft) vs. `morphium/pom.xml:240-248` (`1.5.37`). Welche Version
   nach der Integration tatsächlich im Klassenpfad landet, hängt von der
   `dependencyManagement`-Reihenfolge zwischen Modul- und Parent-POM ab.
   Dies ist explizit als Aufgabe von M5-T4 (Trockenlauf) vorgesehen
   (`morphium-jakarta-data/docs/plans/.../waves/M5-spring-boot.md:410-417`)
   und dort mit `mvn dependency:tree`/`dependency:list` zu verifizieren — in
   dieser rein analytischen Aufgabe kann nur der Konflikt selbst benannt,
   nicht dessen Auflösung gemessen werden.

2. **Namensschema-Verletzung (`spring-boot-morphium-*` statt
   `morphium-spring-boot-*`) ist eine öffentlich sichtbare
   Maven-Central-Koordinate.** Sobald der erste Release erfolgt, ist eine
   spätere Umbenennung mit Breaking Changes für alle Downstream-Nutzer
   verbunden (neue `<artifactId>` in jeder Nutzer-`pom.xml`). Aktuell (kein
   Tag, kein Release, `git log` zeigt 7 Commits ohne Central-Publikation)
   ist dies folgenlos korrigierbar — das Zeitfenster dafür schließt sich mit
   dem ersten Release. Dies ist der einzige der zwölf Prüfpunkte, der laut
   Auftrag „unter Spring-Boot-Nutzern gut bekannt" ist und in einem Review
   sofort auffallen würde.

3. **Fehlender `spring-boot-configuration-processor` bedeutet, dass Nutzer
   keine IDE-Autovervollständigung für `spring.morphium.*`-Properties
   erhalten**, und — praktisch relevanter für die Modulqualität selbst — dass
   bei der Integration in den Reactor kein automatisierter Abgleich
   zwischen `MorphiumProperties.java`-Feldern und dokumentierten Properties
   in README/Metadaten existiert. Ohne das Metadaten-JSON ist die
   Property-Referenz in `README.md:159-179` reine Handarbeit, die bei
   künftigen Änderungen an `MorphiumProperties.java` unbemerkt veralten
   kann. Low-effort zu beheben (eine `<dependency>`-Zeile), aber bisher
   nicht vorhanden.

---

## Anhang: Quellenverzeichnis für diesen Bericht

- Alle `pom.xml`-Dateien des Repos (vollständig gelesen, s. Zitate oben).
- Alle 14 Produktions-Java-Klassen des Repos (vollständig gelesen).
- 5 Test-Java-Klassen + `application-test.properties` (vollständig gelesen).
- `README.md`, `CONTRIBUTING.md`, `.github/workflows/build.yml` (vollständig gelesen).
- `git log --oneline`, `git branch --show-current`, `git remote -v` im Repo ausgeführt.
- Offizielle Spring-Boot-Doku via `curl` (HTTP 200 bestätigt), textextrahiert
  und durchsucht: `developing-auto-configuration.html`, `build-systems.html`.
- Maven-Central-Metadaten für `spring-boot-dependencies` via
  `repo1.maven.org/maven2/.../maven-metadata.xml` und das konkrete
  `spring-boot-dependencies-3.4.4.pom` (für Jackson/Logback/SLF4J-Versionen)
  per `curl` abgerufen.
- `morphium/pom.xml` (vollständig gelesen) als Vergleichsbasis für den
  Morphium-Kern-`dependencyManagement`.
- `quarkus-morphium/MIGRATION-NOTES.md` (vollständig gelesen) als
  strukturelle Vorlage für Abschnitt 3.1.
