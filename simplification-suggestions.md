# Sugestões de Simplificação

**Objetivo:** reduzir sobreengenharia e custo de manutenção em uma aplicação de intranet de baixa concorrência (5-10
usuários simultâneos), sem quebrar contratos existentes.

## Diagnóstico consolidado (investigação atual)

### 1) Backend com concentração excessiva de responsabilidades

- `backend/src/main/java/sgc/subprocesso/SubprocessoController.java` tem **549 linhas**.
- `backend/src/main/java/sgc/subprocesso/service/SubprocessoService.java` tem **1704 linhas**.
- Foram identificados fluxos com regra de negócio duplicada entre Controller e Service (ex.: disponibilização de
  cadastro/revisão de cadastro).

**Risco atual:** acoplamento com HTTP, duplicação de validação e aumento da chance de divergência de comportamento.

### 2) Camada de Facades ainda extensa para o porte da aplicação

Foram encontrados **8 Facades** no backend:

- `AlertaFacade`
- `AtividadeFacade`
- `OrganizacaoFacade`
- `UsuarioFacade`
- `ProcessoFacade`
- `PainelFacade`
- `RelatorioFacade`
- `LoginFacade`

Parte dessas classes agrega valor (orquestração real), mas outra parte tende a ser apenas delegação.

**Risco atual:** camadas adicionais para manutenção sem ganho claro de domínio.

### 3) Frontend com stores globais em volume alto para CRUD simples

Foram encontrados **13 stores** em `frontend/src/stores/*.ts`:

- `alertas`, `analises`, `atividades`, `atribuicoes`, `configuracoes`, `diagnosticos`, `feedback`, `mapas`,
  `perfil`, `processos`, `subprocessos`, `unidades`, `usuarios`.

**Risco atual:** estado espelhado do servidor, risco de stale data e maior custo para evoluir telas simples.

---

## Backlog acionável de simplificação

## Fase 1 (baixo risco, alto retorno)

1. **Remover regra de negócio do Controller no fluxo de disponibilização de cadastro/revisão**
    - **Escopo:** `SubprocessoController` + `SubprocessoService`.
    - **Ação:** Controller delega; Service centraliza validação e payload de erro.
    - **Critério de aceite:** nenhum endpoint monta regra manual no Controller para este fluxo.

2. **Padronizar validações de prontidão de cadastro em ponto único**
    - **Escopo:** métodos que verificam atividades/conhecimentos no `SubprocessoService`.
    - **Ação:** reutilizar uma validação única para evitar mensagens e formatos divergentes.
    - **Critério de aceite:** erro funcional equivalente em todos os fluxos de disponibilização.

3. **Mapear Facades pass-through para remoção incremental**
    - **Escopo inicial:** `OrganizacaoFacade`, `UsuarioFacade`, `LoginFacade`.
    - **Ação:** catalogar métodos que só delegam e medir impacto em controllers.
    - **Critério de aceite:** lista priorizada de remoções sem alteração de comportamento.

## Fase 2 (médio prazo)

4. **Reduzir stores Pinia de leitura simples**
    - **Escopo inicial:** entidades com fetch pontual (ex.: consultas administrativas).
    - **Ação:** mover para composables/services com estado local da tela.
    - **Critério de aceite:** redução de stores globais sem perda de UX.

5. **Quebrar serviços com excesso de responsabilidades**
    - **Escopo inicial:** `SubprocessoService`.
    - **Ação:** extrair serviços focados por fluxo (`cadastro`, `revisão`, `validação`).
    - **Critério de aceite:** redução gradual de linhas e cobertura preservada.

---

## Área escolhida para início da simplificação (em andamento)

### Tema
**Fluxo de disponibilização de cadastro/revisão de cadastro de subprocesso**.

### Motivo da escolha

- É uma simplificação localizada, de baixo risco, e com benefício arquitetural imediato.
- Elimina duplicação entre camadas e aproxima o padrão desejado: Controller fino + regra no Service.

### Próximos passos imediatos

1. Consolidar validação de atividades sem conhecimento no Service.
2. Remover do Controller a lógica de consulta/formatação para erro de validação desse fluxo.
3. Ajustar testes de Controller para validar delegação.
4. Registrar aprendizados e próximos incrementos em `simplification-status.md`.
