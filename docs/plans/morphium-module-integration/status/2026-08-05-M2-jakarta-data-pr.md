# Welle M2 — PR: `morphium-jakarta-data` als Morphium-Modul

| Feld | Wert |
|---|---|
| Meilenstein | M2 |
| Thema | Modul ins `morphium`-Repository übernehmen, Review-PR gegen eigenen Fork, alle Review-Findings beheben, Upstream-PR-Text vorbereiten |
| Erstellt am | 2026-08-05 |
| Erstellt von | Orchestrator-Session 2 |
| Ergebnis | ✅ abgeschlossen (bis auf den Upstream-PR selbst — wartet auf Freigabe) |
| Nächste Welle | M3 (`quarkus-morphium` vorbereiten) — Vorbedingung: M2-Upstream-PR gemergt |

---

## 1. Was in dieser Welle erreicht wurde

Der Branch `pr/jakarta-data-module` im `morphium`-Repository enthält jetzt `morphium-jakarta-data` als vollständig integriertes, optionales Modul: Reactor-Registrierung über das Profil `extensions`, MkDocs-Dokumentation, CHANGELOG-Eintrag und eine erweiterte `release.sh`, die das Modul als viertes Central-Artefakt in einem generischen Modul-Registry-Muster mitführt statt eines dritten Copy-Paste-Blocks. Vor der Vorlage an den Auftraggeber wurde der PR zusätzlich einem echten, kostenlosen Community-Code-Review unterzogen: der Branch wurde als PR #16 gegen den *Default-Branch* des eigenen Forks (`Bardioc1977/morphium:master`, zuvor per Fast-Forward-Sync-PR #15 auf den aktuellen Upstream-Stand gebracht) gestellt, wodurch CodeRabbit, GitHub Copilot und der Codex-Connector automatisch anschlugen. Diese drei Reviewer fanden zusammen 27 konkrete Fundstellen, davon 21 mit echtem funktionalem Bug-Charakter (nicht nur Stil). Alle 21 wurden trianiert (P0–P3) und bis auf einen einzigen, der außerhalb dieses Repositories liegt (Jakarta-Data-`@OrderBy`-Attribute `ignoreCase`/`descending`/Repeatable brauchen einen Annotation-Processor, den `morphium-jakarta-data` als reine Runtime-Bibliothek nicht enthält), behoben und mit 32 neuen Regressionstests abgesichert. Der finale CodeRabbit-Lauf gegen den reparierten Stand meldete keine neuen Findings mehr.

---

## 2. Taskbilanz

| Task | Status | Agent-Modell | Vom Orchestrator verifiziert am | Commits |
|---|---|---|---|---|
| M2-T1 Modul übernehmen, Reactor+Parent anpassen | ✅ | sonnet | 2026-08-05 | `9c52cea80`, `59405119b` |
| M2-T2 Doku-Anbindung | ✅ | sonnet | 2026-08-05 | `4398414de`, `09d6c1c74` |
| M2-T3 `release.sh` erweitern | ✅ | sonnet | 2026-08-05 | `cdc468ce6` |
| Review-Vorbereitung: Fork-`master`-Sync | ✅ | Orchestrator | 2026-08-05 | PR #15 (Bardioc1977/morphium), gemergt |
| Review-PR gegen Fork-`master` erstellt | ✅ | Orchestrator | 2026-08-05 | PR #16 (Bardioc1977/morphium) |
| Review-Triage (CodeRabbit + Copilot + Codex) | ✅ | Orchestrator | 2026-08-05 | `reports/M2-review-triage.md` |
| P0-Bugfixes (9 Findings) | ✅ | sonnet (3× parallel) | 2026-08-05 | `9dc86b0fe`, `cf4f72e03`, `420725a31` |
| P1/P2-Bugfixes (8 von 9 Findings) | ✅ | sonnet (3× parallel) | 2026-08-05 | `c7aa6f3c9`, `a80d5d52c`, `9fb2d0c04` |
| Classloader-Fallback-Fix (nachgetragen) | ✅ | Orchestrator | 2026-08-05 | `cb9950f9a` |
| M2-T4 Vollverifikation vor PR | ✅ | sonnet | 2026-08-05 | — (Bericht `reports/M2-T4-verification.md`) |
| M2-T5 Upstream-PR-Text formuliert | 🟡 vorgelegt, wartet auf Freigabe | Orchestrator | — | — |

**Nacharbeiten, die nötig waren:**
- Die ursprüngliche `morphium-jakarta-data/pom.xml` referenzierte `morphium-parent:6.2.6-SNAPSHOT` (Stand eines veralteten lokalen `morphium`-Checkouts aus M1), während `origin/develop` (Upstream) bereits bei `6.3.0-SNAPSHOT` stand. Vor M2-T1 musste das per eigenständigem `git worktree`-Vergleich aufgelöst werden (Commit `661c791` im `morphium-jakarta-data`-Repo).
- Drei parallel gestartete Bugfix-Subagenten schrieben unkoordiniert in denselben Arbeitsbaum (kein Git-Branching zwischen ihnen). Das führte zu keinem inhaltlichen Konflikt (alle Diffs waren dateiweise oder zeilenweise überschneidungsfrei), aber zu zwei unvollständig abgeschlossenen Einzeltasks (fehlender Commit wegen eines Test-Setup-Bugs, fehlender Commit wegen Iterationslimit). Der Orchestrator hat den Gesamtzustand selbst verifiziert, den Test-Setup-Bug (`java.util.Map.of()` als immutable Map an `storeMap()` übergeben) behoben und alle Änderungen in sauber thematisch getrennte Commits aufgeteilt.
- Ein von Copilot/CodeRabbit übereinstimmend gemeldeter Bug (fehlender Classloader-Fallback beim Laden der GROUP-BY-Record-Klasse) wurde von einem Subagenten bewusst nur recherchiert, nicht gefixt (um Konflikte mit zwei anderen parallel an derselben Datei arbeitenden Subagenten zu vermeiden). Der Orchestrator hat diesen Fix nach Abschluss aller parallelen Tasks selbst nachgetragen.

---

## 3. Konkrete Änderungen am Code

### Geänderte und neue Dateien (kumulativ, `pr/jakarta-data-module` gegen `origin/develop`)

```
CHANGELOG.md                                                          |  24 +
docs/index.md                                                         |  10 +
docs/jakarta-data.md                                                  | 530 ++++++
mkdocs.yml                                                            |   3 +
morphium-jakarta-data/CHANGELOG.md                                    |  29 +
morphium-jakarta-data/README.md                                       | 141 ++
morphium-jakarta-data/pom.xml                                         |  76 ++
morphium-jakarta-data/src/main/java/de/caluga/morphium/data/*.java    | (14 Klassen, inkl. aller Bugfixes)
morphium-jakarta-data/src/test/java/de/caluga/morphium/data/*.java    | (3 bestehende + 3 neue Testklassen: QueryExecutorTest,
                                                                          AbstractMorphiumRepositoryUpdateTest, CursorHelperTest)
pom.xml                                                               |  41 +
release.sh                                                            | 265 ++++---
```

### Repositories und Branches

| Repo | Branch | Basis | Commits | Gepusht? |
|---|---|---|---|---|
| `morphium` | `pr/jakarta-data-module` | `origin/develop` (`e9cf7320a`) | 12 | ja — `fork` (Bardioc1977/morphium), PR #16 gegen Fork-`master` offen |
| `Bardioc1977/morphium` | `master` | war 315 Commits hinter `origin/develop` | — | ja — via PR #15, gemergt (Fast-Forward-Sync) |

### Was ausdrücklich **nicht** geändert wurde

`morphium-core/**` und `poppydb/**` — durchgehend mit `git diff --stat origin/develop -- morphium-core poppydb` (leer) belegt, nach jedem einzelnen Fix-Cluster erneut geprüft.

---

## 4. Verifikationsergebnisse

| Prüfung | Befehl | Ergebnis | Beleg |
|---|---|---|---|
| Reactor-Build | `mvn -B install -DskipTests` | PASS | alle 4 Module SUCCESS |
| Kern-Isolation | `git diff --stat origin/develop -- morphium-core poppydb` | PASS | leer, nach jedem Fix-Commit erneut geprüft |
| Kern-only-Build | `mvn -B install -DskipTests -DskipExtensions` | PASS | `morphium-jakarta-data` fehlt korrekt in Reactor-Summary |
| Modultests (nach allen Fixes) | `mvn -B verify -pl morphium-jakarta-data` | **77 Tests, 0 Failures, 0 Errors** | `/tmp/m2-classloader-fix.log` |
| Volltestsuite (Kern, vor den Review-Fixes) | `mvn -B verify` | 2040 Tests, 3 Failures, 8 Errors — alle als vorbestehend auf `origin/develop` verifiziert | `reports/M2-T4-verification.md` |
| Javadoc | `mvn -B -pl morphium-jakarta-data javadoc:jar source:jar` | PASS, 0 Fehler | — |
| Commit-Hygiene | `git log origin/develop..HEAD --format=... \| grep -i "co-authored\|generated with"` | PASS | keine Treffer über alle 12 Commits |
| Community-Code-Review | CodeRabbit + Copilot + Codex gegen PR #16 | 27 Findings, 21 mit Bug-Charakter, 20 behoben + 1 dokumentiert als nicht behebbar hier | `reports/M2-review-triage.md` |
| Finaler Review-Lauf nach allen Fixes | CodeRabbit erneut gegen `cb9950f9a` | SUCCESS, keine neuen Findings | PR #16 Status-Check |

**Fehlgeschlagene Tests, die schon vorher fehlgeschlagen sind:** 11 Testnamen im Kern (`DualChannelMessagingCompatTest`, `ExclusiveMessageTests`, `LazyLoadingTest.*`, `PerformanceBenchmarkTest.setup`, `ReferenceCascadeTest.*`, `ReferenceTest.*`) — identisch reproduziert gegen einen frischen `origin/develop`-Checkout, Ursachen: Byte-Buddy/JDK-25-Inkompatibilität, fehlender lokaler MongoDB-Server, bekannte Messaging-Timing-Flakies. Siehe `reports/M2-T4-verification.md`.

---

## 5. Invarianten der Optionalität (I1–I5)

| Invariante | Prüfung | Ergebnis |
|---|---|---|
| I1 keine Rückwärts-Dependency | `grep -nE 'jakarta-data\|quarkus\|spring' morphium-core/pom.xml` | PASS |
| I2 keine Framework-Imports im Kern | `mvn -pl morphium-core dependency:tree \| grep -E 'jakarta.data\|io.quarkus\|org.springframework'` | PASS |
| I3 Kern ohne Erweiterungen baubar | `mvn install -DskipTests -DskipExtensions`, Reactor-Summary | PASS |
| I4 kein fremder BOM im Parent | `grep -n 'scope>import' pom.xml` | PASS |
| I5 Abhängigkeitsrichtung einseitig | `mvn -B validate` | PASS |

---

## 6. Pull Requests dieser Welle

| PR | Repo | Branch | Ziel | Vorgelegt am | Freigegeben am | Nummer | Status |
|---|---|---|---|---|---|---|---|
| Fork-Sync (Fast-Forward) | Bardioc1977/morphium | `sync-master-with-upstream-develop` | `master` | — (unkritisch, eigener Fork) | — | #15 | gemergt |
| Review-PR (Community-Bots) | Bardioc1977/morphium | `pr/jakarta-data-module` | `master` | — (unkritisch, eigener Fork) | — | #16 | offen, mergeable, keine offenen Findings |
| **Upstream-PR** | sboesebeck/morphium | `pr/jakarta-data-module` | `develop` | vorgelegt im Chat, 2026-08-05 | **ausstehend** | — | **nicht gestellt — wartet auf explizite Freigabe** |

**Review-Rückmeldungen und ihr Stand:**

| Kommentar-Cluster | Von | Anzahl | Erledigt? | Wie |
|---|---|---|---|---|
| CONTAINS/delete-count/negierte Alias-Operatoren | CodeRabbit | 3 | ✅ | Commit `cf4f72e03` |
| Gemischte And/Or, non-upserting update() | Codex | 2 | ✅ | Commit `9dc86b0fe` |
| Cursor-Pagination (JDQL-Sort, dynamischer Sort, OFFSET-skip, AVG-double) | Codex + CodeRabbit | 6 | ✅ | Commit `420725a31` |
| JDQL-Parser (ORDER BY ohne WHERE, HAVING-Guard, Direction, String-Literale) | CodeRabbit | 4 | ✅ | Commit `c7aa6f3c9` |
| MethodNameParser (unbekannte Felder, Akronym-Combinator) | CodeRabbit + Copilot | 2 | ✅ | Commit `a80d5d52c` |
| Cursor-Keyset-Pflicht | CodeRabbit | 1 | ✅ | Commit `9fb2d0c04` |
| Classloader-Fallback | CodeRabbit | 1 | ✅ | Commit `cb9950f9a` |
| `@OrderBy` ignoreCase/descending/repeatable | CodeRabbit | 1 | ❌ dokumentiert als extern | `reports/M2-review-triage.md` — betrifft Annotation-Processor eines Framework-Adapters, existiert nicht in diesem Repo |
| Doku-Korrekturen (unshipped integrations, Versionsangaben, Stream-Signatur) | CodeRabbit | 3 | ⬜ nicht behoben | P3, siehe Abschnitt 9 |
| Codequalität/Nitpicks (tote Variable, Array-Längen-Check, 15 Performance-Nitpicks) | Copilot + CodeRabbit | 17 | ⬜ nicht behoben | P3, siehe Abschnitt 9 |

---

## 7. Paralyse-Ereignisse und Eingriffe

| Task | Agent | Symptom | Maßnahme (P3-Stufe) | Ergebnis |
|---|---|---|---|---|
| Cluster A (QueryExecutor P0-Fixes) | sonnet | Task endete ohne Commit, weil ein selbst geschriebener Test einen Setup-Bug hatte (`Map.of()` immutable an `storeMap()`) und die Verifikationsbedingung "alle Tests grün" formal nicht erfüllt war | Orchestrator hat den Test-Bug selbst behoben (eine Zeile: `LinkedHashMap`-Wrapper) und den bereits korrekten Produktivcode-Fix committet | Kein Wiederholungslauf nötig, Fix war die ganze Zeit korrekt |
| Cluster C (Cursor-Pagination P0-Fixes) | sonnet | Task erreichte sein Iterationslimit nach Anwenden aller 4 Diffs, aber vor Testerstellung/Commit; meldete einen (fälschlichen) Verdacht auf Fremdüberschreibung durch einen parallelen Task | Orchestrator hat den tatsächlichen Diff-Zustand gegen alle drei parallelen Tasks geprüft (keine Überlappung), die Verifikation selbst durchgeführt und committet | Kein Wiederholungslauf nötig |

Kein Fall von echtem, unbemerktem Datenverlust oder Fehlverhalten durch die parallele Arbeitsweise — beide Fälle waren rein organisatorisch (fehlender letzter Schritt), nicht inhaltlich fehlerhaft.

---

## 8. Erkenntnisse und Planabweichungen

| Erkenntnis | Warum sie zählt | Konsequenz | Plandokument angepasst? |
|---|---|---|---|
| Ein Review-PR gegen den *Default-Branch* eines Forks (statt gegen den regulären Feature-Branch) aktiviert kostenlose Community-Review-Bots (CodeRabbit, Copilot, Codex), die sonst nicht anspringen | Diese dritte Verifikationsebene (über die eigene Testsuite hinaus) fand 21 echte funktionale Bugs, die weder die ursprüngliche Implementierung noch die M1/M2-Verifikationsschritte entdeckt hatten | Für M4 (quarkus-morphium) und M5 (spring-boot-morphium) sollte dieses Muster wiederholt werden: Fork-`master` synchronisieren, Review-PR dagegen stellen, Findings triagieren und beheben, bevor der Upstream-PR vorgelegt wird | Empfehlung für M4/M5 hier vermerkt, noch nicht in `waves/M4-quarkus-pr.md`/`M5-spring-boot.md` nachgezogen |
| Mehrere parallel delegierte Subagenten im selben (nicht isolierten) Arbeitsverzeichnis können ohne Git-Branching sicher koexistieren, solange ihre Aufgaben dateiweise oder zeilenweise nicht überlappen — es entstand kein einziger inhaltlicher Merge-Konflikt über 6 parallele Bugfix-Tasks | Das widerlegt die Annahme, dass parallele Delegation im selben Checkout grundsätzlich riskant ist; das eigentliche Risiko lag bei unvollständigen Einzeltasks (Iterationslimit, Testfehler), nicht bei Datenverlust | Der Orchestrator muss nach jeder parallelen Batch-Delegation den `git status`/Diff-Zustand selbst gegenprüfen, bevor er committet — das wurde in dieser Welle konsequent gemacht und sollte Standardpraxis bleiben | keine Planänderung, aber als Arbeitsweise bestätigt |
| Die Review-Bots fanden Bugs, die bereits in den *bestehenden* 45 Tests aus M1 unentdeckt geblieben waren (z.B. CONTAINS-Exact-Match, delete()-Count) — die ursprüngliche Testsuite war nicht lückenlos, obwohl sie zu 100% grün war | „Alle Tests grün" ist keine Garantie für Korrektheit, wenn die Tests selbst Lücken haben — das M1-Javadoc-Review (Regel „inhaltlich substanziell, nicht nur formal korrekt") hätte um ein analoges Testabdeckungs-Review ergänzt werden können | Für zukünftige Wellen: Community-Code-Review als Pflichtschritt vor jedem Upstream-PR, nicht nur als Kann-Option | Bereits oben vermerkt |

---

## 9. Offene Punkte, die in die nächste Welle übergehen

| Punkt | Warum offen | Wer/Wann | Blockierend? |
|---|---|---|---|
| 3 P3-Doku-Korrekturen (unshipped integrations dokumentiert, instabile Versionsangabe, Stream-Signatur im README) | Niedrige Priorität, keine Laufzeitauswirkung | Kann in M2 selbst noch vor dem Upstream-PR nachgezogen werden, oder im PR-Text als „known minor follow-up" benannt werden | nein |
| 17 Codequalität-Nitpicks (tote Variable, Array-Längen-Check in `release.sh`, 15 Performance-Hinweise von CodeRabbit) | Niedrige Priorität | Optional, kann im PR-Text erwähnt und dem Maintainer zur Entscheidung überlassen werden | nein |
| `@OrderBy` ignoreCase/descending/repeatable nicht vollständig unterstützt | Betrifft einen Annotation-Processor, der nicht in diesem Repository liegt | Muss bei M3/M4 (quarkus-morphium-Vorbereitung) erneut aufgegriffen werden, wenn der dortige Annotation-Processor entsteht/erweitert wird | nein, aber wichtige Notiz für M3 |
| **Upstream-PR-Text ist formuliert und im Chat vorgelegt, aber noch nicht freigegeben** | Freigabepflicht laut Gesamtplan Abschnitt 4.4 | Auftraggeber | **ja — einziger Blocker für den Abschluss von M2** |

---

## 10. Vorbedingungen für die nächste Welle

```
[x] morphium-jakarta-data/ als Modul im Repo, Build grün
[x] morphium-core/** und poppydb/** bitgenau unverändert
[x] Alle fünf Invarianten aus D3 belegt
[x] -DskipExtensions baut Kern ohne das Modul
[x] MkDocs-Seite und Navigation, docs/index.md verlinkt
[x] CHANGELOG-Eintrag im Projektformat
[x] release.sh erweitert, bash -n fehlerfrei
[x] Volltestsuite gelaufen, Ergebnis dokumentiert, Abweichungen als bekannte Flakies belegt
[x] Commit-Hygiene geprüft
[x] Community-Code-Review durchgeführt und alle P0/P1/P2-Findings behoben (neu gegenüber Wellenplan-Vorlage)
[ ] PR-Text vorgelegt und freigegeben  ← einziger offener Schritt
[ ] PR erstellt, Nummer dokumentiert
```

---

## 11. Wiederaufnahme in einem Satz

Lies den im Chat vorgelegten Upstream-PR-Text erneut, hole die explizite Freigabe des Auftraggebers ein, führe dann `git push fork pr/jakarta-data-module` (bereits erfolgt) und `gh pr create --repo sboesebeck/morphium --base develop --head Bardioc1977:pr/jakarta-data-module` aus, dokumentiere die PR-Nummer in `STATE.md`, und starte danach M3 (`quarkus-morphium` vorbereiten) — unter Berücksichtigung der Erkenntnis aus Abschnitt 8, das Fork-`master`-Review-Muster auch dort einzusetzen.
