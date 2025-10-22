# Small little script to convert PNGs -> SVGs
# Needs: vtracer (see github on install)
# NOTE: replace bash w/ zsh if you are on MAC;
# this will NOT work for windows!

#!/usr/bin/env bash
set -euo pipefail

# Directory for output SVGs
OUT_DIR="$HOME/svgs"
mkdir -p "$OUT_DIR"

# Path to vtracer binary
VTRACER="/home/oson/.cargo/bin/vtracer"

# Process all .png and .jpg files in the current directory
for img in *.png *.jpg; do
    # Skip if no matching files
    [[ -e "$img" ]] || continue

    # Strip extension and create output filename
    base="${img%.*}"
    out="$OUT_DIR/$base.svg"

    echo "Converting: $img → $out"
    "$VTRACER" --mode polygon -p 4 --input "$img" --output "$out"
done

echo "✅ Conversion complete. SVGs saved in $OUT_DIR"
