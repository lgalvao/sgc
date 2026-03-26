#!/usr/bin/env node

/**
 * 🛠️ Gerador de Testes de Cobertura Inteligente
 * 
 * Gera esqueletos de testes Mockito com detecção automática de:
 * 1. Dependências (para gerar @Mock)
 * 2. Métodos afetados por linhas não cobertas
 * 3. Branches parciais (decisões não testadas)
 */

const fs = require('node:fs');
const path = require('node:path');
const xml2js = require('xml2js');

const BASE_DIR = path.join(__dirname, '../..');
const REPORT_PATH = path.join(BASE_DIR, 'build/reports/jacoco/test/jacocoTestReport.xml');
const SRC_DIR = path.join(BASE_DIR, 'src/main/java');
const TEST_DIR = path.join(BASE_DIR, 'src/test/java');

async function parseXml(filePath) {
    if (!fs.existsSync(filePath)) {
        console.error(`❌ Relatório não encontrado: ${filePath}`);
        process.exit(1);
    }
    const data = fs.readFileSync(filePath);
    const parser = new xml2js.Parser();
    return parser.parseStringPromise(data);
}

function findClassInReport(report, target) {
    const packages = report.report.package || [];
    for (const pkg of packages) {
        const sourceFiles = pkg.sourcefile || [];
        for (const sf of sourceFiles) {
            const pkgName = pkg.$.name.replaceAll('/', '.');
            const simpleName = sf.$.name.replace('.java', '');
            if (simpleName === target || `${pkgName}.${simpleName}` === target) {
                return { pkg: pkgName, sf };
            }
        }
    }
    return null;
}

function extractDependencies(sourceContent) {
    const dependencies = [];
    const lines = sourceContent.split('\n');
    
    // Procura por campos private final (padrão @RequiredArgsConstructor)
    lines.forEach(line => {
        const match = line.match(/private\s+final\s+([\w<>]+)\s+(\w+);/);
        if (match) {
            dependencies.push({ type: match[1], name: match[2] });
        }
    });
    
    return dependencies;
}

function getGaps(sf) {
    const missedLines = [];
    const missedBranches = [];
    
    if (sf.line) {
        sf.line.forEach(line => {
            const nr = parseInt(line.$.nr);
            const ci = parseInt(line.$.ci || 0);
            const mb = parseInt(line.$.mb || 0);
            const cb = parseInt(line.$.cb || 0);
            
            if (ci === 0) missedLines.push(nr);
            if (mb > 0) missedBranches.push({ nr, mb, total: mb + cb });
        });
    }
    return { missedLines, missedBranches };
}

function mapToMethods(sourceContent, gaps) {
    const lines = sourceContent.split('\n');
    const methods = new Map();
    const allGapLines = new Set([...gaps.missedLines, ...gaps.missedBranches.map(b => b.nr)]);

    allGapLines.forEach(nr => {
        for (let i = nr - 1; i >= 0; i--) {
            const line = lines[i].trim();
            const methodMatch = line.match(/(?:public|protected|private|static|\s) +[\w<>[\] ]+ +(\w+) *\(/);
            if (methodMatch && !line.includes('class ') && !line.includes('return ') && !line.includes('new ')) {
                const methodName = methodMatch[1];
                if (!methods.has(methodName)) {
                    methods.set(methodName, { lines: [], branches: [] });
                }
                if (gaps.missedLines.includes(nr)) methods.get(methodName).lines.push(nr);
                const br = gaps.missedBranches.find(b => b.nr === nr);
                if (br) methods.get(methodName).branches.push(br);
                break;
            }
        }
    });
    return methods;
}

async function main() {
    const target = process.argv[2];
    if (!target) {
        console.log('Uso: node gerar-testes-cobertura.cjs <NomeDaClasse>');
        process.exit(1);
    }

    const report = await parseXml(REPORT_PATH);
    const classInfo = findClassInReport(report, target);

    if (!classInfo) {
        console.error(`❌ Classe '${target}' não encontrada no relatório.`);
        process.exit(1);
    }

    const { pkg, sf } = classInfo;
    const className = sf.$.name.replace('.java', '');
    const sourcePath = path.join(SRC_DIR, pkg.replaceAll('.', '/'), sf.$.name);
    const sourceContent = fs.readFileSync(sourcePath, 'utf-8');

    const gaps = getGaps(sf);
    const deps = extractDependencies(sourceContent);
    const methodMap = mapToMethods(sourceContent, gaps);

    let testCode = `package ${pkg};

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("${className} - Cobertura de Testes")
class ${className}CoverageTest {

    @InjectMocks
    private ${className} target;

`;

    deps.forEach(d => {
        testCode += `    @Mock
    private ${d.type} ${d.name};

`;
    });

    methodMap.forEach((info, methodName) => {
        const desc = [];
        if (info.lines.length > 0) desc.push(`linhas [${info.lines.join(', ')}]`);
        if (info.branches.length > 0) desc.push(`branches [${info.branches.map(b => b.nr).join(', ')}]`);

        testCode += `    @Test
    @DisplayName("Deve cobrir ${desc.join(' e ')} do método ${methodName}")
    void deveCobrir${methodName.charAt(0).toUpperCase() + methodName.slice(1)}() {
        // TODO: Implementar cobertura para:
`;
        if (info.lines.length > 0) testCode += `        // Linhas: ${info.lines.join(', ')}
`;
        if (info.branches.length > 0) info.branches.forEach(b => {
            testCode += `        // Branch na linha ${b.nr}: ${b.mb} de ${b.total} decisões faltantes\n`;
        });
        
        testCode += `        
        // 1. Configurar mocks
        // 2. Executar método
        // 3. Verificar resultados
    }

`;
    });

    testCode += `}
`;

    const testPath = path.join(TEST_DIR, pkg.replaceAll('.', '/'), `${className}CoverageTest.java`);
    
    console.log(`\n🚀 Gerando plano para ${className}...`);
    if (deps.length > 0) console.log(`📦 Detectadas ${deps.length} dependências para Mock.`);
    console.log(`📍 Métodos afetados: ${Array.from(methodMap.keys()).join(', ') || 'Nenhum (verifique exclusões)'}`);

    if (fs.existsSync(testPath)) {
        console.log(`\n⚠️ Arquivo existente: ${testPath}`);
        console.log(`--- Sugestão de Código ---

${testCode}`);
    } else {
        fs.mkdirSync(path.dirname(testPath), { recursive: true });
        fs.writeFileSync(testPath, testCode);
        console.log(`\n✅ Esqueleto gerado com sucesso em: ${testPath}`);
    }
}

main().catch(console.error);
