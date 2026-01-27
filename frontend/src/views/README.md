# Diretório de Views

Este diretório contém os componentes de página (Views) do Vue Router. Cada arquivo aqui representa uma tela completa da
aplicação.

## Organização

As views são organizadas por domínio funcional ou fluxo de trabalho.

### Autenticação

* **LoginView.vue**: Tela de login inicial.

### Painel e Processos

* **PainelView.vue**: Dashboard principal (Home).
* **ProcessoView.vue**: Detalhes e gestão de um processo específico.
* **CadProcesso.vue**: Formulário de criação de novos processos.
* **HistoricoView.vue**: Visão histórica de processos passados.
* **ConfiguracoesView.vue**: Configurações do sistema.

### Unidades

* **UnidadeView.vue**: Gestão e visualização de unidades.

### Fluxo de Subprocesso (Workflow)

* **SubprocessoView.vue**: Container principal (Layout) para o fluxo de uma unidade.

#### Etapa de Cadastro

* **CadAtividades.vue**: Cadastro de atividades.
* **VisAtividades.vue**: Visualização (somente leitura) de atividades.
* **CadAtribuicao.vue**: Cadastro de atribuições temporárias.

#### Etapa de Mapa

* **CadMapa.vue**: Edição do mapa de competências.
* **VisMapa.vue**: Visualização do mapa de competências.

#### Etapa de Diagnóstico

* **AutoavaliacaoDiagnostico.vue**: Tela de autoavaliação.
* **MonitoramentoDiagnostico.vue**: Acompanhamento do diagnóstico pela chefia.
* **ConclusaoDiagnostico.vue**: Encerramento do diagnóstico.
* **OcupacoesCriticasDiagnostico.vue**: Identificação de ocupações críticas.

### Relatórios

* **RelatoriosView.vue**: Central de relatórios do sistema.

## Padrão de Implementação

As Views devem ser **"Smart Components"**:

1. Conectam-se às **Stores** (Pinia) para buscar e modificar dados.
2. Passam dados para **Componentes** (pasta `src/components/`) via props.
3. Respondem a eventos emitidos pelos componentes filhos.
4. Não devem conter lógica de negócio complexa ou chamadas diretas à API (use Stores/Services).