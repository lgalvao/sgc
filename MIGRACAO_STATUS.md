# Status da Migração para Oracle - 2025-12-11 15:20

## ✅ CONCLUÍDO - Fase 1, 2 e Fase 3 parcial

### Modelo de Dados Atualizado

- ✅ TipoUnidade: adicionado SEM_EQUIPE e RAIZ  
- ✅ Unidade → VW_UNIDADE (VIEW imutável com @Immutable)
- ✅ Usuario → VW_USUARIO (VIEW imutável)
- ✅ UsuarioPerfil → VW_USUARIO_PERFIL_UNIDADE (VIEW com chave composta)
- ✅ VinculacaoUnidade → VW_VINCULACAO_UNIDADE (VIEW)
- ✅ UnidadeMapa criada (relaciona unidade com mapa vigente)
- ✅ Administrador criada (gerencia perfis ADMIN)
- ✅ AtribuicaoTemporaria ajustada (campos usuario_titulo + usuario_matricula)
- ✅ Analise ajustada (unidade_codigo em vez de unidade_sigla)
- ✅ **Mapa** → FK para **Subprocesso** (não mais para Unidade)
- ✅ **Subprocesso** → FKs separadas para Processo e Unidade (não usa UnidadeProcesso como entidade)
- ✅ **UNIDADE_PROCESSO** mantida como tabela snapshot (sem entidade JPA)

### Schema H2 (para testes)

- ✅ Movido de `main/resources/db/` para `test/resources/db/`
- ✅ UNIDADE_PROCESSO com PK composta (tabela snapshot)
- ✅ MAPA com FK `subprocesso_codigo`
- ✅ SUBPROCESSO com FKs separadas (processo_codigo, unidade_codigo)
- ✅ Removido campo `sugestoes_apresentadas` de MAPA

### Entidades Java

- ✅ Mapa: FK para Subprocesso, sem campo sugestoesApresentadas
- ✅ MapaDto: campo sugestoesApresentadas removido
- ✅ Subprocesso: @ManyToOne separados para Processo e Unidade
- ✅ MapaService: removido uso de sugestoesApresentadas
- ✅ CopiaMapaService: removido uso de sugestoesApresentadas e setUnidade

### Lógica de Negócio

- ✅ UsuarioService carrega atribuições da VIEW
- ✅ SgrhService carrega atribuições da VIEW  
- ✅ UsuarioRepo adaptado para consultar VW_USUARIO_PERFIL_UNIDADE
- ✅ ProcessoService usa UnidadeMapaRepo
- ✅ UnidadeService usa UnidadeMapaRepo

## 🔄 EM ANDAMENTO - Fase 3

### Compilação

- ⚠️ **Backend compilando** mas com avisos

### Pendências

- ⚠️ SubprocessoService usa `subprocesso.setUnidade()` (linha 217, 219)
- ⚠️ Precisa ajustar código que espera `mapa.setUnidade()`
- ⚠️ Precisa popular UNIDADE_PROCESSO via SQL quando processo é iniciado

## 🎯 PRÓXIMOS PASSOS

### Fase 3 - Ajustes Finais

1. Corrigir SubprocessoService para não usar setUnidade() diretamente
2. Implementar lógica para popular UNIDADE_PROCESSO (snapshot) quando processo inicia
3. Atualizar data.sql para testes
4. Executar testes e corrigir falhas restantes
5. Validar com testes E2E
