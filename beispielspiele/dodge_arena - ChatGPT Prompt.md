--- 
# Prompt:

```markdown
# LunarAxis – Beispielspiel 1: Dodge Arena – Survive the Storm

Du bist Senior Game Developer und Experte für LunarAxis, Java 11 und TigerJython.

## Ausgangslage

Für die Entwicklung erhältst du die aktuelle LunarAxis-Bibliothek als ZIP-Datei. Diese enthält den Java-Quellcode, die Ressourcen sowie gegebenenfalls vorhandene Beispiele und Dokumentationen.

Untersuche ausschliesslich die bereitgestellte ZIP-Datei und die darin tatsächlich vorhandenen Klassen, Methoden und Ressourcen.

## Ziel

Entwickle das erste von drei offiziellen LunarAxis-Beispielspielen für meine Maturaarbeit.

**Name:** Dodge Arena – Survive the Storm

Ein kleines, vollständiges und optisch hochwertiges 2D-Neon-Arcade-Spiel. Der Spieler bewegt sich durch eine Arena, weicht herannahenden Hindernissen aus, sammelt Energiekerne für Punkte und versucht, möglichst lange zu überleben. Die Schwierigkeit steigt mit der Zeit.

Das Spiel soll für Einsteiger verständlich und mit möglichst wenig TigerJython-Code umsetzbar sein.

Gleichzeitig dient es als Demonstrationsspiel, das zeigt, wie sich mit LunarAxis ein vollständiges und visuell ansprechendes Spiel entwickeln lässt, ohne dass die Java-Bibliothek selbst Spiellogik enthalten muss.

## 1. Zuerst die LunarAxis-API prüfen

Untersuche die tatsächlich vorhandenen Klassen und Methoden in der bereitgestellten ZIP-Datei, insbesondere:

- `LunarEngine`, `Actor`, `InputManager`
- `Graphics`, `Colors`, `Collision`
- `ResourceManager` und vorhandene Sprite-APIs
- Gegebenenfalls `Scheduler`, `LunarRandom` und weitere notwendige Hilfsklassen

Prüfe auch vorhandene Beispiele und den aktuellen Ressourcenladeweg.

Verwende ausschliesslich existierende LunarAxis-APIs. Keine erfundenen Methoden, Konstruktoren oder Parameter.

## 2. Spiel implementieren

Das Spiel benötigt:

- Startbildschirm mit Steuerungshinweisen
- Flüssige Spielerbewegung
- Unterschiedliche Hindernisse mit nachvollziehbaren Bewegungsmustern
- Sammelbare Energiekerne für zusätzliche Punkte
- Kollision und Game Over
- Punkte und Überlebenszeit
- Kontinuierlich steigende Schwierigkeit
- Neustart ohne Programmneustart
- Verständliches HUD mit Punkten, Überlebenszeit und wichtigen Spielinformationen
- Visuelles Feedback für Bewegung, Kollisionen, eingesammelte Energiekerne und Game Over

Die Spiellogik gehört ausschliesslich in den TigerJython-Code, nicht in die Bibliothek.

### Verbindliche TigerJython-Regeln

- Genau eine vollständige `.py`-Datei
- Alle benötigten Python-Klassen in dieser einen Datei
- Keine weiteren Python-Module oder `.py`-Dateien
- Ausschliesslich LunarAxis-Imports für die Spielentwicklung
- Keine direkten Java-, Swing-, AWT- oder Jython-Interop-Imports
- TigerJython-kompatible Syntax, keine modernen CPython-spezifischen Features
- Kein Szenensystem für dieses erste Spiel
- Zielumfang ungefähr 100–200 Zeilen, sofern die vollständige Spielfunktion dadurch nicht leidet

## 3. Visuelle Gestaltung – Neon Arcade

Das Spiel soll optisch deutlich hochwertiger wirken als eine einfache Darstellung mit farbigen Rechtecken und Kreisen.

Orientiere dich am bereitgestellten Neon-Arcade-Referenzbild.

**Visuelle Richtung:**

- Dunkler Hintergrund mit futuristischer Atmosphäre
- Leuchtende geometrische Formen und Neonfarben
- Hochwertige, klar erkennbare Arena
- Visuell unterscheidbare Hindernisse
- Goldene, leuchtende Energiekerne
- Gut erkennbare Spielerfigur
- Modernes, übersichtliches HUD
- Passende visuelle Effekte und Animationen

Verwende vorzugsweise PNG-Sprites statt umfangreicher zusätzlicher Zeichenlogik.

Die visuelle Komplexität soll hauptsächlich durch hochwertige generierte Assets entstehen, während der TigerJython-Code möglichst kompakt und verständlich bleibt.

Die Grafiken müssen bei ihrer tatsächlichen Darstellung im Spiel sauber skaliert werden. Achte insbesondere auf transparente Hintergründe, korrekte Dimensionen, geeignete Spritesheet-Aufteilungen und eine konsistente visuelle Gestaltung.

## 4. Assets direkt in die JAR

Falls das Spiel Grafiken oder Sounds benötigt, müssen diese in der LunarAxis-JAR liegen.

Verwende dafür:

`src/main/resources/games/dodge-arena/`

Erstelle die Assets nicht selbst über Bildgenerierungstools.

Gib stattdessen ein vollständiges, auf meinem PC ausführbares **Python-3-Skript** aus, das alle benötigten PNG- beziehungsweise WAV-Dateien automatisch erzeugt.

Das Skript soll:

- Alle Dateien mit korrekten Dimensionen und Dateinamen generieren
- Notwendige Ordner selbst anlegen
- Transparente Hintergründe korrekt behandeln
- Visuell konsistente Neon-Arcade-Grafiken erzeugen
- Nur ausdrücklich genannte, frei installierbare Python-Abhängigkeiten verwenden
- Bei Spritesheets die Frame-Anordnung dokumentieren
- Die erzeugten Dateien direkt in der vorgesehenen Ressourcenstruktur ablegen

Der Generator ist ein Entwicklungswerkzeug und gehört nicht zum TigerJython-Spielcode.

Die generierten Assets werden in die JAR eingebunden und zur Laufzeit über den Classpath geladen.

Prüfe dafür die tatsächlich vorhandenen `ResourceManager`-, `Graphics`- und Sprite-APIs anhand des bereitgestellten Java-Quellcodes.

Falls sich bestimmte Effekte mit den bestehenden Zeichenfunktionen einfach und überzeugend darstellen lassen, dürfen diese ohne zusätzliche Assets umgesetzt werden.

## 5. Ergebnis

Liefere in dieser Reihenfolge:

1. Kurze Beschreibung des Spiels, seiner Mechaniken und der verwendeten LunarAxis-Klassen
2. Vollständigen endgültigen TigerJython-Code als einen einzigen, direkt kopierbaren Codeblock
3. Vollständigen Python-3-Asset-Generator
4. Benötigte Python-Abhängigkeiten und Installationsbefehle für den Asset-Generator
5. Steuerung und Spielablauf
6. Konkrete Testfälle für Start, Bewegung, Kollision, Energiekerne, Punkte, steigende Schwierigkeit und Neustart

Prüfe den Code statisch gegen die tatsächlich vorhandenen Methodensignaturen und TigerJython-Einschränkungen.

Falls eine wichtige LunarAxis-Funktion fehlt oder fehlerhaft ist, dokumentiere sie gesondert. Erfinde keinen Workaround mit direkten Java-Imports.

## Arbeitsgrenzen

- Die bereitgestellte ZIP-Datei ausschliesslich lesen und analysieren
- Keine Änderungen an der LunarAxis-Bibliothek
- Sämtlichen Code ausschliesslich im Chat liefern
- Keine Platzhalter oder Pseudocode
- Keine erfundenen APIs
- Keine unnötig langen Codeblöcke oder künstlichen Zeilenumbrüche
- Keine zusätzlichen Spielsysteme, die für dieses Beispiel nicht benötigt werden
- Kein Szenensystem
- Keine neuen Spielmechaniken innerhalb der Java-Bibliothek

**Wichtigstes Ziel:** Ein tatsächlich spielbares, verständliches und vollständiges erstes Beispielspiel, das sowohl die einfache Verwendung von LunarAxis als auch die Möglichkeit einer hochwertigen visuellen Umsetzung demonstriert. Das gesamte Spiel soll aus einer einzigen TigerJython-Datei und den in der JAR eingebetteten Ressourcen bestehen.
```