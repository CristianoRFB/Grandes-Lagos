#!/usr/bin/env python3
"""Fail closed unless every required P01 suite actually ran without skips."""

from pathlib import Path
import sys
import xml.etree.ElementTree as ET


REQUIRED = {
    "IdentityContractTest": 3,
    "IdentitySecurityIntegrationTest": 16,
    "IdentityHttpIntegrationTest": 5,
    "IdentityPolicyTest": 4,
    "IdentityMigrationTest": 28,
}


def verify(directory: Path) -> int:
    total = 0
    for name, minimum in REQUIRED.items():
        report = directory / f"TEST-com.gloperations.{name}.xml"
        suite = ET.parse(report).getroot()
        if suite.tag != "testsuite" or suite.get("name") != f"com.gloperations.{name}":
            raise ValueError(f"Wrong suite in {report.name}")
        count = int(suite.attrib["tests"])
        cases = suite.findall("testcase")
        if count < minimum or len(cases) != count:
            raise ValueError(f"{name}: expected at least {minimum} executed cases, found {count}")
        for result in ("failures", "errors", "skipped"):
            if int(suite.attrib[result]) != 0:
                raise ValueError(f"{name}: {result} must be zero")
        if any(case.find(result) is not None for case in cases for result in ("failure", "error", "skipped")):
            raise ValueError(f"{name}: non-passing testcase")
        print(f"PASS: {name}: {count} tests, no failures/errors/skips")
        total += count
    return total


if __name__ == "__main__":
    try:
        if len(sys.argv) != 2:
            raise ValueError("usage: check-p01-reports.py <fresh-surefire-reports-directory>")
        total = verify(Path(sys.argv[1]))
    except (OSError, ET.ParseError, ValueError, KeyError) as error:
        print(f"P01 gate FAILED: {error}", file=sys.stderr)
        sys.exit(1)
    print(f"P01 gate passed: {total} required test executions")
