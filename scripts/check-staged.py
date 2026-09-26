#!/usr/bin/env python3
"""Reject commits outside the project's explicit one-file, 20-line rule."""
import subprocess
import sys

rows = subprocess.check_output(
    ['git', 'diff', '--cached', '--numstat', '--no-renames'], text=True
).splitlines()
valid = len(rows) == 1
if valid:
    added, removed, _ = rows[0].split('\t', 2)
    valid = added.isdigit() and removed.isdigit()
    valid = valid and int(added) + int(removed) <= 20
if not valid:
    sys.exit('Commit rejected: exactly one text file and at most 20 added + removed lines.')
print('Staged change satisfies the commit rule.')
