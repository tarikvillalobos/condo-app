#!/usr/bin/env python3
"""Audit every task commit, preserving the original README as the baseline."""
import subprocess
import sys

roots = subprocess.check_output(['git', 'rev-list', '--max-parents=0', 'HEAD'], text=True).splitlines()
if len(roots) != 1:
    sys.exit('Expected a single initial README commit')
baseline = sys.argv[1] if len(sys.argv) > 1 else roots[0]
initial_emails = subprocess.check_output(['git', 'show', '-s', '--format=%ae%n%ce', baseline], text=True).splitlines()
if any(email != 'tarik.villalobos@gmail.com' for email in initial_emails):
    sys.exit('Initial README commit has an unexpected author or committer e-mail')
commits = subprocess.check_output(['git', 'rev-list', f'{baseline}..HEAD'], text=True).splitlines()
for commit in commits:
    rows = subprocess.check_output([
        'git', 'diff-tree', '--no-commit-id', '--numstat', '--no-renames', '-r', commit
    ], text=True).splitlines()
    if len(rows) != 1:
        sys.exit(f'{commit}: expected exactly one changed file')
    added, removed, _ = rows[0].split('\t', 2)
    if not added.isdigit() or not removed.isdigit() or int(added) + int(removed) > 20:
        sys.exit(f'{commit}: binary file or more than 20 changed lines')
    emails = subprocess.check_output(['git', 'show', '-s', '--format=%ae%n%ce', commit], text=True).splitlines()
    if any(email != 'tarik.villalobos@gmail.com' for email in emails):
        sys.exit(f'{commit}: unexpected author or committer e-mail')
print(f'Validated {len(commits)} commits: one file, <=20 lines, expected author and committer.')
