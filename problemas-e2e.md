# Problemas Identificados na Suíte E2E (SGC)

Durante a correção dos bugs de acesso e reatividade, identificamos as seguintes lacunas na suíte de testes E2E atual:

## 1. Abstração de Navegação (SPA vs. page.goto)
*   **Problema**: Muitos helpers de navegação utilizam `page.goto(url)` para saltar diretamente para telas.
*   **Consequência**: Isso força um recarregamento total da aplicação (refresh), o que limpa o estado do Pinia e esconde bugs de reatividade/estado que só ocorrem na navegação interna via `router.push` (cliques em cards/links).
*   **Exemplo**: O bug visual do Mapa de Competências só ocorria na transição interna; ao usar `page.goto`, o sistema se "curava" sozinho.

## 2. Cobertura de Perfil ADMIN
*   **Problema**: A maioria dos cenários foca no "Caminho Feliz" de Chefes (cadastro) e Gestores (aceite).
*   **Consequência**: Fluxos específicos de homologação e visualização por parte do ADMIN em unidades subordinadas possuem pouca cobertura de cliques reais, dependendo muito de acessos diretos via lista ou URL.

## 3. Validação de Estado "Clicável"
*   **Problema**: Os testes verificam frequentemente `toBeVisible()`, mas raramente explicitam `toBeEnabled()`.
*   **Consequência**: Um botão pode estar visível no DOM, mas desabilitado ou sem o handler de clique carregado (devido a falha na carga de dados de permissão), e o teste ainda assim passará se não tentar o clique ou não validar o estado.

## 4. Resiliência do Playwright (Auto-waiting)
*   **Problema**: O mecanismo de espera automática do Playwright é "paciente" demais.
*   **Consequência**: Flutuações na UI (elementos que somem e reaparecem em milissegundos devido a race conditions de reatividade) são ignoradas pela ferramenta, mas percebidas como bugs de instabilidade por usuários reais.

## Próximos Passos (Recomendação)
1.  Refatorar [helpers-atividades.ts](file:///c:/sgc/e2e/helpers/helpers-atividades.ts), [helpers-mapas.ts](file:///c:/sgc/e2e/helpers/helpers-mapas.ts) e [helpers-analise.ts](file:///c:/sgc/e2e/helpers/helpers-analise.ts) para usar transições baseadas em cliques sempre que possível.
2.  Adicionar asserções de `toBeEnabled()` em todas as verificações de botões de ação (Homologar, Validar, Aceitar).
3.  Incluir o perfil ADMIN em fluxos de jornada completa de subprocessos.
