from LunarAxis.core import LunarEngine
from LunarAxis.core.manager import IGameLogic
from LunarAxis.graphics import Animation, Camera2D, Colors, Fonts
from LunarAxis.graphics.debug import DebugDraw
from LunarAxis.graphics.particle import ParticleBuilder, ParticleSystem
from LunarAxis.grid import Grid, Tile, TileMap
from LunarAxis.inputs import Key
from LunarAxis.math import Rect, Vec2
from LunarAxis.objects import Actor
from LunarAxis.physics import Collision
from LunarAxis.util import LunarRandom, SaveManager
from LunarAxis.diagnostics import Logger, LunarException


ROOT = "games/lunar-depths_v2/"
W, H, CELL = 800, 560, 32
HP_MAX = 5
PROFILE_NAME = "lunar_depths_v2"
PROFILE_SCHEMA = "2"
PROFILE_DIRECTORY = "#/lunar-depths_v2"

TITLES = (
    "Versunkene Vorhalle", "Kristallzisterne", "Kern des Abgrunds",
    "Vergessene Katakomben", "Schattenfestung", "Thronsaal des Abgrunds"
)
MAP_SIZES = ((32, 22), (36, 24), (42, 26), (38, 28), (44, 29), (34, 23))
FLOORS = (0x121B2F, 0x18233A, 0x1F2B44)
WALL = 0x25283C
MARKERS = {
    0x44F4DB: "spawn", 0xFF5678: "hunter", 0xBC73FF: "obelisk",
    0xFFD368: "echo", 0x63A4FF: "exit", 0x74F388: "heal",
    0xF1873D: "guard", 0xED5CDB: "runner", 0xFFFFFF: "boss"
}

FIGURES = ("keeper", "hunter", "obelisk", "guard", "runner", "boss")
STATES = ("idle", "walk", "attack", "hit", "death", "dash", "special", "phase")
FRAME_TIMES = (0.18, 0.09, 0.085, 0.065, 0.14, 0.055, 0.09, 0.16)
FACING = ((0, 1), (0, -1), (-1, 0), (1, 0))
NEIGHBORS = ((1, 0), (-1, 0), (0, 1), (0, -1))

ACTIONS = ("Hoch", "Runter", "Links", "Rechts", "Nahkampf", "Dash", "Energiewelle")
DEFAULTS = ("W", "S", "A", "D", "SPACE", "SHIFT", "E")
KEY_NAMES = (
    "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z "
    "UP DOWN LEFT RIGHT SPACE SHIFT CTRL "
    "DIGIT0 DIGIT1 DIGIT2 DIGIT3 DIGIT4 DIGIT5 DIGIT6 DIGIT7 DIGIT8 DIGIT9"
).split()
KEYS = dict((name, getattr(Key, name)) for name in KEY_NAMES)
KEY_LABELS = {
    "SPACE": "LEER", "SHIFT": "SHIFT", "CTRL": "CTRL",
    "UP": "HOCH", "DOWN": "RUNTER", "LEFT": "LINKS", "RIGHT": "RECHTS"
}
RESERVED_MOVEMENT = ("E", "M")
LEFT_MOUSE, RIGHT_MOUSE = 1, 3

SFX = (
    "swing", "hit", "hurt", "echo", "gate", "cast", "dash", "wave",
    "heal", "menu_click", "boss_warn", "boss_slam", "boss_phase", "victory"
)

ATTACK_CD, DASH_CD, WAVE_CD = 0.42, 1.20, 4.50
PLAYER_ATTACK_TIME, PLAYER_DASH_TIME, PLAYER_WAVE_TIME = 0.34, 0.22, 0.36
BOSS_HP = 36
ENEMY_STATS = {
    "hunter": (2, 70.0, 10, 100), "obelisk": (3, 0.0, 11, 150),
    "guard": (5, 48.0, 12, 220), "runner": (3, 102.0, 9, 200),
    "boss": (BOSS_HP, 68.0, 23, 1500)
}

INK = Colors(5, 10, 22)
DARK = Colors(14, 25, 45)
SLATE = Colors(42, 63, 88)
WHITE = Colors(232, 245, 255)
MUTED = Colors(154, 181, 209)
CYAN = Colors(71, 237, 224)
GOLD = Colors(255, 213, 107)
RED = Colors(255, 97, 132)
VIOLET = Colors(185, 133, 255)
GREEN = Colors(112, 249, 160)
SHADE = Colors(4, 8, 18, 153)
COOLDOWN_SHADE = Colors(5, 9, 21, 185)
WARN_FILL = Colors(255, 89, 118, 44)

FONT = "AbyssGrid"
FONT_PATH = ROOT + "fonts/AbyssGrid-Regular.ttf"
MONO, SANS = FONT, FONT


def clamp(value, low, high):
    return max(low, min(high, value))


def length(x, y):
    return (x * x + y * y) ** 0.5


def unit(x, y):
    size = length(x, y)
    if size > 0.0:
        return x / size, y / size
    return 0.0, 0.0


def rect_at(x, y, radius):
    return Rect(x - radius, y - radius, radius * 2, radius * 2)


def centered_text(g, text, x, y, size, color=WHITE):
    g.setColor(color)
    g.drawCenteredString(text, x, y, size, SANS)


def color_alpha(color, alpha):
    return Colors(color.getRed(), color.getGreen(), color.getBlue(),
                  clamp(int(alpha), 0, 255))


def key_label(name):
    return KEY_LABELS.get(name, name[5:] if name.startswith("DIGIT") else name)


def report_java(operation, path, error):
    causes = []
    current = error
    while current is not None:
        causes.append(str(current.toString()))
        current = current.getCause()
    Logger.warn("LunarDepths", "%s | %s | %s" %
                (operation, path, " -> ".join(causes)), error)


def movement_bindings(profile):
    # Keep stored bindings intact when an old profile uses a reserved key.
    result = []
    for i, name in enumerate(profile.bindings[:4]):
        if name in RESERVED_MOVEMENT:
            candidates = (DEFAULTS[i],) + DEFAULTS[:4] + tuple(KEY_NAMES)
            for candidate in candidates:
                if (candidate not in RESERVED_MOVEMENT and candidate not in result
                        and candidate not in profile.bindings[i + 1:4]):
                    name = candidate
                    break
        result.append(name)
    return result


class Profile(object):
    def __init__(self):
        self.store = SaveManager.getInstance()
        self.directory = PROFILE_DIRECTORY
        self.path = self.directory + "/" + PROFILE_NAME + ".json"
        self.stage, self.checkpoint, self.best, self.unlocked = 0, 0, 0, 0
        self.has_run, self.completed = False, False
        self.music, self.effects = 5, 7
        self.bindings = list(DEFAULTS)
        self.notice = ""
        self.read_only = False
        self.load()


    def number(self, name, default, low, high):
        return clamp(int(self.store.getString(name, str(default))), low, high)

    def read_values(self, schema):
        last_stage = 2 if schema == "1" else 5
        stage = self.number("stage", 0, 0, last_stage)
        checkpoint = self.number("checkpoint_score", 0, 0, 999999999)
        best = self.number("best", 0, 0, 999999999)
        unlocked = self.number("unlocked", 0, stage, last_stage)
        music = self.number("music", 5, 0, 10)
        effects = self.number("effects", 7, 0, 10)
        has_run = bool(self.number("has_run", 0, 0, 1))
        completed = schema == "2" and bool(self.number("completed", 0, 0, 1))
        bindings = []
        for i, default in enumerate(DEFAULTS):
            candidate = (default if schema == "1" and i >= 5 else
                         str(self.store.getString("key%d" % i, default)))
            if candidate not in KEYS or (i < 4 and candidate in bindings):
                for alternative in [default] + KEY_NAMES:
                    if alternative not in bindings:
                        candidate = alternative
                        break
            bindings.append(candidate)
        self.stage, self.checkpoint, self.best = stage, checkpoint, best
        self.unlocked, self.has_run, self.completed = unlocked, has_run, completed
        self.music, self.effects, self.bindings = music, effects, bindings

    def load(self):
        operation = "SaveManager.setDirectory"
        try:
            self.store.setDirectory(self.directory)

            operation = "SaveManager.load"
            self.store.load(PROFILE_NAME)

            operation = "read profile schema"
            schema = str(self.store.getString("schema", ""))
            if schema not in ("1", PROFILE_SCHEMA):
                self.read_only = True
                self.notice = "Unbekanntes Speicherformat. Datei bleibt erhalten."
                Logger.warn("LunarDepths", "%s | schema=%s" % (self.path, schema))
                return

            operation = "read profile values"
            self.read_values(schema)
            if schema == "1":
                if self.save():
                    self.notice = "Spielstand uebernommen."

        except LunarException as error:
            self.notice = "Kein Profil geladen. Details in der Konsole."
            report_java(operation, self.path, error)
        except (TypeError, ValueError) as error:
            self.notice = "Ungueltige Profilwerte. Details in der Konsole."
            Logger.warn("LunarDepths", "%s | %s | %s" %
                        (operation, self.path, str(error)))

    def save(self):
        if self.read_only:
            self.notice = "Speichern gesperrt: unbekanntes Profilformat."
            return False

        values = {
            "schema": PROFILE_SCHEMA, "stage": str(self.stage),
            "checkpoint_score": str(self.checkpoint), "best": str(self.best),
            "unlocked": str(self.unlocked), "has_run": str(int(self.has_run)),
            "completed": str(int(self.completed)), "music": str(self.music),
            "effects": str(self.effects)
        }
        for i, key in enumerate(self.bindings):
            values["key%d" % i] = key

        operation = "SaveManager.setDirectory"
        try:
            self.store.setDirectory(self.directory)
            operation = "SaveManager.clear"
            self.store.clear()
            for field in sorted(values):
                operation = "SaveManager.set(%s)" % field
                self.store.set(field, values[field])
            operation = "SaveManager.save"
            self.store.save(PROFILE_NAME)
            self.notice = ""
            return True
        except LunarException as error:
            self.notice = "Speichern fehlgeschlagen. Details in der Konsole."
            report_java(operation, self.path, error)
        except (TypeError, ValueError) as error:
            self.notice = "Profilwert ungueltig. Details in der Konsole."
            Logger.warn("LunarDepths", "%s | %s | %s" %
                        (operation, self.path, str(error)))
        return False


class Assets(object):
    def __init__(self, resources):
        self.resources = resources
        self.ui = {}
        for name, dimensions in (
            ("title", (800, 560)), ("logo", (432, 84)), ("panel", (460, 422)),
            ("hud", (330, 106)), ("bossbar", (432, 54)), ("map", (176, 118))
        ):
            self.ui[name] = self.load_asset_image("ui/" + name + ".png", dimensions)

        self.buttons = self.frames("ui/buttons.png", 360, 40, 1, 3)
        self.icons = self.frames("ui/abilities.png", 40, 40, 3, 1)[0]
        self.motes = self.frames("ui/motes.png", 24, 24, 4, 1)[0]
        self.items = self.frames("sprites/items.png", 32, 32, 6, 1)[0]
        self.effects = self.frames("sprites/effects.png", 64, 64, 4, 3)
        self.animations = {}

        for kind in FIGURES:
            size = 80 if kind == "boss" else 32
            sheet = self.frames("sprites/" + kind + ".png", size, size, 4, 32)
            for facing in range(4):
                for state, name in enumerate(STATES):
                    self.animations[(kind, facing, name)] = Animation(
                        list(sheet[facing * 8 + state]), FRAME_TIMES[state], state < 2
                    )

    def load_asset_image(self, path, dimensions):
        loaded = self.resources.loadImage(ROOT + path)
        if loaded is None:
            raise RuntimeError("Missing V2 asset: " + ROOT + path)
        if (loaded.getWidth(), loaded.getHeight()) != dimensions:
            raise RuntimeError("Invalid V2 asset dimensions: " + ROOT + path)
        return loaded

    def frames(self, path, width, height, cols, rows):
        self.load_asset_image(path, (cols * width, rows * height))
        sheet = self.resources.loadSpriteSheet(ROOT + path, width, height)
        if sheet is None or sheet.getColumns() != cols or sheet.getRows() != rows:
            raise RuntimeError("Invalid V2 sprite sheet: " + ROOT + path)
        return tuple(tuple(sheet.getSprite(col, row) for col in range(cols))
                     for row in range(rows))

    def fighter_frame(self, fighter):
        animation = self.animations[(fighter.kind, fighter.facing, fighter.state)]
        return animation.getFrame(fighter.age)


class Audio(object):
    def __init__(self, manager, profile):
        self.manager, self.profile = manager, profile
        self.tracks = {}
        self.current = None
        for name in ("menu", "depths", "warden"):
            self.tracks[name] = manager.loadMusic(
                "ld2-" + name, ROOT + "audio/" + name + ".wav"
            )
        for name in SFX:
            manager.loadSound("ld2-" + name, ROOT + "audio/" + name + ".wav", 2)

    def music(self, name):
        if self.current == name:
            return
        self.manager.stopMusic()
        self.current = name
        if name is not None:
            self.tracks[name].setVolume(self.profile.music * 0.08)
            self.manager.playMusic("ld2-" + name, True)
            self.apply_volume()

    def apply_volume(self):
        if self.current is not None:
            self.tracks[self.current].setVolume(self.profile.music * 0.08)

    def sound(self, name):
        if self.profile.effects > 0:
            self.manager.playSoundWithPan("ld2-" + name, self.profile.effects * 0.08, 0.0)

class Fighter(object):
    def __init__(self, kind, x, y, stage):
        self.kind = kind
        if kind == "keeper":
            self.maximum, self.speed, self.radius, self.reward = HP_MAX, 165.0, 10, 0
        else:
            self.maximum, self.speed, self.radius, self.reward = ENEMY_STATS[kind]
            if kind not in ("boss", "obelisk"):
                self.speed += stage * 3.0
        self.hp = self.maximum
        self.actor = Actor()
        diameter = self.radius * 2
        self.actor.setWidth(diameter)
        self.actor.setHeight(diameter)
        self.actor.setHitbox(diameter, diameter, 0, 0)
        self.place(x, y)
        self.facing, self.state, self.age = 0, "idle", 0.0
        self.cooldown, self.invulnerable = 0.0, 0.0
        self.plan, self.plan_age = None, 0.0
        self.aim = (0.0, 1.0)
        self.anchor = (x, y)
        self.phase, self.pattern = 1, 0
        self.spent = False
        self.visual_hp, self.trail_hp = float(self.hp), float(self.hp)

    def place(self, x, y):
        self.x, self.y = x, y
        self.actor.setX(x - self.radius)
        self.actor.setY(y - self.radius)

    def bounds(self):
        return self.actor.getPhysicsBounds()

    def face(self, x, y):
        if abs(x) > abs(y):
            self.facing = 3 if x > 0 else 2
        elif y != 0:
            self.facing = 0 if y > 0 else 1

    def change(self, state):
        if state != self.state:
            self.state, self.age = state, 0.0

    def tick(self, dt):
        self.age += dt
        self.cooldown = max(0.0, self.cooldown - dt)
        self.invulnerable = max(0.0, self.invulnerable - dt)
        self.visual_hp += (self.hp - self.visual_hp) * min(1.0, 15.0 * dt)
        self.trail_hp += (self.hp - self.trail_hp) * min(1.0, 3.2 * dt)
        if self.trail_hp < self.visual_hp:
            self.trail_hp = self.visual_hp

    def start_plan(self, name, aim, anchor):
        self.plan, self.plan_age = name, 0.0
        self.aim, self.anchor = aim, anchor
        self.spent = False
        self.face(*aim)
        self.change("attack")

    def cancel_plan(self):
        self.plan, self.plan_age = None, 0.0

    def recover(self, seconds):
        self.cancel_plan()
        self.cooldown = seconds
        self.change("idle")


class Dungeon(object):
    def __init__(self, assets, stage, score):
        self.stage, self.score = stage, score
        self.cols, self.rows = MAP_SIZES[stage]
        self.width, self.height = self.cols * CELL, self.rows * CELL
        map_image = assets.load_asset_image("levels/%d.png" % (stage + 1), MAP_SIZES[stage])
        tile_images = assets.frames("tiles/%d.png" % (stage + 1), CELL, CELL, 4, 1)[0]
        tiles = []
        for i, tile_image in enumerate(tile_images):
            tile = Tile(i, INK, tile_image)
            tile.setSolid(i == 3)
            tiles.append(tile)

        grid = Grid(self.rows, self.cols, CELL)
        walls, enemies, items = set(), [], []
        spawn, exit_xy = None, None
        self.boss = None
        for row in range(self.rows):
            for col in range(self.cols):
                rgb = map_image.getRGB(col, row) & 0xFFFFFF
                if rgb == WALL:
                    index = 3
                    walls.add((col, row))
                elif rgb in FLOORS:
                    index = FLOORS.index(rgb)
                elif rgb in MARKERS:
                    index = 0
                    kind = MARKERS[rgb]
                    x, y = col * CELL + 16, row * CELL + 16
                    if kind == "spawn":
                        if spawn is not None:
                            raise RuntimeError("Duplicate player spawn")
                        spawn = (x, y)
                    elif kind == "exit":
                        if exit_xy is not None:
                            raise RuntimeError("Duplicate exit")
                        exit_xy = (x, y)
                    elif kind in ("echo", "heal"):
                        items.append((kind, x, y))
                    else:
                        enemy = Fighter(kind, x, y, stage)
                        enemies.append(enemy)
                        if kind == "boss":
                            if self.boss is not None:
                                raise RuntimeError("Duplicate Abyss Warden")
                            self.boss = enemy
                else:
                    raise RuntimeError("Unknown level marker %06X" % rgb)
                grid.setTile(row, col, tiles[index])

        self.total_echoes = sum(1 for kind, x, y in items if kind == "echo")
        if (spawn is None or exit_xy is None or self.total_echoes == 0
                or (stage == 5) != (self.boss is not None)):
            raise RuntimeError("Incomplete Lunar Depths V2 map %d" % (stage + 1))
        self.map = TileMap(grid)
        self.walls = frozenset(walls)
        self.items, self.enemies = items, enemies
        self.exit_xy, self.exit = exit_xy, rect_at(exit_xy[0], exit_xy[1], 15)
        self.player = Fighter("keeper", spawn[0], spawn[1], stage)
        self.echoes = self.total_echoes
        self.bolts, self.effects = [], []
        self.fx = ParticleSystem(96)
        self.flow_cell, self.flow = None, {}
        self.melee_cd, self.dash_cd, self.wave_cd = 0.0, 0.0, 0.0
        self.elapsed, self.heal_glow = 0.0, 0.0
        self.message, self.message_time = "Sammle die Echos", 2.2
        self.camera_x, self.camera_y = self.camera_target()
        self.player_attack = None

    def message_for(self, message, seconds=2.0):
        self.message, self.message_time = message, seconds

    def open_exit(self):
        return (self.echoes == 0 and
                (self.boss is None or (self.boss.hp == 0 and self.boss.age >= 0.56)))

    def cell(self, x, y):
        return int(x // CELL), int(y // CELL)

    def open_cell(self, col, row):
        return (0 <= col < self.cols and 0 <= row < self.rows
                and (col, row) not in self.walls)

    def free(self, x, y, radius):
        left, top = self.cell(x - radius, y - radius)
        right, bottom = self.cell(x + radius - 0.001, y + radius - 0.001)
        for row in range(top, bottom + 1):
            for col in range(left, right + 1):
                if not self.open_cell(col, row):
                    return False
        return True

    def move(self, actor, dx, dy):
        start_x, start_y = actor.x, actor.y
        x, y = start_x, start_y
        # Small substeps keep fast dashes and wall checks consistent.
        steps = max(1, int(max(abs(dx), abs(dy)) / 6.0) + 1)
        dx, dy = dx / steps, dy / steps
        for unused in range(steps):
            if self.free(x + dx, y, actor.radius):
                x += dx
            if self.free(x, y + dy, actor.radius):
                y += dy
        actor.place(x, y)
        return length(x - start_x, y - start_y)

    def sight(self, x, y, tx, ty):
        dx, dy = tx - x, ty - y
        steps = max(1, int(length(dx, dy) / 5.0) + 1)
        for step in range(1, steps):
            t = step / float(steps)
            if not self.free(x + dx * t, y + dy * t, 2):
                return False
        return True

    def update_flow(self):
        start = self.cell(self.player.x, self.player.y)
        if start == self.flow_cell:
            return
        self.flow_cell = start
        flow, queue, index = {start: 0}, [start], 0
        while index < len(queue):
            col, row = queue[index]
            index += 1
            for dx, dy in NEIGHBORS:
                other = (col + dx, row + dy)
                if other not in flow and self.open_cell(*other):
                    flow[other] = flow[(col, row)] + 1
                    queue.append(other)
        self.flow = flow

    def chase_target(self, enemy):
        p = self.player
        col, row = self.cell(enemy.x, enemy.y)
        here = self.flow.get((col, row), 99999)
        if here == 0:
            return p.x, p.y
        cx, cy = col * CELL + 16, row * CELL + 16
        for dx, dy in NEIGHBORS:
            if self.flow.get((col + dx, row + dy), 99999) < here:
                if dx != 0 and abs(cy - enemy.y) > 0.5:
                    return enemy.x, cy
                if dy != 0 and abs(cx - enemy.x) > 0.5:
                    return cx, enemy.y
                return cx + dx * CELL, cy + dy * CELL
        return enemy.x, enemy.y

    def camera_target(self):
        return (clamp(self.player.x, W / 2.0, self.width - W / 2.0),
                clamp(self.player.y, H / 2.0, self.height - H / 2.0))

    def update_camera(self, dt):
        x, y = self.camera_target()
        factor = min(1.0, dt * 8.0)
        self.camera_x += (x - self.camera_x) * factor
        self.camera_y += (y - self.camera_y) * factor


class Combat(object):
    def __init__(self, audio):
        self.audio = audio
        self.random = LunarRandom()

    def burst(self, world, x, y, color, count):
        for unused in range(count):
            vx = self.random.nextDouble(-105.0, 105.0)
            vy = self.random.nextDouble(-105.0, 105.0)
            (ParticleBuilder.create(world.fx).position(x, y).velocity(vx, vy)
             .size(3.0).life(0.38).color(color).emit())

    def hurt(self, world, target, damage):
        if target.hp <= 0 or target.invulnerable > 0.0:
            return False
        target.hp = max(0, target.hp - damage)
        self.burst(world, target.x, target.y, RED if target.kind == "keeper" else VIOLET, 10)
        self.audio.sound("hurt" if target.kind == "keeper" else "hit")
        if target.hp == 0:
            target.cancel_plan()
            target.change("death")
            world.score += target.reward
            if target.kind == "boss":
                world.bolts = [bolt for bolt in world.bolts if not bolt[5]]
                world.message_for("Der Abyss Warden ist gefallen.", 3.2)
                self.audio.sound("boss_slam")
            return True

        if target.kind == "boss":
            target.invulnerable = 0.14
            if target.phase == 1 and target.hp * 2 <= target.maximum:
                target.phase = 2
                target.pattern = 0
                target.cancel_plan()
                target.change("phase")
                target.invulnerable = 0.70
                world.bolts = [bolt for bolt in world.bolts if not bolt[5]]
                world.message_for("PHASE II - DER ABGRUND ERWACHT", 2.8)
                self.audio.sound("boss_phase")
            elif target.plan is None:
                target.change("hit")
            return True

        target.invulnerable = 0.82 if target.kind == "keeper" else 0.20
        target.cancel_plan()
        target.change("hit")
        target.cooldown = max(target.cooldown, 0.24)
        return True

    def melee_rect(self, actor):
        dx, dy = actor.aim
        return actor.x + dx * 25, actor.y + dy * 25, dx, dy

    def melee_overlap(self, attack, bounds):
        # Test the world axes and the two axes of the rotated attack.
        x, y, dx, dy = attack
        hx, hy = bounds.getWidth() * 0.5, bounds.getHeight() * 0.5
        ax, ay = bounds.getCenterX() - x, bounds.getCenterY() - y
        nx, ny = abs(dx), abs(dy)
        return (abs(ax) < hx + 20 * nx + 16 * ny
                and abs(ay) < hy + 20 * ny + 16 * nx
                and abs(ax * dx + ay * dy) < 20 + hx * nx + hy * ny
                and abs(-ax * dy + ay * dx) < 16 + hx * ny + hy * nx)

    def player_hit(self, world, wave):
        p = world.player
        attack = self.melee_rect(p)
        if wave:
            world.effects.append((0, p.x, p.y, 0.0, 0.38, 160))
            self.burst(world, p.x, p.y, VIOLET, 24)
        else:
            dx, dy = p.aim
            self.burst(world, p.x + dx * 24, p.y + dy * 24, CYAN, 7)
        for enemy in world.enemies:
            if enemy.hp <= 0 or not world.sight(p.x, p.y, enemy.x, enemy.y):
                continue
            if wave:
                hit = Collision.checkCircle(Vec2(p.x, p.y), 78.0,
                                            Vec2(enemy.x, enemy.y), enemy.radius)
            else:
                hit = self.melee_overlap(attack, enemy.bounds())
            if hit:
                self.hurt(world, enemy, 2 if wave else 1)

    def player_step(self, world, dt, movement, melee, dash, wave, aim):
        p = world.player
        p.tick(dt)
        world.melee_cd = max(0.0, world.melee_cd - dt)
        world.dash_cd = max(0.0, world.dash_cd - dt)
        world.wave_cd = max(0.0, world.wave_cd - dt)
        world.player_attack = None
        if p.hp <= 0:
            return
        if p.state == "hit":
            if p.age < 0.25:
                return
            p.change("idle")
        if p.state == "dash":
            remaining = PLAYER_DASH_TIME - (p.age - dt)
            travel = clamp(remaining, 0.0, dt)
            dx, dy = p.aim
            if travel > 0.0:
                world.move(p, dx * 440 * travel, dy * 440 * travel)
                self.burst(world, p.x, p.y, CYAN, 1)
            if p.age >= PLAYER_DASH_TIME:
                p.change("idle")
            return
        if p.state in ("attack", "special"):
            special = p.state == "special"
            if not p.spent and p.age >= (0.13 if special else 0.085):
                p.spent = True
                self.player_hit(world, special)
            if not special:
                world.player_attack = self.melee_rect(p)
            if p.age >= (PLAYER_WAVE_TIME if special else PLAYER_ATTACK_TIME):
                p.change("idle")
            return

        dx, dy = movement
        p.face(dx, dy)
        if dash and world.dash_cd == 0.0:
            p.aim = aim
            p.face(aim[0], aim[1])
            p.invulnerable = max(p.invulnerable, PLAYER_DASH_TIME + 0.03)
            world.dash_cd = DASH_CD
            p.change("dash")
            self.audio.sound("dash")
        elif wave and world.wave_cd == 0.0:
            world.wave_cd = WAVE_CD
            p.spent = False
            p.change("special")
            self.audio.sound("wave")
        elif melee and world.melee_cd == 0.0:
            p.aim = aim
            p.face(aim[0], aim[1])
            world.melee_cd = ATTACK_CD
            p.spent = False
            p.change("attack")
            self.audio.sound("swing")
        elif dx != 0 or dy != 0:
            nx, ny = unit(dx, dy)
            moved = world.move(p, nx * p.speed * dt, ny * p.speed * dt)
            p.change("walk" if moved > 0.01 else "idle")
        else:
            p.change("idle")

    def bolt(self, world, actor, aim, speed, boss=False):
        dx, dy = aim
        offset = actor.radius + 4
        x, y = actor.x + dx * offset, actor.y + dy * offset
        radius = 6 if boss else 4
        if world.free(x, y, radius):
            world.bolts.append((x, y, dx * speed, dy * speed, 3.0, boss))

    def volley(self, world, actor, aim):
        dx, dy = aim
        offsets = (-0.56, -0.28, 0, 0.28, 0.56) if actor.phase == 2 else (-0.33, 0, 0.33)
        for offset in offsets:
            direction = unit(dx - dy * offset, dy + dx * offset)
            self.bolt(world, actor, direction, 207.0 if actor.phase == 2 else 164.0, True)

    def walk_toward(self, world, actor, target, dt, multiplier=1.0):
        dx, dy = target[0] - actor.x, target[1] - actor.y
        distance = length(dx, dy)
        if distance == 0.0:
            actor.change("idle")
            return
        actor.face(dx, dy)
        step = min(distance, actor.speed * multiplier * dt)
        moved = world.move(actor, dx * step / distance, dy * step / distance)
        actor.change("walk" if moved > 0.01 else "idle")

    def close_hit(self, world, enemy, distance, damage):
        p = world.player
        dx, dy = enemy.aim
        x, y = enemy.x + dx * distance, enemy.y + dy * distance
        if (Collision.checkAABB(rect_at(x, y, distance), p.bounds())
                and world.sight(enemy.x, enemy.y, p.x, p.y)):
            self.hurt(world, p, damage)

    def charge(self, world, actor, dt, speed, damage):
        dx, dy = actor.aim
        world.move(actor, dx * speed * dt, dy * speed * dt)
        if (not actor.spent and
                Collision.checkAABB(actor.bounds(), world.player.bounds())):
            actor.spent = True
            self.hurt(world, world.player, damage)

    def regular_plan(self, world, enemy, dt):
        time = enemy.plan_age
        if enemy.plan == "runner":
            if time >= 0.44:
                enemy.recover(0.76)
            elif time >= 0.22:
                enemy.change("dash")
                self.charge(world, enemy, dt, 340.0, 1)
            return

        windup = {"hunter": 0.18, "guard": 0.47, "obelisk": 0.36}[enemy.plan]
        if not enemy.spent and time >= windup:
            enemy.spent = True
            if enemy.plan == "obelisk":
                self.bolt(world, enemy, enemy.aim, 147.0 + 12 * world.stage)
                self.audio.sound("cast")
            else:
                self.close_hit(world, enemy, 22 if enemy.plan == "guard" else 15,
                               2 if enemy.plan == "guard" else 1)
        if time >= windup + 0.18:
            enemy.recover({"hunter": 0.52, "guard": 0.89,
                           "obelisk": max(0.63, 1.30 - world.stage * 0.11)}[enemy.plan])


    def boss_timing(self, boss):
        if boss.plan == "slam":
            return (0.54 if boss.phase == 2 else 0.76), 0.26
        if boss.plan == "charge":
            return (0.39 if boss.phase == 2 else 0.59), 0.27
        return (0.33 if boss.phase == 2 else 0.48), 0.38

    def boss_plan(self, world, boss, dt):
        time = boss.plan_age
        windup, execution = self.boss_timing(boss)
        if time < windup:
            return
        if boss.plan == "charge":
            if time < windup + execution:
                boss.change("dash")
                speed = 370.0 if boss.phase == 2 else 285.0
                self.charge(world, boss, dt, speed, 2)
        elif boss.plan == "slam":
            if not boss.spent:
                boss.spent = True
                x, y = boss.anchor
                radius = 76 if boss.phase == 2 else 62
                world.effects.append((1, x, y, 0.0, 0.42, radius * 2 + 10))
                self.burst(world, x, y, RED, 22)
                self.audio.sound("boss_slam")
                p = world.player
                if (Collision.checkCircle(Vec2(x, y), radius, Vec2(p.x, p.y), p.radius)
                        and world.sight(x, y, p.x, p.y)):
                    self.hurt(world, p, 2)
        else:
            if not boss.spent:
                boss.spent = True
                self.volley(world, boss, boss.aim)
                self.audio.sound("cast")
            if boss.phase == 2 and time >= windup + 0.21 and boss.cooldown == 0.0:
                boss.cooldown = 1.0
                p = world.player
                aim = unit(p.x - boss.x, p.y - boss.y)
                self.volley(world, boss, aim if aim != (0.0, 0.0) else boss.aim)
        if time >= windup + execution:
            boss.recover(0.36 if boss.phase == 2 else 0.69)

    def boss_step(self, world, boss, dx, dy, distance, visible, dt):
        p = world.player
        if boss.state == "phase":
            if boss.age >= 0.64:
                boss.recover(0.12)
            return
        if boss.plan is not None:
            self.boss_plan(world, boss, dt)
            return
        if boss.state == "hit" and boss.age < 0.15:
            return
        if visible and distance < 360 and boss.cooldown == 0.0:
            pattern = (("volley", "slam", "charge") if boss.phase == 1 else
                       ("charge", "volley", "slam", "charge", "slam", "volley"))
            plan = pattern[boss.pattern % len(pattern)]
            boss.pattern += 1
            aim = unit(dx, dy) if distance > 0.0 else FACING[boss.facing]
            boss.start_plan(plan, aim, (p.x, p.y))
            self.audio.sound("boss_warn")
        elif visible and distance > 75:
            multiplier = 1.38 if boss.phase == 2 else 1.0
            self.walk_toward(world, boss, (p.x, p.y), dt, multiplier)
        else:
            boss.face(dx, dy)
            boss.change("idle")

    def enemy_step(self, world, enemy, dt):
        enemy.tick(dt)
        p = world.player
        if enemy.hp <= 0 or p.hp <= 0:
            return
        dx, dy = p.x - enemy.x, p.y - enemy.y
        distance = length(dx, dy)
        visible = distance < 365 and world.sight(enemy.x, enemy.y, p.x, p.y)
        if enemy.plan is not None:
            enemy.plan_age += dt
        if enemy.kind == "boss":
            self.boss_step(world, enemy, dx, dy, distance, visible, dt)
            return
        if enemy.state == "hit" and enemy.age < 0.24:
            return
        if enemy.plan is not None:
            self.regular_plan(world, enemy, dt)
            return

        reach = {"hunter": 27, "guard": 47, "runner": 133, "obelisk": 259}[enemy.kind]
        if visible and distance < reach and enemy.cooldown == 0.0:
            aim = unit(dx, dy) if distance > 0.0 else FACING[enemy.facing]
            enemy.start_plan(enemy.kind, aim, (p.x, p.y))
        elif enemy.kind == "obelisk":
            enemy.face(dx, dy)
            enemy.change("idle")
        elif distance > enemy.radius + p.radius - 3:
            self.walk_toward(world, enemy, world.chase_target(enemy), dt)
        else:
            enemy.change("idle")

    def projectiles(self, world, dt):
        p = world.player
        bounds = p.bounds()
        remaining = []
        for x, y, vx, vy, life, boss in world.bolts:
            life -= dt
            radius = 6 if boss else 4
            if life <= 0.0:
                continue
            steps = max(1, int(max(abs(vx), abs(vy)) * dt / 5.0) + 1)
            active = True
            for unused in range(steps):
                x += vx * dt / steps
                y += vy * dt / steps
                if not world.free(x, y, radius):
                    active = False
                    break
                if p.hp > 0 and Collision.checkAABB(rect_at(x, y, radius), bounds):
                    self.hurt(world, p, 1)
                    active = False
                    break
            if active:
                remaining.append((x, y, vx, vy, life, boss))
        world.bolts = remaining

    def collect(self, world):
        p = world.player
        bounds = p.bounds()
        remaining = []
        for kind, x, y in world.items:
            if ((kind == "heal" and p.hp == HP_MAX) or
                    not Collision.checkAABB(bounds, rect_at(x, y, 9))):
                remaining.append((kind, x, y))
            elif kind == "echo":
                world.echoes -= 1
                world.score += 250
                self.audio.sound("echo")
                self.burst(world, x, y, GOLD, 15)
                if world.echoes == 0:
                    message = "Tor offen" if world.open_exit() else "Besiege den Abyss Warden."
                    world.message_for(message)
            else:
                p.hp = min(HP_MAX, p.hp + 1)
                world.heal_glow = 0.62
                world.effects.append((2, x, y, 0.0, 0.45, 68))
                world.message_for("+1 Gesundheit")
                self.audio.sound("heal")
                self.burst(world, x, y, GREEN, 16)
        world.items = remaining

    def step(self, world, dt, movement, melee, dash, wave, aim):
        world.elapsed += dt
        world.heal_glow = max(0.0, world.heal_glow - dt)
        world.message_time = max(0.0, world.message_time - dt)
        self.player_step(world, dt, movement, melee, dash, wave, aim)
        world.fx.update(dt)
        world.effects = [(row, x, y, age + dt, life, size)
                         for row, x, y, age, life, size in world.effects if age + dt < life]
        if world.player.hp > 0:
            world.update_flow()
        for enemy in world.enemies:
            self.enemy_step(world, enemy, dt)
        world.enemies = [enemy for enemy in world.enemies
                         if enemy.hp > 0 or enemy.age < 0.56 or enemy.kind == "boss"]
        if world.player.hp > 0:
            self.projectiles(world, dt)
        if world.player.hp > 0:
            self.collect(world)
        world.update_camera(dt)

    def warnings(self, world):
        result = []
        for enemy in world.enemies:
            if enemy.hp <= 0 or enemy.plan is None:
                continue
            x, y = enemy.x, enemy.y
            dx, dy = enemy.aim
            if enemy.kind == "boss":
                windup, unused = self.boss_timing(enemy)
                if enemy.plan_age >= windup:
                    continue
                if enemy.plan == "slam":
                    cx, cy = enemy.anchor
                    result.append(("circle", cx, cy, 76 if enemy.phase == 2 else 62, 0))
                else:
                    reach = 164 if enemy.plan == "charge" else 106
                    result.append(("line", x, y, x + dx * reach, y + dy * reach))
            elif enemy.plan == "guard" and enemy.plan_age < 0.47:
                result.append(("circle", x + dx * 22, y + dy * 22, 22, 0))
            elif enemy.plan == "runner" and enemy.plan_age < 0.22:
                result.append(("line", x, y, x + dx * 80, y + dy * 80))
        return tuple(result)


class Menu(object):
    def __init__(self):
        self.mode, self.selected = "main", 0
        self.return_mode, self.return_index = "main", 0
        self.capture = False
        self.press_index, self.hover = None, -1
        self.last_mouse = None
        self.wait_release = False
        self.age = 0.0
        self.status = ""

    def open(self, mode, mouse_held=False):
        self.mode, self.selected = mode, 0
        self.capture = False
        self.press_index, self.hover = None, -1
        self.wait_release = mouse_held
        self.age = 0.0

    def entries(self, profile):
        if self.mode == "main":
            resume = "Checkpoint fortsetzen" if profile.has_run else "Checkpoint fortsetzen  /  LEER"
            return ("Neue Kampagne", resume, "Kapitelauswahl", "Einstellungen", "Beenden")
        if self.mode == "chapters":
            return tuple("%d  %s%s" % (i + 1, title, "" if i <= profile.unlocked else "  /  ZU")
                         for i, title in enumerate(TITLES)) + ("Zurueck",)
        if self.mode == "pause":
            return ("Fortsetzen", "Checkpoint neu starten", "Einstellungen", "Hauptmenue")
        if self.mode == "over":
            return ("Checkpoint neu starten", "Hauptmenue")
        if self.mode == "victory":
            return ("Kapitelauswahl", "Hauptmenue")
        if self.mode == "settings":
            labels = ["Musik  %3d%%" % (profile.music * 10),
                      "Effekte  %3d%%" % (profile.effects * 10)]
            for action, key in zip(ACTIONS[:4], movement_bindings(profile)):
                labels.append("%-14s %s" % (action, key_label(key)))
            labels.extend(("Nahkampf       LINKSKLICK", "Dash           RECHTSKLICK",
                           "Energiewelle   E"))
            return tuple(labels + ["Standardsteuerung", "Zurueck"])
        return ()

    def row(self, index):
        if self.mode == "settings":
            return 157 + index * 28, 26
        if self.mode == "chapters":
            return 168 + index * 37, 34
        return 186 + index * 46, 40

    def mouse_step(self, keys, count, dt):
        self.age += dt
        self.hover = -1
        if self.wait_release:
            if not keys.isMouseDown(LEFT_MOUSE):
                self.wait_release = False
            return None
        if not keys.isMouseInsideViewport():
            self.last_mouse = None
            self.press_index = None
            return None

        x, y = keys.getMouseX(), keys.getMouseY()
        moved = self.last_mouse != (x, y)
        self.last_mouse = (x, y)
        for index in range(count):
            top, height = self.row(index)
            if 220 <= x < 580 and top <= y < top + height:
                self.hover = index
                break
        if moved and self.hover >= 0 and not self.capture:
            self.selected = self.hover
        if keys.isMousePressed(LEFT_MOUSE):
            self.press_index = self.hover
            if self.hover >= 0 and not self.capture:
                self.selected = self.hover
        if keys.isMouseGestureReleased(LEFT_MOUSE):
            original = self.press_index
            self.press_index = None
            if original is not None and original >= 0 and original == self.hover:
                return original
        if not keys.isMouseDown(LEFT_MOUSE):
            self.press_index = None
        return None


class LunarDepths(IGameLogic):
    def init(self, engine):
        self.engine, self.keys = engine, engine.getInputManager()
        self.profile = Profile()
        if Fonts.loadFont(FONT, FONT_PATH) is None:
            raise RuntimeError("Missing or invalid custom font: " + FONT_PATH)
        self.art = Assets(engine.getResourceManager())
        self.audio = Audio(engine.getAudioManager(), self.profile)
        self.combat = Combat(self.audio)
        self.camera = Camera2D()
        self.aim_camera = Camera2D()
        self.aim_camera.setViewport(W, H)
        self.presented_camera = None
        self.mouse_guard = True
        self.show_minimap = True
        self.menu = Menu()
        self.world = None
        self.debug = False
        self.clock, self.final_score = 0.0, 0
        self.chapter_only = False
        self.audio.music("menu")
        self.publish()

    def pressed(self, key):
        return self.keys.isKeyPressed(key)

    def open(self, mode):
        self.menu.open(mode, self.keys.isMouseDown(LEFT_MOUSE))
        self.mouse_guard = True

    def save(self, message="Gespeichert."):
        saved = self.profile.save()
        self.menu.status = message if saved else self.profile.notice
        return saved

    def begin(self, stage, score):
        self.presented_camera = None
        self.world = Dungeon(self.art, stage, score)
        self.menu.status = ""
        self.open("playing")
        self.audio.music("warden" if stage == 5 else "depths")

    def start_campaign(self):
        self.chapter_only = False
        p = self.profile
        p.stage, p.checkpoint, p.has_run = 0, 0, True
        self.begin(0, 0)
        self.save("Kampagne begonnen.")

    def checkpoint(self):
        if self.chapter_only:
            self.begin(self.world.stage, 0)
        else:
            self.begin(self.profile.stage, self.profile.checkpoint)

    def to_main(self):
        self.world = None
        self.menu.status = ""
        self.open("main")
        self.audio.music("menu")

    def settings(self):
        m = self.menu
        m.return_mode, m.return_index = m.mode, m.selected
        m.status = ""
        self.open("settings")

    def leave_settings(self):
        mode, index = self.menu.return_mode, self.menu.return_index
        self.menu.status = ""
        self.open(mode)
        self.menu.selected = index

    def set_volume(self, index, value, preview):
        p = self.profile
        value = clamp(value, 0, 10)
        if index == 0 and value != p.music:
            p.music = value
            self.audio.apply_volume()
            self.save()
        elif index == 1 and value != p.effects:
            p.effects = value
            if preview:
                self.audio.sound("echo")
            self.save()

    def capture(self, clicked):
        m, p = self.menu, self.profile
        if self.pressed(Key.ESCAPE) or clicked == 10:
            m.capture = False
            m.status = "Tastenwahl abgebrochen."
            return
        for name in KEY_NAMES:
            if not self.pressed(KEYS[name]):
                continue
            index = m.selected - 2
            effective = movement_bindings(p)
            if name in RESERVED_MOVEMENT:
                m.status = "E und M sind reserviert."
            elif ((name in effective and effective.index(name) != index)
                  or (name in p.bindings[:4] and p.bindings.index(name) != index)):
                m.status = "Bewegungstaste bereits belegt."
            else:
                p.bindings[index] = name
                m.capture = False
                self.save()
            return

    def menu_action(self, index, clicked):
        mode = self.menu.mode
        p = self.profile
        if mode == "settings":
            if index < 2:
                if clicked:
                    x = self.keys.getMouseX()
                    self.set_volume(index, int(round((x - 400) / 16.0)), True)
                else:
                    self.menu.status = "Lautstaerke mit LINKS und RECHTS einstellen."
            elif index < 6:
                self.menu.selected = index
                self.menu.capture = True
                self.menu.status = "Neue Taste druecken. ESC bricht ab."
            elif index < 9:
                self.menu.status = "Diese Spielaktion ist fest belegt."
            elif index == 9:
                p.bindings[:4] = DEFAULTS[:4]
                self.save("Standardsteuerung gespeichert.")
            else:
                self.leave_settings()
        elif mode == "main":
            if index == 0:
                self.start_campaign()
            elif index == 1:
                if p.has_run:
                    self.chapter_only = False
                    self.checkpoint()
                else:
                    self.menu.status = "Es ist noch kein Checkpoint vorhanden."
            elif index == 2:
                self.menu.status = ""
                self.open("chapters")
            elif index == 3:
                self.settings()
            else:
                self.audio.music(None)
                self.engine.stop()
        elif mode == "chapters":
            if index == 6:
                self.to_main()
            elif index <= p.unlocked:
                self.chapter_only = True
                self.begin(index, 0)
            else:
                self.menu.status = "Dieses Kapitel ist noch verschlossen."
        elif mode == "pause":
            if index == 0:
                self.open("playing")
            elif index == 1:
                self.checkpoint()
            elif index == 2:
                self.settings()
            else:
                self.to_main()
        elif mode == "over":
            if index == 0:
                self.checkpoint()
            else:
                self.to_main()
        elif mode == "victory":
            self.to_main()
            if index == 0:
                self.open("chapters")

    def menu_step(self, dt):
        m = self.menu
        entries = m.entries(self.profile)
        clicked = m.mouse_step(self.keys, len(entries), dt)
        if m.capture:
            self.capture(clicked)
            return
        if self.pressed(Key.ESCAPE):
            if m.mode == "settings":
                self.leave_settings()
            elif m.mode == "pause":
                self.open("playing")
            elif m.mode in ("over", "victory", "chapters"):
                self.to_main()
            return

        if self.pressed(Key.UP):
            m.selected = (m.selected - 1) % len(entries)
        elif self.pressed(Key.DOWN):
            m.selected = (m.selected + 1) % len(entries)
        if m.mode == "settings" and m.selected < 2:
            old = self.profile.music if m.selected == 0 else self.profile.effects
            if self.pressed(Key.LEFT):
                self.set_volume(m.selected, old - 1, True)
            elif self.pressed(Key.RIGHT):
                self.set_volume(m.selected, old + 1, True)
        if clicked is not None:
            m.selected = clicked
            self.audio.sound("menu_click")
            self.menu_action(clicked, True)
        elif self.pressed(Key.ENTER):
            self.audio.sound("menu_click")
            self.menu_action(m.selected, False)

    def finish(self, victory):
        world = self.world
        self.final_score = world.score
        self.profile.best = max(self.profile.best, world.score)
        if victory and not self.chapter_only:
            self.profile.has_run = False
            self.profile.completed = True
        self.open("victory" if victory else "over")
        self.save()
        self.audio.music("menu")
        if victory:
            self.audio.sound("victory")

    def advance(self):
        world, p = self.world, self.profile
        world.score += 500
        self.audio.sound("gate")
        if world.stage == 5:
            self.finish(True)
            return

        stage, score = world.stage + 1, world.score
        p.best = max(p.best, score)
        p.unlocked = max(p.unlocked, stage)
        if self.chapter_only:
            self.final_score = score
            self.world = None
            self.open("chapters")
            self.menu.selected = stage
            self.audio.music("menu")
            self.save("Kapitel abgeschlossen.")
        else:
            p.stage, p.checkpoint, p.has_run = stage, score, True
            self.begin(stage, score)
            self.save("Checkpoint gespeichert.")
            self.world.message_for(self.menu.status, 2.8)

    def play_step(self, dt):
        world = self.world
        if self.pressed(Key.ESCAPE):
            self.open("pause")
            return

        self.menu.age += dt
        up, down, left, right = [KEYS[name] for name in movement_bindings(self.profile)]
        x = int(self.keys.isKeyDown(right)) - int(self.keys.isKeyDown(left))
        y = int(self.keys.isKeyDown(down)) - int(self.keys.isKeyDown(up))
        if self.pressed(Key.M):
            self.show_minimap = not self.show_minimap

        melee, dash_click = False, False
        aim = FACING[world.player.facing]
        if self.mouse_guard:
            if not self.keys.isMouseDown(LEFT_MOUSE) and not self.keys.isMouseDown(RIGHT_MOUSE):
                self.mouse_guard = False
        elif self.keys.isMouseInsideViewport():
            melee = self.keys.isMousePressed(LEFT_MOUSE)
            dash_click = self.keys.isMousePressed(RIGHT_MOUSE)
            presented = self.presented_camera
            if melee or dash_click:
                if presented is not None and presented[0] is world.map:
                    self.aim_camera.snapTo(presented[1], presented[2])
                    target = self.aim_camera.screenToWorld(
                        self.keys.getMouseX(), self.keys.getMouseY()
                    )
                    aim = unit(target.x - world.player.x, target.y - world.player.y)
                    if aim == (0.0, 0.0):
                        aim = FACING[world.player.facing]
                else:
                    melee, dash_click = False, False

        self.combat.step(world, dt, (x, y), melee, dash_click, self.pressed(Key.E), aim)
        if world.player.hp <= 0:
            if world.player.age >= 0.56:
                self.finish(False)
        elif world.open_exit() and Collision.checkAABB(world.player.bounds(), world.exit):
            self.advance()

    def update(self, dt):
        self.clock += dt
        if self.pressed(Key.F3):
            self.debug = not self.debug
        if self.menu.mode == "playing":
            self.play_step(dt)
        else:
            self.menu_step(dt)
        self.publish()

    def world_snapshot(self, world):
        actors = []
        for fighter in sorted([world.player] + world.enemies, key=lambda f: f.y):
            if fighter.kind == "boss" and fighter.hp == 0 and fighter.age >= 0.56:
                continue
            if (fighter.kind == "keeper" and fighter.invulnerable > 0.0
                    and fighter.state in ("idle", "walk")
                    and (int(world.elapsed * 18) % 2 == 1)):
                image = None
            else:
                image = self.art.fighter_frame(fighter)
            actors.append((image, fighter.kind, fighter.x, fighter.y, fighter.bounds(),
                           fighter.hp, fighter.maximum, fighter.state, fighter.plan))

        particles = []
        for particle in world.fx.getParticles():
            if particle.alive and particle.maxLife > 0.0:
                alpha = 255 * particle.life / particle.maxLife
                particles.append((int(particle.position.x), int(particle.position.y),
                                  max(1, int(particle.size)),
                                  color_alpha(particle.color, alpha)))

        p, boss = world.player, world.boss
        boss_bar = None
        if boss is not None and (boss.pattern > 0 or boss.phase == 2) and boss.hp > 0:
            boss_bar = (boss.visual_hp, boss.trail_hp, boss.phase, boss.hp)

        return {
            "map": world.map, "walls": world.walls, "size": (world.cols, world.rows),
            "stage": world.stage, "camera": (world.camera_x, world.camera_y),
            "exit": world.exit_xy, "exit_rect": world.exit, "open": world.open_exit(),
            "items": tuple(world.items), "bolts": tuple(world.bolts),
            "actors": tuple(actors), "effects": tuple(world.effects),
            "warnings": self.combat.warnings(world), "particles": tuple(particles),
            "player": (p.x, p.y, p.hp, p.visual_hp, p.trail_hp, p.state),
            "echoes": (world.echoes, world.total_echoes), "score": world.score,
            "boss": boss_bar, "heal": world.heal_glow,
            "cds": (world.melee_cd / ATTACK_CD, world.dash_cd / DASH_CD,
                    world.wave_cd / WAVE_CD),
            "message": world.message if world.message_time > 0.0 else "",
            "attack": world.player_attack, "minimap_visible": self.show_minimap
        }

    def publish(self):
        m, p = self.menu, self.profile
        entries = m.entries(p)
        rows = tuple(m.row(i) for i in range(len(entries)))
        world = self.world_snapshot(self.world) if self.world is not None else None
        self.frame = (
            world, m.mode, entries, rows, m.selected, m.hover, m.press_index, m.age,
            m.capture, m.status or p.notice, p.best, p.unlocked, self.final_score,
            (p.music, p.effects), self.clock, self.debug,
            (self.engine.getRenderedFps(), self.engine.getLastLoopFrameTimeSeconds(),
             self.engine.getLastRenderFrameTimeSeconds())
        )

    def draw_sprite(self, g, image, x, y, size):
        g.drawImage(image, int(round(x)) - size // 2, int(round(y)) - size // 2)

    def draw_warning(self, g, warning, debug):
        style, x, y, a, b = warning
        if style == "circle":
            g.setColor(WARN_FILL)
            g.fillCircle(int(x), int(y), a)
            g.setColor(GOLD if debug else RED)
            g.drawCircle(int(x), int(y), a)
            g.drawCircle(int(x), int(y), a - 3)
        else:
            g.setColor(GOLD if debug else RED)
            g.drawLine(int(x), int(y), int(a), int(b))
            g.drawLine(int(x) + 1, int(y), int(a) + 1, int(b))
            g.drawCircle(int(a), int(b), 6)

    def draw_world(self, g, data, debug):
        self.camera.snapTo(*data["camera"])
        g.applyCamera(self.camera)
        try:
            data["map"].draw(g, self.camera)
            ex, ey = data["exit"]
            self.draw_sprite(g, self.art.items[3 if data["open"] else 2], ex, ey, 32)
            for kind, x, y in data["items"]:
                self.draw_sprite(g, self.art.items[0 if kind == "echo" else 1], x, y, 32)
            for warning in data["warnings"]:
                self.draw_warning(g, warning, debug)
            for image, kind, x, y, bounds, hp, maximum, state, plan in data["actors"]:
                if image is not None:
                    self.draw_sprite(g, image, x, y, 80 if kind == "boss" else 32)
                if kind not in ("keeper", "boss") and hp > 0:
                    g.setColor(DARK)
                    g.fillRect(int(x) - 11, int(y) - 18, 22, 3)
                    g.setColor(RED)
                    g.fillRect(int(x) - 11, int(y) - 18, hp * 22 // maximum, 3)
                if debug:
                    DebugDraw.drawRect(g, bounds, CYAN if kind == "keeper" else RED)
                    if kind != "keeper":
                        g.setColor(WHITE)
                        g.drawString("%s %d %s" % (kind, hp, plan or state),
                                     int(x) - 23, int(y) - 23, 10, MONO)
            for x, y, vx, vy, life, boss in data["bolts"]:
                self.draw_sprite(g, self.art.items[5 if boss else 4], x, y, 32)
                if debug:
                    DebugDraw.drawRect(g, rect_at(x, y, 6 if boss else 4), VIOLET)
            for row, x, y, age, life, size in data["effects"]:
                image = self.art.effects[row][min(3, int(4 * age / life))]
                g.drawImage(image, int(x) - size // 2, int(y) - size // 2, size, size)
            for x, y, size, color in data["particles"]:
                g.setColor(color)
                g.fillRect(x, y, size, size)
            if debug:
                self.draw_attack_debug(g, data)
        finally:
            g.resetCamera()

    def draw_attack_debug(self, g, data):
        DebugDraw.drawRect(g, data["exit_rect"], GOLD)
        if data["attack"] is not None:
            ax, ay, dx, dy = data["attack"]
            corners = []
            for forward, side in ((20, 16), (-20, 16), (-20, -16), (20, -16)):
                corners.append((int(ax + forward * dx - side * dy),
                                int(ay + forward * dy + side * dx)))
            g.setColor(GOLD)
            for i in range(4):
                x0, y0 = corners[i]
                x1, y1 = corners[(i + 1) % 4]
                g.drawLine(x0, y0, x1, y1)
        for row, x, y, age, life, size in data["effects"]:
            if row == 0:
                g.setColor(GOLD)
                g.drawCircle(int(x), int(y), 78)

    def bar(self, g, x, y, width, height, shown, trail, maximum, color):
        g.setColor(color_alpha(INK, 190))
        g.fillRect(x, y, width, height)
        g.setColor(GOLD)
        g.fillRect(x, y, int(width * clamp(trail / maximum, 0.0, 1.0)), height)
        g.setColor(color)
        g.fillRect(x, y, int(width * clamp(shown / maximum, 0.0, 1.0)), height)
        g.setColor(SLATE)
        g.drawRect(x, y, width, height)

    def minimap(self, g, data):
        cols, rows = data["size"]
        scale = 2
        left = 676 + (112 - cols * scale) // 2
        top = 12 + (68 - rows * scale) // 2
        g.setColor(color_alpha(INK, 112))
        g.fillRect(left, top, cols * scale, rows * scale)
        g.setColor(color_alpha(MUTED, 135))
        for col, row in data["walls"]:
            g.fillRect(left + col * scale, top + row * scale, scale, scale)
        g.setColor(GOLD)
        for kind, x, y in data["items"]:
            if kind == "echo":
                g.fillRect(left + int(x // CELL) * scale, top + int(y // CELL) * scale, 3, 3)
        ex, ey = data["exit"]
        g.setColor(GREEN if data["open"] else VIOLET)
        g.fillRect(left + int(ex // CELL) * scale, top + int(ey // CELL) * scale, 3, 3)
        x, y = data["player"][:2]
        g.setColor(CYAN)
        g.fillRect(left + int(x // CELL) * scale - 1, top + int(y // CELL) * scale - 1, 5, 5)

    def hud(self, g, data, debug, performance, notice):
        g.setColor(color_alpha(INK, 145))
        g.fillRect(12, 12, 252, 67)
        g.setColor(CYAN)
        g.drawString(TITLES[data["stage"]], 20, 29, 14, SANS)
        x, y, hp, shown, trail, state = data["player"]
        health_color = GREEN if data["heal"] > 0.0 else RED
        self.bar(g, 20, 36, 184, 8, shown, trail, HP_MAX, health_color)
        g.setColor(WHITE)
        g.drawString("%d/%d" % (hp, HP_MAX), 215, 45, 12, MONO)
        remaining, total = data["echoes"]
        g.drawString("ECHO %d/%d    PUNKTE %d" % (total - remaining, total, data["score"]),
                     20, 60, 12, MONO)
        g.setColor(GREEN if data["open"] else MUTED)
        g.drawString("TOR OFFEN" if data["open"] else "TOR VERSCHLOSSEN", 20, 74, 10, MONO)
        if data["minimap_visible"]:
            self.minimap(g, data)

        for index, cooldown in enumerate(data["cds"]):
            left, top = 325 + index * 50, 502
            g.drawImage(self.art.icons[index], left, top)
            if cooldown > 0.0:
                height = max(1, int(36 * min(1.0, cooldown)))
                g.setColor(COOLDOWN_SHADE)
                g.fillRect(left + 2, top + 38 - height, 36, height)
            centered_text(g, ("LMB", "RMB", "E")[index], left + 20, 553, 10, WHITE)

        boss = data["boss"]
        if boss is not None:
            shown, trail, phase, hp = boss
            g.setColor(color_alpha(INK, 150))
            g.fillRect(274, 12, 392, 45)
            centered_text(g, "THE ABYSS WARDEN  /  PHASE %s" %
                          ("II" if phase == 2 else "I"), 470, 32, 14, WHITE)
            self.bar(g, 289, 39, 362, 8, shown, trail, BOSS_HP, VIOLET)

        message = data["message"] or notice
        if message != "":
            g.setColor(SHADE)
            g.fillRect(175, 462, 450, 25)
            centered_text(g, message, 400, 480, 13, GOLD)

        if debug:
            fps, loop, render_time = performance
            cx, cy = data["camera"]
            cols, rows = data["size"]
            g.setColor(DARK)
            g.fillRect(14, 128, 524, 54)
            g.setColor(CYAN)
            g.drawString("FPS %.1f  LOOP %.1f ms  RENDER %.1f ms  FX %d" %
                         (fps, loop * 1000, render_time * 1000, len(data["particles"])),
                         23, 148, 11, MONO)
            g.drawString("P %d,%d HP %d %s  CAM %d,%d  L%d %dx%d" %
                         (x, y, hp, state, cx, cy, data["stage"] + 1, cols, rows),
                         23, 167, 11, MONO)

    def menu_background(self, g, clock):
        g.drawImage(self.art.ui["title"], 0, 0)
        for i in range(17):
            x = (44 + i * 97 + int(clock * (12 + i % 4 * 4))) % (W + 24) - 12
            y = (32 + i * 73 - int(clock * (9 + i % 3 * 4))) % (H + 24) - 12
            g.drawImage(self.art.motes[(int(clock * 5) + i) % 4], x, y)

    def draw_menu(self, g, mode, entries, rows, selected, hover, press, age,
                  capture, notice, best, unlocked, score, volumes):
        g.setColor(color_alpha(SHADE, min(153, 50 + age * 600)))
        g.fillRect(0, 0, W, H)
        g.drawImage(self.art.ui["panel"], 170, 65)
        titles = {
            "pause": "PAUSE", "settings": "EINSTELLUNGEN",
            "over": "DIE TIEFE HAT DICH", "victory": "DER ABGRUND VERSTUMMT",
            "chapters": "KAPITELAUSWAHL"
        }
        if mode == "main":
            centered_text(g, "LUNAR DEPTHS", 400, 122, 33, CYAN)
            centered_text(g, "ENHANCED EDITION", 400, 146, 14, GOLD)
            centered_text(g, "BESTLEISTUNG %d  /  %d VON 6 KAPITELN FREI" %
                          (best, unlocked + 1), 400, 172, 12, MUTED)
        else:
            centered_text(g, titles[mode], 400, 120, 25, CYAN)
            if mode in ("over", "victory"):
                centered_text(g, "PUNKTE %d  /  BESTLEISTUNG %d" % (score, best),
                              400, 152, 13, GOLD)
            elif mode == "settings":
                centered_text(g, "MAUS ODER PFEILTASTEN  /  ENTER  /  ESC",
                              400, 141, 11, MUTED)

        for index, label in enumerate(entries):
            y, height = rows[index]
            highlighted = index == selected or index == hover
            skin = 2 if index == press and index == hover else (1 if highlighted else 0)
            g.drawImage(self.art.buttons[skin][0], 220, y, 360, height)
            g.setColor(CYAN if highlighted else WHITE)
            g.drawString(label, 233, y + height // 2 + 5, 13, MONO)
            if mode == "settings" and index < 2:
                left = 400
                g.setColor(INK)
                g.fillRect(left, y + 10, 160, 7)
                g.setColor(CYAN)
                g.fillRect(left, y + 10, 16 * volumes[index], 7)
                g.setColor(WHITE)
                g.fillRect(left + 16 * volumes[index] - 2, y + 7, 4, 13)
        if notice != "":
            centered_text(g, notice, 400, 477, 12, GOLD)
        if capture:
            g.setColor(SHADE)
            g.fillRect(221, 185, 358, 253)
            centered_text(g, "TASTE DRUECKEN", 400, 291, 22, CYAN)
            centered_text(g, "ESC oder Zurueck bricht ab", 400, 322, 13, WHITE)

    def render(self, g):
        (world, mode, entries, rows, selected, hover, press, age,
         capture, notice, best, unlocked, score, volumes, clock, debug, performance) = self.frame
        g.setColor(INK)
        g.fillRect(0, 0, W, H)
        if world is None:
            self.menu_background(g, clock)
        else:
            self.draw_world(g, world, debug)
            self.hud(g, world, debug, performance, notice)
            if mode == "playing":
                self.presented_camera = (world["map"], world["camera"][0], world["camera"][1])

        if mode != "playing":
            self.draw_menu(g, mode, entries, rows, selected, hover, press, age,
                           capture, notice, best, unlocked, score, volumes)
            if debug and world is None:
                g.setColor(CYAN)
                g.drawString("FPS %.1f  LOOP %.1f ms  RENDER %.1f ms" %
                             (performance[0], performance[1] * 1000, performance[2] * 1000),
                             14, 548, 11, MONO)


engine = LunarEngine(W, H, "Lunar Depths V2 - Enhanced Edition")
engine.enableResizableViewport()
engine.setGameLogic(LunarDepths())
engine.start()