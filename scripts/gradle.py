#!/usr/bin/env python3
"""Bootstrap a verified Gradle distribution; no binary wrapper is versioned."""
import hashlib
import os
import pathlib
import shutil
import subprocess
import sys
import urllib.request
import zipfile

version = '9.4.1'
digest = '2ab2958f2a1e51120c326cad6f385153bb11ee93b3c216c5fccebfdfbb7ec6cb'
root = pathlib.Path(__file__).resolve().parents[1]
os.chdir(root)
subprocess.run([sys.executable, 'scripts/prepare-assets.py'], check=True)
environment = os.environ.copy()
mac_jdk = pathlib.Path('/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home')
if 'JAVA_HOME' not in environment and mac_jdk.exists():
    environment['JAVA_HOME'] = str(mac_jdk)
installed = shutil.which('gradle')
if installed:
    output = subprocess.check_output([installed, '--version'], env=environment, text=True)
    if f'Gradle {version}\n' in output:
        sys.exit(subprocess.call([installed, *sys.argv[1:]], env=environment))
cache = pathlib.Path.home() / '.gradle' / 'condo-bootstrap'
launcher = cache / f'gradle-{version}' / 'bin' / ('gradle.bat' if os.name == 'nt' else 'gradle')
if not launcher.exists():
    cache.mkdir(parents=True, exist_ok=True)
    archive = cache / f'gradle-{version}-bin.zip'
    urllib.request.urlretrieve(f'https://services.gradle.org/distributions/gradle-{version}-bin.zip', archive)
    if hashlib.sha256(archive.read_bytes()).hexdigest() != digest:
        raise SystemExit('Gradle distribution checksum mismatch')
    with zipfile.ZipFile(archive) as bundle:
        bundle.extractall(cache)
    launcher.chmod(0o755)
sys.exit(subprocess.call([str(launcher), *sys.argv[1:]], env=environment))
