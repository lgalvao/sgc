# Relatório de Qualidade — SGC

> **Gerado em:** análise estática completa do código-fonte do projeto  
> **Escopo:** Backend (Java 25 / Spring Boot 4) + Frontend (Vue 3.5 / TypeScript)  
> **Convenções do projeto:** Português brasileiro, `codigo` no lugar de `id`, sufixos Controller/Service/Repo/Dto/Mapper

---

## Sumário Executivo

O SGC é um sistema de gestão de competências com arquitetura bem estruturada, separação clara de camadas e boas práticas consolidadas como: uso de `@PreAuthorize`, DTOs tipados, tratamento centralizado de erros e um sistema de permissões por domínio. No entanto, uma análise profunda revelou **40 problemas** distribuídos em 10 categorias, sendo **9 de alta severidade**, **22 de média** e **9 de baixa**. Os problemas mais críticos envolvem **mensagens de erro internas expostas ao cliente**, **comparação de enums via String**, **operações em bloco com N+1 queries**, e **função de cache `dadosValidos` que sempre retorna `false` sem nunca cachear** nos stores de frontend.

### Métricas Resumidas

| Categoria | Alta 🔴 | Média 🟡 | Baixa 🟢 | Total |
|---|---|---|---|---|
| 1. Complexidade desnecessária | 0 | 4 | 1 | 5 |
| 2. Ramificação excessiva | 1 | 3 | 0 | 4 |
| 3. Inconsistências e nomenclatura | 0 | 3 | 2 | 5 |
| 4. Verbosidade / boilerplate excessivo | 0 | 2 | 2 | 4 |
| 5. Defensividade excessiva / verificações nulas | 1 | 3 | 1 | 5 |
| 6. Nulabilidade e Optional | 1 | 2 | 1 | 4 |
| 7. Lógica de negócio no frontend | 0 | 2 | 1 | 3 |
| 8. Performance e cache | 3 | 2 | 0 | 5 |
| 9. Segurança | 2 | 1 | 1 | 4 |
| 10. Arquitetura / violação de camadas | 1 | 0 | 0 | 1 |
| **Total** | **9** | **22** | **9** | **40** |

---

## 1. Complexidade Desnecessária

### 1.1 🟡 `ProcessoService` com 1315 linhas — God Service

**Descrição:** `ProcessoService` concentra consulta, criação, workflow, validação, notificação e lógica de UI num único arquivo.

**Localização:** `backend/src/main/java/sgc/processo/service/ProcessoService.java` (1315 linhas)

**Evidência:**
```java
// Dependências injetadas: 15 collaborators
private final ProcessoRepo processoRepo;
private final ComumRepo repo;
private final UnidadeHierarquiaService unidadeHierarquiaService;
private final UnidadeService unidadeService;
private final ResponsavelUnidadeService responsavelUnidadeService;
private final SubprocessoService subprocessoService;
private final SubprocessoConsultaService consultaService;
private final LocalizacaoSubprocessoService localizacaoSubprocessoService;
private final SubprocessoValidacaoService validacaoService;
private final UsuarioFacade usuarioService;
private final AlertaFacade servicoAlertas;
private final NotificacaoService notificacaoService;
private final EmailModelosService emailModelosService;
private final SgcPermissionEvaluator permissionEvaluator;
private final SubprocessoTransicaoService transicaoService;
```

**Impacto:** Dificuldade de teste isolado, alta probabilidade de conflitos de merge, violação do Princípio da Responsabilidade Única.

**Sugestão:** Extrair `ProcessoWorkflowService` (iniciar, finalizar, enviarLembrete), `ProcessoConsultaService` (listarAtivos, listarFinalizados, obterDetalhesCompleto) e `ProcessoValidacaoService` das responsabilidades atuais.

---

### 1.2 🟡 `SubprocessoTransicaoService` com 933 linhas

**Descrição:** O service de transição também está excessivamente grande, contendo lógica de disponibilização, aceite, homologação, devolução, reaberturas e alteração de datas em um único arquivo.

**Localização:** `backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java` (933 linhas)

**Evidência:**
```java
// Record interno FluxoCadastroContexto duplicado por falta de separação
@Builder
private record FluxoCadastroContexto(
        String etapa,
        SituacaoSubprocesso situacaoDisponibilizada,
        SituacaoSubprocesso situacaoEmAndamento,
        SituacaoSubprocesso situacaoHomologada,
        TipoTransicao transicaoDevolucao,
        TipoTransicao transicaoAceite,
        TipoTransicao transicaoHomologacao,
        TipoAcaoAnalise acaoDevolucao,
        TipoAcaoAnalise acaoAceite
)
```

**Impacto:** Baixa coesão. O record `FluxoCadastroContexto` é um sinal de que há um sub-domínio implícito não extraído (fluxo de cadastro e revisão).

**Sugestão:** Extrair `CadastroFluxoService` para as operações de aceite/homologação/devolução de cadastro, separando do fluxo de mapa.

---

### 1.3 🟡 `PermissoesSubprocessoDto` com 34 campos booleanos

**Descrição:** O DTO de permissões do subprocesso possui 34 campos booleanos, todos com prefixo `pode` ou `habilitar`, gerando um objeto de configuração excessivamente granular.

**Localização:** `backend/src/main/java/sgc/subprocesso/dto/PermissoesSubprocessoDto.java`

**Evidência:**
```java
@Builder
public record PermissoesSubprocessoDto(
        boolean podeEditarCadastro,
        boolean podeDisponibilizarCadastro,
        boolean podeDevolverCadastro,
        boolean podeAceitarCadastro,
        boolean podeHomologarCadastro,
        boolean podeEditarMapa,
        // ... mais 28 campos
        boolean habilitarHomologarMapa
) {}
```

**Impacto:** Cada nova ação de workflow exige adicionar dois campos (`podeX` + `habilitarX`). O DTO tende a crescer indefinidamente.

**Sugestão:** Substituir por uma lista de ações habilitadas: `Set<String> acoesPermitidas` + `Set<String> acoesHabilitadas`. Isso permite acrescentar novas ações sem alterar o contrato da API.

---

### 1.4 🟡 Views de frontend com mais de 900 linhas

**Descrição:** `CadastroView.vue` (959 linhas) e `MapaView.vue` (888 linhas) são componentes de View excessivamente grandes, misturando lógica de estado, chamadas de serviço e renderização.

**Localização:** `frontend/src/views/CadastroView.vue`, `frontend/src/views/MapaView.vue`

**Impacto:** Dificuldade de manutenção, testes e compreensão do fluxo de dados. Componentes difíceis de reutilizar.

**Sugestão:** Extrair composables como `useCadastroActions`, `useMapaActions` para centralizar a lógica de negócio, deixando os componentes focados exclusivamente em renderização.

---

### 1.5 🟢 `obterUltimaDataLimiteSubprocesso` duplicada no frontend e backend

**Descrição:** A lógica de comparação de datas (`dataLimiteEtapa1 > dataLimiteEtapa2`) aparece tanto no backend (SubprocessoTransicaoService) quanto no frontend (subprocessoService.ts linha 67).

**Localização:** `frontend/src/services/subprocessoService.ts` (função `obterUltimaDataLimiteSubprocesso`)

**Evidência:**
```typescript
function obterUltimaDataLimiteSubprocesso(subprocesso: SubprocessoDetalheResponse["subprocesso"]): string {
    const dataLimiteEtapa1 = subprocesso.dataLimiteEtapa1;
    const dataLimiteEtapa2 = subprocesso.dataLimiteEtapa2;
    if (!dataLimiteEtapa2) {
        return dataLimiteEtapa1;
    }
    return dataLimiteEtapa1 > dataLimiteEtapa2 ? dataLimiteEtapa1 : dataLimiteEtapa2;
}
```

**Impacto:** Se a lógica de datas mudar no backend, o frontend precisará ser atualizado manualmente.

**Sugestão:** O backend deve calcular e retornar `ultimaDataLimite` já resolvida no DTO, eliminando a necessidade do cálculo no frontend.

---

## 2. Ramificação Excessiva

### 2.1 🔴 Comparação de enum via `String.contains("MAPA")` em três locais

**Descrição:** A determinação de se um subprocesso está em etapa de mapa é feita via `getSituacao().name().contains("MAPA")`, frágil e propensa a falsos-positivos se novos estados forem adicionados.

**Localização:**
- `backend/src/main/java/sgc/subprocesso/service/SubprocessoConsultaService.java:92`
- `backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java:895,909`
- `backend/src/main/java/sgc/mapa/service/MapaVisualizacaoService.java:30`

**Evidência:**
```java
// SubprocessoConsultaService.java:92
if (subprocesso.getSituacao() != null && subprocesso.getSituacao().name().contains("MAPA")) {
    throw new ErroInconsistenciaInterna(...);
}

// SubprocessoTransicaoService.java:895
private void atualizarDataLimitePorSituacao(Subprocesso sp, String situacaoSp, LocalDateTime novaData) {
    if (situacaoSp.contains("MAPA")) { // comparação frágil por substring
        sp.setDataLimiteEtapa2(novaData);
        return;
    }
    sp.setDataLimiteEtapa1(novaData);
}

// SubprocessoTransicaoService.java:909
private int obterEtapaPorSituacao(String situacaoSp) {
    return situacaoSp.contains("MAPA") ? 2 : 1; // magic string
}
```

**Impacto:** Alto risco de regressão. Se um futuro estado se chamar, por exemplo, `PRE_MAPEAMENTO`, o código seria classificado erroneamente como "em etapa de mapa". O enum `SituacaoSubprocesso` já possui capacidades de prefixo que deveriam ser aproveitadas.

**Sugestão:** Adicionar um método semântico ao enum:
```java
// Em SituacaoSubprocesso:
public boolean ehEtapaMapa() {
    return name().startsWith(PREFIXO_MAPEAMENTO) && name().contains("MAPA_") ||
           name().startsWith(PREFIXO_REVISAO) && name().contains("MAPA_");
}
```
E substituir todos os `contains("MAPA")` por `situacao.ehEtapaMapa()`.

---

### 2.2 🟡 `verificarSubprocesso` com condicionais encadeadas de difícil leitura

**Descrição:** O método `verificarSubprocesso` no `SgcPermissionEvaluator` possui cinco condicionais encadeadas que misturam ações de leitura e escrita, tornando difícil raciocinar sobre o fluxo de autorização.

**Localização:** `backend/src/main/java/sgc/seguranca/SgcPermissionEvaluator.java` (~linha 95)

**Evidência:**
```java
private boolean verificarSubprocesso(Usuario usuario, Subprocesso sp, AcaoPermissao acao, boolean logarNegacao) {
    Perfil perfil = usuario.getPerfilAtivo();
    Processo processo = sp.getProcesso();

    // Caso especial: importação permite consultar processos finalizados
    if (acao == AcaoPermissao.CONSULTAR_PARA_IMPORTACAO && perfil == CHEFE) {
        return processo.getSituacao() == FINALIZADO || verificarHierarquia(usuario, sp.getUnidade());
    }

    // Processo finalizado: bloqueia escrita, permite leitura
    if (processo.getSituacao() == FINALIZADO) {
        return !acao.dependeLocalizacao() && acao.permitePerfil(perfil);
    }

    // Ações de leitura: verificam hierarquia (exceto admin, que vê tudo)
    if (!acao.dependeLocalizacao()) {
        if (perfil == ADMIN) return true;
        if (acao == AcaoPermissao.VERIFICAR_IMPACTOS) return true; // controle feito no serviço
        return verificarHierarquia(usuario, sp.getUnidade());
    }

    // Ações de escrita: verificam perfil + localização
    if (!acao.permitePerfil(perfil)) {
        return false;
    }
    return verificarLocalizacao(usuario, sp, logarNegacao);
}
```

**Impacto:** Dificulta adição de novos casos especiais (por ex., `VERIFICAR_IMPACTOS` já requer um comentário explicativo inline). Código de segurança é área crítica onde a clareza é essencial.

**Sugestão:** Extrair cada "caso especial" em métodos bem nomeados e documentados com a regra de negócio correspondente.

---

### 2.3 🟡 `listarTodos` com verificação de nulo em paginação desnecessária

**Descrição:** `ProcessoService.listarTodos()` verifica se `paginaCodigos == null` antes de prosseguir. Uma query JPA `@Query` bem definida nunca retorna `null` — retorna `Page.empty()`.

**Localização:** `backend/src/main/java/sgc/processo/service/ProcessoService.java:107`

**Evidência:**
```java
public Page<Processo> listarTodos(Pageable pageable) {
    Page<Long> paginaCodigos = processoRepo.listarCodigos(pageable);
    if (paginaCodigos == null) {  // nunca ocorre em prática
        return processoRepo.findAll(pageable);
    }
    return carregarPaginaComParticipantes(paginaCodigos, pageable);
}
```

**Impacto:** Código defensivo que esconde um possível bug (se `listarCodigos` retornar `null`, o fallback `findAll` causaria N+1 silencioso). O mesmo padrão aparece em `listarIniciadosPorParticipantes`.

**Sugestão:** Remover o check nulo. Se `listarCodigos` puder retornar `null`, corrigir o repositório para retornar `Page.empty()`.

---

### 2.4 🟡 `FluxoCadastroContexto` com factory methods `revisao()` e `mapeamento()` acoplados ao boolean

**Descrição:** O `SubprocessoTransicaoService` usa um boolean `isRevisao` como discriminante para selecionar o contexto de workflow, espalhando o acoplamento em vários métodos.

**Localização:** `backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java`

**Evidência:**
```java
public void aceitarCadastro(Long codSubprocesso, @Nullable String observacoes) {
    Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
    Usuario usuario = usuarioFacade.usuarioAutenticado();
    executarAceite(sp, usuario, observacoes, false);  // false = mapeamento
}

public void aceitarRevisaoCadastro(Long codSubprocesso, @Nullable String observacoes) {
    Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
    Usuario usuario = usuarioFacade.usuarioAutenticado();
    executarAceite(sp, usuario, observacoes, true);  // true = revisão
}
```

**Impacto:** Mantém dois métodos públicos quase idênticos para cada ação. Ao adicionar um novo tipo de processo (ex: `ATUALIZACAO`), seria necessário criar novos métodos E alterar o boolean em todos os lugares.

**Sugestão:** Derivar o contexto diretamente do `TipoProcesso` do subprocesso, eliminando o parâmetro `isRevisao`:
```java
private FluxoCadastroContexto obterContextoCadastro(Subprocesso sp) {
    return switch (sp.getProcesso().getTipo()) {
        case REVISAO -> FluxoCadastroContexto.revisao();
        case MAPEAMENTO -> FluxoCadastroContexto.mapeamento();
        default -> throw new IllegalStateException("Tipo %s sem fluxo de cadastro".formatted(sp.getProcesso().getTipo()));
    };
}
```

---

## 3. Inconsistências e Nomenclatura

### 3.1 🟡 Inconsistência de nomenclatura: `codigo` vs `id` em tipos internos

**Descrição:** O projeto adota consistentemente `codigo` no lugar de `id` nas entidades de domínio, conforme a convenção. Contudo, internamente em alguns DTOs e records anônimos há usos como `codigoSubprocessoDestino` ao invés de simplesmente `codigo`, quebrando a uniformidade. Mais relevante: o `ValidationResult` em `SubprocessoValidacaoService` usa `mensagem` em inglês no contexto do record mas os campos estão em português.

**Localização:** `backend/src/main/java/sgc/subprocesso/service/SubprocessoValidacaoService.java` (linha final)

**Evidência:**
```java
public record ValidationResult(boolean valido, @Nullable String mensagem) {
    public static ValidationResult ofValido() { ... }
    public static ValidationResult ofInvalido(String mensagem) { ... }
}
```

O nome da classe `ValidationResult` está em inglês, enquanto todo o restante do código usa português.

**Impacto:** Inconsistência na convenção do projeto (inglês misturado com português) e potencial confusão de manutenção.

**Sugestão:** Renomear para `ResultadoValidacao` com métodos `deValido()` e `deInvalido(String mensagem)`.

---

### 3.2 🟡 `AlertaService` com método `salvar` que não segue convenção de nomenclatura

**Descrição:** `AlertaService` expõe métodos `salvar` e `salvarTodos` com acesso `public` quando deveriam ser métodos de infraestrutura encapsulados dentro do próprio `AlertaFacade`. Métodos que salvam entidades diretamente em serviços de domínio violam a separação de preocupações.

**Localização:** `backend/src/main/java/sgc/alerta/AlertaService.java`

**Evidência:**
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertaService {
    public Alerta salvar(Alerta alerta) {  // acesso direto ao repositório exposto
        return alertaRepo.save(alerta);
    }

    public List<Alerta> salvarTodos(List<Alerta> alertas) {
        return alertaRepo.saveAll(alertas);
    }
```

**Impacto:** Qualquer outro service pode chamar `alertaService.salvar(alerta)` bypassing a lógica do `AlertaFacade`, que é o ponto de entrada canônico para alertas.

**Sugestão:** Transformar `salvar`/`salvarTodos` em métodos `package-private` ou mover para dentro do `AlertaFacade`.

---

### 3.3 🟡 Mistura entre `codSubprocesso` (parâmetro via URL) e `codigos` (lista via body) sem padronização clara

**Descrição:** As operações em bloco do `SubprocessoController` recebem `@PathVariable Long codSubprocesso` mas essa variável não é usada na maioria dos handlers de bloco — é apenas uma convenção de roteamento.

**Localização:** `backend/src/main/java/sgc/subprocesso/SubprocessoController.java` (linhas 331-341)

**Evidência:**
```java
@PostMapping("/{codSubprocesso}/aceitar-cadastro-bloco")
@PreAuthorize("hasRole('GESTOR') or hasRole('ADMIN')")
public void aceitarCadastroEmBloco(@PathVariable Long codSubprocesso,  // não utilizado no método
                                   @Valid @RequestBody ProcessarEmBlocoRequest request) {
    transicaoService.aceitarCadastroEmBloco(request.subprocessos());
}
```

**Impacto:** O parâmetro `codSubprocesso` é enganoso — sugere que a ação se aplica a um subprocesso específico, quando na verdade se aplica a uma lista. Causa confusão para novos desenvolvedores.

**Sugestão:** Remover `codSubprocesso` da URL para operações em bloco: `POST /api/subprocessos/aceitar-cadastro-bloco`, tornando explícito que é uma operação em nível de coleção. Alternativamente, usar `codProcesso` na URL para indicar o processo ao qual os subprocessos pertencem.

---

### 3.4 🟢 `mascarar` no `SgcPermissionEvaluator` deveria estar em `UtilSanitizacao`

**Descrição:** O método `mascarar(String valor)` no `SgcPermissionEvaluator` implementa mascaramento de dados sensíveis, mas `UtilSanitizacao` já existe exatamente para esse fim.

**Localização:** `backend/src/main/java/sgc/seguranca/SgcPermissionEvaluator.java`

**Evidência:**
```java
private String mascarar(String valor) {
    if (valor.length() <= 4) return "***";
    return "***" + valor.substring(valor.length() - 4);
}
```

**Sugestão:** Mover para `UtilSanitizacao.mascarar()` para reutilização e consistência.

---

### 3.5 🟢 `dadosValidos` com parâmetro ignorado no store

**Descrição:** `useSubprocessoStore` e `useProcessoStore` declaram `dadosValidos(_ : number): boolean { return false; }` — o parâmetro é um underscore silencioso, nunca utilizado, e a função sempre retorna `false`. O nome sugere que verifica dados, mas a implementação atual é uma função identicamente fixa.

**Localização:** `frontend/src/stores/subprocesso.ts:43`, `frontend/src/stores/processo.ts:29`

**Sugestão:** Se a intenção é nunca reusar, remover o parâmetro desnecessário e renomear para `estaInvalido(): boolean { return true; }` para deixar a semântica clara.

---

## 4. Verbosidade / Boilerplate Excessivo

### 4.1 🟡 Redundância no carregamento de contexto em `garantirContextoEdicaoPorProcessoEUnidade`

**Descrição:** Em `useSubprocessoStore`, o método `garantirContextoEdicaoPorProcessoEUnidade` faz o mesmo que `garantirContextoEdicao` mas via rota de processo+unidade. Após resolver o código, chama novamente `garantirContextoEdicao`, duplicando a configuração de `contextoEdicao.value`, `invalido.value = true` e `codSubprocessoCarregado.value`.

**Localização:** `frontend/src/stores/subprocesso.ts` (linhas 100–145)

**Evidência:**
```typescript
// Dentro da promessaCarregamento:
contextoEdicao.value = data;
erroIntegracaoContexto.value = null;
codSubprocessoCarregado.value = codigo;
invalido.value = true;
// E logo após:
const {codigo, contexto} = await promessaCarregamento;
contextoEdicao.value = contexto; // repete a atribuição
codSubprocessoCarregado.value = codigo; // repete
invalido.value = true; // repete
```

**Impacto:** Lógica de atribuição duplicada — se um campo novo for adicionado, precisa ser repetido em dois lugares.

**Sugestão:** Extrair uma função interna `_registrarContexto(codigo, contexto)` que centralize as atribuições.

---

### 4.2 🟡 Pares de métodos quase idênticos `aceitarCadastro` / `aceitarRevisaoCadastro`

**Descrição:** Em `SubprocessoTransicaoService`, há pares de métodos duplicados para cada ação de workflow: um para mapeamento e outro para revisão, diferindo apenas no boolean `isRevisao`.

**Localização:** `backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java`

**Evidência:**
```java
public void aceitarCadastro(Long codSubprocesso, @Nullable String observacoes) {
    Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
    Usuario usuario = usuarioFacade.usuarioAutenticado();
    executarAceite(sp, usuario, observacoes, false); // false = mapeamento
}

public void aceitarRevisaoCadastro(Long codSubprocesso, @Nullable String observacoes) {
    Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
    Usuario usuario = usuarioFacade.usuarioAutenticado();
    executarAceite(sp, usuario, observacoes, true); // true = revisão
}
```

O mesmo padrão se repete para `homologarCadastro`/`homologarRevisaoCadastro` e `devolverCadastro`/`devolverRevisaoCadastro`.

**Impacto:** Cada nova ação de workflow gera um par de métodos. Manutenção dobrada.

**Sugestão:** Unificar em um único método `aceitarCadastro(Long codSubprocesso, @Nullable String observacoes)` que deriva o contexto do `TipoProcesso` do próprio subprocesso (conforme sugerido em 2.4).

---

### 4.3 🟢 `MapaVisualizacaoService` recupera `Mapa` via `orElse(null)` seguido de `if (mapa == null)`

**Descrição:** O service recupera um Optional e converte para null imediatamente antes de verificar o null — padrão que contradiz o propósito do Optional.

**Localização:** `backend/src/main/java/sgc/mapa/service/MapaVisualizacaoService.java:27`

**Evidência:**
```java
Mapa mapa = mapaRepo.buscarCompletoPorSubprocesso(subprocesso.getCodigo()).orElse(null);

if (mapa == null) {
    if (subprocesso.getSituacao() != null && subprocesso.getSituacao().name().contains("MAPA")) {
        throw new ErroInconsistenciaInterna(...);
    }
    return criarRespostaVazia(subprocesso);
}
```

**Sugestão:**
```java
return mapaRepo.buscarCompletoPorSubprocesso(subprocesso.getCodigo())
    .map(mapa -> construirResposta(mapa, ...))
    .orElseGet(() -> {
        validarConsistenciaMapaAusente(subprocesso);
        return criarRespostaVazia(subprocesso);
    });
```

---

### 4.4 🟢 `CacheConfig` com blocos de configuração repetitivos

**Descrição:** Cada cache personalizado no `CacheConfig` é criado com o mesmo bloco de código, variando apenas `maximumSize` e `expireAfterWrite`.

**Localização:** `backend/src/main/java/sgc/comum/config/CacheConfig.java`

**Evidência:**
```java
cacheManager.registerCustomCache(CACHE_VW_UNIDADE, Caffeine.newBuilder()
        .recordStats()
        .maximumSize(1)
        .expireAfterWrite(java.time.Duration.ofMinutes(15))
        .build());
cacheManager.registerCustomCache(CACHE_VW_USUARIO, Caffeine.newBuilder()
        .recordStats()
        .maximumSize(1)
        .expireAfterWrite(java.time.Duration.ofMinutes(15))
        .build());
// ... repetido 4 vezes
```

**Sugestão:** Extrair método auxiliar `registrarCache(String nome, long tamanho, Duration ttl)`.

---

## 5. Defensividade Excessiva / Verificações Nulas

### 5.1 🔴 `paginaCodigos == null` mascara possível bug de N+1

**Descrição:** `ProcessoService.listarTodos()` e `listarIniciadosPorParticipantes()` verificam se a `Page<Long>` retornada pela query é `null`. Spring Data JPA nunca retorna `null` de queries de paginação — este check defensivo, além de desnecessário, mascara um bug em potencial: se por alguma razão `listarCodigos` retornar `null`, o fallback `processoRepo.findAll(pageable)` carregaria todos os processos com fetch join, causando N+1 silencioso e não auditado.

**Localização:** `backend/src/main/java/sgc/processo/service/ProcessoService.java:107,115`

**Evidência:**
```java
Page<Long> paginaCodigos = processoRepo.listarCodigos(pageable);
if (paginaCodigos == null) {         // nunca verdadeiro em JPA
    return processoRepo.findAll(pageable);  // fallback com N+1 silencioso
}
```

**Impacto:** Código enganoso que pode levar desenvolvedores a presumir que `null` é possível aqui. Em caso de bug no repositório, o fallback mascara o problema em vez de alertar.

**Sugestão:** Remover o check. O repositório deve garantir retorno de `Page.empty()` via anotação `@NonNull` ou pelo contrato implícito do Spring Data.

---

### 5.2 🟡 `getSituacao() != null` em enum sempre não-nulo por contrato do banco

**Descrição:** Em múltiplos locais, o código verifica `subprocesso.getSituacao() != null` antes de usar o valor, mesmo que `situacao` seja `@Column(nullable = false)` no banco e tenha valor default `NAO_INICIADO` no `@Builder.Default`.

**Localização:**
- `SubprocessoConsultaService.java:92`
- `MapaVisualizacaoService.java:30`
- `SubprocessoValidacaoService.java` (múltiplos)

**Evidência:**
```java
// SubprocessoConsultaService.java:92
if (subprocesso.getSituacao() != null && subprocesso.getSituacao().name().contains("MAPA")) {

// SubprocessoValidacaoService.java
public void validarSituacaoPermitida(Subprocesso subprocesso, ...) {
    if (subprocesso.getSituacao() == null) {
        throw new IllegalArgumentException("Situação do subprocesso não pode ser nula");
    }
```

**Impacto:** Código defensivo desnecessário que indica incerteza sobre o contrato das entidades. Se `getSituacao()` puder ser `null`, o banco tem um problema de consistência que não deveria ser tratado em camada de negócio.

**Sugestão:** Anotar o campo com `@NonNull` do JSpecify e remover as verificações desnecessárias. Validar na camada de persistência via `@Column(nullable=false)`.

---

### 5.3 🟡 `verificarPermissao` com `@Nullable Usuario` retorna `false` silenciosamente

**Descrição:** O método `verificarPermissao` do `SgcPermissionEvaluator` aceita `@Nullable Usuario`. Se `usuario` for `null`, retorna `false` silenciosamente, sem logging. Isso pode mascarar falhas de autenticação em cenários onde o usuário deveria estar presente.

**Localização:** `backend/src/main/java/sgc/seguranca/SgcPermissionEvaluator.java`

**Evidência:**
```java
public boolean verificarPermissao(@Nullable Usuario usuario, @Nullable Object alvo, AcaoPermissao acao) {
    return verificarPermissao(usuario, alvo, acao, true);
}

private boolean verificarPermissao(@Nullable Usuario usuario, @Nullable Object alvo, ...) {
    if (usuario == null) return false; // silencioso — sem log
```

**Impacto:** Uma falha de autenticação pode ser mascarada como simples "sem permissão", dificultando diagnóstico.

**Sugestão:** Adicionar log de warning quando `usuario == null` em chamadas que não são da interface `PermissionEvaluator`.

---

### 5.4 🟡 `LimitadorTentativasLogin.encontrarIpMaisAntigo` lança exceção em branch que não deveria ser atingível

**Descrição:** A lógica interna do `encontrarIpMaisAntigo` possui um fallback `orElseGet` que lança `ErroConfiguracao` em caso de cache vazio — mas o código envolto garante que `tentativasPorIp.size() >= maxCacheEntries` antes de chamar esse método.

**Localização:** `backend/src/main/java/sgc/seguranca/login/LimitadorTentativasLogin.java:69`

**Evidência:**
```java
private String encontrarIpMaisAntigo() {
    return tentativasPorIp.entrySet().stream()
            .filter(e -> !e.getValue().isEmpty())
            .min(Comparator.comparing(e -> e.getValue().peekFirst()))
            .map(Map.Entry::getKey)
            .orElseGet(() -> {
                String firstKey = tentativasPorIp.keySet().stream().findFirst().orElse(null);
                if (firstKey == null) {
                    throw new ErroConfiguracao("Limitador de login inconsistente...");
                }
                return firstKey;
            });
}
```

**Impacto:** Complexidade defensiva desnecessária para um branch inalcançável na prática.

**Sugestão:** Simplificar usando `.min().orElseThrow(...)` diretamente com mensagem de erro clara.

---

### 5.5 🟢 `subprocesso.getSituacao() != null && subprocesso.getSituacao().name().contains("MAPA")` acumulado

**Descrição:** Já coberto em 2.1 e 5.2, mas vale reforçar que a dupla verificação `!= null` + `.name().contains()` aparece 3 vezes no código. É um indicador de que o enum não possui um método semântico adequado e que há incerteza sobre a nulidade do campo.

**Localização:** Múltiplos services conforme 2.1.

**Sugestão:** Resolver via 2.1 (adicionar `ehEtapaMapa()` ao enum) e 5.2 (garantir `@NonNull`).

---

## 6. Nulabilidade e Optional

### 6.1 🔴 `invalido.value = true` **após** carregamento bem-sucedido nos stores invalida o cache imediatamente

**Descrição:** Em ambos os stores de frontend (`subprocesso.ts` e `processo.ts`), após carregar dados com sucesso, `invalido.value = true` é setado, o que torna o cache imediatamente inválido. A função `dadosValidos` sempre retorna `false`. Isso significa que toda navegação entre views dispara uma nova requisição HTTP ao backend, sem nenhum benefício de cache.

**Localização:** `frontend/src/stores/subprocesso.ts:78,132,139`, `frontend/src/stores/processo.ts:56`

**Evidência:**
```typescript
// subprocesso.ts — dentro de garantirContextoEdicao (sucesso)
const data = await promessaCarregamento;
contextoEdicao.value = data;
erroIntegracaoContexto.value = null;
codSubprocessoCarregado.value = codSubprocesso;
invalido.value = true; // ← invalida cache logo após carregar!

// A função de validação também sempre retorna false:
function dadosValidos(_: number): boolean {
    return false; // nunca reutiliza cache
}
```

**Impacto:** A store mantém estrutura de cache (mapa de promessas, `invalido`, `codSubprocessoCarregado`) mas não funciona como cache — toda ativação de rota vai ao backend. O código é mais complexo do que necessário por manter essa infraestrutura sem uso efetivo. O comentário na store documenta que "não é seguro reutilizar snapshots" mas ainda assim mantém toda a maquinaria de cache ociosa.

**Sugestão:** Se a decisão arquitetural é realmente nunca cachear (devido à natureza mutável das permissões por workflow), simplificar drasticamente a store removendo `invalido`, `carregamentosPorCodigo`, `dadosValidos` e mantendo apenas a deduplicação de requisições concorrentes (inflight dedup). Se o cache for desejável, implementar `dadosValidos` corretamente: `return !invalido.value && codSubprocessoCarregado.value === codSubprocesso`.

---

### 6.2 🟡 `buscarOpt` retorna `null` via `orElse(null)` quebrando o contrato de Optional

**Descrição:** `UsuarioFacade.carregarUsuarioSemAtribuicoesParaAutenticacao` e `buscarOpt` retornam `null` usando `orElse(null)`, anulando o benefício do `Optional`.

**Localização:** `backend/src/main/java/sgc/organizacao/UsuarioFacade.java:30,51`

**Evidência:**
```java
Usuario usuario = usuarioService.buscarOpt(titulo).orElse(null);
// ...
return usuarioService.buscarOpt(titulo).orElse(null);
```

**Impacto:** O chamador precisa verificar `null`, perdendo a semântica do Optional.

**Sugestão:** Retornar `Optional<Usuario>` e deixar o chamador decidir o comportamento com `.orElseThrow()` ou `.orElse(...)`.

---

### 6.3 🟡 `obterSugestoes` retorna `Map<String, Object>` ao invés de `String`

**Descrição:** O método `obterSugestoes` em `SubprocessoConsultaService` retorna `Map<String, Object>` contendo uma única entrada `"sugestoes" -> String`. Isso é uma estrutura artificial que força o caller a fazer map-lookup.

**Localização:** `backend/src/main/java/sgc/subprocesso/service/SubprocessoConsultaService.java`

**Evidência:**
```java
public Map<String, Object> obterSugestoes(Long codSubprocesso) {
    // ...
    return Map.of("sugestoes", sugestoes);  // Map com uma entrada
}
```

**Impacto:** Requer que o controller desestruture o mapa. Viola o princípio de retornar o tipo mais específico possível.

**Sugestão:** Criar um record `SugestoesDto(String sugestoes)` ou retornar diretamente `String`. O controller pode encapsular em `ResponseEntity<Map<String, String>>` se necessário para compatibilidade de API.

---

### 6.4 🟢 Uso de `listarAnalisesCadastro` (primeira análise) via `.stream().findFirst().orElse(null)`

**Descrição:** Em `SubprocessoConsultaService.java:496`, a análise mais recente de validação é buscada como `findFirst().orElse(null)`, retornando null que é propagado sem tratamento ao caller.

**Localização:** `backend/src/main/java/sgc/subprocesso/service/SubprocessoConsultaService.java:496`

**Evidência:**
```java
return listarAnalisesPorTipo(codSubprocesso, TipoAnalise.VALIDACAO).stream().findFirst().orElse(null);
```

**Sugestão:** Retornar `Optional<Analise>` e documentar que o campo pode estar ausente.

---

## 7. Lógica de Negócio no Frontend

### 7.1 🟡 Ordenação de subprocessos por data implementada no frontend

**Descrição:** `obterUltimaDataLimiteSubprocesso` em `subprocessoService.ts` compara duas datas para determinar qual é a mais recente — lógica de negócio que deveria ser centralizada no backend.

**Localização:** `frontend/src/services/subprocessoService.ts` (~linha 62)

**Evidência:**
```typescript
function obterUltimaDataLimiteSubprocesso(subprocesso: SubprocessoDetalheResponse["subprocesso"]): string {
    const dataLimiteEtapa1 = subprocesso.dataLimiteEtapa1;
    const dataLimiteEtapa2 = subprocesso.dataLimiteEtapa2;
    if (!dataLimiteEtapa2) {
        return dataLimiteEtapa1;
    }
    return dataLimiteEtapa1 > dataLimiteEtapa2 ? dataLimiteEtapa1 : dataLimiteEtapa2;
}
```

**Impacto:** Se a regra de negócio mudar (ex: adicionar etapa 3), o frontend precisará ser atualizado manualmente.

**Sugestão:** O backend deve retornar `ultimaDataLimite` como campo calculado no DTO.

---

### 7.2 🟡 Mapeamento de `SubprocessoDetalheResponse` para `SubprocessoDetalhe` no service de frontend

**Descrição:** A função `mapSubprocessoDetalheResponseParaModel` no `subprocessoService.ts` implementa transformação de dados complexa que poderia ser feita pelo backend, ou ser um mapper dedicado.

**Localização:** `frontend/src/services/subprocessoService.ts`

**Evidência:**
```typescript
export function mapSubprocessoDetalheResponseParaModel(dto: SubprocessoDetalheResponse): SubprocessoDetalhe {
    const subprocesso = dto.subprocesso;
    return {
        codigo: subprocesso.codigo,
        unidade: subprocesso.unidade,
        titular: dto.titular,
        // ... 10+ campos mapeados
        prazoEtapaAtual: subprocesso.dataLimiteEtapa2 ?? subprocesso.dataLimiteEtapa1, // lógica de negócio
        etapaAtual: subprocesso.etapaAtual ?? 1,  // default de negócio
        elementosProcesso: [],  // inicialização artificial
    };
}
```

**Impacto:** Lógica de fallback (`?? 1`) e inicialização de campos (`elementosProcesso: []`) misturam mapeamento com regra de negócio.

**Sugestão:** O backend deve retornar um DTO consolidado (`SubprocessoDetalheDto`) sem necessidade de remapeamento no frontend. O `etapaAtual ?? 1` é um exemplo de fallback que deveria viver no backend.

---

### 7.3 🟢 `aceitarCadastroEmBloco` no frontend encapsula lógica de payload de forma inconsistente

**Descrição:** As funções de ação em bloco no frontend (`aceitarCadastroEmBloco`, `homologarCadastroEmBloco`, etc.) constroem manualmente um payload com campos `acao` e `subprocessos`, mas `acao` é uma string estática que poderia ser derivada do endpoint.

**Localização:** `frontend/src/services/subprocessoService.ts`

**Evidência:**
```typescript
export async function aceitarCadastroEmBloco(...): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/aceitar-cadastro-bloco`, {
        acao: 'ACEITAR',  // redundante — o endpoint já define a ação
        subprocessos: payload.unidadeCodigos
    });
}
```

**Sugestão:** Remover o campo `acao` do payload, já que é redundante com a URL. O backend deve ignorar ou nem esperar esse campo.

---

## 8. Performance e Cache

### 8.1 🔴 N+1 em operações em bloco: `forEach` + `buscarSubprocesso` individual

**Descrição:** As operações em bloco (`aceitarCadastroEmBloco`, `homologarCadastroEmBloco`, `aceitarValidacaoEmBloco`, `homologarValidacaoEmBloco`) iteraram com `forEach` chamando `consultaService.buscarSubprocesso(codSubprocesso)` para cada código individualmente.

**Localização:** `backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java:755,791,550,579`

**Evidência:**
```java
public void aceitarCadastroEmBloco(List<Long> subprocessoCodigos) {
    Usuario usuario = usuarioFacade.usuarioAutenticado();
    subprocessoCodigos.forEach(codSubprocesso -> {
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso); // SELECT * for each!
        executarAceite(sp, usuario, "Avaliação em bloco", REVISAO == sp.getProcesso().getTipo());
    });
}
```

O mesmo padrão em `homologarCadastroEmBloco`, `aceitarValidacaoEmBloco`, `homologarValidacaoEmBloco`.

**Impacto:** Para um bloco com 50 subprocessos, são executadas 50 queries SELECT individuais dentro de uma única transação. Em produção com alto volume, isso pode causar timeouts de transação e degradação perceptível de performance.

**Sugestão:** Carregar todos os subprocessos em uma única query antes do loop:
```java
public void aceitarCadastroEmBloco(List<Long> subprocessoCodigos) {
    Usuario usuario = usuarioFacade.usuarioAutenticado();
    List<Subprocesso> subprocessos = subprocessoRepo.buscarPorCodigosComMapaEAtividades(subprocessoCodigos);
    subprocessos.forEach(sp -> executarAceite(sp, usuario, "Avaliação em bloco",
            REVISAO == sp.getProcesso().getTipo()));
}
```

---

### 8.2 🔴 `tornarMapasVigentes` com N+1 em `definirMapaVigente` por subprocesso

**Descrição:** Na finalização de processo, todos os mapas são tornados vigentes via `forEach` que chama `unidadeService.definirMapaVigente` para cada subprocesso individualmente.

**Localização:** `backend/src/main/java/sgc/processo/service/ProcessoService.java:483`

**Evidência:**
```java
private void tornarMapasVigentes(Long codProcesso) {
    consultaService.listarEntidadesPorProcesso(codProcesso)
            .forEach(sp -> unidadeService.definirMapaVigente(
                    sp.getUnidade().getCodigo(), sp.getMapa())); // UPDATE por subprocesso
}
```

**Impacto:** Para um processo com 100 unidades, são executadas 100 queries de UPDATE individuais no momento da finalização — operação crítica de workflow.

**Sugestão:** Implementar `definirMapasVigentesEmBloco(Map<Long, Mapa>)` no `UnidadeService` que executa batch update.

---

### 8.3 🔴 Cache de `VW_*` com `maximumSize(1)` — ineficaz para dados multi-unidade

**Descrição:** Os caches de views materializadas (`CACHE_VW_UNIDADE`, `CACHE_VW_USUARIO`, etc.) estão configurados com `maximumSize(1)`. Isso significa que o cache só guarda o resultado da última chamada, descartando resultados anteriores em qualquer cenário de acesso concorrente.

**Localização:** `backend/src/main/java/sgc/comum/config/CacheConfig.java`

**Evidência:**
```java
cacheManager.registerCustomCache(CACHE_VW_UNIDADE, Caffeine.newBuilder()
        .recordStats()
        .maximumSize(1) // inutilizável em acesso concorrente
        .expireAfterWrite(java.time.Duration.ofMinutes(15))
        .build());
```

**Impacto:** Cache com `maximumSize(1)` em ambiente multiusuário tem taxa de acerto próxima a zero. O sistema paga o overhead do cache sem receber seus benefícios.

**Sugestão:** Aumentar `maximumSize` para um valor representativo (ex: 1000 para VW_UNIDADE, 5000 para VW_USUARIO) ou remover esses caches se as views já tiverem performance adequada sem cache.

---

### 8.4 🟡 Builds de hierarquia de unidades repetidos por requisição em `obterDetalhesCompleto`

**Descrição:** `ProcessoService.obterDetalhesCompleto` constrói a hierarquia de unidades completa (`montarHierarquiaNoDto`) em cada chamada, mesmo que a estrutura organizacional raramente mude.

**Localização:** `backend/src/main/java/sgc/processo/service/ProcessoService.java:292`

**Impacto:** Para um processo com 200 unidades participantes, a construção do mapa hierárquico é recalculada em cada detalhe de processo visualizado.

**Sugestão:** Usar `@Cacheable(CACHE_MAPA_HIERARQUIA_UNIDADES)` para o resultado da hierarquia por processo, com invalidação em alterações de processo.

---

### 8.5 🟡 Frontend sem deduplicação de requisições para `subprocessoService.buscarSubprocessoDetalhe`

**Descrição:** `buscarSubprocessoDetalhe` e `buscarContextoEdicao` fazem chamadas independentes ao backend que retornam dados sobrepostos. Ambos buscam detalhes do subprocesso via endpoints diferentes mas com payload largamente redundante.

**Localização:** `frontend/src/services/subprocessoService.ts`

**Impacto:** Em `SubprocessoView.vue`, podem ser disparadas múltiplas chamadas ao backend para o mesmo subprocesso no mesmo ciclo de montagem.

**Sugestão:** Consolidar no `useSubprocessos` composable para garantir que um único contexto carregado sirva tanto para detalhes quanto para permissões.

---

## 9. Segurança

### 9.1 🔴 Mensagens de erro internas expostas diretamente ao cliente

**Descrição:** O `RestExceptionHandler` expõe mensagens de erro internas com prefixos informativos como `"ESTADO ILEGAL: "`, `"ARGUMENTO INVÁLIDO: "`, `"ACESSO NEGADO: "` concatenadas com `ex.getMessage()`. Para exceções como `IllegalStateException` e `IllegalArgumentException`, a mensagem pode conter detalhes de implementação interna (nomes de entidades, estados, campos do banco de dados) que não deveriam ser visíveis ao usuário final.

**Localização:** `backend/src/main/java/sgc/comum/erros/RestExceptionHandler.java:189,217,231,245,260`

**Evidência:**
```java
@ExceptionHandler(AccessDeniedException.class)
protected ResponseEntity<ErroApi> handleAccessDenied(AccessDeniedException ex) {
    ErroApi erroApi = ErroApi.builder()
            .status(HttpStatus.FORBIDDEN.value())
            .message("ACESSO NEGADO: " + ex.getMessage()) // expõe detalhe interno!
            .build();
    return buildResponseEntity(erroApi);
}

@ExceptionHandler(IllegalStateException.class)
protected ResponseEntity<ErroApi> handleIllegalStateException(IllegalStateException ex) {
    String message = "ESTADO ILEGAL: " + ex.getMessage(); // ex.getMessage() pode ter detalhes sensíveis
    // ...
}
```

**Impacto:** Vazamento de informações internas do sistema (nomes de entidades, estados de máquina, estrutura do banco) para clientes não autorizados. Viola OWASP A01:2021 - Broken Access Control e A05:2021 - Security Misconfiguration.

**Sugestão:**
```java
@ExceptionHandler(AccessDeniedException.class)
protected ResponseEntity<ErroApi> handleAccessDenied(AccessDeniedException ex) {
    log.debug("Acesso negado: {}", ex.getMessage()); // detalhe só no log
    return buildResponseEntity(ErroApi.builder()
            .status(HttpStatus.FORBIDDEN.value())
            .message("Acesso negado.")  // mensagem genérica para o cliente
            .build());
}
```

---

### 9.2 🔴 Fallback de autenticação via `Bearer` token além de cookie — sem validação de origem

**Descrição:** O `FiltroJwt` aceita tanto cookies `jwtToken` quanto header `Authorization: Bearer <token>`. O aceite de Bearer tokens via header aumenta a superfície de ataque para CSRF em contextos onde o token pode ser acessível via JavaScript.

**Localização:** `backend/src/main/java/sgc/seguranca/login/FiltroJwt.java:50`

**Evidência:**
```java
if (jwtToken == null) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        jwtToken = authHeader.substring(7); // aceita Bearer além do cookie
    }
}
```

**Impacto:** Em ambientes onde o JWT é armazenado em `localStorage` do browser (prática insegura), um ataque XSS pode exfiltrar o token para ser reutilizado via Bearer. O mecanismo duplo aumenta a superfície de ataque sem necessidade clara.

**Sugestão:** Documentar explicitamente por que o Bearer é necessário (ex: integração com clientes não-browser, testes de API). Se for apenas para testes, restringir o Bearer ao perfil `test`. Em produção, forçar uso exclusivo de cookie `HttpOnly`.

---

### 9.3 🟡 `limparCachePeriodico` no `LimitadorTentativasLogin` duplica lógica de `limparTentativasAntigas`

**Descrição:** `@Scheduled(fixedRate = 600000)` executa a mesma lógica de remoção de tentativas antigas que `limparTentativasAntigas` já faz por IP individual. A duplicação de lógica em código de segurança é perigosa — uma correção em um local pode não ser replicada no outro.

**Localização:** `backend/src/main/java/sgc/seguranca/login/LimitadorTentativasLogin.java`

**Sugestão:** `limparCachePeriodico` deve chamar `limparTentativasAntigas` para cada IP:
```java
@Scheduled(fixedRate = 600000)
public void limparCachePeriodico() {
    new ArrayList<>(tentativasPorIp.keySet()).forEach(this::limparTentativasAntigas);
}
```

---

### 9.4 🟢 `LimitadorTentativasLogin` conta tentativas por IP, não por usuário

**Descrição:** O limitador de login conta tentativas por IP, o que pode ser contornado por atacantes que alternam entre diferentes endereços IP (proxy rotativo). Além disso, usuários legítimos atrás de NAT compartilhado podem ser bloqueados por tentativas de outros usuários.

**Localização:** `backend/src/main/java/sgc/seguranca/login/LimitadorTentativasLogin.java`

**Impacto:** Baixa eficácia contra ataques distribuídos; possível bloqueio de usuários legítimos em redes corporativas com NAT.

**Sugestão:** Complementar com rate limiting por `tituloEleitoral` (não como único mecanismo, para evitar enumeração de usuários).

---

## 10. Arquitetura / Violação de Camadas

### 10.1 🔴 `SubprocessoController` com `{codSubprocesso}` que não é autorizado pelo parâmetro de URL nas operações em bloco

**Descrição:** As operações em bloco do `SubprocessoController` usam `@PreAuthorize("hasRole('GESTOR') or hasRole('ADMIN')")` em vez de `@PreAuthorize("hasPermission(#codSubprocesso, 'Subprocesso', '...')")`. O parâmetro `codSubprocesso` na URL não é validado como ponto de acesso ao recurso — qualquer Gestor pode chamar qualquer endpoint de bloco com qualquer `codSubprocesso` na URL.

**Localização:** `backend/src/main/java/sgc/subprocesso/SubprocessoController.java:331-341`

**Evidência:**
```java
@PostMapping("/{codSubprocesso}/aceitar-cadastro-bloco")
@PreAuthorize("hasRole('GESTOR') or hasRole('ADMIN')")  // sem verificação por recurso
public void aceitarCadastroEmBloco(@PathVariable Long codSubprocesso,
                                   @Valid @RequestBody ProcessarEmBlocoRequest request) {
    transicaoService.aceitarCadastroEmBloco(request.subprocessos()); // codSubprocesso ignorado
}
```

**Impacto:** Um Gestor de uma unidade pode tentar aceitar cadastros de subprocessos de outras unidades ao enviar os códigos corretos no body. A validação de permissão por subprocesso individual (`request.subprocessos()`) ocorre dentro do `aceitarCadastroEmBloco`, mas a URL sugere que `codSubprocesso` é um scoping que não existe.

**Sugestão:** Ou (a) validar que todos os subprocessos do `request.subprocessos()` pertencem ao `codProcesso` do usuário logado antes de executar, ou (b) remover `codSubprocesso` da URL e usar `codProcesso` como scope real com verificação `hasPermission(#codProcesso, 'Processo', 'ACAO_EM_BLOCO')`.

---

## Apêndice: Pontos Positivos Identificados

Para equilíbrio, vale destacar boas práticas encontradas no código:

1. **`SgcPermissionEvaluator`** — implementação central de permissões bem documentada, com separação clara de verificações de hierarquia e localização.
2. **`Mensagens.java`** — centralização de todas as mensagens de erro/validação em uma única classe bem organizada.
3. **`EntidadeBase`** — implementação correta de `equals`/`hashCode` baseada em `codigo` com proteção para `null`.
4. **`normalizeError` no frontend** — tratamento tipado e completo de erros de API, com mapeamento consistente de status HTTP para `ErrorKind`.
5. **`SituacaoSubprocesso` com `podeTransicionarPara`** — máquina de estados encapsulada no próprio enum, excelente design.
6. **`CacheConfig` com `recordStats()`** — observabilidade de cache habilitada desde a configuração.
7. **Testes extensivos** — suíte de testes abrangente tanto no backend (JUnit) quanto no frontend (Vitest, Playwright).
8. **`RestExceptionHandler`** com `traceId`** — rastreabilidade de erros correlacionada por requisição.
9. **Ausência de `console.log`** em código de produção no frontend — ESLint configurado corretamente.
10. **`LimitadorTentativasLogin`** — proteção contra força bruta implementada com `ConcurrentHashMap` thread-safe.

---

## Resumo de Ações Prioritárias

| Prioridade | Ação | Arquivo(s) |
|---|---|---|
| 🔴 P1 | Remover detalhes internos de erros expostos ao cliente | `RestExceptionHandler.java` |
| 🔴 P1 | Corrigir N+1 nas operações em bloco | `SubprocessoTransicaoService.java` |
| 🔴 P1 | Corrigir `dadosValidos` sempre retornando `false` ou simplificar stores | `subprocesso.ts`, `processo.ts` |
| 🔴 P1 | Validar permissão por recurso nas operações em bloco | `SubprocessoController.java` |
| 🔴 P1 | Corrigir cache VW com `maximumSize(1)` | `CacheConfig.java` |
| 🔴 P1 | Corrigir N+1 em `tornarMapasVigentes` | `ProcessoService.java` |
| 🟡 P2 | Adicionar `ehEtapaMapa()` ao enum e remover comparações por String | `SituacaoSubprocesso.java` |
| 🟡 P2 | Unificar métodos duplicados de aceite/homologação | `SubprocessoTransicaoService.java` |
| 🟡 P2 | Renomear `ValidationResult` para `ResultadoValidacao` | `SubprocessoValidacaoService.java` |
| 🟡 P2 | Mover cálculo de `ultimaDataLimite` para o backend | `SubprocessoDetalheDto`, `subprocessoService.ts` |
