# Diretrizes de UX, validação e feedback

Este guia registra decisões de produto e engenharia para manter a experiência do SGC previsível sem sacrificar fluxos específicos que funcionam melhor com tratamento próprio.

## Princípios

- A UX deve priorizar clareza para a pessoa usuária, não consistência absoluta.
- Todo texto visível, mensagem de erro, comentário e identificador novo deve estar em português brasileiro.
- Contratos devem ser diretos: evitar camadas de compatibilidade, aliases de campo e traduções silenciosas entre backend e frontend.
- A validação deve aparecer perto do local onde a pessoa consegue corrigir o problema.
- Botões desabilitados devem comunicar indisponibilidade da ação, não esconder validação corrigível.

## Visibilidade e disponibilidade de ações

- Se a ação nunca puder ser realizada pelo perfil atual, o botão ou controle não deve ser renderizado.
- Se o perfil pode realizar a ação, mas a localização, situação de workflow, permissão contextual ou estado de carregamento impede a execução agora, o controle deve aparecer desabilitado.
- Ação desabilitada por workflow/localização deve ter motivo acessível e persistente quando o contexto não for óbvio.
- Tooltip pode complementar o motivo, mas não deve ser o único canal quando a informação for essencial.
- Erro corrigível de formulário não deve deixar a ação principal muda; a tentativa deve exibir validação inline ou contextual.

## Canais de feedback

- **Campo**: usar para erro de preenchimento, formato, obrigatoriedade e regra local corrigível na própria tela.
- **Bloco/alerta inline**: usar para erro global do formulário, conflito de regra de negócio ou problema sem campo único.
- **Erro global de validação**: exibir em `BAlert` no contexto do formulário ou modal, preferencialmente antes dos campos ou da área de ações.
- **Toast/notificação de sucesso**: usar apenas para sucesso de operações.
- **Toast para validação**: não usar. Resultado de validação deve ficar inline ou em bloco no contexto da tela.
- **Erro sistêmico**: pode usar alerta global da tela e log técnico com `logger`.

## Validação

- Para formulários simples, validar no envio e exibir erro inline por campo.
- Não desabilitar a ação principal apenas por campo obrigatório vazio ou regra corrigível no formulário; permitir a tentativa e mostrar os erros.
- Por padrão, erros de validação aparecem após a primeira tentativa de submissão.
- Após a primeira tentativa, campos inválidos podem revalidar durante a correção.
- Antes da submissão, validação reativa deve ser reservada a regras objetivas e imediatas.
- Validação no `blur` é exceção para campos específicos, não padrão do sistema.
- Se houver pré-bloqueio excepcional por preenchimento mínimo, mostrar ajuda visível indicando a pendência.
- Revalidação reativa é aceitável para regras objetivas, como datas futuras ou limites conhecidos.
- Em modais, manter mensagens dentro do modal, próximas ao campo ou ao grupo afetado.
- Em listas editáveis por item, exibir erro no item afetado e rolar/focar o primeiro problema quando isso ajudar.

## Backend e contratos

- Preferir DTOs com Bean Validation para entrada de formulário.
- Usar nomes de campos em português e alinhados ao domínio do SGC.
- Usar `codigo` em vez de `id` em contratos novos.
- Evitar aceitar nomes antigos em paralelo quando o contrato correto puder ser ajustado.
- Erros de entrada devem retornar campos estáveis em português quando houver campo corrigível.
- O contrato alvo para erros estruturados é `erros` com itens contendo `campo` e `mensagem`.
- O contrato legado `subErrors` não deve ser usado em código novo nem aceito como alias permanente.

## Exceções saudáveis

- Login pode usar mensagens globais para credenciais inválidas, por segurança.
- Falhas de permissão, rede ou estado inesperado não precisam parecer validação de campo.
- Fluxos complexos podem combinar erro por item e erro global quando isso deixar a ação mais clara.
