#!/bin/bash
# -------------------------------------------------------
# Copyright (c) [2026] MantaBots
# All rights reserved
# -------------------------------------------------------
# Merge config files and push to the Control Hub
# Usage: ./conf.sh <hardware> <config> [conf root]
#   <conf root>/hardware/<hardware>.xml : hub hardware configuration
#   <conf root>/hub/<config>.*.json     : code configuration files, merged into <config>.json
# -------------------------------------------------------

set -e

# ── Arguments ──────────────────────────────────────────
HW_NAME=$1
NAME=$2
CONF_DIR=${3:-$(dirname "$(readlink -f "$0")")/../conf}

if [ -z "$HW_NAME" ] || [ -z "$NAME" ]; then
    echo "Usage: $0 <hardware> <config> [conf root]"
    echo "  hardware  : hub hardware configuration name, without .xml (e.g. starter-bot)"
    echo "  config    : code configuration name, without .json (e.g. mantabots)"
    echo "  conf root : directory containing hub/ and hardware/ (default: ../conf)"
    exit 1
fi

HUB_DIR="$CONF_DIR/hub"
HARDWARE_DIR="$CONF_DIR/hardware"

# ── Find all <n>.*.json files ───────────────────────────
FILES=$(find "$HUB_DIR" -maxdepth 1 -name "${NAME}.*.json" | sort)

if [ -z "$FILES" ]; then
    echo "⚠️  No files matching ${NAME}.*.json found in $HUB_DIR"
    exit 0
fi

echo "📂 Found files:"
for F in $FILES; do echo "   $F"; done

# ── Merge into a single JSON ───────────────────────────
# Files are processed shallowest-first.
# When a file's key path is a sub-path of an already-merged entry
# (e.g. example.robot.subsystems.drivetrain.follower.json extends
#  example.robot.json), its content is deep-merged into that entry
# at the remaining path rather than placed in a parallel branch.
# File name keys must exactly match the JSON keys they extend.
MERGED=$(python3 - "$NAME" $FILES <<'PYEOF'
import json, os, sys

name  = sys.argv[1]
files = sys.argv[2:]

def deep_merge(base, keys, value):
    """Deep-merge value into base at the given key path."""
    if not keys:
        if isinstance(base, dict) and isinstance(value, dict):
            for k, v in value.items():
                if k in base and isinstance(base[k], dict) and isinstance(v, dict):
                    deep_merge(base[k], [], v)
                else:
                    base[k] = v
        return
    k = keys[0]
    if len(keys) == 1:
        if k in base and isinstance(base[k], dict) and isinstance(value, dict):
            deep_merge(base[k], [], value)
        else:
            base[k] = value
    else:
        if k not in base:
            base[k] = {}
        deep_merge(base[k], keys[1:], value)

def key_depth(filepath):
    basename = os.path.basename(filepath)
    return len(basename[len(name)+1:-5].split("."))

files = sorted(files, key=key_depth)
merged = {}

for filepath in files:
    basename = os.path.basename(filepath)
    keys = basename[len(name)+1:-5].split(".")
    try:
        with open(filepath) as f:
            content = json.load(f)

        # Find the longest prefix of keys already present in merged.
        # If found, deep-merge into that existing node at the remaining path.
        best = 0
        for prefix_len in range(1, len(keys)):
            node, ok = merged, True
            for k in keys[:prefix_len]:
                if isinstance(node, dict) and k in node:
                    node = node[k]
                else:
                    ok = False
                    break
            if ok:
                best = prefix_len

        if best > 0:
            node = merged
            for k in keys[:best]:
                node = node[k]
            remaining = keys[best:]
            deep_merge(node, remaining, content)
            anchor = ".".join(keys[:best])
            tail   = ".".join(remaining) if remaining else "(root)"
            print(f"   ✅ {basename} → merged into [{anchor}] at {tail}", file=sys.stderr)
        else:
            deep_merge(merged, keys, content)
            print(f"   ✅ {basename} → {'.'.join(keys)}", file=sys.stderr)

    except Exception as e:
        print(f"   ⚠️  Skipping {basename}: {e}", file=sys.stderr)

print(json.dumps(merged, indent=2))
PYEOF
)

# ── Write merged file ──────────────────────────────────
TMP_FILE="/tmp/${NAME}.json"
echo "$MERGED" > "$TMP_FILE"
echo "✅ Merged config written to $TMP_FILE"

# ── Try ADB push (non-fatal if Control Hub unreachable) ─
set +e

ADB_OK=false
adb connect 192.168.43.1:5555 > /dev/null 2>&1
if adb devices 2>/dev/null | grep -q "192.168.43.1:5555"; then
    ADB_OK=true
fi

if $ADB_OK; then
    echo "📤 Pushing ${NAME}.json to Control Hub..."
    adb push "$TMP_FILE" "/sdcard/FIRST/${NAME}.json"

    HW_FILE="$HARDWARE_DIR/$HW_NAME.xml"
    if [ -f "$HW_FILE" ]; then
        adb push "$HW_FILE" "/sdcard/FIRST/$HW_NAME.xml"
        echo "✅ Pushed: $HW_FILE → /sdcard/FIRST/$HW_NAME.xml"
    else
        echo "⚠️  No hardware file found: $HW_FILE"
    fi
    echo "🏁 Done — configuration '$NAME' and hardware '$HW_NAME' deployed to /sdcard/FIRST/"
else
    echo "⚠️  Control Hub not reachable — merged file kept at $TMP_FILE"
fi
