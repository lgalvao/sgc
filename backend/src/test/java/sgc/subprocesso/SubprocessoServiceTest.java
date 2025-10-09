package sgc.subprocesso;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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
import sgc.subprocesso.dto.*;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @InjectMocks
    private SubprocessoService subprocessoService;

    @Test
    void obterDetalhes_deveRetornarDetalhes_quandoSubprocessoEncontrado() {
        Long id = 1L;
        Subprocesso subprocesso = criarSubprocessoMock(id);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));

        List<Movimentacao> movimentacoes = Collections.singletonList(mock(Movimentacao.class));
        when(repositorioMovimentacao.findBySubprocessoCodigoOrderByDataHoraDesc(id)).thenReturn(movimentacoes);

        List<Atividade> atividades = Collections.singletonList(mock(Atividade.class));
        when(atividadeRepo.findByMapaCodigo(anyLong())).thenReturn(atividades);

        List<Conhecimento> conhecimentos = Collections.singletonList(mock(Conhecimento.class));
        when(repositorioConhecimento.findAll()).thenReturn(conhecimentos);

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
        Subprocesso subprocesso = criarSubprocessoMock(id);
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

        ErroDominioNaoEncontrado exception = assertThrows(
                ErroDominioNaoEncontrado.class,
                () -> subprocessoService.obterCadastro(1L)
        );
        assertTrue(exception.getMessage().contains("Subprocesso não encontrado"));
    }

    @Test
    void obterAtividadesSemConhecimento_deveRetornarLista_quandoSubprocessoComMapa() {
        Long id = 1L;
        Subprocesso subprocesso = criarSubprocessoMock(id);
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
        Subprocesso subprocesso = criarSubprocessoComMapa(id);
        subprocesso.setProcesso(criarProcessoMock());
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));

        subprocessoService.disponibilizarCadastro(id);

        verify(repositorioSubprocesso, times(1)).save(subprocesso);
        assertEquals("CADASTRO_DISPONIBILIZADO", subprocesso.getSituacaoId());
        assertNotNull(subprocesso.getDataFimEtapa1());
        verify(repositorioMovimentacao, times(1)).save(any(Movimentacao.class));
        verify(repositorioAnaliseCadastro, times(1)).deleteBySubprocessoCodigo(id);
    }

    @Test
    void disponibilizarCadastro_deveLancarExcecao_quandoSubprocessoSemMapa() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> subprocessoService.disponibilizarCadastro(id)
        );
        assertTrue(exception.getMessage().contains("Subprocesso sem mapa associado"));
    }

    @Test
    void disponibilizarRevisao_deveExecutarComSucesso_quandoSubprocessoValido() {
        Long id = 1L;
        Subprocesso subprocesso = criarSubprocessoMock(id);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));

        subprocessoService.disponibilizarRevisao(id);

        verify(repositorioSubprocesso, times(1)).save(subprocesso);
        assertEquals("REVISAO_CADASTRO_DISPONIBILIZADA", subprocesso.getSituacaoId());
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
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(new SubprocessoDto());

        SubprocessoDto result = subprocessoService.disponibilizarMapa(id, "observacoes", LocalDate.now(), "usuario");

        assertNotNull(result);
        assertEquals("MAPA_DISPONIBILIZADO", subprocesso.getSituacaoId());
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
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(new SubprocessoDto());

        SubprocessoDto result = subprocessoService.apresentarSugestoes(id, "sugestoes", "usuario");

        assertNotNull(result);
        assertEquals("MAPA_COM_SUGESTOES", subprocesso.getSituacaoId());
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
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(new SubprocessoDto());

        SubprocessoDto result = subprocessoService.validarMapa(id, "usuario");

        assertNotNull(result);
        assertEquals("MAPA_VALIDADO", subprocesso.getSituacaoId());
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
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(new SubprocessoDto());

        SubprocessoDto result = subprocessoService.devolverValidacao(id, "justificativa", "usuario");

        assertNotNull(result);
        assertEquals("MAPA_DISPONIBILIZADO", subprocesso.getSituacaoId());
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
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(new SubprocessoDto());

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
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(new SubprocessoDto());

        SubprocessoDto result = subprocessoService.homologarValidacao(id, "usuario");

        assertNotNull(result);
        assertEquals("MAPA_HOMOLOGADO", subprocesso.getSituacaoId());
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
        subprocesso.setSituacaoId("REVISAO_CADASTRO_HOMOLOGADA");
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(new SubprocessoDto());

        SubprocessoDto result = subprocessoService.salvarAjustesMapa(id, new ArrayList<>(), "usuario");

        assertNotNull(result);
        assertEquals("MAPA_AJUSTADO", subprocesso.getSituacaoId());
        verify(repositorioSubprocesso, times(1)).save(subprocesso);
    }

    @Test
    void salvarAjustesMapa_deveLancarExcecao_quandoSituacaoInvalida() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        subprocesso.setSituacaoId("INVALIDA");
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
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(new SubprocessoDto());

        SubprocessoDto result = subprocessoService.submeterMapaAjustado(id, "usuario");

        assertNotNull(result);
        assertEquals("MAPA_AJUSTADO", subprocesso.getSituacaoId());
        verify(repositorioSubprocesso, times(1)).save(subprocesso);
        verify(repositorioMovimentacao, times(1)).save(any(Movimentacao.class));
    }

    @Test
    void devolverCadastro_deveExecutarComSucesso_quandoSubprocessoValido() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        subprocesso.setProcesso(criarProcessoMock());
        subprocesso.setSituacaoId("CADASTRO_DISPONIBILIZADO");
        Unidade unidade = criarUnidadeMock(10L, "UNI", "Unidade");
        Unidade unidadeSuperior = criarUnidadeMock(20L, "SUPER", "Unidade Superior");
        unidade.setUnidadeSuperior(unidadeSuperior);
        subprocesso.setUnidade(unidade);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(new SubprocessoDto());

        SubprocessoDto result = subprocessoService.devolverCadastro(id, "motivo", "observacoes", "usuario");

        assertNotNull(result);
        assertEquals("CADASTRO_EM_ELABORACAO", subprocesso.getSituacaoId());
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
        subprocesso.setSituacaoId("CADASTRO_DISPONIBILIZADO");
        Unidade unidade = criarUnidadeMock(10L, "UNI", "Unidade");
        Unidade unidadeSuperior = criarUnidadeMock(20L, "SUPER", "Unidade Superior");
        unidade.setUnidadeSuperior(unidadeSuperior);
        subprocesso.setUnidade(unidade);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(new SubprocessoDto());

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
        subprocesso.setSituacaoId("INVALIDA");
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
        subprocesso.setSituacaoId("CADASTRO_DISPONIBILIZADO");
        Unidade unidade = criarUnidadeMock(10L, "UNI", "Unidade");
        Unidade unidadeSuperior = criarUnidadeMock(20L, "SUPER", "Unidade Superior");
        unidade.setUnidadeSuperior(unidadeSuperior);
        subprocesso.setUnidade(unidade);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(new SubprocessoDto());

        SubprocessoDto result = subprocessoService.homologarCadastro(id, "observacoes", "usuario");

        assertNotNull(result);
        assertEquals("CADASTRO_HOMOLOGADO", subprocesso.getSituacaoId());
        verify(repositorioSubprocesso, times(1)).save(subprocesso);
    }

    @Test
    void devolverRevisaoCadastro_deveExecutarComSucesso_quandoSubprocessoValido() {
        Long id = 1L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(id);
        subprocesso.setProcesso(criarProcessoMock());
        subprocesso.setSituacaoId("REVISAO_CADASTRO_DISPONIBILIZADA");
        Unidade unidade = criarUnidadeMock(10L, "UNI", "Unidade");
        unidade.setUnidadeSuperior(criarUnidadeMock(20L, "SUPER", "Unidade Superior"));
        subprocesso.setUnidade(unidade);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(new SubprocessoDto());

        SubprocessoDto result = subprocessoService.devolverRevisaoCadastro(id, "motivo", "observacoes", "usuario");

        assertNotNull(result);
        assertEquals("REVISAO_CADASTRO_EM_ANDAMENTO", subprocesso.getSituacaoId());
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
        subprocesso.setSituacaoId("REVISAO_CADASTRO_DISPONIBILIZADA");
        Unidade unidade = criarUnidadeMock(10L, "UNI", "Unidade");
        unidade.setUnidadeSuperior(criarUnidadeMock(20L, "SUPER", "Unidade Superior"));
        subprocesso.setUnidade(unidade);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(new SubprocessoDto());

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
        subprocesso.setSituacaoId("REVISAO_CADASTRO_DISPONIBILIZADA");
        Unidade unidade = criarUnidadeMock(10L, "UNI", "Unidade");
        unidade.setUnidadeSuperior(criarUnidadeMock(20L, "SUPER", "Unidade Superior"));
        subprocesso.setUnidade(unidade);
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(subprocesso));
        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenReturn(new SubprocessoDto());

        SubprocessoDto result = subprocessoService.homologarRevisaoCadastro(id, "observacoes", "usuario");

        assertNotNull(result);
        assertEquals("REVISAO_CADASTRO_HOMOLOGADA", subprocesso.getSituacaoId());
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
        subprocesso.setProcesso(criarProcessoMock());
        subprocesso.setUnidade(criarUnidadeMock(10L, "UNI", "Unidade"));
        subprocesso.setMapa(new Mapa());
        subprocesso.getMapa().setCodigo(5L);
        return subprocesso;
    }

    private Subprocesso criarSubprocessoComMapa(Long id) {
        Subprocesso subprocesso = criarSubprocessoMock(id);
        subprocesso.setMapa(new Mapa());
        subprocesso.getMapa().setCodigo(5L);
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