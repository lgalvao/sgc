# Relatório de Qualidade — SGC

> **Gerado em:** análise estática atualizada do código-fonte do projeto  
> **Escopo:** Backend (Java 25 / Spring Boot 4) + Frontend (Vue 3.5 / TypeScript)  
> **Convenções do projeto:** Português brasileiro, `codigo` no lugar de `id`, sufixos Controller/Service/Repo/Dto/Mapper

---

## Sumário Executivo

O SGC é um sistema de gestão de competências com arquitetura bem estruturada. Contudo, após uma nova revisão validada com o código atual, constata-se a persistência de **10 problemas técnicos** distribuídos em categorias de arquitetura, nulabilidade, regras de negócio e verbosidade, sendo **2 de alta severidade**, **6 de média** e **2 de baixa**. Problemas previamente relatados (como exposição de logs ao frontend, cache das views materializadas e N+1 nos blocos de aprovação) já foram resolvidos. O foco agora deve ser no desmembramento do **ProcessoService (God Class)**, na otimização de N+1 remanescente em `tornarMapasVigentes` e em corrigir falhas defensivas nulas.

### Métricas Atualizadas

| Categoria | Alta 🔴 | Média 🟡 | Baixa 🟢 | Total |
|---|---|---|---|---|
| 1. Complexidade desnecessária | 0 | 3 | 0 | 3 |
| 2. Verbosidade / boilerplate excessivo | 0 | 0 | 1 | 1 |
| 3. Defensividade excessiva / verificações nulas | 0 | 1 | 0 | 1 |
| 4. Nulabilidade e Optional | 1 | 1 | 1 | 3 |
| 5. Lógica de negócio no frontend | 0 | 1 | 0 | 1 |
| 6. Performance e cache | 1 | 0 | 0 | 1 |
| **Total** | **2** | **6** | **2** | **10** |

---

## 1. Complexidade Desnecessária

### 1.1 🟡 `ProcessoService` atuando como God Service

**Descrição:** `ProcessoService` concentra consulta, criação, workflow, validação, notificação e lógica de UI num único arquivo. Possui 16 dependências injetadas (collaborators), violando abertamente o Princípio de Responsabilidade Única (SRP) e dificultando testes isolados.

**Localização:** `backend/src/main/java/sgc/processo/service/ProcessoService.java`

**Impacto:** Dificuldade de teste isolado e alta probabilidade de conflitos de merge.

**Sugestão:** Extrair lógicas específicas para classes menores, como `ProcessoWorkflowService` (iniciar, finalizar), `ProcessoConsultaService` e `ProcessoValidacaoService`.

---

### 1.2 🟡 `PermissoesSubprocessoDto` com mais de 30 campos booleanos

**Descrição:** O DTO de permissões de UI possui múltiplos campos booleanos detalhando permissões por ação do usuário (ex: `podeEditarCadastro`, `podeDevolverMapa`, etc.). Isso gera um objeto de configuração inflexível.

**Localização:** `backend/src/main/java/sgc/subprocesso/dto/PermissoesSubprocessoDto.java`

**Impacto:** Cada nova ação de workflow exige a modificação da estrutura deste DTO, gerando esforço contínuo em refatorações espalhadas pelo projeto.

**Sugestão:** Substituir os booleanos estáticos por uma coleção genérica `Set<String> acoesPermitidas`. Isso mantém o contrato escalável.

---

### 1.3 🟡 Views massivas no frontend (`CadastroView` e `MapaView`)

**Descrição:** As views de Cadastro e Mapa ainda reúnem centenas de linhas de código, acoplando requisições de serviço, lógica complexa de estado e marcação de renderização no mesmo arquivo.

**Localização:** `frontend/src/views/CadastroView.vue`, `frontend/src/views/MapaView.vue`

**Impacto:** Dificuldade de manutenção e testabilidade. Extrema complexidade para implementar novos requisitos na UI sem causar regressão.

**Sugestão:** Centralizar o gerenciamento de estado e lógicas da página isolando-os em composables (ex: `useCadastroActions`), deixando os componentes puramente voltados para reatividade da UI.

---

## 2. Verbosidade / Boilerplate Excessivo

### 2.1 🟢 `mascarar` duplicado por quatro classes de segurança

**Descrição:** Uma função auxiliar de segurança que mascara dados sensíveis do usuário está duplicada explicitamente como método privado idêntico em 4 classes diferentes.

**Localização:** 
- `sgc/seguranca/LoginFacade.java`
- `sgc/seguranca/SgcPermissionEvaluator.java`
- `sgc/seguranca/login/ClienteAcessoAdE2e.java`
- `sgc/seguranca/login/FiltroJwt.java`

**Sugestão:** Centralizar este bloco reutilizável na classe existente `UtilSanitizacao`.

---

## 3. Defensividade Excessiva / Verificações Nulas

### 3.1 🟡 `verificarPermissao` aceita `@Nullable Usuario` e retorna falso silenciosamente

**Descrição:** O método principal de avaliação de permissões no `SgcPermissionEvaluator` retorna silenciamente `false` se o usuário for passado nulo, sem gerar nenhum log, perdendo o rastro analítico.

**Localização:** `backend/src/main/java/sgc/seguranca/SgcPermissionEvaluator.java`

**Impacto:** Permissões falhas por queda de contexto (onde se espera que haja de fato um usuário autenticado) são tratadas apenas como "falta de permissão", dificultando triagem de incidentes de contexto.

**Sugestão:** Implementar logging (`log.warn`) quando a avaliação falha especificamente devido a `usuario == null`.

---

## 4. Nulabilidade e Optional

### 4.1 🔴 `orElse(null)` quebra semântica em validações do `UsuarioFacade`

**Descrição:** `carregarUsuarioSemAtribuicoesParaAutenticacao` busca um usuário e o força como `null` ao usar `orElse(null)` imediatamente. O chamador então precisa checar o tipo nulo, ferindo o propósito de proteção do Java Optional.

**Localização:** `backend/src/main/java/sgc/organizacao/UsuarioFacade.java` (linhas 30 e 51)

**Impacto:** Risco perigoso de `NullPointerException` se outras classes presumirem proteção. Esvazia a utilidade da assinatura de tipo.

**Sugestão:** Retornar `Optional<Usuario>` ou garantir que o próprio método lance a exceção.

---

### 4.2 🟡 Retorno via mapa genérico em `obterSugestoes` invés de DTO tipado

**Descrição:** `obterSugestoes` em `SubprocessoConsultaService` engloba a string retornada do banco de dados em um `Map.of("sugestoes", sugestoes)` genérico para retorno. 

**Sugestão:** Definir um DTO de retorno formal (`SugestoesDto(String sugestoes)`) ou entregar explicitamente a `String` diretamente ao controller.

---

### 4.3 🟢 Busca da análise via `findFirst().orElse(null)` sem proteção de tipo

**Descrição:** No `SubprocessoVisualizacaoService.java`, a busca pela validação devolve silenciosamente `null` no caso de ausência por falta do objeto no stream.

**Sugestão:** Retornar `Optional<Analise>` direto do serviço, sinalizando de fato que a validação pode ainda não existir para aquele fluxo.

---

## 5. Lógica de Negócio no Frontend

### 5.1 🟡 O frontend reconstrói mapeamento do Subprocesso com default estático

**Descrição:** `mapSubprocessoDetalheResponseParaModel` injeta um valor padrão de etapa (`etapaAtual ?? 1`) caso venha nulo do serviço.

**Localização:** `frontend/src/services/subprocessoService.ts`

**Impacto:** Lógicas da máquina de estado da entidade (`etapaAtual`) estão vazando para o Frontend. Se a regra mudar no Backend, falhará o Frontend.

**Sugestão:** O backend deve expor um DTO unificado, limpo e devidamente preenchido sem repassar a carga dos defaults para o front.

---

## 6. Performance e Cache

### 6.1 🔴 N+1 na atribuição de `tornarMapasVigentes` 

**Descrição:** Durante a finalização de processos longos, `tornarMapasVigentes` carrega uma lista e faz chamadas por um `.forEach` que executa `definirMapasVigentesEmBloco`. No entanto, sob capô o loop acaba disparando transações de atualização individuais a cada iteração, causando um N+1.

**Localização:** `backend/src/main/java/sgc/processo/service/ProcessoService.java:477`

**Impacto:** Se um processo possui centenas de entidades alinhadas, causar-se-á centenas de requisições UPDATE simultâneas ao banco de dados que poderiam facilmente ser englobadas em um UPDATE escalonado.

**Sugestão:** Otimizar e unificar o `definirMapasVigentesEmBloco` para rodar como comando de banco real otimizado.

---

## Resumo de Ações Prioritárias

| Prioridade | Ação | Arquivo(s) |
|---|---|---|
| 🔴 P1 | Corrigir semântica do `orElse(null)` no carregamento de usuários | `UsuarioFacade.java` |
| 🔴 P1 | Otimizar processo N+1 no `tornarMapasVigentes` | `ProcessoService.java` |
| 🟡 P2 | Dividir as responsabilidades do `ProcessoService` em classes menores | `ProcessoService.java` |
| 🟡 P2 | Refatorar `PermissoesSubprocessoDto` para eliminar dezenas de booleanos | `PermissoesSubprocessoDto.java` |
| 🟡 P2 | Centralizar o método duplicado de `mascarar()` dados | `UtilSanitizacao.java` / `SgcPermissionEvaluator.java` |
