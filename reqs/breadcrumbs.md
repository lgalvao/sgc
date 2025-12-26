# Especificação: Barra de Navegação (Breadcrumbs)

## Visão Geral

A barra de navegação exibe breadcrumbs contextuais que indicam a localização atual do usuário na hierarquia de
navegação do sistema. Inclui também um botão de voltar para facilitar a navegação.

## Componentes da Barra

### Botão Voltar

- Exibido em todas as páginas **exceto** Login e Painel
- Ao clicar, navega para a página anterior do histórico (`router.back()`)
- Estilo discreto (outline cinza, ícone pequeno)

### Breadcrumbs

- Exibidos em todas as páginas **exceto** Login e Painel
- Sempre iniciam com o ícone de "casa" (🏠) que leva ao Painel
- O último item é sempre o ativo (não clicável)
- Separador: `›`

## Estrutura por Tipo de Rota

### Rotas de Processo

**Rota**: `/processo/:codProcesso`  
**Nome**: `Processo`

| Perfil            | Breadcrumbs                          |
|-------------------|--------------------------------------|
| ADMIN / GESTOR    | 🏠 > Detalhes do processo            |
| CHEFE / SERVIDOR  | 🏠                                   |

**Regra**: Para CHEFE e SERVIDOR, o breadcrumb "Detalhes do processo" é omitido pois eles só veem processos da própria
unidade.

### Rotas de Subprocesso

Rotas que pertencem a um subprocesso específico de uma unidade.

| Rota                                                | Nome                   | Breadcrumbs (ADMIN/GESTOR)                                       |
|-----------------------------------------------------|------------------------|------------------------------------------------------------------|
| `/processo/:codProcesso/:siglaUnidade`              | Subprocesso            | 🏠 > Detalhes do processo > [SIGLA]                              |
| `/processo/:codProcesso/:siglaUnidade/mapa`         | SubprocessoMapa        | 🏠 > Detalhes do processo > [SIGLA] > Mapa de competências       |
| `/processo/:codProcesso/:siglaUnidade/vis-mapa`     | SubprocessoVisMapa     | 🏠 > Detalhes do processo > [SIGLA] > Visualizar mapa            |
| `/processo/:codProcesso/:siglaUnidade/cadastro`     | SubprocessoCadastro    | 🏠 > Detalhes do processo > [SIGLA] > Atividades e conhecimentos |
| `/processo/:codProcesso/:siglaUnidade/vis-cadastro` | SubprocessoVisCadastro | 🏠 > Detalhes do processo > [SIGLA] > Atividades e conhecimentos |

Para CHEFE e SERVIDOR, o "Detalhes do processo" é omitido:

| Rota                                                | Breadcrumbs (CHEFE/SERVIDOR)              |
|-----------------------------------------------------|-------------------------------------------|
| `/processo/:codProcesso/:siglaUnidade`              | 🏠 > [SIGLA]                              |
| `/processo/:codProcesso/:siglaUnidade/vis-cadastro` | 🏠 > [SIGLA] > Atividades e conhecimentos |

### Rotas de Unidade

Rotas que exibem informações de uma unidade específica.

| Rota                              | Nome                     | Breadcrumbs                          |
|-----------------------------------|--------------------------|--------------------------------------|
| `/unidade/:codUnidade`            | Unidade                  | 🏠 > [SIGLA] > Minha unidade         |
| `/unidade/:codUnidade/mapa`       | Mapa                     | 🏠 > [SIGLA] > Mapa de competências  |
| `/unidade/:codUnidade/atribuicao` | AtribuicaoTemporariaForm | 🏠 > [SIGLA] > Atribuição temporária |

**Fallback**: Se a sigla da unidade não estiver carregada no store, exibe "Unidade X" onde X é o código.

### Outras Rotas

Rotas que não se encaixam nas categorias acima usam a propriedade `meta.breadcrumb` definida no roteador. O breadcrumb
pode ser:

- Uma **string estática**: `meta: { breadcrumb: "Título da página" }`
- Uma **função dinâmica**: `meta: { breadcrumb: (route) => route.params.id }`

## Regras de Navegação

### Links nos Breadcrumbs

1. O **primeiro item** (🏠) sempre leva ao Painel
2. **Itens intermediários** são clicáveis e levam à respectiva página
3. O **último item** (ativo) não é clicável

### Hierarquia de Links

Para rotas de subprocesso, a hierarquia é:

```
🏠 (Painel) → Detalhes do processo (ProcessoView) → [SIGLA] (SubprocessoView) → [Página]
```

Para rotas de unidade, a hierarquia é:

```
🏠 (Painel) → [SIGLA] (UnidadeView) → [Página]
```

## Comportamento Visual

### Estilos

- Fonte: 0.85rem
- Cor dos links: `#6c757d` (cinza)
- Cor dos links hover: `#212529` (preto)
- Cor do item ativo: `#212529` (preto)
- Separador: `›` em cor `#adb5bd`

### Botão Voltar

- Altura: compatível com os breadcrumbs
- Cor: cinza discreto
- Hover: fundo cinza escuro, texto branco

## Cenários

### Cenário 1: ADMIN Visualizando Atividades

1. ADMIN navega para `/processo/99/ASSESSORIA_12/vis-cadastro`
2. Breadcrumbs exibidos: 🏠 > Detalhes do processo > ASSESSORIA_12 > Atividades e conhecimentos
3. Clicar em "ASSESSORIA_12" leva para `/processo/99/ASSESSORIA_12`
4. Clicar em "Detalhes do processo" leva para `/processo/99`

### Cenário 2: CHEFE Visualizando Atividades

1. CHEFE navega para `/processo/99/ASSESSORIA_12/vis-cadastro`
2. Breadcrumbs exibidos: 🏠 > ASSESSORIA_12 > Atividades e conhecimentos
3. "Detalhes do processo" não aparece (CHEFE só vê sua unidade)

### Cenário 3: Visualizando Unidade

1. Usuário navega para `/unidade/1`
2. Store de unidades carrega a unidade com sigla "SEDOC"
3. Breadcrumbs exibidos: 🏠 > SEDOC > Minha unidade
4. Se a unidade não estivesse carregada, mostraria: 🏠 > Unidade 1 > Minha unidade

### Cenário 4: Navegando para Atribuição Temporária

1. ADMIN navega para `/unidade/1/atribuicao`
2. Breadcrumbs exibidos: 🏠 > SEDOC > Atribuição temporária
3. Clicar em "SEDOC" leva para `/unidade/1`

## Implementação

### Arquivos Envolvidos

- `frontend/src/components/BarraNavegacao.vue` - Componente principal
- `frontend/src/router/*.routes.ts` - Definições das rotas com `meta.breadcrumb`

### Dependências

- Store de Perfil (`usePerfilStore`) - Para determinar o perfil do usuário
- Store de Unidades (`useUnidadesStore`) - Para obter a sigla da unidade

### Testes

Os testes estão em `frontend/src/components/__tests__/BarraNavegacao.spec.ts` e cobrem:

- Visibilidade em diferentes páginas (login, painel, outras)
- Renderização correta para rotas de processo, subprocesso e unidade
- Lógica de perfil (omissão de "Detalhes do processo" para CHEFE/SERVIDOR)
- Funcionalidade do botão voltar
