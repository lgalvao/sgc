# Plano de Refatoração: UsuarioFacade

## Objetivo
Resolver os TODOs identificados no arquivo `UsuarioFacade.java` para melhorar a organização e consistência do código.

## Problemas Identificados

### Grupo 1: Métodos que deveriam estar em UnidadeFacade
1. **`buscarResponsavelAtual(String siglaUnidade)`** (linha 97-115)
   - Busca responsável por sigla da unidade
   - Usado por: `SubprocessoFacade.java`

2. **`buscarResponsavelUnidade(Long unidadeCodigo)`** (linha 117-124)
   - Busca responsável por código da unidade
   - Usado por: `RelatorioFacade.java`

3. **`buscarResponsaveisUnidades(List<Long> unidadesCodigos)`** (linha 172-195)
   - Busca responsáveis em lote
   - Usado por: `EventoProcessoListener.java`

### Grupo 2: Métodos que usam String ao invés de enum Perfil
1. **`usuarioTemPerfil(String titulo, String perfil, Long unidadeCodigo)`** (linha 216-228)
2. **`buscarUnidadesPorPerfil(String titulo, String perfil)`** (linha 230-243)

## Estratégia de Refatoração

### Fase 1: Mover métodos de responsáveis para UnidadeFacade

**Ação:** Mover os 3 métodos de responsáveis para `UnidadeFacade.java`, já que:
- Eles são operações sobre **unidades** (quem é o responsável de uma unidade)
- `UnidadeFacade` já tem dependência de `UsuarioRepo`
- Faz sentido semântico que perguntas sobre "responsável da unidade X" estejam em `UnidadeFacade`

**Passos:**
1. Adicionar métodos em `UnidadeFacade`
2. Adicionar imports e dependências necessárias (`UsuarioPerfilRepo`, `ResponsavelDto`)
3. Atualizar os consumidores para usar `unidadeService` ao invés de `usuarioService`
4. Remover métodos de `UsuarioFacade`

### Fase 2: Usar enum Perfil ao invés de String

**Ação:** Alterar a assinatura dos métodos para usar `Perfil perfil` ao invés de `String perfil`

**Passos:**
1. Alterar assinatura em `UsuarioFacade`
2. Atualizar chamadores (testes e código de produção)

## Arquivos Afetados

### Produção
- `UsuarioFacade.java` - remover métodos
- `UnidadeFacade.java` - adicionar métodos
- `SubprocessoFacade.java` - substituir `usuarioService.buscarResponsavelAtual` → `unidadeService.buscarResponsavelAtual`
- `RelatorioFacade.java` - substituir `usuarioService.buscarResponsavelUnidade` → `unidadeService.buscarResponsavelUnidade`
- `EventoProcessoListener.java` - substituir `usuarioService.buscarResponsaveisUnidades` → `unidadeService.buscarResponsaveisUnidades`

### Testes
- `UsuarioServiceTest.java`
- `UsuarioServiceUnitTest.java`
- `SubprocessoFacadeTest2.java`
- `RelatorioFacadeTest.java`
- `EventoProcessoListenerTest.java`
- `EventoProcessoListenerCoverageTest.java`
- `CDU21IntegrationTest.java`
- `CDU07IntegrationTest.java`

## Status
- [x] Planejamento
- [ ] Fase 1: Mover métodos de responsáveis
- [ ] Fase 2: Usar enum Perfil
- [ ] Verificação de compilação
- [ ] Execução de testes
