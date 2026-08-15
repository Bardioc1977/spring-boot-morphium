# D4 — Build-, CI- und Release-Workflow-Harmonisierung

| Feld | Wert |
|---|---|
| Status | **OFFEN — Entscheidung im nächsten JF** |
| Angelegt | 2026-07-25 |
| Vorzugsvariante | `release.sh` erweitern + CI upstream einführen |
| Arbeitsannahme bis zur Klärung | Vorzugsvariante |
| Betrifft | M2, M4, M5, **M6 (Hauptumsetzung)** |

---

## Die Frage

Der Auftrag nennt die Anpassung an die Morphium-Build-Workflows ausdrücklich als
JF-Thema. Drei Teilfragen:

1. Wie kommen die neuen Module ins Maven-Central-Bundle?
2. Wie sieht CI im Upstream-Repo aus (heute: praktisch nicht vorhanden)?
3. Was passiert mit den Workflows der drei aufzulösenden Repos?

---

## Ausgangslage (verifiziert)

### Upstream `sboesebeck/morphium`

`.github/workflows/` auf `origin/develop` enthält **nur**:
- `deploy-docs.yml` — MkDocs nach GitHub Pages
- `sync-wiki.yml`

Es gibt **kein** `build.yml`. Ein `build.yml` existiert ausschließlich auf
`fork/develop` als bewusst fork-only Commit `21ca5ab9`. Upstream werden PRs
also ohne automatisierten Build gemergt.

Der Release läuft über `release.sh` — ein etwa 1000-zeiliges Bash-Skript, lokal
und manuell ausgeführt. Ablauf: Versionsprüfung → `mvn release:prepare` (Tag +
Version-Bump) → Tag auschecken → `mvn clean package verify -DskipTests` →
GPG-Signierung + Checksums → **ein kombiniertes Bundle** aus
`morphium-parent` (POM), `morphium` und `poppydb` → Upload an
`central.sonatype.com/api/v1/publisher/upload` → Merge nach `master`.

Die Modulnamen stehen hartcodiert im Skript:
- Zeile ~630: `for module_dir in morphium-core poppydb`
- Zeilen ~842ff: je Modul ein Kopier-/Signier-Block
- Zeile ~886: Verifikationsschleife über `morphium` und `poppydb`

### Die drei Erweiterungs-Repos

| Repo | Workflows | Besonderheit |
|---|---|---|
| morphium-jakarta-data | `build.yml` | baut morphium aus Quelle vorab |
| quarkus-morphium | `build.yml`, `docs.yml`, `release.yml` | klont morphium **und** mjd, deployt nach GitHub Packages, triggert Showcase-Rebuild via `repository-dispatch` |
| spring-boot-morphium | `build.yml` + weitere | klont morphium (fork/develop) und mjd (main) |

Alle drei bauen ihre Abhängigkeiten im CI **aus dem Quellcode**, weil Morphium
zum Zeitpunkt der Einrichtung nicht auf Central lag. Nach der Integration
entfällt dieser gesamte Mechanismus — das ist der größte Einzelgewinn des
Vorhabens auf der Build-Seite: aus drei CI-Pipelines mit vorgeschalteten
Quell-Builds wird ein `mvn verify`.

---

## Teilfrage 1: Maven Central

### Empfehlung: `release.sh` erweitern, Struktur beibehalten

Das Skript erzeugt bereits ein **kombiniertes** Bundle mit
Maven-Repository-Layout unter `target/bundle-staging/de/caluga/...`. Neue Module
sind additiv: pro Modul ein Block analog zum `poppydb`-Block.

Zu erweitern:
1. `for module_dir in morphium-core poppydb` → um die neuen Verzeichnisse ergänzen
2. Je Modul ein Kopier-/Signier-Block (`.pom`, `.jar`, `-sources.jar`, `-javadoc.jar`)
3. Verifikationsschleife um die neuen artifactIds ergänzen
4. `integration-tests` **ausschließen** — Testmodul, gehört nicht nach Central

Bei D1 = Lockstep bleibt die Bundle-Logik strukturell identisch: eine Version für
alle, ein Upload, ein Sonatype-Deployment. Bei Variante A müsste das Skript
mehrere Versionen parallel verwalten — ein weiteres Argument für Lockstep.

**Central-Pflichten pro neuem Artefakt** (Sonatype validiert das):
`.pom` mit `name`, `description`, `url`, `licenses`, `scm`, `developers`
(erbt vom Parent — deshalb reicht ein knappes Modul-POM), `sources.jar`,
`javadoc.jar`, GPG-Signaturen und Checksums für jede Datei. Das
`maven-javadoc-plugin` steht im Parent-`pluginManagement` mit
`failOnError=false`; jedes neue Modul muss das Plugin in seinem `<build>`
aktivieren — genau wie `poppydb` es tut. **Häufigster Fehler:** vergessenes
`maven-source-plugin` oder `maven-javadoc-plugin` im Modul-POM → Bundle wird
von Sonatype nach dem Upload abgelehnt.

### Verworfene Alternative: `nexus-staging-maven-plugin` / `central-publishing-maven-plugin`

Technisch moderner und würde das Bash-Skript teilweise ersetzen. **Verworfen für
dieses Vorhaben**, weil es den Release-Prozess des Projekts umbaut, das ist eine
Entscheidung des Hauptmaintainers, nicht Teil einer Modul-Integration. Ein PR,
der Modulintegration *und* Release-Umbau mischt, ist schwerer zu bewerten und
wird eher abgelehnt. Als separater Vorschlag nach M6 sinnvoll.

---

## Teilfrage 2: CI im Upstream

Dass Upstream kein `build.yml` hat, ist heute schon ein Risiko; mit drei
zusätzlichen Modulen und Framework-Abhängigkeiten wird es zu einem echten. Wir
haben ein erprobtes `build.yml` auf `fork/develop` — bislang bewusst
fork-only.

**Empfehlung:** In M6 ein `build.yml` **als eigenen, separaten PR** anbieten,
nicht als Teil eines Modul-PRs. Begründung: Ein Workflow, der bei jedem PR läuft,
ist eine Prozessänderung im Projekt eines anderen Maintainers und braucht eine
eigene Zustimmung. Modul-PRs sollen daran nicht scheitern.

Vorgeschlagene Job-Matrix:

| Job | Kommando | Zweck | Laufzeit |
|---|---|---|---|
| `core` | `mvn -B verify -DskipExtensions -Dgroups=core` | Kern-Isolation (D3/I3) | mittel |
| `full` | `mvn -B verify` | Alles inkl. Erweiterungen | lang |
| `isolation` | `scripts/verify-core-isolation.sh` | Invarianten I1–I3 | Sekunden |

Die Volltestsuite dauert ~90 Minuten. Für PR-Läufe daher `-Dgroups=core`; die
vollständige Suite nachts oder bei Push auf `develop`.

**Nicht zu übernehmen:** die fork-spezifischen Teile — Klonen von Morphium aus
Quelle (nach Integration überflüssig), Deploy nach GitHub Packages unter
`Bardioc1977`, `repository-dispatch` an das Showcase-Repo.

---

## Teilfrage 3: Stilllegung der Alt-Repos

Die vier Bardioc1977-Repos werden nach dem jeweiligen Merge stillgelegt — aber
**nicht gelöscht**. Vorgehen je Repo:

1. Alle Workflows deaktivieren (`on: workflow_dispatch` als einziger Trigger).
2. README-Kopf mit Verweis auf `sboesebeck/morphium` und die neuen Koordinaten
   sowie einer Migrationstabelle alt → neu.
3. Letztes Tag setzen, damit der Stand referenzierbar bleibt.
4. Repository auf GitHub archivieren (read-only). **Nicht löschen** — Issues,
   PR-Diskussionen und die Historie sind der Nachweis der Herkunft der
   Beiträge.
5. Offene Dependabot-PRs schließen.

**Sonderfall `quarkus-morphium-showcase`:** Das Repo wird nicht integriert (es
ist eine Demo-Anwendung, kein Modul) und bleibt bestehen. Es muss aber auf die
neuen Koordinaten umgestellt und sein `repository-dispatch`-Trigger entfernt
werden, weil das Quell-Repo verschwindet. Aufgabe in M6.

---

## Teilfrage 4: Dokumentations-Toolchain (Folgefrage aus M3)

| Projekt | Format | Toolchain |
|---|---|---|
| morphium | Markdown | MkDocs Material → GitHub Pages (`deploy-docs.yml`) |
| quarkus-morphium | AsciiDoc | Antora (`docs/modules/ROOT/pages/*.adoc`, `docs.yml`) |
| spring-boot-morphium | Markdown | README-zentriert |

Die Quarkus-Extension-Konventionen erwarten Antora-Doku (`guide`-Feld in
`quarkus-extension.yaml` zeigt darauf). Morphium nutzt MkDocs.

**Empfehlung:** Beides koexistieren lassen.
- Die Antora-Quellen bleiben im Modulverzeichnis `quarkus-morphium/docs/` —
  sie sind Teil der Extension-Konvention und dort erwartet.
- Zusätzlich eine Übersichtsseite `docs/quarkus-extension.md` in der
  MkDocs-Navigation, die auf die Kernkonzepte eingeht und auf die Antora-Doku
  verweist. Analog `docs/jakarta-data.md` und `docs/spring-boot.md`.
- Kein Konvertierungsprojekt. Ein Format-Vereinheitlichungs-Refactoring von
  14 AsciiDoc-Seiten bringt keinen Nutzen und viel Risiko.

Ob der Antora-Build (`docs.yml`) upstream übernommen wird, ist Teil des M6-PRs
zur CI und explizit nachrangig.

---

## Empfehlung zusammengefasst

| Thema | Entscheidung |
|---|---|
| Central | `release.sh` additiv erweitern, kombiniertes Bundle beibehalten |
| Release-Plugin-Umbau | **nicht** in diesem Vorhaben |
| CI upstream | `build.yml` in M6 als **separater** PR, dreistufige Matrix |
| Alt-Repos | archivieren, nicht löschen; README als Wegweiser |
| Showcase | bleibt eigenständig, wird auf neue Koordinaten umgestellt |
| Doku | MkDocs für Übersichtsseiten, Antora im Quarkus-Modul, keine Konvertierung |

---

## JF-Entscheidungsvorlage (eine Folie)

> **Central:** `release.sh` bekommt pro neues Modul einen Block analog zu
> PoppyDB. Ein Bundle, ein Upload, eine Version — vorausgesetzt D1 = Lockstep.
> Der Release-Prozess selbst wird nicht umgebaut.
>
> **CI:** Upstream hat heute keinen Build-Workflow — nur Docs und Wiki-Sync. Wir
> haben ein erprobtes `build.yml` im Fork. Vorschlag: als eigener PR anbieten
> (dreistufig: Kern isoliert / vollständig / Invariantencheck), damit die
> Modul-PRs nicht an einer Prozessdiskussion hängen.
>
> **Gewinn:** Heute bauen drei CI-Pipelines Morphium aus dem Quellcode, weil die
> Artefakte nicht auffindbar waren. Nach der Integration ist das ein `mvn verify`.
>
> **Alt-Repos:** archivieren statt löschen — Historie und PR-Diskussionen
> bleiben als Herkunftsnachweis erhalten. Showcase bleibt bestehen und wird
> umgestellt.
