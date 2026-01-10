# Relatório de Cobertura de Testes - Backend

Este relatório apresenta o estado atual da cobertura de testes do backend, após a implementação de novos testes para cobrir os gaps identificados.

## Resumo Executivo (Atualizado)

| Métrica | Valor Anterior | Valor Atual | Status |
|---------|----------------|-------------|--------|
| **Cobertura de Linhas** | 95.35% | 96.07% | 🟢 Melhorou |
| **Cobertura de Instruções** | 94.40% | 95.15% | 🟢 Melhorou |
| **Cobertura de Branches** | 84.62% | 86.63% | 🟢 Melhorou |
| **Total de Testes** | ~1089 | 1127+ | 🟢 Aumentou |

## Gaps Resolvidos ✅

Os seguintes serviços tiveram seus gaps de cobertura endereçados:

### 1. `MapaSalvamentoService.java`
- **Gaps Resolvidos:** Remoção de competências obsoletas (linhas 102-106), erro de entidade inexistente (135-136), validação de atividades (188) e logs de integridade (216-217).
- **Status:** Cobertura de Instruções em **97.34%** e Linhas em **98.94%**.

### 2. `AccessControlService.java`
- **Gaps Resolvidos:** Usuário nulo em `podeExecutar`, motivos de negação para todos os tipos (Processo, Atividade, Mapa) e tratamento de recurso desconhecido.
- **Status:** Cobertura de Linhas na classe principal em **~100%**.

### 3. `PainelService.java`
- **Gaps Resolvidos:** Ordenação padrão já existente, erros na busca de unidades, visibilidade de unidades e cálculo de link de destino.
- **Status:** Cobertura na classe em **~100%**.

### 4. `LoginService.java`
- **Gaps Resolvidos:** Falha de AD em produção, propagação de `ErroAutenticacao` e negação de acesso no método entrar.
- **Status:** Cobertura de Linhas na classe em **~95%**.

### 5. `UsuarioService.java`
- **Gaps Resolvidos:** Contexto de segurança nulo, building de DTO com usuário null e extração de título de principal.
- **Status:** Cobertura de Linhas em **99.13%**.

---
*Relatório atualizado em: 10/01/2026*
