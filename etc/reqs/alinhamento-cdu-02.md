# Alinhamento CDU-02 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-02.md`.
- Teste E2E analisado: `e2e/cdu-02.spec.ts` (5 cenários `test`, 5 `test.step`, 160 linhas).
- Contextos `describe` identificados: CDU-02 - Visualizar painel, Como ADMIN, Como GESTOR.

## Cobertura observada no E2E
- ✅ Deve exibir estrutura básica do painel e testar ordenação
- ✅ Deve criar processo e visualizá-lo na tabela
- ✅ Processos "Criado" devem aparecer apenas para ADMIN
- ✅ Não deve incluir unidades INTERMEDIARIAS na seleção
- ✅ Deve validar visualização, alertas e ordenação

## Pontos do requisito sem evidência direta no E2E
- ⚠️ Campos da tabela:
- ⚠️ Regras de exibição e funcionamento: (palavras-chave do requisito: exibição)
- ⚠️ Cabeçalhos das colunas deverão ser clicáveis, possibilitando ordenação em ordem crescente e decrescente. (palavras-chave do requisito: cabeçalhos, colunas, clicáveis, possibilitando)
- ⚠️ Itens da tabela serão clicáveis com estas regras: (palavras-chave do requisito: itens, serão, clicáveis, estas)
- ⚠️ Clicar em processos nas situações 'Em andamento' e 'Finalizado' mostrará as telas Detalhes do processo, caso o (palavras-chave do requisito: processos, situações, andamento, finalizado)
- ⚠️ Na seção `Alertas`, O sistema mostra uma tabela com os alertas registrados pelo sistema que tiverem como destino o usuário logado ou, na ausência desta informação específica, a sua unidade de ativa, ouse seja a unidade que escolheu ao fazer o login no sistema). (palavras-chave do requisito: alertas, registrados, pelo, tiverem)
- ⚠️ Campos da tabela:
- ⚠️ Perfil SERVIDOR: (palavras-chave do requisito: servidor)
- ⚠️ Vê apenas os alertas direcionados ao seu título de eleitor (palavras-chave do requisito: alertas, direcionados, título, eleitor)
- ⚠️ Veem os alertas direcionados a eles (palavras-chave do requisito: veem, alertas, direcionados, eles)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
