# Alinhamento CDU-07 - Reanálise

## Escopo da reanálise
- Requisito analisado: `etc/reqs/cdu-07.md`.
- Teste E2E analisado: `e2e/cdu-07.spec.ts` (5 cenários `test`, 0 `test.step`, 372 linhas).
- Contextos `describe` identificados: CDU-07 - Detalhar subprocesso.

## Cobertura observada no E2E
- ✅ Deve exibir detalhes do subprocesso em mapeamento para ADMIN, GESTOR, CHEFE e SERVIDOR
- ✅ Deve habilitar os cards conforme o avanço do subprocesso
- ✅ Deve manter o acesso de visualização no processo finalizado para servidor da própria unidade
- ✅ Deve exibir cards com rotas corretas ao navegar entre subprocessos distintos na mesma sessão
- ✅ Deve exibir os cards do ramo de diagnóstico na tela de detalhes do subprocesso

## Pontos do requisito sem evidência direta no E2E
- ✅ Não foram encontradas lacunas textuais relevantes com a heurística aplicada; ainda assim recomenda-se validação manual funcional.

## Ações recomendadas (teste e sistema)
- Priorizar cenários com dados controlados para validar regra de negócio (não apenas presença de elementos na UI).
- Incluir asserts de navegação/efeito colateral (persistência, alteração de estado, permissões por perfil e unidade ativa).
- Quando o requisito citar integração externa, manter o E2E focado em contrato visível (mensagem, bloqueio, fallback) e complementar com teste de integração/backend.

## Método utilizado nesta reanálise
- Leitura comparativa do texto do requisito (fluxo principal) com os cenários e passos automatizados no arquivo E2E correspondente.
- Marcação de lacunas por ausência de evidência textual de validação no teste; itens marcados como ⚠️ devem ser revisados manualmente na próxima rodada.
