# Plano de Refatoração: ADMIN (Perfil) vs RAIZ (Unidade) vs SEDOC (Apresentação)

**Data:** 2026-02-11  
**Contexto:** Correção do modelo conceitual para separar ADMIN (perfil) de SEDOC (unidade operacional) introduzindo unidade RAIZ

---

## 1. Problema Identificado

### 1.1. Situação Anterior (Incorreta)

O código implementava um **bypass mágico** para ADMIN no `AbstractAccessPolicy.verificaHierarquia()`:

```java
// CÓDIGO INCORRETO (a ser removido)
if (usuario.getPerfilAtivo() == Perfil.ADMIN && requisito != RequisitoHierarquia.TITULAR_UNIDADE) {
    return true; // ADMIN bypassa hierarquia
}
```

**Problemas:**
- ❌ Violava o princípio de que a hierarquia funciona igual para todos
- ❌ ADMIN era tratado como "super-usuário sem limites"
- ❌ Confundia ADMIN (perfil) com SEDOC (unidade)
- ❌ Verificações usavam `sp.getUnidade()` (unidade original) em vez de localização atual (movimentações)

### 1.2. Conceitos Corrigidos

**ADMIN é um PERFIL, não uma unidade:**
- Qualquer usuário da tabela `ADMINISTRADOR` pode logar com perfil ADMIN
- Ao logar como ADMIN, o usuário **não escolhe unidade** (automático)
- Sistema vincula automaticamente à **unidade RAIZ (id=1, sigla='ADMIN', tipo='RAIZ')**
- `VW_USUARIO_PERFIL_UNIDADE` já faz isso: `select a.usuario_titulo, 'ADMIN' as perfil, 1 as unidade_codigo`

**RAIZ é uma unidade virtual:**
- Existe apenas internamente para consistência do modelo (todo usuário tem unidade)
- É a raiz da árvore de unidades (superior a todas)
- **Nunca aparece para o usuário final**
- Árvore de unidades mostra apenas filhas da RAIZ (SEDOC, outras secretarias, etc.)
- Definição no SQL: `select 1, 'UNIDADE RAIZ ADMINISTRATIVA', 'ADMIN', ..., 'RAIZ', 'ATIVA', null`

**SEDOC é uma unidade operacional comum:**
- Não é mais "especial" no código/hierarquia
- Pessoas da SEDOC geralmente têm perfil ADMIN disponível
- Podem logar como ADMIN (unidade RAIZ) ou como CHEFE/GESTOR (unidade SEDOC)
- **Para o usuário:** Em movimentações, alertas e histórico, quando a unidade é RAIZ (id=1), exibimos "SEDOC"

---

## 2. Mudanças Necessárias

### 2.1. Backend - Políticas de Acesso

#### 2.1.1. `AbstractAccessPolicy.verificaHierarquia()`

**Arquivo:** `backend/src/main/java/sgc/seguranca/acesso/AbstractAccessPolicy.java`

**Remover completamente o bypass de ADMIN:**

```java
// ANTES (linhas 75-83 - INCORRETO):
protected boolean verificaHierarquia(Usuario usuario, Unidade unidade, RequisitoHierarquia requisito) {
    final Long codUnidadeRecurso = unidade.getCodigo();
    final Long codUnidadeUsuario = usuario.getUnidadeAtivaCodigo();

    // ADMIN tem privilégios especiais POR SER ADMIN, não por estar em unidade específica
    // ADMIN bypassa verificações de hierarquia (exceto TITULAR_UNIDADE que é pessoal)
    if (usuario.getPerfilAtivo() == Perfil.ADMIN && requisito != RequisitoHierarquia.TITULAR_UNIDADE) {
        return true; // ❌ REMOVER ISSO
    }
    
    return switch (requisito) {
        // ...
    };
}

// DEPOIS (CORRETO):
protected boolean verificaHierarquia(Usuario usuario, Unidade unidade, RequisitoHierarquia requisito) {
    final Long codUnidadeRecurso = unidade.getCodigo();
    final Long codUnidadeUsuario = usuario.getUnidadeAtivaCodigo();
    
    return switch (requisito) {
        case NENHUM -> true;
        case MESMA_UNIDADE -> Objects.equals(codUnidadeUsuario, codUnidadeRecurso);
        case MESMA_OU_SUBORDINADA -> Objects.equals(codUnidadeUsuario, codUnidadeRecurso)
                || hierarquiaService.isSubordinada(unidade, 
                    Unidade.builder().codigo(codUnidadeUsuario).build());
        case SUPERIOR_IMEDIATA -> hierarquiaService.isSuperiorImediata(unidade, 
                    Unidade.builder().codigo(codUnidadeUsuario).build());
        case TITULAR_UNIDADE -> {
            String tituloTitular = unidade.getTituloTitular();
            yield tituloTitular.equals(usuario.getTituloEleitoral());
        }
    };
}
```

**Justificativa:** ADMIN (unidade RAIZ, id=1) é superior a todas as unidades. Quando usa `MESMA_OU_SUBORDINADA`, a verificação retorna `true` naturalmente porque todas as unidades são subordinadas à RAIZ.

#### 2.1.2. `SubprocessoAccessPolicy`

**Arquivo:** `backend/src/main/java/sgc/seguranca/acesso/SubprocessoAccessPolicy.java`

**Mudanças:**

1. **Remover `ACOES_ADMIN_GLOBAIS`** (linhas 33-39):
```java
// ❌ REMOVER COMPLETAMENTE estas linhas:
private static final EnumSet<Acao> ACOES_ADMIN_GLOBAIS = EnumSet.of(
    VISUALIZAR_SUBPROCESSO, VISUALIZAR_MAPA, VISUALIZAR_DIAGNOSTICO,
    EDITAR_MAPA, DEVOLVER_CADASTRO, ACEITAR_CADASTRO, HOMOLOGAR_CADASTRO,
    DEVOLVER_REVISAO_CADASTRO, ACEITAR_REVISAO_CADASTRO, HOMOLOGAR_REVISAO_CADASTRO,
    DISPONIBILIZAR_MAPA, VALIDAR_MAPA, HOMOLOGAR_MAPA, DEVOLVER_MAPA,
    ALTERAR_DATA_LIMITE, REABRIR_CADASTRO, REABRIR_REVISAO
);
```

2. **Remover verificação especial no `canExecute()`** (linhas 258-261):
```java
// ❌ REMOVER este bloco:
// 3. Verifica hierarquia (ADMIN é global para ações administrativas)
if (temPerfil(usuario, ADMIN) && ACOES_ADMIN_GLOBAIS.contains(acao)) {
    return true;
}
```

3. **Corrigir perfis permitidos - `EDITAR_CADASTRO`** (linha 98-101):
```java
// ANTES:
Map.entry(EDITAR_CADASTRO, new RegrasAcao(
    EnumSet.of(ADMIN, GESTOR, CHEFE), // ❌ ADMIN e GESTOR não deveriam
    EnumSet.of(NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO),
    RequisitoHierarquia.MESMA_UNIDADE)),

// DEPOIS:
Map.entry(EDITAR_CADASTRO, new RegrasAcao(
    EnumSet.of(CHEFE), // ✅ Apenas CHEFE (CDU-08)
    EnumSet.of(NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO),
    RequisitoHierarquia.MESMA_UNIDADE)),
```

4. **Corrigir perfis - `EDITAR_REVISAO_CADASTRO`** (linha 124-127):
```java
// ANTES:
Map.entry(EDITAR_REVISAO_CADASTRO, new RegrasAcao(
    EnumSet.of(ADMIN, GESTOR, CHEFE), // ❌
    EnumSet.of(REVISAO_CADASTRO_EM_ANDAMENTO),
    RequisitoHierarquia.MESMA_UNIDADE)),

// DEPOIS:
Map.entry(EDITAR_REVISAO_CADASTRO, new RegrasAcao(
    EnumSet.of(CHEFE), // ✅ Apenas CHEFE (CDU-10)
    EnumSet.of(REVISAO_CADASTRO_EM_ANDAMENTO),
    RequisitoHierarquia.MESMA_UNIDADE)),
```

5. **Corrigir perfis - `EDITAR_MAPA`** (linha 155-163):
```java
// ANTES:
Map.entry(EDITAR_MAPA, new RegrasAcao(
    EnumSet.of(ADMIN, GESTOR, CHEFE), // ❌
    EnumSet.of(...),
    RequisitoHierarquia.MESMA_UNIDADE)),

// DEPOIS:
Map.entry(EDITAR_MAPA, new RegrasAcao(
    EnumSet.of(ADMIN), // ✅ Apenas ADMIN (CDU-15, CDU-16)
    EnumSet.of(NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
            MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO_MAPA_CRIADO,
            MAPEAMENTO_MAPA_COM_SUGESTOES, REVISAO_CADASTRO_EM_ANDAMENTO,
            REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO,
            REVISAO_MAPA_COM_SUGESTOES,
            DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO),
    RequisitoHierarquia.MESMA_UNIDADE)),
```

#### 2.1.3. `AtividadeAccessPolicy`

**Arquivo:** `backend/src/main/java/sgc/seguranca/acesso/AtividadeAccessPolicy.java`

**Corrigir todas as ações de atividade** (apenas CHEFE, conforme CDU-08):

```java
// ANTES (linhas 34-46):
private static final Map<Acao, RegrasAcaoAtividade> REGRAS = Map.ofEntries(
    Map.entry(CRIAR_ATIVIDADE, new RegrasAcaoAtividade(
            EnumSet.of(ADMIN, GESTOR, CHEFE) // ❌
    )),
    Map.entry(EDITAR_ATIVIDADE, new RegrasAcaoAtividade(
            EnumSet.of(ADMIN, GESTOR, CHEFE) // ❌
    )),
    Map.entry(EXCLUIR_ATIVIDADE, new RegrasAcaoAtividade(
            EnumSet.of(ADMIN, GESTOR, CHEFE) // ❌
    )),
    Map.entry(ASSOCIAR_CONHECIMENTOS, new RegrasAcaoAtividade(
            EnumSet.of(ADMIN, GESTOR, CHEFE) // ❌
    ))
);

// DEPOIS:
private static final Map<Acao, RegrasAcaoAtividade> REGRAS = Map.ofEntries(
    Map.entry(CRIAR_ATIVIDADE, new RegrasAcaoAtividade(
            EnumSet.of(CHEFE) // ✅ CDU-08
    )),
    Map.entry(EDITAR_ATIVIDADE, new RegrasAcaoAtividade(
            EnumSet.of(CHEFE) // ✅ CDU-08
    )),
    Map.entry(EXCLUIR_ATIVIDADE, new RegrasAcaoAtividade(
            EnumSet.of(CHEFE) // ✅ CDU-08
    )),
    Map.entry(ASSOCIAR_CONHECIMENTOS, new RegrasAcaoAtividade(
            EnumSet.of(CHEFE) // ✅ CDU-08
    ))
);
```

**Nota:** As atividades têm verificação adicional `RequisitoHierarquia.TITULAR_UNIDADE`, que permanece inalterada.

---

### 2.2. Backend - Apresentação (RAIZ → SEDOC)

O usuário **nunca vê** a unidade RAIZ. Quando a unidade RAIZ (id=1, sigla='ADMIN') apareceria em movimentações/alertas/histórico, deve ser exibida como **"SEDOC"**.

#### 2.2.1. `MovimentacaoMapper` (atualizar)

**Arquivo:** `backend/src/main/java/sgc/subprocesso/mapper/MovimentacaoMapper.java`

**Adicionar método helper para mapear sigla:**

```java
@Mapper(componentModel = "spring", config = CentralMapperConfig.class)
public interface MovimentacaoMapper {
    
    @Mapping(target = "unidadeOrigemSigla", 
             expression = "java(mapUnidadeSiglaParaUsuario(movimentacao.getUnidadeOrigem()))")
    @Mapping(target = "unidadeDestinoSigla", 
             expression = "java(mapUnidadeSiglaParaUsuario(movimentacao.getUnidadeDestino()))")
    // ... outros mapeamentos
    MovimentacaoDto toDto(Movimentacao movimentacao);
    
    /**
     * Mapeia sigla da unidade, substituindo RAIZ (id=1) por "SEDOC" para o usuário.
     * A unidade RAIZ é interna/técnica e nunca deve aparecer na UI.
     */
    default String mapUnidadeSiglaParaUsuario(Unidade unidade) {
        if (unidade == null) return "";
        // Se é unidade RAIZ (id=1), exibe "SEDOC" para o usuário
        return unidade.getCodigo() == 1L ? "SEDOC" : unidade.getSigla();
    }
}
```

#### 2.2.2. Serviços de Notificação/Alerta

**Locais a atualizar:**
- `backend/src/main/java/sgc/alerta/service/AlertaService.java` (ou similar)
- `backend/src/main/java/sgc/notificacao/service/NotificacaoService.java` (ou similar)
- Templates de email que referenciam unidades

**Criar utility method:**

```java
/**
 * Obtém a sigla da unidade para exibição ao usuário.
 * A unidade RAIZ (id=1, sigla='ADMIN') é substituída por "SEDOC" para clareza.
 */
private String obterSiglaParaUsuario(Unidade unidade) {
    if (unidade ==null) return "";
    if (unidade.getCodigo() == 1L) {
        return "SEDOC"; // Usuário vê SEDOC em vez de RAIZ/ADMIN
    }
    return unidade.getSigla();
}
```

**Usar em todas as referências a unidades de origem/destino em:**
- Descrições de alertas
- Assuntos e corpos de emails
- Logs e histórico visível ao usuário

---

### 2.3. Frontend

#### 2.3.1. Menu de Navegação

**Verificar:** A condição de exibir "Unidades" vs "Minha unidade" para ADMIN está correta.

**Localização provável:** Componente de menu/navegação principal

**Lógica esperada:**
```javascript
menuItem = usuario.perfil === 'ADMIN' ? 'Unidades' : 'Minha unidade'
```

#### 2.3.2. Árvore de Unidades

**Arquivo:** `frontend/src/components/ArvoreUnidades.vue`

**Verificar:** Garantir que a unidade RAIZ não é renderizada. A árvore deve começar pelas filhas diretas da RAIZ (SEDOC, outras secretarias).

**Lógica esperada:**
```javascript
// Não renderiza raiz, apenas filhas
unidadesParaMostrar = todasUnidades.filter(u => u.tipo !== 'RAIZ')
// ou
raiz = encontrarUnidadeRaiz()
unidadesParaMostrar = raiz.filhas
```

---

### 2.4. Documentação

#### 2.4.1. `regras-acesso.md`

**Arquivo:** `regras-acesso.md` (raiz do projeto)

**Atualizações necessárias:**

1. **Remover seção duplicada:**
   - Linhas 238 e 242 têm "## 4. Arquitetura de Controle de Acesso" duplicado
   - Manter apenas uma, remover a outra

2. **Limpar seção final:**
   - Linhas 492-519 são restos de versão anterior
   - Remover completamente

3. **Reescrever seção 4.4:**
   - Título atual: "Por Que ADMIN Bypassa Hierarquia?"
   - Novo título: "Por Que ADMIN Não Precisa de Regras Especiais?"
   - Explicar que ADMIN está na RAIZ, que é superior a todas
   - Remover referências ao bypass
   - Explicar RAIZ (interno) vs SEDOC (apresentação)

**Nova redação sugerida para seção 4.4:**

```markdown
### 4.4. Por Que ADMIN Não Precisa de Regras Especiais?

**Conceito:** ADMIN é um perfil vinculado à unidade RAIZ (id=1). A RAIZ é a raiz da árvore de unidades, superior a todas as outras unidades do sistema.

**Como funciona:**
- ADMIN está na unidade RAIZ (id=1, sigla='ADMIN', tipo='RAIZ')
- RAIZ não tem unidade superior (`unidade_superior_codigo = null`)
- RAIZ é superior (direta ou indiretamente) a todas as outras unidades

**Verificação de hierarquia:**
- `MESMA_OU_SUBORDINADA`: Como todas as unidades são subordinadas à RAIZ, ADMIN pode acessar qualquer recurso que permite este requisito
- `SUPERIOR_IMEDIATA`: ADMIN pode acessar recursos de unidades filhas diretas da RAIZ
- `MESMA_UNIDADE`: ADMIN só pode acessar recursos especificamente da RAIZ (raro)
- `TITULAR_UNIDADE`: ADMIN não pode acessar (unidade RAIZ não tem titular)
- `NENHUM`: Permite a ação independente de hierarquia

**Não há bypass:** A hierarquia funciona igual para todos os perfis. ADMIN tem acesso amplo porque está no topo da árvore, não por tratamento especial no código.

**RAIZ vs SEDOC:**
- **Internamente:** RAIZ (id=1, sigla='ADMIN', tipo='RAIZ')
- **Para o usuário:** "SEDOC" em movimentações, alertas, histórico
- **Na UI:** RAIZ nunca aparece (árvore mostra apenas filhas)
- **No menu:** ADMIN vê "Unidades" em vez de "Minha unidade"

**Por que SEDOC nas mensagens?**
Os usuários ADMIN geralmente são da SEDOC (unidade operacional), então faz sentido apresentar as ações administrativas como vindas da "SEDOC" em vez de uma "RAIZ" técnica/invisível.
```

4. **Atualizar exemplos práticos:**
   - Remover menções ao bypass
   - Atualizar cenários para refletir hierarquia natural

---

## 3. Impactos em Testes

### 3.1. Testes de Integração

**Provável quebra:** Testes que assumem que ADMIN pode fazer qualquer coisa sem restrições.

**Ações necessárias:**
- Identificar testes CDU que envolvem ADMIN
- Garantir que ADMIN está na unidade RAIZ (id=1) nos dados de teste
- Para ações de análise/homologação: garantir que subprocessos "chegaram" na RAIZ via movimentações

**Exemplo de correção:**

```java
// ANTES (pode falhar):
@Test
void adminPodeHomologarCadastro() {
    Usuario admin = criarUsuario(Perfil.ADMIN);
    // ... admin pode não ter unidade ou estar em unidade errada
}

// DEPOIS:
@Test
void adminPodeHomologarCadastro() {
    Usuario admin = criarUsuario(Perfil.ADMIN, 1L); // unidade RAIZ
    // Criar movimentação levando subprocesso até a RAIZ
    criarMovimentacao(subprocesso, unidadeSEDOC, unidadeRAIZ);
    // ...
}
```

### 3.2. Testes Unitários de Policies

**Locais:**
- `SubprocessoAccessPolicyTest`
- `AtividadeAccessPolicyTest`
- `ProcessoAccessPolicyTest`

**Verificações:**
- Testes de ADMIN devem configurar usuário com `unidadeAtivaCodigo = 1L`
- Testes de hierarquia devem refletir que RAIZ (1) é superior a todas
- Testes que verificam ações de CHEFE devem falhar para ADMIN onde apropriado

**Exemplo:**

```java
@Test
void adminNaoPodeEditarCadastro() {
    // ADMIN não está na lista de perfis permitidos para EDITAR_CADASTRO
    Usuario admin = Usuario.builder()
        .perfilAtivo(Perfil.ADMIN)
        .unidadeAtivaCodigo(1L)
        .build();
    
    Subprocesso sp = criarSubprocesso(unidade10);
    
    boolean pode = policy.canExecute(admin, Acao.EDITAR_CADASTRO, sp);
    assertFalse(pode, "ADMIN não deve poder editar cadastro (apenas CHEFE)");
}
```

---

## 4. Checklist de Execução

### Fase 1: Backend - Políticas de Acesso (Crítico)
- [ ] `AbstractAccessPolicy.java`: Remover bypass de ADMIN (linhas 75-83)
- [ ] `SubprocessoAccessPolicy.java`: Remover `ACOES_ADMIN_GLOBAIS` (linhas 33-39)
- [ ] `SubprocessoAccessPolicy.java`: Remover verificação especial `canExecute()` (linhas 258-261)
- [ ] `SubprocessoAccessPolicy.java`: `EDITAR_CADASTRO` → apenas `CHEFE` (linha 99)
- [ ] `SubprocessoAccessPolicy.java`: `EDITAR_REVISAO_CADASTRO` → apenas `CHEFE` (linha 125)
- [ ] `SubprocessoAccessPolicy.java`: `EDITAR_MAPA` → apenas `ADMIN` (linha 156)
- [ ] `AtividadeAccessPolicy.java`: Todas ações → apenas `CHEFE` (linhas 35-46)

### Fase 2: Backend - Apresentação (Importante)
- [ ] `MovimentacaoMapper.java`: Adicionar `mapUnidadeSiglaParaUsuario()` e usar em DTOs
- [ ] Serviços de alerta: Usar `obterSiglaParaUsuario()` em descrições
- [ ] Serviços de notificação: Atualizar templates de email (RAIZ→SEDOC)
- [ ] Verificar todos os lugares que constroem mensagens com siglas de unidades

### Fase 3: Testes (Crítico)
- [ ] Executar suite de testes unitários de policies
- [ ] Corrigir testes que assumiam bypass de ADMIN
- [ ] Executar testes de integração (CDUs)
- [ ] Corrigir testes que criam usuários ADMIN sem unidade ou com unidade errada
- [ ] Adicionar testes verificando que ADMIN NÃO pode editar cadastros/atividades

### Fase 4: Frontend (Verificação)
- [ ] Verificar menu: "Unidades" para ADMIN, "Minha unidade" para outros
- [ ] Verificar `ArvoreUnidades.vue`: RAIZ não é renderizada
- [ ] Testar visualmente: RAIZ não aparece em lugar nenhum
- [ ] Verificar movimentações exibem "SEDOC" quando origem/destino é RAIZ

### Fase 5: Documentação (Importante)
- [ ] `regras-acesso.md`: Remover seção duplicada (linha 238 ou 242)
- [ ] `regras-acesso.md`: Limpar linhas 492-519 (lixo)
- [ ] `regras-acesso.md`: Reescrever seção 4.4 (sem bypass, RAIZ/SEDOC)
- [ ] `regras-acesso.md`: Atualizar exemplos práticos

### Fase 6: Validação Final (Crítico)
- [ ] Login como ADMIN: não escolhe unidade, aparece "Unidades" no menu
- [ ] Fluxo completo: criar processo → mapear → disponibilizar → homologar (ADMIN)
- [ ] Verificar histórico de movimentações mostra "SEDOC" não "ADMIN" ou "RAIZ"
- [ ] Verificar emails enviar "SEDOC" nas mensagens
- [ ] ADMIN não consegue editar cadastro de unidades operacionais
- [ ] ADMIN consegue editar/criar mapas (ação exclusiva)

---

## 5. Riscos e Mitigações

| Risco | Probabilidade | Impacto | Mitigação |
|-------|---------------|---------|-----------|
| Testes de integração quebrados | **Alta** | Médio | Executar suite completa, corrigir sistematicamente |
| ADMIN perde acessos necessários | Média | **Alto** | Revisar CDUs, testar end-to-end, rollback se necessário |
| RAIZ aparece na UI | Baixa | Médio | Code review de mappers, teste visual completo |
| Emails/alertas referenciam RAIZ | Média | Médio | Buscar por `unidade.getSigla()` em notificações |
| ADMIN ganha acessos indevidos | Baixa | **Alto** | Testes específicos para ações só de CHEFE |

---

## 6. Critérios de Sucesso

✅ **Sem bypass:** `AbstractAccessPolicy` não tem tratamento especial para ADMIN  
✅ **Perfis corretos:** Ações de atividade/cadastro apenas para CHEFE  
✅ **Hierarquia natural:** ADMIN (RAIZ) acessa via hierarquia, não magic  
✅ **RAIZ invisível:** UI nunca mostra unidade RAIZ em nenhum lugar  
✅ **SEDOC visível:** Movimentações/alertas/emails mostram "SEDOC" quando id=1  
✅ **Testes passam:** Suite completa executa com sucesso  
✅ **Documentação clara:** `regras-acesso.md` reflete novo modelo corretamente  
✅ **CDUs funcionam:** Fluxos end-to-end de mapeamento/revisão funcionam  

---

## 7. Referências

**CDUs relevantes:**
- CDU-08: Manter cadastro (apenas CHEFE)
- CDU-09: Disponibilizar cadastro (CHEFE titular)
- CDU-13: Analisar cadastro (GESTOR aceita, ADMIN homologa)
- CDU-15: Manter mapa (ADMIN)
- CDU-16: Ajustar mapa (ADMIN)

**Arquivos SQL:**
- `backend/etc/sql/ddl_views.sql`: Definição da view VW_UNIDADE (linha 190: unidade RAIZ)
- `backend/etc/sql/ddl_views.sql`: Definição de VW_USUARIO_PERFIL_UNIDADE (linha 272: ADMIN → unidade 1)

**Documentação:**
- `etc/reqs/_intro-glossario.md`: Definições atualizadas
- `regras-acesso.md`: Documento de regras de acesso (a ser atualizado)

---

**Responsável:** Antigravity AI  
**Revisor:** Leonardo Galvão  
**Versão:** 1.0  
**Próxima ação:** Iniciar Fase 1 após aprovação do plano
