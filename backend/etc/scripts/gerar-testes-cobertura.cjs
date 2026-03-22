#!/usr/bin/env node

/**
 * Script para gerar esqueletos de testes de cobertura para uma classe específica
 * Uso: node backend/etc/scripts/gerar-testes-cobertura.cjs <NomeDaClasse>
 * Exemplo: node backend/etc/scripts/gerar-testes-cobertura.cjs AlertaFacade
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
        console.error("💡 Execute './gradlew :backend:test :backend:jacocoTestReport' primeiro.");
        process.exit(1);
    }
    const data = fs.readFileSync(filePath);
    const parser = new xml2js.Parser();
    return parser.parseStringPromise(data);
}

function findClassInReport(report, className) {
    const packages = report.report.package || [];
    for (const pkg of packages) {
        const sourceFiles = pkg.sourcefile || [];
        for (const sf of sourceFiles) {
            if (sf.$.name.replace('.java', '') === className || 
                (pkg.$.name.replaceAll('/', '.') + '.' + sf.$.name.replace('.java', '')) === className) {
                return { pkg: pkg.$.name.replaceAll('/', '.'), sf };
            }
        }
    }
    return null;
}

function findSourceFile(packageName, fileName) {
    const fullPath = path.join(SRC_DIR, packageName.replaceAll('.', '/'), fileName);
    if (fs.existsSync(fullPath)) return fullPath;
    return null;
}

function getMissingLines(sf) {
    const missed = [];
    if (sf.line) {
        sf.line.forEach(line => {
            if (parseInt(line.$.ci || 0) === 0) {
                missed.push(parseInt(line.$.nr));
            }
        });
    }
    return missed;
}

function extractMethodInfo(sourceContent, lineNumbers) {
    const lines = sourceContent.split('\n');
    const methods = new Map();

    lineNumbers.forEach(nr => {
        // Procurar o método que contém esta linha (andando para trás)
        for (let i = nr - 1; i >= 0; i--) {
            const line = lines[i].trim();
            const methodMatch = line.match(/(?:public|protected|private|static|\s) +[\w<>\[\], ]+ +(\w+) *\(/);
            if (methodMatch && !line.includes('class ') && !line.includes('return ')) {
                const methodName = methodMatch[1];
                if (!methods.has(methodName)) {
                    methods.set(methodName, []);
                }
                methods.get(methodName).push(nr);
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
        console.error(`❌ Classe '${target}' não encontrada no relatório de cobertura.`);
        process.exit(1);
    }

    const { pkg, sf } = classInfo;
    const fileName = sf.$.name;
    const className = fileName.replace('.java', '');
    const sourcePath = findSourceFile(pkg, fileName);

    if (!sourcePath) {
        console.error(`❌ Arquivo fonte não encontrado para ${pkg}.${fileName}`);
        process.exit(1);
    }

    const sourceContent = fs.readFileSync(sourcePath, 'utf-8');
    const missingLines = getMissingLines(sf);
    
    if (missingLines.length === 0) {
        console.log(`✅ A classe ${className} já tem 100% de cobertura de linhas!`);
        process.exit(0);
    }

    const methods = extractMethodInfo(sourceContent, missingLines);

    // Gerar esqueleto do teste
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

    // TODO: Adicione @Mock para as dependências da classe

    @BeforeEach
    void setUp() {
        // Inicialização se necessário
    }

`;

    methods.forEach((lines, methodName) => {
        testCode += `    @Test
    @DisplayName("Deve cobrir as linhas [${lines.join(', ')}] do método ${methodName}")
    void deveCobrir${methodName.charAt(0).toUpperCase() + methodName.slice(1)}() {
        // TODO: Implementar teste para cobrir as linhas ${lines.join(', ')}
        // 1. Configurar mocks
        // 2. Executar método
        // 3. Verificar resultados
    }

`;
    });

    testCode += `}\n`;

    const testPath = path.join(TEST_DIR, pkg.replaceAll('.', '/'), `${className}CoverageTest.java`);
    
    console.log(`\n🚀 Plano de Cobertura para ${className}:`);
    console.log(`📍 Linhas não cobertas: ${missingLines.join(', ')}`);
    console.log(`📍 Métodos afetados: ${Array.from(methods.keys()).join(', ')}`);
    
    if (fs.existsSync(testPath)) {
        console.log(`\n⚠️ O arquivo de teste já existe: ${testPath}`);
        console.log(`Considere adicionar os novos casos de teste a ele.`);
        console.log(`\n--- Sugestão de Código ---\n`);
        console.log(testCode);
    } else {
        fs.mkdirSync(path.dirname(testPath), { recursive: true });
        fs.writeFileSync(testPath, testCode);
        console.log(`\n✅ Esqueleto de teste gerado em: ${testPath}`);
    }
}

main().catch(err => {
    console.error('❌ Erro:', err);
    process.exit(1);
});
