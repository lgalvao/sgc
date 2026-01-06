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

def print_report(xml_path, filter_package=None, min_coverage=95.0):
    if not os.path.exists(xml_path):
        print(f"\nErro: Arquivo {xml_path} não encontrado.")
        print("Certifique-se de rodar: ./gradlew test jacocoTestReport\n")
        return

    tree = ET.parse(xml_path)
    root = tree.getroot()

    print("\n" + "="*105)
    print(f"{'RELATÓRIO DE COBERTURA JACOCO':^105}")
    print("="*105)
    header = f"| {'Componente':<45} | {'Instr. %':^10} | {'Linhas %':^10} | {'Coberto':<10} | {'Total':<10} |"
    print(header)
    print("|" + "-"*47 + "|" + "-"*12 + "|" + "-"*12 + "|" + "-"*12 + "|" + "-"*12 + "|")

    # Global counters
    global_counters = {c.get('type'): {'covered': int(c.get('covered')), 'missed': int(c.get('missed'))} for c in root.findall('counter')}

    low_coverage_classes = []

    # Package-level details
    for package in root.findall('package'):
        pname = package.get('name').replace('/', '.')
        if filter_package and filter_package not in pname:
            continue
        
        metrics = {c.get('type'): {'covered': int(c.get('covered')), 'missed': int(c.get('missed'))} for c in package.findall('counter')}
        
        if 'INSTRUCTION' in metrics:
            inst = metrics['INSTRUCTION']
            line = metrics.get('LINE', {'covered': 0, 'missed': 0})
            
            p_inst = calculate_percentage(inst['covered'], inst['missed'])
            p_line = calculate_percentage(line['covered'], line['missed'])
            total_inst = inst['covered'] + inst['missed']
            
            print(f"| {pname:<45} | {p_inst:>8.2f}% | {p_line:>8.2f}% | {inst['covered']:>10} | {total_inst:>10} |")

        # Check classes
        for cls in package.findall('class'):
            cname = cls.get('name').replace('/', '.')
            c_metrics = {c.get('type'): {'covered': int(c.get('covered')), 'missed': int(c.get('missed'))} for c in cls.findall('counter')}
            
            if 'LINE' in c_metrics:
                l = c_metrics['LINE']
                perc = calculate_percentage(l['covered'], l['missed'])
                if perc < min_coverage:
                    low_coverage_classes.append({
                        'name': cname,
                        'covered': l['covered'],
                        'missed': l['missed'],
                        'total': l['covered'] + l['missed'],
                        'percentage': perc
                    })

    print("|" + "-"*47 + "|" + "-"*12 + "|" + "-"*12 + "|" + "-"*12 + "|" + "-"*12 + "|")
    
    if 'INSTRUCTION' in global_counters:
        inst = global_counters['INSTRUCTION']
        line = global_counters.get('LINE', {'covered': 0, 'missed': 0})
        p_inst = calculate_percentage(inst['covered'], inst['missed'])
        p_line = calculate_percentage(line['covered'], line['missed'])
        total_inst = inst['covered'] + inst['missed']
        print(f"| {'TOTAL DO PROJETO':<45} | {p_inst:>8.2f}% | {p_line:>8.2f}% | {inst['covered']:>10} | {total_inst:>10} |")
    
    print("="*105 + "\n")

    if low_coverage_classes:
        print(f"CLASSES COM COBERTURA DE LINHAS ABAIXO DE {min_coverage}% (Ordenadas por linhas perdidas):")
        print("-" * 105)
        print(f"| {'Classe':<65} | {'Cob. %':^10} | {'Perdidas':^10} | {'Total':^10} |")
        print("-" * 105)
        
        # Sort by missed lines descending
        low_coverage_classes.sort(key=lambda x: x['missed'], reverse=True)
        
        for c in low_coverage_classes[:20]: # Show top 20
            print(f"| {c['name']:<65} | {c['percentage']:>8.2f}% | {c['missed']:>10} | {c['total']:>10} |")
        print("-" * 105 + "\n")

    if 'BRANCH' in global_counters:
        b = global_counters['BRANCH']
        perc_b = calculate_percentage(b['covered'], b['missed'])
        print(f"Cobertura de Branches (Global): {perc_b:.2f}% ({b['covered']}/{b['covered']+b['missed']})")

if __name__ == "__main__":
    report_path = "build/reports/jacoco/test/jacocoTestReport.xml"
    pkg_filter = sys.argv[1] if len(sys.argv) > 1 else None
    min_cov = float(sys.argv[2]) if len(sys.argv) > 2 else 98.0
    print_report(report_path, pkg_filter, min_cov)

