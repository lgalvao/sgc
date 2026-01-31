# ADR-001: Uso do Padrão Facade para Orquestração de Serviços

---

## Contexto e Problema

O sistema SGC possui múltiplos módulos (processo, subprocesso, mapa, atividade) com lógica de negócio complexa. Cada
módulo tem vários services especializados que implementam funcionalidades específicas.

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


## Consequências

### Positivas ✅

- Controllers mais simples
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
