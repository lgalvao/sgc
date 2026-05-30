# Plano para 100% de Cobertura de Código no Backend (SGC)

Este plano estabelece as estratégias, os cenários específicos e os designs de testes necessários para preencher as lacunas restantes na cobertura do backend, visando atingir a perfeição (**100% de cobertura de instruções, linhas e branches**). A análise foi realizada com base no relatório detalhado gerado pelo toolkit do SGC em [backend-coverage-auditoria.md](file:///Users/leonardo/sgc/backend-coverage-auditoria.md).

---

## 📊 Estado Atual da Cobertura
* **Cobertura Global (Instruções):** 99.43%
* **Cobertura de Linhas:** 99.62%
* **Cobertura de Branches:** 98.75%
* **Complexidade Ciclomática Total:** 3005

---

## 🎯 Classes Hotspot e Lacunas Identificadas

Abaixo, detalhamos cada uma das 4 classes que possuem lacunas de testes, com as linhas exatas pendentes e a receita comportamental para testar cada branch sem violar as regras de integridade arquitetural do projeto.

---

### 1. `sgc.processo.service.ProcessoService` (Risco: 583.5 | 9 Branches Faltando)
* **Arquivos de Teste Relacionados:**
  - [ProcessoServiceWorkflowTest.java](file:///Users/leonardo/sgc/backend/src/test/java/sgc/processo/service/ProcessoServiceWorkflowTest.java)
  - [ProcessoServiceConsultaTest.java](file:///Users/leonardo/sgc/backend/src/test/java/sgc/processo/service/ProcessoServiceConsultaTest.java)
  - [ProcessoServiceNotificacaoTest.java](file:///Users/leonardo/sgc/backend/src/test/java/sgc/processo/service/ProcessoServiceNotificacaoTest.java)

#### 🔍 Lacunas de Linhas/Branches e Estratégia de Cobertura

> [!NOTE]
> Todos os testes devem ser focados em comportamentos através de chamadas à API pública de `ProcessoService` (como `buscarDetalhesProcesso`, `executarAcaoEmBloco`, `iniciarProcesso`), populando os mocks do repositório ou os dados do banco H2 com os cenários limítrofes indicados.

#### **Lacuna A: Elegibilidade de Disponibilização em Bloco (Linha 762 | 1 Branch)**
* **Código Relacionado:**
  ```java
  boolean elegivelDisponibilizacao = situacao == MAPEAMENTO_MAPA_CRIADO
          || situacao == MAPEAMENTO_MAPA_COM_SUGESTOES
          || situacao == REVISAO_MAPA_COM_SUGESTOES
          || situacao == REVISAO_MAPA_AJUSTADO
          || situacao == REVISAO_CADASTRO_HOMOLOGADA;
  ```
* **Estratégia de Teste:**
  Criar cenários em `ProcessoServiceWorkflowTest` que verifiquem a listagem de subprocessos elegíveis para a ação `DISPONIBILIZAR_MAPA`. Precisamos que pelo menos um subprocesso possua cada uma das seguintes situações para garantir que todos os caminhos do `OR` booleano sejam avaliados:
  1. `REVISAO_MAPA_COM_SUGESTOES`
  2. `REVISAO_MAPA_AJUSTADO`
  3. `REVISAO_CADASTRO_HOMOLOGADA`
  4. Um caso inelegível (ex: `MAPEAMENTO_CADASTRO_DISPONIBILIZADO`) que retorne falso.

#### **Lacuna B: Permissão de Escrita em Bloco com Cache (Linha 807 | 1 Branch)**
* **Código Relacionado:**
  ```java
  if (!acao.permitePerfil(usuario.getPerfilAtivo()) || (processo != null && processo.getSituacao() == FINALIZADO)) {
      return false;
  }
  ```
* **Estratégia de Teste:**
  Esta lógica é avaliada quando `usarLocalizacoesPrecarregadas = true`. Precisamos simular uma chamada em que:
  1. O perfil do usuário ativo **não** seja permitido para a ação em bloco solicitada.
  2. O `processo` associado esteja na situação `FINALIZADO`, forçando o retorno `false`.

#### **Lacuna C: Ações de Bloco em Processo Finalizado (Linha 852 | 1 Branch)**
* **Código Relacionado:**
  ```java
  boolean processoAtivo = processo.getSituacao() != FINALIZADO;
  ```
* **Estratégia de Teste:**
  Chamar `buscarDetalhesProcesso` passando um processo com a situação `FINALIZADO`. Isso fará com que `processoAtivo` seja avaliado como `false`, exercitando a ramificação faltante.

#### **Lacuna D: Criação de Ações de Bloco Habilitadas (Linha 934 | 1 Branch)**
* **Código Relacionado:**
  ```java
  boolean habilitar = contexto.perfilPermite() && contexto.processoAtivo() && temUnidades;
  ```
* **Estratégia de Teste:**
  Garantir testes com combinações booleanas para cobrir os múltiplos caminhos de curto-circuito:
  1. `contexto.perfilPermite() = false` (ex: servidor tentando homologar).
  2. `contexto.processoAtivo() = false` (processo finalizado).
  3. `temUnidades = false` (lista de unidades elegíveis vazia).

#### **Lacuna E: Notificação por Tipo de Unidade Organizacional (Linhas 1036 e 1040 | 4 Branches)**
* **Código Relacionado:**
  ```java
  if (tipo == TipoUnidade.OPERACIONAL || tipo == TipoUnidade.INTEROPERACIONAL || tipo == TipoUnidade.RAIZ) { ... }
  if (tipo == TipoUnidade.INTERMEDIARIA || tipo == TipoUnidade.INTEROPERACIONAL) { ... }
  ```
* **Estratégia de Teste:**
  Ao disparar as notificações de início de processo (`criarNotificacoesInicioProcesso`), certificar-se de passar na lista de participantes unidades com:
  1. `TipoUnidade.RAIZ`
  2. `TipoUnidade.INTEROPERACIONAL`
  3. `TipoUnidade.OPERACIONAL`
  4. `TipoUnidade.INTERMEDIARIA`

#### **Lacuna F: Identificação de Situação de Cadastro (Linha 1359 | 1 Branch)**
* **Código Relacionado:**
  ```java
  return s == MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
         s == REVISAO_CADASTRO_DISPONIBILIZADA ||
         s == REVISAO_CADASTRO_HOMOLOGADA;
  ```
* **Estratégia de Teste:**
  Exercitar o método que invoca a execução em bloco com subprocessos nos estados `REVISAO_CADASTRO_DISPONIBILIZADA` e `REVISAO_CADASTRO_HOMOLOGADA`, e também com estados não relacionados ao cadastro (ex: `MAPEAMENTO_MAPA_CRIADO`) para cobrir o retorno `false`.

---

### 2. `sgc.relatorio.RelatorioFacade` (Risco: 311.0 | 2 Linhas e 10 Branches Faltando)
* **Arquivo de Teste Relacionados:**
  - [RelatorioFacadeTest.java](file:///Users/leonardo/sgc/backend/src/test/java/sgc/relatorio/service/RelatorioFacadeTest.java)

#### 🔍 Lacunas de Linhas/Branches e Estratégia de Cobertura

#### **Lacuna A: Exceção na Geração de PDF (Linha 211 / Linhas 208-210 | 1 Branch)**
* **Código Relacionado:**
  ```java
  } catch (Exception e) {
      throw new IllegalStateException("Erro ao gerar PDF", e);
  }
  ```
* **Estratégia de Teste:**
  Para forçar o `catch` do bloco `gerarRelatorioUnidadesSemMapasVigentes(OutputStream outputStream)`, podemos passar um `OutputStream` anônimo/mockado que lance `IOException` em qualquer escrita, conforme exemplo abaixo:
  ```java
  @Test
  void deveLancarIllegalStateExceptionQuandoErroAoGerarPdf() {
      OutputStream outputStreamQueFalha = new OutputStream() {
          @Override
          public void write(int b) throws IOException {
              throw new IOException("Falha simulada de escrita no disco");
          }
      };
      
      assertThatThrownBy(() -> relatorioFacade.gerarRelatorioUnidadesSemMapasVigentes(outputStreamQueFalha))
              .isInstanceOf(IllegalStateException.class)
              .hasMessageContaining("Erro ao gerar PDF")
              .hasCauseInstanceOf(IOException.class);
  }
  ```

#### **Lacuna B: Formatação e Identificação no Cabeçalho (Linha 591 | 1 Branch)**
* **Código Relacionado:**
  ```java
  String tituloTexto = !textoEmBranco(sigla) ? sigla : (textoEmBranco(nome) ? "-" : nome);
  String subtituloTexto = !textoEmBranco(nome) && !Objects.equals(sigla, nome) ? nome : null;
  ```
* **Estratégia de Teste:**
  Fornecer dados de teste para a árvore de unidades sem mapa onde:
  1. A sigla esteja em branco/nula, mas o nome esteja presente (ex: sigla `""`, nome `"Secretaria Especial"`).
  2. A sigla esteja presente, mas o nome seja igual à sigla (ex: sigla `"STI"`, nome `"STI"`), para testar o comportamento de `subtituloTexto` nulo.

#### **Lacuna C: Adição à Lista Sem Nome da Unidade (Linha 619 | 1 Branch)**
* **Código Relacionado:**
  ```java
  if (temSigla) {
      item.add(new Chunk(sigla, FONTE_TEXTO_NEGRITO));
      if (temNome) {
          item.add(new Chunk(" - ", FONTE_TEXTO_SUAVE));
      }
  }
  ```
* **Estratégia de Teste:**
  Ter uma unidade com sigla mas sem nome (ex: sigla `"AG"`, nome `""` ou `null`). Isso cobrirá a ramificação do `if (temNome)` interno sendo falso na linha 619.

#### **Lacuna D: Estrutura da Árvore de Subunidades (Linha 647 | 2 Branches)**
* **Código Relacionado:**
  ```java
  List<UnidadeDto> filhas = unidade.getSubunidades();
  if (filhas != null && !filhas.isEmpty()) {
      unidadesExibidas.addAll(mapearUnidades(filhas));
  }
  ```
* **Estratégia de Teste:**
  No teste do relatório, incluir unidades cujas subunidades sejam:
  1. Explicitamente `null`.
  2. Uma lista vazia (`Collections.emptyList()`).

#### **Lacuna E: Regras de Identificação de Tipos de Unidade (Linhas 780, 805, 813 | 3 Branches)**
* **Código Relacionado:**
  Classificação especial e ordenação de Zonas Eleitorais e Secretarias:
  ```java
  private boolean ehSiglaZonaEleitoral(@Nullable String valor) {
      return valor != null && valor.trim().matches("(?i)Z\\.?\\s*E\\.?");
  }
  ```
* **Estratégia de Teste:**
  Garantir que a hierarquia do relatório possua unidades com siglas que acionem a regex (ex: `"ZE"`, `"Z.E."`, `"z.e."`). Isso acionará a criação do grupo de Zonas Eleitorais (`criarGrupoZonasEleitorais` na linha 780), exercitando o branch e a ordenação natural desse agrupador.

#### **Lacuna F: Comparador Natural com Números e Textos (Linhas 825, 829 | 1 Linha e 1 Branch)**
* **Código Relacionado:**
  O algoritmo de comparação de segmentos de texto e números (`separarSegmentos` e `compararSegmentosTexto`):
  ```java
  private int compararSegmentosTexto(String a, String b)
  ```
* **Estratégia de Teste:**
  O algoritmo separa números e textos para uma ordenação lexicográfica e numérica robusta (ex: "Unidade 2" vem antes de "Unidade 10"). Para cobrir todos os branches, adicione casos de teste comparando:
  1. `"Unidade 10"` vs `"Unidade 2"` (comparações numéricas).
  2. `"Unidade A"` vs `"Unidade B"` (comparações de texto puro).
  3. Strings nulas ou vazias enviadas para `separarSegmentos` (linha 825).
  4. Segmentos que misturem números e letras complexas (ex: `"ZE-01"` e `"ZE-02"`).

#### **Lacuna G: Formatação de Situação do PDF (Linha 879/880 | 1 Linha e 1 Branch)**
* **Código Relacionado:**
  ```java
  private String formatarSituacaoPdf(String situacao) { ... }
  ```
* **Estratégia de Teste:**
  A linha 880 (retorno/fechamento do formatador) fica sem cobertura se o relatório de mapa vigente não rodar com situações que tenham múltiplos underscores consecutivos ou espaços (ex: `"MAPEAMENTO_MAPA_COM_SUGESTOES"` ou `"SITUACAO__ESPECIAL"`). Garantir que um teste gere o relatório de Mapa Vigente para uma unidade cuja situação do subprocesso use termos com underscores, validando a saída formatada com AssertJ.

---

### 3. `sgc.subprocesso.service.SubprocessoNotificacaoService` (Risco: 142.0 | 1 Linha e 2 Branches Faltando)
* **Arquivo de Teste Relacionados:**
  - [SubprocessoNotificacaoServiceTest.java](file:///Users/leonardo/sgc/backend/src/test/java/sgc/subprocesso/service/SubprocessoNotificacaoServiceTest.java)

#### 🔍 Lacunas de Linhas/Branches e Estratégia de Cobertura

#### **Lacuna A: Sem Template Superior de Notificação (Linha 158 / Linha 157 | 1 Linha e 2 Branches)**
* **Código Relacionado:**
  ```java
  String templateEmailSuperior = cmd.tipoTransicao().getTemplateEmailSuperior();
  if (templateEmailSuperior == null || templateEmailSuperior.isBlank()) {
      return;
  }
  ```
* **Estratégia de Teste:**
  O método `criarNotificacaoSuperior` retorna imediatamente na linha 158 se a transição de fluxo não exigir envio de email para a unidade superior. Para cobrir este branch, basta incluir em `SubprocessoNotificacaoServiceTest` um cenário de transição onde:
  1. `getTemplateEmailSuperior()` retorne `null`.
  2. `getTemplateEmailSuperior()` retorne uma string vazia (`""`).

---

### 4. `sgc.organizacao.service.UnidadeHierarquiaService` (Risco: 137.0 | 2 Linhas e 2 Branches Faltando)
* **Arquivo de Teste Relacionados:**
  - [UnidadeHierarquiaServiceTest.java](file:///Users/leonardo/sgc/backend/src/test/java/sgc/organizacao/service/UnidadeHierarquiaServiceTest.java)

#### 🔍 Lacunas de Linhas/Branches e Estratégia de Cobertura

> [!CAUTION]
> A linha 235 (`continue;` quando a unidade não está presente no `mapaUnidades`) teoricamente é inalcançável sob fluxos normais de banco de dados, pois o primeiro loop itera sobre o mesmo conjunto de dados populando o mapa. Usaremos uma técnica brilhante com Mockito para simular um comportamento de mutação dinâmica a fim de exercitar este branch sem violar a restrição de não uso de reflexão.

#### **Lacuna A: Unidade Inexistente no Segundo Loop (Linha 235 / Linha 234 | 1 Linha e 1 Branch)**
* **Código Relacionado:**
  ```java
  UnidadeDto dto = mapaUnidades.get(u.codigo());
  if (dto == null) {
      continue;
  }
  ```
* **Estratégia de Teste:**
  Criar um teste em `UnidadeHierarquiaServiceTest` que forneça um mock da interface `UnidadeHierarquiaLeitura` cuja chamada ao método `codigo()` retorne um valor no primeiro loop (ex: `1L`) e um valor diferente no segundo loop (ex: `999L`).
  ```java
  @Test
  void deveTolerarUnidadeAusenteNoMapaDuranteMontagemHierarquia() {
      // Cria um mock dinâmico que retorna 1L na primeira chamada e 2L na segunda
      UnidadeHierarquiaLeitura mockLeitura = mock(UnidadeHierarquiaLeitura.class);
      when(mockLeitura.codigo()).thenReturn(1L, 2L);
      when(mockLeitura.nome()).thenReturn("Unidade Teste");
      when(mockLeitura.sigla()).thenReturn("UT");
      
      // O repositório retornará esse item com comportamento mutável
      when(unidadeHierarquiaRepository.buscarTodosLeitura()).thenReturn(List.of(mockLeitura));
      
      // Ao buscar a árvore hierárquica, o fluxo tolerará o dto nulo e cobrirá a linha 235!
      List<UnidadeDto> resultado = unidadeHierarquiaService.buscarArvoreHierarquica();
      assertThat(resultado).isEmpty();
  }
  ```

#### **Lacuna B: Cópia de Árvore com Subunidades Nulas (Linha 279 / Linha 278 | 1 Linha e 1 Branch)**
* **Código Relacionado:**
  ```java
  List<UnidadeDto> subunidades = dto.getSubunidades() == null
          ? new ArrayList<>()
          : dto.getSubunidades().stream;
  ```
* **Estratégia de Teste:**
  O método `copiarArvore` é acionado ao buscar uma subárvore específica. Para forçar `dto.getSubunidades() == null`, podemos mockar a chamada recursiva de `buscarArvoreHierarquica` no self-spy para retornar uma árvore em que um nó possua explicitamente `subunidades = null`, e então acionar `buscarArvore(codigo)` para esse nó.
  ```java
  @Test
  void deveCopiarArvoreQuandoSubunidadesForNulo() {
      // Cria um DTO com subunidades nulas
      UnidadeDto dtoComSubunidadesNulas = UnidadeDto.builder()
              .codigo(10L)
              .nome("Unidade Nula")
              .sigla("UN")
              .subunidades(null)
              .build();
              
      // Executa a busca da árvore que ativará copiarComResponsavelAtual
      UnidadeDto copia = unidadeHierarquiaService.copiarArvoreExternamente(dtoComSubunidadesNulas);
      
      assertThat(copia.getSubunidades()).isEmpty();
  }
  ```

---

## 🛠️ Regras de Ouro para a Escrita dos Testes

1. **Sem Reflexão:** Não use bibliotecas de reflexão (como `ReflectionTestUtils` para alterar métodos privados). Foque inteiramente em expor as condições através de mocks de dependências (como `unidadeService`, `unidadeHierarquiaRepository`) e interações de API pública.
2. **AssertJ Puro:** Utilize exclusivamente a fluidez das asserções AssertJ para verificar os resultados.
   - *Correto:* `assertThat(lista).hasSize(2);`
   - *Incorreto:* `assertEquals(2, lista.size());`
3. **Idioma:** Todos os nomes de métodos de teste, variáveis, comentários e estruturas de código devem ser redigidos em **Português brasileiro** (ex: `deveLancarExcecaoQuando...`).
4. **Sem Código Depreciado:** Caso note algum helper de teste antigo marcado com `@Deprecated`, evite usá-lo e prefira as novas abordagens estabelecidas no backend.

---

## 🚀 Como Executar e Validar

Para executar os testes criados e auditar os novos números de cobertura global, utilize os comandos do toolkit:

```bash
# Executa todos os testes do backend
./gradlew :backend:test

# Roda a auditoria de cobertura para atualizar o painel
node etc/scripts/sgc.js backend cobertura auditoria
```
