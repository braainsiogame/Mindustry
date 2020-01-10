import sys
from consts import PACKAGES
exports = {}
for line in sys.stdin.readlines():
    i = line.rfind("/")
    if i != -1:
        package = line[:i]
        name = line[i + 1:-6]
        if package in PACKAGES and name != "":
            exports.setdefault(package, set()).add(name)
for package in exports:
    print("export { " + ", ".join(exports[package]) + " } from \"./Packages/" + package + "\";")