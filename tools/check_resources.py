#!/usr/bin/env python3
"""Check translation parity, format placeholders, XML and all original buttons."""
import re
from pathlib import Path
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
res = root / 'app/src/main/res'
for path in res.rglob('*.xml'):
    ET.parse(path)
a = ET.parse(res / 'values/strings.xml').getroot()
b = ET.parse(res / 'values-pt-rBR/strings.xml').getroot()
def entries(tree):
    return {n.attrib['name']: n for n in tree}
english, portuguese = entries(a), entries(b)
assert english.keys() == portuguese.keys(), 'Translation keys differ'
for key, node in english.items():
    translated = portuguese[key]
    assert node.tag == translated.tag, key
    assert len(node) == len(translated), key
    assert sorted(re.findall(r'%\d+\$[sd]', ''.join(node.itertext()))) == sorted(re.findall(r'%\d+\$[sd]', ''.join(translated.itertext()))), key
source = (root / 'app/src/main/java/io/github/folkzb/hentremote/Command.java').read_text()
buttons = re.findall(r'\("([a-z_]+)", R.string.([a-z_]+)\)', source)
assert len(buttons) == 13, buttons
for _, label in buttons:
    assert label in english, label
print(f'OK: {len(english)} localized resources, 13 original buttons, XML well formed')
