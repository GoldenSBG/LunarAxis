# LunarAxis

**Eine modulare Java-Spielbibliothek zur Entwicklung von 2D-Spielen mit TigerJython.**

LunarAxis wurde im Rahmen einer Maturaarbeit an der Kantonsschule Glarus (2026/2027) entwickelt. Die Bibliothek stellt grundlegende und erweiterte Funktionen für die Entwicklung von 2D-Spielen bereit und wurde als Alternative zur bestehenden Spielbibliothek JGameGrid (GameGrid) konzipiert.

LunarAxis ist in **Java** programmiert. Die eigentlichen Spiele werden hingegen direkt in **TigerJython (Python/Jython)** entwickelt. Dafür sind keine eigenen Java-Kenntnisse erforderlich.

## Funktionen

LunarAxis stellt unter anderem folgende Funktionen bereit:

- **Spielschleife:** Automatische Aktualisierung und Darstellung mit festem Zeitschritt (Fixed Timestep).
- **Grafik:** Zeichnen von Formen, Bildern und Texten sowie Sprite-Animationen.
- **Eingaben:** Verarbeitung von Tastatur- und Mauseingaben, einschliesslich einzelner Tastendrücke.
- **Spielobjekte:** Actors mit Position, Grösse, Animationen und individuell einstellbaren Hitboxen.
- **Physik und Kollision:** Bewegung, Schwerkraft, Kollisionserkennung und Kollisionsauflösung.
- **Kamera und Viewport:** 2D-Kamera mit Bewegung, Zoom und Rotation sowie proportional skalierbare Spielfenster.
- **Szenenverwaltung:** Verwaltung von Spielzuständen und Szenenwechsel mit Überblendungen.
- **Levelsystem:** Raster, TileMaps, Laden von Leveldateien und Triggerbereiche.
- **Audio:** Soundeffekte, Hintergrundmusik und positionsabhängige Audiowiedergabe.
- **Benutzeroberfläche:** Buttons, Texte, Schieberegler, Eingabefelder und weitere UI-Elemente.
- **Partikelsystem:** Partikeleffekte mit Wiederverwendung von Objekten.
- **Hilfsfunktionen:** EventBus, Scheduler, Zufallszahlen, Ressourcenverwaltung und Speichern von Spielständen.
- **Diagnose:** Validierung von Eingaben, strukturierte Fehlermeldungen, Logging und Debugging-Hilfen.

LunarAxis übernimmt die wiederkehrenden technischen Aufgaben eines Spiels. Die eigentlichen Spielregeln, beispielsweise die Punkteberechnung, das Verhalten von Gegnern oder der Levelaufstieg, werden weiterhin im TigerJython-Skript festgelegt.

## Voraussetzungen

Für die Verwendung werden benötigt:

- Desktop-TigerJython mit Jython-Unterstützung
- Eine kompatible Java-Laufzeitumgebung (Java 11 oder neuer)
- Die LunarAxis-JAR-Datei

LunarAxis verwendet **Java Swing und Java2D** zur grafischen Darstellung und ist für die Desktop-Version von TigerJython vorgesehen.

Die browserbasierte Umgebung WebTigerJython wird nicht unterstützt.

## Installation

1. Lade die fertige LunarAxis-JAR-Datei aus dem Ordner [`jars/`](jars/) herunter oder erstelle sie mithilfe von Maven aus dem Quellcode.
2. Schliesse Desktop-TigerJython (falls geöffnet)
3. Öffne den `Lib`-Ordner deiner Desktop-TigerJython-Installation (meist C:\Program Files\TJGroup\TigerJython-Win64_V2\bin\Lib)
3. Kopiere die LunarAxis-JAR-Datei in diesen Ordner.
4. Starte TigerJython neu.

Bei der untersuchten Windows-Installation befindet sich der relevante `Lib`-Ordner neben der Datei `tigerjython2.jar`.

Überprüfe die Installation mit folgendem Python-Code:

```python
from LunarAxis.util import Version

print(Version.VERSION)
```

Wenn die Versionsnummer ausgegeben wird, konnte TigerJython die Bibliothek laden.

Weitere Hinweise zur Installation und zu Ressourcenpfaden findest du in der Benutzerdokumentation.

## Erste Schritte

Das folgende Beispiel erstellt ein Spielfenster mit einem einfachen Hintergrund und einer Textausgabe.

```python
from LunarAxis.core import LunarEngine
from LunarAxis.core.manager import IGameLogic
from LunarAxis.graphics import Colors


class MeinSpiel(IGameLogic):
    def init(self, engine):
        self.engine = engine

    def render(self, g):
        g.setColor(Colors("#142336"))
        g.fillRect(0, 0, 640, 400)

        g.setColor(Colors.WHITE)
        g.drawString("Hallo LunarAxis!", 30, 50)


engine = LunarEngine(640, 400, "Mein erstes LunarAxis-Spiel")
engine.setGameLogic(MeinSpiel())
engine.start()
```

Das Beispiel verwendet drei grundlegende Komponenten:

- `LunarEngine` erstellt das Spielfenster und verwaltet die Spielschleife.
- `IGameLogic` verbindet den Python-Code mit der Bibliothek.
- `Graphics` stellt Zeichenfunktionen für Formen, Bilder und Texte bereit.

Ein Spiel kann zusätzlich die Methode `update(deltaTime)` implementieren, um beispielsweise Spielobjekte zu bewegen oder Eingaben zu verarbeiten.

## Beispielspiele

Zur Demonstration der Bibliothek wurden drei Beispielspiele entwickelt:

| Spiel | Beschreibung |
|---|---|
| **Dodge Arena** | Ein Ausweichspiel, bei dem Hindernissen ausgewichen und Energiekerne eingesammelt werden. Demonstriert unter anderem Eingaben, Grafik und Kollisionserkennung. |
| **Lunar Tetris** | Eine Tetris-Umsetzung mit Spielfeld, fallenden Spielsteinen, Punkteberechnung und Levelsystem. |
| **Lunar Depths – Echo of the Abyss** | Ein 2D-Action-Abenteuer aus der Vogelperspektive mit Gegnern, mehreren Leveln, Animationen, Kamera, Partikeln, Audio und Spielstandspeicherung. |

Für Lunar Depths existiert zusätzlich eine erweiterte Version mit weiteren Leveln, Gegnertypen und einem Endgegner.

## Dokumentation

Für LunarAxis stehen zwei Arten der Dokumentation zur Verfügung:

**Benutzerdokumentation für TigerJython**

Eine schrittweise aufgebaute Lernanleitung mit Installationshinweisen, vollständigen Beispielprogrammen, Übungsaufgaben, Spielprojekten und einem API-Nachschlagewerk.

**JavaDoc**

Technische Dokumentation der Java-Klassen, Methoden und Schnittstellen für die genauere Untersuchung der Bibliothek.

Die Dokumentationen sind als ergänzende Materialien zum Projekt vorgesehen.

## Technische Grundlagen

| Bereich | Technologie |
|---|---|
| Programmiersprache | Java |
| Java-Zielversion | Java 11 |
| Grafikdarstellung | Java2D / Swing |
| Build-System | Apache Maven |
| Entwicklungsumgebung für Spiele | Desktop-TigerJython |
| Skriptsprache | Python über Jython |
| Bereitstellung | JAR-Datei |

## Projekt und Hintergrund

LunarAxis ist im Rahmen der Maturaarbeit **«Programmierung einer Spielbibliothek für pädagogische Zwecke»** entstanden.

Ziel des Projekts ist die Entwicklung einer modularen Spielbibliothek, die in TigerJython eingesetzt werden kann und gegenüber JGameGrid zusätzliche technische Möglichkeiten für die 2D-Spieleentwicklung bereitstellt.

Neben der Bibliothek umfasst die Maturaarbeit einen technischen Vergleich mit JGameGrid sowie praktische Beispielspiele.

## Lizenz

Dieses Projekt steht unter der **MIT-Lizenz**.

Die Bedingungen zur Verwendung, Veränderung und Weitergabe sind in der Datei [LICENSE](LICENSE) festgehalten.

## Autor

**Patrick André Salvador**

Maturaarbeit 2026/2027  
Kantonsschule Glarus, Schweiz

**Betreuungsperson:** Timon Ruther  
**Referent:** Beat Temperli