--- 
# Prompt:

```markdown
# LunarAxis – Beispielspiel 2: Lunar Tetris – Neon Blocks

Du bist Senior Game Developer und Experte für Java 11, TigerJython und LunarAxis.

## Ausgangslage

Für die Entwicklung erhältst du die aktuelle LunarAxis-Bibliothek als ZIP-Datei. Diese enthält den Java-Quellcode, die Ressourcen sowie gegebenenfalls vorhandene Beispiele und Dokumentationen.

Untersuche ausschliesslich die bereitgestellte ZIP-Datei und die darin tatsächlich vorhandenen Klassen, Methoden und Ressourcen.

## Ziel

Entwickle das zweite von drei offiziellen LunarAxis-Beispielspielen für meine Maturaarbeit.

**Name:** Lunar Tetris – Neon Blocks

Ein vollständiges, spielbares Tetris mit hochwertiger Neon-Optik. Es soll deutlich mehr Funktionen demonstrieren als Dodge Arena, aber weiterhin möglichst kompakt und verständlich bleiben.

## 1. Bestehende API untersuchen

Lies die aktuellen Implementierungen von `LunarEngine`, `IGameLogic`, `InputManager`, `Key`, `Graphics`, `Colors`, `ResourceManager`, `Grid`, `TileMap`, `Scheduler` und relevanten Hilfsklassen aus der bereitgestellten ZIP-Datei.

Prüfe, welche Klassen tatsächlich für Tetris geeignet sind. Verwende vorhandene Funktionen sinnvoll, aber erzwinge keine ungeeigneten Abstraktionen.

Falls der frühere Tetris-Testcode in der ZIP-Datei vorhanden ist, untersuche ihn als Referenz.

Keine erfundenen APIs oder unbestätigten Annahmen.

## 2. Gameplay

Implementiere ein vollständiges Tetris mit:

- Spielfeld mit 10 Spalten und 20 sichtbaren Reihen
- Allen sieben Tetromino-Formen (I, O, T, S, Z, J, L)
- Zufallsauswahl über ein 7-Bag-System
- Links-/Rechtsbewegung und automatischem Fallen
- Soft Drop und Hard Drop
- Rotation mit korrekt definierten Wand- und Boden-Kicks, möglichst nach Super Rotation System (SRS)
- Ghost-Piece zur Anzeige der Landeposition
- Hold-Funktion, maximal einmal pro aktivem Tetromino
- Vorschau der nächsten Tetrominos
- Vollständiger Linienerkennung und -entfernung
- Punktewertung für Single, Double, Triple und Tetris
- Steigendem Level und zunehmender Fallgeschwindigkeit
- Game Over, Neustart und Pause
- Klarer Steuerungsübersicht

Beachte Eingabewiederholung, schnelle Richtungswechsel, Rotationen nahe Wänden, Spawn-Kollisionen und das korrekte Verhalten nach einem Hard Drop.

Wichtig: Die Spiellogik liegt vollständig im TigerJython-Code. LunarAxis stellt nur die allgemeinen technischen Funktionen bereit.

## 3. Optik und Assets

Orientiere dich an folgenden Aspekten: dunkler Hintergrund, Neonfarben, hochwertige Blöcke, dezente Leuchteffekte und klar strukturiertes HUD.

Alle Grafiken werden in der JAR gespeichert unter:

`src/main/resources/games/lunar-tetris/`

Erstelle die Assets nicht direkt mit einem Bildgenerator.

Liefere stattdessen ein vollständiges ausführbares Python-3-Skript mit Pillow, das sämtliche benötigten PNGs automatisch erzeugt.

Bevorzuge ein gemeinsames Block-Spritesheet oder andere kompakte, effizient verwendbare Assets. Prüfe vorher die tatsächliche `SpriteSheet`- und `Graphics`-API anhand der bereitgestellten ZIP-Datei.

Die Grafik soll nicht unnötig viel zusätzlichen TigerJython-Code erfordern.

## 4. Verbindliche TigerJython-Regeln

- Genau eine vollständige `.py`-Datei
- Alle Spielklassen in dieser Datei
- Kein Aufteilen in mehrere Python-Module
- TigerJython-/Jython-kompatible Syntax
- Nur die vorhandenen LunarAxis-APIs für Engine, Rendering, Input und Ressourcen
- Keine direkten Java-, AWT-, Swing- oder JavaFX-Imports
- Keine externen Python-Pakete im TigerJython-Spiel
- Keine Änderungen an der LunarAxis-Bibliothek

Zielumfang: ungefähr 300–500 Zeilen, sofern Tetris dadurch vollständig und korrekt bleibt. Korrektes Gameplay hat Vorrang vor der Zeilenzahl.

## 5. Ausgabe

Liefere in dieser Reihenfolge:

1. Kurze Beschreibung und Übersicht der verwendeten LunarAxis-Klassen
2. Den vollständigen endgültigen TigerJython-Spielcode in genau einem kopierbaren Codeblock
3. Den vollständigen Python-3-Asset-Generator
4. Installationsbefehle für notwendige Generator-Abhängigkeiten
5. Steuerung und Beschreibung der Spielmechaniken
6. Testfälle für sämtliche wesentlichen Funktionen und Randfälle

Prüfe alle Methodensignaturen gegen den tatsächlichen LunarAxis-Code aus der ZIP-Datei.

Prüfe insbesondere die Tetris-Logik für Rotation, Kicks, Hold, Linienentfernung, Ghost-Piece, Punktestand, Levelwechsel und Neustart.

Falls ein Problem erkannt wird, gib direkt den vollständigen korrigierten Endcode aus. Keine nachträglichen Patches, Ersatzblöcke oder Anweisungen, einzelne Zeilen selbst zusammenzusetzen.

## Arbeitsgrenzen

- Die bereitgestellte ZIP-Datei ausschliesslich lesen und analysieren
- Keine Änderungen an der LunarAxis-Bibliothek
- Sämtlichen Code ausschliesslich im Chat liefern
- Keine Platzhalter oder Pseudocode
- Keine erfundenen APIs
- Keine unnötig langen Codeblöcke oder künstlichen Zeilenumbrüche
- Keine zusätzlichen Spielsysteme, die für dieses Beispiel nicht benötigt werden
- Keine neuen Spielmechaniken innerhalb der Java-Bibliothek

**Wichtigstes Ziel:** Ein hochwertiges, vollständiges und tatsächlich spielbares Tetris, das die erweiterten Möglichkeiten von LunarAxis demonstriert und sich als zweites offizielles Beispiel für die Maturaarbeit eignet. Das gesamte Spiel soll aus einer einzigen TigerJython-Datei und den in der JAR eingebetteten Ressourcen bestehen.
```