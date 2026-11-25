
# Dodge Arena - Survive the Storm
# Reine TigerJython / JGameGrid Version
# Keine LunarAxis-Abhaengigkeiten

from gamegrid import *
from java.awt import Color, Font, RenderingHints
from javax.imageio import ImageIO
from java.io import File
from java.lang import System
import random
import math

# --------------------------------------------------
# KONFIGURATION
# --------------------------------------------------

WIDTH = 760
HEIGHT = 520

LEFT = 24
TOP = 72
RIGHT = 736
BOTTOM = 496

CENTER_X = 380
CENTER_Y = 284

PLAYER_R = 12
TOKEN_R = 8

ASSETS = "C:/Users/patri/IdeaProjects/LunarAxis/src/main/resources/games/dodge-arena/"

FRAME_MS = 16

TEXT = Color(229, 244, 255)
MUTED = Color(157, 184, 213)
CYAN = Color(68, 240, 234)
GOLD = Color(255, 211, 89)
RED = Color(255, 88, 118)
VIOLET = Color(186, 132, 255)
SHADE = Color(4, 8, 19, 175)

rng = random.Random()

# --------------------------------------------------
# BILDER LADEN
# --------------------------------------------------

images = {}

def load_images():
    names = (
        "arena",
        "panel",
        "player",
        "player-hit",
        "core",
        "bolt-right",
        "bolt-left",
        "bolt-down",
        "bolt-up"
        )

    for name in names:
        path = ASSETS + name + ".png"
        file = File(path)

        if not file.isFile():
            raise RuntimeError("Dodge Arena asset fehlt: " +
                file.getAbsolutePath())

        image = ImageIO.read(file)

        if image is None:
            raise RuntimeError("Bild unlesbar: " + path)

        images[name] = image

load_images()

# --------------------------------------------------
# SPIELZUSTAND
# --------------------------------------------------

state = "ready"

x = float(CENTER_X)
y = float(CENTER_Y)

elapsed = 0.0
tokens = 0

flash = 0.0
until_spawn = 0.9

hazards = []
token = (CENTER_X, CENTER_Y)

last_time = System.nanoTime()

# --------------------------------------------------
# HILFSFUNKTIONEN
# --------------------------------------------------

def clamp(value, minimum, maximum):
    return max(minimum, min(maximum, value))


def circle_collision(x1, y1, r1, x2, y2, r2):
    dx = x2 - x1
    dy = y2 - y1
    radius = r1 + r2

    return dx * dx + dy * dy <= radius * radius


def random_int(low, high):
    return rng.randrange(low, high)


def place_token():
    global token

    if x < CENTER_X:
        tx = random_int(420, RIGHT - 32)
    else:
        tx = random_int(LEFT + 32, 340)

    ty = random_int(TOP + 32, BOTTOM - 32)

    token = (tx, ty)


def reset_game():
    global x, y, elapsed, tokens
    global flash, until_spawn, hazards, state

    x = float(CENTER_X)
    y = float(CENTER_Y)

    elapsed = 0.0
    tokens = 0

    flash = 0.0
    until_spawn = 0.9

    hazards = []

    place_token()

    state = "playing"


def spawn_hazard():
    side = random_int(0, 4)

    if side < 2:
        r = 13

        if side == 0:
            hx = LEFT + r
            vx = 1
        else:
            hx = RIGHT - r
            vx = -1

        hy = random_int(TOP + 24, BOTTOM - 24)
        vy = 0
        color = RED

    else:
        r = 17

        hx = random_int(LEFT + 24, RIGHT - 24)
        vx = 0

        if side == 2:
            hy = TOP + r
            vy = 1
        else:
            hy = BOTTOM - r
            vy = -1

        color = VIOLET

    hazards.append((float(hx), float(hy), vx, vy, r, color, 0.65))

# --------------------------------------------------
# GAME LOGIC
# --------------------------------------------------

def update_game(dt):
    global state, x, y
    global elapsed, flash, until_spawn
    global hazards, tokens

    if state != "playing":
        if isKeyPressed(32):
            reset_game()
        return

    # WASD
    dx = int(isKeyPressed(68)) - int(isKeyPressed(65))
    dy = int(isKeyPressed(83)) - int(isKeyPressed(87))

    step = 265.0 * dt

    if dx != 0 and dy != 0:
        step *= 0.70710678

    x = clamp(x + dx * step,
        LEFT + PLAYER_R,
        RIGHT - PLAYER_R)

    y = clamp(y + dy * step,
        TOP + PLAYER_R,
        BOTTOM - PLAYER_R)

    # Zeit und Schwierigkeitsanstieg
    elapsed += dt
    flash = max(0.0, flash - dt)

    ramp = elapsed / (elapsed + 24.0)
    speed = 130.0 + 260.0 * ramp

    until_spawn -= dt

    if until_spawn <= 0.0:
        spawn_hazard()
        until_spawn += 1.25 - 1.02 * ramp

    # Projektile
    remaining = []

    for hx, hy, vx, vy, r, color, warning in hazards:
        travel = max(0.0, dt - warning)
        warning = max(0.0, warning - dt)

        hx += vx * speed * travel
        hy += vy * speed * travel

        if (LEFT + r <= hx <= RIGHT - r and
            TOP + r <= hy <= BOTTOM - r):

            remaining.append((hx, hy, vx, vy, r, color, warning))

            if warning == 0.0:
                if circle_collision(x, y, PLAYER_R, hx, hy, r):
                    state = "over"

    hazards = remaining

    # Kerne einsammeln
    if state == "playing":
        tx, ty = token

        if circle_collision(x, y, PLAYER_R, tx, ty, TOKEN_R):
            tokens += 1
            flash = 0.35
            place_token()

# --------------------------------------------------
# RENDERING MIT GAMEGRID
# --------------------------------------------------

def draw_sprite(bg, name, px, py):
    # Alle beweglichen Originalsprites: 64 x 64
    bg.drawImage(images[name],
        int(round(px)) - 32,
        int(round(py)) - 32)


def draw_text(g, value, px, py, size, color, family="SansSerif", centered=False):
    font = Font(family, Font.PLAIN, size)

    g.setFont(font)
    g.setColor(color)

    if centered:
        fm = g.getFontMetrics(font)
        px = px - fm.stringWidth(value) / 2

    g.drawString(value, int(px), int(py))


def draw_hud(g):
    score = int(elapsed) + 25 * tokens

    draw_text(g, "DODGE ARENA",
        40, 34, 21, CYAN)

    draw_text(g, "SURVIVE THE STORM",
        41, 50, 11, MUTED, "Monospaced")

    draw_text(g, "PUNKTE",
        456, 29, 11, MUTED, "Monospaced")

    draw_text(g, "ZEIT",
        607, 29, 11, MUTED, "Monospaced")

    draw_text(g, "%04d" % score,
        456, 50, 19, GOLD, "Monospaced")

    draw_text(g, "%05.1f s" % elapsed,
        607, 50, 19, TEXT, "Monospaced")


def draw_overlay(bg, g):
    g.setColor(SHADE)
    g.fillRect(LEFT, TOP, RIGHT - LEFT, BOTTOM - TOP)

    bg.drawImage(images["panel"],
        CENTER_X - 240,
        CENTER_Y - 110)

    if state == "ready":
        title = "SURVIVE THE STORM"
        prompt = "LEERTASTE ZUM STARTEN"
        subtitle = ("Pfeiltasten  /  " "Ausweichen und Kerne sammeln")
        accent = CYAN
    else:
        title = "GAME OVER"
        prompt = "LEERTASTE FUER EINE NEUE RUNDE"
        subtitle = "Vom Sturm erwischt"
        accent = RED

    draw_text(g, title,
        CENTER_X, 249, 31,
        accent, centered=True)

    draw_text(g, subtitle,
        CENTER_X, 290, 16,
        MUTED, centered=True)

    draw_text(g, prompt,
        CENTER_X, 344, 14,
        TEXT, "Monospaced", centered=True)


def render_game():
    bg = getBg()

    # Hintergrundbuffer zuruecksetzen
    bg.clear()

    # Original-Hintergrund
    bg.drawImage(images["arena"], 0, 0)

    # Kern
    draw_sprite(bg, "core", token[0], token[1])

    g = bg.getContext()

    g.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON)

    # Projektile und Vorwarnungen
    for hx, hy, vx, vy, r, color, warning in hazards:

        if warning > 0.0:
            ix = int(round(hx))
            iy = int(round(hy))

            g.setColor(color)

            radius = r + int(warning * 8)

            g.drawOval(ix - radius, iy - radius, radius * 2, radius * 2)

            g.drawLine(ix, iy, 
                int(round(hx + vx * (r + 5))), int(round(hy + vy * (r + 5))))

        else:
            if vx != 0:
                if vx > 0:
                    name = "bolt-right"
                else:
                    name = "bolt-left"
            else:
                if vy > 0:
                    name = "bolt-down"
                else:
                    name = "bolt-up"

            draw_sprite(bg, name, hx, hy)

    # Spielfigur
    if state == "over":
        player_image = "player-hit"
    else:
        player_image = "player"

    draw_sprite(bg, player_image, x, y)

    # Sammel-Effekt
    if flash > 0.0:
        radius = (PLAYER_R + 4 + int((0.35 - flash) * 30))

        g.setColor(GOLD)

        ix = int(round(x))
        iy = int(round(y))

        g.drawOval(ix - radius, iy - radius, radius * 2, radius * 2)

    # Interface
    draw_hud(g)

    if state != "playing":
        draw_overlay(bg, g)


# --------------------------------------------------
# GAMEGRID SIMULATIONSZYKLUS
# --------------------------------------------------

def on_act():
    global last_time

    now = System.nanoTime()
    dt = (now - last_time) / 1000000000.0
    last_time = now

    update_game(dt)
    render_game()


# --------------------------------------------------
# INITIALISIERUNG
# --------------------------------------------------

makeGameGrid(WIDTH, HEIGHT, 1, None, False)

setTitle("Dodge Arena - Survive the Storm")
setBgColor(Color(4, 8, 19))

setSimulationPeriod(FRAME_MS)

# Ausgangszustand wie im Original
reset_game()
state = "ready"

registerAct(on_act)

render_game()

show()
doRun()