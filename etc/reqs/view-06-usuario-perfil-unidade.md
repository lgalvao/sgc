# VIEW-06 - VW_USUARIO_PERFIL_UNIDADE - Perfis e Permissões de Usuários

## Finalidade

Esta view é o **coração do sistema de autorização** do SGC. Estabelece o mapeamento completo entre usuários, perfis e unidades, determinando quais perfis cada usuário pode assumir e em quais unidades pode atuar. É consultada imediatamente após o login para determinar as opções de acesso do usuário e é a base para todas as validações de permissão no sistema.

## Origem dos Dados

**Tabelas do SGC:**
- `ADMINISTRADOR`: Usuários com perfil ADMIN

**Views do Sistema:**
- `VW_USUARIO`: Usuários ativos com lotação
- `VW_RESPONSABILIDADE`: Responsáveis por unidades
- `VW_UNIDADE`: Unidades e suas classificações

## Estrutura da View

| Coluna | Tipo | Descrição | Origem |
|--------|------|-----------|--------|
| `usuario_titulo` | VARCHAR2(12) | Título de eleitor do usuário (PK) | Consolidado de todas as fontes |
| `perfil` | VARCHAR2(20) | Perfil do usuário (PK) | Calculado (ver RN-VIEW06-02) |
| `unidade_codigo` | NUMBER | Código da unidade onde o perfil se aplica (PK) | Calculado por perfil |

**Chave primária composta:** (`usuario_titulo`, `perfil`, `unidade_codigo`)

**Justificativa da PK:**
- Um usuário pode ter múltiplos perfis
- Um usuário pode atuar em múltiplas unidades
- Cada combinação (usuário, perfil, unidade) é única

## Regras de Negócio

### RN-VIEW06-01: Estrutura da View - UNION de Perfis

A view consolida quatro consultas UNION, uma para cada tipo de perfil:

```sql
SELECT usuario_titulo, perfil, unidade_codigo
FROM (
    -- Query 1: Perfil ADMIN
    -- Query 2: Perfil GESTOR
    -- Query 3: Perfil CHEFE
    -- Query 4: Perfil SERVIDOR
)
```

**Características:**
- Cada query é independente e determina um perfil específico
- UNION combina todos os resultados, eliminando duplicatas
- Ordem das queries não afeta o resultado

### RN-VIEW06-02: Perfil ADMIN

```sql
SELECT a.usuario_titulo, 'ADMIN' as perfil, 1 as unidade_codigo
FROM administrador a
JOIN vw_usuario u ON u.titulo = a.usuario_titulo
```

**Critérios:**
- Usuário deve estar cadastrado em `ADMINISTRADOR`
- Usuário deve existir em `VW_USUARIO` (lotação ativa)
- Unidade é sempre 1 (ADMIN - unidade raiz virtual)

**Características do perfil ADMIN:**
- Acesso total ao sistema
- Cria, configura e monitora processos
- Homologa cadastros e mapas de competências
- Cadastra atribuições temporárias
- Acessa configurações do sistema
- Atua no contexto da unidade ADMIN (código 1)

**Perda do perfil:**
- Remoção da tabela `ADMINISTRADOR`
- Encerramento de lotação (desaparece de `VW_USUARIO`)

### RN-VIEW06-03: Perfil GESTOR

```sql
SELECT r.usuario_titulo, 'GESTOR' as perfil, r.unidade_codigo
FROM vw_responsabilidade r
JOIN vw_unidade u ON r.unidade_codigo = u.codigo 
WHERE u.tipo IN ('INTERMEDIARIA', 'INTEROPERACIONAL')
```

**Critérios:**
- Usuário deve ser responsável por uma unidade (titular, substituto ou atribuição temporária)
- Unidade deve ser do tipo `INTERMEDIARIA` ou `INTEROPERACIONAL`
- Responsabilidade deve estar vigente (implícito em `VW_RESPONSABILIDADE`)

**Características do perfil GESTOR:**
- Validação de informações de unidades subordinadas
- Submissão para análise da unidade superior
- Devolução para unidade subordinada (quando há ajustes)
- Visualização da árvore de unidades subordinadas
- Não cadastra atividades próprias (se unidade for INTERMEDIARIA)

**Unidades elegíveis:**
- **INTERMEDIARIA**: Tem subordinadas operacionais, mas apenas titular lotado
- **INTEROPERACIONAL**: Tem subordinadas operacionais E servidores próprios (acumula GESTOR + CHEFE)

**Múltiplas unidades:**
- Usuário pode ser GESTOR de várias unidades simultaneamente
- Cada entrada na view representa uma unidade diferente
- Após login, usuário seleciona em qual unidade vai atuar

### RN-VIEW06-04: Perfil CHEFE

```sql
SELECT r.usuario_titulo, 'CHEFE' as perfil, r.unidade_codigo
FROM vw_responsabilidade r
JOIN vw_unidade u ON r.unidade_codigo = u.codigo 
WHERE u.tipo IN ('INTEROPERACIONAL', 'OPERACIONAL')
```

**Critérios:**
- Usuário deve ser responsável por uma unidade
- Unidade deve ser do tipo `INTEROPERACIONAL` ou `OPERACIONAL`
- Responsabilidade deve estar vigente

**Características do perfil CHEFE:**
- Cadastro de atividades e conhecimentos da unidade
- Submissão de cadastro para validação
- Validação de mapas de competências
- Indicação de sugestões para mapas
- Gestão de equipe da unidade

**Unidades elegíveis:**
- **OPERACIONAL**: Unidade-folha com equipe (2+ servidores), sem subordinadas
- **INTEROPERACIONAL**: Tem subordinadas E equipe própria (acumula CHEFE + GESTOR)

**Acumulação GESTOR+CHEFE:**
- Usuários de unidades INTEROPERACIONAL têm ambos os perfis
- Podem alternar entre perfis conforme necessidade
- Ações diferentes disponíveis em cada perfil

### RN-VIEW06-05: Perfil SERVIDOR

```sql
SELECT usu.titulo as usuario_titulo, 'SERVIDOR' as perfil, uni.codigo as unidade_codigo
FROM vw_usuario usu
JOIN vw_unidade uni ON usu.unidade_comp_codigo = uni.codigo
WHERE usu.titulo <> uni.titulo_titular
```

**Critérios:**
- Usuário deve existir em `VW_USUARIO`
- Usuário NÃO deve ser o titular da unidade de competência
- Unidade de competência determina a unidade do perfil

**Características do perfil SERVIDOR:**
- Participa apenas de processos de DIAGNÓSTICO
- Não cadastra atividades em processos de Mapeamento/Revisão
- Não valida informações de outras unidades
- Avalia importância e domínio de competências (em diagnósticos)

**Unidade aplicável:**
- Sempre a `unidade_comp_codigo` do usuário (de `VW_USUARIO`)
- Pode ser diferente da unidade de lotação (casos de unidades SEM_EQUIPE)

**Exclusão de titulares:**
- Titulares já têm perfil CHEFE ou GESTOR
- Condição `usu.titulo <> uni.titulo_titular` evita duplicação
- Substitutos e atribuídos temporariamente também são excluídos (pois aparecem como titulares em `VW_RESPONSABILIDADE`)

### RN-VIEW06-06: Múltiplos Perfis e Unidades

Um usuário pode aparecer múltiplas vezes na view:

**Exemplo 1 - Administrador que também é CHEFE:**
```
usuario_titulo: 001234567890
Registros:
  (001234567890, ADMIN, 1)
  (001234567890, CHEFE, 150)
  (001234567890, SERVIDOR, 150)  -- Se não for titular da 150
```

**Exemplo 2 - GESTOR de múltiplas unidades:**
```
usuario_titulo: 002345678901
Registros:
  (002345678901, GESTOR, 100)
  (002345678901, GESTOR, 120)
  (002345678901, CHEFE, 100)  -- Se 100 for INTEROPERACIONAL
```

**Exemplo 3 - Servidor comum:**
```
usuario_titulo: 003456789012
Registros:
  (003456789012, SERVIDOR, 200)
```

### RN-VIEW06-07: Validação de Usuários Ativos

Todas as queries fazem JOIN com `VW_USUARIO`:

**Implicações:**
- Usuário sem lotação ativa não aparece na view
- Perda de lotação = perda imediata de todos os perfis
- Dados sempre sincronizados com SGRH

**Exceção - Perfil ADMIN:**
- Cadastro em `ADMINISTRADOR` persiste após perda de lotação
- Mas usuário não consegue logar se não estiver em `VW_USUARIO`
- Requer correção: ou remover de `ADMINISTRADOR` ou reativar lotação

## Casos de Uso da View

### CU-VIEW06-01: Seleção de Perfil e Unidade no Login (CDU-01)

**Contexto:** Após autenticação bem-sucedida, determinar perfis disponíveis.

**Fluxo:**

```sql
-- 1. Buscar todos os perfis/unidades do usuário
SELECT perfil, unidade_codigo
FROM VW_USUARIO_PERFIL_UNIDADE
WHERE usuario_titulo = :titulo_autenticado
ORDER BY perfil, unidade_codigo;

-- 2. Processar resultados:
-- - Se 1 registro: Selecionar automaticamente
-- - Se > 1 registro: Mostrar tela de seleção
```

**Tela de seleção:**
- Agrupar por perfil
- Para cada perfil, listar unidades aplicáveis
- Usuário seleciona par (perfil, unidade)
- Sistema armazena na sessão

**Exemplo de interface:**
```
Selecione seu perfil e unidade:

● ADMIN
  └─ Unidade ADMIN

● GESTOR  
  ├─ Coordenadoria A (CDA)
  └─ Coordenadoria B (CDB)

● CHEFE
  └─ Seção X (SECX)
```

### CU-VIEW06-02: Validação de Acesso a Funcionalidades

**Contexto:** Antes de exibir tela ou permitir ação, validar se usuário tem perfil adequado.

**Implementação:**

```sql
-- Verificar se usuário tem perfil ADMIN
SELECT COUNT(*) > 0 AS tem_perfil_admin
FROM VW_USUARIO_PERFIL_UNIDADE
WHERE usuario_titulo = :titulo_usuario
  AND perfil = 'ADMIN';

-- Verificar se usuário é CHEFE da unidade específica
SELECT COUNT(*) > 0 AS pode_cadastrar
FROM VW_USUARIO_PERFIL_UNIDADE
WHERE usuario_titulo = :titulo_usuario
  AND perfil = 'CHEFE'
  AND unidade_codigo = :codigo_unidade_subprocesso;
```

**Validações comuns:**
- Criar processo: Requer perfil ADMIN
- Cadastrar atividades: Requer perfil CHEFE na unidade do subprocesso
- Validar cadastro: Requer perfil GESTOR na unidade superior
- Homologar mapa: Requer perfil ADMIN
- Participar de diagnóstico: Requer perfil SERVIDOR ou superior

### CU-VIEW06-03: Filtragem de Processos Visíveis (CDU-02)

**Contexto:** Tela Painel mostra apenas processos relevantes ao usuário.

**Implementação:**

```sql
-- Buscar unidades onde usuário pode atuar
SELECT DISTINCT unidade_codigo
FROM VW_USUARIO_PERFIL_UNIDADE
WHERE usuario_titulo = :titulo_usuario;

-- Buscar processos que incluem essas unidades ou suas subordinadas
SELECT DISTINCT p.*
FROM PROCESSO p
JOIN UNIDADE_PROCESSO up ON p.codigo = up.processo_codigo
WHERE up.unidade_codigo IN (:lista_unidades_usuario)
   OR up.unidade_codigo IN (
       SELECT codigo FROM VW_UNIDADE
       START WITH codigo IN (:lista_unidades_usuario)
       CONNECT BY PRIOR codigo = unidade_superior_codigo
   );
```

**Lógica:**
- ADMIN: Vê todos os processos (unidade 1 é raiz de todas)
- GESTOR/CHEFE: Vê processos da sua unidade e subordinadas
- SERVIDOR: Vê processos da sua unidade de competência

### CU-VIEW06-04: Controle de Menu e Botões

**Contexto:** Exibir opções de menu conforme perfil ativo.

**Implementação (frontend):**

```typescript
// Verificar se usuário tem perfil ADMIN em alguma unidade
const temPerfilAdmin = perfisUsuario.some(p => p.perfil === 'ADMIN');

// Mostrar botão "Criar processo" apenas para ADMIN
<button v-if="temPerfilAdmin" @click="criarProcesso">
  Criar Processo
</button>

// Mostrar link de Configurações apenas para ADMIN
<router-link v-if="perfilAtual === 'ADMIN'" to="/configuracoes">
  <i class="bi bi-gear"></i>
</router-link>
```

### CU-VIEW06-05: Auditoria e Logs

**Contexto:** Registrar perfil usado ao realizar ação.

**Implementação:**

```sql
-- Ao registrar análise, movimentação, etc.
INSERT INTO ANALISE (
    subprocesso_codigo, tipo, acao, usuario_titulo, ...
) VALUES (
    :subprocesso, :tipo, :acao, :titulo_usuario, ...
);

-- Para auditoria posterior, consultar perfil que usuário tinha na época
SELECT p.perfil, p.unidade_codigo
FROM VW_USUARIO_PERFIL_UNIDADE p
WHERE p.usuario_titulo = :titulo_usuario_acao
  AND p.unidade_codigo = :unidade_acao;
```

**Limitação:** View mostra estado atual, não histórico. Para auditoria completa:
- Registrar perfil usado no momento da ação
- Ou reconstituir permissões históricas através de `VW_RESPONSABILIDADE` + `ATRIBUICAO_TEMPORARIA`

### CU-VIEW06-06: Relatórios de Usuários e Perfis

**Contexto:** Gerar relatórios de distribuição de perfis.

**Exemplos de consultas:**

```sql
-- Contar usuários por perfil
SELECT perfil, COUNT(DISTINCT usuario_titulo) AS qtd_usuarios
FROM VW_USUARIO_PERFIL_UNIDADE
GROUP BY perfil
ORDER BY qtd_usuarios DESC;

-- Listar todos os administradores
SELECT u.nome, u.email, u.unidade_lot_codigo
FROM VW_USUARIO_PERFIL_UNIDADE p
JOIN VW_USUARIO u ON p.usuario_titulo = u.titulo
WHERE p.perfil = 'ADMIN';

-- Unidades sem CHEFE definido
SELECT uni.codigo, uni.sigla, uni.nome
FROM VW_UNIDADE uni
WHERE uni.tipo IN ('OPERACIONAL', 'INTEROPERACIONAL')
  AND NOT EXISTS (
      SELECT 1 FROM VW_USUARIO_PERFIL_UNIDADE
      WHERE perfil = 'CHEFE' AND unidade_codigo = uni.codigo
  );

-- Usuários com múltiplos perfis
SELECT usuario_titulo, COUNT(DISTINCT perfil) AS qtd_perfis
FROM VW_USUARIO_PERFIL_UNIDADE
GROUP BY usuario_titulo
HAVING COUNT(DISTINCT perfil) > 1;
```

## Relação com Outras Views e Tabelas

### Dependências

**VW_USUARIO:**
- Valida que usuários têm lotação ativa
- Fornece `unidade_comp_codigo` para perfil SERVIDOR
- JOIN em todos os perfis

**VW_RESPONSABILIDADE:**
- Determina perfis GESTOR e CHEFE
- Filtra por tipo de unidade

**VW_UNIDADE:**
- Filtra unidades por tipo para GESTOR e CHEFE
- Fornece `titulo_titular` para exclusão em SERVIDOR

**ADMINISTRADOR:**
- Determina perfil ADMIN
- Tabela gerenciada manualmente

### Consumidores da View

**CDU-01 (Login):**
- Determinação de perfis disponíveis
- Seleção de perfil/unidade

**CDU-02 (Painel):**
- Filtragem de processos visíveis
- Controle de exibição de botões

**Todos os casos de uso:**
- Validações de autorização
- Controle de acesso

## Dependências

### Permissões Necessárias

Todas as permissões são herdadas das views dependentes:
- Permissões do SGRH (via `VW_USUARIO`, `VW_RESPONSABILIDADE`, `VW_UNIDADE`)
- Acesso à tabela `ADMINISTRADOR` (própria do SGC)

### Tabelas e Views de Origem

- `ADMINISTRADOR`: Perfil ADMIN
- `VW_USUARIO`: Validação de usuários ativos
- `VW_RESPONSABILIDADE`: Perfis GESTOR e CHEFE
- `VW_UNIDADE`: Classificação de unidades e titulares

## Considerações de Performance

### Otimização de UNION

A view usa UNION (não UNION ALL), eliminando duplicatas:

**Duplicatas possíveis:**
- Teoricamente não devem ocorrer (perfis são mutuamente exclusivos por construção)
- UNION garante que mesmo com dados inconsistentes não haverá duplicatas

**Considerar UNION ALL:**
- Se garantido que não há sobreposição
- Eliminaria overhead de remoção de duplicatas
- Requer análise cuidadosa das queries

### Materialização

**Argumentos a favor:**
- View consultada em CADA login (alta frequência)
- Consultas em validações de permissão (muito frequentes)
- Dados mudam relativamente pouco

**Argumentos contra:**
- Dados dependem de múltiplas fontes (SGRH, ADMINISTRADOR, ATRIBUICAO_TEMPORARIA)
- Mudanças devem refletir imediatamente (substituições, atribuições)
- Lógica relativamente simples

**Recomendação:**
- Inicialmente manter como view regular
- Monitorar performance
- Se necessário, criar materialized view com refresh frequente (ex: a cada 5 minutos)

### Índices

Como é uma view, índices devem estar nas tabelas base:

```sql
-- ADMINISTRADOR
CREATE INDEX idx_admin_titulo ON ADMINISTRADOR(usuario_titulo);

-- Índices em VW_RESPONSABILIDADE, VW_USUARIO, VW_UNIDADE são suficientes
```

## Exemplo de Registros

```
+-----------------+----------+----------------+
| usuario_titulo  | perfil   | unidade_codigo |
+-----------------+----------+----------------+
| 001234567890    | ADMIN    | 1              |
| 001234567890    | CHEFE    | 150            |
| 002345678901    | GESTOR   | 100            |
| 002345678901    | CHEFE    | 100            |
| 003456789012    | SERVIDOR | 200            |
| 004567890123    | CHEFE    | 250            |
| 004567890123    | SERVIDOR | 250            |
| 005678901234    | GESTOR   | 120            |
| 005678901234    | GESTOR   | 130            |
+-----------------+----------+----------------+
```

**Interpretação:**

1. **001234567890:**
   - Administrador do sistema (pode atuar na unidade ADMIN)
   - Também é CHEFE da unidade 150
   - Após login, escolhe entre ADMIN ou CHEFE-150

2. **002345678901:**
   - GESTOR e CHEFE da unidade 100
   - Unidade 100 é INTEROPERACIONAL (acumula perfis)
   - Pode atuar como gestor (validando subordinadas) ou chefe (cadastrando atividades)

3. **003456789012:**
   - Apenas SERVIDOR da unidade 200
   - Não é responsável pela unidade
   - Participa apenas de diagnósticos

4. **004567890123:**
   - CHEFE da unidade 250 (responsável)
   - Também aparece como SERVIDOR (mas não deveria, há um erro lógico aqui*)
   - *Nota: Isso indica inconsistência - titular não deveria ter perfil SERVIDOR

5. **005678901234:**
   - GESTOR de duas unidades (120 e 130)
   - É responsável (titular, substituto ou atribuição) de ambas
   - Após login, escolhe em qual unidade vai atuar

## Notas de Implementação

### Validações de Consistência

**Inconsistência possível: Titular como SERVIDOR**

```sql
-- Titular não deveria ter perfil SERVIDOR na própria unidade
-- A condição WHERE usu.titulo <> uni.titulo_titular deveria prevenir isso
-- Mas pode ocorrer se:
-- 1. uni.titulo_titular for NULL (unidade sem titular)
-- 2. Usuário for substituto/atribuído (aparecem em VW_RESPONSABILIDADE mas não em VW_UNIDADE.titulo_titular)
```

**Solução:**
```sql
-- Query SERVIDOR corrigida:
SELECT usu.titulo, 'SERVIDOR', uni.codigo
FROM vw_usuario usu
JOIN vw_unidade uni ON usu.unidade_comp_codigo = uni.codigo
WHERE usu.titulo NOT IN (
    SELECT usuario_titulo 
    FROM vw_responsabilidade 
    WHERE unidade_codigo = uni.codigo
);
```

### Mudanças Dinâmicas

**Eventos que afetam perfis:**

1. **Nomeação/Dispensa de titular:**
   - Ganha/perde perfil GESTOR ou CHEFE
   - `VW_UNIDADE.titulo_titular` muda → `VW_RESPONSABILIDADE` muda

2. **Início/Fim de substituição:**
   - Substituto ganha perfil ao iniciar
   - Perde perfil ao terminar
   - `VW_RESPONSABILIDADE` reflete automaticamente

3. **Atribuição temporária:**
   - Usuário atribuído ganha perfil
   - Ao expirar, perde perfil (se não for titular/substituto)
   - `VW_RESPONSABILIDADE` filtra por vigência

4. **Mudança de tipo de unidade:**
   - Unidade muda de OPERACIONAL para INTERMEDIARIA
   - Responsável perde perfil CHEFE, ganha apenas GESTOR
   - `VW_UNIDADE.tipo` muda → perfis se ajustam

5. **Encerramento de lotação:**
   - Usuário desaparece de `VW_USUARIO`
   - Perde todos os perfis
   - Não consegue mais fazer login

### Sessão e Cache

**Armazenamento em sessão:**
- Após login, armazenar perfis disponíveis
- Ao selecionar perfil/unidade, armazenar na sessão
- Não reconsultar view a cada requisição

**Atualização de sessão:**
- Considerar TTL para sessão
- Revalidar perfis periodicamente (ex: a cada 1 hora)
- Ou forçar logout ao detectar mudança de permissões

**Segurança:**
- Sempre validar permissões no backend
- Não confiar apenas em dados da sessão
- Reconsultar view para operações críticas
