# D1 — Versionierung der Erweiterungsmodule

| Feld | Wert |
|---|---|
| Status | **OFFEN — Entscheidung im nächsten JF** |
| Angelegt | 2026-07-25 |
| Vorzugsvariante | **B — Lockstep mit der Morphium-Version** |
| Arbeitsannahme bis zur Klärung | Variante B |
| Betrifft | M1, M3, M5, M6 |

---

## Die Frage

Behalten `morphium-jakarta-data`, `quarkus-morphium` und `spring-boot-morphium`
nach der Integration ihre **eigenen Versionslinien** (heute `1.1.0`, `1.2.0`,
`1.0.0-SNAPSHOT`), oder übernehmen sie die **Version des Morphium-Parent**
(`6.2.6-SNAPSHOT`)?

Der Auftrag lautet ausdrücklich: *„Ein wichtiger Punkt ist die Versionsfreiheit
des optionalen Paketes oder ob wir uns direkt der Morphium Version anschließen.
Bitte challengen."* Das folgende ist genau das — inklusive der Gegenargumente
zur Empfehlung.

---

## Variante A — Eigene Versionslinie

Modul behält `<version>1.2.0</version>` und trackt die Kernversion über eine
Property `<morphium.version>`. Das ist der Status quo der drei Repos.

### Argumente dafür

**A1 — Semantische Ehrlichkeit.** SemVer bindet die Versionsnummer an die
Kompatibilität *dieses* Artefakts. Wenn Morphium 6.2.6 → 6.3.0 geht, weil sich
im Wire-Protokoll etwas ändert, hat sich an der API von
`morphium-jakarta-data` nichts geändert. Eine erzwungene 6.3.0 lügt über einen
Änderungsumfang, den es nicht gab. Umgekehrt schlimmer: ein Breaking Change in
`AbstractMorphiumRepository` müsste bei Lockstep auf den nächsten
Major-Release des Kerns warten oder wäre in einer 6.2.7 versteckt — SemVer-Bruch.

**A2 — Entkoppelte Release-Kadenz.** Die Quarkus-Extension muss auf
Quarkus-Releases reagieren, nicht auf Morphium-Releases. Quarkus veröffentlicht
alle 2–4 Wochen; ein Upgrade von Quarkus 3.32 → 3.36 ist eine
Extension-Änderung ohne jeden Bezug zum Kern. Bei Lockstep erfordert dieses
Upgrade einen vollständigen Morphium-Release inklusive 90-Minuten-Testsuite
und Sonatype-Bundle für Kern und PoppyDB — für eine geänderte
Property-Zeile. Analog: Spring Boot 3.4 → 3.5.

**A3 — Kompatibilitätsmatrix bleibt ausdrückbar.** Bei eigener Version kann
`quarkus-morphium 1.3.0` sowohl gegen Morphium 6.2.x als auch 6.3.x
funktionieren und das dokumentieren. Bei Lockstep ist die Matrix per
Konstruktion diagonal — was Flexibilität kostet, wenn ein Anwender auf einer
älteren Kernversion festhängt.

**A4 — Geringster Migrationsaufwand.** Die drei Repos sind heute schon so
gebaut. Variante A erfordert an den POM-Versionen gar nichts.

### Argumente dagegen

**A5 — Der Status quo ist empirisch gescheitert.** Genau diese Freiheit hat den
heutigen Zustand produziert:

```
morphium              6.2.6-SNAPSHOT   (origin/develop)
morphium-jakarta-data 1.1.0-SNAPSHOT → morphium 6.2.5-SNAPSHOT
quarkus-morphium      1.2.0          → morphium 6.2.5-SNAPSHOT, mjd 1.1.0-SNAPSHOT
spring-boot-morphium  1.0.0-SNAPSHOT → morphium 6.2.4,          mjd 1.1.0
```

Vier verschiedene Meinungen darüber, was „die aktuelle Morphium-Version" ist,
Mischung aus SNAPSHOT und Release über Modulgrenzen hinweg, und in der
quarkus-morphium-Historie mehrere Commits, die ausschließlich Versions-Properties
nachziehen (`build: align morphium-jakarta-data to 1.1.0-SNAPSHOT on develop`,
`build: pin morphium to 6.2.4 release on main`, `build: bump morphium to
6.2.5-SNAPSHOT on develop`). Das ist wiederkehrender manueller Aufwand ohne
fachlichen Ertrag — und er wächst quadratisch mit der Modulzahl.

**A6 — Vier Versionsnummern ≠ vier unabhängige Artefakte.** Die Module sind
faktisch eng gekoppelt: `registerTypeIds()` (#166) und
`preRegisterClassesWithAnnotation()` (#200) wurden **eigens für** die
Framework-Integrationen in den Kern eingebracht. Der Commit
`fix: use ClassGraphCache.preRegisterClassesWithAnnotation (renamed in morphium)`
in quarkus-morphium ist der Beweis: eine Kern-Umbenennung erzwang unmittelbar
eine Extension-Änderung. Die behauptete Unabhängigkeit existiert in der
Praxis nicht.

**A7 — In einem Mono-Repo ist eigene Versionierung teuer.** Der Reactor baut
alles gemeinsam. Ein Modul mit abweichender Version kann `${project.version}`
nicht nutzen, braucht eine eigene Property, eine eigene `dependencyManagement`-
Zeile, eigene Einträge in `release.sh` und eine eigene Logik in
`maven-release-plugin` (`autoVersionSubmodules=true` — heute aktiv — setzt
genau das Gegenteil voraus). Man zahlt den Mono-Repo-Preis, ohne den
Mono-Repo-Nutzen zu bekommen.

**A8 — `poppydb` beweist, dass Lockstep im Projekt trägt.** PoppyDB ist ein
eigenständiges Produkt — ein MongoDB-wire-kompatibler Server mit eigenem CLI,
eigener Doku (`docs/poppydb.md`), eigenem Assembly. Es hat trotzdem
`${project.version}` und keine eigene Linie. Das ist die etablierte Konvention
des Zielprojekts. Ein neues Modul mit abweichender Regel bräuchte eine starke
Begründung.

---

## Variante B — Lockstep mit der Morphium-Version (Empfehlung)

Modul erbt Version vom `morphium-parent`, Abhängigkeit auf den Kern über
`${project.version}` — exakt wie `poppydb`.

```xml
<parent>
  <groupId>de.caluga</groupId>
  <artifactId>morphium-parent</artifactId>
  <version>6.2.6-SNAPSHOT</version>
</parent>
<artifactId>morphium-jakarta-data</artifactId>
<!-- keine <version> -->
```

### Argumente dafür

**B1 — Nulldeutige Kompatibilität.** `de.caluga:quarkus-morphium:6.3.0`
funktioniert mit `de.caluga:morphium:6.3.0`. Keine Matrix, kein
Kompatibilitätsdokument, keine Support-Frage „welche Extension-Version passt zu
meinem Morphium?". Bei drei Modulen plus Kern plus PoppyDB ersetzt das eine
5×N-Matrix durch eine Identität.

**B2 — Ein Release-Vorgang.** `release.sh` bumpt via
`autoVersionSubmodules=true` alle Module in einem Schritt, baut ein Bundle,
lädt es einmal zu Sonatype. Keine Reihenfolgeabhängigkeit (heute: Kern
releasen → warten → mjd releasen → warten → Extension releasen), keine
Zwischenzustände mit dangling SNAPSHOT-Referenzen.

**B3 — Die Verflechtungs-Commits verschwinden.** Alle „align/bump/pin"-Commits
entfallen ersatzlos, weil `${project.version}` sich selbst nachzieht.

**B4 — Konsistenz mit der Projektkonvention.** Siehe A8. Ein Contributor, der
`poppydb` versteht, versteht die neuen Module ohne Zusatzerklärung. Das ist bei
einem PR gegen ein fremdes Projekt ein relevantes Akzeptanzargument.

**B5 — Präzedenz in der Java-Welt.** Quarkus selbst versioniert alle ~400
Extensions im Lockstep mit dem Core. Spring Boot versioniert alle Starter im
Lockstep. Micronaut ebenso. Für *plattformnahe Integrationsmodule* ist Lockstep
der etablierte Standard, nicht die Ausnahme — genau weil die
Kompatibilitätsmatrix sonst unbeherrschbar wird.

### Argumente dagegen (die ernst zu nehmenden)

**B6 — Quarkus-/Spring-Upgrades erzwingen Kern-Releases.** Das ist A2 und der
stärkste Einwand. Er ist real, aber abgemildert: Quarkus-Extensions müssen bei
einem Quarkus-Minor-Upgrade in der Regel **nicht** neu released werden —
`requires-quarkus-core: "[3.32,)"` deckt kommende Minors ab; Anwender können
eine gegen 3.32 gebaute Extension mit Quarkus 3.36 nutzen. Ein Extension-Release
ist nur bei tatsächlichen Breaking Changes der Quarkus-Build-API nötig. Und
Morphium released ohnehin häufig — 6.2.0 bis 6.2.6 innerhalb von etwa fünf
Monaten.

**B7 — Versionssprung ohne Inhalt.** `morphium-jakarta-data` springt von `1.1.0`
auf `6.2.7`. Für bestehende Nutzer der Bardioc1977-Artefakte ist das
irritierend. Mitigation: Da diese Artefakte nie auf Maven Central lagen (nur
GitHub Packages unter `Bardioc1977`), ist die installierte Basis praktisch leer.
Der Sprung ist ein Einmalereignis, dokumentiert im CHANGELOG und in einer
Migrationsnotiz. Das ist billiger als dauerhafte Matrixpflege.

**B8 — Kernrelease bei reinem Extension-Bugfix.** Ein Bug nur in der Extension
erzwingt Version 6.2.8 für Kern und PoppyDB, obwohl sich dort nichts ändert.
Praktisch unkritisch: Patch-Releases sind billig, und ein identischer Kern unter
neuer Patchnummer schadet niemandem. Die Alternative — divergierende
Versionslinien — kostet dauerhaft mehr.

---

## Variante C — Hybrid (verworfen, aber der Vollständigkeit halber)

`morphium-jakarta-data` im Lockstep (kernnah, Vertragsschicht), die
Framework-Adapter mit eigener Linie (extern getriebene Kadenz).

**Verworfen**, weil das die Nachteile beider Varianten kombiniert: Der Reactor
enthält dann sowohl `${project.version}`- als auch property-versionierte
Module, `autoVersionSubmodules` funktioniert nur teilweise, `release.sh`
braucht zwei Codepfade, und für Anwender ist nicht mehr erkennbar, welche
Regel für welches Artefakt gilt. Der Erklärungsaufwand übersteigt den
gewonnenen Freiheitsgrad. Eine gemischte Regel ist schlechter als jede der
beiden konsistenten Regeln.

---

## Empfehlung

**Variante B — Lockstep.** Begründung in einem Satz: Die Module sind keine
unabhängigen Produkte, sondern Integrationsschichten mit nachgewiesener
Kopplung an Kern-APIs; die Freiheit der Variante A hat in fünf Monaten
messbar nur Pflegeaufwand und Versionsdivergenz erzeugt und keinen einzigen
Fall, in dem sie einen Nutzen gebracht hätte.

Sekundär, aber nicht unwichtig: Für den PR-Empfänger (Stephan) ist Variante B
die risikoärmere Option. Sie folgt seiner eigenen Konvention (`poppydb`),
erfordert keine Änderung an `maven-release-plugin`-Konfiguration und
`release.sh` bekommt drei analoge Blöcke statt eines zweiten Release-Regimes.
Ein PR, der die bestehende Konvention fortschreibt, wird eher angenommen als
einer, der eine zweite einführt.

### Absicherung gegen den Haupteinwand (B6)

Damit B6 nicht zum echten Problem wird, gehört Folgendes verbindlich zur
Umsetzung:

1. **`requires-quarkus-core` mit offener Obergrenze** — `"[3.32,)"`, wie heute.
   Damit deckt ein Extension-Build kommende Quarkus-Minors ab.
2. **Quarkus- und Spring-Boot-Version als Parent-Property** —
   `<quarkus.version>` und `<spring-boot.version>` in `morphium-parent`, damit
   ein Framework-Upgrade eine Einzeiländerung an einer Stelle ist.
3. **Dokumentierte Mindestversionen** je Modul in dessen README, statt einer
   Kompatibilitätsmatrix.
4. **Kompatibilitätszusage im CHANGELOG:** „Extension-Artefakte der Reihe 6.2.x
   sind untereinander gegen jede Morphium-6.2.x austauschbar." Damit ist B8
   auch für Anwender entschärft.

---

## Was der Orchestrator bis zur JF-Entscheidung tut

- Arbeitet mit **Variante B**.
- Kennzeichnet in jedem erzeugten POM die Stelle mit
  `<!-- ENTSCHEIDUNG-OFFEN D1: Lockstep. Bei Variante A hier eigene <version> -->`.
- Hält die Rückabwicklung auf Variante A billig: Sie besteht aus dem Einfügen
  eines `<version>`-Elements pro Modul-POM plus einer `<morphium.version>`-Property.
  **Keine** Codeänderung, keine Paketumbenennung hängt an D1. Diese Eigenschaft
  ist bei jedem Task zu erhalten.

---

## JF-Entscheidungsvorlage (eine Folie)

> **Frage:** Eigene Versionen für die drei Erweiterungsmodule oder Lockstep mit
> Morphium?
>
> **Empfehlung:** Lockstep — Modulversion = Morphium-Version, wie bei PoppyDB.
>
> **Dafür:** Kompatibilität ist trivial (6.3.0 zu 6.3.0), ein Release-Vorgang
> statt vier, sämtliche „bump/align/pin"-Commits entfallen, Konvention des
> Zielprojekts wird fortgeschrieben, entspricht dem Vorgehen von Quarkus und
> Spring Boot bei ihren Integrationsmodulen.
>
> **Dagegen:** Ein Quarkus- oder Spring-Boot-Upgrade kann einen Morphium-Release
> anstoßen. Abgemildert durch offene Versionsobergrenze in der
> Extension-Metadaten (`[3.32,)`) — die meisten Framework-Minors erfordern
> keinen Neubau.
>
> **Einmalkosten:** Versionssprung 1.x → 6.2.x. Unkritisch, weil die Artefakte
> nie auf Maven Central lagen.
>
> **Rückabwicklung:** billig — ein `<version>`-Element pro POM.
