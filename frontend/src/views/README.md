# Diretório de Views

Este diretório contém os componentes de página (Views) do Vue Router. Cada arquivo aqui representa uma tela completa da aplicação.

## Organização

As views são organizadas por domínio funcional ou fluxo de trabalho.

### Autenticação
*   `Login.vue`: Tela de login inicial.
*   `SelecaoPerfil.vue`: Tela intermediária para escolha de perfil/unidade (se o usuário tiver múltiplos).

### Processos
*   `PainelProcessos.vue`: Dashboard principal listando processos ativos.
*   `DetalheProcesso.vue`: Visão geral de um processo específico.

### Subprocessos (Fluxo da Unidade)
*   `WorkflowSubprocesso.vue`: Container principal para o fluxo de trabalho de uma unidade. Gerencia a navegação entre as etapas (Cadastro, Mapa, Validação).
*   `CadAtividades.vue`: Tela de cadastro de atividades e conhecimentos.
*   `MapaCompetencias.vue`: Tela de edição do mapa de competências.

## Padrão de Implementação

As Views devem ser **"Smart Components"**:
1.  Conectam-se às **Stores** (Pinia) para buscar e modificar dados.
2.  Passam dados para **Componentes** (pasta `src/components/`) via props.
3.  Respondem a eventos emitidos pelos componentes filhos.
4.  Não devem conter lógica de negócio complexa ou chamadas diretas à API (use Stores/Services).
