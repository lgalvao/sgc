# Stores (Gerenciamento de Estado - Pinia)

Última atualização: 2025-12-14

Este diretório contém as **Stores do Pinia**, responsáveis pelo gerenciamento de estado global da aplicação.

As stores atuam como a "fonte da verdade" para os dados que precisam ser compartilhados entre múltiplos componentes ou
que precisam persistir durante a navegação.

## Estrutura de uma Store

As stores são definidas usando a sintaxe `defineStore` utilizando o estilo **Setup Stores** (similar à Composition API).

A função de setup deve retornar um objeto contendo:

- **State (`ref`, `reactive`):** Os dados em si.
- **Getters (`computed`):** Propriedades derivadas ou calculadas a partir do estado.
- **Actions (`function`):** Métodos para modificar o estado, muitas vezes invocando `services` para buscar dados da API.

### Stores Disponíveis

- **`alertas.ts`**: Lista de alertas do usuário e contagem de não lidos.
- **`analises.ts`**: Histórico de análises do subprocesso atual.
- **`atividades.ts`**: Cache ou gestão de atividades sendo editadas.
- **`atribuicoes.ts`**: Gerencia o estado das atribuições temporárias de unidades.
- **`configuracoes.ts`**: Preferências da aplicação.
- **`mapas.ts`**: Estado do mapa de competências (atividades, conhecimentos) sendo visualizado ou editado. Fundamental
  para a edição complexa de mapas.
- **`notificacoes.ts`**: Gerencia notificações toast e modais globais.
- **`perfil.ts`**: Dados do usuário logado, perfil selecionado e tokens de autenticação. Persiste dados no
  `localStorage` para manter a sessão.
- **`processos.ts`**: Lista de processos e filtros ativos.
- **`subprocessos.ts`**: Dados do subprocesso atual selecionado.
- **`unidades.ts`**: Árvore de unidades e dados organizacionais.
- **`usuarios.ts`**: Gestão de usuários (para telas de administração e cache).

## Boas Práticas

- **Single Source of Truth:** Evite duplicar dados entre stores e componentes.
- **Stores Pequenas e Focadas:** Prefira várias stores especializadas a uma store gigante.

## Detalhamento técnico (gerado em 2025-12-14)

Resumo detalhado dos artefatos, comandos e observações técnicas gerado automaticamente.
