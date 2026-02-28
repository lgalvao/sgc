# Diretório de Views

Este diretório contém os componentes de página (Views) do Vue Router. Cada arquivo aqui representa uma tela completa da
aplicação.

## Organização

As views são organizadas por domínio funcional ou fluxo de trabalho.

### Autenticação

* **LoginView.vue**: Tela de login inicial.

### Painel e Processos

* **PainelView.vue**: Dashboard principal do sistema.
* **ProcessoView.vue**: Detalhes e gestão de um processo específico.
* **CadProcesso.vue**: Formulário de criação de novos processos.
* **HistoricoView.vue**: Visão histórica de processos finalizados.
* **ConfiguracoesView.vue**: Configurações globais do sistema.

### Unidades

* **UnidadeView.vue**: Gestão e visualização de detalhes de uma unidade.

### Fluxo de Subprocesso (Workflow)

* **SubprocessoView.vue**: Container principal para o fluxo de uma unidade dentro de um processo.

#### Etapa de Cadastro

* **CadAtividades.vue**: Cadastro e edição de atividades da unidade.
* **VisAtividades.vue**: Visualização de atividades (somente leitura).
* **CadAtribuicao.vue**: Cadastro de atribuições temporárias.

#### Etapa de Mapa

* **CadMapa.vue**: Edição do mapa de competências.
* **VisMapa.vue**: Visualização do mapa de competências.

#### Etapa de Diagnóstico

* **AutoavaliacaoDiagnostico.vue**: Tela para o servidor realizar sua autoavaliação.
* **MonitoramentoDiagnostico.vue**: Acompanhamento do progresso do diagnóstico pela chefia.
* **OcupacoesCriticasDiagnostico.vue**: Identificação de ocupações com gaps críticos.
* **ConclusaoDiagnostico.vue**: Encerramento e resumo do diagnóstico.

### Relatórios

* **RelatoriosView.vue**: Central de relatórios gerenciais.

## Padrão de Implementação

As Views devem ser **"Smart Components"**:

1. **Estado**: Conectam-se às **Stores** (Pinia) para buscar e modificar dados.
2. **Comunicação**: Passam dados para componentes filhos via `props` e respondem a `emits`.
3. **Lógica**: Utilizam **Composables** para encapsular lógica de UI complexa.
4. **Responsabilidade**: Não devem conter chamadas diretas ao Axios (delegar para Stores/Services).
