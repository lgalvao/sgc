# Sprint 1: BFF e Agregação de Dados

## Objetivo

Eliminar o padrão de API chaining no frontend criando endpoints BFF (Backend for Frontend) que agregam dados de múltiplas entidades em uma única requisição.

## Problema Atual

### Exemplo: CadMapa.vue (linhas 164-178)

```typescript
onMounted(async () => {
  // 1. Buscar unidade (1ª requisição)
  await unidadesStore.buscarUnidade(siglaUnidade.value);
  
  // 2. Resolver subprocesso (2ª requisição)
  const id = await subprocessosStore.buscarSubprocessoPorProcessoEUnidade(
      codProcesso.value,
      siglaUnidade.value,
  );

  if (id) {
    // 3. Buscar dados em paralelo (3 requisições simultâneas)
    await Promise.all([
      mapasStore.buscarMapaCompleto(id),           // 3ª requisição
      subprocessosStore.buscarSubprocessoDetalhe(id), // 4ª requisição  
      atividadesStore.buscarAtividadesParaSubprocesso(id), // 5ª requisição
    ]);
  }
});
```

**Total: 5 requisições HTTP = ~800ms de latência**

### Impactos Negativos

- ❌ Latência: ~500-800ms extras (RTT médio de 100ms)
- ❌ Complexidade: Tratamento de erro em 5 pontos diferentes
- ❌ Acoplamento: Frontend precisa conhecer relações entre entidades
- ❌ Race Conditions: Risco de estado inconsistente
- ❌ Waterfall: Primeiras 2 requisições são sequenciais (bloqueantes)

## Solução

### Backend: Criar Endpoint BFF

**Localização**: `backend/src/main/java/br/jus/trf1/sgc/application/subprocesso/`

#### Novo DTO de Resposta

```java
package br.jus.trf1.sgc.application.subprocesso;

import br.jus.trf1.sgc.application.atividade.AtividadeDto;
import br.jus.trf1.sgc.application.mapa.MapaCompletoDto;
import br.jus.trf1.sgc.application.unidade.UnidadeDto;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ContextoEdicaoMapaDto {
    UnidadeDto unidade;
    SubprocessoDetalheDto subprocesso;
    MapaCompletoDto mapa;
    List<AtividadeDto> atividadesDisponiveis;
}
```

#### Novo Service

```java
package br.jus.trf1.sgc.application.subprocesso;

import br.jus.trf1.sgc.application.atividade.AtividadeMapper;
import br.jus.trf1.sgc.application.mapa.MapaMapper;
import br.jus.trf1.sgc.application.unidade.UnidadeMapper;
import br.jus.trf1.sgc.domain.atividade.AtividadeService;
import br.jus.trf1.sgc.domain.mapa.MapaService;
import br.jus.trf1.sgc.domain.subprocesso.SubprocessoService;
import br.jus.trf1.sgc.domain.unidade.UnidadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubprocessoContextoService {
    
    private final SubprocessoService subprocessoService;
    private final UnidadeService unidadeService;
    private final MapaService mapaService;
    private final AtividadeService atividadeService;
    
    @Transactional(readOnly = true)
    public ContextoEdicaoMapaDto obterContextoEdicao(Integer codSubprocesso) {
        var subprocesso = subprocessoService.buscar(codSubprocesso);
        var unidade = unidadeService.buscar(subprocesso.getUnidadeSigla());
        var mapa = mapaService.buscarPorSubprocesso(codSubprocesso).orElse(null);
        var atividades = atividadeService.listarPorSubprocesso(codSubprocesso);
        
        return ContextoEdicaoMapaDto.builder()
            .unidade(UnidadeMapper.toDto(unidade))
            .subprocesso(SubprocessoMapper.toDetalheDto(subprocesso))
            .mapa(mapa != null ? MapaMapper.toCompletoDto(mapa) : null)
            .atividadesDisponiveis(AtividadeMapper.toDtoList(atividades))
            .build();
    }
}
```

#### Novo Endpoint no Controller

```java
// Adicionar em SubprocessoController.java

@GetMapping("/{id}/contexto-edicao-mapa")
public ResponseEntity<ContextoEdicaoMapaDto> obterContextoEdicaoMapa(@PathVariable Integer id) {
    return ResponseEntity.ok(contextoService.obterContextoEdicao(id));
}
```

### Frontend: Criar Novo Service Method

**Localização**: `frontend/src/services/subprocessoService.ts`

```typescript
export async function obterContextoEdicaoMapa(codSubprocesso: number): Promise<ContextoEdicaoMapaDto> {
    const response = await apiClient.get<ContextoEdicaoMapaDto>(
        `/subprocessos/${codSubprocesso}/contexto-edicao-mapa`
    );
    return response.data;
}
```

### Frontend: Adicionar Tipo

**Localização**: `frontend/src/types/tipos.ts`

```typescript
export interface ContextoEdicaoMapaDto {
    unidade: UnidadeDto;
    subprocesso: SubprocessoDetalheDto;
    mapa: MapaCompletoDto | null;
    atividadesDisponiveis: AtividadeDto[];
}
```

### Frontend: Refatorar CadMapa.vue

**ANTES** (linhas 164-178):
```typescript
onMounted(async () => {
  await unidadesStore.buscarUnidade(siglaUnidade.value);
  const id = await subprocessosStore.buscarSubprocessoPorProcessoEUnidade(
      codProcesso.value,
      siglaUnidade.value,
  );
  if (id) {
    codSubprocesso.value = id;
    await Promise.all([
      mapasStore.buscarMapaCompleto(id),
      subprocessosStore.buscarSubprocessoDetalhe(id),
      atividadesStore.buscarAtividadesParaSubprocesso(id),
    ]);
  }
});
```

**DEPOIS**:
```typescript
onMounted(async () => {
  try {
    const id = await subprocessosStore.buscarSubprocessoPorProcessoEUnidade(
        codProcesso.value,
        siglaUnidade.value,
    );
    
    if (!id) {
      // Subprocesso não encontrado
      return;
    }
    
    codSubprocesso.value = id;
    const contexto = await subprocessoService.obterContextoEdicaoMapa(id);
    
    // Atualizar stores com dados agregados
    unidadesStore.setUnidade(contexto.unidade);
    subprocessosStore.setSubprocessoDetalhe(contexto.subprocesso);
    mapasStore.setMapaCompleto(contexto.mapa);
    atividadesStore.setAtividadesParaSubprocesso(id, contexto.atividadesDisponiveis);
    
  } catch (error) {
    console.error('Erro ao carregar contexto:', error);
  }
});
```

### Frontend: Adicionar Métodos Setter nas Stores

#### unidades.ts
```typescript
function setUnidade(unidade: UnidadeDto) {
  state.unidade = unidade;
}
```

#### subprocessos.ts
```typescript
function setSubprocessoDetalhe(subprocesso: SubprocessoDetalheDto) {
  state.subprocessoDetalhe = subprocesso;
}
```

#### mapas.ts
```typescript
function setMapaCompleto(mapa: MapaCompletoDto | null) {
  state.mapaCompleto = mapa;
}
```

#### atividades.ts
```typescript
function setAtividadesParaSubprocesso(codSubprocesso: number, atividades: AtividadeDto[]) {
  state.atividadesPorSubprocesso[codSubprocesso] = atividades;
}
```

## Eliminar useSubprocessoResolver

### Problema

O composable `useSubprocessoResolver.ts` realiza travessia recursiva O(n) em árvore de unidades no cliente.

```typescript
function buscarUnidadeNaArvore(unidades: UnidadeParticipante[], sigla: string): UnidadeParticipante | null {
    for (const u of unidades) {
        if (u.sigla === sigla) {
            return u;
        }
        if (u.filhos && u.filhos.length > 0) {
            const encontrada = buscarUnidadeNaArvore(u.filhos, sigla);
            if (encontrada) return encontrada;
        }
    }
    return null;
}
```

### Solução

**1. Identificar usos atuais**

```bash
grep -r "useSubprocessoResolver" frontend/src/views/
```

**2. Substituir por chamada direta**

```typescript
// ❌ ANTES
const { codSubprocesso } = useSubprocessoResolver(codProcesso, siglaUnidade);

// ✅ DEPOIS
const codSubprocesso = ref<number | null>(null);
onMounted(async () => {
  codSubprocesso.value = await subprocessosStore.buscarSubprocessoPorProcessoEUnidade(
    codProcesso.value, 
    siglaUnidade.value
  );
});
```

**3. Remover arquivo**

```bash
rm frontend/src/composables/useSubprocessoResolver.ts
```

## Checklist Técnica para Agente IA

### Backend

- [ ] Criar `ContextoEdicaoMapaDto.java`
- [ ] Criar `SubprocessoContextoService.java`
- [ ] Adicionar método em `SubprocessoController.java`
- [ ] Testar endpoint manualmente: `GET /api/subprocessos/{id}/contexto-edicao-mapa`
- [ ] Criar teste unitário para `SubprocessoContextoService`
- [ ] Criar teste de integração para endpoint

### Frontend - Types & Services

- [ ] Adicionar interface `ContextoEdicaoMapaDto` em `tipos.ts`
- [ ] Adicionar método `obterContextoEdicaoMapa` em `subprocessoService.ts`

### Frontend - Stores

- [ ] Adicionar `setUnidade` em `unidades.ts`
- [ ] Adicionar `setSubprocessoDetalhe` em `subprocessos.ts`
- [ ] Adicionar `setMapaCompleto` em `mapas.ts`
- [ ] Adicionar `setAtividadesParaSubprocesso` em `atividades.ts`

### Frontend - Refatoração CadMapa.vue

- [ ] Refatorar `onMounted` para usar novo endpoint
- [ ] Validar que não há regressão de funcionalidade
- [ ] Testar fluxo completo no navegador
- [ ] Medir latência ANTES (baseline)
- [ ] Medir latência DEPOIS
- [ ] Confirmar redução de requisições (5 → 2)

### Frontend - Eliminar useSubprocessoResolver

- [ ] Identificar todas as Views que usam o composable
- [ ] Substituir por chamada direta ao store
- [ ] Remover arquivo `useSubprocessoResolver.ts`
- [ ] Remover testes do composable
- [ ] Validar que não quebrou nenhuma View

### Testes

- [ ] Executar `npm run test:unit`
- [ ] Executar `npm run quality:all`
- [ ] Validar build: `npm run build`

## Critérios de Aceitação

### Funcional

✅ Tela CadMapa carrega sem erros  
✅ Todas as informações são exibidas corretamente  
✅ Modais de criação/edição de competência funcionam  
✅ Disponibilização de mapa funciona  

### Performance

✅ Redução de 5 → 2 requisições HTTP no CadMapa  
✅ Latência de carregamento < 300ms (antes ~800ms)  
✅ Não há requisições duplicadas  

### Código

✅ Arquivo `useSubprocessoResolver.ts` foi removido  
✅ Não há imports do composable removido  
✅ Testes unitários passam  
✅ ESLint/TypeScript sem erros  

## Comandos de Validação

```bash
# Backend - Testar endpoint
curl -X GET http://localhost:8080/api/subprocessos/1/contexto-edicao-mapa \
  -H "Authorization: Bearer {token}"

# Frontend - Validar build
cd frontend
npm run build

# Frontend - Executar testes
npm run test:unit

# Frontend - Quality check
npm run quality:all

# Validar que useSubprocessoResolver foi removido
grep -r "useSubprocessoResolver" frontend/src/ || echo "✅ Removido com sucesso"
```

## Métricas de Sucesso

| Métrica | Antes | Depois | Ganho |
|---------|-------|--------|-------|
| Requisições HTTP (CadMapa) | 5 | 2 | -60% |
| Latência carregamento | ~800ms | ~250ms | -69% |
| Linhas de código (onMounted) | 16 | 12 | -25% |
| Pontos de falha | 5 | 2 | -60% |
| Travessia de árvore | O(n) | O(1) | ∞ |

## Próxima Sprint

👉 **Sprint 2**: `sprint-02-composables.md` - Centralizar tratamento de erros com `useFormErrors`

---

**Estimativa**: 6-8 horas (Backend: 3h, Frontend: 3h, Testes: 2h)  
**Prioridade**: 🔴 Alta  
**Dependências**: Nenhuma