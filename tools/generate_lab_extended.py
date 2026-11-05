#!/usr/bin/env python3
"""Generate batches B–D from the pinned IRremoteESP8266 checkout."""
import base64
import concurrent.futures
import gzip
import json
import os
import pathlib
import shutil
import subprocess
import sys
import tempfile
import textwrap

REV = "1e2f0f3ef0a93cbf2a8ddb2e95130f8f4c584b3f"
upstream = pathlib.Path(sys.argv[1]).resolve()
root = pathlib.Path(__file__).resolve().parent.parent
assets = root / "app/src/lab/assets/ir"
assert subprocess.check_output(["git", "-C", str(upstream), "rev-parse", "HEAD"], text=True).strip() == REV

with tempfile.TemporaryDirectory(prefix="hent-ir-") as tmp:
    tmp = pathlib.Path(tmp)
    for folder in ("src", "test"):
        shutil.copytree(upstream / folder, tmp / folder)
    # Instrument the host mock, and the base sender used by some encoders.
    mock = tmp / "test/IRsend_test.h"
    mock.write_text(mock.read_text().replace("class IRsendTest :", "extern void recordEdge(int, unsigned);\n\nclass IRsendTest :")
                    .replace("uint16_t mark(uint16_t usec) {", "uint16_t mark(uint16_t usec) {\n    recordEdge(usec, _freq_unittest);")
                    .replace("void space(uint32_t time) {", "void space(uint32_t time) {\n    recordEdge(-static_cast<int>(time), 0);"))
    base = tmp / "src/IRsend.cpp"
    base.write_text(base.read_text().replace("uint16_t IRsend::mark(uint16_t usec) {",
                    "extern void recordEdge(int, unsigned);\nuint16_t IRsend::mark(uint16_t usec) {\n  recordEdge(usec, _freq_unittest);\n  return 1;")
                    .replace("void IRsend::space(uint32_t time) {",
                    "void IRsend::space(uint32_t time) {\n  recordEdge(-static_cast<int>(time), 0);\n  return;"))
    flags = ["g++", "-std=gnu++11", "-DUNIT_TEST", f"-I{tmp / 'src'}", f"-I{tmp / 'test'}"]
    sources = sorted((tmp / "src").glob("*.cpp")) + [root / "tools/generate_lab_extended.cpp"]

    def compile_unit(source):
        obj = tmp / (source.stem + ".o")
        subprocess.run([*flags, "-c", str(source), "-o", str(obj)], check=True)
        return str(obj)

    with concurrent.futures.ThreadPoolExecutor(max_workers=min(8, os.cpu_count() or 1)) as pool:
        objects = list(pool.map(compile_unit, sources))
    binary = tmp / "generate"
    subprocess.run(["g++", *objects, "-o", str(binary)], check=True)
    rows = [json.loads(line) for line in subprocess.check_output([binary], text=True).splitlines()]

def fingerprint(row):
    # The final idle has no bearing on decoding a single manually sent command.
    return row["hz"], tuple(row["us"][:-1])

seen = {fingerprint(row) for row in json.loads((assets / "batch-a.json").read_text())}
for batch in "BCD":
    kept = []
    for row in rows:
        if row["id"][0] != batch:
            continue
        # Keep OFF paired with its ON for usability, but do not ask the user
        # to retry an identical ON/TOGGLE from another model.
        key = fingerprint(row)
        if row["action"] != "OFF" and key in seen:
            continue
        if row["action"] == "OFF" and not any(x["id"] == row["id"][:-3] + "ON" for x in kept):
            continue
        seen.add(key)
        kept.append(row)
    payload = json.dumps(kept, separators=(",", ":")).encode()
    encoded = base64.b64encode(gzip.compress(payload, mtime=0)).decode()
    (assets / f"batch-{batch.lower()}.json.gz.b64").write_text(
        "\n".join(textwrap.wrap(encoded, 100)) + "\n")
    print(f"{batch}: {len(kept)} signals; {sum(x['action'] != 'OFF' for x in kept)} initial tests")
