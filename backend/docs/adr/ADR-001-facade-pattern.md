# ADR-001: Uso do Padrão Facade para Orquestração de Serviços

**Data**: 2026-01-10  
**Status**: ✅ Aceito e Implementado  
**Decisores**: Equipe de Arquitetura SGC

---

## Contexto e Problema

O sistema SGC possui múltiplos módulos (processo, subprocesso, mapa, atividade) com lógica de negócio complexa. Cada módulo tem vários services especializados que implementam funcionalidades específicas.

**Problemas Identificados:**
1. Controllers acessavam diretamente múltiplos services
2. Lógica de orquestração espalhada nos controllers
3. Difícil testar fluxos completos
4. Violação do Single Responsibility Principle nos controllers
5. Acoplamento excessivo entre controllers e services

---

## Decisão

Adotar o **Padrão Facade** para encapsular a orquestração de services especializados.

**Princípios:**
1. Um Facade por módulo de domínio
2. Controllers usam APENAS Facades
3. Facades orquestram services especializados
4. Services especializados focam em responsabilidade única

---

## Implementação

### Facades Implementadas

| Módulo | Facade | Services | Status |
|--------|--------|----------|--------|
| Processo | ProcessoFacade | 3 | ✅ |
| Subprocesso | SubprocessoFacade | 11 | ✅ |
| Mapa | MapaFacade | 7 | ✅ |
| Atividade | AtividadeFacade | 2 | ✅ |

---

## Consequências

### Positivas ✅
- Controllers mais simples (1 dependência)
- Lógica de orquestração centralizada
- Services mais coesos
- Melhor testabilidade
- Facilita evolução

### Negativas ❌
- Camada adicional
- Possível God Class se mal gerenciada

---

## Conformidade

Testes ArchUnit garantem que controllers usam apenas Facades:
- Ver `sgc.arquitetura.ArchConsistencyTest`

---

**Revisão próxima**: 2026-07-10  
**Autor**: GitHub Copilot AI Agent
