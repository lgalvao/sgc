# CDU-28 - Manter atribuição temporária

**Ator:** ADMIN

## Pré-condições

- Processo com situação 'iniciado' e usuário autenticado com perfil ADMIN.

## Fluxo principal

1. Em qualquer tela, usuário ADMIN clica em `Minha unidade` no menu.
2. O sistema mostra a tela Detalhes de unidade da SEDOC, que inclui a árvore completa de unidades.
3. Usuário ADMIN clica no botão `Criar atribuição`.
4. Sistema apresenta uma tela modal com estes campos:
   - Lista de servidores da unidade selecionada.
   - Campo Início (data)
   - Campo Término (data)
   - Campo Justificativa (texto simples)
   - Botões `Confirmar` e `Cancelar`
5. Usuário ADMIN seleciona o servidor, define as datas e inclui uma justificativa. Todos os campos são obrigatórios.
6. O sistema registra internamente a atribuição temporária e mostra uma confirmação "Atribuição criada".
7. [Faltando criação de notificações e alertas]
8. O usuário que recebe a atribuição temporária passa a ter os mesmos direitos do perfil CHEFE. A atribuição temporária terá prioridade sobre os dados de titularidade importados do SGRH.
