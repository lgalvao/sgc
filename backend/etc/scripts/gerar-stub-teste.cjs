#!/usr/bin/env node
const fs = require('node:fs');
const path = require('node:path');
const xml2js = require('xml2js');
const BASE_DIR = path.join(__dirname, '../..');
const REPORT_PATH = path.join(BASE_DIR, 'build/reports/jacoco/test/jacocoTestReport.xml');
const SRC_DIR = path.join(BASE_DIR, 'src/main/java');
const TEST_DIR = path.join(BASE_DIR, 'src/test/java');
async function main() {
    const target = process.argv[2];
    if (!target) { console.log('Uso: node gerar-stub-teste.cjs <NomeDaClasse>'); process.exit(1); }
    const data = fs.readFileSync(REPORT_PATH);
    const parser = new xml2js.Parser();
    const result = await parser.parseStringPromise(data);
    const pkg = result.report.package.find(p => p.sourcefile.some(sf => sf.$.name.replace('.java','') === target || (p.$.name.replace(/\//g,'.') + '.' + sf.$.name.replace('.java','')) === target));
    if (!pkg) { console.error('❌ Classe não encontrada'); process.exit(1); }
    const sf = pkg.sourcefile.find(f => f.$.name.replace('.java','') === target || (pkg.$.name.replace(/\//g,'.') + '.' + f.$.name.replace('.java','')) === target);
    const pkgName = pkg.$.name.replace(/\//g,'.');
    const className = sf.$.name.replace('.java','');
    const srcPath = path.join(SRC_DIR, pkg.$.name, sf.$.name);
    const srcContent = fs.readFileSync(srcPath, 'utf-8');
    const deps = [];
    srcContent.split('\n').forEach(l => { const m = l.match(/private\s+final\s+([\w<>]+)\s+(\w+);/); if (m) deps.push({type:m[1], name:m[2]}); });
    const testPath = path.join(TEST_DIR, pkg.$.name, className + 'CoverageTest.java');
    let code = `package ${pkgName};\n\nimport org.junit.jupiter.api.*;\nimport org.junit.jupiter.api.extension.ExtendWith;\nimport org.mockito.*;\nimport org.mockito.junit.jupiter.MockitoExtension;\nimport static org.assertj.core.api.Assertions.*;\nimport static org.mockito.Mockito.*;\n\n@ExtendWith(MockitoExtension.class)\n@DisplayName("${className} - Cobertura de Testes")\nclass ${className}CoverageTest {\n\n    @InjectMocks\n    private ${className} target;\n\n`;
    deps.forEach(d => { code += `    @Mock\n    private ${d.type} ${d.name};\n\n`; });
    code += `    @Test\n    @DisplayName("Deve cobrir lacunas do método")\n    void deveCobrirLacunas() {\n        // TODO: Implementar cobertura\n    }\n}\n`;
    if (!fs.existsSync(testPath)) {
        fs.mkdirSync(path.dirname(testPath), {recursive:true});
        fs.writeFileSync(testPath, code);
        console.log('✅ Stub gerado em: ' + testPath);
    } else { console.log('⚠️ Arquivo existente: ' + testPath); }
}
main().catch(console.error);