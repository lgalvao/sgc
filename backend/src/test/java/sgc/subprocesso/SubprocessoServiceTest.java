package sgc.subprocesso;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.modelo.AlertaRepo;
import sgc.analise.modelo.AnaliseCadastro;
import sgc.analise.modelo.AnaliseCadastroRepo;
import sgc.analise.modelo.AnaliseValidacao;
import sgc.analise.modelo.AnaliseValidacaoRepo;
import sgc.atividade.dto.AtividadeMapper;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.conhecimento.dto.ConhecimentoMapper;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.mapa.modelo.Mapa;
import sgc.notificacao.NotificacaoService;
import sgc.notificacao.modelo.NotificacaoRepo;
import sgc.subprocesso.dto.MovimentacaoMapper;
import sgc.subprocesso.dto.SubprocessoCadastroDto;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.dto.SubprocessoMapper;
import sgc.subprocesso.dto.SugestoesDto;
import sgc.subprocesso.dto.AnaliseValidacaoDto;
import sgc.subprocesso.dto.MapaAjusteDto;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import sgc.comum.modelo.Usuario;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import sgc.comum.enums.SituacaoSubprocesso;

@ExtendWith(MockitoExtension.class)
public class SubprocessoServiceTest {
    @Mock
    private SubprocessoRepo repositorioSubprocesso;

    @Mock
    private MovimentacaoRepo repositorioMovimentacao;

    @Mock
    private AtividadeRepo atividadeRepo;

    @Mock
    private ConhecimentoRepo repositorioConhecimento;

    @Mock
    private AnaliseCadastroRepo repositorioAnaliseCadastro;

    @Mock
    private AnaliseValidacaoRepo repositorioAnaliseValidacao;

    @Mock
    private NotificacaoRepo repositorioNotificacao;

    @Mock
    private CompetenciaRepo competenciaRepo;

    @Mock
    private CompetenciaAtividadeRepo competenciaAtividadeRepo;

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private ApplicationEventPublisher publicadorDeEventos;

    @Mock
    private AlertaRepo repositorioAlerta;

    @Mock
    private AtividadeMapper atividadeMapper;

    @Mock
    private ConhecimentoMapper conhecimentoMapper;

    @Mock
    private MovimentacaoMapper movimentacaoMapper;

    @Mock
    private SubprocessoMapper subprocessoMapper;

    @Mock
    private sgc.unidade.modelo.UnidadeRepo unidadeRepo;

    @InjectMocks
    private SubprocessoService subprocessoService;

    @BeforeEach
    void setUp() {
        // Since we are calling a method on the service from within the service, we need a spy
        subprocessoService = spy(new SubprocessoService(
            repositorioSubprocesso,
            repositorioMovimentacao,
            atividadeRepo,
            repositorioConhecimento,
            repositorioAnaliseCadastro,
            repositorioAnaliseValidacao,
            repositorioNotificacao,
            competenciaRepo,
            competenciaAtividadeRepo,
            notificacaoService,
            publicadorDeEventos,
            repositorioAlerta,
            unidadeRepo,
            atividadeMapper,
            conhecimentoMapper,
            movimentacaoMapper,
            subprocessoMapper
        ));
    }

    @Test
    void obterDetalhes_deveRetornarDetalhes_quandoSubprocessoEncontrado() {
        Long id = 1L;
        Subprocesso subprocesso = criarSubprocessoComMapa(id);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));

        List<Movimentacao> movimentacoes = Collections.singletonList(mock(Movimentacao.class));
        when(repositorioMovimentacao.findBySubprocessoCodigoOrderByDataHoraDesc(id)).thenReturn(movimentacoes);

        List<Atividade> atividades = Collections.singletonList(mock(Atividade.class));
        when(atividadeRepo.findByMapaCodigo(anyLong())).thenReturn(atividades);



        SubprocessoDetalheDto result = subprocessoService.obterDetalhes(id, "ADMIN", null);

        assertNotNull(result);
    }

    @Test
    void obterDetalhes_deveLancarExcecao_quandoPerfilNulo() {
        ErroDominioAccessoNegado exception = assertThrows(
                ErroDominioAccessoNegado.class,
                () -> subprocessoService.obterDetalhes(1L, null, null)
        );
        assertEquals("Perfil inválido para acesso aos detalhes do subprocesso.", exception.getMessage());
    }

    @Test
    void obterDetalhes_deveLancarExcecao_quandoSubprocessoNaoEncontrado() {
        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.empty());

        ErroDominioNaoEncontrado exception = assertThrows(
                ErroDominioNaoEncontrado.class,
                () -> subprocessoService.obterDetalhes(1L, "ADMIN", null)
        );
        assertTrue(exception.getMessage().contains("Subprocesso não encontrado"));
    }

    @Test
    void obterDetalhes_deveLancarExcecao_quandoGestorSemPermissao() {
        Long id = 1L;
        Subprocesso subprocesso = criarSubprocessoMock(id);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));

        ErroDominioAccessoNegado exception = assertThrows(
                ErroDominioAccessoNegado.class,
                () -> subprocessoService.obterDetalhes(id, "GESTOR", 2L) // Diferente da unidade do subprocesso
        );
        assertTrue(exception.getMessage().contains("Usuário sem permissão"));
    }

    @Test
    void obterDetalhes_deveLancarExcecao_quandoPerfilInvalido() {
        Long id = 1L;
        Subprocesso subprocesso = criarSubprocessoMock(id);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));

        ErroDominioAccessoNegado exception = assertThrows(
                ErroDominioAccessoNegado.class,
                () -> subprocessoService.obterDetalhes(id, "INVALIDO", null)
        );
        assertTrue(exception.getMessage().contains("Perfil sem permissão"));
    }

    @Test
    void obterCadastro_deveRetornarCadastro_quandoSubprocessoEncontrado() {
        Long id = 1L;
        Subprocesso subprocesso = criarSubprocessoComMapa(id);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));

        List<Atividade> atividades = Collections.singletonList(mock(Atividade.class));
        when(atividadeRepo.findByMapaCodigo(anyLong())).thenReturn(atividades);

        List<Conhecimento> conhecimentos = Collections.singletonList(mock(Conhecimento.class));
        when(repositorioConhecimento.findByAtividadeCodigo(anyLong())).thenReturn(conhecimentos);
        when(conhecimentoMapper.toDTO(any(Conhecimento.class))).thenReturn(new sgc.conhecimento.dto.ConhecimentoDto(1L, 1L, "Teste"));

        SubprocessoCadastroDto result = subprocessoService.obterCadastro(id);

        assertNotNull(result);
    }

    @Test
    void obterCadastro_deveLancarExcecao_quandoSubprocessoNaoEncontrado() {
        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.empty());

        var exception = assertThrows(
                sgc.comum.erros.ErroEntidadeNaoEncontrada.class,
                () -> subprocessoService.obterCadastro(1L)
        );
        assertTrue(exception.getMessage().contains("Subprocesso com ID 1 não encontrado."));
    }

    @Test
    void obterAtividadesSemConhecimento_deveRetornarLista_quandoSubprocessoComMapa() {
        Long id = 1L;
        Subprocesso subprocesso = criarSubprocessoComMapa(id);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));

        List<Atividade> atividades = Arrays.asList(mock(Atividade.class), mock(Atividade.class));
        when(atividadeRepo.findByMapaCodigo(anyLong())).thenReturn(atividades);

        when(repositorioConhecimento.findByAtividadeCodigo(anyLong())).thenReturn(new ArrayList<>());

        List<Atividade> result = subprocessoService.obterAtividadesSemConhecimento(id);

        assertNotNull(result);
    }

    @Test
    void obterAtividadesSemConhecimento_deveRetornarListaVazia_quandoSubprocessoSemMapa() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));

        List<Atividade> result = subprocessoService.obterAtividadesSemConhecimento(id);

        assertEquals(0, result.size());
    }

    @Test
    void disponibilizarCadastro_deveExecutarComSucesso_quandoSubprocessoValido() {
        Long id = 1L;
        Usuario chefe = new Usuario();
        chefe.setTitulo("chefe");
        Subprocesso subprocesso = criarSubprocessoComMapaEChefe(id, chefe);
        subprocesso.setProcesso(criarProcessoMock());
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));
        doReturn(Collections.emptyList()).when(subprocessoService).obterAtividadesSemConhecimento(id);

        subprocessoService.disponibilizarCadastro(id, chefe);

        verify(repositorioSubprocesso, times(1)).save(subprocesso);
        assertEquals(sgc.comum.enums.SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO, subprocesso.getSituacao());
        assertNotNull(subprocesso.getDataFimEtapa1());
        verify(repositorioMovimentacao, times(1)).save(any(Movimentacao.class));
    }

    @Test
    void disponibilizarCadastro_deveLancarExcecao_quandoUsuarioNaoForChefe() {
        Long id = 1L;
        Usuario chefe = new Usuario();
        chefe.setTitulo("chefe");
        Usuario outroUsuario = new Usuario();
        outroUsuario.setTitulo("outro");
        Subprocesso subprocesso = criarSubprocessoComMapaEChefe(id, chefe);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));

        assertThrows(ErroDominioAccessoNegado.class, () -> {
            subprocessoService.disponibilizarCadastro(id, outroUsuario);
        });
    }

    private Subprocesso criarSubprocessoComMapaEChefe(Long id, Usuario chefe) {
        Subprocesso subprocesso = criarSubprocessoComMapa(id);
        subprocesso.getUnidade().setTitular(chefe);
        return subprocesso;
    }

    @Test
    void disponibilizarRevisao_deveExecutarComSucesso_quandoSubprocessoValido() {
        Long id = 1L;
        Usuario chefe = new Usuario();
        chefe.setTitulo("chefe");
        Subprocesso subprocesso = criarSubprocessoComMapaEChefe(id, chefe);
        subprocesso.setProcesso(criarProcessoMock());
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));
        doReturn(Collections.emptyList()).when(subprocessoService).obterAtividadesSemConhecimento(id);


        subprocessoService.disponibilizarRevisao(id, chefe);

        verify(repositorioSubprocesso, times(1)).save(subprocesso);
        assertEquals(sgc.comum.enums.SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA, subprocesso.getSituacao());
        assertNotNull(subprocesso.getDataFimEtapa1());
        verify(repositorioMovimentacao, times(1)).save(any(Movimentacao.class));
        verify(repositorioAnaliseCadastro, times(1)).deleteBySubprocessoCodigo(id);
    }

    @Test
    void disponibilizarMapa_deveExecutarComSucesso_quandoSubprocessoValido() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        subprocesso.setMapa(new Mapa());
        subprocesso.setProcesso(criarProcessoMock());
        subprocesso.setUnidade(criarUnidadeMock(10L, "UNI", "Unidade"));
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));

        subprocessoService.disponibilizarMapa(id, "observacoes", LocalDate.now(), "usuario");

        verify(repositorioSubprocesso, times(1)).save(subprocesso);
    }

    @Test
    void disponibilizarMapa_deveLancarExcecao_quandoSubprocessoSemMapa() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> subprocessoService.disponibilizarMapa(id, "observacoes", LocalDate.now(), "usuario")
        );
        assertTrue(exception.getMessage().contains("Subprocesso sem mapa associado"));
    }

    @Test
    void apresentarSugestoes_deveExecutarComSucesso_quandoSubprocessoValido() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        subprocesso.setMapa(new Mapa());
        subprocesso.setProcesso(criarProcessoMock());
        subprocesso.setUnidade(criarUnidadeMock(10L, "UNI", "Unidade"));
        subprocesso.getUnidade().setUnidadeSuperior(criarUnidadeMock(20L, "SUPER", "Unidade Superior"));
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(SubprocessoDto.builder().build());

        SubprocessoDto result = subprocessoService.apresentarSugestoes(id, "sugestoes", "usuario");

        assertNotNull(result);
        assertEquals(sgc.comum.enums.SituacaoSubprocesso.MAPA_COM_SUGESTOES, subprocesso.getSituacao());
        verify(repositorioSubprocesso, times(1)).save(subprocesso);
        verify(repositorioAnaliseValidacao, times(1)).deleteBySubprocesso_Codigo(id);
    }

    @Test
    void validarMapa_deveExecutarComSucesso_quandoSubprocessoValido() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        subprocesso.setMapa(new Mapa());
        subprocesso.setProcesso(criarProcessoMock());
        subprocesso.setUnidade(criarUnidadeMock(10L, "UNI", "Unidade"));
        subprocesso.getUnidade().setUnidadeSuperior(criarUnidadeMock(20L, "SUPER", "Unidade Superior"));
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(SubprocessoDto.builder().build());

        SubprocessoDto result = subprocessoService.validarMapa(id, "usuario");

        assertNotNull(result);
        assertEquals(sgc.comum.enums.SituacaoSubprocesso.MAPA_VALIDADO, subprocesso.getSituacao());
        verify(repositorioSubprocesso, times(1)).save(subprocesso);
        verify(repositorioAnaliseValidacao, times(1)).deleteBySubprocesso_Codigo(id);
    }

    @Test
    void obterSugestoes_deveRetornarSugestoes_quandoSubprocessoValido() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        subprocesso.setMapa(new Mapa());
        subprocesso.setMapa(new Mapa());
        subprocesso.getMapa().setSugestoes("sugestoes");
        subprocesso.setUnidade(criarUnidadeMock(10L, "UNI", "Unidade"));
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));

        SugestoesDto result = subprocessoService.obterSugestoes(id);

        assertNotNull(result);
        assertEquals("sugestoes", result.sugestoes());
        assertTrue(result.sugestoesApresentadas());
        assertEquals("Unidade", result.unidadeNome());
    }

    @Test
    void obterHistoricoValidacao_deveRetornarHistorico_quandoSubprocessoValido() {
        Long id = 1L;
        AnaliseValidacao analise = new AnaliseValidacao();
        analise.setCodigo(1L);
        analise.setDataHora(LocalDateTime.now());
        analise.setObservacoes("observacao");

        when(repositorioAnaliseValidacao.findBySubprocesso_CodigoOrderByDataHoraDesc(id))
                .thenReturn(Collections.singletonList(analise));

        List<AnaliseValidacaoDto> result = subprocessoService.obterHistoricoValidacao(id);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void devolverValidacao_deveExecutarComSucesso_quandoSubprocessoValido() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        subprocesso.setProcesso(criarProcessoMock());
        subprocesso.setUnidade(criarUnidadeMock(10L, "UNI", "Unidade"));
        subprocesso.getUnidade().setUnidadeSuperior(criarUnidadeMock(20L, "SUPER", "Unidade Superior"));
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(SubprocessoDto.builder().build());

        SubprocessoDto result = subprocessoService.devolverValidacao(id, "justificativa", "usuario");

        assertNotNull(result);
        assertEquals(sgc.comum.enums.SituacaoSubprocesso.MAPA_DISPONIBILIZADO, subprocesso.getSituacao());
        assertNull(subprocesso.getDataFimEtapa2());
        verify(repositorioSubprocesso, times(1)).save(subprocesso);
        verify(repositorioAnaliseValidacao, times(1)).save(any(AnaliseValidacao.class));
    }

    @Test
    void aceitarValidacao_deveExecutarComSucesso_quandoSubprocessoValido() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        subprocesso.setProcesso(criarProcessoMock());
        subprocesso.setUnidade(criarUnidadeMock(10L, "UNI", "Unidade"));
        Unidade unidadeSuperior = criarUnidadeMock(20L, "SUPER", "Unidade Superior");
        Unidade unidadeSuperSuperior = criarUnidadeMock(30L, "SSUPER", "Unidade Super Superior");
        unidadeSuperior.setUnidadeSuperior(unidadeSuperSuperior);
        subprocesso.getUnidade().setUnidadeSuperior(unidadeSuperior);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(SubprocessoDto.builder().build());

        SubprocessoDto result = subprocessoService.aceitarValidacao(id, "usuario");

        assertNotNull(result);
        verify(repositorioAnaliseValidacao, times(1)).save(any(AnaliseValidacao.class));
        verify(repositorioMovimentacao, times(1)).save(any(Movimentacao.class));
        verify(subprocessoMapper, times(1)).toDTO(subprocesso);
    }

    @Test
    void homologarValidacao_deveExecutarComSucesso_quandoSubprocessoValido() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(SubprocessoDto.builder().build());

        SubprocessoDto result = subprocessoService.homologarValidacao(id, "usuario");

        assertNotNull(result);
        assertEquals(sgc.comum.enums.SituacaoSubprocesso.MAPA_HOMOLOGADO, subprocesso.getSituacao());
        verify(repositorioSubprocesso, times(1)).save(subprocesso);
    }

    @Test
    void obterMapaParaAjuste_deveRetornarDto_quandoSubprocessoValido() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        subprocesso.setMapa(new Mapa());
        subprocesso.setMapa(new Mapa());
        subprocesso.setUnidade(criarUnidadeMock(10L, "UNI", "Unidade"));
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));

        MapaAjusteDto result = subprocessoService.obterMapaParaAjuste(id);

        assertNotNull(result);
        assertEquals(subprocesso.getMapa().getCodigo(), result.mapaId());
        assertEquals(subprocesso.getUnidade().getNome(), result.unidadeNome());
    }

    @Test
    void obterMapaParaAjuste_deveLancarExcecao_quandoSubprocessoSemMapa() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> subprocessoService.obterMapaParaAjuste(id)
        );
        assertTrue(exception.getMessage().contains("Subprocesso sem mapa associado"));
    }

    @Test
    void salvarAjustesMapa_deveExecutarComSucesso_quandoSubprocessoValido() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        subprocesso.setSituacao(sgc.comum.enums.SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(SubprocessoDto.builder().build());

        SubprocessoDto result = subprocessoService.salvarAjustesMapa(id, new ArrayList<>(), "usuario");

        assertNotNull(result);
        assertEquals(sgc.comum.enums.SituacaoSubprocesso.MAPA_AJUSTADO, subprocesso.getSituacao());
        verify(repositorioSubprocesso, times(1)).save(subprocesso);
    }

    @Test
    void salvarAjustesMapa_deveLancarExcecao_quandoSituacaoInvalida() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        subprocesso.setSituacao(sgc.comum.enums.SituacaoSubprocesso.NAO_INICIADO);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> subprocessoService.salvarAjustesMapa(id, new ArrayList<>(), "usuario")
        );
        assertTrue(exception.getMessage().contains("Ajustes no mapa só podem ser feitos em estados específicos"));
    }

    @Test
    void submeterMapaAjustado_deveExecutarComSucesso_quandoSubprocessoValido() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        subprocesso.setUnidade(criarUnidadeMock(10L, "UNI", "Unidade"));
        subprocesso.getUnidade().setUnidadeSuperior(criarUnidadeMock(20L, "SUPER", "Unidade Superior"));
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(SubprocessoDto.builder().build());

        SubprocessoDto result = subprocessoService.submeterMapaAjustado(id, "usuario");

        assertNotNull(result);
        assertEquals(sgc.comum.enums.SituacaoSubprocesso.MAPA_AJUSTADO, subprocesso.getSituacao());
        verify(repositorioSubprocesso, times(1)).save(subprocesso);
        verify(repositorioMovimentacao, times(1)).save(any(Movimentacao.class));
    }

    @Test
    void devolverCadastro_deveExecutarComSucesso_quandoSubprocessoValido() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        subprocesso.setProcesso(criarProcessoMock());
        subprocesso.setSituacao(sgc.comum.enums.SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);
        Unidade unidade = criarUnidadeMock(10L, "UNI", "Unidade");
        Unidade unidadeSuperior = criarUnidadeMock(20L, "SUPER", "Unidade Superior");
        unidade.setUnidadeSuperior(unidadeSuperior);
        subprocesso.setUnidade(unidade);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(SubprocessoDto.builder().build());

        SubprocessoDto result = subprocessoService.devolverCadastro(id, "motivo", "observacoes", "usuario");

        assertNotNull(result);
        assertEquals(sgc.comum.enums.SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, subprocesso.getSituacao());
        assertNull(subprocesso.getDataFimEtapa1());
        verify(repositorioSubprocesso, times(1)).save(subprocesso);
        verify(repositorioAnaliseCadastro, times(1)).save(any(AnaliseCadastro.class));
    }

    @Test
    void aceitarCadastro_deveExecutarComSucesso_quandoSubprocessoValido() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        subprocesso.setProcesso(criarProcessoMock());
        subprocesso.setSituacao(sgc.comum.enums.SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);
        Unidade unidade = criarUnidadeMock(10L, "UNI", "Unidade");
        Unidade unidadeSuperior = criarUnidadeMock(20L, "SUPER", "Unidade Superior");
        unidade.setUnidadeSuperior(unidadeSuperior);
        subprocesso.setUnidade(unidade);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(SubprocessoDto.builder().build());

        SubprocessoDto result = subprocessoService.aceitarCadastro(id, "observacoes", "usuario");

        assertNotNull(result);
        verify(repositorioAnaliseCadastro, times(1)).save(any(AnaliseCadastro.class));
        verify(repositorioMovimentacao, times(1)).save(any(Movimentacao.class));
    }

    @Test
    void aceitarCadastro_deveLancarExcecao_quandoSituacaoInvalida() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        subprocesso.setSituacao(sgc.comum.enums.SituacaoSubprocesso.NAO_INICIADO);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> subprocessoService.aceitarCadastro(id, "observacoes", "usuario")
        );
        assertTrue(exception.getMessage().contains("Ação de aceite só pode ser executada em cadastros disponibilizados"));
    }

    @Test
    void homologarCadastro_deveExecutarComSucesso_quandoSubprocessoValido() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        subprocesso.setProcesso(criarProcessoMock());
        subprocesso.setSituacao(sgc.comum.enums.SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);
        Unidade unidade = criarUnidadeMock(10L, "UNI", "Unidade");
        Unidade unidadeSuperior = criarUnidadeMock(20L, "SUPER", "Unidade Superior");
        unidade.setUnidadeSuperior(unidadeSuperior);
        subprocesso.setUnidade(unidade);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));

        Unidade sedoc = new Unidade();
        sedoc.setSigla("SEDOC");
        when(unidadeRepo.findBySigla("SEDOC")).thenReturn(Optional.of(sedoc));

        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(SubprocessoDto.builder().build());

        SubprocessoDto result = subprocessoService.homologarCadastro(id, "observacoes", "usuario");

        assertNotNull(result);
        assertEquals(sgc.comum.enums.SituacaoSubprocesso.CADASTRO_HOMOLOGADO, subprocesso.getSituacao());
        verify(repositorioSubprocesso, times(1)).save(subprocesso);
    }

    @Test
    void devolverRevisaoCadastro_deveExecutarComSucesso_quandoSubprocessoValido() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        subprocesso.setProcesso(criarProcessoMock());
        subprocesso.setSituacao(sgc.comum.enums.SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        Unidade unidade = criarUnidadeMock(10L, "UNI", "Unidade");
        unidade.setUnidadeSuperior(criarUnidadeMock(20L, "SUPER", "Unidade Superior"));
        subprocesso.setUnidade(unidade);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(SubprocessoDto.builder().build());

        SubprocessoDto result = subprocessoService.devolverRevisaoCadastro(id, "motivo", "observacoes", "usuario");

        assertNotNull(result);
        assertEquals(sgc.comum.enums.SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, subprocesso.getSituacao());
        assertNull(subprocesso.getDataFimEtapa1());
        verify(repositorioSubprocesso, times(1)).save(subprocesso);
        verify(repositorioAnaliseCadastro, times(1)).save(any(AnaliseCadastro.class));
    }

    @Test
    void aceitarRevisaoCadastro_deveExecutarComSucesso_quandoSubprocessoValido() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        subprocesso.setProcesso(criarProcessoMock());
        subprocesso.setSituacao(sgc.comum.enums.SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        Unidade unidade = criarUnidadeMock(10L, "UNI", "Unidade");
        unidade.setUnidadeSuperior(criarUnidadeMock(20L, "SUPER", "Unidade Superior"));
        subprocesso.setUnidade(unidade);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(SubprocessoDto.builder().build());

        SubprocessoDto result = subprocessoService.aceitarRevisaoCadastro(id, "observacoes", "usuario");

        assertNotNull(result);
        verify(repositorioAnaliseCadastro, times(1)).save(any(AnaliseCadastro.class));
        verify(repositorioMovimentacao, times(1)).save(any(Movimentacao.class));
    }

    @Test
    void homologarRevisaoCadastro_deveExecutarComSucesso_quandoSubprocessoValido() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        subprocesso.setProcesso(criarProcessoMock());
        subprocesso.setSituacao(sgc.comum.enums.SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        Unidade unidade = criarUnidadeMock(10L, "UNI", "Unidade");
        unidade.setUnidadeSuperior(criarUnidadeMock(20L, "SUPER", "Unidade Superior"));
        subprocesso.setUnidade(unidade);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(SubprocessoDto.builder().build());

        SubprocessoDto result = subprocessoService.homologarRevisaoCadastro(id, "observacoes", "usuario");

        assertNotNull(result);
        assertEquals(sgc.comum.enums.SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, subprocesso.getSituacao());
        verify(repositorioSubprocesso, times(1)).save(subprocesso);
    }

    // Helper methods
    private sgc.processo.modelo.Processo criarProcessoMock() {
        sgc.processo.modelo.Processo processo = new sgc.processo.modelo.Processo();
        processo.setDescricao("Processo Teste");
        processo.setCodigo(100L);
        return processo;
    }

    private Subprocesso criarSubprocessoMock(Long id) {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        subprocesso.setUnidade(criarUnidadeMock(10L, "UNI", "Unidade"));
        subprocesso.setSituacao(SituacaoSubprocesso.NAO_INICIADO); // Set a default situation
        return subprocesso;
    }

    private Subprocesso criarSubprocessoComMapa(Long id) {
        Subprocesso subprocesso = criarSubprocessoMock(id);
        Mapa mapa = new Mapa();
        mapa.setCodigo(5L);
        subprocesso.setMapa(mapa);
        return subprocesso;
    }

    private Unidade criarUnidadeMock(Long codigo, String sigla, String nome) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigo);
        unidade.setSigla(sigla);
        unidade.setNome(nome);
        return unidade;
    }
}