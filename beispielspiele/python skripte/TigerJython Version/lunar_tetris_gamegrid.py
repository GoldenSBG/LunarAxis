
# ============================================================
# LUNAR TETRIS - NEON BLOCKS
# TigerJython + JGameGrid
#
# Keine LunarAxis-Abhaengigkeiten
# Keine Speicherung von Spielstaenden oder Dateien
# ============================================================

from gamegrid import *
from java.awt import Color, Font, RenderingHints
from java.awt.event import KeyEvent
from java.lang import System
from java.io import File
from javax.imageio import ImageIO
import random


# ============================================================
# 1. BILDERORDNER - NUR DIESEN PFAD ANPASSEN
# ============================================================

ASSET_DIR = "C:/Users/patri/IdeaProjects/LunarAxis/src/main/resources/games/lunar-tetris/"

# In diesem Ordner muessen liegen:
# arena.png
# blocks.png


# ============================================================
# 2. KONFIGURATION
# ============================================================

WIDTH, HEIGHT = 800, 666
COLS, ROWS, CELL = 10, 20, 28
BOARD_X, BOARD_Y = 260, 80

KINDS = "IOTSZJL"
EMPTY = -1

TEXT = Color(231, 244, 255)
MUTED = Color(149, 177, 208)
CYAN = Color(73, 239, 242)
GOLD = Color(255, 212, 100)
RED = Color(255, 100, 133)
SHADE = Color(5, 9, 21, 204)
PANEL = Color(12, 22, 43)

MONO = "Monospaced"
SANS = "SansSerif"

LINE_POINTS = (0, 100, 300, 500, 800)
CLEAR_NAMES = ("", "SINGLE", "DOUBLE", "TRIPLE", "TETRIS")

LOCK_DELAY = 0.50
LOCK_RESETS = 15
REPEAT_DELAY = 0.15
REPEAT_INTERVAL = 0.045
SOFT_INTERVAL = 0.04

KEY_CODES = {
    "LEFT": KeyEvent.VK_LEFT,
    "RIGHT": KeyEvent.VK_RIGHT,
    "DOWN": KeyEvent.VK_DOWN,
    "UP": KeyEvent.VK_UP,
    "ENTER": KeyEvent.VK_ENTER,
    "P": KeyEvent.VK_P,
    "ESCAPE": KeyEvent.VK_ESCAPE,
    "C": KeyEvent.VK_C,
    "X": KeyEvent.VK_X,
    "Z": KeyEvent.VK_Z,
    "SPACE": KeyEvent.VK_SPACE
}


# ============================================================
# 3. BILDER LADEN
# ============================================================

def load_png(name):
    # Direkter absoluter Dateipfad.
    # Keine Verwendung von __file__ oder user.dir.
    path = File(ASSET_DIR + name)

    if not path.isFile():
        raise RuntimeError(
            "Bild nicht gefunden: " + path.getAbsolutePath()
        )

    image = ImageIO.read(path)

    if image is None:
        raise RuntimeError(
            "Bild konnte nicht gelesen werden: " +
            path.getAbsolutePath()
        )

    return image


# ============================================================
# 4. GAMEGRID / JAVA2D ZEICHENADAPTER
# ============================================================

class GraphicsAdapter(object):

    def __init__(self, graphics):
        self.g = graphics

        self.g.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        )

        self.g.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        )

    def setColor(self, color):
        self.g.setColor(color)

    def drawImage(self, image, x, y, width=None, height=None):
        if width is None:
            self.g.drawImage(
                image, int(x), int(y), None
            )
        else:
            self.g.drawImage(
                image,
                int(x), int(y),
                int(width), int(height),
                None
            )

    def drawString(self, value, x, y, size, family):
        self.g.setFont(
            Font(family, Font.PLAIN, int(size))
        )
        self.g.drawString(
            str(value), int(x), int(y)
        )

    def drawCenteredString(self, value, x, y, size, family):
        font = Font(family, Font.PLAIN, int(size))
        self.g.setFont(font)

        value = str(value)
        width = self.g.getFontMetrics(font).stringWidth(value)

        self.g.drawString(
            value, int(x - width / 2), int(y)
        )

    def fillRect(self, x, y, width, height):
        self.g.fillRect(
            int(x), int(y),
            int(width), int(height)
        )

    def drawRect(self, x, y, width, height):
        self.g.drawRect(
            int(x), int(y),
            int(width), int(height)
        )


# ============================================================
# 5. TETROMINO-DEFINITIONEN
# ============================================================

SPAWN_CELLS = (
    ((0, 1), (1, 1), (2, 1), (3, 1)),  # I
    ((1, 0), (2, 0), (1, 1), (2, 1)),  # O
    ((1, 0), (0, 1), (1, 1), (2, 1)),  # T
    ((1, 0), (2, 0), (0, 1), (1, 1)),  # S
    ((0, 0), (1, 0), (1, 1), (2, 1)),  # Z
    ((0, 0), (0, 1), (1, 1), (2, 1)),  # J
    ((2, 0), (0, 1), (1, 1), (2, 1))   # L
)


# ============================================================
# 6. SRS WALL-KICKS
# ============================================================

JLSTZ_KICKS = {
    (0, 1): ((0, 0), (-1, 0), (-1, 1), (0, -2), (-1, -2)),
    (1, 0): ((0, 0), (1, 0), (1, -1), (0, 2), (1, 2)),
    (1, 2): ((0, 0), (1, 0), (1, -1), (0, 2), (1, 2)),
    (2, 1): ((0, 0), (-1, 0), (-1, 1), (0, -2), (-1, -2)),
    (2, 3): ((0, 0), (1, 0), (1, 1), (0, -2), (1, -2)),
    (3, 2): ((0, 0), (-1, 0), (-1, -1), (0, 2), (-1, 2)),
    (3, 0): ((0, 0), (-1, 0), (-1, -1), (0, 2), (-1, 2)),
    (0, 3): ((0, 0), (1, 0), (1, 1), (0, -2), (1, -2))
}

I_KICKS = {
    (0, 1): ((0, 0), (-2, 0), (1, 0), (-2, -1), (1, 2)),
    (1, 0): ((0, 0), (2, 0), (-1, 0), (2, 1), (-1, -2)),
    (1, 2): ((0, 0), (-1, 0), (2, 0), (-1, 2), (2, -1)),
    (2, 1): ((0, 0), (1, 0), (-2, 0), (1, -2), (-2, 1)),
    (2, 3): ((0, 0), (2, 0), (-1, 0), (2, 1), (-1, -2)),
    (3, 2): ((0, 0), (-2, 0), (1, 0), (-2, -1), (1, 2)),
    (3, 0): ((0, 0), (1, 0), (-2, 0), (1, -2), (-2, 1)),
    (0, 3): ((0, 0), (-1, 0), (2, 0), (-1, 2), (2, -1))
}


def make_rotations():
    result = []

    for kind, cells in enumerate(SPAWN_CELLS):
        states = [cells]
        pivot_edge = 3 if kind == 0 else 2

        for unused in range(3):
            if kind != 1:
                cells = tuple(
                    (pivot_edge - y, x)
                    for x, y in cells
                )

            states.append(cells)

        result.append(tuple(states))

    return tuple(result)


SHAPES = make_rotations()


# ============================================================
# 7. TETRIS SPIEL
# ============================================================

class GameGridTetris(object):

    def __init__(self):
        self.random = random.Random()

        self.down = {}
        self.pressed = {}

        # Bilder ausschliesslich aus ASSET_DIR
        self.background = load_png("arena.png")
        sheet = load_png("blocks.png")

        if (sheet.getWidth() != CELL * 7 or
                sheet.getHeight() != CELL * 2):
            raise RuntimeError(
                "blocks.png muss 196 x 56 Pixel gross sein."
            )

        self.blocks = tuple(
            sheet.getSubimage(i * CELL, 0, CELL, CELL)
            for i in range(7)
        )

        self.ghosts = tuple(
            sheet.getSubimage(i * CELL, CELL, CELL, CELL)
            for i in range(7)
        )

        self.reset()
        self.state = "ready"
        self.publish_frame()

    # --------------------------------------------------------
    # TASTATUR
    # --------------------------------------------------------

    def poll_keys(self):
        previous = self.down

        self.down = dict(
            (name, bool(isKeyPressed(code)))
            for name, code in KEY_CODES.items()
        )

        self.pressed = dict(
            (
                name,
                self.down[name] and
                not previous.get(name, False)
            )
            for name in KEY_CODES
        )

    def key_down(self, name):
        return self.down.get(name, False)

    def key_pressed(self, name):
        return self.pressed.get(name, False)

    # --------------------------------------------------------
    # SPIEL ZURUECKSETZEN
    # --------------------------------------------------------

    def reset(self):
        self.board = [
            [EMPTY] * COLS
            for unused in range(ROWS)
        ]

        self.queue = []
        self.held = EMPTY
        self.hold_used = False

        self.score = 0
        self.lines = 0
        self.level = 1

        self.clear_label = ""
        self.clear_timer = 0.0

        self.direction = 0
        self.repeat_time = 0.0

        self.state = "playing"
        self.spawn(self.take_next())

    # --------------------------------------------------------
    # 7-BAG RANDOMIZER
    # --------------------------------------------------------

    def refill_queue(self):
        while len(self.queue) < 4:
            bag = list(range(len(KINDS)))

            for i in range(len(bag) - 1, 0, -1):
                j = self.random.randrange(i + 1)

                bag[i], bag[j] = bag[j], bag[i]

            self.queue.extend(bag)

    def take_next(self):
        self.refill_queue()
        return self.queue.pop(0)

    # --------------------------------------------------------
    # STEIN SPAWNEN
    # --------------------------------------------------------

    def spawn(self, kind):
        self.kind = kind
        self.rotation = 0

        self.x = 3
        self.y = 0

        self.fall_time = 0.0
        self.lock_time = 0.0
        self.lock_resets = 0

        self.soft_active = False

        if not self.fits(
            self.rotation,
            self.x,
            self.y
        ):
            self.state = "over"

    # --------------------------------------------------------
    # KOLLISIONS- UND POSITIONSPRUEFUNG
    # --------------------------------------------------------

    def fits(self, rotation, origin_x, origin_y):
        for dx, dy in SHAPES[self.kind][rotation]:
            x = origin_x + dx
            y = origin_y + dy

            if x < 0 or x >= COLS:
                return False

            if y < -2 or y >= ROWS:
                return False

            if y >= 0 and self.board[y][x] != EMPTY:
                return False

        return True

    def grounded(self):
        return not self.fits(
            self.rotation,
            self.x,
            self.y + 1
        )

    def landing_y(self):
        y = self.y

        while self.fits(
            self.rotation,
            self.x,
            y + 1
        ):
            y += 1

        return y

    # --------------------------------------------------------
    # LOCK DELAY
    # --------------------------------------------------------

    def reset_lock(self, was_grounded):
        if (was_grounded and
                self.lock_resets < LOCK_RESETS):

            self.lock_time = 0.0
            self.lock_resets += 1

    # --------------------------------------------------------
    # BEWEGUNG
    # --------------------------------------------------------

    def move_horizontal(self, direction):
        target_x = self.x + direction

        if self.fits(
            self.rotation,
            target_x,
            self.y
        ):
            was_grounded = self.grounded()

            self.x = target_x
            self.reset_lock(was_grounded)

    # --------------------------------------------------------
    # ROTATION
    # --------------------------------------------------------

    def rotate(self, direction):
        if self.kind == 1:
            return

        target = (self.rotation + direction) % 4

        if self.kind == 0:
            kicks = I_KICKS
        else:
            kicks = JLSTZ_KICKS

        was_grounded = self.grounded()

        for dx, up in kicks[(self.rotation, target)]:
            x = self.x + dx
            y = self.y - up

            if self.fits(target, x, y):
                self.x = x
                self.y = y
                self.rotation = target

                self.reset_lock(was_grounded)
                return

    # --------------------------------------------------------
    # HOLD
    # --------------------------------------------------------

    def hold(self):
        if self.held == EMPTY:
            incoming = self.take_next()
        else:
            incoming = self.held

        self.held = self.kind
        self.hold_used = True

        self.spawn(incoming)

    # --------------------------------------------------------
    # HARD DROP
    # --------------------------------------------------------

    def hard_drop(self):
        target_y = self.landing_y()

        self.score += 2 * (target_y - self.y)
        self.y = target_y

        self.lock_piece()

    # --------------------------------------------------------
    # STEIN FIXIEREN UND ZEILEN LOESCHEN
    # --------------------------------------------------------

    def lock_piece(self):
        cells = [
            (self.x + dx, self.y + dy)
            for dx, dy in
            SHAPES[self.kind][self.rotation]
        ]

        for x, y in cells:
            if y < 0:
                self.state = "over"
                return

        for x, y in cells:
            self.board[y][x] = self.kind

        remaining = [
            row for row in self.board
            if EMPTY in row
        ]

        cleared = ROWS - len(remaining)

        if cleared:
            self.board = (
                [[EMPTY] * COLS
                 for unused in range(cleared)]
                + remaining
            )

            gained = LINE_POINTS[cleared] * self.level

            self.score += gained
            self.lines += cleared
            self.level = 1 + self.lines // 10

            self.clear_label = "%s  +%d" % (
                CLEAR_NAMES[cleared],
                gained
            )

            self.clear_timer = 1.2

        self.hold_used = False
        self.spawn(self.take_next())

    # --------------------------------------------------------
    # HORIZONTALE TASTENWIEDERHOLUNG
    # --------------------------------------------------------

    def update_horizontal(self, dt):
        left = self.key_down("LEFT")
        right = self.key_down("RIGHT")

        if left and right:
            if self.key_pressed("RIGHT"):
                direction = 1
            elif self.key_pressed("LEFT"):
                direction = -1
            else:
                direction = self.direction
        else:
            direction = int(right) - int(left)

        if direction != self.direction:
            self.direction = direction
            self.repeat_time = REPEAT_DELAY

            if direction:
                self.move_horizontal(direction)

        elif direction:
            self.repeat_time -= dt

            while self.repeat_time <= 0.0:
                self.move_horizontal(direction)
                self.repeat_time += REPEAT_INTERVAL

    # --------------------------------------------------------
    # FALLGESCHWINDIGKEIT / SOFT DROP
    # --------------------------------------------------------

    def update_fall(self, dt):
        soft = self.key_down("DOWN")

        interval = max(
            0.06,
            0.80 * (0.82 ** (self.level - 1))
        )

        if soft:
            interval = SOFT_INTERVAL

        if soft != self.soft_active:
            if soft:
                self.fall_time = interval - dt
            else:
                self.fall_time = 0.0

            self.soft_active = soft

        self.fall_time += dt

        while self.fall_time >= interval:
            self.fall_time -= interval

            if self.grounded():
                self.fall_time = 0.0
                break

            self.y += 1

            if soft:
                self.score += 1

        if self.grounded():
            self.lock_time += dt

            if self.lock_time >= LOCK_DELAY:
                self.lock_piece()

    # --------------------------------------------------------
    # SPIEL-UPDATE
    # --------------------------------------------------------

    def update(self, dt):
        self.step(dt)
        self.publish_frame()

    def step(self, dt):

        if self.state in ("ready", "over"):
            if self.key_pressed("ENTER"):
                self.reset()
            return

        if (self.key_pressed("P") or
                self.key_pressed("ESCAPE")):

            if self.state == "playing":
                self.state = "paused"
            else:
                self.state = "playing"

            self.direction = 0
            return

        if self.state == "paused":
            return

        self.clear_timer = max(
            0.0,
            self.clear_timer - dt
        )

        if (not self.hold_used and
                self.key_pressed("C")):
            self.hold()
            return

        self.update_horizontal(dt)

        if (self.key_pressed("UP") or
                self.key_pressed("X")):
            self.rotate(1)

        elif self.key_pressed("Z"):
            self.rotate(-1)

        if self.key_pressed("SPACE"):
            self.hard_drop()
            return

        self.update_fall(dt)

    # --------------------------------------------------------
    # FRAME SNAPSHOT
    # --------------------------------------------------------

    def publish_frame(self):
        active = None

        if self.state in ("playing", "paused"):
            active = (
                self.kind,
                self.rotation,
                self.x,
                self.y,
                self.landing_y()
            )

        self.frame = (
            self.state,
            tuple(tuple(row) for row in self.board),
            active,
            self.held,
            self.hold_used,
            tuple(self.queue[:3]),
            self.score,
            self.level,
            self.lines,
            self.clear_label
            if self.clear_timer > 0.0 else ""
        )

    # --------------------------------------------------------
    # STEINE ZEICHNEN
    # --------------------------------------------------------

    def draw_piece(
        self, g, image,
        kind, rotation, x, y
    ):
        for dx, dy in SHAPES[kind][rotation]:
            if y + dy >= 0:
                g.drawImage(
                    image,
                    BOARD_X + (x + dx) * CELL,
                    BOARD_Y + (y + dy) * CELL
                )

    # --------------------------------------------------------
    # HOLD / NEXT VORSCHAU
    # --------------------------------------------------------

    def draw_preview(self, g, kind, cx, cy):
        if kind == EMPTY:
            return

        cells = SPAWN_CELLS[kind]
        size = 20

        min_x = min(x for x, y in cells)
        max_x = max(x for x, y in cells)
        min_y = min(y for x, y in cells)
        max_y = max(y for x, y in cells)

        x0 = cx - (max_x - min_x + 1) * size // 2
        y0 = cy - (max_y - min_y + 1) * size // 2

        for x, y in cells:
            g.drawImage(
                self.blocks[kind],
                x0 + (x - min_x) * size,
                y0 + (y - min_y) * size,
                size,
                size
            )

    # --------------------------------------------------------
    # HUD
    # --------------------------------------------------------

    def draw_hud(
        self, g, held, hold_used,
        queue, score, level, lines, clear_label
    ):
        g.setColor(CYAN)
        g.drawString(
            "LUNAR TETRIS", 24, 41, 30, SANS
        )

        g.setColor(MUTED)
        g.drawString(
            "NEON BLOCKS  /  10 x 20",
            570, 39, 13, MONO
        )

        g.drawString(
            "HOLD  /  C", 40, 109, 14, MONO
        )

        g.drawString(
            "NEXT", 580, 109, 14, MONO
        )

        g.drawString(
            "PUNKTE", 40, 255, 13, MONO
        )

        g.drawString(
            "LEVEL", 40, 335, 13, MONO
        )

        g.drawString(
            "LINIEN", 40, 415, 13, MONO
        )

        g.setColor(TEXT)

        g.drawString(
            "%08d" % score,
            40, 287, 24, MONO
        )

        g.drawString(
            "%02d" % level,
            40, 367, 24, MONO
        )

        g.drawString(
            "%03d" % lines,
            40, 447, 24, MONO
        )

        self.draw_preview(g, held, 130, 155)

        if hold_used:
            g.setColor(MUTED)
            hold_label = "GESPERRT"
        else:
            g.setColor(CYAN)
            hold_label = "BEREIT"

        g.drawCenteredString(
            hold_label, 130, 201, 12, MONO
        )

        for i, kind in enumerate(queue):
            self.draw_preview(
                g, kind, 670, 150 + i * 66
            )

        g.setColor(GOLD)
        g.drawCenteredString(
            clear_label, 130, 523, 15, MONO
        )

        g.setColor(CYAN)
        g.drawString(
            "STEUERUNG", 580, 377, 14, MONO
        )

        g.setColor(TEXT)

        controls = (
            "LINKS / RECHTS  Zug",
            "HOCH / X       Dreh.",
            "Z              Gegen",
            "RUNTER         Soft",
            "LEERTASTE      Hard",
            "C              Hold",
            "P / ESC        Pause",
            "ENTER          Start"
        )

        for i, line in enumerate(controls):
            g.drawString(
                line,
                580,
                407 + i * 25,
                13,
                MONO
            )

    # --------------------------------------------------------
    # START / PAUSE / GAME OVER
    # --------------------------------------------------------

    def draw_overlay(self, g, state):
        title, hint, accent = {
            "ready": (
                "NEON BLOCKS",
                "ENTER  /  START",
                CYAN
            ),
            "paused": (
                "PAUSE",
                "P / ESC  /  WEITER",
                CYAN
            ),
            "over": (
                "GAME OVER",
                "ENTER  /  NEUE RUNDE",
                RED
            )
        }[state]

        g.setColor(SHADE)

        g.fillRect(
            BOARD_X,
            BOARD_Y,
            COLS * CELL,
            ROWS * CELL
        )

        g.setColor(PANEL)
        g.fillRect(
            274, 302, 252, 116
        )

        g.setColor(accent)
        g.drawRect(
            274, 302, 252, 116
        )

        g.drawCenteredString(
            title, 400, 349, 26, SANS
        )

        g.setColor(TEXT)
        g.drawCenteredString(
            hint, 400, 386, 13, MONO
        )

    # --------------------------------------------------------
    # RENDERING
    # --------------------------------------------------------

    def render(self, g):
        (
            state,
            board,
            active,
            held,
            hold_used,
            queue,
            score,
            level,
            lines,
            label
        ) = self.frame

        # Hintergrund
        g.drawImage(self.background, 0, 0)

        # Bereits fixierte Bloecke
        for y, row in enumerate(board):
            for x, kind in enumerate(row):
                if kind != EMPTY:
                    g.drawImage(
                        self.blocks[kind],
                        BOARD_X + x * CELL,
                        BOARD_Y + y * CELL
                    )

        # Aktiver Stein und Ghost
        if active is not None:
            kind, rotation, x, y, ghost_y = active

            if ghost_y != y:
                self.draw_piece(
                    g,
                    self.ghosts[kind],
                    kind,
                    rotation,
                    x,
                    ghost_y
                )

            self.draw_piece(
                g,
                self.blocks[kind],
                kind,
                rotation,
                x,
                y
            )

        # HUD
        self.draw_hud(
            g,
            held,
            hold_used,
            queue,
            score,
            level,
            lines,
            label
        )

        # Overlay
        if state != "playing":
            self.draw_overlay(g, state)


# ============================================================
# 8. GAMEGRID INITIALISIEREN
# ============================================================

makeGameGrid(
    WIDTH - 1,
    HEIGHT - 1,
    1,
    None,
    None,
    False
)

setTitle("Lunar Tetris - Neon Blocks (GameGrid)")
setBgColor(Color(5, 9, 21))
setSimulationPeriod(16)

game = GameGridTetris()

last_ns = System.nanoTime()


# ============================================================
# 9. GAMEGRID SIMULATIONSZYKLUS
# ============================================================

def on_act():
    global last_ns

    now = System.nanoTime()

    dt = max(
        0.0,
        (now - last_ns) / 1000000000.0
    )

    last_ns = now

    game.poll_keys()
    game.update(dt)

    bg = getBg()
    bg.clear()

    graphics = GraphicsAdapter(bg.getContext())
    game.render(graphics)


# ============================================================
# 10. SPIEL STARTEN
# ============================================================

registerAct(on_act)

bg = getBg()
bg.clear()

game.render(
    GraphicsAdapter(bg.getContext())
)

show()
doRun()
