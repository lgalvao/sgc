# Diretrizes de UX, validação e feedback

Este guia registra decisões de produto e engenharia para manter a experiência do SGC previsível sem sacrificar fluxos
específicos que funcionam melhor com tratamento próprio.

## Princípios

- A UX deve priorizar clareza para a pessoa usuária, não consistência absoluta.
- Todo texto visível, mensagem de erro, comentário e identificador novo deve estar em português brasileiro.
- Contratos devem ser diretos: evitar camadas de compatibilidade, aliases de campo e traduções silenciosas entre backend
  e frontend.
- A validação deve aparecer perto do local onde a pessoa consegue corrigir o problema.
- Botões desabilitados devem comunicar indisponibilidade da ação, não esconder validação corrigível.

## Visibilidade e disponibilidade de ações

- Se a ação nunca puder ser realizada pelo perfil atual, o botão ou controle não deve ser renderizado.
- Se o perfil pode realizar a ação, mas a localização, situação de workflow, permissão contextual ou estado de
  carregamento impede a execução agora, o controle deve aparecer desabilitado.
- **Exceção (cadastro de atividades / mapa):** quando o perfil CHEFE (cadastro) ou ADMIN (mapa) possui capacidade estrutural de edição, mas a situação do subprocesso impede a edição naquele momento, os controles de edição **devem ser ocultados** (não apenas desabilitados).
- Ação desabilitada por workflow/localização deve ter motivo acessível e persistente quando o contexto não for óbvio.
- Tooltip pode complementar o motivo, mas não deve ser o único canal quando a informação for essencial.
- Erro corrigível de formulário não deve deixar a ação principal muda; a tentativa deve exibir validação inline ou
  contextual.

## Canais de feedback

- **Campo**: usar para erro de preenchimento, formato, obrigatoriedade e regra local corrigível na própria tela.
- **Bloco/alerta inline**: usar para erro global do formulário, conflito de regra de negócio ou problema sem campo
  único.
- **Erro global de validação**: exibir em `BAlert` no contexto do formulário ou modal, preferencialmente antes dos
  campos ou da área de ações.
- **Toast/notificação de sucesso**: usar apenas para sucesso de operações.
- **Toast para validação**: não usar. Resultado de validação deve ficar inline ou em bloco no contexto da tela.
- **Erro sistêmico**: pode usar alerta global da tela e log técnico com `logger`.

## Validação

- Para formulários simples, validar no envio e exibir erro inline por campo.
- Não desabilitar a ação principal apenas por campo obrigatório vazio ou regra corrigível no formulário; permitir a
  tentativa e mostrar os erros.
- Por padrão, erros de validação aparecem após a primeira tentativa de submissão.
- Após a primeira tentativa, campos inválidos podem revalidar durante a correção.
- Antes da submissão, validação reativa deve ser reservada a regras objetivas e imediatas.
- Validação no `blur` é exceção para campos específicos, não padrão do sistema.
- Se houver pré-bloqueio excepcional por preenchimento mínimo, mostrar ajuda visível indicando a pendência.
- Revalidação reativa é aceitável para regras objetivas, como datas futuras ou limites conhecidos.
- Em modais, manter mensagens dentro do modal, próximas ao campo ou ao grupo afetado.
- Em listas editáveis por item, exibir erro no item afetado e rolar/focar o primeiro problema quando isso ajudar.

## Acessibilidade e visibilidade

- **Ações permanentes**: É vedado o uso de estados de 'hover' (passar o mouse) para ocultar botões de ação essenciais (
  ex. editar e excluir). As ações devem estar permanentemente visíveis para garantir operabilidade plena via teclado e
  dispositivos móveis.
- **Informação de domínio inline**: Dados fundamentais (como a lista de conhecimentos associada a uma atividade) devem
  ser apresentados de forma inline ou estruturada no corpo da página, nunca escondidos atrás de tooltips ou popovers.
- **Proximidade**: Os controles de ação (botões) devem ser posicionados o mais próximo possível do item que eles afetam
  para reduzir a carga cognitiva e facilitar a identificação da funcionalidade.
- **Operabilidade via teclado**: Todo elemento interativo deve ser um elemento nativo de formulário ou botão (como os do
  `BootstrapVueNext`), garantindo que integrem a ordem natural de tabulação.

## Resiliência visual e textos longos

- **Quebras automáticas**: Containers devem ser preparados para acomodar textos longos sem quebra de layout, utilizando
  `overflow-wrap: anywhere` e `word-break: break-word`.
- **Layout flexível**: Priorizar o uso de larguras flexíveis e expansão vertical. Cards e blocos de conteúdo devem
  crescer conforme o volume de dados de domínio.
- **Componentes BVN**: Sempre utilizar componentes da biblioteca `BootstrapVueNext` (ex: `BCard`, `BListGroup`,
  `BButton`) antes de recorrer a estilos manuais, garantindo que o comportamento responsivo e acessível da biblioteca
  seja preservado.

## Backend e contratos

- Preferir DTOs com Bean Validation para entrada de formulário.
- Usar nomes de campos em português e alinhados ao domínio do SGC.
- Usar `codigo` em vez de `id`.
- Erros de entrada devem retornar campos estáveis em português quando houver campo corrigível.
- O contrato alvo para erros estruturados é `erros` com itens contendo `campo` e `mensagem`.

## Exceções saudáveis

- Login pode usar mensagens globais para credenciais inválidas, por segurança.
- Fluxos complexos podem combinar erro por item e erro global quando isso deixar a ação mais clara.
