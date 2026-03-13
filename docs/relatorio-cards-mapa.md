# Relatório de Alterações — Card "Mapa de Competências"

## Problema identificado

O card **Mapa de Competências** na tela de detalhes do subprocesso estava sempre
visível e clicável, independentemente da situação do subprocesso ou do perfil do
usuário. Isso contraria o CDU-07, que define regras precisas de quando o card deve
estar habilitado.

---

## Regra de negócio (CDU-07)

| Condição | Perfil | Comportamento esperado |
|---|---|---|
| Antes da homologação do cadastro | Qualquer | Card **visível, mas desabilitado** (não clicável) |
| Após homologação do cadastro (`CADASTRO_HOMOLOGADO`) | ADMIN | Card habilitado em **modo edição** |
| Após disponibilização do mapa (`MAPA_DISPONIBILIZADO`) | Todos os demais | Card habilitado em **modo visualização** |

A permissão que controla isso é `habilitarAcessoMapa`, calculada no backend pelo
método `verificarAcessoMapaHabilitado()` em `SubprocessoService.java`.

---

## O que estava errado

O backend calculava `habilitarAcessoMapa` corretamente e enviava o valor no DTO
`PermissoesSubprocessoDto`. No entanto, **o frontend simplesmente ignorava esse
campo**:

- `useAcesso.ts` não expunha `habilitarAcessoMapa`
- `SubprocessoCards.vue` renderizava sempre o card de visualização (`v-else`), sem
  verificar se o acesso ao mapa estava habilitado

---

## Alterações realizadas

### 1. `frontend/src/composables/useAcesso.ts`

Adicionada a exposição de `habilitarAcessoMapa`, lendo diretamente das permissões
calculadas pelo backend:

```ts
const habilitarAcessoMapa = computed(() =>
  getSubprocesso()?.permissoes?.habilitarAcessoMapa ?? false
);

return {
  // ... demais permissões ...
  habilitarAcessoMapa
};
```

---

### 2. `frontend/src/components/processo/SubprocessoCards.vue`

**Script:** Adicionado `habilitarAcessoMapa` na desestruturação de `useAcesso` e
criado o computed `mapaHabilitado`:

```ts
const {podeEditarCadastro, podeEditarMapa, habilitarAcessoMapa} = useAcesso(subprocesso);
const mapaHabilitado = computed(() => habilitarAcessoMapa.value);
```

**Template:** O card passou de 2 estados para 3 estados exclusivos:

```
podeEditarMapaFinal = true
  └─ card-subprocesso-mapa-edicao        (clicável, navega para edição do mapa)

podeEditarMapaFinal = false E mapaHabilitado = true
  └─ card-subprocesso-mapa-visualizacao  (clicável, navega para visualização do mapa)

podeEditarMapaFinal = false E mapaHabilitado = false
  └─ card-subprocesso-mapa-desabilitado  (visível, opaco, cursor not-allowed, sem navegação)
```

**Estilo:** Adicionada a classe `card-disabled` para o estado desabilitado:

```css
.card-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
```

---

## Alterações nos testes

### `frontend/src/composables/__tests__/useAcesso.spec.ts`

- Adicionado `habilitarAcessoMapa: true` nos dados do teste "deve mapear permissoes
  corretamente"
- Adicionada asserção `expect(acesso.habilitarAcessoMapa.value).toBe(false)` no
  teste de valores padrão
- Adicionada asserção `expect(acesso.habilitarAcessoMapa.value).toBe(true)` no
  teste de mapeamento

### `frontend/src/components/__tests__/SubprocessoCards.spec.ts`

- Adicionado `habilitarAcessoMapa: ref(accessOverrides.habilitarAcessoMapa ?? true)`
  no mock de `useAcesso` dentro de `mountComponent`
- Adicionado novo teste: **"renderiza card de mapa desabilitado quando acesso ao
  mapa não está habilitado"**, que verifica:
  - O `data-testid="card-subprocesso-mapa-desabilitado"` existe
  - Os cards de edição e visualização **não** existem
  - Clicar no card desabilitado **não** dispara navegação

### `e2e/cdu-07.spec.ts`

Corrigidas as asserções para ADMIN e CHEFE na situação inicial do processo
(`NAO_INICIADO`). Como a homologação do cadastro ainda não ocorreu, o card deve
aparecer **desabilitado**:

```
Antes: esperava card-subprocesso-mapa-visualizacao
Depois: espera card-subprocesso-mapa-desabilitado
```

---

## Arquivos modificados

| Arquivo | Tipo de mudança |
|---|---|
| `frontend/src/composables/useAcesso.ts` | Expõe `habilitarAcessoMapa` |
| `frontend/src/components/processo/SubprocessoCards.vue` | Lógica de 3 estados + estilo `card-disabled` |
| `frontend/src/composables/__tests__/useAcesso.spec.ts` | Testes atualizados |
| `frontend/src/components/__tests__/SubprocessoCards.spec.ts` | Mock atualizado + novo teste |
| `e2e/cdu-07.spec.ts` | Asserções corrigidas para situação inicial |
