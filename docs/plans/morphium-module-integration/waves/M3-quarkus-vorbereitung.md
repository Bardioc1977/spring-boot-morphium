# Welle M3 — `quarkus-morphium` als Morphium-Modul vorbereiten

| Feld | Wert |
|---|---|
| Meilenstein | M3 |
| Status | ⬜ TODO |
| Abhängig von | M2 **gemergt** (T2–T5); M3-T1 kann vorgezogen werden |
| Arbeitsort | `quarkus-morphium/`, Branch **`move-to-morphium`** |
| Ziel-Zustandsdokument | `status/<datum>-M3-quarkus-vorbereitung.md` |
| Agenten | 6 Sonnet-Agenten |

---

## Ziel

Der Branch `move-to-morphium` in `Bardioc1977/quarkus-morphium` enthält den
Stand, der als Verzeichnis `quarkus-morphium/` mit vier Submodulen in das
`morphium`-Repository übernommen werden kann — groupId migriert,
Extension-Guidelines nachweislich eingehalten, Doku konsistent.

Dies ist die umfangreichste Welle: 101 Java-Dateien, vier Submodule, eine
Antora-Doku, ein Testcontainers-basierter Integrationstest-Baum und eine
groupId-Migration.

---

## Besonderes Risiko dieser Welle

| Risiko | Gegenmaßnahme |
|---|---|
| Testcontainers braucht Docker; im Morphium-Reactor neu | M3-T5 macht `integration-tests` konditional; Kern-Contributor darf nicht an Docker scheitern |
| Extension-Guidelines sind umfangreich und teils implizit | M3-T1 ist ein reiner Audit-Task **vor** jeder Änderung, mit Quellenbelegen |
| groupId-Migration betrifft POMs, Metadaten, Doku, CI | M3-T2 arbeitet nach einer erschöpfenden `grep`-Liste, nicht nach Gefühl |
| Antora vs. MkDocs | M3-T4 nach D4-Empfehlung: koexistieren, nicht konvertieren |
| 62 Testdateien, Laufzeit unbekannt | M3-T6 misst und dokumentiert die Laufzeit für die CI-Planung in M6 |

---

## Task-Übersicht

| ID | Task | Modell | Vorziehbar vor M2-Merge | Status |
|---|---|---|---|---|
| M3-T1 | Quarkus-Extension-Guideline-Audit | sonnet | ✅ ja | ⬜ |
| M3-T2 | groupId-Migration + POM-Umbau auf morphium-parent | sonnet | ❌ nein | ⬜ |
| M3-T3 | Extension-Metadaten aktualisieren | sonnet | ❌ nein | ⬜ |
| M3-T4 | Doku: Antora anpassen + MkDocs-Übersichtsseite | sonnet | ✅ ja | ⬜ |
| M3-T5 | `integration-tests` konditional machen | sonnet | ❌ nein | ⬜ |
| M3-T6 | Integrations-Trockenlauf + Laufzeitmessung | sonnet | ❌ nein | ⬜ |

---

## M3-T1 — Quarkus-Extension-Guideline-Audit

Der Auftrag sagt: *„Wir halten uns selbstverständlich an alle Richtlinien von
Quarkus Extensions. Dies ist unbedingt nochmals zu prüfen."* Dieser Task ist
genau diese Prüfung — **vor** allen Änderungen, damit die Migration die
Konformität nicht versehentlich bricht.

**Prompt für den Sonnet-Agenten:**

````
AUFGABE: Vollständiges Konformitätsaudit der Quarkus-Extension
/Volumes/Entwicklung/workspaces/porsche/morphium-workspace/quarkus-morphium
gegen die offiziellen Quarkus-Extension-Richtlinien. Dies ist ein reiner
ANALYSE-Task: Du änderst keine einzige Datei im Projekt, außer dem Bericht.

QUELLEN — rufe diese ab (WebFetch) und arbeite mit dem tatsächlichen Inhalt,
nicht aus dem Gedächtnis:
- https://quarkus.io/guides/building-my-first-extension
- https://quarkus.io/guides/writing-extensions
- https://quarkus.io/guides/extension-metadata
- https://quarkus.io/guides/writing-native-applications-tips
- https://hub.quarkiverse.io/checklistfornewprojects/ (Quarkiverse-Checkliste,
  soweit erreichbar — sie ist die konkreteste Sammlung von Konventionen)
Wenn eine URL nicht erreichbar ist, notiere das und suche die Information über
WebSearch. Verlasse dich nicht auf Vorwissen — Quarkus-Konventionen ändern sich
zwischen Versionen.

PRÜFPUNKTE — für jeden: Status (ERFÜLLT / VERLETZT / TEILWEISE / N.A.), Belegstelle
im Projekt (Datei:Zeile) und, bei Abweichung, eine konkrete Handlungsempfehlung:

Struktur:
1. Trennung runtime/deployment vorhanden und korrekt
2. deployment-Modul hängt von runtime ab, nicht umgekehrt
3. Kein Quarkus-Deployment-Artefakt in runtime-Abhängigkeiten
4. artifactId-Konvention: `quarkus-<name>` für runtime,
   `quarkus-<name>-deployment` für deployment
5. `quarkus-extension-maven-plugin` mit Goal `extension-descriptor` in runtime
6. `quarkus-extension-processor` als annotationProcessorPath in beiden Modulen
7. Dependency-Parität runtime ↔ deployment (jede runtime-Extension-Dependency
   braucht ihr deployment-Gegenstück; die Extension-Validierung prüft das)
8. Optionale Dependencies korrekt paarweise optional gesetzt

Metadaten:
9. `src/main/resources/META-INF/quarkus-extension.yaml` vorhanden und valide
10. Pflichtfelder: name, description, metadata.keywords, metadata.categories,
    metadata.status, metadata.guide
11. `metadata.config` mit den Config-Präfixen
12. `built-with-quarkus-core`, `requires-quarkus-core`, `minimum-java-version`
13. `status`: ist "preview" für den aktuellen Reifegrad angemessen? Begründen.

Konfiguration:
14. `@ConfigMapping` / `@ConfigRoot` korrekt verwendet, Build-Time vs. Runtime
    korrekt getrennt
15. Config-Doku wird generiert (quarkus-extension-processor)
16. Config-Präfix `quarkus.morphium.*` durchgängig

Build-Steps:
17. `@BuildStep`-Methoden: keine unnötige Arbeit zur Build-Zeit
18. `@Record(RUNTIME_INIT)` vs. `STATIC_INIT` korrekt gewählt
19. `FeatureBuildItem` wird produziert
20. Native-Image: `ReflectiveClassBuildItem` für alle @Entity/@Embedded;
    `RuntimeInitializedClassBuildItem` wo nötig
21. `Capabilities`-Gating für optionale Integrationen (Jackson, JSON-B, Health)

Dev-Erfahrung:
22. Dev Services korrekt implementiert (`DevServicesResultBuildItem`)
23. Dev UI Integration (`CardPageBuildItem`)
24. Live-Reload-Verhalten sinnvoll

Tests:
25. Extension-Tests im deployment-Modul mit `QuarkusUnitTest`
26. integration-tests-Modul separat, wird nicht deployed
27. Native-Test-Profil vorhanden?

Sonstiges:
28. `quarkus-extension.yaml` `guide`-URL erreichbar
29. Lizenzheader-Konvention des Zielprojekts (Morphium nutzt KEINE
    Lizenzheader in Quelldateien — prüfe, ob die Extension welche hat, und
    bewerte, ob das nach der Integration zu vereinheitlichen ist)
30. Deprecated Quarkus-APIs in Verwendung? (Extension ist gegen 3.32.3 gebaut;
    prüfe die Build-Ausgabe auf Deprecation-Warnungen)

ZUSÄTZLICH: Prüfe, welche der Punkte durch die kommende groupId-Migration
(io.quarkiverse.morphium → de.caluga) und die Einbettung in den
Morphium-Reactor BERÜHRT werden. Das ist der wichtigste Teil des Berichts, weil
er die Aufgabenliste für M3-T2 und M3-T3 definiert. Lies dazu
/Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium-jakarta-data/docs/plans/morphium-module-integration/decisions/D2-groupid-namespace.md
und D3-reactor-strategie.md.

Führe zur Verifikation aus:
  cd /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/quarkus-morphium
  mvn -B -DskipTests install > /tmp/m3-t1-build.log 2>&1
Lies die Logdatei mit dem Read-Tool aus (nicht pipen) und werte Warnungen der
Extension-Validierung aus — das quarkus-extension-maven-plugin meldet
Paritätsverletzungen dort.
Hinweis: Der Build braucht morphium und morphium-jakarta-data im lokalen
Repository. Falls sie fehlen, installiere sie zuerst mit -DskipTests aus den
Workspace-Verzeichnissen.

BERICHT: Schreibe nach
/Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium-jakarta-data/docs/plans/morphium-module-integration/reports/M3-T1-extension-audit.md
Struktur: Zusammenfassung (wie viele erfüllt/verletzt), dann die Prüftabelle mit
allen 30 Punkten, dann ein Abschnitt "Von der Migration betroffene Punkte" als
priorisierte Aufgabenliste, dann ein Abschnitt "Offene Fragen".

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- JEDE Änderung an Projektdateien. Dies ist ein Analyse-Task. Die einzige Datei,
  die du schreibst, ist der Bericht.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.

BERICHTE am Ende: die Zusammenfassung und die Liste der von der Migration
betroffenen Punkte.
````

**Verifikation (Orchestrator):** Bericht lesen. Bei „alles erfüllt" ohne einen
einzigen Befund skeptisch sein — bei 30 Prüfpunkten und einer Extension mit
`status: preview` ist das unwahrscheinlich. Dann gezielt nachfragen (SendMessage)
zu Punkt 7 (Dependency-Parität) und 20 (Native-Image), den erfahrungsgemäß
kritischen Stellen.

---

## M3-T2 — groupId-Migration und POM-Umbau

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/quarkus-morphium
auf dem Branch `move-to-morphium`. Prüfe mit `git branch --show-current`.

LIES ZUERST:
- docs/plans/morphium-module-integration/decisions/D2-groupid-namespace.md
- docs/plans/morphium-module-integration/decisions/D1-versionierung.md
- docs/plans/morphium-module-integration/decisions/D3-reactor-strategie.md
- docs/plans/morphium-module-integration/reports/M3-T1-extension-audit.md
  (Abschnitt "Von der Migration betroffene Punkte")
- /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium/poppydb/pom.xml
  (Vorbild für ein Morphium-Modul-POM)
- /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium/morphium-jakarta-data/pom.xml
  (das in M2 integrierte Modul — dein direktes Vorbild)

SCHRITT 1 — Vollständige Bestandsaufnahme, BEVOR du etwas änderst:
  grep -rn "io.quarkiverse" --include=pom.xml --include="*.yaml" --include="*.yml" \
    --include="*.java" --include="*.adoc" --include="*.md" --include="*.properties" . \
    | grep -v "/target/"
  grep -rn "1\.2\.0" --include=pom.xml . | grep -v "/target/"
  grep -rn "Bardioc1977" . --include="*.xml" --include="*.yaml" --include="*.yml" \
    --include="*.adoc" --include="*.md" | grep -v "/target/"
Erstelle daraus eine Trefferliste mit je einer Entscheidung (ändern / bleibt /
entfällt). Diese Liste ist Teil des Abschlussberichts und deine Arbeitsgrundlage.

SCHRITT 2 — POM-Umbau:
a) ./pom.xml wird vom Standalone-Parent zum Zwischenmodul:
   - <parent> auf de.caluga:morphium-parent:6.2.6-SNAPSHOT
   - artifactId bleibt `quarkus-morphium-parent`, packaging bleibt `pom`
   - KEIN eigenes <groupId>, KEIN eigenes <version> (geerbt)
     Kommentar darüber:
     <!-- ENTSCHEIDUNG-OFFEN D1: Lockstep mit Morphium-Version. Bei Variante A
          hier <version> und <morphium.version> ergänzen. -->
   - <modules> bleibt: runtime, deployment, testing, integration-tests
     (integration-tests wird in M3-T5 konditional gemacht — hier nicht anfassen)
   - Quarkus-BOM-Import BLEIBT in DIESEM POM. Er darf NICHT nach
     morphium-parent wandern — sonst löst jeder Kern-Build ~400
     Quarkus-Artefakte auf. Setze einen Kommentar, der das festhält und auf
     D3/Invariante I4 verweist.
   - <quarkus.version> bleibt hier als Property. Setze den Kommentar:
     <!-- M4: Verschiebung nach morphium-parent prüfen (siehe D1, Absicherung B6) -->
   - Entferne: <distributionManagement> (GitHub Packages unter Bardioc1977),
     <scm>, <issueManagement>, <developers>, <licenses>, <url> — alles kommt vom
     morphium-parent.
   - Entferne Properties, die der Parent liefert: maven.compiler.*,
     project.build.sourceEncoding.
   - <morphium.version> und <morphium-jakarta-data.version> ENTFERNEN; die
     Abhängigkeiten auf de.caluga:morphium und de.caluga:morphium-jakarta-data
     nutzen künftig ${project.version}.
   - <jakarta.data.version> ENTFERNEN — die Property steht seit M2 im
     morphium-parent.
   - Plugin-Versionen in pluginManagement: behalte nur, was der morphium-parent
     NICHT führt (insbesondere quarkus-extension-maven-plugin). Prüfe das
     gegen den Parent.

b) runtime/pom.xml, deployment/pom.xml, testing/pom.xml, integration-tests/pom.xml:
   - <parent> auf io.quarkiverse.morphium:quarkus-morphium-parent umstellen auf
     de.caluga:quarkus-morphium-parent — beachte: die Parent-Referenz braucht
     eine explizite <version>, hier ${project.version} ist NICHT erlaubt.
     Verwende 6.2.6-SNAPSHOT.
     Prüfe, wie morphium-jakarta-data/pom.xml und poppydb/pom.xml das lösen, und
     mach es identisch.
   - Alle internen Modulreferenzen: groupId io.quarkiverse.morphium →
     ${project.groupId} verwenden (dann bleibt es künftig automatisch korrekt),
     Version → ${project.version}.
   - artifactIds bleiben UNVERÄNDERT: quarkus-morphium,
     quarkus-morphium-deployment, quarkus-morphium-testing.

SCHRITT 3 — Konsistenzprüfung:
  grep -rn "io.quarkiverse" --include=pom.xml . | grep -v "/target/"
Es darf danach KEIN Treffer in einer pom.xml mehr geben. Treffer in .adoc/.md
behandelt M3-T4, Treffer in quarkus-extension.yaml behandelt M3-T3 — lass sie
hier stehen und liste sie im Bericht.

VERIFIKATION, die du selbst ausführen musst:
Voraussetzung: morphium 6.2.6-SNAPSHOT (inkl. morphium-jakarta-data) muss lokal
installiert sein:
  cd /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium
  git branch --show-current    # notiere den Branch im Bericht
  mvn -B install -DskipTests
Dann:
  cd /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/quarkus-morphium
  mvn -B install -DskipTests > /tmp/m3-t2-build.log 2>&1
  mvn -B verify -pl runtime,deployment,testing > /tmp/m3-t2-verify.log 2>&1
Lies die Logdateien mit dem Read-Tool (nicht pipen). Beide müssen erfolgreich
sein. Prüfe in der Ausgabe auch die Meldungen des
quarkus-extension-maven-plugin — Paritätsfehler erscheinen dort.
integration-tests kann in diesem Task fehlschlagen (Docker) — das ist erwartet
und Aufgabe von M3-T5. Notiere den Fehler, behebe ihn nicht.

Committe in maximal 3 Commits, z.B.
  "build: migrate groupId to de.caluga"
  "build: convert to morphium-parent module POMs"

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Jede Änderung im Repository
  /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium
  (dort darfst du ausschließlich `mvn install` ausführen).
- Änderungen an Java-Quellcode. Dieser Task ist reine POM-Arbeit. Die
  Java-Pakete (de.caluga.morphium.quarkus.*) bleiben unverändert — die
  groupId-Migration betrifft KEINE Paketnamen.
- Änderungen an .adoc/.md-Dateien (macht M3-T4) und an
  quarkus-extension.yaml (macht M3-T3).
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.

BERICHTE am Ende: die vollständige Trefferliste aus Schritt 1 mit Entscheidungen,
die Diffs aller fünf POMs, die Build-Ergebnisse, und die verbleibenden
io.quarkiverse-Treffer außerhalb der POMs (Arbeitsvorrat für T3/T4).
````

---

## M3-T3 — Extension-Metadaten aktualisieren

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/quarkus-morphium
auf dem Branch `move-to-morphium`.

LIES ZUERST den Audit-Bericht
docs/plans/morphium-module-integration/reports/M3-T1-extension-audit.md
und D2-groupid-namespace.md.

AUFGABE: Aktualisiere die Extension-Metadaten für die Integration in
sboesebeck/morphium.

DATEI: runtime/src/main/resources/META-INF/quarkus-extension.yaml
(Die Datei unter runtime/target/ ist generiert — nicht anfassen.)

Änderungen:
1. `metadata.guide`: zeigt heute auf
   github.com/Bardioc1977/quarkus-morphium/blob/main/docs/modules/ROOT/pages/index.adoc.
   Umstellen auf die Zieladresse im Hauptprojekt. Kläre zuerst durch Prüfung von
   /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium/mkdocs.yml
   und deploy_docs.sh, wohin die veröffentlichte Doku geht
   (sboesebeck.github.io/morphium/) und wähle die stabilste erreichbare URL.
   Begründe die Wahl im Bericht.
2. `metadata.scm-url` (falls in der Quelldatei vorhanden): auf
   https://github.com/sboesebeck/morphium
3. `built-with-quarkus-core` / `requires-quarkus-core`: prüfe gegen die
   tatsächliche quarkus.version im POM. `requires-quarkus-core` muss eine offene
   Obergrenze haben ("[3.32,)") — das ist die Absicherung gegen den
   Lockstep-Haupteinwand aus D1 (Punkt B6). Wenn es anders ist, korrigieren.
4. `metadata.status`: Bewerte anhand des Audit-Berichts, ob "preview" weiter
   angemessen ist oder auf "stable" gehoben werden sollte. Empfehlung mit
   Begründung im Bericht; ändere den Wert nur, wenn der Audit-Bericht das
   stützt. Im Zweifel "preview" belassen — eine zu optimistische
   Reifegradangabe ist schädlicher als eine konservative.
5. `metadata.categories`, `keywords`, `config`: auf Vollständigkeit prüfen.
   Vergleiche `config` mit den tatsächlichen Config-Präfixen im Code
   (grep nach @ConfigMapping und @ConfigRoot in runtime/src/main/java).
6. `minimum-java-version`: gegen den Parent prüfen (21).

WEITERE DATEIEN, die zu prüfen sind:
- runtime/src/main/resources-filtered/** — enthält gefilterte Ressourcen; prüfe
  auf Versions- oder Koordinaten-Platzhalter, die von der groupId-Änderung
  betroffen sind.
- Alle Dateien unter src/main/resources/META-INF/ in allen Modulen.
- Suche projektweit nach Verweisen auf die alte groupId in Nicht-POM-Dateien
  (ohne .adoc/.md, die macht M3-T4):
  grep -rn "io.quarkiverse" --include="*.yaml" --include="*.yml" \
    --include="*.properties" --include="*.java" . | grep -v "/target/"

VERIFIKATION, die du selbst ausführen musst:
  python3 -c "import yaml; yaml.safe_load(open('runtime/src/main/resources/META-INF/quarkus-extension.yaml'))"
  mvn -B -pl runtime install -DskipTests > /tmp/m3-t3.log 2>&1
Lies die Logdatei mit dem Read-Tool. Prüfe danach die GENERIERTE Datei
runtime/target/classes/META-INF/quarkus-extension.yaml: Das Feld `artifact` muss
jetzt `de.caluga:quarkus-morphium::jar:6.2.6-SNAPSHOT` lauten. Wenn dort noch
io.quarkiverse steht, ist die POM-Migration aus M3-T2 unvollständig — melden,
nicht selbst reparieren.
Prüfe die `guide`-URL mit WebFetch auf Erreichbarkeit. Wenn sie noch nicht
existiert (weil die Doku erst mit M4 veröffentlicht wird), ist das in Ordnung —
notiere es als bekannten Zustand im Bericht.

Committe als "build: update extension metadata for morphium integration".

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Änderungen an pom.xml-Dateien (das war M3-T2).
- Änderungen an .adoc/.md-Dateien (macht M3-T4).
- Änderungen an Dateien unter target/.
- Jede Änderung im morphium-Repository.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.

BERICHTE am Ende: den Diff der quarkus-extension.yaml, den Inhalt der
generierten Datei aus target/, deine Begründung zur guide-URL und zum
status-Feld, und die Config-Präfix-Gegenprüfung.
````

---

## M3-T4 — Dokumentation

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/quarkus-morphium
auf dem Branch `move-to-morphium`.

LIES ZUERST docs/plans/morphium-module-integration/decisions/D4-build-release-workflow.md,
Abschnitt "Teilfrage 4: Dokumentations-Toolchain". Die Entscheidung lautet:
Antora-Doku bleibt im Modulverzeichnis, ZUSÄTZLICH eine MkDocs-Übersichtsseite
im Hauptprojekt. KEINE Formatkonvertierung.

AUFGABE 1 — Antora-Doku aktualisieren:
Dateien: docs/antora.yml, docs/modules/ROOT/nav.adoc,
docs/modules/ROOT/pages/*.adoc (13 Seiten),
docs/modules/ROOT/pages/includes/attributes.adoc
- Alle Maven-Koordinaten auf de.caluga umstellen (groupId), Versionen auf die
  Morphium-Version. Prüfe, ob attributes.adoc Versionsattribute definiert, die
  zentral geändert werden können — dann dort ändern statt an 13 Stellen.
- Alle Links auf github.com/Bardioc1977/* auf sboesebeck/morphium umstellen.
- Build-Anleitungen: aus "git clone morphium, git clone morphium-jakarta-data,
  dann bauen" wird "mvn -pl quarkus-morphium -am verify im Morphium-Repo".
- Abschnitt oder Hinweisblock ergänzen: Dies ist ein optionales Modul von
  Morphium; der Kern hängt nicht davon ab.
- Prüfe jede Seite auf inhaltlich veraltete Aussagen, die sich durch die
  Integration ändern (z.B. "Morphium ist nicht auf Maven Central verfügbar" —
  falls so eine Aussage existiert, ist sie nach der Integration falsch).

AUFGABE 2 — MkDocs-Übersichtsseite für das Hauptprojekt:
Erstelle `docs-for-morphium/quarkus-extension.md`. Diese Datei wird in M4 als
docs/quarkus-extension.md ins Morphium-Repo kopiert.
Stil- und Strukturreferenz:
  /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium/docs/poppydb.md
  /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium/docs/jakarta-data.md
  (falls M2 gemergt ist; sonst die Vorlage aus
   morphium-jakarta-data/docs-for-morphium/jakarta-data.md)
Nutze nur Markdown-Extensions, die morphium/mkdocs.yml aktiviert.
Inhalt: Was die Extension leistet (CDI-Producer, @ConfigMapping-Konfiguration,
@MorphiumTransactional, Health Checks, Dev Services, Dev UI, Jakarta-Data-
Repositories via Gizmo-Bytecode-Generierung zur Build-Zeit, Native-Image-Support,
MorphiumId-JSON-Serialisierung, Migrations-Runner); Installation mit
Koordinaten; die wichtigsten quarkus.morphium.*-Properties als Tabelle
(verifiziert gegen runtime/src/main/java/**/MorphiumRuntimeConfig.java und die
weiteren Config-Klassen — erfinde keine Property); Verweis auf die
ausführliche Antora-Doku; Abschnitt Optionalität.
Umfang: eine Übersichtsseite, die auf die Antora-Doku führt — nicht deren
Duplikat.

AUFGABE 3 — README.md, CHANGELOG.md, RELEASES.md, CLAUDE.md:
- README.md (19 kB): auf Modulstatus umschreiben. Badges entfernen, die sich auf
  Bardioc1977-Workflows beziehen. Koordinaten aktualisieren. Klarstellen, dass
  es ein Morphium-Modul ist. Migrationstabelle alt → neu für bestehende Nutzer
  (io.quarkiverse.morphium:quarkus-morphium:1.2.0 → de.caluga:quarkus-morphium:6.2.x).
- CHANGELOG.md: Eintrag für die Integration im Format des Hauptprojekts (lies
  /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium/CHANGELOG.md
  für Ton und Struktur).
- RELEASES.md: prüfen, ob nach der Integration noch sinnvoll. Wenn der Inhalt
  vom Morphium-Release-Prozess abgelöst wird: NICHT löschen, sondern im Bericht
  zur Löschung in M4 vorschlagen.
- CLAUDE.md: enthält projektspezifische Anweisungen. Auf Aktualität prüfen und
  in MIGRATION-NOTES.md bewerten (kommt mit / entfällt).

AUFGABE 4 — MIGRATION-NOTES.md erstellen:
Tabelle aller Dateien und Verzeichnisse im Wurzelverzeichnis mit Entscheidung
"kommt mit" / "entfällt" plus Begründung. Zu bewerten: LICENSE,
CODE_OF_CONDUCT.md, CONTRIBUTING.md, SECURITY.md, CLAUDE.md, RELEASES.md,
.github/**, .editorconfig, .gitignore, .mcp.json, antora-playbook.yml, build/,
deployment/ (Verzeichnis "deployment" ist das Maven-Modul — nicht verwechseln
mit dem gleichnamigen Deploy-Ordner, prüfe was drin ist), docs/,
docs-for-morphium/. Faustregel: was das Hauptprojekt auf Repo-Ebene schon hat,
entfällt. Lösche nichts.

VERIFIKATION, die du selbst ausführen musst:
  grep -rn "Bardioc1977" docs/ README.md CHANGELOG.md docs-for-morphium/
  grep -rn "io.quarkiverse" docs/ README.md docs-for-morphium/
Beide sollen nur noch bewusst gesetzte Historien-/Migrationshinweise treffen —
liste jeden verbleibenden Treffer mit Begründung auf.
Alle relativen Links prüfen: Zieldatei existiert? Tabelle mit OK/FEHLT.
Alle Property-Namen in der Übersichtsseite gegen den Code belegen (Datei:Zeile).

Committe in 3–4 thematischen Commits.

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Änderungen an pom.xml, quarkus-extension.yaml oder Java-Code.
- Löschen von Dateien.
- Jede Änderung im morphium-Repository (auch nicht an dessen mkdocs.yml — das
  macht M4).
- Konvertierung von AsciiDoc nach Markdown oder umgekehrt.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.

BERICHTE am Ende: MIGRATION-NOTES.md im Volltext, die Link-Prüftabelle, die
Property-Belegtabelle, verbleibende Bardioc1977-/io.quarkiverse-Treffer mit
Begründung, und den vorgeschlagenen mkdocs.yml-Navigationseintrag.
````

---

## M3-T5 — `integration-tests` konditional machen

**Prompt für den Sonnet-Agenten:**

````
Du arbeitest im Repository /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/quarkus-morphium
auf dem Branch `move-to-morphium`.

PROBLEM: Das Modul integration-tests nutzt Testcontainers (Dev Services mit
MongoDBContainer) und braucht damit Docker. Nach der Integration in den
Morphium-Reactor würde ein Kern-Contributor ohne Docker beim Vollbau scheitern.
Das widerspricht der Zusage aus D3, dass die Erweiterungen den Kern-Build nicht
belasten.

LIES ZUERST docs/plans/morphium-module-integration/decisions/D3-reactor-strategie.md,
Abschnitt "Begleitmaßnahmen", Punkt 3.

AUFGABE: Mach die Ausführung der Integrationstests konditional, ohne sie
abzuschalten.

SCHRITT 1 — Analysieren, bevor du änderst:
- Welche Tests unter integration-tests/src/test brauchen tatsächlich Docker?
  Sieh dir insbesondere an, welche das InMemMorphiumTestProfile bzw. den
  In-Memory-Treiber nutzen (die brauchen KEIN Docker) und welche Dev Services
  mit Container starten.
- Prüfe die Dev-Services-Konfiguration: gibt es eine Property, mit der
  Dev Services abgeschaltet werden können
  (quarkus.morphium.devservices.enabled o.ä.)? Lies dazu
  deployment/src/main/java/**/MorphiumDevServicesBuildTimeConfig.java.
- Ergebnis: eine Tabelle Test → braucht Docker (ja/nein) → Begründung.

SCHRITT 2 — Lösung umsetzen. Bewerte diese Optionen und wähle begründet:
  Option A: integration-tests im morphium-parent nur in einem eigenen Profil
            (z.B. -DskipITs steuert es), Standard: aktiv.
  Option B: Docker-Verfügbarkeit prüfen und die containerbasierten Tests per
            JUnit-Annotation (@EnabledIf / @DisabledIfSystemProperty /
            org.testcontainers.DockerClientFactory) überspringen statt den Build
            zu brechen.
  Option C: Kombination — Modul baut immer, containerbasierte Tests
            überspringen sich selbst bei fehlendem Docker.
  EMPFEHLUNG: Option C ist am robustesten, weil sie ohne Reactor-Sonderfall
  funktioniert und der Nutzer eine klare Skip-Meldung sieht. Prüfe, ob
  Testcontainers dafür einen etablierten Mechanismus mitbringt, und nutze den,
  statt selbst zu erfinden.
  Wenn du eine andere Option wählst: begründen.

SCHRITT 3 — Nachweis:
- Mit Docker verfügbar: Tests laufen und sind grün.
- Simuliere fehlendes Docker und belege, dass der Build NICHT scheitert, sondern
  die betroffenen Tests übersprungen werden. Prüfe zunächst mit
  `docker info` bzw. `command -v docker`, was verfügbar ist, und beschreibe im
  Bericht, wie du das Fehlen simuliert hast (z.B. DOCKER_HOST auf einen
  ungültigen Wert). Wenn eine belastbare Simulation nicht möglich ist: NICHT
  raten — im Bericht als offene Verifikation kennzeichnen, damit M6 es in CI
  prüfen kann.

VERIFIKATION, die du selbst ausführen musst:
  mvn -B verify -pl integration-tests > /tmp/m3-t5-with-docker.log 2>&1
  # dann ohne Docker:
  <deine Simulation> mvn -B verify -pl integration-tests > /tmp/m3-t5-no-docker.log 2>&1
Lies beide Logdateien mit dem Read-Tool (nicht pipen). Dokumentiere jeweils
Tests run / Failures / Errors / Skipped.
Messe zusätzlich die Laufzeit beider Läufe — die Zahl braucht M6 für die
CI-Planung.

STRIKTES NICHT-ZIEL: Keine Tests löschen, keine Tests inhaltlich ändern, keine
Assertion abschwächen. Wenn ein Test fachlich fehlschlägt: melden, nicht
anpassen.

Committe als "test: skip container-based integration tests when docker is absent".

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Löschen oder inhaltliches Abschwächen von Tests.
- Jede Änderung im morphium-Repository.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.

BERICHTE am Ende: die Test→Docker-Tabelle, die gewählte Option mit Begründung,
beide Testläufe mit Zahlen und Laufzeiten, und die verbleibenden offenen
Verifikationspunkte.
````

---

## M3-T6 — Integrations-Trockenlauf

Analog M1-T5, aber mit vier Submodulen und Laufzeitmessung.

**Prompt für den Sonnet-Agenten:**

````
AUFGABE: Trockenlauf der Integration von quarkus-morphium als Modul in Morphium,
in einer WEGWERF-KOPIE.

VORGEHEN:
1. rm -rf /tmp/m3-dryrun && mkdir -p /tmp/m3-dryrun
   cp -R /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium /tmp/m3-dryrun/morphium
   Notiere im Bericht, welcher Branch dort ausgecheckt ist. Erwartung: ein Stand,
   der morphium-jakarta-data bereits als Modul enthält (M2 gemergt). Wenn nicht,
   melde das und brich ab — ohne M2 ist dieser Trockenlauf nicht aussagekräftig.
2. Kopiere aus dem Branch move-to-morphium von
   /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/quarkus-morphium
   nach /tmp/m3-dryrun/morphium/quarkus-morphium/ genau die Dateien, die
   MIGRATION-NOTES.md als "kommt mit" ausweist (in der Regel: pom.xml, runtime/,
   deployment/, testing/, integration-tests/, docs/, README.md, CHANGELOG.md).
   Ohne .git, ohne target/, ohne docs-for-morphium, ohne MIGRATION-NOTES.md.
3. /tmp/m3-dryrun/morphium/pom.xml: <module>quarkus-morphium</module> im Profil
   `extensions` ergänzen (siehe D3).
4. Verifikation ausführen, jedes Ergebnis protokollieren:
   cd /tmp/m3-dryrun/morphium
   mvn -B install -DskipTests > /tmp/m3-dryrun/install.log 2>&1
   mvn -B verify -pl quarkus-morphium/runtime,quarkus-morphium/deployment,quarkus-morphium/testing > /tmp/m3-dryrun/verify-ext.log 2>&1
   mvn -B install -DskipTests -DskipExtensions > /tmp/m3-dryrun/core-only.log 2>&1
   mvn -q -pl morphium-core dependency:tree > /tmp/m3-dryrun/core-tree.txt
   Logdateien mit dem Read-Tool auswerten, nicht pipen.
5. INVARIANTEN I1–I5 aus D3 prüfen, jede mit Befehl und Ergebnis. Zusätzlich
   für diese Welle kritisch:
   I2-erweitert: core-tree.txt enthält KEIN io.quarkus und KEIN
                 org.testcontainers
   I4-erweitert: /tmp/m3-dryrun/morphium/pom.xml importiert KEINE Quarkus-BOM;
                 der BOM-Import steht ausschließlich in
                 quarkus-morphium/pom.xml
   Prüfe außerdem, dass der -DskipExtensions-Build KEIN Quarkus-Artefakt
   heruntergeladen hat (Reactor-Summary und Downloadzeilen im Log auswerten).
6. LAUFZEITMESSUNG — für die CI-Planung in M6 gebraucht:
   Messe und dokumentiere: (a) Dauer `mvn install -DskipTests` gesamt,
   (b) Dauer `verify` der drei Extension-Module, (c) Dauer der
   integration-tests, (d) Dauer `install -DskipTests -DskipExtensions`.
   Nutze `time` oder die Maven-Ausgabe "Total time".
7. Bericht nach
   /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium-jakarta-data/docs/plans/morphium-module-integration/reports/M3-T6-dryrun.md
   mit: Befehlstabelle PASS/FAIL, Invariantentabelle, Laufzeittabelle, dem
   exakten morphium/pom.xml-Diff als Copy-Paste-Patch für M4, und der
   Kopierliste.
8. /tmp/m3-dryrun NICHT aufräumen.

VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Jede Änderung unter
  /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium oder
  /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/quarkus-morphium.
  Du arbeitest ausschließlich in /tmp/m3-dryrun und schreibst eine Berichtsdatei.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.

BERICHTE am Ende: Invariantentabelle, Laufzeittabelle, pom.xml-Patch.
````

**Verifikation (Orchestrator):**
```bash
cd morphium && git status --short          # unverändert
cd ../quarkus-morphium && git status --short   # unverändert
```

---

## Abschluss der Welle M3

1. Gesamtverifikation: `cd quarkus-morphium && mvn -B verify`
2. Zustandsdokument `status/<datum>-M3-quarkus-vorbereitung.md`.
3. `STATE.md` und Gesamtplan aktualisieren.
4. JF-Dokument aktualisieren.
5. Bericht an den Auftraggeber, inklusive der Frage nach dem Push des Branches.

## Definition of Done

- [ ] Audit-Bericht mit allen 30 Prüfpunkten, Befunde adressiert oder als
      bewusste Abweichung dokumentiert
- [ ] groupId in allen POMs `de.caluga`, artifactIds unverändert
- [ ] Vier Submodule mit `quarkus-morphium-parent` → `morphium-parent`
- [ ] Quarkus-BOM-Import ausschließlich in `quarkus-morphium/pom.xml` (I4)
- [ ] `quarkus-extension.yaml` aktualisiert; generierte `artifact`-Koordinate
      zeigt `de.caluga`
- [ ] Antora-Doku aktualisiert, keine offenen Bardioc1977-Verweise
- [ ] `docs-for-morphium/quarkus-extension.md` vorhanden, Properties gegen Code belegt
- [ ] `MIGRATION-NOTES.md` mit Dateiinventar
- [ ] Integrationstests überspringen sich ohne Docker, Build bleibt grün
- [ ] Trockenlauf mit allen Invarianten PASS, Laufzeiten gemessen
- [ ] `morphium/`-Repository unverändert
- [ ] Zustandsdokument geschrieben, `STATE.md` und Gesamtplan aktualisiert
