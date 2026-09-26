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
