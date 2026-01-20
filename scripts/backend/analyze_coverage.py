import os
import re
import xml.etree.ElementTree as ET

RANKING_FILE = 'complexity-ranking.md'
JACOCO_FILE = 'backend/build/reports/jacoco/test/jacocoTestReport.xml'
OUTPUT_FILE = 'classes-needing-tests.md'

def parse_ranking():
    classes = []
    try:
        with open(RANKING_FILE, 'r', encoding='utf-8') as f:
            lines = f.readlines()

        # Find the table "Top 50 Classes Mais Complexas"
        in_table = False
        for line in lines:
            if '| Rank | Classe |' in line:
                in_table = True
                continue
            if in_table and line.strip().startswith('|'):
                # Handle potential markdown alignment row
                if '---' in line:
                    continue

                parts = [p.strip() for p in line.split('|') if p.strip()]
                if len(parts) >= 5: # At least up to complexity
                    # Rank | Classe | Pacote | Score | Complexity | ...
                    try:
                        classes.append({
                            'rank': int(parts[0]),
                            'class': parts[1].replace('`', ''),
                            'package': parts[2].replace('`', ''),
                            'score': float(parts[3]),
                            'complexity': int(parts[4])
                        })
                    except ValueError:
                        continue

            if in_table and not line.strip():
                break # End of table
    except Exception as e:
        print(f"Error parsing ranking: {e}")
    return classes

def parse_coverage():
    coverage_map = {}
    try:
        tree = ET.parse(JACOCO_FILE)
        root = tree.getroot()

        for package in root.findall('package'):
            pkg_name = package.get('name') # e.g. sgc/organizacao
            for cls in package.findall('class'):
                cls_name = cls.get('name') # e.g. sgc/organizacao/UsuarioFacade
                simple_name = cls_name.split('/')[-1]

                counters = cls.findall('counter')
                branch_coverage = 0.0
                missed = 0
                covered = 0
                has_branches = False

                for c in counters:
                    if c.get('type') == 'BRANCH':
                        has_branches = True
                        missed = int(c.get('missed'))
                        covered = int(c.get('covered'))
                        total = missed + covered
                        if total > 0:
                            branch_coverage = (covered / total) * 100
                        else:
                            branch_coverage = 100.0

                if not has_branches:
                    branch_coverage = 100.0

                coverage_map[simple_name] = {
                    'branch_coverage': branch_coverage,
                    'missed': missed,
                    'covered': covered
                }
    except Exception as e:
        print(f"Error parsing coverage: {e}")
    return coverage_map

def generate_report(ranked_classes, coverage_map):
    needing_tests = []

    for cls in ranked_classes:
        name = cls['class']
        if name in coverage_map:
            cov = coverage_map[name]
            if cov['branch_coverage'] < 80.0:
                cls['coverage'] = cov['branch_coverage']
                cls['missed_branches'] = cov['missed']
                needing_tests.append(cls)
        else:
            # Maybe not found in report (e.g. interface or no tests run)
            # Or coverage map key mismatch
            cls['coverage'] = 0.0
            cls['missed_branches'] = '?'
            needing_tests.append(cls)

    with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
        f.write("# Classes Needing Regression Tests\n\n")
        f.write("Top complex classes with < 80% branch coverage:\n\n")
        f.write("| Rank | Class | Score | Complexity | Coverage | Missed Branches |\n")
        f.write("|------|-------|-------|------------|----------|-----------------|\n")

        if not needing_tests:
            f.write("\nNo classes found with < 80% coverage among top 50.\n")

        for item in needing_tests:
            cov_str = f"{item['coverage']:.1f}%"
            f.write(f"| {item['rank']} | `{item['class']}` | {item['score']} | {item['complexity']} | {cov_str} | {item['missed_branches']} |\n")

if __name__ == '__main__':
    print("Analyzing coverage...")
    ranked = parse_ranking()
    print(f"Parsed {len(ranked)} ranked classes.")
    coverage = parse_coverage()
    print(f"Parsed coverage for {len(coverage)} classes.")
    generate_report(ranked, coverage)
    print(f"Report generated: {OUTPUT_FILE}")
