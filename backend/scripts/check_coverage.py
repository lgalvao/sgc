#!/usr/bin/env python3
import os
import sys
import xml.etree.ElementTree as ET


def calculate_percentage(covered, missed):
    total = covered + missed
    if total == 0:
        return 0.0
    return (covered / total) * 100

def get_counters(element):
    return {c.get('type'): {'covered': int(c.get('covered')), 'missed': int(c.get('missed'))} for c in element.findall('counter')}

def process_package(package, filter_package):
    pname = package.get('name').replace('/', '.')
    if filter_package and filter_package not in pname:
        return None, []
    
    metrics = get_counters(package)
    low_cov_classes = []

    if 'INSTRUCTION' in metrics:
        inst = metrics['INSTRUCTION']
        line = metrics.get('LINE', {'covered': 0, 'missed': 0})
        
        p_inst = calculate_percentage(inst['covered'], inst['missed'])
        p_line = calculate_percentage(line['covered'], line['missed'])
        total_inst = inst['covered'] + inst['missed']
        
        print(f"| {pname:<45} | {p_inst:>8.2f}% | {p_line:>8.2f}% | {inst['covered']:>10} | {total_inst:>10} |")

    for cls in package.findall('class'):
        c_metrics = get_counters(cls)
        if 'LINE' in c_metrics:
            l = c_metrics['LINE']
            perc = calculate_percentage(l['covered'], l['missed'])
            
            cname = cls.get('name').replace('/', '.')

            if perc < 100.0: # Collect all non-100 for potential sorting/filtering
                 low_cov_classes.append({
                    'name': cname,
                    'covered': l['covered'],
                    'missed': l['missed'],
                    'total': l['covered'] + l['missed'],
                    'percentage': perc,
                    'metrics': c_metrics # Store full metrics for later branch processing
                })
    return metrics, low_cov_classes

def print_class_details(low_coverage_classes, min_coverage):
    # Filter by user specified min_coverage
    filtered_classes = [c for c in low_coverage_classes if c['percentage'] < min_coverage]
    
    if not filtered_classes:
        return

    # Sort by missed branches descending (or missed lines if no branches)
    # Enrich with branch data
    for c in filtered_classes:
        c['missed_branches'] = 0
        c['total_branches'] = 0
        c['branch_percentage'] = 100.0
        
        if 'BRANCH' in c['metrics']:
             b_metrics = c['metrics']['BRANCH']
             c['missed_branches'] = b_metrics['missed']
             c['total_branches'] = b_metrics['covered'] + b_metrics['missed']
             c['branch_percentage'] = calculate_percentage(b_metrics['covered'], b_metrics['missed'])

    filtered_classes.sort(key=lambda x: x.get('missed_branches', 0), reverse=True)
    
    print(f"| {'Classe':<60} | {'Linhas %':^10} | {'Missed L':^10} | {'Branches %':^10} | {'Missed B':^10} |")
    print("-" * 115)

    for c in filtered_classes[:20]: # Show top 20
        branch_cov_str = f"{c.get('branch_percentage', 100.0):>8.2f}%" if c.get('total_branches', 0) > 0 else "N/A"
        print(f"| {c['name']:<60} | {c['percentage']:>8.2f}% | {c['missed']:>10} | {branch_cov_str:>10} | {c.get('missed_branches', 0):>10} |")
    print("-" * 115 + "\n")

def print_global_summary(global_counters):
    if 'INSTRUCTION' in global_counters:
        inst = global_counters['INSTRUCTION']
        line = global_counters.get('LINE', {'covered': 0, 'missed': 0})
        p_inst = calculate_percentage(inst['covered'], inst['missed'])
        p_line = calculate_percentage(line['covered'], line['missed'])
        total_inst = inst['covered'] + inst['missed']
        print(f"| {'TOTAL DO PROJETO':<45} | {p_inst:>8.2f}% | {p_line:>8.2f}% | {inst['covered']:>10} | {total_inst:>10} |")
    
    print("="*105 + "\n")

    if 'BRANCH' in global_counters:
        b = global_counters['BRANCH']
        perc_b = calculate_percentage(b['covered'], b['missed'])
        print(f"Cobertura de Branches (Global): {perc_b:.2f}% ({b['covered']}/{b['covered']+b['missed']})")

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
    print(f"| {'Componente':<45} | {'Instr. %':^10} | {'Linhas %':^10} | {'Coberto':<10} | {'Total':<10} |")
    print("|" + "-"*47 + "|" + "-"*12 + "|" + "-"*12 + "|" + "-"*12 + "|" + "-"*12 + "|")

    global_counters = get_counters(root)
    all_low_cov_classes = []

    for package in root.findall('package'):
        _, classes = process_package(package, filter_package)
        all_low_cov_classes.extend(classes)

    print("|" + "-"*47 + "|" + "-"*12 + "|" + "-"*12 + "|" + "-"*12 + "|" + "-"*12 + "|")
    
    print_global_summary(global_counters)
    print_class_details(all_low_cov_classes, min_coverage)

if __name__ == "__main__":
    report_path = "build/reports/jacoco/test/jacocoTestReport.xml"
    pkg_filter = sys.argv[1] if len(sys.argv) > 1 else None
    min_cov = float(sys.argv[2]) if len(sys.argv) > 2 else 98.0
    print_report(report_path, pkg_filter, min_cov)

