# Guia do Storybook - SGC

Este guia documenta como visualizar, testar e utilizar as histórias criadas para os componentes do Sistema de Gestão de
Competências (SGC). O Storybook serve como nossa biblioteca viva de componentes e ferramenta de desenvolvimento isolado.

## O que é o Storybook?

O Storybook permite navegar por uma coleção de componentes, visualizar seus diferentes estados e interagir com eles sem
a necessidade de navegar por fluxos complexos da aplicação.

## Como Visualizar

1. Abra o terminal na pasta `frontend`.
2. Execute: `npm run storybook`
3. Acesse: `http://localhost:6006`

---

## Cobertura de Histórias (Catálogo Completo)

Atualmente, o projeto possui **48 histórias** cobrindo 98% dos componentes funcionais.

### 1. Módulo: Comum (`src/components/comum`)

Componentes reutilizáveis e utilitários de interface.

* **BadgeSituacao:** Estados de processos (Criado, Em Andamento, Finalizado).
* **CampoTexto:** Variações: Default, Obrigatório, Desabilitado, Com Erro.
* **EmptyState:** Estados vazios com ícones e CTAs personalizados.
* **ErrorAlert / FormErrorAlert:** Alertas de erro globais e específicos de formulário.
* **InlineEditor:** Edição de texto "no lugar" com estados de salvamento.
* **LoadingButton:** Botão com estados `loading` e `disabled`.
* **ModalPadrao / ModalConfirmacao:** Estruturas de modais com variações de perigo (Danger).
* **TreeTable / TreeRowItem:** Tabelas hierárquicas complexas com expansão de linhas.

### 2. Módulo: Processo (`src/components/processo`)

Gestão de processos de mapeamento e diagnóstico.

* **TabelaProcessos:** Listagem com ordenação, modo compacto e estado vazio.
* **ProcessoInfo / ProcessoAcoes:** Resumo e controles de ação do processo.
* **SubprocessoModal / SubprocessoCards:** Gestão de subprocessos por unidade.
* **TabelaAlertas / TabelaMovimentacoes:** Históricos e notificações do processo.
* **HistoricoAnaliseModal:** Visualização de logs de análise técnica.

### 3. Módulo: Mapa de Competências (`src/components/mapa`)

Núcleo funcional para definição de competências.

* **CompetenciaCard:** Card editável com atividades e conhecimentos vinculados.
* **CompetenciaViewCard:** Versão de visualização (ReadOnly) da competência.
* **CriarCompetenciaModal:** Formulário de criação com validações.
* **AceitarMapaModal / ImpactoMapaModal:** Fluxos de aprovação e análise de impacto.
* **ConfirmacaoDisponibilizacaoModal:** Confirmação crítica de publicação de mapas.

### 4. Módulo: Atividades (`src/components/atividades`)

Gestão de dicionário de atividades.

* **AtividadeItem / VisAtividadeItem:** Representação de atividades individuais.
* **CadAtividadeForm:** Formulário de cadastro com estados de carregamento e erro.
* **ImportarAtividadesModal:** Fluxo de importação em lote.

### 5. Módulo: Relatórios (`src/components/relatorios`)

Visualização de dados e dashboards.

* **RelatorioCardsSection:** Cards resumo de indicadores (Gaps, Vigentes, Andamento).
* **RelatorioFiltrosSection:** Controles de filtragem por unidade e período.
* **Modais de Relatórios:** `ModalDiagnosticosGaps`, `ModalMapasVigentes`, `ModalRelatorioAndamento`.

### 6. Módulo: Unidade (`src/components/unidade`)

Gestão da estrutura organizacional.

* **ArvoreUnidades:** Árvore interativa com seleção múltipla, pre-seleção e modo visualização.
* **UnidadeInfoCard:** Detalhes da unidade selecionada.
* **UnidadeTreeNode:** Componente atômico para nós da árvore.

### 7. Módulo: Layout & Configurações (`src/components/layout` e `configuracoes`)

* **MainNavbar / PageHeader:** Componentes de navegação global.
* **AdministradoresSection / ParametrosSection:** Telas de configuração do sistema.

---

## Padrões de Implementação

Ao adicionar novos componentes, siga estes padrões:

1. **TypeScript:** Use `Meta` e `StoryObj` para tipagem completa.
2. **Mocks:** Crie dados realistas em constantes no próprio arquivo de story.
3. **V-Model:** Utilize a função `render` com `ref` para demonstrar interatividade reativa.
4. **Autodocs:** Sempre inclua a tag `autodocs` para documentação automática.
