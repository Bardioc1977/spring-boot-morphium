# D2 — groupId und Namensraum der Artefakte

| Feld | Wert |
|---|---|
| Status | **OFFEN — Entscheidung im nächsten JF** |
| Angelegt | 2026-07-25 |
| Vorzugsvariante | **`de.caluga` für alle Artefakte** |
| Arbeitsannahme bis zur Klärung | `de.caluga` |
| Betrifft | M3, M4, M5, M6 |

---

## Die Frage

`quarkus-morphium` verwendet heute `groupId = io.quarkiverse.morphium`. Diese
groupId ist nach der Integration nicht haltbar. Welche groupId und welche
Artefakt-IDs gelten künftig?

---

## Ausgangslage

| Projekt | groupId heute | artifactIds heute |
|---|---|---|
| morphium-jakarta-data | `de.caluga` | `morphium-jakarta-data` |
| quarkus-morphium | `io.quarkiverse.morphium` | `quarkus-morphium-parent`, `quarkus-morphium`, `quarkus-morphium-deployment`, `quarkus-morphium-testing` |
| spring-boot-morphium | `de.caluga` | `spring-boot-morphium-parent`, `-autoconfigure`, `-starter`, `-test` |

Kern und PoppyDB liegen unter `de.caluga` (`morphium`, `poppydb`,
`morphium-parent`).

---

## Warum `io.quarkiverse.morphium` nicht bleiben kann

1. **Namensraum-Eigentum.** `io.quarkiverse` gehört der Quarkiverse-Organisation.
   Für einen Central-Deploy unter dieser groupId braucht man einen
   Namespace-Nachweis auf `central.sonatype.com` — den bekommt nur, wer
   `quarkiverse.io` kontrolliert oder von der Organisation autorisiert ist. Wir
   sind nicht autorisiert; das ist der Grund, warum das Vorhaben überhaupt
   entsteht (die Quarkiverse-Verantwortlichen reagieren nicht).
2. **Der Publish würde technisch abgelehnt.** Sonatype validiert das
   Namespace-Ownership beim Upload. Der Deploy schlägt fehl, nicht erst der
   Review.
3. **Irreführung.** Ein Artefakt unter `io.quarkiverse.*`, das nicht im
   Quarkiverse liegt, suggeriert eine Herkunft und ein Supportmodell, die es
   nicht hat. Das ist auch ohne technische Hürde nicht in Ordnung.
4. **Widerspruch zur Modulzugehörigkeit.** Nach der Integration ist die
   Extension ein Morphium-Modul. Eine fremde groupId innerhalb des
   `morphium-parent`-Reactors ist verwirrend und bricht mit `poppydb`.

---

## Varianten

### Variante A — `de.caluga` für alles (Empfehlung)

```xml
<groupId>de.caluga</groupId>
<artifactId>quarkus-morphium</artifactId>
```

Erbt die groupId vom `morphium-parent`, keine eigene Deklaration nötig.

**Dafür:** Ein Namensraum für das gesamte Projekt; `de.caluga` ist bereits als
Central-Namespace freigegeben (Kern und PoppyDB sind dort publiziert), also
keine neue Namespace-Beantragung; konsistent mit `poppydb` und
`morphium-jakarta-data`; kürzeste Koordinaten.

**Dagegen:** Bricht die Quarkus-Konvention, nach der Extensions unter
`io.quarkiverse.<name>` liegen. Praktisch irrelevant: Diese Konvention gilt
*für Quarkiverse-Extensions*. Extensions außerhalb des Quarkiverse verwenden
routinemäßig ihre eigene groupId — Beispiele: `org.mongodb:quarkus-mongodb-*`
wäre so, `io.debezium`, `com.datastax`, `org.kie`. Die Konvention, die für uns
zählt, ist die technische aus dem Quarkus-Extension-Guide (Struktur
runtime/deployment, `quarkus-extension.yaml`, `extension-descriptor`-Goal), und
die ist groupId-unabhängig.

### Variante B — Eigener Namensraum, z. B. `de.caluga.quarkus`

**Dafür:** Trennt Erweiterungen optisch vom Kern.

**Dagegen:** Neuer Namespace muss bei Sonatype freigegeben werden (Subdomain von
`de.caluga` — geht, aber zusätzlicher Vorgang, den nur Stephan als
Domaininhaber durchführen kann). Kein funktionaler Gewinn: Die Trennung ist
schon über die artifactId sichtbar. Erhöht die Zahl der Namespaces, die im
Release-Skript und in Bundles zu behandeln sind.

### Variante C — Antrag beim Quarkiverse (parallel weiterverfolgen)

Nicht als Ersatz, sondern als perspektivische Option. Falls die
Quarkiverse-Verantwortlichen doch reagieren, kann die Extension zusätzlich dort
gespiegelt werden. Das ist ausdrücklich **kein Blocker** und keine Vorbedingung
für M3/M4.

---

## Empfehlung

**Variante A.** Alle Artefakte unter `de.caluga`, groupId wird vom
`morphium-parent` geerbt und in den Modul-POMs nicht wiederholt.

### Resultierende Koordinaten

| Modul | Koordinaten (bei D1=Lockstep) |
|---|---|
| Kern | `de.caluga:morphium:6.2.x` |
| PoppyDB | `de.caluga:poppydb:6.2.x` |
| Jakarta Data | `de.caluga:morphium-jakarta-data:6.2.x` |
| Quarkus Runtime | `de.caluga:quarkus-morphium:6.2.x` |
| Quarkus Deployment | `de.caluga:quarkus-morphium-deployment:6.2.x` |
| Quarkus Testing | `de.caluga:quarkus-morphium-testing:6.2.x` |
| Spring Autoconfigure | `de.caluga:spring-boot-morphium-autoconfigure:6.2.x` |
| Spring Starter | `de.caluga:spring-boot-morphium-starter:6.2.x` |
| Spring Test | `de.caluga:spring-boot-morphium-test:6.2.x` |

Die `*-parent`-POMs der drei Teilprojekte entfallen — ihre Rolle übernimmt
`morphium-parent`. Bei `quarkus-morphium` und `spring-boot-morphium` bedeutet
das: aus je vier POM-Ebenen werden drei.

> **Artefakt-IDs bleiben unverändert.** Nur die groupId der Quarkus-Module
> ändert sich. Das hält den Bruch für bestehende Nutzer minimal: eine Zeile in
> deren POM.

---

## Konsequenzen, die die Umsetzung berücksichtigen muss

1. **Java-Pakete bleiben, wie sie sind** — `de.caluga.morphium.quarkus.*`,
   `de.caluga.morphium.spring.*`, `de.caluga.morphium.data.*`. Kein
   Paket-Refactoring. Der Aufwand von D2 ist reine POM-Arbeit.
2. **`quarkus-extension.yaml` muss angepasst werden.** Das Feld `artifact`
   enthält heute `io.quarkiverse.morphium:quarkus-morphium::jar:1.2.0`. Es wird
   vom `extension-descriptor`-Goal generiert; die handgeschriebene Variante
   unter `src/main/resources/META-INF/` enthält es nicht und braucht keine
   Änderung. Die Felder `guide` und `scm-url` zeigen auf Bardioc1977 und müssen
   auf `sboesebeck/morphium` umgestellt werden.
3. **`deployment`-Modul referenziert die groupId explizit** —
   `io.quarkiverse.morphium:quarkus-morphium`. Nach der Umstellung
   `${project.groupId}` verwenden, dann bleibt es künftig automatisch korrekt.
4. **Migrationsnotiz für Anwender** ist Pflicht: alte → neue Koordinaten,
   im CHANGELOG und in der Modul-README.
5. **`distributionManagement` auf GitHub Packages entfällt.** Der
   quarkus-morphium-Parent deklariert heute
   `maven.pkg.github.com/Bardioc1977/quarkus-morphium`. Nach der Integration
   gilt der Central-Pfad des Kernprojekts.

---

## JF-Entscheidungsvorlage (eine Folie)

> **Frage:** Welche groupId für die Quarkus-Extension nach der Integration?
>
> **Problem:** `io.quarkiverse.morphium` ist nicht unser Namensraum. Ein
> Central-Deploy darunter wird von Sonatype abgelehnt, weil wir das
> Namespace-Ownership nicht nachweisen können — und genau die
> Quarkiverse-Aufnahme, die das legitimieren würde, kommt nicht zustande.
>
> **Empfehlung:** `de.caluga` für alle Artefakte, geerbt vom `morphium-parent`.
> Namensraum ist bereits für Central freigegeben, kein neuer Antrag nötig.
>
> **Auswirkung für Anwender:** eine geänderte Zeile in der POM. Artefakt-IDs und
> Java-Pakete bleiben identisch.
>
> **Nicht betroffen:** Die technischen Quarkus-Extension-Vorgaben (Struktur,
> Metadaten, Build-Plugins) sind groupId-unabhängig und werden vollständig
> eingehalten.
