"""Synthetic report fixtures verify the CI guard, not the application."""

import contextlib
import importlib.util
import io
from pathlib import Path
import tempfile
import unittest
import xml.etree.ElementTree as ET

spec = importlib.util.spec_from_file_location("p01_reports", Path(__file__).with_name("check-p01-reports.py"))
guard = importlib.util.module_from_spec(spec)
spec.loader.exec_module(guard)


class ReportGuardTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.directory = Path(self.temporary.name)
        for name, count in guard.REQUIRED.items():
            suite = ET.Element("testsuite", name=f"com.gloperations.{name}", tests=str(count),
                               failures="0", errors="0", skipped="0")
            for number in range(count):
                ET.SubElement(suite, "testcase", name=f"synthetic-{number}")
            ET.ElementTree(suite).write(self.directory / f"TEST-com.gloperations.{name}.xml")
        self.first = self.directory / "TEST-com.gloperations.IdentityContractTest.xml"

    def verify(self):
        with contextlib.redirect_stdout(io.StringIO()):
            return guard.verify(self.directory)

    def test_accepts_complete_successful_reports(self):
        self.assertEqual(self.verify(), sum(guard.REQUIRED.values()))

    def test_rejects_missing_report(self):
        self.first.unlink()
        with self.assertRaises(OSError):
            self.verify()

    def test_rejects_skips_failures_errors_and_insufficient_counts(self):
        original = self.first.read_bytes()
        for attribute in ("skipped", "failures", "errors", "tests"):
            with self.subTest(attribute=attribute):
                suite = ET.fromstring(original)
                suite.set(attribute, "1")
                ET.ElementTree(suite).write(self.first)
                with self.assertRaises(ValueError):
                    self.verify()
        self.first.write_bytes(original)

    def test_rejects_hidden_skip_even_if_summary_claims_success(self):
        suite = ET.parse(self.first).getroot()
        ET.SubElement(suite.find("testcase"), "skipped")
        ET.ElementTree(suite).write(self.first)
        with self.assertRaises(ValueError):
            self.verify()

    def test_rejects_malformed_report(self):
        self.first.write_text("<broken", encoding="utf-8")
        with self.assertRaises(ET.ParseError):
            self.verify()


if __name__ == "__main__":
    unittest.main()
