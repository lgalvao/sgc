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
            
            # Simple heuristic to guess full class name or just use file path
            full_path = f"{pname}.{sfname}"
            
            if filter_class and filter_class not in full_path:
                continue
            
            missed_lines = []
            partial_lines = []

            for line in sourcefile.findall('line'):
                nr = line.get('nr')
                ci = int(line.get('ci', 0))
                mb = int(line.get('mb', 0))
                cb = int(line.get('cb', 0))

                if ci == 0:
                    missed_lines.append(nr)
                elif mb > 0:
                    # Partial coverage: Executed but missed branches
                    partial_lines.append(f"{nr}({mb}/{mb+cb})")
            
            # Weight: Missed line = 1 point, Partial line = 0.5 point (just for sorting)
            weight = len(missed_lines) + (len(partial_lines) * 0.5)

            if weight > 0:
                results.append({
                    'file': full_path,
                    'missed': missed_lines,
                    'partial': partial_lines,
                    'weight': weight
                })

    # Sort by weight descending
    results.sort(key=lambda x: x['weight'], reverse=True)

    print(f"\n{'='*100}")
    print(f"{'TOP ARQUIVOS COM LINHAS/BRANCHES PERDIDAS':^100}")
    print(f"{'='*100}\n")

    for r in results[:50]:
        print(f"📄 {r['file']}")
        if r['missed']:
            print(f"   🔴 Linhas não executadas: {', '.join(r['missed'])}")
        if r['partial']:
            print(f"   🟡 Branches perdidos (miss/total): {', '.join(r['partial'])}")
        print("-" * 50)

if __name__ == "__main__":
    report_path = "build/reports/jacoco/test/jacocoTestReport.xml"
    # Optional filter argument
    filter_arg = sys.argv[1] if len(sys.argv) > 1 else None
    find_missed_lines(report_path, filter_arg)
