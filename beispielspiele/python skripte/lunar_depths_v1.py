from LunarAxis.core import LunarEngine
from LunarAxis.core.manager import IGameLogic
from LunarAxis.inputs import Key
from LunarAxis.graphics import Animation, Camera2D, Colors
from LunarAxis.graphics.debug import DebugDraw
from LunarAxis.graphics.particle import ParticleBuilder, ParticleSystem
from LunarAxis.grid import Grid, Tile, TileMap
from LunarAxis.math import Rect
from LunarAxis.objects import Actor
from LunarAxis.physics import Collision
from LunarAxis.util import LunarRandom, SaveManager
from LunarAxis.diagnostics import Logger, LunarException

ROOT = "games/lunar-depths_v1/"
W, H, CELL = 800, 560, 32
MAX_HP = 5
TITLES = ("Versunkene Vorhalle", "Kristallzisterne", "Kern des Abgrunds")
ACTIONS = ("Hoch", "Runter", "Links", "Rechts", "Angriff")
DEFAULT_KEYS = (Key.W, Key.S, Key.A, Key.D, Key.SPACE)
KEY_NAMES = ("A B C D E F G H I J K L M N O P Q R S T U V W X Y Z "
             "UP DOWN LEFT RIGHT SPACE SHIFT CTRL "
             "DIGIT0 DIGIT1 DIGIT2 DIGIT3 DIGIT4 DIGIT5 DIGIT6 DIGIT7 DIGIT8 DIGIT9").split()
BINDABLE = tuple(getattr(Key, name) for name in KEY_NAMES)
KEY_BY_NAME = dict((str(key), key) for key in BINDABLE)
SOUNDS = ("swing", "hit", "hurt", "echo", "gate", "cast")
NEIGHBORS = ((1, 0), (-1, 0), (0, 1), (0, -1))
STATES = ("idle", "walk", "attack", "hit", "death")
FRAME_TIME = (0.16, 0.09, 0.085, 0.065, 0.13)

INK = Colors(6, 10, 23)
PANEL = Colors(13, 23, 43)
SHADE = Colors(5, 9, 20, 196)
WHITE = Colors(230, 245, 255)
MUTED = Colors(150, 180, 204)
CYAN = Colors(71, 239, 227)
GOLD = Colors(255, 209, 104)
RED = Colors(255, 95, 133)
LILAC = Colors(190, 131, 255)
WALL_MAP = Colors(43, 62, 84)
FLOOR_MAP = Colors(20, 34, 53)


def clamp(value, lo, hi):
    return max(lo, min(hi, value))


def box(x, y, radius):
    return Rect(x - radius, y - radius, radius * 2, radius * 2)


def magnitude(x, y):
    return (x * x + y * y) ** 0.5


def require_image(resources, path):
    image = resources.loadImage(ROOT + path)
    if image is None:
        raise RuntimeError("Missing Lunar Depths image: " + ROOT + path)
    return image


def require_sheet(resources, path, size, cols, rows):
    sheet = resources.loadSpriteSheet(ROOT + path, size, size)
    if sheet is None or sheet.getColumns() != cols or sheet.getRows() != rows:
        raise RuntimeError("Invalid Lunar Depths sheet: " + ROOT + path)
    return sheet


class Profile(object):
    def __init__(self):
        self.store = SaveManager.getInstance()
        self.directory = "games/lunar-depths_v1/"
        self.filename = "lunar_depths_v1"
        self.path = self.directory + "/" + self.filename + ".json"
        self.notice = ""
        self.read_only = False
        self.defaults()
        self.load_profile()

    def defaults(self):
        self.stage, self.score, self.best, self.unlocked = 0, 0, 0, 0
        self.has_run = False
        self.music, self.effects = 5, 7
        self.binds = list(DEFAULT_KEYS)

    def number(self, name, default, lo, hi):
        return clamp(int(self.store.getString(name, str(default))), lo, hi)

    def log_save_error(self, operation, error):
        causes = []
        cause = error
        while cause is not None:
            causes.append(str(cause.toString()))
            cause = cause.getCause()
        Logger.warn(
            "LunarDepthsV1",
            "%s | %s | %s" % (operation, self.path, " -> ".join(causes)),
            error
        )

    def load_profile(self):
        operation = "SaveManager.setDirectory"
        try:
            self.store.setDirectory(self.directory)
            operation = "SaveManager.load"
            self.store.load(self.filename)

            operation = "read profile schema"
            schema = str(self.store.getString("schema", ""))
            if schema != "1":
                self.read_only = True
                self.notice = "Unbekanntes Speicherformat. Datei bleibt erhalten."
                Logger.warn("LunarDepthsV1", "%s | schema=%s" % (self.path, schema))
                return

            operation = "read profile values"
            stage = self.number("stage", 0, 0, 2)
            score = self.number("checkpoint_score", 0, 0, 9999999)
            best = self.number("best", 0, 0, 9999999)
            unlocked = self.number("unlocked", 0, stage, 2)
            music = self.number("music", 5, 0, 10)
            effects = self.number("effects", 7, 0, 10)
            has_run = self.number("has_run", 0, 0, 1)
            binds = [
                KEY_BY_NAME.get(str(self.store.getString("key%d" % i, "")))
                for i in range(len(ACTIONS))
            ]

            self.stage, self.score, self.best = stage, score, best
            self.unlocked, self.has_run = unlocked, bool(has_run)
            self.music, self.effects = music, effects
            if None not in binds and len(set(binds)) == len(ACTIONS):
                self.binds = binds
            else:
                self.notice = "Standardsteuerung wiederhergestellt."

        except LunarException as error:
            self.notice = "Kein V1-Profil geladen. Details in der Konsole."
            self.log_save_error(operation, error)
        except (TypeError, ValueError) as error:
            self.notice = "Ungueltige Profilwerte. Details in der Konsole."
            Logger.warn("LunarDepthsV1", "%s | %s | %s" %
                        (operation, self.path, str(error)))

    def save(self):
        if self.read_only:
            self.notice = "Speichern gesperrt: unbekanntes Profilformat."
            return False

        values = {
            "schema": "1",
            "stage": str(self.stage),
            "best": str(self.best),
            "checkpoint_score": str(self.score),
            "unlocked": str(self.unlocked),
            "has_run": str(int(self.has_run)),
            "music": str(self.music),
            "effects": str(self.effects)
        }
        for i, key in enumerate(self.binds):
            values["key%d" % i] = str(key)

        operation = "SaveManager.setDirectory"
        try:
            self.store.setDirectory(self.directory)
            operation = "SaveManager.clear"
            self.store.clear()
            for name, value in values.items():
                operation = "SaveManager.set(%s)" % name
                self.store.set(name, value)

            operation = "SaveManager.save"
            self.store.save(self.filename)
            self.notice = ""
            return True

        except LunarException as error:
            self.notice = "Speichern fehlgeschlagen. Details in der Konsole."
            self.log_save_error(operation, error)
        except (TypeError, ValueError) as error:
            self.notice = "Ungueltiger Speicherwert. Details in der Konsole."
            Logger.warn("LunarDepthsV1", "%s | %s | %s" %
                        (operation, self.path, str(error)))
        return False

class Art(object):
    def __init__(self, resources):
        self.title = require_image(resources, "ui/title.png")
        tile_sheet = require_sheet(resources, "tiles/dungeon.png", CELL, 4, 1)
        self.tiles = []
        for i in range(4):
            tile = Tile(i, INK, tile_sheet.getSprite(i, 0))
            tile.setSolid(i == 3)
            self.tiles.append(tile)
        item_sheet = require_sheet(resources, "sprites/items.png", CELL, 4, 1)
        self.items = tuple(item_sheet.getSprite(i, 0) for i in range(4))
        self.animations = {}
        for name in ("keeper", "hunter", "obelisk"):
            sheet = require_sheet(resources, "sprites/" + name + ".png", CELL, 4, 20)
            for facing in range(4):
                for state, label in enumerate(STATES):
                    row = facing * 5 + state
                    frames = [sheet.getSprite(col, row) for col in range(4)]
                    self.animations[(name, facing, label)] = Animation(
                        frames, FRAME_TIME[state], state < 2
                    )

    def frame(self, fighter):
        animation = self.animations[(fighter.kind, fighter.facing, fighter.state)]
        return animation.getFrame(fighter.age)


class Fighter(object):
    def __init__(self, kind, x, y, hp):
        self.kind, self.hp = kind, hp
        self.actor = Actor()
        self.actor.setWidth(20)
        self.actor.setHeight(20)
        self.actor.setHitbox(20, 20, 0, 0)
        self.place(x, y)
        self.facing = 0
        self.state, self.age = "idle", 0.0
        self.cooldown, self.invulnerable = 0.0, 0.0

    def place(self, x, y):
        self.x, self.y = x, y
        self.actor.setX(x - 10)
        self.actor.setY(y - 10)

    def bounds(self):
        return self.actor.getPhysicsBounds()

    def face(self, dx, dy):
        if abs(dx) > abs(dy):
            self.facing = 3 if dx > 0 else 2
        elif dy:
            self.facing = 0 if dy > 0 else 1

    def change(self, state):
        if state != self.state:
            self.state, self.age = state, 0.0

    def tick(self, dt):
        self.age += dt
        self.cooldown = max(0.0, self.cooldown - dt)
        self.invulnerable = max(0.0, self.invulnerable - dt)
        if self.state == "attack" and self.age >= 0.34:
            self.change("idle")
        elif self.state == "hit" and self.age >= 0.26:
            self.change("idle")


class Dungeon(object):
    WALL, SPAWN, HUNTER, OBELISK = 0x25283C, 0x44F4DB, 0xFF5678, 0xBC73FF
    ECHO, EXIT, HEAL = 0xFFD368, 0x63A4FF, 0x74F388
    FLOORS = (0x121B2F, 0x18233A, 0x1F2B44)

    def __init__(self, image, art, stage, score):
        self.cols, self.rows = image.getWidth(), image.getHeight()
        self.width, self.height = self.cols * CELL, self.rows * CELL
        self.stage, self.score = stage, score
        grid = Grid(self.rows, self.cols, CELL)
        self.walls, self.enemies, self.items = set(), [], []
        self.spawn, self.exit, self.exit_xy = None, None, None
        self.echoes, self.bolts = 0, []
        markers = (self.SPAWN, self.HUNTER, self.OBELISK, self.ECHO, self.EXIT, self.HEAL)
        for row in range(self.rows):
            for col in range(self.cols):
                rgb = image.getRGB(col, row) & 0xFFFFFF
                if rgb == self.WALL:
                    self.walls.add((col, row))
                    tile = 3
                elif rgb in self.FLOORS:
                    tile = self.FLOORS.index(rgb)
                elif rgb in markers:
                    tile = 0
                    self.marker(rgb, col, row)
                else:
                    raise RuntimeError("Unknown level RGB %06X" % rgb)
                grid.setTile(row, col, art.tiles[tile])

        if self.spawn is None or self.exit is None or self.echoes == 0:
            raise RuntimeError("Level needs one spawn, one exit and echoes")
        self.map = TileMap(grid)
        self.player = Fighter("keeper", self.spawn[0], self.spawn[1], MAX_HP)
        self.fx = ParticleSystem(96)
        self.flow_origin, self.flow = None, {}
        self.swing_pending = False
        self.camera_x, self.camera_y = self.camera_target()

    def marker(self, rgb, col, row):
        x, y = col * CELL + CELL // 2, row * CELL + CELL // 2
        if rgb == self.SPAWN:
            if self.spawn is not None:
                raise RuntimeError("Duplicate player spawn")
            self.spawn = (x, y)
        elif rgb == self.EXIT:
            if self.exit is not None:
                raise RuntimeError("Duplicate exit")
            self.exit, self.exit_xy = Rect(col * CELL, row * CELL, CELL, CELL), (x, y)
        elif rgb == self.HUNTER:
            self.enemies.append(Fighter("hunter", x, y, 2 + self.stage // 2))
        elif rgb == self.OBELISK:
            self.enemies.append(Fighter("obelisk", x, y, 3))
        else:
            self.items.append((rgb, x, y))
            if rgb == self.ECHO:
                self.echoes += 1

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

    def move(self, fighter, dx, dy):
        x, y = fighter.x, fighter.y
        if self.free(x + dx, y, 10):
            x += dx
        if self.free(x, y + dy, 10):
            y += dy
        fighter.place(x, y)

    def sight(self, x, y, tx, ty):
        dx, dy = tx - x, ty - y
        steps = max(1, int(magnitude(dx, dy) / 6.0) + 1)
        for i in range(1, steps):
            part = i / float(steps)
            if not self.free(x + dx * part, y + dy * part, 2):
                return False
        return True

    def update_flow(self):
        origin = self.cell(self.player.x, self.player.y)
        if origin == self.flow_origin:
            return
        self.flow_origin = origin
        self.flow = {origin: 0}
        queue = [origin]
        index = 0
        while index < len(queue):
            col, row = queue[index]
            index += 1
            for dx, dy in NEIGHBORS:
                cell = (col + dx, row + dy)
                if cell not in self.flow and self.open_cell(*cell):
                    self.flow[cell] = self.flow[(col, row)] + 1
                    queue.append(cell)

    def hunter_target(self, enemy):
        col, row = self.cell(enemy.x, enemy.y)
        here = self.flow.get((col, row), 99999)
        if here == 0:
            return self.player.x, self.player.y
        cx, cy = col * CELL + CELL // 2, row * CELL + CELL // 2
        for dx, dy in NEIGHBORS:
            if self.flow.get((col + dx, row + dy), 99999) < here:
                if dx and abs(enemy.y - cy) > 1:
                    return enemy.x, cy
                if dy and abs(enemy.x - cx) > 1:
                    return cx, enemy.y
                return cx + dx * CELL, cy + dy * CELL
        return enemy.x, enemy.y

    def camera_target(self):
        return (clamp(self.player.x, W / 2.0, self.width - W / 2.0),
                clamp(self.player.y, H / 2.0, self.height - H / 2.0))

    def update_camera(self, dt):
        tx, ty = self.camera_target()
        factor = min(1.0, dt * 8.0)
        self.camera_x += (tx - self.camera_x) * factor
        self.camera_y += (ty - self.camera_y) * factor


class LunarDepths(IGameLogic):
    def init(self, engine):
        self.engine = engine
        self.keys = engine.getInputManager()
        self.random = LunarRandom()
        self.profile = Profile()
        resources = engine.getResourceManager()
        self.art = Art(resources)
        self.levels = tuple(require_image(resources, "levels/%d.png" % (i + 1))
                            for i in range(3))
        self.audio = engine.getAudioManager()
        self.music = self.audio.loadMusic("depths-theme", ROOT + "audio/theme.wav")
        for sound in SOUNDS:
            self.audio.loadSound("depths-" + sound, ROOT + "audio/" + sound + ".wav", 2)
        self.music.setVolume(self.profile.music * 0.08)
        self.camera = Camera2D()
        self.dungeon = None
        self.mode, self.selection = "main", 0
        self.settings_from, self.settings_index = "main", 0
        self.rebinding, self.debug = False, False
        self.status = ""
        self.final_score = 0
        self.publish()

    def pressed(self, key):
        return self.keys.isKeyPressed(key)

    def sound(self, name):
        if self.profile.effects:
            self.audio.playSoundWithPan("depths-" + name, self.profile.effects * 0.08, 0.0)

    def save(self, message="Gespeichert"):
        self.status = message if self.profile.save() else self.profile.notice

    def open_mode(self, mode):
        self.mode, self.selection = mode, 0
        self.rebinding = False

    def begin(self):
        self.audio.stopMusic()
        self.dungeon = Dungeon(self.levels[self.profile.stage], self.art,
                               self.profile.stage, self.profile.score)
        self.music.setVolume(self.profile.music * 0.08)
        self.audio.playMusic("depths-theme", True)
        self.open_mode("playing")

    def main_menu(self):
        self.audio.stopMusic()
        self.dungeon = None
        self.open_mode("main")

    def settings(self):
        self.settings_from, self.settings_index = self.mode, self.selection
        self.status = ""
        self.open_mode("settings")

    def leave_settings(self):
        self.open_mode(self.settings_from)
        self.selection = self.settings_index

    def options(self):
        if self.mode == "main":
            return ("Kampagne starten", "Checkpoint fortsetzen", "Einstellungen", "Beenden")
        if self.mode == "pause":
            return ("Fortsetzen", "Level neu starten", "Einstellungen", "Hauptmenue")
        if self.mode == "over":
            return ("Checkpoint neu starten", "Hauptmenue")
        if self.mode == "victory":
            return ("Hauptmenue",)
        labels = ["Musik         %3d%%" % (self.profile.music * 10),
                  "Effekte       %3d%%" % (self.profile.effects * 10)]
        for action, key in zip(ACTIONS, self.profile.binds):
            labels.append("%-13s %s" % (action, str(key)))
        return tuple(labels + ["Standardsteuerung", "Zurueck"])

    def capture(self):
        if self.pressed(Key.ESCAPE):
            self.rebinding = False
            self.status = "Belegung abgebrochen."
            return
        for key in BINDABLE:
            if self.pressed(key):
                index = self.selection - 2
                if key in self.profile.binds and key != self.profile.binds[index]:
                    other = ACTIONS[self.profile.binds.index(key)]
                    self.status = "Taste belegt durch " + other + ". Andere Taste druecken."
                else:
                    self.profile.binds[index] = key
                    self.rebinding = False
                    self.save()
                return

    def change_volume(self, amount):
        if self.selection == 0:
            self.profile.music = clamp(self.profile.music + amount, 0, 10)
            self.music.setVolume(self.profile.music * 0.08)
        else:
            self.profile.effects = clamp(self.profile.effects + amount, 0, 10)
            self.sound("echo")
        self.save()

    def menu_input(self):
        if self.rebinding:
            self.capture()
            return
        if self.pressed(Key.ESCAPE):
            if self.mode == "settings":
                self.leave_settings()
            elif self.mode == "pause":
                self.open_mode("playing")
            elif self.mode in ("over", "victory"):
                self.main_menu()
            return
        count = len(self.options())
        if self.pressed(Key.UP):
            self.selection = (self.selection - 1) % count
        elif self.pressed(Key.DOWN):
            self.selection = (self.selection + 1) % count

        if self.mode == "settings" and self.selection < 2:
            if self.pressed(Key.LEFT):
                self.change_volume(-1)
            elif self.pressed(Key.RIGHT):
                self.change_volume(1)
        if not self.pressed(Key.ENTER):
            return

        i = self.selection
        if self.mode == "settings":
            if 2 <= i < 7:
                self.rebinding = True
                self.status = "Neue Taste druecken. ESC bricht ab."
            elif i == 7:
                self.profile.binds = list(DEFAULT_KEYS)
                self.save("Standardsteuerung gespeichert.")
            elif i == 8:
                self.leave_settings()
        elif self.mode == "main":
            if i == 0:
                self.profile.stage, self.profile.score, self.profile.has_run = 0, 0, True
                self.save()
                self.begin()
            elif i == 1:
                if self.profile.has_run:
                    self.begin()
                else:
                    self.status = "Noch kein Checkpoint vorhanden."
            elif i == 2:
                self.settings()
            else:
                self.engine.stop()
        elif self.mode == "pause":
            if i == 0:
                self.open_mode("playing")
            elif i == 1:
                self.begin()
            elif i == 2:
                self.settings()
            else:
                self.main_menu()
        elif self.mode == "over" and i == 0:
            self.begin()
        else:
            self.main_menu()

    def burst(self, world, x, y, color, count):
        for unused in range(count):
            vx = self.random.nextDouble(-86.0, 86.0)
            vy = self.random.nextDouble(-86.0, 86.0)
            (ParticleBuilder.create(world.fx).position(x, y).velocity(vx, vy)
             .size(3.0).life(0.36).color(color).emit())

    def hurt(self, world, target):
        if target.hp <= 0 or target.invulnerable > 0.0:
            return
        target.hp -= 1
        target.invulnerable = 0.80 if target.kind == "keeper" else 0.22
        target.change("death" if target.hp == 0 else "hit")
        self.burst(world, target.x, target.y, RED if target.kind == "keeper" else LILAC, 11)
        self.sound("hurt" if target.kind == "keeper" else "hit")
        if target.hp == 0 and target.kind != "keeper":
            world.score += 150 if target.kind == "obelisk" else 100

    def strike(self, world):
        p = world.player
        dx, dy = ((0, 1), (0, -1), (-1, 0), (1, 0))[p.facing]
        x, y = p.x + dx * 24, p.y + dy * 24
        reach = Rect(x - (19 if dx else 15), y - (19 if dy else 15),
                     38 if dx else 30, 38 if dy else 30)
        self.burst(world, x, y, CYAN, 7)
        for enemy in world.enemies:
            if (enemy.hp > 0 and Collision.checkAABB(reach, enemy.bounds())
                    and world.sight(p.x, p.y, enemy.x, enemy.y)):
                self.hurt(world, enemy)

    def player_step(self, world, dt):
        p = world.player
        p.tick(dt)
        if p.hp <= 0:
            return
        if p.state == "attack":
            if world.swing_pending and p.age >= 0.085:
                world.swing_pending = False
                self.strike(world)
            return
        if p.state == "hit":
            world.swing_pending = False
            return
        up, down, left, right, attack = self.profile.binds
        dx = int(self.keys.isKeyDown(right)) - int(self.keys.isKeyDown(left))
        dy = int(self.keys.isKeyDown(down)) - int(self.keys.isKeyDown(up))
        p.face(dx, dy)
        if self.pressed(attack):
            p.change("attack")
            world.swing_pending = True
            self.sound("swing")
            return
        length = magnitude(dx, dy)
        if length:
            world.move(p, dx * 162.0 * dt / length, dy * 162.0 * dt / length)
        p.change("walk" if length else "idle")

    def enemy_step(self, world, enemy, dt):
        enemy.tick(dt)
        p = world.player
        if enemy.hp <= 0 or p.hp <= 0 or enemy.state == "hit":
            return
        dx, dy = p.x - enemy.x, p.y - enemy.y
        distance = magnitude(dx, dy)
        if enemy.kind == "hunter":
            if Collision.checkAABB(enemy.bounds(), p.bounds()):
                enemy.face(dx, dy)
                if enemy.cooldown == 0.0:
                    enemy.change("attack")
                    enemy.cooldown = 0.72
                    self.hurt(world, p)
            elif enemy.state != "attack":
                tx, ty = world.hunter_target(enemy)
                vx, vy = tx - enemy.x, ty - enemy.y
                length = magnitude(vx, vy)
                if length:
                    step = min(length, (69.0 + 13.0 * world.stage) * dt)
                    enemy.face(vx, vy)
                    world.move(enemy, vx * step / length, vy * step / length)
                enemy.change("walk" if length else "idle")
        else:
            enemy.face(dx, dy)
            if (0 < distance < 275 and enemy.cooldown == 0.0
                    and world.sight(enemy.x, enemy.y, p.x, p.y)):
                speed = 145.0 + 18.0 * world.stage
                world.bolts.append((enemy.x, enemy.y, dx * speed / distance,
                                    dy * speed / distance, 2.6))
                enemy.cooldown = 1.8 - world.stage * 0.25
                enemy.change("attack")
                self.sound("cast")

    def projectiles(self, world, dt):
        remaining = []
        player_bounds = world.player.bounds()
        for x, y, vx, vy, life in world.bolts:
            x, y, life = x + vx * dt, y + vy * dt, life - dt
            if life <= 0.0 or not world.free(x, y, 4):
                continue
            if Collision.checkAABB(box(x, y, 4), player_bounds):
                self.hurt(world, world.player)
            else:
                remaining.append((x, y, vx, vy, life))
        world.bolts = remaining

    def collect(self, world):
        p = world.player
        bounds = p.bounds()
        remaining = []
        for kind, x, y in world.items:
            if (kind == Dungeon.HEAL and p.hp == MAX_HP
                    or not Collision.checkAABB(bounds, box(x, y, 9))):
                remaining.append((kind, x, y))
            elif kind == Dungeon.ECHO:
                world.echoes -= 1
                world.score += 250
                self.sound("echo")
                self.burst(world, x, y, GOLD, 16)
            else:
                p.hp += 1
                self.sound("echo")
                self.burst(world, x, y, CYAN, 12)
        world.items = remaining

    def finish(self, victory):
        self.final_score = self.dungeon.score
        self.profile.best = max(self.profile.best, self.final_score)
        if victory:
            self.profile.has_run = False
        self.save()
        self.audio.stopMusic()
        self.open_mode("victory" if victory else "over")

    def next_level(self, world):
        world.score += 500
        self.sound("gate")
        if world.stage == 2:
            self.finish(True)
            return
        self.profile.best = max(self.profile.best, world.score)
        self.profile.stage, self.profile.score = world.stage + 1, world.score
        self.profile.unlocked = max(self.profile.unlocked, self.profile.stage)
        self.save("Checkpoint gespeichert.")
        self.begin()

    def play_step(self, dt):
        world = self.dungeon
        if self.pressed(Key.ESCAPE):
            self.open_mode("pause")
            return
        self.player_step(world, dt)
        world.fx.update(dt)
        if world.player.hp <= 0:
            if world.player.age >= 0.52:
                self.finish(False)
            return

        world.update_flow()
        for enemy in world.enemies:
            self.enemy_step(world, enemy, dt)
        world.enemies = [enemy for enemy in world.enemies
                         if enemy.hp > 0 or enemy.age < 0.52]
        self.projectiles(world, dt)
        if world.player.hp > 0:
            self.collect(world)
            if world.echoes == 0 and Collision.checkAABB(world.player.bounds(), world.exit):
                self.next_level(world)
                return
        world.update_camera(dt)

    def update(self, dt):
        if self.pressed(Key.F3):
            self.debug = not self.debug
        if self.mode == "playing":
            self.play_step(dt)
        else:
            self.menu_input()
        self.publish()

    def world_frame(self, world):
        fighters = [world.player] + world.enemies
        fighters.sort(key=lambda fighter: fighter.y)
        actors = tuple((self.art.frame(f), int(round(f.x)), int(round(f.y)),
                        f.bounds(), f.hp, f.kind) for f in fighters)
        particles = tuple((int(p.position.x), int(p.position.y), int(p.size),
                           Colors(p.color.getRed(), p.color.getGreen(), p.color.getBlue(),
                                  clamp(int(255 * p.life / p.maxLife), 0, 255)))
                          for p in world.fx.getParticles() if p.alive and p.maxLife > 0)
        p = world.player
        return (world.map, world.walls, world.cols, world.rows, world.stage,
                world.camera_x, world.camera_y, world.exit_xy, world.exit,
                world.echoes, tuple(world.items), tuple(world.bolts), actors,
                particles, p.x, p.y, p.hp, world.score)

    def publish(self):
        world = None if self.dungeon is None else self.world_frame(self.dungeon)
        options = () if self.mode == "playing" else self.options()
        self.frame = (self.mode, self.selection, options, world, self.debug,
                      self.status or self.profile.notice, self.profile.best,
                      self.profile.unlocked, self.final_score,
                      tuple(str(key) for key in self.profile.binds), self.engine.getFpsString())

    def item(self, g, index, x, y):
        g.drawImage(self.art.items[index], int(x) - 16, int(y) - 16)

    def draw_world(self, g, world, debug, bindings, fps):
        (tilemap, walls, cols, rows, stage, cx, cy, exit_xy, exit_rect,
         echoes, items, bolts, actors, particles, px, py, hp, score) = world
        self.camera.snapTo(cx, cy)
        g.applyCamera(self.camera)
        try:
            tilemap.draw(g, self.camera)
            self.item(g, 2 if echoes else 3, exit_xy[0], exit_xy[1])
            for kind, x, y in items:
                self.item(g, 0 if kind == Dungeon.ECHO else 1, x, y)
            for image, x, y, bounds, life, kind in actors:
                g.drawImage(image, x - 16, y - 16)
                if kind != "keeper" and life > 0:
                    g.setColor(RED)
                    g.fillRect(x - 9, y - 19, life * 6, 2)
                if debug:
                    DebugDraw.drawRect(g, bounds, CYAN if kind == "keeper" else RED)
            for x, y, vx, vy, life in bolts:
                g.setColor(LILAC)
                g.fillCircle(int(x), int(y), 5)
                g.setColor(WHITE)
                g.fillCircle(int(x), int(y), 2)
                if debug:
                    DebugDraw.drawRect(g, box(x, y, 4), LILAC)
            for x, y, size, color in particles:
                g.setColor(color)
                g.fillRect(x, y, size, size)
            if debug:
                DebugDraw.drawRect(g, exit_rect, GOLD)
        finally:
            g.resetCamera()

        g.setColor(PANEL)
        g.fillRect(14, 14, 315, 88)
        g.setColor(CYAN)
        g.drawString(TITLES[stage], 26, 35, 17, "SansSerif")
        for i in range(MAX_HP):
            g.setColor(RED if i < hp else WALL_MAP)
            g.fillRect(26 + i * 30, 45, 25, 9)
        g.setColor(WHITE)
        g.drawString("Echos: %d   Punkte: %d" % (echoes, score), 26, 76, 14, "Monospaced")
        g.setColor(GOLD if echoes else CYAN)
        g.drawString("Tor verschlossen" if echoes else "Tor offen", 26, 94, 12, "SansSerif")
        self.minimap(g, cols, rows, walls, items, px, py, exit_xy)

        g.setColor(PANEL)
        g.fillRect(14, 521, 772, 26)
        g.setColor(MUTED)
        hint = "%s/%s/%s/%s  Bewegung   %s  Angriff   ESC  Pause   F3  Debug" % bindings
        g.drawString(hint, 25, 539, 12, "Monospaced")
        if debug:
            g.setColor(PANEL)
            g.fillRect(14, 110, 409, 28)
            g.setColor(CYAN)
            g.drawString("%s  P(%d,%d)  CAM(%d,%d)  FX:%d" %
                         (fps, px, py, cx, cy, len(particles)), 22, 129, 11, "Monospaced")

    def minimap(self, g, cols, rows, walls, items, px, py, exit_xy):
        scale = 3
        ox, oy = W - cols * scale - 24, 24
        g.setColor(PANEL)
        g.fillRect(ox - 8, oy - 8, cols * scale + 16, rows * scale + 16)
        g.setColor(FLOOR_MAP)
        g.fillRect(ox, oy, cols * scale, rows * scale)
        g.setColor(WALL_MAP)
        for col, row in walls:
            g.fillRect(ox + col * scale, oy + row * scale, scale, scale)
        g.setColor(GOLD)
        for kind, x, y in items:
            if kind == Dungeon.ECHO:
                g.fillRect(ox + int(x // CELL) * scale, oy + int(y // CELL) * scale, 3, 3)
        g.setColor(LILAC)
        g.fillRect(ox + int(exit_xy[0] // CELL) * scale, oy + int(exit_xy[1] // CELL) * scale, 3, 3)
        g.setColor(CYAN)
        g.fillRect(ox + int(px // CELL) * scale - 1, oy + int(py // CELL) * scale - 1, 5, 5)

    def draw_menu(self, g, mode, selected, options, status, best, unlocked, score):
        titles = {"main": "LUNAR DEPTHS", "pause": "PAUSE", "settings": "EINSTELLUNGEN",
                  "over": "DIE TIEFE HAT DICH", "victory": "DER ABGRUND VERSTUMMT"}
        g.setColor(SHADE)
        g.fillRect(0, 0, W, H)
        g.setColor(PANEL)
        g.fillRect(158, 68, 484, 424)
        g.setColor(CYAN)
        g.drawRect(158, 68, 484, 424)
        g.drawCenteredString(titles[mode], 400, 111, 26, "SansSerif")
        g.setColor(MUTED)
        subtitle = "ECHOES OF THE ABYSS"
        if mode == "main":
            subtitle = "BESTLEISTUNG %d   /   LEVEL %d VON 3 FREI" % (best, unlocked + 1)
        elif mode in ("over", "victory"):
            subtitle = "PUNKTE %d   /   BESTLEISTUNG %d" % (score, best)
        elif mode == "settings":
            subtitle = "PFEILTASTEN  /  ENTER  /  ESC"
        g.drawCenteredString(subtitle, 400, 138, 12, "Monospaced")

        for i, option in enumerate(options):
            y = 173 + i * 29
            if i == selected:
                g.setColor(WALL_MAP)
                g.fillRect(184, y - 18, 432, 25)
            g.setColor(CYAN if i == selected else WHITE)
            g.drawString(("> " if i == selected else "  ") + option, 198, y, 15, "Monospaced")
        if status:
            g.setColor(GOLD)
            g.drawCenteredString(status, 400, 462, 12, "SansSerif")

    def render(self, g):
        mode, selected, options, world, debug, status, best, unlocked, score, bindings, fps = self.frame
        g.setColor(INK)
        g.fillRect(0, 0, W, H)
        if world is None:
            g.drawImage(self.art.title, 0, 0)
        else:
            self.draw_world(g, world, debug, bindings, fps)
        if mode != "playing":
            self.draw_menu(g, mode, selected, options, status, best, unlocked, score)
            if debug and world is None:
                DebugDraw.drawScreenText(g, fps, 18, 24, CYAN)


engine = LunarEngine(W, H, "Lunar Depths - Echoes of the Abyss")
engine.setGameLogic(LunarDepths())
engine.start()