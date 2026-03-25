# Alinhamento CDU-01 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-01.md`.
- Teste E2E analisado: `e2e/cdu-01.spec.ts` (9 cenários `test`, 0 `test.step`, 109 linhas).
- Contextos `describe` identificados: CDU-01 - Realizar login e exibir estrutura das telas.

## Cobertura observada no E2E
- ✅ Deve exibir erro com credenciais inválidas
- ✅ Deve realizar login com sucesso (Perfil único)
- ✅ Deve exibir seleção de perfil se houver múltiplos
- ✅ Deve exibir barra de navegação após login como ADMIN
- ✅ Deve exibir barra de navegação com restrições para GESTOR
- ✅ Deve exibir barra de navegação com restrições para CHEFE
- ✅ Deve exibir barra de navegação com restrições para SERVIDOR
- ✅ Deve exibir informações do usuário e controles
- ✅ Deve exibir rodapé

## Pontos do requisito sem evidência direta no E2E
- ⚠️ O usuário acessa o sistema (palavras-chave do requisito: acessa)
- ⚠️ O sistema verifica título e a senha (autenticação simples) através da API do Sistema 'Acesso AD' do TRE-PE (palavras-chave do requisito: verifica, título, senha, autenticação)
- ⚠️ O sistema determina os perfis disponíveis para o usuário, seguindo estas regras, não exclusivas: (palavras-chave do requisito: determina, disponíveis, seguindo, estas)
- ⚠️ O sistema determina quais pares 'perfil-unidade' se aplicam ao usuário logado, seguindo estas regras: (palavras-chave do requisito: determina, quais, pares, perfil-unidade)
- ⚠️ O usuário seleciona o perfil/unidade com o qual vai atuar (palavras-chave do requisito: seleciona, unidade, qual, atuar)
- ⚠️ Ícone/logotipo do sistema (abre a tela `Painel`) (palavras-chave do requisito: ícone, logotipo, abre, painel)
- ⚠️ Itens adicionais, alinhados à direita: (palavras-chave do requisito: itens, adicionais, alinhados, direita)
- ⚠️ Para todos os perfis:
- ⚠️ Mostrar adicionalmente dois ícones para acesso as telas `Configurações` e `Administradores` (palavras-chave do requisito: mostrar, adicionalmente, dois, ícones)
- ⚠️ O `Conteúdo` compõe a parte central onde serão 'encaixadas' todas as telas. (palavras-chave do requisito: conteúdo, compõe, parte, central)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
