# Guia do Storybook - SGC

Este guia documenta como visualizar, testar e utilizar as histórias criadas para os componentes do Sistema de Gestão de Competências (SGC).

## O que é o Storybook?

O Storybook é uma ferramenta para desenvolvimento de interfaces de usuário (UI) isoladas. Ele permite navegar por uma coleção de componentes, visualizar seus diferentes estados e interagir com eles sem a necessidade de navegar por fluxos complexos da aplicação.

## Como Visualizar

Para iniciar o ambiente do Storybook e visualizar os componentes:

1.  Abra o terminal na raiz do projeto.
2.  Navegue até a pasta `frontend`:
    ```bash
    cd frontend
    ```
3.  Execute o comando:
    ```bash
    npm run storybook
    ```
4.  O Storybook será aberto automaticamente no seu navegador em `http://localhost:6006`.

## Como Testar e Interagir

Dentro da interface do Storybook, você pode realizar testes manuais e verificar o comportamento dos componentes:

*   **Painel de Controles (Controls):** Na aba "Controls", você pode modificar dinamicamente as propriedades (`props`) do componente (como textos, estados de carregamento, desabilitação, etc.) e ver a atualização em tempo real.
*   **Ações (Actions):** A aba "Actions" registra eventos emitidos pelo componente, como cliques (`@click`), mudanças de valor (`update:modelValue`), etc.
*   **Interações (Interactions):** Caso testes de interação automatizados sejam adicionados no futuro, eles aparecerão nesta aba.

## Cobertura de Histórias

Abaixo estão listados os componentes que possuem histórias configuradas, organizados por módulo.

### Módulo: Comum (`frontend/src/components/comum`)

Componentes reutilizáveis em toda a aplicação.

*   **BadgeSituacao** (`BadgeSituacao.stories.ts`)
    *   Visualização de badges para situações de processo (Criado, Em Andamento, Finalizado).
    *   Teste de textos personalizados.
*   **CampoTexto** (`CampoTexto.stories.ts`)
    *   Entrada de texto padrão.
    *   Estados: Obrigatório, Desabilitado, Com Erro (validação).
*   **EmptyState** (`EmptyState.stories.ts`)
    *   Estado vazio padrão.
    *   Variações com ícones e descrições personalizadas.
    *   Exemplo com conteúdo completo (botão de ação).
*   **ErrorAlert** (`ErrorAlert.stories.ts`)
    *   Alertas de erro padrão.
    *   Variações de severidade: Aviso (Warning), Informação (Info).
    *   Exibição de detalhes técnicos do erro.
*   **LoadingButton** (`LoadingButton.stories.ts`)
    *   Botão padrão.
    *   Estado de carregamento (`loading`) com texto alternativo.
    *   Estado desabilitado.
    *   Botão com ícone.
*   **ModalConfirmacao** (`ModalConfirmacao.stories.ts`)
    *   Modal padrão de confirmação.
    *   Variação de perigo (Danger) para ações destrutivas.
    *   Estado de carregamento (Loading) durante a confirmação.
    *   Customização de títulos e botões.

### Módulo: Unidade (`frontend/src/components/unidade`)

*   **ArvoreUnidades** (`ArvoreUnidades.stories.ts`)
    *   **Default:** Árvore interativa com seleção múltipla.
    *   **ComPreSelecao:** Inicialização com itens já selecionados.
    *   **ApenasVisualizacao:** Árvore em modo somente leitura (sem checkboxes).
    *   **OcultandoRaiz:** Exibição da árvore ignorando o nó raiz (mostra apenas os filhos diretos).

### Módulo: Processo (`frontend/src/components/processo`)

*   **TabelaProcessos** (`TabelaProcessos.stories.ts`)
    *   **Default:** Listagem padrão de processos.
    *   **ComDataFinalizacao:** Exibição da coluna de data de finalização e ordenação.
    *   **Compacto:** Versão compacta da tabela (ex: para widgets).
    *   **Vazio:** Estado da tabela sem registros, com Call-to-Action (CTA).

### Módulo: Mapa (`frontend/src/components/mapa`)

*   **CompetenciaCard** (`CompetenciaCard.stories.ts`)
    *   **Default:** Card de competência editável com atividades e conhecimentos.
    *   **ReadOnly:** Modo de visualização (sem controles de edição).
    *   **ComAtividadeSemConhecimento:** Cenário onde uma atividade não possui conhecimentos vinculados.

---

**Observação:** Ao adicionar novos componentes, lembre-se de criar o arquivo `.stories.ts` correspondente para manter esta documentação viva e o catálogo de componentes atualizado.
