#!/usr/bin/env python3
import xml.etree.ElementTree as ET
import sys
import os

def find_missed_lines(xml_path, filter_class=None):
    if not os.path.exists(xml_path):
        print(f"Erro: {xml_path} não encontrado.")
        return

    tree = ET.parse(xml_path)
    root = tree.getroot()

    results = []

    for package in root.findall('package'):
        pname = package.get('name').replace('/', '.')
        for sourcefile in package.findall('sourcefile'):
            sfname = sourcefile.get('name')
            full_path = f"{pname}.{sfname}"
            
            missed_lines = []
            for line in sourcefile.findall('line'):
                ci = int(line.get('ci'))
                if ci == 0:
                    missed_lines.append(line.get('nr'))
            
            if missed_lines:
                results.append({
                    'file': full_path,
                    'lines': missed_lines,
                    'count': len(missed_lines)
                })

    # Sort by count descending
    results.sort(key=lambda x: x['count'], reverse=True)

    for r in results[:30]: # Top 30 files with missed lines
        print(f"{r['file']}: {r['count']} linhas perdidas: {', '.join(r['lines'])}")

if __name__ == "__main__":
    report_path = "build/reports/jacoco/test/jacocoTestReport.xml"
    find_missed_lines(report_path)
