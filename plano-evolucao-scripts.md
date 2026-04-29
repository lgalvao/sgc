# Plano de Evolução: Toolkit de Scripts do SGC

Este plano visa transformar o toolkit de scripts de uma coleção de utilitários fragmentados em um ecossistema de alta efetividade, focado em **automação de correção**, **consolidação de inteligência** e **higiene do repositório**.

## 1. Visão Geral (Objetivos)

1.  **Reduzir a Fadiga de Ferramentas:** Consolidar múltiplos scripts de diagnóstico em comandos únicos e acionáveis.
2.  **Mudar para Correção Ativa (Auto-Fix):** Evoluir de "apontadores de erro" para "agentes de correção".
3.  **Higiene Crítica:** Eliminar scripts obsoletos de transição que não agregam valor contínuo.
4.  **Integração Profunda:** Promover auditorias avulsas para portões de qualidade mandatórios (CI/Pre-commit).

---

## 2. Eixos de Evolução

### 2.1 Consolidação de Cobertura e Qualidade (Backend & Frontend)
Atualmente, a inteligência de cobertura está dispersa em ~10 arquivos.
- **Ação:** Fundir `cobertura-analisar`, `cobertura-complexidade`, `cobertura-lacunas`, `cobertura-plano` e `cobertura-priorizar` em um único comando: **`sgc backend cobertura auditoria`**.
- **Resultado Esperado:** Um único relatório (JSON/Markdown) que cruza cobertura baixa + complexidade alta + impacto de negócio, entregando um "Top 5 Pendências" prioritário.

### 2.2 Ativação de Auto-Fix
Transformar auditorias passivas em corretores ativos.
- **Ação:** Adicionar flag `--fix` aos comandos:
    - `frontend mensagens analisar`: Remover chaves órfãs e ordenar chaves automaticamente.
    - `backend java auditar-null`: Injetar anotações `@Nullable` (JSpecify) em locais detectados com segurança.
    - `frontend test-ids-duplicados`: Renomear IDs duplicados seguindo padrão semântico (se seguro).
- **Resultado Esperado:** Redução do trabalho braçal de manutenção e aumento da velocidade de correção de dívida técnica.

### 2.3 Promoção de Auditorias para Portões de Qualidade
Auditorias importantes hoje são opcionais e manuais.
- **Ação:** Integrar os seguintes scripts no fluxo de `qa snapshot coletar` (que roda no CI):
    - `validacoes-auditar.js`: Garantir que restrições do Backend (ex: `@Size`) batem com as do Frontend.
    - `test-ids-duplicados.js`: Evitar falhas em cascata em testes E2E.
- **Resultado Esperado:** Prevenção de bugs de sincronia e regressões visuais antes que cheguem em Produção.

### 2.4 Expurgos de Scripts de Transição
Vários scripts foram úteis em refatorações passadas, mas hoje geram ruído.
- **Ação:** Deletar permanentemente:
    - `codigo/id-legado-identificar.js` (Assumindo que a migração para `codigo` está concluída).
    - `codigo/title-case-identificar.js` e `corrigir.js` (Tarefas de higiene visual já executadas).
    - `codigo/comentarios-limpar-*` (Substituir por regras de linter se necessário).
- **Resultado Esperado:** Menor superfície de manutenção e diretórios mais limpos.

---

## 3. Cronograma de Implementação

### Fase 1: Higiene e Consolidação (Agora)
- [ ] Deletar scripts obsoletos identificados.
- [ ] Criar o comando unificado de cobertura e desativar os scripts fragmentados.

### Fase 2: Ativação de Auto-Fix e Portões (Próximo Passo)
- [ ] Implementar `--fix` no analisador de mensagens e auditoria de nulos.
- [ ] Vincular auditorias de validação ao snapshot de QA.

### Fase 3: Refinamento de UX
- [ ] Garantir que o comando `sgc` sugira os novos comandos consolidados e oculte os legados.

---

## 4. Critérios de Sucesso
1.  **Redução de 40%** no número total de arquivos em `etc/scripts` sem perda de funcionalidade.
2.  **Zero intervenção manual** necessária para corrigir problemas detectados por scripts de mensagens e FQN.
3.  **Interrupção de CI** automática caso haja divergência de validação entre Frontend e Backend.
