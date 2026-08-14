#!/usr/bin/env python3
"""Parse the SRG->official TSRG and produce a sed script to remap SRG names in source."""
import re, sys, subprocess

TSRG = "/root/.gradle/caches/forge_gradle/minecraft_user_repo/de/oceanlabs/mcp/mcp_config/1.20.1-20230612.114412/srg_to_official_1.20.1.tsrg"
SRC_DIR = "/workspace/BetterBloodMagic/src/main/java"

out = subprocess.check_output(["grep", "-rohE", r"(m|f)_[0-9]+_", SRC_DIR], text=True)
needed = set(out.split())

srg_to_mojang = {}
with open(TSRG) as f:
    for line in f:
        if not line.startswith("\t") or line.startswith("\t\t"):
            continue
        parts = line.strip().split()
        if not parts:
            continue
        srg = parts[0]
        if not re.match(r"^[mf]_[0-9]+_$", srg):
            continue
        if srg not in needed:
            continue
        if len(parts) >= 3 and parts[1].startswith("("):
            mojang = parts[2]
        elif len(parts) >= 2:
            mojang = parts[1]
        else:
            continue
        if mojang and mojang != srg:
            srg_to_mojang[srg] = mojang

missing = needed - set(srg_to_mojang.keys())
if missing:
    print("MISSING:", file=sys.stderr)
    for m in sorted(missing):
        print(f"  {m}", file=sys.stderr)

print(f"# {len(srg_to_mojang)} mappings", file=sys.stderr)
with open("/workspace/remap_bbm.sed", "w") as f:
    for srg in sorted(srg_to_mojang, key=len, reverse=True):
        mojang = srg_to_mojang[srg]
        f.write(f"s/\\b{re.escape(srg)}/{re.escape(mojang)}/g\n")

for srg in sorted(srg_to_mojang):
    print(f"{srg} -> {srg_to_mojang[srg]}")
