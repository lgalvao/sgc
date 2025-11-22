# Stores (Gerenciamento de Estado - Pinia)

Este diretório contém as **Stores do Pinia**, responsáveis pelo gerenciamento de estado global da aplicação.

As stores atuam como a "fonte da verdade" para os dados que precisam ser compartilhados entre múltiplos componentes ou que precisam persistir durante a navegação.

## Estrutura de uma Store

As stores são definidas usando a sintaxe `defineStore` e, preferencialmente, o estilo "Setup Stores" (similar à Composition API), retornando um objeto com:
- **State (`ref`, `reactive`):** Os dados em si.
- **Getters (`computed`):** Propriedades derivadas ou calculadas a partir do estado.
- **Actions (`function`):** Métodos para modificar o estado, muitas vezes invocando `services` para buscar dados da API.

### Stores Disponíveis

- **`alertas.ts`**: Lista de alertas do usuário e contagem de não lidos.
- **`analises.ts`**: Histórico de análises do subprocesso atual.
- **`atividades.ts`**: Cache ou gestão de atividades sendo editadas.
- **`configuracoes.ts`**: Preferências da aplicação.
- **`mapas.ts`**: Estado do mapa de competências (atividades, conhecimentos) sendo visualizado ou editado. Fundamental para a edição complexa de mapas.
- **`notificacoes.ts`**: Gerencia notificações toast e modais globais.
- **`perfil.ts`**: Dados do usuário logado, perfil selecionado e tokens de autenticação. Persiste dados no `localStorage`.
- **`processos.ts`**: Lista de processos e filtros ativos.
- **`subprocessos.ts`**: Dados do subprocesso atual selecionado.
- **`unidades.ts`**: Árvore de unidades e dados organizacionais.
- **`usuarios.ts`**: Gestão de usuários (para telas de administração).

## Boas Práticas
- **Single Source of Truth:** Evite duplicar dados entre stores e componentes.
- **Stores Pequenas e Focadas:** Prefira várias stores especializadas a uma store gigante "global".
- **Persistência:** A store `perfil` é um exemplo crítico que deve sincronizar seu estado com o `localStorage` para manter o login ativo após o refresh da página.
