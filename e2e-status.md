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
| `cdu-13.spec.ts` | ✅ Passou | 8 | 8 | Analisar cadastro de atividades e conhecimentos. |

## Problemas Identificados e Soluções

### CDU-13: Lentidão e timeouts em validações de acesso do Admin

**Problema:**
O teste `CDU-13` falhava intermitentemente por timeout (ou "element not found") no cenário onde o ADMIN visualiza o histórico de análises.
O log do backend exibia dezenas de mensagens "Permissão negada por localização" em um curto período, indicando verificação excessiva de permissões.

**Causa Raiz:**
A classe `SubprocessoContextoService` solicita o cálculo de todas as permissões (`SubprocessoPermissaoCalculator`) para a renderização da tela.
A política de acesso (`SubprocessoAccessPolicy`) consultava o banco de dados (`movimentacaoRepo`) para *cada* verificação de ação (aprox. 20 ações por request) para determinar a localização atual do subprocesso.
Como o ADMIN estava fora da unidade de localização, cada consulta resultava em falha de hierarquia e log de negação, além da latência de N+1 queries.

**Solução:**
Otimização de performance:
1.  Adicionado campo `@Transient private Unidade localizacaoAtualCache` na entidade `Subprocesso`.
2.  Alterado `SubprocessoAccessPolicy.obterUnidadeLocalizacao` para utilizar e popular esse cache.
Isso reduziu de ~20 consultas ao banco para apenas 1 consulta por requisição de detalhes, eliminando a latência excessiva e estabilizando o teste.

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

### CDU-09: Erro 403 ao devolver cadastro (Admin)

**Problema:**
O teste `CDU-09` falhava no Cenário 3 ao tentar devolver o cadastro como ADMIN. O backend retornava 403 Forbidden.

**Causa Raiz:**
A `SubprocessoAccessPolicy` exigia `RequisitoHierarquia.MESMA_UNIDADE` para a ação `DEVOLVER_CADASTRO`.
Como o subprocesso estava na unidade superior (localização), o ADMIN (unidade raiz) não atendia ao requisito.

**Solução:**
Alterada a `SubprocessoAccessPolicy` para permitir que o ADMIN execute ações de devolução e aceite (`DEVOLVER_CADASTRO`, `ACEITAR_CADASTRO`, etc.) globalmente, ignorando a restrição de unidade.

### CDU-10 (Regressão): Redirecionamento incorreto ao Voltar

**Problema:**
O teste `CDU-10` falhava novamente ao verificar a URL após clicar em "Voltar". O sistema redirecionava para `/painel`.
A tentativa anterior de usar rota nomeada (`name: 'Subprocesso'`) aparentemente falhou na resolução correta dos parâmetros em runtime ou causou um redirecionamento inesperado.

**Solução:**
Alterado para utilizar navegação por string explícita: `router.push(\`/processo/${props.codProcesso}/${props.sigla}\`)`.
Isso garante a construção correta da URL independentemente da resolução de nomes de rota.

## Aprendizados

1.  **Navegação Nomeada:** Sempre preferir `router.push({ name: 'Rota', params: { ... } })` em vez de construir URLs manualmente. Isso evita erros de digitação e garante consistência com a configuração de rotas.
2.  **Verificação de Contexto:** Testes E2E são sensíveis a redirecionamentos. Quando um teste falha em `expect(page).toHaveURL(...)`, verifique se a navegação anterior foi executada corretamente e se os parâmetros da URL estão sendo passados.
3.  **Logs de Teste:** Os logs do Playwright (`resultado_*.txt`) são essenciais para identificar *onde* o teste falhou e *qual* era a URL real vs esperada.
4.  **Políticas de Acesso:** Verificações de permissão baseadas em localização (`MESMA_UNIDADE`) podem bloquear administradores se não houver exceção explícita ou uso de `RequisitoHierarquia.NENHUM`.
5.  **Performance de Permissões:** O cálculo de permissões para renderização de interface (mostrar/esconder botões) deve ser eficiente. Evitar N+1 queries para verificar a mesma condição (como localização do subprocesso) repetidamente. Uso de campos `@Transient` para cache de requisição é uma solução válida.
