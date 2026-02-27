# Análise de Problemas - CDU-12 (Verificar impactos no mapa)

## 1. Problemas Identificados e Corrigidos no Backend

### 1.1 Erro de Integridade Referencial na Limpeza (E2E)
**Problema:** A rota `/e2e/processo/{codigo}/limpar`, usada pelos testes para resetar o banco, estava falhando devido a violações de chave estrangeira. Ela tentava deletar o processo antes de apagar referências em tabelas filhas (como `alerta` e `unidade_mapa`).
**Solução:** Criamos um endpoint robusto `/e2e/processo/{codigo}/limpar-completo` que desabilita temporariamente a integridade referencial (`SET REFERENTIAL_INTEGRITY FALSE`), limpa toda a cascata de dependências (incluindo `unidade_mapa` e `alerta_usuario`) e depois reativa as restrições.

### 1.2 PathElementException do Hibernate
**Problema:** Ocorria um erro 500 no backend ao tentar buscar um Subprocesso pelo código do mapa. O método genérico `ComumRepo.buscar(Subprocesso.class, "mapa.codigo", codMapa)` não suporta navegação por propriedades aninhadas sem a construção explícita de um `Join` na Criteria API, gerando exceção no Hibernate.
**Solução:** Substituímos essas chamadas por consultas nativas do Spring Data JPA, utilizando `subprocessoRepo.findByMapa_Codigo(codMapa)`.

### 1.3 Regra de Negócio de Permissões Frouxa
**Problema:** A propriedade `podeVisualizarImpacto` estava sendo retornada como `true` para cenários incorretos (como em processos de MAPEAMENTO ou na fase `NAO_INICIADO` da Revisão), o que permitia acesso ao modal mesmo quando não havia mapa vigente para comparar.
**Solução:** Ajustamos o `SubprocessoService.java` para refletir estritamente o manual CDU-12. O botão agora só aparece se `temMapaVigente == true` E se o processo estiver nas fases corretas de REVISÃO (ex: `REVISAO_CADASTRO_EM_ANDAMENTO` para o Chefe).

## 2. Status Atual da Falha no E2E

**Sintoma:** O teste `cdu-12.spec.ts` falha no passo "Fluxo CHEFE: Realizar alterações e verificar impactos" informando que não conseguiu encontrar o botão `cad-atividades__btn-impactos-mapa-edicao`.

**Análise do fluxo:**
1. O Chefe entra na tela de Atividades. O processo está como `NÃO INICIADO`.
2. Como a situação não é `REVISAO_CADASTRO_EM_ANDAMENTO`, a propriedade `podeVisualizarImpacto` é enviada como `false`. O botão não é renderizado (comportamento correto).
3. O teste simula a adição de uma nova atividade (`Atividade Nova Revisão...`).
4. No backend, a criação da atividade dispara a mudança de status do subprocesso para `REVISAO_CADASTRO_EM_ANDAMENTO`. O endpoint de criação retorna um objeto `AtividadeOperacaoResponse` que contém o novo status e as permissões atualizadas (`podeVisualizarImpacto` deve vir `true` agora).
5. No frontend, o Pinia store (`atividades.ts`) recebe essa resposta e chama `atualizarStatusLocal`, que mescla as novas permissões no estado.
6. O teste clica no botão "Mais Ações" e tenta encontrar o botão "Impacto no mapa", **mas ele não está lá**.

**Hipóteses para a continuidade da investigação:**
- **Reatividade do Vue:** O componente pode não estar reagindo corretamente à atualização das permissões aninhadas em `subprocessoDetalhe.value.permissoes`.
- **Geração de Permissões no Backend:** Quando o `AtividadeFacade` invoca a geração do DTO de resposta, a transação que alterou o status do processo para `EM_ANDAMENTO` pode ainda não ter sido "commitada", ou o EntityManager está usando uma entidade em cache com o status antigo (`NAO_INICIADO`), gerando as permissões erradas na resposta da requisição de criação.
- **Perda do Mapa Vigente:** É necessário garantir que o mapa vigente da unidade não foi acidentalmente limpo no banco pelo hook do Playwright entre um teste e outro.