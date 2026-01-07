import subprocess
import sys

def parse_points(s):
    points = {}
    s = s.strip().strip('"').strip()
    if not s:
        return points
    for entry in s.split(';'):
        if ':' in entry:
            name, coords = entry.split(':', 1)
            points[name] = coords
    return points

def apply_patch(base, patch):
    result = base.copy()
    added = []
    updated = []
    for name, coords in patch.items():
        if name in result:
            if result[name] != coords:
                updated.append(name)
            result[name] = coords
        else:
            result[name] = coords
            added.append(name)
    return result, added, updated

def points_to_string(points):
    return ';'.join(f"{name}:{coords}" for name, coords in points.items())

def copy_to_clipboard(text):
    try:
        if sys.platform == 'darwin':
            subprocess.run(['pbcopy'], input=text.encode(), check=True)
        elif sys.platform == 'win32':
            subprocess.run(['clip'], input=text.encode(), check=True)
        else:
            return False
        return True
    except:
        return False

def format_names(names):
    if not names:
        return ""
    if len(names) <= 6:
        return f" [{', '.join(names)}]"
    first = ', '.join(names[:3])
    last = ', '.join(names[-3:])
    middle = len(names) - 6
    return f" [{first}, ... (+{middle} more), {last}]"

def main():
    print("Paste base string:")
    s1 = input().strip()
    print("\nPaste patch string:")
    s2 = input().strip()

    base = parse_points(s1)
    patch = parse_points(s2)

    if base == patch:
        print("\n[INFO] Strings identical. No changes needed.")
        copy_to_clipboard(points_to_string(base))
        print("[INFO] Copied to clipboard.")
        return

    result, added, updated = apply_patch(base, patch)
    result_str = points_to_string(result)

    copy_to_clipboard(result_str)

    print(f"\n[INFO] Base: {len(base)} points")
    print(f"[INFO] Patch: {len(patch)} points")
    print(f"[INFO] Points added: {len(added)}{format_names(added)}")
    print(f"[INFO] Points updated: {len(updated)}{format_names(updated)}")
    print(f"[INFO] Total: {len(result)} points")
    print("[INFO] Result copied to clipboard.")

if __name__ == "__main__":
    main()
