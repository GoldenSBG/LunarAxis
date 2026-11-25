from LunarAxis.core import LunarEngine
from LunarAxis.core.manager import IGameLogic
from LunarAxis.graphics import Colors
from LunarAxis.inputs import Key
from LunarAxis.util import LunarRandom

WIDTH, HEIGHT = 800, 666
COLS, ROWS, CELL = 10, 20, 28
BOARD_X, BOARD_Y = 260, 80
ASSETS = "games/lunar-tetris/"
KINDS = "IOTSZJL"
EMPTY = -1

TEXT = Colors(231, 244, 255)
MUTED = Colors(149, 177, 208)
CYAN = Colors(73, 239, 242)
GOLD = Colors(255, 212, 100)
RED = Colors(255, 100, 133)
SHADE = Colors(5, 9, 21, 204)
PANEL = Colors(12, 22, 43)
MONO = "Monospaced"
SANS = "SansSerif"

SPAWN_CELLS = (
    ((0, 1), (1, 1), (2, 1), (3, 1)),  # I
    ((1, 0), (2, 0), (1, 1), (2, 1)),  # O
    ((1, 0), (0, 1), (1, 1), (2, 1)),  # T
    ((1, 0), (2, 0), (0, 1), (1, 1)),  # S
    ((0, 0), (1, 0), (1, 1), (2, 1)),  # Z
    ((0, 0), (0, 1), (1, 1), (2, 1)),  # J
    ((2, 0), (0, 1), (1, 1), (2, 1)),  # L
)

# Orientations are 0, R, 2, L. Positive kick Y points upward.
JLSTZ_KICKS = {
    (0, 1): ((0, 0), (-1, 0), (-1, 1), (0, -2), (-1, -2)),
    (1, 0): ((0, 0), (1, 0), (1, -1), (0, 2), (1, 2)),
    (1, 2): ((0, 0), (1, 0), (1, -1), (0, 2), (1, 2)),
    (2, 1): ((0, 0), (-1, 0), (-1, 1), (0, -2), (-1, -2)),
    (2, 3): ((0, 0), (1, 0), (1, 1), (0, -2), (1, -2)),
    (3, 2): ((0, 0), (-1, 0), (-1, -1), (0, 2), (-1, 2)),
    (3, 0): ((0, 0), (-1, 0), (-1, -1), (0, 2), (-1, 2)),
    (0, 3): ((0, 0), (1, 0), (1, 1), (0, -2), (1, -2)),
}

I_KICKS = {
    (0, 1): ((0, 0), (-2, 0), (1, 0), (-2, -1), (1, 2)),
    (1, 0): ((0, 0), (2, 0), (-1, 0), (2, 1), (-1, -2)),
    (1, 2): ((0, 0), (-1, 0), (2, 0), (-1, 2), (2, -1)),
    (2, 1): ((0, 0), (1, 0), (-2, 0), (1, -2), (-2, 1)),
    (2, 3): ((0, 0), (2, 0), (-1, 0), (2, 1), (-1, -2)),
    (3, 2): ((0, 0), (-2, 0), (1, 0), (-2, -1), (1, 2)),
    (3, 0): ((0, 0), (1, 0), (-2, 0), (1, -2), (-2, 1)),
    (0, 3): ((0, 0), (-1, 0), (2, 0), (-1, 2), (2, -1)),
}

LINE_POINTS = (0, 100, 300, 500, 800)
CLEAR_NAMES = ("", "SINGLE", "DOUBLE", "TRIPLE", "TETRIS")
LOCK_DELAY = 0.50
LOCK_RESETS = 15
REPEAT_DELAY = 0.15
REPEAT_INTERVAL = 0.045
SOFT_INTERVAL = 0.04


def make_rotations():
    result = []
    for kind, cells in enumerate(SPAWN_CELLS):
        states = [cells]
        pivot_edge = 3 if kind == 0 else 2
        for unused in range(3):
            if kind != 1:
                cells = tuple((pivot_edge - y, x) for x, y in cells)
            states.append(cells)
        result.append(tuple(states))
    return tuple(result)


SHAPES = make_rotations()


class LunarTetris(IGameLogic):
    def init(self, engine):
        self.keys = engine.getInputManager()
        self.random = LunarRandom()
        resources = engine.getResourceManager()
        self.background = resources.loadImage(ASSETS + "arena.png")
        sheet = resources.loadSpriteSheet(ASSETS + "blocks.png", CELL, CELL)
        if self.background is None or sheet is None:
            raise RuntimeError("Missing Lunar Tetris PNGs in " + ASSETS)
        if sheet.getColumns() != 7 or sheet.getRows() != 2:
            raise RuntimeError("Expected a 7 x 2 block sheet in " + ASSETS)
        self.blocks = tuple(sheet.getSprite(i, 0) for i in range(7))
        self.ghosts = tuple(sheet.getSprite(i, 1) for i in range(7))
        self.reset()
        self.state = "ready"
        self.publish_frame()

    def reset(self):
        self.board = [[EMPTY] * COLS for unused in range(ROWS)]
        self.queue = []
        self.held = EMPTY
        self.hold_used = False
        self.score, self.lines, self.level = 0, 0, 1
        self.clear_label, self.clear_timer = "", 0.0
        self.direction, self.repeat_time = 0, 0.0
        self.state = "playing"
        self.spawn(self.take_next())

    def refill_queue(self):
        while len(self.queue) < 4:
            bag = list(range(len(KINDS)))
            for i in range(len(bag) - 1, 0, -1):
                j = self.random.nextInt(i + 1)
                bag[i], bag[j] = bag[j], bag[i]
            self.queue.extend(bag)

    def take_next(self):
        self.refill_queue()
        return self.queue.pop(0)

    def spawn(self, kind):
        self.kind, self.rotation = kind, 0
        self.x, self.y = 3, 0
        self.fall_time, self.lock_time = 0.0, 0.0
        self.lock_resets = 0
        self.soft_active = False
        if not self.fits(self.rotation, self.x, self.y):
            self.state = "over"

    def fits(self, rotation, origin_x, origin_y):
        for dx, dy in SHAPES[self.kind][rotation]:
            x, y = origin_x + dx, origin_y + dy
            if x < 0 or x >= COLS or y < -2 or y >= ROWS:
                return False
            if y >= 0 and self.board[y][x] != EMPTY:
                return False
        return True

    def grounded(self):
        return not self.fits(self.rotation, self.x, self.y + 1)

    def landing_y(self):
        y = self.y
        while self.fits(self.rotation, self.x, y + 1):
            y += 1
        return y

    def reset_lock(self, was_grounded):
        if was_grounded and self.lock_resets < LOCK_RESETS:
            self.lock_time = 0.0
            self.lock_resets += 1

    def move_horizontal(self, direction):
        x = self.x + direction
        if self.fits(self.rotation, x, self.y):
            was_grounded = self.grounded()
            self.x = x
            self.reset_lock(was_grounded)

    def rotate(self, direction):
        if self.kind == 1:
            return
        target = (self.rotation + direction) % 4
        kicks = I_KICKS if self.kind == 0 else JLSTZ_KICKS
        was_grounded = self.grounded()
        for dx, up in kicks[(self.rotation, target)]:
            x, y = self.x + dx, self.y - up
            if self.fits(target, x, y):
                self.x, self.y, self.rotation = x, y, target
                self.reset_lock(was_grounded)
                return

    def hold(self):
        incoming = self.take_next() if self.held == EMPTY else self.held
        self.held = self.kind
        self.hold_used = True
        self.spawn(incoming)

    def hard_drop(self):
        target_y = self.landing_y()
        self.score += 2 * (target_y - self.y)
        self.y = target_y
        self.lock_piece()

    def lock_piece(self):
        cells = [(self.x + dx, self.y + dy)
                 for dx, dy in SHAPES[self.kind][self.rotation]]
        # Check top-out before writing any part of the piece.
        if any(y < 0 for x, y in cells):
            self.state = "over"
            return
        for x, y in cells:
            self.board[y][x] = self.kind

        remaining = [row for row in self.board if EMPTY in row]
        cleared = ROWS - len(remaining)
        if cleared:
            self.board = [[EMPTY] * COLS for unused in range(cleared)] + remaining
            gained = LINE_POINTS[cleared] * self.level
            self.score += gained
            self.lines += cleared
            self.level = 1 + self.lines // 10
            self.clear_label = "%s  +%d" % (CLEAR_NAMES[cleared], gained)
            self.clear_timer = 1.2

        self.hold_used = False
        self.spawn(self.take_next())

    def update_horizontal(self, dt):
        left = self.keys.isKeyDown(Key.LEFT)
        right = self.keys.isKeyDown(Key.RIGHT)
        if left and right:
            if self.keys.isKeyPressed(Key.RIGHT):
                direction = 1
            elif self.keys.isKeyPressed(Key.LEFT):
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

    def update_fall(self, dt):
        soft = self.keys.isKeyDown(Key.DOWN)
        interval = max(0.06, 0.80 * (0.82 ** (self.level - 1)))
        if soft:
            interval = SOFT_INTERVAL
        if soft != self.soft_active:
            # Soft drop responds at once, then repeats at its own rate.
            self.fall_time = interval - dt if soft else 0.0
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

    def update(self, dt):
        self.step(dt)
        self.publish_frame()

    def step(self, dt):
        if self.state == "ready" or self.state == "over":
            if self.keys.isKeyPressed(Key.ENTER):
                self.reset()
            return

        if self.keys.isKeyPressed(Key.P) or self.keys.isKeyPressed(Key.ESCAPE):
            self.state = "paused" if self.state == "playing" else "playing"
            self.direction = 0
            return
        if self.state == "paused":
            return

        self.clear_timer = max(0.0, self.clear_timer - dt)
        if not self.hold_used and self.keys.isKeyPressed(Key.C):
            self.hold()
            return

        self.update_horizontal(dt)
        if self.keys.isKeyPressed(Key.UP) or self.keys.isKeyPressed(Key.X):
            self.rotate(1)
        elif self.keys.isKeyPressed(Key.Z):
            self.rotate(-1)

        if self.keys.isKeyPressed(Key.SPACE):
            self.hard_drop()
            return

        self.update_fall(dt)

    def publish_frame(self):
        # Rendering reads a snapshot, separate from the mutable board.
        active = None
        if self.state in ("playing", "paused"):
            active = (self.kind, self.rotation, self.x, self.y, self.landing_y())
        self.frame = (
            self.state, tuple(tuple(row) for row in self.board), active,
            self.held, self.hold_used, tuple(self.queue[:3]),
            self.score, self.level, self.lines,
            self.clear_label if self.clear_timer > 0.0 else ""
        )

    def draw_piece(self, g, image, kind, rotation, x, y):
        for dx, dy in SHAPES[kind][rotation]:
            if y + dy >= 0:
                g.drawImage(image, BOARD_X + (x + dx) * CELL,
                            BOARD_Y + (y + dy) * CELL)

    def draw_preview(self, g, kind, cx, cy):
        if kind == EMPTY:
            return
        cells = SPAWN_CELLS[kind]
        size = 20
        min_x, max_x = min(x for x, y in cells), max(x for x, y in cells)
        min_y, max_y = min(y for x, y in cells), max(y for x, y in cells)
        x0 = cx - (max_x - min_x + 1) * size // 2
        y0 = cy - (max_y - min_y + 1) * size // 2
        for x, y in cells:
            g.drawImage(self.blocks[kind], x0 + (x - min_x) * size,
                        y0 + (y - min_y) * size, size, size)

    def draw_hud(self, g, held, hold_used, queue, score, level, lines, clear_label):
        g.setColor(CYAN)
        g.drawString("LUNAR TETRIS", 24, 41, 30, SANS)
        g.setColor(MUTED)
        g.drawString("NEON BLOCKS  /  10 x 20", 570, 39, 13, MONO)
        g.drawString("HOLD  /  C", 40, 109, 14, MONO)
        g.drawString("NEXT", 580, 109, 14, MONO)
        g.drawString("PUNKTE", 40, 255, 13, MONO)
        g.drawString("LEVEL", 40, 335, 13, MONO)
        g.drawString("LINIEN", 40, 415, 13, MONO)
        g.setColor(TEXT)
        g.drawString("%08d" % score, 40, 287, 24, MONO)
        g.drawString("%02d" % level, 40, 367, 24, MONO)
        g.drawString("%03d" % lines, 40, 447, 24, MONO)

        self.draw_preview(g, held, 130, 155)
        g.setColor(MUTED if hold_used else CYAN)
        g.drawCenteredString("GESPERRT" if hold_used else "BEREIT", 130, 201, 12, MONO)
        for i, kind in enumerate(queue):
            self.draw_preview(g, kind, 670, 150 + i * 66)

        g.setColor(GOLD)
        g.drawCenteredString(clear_label, 130, 523, 15, MONO)
        g.setColor(CYAN)
        g.drawString("STEUERUNG", 580, 377, 14, MONO)
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
            g.drawString(line, 580, 407 + i * 25, 13, MONO)

    def draw_overlay(self, g, state):
        title, hint, accent = {
            "ready": ("NEON BLOCKS", "ENTER  /  START", CYAN),
            "paused": ("PAUSE", "P / ESC  /  WEITER", CYAN),
            "over": ("GAME OVER", "ENTER  /  NEUE RUNDE", RED)
        }[state]
        g.setColor(SHADE)
        g.fillRect(BOARD_X, BOARD_Y, COLS * CELL, ROWS * CELL)
        g.setColor(PANEL)
        g.fillRect(274, 302, 252, 116)
        g.setColor(accent)
        g.drawRect(274, 302, 252, 116)
        g.drawCenteredString(title, 400, 349, 26, SANS)
        g.setColor(TEXT)
        g.drawCenteredString(hint, 400, 386, 13, MONO)

    def render(self, g):
        state, board, active, held, hold_used, queue, score, level, lines, label = self.frame
        g.drawImage(self.background, 0, 0)
        for y, row in enumerate(board):
            for x, kind in enumerate(row):
                if kind != EMPTY:
                    g.drawImage(self.blocks[kind], BOARD_X + x * CELL, BOARD_Y + y * CELL)

        if active is not None:
            kind, rotation, x, y, ghost_y = active
            if ghost_y != y:
                self.draw_piece(g, self.ghosts[kind], kind, rotation, x, ghost_y)
            self.draw_piece(g, self.blocks[kind], kind, rotation, x, y)

        self.draw_hud(g, held, hold_used, queue, score, level, lines, label)
        if state != "playing":
            self.draw_overlay(g, state)


engine = LunarEngine(WIDTH, HEIGHT, "Lunar Tetris - Neon Blocks")
engine.setGameLogic(LunarTetris())
engine.start()