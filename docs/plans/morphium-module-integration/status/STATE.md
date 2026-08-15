# STATE — lebendes Zustandsdokument

> **Zweite Datei, die ein frischer Orchestrator liest** (nach dem
> [Gesamtplan](../README.md)). Sie ist die einzige Quelle der Wahrheit über den
> aktuellen Stand. Der Orchestrator aktualisiert sie **nach jedem
> abgeschlossenen Task**, nicht erst nach einer Welle.

| Feld | Wert |
|---|---|
| Letzte Aktualisierung | 2026-08-15 |
| Aktualisiert von | Orchestrator-Session, nach Erstellung des M5-Upstream-PRs |
| Ablageort dieses Plans | `Bardioc1977/spring-boot-morphium`, Verzeichnis `docs/plans/morphium-module-integration/` (übernommen aus dem gelöschten Repo `morphium-jakarta-data`, Branch `move-to-morphium`) |
| **Aktuelle Welle** | **M5 abgeschlossen: Upstream-PR #299 gegen `sboesebeck/morphium:develop` erstellt und offen (`Bardioc1977:pr/spring-boot-module`, mergeable)** |
| Aktueller Task | keiner; wartet auf Stephans Review von PR #299 |
| Blocker | Keiner. Cherry-Pick-Rebase (Abschnitt 9b) wurde ausgeführt: 6 M5-Commits auf frischen Branch von `origin/develop` (HEAD `0980ecf16`), plus 1 Zusatzcommit (4 POM-Parent-Versionen von `6.3.0-SNAPSHOT` auf das inzwischen aktuelle `6.3.2-SNAPSHOT` synchronisiert — der alte Branch war 184 Commits hinter `develop`). Diff-Check `git diff --stat origin/develop -- morphium-core poppydb morphium-jakarta-data quarkus-morphium` war leer, voller Reactor-Build gruen, 15/15 Tests gruen. |
| Nächste Aktion | Auf Review/Feedback von Stephan zu PR #299 warten. |

---

## 1. Kurzlage in drei Sätzen

Welle M2 ist vollständig abgeschlossen: PR #266 wurde von Stephan gemergt (als Squash-Commit `e60136245` in `origin/develop`), nach einer eigenen Review-Runde durch ihn, die 2 echte Bugs fand (NOT_CONTAINS-Operator, JDQL-LIKE-Escaping) — beide sofort behoben, testverifiziert, bestätigt. Welle M4 ist ebenfalls abgeschlossen: `quarkus-morphium` wurde per Cherry-Pick (nicht `git rebase`, wegen des Squash-Merges) auf den aktuellen `origin/develop` übertragen, alle 242 Integrationstests grün, Upstream-PR #267 erstellt und offen. Welle M5 ist fork-seitig abgeschlossen (`spring-boot-morphium`, inkl. 3 vom Copilot-Review gefundenen und behobenen Bugs: fehlende Transaction-Aspect-Registrierung, ignorierte `@By`-Bindungen, kaputter Async-Dispatch), wartet aber auf den Merge von PR #267, bevor derselbe Cherry-Pick-Rebase-Schritt für den Upstream-PR nötig ist.

---

## 2. Wellenstatus

| Welle | Status | Fortschritt | Zustandsdokument |
|---|---|---|---|
| M0 Planung | ✅ DONE | 100 % | [2026-07-25-M0-planung.md](2026-07-25-M0-planung.md) |
| M1 jakarta-data Vorbereitung | ✅ DONE | 5/5 Tasks | [2026-08-05-M1-jakarta-data-vorbereitung.md](2026-08-05-M1-jakarta-data-vorbereitung.md) |
| M2 jakarta-data PR | ✅ DONE (PR #266 gemergt) | 5/5 Tasks | [2026-08-05-M2-jakarta-data-pr.md](2026-08-05-M2-jakarta-data-pr.md) |
| M3 quarkus Vorbereitung | ✅ DONE | 6/6 Tasks | [2026-08-05-M3-quarkus-vorbereitung.md](2026-08-05-M3-quarkus-vorbereitung.md) |
| M4 quarkus PR | ✅ DONE (Upstream-PR #267 offen) | 7/7 Tasks | [2026-08-05-M4-quarkus-pr.md](2026-08-05-M4-quarkus-pr.md) |
| M5 spring-boot (A+B) | ✅ DONE fork-seitig; Upstream blockiert bis #267 gemergt | 6/7 Tasks | — |
| M6 Konsolidierung | ⬜ TODO | 0/7 Tasks | — |

Legende: ⬜ TODO · 🟡 IN ARBEIT · ⏸️ BLOCKIERT · ✅ DONE · ❌ ABGEBROCHEN

---

## 3. Taskstatus der aktuellen Welle

**Welle M5 — PR: `spring-boot-morphium` als Morphium-Modul (fork-seitig abgeschlossen)**

| Task | Status | Agent | Gestartet | Verifiziert | Bemerkung |
|---|---|---|---|---|---|
| M5-T1 Bestandsaufnahme + Konventionspruefung | ✅ DONE | sonnet | 2026-08-05 | 2026-08-05 | Bericht `reports/M5-T1-spring-audit.md` |
| M5-T2 POM-Konvertierung + Umbenennung + Property-Praefix | ✅ DONE | sonnet + Orchestrator | 2026-08-05 | 2026-08-05 | Modul-Umbenennung spring-boot-morphium-*->morphium-spring-boot-*, Property-Praefix spring.morphium.*->morphium.* |
| M5-T3 Javadoc + Dokumentation | ✅ DONE | sonnet + Orchestrator | 2026-08-05 | 2026-08-05 | README, CHANGELOG, docs-for-morphium/spring-boot.md |
| M5-T4 Trockenlauf | ✅ DONE | sonnet + Orchestrator | 2026-08-05 | 2026-08-05 | Bericht `reports/M5-T4-dryrun.md`; JUnit-Jupiter-Versionsskew gefunden (fuer M6 vorgemerkt) |
| M5-T5 Module uebernehmen, Reactor, Doku, release.sh | ✅ DONE | sonnet + Orchestrator (Starter-Packaging-Bug behoben) | 2026-08-05 | 2026-08-05 | Branch pr/spring-boot-module von pr/quarkus-extension-module; morphium-spring-boot-starter hatte kein src/, dadurch fehlende sources/javadoc-Jars -- behoben mit package-info.java |
| M5-T6 Vollverifikation vor PR | ✅ DONE | sonnet + Orchestrator | 2026-08-05 | 2026-08-05 | Bericht `reports/M5-T6-verification.md`; identische Kern-Baseline wie M2-T4/M3-T6/M4-T4 |
| Fork-Review-PR erstellt | ✅ DONE | Orchestrator | 2026-08-05 | 2026-08-05 | PR #18 gegen Bardioc1977/morphium:pr/quarkus-extension-module; CodeRabbit uebersprang initial (Nicht-Default-Basis-Branch), per @coderabbitai review getriggert |
| Copilot-Findings behoben | ✅ DONE | Orchestrator | 2026-08-06 | 2026-08-06 | 3 reale Bugs behoben: fehlende MorphiumTransactionAspect-Registrierung (@Component->@AutoConfiguration), @Param statt @By in buildConditionsSpec, fehlender Async-Dispatch fuer CompletionStage-Rueckgabetypen (inkl. Async-Suffix-Stripping analog quarkus-morphium); 15/15 Tests gruen; Commits `9a0ea506e`,`38705fbf9` |
| Upstream-PR-Text, Erstellung | ⏸️ BLOCKIERT | Orchestrator | — | — | wartet auf Merge von PR #267 (M4), dann Cherry-Pick-Rebase noetig (Squash-Merge-bedingt, siehe Erkenntnisliste) |

**Welle M4 — PR: `quarkus-morphium` als Morphium-Modul (Upstream-PR #267 offen)**

| Task | Status | Agent | Gestartet | Verifiziert | Bemerkung |
|---|---|---|---|---|---|
| M4-T1 Submodule übernehmen, Reactor anpassen | ✅ DONE | sonnet | 2026-08-05 | 2026-08-05 | Commits `16e126ac4`,`68751c443` |
| M4-T2 Doku-Anbindung | ✅ DONE | sonnet + Orchestrator | 2026-08-05 | 2026-08-05 | Commits `4634656df`,`89dd2ed5e` |
| M4-T3 release.sh erweitern | ✅ DONE | sonnet + Orchestrator (Luecke gefixt) | 2026-08-05 | 2026-08-05 | Commit `8b617de4b` |
| M4-T4 Vollverifikation vor PR | ✅ DONE | sonnet + Orchestrator | 2026-08-05 | 2026-08-05 | 242/242 Integrationstests, keine Kern-Regression; Bericht `reports/M4-T4-verification.md` |
| Fork-Review-PR erstellt | ✅ DONE | Orchestrator | 2026-08-05 | 2026-08-05 | PR #17 gegen Bardioc1977/morphium:pr/jakarta-data-module |
| Copilot-Findings behoben | ✅ DONE | Orchestrator | 2026-08-05 | 2026-08-05 | 2xP1+2xP2 behoben, Commits `328d2fb6d`,`3a7a29e7d`; 7xP3 zurueckgestellt |
| Upstream-PR-Text, Erstellung | ✅ DONE | Orchestrator | 2026-08-06 | 2026-08-06 | Nach Merge von PR #266: Cherry-Pick der 7 M4-Commits auf frischen Branch von origin/develop (git rebase schlug fehl wegen Squash-Merge-Konflikt), 242/242 Tests gruen nach Uebertragung, Force-Push auf Fork-Branch (User-Genehmigung, da PR #17 Review-only ohnehin bereits abgeschlossen war), PR #267 gegen sboesebeck/morphium:develop erstellt |

**Welle M2 — PR: `morphium-jakarta-data` als Morphium-Modul (abgeschlossen)**

| Task | Status | Agent | Gestartet | Verifiziert | Bemerkung |
|---|---|---|---|---|---|
| M2-T1 Modul übernehmen, Reactor + Parent anpassen | ✅ DONE | sonnet | 2026-08-05 | 2026-08-05 | Commits `9c52cea80`,`59405119b` |
| M2-T2 Doku-Anbindung | ✅ DONE | sonnet | 2026-08-05 | 2026-08-05 | Commits `4398414de`,`09d6c1c74` |
| M2-T3 release.sh erweitern | ✅ DONE | sonnet | 2026-08-05 | 2026-08-05 | Commit `cdc468ce6`; Modul-Registry statt Copy-Paste |
| Review-PR + Bugfix-Runden (zusätzlich zum Wellenplan) | ✅ DONE | Orchestrator + sonnet (6× parallel) | 2026-08-05 | 2026-08-05 | PR #16 gegen Fork-master; 27 Findings, 20 behoben, 1 dokumentiert extern; Commits `9dc86b0fe`,`cf4f72e03`,`420725a31`,`c7aa6f3c9`,`a80d5d52c`,`9fb2d0c04`,`cb9950f9a` |
| M2-T4 Vollverifikation vor PR | ✅ DONE | sonnet | 2026-08-05 | 2026-08-05 | 77 Tests gruen; Bericht `reports/M2-T4-verification.md` |
| M2-T5 PR-Text, Vorlage, Erstellung | ✅ DONE | Orchestrator | 2026-08-05 | 2026-08-05 | PR #266 gegen sboesebeck/morphium:develop, freigegeben und erstellt |

**Welle M3 — `quarkus-morphium` als Morphium-Modul vorbereiten (abgeschlossen)**

| Task | Status | Agent | Gestartet | Verifiziert | Bemerkung |
|---|---|---|---|---|---|
| M3-T1 Extension-Guideline-Audit | ✅ DONE | sonnet | 2026-08-05 | 2026-08-05 | Bericht `reports/M3-T1-extension-audit.md`, stichprobenartig verifiziert |
| M3-T2 groupId-Migration + POM-Umbau | ✅ DONE | sonnet | 2026-08-05 | 2026-08-05 | Commits `7bc35ba`,`f40e08d` |
| M3-T3 Extension-Metadaten aktualisieren | ✅ DONE | sonnet | 2026-08-05 | 2026-08-05 | Commit `36d57fd` |
| M3-T4 Doku (Antora + MkDocs + README/CHANGELOG/MIGRATION-NOTES) | ✅ DONE | sonnet + Orchestrator | 2026-08-05 | 2026-08-05 | Commits `b0c7006`,`32ba563`; Orchestrator schloss 3 offene Teilaufgaben ab |
| M3-T5 integration-tests konditional machen | ✅ DONE | sonnet + Orchestrator (Fix) | 2026-08-05 | 2026-08-05 | Commit `f46551d`; Orchestrator korrigierte einen echten Bug im urspruenglichen Ansatz, 242/242 Tests gruen |
| M3-T6 Integrations-Trockenlauf + Laufzeitmessung | ✅ DONE | sonnet + Orchestrator | 2026-08-05 | 2026-08-05 | Bericht `reports/M3-T6-dryrun.md`; alle Invarianten PASS |

**Welle M2 (abgeschlossen):**

| Task | Status | Agent | Gestartet | Verifiziert | Bemerkung |
|---|---|---|---|---|---|
| M1-T1 POM-Konvertierung | ✅ DONE | sonnet | 2026-08-05 | 2026-08-05 | Commit `5a499f7` |
| M1-T2 Javadoc | ✅ DONE | sonnet | 2026-08-05 | 2026-08-05 | Commits `8974756`,`861c044`,`d6cb423`; H3/H1-Fehler behoben |
| M1-T3 README/CHANGELOG/Migration | ✅ DONE | sonnet | 2026-08-05 | 2026-08-05 | Commits `3c3a5d7`,`c25048b`,`5bbf64a` |
| M1-T4 MkDocs-Seite | ✅ DONE | sonnet | 2026-08-05 | 2026-08-05 | Commit `0850f4b`; Codebeispiele tatsächlich kompiliert |
| M1-T5 Trockenlauf | ✅ DONE | sonnet | 2026-08-05 | 2026-08-05 | Alle I1–I5 PASS; Bericht `reports/M1-T5-dryrun.md` |

> **Regel P4 gilt:** Ein Task wird erst `DONE`, wenn der Orchestrator den im
> Wellendokument genannten `Verifikation:`-Befehl **selbst** ausgeführt hat.
> Spalte „Verifiziert" trägt das Datum dieser Prüfung, nicht die
> Fertigmeldung des Agenten.

---

## 4. Offene Entscheidungen

| ID | Frage | Vorzugsvariante | Status | Blockiert |
|---|---|---|---|---|
| D1 | Versionsfreiheit vs. Lockstep | Lockstep | **OFFEN — JF** | nichts (Arbeitsannahme gesetzt) |
| D2 | groupId der Quarkus-Extension | `de.caluga` | **OFFEN — JF** | nichts (Arbeitsannahme gesetzt) |
| D3 | Reactor-Strategie | Profil `extensions`, default aktiv | **OFFEN — JF** | nichts (Arbeitsannahme gesetzt) |
| D4 | Build/Release/CI | `release.sh` erweitern + CI-PR | **OFFEN — JF** | nichts (Arbeitsannahme gesetzt) |

Alle vier Entscheidungen sind **nicht blockierend**: der Orchestrator arbeitet
mit der dokumentierten Vorzugsvariante weiter. Sollte das JF anders entscheiden,
sind die Kosten pro Entscheidung im jeweiligen D-Dokument im Abschnitt zur
Umkehrbarkeit benannt.

**Nach dem JF zu tun:** Jedes D-Dokument von „OFFEN" auf die getroffene
Entscheidung setzen, mit Datum und — falls abweichend von der Empfehlung — den
daraus folgenden Änderungen an den Wellenplänen.

---

## 5. Zustand der Repositories

| Repo | Branch | Sauber? | Fork-Push erfolgt? | Bemerkung |
|---|---|---|---|---|
| morphium | pr/quarkus-extension-module | ja (nur bekannte untracked Fremd-Dateien) | ja (fork, force-pushed nach Cherry-Pick) | M4 Upstream-PR #267 offen gegen sboesebeck/morphium:develop; morphium-jakarta-data (M2) bereits gemergt in origin/develop enthalten |
| morphium-jakarta-data | move-to-morphium | ja | ja (fork) | M1+M2+M3+M4+M5-Plandokumentation liegt hier; M2 gemergt |
| quarkus-morphium | move-to-morphium | ja (2 untracked Fremd-Dateien) | nein | Quelle fuer M4, unveraendert seit Integration ins morphium-Repo |
| spring-boot-morphium | move-to-morphium | ja | nein | Quelle fuer M5, unveraendert seit Integration ins morphium-Repo |
| quarkus-morphium-showcase | main | ja | — | unverändert |

> Der Orchestrator prüft diese Tabelle beim Wiederaufnehmen mit `git status` und
> `git branch --show-current` in jedem Repo und korrigiert sie, **bevor** er
> weiterarbeitet (Wiederaufnahme-Schritt 5 im Gesamtplan).

---

## 6. Pull Requests

| PR | Repo | Branch | Ziel | Vorgelegt | Freigegeben | Nummer | Status |
|---|---|---|---|---|---|---|---|
| M2 jakarta-data | sboesebeck/morphium | `pr/jakarta-data-module` | develop | ✅ (im Chat) | ✅ 2026-08-05 | #266 | **gemergt** (Squash `e60136245`, 2026-08-05 21:43) |
| M4 quarkus (Fork-Review) | Bardioc1977/morphium | `pr/quarkus-extension-module` | `pr/jakarta-data-module` | — (unkritisch, eigener Fork) | — | #17 | offen, Review-only, kein Merge geplant |
| M4 quarkus (Upstream) | sboesebeck/morphium | `pr/quarkus-extension-module` | develop | ✅ (im Chat) | ✅ 2026-08-06 | #267 | **offen** |
| M5 spring-boot (Fork-Review) | Bardioc1977/morphium | `pr/spring-boot-module` | `pr/quarkus-extension-module` | — (unkritisch, eigener Fork) | — | #18 | offen, Review-only, kein Merge geplant |
| M5 spring-boot (Upstream) | sboesebeck/morphium | `pr/spring-boot-module` | develop | ⬜ | ⬜ | — | nicht gestellt — blockiert bis PR #267 gemergt + Cherry-Pick-Rebase |
| M6-A Isolationsskript | sboesebeck/morphium | `pr/verify-core-isolation` | develop | ⬜ | ⬜ | — | nicht gestellt |
| M6-B CI-Workflow | sboesebeck/morphium | `pr/ci-workflow` | develop | ⬜ | ⬜ | — | nicht gestellt |
| M6-C Doku | sboesebeck/morphium | `pr/docs-consolidation` | develop | ⬜ | ⬜ | — | nicht gestellt |
| M6-D Showcase | Bardioc1977/quarkus-morphium-showcase | `chore/morphium-module-coordinates` | main | ⬜ | ⬜ | — | nicht gestellt |

> **🚦 Ohne Häkchen in „Freigegeben" wird kein `gh pr create` ausgeführt.**
> Die Freigabe ist die ausdrückliche Zustimmung des Auftraggebers im Chat,
> nachdem ihm der vollständige PR-Text gezeigt wurde.

---

## 7. Paralyse-Ereignisse

| Datum | Welle/Task | Agent | Symptom | Maßnahme | Ergebnis |
|---|---|---|---|---|---|
| — | — | — | — | — | — |

> Jedes Eingreifen nach Regel P3 wird hier protokolliert. Ein leerer Eintrag
> nach mehreren Wellen ist verdächtig — er bedeutet meist, dass die Heartbeats
> nicht gelaufen sind, nicht dass es keine Probleme gab.

---

## 8. Berichte der Agenten

| Bericht | Welle/Task | Vorhanden |
|---|---|---|
| `reports/M1-T5-dryrun.md` | M1-T5 | ✅ |
| `reports/M2-T4-verification.md` | M2-T4 | ✅ |
| `reports/M3-T1-extension-audit.md` | M3-T1 | ✅ |
| `reports/M3-T6-dryrun.md` | M3-T6 | ✅ |
| `reports/M4-T4-verification.md` | M4-T4 | ✅ |
| `reports/M5-T1-spring-audit.md` | M5-T1 | ✅ |
| `reports/M5-T4-dryrun.md` | M5-T4 | ✅ |
| `reports/M5-T6-verification.md` | M5-T6 | ✅ |
| `reports/M6-T2-release-dryrun.md` | M6-T2 | ⬜ |
| `reports/M6-T3-ci-proposal.md` | M6-T3 | ⬜ |

---

## 9. Erkenntnisse und Abweichungen vom Plan

> Hier trägt der Orchestrator ein, was sich während der Umsetzung als anders
> herausgestellt hat als geplant. Diese Liste ist die wichtigste Information für
> eine spätere Session — Pläne sind Annahmen, das hier sind Fakten.

| Datum | Erkenntnis | Konsequenz | Plandokument angepasst? |
|---|---|---|---|
| 2026-07-25 | Upstream `sboesebeck/morphium` hat auf `develop` keinen Build-Workflow (nur `deploy-docs.yml`, `sync-wiki.yml`) | CI ist kein „Anpassen", sondern eine Neueinführung → eigener PR in M6 | ✅ D4, M6 |
| 2026-07-25 | `io.quarkiverse.morphium` ist als groupId auf Central nicht verwendbar (Namespace-Ownership) | groupId-Migration wird Pflichtteil von M3 | ✅ D2, M3 |
| 2026-07-25 | `release.sh` nennt Module namentlich an mindestens drei Stellen | Jede Modulwelle muss das Skript erweitern; M6 prüft es im Zusammenhang | ✅ D4, M2/M4/M5/M6 |
| 2026-07-25 | `quarkus-morphium/integration-tests` braucht Docker | Neue Infrastrukturanforderung für Kern-Contributor → M3-T5 macht sie bedingt | ✅ M3 |
| 2026-07-25 | Morphium-Quellcode enthält keine Lizenzheader (0 von 309 Dateien) | Agenten dürfen keine hinzufügen | ✅ alle Doku-Tasks |
| 2026-07-25 | Spring-artifactIds nutzen das reservierte Präfix `spring-boot-` (`spring-boot-morphium-starter` statt `morphium-spring-boot-starter`); Verzeichnisnamen = artifactIds | Umbenennung jetzt folgenlos (kein Release), später teuer → M5-T2. Die `-pl`-Pfade in M5/M6 sind danach anzupassen | ✅ Gesamtplan Abschnitt 6, M5, M6 |
| 2026-08-05 | Fork-Review-Muster (PR gegen eigenen Fork, CodeRabbit/Copilot/Codex-Review vor jedem Upstream-PR) fand in M2 21 echte Bugs, die eigene Tests nicht fanden | Gilt zwingend auch für M4/M5, vom Auftraggeber im Chat nochmal bestätigt | ✅ M2, für M4/M5 vorgemerkt |
| 2026-08-05 | `@EnabledIfDockerAvailable` (testcontainers-junit-jupiter 2.0.3) meldet unter Quarkus-Test-Classloading fälschlich "Docker nicht verfügbar", obwohl im selben JVM-Kontext bereits ein Container lief | Manueller `@BeforeAll`/`Assumptions`-Check gegen `DockerClientFactory.instance().isDockerAvailable()` ist robuster; für M5 vormerken, falls ähnliches Docker-Gating dort nötig wird | ✅ M3, für M5 vorgemerkt |
| 2026-08-05 | Ein Modul-PR kann von einem noch ungemergten vorherigen Modul-Branch abzweigen (statt auf dessen Merge zu warten), solange vor dem Upstream-PR ein Rebase auf den dann aktuellen `origin/develop` erfolgt | Entkoppelt Fortschritt vom externen Review-Zyklus, ohne die spaetere Diff-Sauberkeit zu gefaehrden | ✅ M4, fuer M5 vorgemerkt falls M2 dann noch offen ist |
| 2026-08-05 | `META-INF/quarkus-build-steps.list` ist der tatsaechliche Ladeschritt-Mechanismus fuer Quarkus-Deployment-Prozessoren (kein automatisches Scanning) — ein unvollstaendiger Eintrag deaktiviert Features lautlos, Tests merken es nicht (sie rufen Prozessoren direkt auf) | Diese Datei bei jeder neuen Deployment-Prozessor-Klasse aktiv pflegen | ✅ M4, fuer M5 vorgemerkt |
| 2026-08-05 | CodeRabbit hat ein hartes 100-Dateien-Limit pro Review (kein Config-Override gefunden) | Grosse Modul-PRs (>100 Dateien) bekommen kein CodeRabbit-Review; entweder aufteilen oder Skip akzeptieren | ✅ M4, fuer M5 vorgemerkt |
| 2026-08-05 | `jakarta.data.repository.Is` existiert nicht in der stabilen `jakarta.data-api:1.0.0`, erst ab Jakarta Data 1.1 (aktuell nur Milestone 1.1.0-M3) | Dokubeispiele mit `@Is` waeren nicht kompilierbar; morphium-jakarta-data nutzt dieselbe 1.0.0-Version — dort ebenfalls pruefen | ⬜ M6 sollte morphium-jakarta-data-Doku auf aehnliche 1.1-Vorgriffe pruefen |
| 2026-08-06 | GitHub fuehrt PR-Merges als SQUASH durch (bestaetigt an PR #266: ein einziger Commit `e60136245` statt der 12 Original-Commits als Vorfahren) — ein normaler `git rebase origin/develop` auf einem Branch, der von den alten Einzel-Commits abstammt, erzeugt garantiert add/add-Konflikte, weil git denselben Inhalt doppelt zusammenzufuehren versucht | Bei jedem Modul-Branch, der von einem noch ungemergten Vorgaenger-Branch abzweigt: nach dessen Merge NICHT rebasen, sondern die eigenen Commits per `git cherry-pick <hash1> <hash2> ...` auf einen frischen Branch von `origin/develop` uebertragen. Bestaetigt konfliktfrei fuer alle 7 M4-Commits | ✅ M4, fuer M5 vorgemerkt (identisches Vorgehen erforderlich) |
| 2026-08-06 | Ein Force-Push auf einen bereits bestehenden Fork-Branch mit inkompatibler Historie (nach Cherry-Pick-Rebase) ist nur dann vertretbar, wenn der Branch ausschliesslich fuer einen Review-only-PR ohne geplanten Merge genutzt wird (kein geschuetzter Branch, keine anderen Konsumenten) | Force-Push fuer pr/quarkus-extension-module nach expliziter User-Genehmigung durchgefuehrt, PR #17 blieb technisch funktionsfaehig (GitHub aktualisiert den Diff automatisch) | ✅ M4, fuer M5 identisch anzuwenden |
| 2026-08-06 | GitHub-Bots (CodeRabbit) deaktivieren automatische Reviews auf Nicht-Default-Basis-Branches generell (nicht nur wegen Datei-Anzahl wie bei PR #17/M4) — bestaetigt bei PR #18 (M5): "Auto reviews are disabled on base/target branches other than the default branch" | Bei jedem Fork-Review-PR gegen einen Nicht-Default-Branch: `@coderabbitai review` sofort manuell triggern, nicht auf automatisches Anspringen warten. Copilot muss ebenfalls oft manuell per @copilot-Kommentar angefordert werden | ✅ M4/M5, fuer M6 vorgemerkt falls dort noch Fork-Review-PRs anfallen |

---

## 9b. Vorgehen fuer den M5-Upstream-PR nach Merge von PR #267 (Kopiervorlage aus M4)

```bash
cd /Volumes/Entwicklung/workspaces/porsche/morphium-workspace/morphium
gh pr view 267 --repo sboesebeck/morphium --json state,mergedAt   # muss MERGED zeigen
git fetch origin
git log --oneline pr/jakarta-data-module..pr/spring-boot-module --reverse
  # liefert die Liste der M5-eigenen Commit-Hashes in chronologischer Reihenfolge
git checkout -b pr/spring-boot-module-v2 origin/develop
git cherry-pick <hash1> <hash2> ... <hashN>   # alle M5-Commits in der obigen Reihenfolge
git diff --stat origin/develop -- morphium-core poppydb morphium-jakarta-data quarkus-morphium
  # MUSS leer sein
mvn -B install -DskipTests   # voller Reactor
mvn -B verify -pl spring-boot-morphium/morphium-spring-boot-autoconfigure,spring-boot-morphium/morphium-spring-boot-starter,spring-boot-morphium/morphium-spring-boot-test
  # MUSS 15/15 Tests gruen liefern
git branch -D pr/spring-boot-module
git branch -m pr/spring-boot-module-v2 pr/spring-boot-module
git push fork pr/spring-boot-module --force   # Force-Push OK, PR #18 ist Review-only
```
Danach: PR-Text formulieren (siehe `status/2026-08-05-M4-quarkus-pr.md`/PR #267 als Stilvorlage), im Chat vorlegen, Freigabe abwarten, `gh pr create --repo sboesebeck/morphium --base develop --head Bardioc1977:pr/spring-boot-module`.

---

## 10. Wiederaufnahme-Checkliste für den Orchestrator

```
[ ] Gesamtplan README.md gelesen
[ ] diese Datei gelesen
[ ] Wellendokument der aktuellen Welle gelesen
[ ] letzter Zustandsbericht in status/ gelesen
[ ] git status + git branch --show-current in allen fünf Repos geprüft
[ ] Abschnitt 5 dieser Datei entsprechend korrigiert
[ ] für jeden DONE-Task der aktuellen Welle den Verifikation:-Befehl ausgeführt
[ ] laufende Hintergrund-Agenten geprüft (TaskList)
[ ] erst dann: nächsten TODO-Task starten
```
