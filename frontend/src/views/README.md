# Views (Páginas da Aplicação)

Este diretório contém os componentes de **página** (Views) do Vue.js. As views são os componentes de nível superior que são carregados diretamente pelo Vue Router.

## Responsabilidades de uma View

Diferente dos componentes "burros" (`src/components`), as views são "inteligentes":
1.  **Orquestração:** Conectam o estado global (`stores`) aos componentes de UI.
2.  **Ciclo de Vida:** Gerenciam o carregamento inicial de dados (geralmente no `onMounted`).
3.  **Layout:** Definem a estrutura macro da página (títulos, disposição dos componentes).

## Views Principais

- **`LoginView.vue`**: Tela de login.
- **`PainelView.vue`**: Dashboard principal do usuário.
- **`ProcessoView.vue`**: Listagem e gestão de processos.
- **`SubprocessoView.vue`**: Tela de detalhes de uma unidade dentro de um processo (o "coração" operacional do sistema).
- **`CadMapa.vue` / `VisMapa.vue`**: Telas para edição e visualização do mapa de competências.
- **`CadAtividades.vue`**: Tela para cadastro de atividades e conhecimentos.
- **`DiagnosticoEquipe.vue` / `OcupacoesCriticas.vue`**: Telas específicas para o processo de Diagnóstico.
- **`HistoricoView.vue`**: Visualização de históricos passados.
- **`RelatoriosView.vue`**: Geração e visualização de relatórios gerenciais.
- **`ConfiguracoesView.vue`**: Configurações do sistema (apenas ADMIN).
