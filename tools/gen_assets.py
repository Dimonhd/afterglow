#!/usr/bin/env python3
"""Генерирует все ресурсы мода Afterglow (текстуры, модели, blockstates, lang, рецепты, лут, теги).
Запуск из корня проекта:  python3 tools/gen_assets.py
"""
import json
import os
import random
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
MOD = "afterglow"
A = os.path.join(ROOT, "assets", MOD)
D = os.path.join(ROOT, "data", MOD)


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, ensure_ascii=False, indent=2)
        f.write("\n")


# ---------------------------------------------------------------- textures
def noisy(base, amp, seed):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            d = rnd.randint(-amp, amp)
            px[x, y] = tuple(max(0, min(255, c + d)) for c in base) + (255,)
    return img


def save_tex(name, img, sub="block"):
    path = os.path.join(A, "textures", sub, name + ".png")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)


def make_textures():
    save_tex("wire", noisy((196, 112, 52), 14, 1))
    body = noisy((92, 96, 104), 8, 2)
    d = ImageDraw.Draw(body)
    d.rectangle([0, 0, 15, 15], outline=(58, 60, 66))
    for (x, y) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        body.putpixel((x, y), (150, 152, 158, 255))
    save_tex("source_body", body)
    plus = noisy((170, 40, 40), 10, 3)
    d = ImageDraw.Draw(plus)
    d.rectangle([4, 7, 11, 8], fill=(240, 230, 230))
    d.rectangle([7, 4, 8, 11], fill=(240, 230, 230))
    save_tex("source_plus", plus)
    minus = noisy((40, 60, 150), 10, 4)
    d = ImageDraw.Draw(minus)
    d.rectangle([4, 7, 11, 8], fill=(230, 230, 240))
    save_tex("source_minus", minus)
    save_tex("metal", noisy((160, 162, 168), 12, 5))
    sw = noisy((70, 72, 78), 8, 6)
    ImageDraw.Draw(sw).rectangle([0, 0, 15, 15], outline=(40, 42, 46))
    save_tex("switch_body", sw)
    save_tex("switch_lever", noisy((200, 60, 50), 10, 7))
    so = noisy((110, 84, 60), 10, 8)
    ImageDraw.Draw(so).rectangle([0, 0, 15, 15], outline=(70, 52, 36))
    save_tex("socket_body", so)
    save_tex("socket_thread", noisy((176, 150, 90), 14, 9))
    glass = noisy((215, 225, 230), 6, 10)
    ImageDraw.Draw(glass).rectangle([0, 0, 15, 15], outline=(160, 175, 185))
    save_tex("bulb_glass", glass)
    lit = noisy((255, 236, 150), 8, 11)
    ImageDraw.Draw(lit).rectangle([0, 0, 15, 15], outline=(255, 210, 90))
    save_tex("bulb_glass_lit", lit)

    # предмет: лампа
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([3, 1, 12, 10], fill=(225, 235, 240, 255), outline=(150, 165, 175, 255))
    d.rectangle([5, 3, 6, 4], fill=(255, 255, 255, 255))
    d.line([6, 8, 6, 6, 9, 6, 9, 8], fill=(90, 90, 90, 255))
    d.rectangle([5, 10, 10, 11], fill=(170, 160, 120, 255))
    d.rectangle([5, 12, 10, 13], fill=(130, 120, 90, 255))
    d.rectangle([6, 14, 9, 14], fill=(60, 60, 60, 255))
    save_tex("lamp", img, "item")


# ------------------------------------------------------------------ models
def face(tex, uv=None):
    f = {"texture": "#" + tex}
    if uv:
        f["uv"] = uv
    return f


def cube(frm, to, tex_all=None, tex_by_face=None):
    faces = {}
    for k in ["north", "east", "south", "west", "up", "down"]:
        t = (tex_by_face or {}).get(k, tex_all)
        faces[k] = {"texture": "#" + t}
    return {"from": frm, "to": to, "faces": faces}


def block_model(name, textures, elements, parent="minecraft:block/block"):
    obj = {"parent": parent, "textures": textures, "elements": elements}
    write_json(os.path.join(A, "models", "block", name + ".json"), obj)


def make_models():
    # провод
    block_model("wire_core", {"all": f"{MOD}:block/wire", "particle": f"{MOD}:block/wire"},
                [cube([6, 6, 6], [10, 10, 10], "all")])
    block_model("wire_arm", {"all": f"{MOD}:block/wire", "particle": f"{MOD}:block/wire"},
                [cube([6, 6, 0], [10, 10, 6], "all")])
    block_model("wire_item", {"all": f"{MOD}:block/wire", "particle": f"{MOD}:block/wire"},
                [cube([6, 6, 6], [10, 10, 10], "all"),
                 cube([6, 6, 0], [10, 10, 6], "all"),
                 cube([6, 6, 10], [10, 10, 16], "all")])

    # источник: «+» на севере (лицевая грань), «−» на юге
    block_model("source", {
        "body": f"{MOD}:block/source_body", "plus": f"{MOD}:block/source_plus",
        "minus": f"{MOD}:block/source_minus", "particle": f"{MOD}:block/source_body"},
        [cube([3, 3, 3], [13, 13, 13], "body"),
         cube([6, 6, 0], [10, 10, 3], "body", {"north": "plus"}),
         cube([6, 6, 13], [10, 10, 16], "body", {"south": "minus"})])

    # выключатель
    sw_tex = {"body": f"{MOD}:block/switch_body", "metal": f"{MOD}:block/metal",
              "lever": f"{MOD}:block/switch_lever", "particle": f"{MOD}:block/switch_body"}
    for name, angle in (("switch_open", -45), ("switch_closed", 45)):
        lever = cube([7, 13, 7], [9, 17, 9], "lever")
        lever["rotation"] = {"origin": [8, 13, 8], "axis": "x", "angle": angle}
        block_model(name, sw_tex, [
            cube([3, 3, 3], [13, 13, 13], "body"),
            cube([6, 6, 0], [10, 10, 3], "metal"),
            cube([6, 6, 13], [10, 10, 16], "metal"),
            lever])

    # цоколь и лампа
    so_tex = {"body": f"{MOD}:block/socket_body", "thread": f"{MOD}:block/socket_thread",
              "metal": f"{MOD}:block/metal", "glass": f"{MOD}:block/bulb_glass",
              "glass_lit": f"{MOD}:block/bulb_glass_lit", "particle": f"{MOD}:block/socket_body"}
    block_model("socket", so_tex, [
        cube([3, 3, 3], [13, 9, 13], "body"),
        cube([6, 9, 6], [10, 11, 10], "thread"),
        cube([6, 6, 0], [10, 10, 3], "metal"),
        cube([6, 6, 13], [10, 10, 16], "metal")])
    block_model("socket_bulb", so_tex, [
        cube([5, 11, 5], [11, 16, 11], "glass")])
    block_model("socket_bulb_lit", so_tex, [
        cube([5, 11, 5], [11, 16, 11], "glass_lit")])

    # модели предметов
    items = os.path.join(A, "models", "item")
    write_json(os.path.join(items, "wire.json"), {"parent": f"{MOD}:block/wire_item"})
    write_json(os.path.join(items, "source.json"), {"parent": f"{MOD}:block/source"})
    write_json(os.path.join(items, "switch.json"), {"parent": f"{MOD}:block/switch_open"})
    write_json(os.path.join(items, "socket.json"), {"parent": f"{MOD}:block/socket"})
    write_json(os.path.join(items, "lamp.json"),
               {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/lamp"}})


# -------------------------------------------------------------- blockstates
FACING_ROT = {
    "north": {}, "east": {"y": 90}, "south": {"y": 180}, "west": {"y": 270},
    "up": {"x": 270}, "down": {"x": 90},
}


def apply(model, rot):
    d = {"model": f"{MOD}:block/{model}"}
    d.update(rot)
    return d


def make_blockstates():
    bs = os.path.join(A, "blockstates")

    # провод
    arm_rot = {"north": {}, "east": {"y": 90}, "south": {"y": 180},
               "west": {"y": 270}, "up": {"x": 270}, "down": {"x": 90}}
    parts = [{"apply": {"model": f"{MOD}:block/wire_core"}}]
    for d, rot in arm_rot.items():
        parts.append({"when": {d: "true"}, "apply": apply("wire_arm", rot)})
    write_json(os.path.join(bs, "wire.json"), {"multipart": parts})

    # источник
    write_json(os.path.join(bs, "source.json"),
               {"variants": {f"facing={f}": apply("source", r) for f, r in FACING_ROT.items()}})

    # выключатель
    variants = {}
    for f, r in FACING_ROT.items():
        variants[f"closed=false,facing={f}"] = apply("switch_open", r)
        variants[f"closed=true,facing={f}"] = apply("switch_closed", r)
    write_json(os.path.join(bs, "switch.json"), {"variants": variants})

    # цоколь (multipart; яркость лампы хранится в свойстве "level" 0..15)
    lit_levels = "|".join(str(i) for i in range(1, 16))
    parts = []
    for f, r in FACING_ROT.items():
        parts.append({"when": {"facing": f}, "apply": apply("socket", r)})
        parts.append({"when": {"facing": f, "has_bulb": "true", "level": "0"},
                      "apply": apply("socket_bulb", r)})
        parts.append({"when": {"facing": f, "has_bulb": "true", "level": lit_levels},
                      "apply": apply("socket_bulb_lit", r)})
    write_json(os.path.join(bs, "socket.json"), {"multipart": parts})


# --------------------------------------------------------------------- lang
def make_lang():
    ru = {
        "itemGroup.afterglow": "Afterglow: Электричество",
        "block.afterglow.wire": "Провод",
        "block.afterglow.source": "Источник питания",
        "block.afterglow.switch": "Выключатель",
        "block.afterglow.socket": "Цоколь",
        "item.afterglow.lamp": "Лампа накаливания",
        "tooltip.afterglow.lamp": "%1$s В, %2$s Вт (R = %3$s Ом)",
        "message.afterglow.source": "Напряжение: %1$s В | Ток: %2$s А | Мощность: %3$s Вт",
    }
    en = {
        "itemGroup.afterglow": "Afterglow: Electricity",
        "block.afterglow.wire": "Wire",
        "block.afterglow.source": "Power Supply",
        "block.afterglow.switch": "Switch",
        "block.afterglow.socket": "Lamp Socket",
        "item.afterglow.lamp": "Incandescent Lamp",
        "tooltip.afterglow.lamp": "%1$s V, %2$s W (R = %3$s Ohm)",
        "message.afterglow.source": "Voltage: %1$s V | Current: %2$s A | Power: %3$s W",
    }
    write_json(os.path.join(A, "lang", "ru_ru.json"), ru)
    write_json(os.path.join(A, "lang", "en_us.json"), en)


# ------------------------------------------------------- recipes, loot, tags
def item(i):
    return {"item": i}


def shaped(name, pattern, key, result, count=1):
    write_json(os.path.join(D, "recipe", name + ".json"), {
        "type": "minecraft:crafting_shaped", "category": "redstone",
        "key": {k: item(v) for k, v in key.items()},
        "pattern": pattern,
        "result": {"id": f"{MOD}:{result}", "count": count},
    })


def make_data():
    shaped("wire", ["CCC"], {"C": "minecraft:copper_ingot"}, "wire", 6)
    shaped("source", ["ICI", "CRC", "III"],
           {"I": "minecraft:iron_ingot", "C": "minecraft:copper_ingot", "R": "minecraft:redstone"}, "source")
    shaped("switch", [" S ", "ICI"],
           {"S": "minecraft:stick", "I": "minecraft:iron_ingot", "C": "minecraft:copper_ingot"}, "switch")
    shaped("socket", [" I ", "CIC"],
           {"I": "minecraft:iron_ingot", "C": "minecraft:copper_ingot"}, "socket")
    shaped("lamp", ["P", "R", "C"],
           {"P": "minecraft:glass_pane", "R": "minecraft:redstone", "C": "minecraft:copper_ingot"}, "lamp")

    for b in ("wire", "source", "switch", "socket"):
        write_json(os.path.join(D, "loot_table", "blocks", b + ".json"), {
            "type": "minecraft:block",
            "pools": [{
                "rolls": 1.0, "bonus_rolls": 0.0,
                "entries": [{"type": "minecraft:item", "name": f"{MOD}:{b}"}],
                "conditions": [{"condition": "minecraft:survives_explosion"}],
            }],
        })

    write_json(os.path.join(ROOT, "data", "minecraft", "tags", "block", "mineable", "pickaxe.json"), {
        "replace": False,
        "values": [f"{MOD}:{b}" for b in ("wire", "source", "switch", "socket")],
    })


if __name__ == "__main__":
    make_textures()
    make_models()
    make_blockstates()
    make_lang()
    make_data()
    print("assets generated into", os.path.normpath(ROOT))
