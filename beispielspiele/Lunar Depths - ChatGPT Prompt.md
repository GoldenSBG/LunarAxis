--- 
# Prompt (V1)

```markdown
# LunarAxis – Beispielspiel 3: Lunar Depths – Echoes of the Abyss

Du bist Senior Game Developer, Java-11-Architekt, Pixel-Art-Technical-Artist und TigerJython-Experte.

## Ausgangslage

Für die Entwicklung erhältst du die aktuelle LunarAxis-Bibliothek als ZIP-Datei. Diese enthält den Java-Quellcode, die Ressourcen sowie gegebenenfalls vorhandene Beispiele und Dokumentationen.

Untersuche ausschliesslich die bereitgestellte ZIP-Datei und die darin tatsächlich vorhandenen Klassen, Methoden und Ressourcen.

## Ziel

Entwickle das dritte und anspruchsvollste offizielle LunarAxis-Beispielspiel für meine Maturaarbeit.

**Name:** Lunar Depths – Echoes of the Abyss

Ein vollständiges, visuell hochwertiges 2D-Top-down-Action-Adventure mit mehreren Levels, animierten Spielfiguren, Gegnern, Kämpfen, Audio, Partikeln, Menüs, Speicherständen, konfigurierbarer Steuerung und Debug-Werkzeugen.

Dieses Spiel soll möglichst viele tatsächlich vorhandene LunarAxis-Systeme sinnvoll miteinander kombinieren und die Leistungsfähigkeit der Bibliothek demonstrieren.

Anders als Dodge Arena und Lunar Tetris soll sich dieses Beispiel wie ein kleines, veröffentlichungsfähiges Spiel anfühlen, nicht wie eine technische Demo.

Gleichzeitig soll anhand des TigerJython-Quellcodes nachvollziehbar sein, wie ein grösseres Spiel mit den vorhandenen LunarAxis-Klassen aufgebaut werden kann.

## 1. Zuerst die LunarAxis-API prüfen

Untersuche die tatsächlich vorhandenen Klassen, Methoden und Schnittstellen in der bereitgestellten ZIP-Datei, insbesondere:

- `LunarEngine`, `IGameLogic`, `GamePanel`
- `SceneManager`, `Scene`, `BaseScene`
- `Actor`, `Animation`, `SpriteSheet`, `Camera2D`
- `Graphics`, `Colors`, `ResourceManager`
- `Level`, `LevelLoader`, `TileMap`, `Grid`, `LevelTrigger`
- `Collision`, `RigidBody2D`, `Gravity`
- `InputManager`, `Key`
- `ParticleSystem`, `ParticleEmitter`, `ParticleBuilder`, `ParticleRenderer`
- `AudioManager`, `Music`, `SoundClip`, `SoundPool`
- `UIManager` und relevante UI-Elemente
- `SaveManager`, `Scheduler`, `LunarRandom`
- Debug-Systeme und vorhandene Logging-APIs

Prüfe auch das Zusammenspiel der Systeme, ihre tatsächlichen Möglichkeiten und technische Einschränkungen.

Verwende ausschliesslich existierende LunarAxis-APIs. Keine erfundenen Methoden, Konstruktoren oder Klassen.

Nicht jede vorhandene Klasse muss zwingend verwendet werden. Nutze jedes System dort, wo es tatsächlich sinnvoll ist, und erzwinge keine ungeeigneten Abstraktionen.

## 2. Spielkonzept und Gameplay

Entwickle ein konkretes 2D-Top-down-Action-Adventure mit Dungeon-Erkundung.

Das Spiel benötigt:

- Flüssige Spielerbewegung in vier Richtungen
- Animierten Spieler mit Idle-, Walk-, Attack-, Hit- und Death-Animationen
- Mindestens zwei unterschiedliche Gegnertypen mit eigenem Verhalten
- Angriffssystem mit Schaden, Gesundheit und Trefferreaktionen
- Kurze Unverwundbarkeit nach erhaltenem Schaden
- Sammelbare Objekte und nachvollziehbare Fortschrittsmechanik
- Mindestens drei unterschiedliche Levels mit steigendem Schwierigkeitsgrad
- Sinnvolles Spielziel mit Sieg- und Niederlagezustand
- Kamera, die dem Spieler folgt und Levelgrenzen berücksichtigt
- Einfache Minimap oder eine andere übersichtliche Levelnavigation
- Healthbar und verständliches HUD
- Passende Animationen, Soundeffekte und Partikeleffekte

Die Gegner sollen sich nachvollziehbar verhalten und spielerisch unterschiedliche Herausforderungen darstellen.

Achte darauf, dass Bewegung, Animationen, Angriff, Schaden und Kollisionen korrekt aufeinander abgestimmt sind.

Die Spiellogik gehört ausschliesslich in den TigerJython-Code. LunarAxis stellt nur die allgemeinen technischen Funktionen bereit.

## 3. Menüs und Spielzustände

Das Spiel benötigt folgende Benutzeroberflächen:

**Hauptmenü:**

- Neues Spiel starten
- Gespeicherten Spielstand fortsetzen
- Einstellungen öffnen
- Spiel beenden

**Pause-Menü:**

- Spiel fortsetzen
- Neustart
- Einstellungen
- Zum Hauptmenü zurückkehren

**Einstellungen:**

- Musiklautstärke
- Soundeffektlautstärke
- Individuell konfigurierbare Steuerung

**Weitere Bildschirme:**

- Game Over
- Victory

Menüs müssen verständlich gestaltet und mit der Tastatur bedienbar sein.

Die Keybind-Konfiguration muss tatsächlich funktionieren: Der Spieler wählt eine Aktion, drückt eine neue Taste, mögliche Konflikte werden behandelt und die neue Belegung wird gespeichert.

Nutze das vorhandene Szenen- und UI-System von LunarAxis, sofern die tatsächlichen APIs dafür geeignet sind.

## 4. Levels und Levelsystem

Entwickle mindestens drei unterschiedliche Levels mit steigendem Schwierigkeitsgrad.

Jedes Level benötigt:

- Eigenständiges Layout
- Visuell unterscheidbare Umgebung
- Wände und begehbare Flächen
- Spieler-Startposition
- Sinnvolle Gegnerplatzierung
- Sammelobjekte und Fortschrittsziele
- Levelausgang beziehungsweise Übergang
- Korrekte Kollisionen und Trigger

Die Level sollen sich hinsichtlich Aufbau, Gegnern und Herausforderungen unterscheiden.

Verwende farbcodierte PNG-Dateien.

Trenne visuelle Tiles, Kollisionsinformationen und Triggerdaten sauber voneinander.

Programmiere nicht die vollständigen Levelkarten manuell in den TigerJython-Spielcode.

## 5. Speicher- und Fortschrittssystem

Verwende den vorhandenen LunarAxis-`SaveManager`, um den Spielfortschritt dauerhaft zu speichern.

Zu speichern sind:

- Highscore beziehungsweise Bestleistung
- Freigeschaltete Levels
- Relevanter Spielfortschritt
- Musik- und Effektlautstärke
- Individuelle Keybinds

Beim nächsten Programmstart müssen die gespeicherten Werte wieder geladen werden.

Berücksichtige fehlende, beschädigte oder veraltete Speicherdateien.

Speicherstände gehören in ein geeignetes beschreibbares Benutzerverzeichnis ausserhalb der JAR. Die eingebetteten Spielressourcen dürfen nicht verändert werden.

Verwende die vorhandenen LunarAxis-Funktionen statt einer selbstgeschriebenen Speicherverwaltung.

## 6. Debugging

Das Spiel benötigt einen per Taste umschaltbaren Debug-Modus.

Zeige darin, soweit von den vorhandenen APIs unterstützt:

- FPS und relevante Laufzeitinformationen
- Spielerposition
- Actor- und Kollisions-Hitboxen
- Kamera- und Positionsinformationen
- Weitere sinnvolle Debug-Daten

Der Debug-Modus darf die eigentlichen Spielregeln nicht verändern.

## 7. Visuelle Gestaltung und Animationen

Das Spiel soll eine stilistisch einheitliche, hochwertige Pixel-Art-Gestaltung erhalten.

Erforderlich sind:

- Animiertes Spieler-Spritesheet
- Gegner-Spritesheets mit eigenen Animationen
- Passende Tilesets für Umgebungen und Hindernisse
- Sammelobjekte und interaktive Gegenstände
- Menü- und HUD-Grafiken
- Angriffs- und Partikeleffekte

Die Figuren müssen über alle Animationsframes hinweg konsistente Proportionen besitzen.

Dokumentiere Framegrössen, Animationsreihenfolgen und Spritesheet-Aufteilungen exakt.

Achte darauf, dass die Animationen tatsächlich zum jeweiligen Spielzustand passen.

## 8. Assets direkt in die JAR

Alle benötigten Ressourcen werden in der LunarAxis-JAR gespeichert unter:

`src/main/resources/games/lunar-depths/`

Verwende eine saubere Unterstruktur für Sprites, Tilesets, Levels, Audio und gegebenenfalls weitere Ressourcen.

Erstelle die Assets nicht direkt über Bildgenerierungstools.

Gib stattdessen einen vollständigen, auf meinem PC ausführbaren **Python-3-Asset-Generator** aus, der sämtliche benötigten PNG-Spritesheets, Tilesets, Leveldateien und Audiodateien automatisch erzeugt.

Das Skript soll:

- Alle benötigten Dateien reproduzierbar generieren
- Notwendige Ordner selbst anlegen
- Transparente Hintergründe korrekt behandeln
- Einheitliche Pixel-Art-Grafiken erzeugen
- Konsistente Spritesheet-Frames und Animationsreihenfolgen verwenden
- Levelmarker und ihre Bedeutungen dokumentieren
- Unterstützte Audiodateien erzeugen
- Nur ausdrücklich genannte, frei installierbare Python-Abhängigkeiten verwenden
- Sämtliche Ressourcen in der vorgesehenen Ordnerstruktur ablegen

Der Generator darf Pillow, Python-Standardbibliotheken und weitere klar benannte installierbare Abhängigkeiten verwenden.

Der Generator ist ein Entwicklungswerkzeug und gehört nicht zum TigerJython-Spielcode.

Die erzeugten Assets werden in die JAR eingebunden und zur Laufzeit über den Classpath geladen.

Prüfe die tatsächlichen `ResourceManager`-, `SpriteSheet`-, `Graphics`-, `LevelLoader`- und Audio-APIs anhand des bereitgestellten Java-Quellcodes.

## 9. Musik und Soundeffekte

Integriere Hintergrundmusik und mehrere unterschiedliche Soundeffekte.

Erforderlich sind insbesondere:

- Passende Hintergrundmusik
- Soundeffekte für Angriffe und Treffer
- Geräusche für Sammelobjekte und Spielereignisse
- Korrektes Starten und Stoppen der Musik bei Szenenwechseln
- Getrennte Lautstärkeregelung für Musik und Effekte

Prüfe die tatsächlich unterstützten Audioformate.

Vermeide überlagernde Musikstücke und Sounds, die nach dem Verlassen einer Szene unbeabsichtigt weiterlaufen.

Sämtliche Audiodateien müssen aus der JAR geladen werden können.

## 10. Verbindliche TigerJython-Regeln

- Genau eine vollständige `.py`-Datei
- Alle benötigten Python-Klassen innerhalb dieser einen Datei
- Keine weiteren Python-Spielmodule oder `.py`-Dateien
- Ausschliesslich vorhandene LunarAxis-APIs für Engine, Rendering, Eingaben, Ressourcen, Audio, Speicherung und sonstige allgemeine Spielsysteme
- Keine direkten Java-, AWT-, Swing- oder JavaFX-Imports
- Keine modernen CPython-spezifischen Features
- TigerJython-/Jython-kompatible Syntax
- Keine zusätzlichen Python-Pakete zur Laufzeit
- Keine Spielmechaniken innerhalb der LunarAxis-Java-Bibliothek
- Vorhandene LunarAxis-Systeme statt selbstgeschriebener Engine-Ersatzfunktionen verwenden
- Keine künstliche Zeilenbegrenzung, aber verständliche und wartbare Implementierung

Verteile die Verantwortlichkeiten sinnvoll auf mehrere Python-Klassen innerhalb der einen Datei, beispielsweise für Spielsteuerung, Spieler, Gegner, Levelablauf, UI und Einstellungen.

Stelle sicher, dass Szenenwechsel korrekt funktionieren und keine veralteten Gegnerzustände oder Ressourcen zurücklassen.

## 11. Ergebnis

Liefere in dieser Reihenfolge:

1. Übersicht der tatsächlich vorhandenen LunarAxis-APIs und relevanter technischer Einschränkungen
2. Vollständigen endgültigen TigerJython-Code
3. Vollständigen Python-3-Asset-/Level-/Audio-Generator
4. Benötigte Python-Abhängigkeiten und Installationsbefehle für den Generator
5. Steuerung, Menüs, Einstellungen und Speicherverhalten
6. Umfangreiche Testfälle für die wesentlichen Spielsysteme

Prüfe die Implementierung statisch gegen die tatsächlich vorhandenen LunarAxis-Methodensignaturen und die TigerJython-Einschränkungen.

Prüfe insbesondere:

- Bewegung und Kollisionen
- Gegner-KI und Angriffssystem
- Animationen und Levelwechsel
- Kamera und Minimap
- Audio und Partikel
- Menübedienung und Key-Rebinding
- Speichern und Laden
- Game Over, Victory und Neustart
- Debug-Funktionen und Ressourcenverwaltung

Falls der Codeumfang eine einzelne Antwort übersteigt, teile die Ausgabe in klar gekennzeichnete, aufeinander aufbauende Abschnitte auf.

Die Abschnitte müssen sich ohne Überschneidungen und ohne fehlende Zeilen zu einer einzigen vollständigen `.py`-Datei zusammensetzen lassen.

Keine Platzhalter, kein Pseudocode, keine ausgelassenen Methoden und keine widersprüchlichen Zwischenversionen.

## Arbeitsgrenzen

- Die bereitgestellte ZIP-Datei ausschliesslich lesen und analysieren
- Keine Änderungen an der LunarAxis-Bibliothek
- Sämtlichen Code ausschliesslich im Chat liefern
- Keine Platzhalter oder Pseudocode
- Keine erfundenen APIs
- Keine unnötigen Code-Duplizierungen
- Keine unnötig grossen Methoden oder künstlichen Zeilenumbrüche
- Keine zusätzlichen Spielsysteme, die für dieses Beispiel nicht benötigt werden
- Keine neuen Spielmechaniken innerhalb der Java-Bibliothek

**Wichtigstes Ziel:** Ein vollständiges, visuell hochwertiges und tatsächlich spielbares Action-Adventure, das zahlreiche LunarAxis-Systeme miteinander kombiniert. Das Spiel soll sowohl als Vorschau auf die Möglichkeiten der Bibliothek als auch als nachvollziehbares Beispiel für die Entwicklung komplexerer Spiele in TigerJython dienen.

```

---


# Prompt (V2)


```markdown
# LunarAxis – Beispielspiel 3: Lunar Depths – Enhanced Edition (V2)

Du bist Senior Game Developer, Java-11-/TigerJython-Experte, Game-UI/UX-Designer, Technical Artist und QA Engineer.

## Ausgangslage

Für die Entwicklung erhältst du die aktuelle LunarAxis-Bibliothek als ZIP-Datei. Diese enthält den Java-Quellcode, die Ressourcen sowie gegebenenfalls vorhandene Beispiele und Dokumentationen.

Untersuche ausschliesslich die bereitgestellte ZIP-Datei und die darin tatsächlich vorhandenen Klassen, Methoden und Ressourcen.

Zusätzlich erhältst du den bisherigen vollständigen TigerJython-Spielcode von **Lunar Depths V1** und den dazugehörigen Python-3-Asset-Generator.

V1 bildet die funktionale und technische Grundlage für die Weiterentwicklung. Bestehende funktionierende Systeme sollen erhalten bleiben und gezielt erweitert werden.

## Ziel

Entwickle **Lunar Depths V2 – Enhanced Edition** als umfangreiche Weiterentwicklung des dritten offiziellen LunarAxis-Beispielspiels.

Das Spiel soll sich wie ein hochwertiges, kleines 2D-Action-Adventure anfühlen, das tatsächlich veröffentlicht werden könnte.

Erweitere insbesondere:

- Levelumfang und Kampagnenfortschritt
- Spielerfähigkeiten und mausgerichtete Kampfsteuerung
- Gegner und Endboss
- Animationen und Partikeleffekte
- Grafisch gestaltete Menüs mit Mausbedienung
- HUD, Minimap und Bossanzeige
- Musik und Soundeffekte
- Eigene Schriftart
- Maximierbares Spielfenster
- Speichern, Einstellungen und Debugging

Das Spiel soll eine deutlich umfangreichere Nutzung der LunarAxis-Bibliothek demonstrieren als V1.

Die eigentliche Spiellogik bleibt vollständig in TigerJython. LunarAxis stellt nur die allgemeinen technischen Grundlagen bereit.

## 1. Bestehende Implementierung und LunarAxis-API untersuchen

Analysiere zunächst den vollständigen bisherigen V1-Spielcode, den Asset-Generator sowie die tatsächlich vorhandenen Java-Klassen aus der bereitgestellten ZIP-Datei.

Prüfe insbesondere:

- `LunarEngine`, `IGameLogic`, `GameWindow`, `GamePanel`
- `InputManager`, `Key`
- `SaveManager`, `Logger`, `Errors`, `LunarException`
- `ResourceManager`, `Graphics`, `Fonts`, `Colors`
- `Actor`, `Animation`, `SpriteSheet`, `Camera2D`
- `SceneManager`, `BaseScene`, `UIManager` und UI-Elemente
- `Grid`, `TileMap`, `LevelLoader`, `LevelTrigger`
- `Collision`, `ParticleSystem`, `ParticleBuilder`
- `AudioManager`, `Music`, `SoundClip`, `SoundPool`
- Vorhandene Debug-Funktionen und relevante Hilfsklassen

Untersuche die tatsächlichen Schnittstellen und das Zusammenspiel der Systeme.

Verwende ausschliesslich existierende LunarAxis-APIs. Keine erfundenen Methoden, Konstruktoren oder Klassen.

Erhalte funktionierende V1-Bestandteile und ersetze sie nicht ohne technischen Grund.

## 2. Kampagne mit sechs Levels

Erweitere die bestehende Kampagne von drei auf **sechs unterschiedliche Levels**.

1. **Versunkene Vorhalle:** Einführung, Grundmechaniken und erste Gegner
2. **Kristallzisterne:** Grössere Karte, Fernkämpfer und neue Herausforderungen
3. **Kern des Abgrunds:** Komplexere Wege und kombinierte Gegnergruppen
4. **Vergessene Katakomben:** Neue Gegnertypen und gefährlichere Passagen
5. **Schattenfestung:** Anspruchsvolle Kämpfe und Vorbereitung auf das Finale
6. **Thronsaal des Abgrunds:** Eigenständiges Endboss-Level

Jedes Level benötigt:

- Eigenständiges, durchdachtes Layout
- Unterscheidbare grafische Gestaltung
- Sinnvolle Gegnerplatzierung
- Echte Levelgrenzen und Wandkollisionen
- Sammelobjekte, Heilung und Fortschrittsziele
- Steigenden Schwierigkeitsgrad
- Funktionsfähige Levelwechsel und Checkpoints

Die Leveldateien werden mit dem Asset-Generator erzeugt und in die LunarAxis-JAR eingebunden.

Nutze die bestehende farbcodierte PNG-Struktur oder ein nachweislich kompatibles JSON-Format.

Vermeide sechs nahezu identische Karten. Die Level sollen sich spielerisch und visuell unterscheiden.

## 3. Spieler, Kampf und Fähigkeiten

Überarbeite den Spieler mit vollständig abgestimmten Animationen:

- Idle
- Walk
- Attack
- Hit
- Death
- Dash
- Spezialangriffe

Die Spielfigur besitzt vier animierte Blickrichtungen mit konsistenten Framegrössen, Proportionen und Trefferzonen.

### Mausbasierte Kampfsteuerung

Verwende folgende Steuerung:

- **WASD:** Spielerbewegung
- **Linksklick:** Normaler Nahkampfangriff in Richtung des Mauszeigers
- **Rechtsklick:** Dash in Richtung des Mauszeigers
- **E:** Kreisförmige Energiewelle
- **M:** Minimap ein- beziehungsweise ausblenden

Bewegungsrichtung und Angriffsrichtung müssen unabhängig voneinander funktionieren.

### Nahkampfangriff

Der normale Angriff richtet sich nach der tatsächlichen Position des Mauszeigers in der Spielwelt.

- Mausposition korrekt von Bildschirm- in Weltkoordinaten umrechnen
- Angriffsrichtung anhand des genauen Zielvektors bestimmen
- Trefferbereich passend zur Zielrichtung ausrichten
- Spielfigur visuell in eine der vier unterstützten Spriterichtungen drehen
- Trefferberechnung unabhängig von der Beschränkung auf vier Spriterichtungen durchführen
- Bestehende Cooldowns und Schadensregeln berücksichtigen
- Nur einen Angriff pro tatsächlichem Klick auslösen

Verwende bei aktiver Kamera die vorhandene `Camera2D.screenToWorld()`-Methode mit den bereits vom `InputManager` umgerechneten logischen Mauskoordinaten.

### Dash

Der Dash wird über die rechte Maustaste ausgelöst und bewegt den Spieler in Richtung des Mauszeigers.

- Eigene Dash-Animation
- Passender Cooldown
- Korrekte Kollisionsprüfung während der Bewegung
- Kein Durchqueren blockierender Wände
- Visuelles und akustisches Feedback

### Energiewelle

Die Spezialfähigkeit wird über `E` aktiviert.

- Kreisförmiger beziehungsweise flächenwirksamer Angriff
- Eigene Animation und Partikeleffekte
- Eindeutige Treffer- und Schadensregeln
- Cooldown und verständliche HUD-Anzeige
- Passender Soundeffekt

Die Fähigkeiten sollen unterschiedliche taktische Möglichkeiten bieten und nicht nur optische Varianten desselben Angriffs sein.

Neue Fähigkeiten müssen mit den vorhandenen Einstellungen und gespeicherten Keybinds zusammenpassen. Die vorgesehenen Mausaktionen und die festen Tasten `E` und `M` dürfen keine widersprüchlichen Steuerungsbelegungen verursachen.

Menüklicks dürfen keine Angriffe oder Dashes auslösen.

## 4. Gegner und Endboss

Erweitere die regulären Gegner auf mindestens vier unterscheidbare Typen:

- **Jäger:** Verfolgt den Spieler
- **Obelisk:** Stationärer Fernkämpfer
- **Wächter:** Langsamer, robuster Nahkämpfer
- **Schattenläufer:** Schnell, beweglich und mit eigenem Angriffsmuster

Jeder Gegnertyp benötigt eigene Werte, KI, Animationen, Angriffe und Trefferreaktionen.

### Endboss – The Abyss Warden

Level 6 enthält einen einzigartigen, grossen, vollständig animierten Boss.

Erforderlich sind:

- Eigenes grosses Spritesheet
- Idle-, Bewegungs-, Angriffs-, Treffer-, Phasenwechsel- und Todesanimationen
- Deutlich mehr Lebenspunkte als normale Gegner
- Eigene Boss-Healthbar mit Namen und animiertem Schadensverlauf
- Mindestens drei unterschiedliche Angriffe
- Mindestens zwei Kampfphasen
- Neue Angriffskombinationen und erhöhtes Tempo in Phase 2
- Faire Trefferzonen und ausweichbare Angriffsmuster
- Eigene Boss-Musik und Soundeffekte
- Eindeutige Todesanimation und abschliessender Victory-Bildschirm

Die Angriffe umfassen:

1. Gerichtete Projektile beziehungsweise Projektilsalven
2. Flächenangriffe mit deutlich sichtbaren Vorwarnungen
3. Nahkampf- oder Dash-Angriffe

Die Kampfphasen wechseln abhängig von der verbleibenden Gesundheit des Bosses.

Der Boss muss tatsächlich bekämpft und besiegt werden. Das Finale darf nicht nur durch das Einsammeln von Objekten abgeschlossen werden.

## 5. Professionelle Menüs mit Mausbedienung

Überarbeite folgende Benutzeroberflächen:

- Hauptmenü
- Pause-Menü
- Settings
- Game Over
- Victory

Die Menüs benötigen:

- Hochwertige Hintergründe und dekorative UI-Grafiken
- Animierte Hintergründe, beispielsweise schwebende Partikel oder Lichtbewegungen
- Individuell gestaltete Buttons
- Hover-, Press- und Auswahlzustände
- Vollständige Maussteuerung
- Weiterhin funktionierende Tastatursteuerung
- Flüssige Menüübergänge
- Lautstärkeregler und Keybind-Einstellungen
- Korrekte Eingabebehandlung bei Pause und Szenenwechsel

Nutze vorhandene LunarAxis-UI-Komponenten, sofern sie zur Gestaltung passen.

Vermeide eine unnötige Eigenimplementierung eines zweiten allgemeinen UI-Systems innerhalb des Spiels.

Die Maussteuerung der Menüs muss klar von den Kampfaktionen getrennt sein.

## 6. HUD, Minimap und Bossanzeige

Ersetze das bisherige einfache HUD durch eine zusammenhängende, hochwertige grafische Oberfläche.

Erforderlich sind:

- Kompaktes Spieler-HUD oben links
- Hochwertiger HUD-Rahmen aus PNG-Assets
- Flüssige, proportionale Healthbar
- Optional verzögerte Schadensanzeige
- Sichtbare Heilungsanimation
- Fähigkeiten-Icons mit Cooldown-Darstellung
- Echo-Fortschritt, Punktestand und Levelname
- Klare Statusmeldungen
- Gute Lesbarkeit

### Minimap

- Kompakt und optisch dezent
- Mit `M` ein- und ausblendbar
- Wichtige Levelinformationen gut erkennbar
- Bei Bedarf halbtransparente Darstellung

### Boss-Healthbar

- Oben mittig in einem geeigneten freien Bereich
- Mit Bossnamen
- Flüssiger Gesundheitsanzeige und verzögertem Schadensverlauf
- Nur während des Bosskampfes sichtbar

HUD, Minimap und Bossbar dürfen sich nicht gegenseitig überlagern.

Achte besonders darauf, dass Sammelobjekte, Gegner, Angriffsbereiche und wichtige Spielhinweise möglichst nicht durch die Benutzeroberfläche verdeckt werden.

Berücksichtige, dass relevante Spielobjekte durch Kamerabewegungen auch hinter fest positionierte HUD-Elemente gelangen können.

Falls nötig, verwende kleinere, verschobene oder situationsabhängig transparentere UI-Elemente.

Die logische Spielauflösung beträgt **800 × 560**.

## 7. Maximierbares Spielfenster

Das Lunar-Depths-Fenster soll über die normale Windows-Maximieren-Schaltfläche maximiert und anschliessend wiederhergestellt werden können.

Zusätzlich soll das Fenster manuell vergrössert und verkleinert werden können.

Die LunarAxis-Bibliothek stellt dafür die öffentliche Methode bereit:

```python
engine.enableResizableViewport()
```

Aktiviere diese nach dem Erstellen der `LunarEngine` und vor `engine.start()`.

Anforderungen:

- Logische Spielauflösung bleibt 800 × 560
- Gesamte Darstellung wird proportional skaliert
- Keine Verzerrung des Seitenverhältnisses
- Letterboxing bei abweichenden Fensterformaten
- Kamera, HUD, Menüs, Minimap und Bossbar bleiben korrekt ausgerichtet
- Mausklicks, Hover-Effekte und Button-Hitboxen funktionieren bei jeder Fenstergrösse
- Angriffe und Dash verwenden auch im maximierten Fenster korrekte Maus- und Weltkoordinaten
- Klicks ausserhalb des eigentlichen Spielbildes lösen keine Spielaktionen aus
- Rückkehr zur ursprünglichen Fenstergrösse ohne Darstellungsfehler

Verwende das vorhandene Viewport-System der LunarAxis-Bibliothek.

Implementiere keine eigene Fenster-Engine und keine zusätzliche manuelle Letterbox-Skalierung im TigerJython-Code.

Verwende `isMouseInsideViewport()` vor koordinatenabhängigen Spielaktionen.

## 8. Eigene TrueType-Schrift

Das Spiel soll eine eigens generierte Schriftart erhalten, nicht lediglich eine umbenannte Standardschrift wie SansSerif oder Monospaced.

Erstelle dafür einen vollständigen, ausführbaren **Python-3-Font-Generator**, vorzugsweise mit `fontTools`.

Die Schrift soll folgende Eigenschaften besitzen:

- Eigenständige Glyphenformen
- Futuristischer, leicht kantiger Stil
- Passend zur Lunar-Depths-Gestaltung
- Gute Lesbarkeit auch bei kleinen HUD-Texten
- Gross- und Kleinbuchstaben
- Ziffern und Satzzeichen
- Benötigte deutsche Sonderzeichen
- Keine Abhängigkeit von fremden urheberrechtlich geschützten Schriftdateien

Die Schrift wird als echte TTF-Datei erzeugt:

`AbyssGrid-Regular.ttf`

Speichere sie unter:

`src/main/resources/games/lunar-depths_v2/fonts/`

Prüfe die vorhandenen `ResourceManager`-, `Fonts`- und `Graphics`-APIs.

Lade die Schrift zur Laufzeit aus der JAR und verwende sie tatsächlich für:

- Hauptmenü
- Pause-Menü und Settings
- HUD
- Bossbar
- Statusanzeigen und weitere Spieltexte

## 9. Animationen und Asset-System

Erweitere den bisherigen Python-3-Asset-Generator vollständig.

Erzeuge damit:

- Spieler-Spritesheets mit allen benötigten Animationen
- Vier unterschiedliche Gegner-Spritesheets
- Grosses Boss-Spritesheet
- Passende Tilesets für alle sechs Level
- Farbcodierte Level-PNGs mit Markerinformationen
- Menü-Hintergründe und UI-Grafiken
- Animierte Menüelemente
- HUD, Healthbar-Rahmen, Bossbar und Fähigkeiten-Icons
- Partikel- und Angriffseffekte
- Sämtliche benötigten Musik- und Sounddateien

Die Grafiken müssen stilistisch zusammenpassen und eine erkennbare Weiterentwicklung gegenüber V1 darstellen.

Achte besonders auf:

- Konsistente Figurenproportionen
- Einheitliche Framegrössen
- Korrekte Animationen und Spritesheet-Indizes
- Transparente Hintergründe
- Saubere Skalierung
- Korrekte Levelmarker
- Passende Farben und visuelle Effekte

**Keine direkte Bildgenerierung durch ChatGPT.**

Liefere stattdessen einen vollständigen, auf meinem PC ausführbaren Python-3-Generator, der alle Dateien reproduzierbar erzeugt.

Verwende dafür:

`src/main/resources/games/lunar-depths_v2/`

Der Generator darf Pillow, Python-Standardbibliotheken und weitere klar benannte installierbare Abhängigkeiten verwenden.

Lege die notwendigen Unterordner automatisch an und dokumentiere die verwendeten Dateinamen, Abmessungen und Frame-Anordnungen.

Der Generator gehört nicht zum TigerJython-Spielcode.

## 10. Musik und Soundeffekte

Erweitere das vorhandene Audiosystem sinnvoll.

Erforderlich sind:

- Unterschiedliche Musik für Hauptmenü und reguläre Levels
- Eigener Boss-Soundtrack
- Kampfeffekte für Angriffe, Dash und Energiewelle
- Treffer- und Schadensgeräusche
- Geräusche für Heilung und Sammelobjekte
- Menü-Soundeffekte
- Geräusche für Levelwechsel und besondere Ereignisse

Musik- und Effektlautstärke bleiben getrennt regelbar und dauerhaft gespeichert.

Achte darauf, dass:

- Musik bei Szenenwechseln korrekt gestartet und gestoppt wird
- Keine ungewollt überlagernden Musikstücke entstehen
- Sounds nicht unbeabsichtigt dauerhaft weiterlaufen
- Die verwendeten WAV- beziehungsweise Audioformate von LunarAxis unterstützt werden

Alle Audioressourcen müssen in der JAR liegen und ohne zusätzliche Downloads funktionieren.

## 11. Speicherstände und Einstellungen

Verwende weiterhin den vorhandenen LunarAxis-`SaveManager`.

Speichere dauerhaft:

- Bestleistung beziehungsweise Highscore
- Kampagnenfortschritt
- Checkpoints
- Freigeschaltete Levels
- Musiklautstärke
- Effektlautstärke
- Individuell konfigurierbare Keybinds
- Weitere notwendige Spieleinstellungen

Speicherstände müssen nach einem vollständigen TigerJython-Neustart korrekt wiederhergestellt werden.

Verwende einen geeigneten, beschreibbaren Benutzerordner ausserhalb der JAR.

Berücksichtige die Übernahme bestehender V1-Profildaten und die Kompatibilität mit dem erweiterten V2-Speicherformat.

Nutze die tatsächlich vorhandenen `SaveManager`-Methoden. Implementiere keine unnötige eigene Speicherverwaltung mit direkten Betriebssystem- oder Java-Zugriffen.

Fehlende, beschädigte oder veraltete Speicherdateien müssen nachvollziehbar behandelt werden.

## 12. Debugging

Der Debug-Modus muss über `F3` ein- und ausschaltbar sein.

Erweitere ihn um sinnvolle Informationen:

- FPS und Frametimes, soweit verfügbar
- Spielerposition
- Gesundheit und Bewegungszustand
- Kamera- und Levelinformationen
- Actor-Hitboxen und Kollisionsbereiche
- Gegnerinformationen
- Projektil- und Angriffshitboxen
- Partikelanzahl
- Weitere tatsächlich vorhandene Diagnosefunktionen

Debugging darf weder die Spielregeln noch die Speicherstände verändern.

## 13. Verbindliche TigerJython-Regeln

- Genau eine vollständige `.py`-Datei für das gesamte Spiel
- Alle benötigten Python-Klassen innerhalb dieser einen Datei
- Keine weiteren Python-Spielmodule oder `.py`-Dateien
- Ausschliesslich vorhandene LunarAxis-APIs für Engine, Rendering, Input, Ressourcen, Audio, Speicherung und andere allgemeine Systeme
- Keine direkten Java-, AWT-, Swing- oder JavaFX-Imports
- Keine modernen CPython-spezifischen Features
- TigerJython-/Jython-kompatible Syntax
- Keine zusätzlichen Python-Pakete zur Laufzeit in TigerJython
- Sämtliche Assets aus der LunarAxis-JAR
- Keine Spielmechaniken in die LunarAxis-Java-Bibliothek verschieben
- Keine unnötige Eigenimplementierung bereits vorhandener LunarAxis-Systeme
- Keine künstliche Zeilenbegrenzung, aber kompakte und wartbare Implementierung

Die vollständige Spiellogik bleibt in TigerJython.

Funktionierende V1-Systeme sollen übernommen und sinnvoll erweitert werden.

Verändere bestehende Funktionen nur, wenn dies für die neuen Anforderungen erforderlich ist.

## 14. Abschliessende Qualitätsprüfung

Prüfe die vollständige Implementierung gegen die tatsächlich vorhandenen LunarAxis-APIs.

Kontrolliere insbesondere:

- Sämtliche sechs Level
- Levelwechsel und Checkpoints
- Spielerbewegung und Kamera
- Links- und Rechtsklick
- Maus-zu-Welt-Koordinatenumrechnung
- Angriffsrichtungen und Trefferbereiche
- Dash-Kollisionen
- Energiewelle und Cooldowns
- Gegner-KI und Bossverhalten
- Alle Animationen und Spritesheet-Indizes
- HUD, Minimap und Bossbar
- Menü- und Mauseingaben
- Fenstervergrösserung und Letterboxing
- Key-Rebinding und Eingabekonflikte
- Speichern, Laden und Neustart
- Laden der Schriftart aus der JAR
- Musik und Soundeffekte
- Performance und Speicherverbrauch
- Laufzeitfehler und Ausnahmefälle

Unterscheide ausdrücklich zwischen statisch geprüften Funktionen und tatsächlich ausgeführten Laufzeittests.

Behaupte keine Tests, die nicht durchgeführt wurden.

## 15. Ergebnis

Liefere in dieser Reihenfolge:

1. Kurze technische Analyse der relevanten LunarAxis-APIs
2. Übersicht der endgültigen Spielmechaniken und Architektur
3. Vollständigen endgültigen TigerJython-Spielcode
4. Vollständigen endgültigen Python-3-Asset-/Audio-/Level-Generator
5. Vollständigen Python-3-Font-Generator
6. Benötigte Python-Abhängigkeiten und Installationsbefehle für die Generatoren
7. Steuerung, Menüs, Einstellungen und Speicherverhalten
8. Umfassende Testfälle für die wesentlichen Spielsysteme

Der vollständige TigerJython-Code muss sämtliche Imports, Konstanten, Klassen, Methoden und den Engine-Start enthalten.

Falls die Datei nicht vollständig in eine Antwort passt, teile sie in mehrere nummerierte Abschnitte auf.

Jeder Abschnitt muss an einer sauberen Grenze enden. Die Abschnitte müssen sich ohne Überschneidungen und ohne fehlende Zeilen direkt hintereinander zu einer einzigen vollständigen `.py`-Datei zusammensetzen lassen.

Keine Platzhalter, kein Pseudocode, keine ausgelassenen Methoden, keine nachträglichen Code-Patches und keine widersprüchlichen Zwischenversionen.

Prüfe vor der endgültigen Ausgabe die Konsistenz aller Klassen, Methoden, Ressourcenpfade und Generatoren.

## Arbeitsgrenzen

- Die bereitgestellte ZIP-Datei ausschliesslich lesen und analysieren
- Den bereitgestellten V1-Spielcode und Asset-Generator als Grundlage verwenden
- Keine Änderungen an der LunarAxis-Bibliothek
- Sämtlichen Code ausschliesslich im Chat liefern
- Keine Platzhalter oder Pseudocode
- Keine erfundenen APIs
- Keine unnötigen Code-Duplizierungen
- Keine unnötig langen Methoden oder künstlichen Zeilenumbrüche
- Keine zusätzlichen Spielsysteme, die für dieses Beispiel nicht benötigt werden
- Keine neuen Spielmechaniken innerhalb der Java-Bibliothek

**Wichtigstes Ziel:** Ein vollständiges, optisch hochwertiges und tatsächlich spielbares 2D-Action-Adventure mit sechs Levels, vier regulären Gegnertypen, animiertem Endboss, mausgerichtetem Kampfsystem, Fähigkeiten, professionellen Menüs, HUD, Musik, eigener Schriftart und dauerhaften Speicherständen. Das Spiel soll die erweiterten Möglichkeiten von LunarAxis demonstrieren und als anspruchsvolles Referenzprojekt für die Entwicklung mit TigerJython dienen.

```