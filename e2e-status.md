# Status dos Testes E2E (Playwright)

Este documento registra o status dos testes End-to-End executados e as lições aprendidas durante o processo de correção.

## Status dos Testes (CDU-01 a CDU-36)

| Arquivo Teste | Status | Testes Passados | Total Testes | Notas |
| :--- | :---: | :---: | :---: | :--- |
| `cdu-01.spec.ts` | ✅ Passou | 6 | 6 | Login e estrutura básica. |
| `cdu-02.spec.ts` | ✅ Passou | 10 | 10 | Painel e Processos (Admin/Gestor). |
| `cdu-03.spec.ts` | ✅ Passou | 3 | 3 | Manter Processo (CRUD). |
| `cdu-04.spec.ts` | ✅ Passou | 1 | 1 | Iniciar processo de mapeamento. |
| `cdu-05.spec.ts` | ✅ Passou | 8 | 8 | Iniciar processo de revisão (Fluxo completo). |
| `cdu-06.spec.ts` | ✅ Passou | 2 | 2 | Detalhar processo (Admin/Gestor). |
| `cdu-07.spec.ts` | ✅ Passou | 1 | 1 | Detalhar subprocesso. |
| `cdu-08.spec.ts` | ✅ Passou | 2 | 2 | Manter cadastro de atividades (Mapeamento/Revisão). |
| `cdu-09.spec.ts` | ✅ Passou | 4 | 4 | Disponibilizar cadastro de atividades (Validações). |
| `cdu-10.spec.ts` | ✅ Passou | 1 | 1 | Disponibilizar revisão do cadastro (Fluxo completo). |
| `cdu-11.spec.ts` | ✅ Passou | 6 | 6 | Visualizar cadastro de atividades e conhecimentos. |
| `cdu-12.spec.ts` | ✅ Passou | 7 | 7 | Verificar impactos no mapa de competências. |
| `cdu-13.spec.ts` | ✅ Passou | 8 | 8 | Analisar cadastro de atividades e conhecimentos. |
| `cdu-14.spec.ts` | ✅ Passou | 20 | 20 | Analisar revisão de cadastro. |
| `cdu-15.spec.ts` | ✅ Passou | 7 | 7 | Manter mapa de competências. |
| `cdu-16.spec.ts` | ✅ Passou | 19 | 19 | Ajustar mapa de competências. |
| `cdu-17.spec.ts` | ✅ Passou | 10 | 10 | Disponibilizar mapa de competências. |
| `cdu-18.spec.ts` | ✅ Passou | 2 | 2 | Visualizar mapa de competências. |
| `cdu-19.spec.ts` | ✅ Passou | 3 | 3 | Validar mapa de competências. |
| `cdu-20.spec.ts` | ✅ Passou | 13 | 13 | Analisar validação de mapa. |
| `cdu-21.spec.ts` | ✅ Passou | 13 | 13 | Finalizar processo. |
| `cdu-22 a 26.spec.ts` | ✅ Passou | 37 | 37 | Operações em bloco. |
| `cdu-27 a 31.spec.ts` | ✅ Passou | 16 | 16 | Gestão e Configurações. |
| `cdu-32 a 36.spec.ts` | ✅ Passou | 11 | 11 | Relatórios e Reabertura. |
| `regressão/micro` | ✅ Passou | 32 | 32 | Testes de regressão e micro-funcionalidades. |

## Problemas Identificados e Soluções

### Omissão de campos em DTOs (Jackson @JsonView)

**Problema:**
Alguns campos essenciais (como `permissoes` e `subprocesso.codigo`) estavam faltando nas respostas JSON das APIs de Atividades e Conhecimentos durante os testes E2E. Isso impedia o frontend de habilitar botões de ação (como "Disponibilizar") imediatamente após a primeira atividade ser adicionada.

**Causa Raiz:**
Uso inconsistente de `@JsonView`. Alguns DTOs tinham anotações em seus campos, mas os controllers que os retornavam não especificavam uma view, ou usavam uma view que não abrangia os campos aninhados. O Jackson omitia esses campos por padrão.

**Solução:**
Removido o uso de `@JsonView` em DTOs de resposta de API (`PermissoesSubprocessoDto`, `AtividadeDto`, `AtividadeOperacaoResponse`, etc.). Como esses DTOs são criados especificamente para transporte, eles devem ser totalmente serializados. Os controllers que retornam DTOs também tiveram suas anotações `@JsonView` removidas para garantir consistência.

### Reatividade no Store de Subprocessos

**Problema:**
A UI do frontend não reagia a mudanças na situação ou permissões do subprocesso quando atualizadas via store após uma operação CRUD.

**Causa Raiz:**
Mutação direta de propriedades em um objeto `ref`. Embora o Vue 3 detecte algumas mutações, a dependência em `computed` propriedades que usavam `unref` nem sempre disparava o recálculo corretamente se a referência do objeto pai permanecesse a mesma.

**Solução:**
Alterado o store `subprocessos.ts` para substituir o objeto `subprocessoDetalhe.value` por um novo objeto (spread operator) sempre que houver uma atualização local de status ou permissões. Isso garante o disparo da reatividade em todo o sistema.

### Botão de Impacto visível em Mapeamento

**Problema:**
O botão "Impacto no mapa" aparecia indevidamente em processos de Mapeamento.

**Causa Raiz:**
A regra de segurança `VERIFICAR_IMPACTOS` em `SubprocessoSecurity` não checava o tipo do processo, apenas a situação (que para `NAO_INICIADO` é comum a ambos os tipos).

**Solução:**
Adicionada verificação explícita no backend para garantir que `VERIFICAR_IMPACTOS` só retorne `true` se o processo for do tipo `REVISAO`.

### Permissão de Workflow para ADMIN

**Problema:**
O ADMIN era bloqueado ao tentar realizar ações de workflow (aceitar, devolver, homologar) se o subprocesso não estivesse localizado na unidade raiz.

**Causa Raiz:**
A "Regra de Ouro" de localização estava sendo aplicada rigidamente ao ADMIN para ações que ele deveria poder executar globalmente.

**Solução:**
Adicionada uma exceção em `SubprocessoSecurity` permitindo que o perfil `ADMIN` execute ações de workflow (`ACEITAR`, `DEVOLVER`, `HOMOLOGAR`) independentemente da localização atual do subprocesso.


## Aprendizados

1.  **Navegação Nomeada:** Sempre preferir `router.push({ name: 'Rota', params: { ... } })` em vez de construir URLs manualmente. Isso evita erros de digitação e garante consistência com a configuração de rotas.
2.  **Verificação de Contexto:** Testes E2E são sensíveis a redirecionamentos. Quando um teste falha em `expect(page).toHaveURL(...)`, verifique se a navegação anterior foi executada corretamente e se os parâmetros da URL estão sendo passados.
3.  **Logs de Teste:** Os logs do Playwright (`resultado_*.txt`) são essenciais para identificar *onde* o teste falhou e *qual* era a URL real vs esperada.
4.  **Políticas de Acesso:** Verificações de permissão baseadas em localização (`MESMA_UNIDADE`) podem bloquear administradores se não houver exceção explícita ou uso de `RequisitoHierarquia.NENHUM`.
5.  **Performance de Permissões:** O cálculo de permissões para renderização de interface (mostrar/esconder botões) deve ser eficiente. Evitar N+1 queries para verificar a mesma condição (como localização do subprocesso) repetidamente. Uso de campos `@Transient` para cache de requisição é uma solução válida.
