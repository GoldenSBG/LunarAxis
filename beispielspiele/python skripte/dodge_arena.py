from LunarAxis.core import LunarEngine
from LunarAxis.core.manager import IGameLogic
from LunarAxis.graphics import Colors
from LunarAxis.inputs import Key
from LunarAxis.math import Vec2
from LunarAxis.physics import Collision
from LunarAxis.util import LunarRandom

WIDTH, HEIGHT = 760, 520
LEFT, TOP, RIGHT, BOTTOM = 24, 72, 736, 496
CENTER_X, CENTER_Y = 380, 284
PLAYER_R, TOKEN_R = 12, 8
ASSETS = "games/dodge-arena/"

TEXT = Colors(229, 244, 255)
MUTED = Colors(157, 184, 213)
CYAN = Colors(68, 240, 234)
GOLD = Colors(255, 211, 89)
RED = Colors(255, 88, 118)
VIOLET = Colors(186, 132, 255)
SHADE = Colors(4, 8, 19, 175)


class DodgeArena(IGameLogic):
    def init(self, engine):
        self.keys = engine.getInputManager()
        self.random = LunarRandom()
        resources = engine.getResourceManager()
        self.images = {}
        for name in ("arena", "panel", "player", "player-hit", "core",
                     "bolt-right", "bolt-left", "bolt-down", "bolt-up"):
            path = ASSETS + name + ".png"
            image = resources.loadImage(path)
            if image is None:
                raise RuntimeError("Missing Dodge Arena asset: " + path)
            self.images[name] = image
        self.reset()
        self.state = "ready"
        self.publish_frame()

    def reset(self):
        self.x, self.y = CENTER_X, CENTER_Y
        self.elapsed, self.tokens = 0.0, 0
        self.flash, self.until_spawn = 0.0, 0.9
        self.hazards = []
        self.place_token()
        self.state = "playing"

    def place_token(self):
        # Place the next token in the opposite half of the arena.
        if self.x < CENTER_X:
            x = self.random.nextInt(420, RIGHT - 32)
        else:
            x = self.random.nextInt(LEFT + 32, 340)
        self.token = (x, self.random.nextInt(TOP + 32, BOTTOM - 32))

    def spawn(self):
        side = self.random.nextInt(4)
        if side < 2:
            r = 13
            x = LEFT + r if side == 0 else RIGHT - r
            y = self.random.nextInt(TOP + 24, BOTTOM - 24)
            dx, dy = (1 if side == 0 else -1), 0
            color = RED
        else:
            r = 17
            x = self.random.nextInt(LEFT + 24, RIGHT - 24)
            y = TOP + r if side == 2 else BOTTOM - r
            dx, dy = 0, (1 if side == 2 else -1)
            color = VIOLET
        self.hazards.append((x, y, dx, dy, r, color, 0.65))

    def update(self, dt):
        if self.state != "playing":
            if self.keys.isKeyPressed(Key.SPACE):
                self.reset()
                self.publish_frame()
            return

        dx = int(self.keys.isKeyDown(Key.D)) - int(self.keys.isKeyDown(Key.A))
        dy = int(self.keys.isKeyDown(Key.S)) - int(self.keys.isKeyDown(Key.W))
        step = 265.0 * dt
        if dx and dy:
            step *= 0.70710678
        self.x = max(LEFT + PLAYER_R, min(RIGHT - PLAYER_R, self.x + dx * step))
        self.y = max(TOP + PLAYER_R, min(BOTTOM - PLAYER_R, self.y + dy * step))
        player = Vec2(self.x, self.y)

        self.elapsed += dt
        self.flash = max(0.0, self.flash - dt)
        ramp = self.elapsed / (self.elapsed + 24.0)
        speed = 130.0 + 260.0 * ramp
        self.until_spawn -= dt
        if self.until_spawn <= 0.0:
            self.spawn()
            self.until_spawn += 1.25 - 1.02 * ramp

        remaining = []
        for x, y, vx, vy, r, color, warning in self.hazards:
            travel = max(0.0, dt - warning)
            warning = max(0.0, warning - dt)
            x += vx * speed * travel
            y += vy * speed * travel
            if LEFT + r <= x <= RIGHT - r and TOP + r <= y <= BOTTOM - r:
                remaining.append((x, y, vx, vy, r, color, warning))
                if warning == 0.0 and Collision.checkCircle(player, PLAYER_R, Vec2(x, y), r):
                    self.state = "over"
        self.hazards = remaining

        if self.state == "playing":
            tx, ty = self.token
            if Collision.checkCircle(player, PLAYER_R, Vec2(tx, ty), TOKEN_R):
                self.tokens += 1
                self.flash = 0.35
                self.place_token()
        self.publish_frame()

    def publish_frame(self):
        # Publish one snapshot for the render callback.
        score = int(self.elapsed) + 25 * self.tokens
        self.frame = (self.state, self.x, self.y, self.token,
                      tuple(self.hazards), self.elapsed, score, self.flash)

    def draw_sprite(self, g, name, x, y):
        # All moving sprites are 64 x 64 with their origin at the center.
        g.drawImage(self.images[name], int(round(x)) - 32, int(round(y)) - 32)

    def render(self, g):
        state, x, y, token, hazards, elapsed, score, flash = self.frame
        g.drawImage(self.images["arena"], 0, 0)
        self.draw_sprite(g, "core", token[0], token[1])
        for hx, hy, vx, vy, r, color, warning in hazards:
            if warning > 0.0:
                hx, hy = int(round(hx)), int(round(hy))
                g.setColor(color)
                g.drawCircle(hx, hy, r + int(warning * 8))
                g.drawLine(hx, hy, hx + vx * (r + 5), hy + vy * (r + 5))
            else:
                if vx:
                    name = "bolt-right" if vx > 0 else "bolt-left"
                else:
                    name = "bolt-down" if vy > 0 else "bolt-up"
                self.draw_sprite(g, name, hx, hy)

        self.draw_sprite(g, "player-hit" if state == "over" else "player", x, y)
        if flash > 0.0:
            g.setColor(GOLD)
            radius = PLAYER_R + 4 + int((0.35 - flash) * 30)
            g.drawCircle(int(round(x)), int(round(y)), radius)

        self.draw_hud(g, elapsed, score)
        if state != "playing":
            self.draw_overlay(g, state)

    def draw_hud(self, g, elapsed, score):
        g.setColor(CYAN)
        g.drawString("DODGE ARENA", 40, 34, 21, "SansSerif")
        g.setColor(MUTED)
        g.drawString("SURVIVE THE STORM", 41, 50, 11, "Monospaced")
        g.drawString("PUNKTE", 456, 29, 11, "Monospaced")
        g.drawString("ZEIT", 607, 29, 11, "Monospaced")
        g.setColor(GOLD)
        g.drawString("%04d" % score, 456, 50, 19, "Monospaced")
        g.setColor(TEXT)
        g.drawString("%05.1f s" % elapsed, 607, 50, 19, "Monospaced")

    def draw_overlay(self, g, state):
        g.setColor(SHADE)
        g.fillRect(LEFT, TOP, RIGHT - LEFT, BOTTOM - TOP)
        g.drawImage(self.images["panel"], CENTER_X - 240, CENTER_Y - 110)
        if state == "ready":
            title, prompt = "SURVIVE THE STORM", "LEERTASTE ZUM STARTEN"
            subtitle, accent = "Pfeiltasten  /  Ausweichen und Kerne sammeln", CYAN
        else:
            title, prompt = "GAME OVER", "LEERTASTE FUER EINE NEUE RUNDE"
            subtitle, accent = "Vom Sturm erwischt", RED
        g.setColor(accent)
        g.drawCenteredString(title, CENTER_X, 249, 31, "SansSerif")
        g.setColor(MUTED)
        g.drawCenteredString(subtitle, CENTER_X, 290, 16, "SansSerif")
        g.setColor(TEXT)
        g.drawCenteredString(prompt, CENTER_X, 344, 14, "Monospaced")


engine = LunarEngine(WIDTH, HEIGHT, "Dodge Arena - Survive the Storm")
engine.setGameLogic(DodgeArena())
engine.start()