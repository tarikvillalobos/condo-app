import difflib, pathlib, subprocess, sys

def git(*args, data=None):
    return subprocess.check_output(['git', *args], input=data)

def commit_file(name, message):
    path = pathlib.Path(name)
    final = path.read_bytes()
    assert b'\0' not in final, 'Binary files require explicit permission'
    assert not git('diff','--cached','--name-only').strip(), 'Staging must be empty'
    try:
        original = git('show', 'HEAD:' + name).decode().splitlines(True)
    except subprocess.CalledProcessError:
        original = []
    target = final.decode().splitlines(True)
    current = list(original)
    changes = list(difflib.SequenceMatcher(a=original, b=target).get_opcodes())
    offset = 0
    part = 0
    def save():
        nonlocal part
        blob = git('hash-object','-w','--stdin',data=''.join(current).encode()).strip().decode()
        mode = '100755' if path.stat().st_mode & 0o111 else '100644'
        git('update-index','--add','--cacheinfo',mode,blob,name)
        rows = git('diff','--cached','--numstat').decode().splitlines()
        assert len(rows) == 1, rows
        added, removed, changed = rows[0].split('\t')
        assert changed == name and int(added) + int(removed) <= 20, rows
        part += 1
        git('commit','-m',f'{message} ({part})')
    for tag, a, b, c, d in changes:
        if tag == 'equal': continue
        pos = a + offset
        remaining = b - a
        while remaining:
            count = min(20,remaining)
            del current[pos:pos+count]
            offset -= count
            remaining -= count
            save()
        values = target[c:d]
        for start in range(0,len(values),20):
            chunk = values[start:start+20]
            current[pos:pos] = chunk
            pos += len(chunk)
            offset += len(chunk)
            save()
    assert current == target
    assert path.read_bytes() == final
    print(f'{name}: {part} compliant commits')

if __name__ == '__main__':
    commit_file(sys.argv[1],sys.argv[2])
