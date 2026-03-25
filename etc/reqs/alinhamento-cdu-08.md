# Alinhamento CDU-08 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-08.md`.
- Teste E2E analisado: `e2e/cdu-08.spec.ts` (3 cenários `test`, 12 `test.step`, 246 linhas).
- Contextos `describe` identificados: CDU-08 - Manter cadastro de atividades e conhecimentos.

## Cobertura observada no E2E
- ✅ Cenário 1: Processo de Mapeamento (Fluxo completo + Importação + Auto-save)
- ✅ Cenário 2: Processo de Revisão (Botão impacto)
- ✅ Cenário 3: Seleções limpas ao trocar processo/unidade no modal de importação

## Pontos do requisito sem evidência direta no E2E
- ⚠️ O usuário pode incluir primeiro várias atividades e depois os conhecimentos correspondentes; ou trabalhar em uma atividade por vez até concluir todos os seus conhecimentos. O sistema deve permitir os dois modos de trabalho. (palavras-chave do requisito: pode, incluir, primeiro, várias)
- ⚠️ Para cada atividade já cadastrada, ao passar o mouse, o sistema exibe botões de edição e remoção. (palavras-chave do requisito: atividade, cadastrada, passar, mouse)
- ⚠️ Se o usuário clicar em `Salvar`, o sistema salva a alteração e volta a exibir os botões `Editar` e `Remover` ao lado do nome da atividade. (palavras-chave do requisito: salvar, salva, alteração, volta)
- ⚠️ Se o usuário clicar em `Cancelar`, o sistema não salva a alteração e volta a exibir o nome da atividade que estava antes da modificação com os botões `Editar` e `Remover` ao lado. (palavras-chave do requisito: cancelar, salva, alteração, volta)
- ⚠️ Se o usuário clicar em `Salvar`, o sistema salva a alteração e volta a exibir os botões `Editar` e `Remover` ao lado do nome do conhecimento. (palavras-chave do requisito: salvar, salva, alteração, volta)
- ⚠️ Se o usuário clicar em `Cancelar`, o sistema não salva a alteração e volta a exibir o nome do conhecimento que estava antes da modificação com os botões `Editar` e `Remover` ao lado. (palavras-chave do requisito: cancelar, salva, alteração, volta)
- ⚠️ Após cada ação de criação, edição ou exclusão, as informações deverão ser salvas automaticamente e vinculadas ao mapa de competências do subprocesso, não sendo necessária nenhuma ação adicional para garantir a persistência dessa informação. (palavras-chave do requisito: após, ação, criação, edição)

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
