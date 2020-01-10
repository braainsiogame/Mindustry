import sys
from consts import PACKAGES
for line in sys.stdin.readlines():
	i = line.rfind("/")
	if i != -1 and line[:i] in PACKAGES:
		print(line.strip().replace("/", ".")[:-4] + "class,")