#!/usr/bin/env python3
"""Fetch pinned fonts without committing binary artifacts."""
import hashlib
import pathlib
import urllib.request

root = pathlib.Path(__file__).resolve().parents[1]
assets = root / 'app/src/commonMain/composeResources/font'
assets.mkdir(parents=True, exist_ok=True)
fonts = {
    'manrope': '3ae11c49db0455a3cc33e37d380f20fdb8c7f8b41dc07625c177e3d87a9d6ae6',
    'sora': '84ff7096ae3ec6c8be47d906d1a0ba4de7f2ce78c615275c77301964a316e16c',
}
for name, digest in fonts.items():
    target = assets / (name + '.ttf')
    data = target.read_bytes() if target.exists() else urllib.request.urlopen(
        f'https://raw.githubusercontent.com/google/fonts/main/ofl/{name}/{name.title()}%5Bwght%5D.ttf'
    ).read()
    if hashlib.sha256(data).hexdigest() != digest:
        raise SystemExit(f'Font integrity check failed: {name}')
