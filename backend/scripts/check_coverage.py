#!/usr/bin/env python3
import xml.etree.ElementTree as ET
import sys
import os

def calculate_percentage(covered, missed):
    total = covered + missed
    if total == 0:
        return 0.0
    return (covered / total) * 100

def format_row(name, covered, missed, total, percentage):
    return f"| {name:<40} | {covered:>7} | {missed:>7} | {total:>7} | {percentage:>6.2f}% |"

def print_report(xml_path, filter_package=None):
    if not os.path.exists(xml_path):
        print(f"Erro: Arquivo {xml_path} não encontrado.")
        return

    tree = ET.parse(xml_path)
    root = tree.getroot()

    print("\n" + "="*85)
    print(f"{'RELATÓRIO DE COBERTURA JACOCO':^85}")
    print("="*85)
    header = f"| {'Componente':<40} | {'Coberto':<7} | {'Faltou':<7} | {'Total':<7} | {' % ':^7} |"
    print(header)
    print("|" + "-"*42 + "|" + "-"*9 + "|" + "-"*9 + "|" + "-"*9 + "|" + "-"*9 + "|")

    # Global counters are at the end of the report (last children of root)
    global_counters = {}
    for counter in root.findall('counter'):
        ctype = counter.get('type')
        global_counters[ctype] = {
            'covered': int(counter.get('covered')),
            'missed': int(counter.get('missed'))
        }

    # Package-level details
    for package in root.findall('package'):
        pname = package.get('name').replace('/', '.')
        if filter_package and filter_package not in pname:
            continue
        
        # We look for the summary counters of the package (direct children)
        for counter in package.findall('counter'):
            if counter.get('type') == 'INSTRUCTION':
                covered = int(counter.get('covered'))
                missed = int(counter.get('missed'))
                total = covered + missed
                perc = calculate_percentage(covered, missed)
                print(format_row(pname, covered, missed, total, perc))

    print("|" + "-"*42 + "|" + "-"*9 + "|" + "-"*9 + "|" + "-"*9 + "|" + "-"*9 + "|")
    
    # Final global summary
    if 'INSTRUCTION' in global_counters:
        c = global_counters['INSTRUCTION']
        total = c['covered'] + c['missed']
        perc = calculate_percentage(c['covered'], c['missed'])
        print(format_row("TOTAL DO PROJETO", c['covered'], c['missed'], total, perc))
    
    print("="*85 + "\n")

    if 'BRANCH' in global_counters:
        b = global_counters['BRANCH']
        perc_b = calculate_percentage(b['covered'], b['missed'])
        print(f"Cobertura de Branches: {perc_b:.2f}% ({b['covered']}/{b['covered']+b['missed']})")

if __name__ == "__main__":
    report_path = "build/reports/jacoco/test/jacocoTestReport.xml"
    pkg_filter = sys.argv[1] if len(sys.argv) > 1 else None
    print_report(report_path, pkg_filter)
