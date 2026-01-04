# Plano de Cobertura de Testes Frontend

O objetivo deste plano é aumentar a cobertura de testes do frontend para 95%, com foco especial em branches, funções e linhas de código não cobertas.

## Progresso Atual (Views)

### Views Atualizadas

#### `src/views/ProcessoView.vue`
- **Cobertura Anterior**: 38.84% (Linhas)
- **Cobertura Atual**:
  - Statements: 75%
  - Branches: 63.29%
  - Functions: 80%
  - Lines: 75.53%
- **Ações Realizadas**:
  - Criado `src/views/__tests__/ProcessoView.spec.ts`.
  - Mockado `vue-router` e serviços (`subprocessoService`).
  - Testados carregamento, exibição, navegação e ações em bloco (aceitar/homologar).

#### `src/views/ConfiguracoesView.vue`
- **Cobertura Anterior**: 38.88% (Linhas)
- **Cobertura Atual**:
  - Statements: 88.17%
  - Branches: 93.75%
  - Functions: 75%
  - Lines: 88.88%
- **Ações Realizadas**:
  - Criado `src/views/__tests__/ConfiguracoesView.spec.ts`.
  - Mockados stores (`configuracoes`, `perfil`, `feedback`) e serviços (`administradorService`, `axios-setup`).
  - Cobertos cenários de:
    - Carregamento inicial (sucesso e falha).
    - Edição e salvamento de configurações (validação, sucesso, erro).
    - Gerenciamento de administradores (listar, adicionar, remover, permissões).

#### `src/views/SubprocessoView.vue`
- **Cobertura Anterior**: 53.03% (Linhas)
- **Cobertura Atual**:
  - Statements: 92.64%
  - Branches: 78.33%
  - Functions: 94.44%
  - Lines: 93.93%
- **Ações Realizadas**:
  - Criado `src/views/__tests__/SubprocessoView.spec.ts` (substituindo/criando novo).
  - Mockados stores (`subprocessos`, `mapas`, `feedback`) e serviços (`processoService`).
  - Cobertos cenários de:
    - Carregamento inicial e renderização condicional.
    - Modal de alteração de data limite (permissões, sucesso, erro).
    - Reabertura de cadastro/revisão (validação, sucesso, erro).
    - Envio de lembretes.

#### `src/views/CadAtividades.vue`
- **Cobertura Anterior**: 77.5% (Linhas)
- **Cobertura Atual**:
  - Statements: 76.5%
  - Branches: 70.8%
  - Functions: 66.07%
  - Lines: 78.18%
- **Ações Realizadas**:
  - Criado `src/views/__tests__/CadAtividades.spec.ts`.
  - Mockados serviços de `atividadeService`, `subprocessoService`, `cadastroService`.
  - Cobertos fluxos principais: Adição, Remoção, Edição de Atividade/Conhecimento e Disponibilização.
  - Testes com stores reais (pinia stubActions: false) mas com serviços mockados.

## Próximos Passos (Prioridade)

1.  **Views (Componentes de Página)**
    - `src/views/VisAtividades.vue` (82.53% Linhas) - Alvo para chegar a >90%.

2.  **Componentes UI**
    - `src/components/ProcessoHeader.vue`
    - `src/components/AcaoBloco.vue`

3.  **Utilitários**
    - `src/mappers/processos.ts`

## Estratégia Continuada

Manter o foco em criar testes que simulem cenários reais de uso (mockando serviços) para garantir que a lógica de negócio no frontend esteja robusta.
