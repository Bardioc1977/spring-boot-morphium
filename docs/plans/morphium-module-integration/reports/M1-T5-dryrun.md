# M1-T5 — Trockenlauf: Integration von morphium-jakarta-data als Modul

| Feld | Wert |
|---|---|
| Datum | 2026-08-05 |
| Ort des Trockenlaufs | `/tmp/m1-dryrun` (Wegwerf-Kopie, NICHT das echte Repository) |
| Ergebnis | **Alle Verifikationsschritte und Invarianten I1–I5: PASS** |
| Betroffene reale Repos | Keine Änderung. `morphium/` und `morphium-jakarta-data/` (echtes Repo) wurden nicht verändert. |

---

## 0. Vorbereitung und Abweichungen vom Skript

### Branch-Checkout im Ziel-Repo (Schritt 1)

`git checkout develop` in der Kopie war **erfolgreich** (kein lokaler
Änderungs-Konflikt):

```
$ cd /tmp/m1-dryrun/morphium && git checkout develop
Zu Branch 'develop' gewechselt
Ihr Branch ist auf demselben Stand wie 'fork/develop'.
```

→ Ergebnis: **PASS**, Arbeitskopie steht auf `develop`
(`morphium-parent` Version `6.2.5-SNAPSHOT`).

### Quellbranch für morphium-jakarta-data

Wie im Auftrag vorgegeben, wurde aus dem **bereits fertiggestellten Branch
`move-to-morphium`** des Repos `morphium-jakarta-data` kopiert (nicht aus
`develop` dieses Repos).

### Versions-Diskrepanz Parent (nicht im Skript vorgesehen, aber notwendig)

Der Modul-`pom.xml` auf `move-to-morphium` referenziert
`morphium-parent` Version `6.2.6-SNAPSHOT` (Stand des echten
`morphium`-Repos zum Zeitpunkt der M1-Vorarbeit, aktueller HEAD dort).
Die Dryrun-Kopie steht aber auf `develop`, wo `morphium-parent` noch
`6.2.5-SNAPSHOT` ist. Ein reiner 1:1-Kopiervorgang hätte damit sofort
mit einem "non-resolvable parent POM"-Fehler abgebrochen.

**Für den Trockenlauf wurde die Parent-Version in der Kopie auf
`6.2.5-SNAPSHOT` korrigiert** (mit Kommentar im POM markiert), um den
Reactor lauffähig zu machen. Das ist reine Dryrun-Anpassung an den
Zielbranch dieser Kopie und **kein Teil des zu übernehmenden Patches**.

**Konsequenz für M2:** Wenn `morphium-jakarta-data` tatsächlich in den
`morphium`-Reaktor integriert wird, muss die Parent-Version im
Modul-POM zum Zeitpunkt der Integration mit der dann aktuellen
`morphium-parent`-Version übereinstimmen (Snapshot wird ohnehin von
Maven aus dem Parent geerbt, sobald das Modul im selben Reactor liegt —
dann kann/sollte die `<version>` im `<parent>`-Block sogar entfallen,
da sie beim Reactor-Build automatisch aufgelöst wird, solange sie exakt
matcht). Dies vor dem eigentlichen M2-Merge nochmal gegen den dann
aktuellen `develop`-Stand von `morphium` prüfen.

### `.DS_Store`

Beim Kopieren von `src/` wurde eine macOS-Metadatei
`morphium-jakarta-data/src/.DS_Store` mitkopiert (kein Bestandteil des
Git-Trackings, aber physisch im Quellverzeichnis vorhanden). Sie wurde
**nicht** entfernt (Sicherheitsmechanismus der Ausführungsumgebung hat
wiederholte Löschversuche blockiert). Sie beeinflusst den Build nicht
(Maven ignoriert sie), ist aber beim tatsächlichen M2-Kopiervorgang
schlicht **nicht mitzukopieren** (sie ist ohnehin nicht Teil des
Git-Trackings von `move-to-morphium`).

---

## 1. Arbeitskopie

```
rm -rf /tmp/m1-dryrun && mkdir -p /tmp/m1-dryrun
cp -R .../morphium /tmp/m1-dryrun/morphium
cd /tmp/m1-dryrun/morphium && git checkout develop
```

**Ergebnis: PASS** — Branch `develop`, `morphium-parent` `6.2.5-SNAPSHOT`.

---

## 2. Modul kopiert

Kopiert aus `morphium-jakarta-data` (Branch `move-to-morphium`) nach
`/tmp/m1-dryrun/morphium/morphium-jakarta-data/`:

- `pom.xml`
- `src/` (komplett: `main/java/...` 14 Klassen, `test/java/...` 3 Testklassen)
- `README.md`
- `CHANGELOG.md`

**Nicht kopiert** (wie vorgegeben): `.git`, `target`, `.github`, `LICENSE`,
`CODE_OF_CONDUCT.md`, `SECURITY.md`, `CONTRIBUTING.md`,
`MIGRATION-NOTES.md`, `docs-for-morphium`.

**Ergebnis: PASS**

---

## 3. Anpassungen an `pom.xml`

### 3a. Root-`pom.xml` (`morphium-parent`)

Umgesetzt nach **Variante B** aus D3-reactor-strategie.md
(Profil `extensions`, per Default aktiv via
`<name>!skipExtensions</name>`):

- Kommentarblock mit den Invarianten I1–I5 direkt vor `<modules>`
  ergänzt (Begleitmaßnahme 1 aus D3, "damit der nächste Contributor
  die Regel aus der POM lernt").
- `<properties>`: `<jakarta.data.version>1.0.0</jakarta.data.version>`
  ergänzt.
- `<dependencyManagement>`: `jakarta.data:jakarta.data-api` mit
  `${jakarta.data.version}` ergänzt — **kein** BOM-Import (Invariante I4
  bleibt gewahrt).
- `<profiles>`: neues Profil `extensions` mit
  `<activation><property><name>!skipExtensions</name></property></activation>`
  und `<modules><module>morphium-jakarta-data</module></modules>`.

Der exakte Diff gegen `develop`/`morphium/pom.xml` ist unten unter
**"Copy-Paste-Patch für M2"** dokumentiert.

### 3b. Modul-`pom.xml` (`morphium-jakarta-data`)

Die zwei mit "M2:"-Kommentar markierten Stellen wurden umgestellt:

1. `<properties><jakarta.data.version>1.0.0</jakarta.data.version></properties>`
   im Modul entfernt (jetzt vom Parent geerbt).
2. `<dependency><groupId>jakarta.data</groupId>...</dependency>` im
   Modul **ohne** `<version>`-Tag (Version kommt jetzt aus
   `dependencyManagement` des Parents).

Zusätzlich (Dryrun-spezifisch, s. o.): `<parent><version>` von
`6.2.6-SNAPSHOT` auf `6.2.5-SNAPSHOT` korrigiert, mit Kommentar markiert.

**Ergebnis: PASS** — POM ist danach valide, Reactor erkennt das Modul.

---

## 4. Verifikation — Befehle und Ergebnisse

| # | Befehl | Ergebnis |
|---|---|---|
| 4.0 | `mvn -B validate` | **PASS** — 4 Module erkannt (Parent, Morphium, PoppyDB, Morphium Jakarta Data), `BUILD SUCCESS`, kein Zyklus-Fehler |
| 4.1 | `mvn -B -q install -DskipTests` | **PASS** — Exit-Code 0, Reaktor baut alle 4 Module inkl. `morphium-jakarta-data` |
| 4.2 | `mvn -B verify -pl morphium-jakarta-data` | **PASS** — Exit-Code 0, `Tests run: 45, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS` |
| 4.3 | `mvn -B -q install -DskipTests -DskipExtensions` | **PASS** — Exit-Code 0 |
| 4.4 | (verbose Wiederholung von 4.3 zur Reactor-Summary-Prüfung) `mvn -B install -DskipTests -DskipExtensions` | **PASS** — Reactor Summary zeigt nur `Morphium Parent`, `Morphium`, `PoppyDB` — **kein** `Morphium Jakarta Data` |
| 4.5 | `mvn -q -pl morphium-core dependency:tree -DoutputFile=/tmp/m1-dryrun/core-tree.txt` | **PASS** — Datei erzeugt, 46 Zeilen |
| 4.6 | `mvn -q -pl morphium-jakarta-data dependency:tree -DoutputFile=/tmp/m1-dryrun/mjd-tree.txt` | **PASS** — Datei erzeugt, 37 Zeilen, enthält `jakarta.data:jakarta.data-api:jar:1.0.0:compile` |

Hinweis: `-DoutputFile` wurde für die Dependency-Trees verwendet, weil
`mvn -q ... dependency:tree > datei.txt` bei `-q` nur die (Java-)
Startup-Warnungen umleitet — das Plugin schreibt seine Ausgabe bei
`-q` sonst nicht auf stdout. Funktional identisch zum geforderten
Befehl, nur mit garantierter Dateibefüllung.

Logs liegen unter `/tmp/m1-dryrun/step4a-install.log`,
`step4b-verify-mjd.log`, `step4c-skipext.log`,
`step4c-skipext-verbose.log`.

---

## 5. Invariantentabelle (D3-reactor-strategie.md)

| # | Invariante | Verifikationsbefehl | Ergebnis | Beleg |
|---|---|---|---|---|
| I1 | `morphium-core/pom.xml` enthält keine Referenz auf ein Erweiterungsmodul | `grep -E 'jakarta-data\|quarkus\|spring' morphium-core/pom.xml` | **PASS** | `grep` liefert Exit-Code 1 (keine Treffer) |
| I2 | Kern-Dependency-Tree frei von Erweiterungen/Fremd-APIs | `grep -E 'jakarta\.data\|io\.quarkus\|org\.springframework' core-tree.txt` | **PASS** | `grep` liefert Exit-Code 1 (keine Treffer) in `/tmp/m1-dryrun/core-tree.txt` |
| I3 | Kern + PoppyDB ohne Erweiterungen baubar | `mvn install -DskipTests -DskipExtensions`, Reactor-Summary prüfen | **PASS** | Exit 0; Reactor Summary listet nur `Morphium Parent`, `Morphium`, `PoppyDB` — `morphium-jakarta-data` fehlt vollständig |
| I4 | `morphium-parent` erzwingt keine Fremd-BOM für Kern-Builds | `grep -c "scope>import" pom.xml` | **PASS** | 0 Treffer; `jakarta.data-api` ist Einzel-Dependency, kein `<type>pom</type><scope>import</scope>`-Konstrukt, und ohnehin nicht Quarkus/Spring |
| I5 | Keine zyklische Modulabhängigkeit | `mvn -B validate` am Root | **PASS** | `BUILD SUCCESS`, keine Zyklus-Fehlermeldung von Maven |

**Alle Invarianten I1–I5 sind erfüllt. Kein Verstoß, keine Abbruchsituation.**

---

## 6. Exakter Diff für `morphium/pom.xml` — Copy-Paste-Patch für M2

Basis: `develop`-Stand von `morphium/pom.xml` zum Zeitpunkt dieses
Trockenlaufs. **Vor dem tatsächlichen Anwenden in M2 gegen den dann
aktuellen `develop`-Stand erneut prüfen** (Zeilennummern/Kontext können
sich verschoben haben).

```diff
--- morphium/pom.xml (vorher)
+++ morphium/pom.xml (nachher)
@@ -30,6 +30,22 @@
       <email>sb@caluga.de</email>
     </developer>
   </developers>
+  <!--
+    M1: Kern-Module (immer gebaut) vs. Erweiterungsmodule (Profil "extensions",
+    per Default aktiv). Siehe D3-reactor-strategie.md, Variante B.
+
+    Invarianten (I1-I5), die dieser Aufbau garantiert:
+      I1: morphium-core/pom.xml referenziert keine Erweiterung.
+      I2: Der Kern-Dependency-Tree ist frei von jakarta.data/io.quarkus/org.springframework.
+      I3: Kern (+PoppyDB) ist ohne die Erweiterungen baubar: -DskipExtensions.
+      I4: Fremd-BOMs (Quarkus/Spring) werden NICHT hier importiert, sondern
+          ausschliesslich im jeweiligen Erweiterungsmodul.
+      I5: Keine zyklische Modulabhaengigkeit (Erweiterung -> Kern, niemals umgekehrt).
+
+    "mvn install"                    -> baut Kern + PoppyDB + alle Erweiterungen
+    "mvn install -DskipExtensions"    -> baut nur Kern + PoppyDB (kein Docker,
+                                          kein Quarkus-/Spring-Download noetig)
+  -->
   <modules>
     <module>morphium-core</module>
     <module>poppydb</module>
@@ -49,6 +65,9 @@
     <!-- JUnit tag control (defaults) -->
     <test.includeTags />
     <test.excludeTags>external</test.excludeTags>
+    <!-- M2: Property fuer das Erweiterungsmodul morphium-jakarta-data.
+         Bewusst NUR die Version, keine Quarkus-/Spring-BOM (siehe I4). -->
+    <jakarta.data.version>1.0.0</jakarta.data.version>
   </properties>
   <build>
     <pluginManagement>
@@ -311,6 +330,14 @@
           </exclusion>
         </exclusions>
       </dependency>
+      <!-- M2: fuer das Erweiterungsmodul morphium-jakarta-data. Einzelne
+           Dependency, KEIN BOM-Import (I4 bleibt fuer diese Erweiterung
+           ohnehin unkritisch, da Jakarta Data kein eigenes BOM mitbringt). -->
+      <dependency>
+        <groupId>jakarta.data</groupId>
+        <artifactId>jakarta.data-api</artifactId>
+        <version>${jakarta.data.version}</version>
+      </dependency>
     </dependencies>
   </dependencyManagement>
   <profiles>
@@ -369,6 +396,20 @@
       <properties>
         <morphium.driver>single</morphium.driver>
       </properties>
+    </profile>
+    <!-- M1: Erweiterungsmodule. Per Default aktiv (siehe D3, Variante B).
+         Deaktivierbar mit "-DskipExtensions" fuer einen Kern-only-Build
+         ohne Quarkus-/Spring-Download und ohne Docker. -->
+    <profile>
+      <id>extensions</id>
+      <activation>
+        <property>
+          <name>!skipExtensions</name>
+        </property>
+      </activation>
+      <modules>
+        <module>morphium-jakarta-data</module>
+      </modules>
     </profile>
   </profiles>
 </project>
```

### Zusätzlich am Modul-`pom.xml` (`morphium-jakarta-data/pom.xml`)

Nach Aufnahme der Property/Dependency in den Parent müssen die
"M2:"-markierten Stellen im Modul entsprechend reduziert werden:

```diff
--- morphium-jakarta-data/pom.xml (vorher, mit lokalen "M2:"-Platzhaltern)
+++ morphium-jakarta-data/pom.xml (nachher, geerbt vom Parent)
@@ -13,10 +13,6 @@
   <name>Morphium Jakarta Data</name>
   <description>Framework-agnostic Jakarta Data runtime for Morphium ODM</description>
-  <properties>
-    <!-- M2: nach morphium-parent verschieben (properties + dependencyManagement) -->
-    <jakarta.data.version>1.0.0</jakarta.data.version>
-  </properties>
   <dependencies>
     <dependency>
       <groupId>de.caluga</groupId>
       <artifactId>morphium</artifactId>
       <version>${project.version}</version>
     </dependency>
-    <!-- M2: nach morphium-parent verschieben (properties + dependencyManagement) -->
+    <!-- M2: jetzt aus morphium-parent geerbt (properties + dependencyManagement) -->
     <dependency>
       <groupId>jakarta.data</groupId>
       <artifactId>jakarta.data-api</artifactId>
-      <version>${jakarta.data.version}</version>
     </dependency>
```

**Wichtiger Hinweis für M2 zur Parent-Version:** Im aktuellen
`move-to-morphium`-Branch steht im `<parent>`-Block
`<version>6.2.6-SNAPSHOT</version>`. Diese muss beim tatsächlichen
Merge exakt der Version entsprechen, die `morphium/pom.xml` zu diesem
Zeitpunkt trägt (aktuell wechselnd zwischen Branches beobachtet:
`develop`=6.2.5-SNAPSHOT, `origin/develop`=6.3.0-SNAPSHOT,
`feature/query-atomic-upsert`=6.2.6-SNAPSHOT). **Vor M2 unbedingt den
Zielbranch/-commit von `morphium` festlegen und die Parent-Version im
Modul-POM exakt darauf abstimmen**, sonst schlägt die Parent-Auflösung
fehl.

---

## 7. Dateien, die in M2 tatsächlich kopiert werden müssen

Aus `morphium-jakarta-data` (Branch `move-to-morphium`) nach
`morphium/morphium-jakarta-data/` — identisch zur Menge, die auch in
diesem Trockenlauf verwendet wurde:

```
CHANGELOG.md
pom.xml
README.md
src/main/java/de/caluga/morphium/data/AbstractMorphiumRepository.java
src/main/java/de/caluga/morphium/data/CursorHelper.java
src/main/java/de/caluga/morphium/data/FindMethodBridge.java
src/main/java/de/caluga/morphium/data/JdqlMethodBridge.java
src/main/java/de/caluga/morphium/data/JdqlParser.java
src/main/java/de/caluga/morphium/data/JdqlQuery.java
src/main/java/de/caluga/morphium/data/MethodNameParser.java
src/main/java/de/caluga/morphium/data/MorphiumPage.java
src/main/java/de/caluga/morphium/data/MorphiumRepository.java
src/main/java/de/caluga/morphium/data/QueryDescriptor.java
src/main/java/de/caluga/morphium/data/QueryExecutor.java
src/main/java/de/caluga/morphium/data/QueryMethodBridge.java
src/main/java/de/caluga/morphium/data/QueryResultHelper.java
src/main/java/de/caluga/morphium/data/RepositoryMetadata.java
src/main/java/de/caluga/morphium/data/SortMapper.java
src/test/java/de/caluga/morphium/data/JdqlParserTest.java
src/test/java/de/caluga/morphium/data/MethodNameParserTest.java
src/test/java/de/caluga/morphium/data/QueryExecutorAliasTest.java
```

(19 Dateien: 3 Meta-/Build-Dateien + 14 Hauptquellklassen + 3 Testklassen.)

**Explizit NICHT kopieren** (bewusst ausgeschlossen, gehören nicht in
den `morphium`-Reactor):
`.git`, `target`, `.github/`, `LICENSE`, `CODE_OF_CONDUCT.md`,
`SECURITY.md`, `CONTRIBUTING.md`, `MIGRATION-NOTES.md`,
`docs-for-morphium/`, sowie sämtliche `docs/`-Planungsartefakte des
`morphium-jakarta-data`-Repos selbst (`docs/jf/`,
`docs/plans/morphium-module-integration/...`) — letztere bleiben im
Planungs-Repository, nicht im Produktionscode von `morphium`.

Zusätzlich am Ziel-POM anzuwenden: der Patch aus Abschnitt 6 (Root-POM
+ Modul-POM-Bereinigung).

---

## 8. Zusammenfassung

- Trockenlauf vollständig in `/tmp/m1-dryrun/morphium` durchgeführt,
  reales Repository unter
  `/Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium`
  wurde **nicht verändert**.
- Variante B aus D3-reactor-strategie.md (Profil `extensions`, per
  Default aktiv über `!skipExtensions`) wurde erfolgreich umgesetzt und
  end-to-end verifiziert.
- Vollbau (`mvn install`) baut alle 4 Module inkl. Erweiterung,
  45 Modultests grün.
- `-DskipExtensions`-Build baut nachweislich **nur** Kern + PoppyDB.
- Alle 5 Invarianten I1–I5 sind mit Befehl und Ergebnis belegt: **PASS**.
- Einzige nennenswerte Abweichung vom Skript: Anpassung der
  Parent-Version im Modul-POM (`6.2.6-SNAPSHOT` → `6.2.5-SNAPSHOT`) für
  den Dryrun, da die Kopie auf `develop` steht. Dies ist beim
  tatsächlichen M2-Merge gegen den dann aktuellen Stand von
  `morphium/pom.xml` neu zu prüfen.
- `/tmp/m1-dryrun` bleibt für die Prüfung durch den Orchestrator
  erhalten (Logs: `step4a-install.log`, `step4b-verify-mjd.log`,
  `step4c-skipext.log`, `step4c-skipext-verbose.log`,
  `core-tree.txt`, `mjd-tree.txt`, `root-pom.unified.diff`,
  `m2-module-files.txt`).
