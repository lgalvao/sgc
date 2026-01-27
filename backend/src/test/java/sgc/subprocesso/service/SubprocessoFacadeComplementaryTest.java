package sgc.subprocesso.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.analise.AnaliseFacade;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.ConhecimentoService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.AtualizarSubprocessoRequest;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.dto.ContextoEdicaoDto;
import sgc.subprocesso.dto.CriarSubprocessoRequest;
import sgc.subprocesso.dto.MapaAjusteDto;
import sgc.subprocesso.dto.SubprocessoCadastroDto;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.dto.SugestoesDto;
import sgc.subprocesso.dto.ValidacaoCadastroDto;
import sgc.subprocesso.mapper.MapaAjusteMapper;
import sgc.subprocesso.mapper.SubprocessoDetalheMapper;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;
import sgc.subprocesso.service.workflow.SubprocessoWorkflowService;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("SubprocessoFacade - Testes Complementares")
class SubprocessoFacadeComplementaryTest {
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private UnidadeFacade unidadeFacade;
    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private SubprocessoValidacaoService validacaoService;
    @Mock
    private SubprocessoWorkflowService workflowService;
    @Mock
    private AtividadeService atividadeService;
    @Mock
    private MovimentacaoRepo repositorioMovimentacao;
    @Mock
    private SubprocessoDetalheMapper subprocessoDetalheMapper;
    @Mock
    private AnaliseFacade analiseFacade;
    @Mock
    private CompetenciaService competenciaService;
    @Mock
    private ConhecimentoService conhecimentoService;
    @Mock
    private MapaAjusteMapper mapaAjusteMapper;
    @Mock
    private sgc.seguranca.acesso.AccessControlService accessControlService;
    @Mock
    private sgc.subprocesso.model.SubprocessoRepo subprocessoRepo;
    @Mock
    private sgc.subprocesso.model.SubprocessoMovimentacaoRepo movimentacaoRepo;
    @Mock
    private sgc.mapa.service.CopiaMapaService copiaMapaService;

    @InjectMocks
    private SubprocessoFacade subprocessoFacade;

    @Nested
    @DisplayName("Cenários de Leitura")
    @SuppressWarnings("unused")
    class LeituraTests {
        @Test
        @DisplayName("Deve verificar acesso da unidade ao processo")
        void deveVerificarAcessoUnidadeAoProcesso() {
            when(crudService.verificarAcessoUnidadeAoProcesso(1L, List.of(10L, 20L)))
                    .thenReturn(true);
            assertThat(subprocessoFacade.verificarAcessoUnidadeAoProcesso(1L, List.of(10L, 20L))).isTrue();
        }

        @Test
        @DisplayName("Deve listar entidades por processo")
        void deveListarEntidadesPorProcesso() {
            when(crudService.listarEntidadesPorProcesso(1L))
                    .thenReturn(List.of(new Subprocesso()));
            assertThat(subprocessoFacade.listarEntidadesPorProcesso(1L)).hasSize(1);
        }

        @Test
        @DisplayName("Deve listar atividades do subprocesso convertidas para DTO")
        void deveListarAtividadesSubprocesso() {
            Subprocesso sp = new Subprocesso();
            Mapa mapa = new Mapa();
            mapa.setCodigo(1L);
            sp.setMapa(mapa);

            when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
            when(atividadeService.buscarPorMapaCodigoComConhecimentos(1L)).thenReturn(List.of());

            List<AtividadeVisualizacaoDto> result = subprocessoFacade.listarAtividadesSubprocesso(1L);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Deve retornar situação quando subprocesso existe")
        void deveRetornarSituacaoQuandoSubprocessoExiste() {
            when(crudService.obterStatus(1L)).thenReturn(SubprocessoSituacaoDto.builder().build());
            assertThat(subprocessoFacade.obterSituacao(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve retornar entidade por código do mapa")
        void deveRetornarEntidadePorCodigoMapa() {
            when(crudService.obterEntidadePorCodigoMapa(100L)).thenReturn(new Subprocesso());
            assertThat(subprocessoFacade.obterEntidadePorCodigoMapa(100L)).isNotNull();
        }

        @Test
        @DisplayName("Deve retornar lista vazia se não houver atividades sem conhecimento")
        void deveRetornarListaVaziaSeNaoHouverAtividadesSemConhecimento() {
            when(validacaoService.obterAtividadesSemConhecimento(1L)).thenReturn(Collections.emptyList());
            List<Atividade> result = subprocessoFacade.obterAtividadesSemConhecimento(1L);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Deve buscar subprocesso por ID")
        void deveBuscarSubprocessoPorId() {
            when(crudService.buscarSubprocesso(1L)).thenReturn(new Subprocesso());
            assertThat(subprocessoFacade.buscarSubprocesso(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve buscar subprocesso com mapa")
        void deveBuscarSubprocessoComMapa() {
            when(crudService.buscarSubprocessoComMapa(1L)).thenReturn(new Subprocesso());
            assertThat(subprocessoFacade.buscarSubprocessoComMapa(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve obter atividades sem conhecimento por Mapa")
        void deveObterAtividadesSemConhecimentoPorMapa() {
            Mapa mapa = new Mapa();
            when(validacaoService.obterAtividadesSemConhecimento(mapa)).thenReturn(Collections.emptyList());
            assertThat(subprocessoFacade.obterAtividadesSemConhecimento(mapa)).isEmpty();
        }

        @Test
        @DisplayName("Deve listar subprocessos homologados")
        void deveListarSubprocessosHomologados() {
            when(workflowService.listarSubprocessosHomologados()).thenReturn(Collections.emptyList());
            assertThat(subprocessoFacade.listarSubprocessosHomologados()).isEmpty();
        }
    }

    @Nested
    @SuppressWarnings("unused")
    @DisplayName("Cenários de Escrita (CRUD)")
    class CrudTests {
        @Test
        @DisplayName("Deve criar subprocesso com sucesso")
        void deveCriarSubprocessoComSucesso() {
            CriarSubprocessoRequest request = CriarSubprocessoRequest.builder().codProcesso(1L).codUnidade(1L).build();
            SubprocessoDto response = SubprocessoDto.builder().build();
            when(crudService.criar(request)).thenReturn(response);
            assertThat(subprocessoFacade.criar(request)).isNotNull();
        }

        @Test
        @DisplayName("Deve atualizar subprocesso com sucesso")
        void deveAtualizarSubprocessoComSucesso() {
            AtualizarSubprocessoRequest request = AtualizarSubprocessoRequest.builder().codMapa(100L).build();
            SubprocessoDto response = SubprocessoDto.builder().codMapa(100L).build();
            when(crudService.atualizar(1L, request)).thenReturn(response);
            assertThat(subprocessoFacade.atualizar(1L, request)).isNotNull();
        }

        @Test
        @DisplayName("Deve excluir subprocesso com sucesso")
        void deveExcluirSubprocessoComSucesso() {
            subprocessoFacade.excluir(1L);
            verify(crudService).excluir(1L);
        }
    }

    @Nested
    @DisplayName("Cenários de Validação")
    @SuppressWarnings("unused")
    class ValidacaoTests {
        @Test
        @DisplayName("Deve validar existência de atividades - Sucesso")
        void deveValidarExistenciaAtividadesSucesso() {
            subprocessoFacade.validarExistenciaAtividades(1L);
            verify(validacaoService).validarExistenciaAtividades(1L);
        }

        @Test
        @DisplayName("validarCadastro sucesso")
        void validarCadastroSucesso() {
            ValidacaoCadastroDto val = ValidacaoCadastroDto.builder().valido(true).build();
            when(validacaoService.validarCadastro(1L)).thenReturn(val);

            ValidacaoCadastroDto result = subprocessoFacade.validarCadastro(1L);
            assertThat(result.valido()).isTrue();
        }

        @Test
        @DisplayName("Deve validar associações do mapa")
        void deveValidarAssociacoesMapa() {
            subprocessoFacade.validarAssociacoesMapa(100L);
            verify(validacaoService).validarAssociacoesMapa(100L);
        }
    }

    @Nested
    @DisplayName("Cenários de Transição de Estado")
    @SuppressWarnings("unused")
    class TransicaoEstadoTests {
        @Test
        @DisplayName("Deve atualizar situação para EM ANDAMENTO")
        void deveAtualizarParaEmAndamentoMapeamento() {
            subprocessoFacade.atualizarSituacaoParaEmAndamento(100L);
            verify(workflowService).atualizarSituacaoParaEmAndamento(100L);
        }

        @Test
        @DisplayName("Deve alterar data limite")
        void deveAlterarDataLimite() {
            LocalDate novaData = java.time.LocalDate.now();
            subprocessoFacade.alterarDataLimite(1L, novaData);
            verify(workflowService).alterarDataLimite(1L, novaData);
        }

        @Test
        @DisplayName("Deve reabrir cadastro")
        void deveReabrirCadastro() {
            subprocessoFacade.reabrirCadastro(1L, "Justificativa");
            verify(workflowService).reabrirCadastro(1L, "Justificativa");
        }

        @Test
        @DisplayName("Deve reabrir revisão cadastro")
        void deveReabrirRevisaoCadastro() {
            subprocessoFacade.reabrirRevisaoCadastro(1L, "Justificativa");
            verify(workflowService).reabrirRevisaoCadastro(1L, "Justificativa");
        }
    }

    @Nested
    @DisplayName("Cenários de Permissões e Detalhes")
    @SuppressWarnings("unused")
    class PermissaoDetalheTests {
        @Test
        @DisplayName("obterPermissoes lança exceção quando não autenticado")
        void obterPermissoesSemAutenticacao() {
            when(usuarioService.obterUsuarioAutenticado())
                    .thenThrow(new sgc.comum.erros.ErroAccessoNegado("Nenhum usuário autenticado"));

            var exception = org.junit.jupiter.api.Assertions.assertThrows(sgc.comum.erros.ErroAccessoNegado.class, () ->
                    subprocessoFacade.obterPermissoes(1L)
            );
            assertThat(exception).isNotNull();
        }

        @Test
        @DisplayName("obterPermissoes lança exceção quando autenticação não tem nome")
        void obterPermissoesComAutenticacaoSemNome() {
            when(usuarioService.obterUsuarioAutenticado())
                    .thenThrow(new sgc.comum.erros.ErroAccessoNegado("Usuário sem nome"));

            var exception = org.junit.jupiter.api.Assertions.assertThrows(sgc.comum.erros.ErroAccessoNegado.class, () ->
                    subprocessoFacade.obterPermissoes(1L)
            );
            assertThat(exception).isNotNull();
        }

        @Test
        @DisplayName("Deve obter permissoes com sucesso")
        void deveObterPermissoes() {
            Long codigo = 1L;
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(codigo);
            sgc.processo.model.Processo proc = new sgc.processo.model.Processo();
            proc.setTipo(sgc.processo.model.TipoProcesso.MAPEAMENTO);
            sp.setProcesso(proc);
            Usuario usuario = new Usuario();

            when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
            // We can mock 'podeExecutar' calls implicitly returning false by default boolean mock

            SubprocessoPermissoesDto result = subprocessoFacade.obterPermissoes(codigo);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Deve obter detalhes do subprocesso")
        void deveObterDetalhes() {
            Long codigo = 1L;
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(codigo);
            sgc.processo.model.Processo proc = new sgc.processo.model.Processo();
            proc.setTipo(sgc.processo.model.TipoProcesso.MAPEAMENTO);
            sp.setProcesso(proc);
            Unidade unidade = new Unidade();
            unidade.setSigla("SIGLA");
            unidade.setTituloTitular("TITULAR");
            sp.setUnidade(unidade);

            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral("123");

            when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
            when(unidadeFacade.buscarResponsavelAtual("SIGLA")).thenReturn(new Usuario());
            when(usuarioService.buscarPorLogin("TITULAR")).thenReturn(new Usuario());
            when(repositorioMovimentacao.findBySubprocessoCodigoOrderByDataHoraDesc(codigo)).thenReturn(List.of());

            when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any())).thenReturn(SubprocessoDetalheDto.builder().unidade(SubprocessoDetalheDto.UnidadeDto.builder().sigla("SIGLA").build()).build());

            SubprocessoDetalheDto result = subprocessoFacade.obterDetalhes(codigo, sgc.organizacao.model.Perfil.ADMIN);

            assertThat(result).isNotNull();
            verify(accessControlService).verificarPermissao(usuario, sgc.seguranca.acesso.Acao.VISUALIZAR_SUBPROCESSO, sp);
        }

        @Test
        @DisplayName("obterDetalhes deve lidar com exceção ao buscar titular")
        void obterDetalhesTitularException() {
            Long codigo = 1L;
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(codigo);
            sgc.processo.model.Processo proc = new sgc.processo.model.Processo();
            proc.setTipo(sgc.processo.model.TipoProcesso.MAPEAMENTO);
            sp.setProcesso(proc);
            Unidade unidade = new Unidade();
            unidade.setSigla("SIGLA");
            unidade.setTituloTitular("TITULAR");
            sp.setUnidade(unidade);

            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral("123");

            when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
            when(unidadeFacade.buscarResponsavelAtual("SIGLA")).thenReturn(new Usuario());
            when(usuarioService.buscarPorLogin("TITULAR")).thenThrow(new RuntimeException("Erro"));
            when(repositorioMovimentacao.findBySubprocessoCodigoOrderByDataHoraDesc(codigo)).thenReturn(List.of());
            when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any())).thenReturn(SubprocessoDetalheDto.builder().build());

            SubprocessoDetalheDto result = subprocessoFacade.obterDetalhes(codigo, sgc.organizacao.model.Perfil.ADMIN);

            // Verifica que executou sem erro mesmo com exceção ao buscar titular
            assertThat(result).isNotNull();
            // Confirma que tentou buscar o titular (mesmo que tenha falhado)
            verify(usuarioService).buscarPorLogin("TITULAR");
        }

        @Test
        @DisplayName("Deve obter contexto de edição")
        void deveObterContextoEdicao() {
            Long codigo = 1L;
            Usuario usuario = new Usuario();
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(codigo);
            sgc.processo.model.Processo proc = new sgc.processo.model.Processo();
            proc.setTipo(sgc.processo.model.TipoProcesso.MAPEAMENTO);
            sp.setProcesso(proc);
            Unidade unidade = new Unidade();
            unidade.setSigla("SIGLA");
            sp.setUnidade(unidade);
            Mapa mapa = new Mapa();
            mapa.setCodigo(100L);
            sp.setMapa(mapa);

            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
            when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
            when(repositorioMovimentacao.findBySubprocessoCodigoOrderByDataHoraDesc(codigo)).thenReturn(List.of());
            when(usuarioService.buscarResponsavelAtual("SIGLA")).thenReturn(new Usuario());

            sgc.organizacao.dto.UnidadeDto unidadeDto = sgc.organizacao.dto.UnidadeDto.builder().sigla("SIGLA").build();
            SubprocessoDetalheDto.UnidadeDto subUnidadeDto = SubprocessoDetalheDto.UnidadeDto.builder().sigla("SIGLA").build();
            when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any())).thenReturn(SubprocessoDetalheDto.builder().unidade(subUnidadeDto).build());
            when(unidadeFacade.buscarPorSigla("SIGLA")).thenReturn(unidadeDto);
            when(atividadeService.buscarPorMapaCodigoComConhecimentos(100L)).thenReturn(java.util.Collections.emptyList());

            ContextoEdicaoDto result = subprocessoFacade.obterContextoEdicao(codigo, sgc.organizacao.model.Perfil.ADMIN);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("obterPermissoes para processo REVISAO")
        void obterPermissoesProcessoRevisao() {
            Long codigo = 1L;
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(codigo);
            sgc.processo.model.Processo proc = new sgc.processo.model.Processo();
            proc.setTipo(sgc.processo.model.TipoProcesso.REVISAO);
            sp.setProcesso(proc);

            Usuario usuario = new Usuario();

            when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            SubprocessoPermissoesDto result = subprocessoFacade.obterPermissoes(codigo);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("obterPermissoes para processo MAPEAMENTO")
        void obterPermissoesProcessoMapeamento() {
            Long codigo = 1L;
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(codigo);
            sgc.processo.model.Processo proc = new sgc.processo.model.Processo();
            proc.setTipo(sgc.processo.model.TipoProcesso.MAPEAMENTO);
            sp.setProcesso(proc);

            Usuario usuario = new Usuario();

            when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            SubprocessoPermissoesDto result = subprocessoFacade.obterPermissoes(codigo);
            assertThat(result).isNotNull();
            // Verifica que com MAPEAMENTO as ações são as padrões (não REVISAO)
            verify(accessControlService).podeExecutar(usuario, sgc.seguranca.acesso.Acao.DISPONIBILIZAR_CADASTRO, sp);
        }
    }

    @Nested
    @DisplayName("Cenários de DTO e Mapeamento")
    @SuppressWarnings("unused")
    class DtoMappingTests {
        @Test
        @DisplayName("obterSugestoes")
        void obterSugestoes() {
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            mapa.setSugestoes("");
            sp.setMapa(mapa);

            sgc.organizacao.model.Unidade u = new sgc.organizacao.model.Unidade();
            u.setNome("Unidade Teste");
            sp.setUnidade(u);
            when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

            SugestoesDto result = subprocessoFacade.obterSugestoes(1L);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("listar")
        void listar() {
            when(crudService.listar()).thenReturn(List.of(new SubprocessoDto()));
            List<SubprocessoDto> result = subprocessoFacade.listar();
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("obterPorProcessoEUnidade")
        void obterPorProcessoEUnidade() {
            when(crudService.obterPorProcessoEUnidade(1L, 10L)).thenReturn(SubprocessoDto.builder().codigo(1L).build());
            SubprocessoDto result = subprocessoFacade.obterPorProcessoEUnidade(1L, 10L);
            assertThat(result).isNotNull();
            assertThat(result.getCodigo()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve obter cadastro")
        void deveObterCadastro() {
            Long codigo = 1L;
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(codigo);
            sp.setUnidade(new Unidade());
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);

            when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
            Atividade ativ = new Atividade();
            ativ.setCodigo(100L);
            ativ.setConhecimentos(List.of());
            when(atividadeService.buscarPorMapaCodigoComConhecimentos(10L)).thenReturn(List.of(ativ));

            SubprocessoCadastroDto result = subprocessoFacade.obterCadastro(codigo);
            assertThat(result).isNotNull();
            assertThat(result.getAtividades()).hasSize(1);
        }

        @Test
        @DisplayName("Deve obter mapa para ajuste")
        void deveObterMapaParaAjuste() {
            Long codigo = 1L;
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(codigo);
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);

            when(crudService.buscarSubprocessoComMapa(codigo)).thenReturn(sp);
            when(analiseFacade.listarPorSubprocesso(codigo, sgc.analise.model.TipoAnalise.VALIDACAO)).thenReturn(List.of());
            when(competenciaService.buscarPorCodMapaSemRelacionamentos(10L)).thenReturn(List.of());
            when(competenciaService.buscarIdsAssociacoesCompetenciaAtividade(10L)).thenReturn(Collections.emptyMap());
            when(atividadeService.buscarPorMapaCodigoSemRelacionamentos(10L)).thenReturn(List.of());
            when(conhecimentoService.listarPorMapa(10L)).thenReturn(List.of());
            when(mapaAjusteMapper.toDto(any(), any(), any(), any(), any(), any())).thenReturn(MapaAjusteDto.builder().build());

            MapaAjusteDto result = subprocessoFacade.obterMapaParaAjuste(codigo);
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Cenários Complexos")
    @SuppressWarnings("unused")
    class SubprocessoFacadeRefactoredTest {
        @Test
        @DisplayName("Deve salvar ajustes do mapa")
        void deveSalvarAjustesMapa() {
            Long codigo = 1L;
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(codigo);
            sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);

            when(subprocessoRepo.findById(codigo)).thenReturn(java.util.Optional.of(sp));

            CompetenciaAjusteDto compDto = CompetenciaAjusteDto.builder()
                    .codCompetencia(10L)
                    .nome("Nova Comp")
                    .atividades(List.of())
                    .build();

            Competencia comp = new Competencia();
            comp.setCodigo(10L);
            when(competenciaService.buscarPorCodigos(List.of(10L))).thenReturn(List.of(comp));

            subprocessoFacade.salvarAjustesMapa(codigo, List.of(compDto));

            verify(competenciaService).salvarTodas(anyList());
            verify(subprocessoRepo).save(sp);
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        }

        @Test
        @DisplayName("salvarAjustesMapa deve aceitar REVISAO_MAPA_AJUSTADO")
        void salvarAjustesMapaAceitaRevisaoMapaAjustado() {
            Long codigo = 1L;
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(codigo);
            sp.setSituacao(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);

            when(subprocessoRepo.findById(codigo)).thenReturn(java.util.Optional.of(sp));
            // empty list of adjustments
            subprocessoFacade.salvarAjustesMapa(codigo, List.of());

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        }

        @Test
        @DisplayName("salvarAjustesMapa deve lançar exceção se situação inválida")
        void salvarAjustesMapaSituacaoInvalida() {
            Long codigo = 1L;
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(codigo);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            when(subprocessoRepo.findById(codigo)).thenReturn(java.util.Optional.of(sp));

            var exception = org.junit.jupiter.api.Assertions.assertThrows(sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida.class, () ->
                    subprocessoFacade.salvarAjustesMapa(codigo, List.of())
            );
            assertThat(exception).isNotNull();
        }

        @Test
        @DisplayName("Deve importar atividades")
        void deveImportarAtividades() {
            Long dest = 1L;
            Long orig = 2L;

            Subprocesso spDest = new Subprocesso();
            spDest.setCodigo(dest);
            spDest.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
            Mapa mapaDest = new Mapa();
            mapaDest.setCodigo(10L);
            spDest.setMapa(mapaDest);
            sgc.processo.model.Processo processo = new sgc.processo.model.Processo();
            processo.setTipo(sgc.processo.model.TipoProcesso.MAPEAMENTO);
            spDest.setProcesso(processo);
            spDest.setUnidade(new Unidade());

            Subprocesso spOrig = new Subprocesso();
            spOrig.setCodigo(orig);
            Mapa mapaOrig = new Mapa();
            mapaOrig.setCodigo(20L);
            spOrig.setMapa(mapaOrig);
            spOrig.setUnidade(new Unidade());

            when(subprocessoRepo.findById(dest)).thenReturn(java.util.Optional.of(spDest));
            when(subprocessoRepo.findById(orig)).thenReturn(java.util.Optional.of(spOrig));

            subprocessoFacade.importarAtividades(dest, orig);

            verify(copiaMapaService).importarAtividadesDeOutroMapa(20L, 10L);
            verify(movimentacaoRepo).save(any());
            assertThat(spDest.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        }

        @Test
        @DisplayName("importarAtividades falha se situação destino inválida")
        void importarAtividadesSituacaoDestinoInvalida() {
            Long dest = 1L;
            Subprocesso spDest = new Subprocesso();
            spDest.setCodigo(dest);
            spDest.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO); // Invalid for import

            when(subprocessoRepo.findById(dest)).thenReturn(java.util.Optional.of(spDest));

            var exception = org.junit.jupiter.api.Assertions.assertThrows(sgc.subprocesso.erros.ErroAtividadesEmSituacaoInvalida.class, () ->
                    subprocessoFacade.importarAtividades(dest, 2L)
            );
            assertThat(exception).isNotNull();
        }

        @Test
        @DisplayName("importarAtividades lida com tipo processo REVISAO e Default")
        void importarAtividadesTiposProcesso() {
            // Case REVISAO
            Long dest = 1L;
            Subprocesso spDest = new Subprocesso();
            spDest.setCodigo(dest);
            spDest.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
            spDest.setMapa(new Mapa());
            spDest.setUnidade(new Unidade());
            sgc.processo.model.Processo proc = new sgc.processo.model.Processo();
            proc.setTipo(sgc.processo.model.TipoProcesso.REVISAO);
            spDest.setProcesso(proc);

            Long orig = 2L;
            Subprocesso spOrig = new Subprocesso();
            spOrig.setMapa(new Mapa());
            spOrig.setUnidade(new Unidade()); // Origem unidade not null

            when(subprocessoRepo.findById(dest)).thenReturn(java.util.Optional.of(spDest));
            when(subprocessoRepo.findById(orig)).thenReturn(java.util.Optional.of(spOrig));

            subprocessoFacade.importarAtividades(dest, orig);
            assertThat(spDest.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);

            // Case Default (using DIAGNOSTICO to trigger default branch)
            spDest.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
            proc.setTipo(sgc.processo.model.TipoProcesso.DIAGNOSTICO);

            subprocessoFacade.importarAtividades(dest, orig);

            // Default case logs debug and doesn't change status, so it remains NAO_INICIADO
            assertThat(spDest.getSituacao()).isEqualTo(SituacaoSubprocesso.NAO_INICIADO);
        }
    }
}
