#!/usr/bin/env node

/**
 * Script para gerar testes de cobertura automaticamente
 * 
 * Este script analisa classes sem cobert ura completa e gera
 * esquelet os de testes para atingir 100%
 */

const fs = require('node:fs');
const path = require('node:path');

const SOURCE_DIR = path.join(__dirname, '../../src/main/java');
const TEST_DIR = path.join(__dirname, '../../src/test/java');

const TEMPLATE_COVERAGE_TEST = `package {{PACKAGE}};

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de cobertura para {{CLASS_NAME}}
 * GERADO AUTOMATICAMENTE - Expandir conforme necessário
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("{{CLASS_NAME}} Coverage Tests")
class {{CLASS_NAME}}CoverageTest {

    @InjectMocks
    private {{CLASS_NAME}} target;

    // TODO: Adicionar mocks necessários
    // @Mock
    // private DependencyClass dependency;

    @Nested
    @DisplayName("Cobertura de Métodos Principais")
    class MetodosPrincipais {

        @Test
        @DisplayName("Deve cobrir cenário básico")
        void deveCobrirCenarioBasico() {
            // TODO: Implementar teste
            // Arrange
            // Act
            // Assert
            fail("Teste não implementado");
        }

        @Test
        @DisplayName("Deve cobrir branches não cobertos")
        void deveCobrirBranches() {
            // TODO: Implementar teste para branches específicos
            fail("Teste não implementado");
        }
    }

    @Nested
    @DisplayName("Cobertura de Casos de Erro")
    class CasosDeErro {

        @Test
        @DisplayName("Deve cobrir tratamento de exceções")
        void deveCobrirTratamentoExcecoes() {
            // TODO: Implementar teste
            fail("Teste não implementado");
        }
    }

    // TODO: Adicionar mais testes conforme necessário para atingir 100% de cobertura
    // Linhas não cobertas: {{MISSED_LINES}}
    // Branches não cobertos: {{MISSED_BRANCHES}}
}
`;

function ensureDirectoryExists(dirPath) {
    if (!fs.existsSync(dirPath)) {
        fs.mkdirSync(dirPath, { recursive: true });
    }
}

function generateCoverageTest(className, packageName, missedLines = [], missedBranches = []) {
    const testContent = TEMPLATE_COVERAGE_TEST
        .replaceAll('{{PACKAGE}}', packageName)
        .replaceAll('{{CLASS_NAME}}', className)
        .replaceAll('{{MISSED_LINES}}', missedLines.join(', ') || 'Nenhuma')
        .replaceAll('{{MISSED_BRANCHES}}', missedBranches.join(', ') || 'Nenhum');

    const packagePath = packageName.replaceAll('.', '/');
    const testDir = path.join(TEST_DIR, packagePath);
    ensureDirectoryExists(testDir);

    const testFilePath = path.join(testDir, `${className}CoverageTest.java`);

    if (fs.existsSync(testFilePath)) {
        console.log(`⚠️  Teste já existe: ${testFilePath}`);
        return null;
    }

    fs.writeFileSync(testFilePath, testContent);
    console.log(`✅ Gerado: ${testFilePath}`);
    return testFilePath;
}

function main() {
    const args = process.argv.slice(2);
    
    if (args.length === 0) {
        console.log(`
Uso: node gerar-testes-cobertura.cjs <classe> [opções]

Exemplos:
  node gerar-testes-cobertura.cjs ProcessoFacade
  node gerar-testes-cobertura.cjs sgc.processo.service.ProcessoFacade
  node gerar-testes-cobertura.cjs ProcessoFacade --lines "59,63,68,69"
  node gerar-testes-cobertura.cjs --all  # Gera para todas as classes sem teste

Opções:
  --lines "1,2,3"     Linhas não cobertas
  --branches "1,2,3"  Branches não cobertos
  --all               Gera para todas as classes que precisam
        `);
        return;
    }

    if (args[0] === '--all') {
        console.log('🚀 Gerando testes para todas as classes sem cobertura...');
        console.log('⚠️  Esta funcionalidade requer integração com o relatório JaCoCo');
        console.log('💡 Use o plano-100-cobertura.md como referência');
        return;
    }

    let className = args[0];
    let packageName = 'sgc';

    if (className.includes('.')) {
        const parts = className.split('.');
        className = parts.pop();
        packageName = parts.join('.');
    }

    const linesArg = args.find(a => a.startsWith('--lines'));
    const branchesArg = args.find(a => a.startsWith('--branches'));

    const missedLines = linesArg ? linesArg.split('=')[1]?.split(',') || [] : [];
    const missedBranches = branchesArg ? branchesArg.split('=')[1]?.split(',') || [] : [];

    generateCoverageTest(className, packageName, missedLines, missedBranches);
}

main();
