# D3 — Reactor-Strategie und die technische Bedeutung von „optional"

| Feld | Wert |
|---|---|
| Status | **OFFEN — Entscheidung im nächsten JF** |
| Angelegt | 2026-07-25 |
| Vorzugsvariante | **Profil `extensions`, per Default aktiv** |
| Arbeitsannahme bis zur Klärung | Vorzugsvariante |
| Betrifft | M1, M3, M5, M6 |

---

## Die Frage

Der Auftrag verlangt, dass die drei Erweiterungen **optionale Module** von
Morphium sind. Wie wird das in Maven konkret realisiert — und was heißt
„optional" hier überhaupt?

---

## Klarstellung vorab: `<optional>true</optional>` ist nicht die Antwort

Das ist die naheliegende, aber falsche Lesart, und sie muss ausgeschlossen
werden, bevor jemand sie umsetzt.

`<optional>true</optional>` ist ein Attribut **einer Dependency** und bedeutet:
„A braucht B zum Kompilieren, aber wer A einbindet, bekommt B nicht automatisch
mitgeliefert." Es setzt also eine Abhängigkeit `A → B` voraus.

Hier existiert diese Abhängigkeit gar nicht:

```
morphium-jakarta-data ──→ morphium          (existiert, compile)
morphium              ──→ morphium-jakarta-data   (existiert NICHT und darf nie entstehen)
```

Es gibt nichts, was man als „optional" markieren könnte. Der Kern kennt die
Erweiterungen nicht. Die Optionalität entsteht dadurch, dass ein Anwender das
Erweiterungs-Artefakt in seine POM aufnimmt — oder eben nicht.

**Zusammengefasst:** „Optionales Modul" = eigenes, separat konsumierbares
Artefakt im gleichen Reactor, mit strikt einseitiger Abhängigkeitsrichtung. Kein
POM-Flag.

Der einzige legitime Einsatz von `<optional>true</optional>` in diesem Vorhaben
ist *innerhalb* der Erweiterungen — dort ist es bereits korrekt verwendet, z. B.
`quarkus-smallrye-health` und `quarkus-jackson` im Quarkus-Runtime-Modul. Das
bleibt unverändert.

---

## Die Invarianten, die tatsächlich sicherzustellen sind

| # | Invariante | Verifikationsbefehl |
|---|---|---|
| I1 | `morphium-core/pom.xml` enthält keine Referenz auf ein Erweiterungsmodul | `grep -E 'jakarta-data\|quarkus\|spring' morphium-core/pom.xml` → keine Treffer |
| I2 | Der Kern-Dependency-Tree ist frei von Erweiterungen und deren Fremd-APIs | `mvn -q -pl morphium-core dependency:tree` → kein `jakarta.data`, `io.quarkus`, `org.springframework` |
| I3 | Kern und PoppyDB sind ohne die Erweiterungen baubar | `mvn -pl morphium-core,poppydb -am verify` |
| I4 | `morphium-parent` erzwingt keine Fremd-BOM für Kern-Builds | Quarkus-/Spring-BOM-Import steht **nicht** im Parent-`dependencyManagement`, sondern im jeweiligen Modul |
| I5 | Keine zyklische Modulabhängigkeit | `mvn validate` am Root (Maven erkennt Zyklen selbst) |

I4 ist der subtilste Punkt und die häufigste Fehlerquelle bei solchen
Integrationen: Ein `<scope>import</scope>` der Quarkus-BOM im Parent würde bei
*jedem* Kern-Build eine Auflösung von ~400 Quarkus-Artefakten auslösen. Die
BOM-Imports müssen deshalb in `quarkus-morphium/pom.xml` bzw.
`spring-boot-morphium/pom.xml` bleiben, nicht in `morphium-parent` wandern.

---

## Varianten der Reactor-Einbindung

### Variante A — Alle Module unbedingt im Default-Reactor

```xml
<modules>
  <module>morphium-core</module>
  <module>poppydb</module>
  <module>morphium-jakarta-data</module>
  <module>quarkus-morphium</module>
  <module>spring-boot-morphium</module>
</modules>
```

**Dafür:** Einfachste Konfiguration; `mvn install` baut alles; CI-Konfiguration
trivial; kein Risiko, dass ein Modul versehentlich nie gebaut wird.

**Dagegen:** Jeder Contributor, der eine Zeile im Kern ändert, lädt Quarkus- und
Spring-Boot-Abhängigkeiten (grob 600 MB im lokalen Repository) und braucht für
`quarkus-morphium/integration-tests` **Docker** (Testcontainers). Die
Einstiegshürde für Kern-Beiträge steigt deutlich. Für ein Projekt mit einem
Hauptmaintainer ist das ein echtes Argument — und es ist genau der Punkt, an dem
ein PR abgelehnt werden könnte.

### Variante B — Profil `extensions`, per Default aktiv (Empfehlung)

```xml
<modules>
  <module>morphium-core</module>
  <module>poppydb</module>
</modules>

<profiles>
  <profile>
    <id>extensions</id>
    <activation>
      <property>
        <name>!skipExtensions</name>
      </property>
    </activation>
    <modules>
      <module>morphium-jakarta-data</module>
      <module>quarkus-morphium</module>
      <module>spring-boot-morphium</module>
    </modules>
  </profile>
</profiles>
```

Verhalten:
- `mvn install` → baut alles (Profil ist aktiv, weil `skipExtensions` nicht gesetzt ist)
- `mvn install -DskipExtensions` → nur Kern und PoppyDB
- CI kann beides matrixen

**Dafür:** Default ist vollständig — niemand vergisst ein Modul, und der
Release-Prozess bekommt alles. Gleichzeitig hat jeder Kern-Entwickler eine
Ein-Flag-Fluchttür ohne Docker und ohne Quarkus-Download. Die
Negativ-Property-Aktivierung ist ein etabliertes Maven-Idiom und
werkzeugverträglich (IDEs zeigen das Profil als aktiv).

**Dagegen:** Etwas mehr Konfiguration; `<modules>` an zwei Stellen. Wer nur
`<modules>` liest, übersieht möglicherweise die Profil-Module — daher gehört
ein Kommentarblock an beide Stellen.

### Variante C — Profil, per Default inaktiv

```
mvn install                     → nur Kern + PoppyDB
mvn install -Pextensions        → alles
```

**Dafür:** Maximale Entlastung des Kern-Builds; die Erweiterungen sind auch
konfigurativ „optional".

**Dagegen:** Fehleranfällig in der Praxis. Ein `release.sh`-Lauf ohne `-P` würde
die Erweiterungen stillschweigend übergehen und ein unvollständiges
Central-Bundle erzeugen — ein Fehler, der erst nach dem Upload auffällt. Ebenso
CI, Dependabot-Builds und lokale Vollverifikation. „Per Default nicht gebaut"
heißt in der Praxis „gelegentlich nicht gebaut".

### Variante D — Getrennter Aggregator (`pom-all.xml`)

Verworfen. Zwei Reactor-Wurzeln verwirren Werkzeuge, IDEs und
`maven-release-plugin`; die Version müsste an zwei Stellen gepflegt werden.

---

## Empfehlung

**Variante B.** Default vollständig, Opt-out über `-DskipExtensions`.

Sie erfüllt beide Anforderungen gleichzeitig: Der Kern bleibt unbelastet und
eigenständig baubar (das ist der Kern des Auftrags „optionale Module"), und der
Release-Pfad kann nicht versehentlich Module überspringen.

### Begleitmaßnahmen

1. **Kommentarblock in `morphium-parent`**, der die Invarianten I1–I5 und die
   Bedeutung von `skipExtensions` erklärt. Der nächste Contributor soll die
   Regel aus der POM lernen, nicht aus einem Dokument.
2. **Fremd-BOMs bleiben in den Modul-POMs** (I4).
3. **`integration-tests` der Quarkus-Extension mit eigener Aktivierungsbedingung**
   — sinnvollerweise `-DskipITs`-fähig oder auf `docker`-Verfügbarkeit
   konditioniert, damit ein Vollbau ohne Docker nicht scheitert. Konkrete
   Ausarbeitung in M3.
4. **CI-Matrix in M6:** ein Job `-DskipExtensions` (beweist I3), ein Job
   vollständig (beweist Integration).
5. **Automatisierter Invariantentest.** In M6 ein Skript
   `scripts/verify-core-isolation.sh`, das I1–I3 prüft und in CI läuft. Damit
   kann die Optionalität nicht unbemerkt erodieren — das ist die eigentliche
   Absicherung des „ZWINGEND" aus dem Auftrag.

---

## JF-Entscheidungsvorlage (eine Folie)

> **Klarstellung:** „Optionales Modul" heißt nicht `<optional>true</optional>`.
> Es heißt: eigenes Artefakt, Abhängigkeit ausschließlich Modul → Kern, niemals
> umgekehrt, und der Kern bleibt ohne die Module baubar.
>
> **Frage:** Werden die Erweiterungen bei jedem `mvn install` mitgebaut?
>
> **Empfehlung:** Ja, per Default — abschaltbar mit `-DskipExtensions`. Damit
> lädt ein Kern-Contributor bei Bedarf kein Quarkus/Spring und braucht kein
> Docker, während der Release-Prozess nie versehentlich ein Modul übergeht.
>
> **Absicherung:** CI-Job, der beweist, dass der Kern-Dependency-Tree frei von
> Jakarta Data, Quarkus und Spring ist. Damit ist die Optionalität dauerhaft
> überprüft und nicht nur zugesagt.
