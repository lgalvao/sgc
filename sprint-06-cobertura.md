# Sprint 6: Cobertura JaCoCo e Visibilidade

**Baseado em:** `analise-junit-nova.md` - Onda 6

## Contexto do Projeto SGC

### ✅ JaCoCo Já Está Configurado!

O projeto **já possui JaCoCo totalmente integrado** no `backend/build.gradle.kts`:

```kotlin
plugins {
    java
    jacoco  // ✅ Plugin JaCoCo ativado
    // ...
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)  // ✅ Relatório gerado automaticamente
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)   // ✅ Para CI/CD
        csv.required.set(true)   // ✅ Para análise
        html.required.set(false) // ❌ HTML desabilitado (pode habilitar)
    }
}
```

### Estado Atual
- **Plugin**: JaCoCo configurado e funcional
- **Relatórios XML/CSV**: Gerados automaticamente após testes
- **Relatório HTML**: Atualmente desabilitado
- **Localização**: `backend/build/reports/jacoco/test/`

### O Que Falta (Objetivo deste Sprint)
1. **Habilitar relatório HTML** para visualização local
2. **Configurar quality gate** com limites mínimos de cobertura
3. **Documentar como visualizar** e interpretar relatórios
4. **Integrar ao CI** (se ainda não estiver)

## Objetivo
Melhorar a visibilidade da cobertura de testes e estabelecer quality gates para evitar regressões futuras.

## Tarefas

### 1. Habilitar Relatório HTML
Atualizar `backend/build.gradle.kts`:
```kotlin
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(true)
        html.required.set(true)  // ⬅️ Mudar para true
    }
}
```

### 2. Configurar Quality Gate (Opcional)
Adicionar verificação de cobertura mínima:
```kotlin
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.60".toBigDecimal()  // 60% mínimo (ajustar conforme baseline)
            }
        }
    }
}

// Opcional: fazer check depender da verificação
tasks.named("check") {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
```

**IMPORTANTE**: Antes de configurar o gate, medir a cobertura atual:
```bash
./gradlew :backend:test :backend:jacocoTestReport
# Verificar cobertura atual nos relatórios
```

### 3. Documentar Comandos
Adicionar ao `backend/README.md` ou criar `TESTING.md`:
- Como gerar relatórios
- Onde visualizar (caminho do HTML)
- Como interpretar métricas

## Comandos de Execução

### Executar testes e gerar relatório
```bash
./gradlew :backend:test :backend:jacocoTestReport
```

### Visualizar relatório HTML (após habilitar)
```bash
# Relatório gerado em:
open backend/build/reports/jacoco/test/html/index.html

# Ou no navegador:
# file:///path/to/sgc/backend/build/reports/jacoco/test/html/index.html
```

### Verificar cobertura atual (XML)
```bash
# Ver resumo no XML
cat backend/build/reports/jacoco/test/jacocoTestReport.xml | grep -A 2 '<counter'
```

### Verificar cobertura atual (CSV)
```bash
# Arquivos CSV gerados
ls -lh backend/build/reports/jacoco/test/*.csv
```

### Executar verificação de cobertura (após configurar gate)
```bash
./gradlew :backend:jacocoTestCoverageVerification
```

### Ver estrutura de relatórios
```bash
find backend/build/reports/jacoco -type f
```

## Estrutura de Relatórios JaCoCo

```
backend/build/reports/jacoco/test/
├── jacocoTestReport.xml      # Formato XML (CI/CD)
├── jacocoTestReport.csv      # Formato CSV (análise)
├── html/                     # Relatório HTML interativo
│   ├── index.html            # ⬅️ Página principal
│   ├── sgc/                  # Por pacote
│   │   ├── processo/
│   │   ├── subprocesso/
│   │   └── ...
│   └── jacoco-resources/
```

## Métricas JaCoCo

JaCoCo mede diferentes tipos de cobertura:
- **Linha (Line)**: % de linhas executadas
- **Branch**: % de ramificações (if/switch) testadas
- **Instrução (Instruction)**: % de bytecode executado
- **Complexidade**: Cobertura de caminhos (McCabe)
- **Método**: % de métodos executados
- **Classe**: % de classes tocadas

**Foco principal**: Line e Branch coverage.

## Integração com CI/CD

### GitHub Actions (exemplo)
```yaml
- name: Run tests with coverage
  run: ./gradlew :backend:test :backend:jacocoTestReport

- name: Upload coverage report
  uses: actions/upload-artifact@v3
  with:
    name: jacoco-report
    path: backend/build/reports/jacoco/test/
```

### Quality Gate no CI
```yaml
- name: Check coverage threshold
  run: ./gradlew :backend:jacocoTestCoverageVerification
```

## Estabelecendo Baseline

### Passo 1: Medir cobertura atual
```bash
./gradlew :backend:test :backend:jacocoTestReport
# Abrir HTML e anotar % atual
```

### Passo 2: Definir gate inicial
Configurar limite ligeiramente **abaixo** da cobertura atual (ex: se atual é 65%, usar 60%) para não quebrar build, mas monitorar regressões.

### Passo 3: Meta progressiva
Estabelecer meta de melhoria gradual:
- Baseline: 60%
- Sprint +1: 65%
- Sprint +2: 70%
- Meta final: 80%+

## Critérios de Aceite
- Relatórios de cobertura HTML são gerados com sucesso via `./gradlew :backend:jacocoTestReport`.
- Relatório HTML pode ser aberto e visualizado no navegador.
- Cobertura atual (baseline) está documentada.
- (Opcional) Quality gate configurado com limite inicial não-bloqueante.
- Comandos de geração e visualização documentados no README do backend.

---

## Diretrizes para agentes de IA (Regras de Ouro)

1. **PRs Pequenos:** Um tema por PR.
2. **Critérios Universais de Aceite:**
   - `./gradlew test` (ou `mvn test`) passa.
   - Não aumentar flakiness (nenhum teste novo com `Thread.sleep`).
   - Não reintroduzir `Strictness.LENIENT`.
   - Sem hardcode em integração sem criação explícita.
3. **Não refatorar produção** a menos que estritamente necessário para o teste.

## Guia de Estilo (Obrigatório)

### Estrutura AAA
```java
@Test
@DisplayName("Deve criar processo quando dados válidos")
void deveCriarProcessoQuandoDadosValidos() {
    // Arrange
    // Act
    // Assert
}
```

### Nomenclatura
- **Método:** `deve{Acao}Quando{Condicao}`
- **Variáveis:** Português, descritivas.
- **Agrupamento:** `@Nested` por feature/fluxo.

### Mockito
- **Proibido:** `Strictness.LENIENT` (padrão).
- **Preferência:** Stubs locais.

## Checklist de Revisão

- [ ] Testes passam local/CI.
- [ ] `LENIENT` não aparece no diff.
- [ ] Não houve adição de `Thread.sleep`.
- [ ] Integração não depende de seed global sem setup explícito.
- [ ] PR descreve comandos executados e métricas simples (grep/contagem de arquivos).
