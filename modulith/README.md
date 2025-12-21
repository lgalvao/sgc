# Plano de Adoção do Spring Modulith no SGC

## Visão Geral

Este diretório contém o planejamento completo para adoção do Spring Modulith no projeto SGC. A implementação está dividida em 4 sprints incrementais, com foco em minimizar riscos e validar benefícios progressivamente.

## Estrutura do Diretório

```
modulith/
├── README.md                           # Este arquivo - visão geral do plano
├── modulith-report.md                  # Análise técnica completa e recomendações
├── sprint-01-setup-poc.md              # Sprint 1: Setup e Prova de Conceito
├── sprint-02-foundation-integration.md # Sprint 2: Módulos Foundation e Integration
├── sprint-03-core-domain.md            # Sprint 3: Módulos Core Domain
├── sprint-04-supporting-finalizacao.md # Sprint 4: Módulos Supporting e Finalização
├── contexto-tecnico.md                 # Detalhes de configuração e implementação
├── guia-refatoracao.md                 # Guia passo a passo para refatorar módulos
└── metricas-sucesso.md                 # Métricas e critérios de sucesso
```

## Resumo da Estratégia

### Objetivo
Adotar o Spring Modulith de forma incremental para fortalecer as garantias arquiteturais do SGC, melhorar a comunicação entre módulos via eventos assíncronos, e automatizar a documentação da arquitetura.

### Recomendação
✅ **ADOTAR** - A análise técnica demonstrou que os benefícios justificam o investimento moderado necessário.

### Prioridade
**MÉDIA-ALTA** - O projeto já possui boas práticas, mas se beneficiará significativamente das garantias do Spring Modulith.

### Esforço Estimado
**2-4 semanas** distribuídas em 4 sprints de 1 semana cada.

## Timeline de Implementação

| Sprint | Foco | Duração | Módulos Afetados |
|--------|------|---------|------------------|
| **Sprint 1** | Setup e PoC | 1 semana | `alerta`, `analise` |
| **Sprint 2** | Foundation/Integration | 1 semana | `unidade`, `sgrh` |
| **Sprint 3** | Core Domain | 2 semanas | `mapa`, `atividade`, `processo`, `subprocesso` |
| **Sprint 4** | Supporting e Finalização | 1 semana | `notificacao`, `painel` |

## Principais Benefícios Esperados

1. **Enforcement Arquitetural em Tempo de Compilação**
   - Prevenção automática de violações de limites entre módulos
   - Redução estimada de 70% em bugs arquiteturais

2. **Eventos Assíncronos Resilientes**
   - Event Publication Registry para garantir processamento
   - Retry automático e auditoria de eventos

3. **Documentação Automatizada**
   - Geração de diagramas C4 e PlantUML
   - Documentação sempre sincronizada com o código

4. **Testes Modulares Mais Rápidos**
   - Redução estimada de 30% no tempo de execução
   - Melhor isolamento e clareza de dependências

## Riscos e Mitigações

| Risco | Mitigação |
|-------|-----------|
| Quebra de funcionalidades | Testes abrangentes + refatoração incremental |
| Dependências cíclicas complexas | Começar por módulos simples + estratégias de quebra documentadas |
| Aumento temporário de complexidade | Adoção gradual + treinamento da equipe |

## Como Usar Este Plano

### Para Agentes de IA

Cada arquivo de sprint contém:
- **Contexto detalhado** do projeto e módulos
- **Objetivos específicos** da sprint
- **Tarefas passo a passo** com comandos exatos
- **Critérios de aceite** claros e verificáveis
- **Exemplos de código** antes e depois
- **Comandos de verificação** para validar cada etapa

### Ordem de Execução

1. **Leia primeiro**: `modulith-report.md` para entender o contexto completo
2. **Configure**: `contexto-tecnico.md` para detalhes de implementação
3. **Execute**: Sprints na ordem (01 → 02 → 03 → 04)
4. **Consulte**: `guia-refatoracao.md` durante a refatoração de cada módulo
5. **Valide**: `metricas-sucesso.md` ao final de cada sprint

## Pré-requisitos

- Spring Boot 4.0.1+ (✅ atendido - versão atual: 4.0.1)
- Java 21+ (✅ atendido - versão atual: 21)
- Gradle 8+ (✅ atendido - versão atual: 8.x)
- Conhecimento de Spring Events (✅ já utilizado no projeto)
- Testes de arquitetura com ArchUnit (✅ já implementado)

## Comandos Principais

### Executar testes após mudanças
```bash
./gradlew :backend:test
```

### Verificar estrutura de módulos (após Sprint 1)
```bash
./gradlew :backend:test --tests ModulithStructureTest
```

### Gerar documentação automatizada (após Sprint 4)
```bash
./gradlew :backend:test --tests ModulithDocumentationTest
```

## Métricas de Sucesso

Após implementação completa (6 meses):
- ✅ 0 violações de limites de módulos em build
- ✅ 100% de módulos com estrutura `api/` e `internal/`
- ✅ Documentação arquitetural gerada automaticamente
- ✅ Event Publication Registry configurado
- ✅ Redução de 30%+ no tempo de testes de integração
- ✅ NPS da equipe ≥ 8/10

## Referências

- [Spring Modulith - Documentação Oficial](https://docs.spring.io/spring-modulith/reference/)
- [Spring Modulith - GitHub](https://github.com/spring-projects/spring-modulith)
- Análise completa: `modulith-report.md`
- Padrões do projeto: `/regras/backend-padroes.md`

## Suporte

Para dúvidas ou problemas durante a implementação:
1. Consulte o `guia-refatoracao.md`
2. Revise exemplos no `modulith-report.md` (seção 7)
3. Consulte a documentação oficial do Spring Modulith

---

**Status**: 🟡 Planejamento Completo - Aguardando Início da Implementação  
**Próximo Passo**: Executar Sprint 1 (Setup e PoC)
