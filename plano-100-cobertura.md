# Plano para Alcançar 100% de Cobertura de Testes

**Gerado em:** 2026-03-28

## Situacao Atual

- **Cobertura Global de Linhas:** 98.91%
- **Cobertura Global de Branches:** 91.26%
- **Total de Arquivos Analisados:** 62
- **Arquivos com Cobertura < 100%:** 27
- **Arquivos com 100% de Cobertura:** 35

## Progresso por Categoria

- **CRITICO - Logica de negocio central:** 17 arquivo(s) pendente(s)
- **IMPORTANTE - API e transformacao de dados:** 3 arquivo(s) pendente(s)
- **NORMAL - Entidades e utilitarios:** 7 arquivo(s) pendente(s)

---

## CRITICO - Logica de negocio central

**Total:** 17 arquivo(s) com lacunas

### 1. `sgc.processo.service.ProcessoService`

- **Cobertura de Linhas:** 96.63% (11 linha(s) nao cobertas)
- **Cobertura de Branches:** 86.52% (24 branch(es) nao cobertos)
- **Linhas nao cobertas:** 322, 517, 529, 530, 531, 533, 534, 536, 537, 538, 540
- **Branches nao cobertos:** 319(1/2), 357(1/2), 419(1/2), 441(1/2), 477(1/4), 480(1/2), 484(1/4), 486(1/2), 487(2/2), 490(3/8), 516(1/2), 526(2/4), 529(2/2), 533(2/2), 536(2/2), 570(1/2), 577(1/6)

**Acao necessaria:** Criar ou expandir `ProcessoServiceCoverageTest.java` para cobrir todas as linhas e branches.

### 2. `sgc.subprocesso.service.SubprocessoTransicaoService`

- **Cobertura de Linhas:** 98.36% (7 linha(s) nao cobertas)
- **Cobertura de Branches:** 84.31% (16 branch(es) nao cobertos)
- **Linhas nao cobertas:** 164, 165, 262, 263, 265, 271, 707
- **Branches nao cobertos:** 77(1/2), 83(1/2), 163(1/2), 228(1/4), 261(1/2), 262(2/2), 270(1/2), 284(1/2), 362(1/2), 486(1/2), 494(1/2), 525(1/2), 569(1/2), 675(1/4), 706(1/2)

**Acao necessaria:** Criar ou expandir `SubprocessoTransicaoServiceCoverageTest.java` para cobrir todas as linhas e branches.

### 3. `sgc.subprocesso.service.SubprocessoService`

- **Cobertura de Linhas:** 99.36% (3 linha(s) nao cobertas)
- **Cobertura de Branches:** 90.91% (23 branch(es) nao cobertos)
- **Linhas nao cobertas:** 313, 763, 882
- **Branches nao cobertos:** 312(1/2), 354(1/4), 373(1/2), 402(1/2), 417(1/2), 548(1/2), 593(1/4), 631(1/4), 632(1/6), 660(1/4), 675(1/2), 685(1/2), 760(1/3), 799(1/4), 826(2/2), 834(1/2), 842(1/2), 869(1/2), 870(1/2), 872(2/2)

**Acao necessaria:** Criar ou expandir `SubprocessoServiceCoverageTest.java` para cobrir todas as linhas e branches.

### 4. `sgc.subprocesso.service.SubprocessoNotificacaoService`

- **Cobertura de Linhas:** 98.77% (1 linha(s) nao cobertas)
- **Cobertura de Branches:** 78.57% (6 branch(es) nao cobertos)
- **Linhas nao cobertas:** 157
- **Branches nao cobertos:** 46(1/2), 51(1/2), 76(1/2), 123(1/2), 156(2/4)

**Acao necessaria:** Criar ou expandir `SubprocessoNotificacaoServiceCoverageTest.java` para cobrir todas as linhas e branches.

### 5. `sgc.organizacao.service.ResponsavelUnidadeService`

- **Cobertura de Linhas:** 98.77% (1 linha(s) nao cobertas)
- **Cobertura de Branches:** 68.75% (5 branch(es) nao cobertos)
- **Linhas nao cobertas:** 158
- **Branches nao cobertos:** 69(1/2), 79(1/2), 134(1/2), 157(2/4)

**Acao necessaria:** Criar ou expandir `ResponsavelUnidadeServiceCoverageTest.java` para cobrir todas as linhas e branches.

### 6. `sgc.organizacao.service.UnidadeHierarquiaService`

- **Cobertura de Linhas:** 98.04% (2 linha(s) nao cobertas)
- **Cobertura de Branches:** 93.75% (3 branch(es) nao cobertos)
- **Linhas nao cobertas:** 166, 240
- **Branches nao cobertos:** 165(1/2), 183(1/4), 239(1/2)

**Acao necessaria:** Criar ou expandir `UnidadeHierarquiaServiceCoverageTest.java` para cobrir todas as linhas e branches.

### 7. `sgc.alerta.EmailService`

- **Cobertura de Linhas:** 94.29% (2 linha(s) nao cobertas)
- **Cobertura de Branches:** 100.00% (0 branch(es) nao cobertos)
- **Linhas nao cobertas:** 62, 63

**Acao necessaria:** Criar ou expandir `EmailServiceCoverageTest.java` para cobrir todas as linhas e branches.

### 8. `sgc.processo.painel.PainelFacade`

- **Cobertura de Linhas:** 100.00% (0 linha(s) nao cobertas)
- **Cobertura de Branches:** 92.31% (4 branch(es) nao cobertos)
- **Branches nao cobertos:** 154(2/4), 165(1/4), 190(1/4)

**Acao necessaria:** Criar ou expandir `PainelFacadeCoverageTest.java` para cobrir todas as linhas e branches.

### 9. `sgc.mapa.service.CopiaMapaService`

- **Cobertura de Linhas:** 98.82% (1 linha(s) nao cobertas)
- **Cobertura de Branches:** 96.15% (1 branch(es) nao cobertos)
- **Linhas nao cobertas:** 105
- **Branches nao cobertos:** 104(1/2)

**Acao necessaria:** Criar ou expandir `CopiaMapaServiceCoverageTest.java` para cobrir todas as linhas e branches.

### 10. `sgc.mapa.service.MapaManutencaoService`

- **Cobertura de Linhas:** 100.00% (0 linha(s) nao cobertas)
- **Cobertura de Branches:** 83.33% (3 branch(es) nao cobertos)
- **Branches nao cobertos:** 133(1/2), 150(1/2), 289(1/2)

**Acao necessaria:** Criar ou expandir `MapaManutencaoServiceCoverageTest.java` para cobrir todas as linhas e branches.

### 11. `sgc.seguranca.LoginFacade`

- **Cobertura de Linhas:** 100.00% (0 linha(s) nao cobertas)
- **Cobertura de Branches:** 90.91% (2 branch(es) nao cobertos)
- **Branches nao cobertos:** 83(1/2), 116(1/2)

**Acao necessaria:** Criar ou expandir `LoginFacadeCoverageTest.java` para cobrir todas as linhas e branches.

### 12. `sgc.alerta.AlertaFacade`

- **Cobertura de Linhas:** 100.00% (0 linha(s) nao cobertas)
- **Cobertura de Branches:** 97.50% (1 branch(es) nao cobertos)
- **Branches nao cobertos:** 45(1/2)

**Acao necessaria:** Criar ou expandir `AlertaFacadeCoverageTest.java` para cobrir todas as linhas e branches.

### 13. `sgc.mapa.service.ImpactoMapaService`

- **Cobertura de Linhas:** 100.00% (0 linha(s) nao cobertas)
- **Cobertura de Branches:** 98.15% (1 branch(es) nao cobertos)
- **Branches nao cobertos:** 81(1/2)

**Acao necessaria:** Criar ou expandir `ImpactoMapaServiceCoverageTest.java` para cobrir todas as linhas e branches.

### 14. `sgc.mapa.service.MapaSalvamentoService`

- **Cobertura de Linhas:** 100.00% (0 linha(s) nao cobertas)
- **Cobertura de Branches:** 97.22% (1 branch(es) nao cobertos)
- **Branches nao cobertos:** 148(1/2)

**Acao necessaria:** Criar ou expandir `MapaSalvamentoServiceCoverageTest.java` para cobrir todas as linhas e branches.

### 15. `sgc.organizacao.service.HierarquiaService`

- **Cobertura de Linhas:** 100.00% (0 linha(s) nao cobertas)
- **Cobertura de Branches:** 90.00% (1 branch(es) nao cobertos)
- **Branches nao cobertos:** 27(1/2)

**Acao necessaria:** Criar ou expandir `HierarquiaServiceCoverageTest.java` para cobrir todas as linhas e branches.

### 16. `sgc.organizacao.UsuarioFacade`

- **Cobertura de Linhas:** 100.00% (0 linha(s) nao cobertas)
- **Cobertura de Branches:** 93.75% (1 branch(es) nao cobertos)
- **Branches nao cobertos:** 46(1/6)

**Acao necessaria:** Criar ou expandir `UsuarioFacadeCoverageTest.java` para cobrir todas as linhas e branches.

### 17. `sgc.subprocesso.service.SubprocessoValidacaoService`

- **Cobertura de Linhas:** 100.00% (0 linha(s) nao cobertas)
- **Cobertura de Branches:** 98.21% (1 branch(es) nao cobertos)
- **Branches nao cobertos:** 104(1/4)

**Acao necessaria:** Criar ou expandir `SubprocessoValidacaoServiceCoverageTest.java` para cobrir todas as linhas e branches.


## IMPORTANTE - API e transformacao de dados

**Total:** 3 arquivo(s) com lacunas

### 1. `sgc.organizacao.UnidadeController`

- **Cobertura de Linhas:** 100.00% (0 linha(s) nao cobertas)
- **Cobertura de Branches:** 50.00% (2 branch(es) nao cobertos)
- **Branches nao cobertos:** 60(2/4)

**Acao necessaria:** Criar ou expandir `UnidadeControllerCoverageTest.java` para cobrir todas as linhas e branches.

### 2. `sgc.organizacao.UsuarioController`

- **Cobertura de Linhas:** 100.00% (0 linha(s) nao cobertas)
- **Cobertura de Branches:** 75.00% (1 branch(es) nao cobertos)
- **Branches nao cobertos:** 58(1/4)

**Acao necessaria:** Criar ou expandir `UsuarioControllerCoverageTest.java` para cobrir todas as linhas e branches.

### 3. `sgc.seguranca.login.LoginController`

- **Cobertura de Linhas:** 100.00% (0 linha(s) nao cobertas)
- **Cobertura de Branches:** 95.00% (1 branch(es) nao cobertos)
- **Branches nao cobertos:** 104(1/2)

**Acao necessaria:** Criar ou expandir `LoginControllerCoverageTest.java` para cobrir todas as linhas e branches.


## NORMAL - Entidades e utilitarios

**Total:** 7 arquivo(s) com lacunas

### 1. `sgc.comum.erros.RestExceptionHandler`

- **Cobertura de Linhas:** 92.77% (6 linha(s) nao cobertas)
- **Cobertura de Branches:** 91.67% (1 branch(es) nao cobertos)
- **Linhas nao cobertas:** 189, 190, 191, 193, 194, 195
- **Branches nao cobertos:** 53(1/4)

**Acao necessaria:** Criar ou expandir `RestExceptionHandlerCoverageTest.java` para cobrir todas as linhas e branches.

### 2. `sgc.seguranca.login.LimitadorTentativasLogin`

- **Cobertura de Linhas:** 94.83% (3 linha(s) nao cobertas)
- **Cobertura de Branches:** 84.38% (5 branch(es) nao cobertos)
- **Linhas nao cobertas:** 77, 100, 103
- **Branches nao cobertos:** 69(1/2), 74(1/2), 99(2/4), 102(1/2)

**Acao necessaria:** Criar ou expandir `LimitadorTentativasLoginCoverageTest.java` para cobrir todas as linhas e branches.

### 3. `sgc.seguranca.dto.UsuarioAcessoAd`

- **Cobertura de Linhas:** 0.00% (2 linha(s) nao cobertas)
- **Cobertura de Branches:** 100.00% (0 branch(es) nao cobertos)
- **Linhas nao cobertas:** 14, 26

**Acao necessaria:** Criar ou expandir `UsuarioAcessoAdCoverageTest.java` para cobrir todas as linhas e branches.

### 4. `sgc.seguranca.SgcPermissionEvaluator`

- **Cobertura de Linhas:** 100.00% (0 linha(s) nao cobertas)
- **Cobertura de Branches:** 95.59% (3 branch(es) nao cobertos)
- **Branches nao cobertos:** 115(1/4), 190(1/2), 203(1/2)

**Acao necessaria:** Criar ou expandir `SgcPermissionEvaluatorCoverageTest.java` para cobrir todas as linhas e branches.

### 5. `sgc.seguranca.login.ClienteAcessoAdE2e`

- **Cobertura de Linhas:** 100.00% (0 linha(s) nao cobertas)
- **Cobertura de Branches:** 75.00% (1 branch(es) nao cobertos)
- **Branches nao cobertos:** 19(1/2)

**Acao necessaria:** Criar ou expandir `ClienteAcessoAdE2eCoverageTest.java` para cobrir todas as linhas e branches.

### 6. `sgc.seguranca.login.GerenciadorJwt`

- **Cobertura de Linhas:** 100.00% (0 linha(s) nao cobertas)
- **Cobertura de Branches:** 94.44% (1 branch(es) nao cobertos)
- **Branches nao cobertos:** 34(1/4)

**Acao necessaria:** Criar ou expandir `GerenciadorJwtCoverageTest.java` para cobrir todas as linhas e branches.

### 7. `sgc.subprocesso.model.Subprocesso`

- **Cobertura de Linhas:** 100.00% (0 linha(s) nao cobertas)
- **Cobertura de Branches:** 95.45% (1 branch(es) nao cobertos)
- **Branches nao cobertos:** 70(1/2)

**Acao necessaria:** Criar ou expandir `SubprocessoCoverageTest.java` para cobrir todas as linhas e branches.

---

## Scripts Disponiveis

1. `node backend/etc/scripts/super-cobertura.cjs --run` - Gera relatorio de lacunas
2. `node backend/etc/scripts/verificar-cobertura.cjs --missed` - Lista arquivos com mais gaps
3. `node backend/etc/scripts/analisar-cobertura.cjs` - Analise detalhada com tabelas
4. `python3 backend/etc/scripts/analyze_tests.py` - Identifica arquivos sem testes
5. `python3 backend/etc/scripts/prioritize_tests.py` - Prioriza criacao de testes
