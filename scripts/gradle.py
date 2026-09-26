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
