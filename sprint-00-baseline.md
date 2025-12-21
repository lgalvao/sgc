# Sprint 0: Baseline e Guardrails

**Baseado em:** `analise-junit-nova.md` - Onda 0

## Contexto do Projeto SGC

O **SGC (Sistema de Gestão de Competências)** é um sistema corporativo desenvolvido com:
- **Backend:** Java 21, Spring Boot 4.0.1, Hibernate, JPA, H2 (testes)
- **Build System:** Gradle 9.2.1 (usa Gradle Wrapper)
- **Framework de Testes:** JUnit 5, Mockito, AssertJ, Spring Boot Test
- **Estrutura:** Modular Monolith com módulos de domínio (processo, subprocesso, mapa, atividade, etc)

### Estatísticas Atuais
- **98 arquivos de teste** (`*Test.java`) em `backend/src/test/java/sgc/`
- **30 testes de integração** em `backend/src/test/java/sgc/integracao/`
- **478 anotações @DisplayName** (múltiplos usos indicam boa padronização)
- **56 anotações @Nested** para organização de testes
- **1 ocorrência** de `Strictness.LENIENT` encontrada
- **0 testes parametrizados** (`@ParameterizedTest`) - oportunidade para Sprint 7
- Testes separados em: unitários (`@ExtendWith(MockitoExtension.class)`) e integração (`@SpringBootTest`)

### Arquitetura de Módulos
O backend está organizado em módulos de domínio em `backend/src/main/java/sgc/`:
- `processo` - Orquestrador central do sistema
- `subprocesso` - Máquina de estados e workflow
- `mapa` - Mapas de competências
- `atividade` - Atividades e conhecimentos associados
- `analise` - Auditoria e revisão de processos
- `notificacao` e `alerta` - Comunicação reativa (eventos)
- `sgrh` e `unidade` - Estrutura organizacional e usuários
- `painel` - Visualizações e dashboards
- `comum` - Componentes transversais (erros, config, util)

### Referências
- Arquitetura detalhada: `backend/README.md`
- Convenções do projeto: `AGENTS.md`
- Análise completa: `analise-junit-nova.md`

---

## Objetivo
Garantir medições e diagnóstico rápido antes de iniciar as refatorações pesadas.

## Tarefas
- Garantir pipeline rodando testes e publicando relatórios.
- Documentar comandos de execução.
- Validar que o ambiente de testes está funcionando corretamente.

## Comandos de Execução

### Executar todos os testes do backend
```bash
./gradlew :backend:test
```

### Gerar relatório de cobertura JaCoCo (já configurado)
```bash
./gradlew :backend:jacocoTestReport
```
Relatório gerado em: `backend/build/reports/jacoco/test/html/index.html`

### Executar verificação de qualidade completa
```bash
./gradlew :backend:qualityCheck
```

### Verificar uso de LENIENT
```bash
grep -R "Strictness.LENIENT" backend/src/test --include="*.java"
```

### Contar testes com @DisplayName
```bash
grep -R "@DisplayName" backend/src/test --include="*.java" | wc -l
```

### Contar testes com @Nested
```bash
grep -R "@Nested" backend/src/test --include="*.java" | wc -l
```

## Critérios de Aceite
- `./gradlew :backend:test` executa com sucesso.
- JaCoCo gera relatórios de cobertura (já está configurado no `backend/build.gradle.kts`).
- Comandos de verificação documentados e validados.

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
