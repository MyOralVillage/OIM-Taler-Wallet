# fixes corrupted merges by forcing back to HEAD.
# very easy modification to get it to force accept most recent conflicts.

python3 << 'EOF'
import os
import re

def fix_conflicts(content):
    # Keep everything between <<<<<<< HEAD and =======
    # Remove everything between ======= and >>>>>>>
    # Remove all conflict markers

    lines = content.split('\n')
    result = []
    in_head = False
    in_incoming = False

    for line in lines:
        if line.startswith('<<<<<<< HEAD'):
            in_head = True
            continue
        elif line == '=======' or line.startswith('======='):
            in_head = False
            in_incoming = True
            continue
        elif line.startswith('>>>>>>> '):
            in_incoming = False
            continue

        # Keep lines that are in HEAD section or not in any conflict
        if not in_incoming:
            result.append(line)

    return '\n'.join(result)

for root, dirs, files in os.walk('.'):
    # Skip .git directory
    if '.git' in root:
        continue

    for file in files:
        if file.endswith(('.kt', '.gradle', '.md')):
            filepath = os.path.join(root, file)
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()

                if '<<<<<<< HEAD' in content:
                    fixed = fix_conflicts(content)
                    with open(filepath, 'w', encoding='utf-8') as f:
                        f.write(fixed)
                    print(f"Fixed: {filepath}")
            except Exception as e:
                print(f"Error processing {filepath}: {e}")

print("\nDone! Kept HEAD, rejected incoming changes.")
EOF
