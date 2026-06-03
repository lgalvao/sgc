package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.springframework.data.domain.*;
import sgc.comum.erros.*;
import sgc.fixture.*;
import sgc.organizacao.model.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.SubprocessoValidacaoService.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static sgc.processo.model.SituacaoProcesso.*;
import static sgc.processo.model.TipoProcesso.*;
import static sgc.seguranca.AcaoPermissao.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@DisplayName("ProcessoService Consulta Test suite")
class ProcessoServiceConsultaTest extends ProcessoServiceTestBase {

    @Nested
    @DisplayName("Detalhes e Elegibilidade")
    class DetalhesEElegibilidade {
        @Test
        @DisplayName("Deve obter detalhes completos do processo")
        void deveObterDetalhesCompletos() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);

            Processo p = new Processo();
            p.setCodigo(codProcesso);
            p.setDescricao("Processo");
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

            Unidade u = criarUnidadeValida(10L);
            p.adicionarParticipantes(Set.of(u));

            when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);
            when(permissionEvaluator.verificarPermissao(usuario, p, AcaoPermissao.FINALIZAR_PROCESSO)).thenReturn(true);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setUnidade(u);
            sp.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), u));
            when(validacaoService.validarSubprocessosParaFinalizacao(codProcesso)).thenReturn(ResultadoValidacao.ofValido());

            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
            ProcessoDetalheDto result = processoService.obterDetalhesCompleto(codProcesso, false);

            assertThat(result).isNotNull();
            assertThat(result.getCodigo()).isEqualTo(codProcesso);
            assertThat(result.getUnidades()).isNotEmpty();
        }

        @Test
        @DisplayName("Deve testar elegibilidade de disponibilização em bloco para múltiplas situações")
        void deveTestarElegibilidadeDisponibilizacaoBloco() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setUnidadeAtivaCodigo(10L);
            usuario.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            // Lacuna A: REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_AJUSTADO, REVISAO_CADASTRO_HOMOLOGADA
            Subprocesso s1 = new Subprocesso();
            s1.setCodigo(101L);
            s1.setSituacao(REVISAO_MAPA_COM_SUGESTOES);
            Unidade u1 = criarUnidadeValida(10L);
            s1.setUnidade(u1);

            Subprocesso s2 = new Subprocesso();
            s2.setCodigo(102L);
            s2.setSituacao(REVISAO_MAPA_AJUSTADO);
            Unidade u2 = criarUnidadeValida(20L);
            s2.setUnidade(u2);

            Subprocesso s3 = new Subprocesso();
            s3.setCodigo(103L);
            s3.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);
            Unidade u3 = criarUnidadeValida(30L);
            s3.setUnidade(u3);

            Processo p = new Processo();
            p.setCodigo(codProcesso);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            p.setTipo(MAPEAMENTO);
            p.adicionarParticipantes(Set.of(u1, u2, u3));
            when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);
            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(s1, s2, s3));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(
                    s1.getCodigo(), u1,
                    s2.getCodigo(), u1,
                    s3.getCodigo(), u1
            ));

            ProcessoDetalheDto result = processoService.obterDetalhesCompleto(codProcesso, true);

            assertThat(result.getElegiveis()).extracting(SubprocessoElegivelDto::isHabilitarDisponibilizarMapaBloco)
                    .containsExactly(true, true, true);
        }

        @Test
        @DisplayName("Deve retornar null na última data limite quando ambas forem nulas")
        void deveRetornarNullNaUltimaDataLimiteQuandoAmbasNulas() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setUnidadeAtivaCodigo(10L);
            usuario.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Unidade u = criarUnidadeValida(10L);
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setUnidade(u);
            sp.setSituacao(MAPEAMENTO_MAPA_CRIADO); // Garantir elegibilidade
            sp.setDataLimiteEtapa1(null); // Lacuna linha 991 (Case 1: A=null, B=null)
            sp.setDataLimiteEtapa2(null);

            Processo p = new Processo();
            p.setCodigo(codProcesso);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            p.setTipo(MAPEAMENTO);
            p.adicionarParticipantes(Set.of(u));
            when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);
            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), u));

            ProcessoDetalheDto result = processoService.obterDetalhesCompleto(codProcesso, true);

            assertThat(result.getElegiveis().get(0).getUltimaDataLimite()).isNull();
        }

        @Test
        @DisplayName("Deve retornar data limite da etapa 2 quando for a única preenchida")
        void deveRetornarDataLimiteEtapa2QuandoUnicaPreenchida() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setUnidadeAtivaCodigo(10L);
            usuario.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Unidade u = criarUnidadeValida(10L);
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setUnidade(u);
            sp.setSituacao(MAPEAMENTO_MAPA_CRIADO);
            sp.setDataLimiteEtapa1(null);
            LocalDateTime data2 = LocalDateTime.now();
            sp.setDataLimiteEtapa2(data2); // Lacuna linha 991 (Case 2: A=null, B!=null)

            Processo p = new Processo();
            p.setCodigo(codProcesso);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            p.setTipo(MAPEAMENTO);
            p.adicionarParticipantes(Set.of(u));
            when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);
            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), u));

            ProcessoDetalheDto result = processoService.obterDetalhesCompleto(codProcesso, true);

            assertThat(result.getElegiveis().get(0).getUltimaDataLimite()).isEqualTo(data2);
        }

        @Test
        @DisplayName("Deve cobrir branch de subprocesso sem processo na permissão de escrita")
        void deveCobrirSubprocessoSemProcessoNaPermissao() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setUnidadeAtivaCodigo(10L);
            usuario.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Unidade u = criarUnidadeValida(10L);
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setUnidade(u);
            sp.setSituacao(MAPEAMENTO_MAPA_CRIADO);
            sp.setProcesso(null); // Lacuna linha 807 - processo == null

            Processo p = new Processo();
            p.setCodigo(codProcesso);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            p.setTipo(MAPEAMENTO);
            p.adicionarParticipantes(Set.of(u));
            when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);
            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), u));

            ProcessoDetalheDto result = processoService.obterDetalhesCompleto(codProcesso, true);

            assertThat(result.getElegiveis().get(0).isHabilitarDisponibilizarMapaBloco()).isTrue();
        }

        @Test
        @DisplayName("Deve desabilitar ações em bloco quando processo está finalizado")
        void deveDesabilitarAcoesBlocoQuandoProcessoFinalizado() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setUnidadeAtivaCodigo(10L);
            usuario.setPerfilAtivo(Perfil.CHEFE);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Unidade u = criarUnidadeValida(10L);
            Processo p = new Processo();
            p.setCodigo(codProcesso);
            p.setSituacao(SituacaoProcesso.FINALIZADO); // Lacuna C e B
            p.setTipo(MAPEAMENTO);
            p.adicionarParticipantes(Set.of(u));
            when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(MAPEAMENTO_MAPA_CRIADO);
            sp.setUnidade(u);
            sp.setProcesso(p);

            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), u));

            ProcessoDetalheDto result = processoService.obterDetalhesCompleto(codProcesso, true);

            // Lacuna C: todas as ações devem estar desabilitadas pois o processo está FINALIZADO
            assertThat(result.getAcoesBloco()).allSatisfy(acao -> assertThat(acao.isHabilitar()).isFalse());
        }

        @Test
        @DisplayName("Deve desabilitar ação em bloco quando perfil não possui permissão")
        void deveDesabilitarAcaoBlocoQuandoPerfilNaoPermite() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setPerfilAtivo(Perfil.SERVIDOR); // Perfil que geralmente não homologa
            usuario.setUnidadeAtivaCodigo(10L);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Unidade u = criarUnidadeValida(10L);
            Processo p = new Processo();
            p.setCodigo(codProcesso);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            p.setTipo(MAPEAMENTO);
            p.adicionarParticipantes(Set.of(u));
            when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(MAPEAMENTO_MAPA_VALIDADO);
            sp.setUnidade(u);
            sp.setProcesso(p);

            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), u));

            ProcessoDetalheDto result = processoService.obterDetalhesCompleto(codProcesso, true);

            // Homologação deve estar desabilitada para SERVIDOR (Lacuna linha 807 - permitePerfil = false)
            assertThat(result.getAcoesBloco()).filteredOn(a -> a.getCodigo().contains("homologar"))
                    .allSatisfy(acao -> assertThat(acao.isHabilitar()).isFalse());
        }

        @Test
        @DisplayName("Deve desabilitar ação em bloco quando não há unidades elegíveis")
        void deveDesabilitarAcaoBlocoQuandoNaoHaUnidades() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Unidade u = criarUnidadeValida(10L);
            Processo p = new Processo();
            p.setCodigo(codProcesso);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            p.setTipo(MAPEAMENTO);
            p.adicionarParticipantes(Set.of(u));
            when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);

            // Subprocesso não elegível para nenhuma ação em bloco
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
            sp.setUnidade(u);
            sp.setProcesso(p);

            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), u));

            ProcessoDetalheDto result = processoService.obterDetalhesCompleto(codProcesso, true);

            assertThat(result.getAcoesBloco()).allSatisfy(acao -> assertThat(acao.isHabilitar()).isFalse());
        }

        @Test
        @DisplayName("Deve listar subprocessos elegíveis para ação em bloco")
        void deveListarSubprocessosElegiveis() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setUnidadeAtivaCodigo(10L);
            usuario.setPerfilAtivo(Perfil.CHEFE);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Subprocesso s1 = new Subprocesso();
            s1.setCodigo(101L);
            s1.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            Unidade u1 = new Unidade();
            u1.setCodigo(10L);
            s1.setUnidade(u1);

            Subprocesso s2 = new Subprocesso();
            s2.setCodigo(102L);
            s2.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            Unidade u2 = new Unidade();
            u2.setCodigo(20L);
            s2.setUnidade(u2);

            Subprocesso s3 = new Subprocesso();
            s3.setCodigo(103L);
            s3.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO); // Não elegível
            Unidade u3 = new Unidade();
            u3.setCodigo(30L);
            s3.setUnidade(u3);

            when(consultaService.listarEntidadesPorProcessoEUnidades(eq(codProcesso), anyList())).thenReturn(List.of(s1, s2, s3));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(
                    s1.getCodigo(), u1,
                    s2.getCodigo(), u1
            ));
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, s1, AcaoPermissao.ACEITAR_CADASTRO)).thenReturn(true);
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, s1, AcaoPermissao.HOMOLOGAR_CADASTRO)).thenReturn(false);
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, s2, AcaoPermissao.ACEITAR_MAPA)).thenReturn(true);
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, s2, AcaoPermissao.HOMOLOGAR_MAPA)).thenReturn(false);

            List<SubprocessoElegivelDto> result = processoService.listarSubprocessosElegiveis(codProcesso);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Deve montar hierarquia no DTO corretamente para GESTOR")
        void deveMontarHierarquiaDtoGestor() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setUnidadeAtivaCodigo(10L); // Pai
            usuario.setPerfilAtivo(Perfil.GESTOR);

            Processo p = new Processo();
            p.setCodigo(codProcesso);
            p.setDescricao("Processo");
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

            Unidade uPai = criarUnidadeValida(10L);
            uPai.setSigla("PAI");
            p.adicionarParticipantes(Set.of(uPai));

            Unidade uFilho = criarUnidadeValida(20L);
            uFilho.setSigla("FILHO");
            uFilho.setUnidadeSuperior(uPai);
            p.adicionarParticipantes(Set.of(uFilho));

            Unidade uSemSub = criarUnidadeValida(30L);
            uSemSub.setSigla("SEMSUB");
            p.adicionarParticipantes(Set.of(uSemSub));

            when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);
            when(unidadeHierarquiaService.buscarIdsDescendentes(10L)).thenReturn(List.of(20L));

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setUnidade(uPai);
            sp.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            // Filho não tem subprocesso para cobrir branch sp != null

            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), uPai));

            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
            ProcessoDetalheDto result = processoService.obterDetalhesCompleto(codProcesso, false);

            assertThat(result.getUnidades()).isNotEmpty();
            assertThat(result.getUnidades().getFirst().getFilhos()).isNotEmpty();
        }

        @Test
        @DisplayName("isElegivelParaAcaoEmBloco deve retornar false quando elegivelMapa mas sem permissao ACEITAR ou HOMOLOGAR")
        void isElegivelParaAcaoEmBloco_DeveRetornarFalseQuandoElegivelMapaSemPermissoes() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            sp.setUnidade(new Unidade());

            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, sp, ACEITAR_MAPA)).thenReturn(false);
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, sp, HOMOLOGAR_MAPA)).thenReturn(false);

            List<SubprocessoElegivelDto> result = processoService.listarSubprocessosElegiveis(codProcesso);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("isElegivelParaAcaoEmBloco deve retornar false quando elegivelDisponibilizacao mas sem permissao DISPONIBILIZAR_MAPA")
        void isElegivelParaAcaoEmBloco_DeveRetornarFalseQuandoElegivelDisponibilizacaoSemPermissao() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            sp.setUnidade(new Unidade());

            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, sp, DISPONIBILIZAR_MAPA)).thenReturn(false);

            List<SubprocessoElegivelDto> result = processoService.listarSubprocessosElegiveis(codProcesso);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Consultas e Detalhes")
    class Consultas {
        @Test
        @DisplayName("Deve listar para importacao")
        void deveListarParaImportacao() {
            Processo p = new Processo();
            when(processoRepo.listarPorSituacaoComParticipantes(SituacaoProcesso.FINALIZADO)).thenReturn(List.of(p));

            List<Processo> res = processoService.listarParaImportacao();
            assertThat(res).containsExactly(p);
            verify(processoRepo).listarPorSituacaoComParticipantes(SituacaoProcesso.FINALIZADO);
        }

        @Test
        @DisplayName("Deve listar ativos para ADMIN")
        void deveListarAtivosParaAdmin() {
            Usuario admin = new Usuario();
            admin.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(admin);

            Processo p = new Processo();
            when(processoRepo.listarPorSituacao(SituacaoProcesso.EM_ANDAMENTO)).thenReturn(List.of(p));

            List<Processo> res = processoService.listarAtivos();
            assertThat(res).containsExactly(p);
            verify(processoRepo).listarPorSituacao(SituacaoProcesso.EM_ANDAMENTO);
            verify(processoRepo, never()).listarPorSituacaoEUnidadeCodigos(any(), any());
        }

        @Test
        @DisplayName("Deve listar ativos para usuario normal")
        void deveListarAtivosParaUsuarioNormal() {
            Usuario gestor = new Usuario();
            gestor.setPerfilAtivo(Perfil.GESTOR);
            Unidade u = new Unidade();
            u.setCodigo(1L);
            gestor.setUnidadeAtivaCodigo(1L);
            when(usuarioService.usuarioAutenticado()).thenReturn(gestor);
            when(unidadeHierarquiaService.buscarIdsDescendentes(1L)).thenReturn(List.of());

            Processo p = new Processo();
            when(processoRepo.listarPorSituacaoEUnidadeCodigos(eq(SituacaoProcesso.EM_ANDAMENTO), anyList())).thenReturn(List.of(p));

            List<Processo> res = processoService.listarAtivos();
            assertThat(res).containsExactly(p);
            verify(processoRepo).listarPorSituacaoEUnidadeCodigos(eq(SituacaoProcesso.EM_ANDAMENTO), anyList());
            verify(processoRepo, never()).listarPorSituacao(any());
        }

        @Test
        @DisplayName("Deve listar iniciados por participantes")
        void deveListarIniciadosPorParticipantes() {
            Pageable pageable = Pageable.unpaged();
            Processo p = new Processo();
            p.setCodigo(5L);
            when(processoRepo.listarCodigosPorParticipantesESituacaoDiferente(anyList(), eq(SituacaoProcesso.CRIADO), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(5L), pageable, 1));
            when(processoRepo.listarPorCodigosComParticipantes(List.of(5L))).thenReturn(List.of(p));

            Page<Processo> res = processoService.listarIniciadosPorParticipantes(List.of(1L), pageable);
            assertThat(res.getContent()).containsExactly(p);
            verify(processoRepo).listarCodigosPorParticipantesESituacaoDiferente(List.of(1L), SituacaoProcesso.CRIADO, pageable);
        }

        @Test
        @DisplayName("Deve listar iniciados por subprocessos")
        void deveListarIniciadosPorSubprocessos() {
            Pageable pageable = Pageable.unpaged();
            Processo p = new Processo();
            p.setCodigo(10L);
            when(configuracaoService.buscarDiasInativacaoProcesso()).thenReturn(10);
            when(processoRepo.listarCodigosAtivosPorSubprocessos(eq(List.of(1L)), eq(SituacaoProcesso.CRIADO), eq(SituacaoProcesso.FINALIZADO), any(), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(10L), pageable, 1));
            when(processoRepo.listarPorCodigosComParticipantes(List.of(10L))).thenReturn(List.of(p));

            Page<Processo> res = processoService.listarIniciadosPorSubprocessos(List.of(1L), pageable);

            assertThat(res.getContent()).containsExactly(p);
            verify(processoRepo).listarCodigosAtivosPorSubprocessos(eq(List.of(1L)), eq(SituacaoProcesso.CRIADO), eq(SituacaoProcesso.FINALIZADO), any(), eq(pageable));
        }

        @Test
        @DisplayName("Deve listar unidades bloqueadas por tipo")
        void deveListarUnidadesBloqueadasPorTipo() {
            when(processoRepo.listarUnidadesBloqueadasPorSituacaoETipo(SituacaoProcesso.EM_ANDAMENTO, TipoProcesso.MAPEAMENTO))
                    .thenReturn(List.of(1L, 2L));

            List<Long> res = processoService.listarUnidadesBloqueadasPorTipo(TipoProcesso.MAPEAMENTO);
            assertThat(res).containsExactly(1L, 2L);
            verify(processoRepo).listarUnidadesBloqueadasPorSituacaoETipo(SituacaoProcesso.EM_ANDAMENTO, TipoProcesso.MAPEAMENTO);
        }

        @Test
        @DisplayName("Deve buscar entidade por ID")
        void deveBuscarEntidadePorId() {
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            when(repo.buscar(Processo.class, id)).thenReturn(processo);

            Processo res = processoService.buscarPorCodigo(id);
            assertThat(res).isEqualTo(processo);
        }

        @Test
        @DisplayName("Deve obter processo por ID (Optional)")
        void deveobterPorCodigoOptional() {
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            when(processoRepo.buscarPorCodigoComParticipantes(id)).thenReturn(Optional.of(processo));

            Optional<Processo> res = processoService.buscarOpt(id);
            assertThat(res).isPresent();
        }

        @Test
        @DisplayName("Deve listar todos com paginação")
        void deveListarTodosPaginado() {
            Pageable pageable = Pageable.unpaged();
            when(configuracaoService.buscarDiasInativacaoProcesso()).thenReturn(10);
            when(processoRepo.listarCodigosAtivos(eq(SituacaoProcesso.FINALIZADO), any(), eq(pageable))).thenReturn(Page.empty());

            var res = processoService.listarTodos(pageable);
            assertThat(res).isEmpty();
        }
    }

    @Nested
    @DisplayName("Gaps de Data Limite no DTO")
    class DataLimiteGaps {

        @Test
        @DisplayName("obterDetalhesCompleto deve priorizar dataLimite2 quando etapa 1 é posterior à etapa 2")
        void etapa1PosteriorEtapa2() {
            Long cod = 1L;
            Processo p = new Processo();
            p.setCodigo(cod);
            p.setTipo(MAPEAMENTO);
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.ADMIN);
            u.setUnidadeAtivaCodigo(10L);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            Unidade uni = new Unidade();
            uni.setCodigo(10L);
            uni.setNome("Unidade 10");
            uni.setSigla("U10");
            sp.setUnidade(uni);
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            sp.setDataLimiteEtapa1(now.plusDays(2));
            sp.setDataLimiteEtapa2(now.plusDays(1));

            when(repo.buscar(Processo.class, cod)).thenReturn(p);
            when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), uni));
            when(permissionEvaluator.verificarPermissao(u, p, sgc.seguranca.AcaoPermissao.FINALIZAR_PROCESSO)).thenReturn(true);
            when(validacaoService.validarSubprocessosParaFinalizacao(cod)).thenReturn(ResultadoValidacao.ofValido());

            when(usuarioService.usuarioAutenticado()).thenReturn(u);
            ProcessoDetalheDto res = processoService.obterDetalhesCompleto(cod, true);
            assertThat(res.getElegiveis()).isNotEmpty();
            assertThat(res.getElegiveis().getFirst().getUltimaDataLimite()).isEqualTo(sp.getDataLimiteEtapa2());
        }

        @Test
        @DisplayName("obterDetalhesCompleto deve retornar dataLimite2 quando válida via elegíveis")
        void etapa2Valida() {
            Long cod = 1L;
            Processo p = new Processo();
            p.setCodigo(cod);
            p.setTipo(MAPEAMENTO);
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.ADMIN);
            u.setUnidadeAtivaCodigo(10L);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            Unidade uni = new Unidade();
            uni.setCodigo(10L);
            uni.setNome("Unidade 10");
            uni.setSigla("U10");
            sp.setUnidade(uni);
            java.time.LocalDateTime d1 = java.time.LocalDateTime.now().plusDays(1);
            java.time.LocalDateTime d2 = java.time.LocalDateTime.now().plusDays(2);
            sp.setDataLimiteEtapa1(d1);
            sp.setDataLimiteEtapa2(d2);

            when(repo.buscar(Processo.class, cod)).thenReturn(p);
            when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), uni));
            when(permissionEvaluator.verificarPermissao(u, p, sgc.seguranca.AcaoPermissao.FINALIZAR_PROCESSO)).thenReturn(true);
            when(validacaoService.validarSubprocessosParaFinalizacao(cod)).thenReturn(ResultadoValidacao.ofValido());

            when(usuarioService.usuarioAutenticado()).thenReturn(u);
            ProcessoDetalheDto res = processoService.obterDetalhesCompleto(cod, true);
            assertThat(res.getElegiveis()).isNotEmpty();
            assertThat(res.getElegiveis().getFirst().getUltimaDataLimite()).isEqualTo(d2);
        }

        @Test
        @DisplayName("obterDetalhesCompleto deve tratar subprocesso sem mapa")
        void subprocessoSemMapa() {
            Long cod = 1L;
            Processo p = new Processo();
            p.setCodigo(cod);
            p.setTipo(MAPEAMENTO);
            Usuario u = new Usuario();
            u.setPerfilAtivo(Perfil.ADMIN);
            u.setUnidadeAtivaCodigo(10L);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            Unidade uni = new Unidade();
            uni.setCodigo(10L);
            uni.setSigla("U10");
            uni.setNome("Unidade 10");
            uni.setTipo(TipoUnidade.OPERACIONAL);
            uni.setSituacao(SituacaoUnidade.ATIVA);
            sp.setUnidade(uni);
            sp.setDataLimiteEtapa1(java.time.LocalDateTime.now());
            sp.setMapa(null);

            p.adicionarParticipantes(Set.of(uni));

            when(repo.buscar(Processo.class, cod)).thenReturn(p);
            when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), uni));
            when(permissionEvaluator.verificarPermissao(any(Usuario.class), any(Processo.class), any(AcaoPermissao.class))).thenReturn(true);
            when(validacaoService.validarSubprocessosParaFinalizacao(cod)).thenReturn(ResultadoValidacao.ofValido());

            when(usuarioService.usuarioAutenticado()).thenReturn(u);
            ProcessoDetalheDto res = processoService.obterDetalhesCompleto(cod, false);
            assertThat(res.getUnidades()).hasSize(1);
            assertThat(res.getUnidades().getFirst().getMapaCodigo()).isNull();
        }
    }

    @Test
    @DisplayName("obterDetalhesCompleto deve reutilizar subprocessos ja carregados ao listar elegiveis")
    void deveReutilizarSubprocessosAoListarElegiveisNoContextoCompleto() {
        Long cod = 1L;
        Processo processo = new Processo();
        processo.setCodigo(cod);
        processo.setTipo(MAPEAMENTO);
        processo.setSituacao(EM_ANDAMENTO);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("U10");
        unidade.setNome("Unidade 10");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        processo.adicionarParticipantes(Set.of(unidade));

        Usuario usuario = new Usuario();
        usuario.setPerfilAtivo(Perfil.ADMIN);
        usuario.setUnidadeAtivaCodigo(10L);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(1));

        when(repo.buscar(Processo.class, cod)).thenReturn(processo);
        when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(subprocesso));
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(subprocesso.getCodigo(), unidade));
        when(permissionEvaluator.verificarPermissao(usuario, processo, FINALIZAR_PROCESSO)).thenReturn(false);

        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        ProcessoDetalheDto resultado = processoService.obterDetalhesCompleto(cod, true);

        assertThat(resultado.getElegiveis()).hasSize(1);
        verify(consultaService, times(1)).listarEntidadesPorProcesso(cod);
        verify(localizacaoSubprocessoService, times(1)).obterLocalizacoesAtuais(anyCollection());
        verify(permissionEvaluator, never()).verificarPermissaoSilenciosa(eq(usuario), any(Subprocesso.class), any(AcaoPermissao.class));
    }

    @Test
    @DisplayName("listarIniciadosPorParticipantes - deve retornar pagina normal")
    void listarIniciadosPorParticipantes_Sucesso() {
        when(processoRepo.listarCodigosPorParticipantesESituacaoDiferente(anyList(), eq(SituacaoProcesso.CRIADO), any()))
                .thenReturn(new PageImpl<>(List.of(1L)));
        Processo p = new Processo();
        p.setCodigo(1L);
        when(processoRepo.listarPorCodigosComParticipantes(anyList())).thenReturn(List.of(p));

        Page<Processo> result = processoService.listarIniciadosPorParticipantes(List.of(10L), PageRequest.of(0, 10));
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("obterDetalhesCompleto deve lançar erro quando participante tiver nome vazio")
    void obterDetalhesCompletoDeveLancarErroQuandoParticipanteTiverNomeVazio() {
        Long codProcesso = 1L;
        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setTipo(MAPEAMENTO);

        Unidade unidade = criarUnidadeValida(10L);
        unidade.setNome(" ");
        processo.adicionarParticipantes(Set.of(unidade));

        Usuario usuario = new Usuario();
        usuario.setPerfilAtivo(Perfil.ADMIN);
        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        when(repo.buscar(Processo.class, codProcesso)).thenReturn(processo);
        when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of());

        assertThatThrownBy(() -> processoService.obterDetalhesCompleto(codProcesso, false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Snapshot inconsistente");
    }

    @Test
    @DisplayName("obterDetalhesCompleto deve lançar erro quando participante tiver sigla vazia")
    void obterDetalhesCompletoDeveLancarErroQuandoParticipanteTiverSiglaVazia() {
        Long codProcesso = 1L;
        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setTipo(MAPEAMENTO);

        Unidade unidade = criarUnidadeValida(10L);
        unidade.setSigla("");
        processo.adicionarParticipantes(Set.of(unidade));

        Usuario usuario = new Usuario();
        usuario.setPerfilAtivo(Perfil.ADMIN);
        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        when(repo.buscar(Processo.class, codProcesso)).thenReturn(processo);
        when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of());

        assertThatThrownBy(() -> processoService.obterDetalhesCompleto(codProcesso, false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Snapshot inconsistente");
    }

    @Test
    @DisplayName("buscarPorCodigoComParticipantes deve lançar erro quando processo não existe")
    void buscarPorCodigoComParticipantesDeveLancarErroQuandoProcessoNaoExiste() {
        when(processoRepo.buscarPorCodigoComParticipantes(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> processoService.buscarPorCodigoComParticipantes(99L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Processo");
    }

    @Test
    @DisplayName("podeDisponibilizarEmBloco deve exercitar todas as combinações de situações de subprocesso")
    void podeDisponibilizarEmBloco_DeveExercitarTodasAsCombinacoesDeSituacoes() {
        Long codProcesso = 1L;
        
        Usuario usuario = new Usuario();
        usuario.setPerfilAtivo(Perfil.GESTOR);
        usuario.setUnidadeAtivaCodigo(10L);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setUnidade(unidade);
        sp.setSituacaoForcada(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);

        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        when(consultaService.listarEntidadesPorProcessoEUnidades(eq(codProcesso), any())).thenReturn(List.of(sp));
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(any())).thenReturn(Map.of(sp.getCodigo(), unidade));
        when(permissionEvaluator.verificarPermissaoSilenciosa(any(), any(), any())).thenReturn(true);

        List<SubprocessoElegivelDto> resultado = processoService.listarSubprocessosElegiveis(codProcesso);
        assertThat(resultado).isNotEmpty();

        sp.setSituacaoForcada(sgc.subprocesso.model.SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        List<SubprocessoElegivelDto> resultado2 = processoService.listarSubprocessosElegiveis(codProcesso);
        assertThat(resultado2).isEmpty();
    }

    @Test
    @DisplayName("podeDisponibilizarEmBloco deve exercitar todas as situações elegíveis e isSituacaoCadastro")
    void podeDisponibilizarEmBloco_DeveExercitarTodasAsSituacoesElegiveisEIsSituacaoCadastro() {
        Long codProcesso = 1L;
        Usuario usuario = new Usuario();
        usuario.setPerfilAtivo(Perfil.GESTOR);
        usuario.setUnidadeAtivaCodigo(10L);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setUnidade(unidade);

        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        when(consultaService.listarEntidadesPorProcessoEUnidades(eq(codProcesso), any())).thenReturn(List.of(sp));
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(any())).thenReturn(Map.of(sp.getCodigo(), unidade));
        when(permissionEvaluator.verificarPermissaoSilenciosa(any(), any(), any())).thenReturn(true);

        // Cenário 1: MAPEAMENTO_MAPA_COM_SUGESTOES
        sp.setSituacaoForcada(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES);
        List<SubprocessoElegivelDto> res1 = processoService.listarSubprocessosElegiveis(codProcesso);
        assertThat(res1).isNotEmpty();

        // Cenário 2: REVISAO_MAPA_COM_SUGESTOES
        sp.setSituacaoForcada(sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES);
        List<SubprocessoElegivelDto> res2 = processoService.listarSubprocessosElegiveis(codProcesso);
        assertThat(res2).isNotEmpty();

        // Cenário 3: REVISAO_MAPA_AJUSTADO
        sp.setSituacaoForcada(sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        List<SubprocessoElegivelDto> res3 = processoService.listarSubprocessosElegiveis(codProcesso);
        assertThat(res3).isNotEmpty();

        // Cenário 4: REVISAO_CADASTRO_HOMOLOGADA
        sp.setSituacaoForcada(sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        List<SubprocessoElegivelDto> res4 = allocSubprocessosElegiveisMock(codProcesso, sp, unidade);
        assertThat(res4).isNotEmpty();

        // Cenários para isSituacaoCadastro
        // REVISAO_CADASTRO_DISPONIBILIZADA
        sp.setSituacaoForcada(sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        List<SubprocessoElegivelDto> res5 = allocSubprocessosElegiveisMock(codProcesso, sp, unidade);
        assertThat(res5).isNotEmpty();

        // MAPEAMENTO_CADASTRO_DISPONIBILIZADO
        sp.setSituacaoForcada(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        List<SubprocessoElegivelDto> res6 = allocSubprocessosElegiveisMock(codProcesso, sp, unidade);
        assertThat(res6).isNotEmpty();
    }

    private List<SubprocessoElegivelDto> allocSubprocessosElegiveisMock(Long codProcesso, Subprocesso sp, Unidade unidade) {
        return processoService.listarSubprocessosElegiveis(codProcesso);
    }

    @Test
    @DisplayName("Deve avaliar elegibilidade com processo finalizado e perfil invalido em bloco")
    void deveAvaliarElegibilidadeComProcessoFinalizadoEPerfilInvalidoEmBloco() {
        Long codProcesso = 1L;
        Usuario usuario = new Usuario();
        usuario.setPerfilAtivo(Perfil.SERVIDOR);
        usuario.setUnidadeAtivaCodigo(10L);

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processo.setDescricao("Processo Finalizado");

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setUnidade(unidade);
        sp.setProcesso(processo);
        sp.setSituacaoForcada(sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);

        when(repo.buscar(Processo.class, codProcesso)).thenReturn(processo);
        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), unidade));

        // Com incluirElegiveis = true, acionará a precarga e exercitará a linha 807
        ProcessoDetalheDto resultado = processoService.obterDetalhesCompleto(codProcesso, true);
        assertThat(resultado).isNotNull();
        // O subprocesso deve ser considerado inelegível e a lista getElegiveis() deve estar vazia (cobertura linha 807)
        assertThat(resultado.getElegiveis()).isEmpty();
    }

    @Test
    @DisplayName("buscarDetalhesProcesso deve cobrir acoes de bloco desabilitadas e processo finalizado")
    void deveCobrirAcoesDeBlocoDesabilitadasEProcessoFinalizado() {
        Long codProcesso = 1L;
        Usuario usuario = new Usuario();
        usuario.setPerfilAtivo(Perfil.SERVIDOR); // Perfil sem autorizacao em bloco
        usuario.setUnidadeAtivaCodigo(10L);      // Cobre obterIdsUnidadesAcesso sem NullPointerException

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processo.setDescricao("Processo Finalizado");

        when(repo.buscar(Processo.class, codProcesso)).thenReturn(processo);
        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of());

        ProcessoDetalheDto resultado = processoService.obterDetalhesCompleto(codProcesso, false);
        assertThat(resultado).isNotNull();
        // Ações de bloco devem estar vazias ou desabilitadas
        assertThat(resultado.getAcoesBloco()).allSatisfy(acao -> assertThat(acao.isHabilitar()).isFalse());
    }

    @Test
    @DisplayName("verificarPermissaoEscritaEmBloco deve permitir acao quando processo ativo e perfil permitido")
    void devePermitirEscritaQuandoAtivoEPermitido() {
        Long cod = 1L;
        Unidade uni = criarUnidadeValida(10L);
        lenient().when(validacaoService.validarSubprocessosParaFinalizacao(any())).thenReturn(ResultadoValidacao.ofValido());
        Usuario admin = new Usuario(); admin.setPerfilAtivo(Perfil.ADMIN); admin.setUnidadeAtivaCodigo(10L);
        when(usuarioService.usuarioAutenticado()).thenReturn(admin);
        Processo p = new Processo(); p.setCodigo(cod); p.setSituacao(EM_ANDAMENTO); p.setTipo(MAPEAMENTO);
        p.adicionarParticipantes(Set.of(uni));
        when(repo.buscar(Processo.class, cod)).thenReturn(p);
        Subprocesso sp = new Subprocesso(); sp.setCodigo(100L); sp.setUnidade(uni); sp.setProcesso(p); 
        sp.setSituacaoForcada(sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES);
        when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(sp));
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(100L, uni));
        ProcessoDetalheDto res = processoService.obterDetalhesCompleto(cod, true);
        assertThat(res.getElegiveis().get(0).isHabilitarDisponibilizarMapaBloco()).isTrue();
    }

    @Test
    @DisplayName("verificarPermissaoEscritaEmBloco deve negar quando processo finalizado")
    void deveNegarEscritaQuandoProcessoFinalizado() {
        Long cod = 1L;
        Unidade uni = criarUnidadeValida(10L);
        lenient().when(validacaoService.validarSubprocessosParaFinalizacao(any())).thenReturn(ResultadoValidacao.ofValido());
        Usuario admin = new Usuario(); admin.setPerfilAtivo(Perfil.ADMIN); admin.setUnidadeAtivaCodigo(10L);
        when(usuarioService.usuarioAutenticado()).thenReturn(admin);
        Processo p = new Processo(); p.setCodigo(cod); p.setSituacao(SituacaoProcesso.FINALIZADO); p.setTipo(MAPEAMENTO);
        p.adicionarParticipantes(Set.of(uni));
        when(repo.buscar(Processo.class, cod)).thenReturn(p);
        Subprocesso sp = new Subprocesso(); sp.setCodigo(100L); sp.setUnidade(uni); sp.setProcesso(p); 
        sp.setSituacaoForcada(sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES);
        when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(sp));
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(100L, uni));
        ProcessoDetalheDto res = processoService.obterDetalhesCompleto(cod, true);
        
        // Se o processo está FINALIZADO, nenhuma ação é permitida, então elegiveis deve ser vazio
        assertThat(res.getElegiveis()).isEmpty();
    }

    @Test
    @DisplayName("verificarPermissaoEscritaEmBloco deve permitir quando processo é nulo")
    void devePermitirEscritaQuandoProcessoNulo() {
        Long cod = 1L;
        Unidade uni = criarUnidadeValida(10L);
        lenient().when(validacaoService.validarSubprocessosParaFinalizacao(any())).thenReturn(ResultadoValidacao.ofValido());
        Usuario admin = new Usuario(); admin.setPerfilAtivo(Perfil.ADMIN); admin.setUnidadeAtivaCodigo(10L);
        when(usuarioService.usuarioAutenticado()).thenReturn(admin);
        Processo p = new Processo(); p.setCodigo(cod); p.setSituacao(EM_ANDAMENTO); p.setTipo(MAPEAMENTO);
        p.adicionarParticipantes(Set.of(uni));
        when(repo.buscar(Processo.class, cod)).thenReturn(p);
        Subprocesso sp = new Subprocesso(); sp.setCodigo(100L); sp.setUnidade(uni); sp.setProcesso(null); 
        sp.setSituacaoForcada(sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES);
        when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(sp));
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(100L, uni));
        ProcessoDetalheDto res = processoService.obterDetalhesCompleto(cod, true);
        assertThat(res.getElegiveis().get(0).isHabilitarDisponibilizarMapaBloco()).isTrue();
    }

    @Test
    @DisplayName("verificarPermissaoEscritaEmBloco deve negar quando perfil não permitido")
    void deveNegarEscritaQuandoPerfilNaoPermitido() {
        Long cod = 1L;
        Unidade uni = criarUnidadeValida(10L);
        lenient().when(validacaoService.validarSubprocessosParaFinalizacao(any())).thenReturn(ResultadoValidacao.ofValido());
        lenient().when(unidadeHierarquiaService.buscarIdsDescendentes(anyLong())).thenReturn(List.of(10L));
        Usuario gestor = new Usuario(); gestor.setPerfilAtivo(Perfil.GESTOR); gestor.setUnidadeAtivaCodigo(10L);
        when(usuarioService.usuarioAutenticado()).thenReturn(gestor);
        Processo p = new Processo(); p.setCodigo(cod); p.setSituacao(EM_ANDAMENTO); p.setTipo(MAPEAMENTO);
        p.adicionarParticipantes(Set.of(uni));
        when(repo.buscar(Processo.class, cod)).thenReturn(p);
        Subprocesso sp = new Subprocesso(); sp.setCodigo(100L); sp.setUnidade(uni); sp.setProcesso(p); 
        sp.setSituacaoForcada(sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES);
        when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(sp));
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(100L, uni));
        ProcessoDetalheDto res = processoService.obterDetalhesCompleto(cod, true);
        assertThat(res.getElegiveis().get(0).isHabilitarDisponibilizarMapaBloco()).isFalse();
    }

    @Test
    @DisplayName("criarAcaoBloco deve cobrir todas as combinações de branch na linha 934")
    void deveCobrirTodasCombinacoesHabilitacaoAcaoBloco() {
        Long cod = 1L;
        Processo p = new Processo();
        p.setCodigo(cod);
        p.setTipo(MAPEAMENTO);

        Usuario admin = new Usuario();
        admin.setPerfilAtivo(Perfil.ADMIN);
        admin.setUnidadeAtivaCodigo(10L);

        Usuario servidor = new Usuario();
        servidor.setPerfilAtivo(Perfil.SERVIDOR);
        servidor.setUnidadeAtivaCodigo(10L);

        Unidade uni = criarUnidadeValida(10L);
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setUnidade(uni);
        sp.setProcesso(p);

        // Case 1: perfilPermite=false (A=false) -> servidor tentando homologar
        when(repo.buscar(Processo.class, cod)).thenReturn(p);
        when(usuarioService.usuarioAutenticado()).thenReturn(servidor);
        p.setSituacao(EM_ANDAMENTO);
        when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(sp));
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), uni));
        
        ProcessoDetalheDto res1 = processoService.obterDetalhesCompleto(cod, true);
        assertThat(res1.getAcoesBloco()).filteredOn(a -> a.getCodigo().contains("homologar"))
                .allSatisfy(a -> assertThat(a.isHabilitar()).isFalse());

        // Case 2: perfilPermite=true, processoAtivo=false (A=true, B=false) -> admin, processo FINALIZADO
        when(usuarioService.usuarioAutenticado()).thenReturn(admin);
        p.setSituacao(SituacaoProcesso.FINALIZADO);
        ProcessoDetalheDto res2 = processoService.obterDetalhesCompleto(cod, true);
        assertThat(res2.getAcoesBloco()).allSatisfy(a -> assertThat(a.isHabilitar()).isFalse());

        // Case 3: perfilPermite=true, processoAtivo=true, temUnidades=false (A=true, B=true, C=false)
        p.setSituacao(EM_ANDAMENTO);
        when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of()); // Sem unidades elegiveis
        ProcessoDetalheDto res3 = processoService.obterDetalhesCompleto(cod, true);
        assertThat(res3.getAcoesBloco()).allSatisfy(a -> assertThat(a.isHabilitar()).isFalse());
    }

}
