# Gesamtplan: Integration der Erweiterungsmodule in Morphium

> **Einstiegsdokument für den Orchestrator.**
> Ein frischer Opus-Orchestrator liest ausschließlich diese Datei plus
> `status/STATE.md` und weiß danach, wo das Vorhaben steht und was als Nächstes
> zu tun ist. Alle Wellen-Details liegen in `waves/`.

| Feld | Wert |
|---|---|
| Angelegt | 2026-07-25 |
| Letzte Aktualisierung | 2026-07-25 |
| Ablageort | Repository `Bardioc1977/morphium-jakarta-data`, Branch `develop`, Verzeichnis `docs/` |
| Arbeits-Workspace | `/Volumes/Entwicklung/workspaces/porsche/morphium-workspace/` mit den Schwester-Checkouts `morphium/`, `quarkus-morphium/`, `spring-boot-morphium/` |
| Aktueller Stand | Welle 0 abgeschlossen (Planung), Welle 1 nicht gestartet |
| Zustandsdokument | [`status/STATE.md`](status/STATE.md) |
| JF-Präsentationsdokument | [`../../jf/2026-07-morphium-modularisierung.md`](../../jf/2026-07-morphium-modularisierung.md) (deutsch) · [`…-modularization.en.md`](../../jf/2026-07-morphium-modularization.en.md) (englisch, Versandfassung) |

---

## 1. Zielbild

Die drei heute eigenständigen Repositories unter `Bardioc1977` werden **Module
des offiziellen Morphium-Projekts** (`sboesebeck/morphium`):

```
morphium/                        (sboesebeck/morphium)
├── pom.xml                      morphium-parent
├── morphium-core/               → artifactId: morphium          [Kern, unverändert]
├── poppydb/                     → artifactId: poppydb           [bestehend]
├── morphium-jakarta-data/       → artifactId: morphium-jakarta-data   [NEU, M1/M2]
├── quarkus-morphium/            → 4 Submodule                   [NEU, M3/M4]
│   ├── runtime/                 → artifactId: quarkus-morphium
│   ├── deployment/              → artifactId: quarkus-morphium-deployment
│   ├── testing/                 → artifactId: quarkus-morphium-testing
│   └── integration-tests/       (nicht deployed)
└── spring-boot-morphium/        → 3 Submodule                   [NEU, M5]
    ├── autoconfigure/
    ├── starter/
    └── test/
```

### Nicht verhandelbare Randbedingung: Optionalität

**Die Abhängigkeitsrichtung ist strikt einseitig.** `morphium-core` darf zu
keinem Zeitpunkt und in keiner Form von einem Erweiterungsmodul abhängen —
weder als `compile`-, `provided`-, `optional`- noch `test`-Dependency.

Konkret bedeutet „optionales Modul":

1. **Keine Rückwärts-Dependency.** `morphium-core/pom.xml` bleibt inhaltlich
   unberührt. Verifikation: `mvn -pl morphium-core -am dependency:tree` enthält
   kein Erweiterungsmodul.
2. **Keine transitive Belastung.** Wer nur `de.caluga:morphium` einbindet,
   erhält kein `jakarta.data-api`, kein Quarkus, kein Spring.
3. **Abschaltbarer Reactor-Anteil.** Der Kern muss ohne die Erweiterungen
   baubar bleiben (`mvn -pl morphium-core,poppydb -am verify`), damit ein
   Core-Contributor nicht Quarkus- und Spring-BOMs auflösen muss.

> **Wichtige Präzisierung, die der Orchestrator kennen muss:** `<optional>true</optional>`
> in der POM ist hier **nicht** das Mittel der Wahl und wäre eine Fehlinterpretation
> des Auftrags. `optional` beschreibt eine vorhandene, aber nicht transitiv
> propagierte Abhängigkeit. Hier existiert überhaupt keine Abhängigkeit
> `morphium → modul`. Die Optionalität entsteht durch die Modulgrenze und
> die Konsumenten-Wahl, nicht durch ein POM-Flag. Siehe
> [`decisions/D3-reactor-strategie.md`](decisions/D3-reactor-strategie.md).

---

## 2. Offene Entscheidungen für das nächste JF

Diese Punkte sind **bewusst nicht** vom Orchestrator zu entscheiden. Sie sind
mit Argumentation und Empfehlung aufbereitet und werden von Heiko im JF
eingebracht. Der Orchestrator arbeitet bis zur Klärung mit der dokumentierten
**Vorzugsvariante** und markiert alle davon abhängigen Artefakte als
`ENTSCHEIDUNG-OFFEN`.

| ID | Frage | Vorzugsvariante | Dokument |
|---|---|---|---|
| D1 | Eigene Versionslinie pro Modul oder Lockstep mit Morphium? | **Lockstep** (`${project.version}`) | [D1-versionierung.md](decisions/D1-versionierung.md) |
| D2 | Welche `groupId` für die Quarkus-Extension? | **`de.caluga`** (`io.quarkiverse.*` ist blockiert) | [D2-groupid-namespace.md](decisions/D2-groupid-namespace.md) |
| D3 | Erweiterungen im Default-Reactor oder im Maven-Profil? | **Profil `extensions`, per Default aktiv** | [D3-reactor-strategie.md](decisions/D3-reactor-strategie.md) |
| D4 | Wie werden Build/Release-Workflows harmonisiert? | **`release.sh` erweitern, GitHub-Actions-CI upstream einführen** | [D4-build-release-workflow.md](decisions/D4-build-release-workflow.md) |

---

## 3. Wellenplan

Sechs Meilensteine. Jeder Meilenstein ist eine **Welle** aus mehreren
parallel oder sequenziell laufenden Sonnet-Agenten. Nach **jeder** Welle wird
zwingend ein Zustandsdokument in `status/` geschrieben und `STATE.md` sowie
dieser Gesamtplan aktualisiert.

```
M1 ──► M2 ──┐
            ├──► M6
M3 ──► M4 ──┤
            │
M5 ─────────┘
```

| Welle | Meilenstein | Ziel | Abhängig von | Plandokument |
|---|---|---|---|---|
| **M1** | jakarta-data vorbereiten | `morphium-jakarta-data` auf Branch `move-to-morphium` als integrationsfähiges Morphium-Modul, voll dokumentiert | — | [waves/M1-jakarta-data-vorbereitung.md](waves/M1-jakarta-data-vorbereitung.md) |
| **M2** | jakarta-data PR | PR-Vorlage → Freigabe → PR gegen `sboesebeck/morphium:develop`, inkl. Maven-Central-Konzept | M1 | [waves/M2-jakarta-data-pr.md](waves/M2-jakarta-data-pr.md) |
| **M3** | quarkus-morphium vorbereiten | Extension-Guideline-Audit, groupId-Migration, Modulstruktur, Doku | M2 (gemergt) | [waves/M3-quarkus-vorbereitung.md](waves/M3-quarkus-vorbereitung.md) |
| **M4** | quarkus-morphium PR | PR-Vorlage → Freigabe → PR, inkl. Extension-Registry/Central | M3 | [waves/M4-quarkus-pr.md](waves/M4-quarkus-pr.md) |
| **M5** | spring-boot-morphium | Vorbereitung **und** PR (analog M3+M4, kleinerer Umfang) | M2 (gemergt) | [waves/M5-spring-boot.md](waves/M5-spring-boot.md) |
| **M6** | Konsolidierung | Build/Release/CI-Harmonisierung, Showcase-Umstellung, Abbau der Bardioc1977-Repos | M2, M4, M5 | [waves/M6-konsolidierung.md](waves/M6-konsolidierung.md) |

**Warum M6 existiert:** Die fünf vom Auftraggeber genannten Schritte enden mit
dem letzten PR. Danach bleibt substanzielle Arbeit übrig, die niemand sonst
macht: der gemeinsame Release-Pfad nach Maven Central, die CI im
Upstream-Repo (dort existiert heute **kein** `build.yml` — nur der Fork hat
eines), die Umstellung von `quarkus-morphium-showcase` auf die neuen
Koordinaten und das kontrollierte Stilllegen der vier Bardioc1977-Repos. Ohne
M6 hinterlässt das Vorhaben vier verwaiste Repos und einen Release-Prozess,
der die neuen Module nicht kennt.

---

## 4. Orchestrator-Kontrakt

### 4.1 Wiederaufnahme (jederzeit, auch nach Tagen)

Der Startprompt für eine neue Session liegt in
[`RESUME-PROMPT.md`](RESUME-PROMPT.md) — kopierfertig, mit der zuletzt
verifizierten Ausgangslage. Diese Datei wird nach jeder Welle nachgezogen.

Ablauf beim Start einer neuen Orchestrator-Session:

1. Diese Datei lesen.
2. `status/STATE.md` lesen → liefert aktuelle Welle, offene Tasks, Blocker.
3. Das Plandokument der aktuellen Welle in `waves/` lesen.
4. Den letzten Zustandsbericht in `status/` lesen (chronologisch letzter
   Dateiname).
5. **Ist-Zustand am Code verifizieren, nicht dem Dokument glauben.** Für jede
   als `DONE` markierte Aufgabe der aktuellen Welle einen Kurz-Check ausführen
   (die Wellen-Dokumente nennen pro Task einen `Verifikation:`-Befehl).
   Abweichungen in `STATE.md` korrigieren, bevor weitergearbeitet wird.
6. Erst dann den nächsten `TODO`-Task starten.

### 4.2 Modellwahl

- **Orchestrator: Opus.** Plant, prüft, entscheidet, schreibt Zustandsdokumente,
  formuliert PR-Texte.
- **Ausführung: ausschließlich Sonnet.** Jeder `Agent`-Aufruf setzt explizit
  `model: "sonnet"`. Keine Ausnahme, auch nicht für „schwierige" Tasks — bei zu
  schwierigen Tasks wird der Task zerlegt, nicht das Modell hochgestuft.
- Der Orchestrator schreibt selbst nur: Plandokumente, Zustandsdokumente,
  PR-Texte, JF-Dokument. Produktivcode und POMs schreiben Sonnet-Agenten.

### 4.3 Paralyse-Erkennung (verbindlich)

Sonnet-Agenten in langen Refactoring-Tasks neigen zu drei Ausfallmustern:
stiller Abbruch, Endlosschleife auf einem fehlschlagenden Build, und
Scope-Drift (Agent „verbessert" nebenbei fremden Code). Gegenmaßnahmen:

**Regel P1 — Task-Zuschnitt.** Kein Agent-Task ohne (a) klar benannte
Dateiliste oder Verzeichnisgrenze, (b) maschinell prüfbares Abschlusskriterium,
(c) explizite Nicht-Ziele. Ein Task, der länger als ~20 Minuten laufen sollte,
wird zerlegt.

**Regel P2 — Heartbeat.** Nach jedem Start eines Hintergrund-Agenten prüft der
Orchestrator dessen Fortschritt spätestens alle **10 Minuten** über `TaskList` /
`TaskOutput`. Bei drei aufeinanderfolgenden Checks ohne Fortschritt (kein neues
Tool-Ereignis, kein Dateisystem-Delta) gilt der Agent als paralysiert.

**Regel P3 — Eskalation bei Paralyse.**
1. `SendMessage` an den Agenten mit konkreter Frage: „Woran arbeitest du gerade?
   Nenne die Datei und das nächste Kommando."
2. Keine verwertbare Antwort binnen eines weiteren Checks → `TaskStop`.
3. Task zerlegen, Teil-Tasks mit engerem Scope neu starten. Nie denselben
   Prompt unverändert wiederholen.
4. Paralyse-Ereignis im Zustandsdokument der Welle protokollieren
   (Agent, Task, Symptom, Zerlegung).

**Regel P4 — Verifikation durch den Orchestrator, nicht durch den Agenten.**
Ein Agent, der „fertig" meldet, gilt als *behauptet fertig*. Der Orchestrator
führt den `Verifikation:`-Befehl des Tasks selbst aus. Erst dann `DONE`.

**Regel P5 — Build-Timeouts.** Lange Builds laufen mit
`run_in_background: true` in eine Logdatei. Der Volltest-Lauf von Morphium
dauert ~90 Minuten; Ausgaben **nie** durch `| tail` oder `| grep` pipen (ein
Hook im Workspace unterbindet das). Für zielgerichtete Läufe `-Dtest=Klasse`
oder `-Dgroups=core` verwenden.

### 4.4 Harte Verbote für alle Agenten

Diese Sätze sind in **jeden** Agenten-Prompt zu kopieren:

```
VERBOTEN — ohne Ausnahme:
- `gh pr create`, `gh pr merge`, `gh release create` — jegliches Erzeugen von
  Pull Requests oder Releases. Auch nicht gegen Bardioc1977-Repos.
- `git push` in irgendeiner Form.
- Änderungen an `morphium/morphium-core/**` und `morphium/poppydb/**`.
- `git filter-repo`, `git rebase -i`, History-Rewrites, force-push.
- Co-Authored-By-Zeilen in Commit-Messages (weder Claude noch eine
  E-Mail-Adresse).
- "🤖 Generated with Claude Code" in irgendeinem Text.
Commits im lokalen Arbeitsbaum sind erlaubt und erwünscht.
```

**Der Orchestrator selbst stellt ebenfalls keine PRs ohne Vorlage.**
Verbindlicher Ablauf: PR-Text vollständig ausformulieren → dem Auftraggeber im
Chat zeigen → auf explizite Freigabe warten → erst dann `gh pr create`. Das
gilt für jeden PR, insbesondere gegen `sboesebeck/morphium`.

### 4.5 Git-Workflow (triangulär)

```bash
# Vorbereitungs-Branches in den Bardioc1977-Repos:
git checkout -b move-to-morphium <basis>

# Upstream-PR-Branches IMMER von origin/develop, NIE von origin/master:
cd morphium
git fetch origin
git checkout -b pr/<thema> origin/develop
# ... Arbeit, Commits ...
# Push und PR NUR nach Freigabe, durch den Orchestrator:
git push fork pr/<thema>
gh pr create --head Bardioc1977:pr/<thema> --base develop --repo sboesebeck/morphium
```

`origin` = `sboesebeck/morphium` (upstream), `fork` = `Bardioc1977/morphium`
(Push-Ziel). PRs zielen auf `develop`.

### 4.6 PR-Konventionen (bestätigt über 59 gemergte PRs)

- PR-Body beginnt mit „Hi Stephan," und endet mit „Cheers!"
- Kein `Co-Authored-By`, kein Generated-with-Hinweis.
- Merge durch Stephan; falls wir mergen: `gh pr merge --merge`, **niemals**
  `--squash` (GitHub injiziert dabei Co-authored-by).
- Ein PR = ein Thema. Die Modul-PRs sind groß; deshalb je Modul ein PR mit
  sauber getrennten Commits.

---

## 5. Statusübersicht (vom Orchestrator zu pflegen)

| Welle | Status | Zustandsdokument | Abgeschlossen am |
|---|---|---|---|
| M0 Planung | ✅ DONE | `status/2026-07-25-M0-planung.md` | 2026-07-25 |
| M1 jakarta-data Vorbereitung | ✅ DONE | `status/2026-08-05-M1-jakarta-data-vorbereitung.md` | 2026-08-05 |
| M2 jakarta-data PR | ✅ DONE (PR #266 gemergt) | `status/2026-08-05-M2-jakarta-data-pr.md` | 2026-08-05 |
| M3 quarkus Vorbereitung | ✅ DONE | `status/2026-08-05-M3-quarkus-vorbereitung.md` | 2026-08-05 |
| M4 quarkus PR | ✅ DONE (Upstream-PR #267 offen) | `status/2026-08-05-M4-quarkus-pr.md` | 2026-08-06 |
| M5 spring-boot | ✅ DONE fork-seitig; Upstream blockiert bis #267 gemergt | — | — |
| M6 Konsolidierung | ⬜ TODO | — | — |

Legende: ⬜ TODO · 🟡 IN ARBEIT · ⏸️ BLOCKIERT · ✅ DONE

---

## 6. Ausgangslage (Stand 2026-07-25, verifiziert)

### Repositories

| Projekt | GitHub | Default-Branch | Version | Java-Dateien |
|---|---|---|---|---|
| morphium | `sboesebeck/morphium` (`origin`), `Bardioc1977/morphium` (`fork`) | `master` / Arbeit auf `develop` | `6.2.6-SNAPSHOT` | 309 (core, main) |
| morphium-jakarta-data | `Bardioc1977/morphium-jakarta-data` | `main` | `1.1.0-SNAPSHOT` (develop) | 18 (15 main / 3 test) |
| quarkus-morphium | `Bardioc1977/quarkus-morphium` | `main` | `1.2.0` | 101 (39 main / 62 test) |
| spring-boot-morphium | `Bardioc1977/spring-boot-morphium` | `main` | `1.0.0-SNAPSHOT` | 15 (10 main / 5 test) |
| quarkus-morphium-showcase | `Bardioc1977/quarkus-morphium-showcase` | `main` | — | Demo-App |

### Aktuelle Versionsverflechtung

```
morphium (origin/develop)      6.2.6-SNAPSHOT
  └─ morphium-jakarta-data     1.1.0-SNAPSHOT  → morphium 6.2.5-SNAPSHOT
       ├─ quarkus-morphium     1.2.0           → morphium 6.2.5-SNAPSHOT, mjd 1.1.0-SNAPSHOT, Quarkus 3.32.3
       └─ spring-boot-morphium 1.0.0-SNAPSHOT  → morphium 6.2.4, mjd 1.1.0, Spring Boot 3.4.4
```

Die Inkonsistenz (SNAPSHOT vs. Release, 6.2.4 vs. 6.2.5 vs. 6.2.6) ist genau
das Problem, das D1 adressiert.

### Bereits geleistete Upstream-Beiträge

59 gemergte PRs (#113–#213) seit 2026-02-26, darunter die beiden Hooks, die die
Framework-Integrationen überhaupt ermöglichen:
`AnnotationAndReflectionHelper.registerTypeIds()` (#166) und
`ClassGraphCache.preRegisterClassesWithAnnotation()` (#200).

### Relevante Beobachtungen für die Integration

1. **`poppydb` ist die Referenzimplementierung eines Morphium-Moduls.** Es nutzt
   `${project.version}` (Lockstep), erbt sämtliche Plugin-Konfiguration aus
   `pluginManagement` des Parent, deklariert keine eigenen Plugin-Versionen und
   liefert `src/main/assembly` für sein CLI. Jedes neue Modul folgt exakt diesem
   Muster.
2. **Upstream hat keine CI.** `origin/develop` enthält nur
   `deploy-docs.yml` und `sync-wiki.yml`. `build.yml` existiert ausschließlich
   auf `fork/develop` als fork-only Commit `21ca5ab9`. M6 muss das adressieren.
3. **`release.sh` ist modul-hartcodiert.** Zeilen ~630 und ~842ff nennen
   `morphium-core` und `poppydb` namentlich und bauen ein kombiniertes
   Sonatype-Bundle. Jedes neue Modul erfordert eine Erweiterung dieses Skripts.
4. **`io.quarkiverse.morphium` ist als groupId nicht haltbar.** Der
   `io.quarkiverse`-Namensraum gehört dem Quarkiverse; eine Namespace-Freigabe
   auf Maven Central bekommen wir dafür nicht. Siehe D2.
5. **`quarkus-morphium/integration-tests` nutzt Testcontainers** und braucht
   damit Docker. Im Morphium-Reactor ist das eine neue Infrastrukturanforderung.
6. **Dokumentationsformate divergieren.** Morphium nutzt MkDocs Material
   (`mkdocs.yml`, `docs/*.md`), quarkus-morphium nutzt Antora/AsciiDoc
   (`docs/modules/ROOT/pages/*.adoc`). M3 muss klären, wie beides koexistiert.
7. **Der Spring-Starter verletzt die Namenskonvention.** Die artifactIds lauten
   `spring-boot-morphium-autoconfigure`, `-starter`, `-test`. Das Präfix
   `spring-boot-` ist für Artefakte des Spring-Teams reserviert; Drittanbieter
   sollen `<projekt>-spring-boot-starter` verwenden. Da noch kein
   Central-Release existiert, ist eine Umbenennung jetzt folgenlos und später
   teuer. M5-T1 prüft es, M5-T2 setzt es um. **Konsequenz für den
   Orchestrator:** Die `-pl`-Pfade in den M5- und M6-Prompts nennen die heutigen
   Verzeichnisnamen und sind nach einer Umbenennung anzupassen.

---

## 7. Dateien dieses Plans

```
docs/plans/morphium-module-integration/
├── README.md                              ← diese Datei (Gesamtplan)
├── RESUME-PROMPT.md                       ← Startprompt für eine neue Orchestrator-Session
├── waves/
│   ├── M1-jakarta-data-vorbereitung.md
│   ├── M2-jakarta-data-pr.md
│   ├── M3-quarkus-vorbereitung.md
│   ├── M4-quarkus-pr.md
│   ├── M5-spring-boot.md
│   └── M6-konsolidierung.md
├── decisions/
│   ├── D1-versionierung.md                ← Challenge Versionsfreiheit vs. Lockstep
│   ├── D2-groupid-namespace.md
│   ├── D3-reactor-strategie.md
│   └── D4-build-release-workflow.md
├── status/
│   ├── STATE.md                           ← lebendes Zustandsdokument
│   ├── TEMPLATE.md                        ← Vorlage für Wiedervorlage-Dokumente
│   ├── 2026-07-25-M0-planung.md           ← Zustandsdokument Welle M0
│   └── <datum>-<welle>-<thema>.md         ← Zustandsberichte nach jeder Welle
└── reports/                               ← Agenten-Ergebnisberichte, Audits
```

**Zustandsdokument nach jeder Welle:** `status/TEMPLATE.md` kopieren nach
`status/<YYYY-MM-DD>-<welle>-<thema>.md` und vollständig ausfüllen. Kein
Abschnitt wird gelöscht; nicht zutreffende Abschnitte werden mit „entfällt,
weil …" begründet. Anschließend `STATE.md`, die Statustabelle in Abschnitt 5
dieser Datei und das JF-Dokument aktualisieren.

Zusätzlich außerhalb dieses Ordners:

```
docs/jf/
├── 2026-07-morphium-modularisierung.md      ← JF-Präsentationsdokument (deutsch)
├── 2026-07-morphium-modularization.en.md    ← englische Fassung für den Versand
├── 2026-07-morphium-modularisierung.pdf     ← erzeugt, nicht handisch editieren
├── 2026-07-morphium-modularization.pdf      ← erzeugt, nicht handisch editieren
├── build-pdf.sh                             ← pandoc + xelatex; ohne Argument englisch
├── pdf-header.tex                           ← LaTeX-Vorspann (Kopfzeile, Glyph-Fallbacks)
├── pdf-structure.lua                        ← Überschriftenebenen, Tabellenbreiten
└── pdf-links.lua                            ← Repo-Links zu Klartext (PDF geht per Mail)
```

**Nach jeder Welle** wird das JF-Dokument in **beiden** Sprachfassungen
nachgezogen und beide PDFs neu erzeugt (`./build-pdf.sh` und `./build-pdf.sh
2026-07-morphium-modularisierung.md`). Die PDFs sind Build-Ergebnisse — inhaltliche
Änderungen gehören ausschließlich in die Markdown-Quellen.
