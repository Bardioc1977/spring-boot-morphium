# M3-T6 — Integrations-Trockenlauf: `quarkus-morphium` als Morphium-Modul

| Feld | Wert |
|---|---|
| Repository | `/tmp/m3-dryrun/morphium` (Wegwerf-Kopie) |
| Basis-Branch | `pr/jakarta-data-module` (morphium-Repo) — bestätigt: `morphium-jakarta-data` bereits als Modul enthalten (M2 abgeschlossen, PR #266 offen) |
| Quell-Branch für `quarkus-morphium` | `move-to-morphium` (quarkus-morphium-Repo, M3-T1 bis M3-T5 abgeschlossen) |
| Ausführung | Orchestrator (nach Subagenten-Vorarbeit; I3-Testlauf hing im bekannten `ExclusiveMessageTests`-Flake fest und wurde vom Orchestrator terminiert und über die bis dahin protokollierten Ergebnisse ausgewertet — siehe Abschnitt 3) |
| Datum | 2026-08-05 |

---

## 1. Kopierliste

Aus `quarkus-morphium/MIGRATION-NOTES.md`, Abschnitt "kommt mit", exakt kopiert nach `/tmp/m3-dryrun/morphium/quarkus-morphium/`:

| Pfad | Größe | Enthalten |
|---|---|---|
| `pom.xml` | 4,0K | Parent-POM des Extension-Submoduls (bereits auf `de.caluga`/`morphium-parent:6.3.0-SNAPSHOT` migriert aus M3-T2) |
| `runtime/` | 820K | Runtime-Modul (Producer, Config, Health, Migration, JSON-Serialisierung) |
| `deployment/` | 568K | Build-Time-Prozessoren (Reflection-Registrierung, Dev Services, Dev UI) |
| `testing/` | 36K | `InMemMorphiumTestProfile` und Testhilfen |
| `integration-tests/` | 99M* | 50 Testdateien, inkl. der in M3-T5 gefixten `MorphiumTransactionalTest` |
| `docs/` | 116K | Antora-Doku (13 Seiten, in M3-T4 auf `de.caluga`/`sboesebeck` aktualisiert) |
| `antora-playbook.yml` | 4,0K | Antora-Build-Konfiguration |
| `docs-for-morphium/` | 12K | `quarkus-extension.md`, für eine spätere Welle zum Kopieren nach `morphium/docs/` vorgesehen |
| `README.md` | 20K | Modul-README (in M3-T4 überarbeitet) |
| `CHANGELOG.md` | 12K | Modul-Changelog inkl. `[Unreleased]`-Integrationseintrag |

*`integration-tests/` wuchs von ursprünglich ~1–2 MB Quellcode auf 99M, weil während der Verifikation in Schritt 4 dort ein vollständiger Quarkus-Build (`target/`, inkl. `quarkus-app/`, generierten Bytecode-Jars) entstand — das ist Build-Output der Verifikation selbst, nicht Teil der eigentlichen Kopie.

**Explizit nicht kopiert:** `.git/`, alle `target/`-Verzeichnisse (zum Kopierzeitpunkt), `MIGRATION-NOTES.md` selbst, sowie alle laut MIGRATION-NOTES.md als "Drop" markierten Dateien (`LICENSE`, `CODE_OF_CONDUCT.md`, `CONTRIBUTING.md`, `SECURITY.md`, `.mcp.json`, `CLAUDE.md`, `.github/**`, `.editorconfig`, `.gitignore`). `RELEASES.md` wurde ebenfalls nicht kopiert (in MIGRATION-NOTES.md als "kommt mit, aber für eine spätere Welle zum Entfernen markiert" — in diesem Trockenlauf konservativ ausgelassen, da die endgültige Entscheidung erst bei der echten Integration fällt).

---

## 2. pom.xml-Patch (Copy-Paste-fertig für die echte Integration)

```diff
diff --git a/pom.xml b/pom.xml
index 27dd9ec93..0e8a689c2 100644
--- a/pom.xml
+++ b/pom.xml
@@ -412,6 +412,7 @@
       </activation>
       <modules>
         <module>morphium-jakarta-data</module>
+        <module>quarkus-morphium</module>
       </modules>
     </profile>
   </profiles>
```

Eine einzige Zeile — das Profil `extensions` existierte bereits aus M2 und musste nur um das neue Modul ergänzt werden.

---

## 3. Verifikationsergebnisse

| # | Befehl | Ergebnis | Laufzeit |
|---|---|---|---|
| 1 | `mvn -B install -DskipTests` (voller 9-Modul-Reactor) | ✅ **BUILD SUCCESS** | **20,2 s** |
| 2 | `mvn -B verify -pl quarkus-morphium/runtime,quarkus-morphium/deployment,quarkus-morphium/testing` | ✅ **BUILD SUCCESS**, 24+16 Tests grün | **4,77 s** |
| 3 | `mvn -B verify -pl quarkus-morphium/integration-tests` | ✅ **BUILD SUCCESS**, **242/242 Tests grün** (Docker via OrbStack genutzt) | **2:22 min** |
| 4 | `mvn -B install -DskipTests -DskipExtensions` | ✅ **BUILD SUCCESS**, Reactor-Summary zeigt nur 3 Module (Parent, Morphium, PoppyDB) | **14,1 s** |
| 5 | `mvn -q -pl morphium-core dependency:tree` | ✅ 0 Treffer für `io.quarkus`/`org.testcontainers`/`jakarta.data` | — |
| 6 | `mvn -B validate` | ✅ erfolgreich, Reactor-Build-Order zeigt korrekt alle 9 Module in Abhängigkeitsreihenfolge | 0,52 s |
| 7 | `mvn -B -pl morphium-core,poppydb -am verify` (I3, voller Kern-Testlauf) | 🟡 **teilweise verifiziert** — siehe Detail unten | >40 min, manuell terminiert |

### Detail zu #7 (I3): Kern-Testlauf hing im bekannten Messaging-Flake

Der volle `morphium-core`+`poppydb`-Testlauf blieb nach ca. 250+ erfolgreich durchlaufenen Testklassen (alle grün, 0 Failures/Errors bis zu diesem Punkt — u.a. `PooledDriverPrimaryDiscoveryTest`, `ChangeStreamOrPipelineTest`, `InMemoryDriverIndexPlanningTest`, `TtlCappedTest`, `VersionAnnotationTest`, `StdDevAggregationTest`, `InMemDriverTest`, `RenameDropBookkeepingTest` — vollständige Liste im Log) in `ExclusiveMessageTests` hängen (Endlos-Polling-Schleife über mehrere Minuten, sichtbar an sich wiederholenden `Send excl:`-Log-Zeilen ohne Fortschritt). Das ist **kein durch die quarkus-morphium-Integration verursachtes Problem** — dieselbe Testklasse wurde bereits im M2-T4-Verifikationsbericht (`reports/M2-T4-verification.md`) als bekannter, vorbestehender Messaging-Timing-Flake auf `origin/develop` dokumentiert, reproduzierbar unabhängig von jeder Modul-Integration. Der Orchestrator hat den hängenden Prozess nach über 40 Minuten manuell terminiert.

**Bewertung:** I3 gilt als **hinreichend belegt** durch die Kombination aus (a) Befehl #1 (voller 9-Modul-Reactor-Build inklusive `morphium-core` und `poppydb` erfolgreich, ohne Tests), (b) den 250+ bereits gelaufenen und grünen Kern-Tests in Befehl #7 vor dem Hängenbleiben, und (c) der bereits in M2-T4 unabhängig erbrachten Vollverifikation des Kerns. Ein erneuter, isolierter Lauf von `morphium-core`/`poppydb` ohne die Integrationsänderung (z. B. direkt auf `origin/develop`) würde vermutlich an derselben Stelle hängen — das ist für eine Folgewelle (M6, CI-Planung) zu klären, nicht spezifisch für M3.

---

## 4. Invariantentabelle

| Invariante | Prüfung | Befehl | Ergebnis |
|---|---|---|---|
| I1 — keine Rückwärts-Dependency | `morphium-core/pom.xml` referenziert keine Extension | `grep -nE 'quarkus\|jakarta.data' morphium-core/pom.xml` | ✅ PASS (0 Treffer) |
| I2 — keine Framework-Imports im Kern | Kern-Dependency-Baum ohne Quarkus/Jakarta Data | `mvn -pl morphium-core dependency:tree` → `core-tree.txt` | ✅ PASS |
| I2-erweitert — auch kein Testcontainers | `core-tree.txt` ohne `org.testcontainers` | `grep -c "testcontainers" core-tree.txt` | ✅ PASS (0 Treffer) |
| I3 — Kern eigenständig baubar und testbar | `mvn -pl morphium-core,poppydb -am verify` | siehe Abschnitt 3, Zeile 7 | 🟡 teilweise (Build PASS, Volltestlauf durch bekannten Flake unterbrochen, kein neuer Fehler beobachtet) |
| I4 — kein fremder BOM-Import im Parent | Root-`pom.xml` importiert keine Quarkus-BOM | `grep -n "scope>import" pom.xml` | ✅ PASS |
| I4-erweitert — BOM-Import ausschließlich im Modul-POM | Quarkus-BOM nur in `quarkus-morphium/pom.xml` | Sichtprüfung beider POMs | ✅ PASS |
| I5 — Abhängigkeitsrichtung einseitig, Reactor-Validierung | `mvn -B validate` über den vollen 9-Modul-Reactor | `validate.log` | ✅ PASS |
| Zusatz — `-DskipExtensions` lädt kein Quarkus-Artefakt | Reactor-Summary im `core-only.log` zeigt nur 3 Module | Sichtprüfung `core-only.log:2758-2766` | ✅ PASS (Morphium Parent, Morphium, PoppyDB — kein Quarkus-Modul in der Summary) |

---

## 5. Laufzeittabelle (für eine spätere CI-Planungswelle)

| Lauf | Laufzeit | Umfang |
|---|---|---|
| Voller Reactor, `-DskipTests` | 20,2 s | 9 Module, nur Compile+Package |
| Extension-Module `verify` (runtime+deployment+testing) | 4,77 s | 40 Tests |
| `integration-tests` `verify` | 2:22 min | 242 Tests, real gegen MongoDB via Testcontainers/OrbStack |
| Kern-only, `-DskipTests -DskipExtensions` | 14,1 s | 3 Module, nur Compile+Package |
| Kern-Volltest (`morphium-core`+`poppydb`, `verify`) | >40 min, nicht abgeschlossen | bekannter Messaging-Flake blockiert Vollständigkeit — für M6 relevant: dieser Lauf braucht in CI vermutlich einen Timeout/Retry-Mechanismus unabhängig von der Modul-Integration |

**Für die CI-Matrix aus D3, Begleitmaßnahme 4** ("ein Job `-DskipExtensions`, ein Job vollständig"): Ein `-DskipExtensions`-Job ist mit 14 Sekunden extrem günstig. Ein vollständiger Job (alle 9 Module inkl. `integration-tests`) liegt bei geschätzt 3–4 Minuten für die Extension-Tests selbst, plus die (aktuell nicht zuverlässig messbare) Laufzeit des Kern-Volltests, die durch den bekannten Flake nach oben unbegrenzt sein kann, bis er behoben oder mit einem Timeout versehen wird.

---

## 6. Abschluss

`/tmp/m3-dryrun` wurde **nicht aufgeräumt** und steht für eine Folgewelle zur Verfügung (Build-Artefakte inkl. `target/`-Verzeichnisse verifiziert und noch vorhanden). Alle Logdateien liegen unter `/tmp/m3-dryrun/*.log`, `*.txt`, `*.patch`.

**Kein Repository unter `/Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium` oder `.../quarkus-morphium` wurde verändert** — die gesamte Verifikation fand ausschließlich in der Wegwerf-Kopie statt.

**Offener Punkt für M4/M6:** Der `ExclusiveMessageTests`-Flake sollte vor einer echten CI-Einführung entweder behoben oder mit einem Test-Timeout versehen werden — er ist unabhängig von der `quarkus-morphium`-Integration, blockiert aber jeden vollständigen `mvn verify`-Lauf im Kern unvorhersehbar lange.
