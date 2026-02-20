# Status dos Testes E2E (Playwright)

Este documento registra o status dos testes End-to-End executados e as lições aprendidas durante o processo de correção.

## Status dos Testes (CDU-01 a CDU-10)

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

## Problemas Identificados e Soluções

### CDU-12: Permissão negada para ADMIN ao verificar impactos

**Problema:**
O teste `CDU-12` falhava no Cenário 5 (`Verificar visualização pelo Admin`), onde o ADMIN tentava acessar a visualização de impactos de uma revisão disponibilizada (`REVISAO_CADASTRO_DISPONIBILIZADA`).
O botão "Impacto no mapa" não aparecia, indicando falta de permissão.

**Causa Raiz:**
A política de acesso `SubprocessoAccessPolicy` exigia `RequisitoHierarquia.MESMA_UNIDADE` para a ação `VERIFICAR_IMPACTOS` mesmo para o perfil ADMIN.
Como o subprocesso estava em `REVISAO_CADASTRO_DISPONIBILIZADA`, sua localização era a unidade superior (ex: `COORD_21`). O usuário ADMIN (unidade `ADMIN`) não atendia ao critério de `MESMA_UNIDADE`.

**Solução:**
Alterada a regra em `SubprocessoAccessPolicy.canExecuteVerificarImpactos` para usar `RequisitoHierarquia.NENHUM` para o perfil ADMIN, permitindo acesso global à verificação de impactos, conforme esperado para administradores.

### CDU-10: Redirecionamento Incorreto ao Voltar do Cadastro de Atividades

**Problema:**
O teste `CDU-10` falhava ao verificar a URL após clicar no botão "Voltar" na tela de Cadastro de Atividades (`AtividadesCadastroView`).
O teste esperava ser redirecionado para `/processo/{id}/{sigla}`, mas estava sendo redirecionado para `/painel` (ou URL incorreta).

**Causa Raiz:**
O botão "Voltar" utilizava `router.push` com uma string construída manualmente: `` `/processo/${props.codProcesso}/${props.sigla}` ``.
Provavelmente, a construção da string estava incorreta ou faltava algum parâmetro, levando a um fallback para a rota padrão ou erro de navegação.

**Solução:**
Refatoração da navegação para utilizar o nome da rota e parâmetros explícitos:
```typescript
router.push({
    name: 'Subprocesso',
    params: {
        codProcesso: props.codProcesso,
        siglaUnidade: props.sigla
    }
});
```
Isso garante que o Vue Router construa a URL corretamente baseada na definição da rota `Subprocesso`.

## Aprendizados

1.  **Navegação Nomeada:** Sempre preferir `router.push({ name: 'Rota', params: { ... } })` em vez de construir URLs manualmente. Isso evita erros de digitação e garante consistência com a configuração de rotas.
2.  **Verificação de Contexto:** Testes E2E são sensíveis a redirecionamentos. Quando um teste falha em `expect(page).toHaveURL(...)`, verifique se a navegação anterior foi executada corretamente e se os parâmetros da URL estão sendo passados.
3.  **Logs de Teste:** Os logs do Playwright (`resultado_*.txt`) são essenciais para identificar *onde* o teste falhou e *qual* era a URL real vs esperada.
4.  **Políticas de Acesso:** Verificações de permissão baseadas em localização (`MESMA_UNIDADE`) podem bloquear administradores se não houver exceção explícita ou uso de `RequisitoHierarquia.NENHUM`.
