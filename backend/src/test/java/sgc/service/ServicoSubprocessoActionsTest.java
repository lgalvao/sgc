package sgc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sgc.alerta.AlertaRepository;
import sgc.atividade.AnaliseCadastro;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.notificacao.ServicoNotificacao;
import sgc.subprocesso.*;
import sgc.atividade.AnaliseValidacao;
import sgc.subprocesso.AnaliseValidacaoRepository;
import sgc.unidade.Unidade;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ServicoSubprocessoActionsTest {

    @Mock
    private SubprocessoRepository subprocessoRepository;
    @Mock
    private MovimentacaoRepository movimentacaoRepository;
    @Mock
    private AnaliseCadastroRepository analiseCadastroRepository;
    @Mock
    private AnaliseValidacaoRepository analiseValidacaoRepository;
    @Mock
    private ServicoNotificacao servicoNotificacao;
    @Mock
    private AlertaRepository alertaRepository;
    @Mock
    private SubprocessoMapper subprocessoMapper;

    @InjectMocks
    private ServicoSubprocesso servicoSubprocesso;

    private Subprocesso subprocesso;
    private Unidade unidadeSubordinada;
    private Unidade unidadeSuperior;
    private final String usuarioTitulo = "USUARIO_TESTE";

    @BeforeEach
    void setUp() {
        unidadeSubordinada = new Unidade();
        unidadeSubordinada.setCodigo(10L);
        unidadeSubordinada.setSigla("UNID_SUB");
        unidadeSubordinada.setNome("Unidade Subordinada");

        unidadeSuperior = new Unidade();
        unidadeSuperior.setCodigo(20L);
        unidadeSuperior.setSigla("UNID_SUP");
        unidadeSuperior.setNome("Unidade Superior");
        unidadeSubordinada.setUnidadeSuperior(unidadeSuperior);

        subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        subprocesso.setUnidade(unidadeSubordinada);
        subprocesso.setSituacaoId("CADASTRO_DISPONIBILIZADO");
        subprocesso.setProcesso(new sgc.processo.Processo());
        subprocesso.getProcesso().setDescricao("Processo Teste");

        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenAnswer(inv -> {
            Subprocesso sp = inv.getArgument(0);
            SubprocessoDTO dto = new SubprocessoDTO();
            dto.setCodigo(sp.getCodigo());
            dto.setSituacaoId(sp.getSituacaoId());
            return dto;
        });
    }

    @Test
    void aceitarCadastro_deveSalvarAnaliseEMovimentacao_quandoValido() {
        subprocesso.setSituacaoId("CADASTRO_DISPONIBILIZADO");
        when(subprocessoRepository.findById(1L)).thenReturn(Optional.of(subprocesso));
        when(analiseCadastroRepository.save(any(AnaliseCadastro.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(inv -> inv.getArgument(0));

        SubprocessoDTO result = servicoSubprocesso.aceitarCadastro(1L, "Observações", usuarioTitulo);

        assertNotNull(result);
        assertEquals("CADASTRO_DISPONIBILIZADO", result.getSituacaoId());

        ArgumentCaptor<AnaliseCadastro> analiseCaptor = ArgumentCaptor.forClass(AnaliseCadastro.class);
        verify(analiseCadastroRepository, times(1)).save(analiseCaptor.capture());
        assertEquals("ACEITE", analiseCaptor.getValue().getAcao());
        assertEquals("Observações", analiseCaptor.getValue().getObservacoes());
        assertEquals(usuarioTitulo, analiseCaptor.getValue().getAnalistaUsuarioTitulo());

        ArgumentCaptor<Movimentacao> movCaptor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoRepository, times(1)).save(movCaptor.capture());
        assertEquals("Cadastro de atividades e conhecimentos aceito", movCaptor.getValue().getDescricao());
        assertEquals(unidadeSubordinada, movCaptor.getValue().getUnidadeOrigem());
        assertEquals(unidadeSuperior, movCaptor.getValue().getUnidadeDestino());

        verifyNoInteractions(servicoNotificacao);
        verifyNoInteractions(alertaRepository);
    }

    @Test
    void aceitarCadastro_deveLancarExcecao_quandoSubprocessoNaoEncontrado() {
        when(subprocessoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ErroDominioNaoEncontrado.class, () -> servicoSubprocesso.aceitarCadastro(1L, "Observações", usuarioTitulo));
    }

    @Test
    void aceitarCadastro_deveLancarExcecao_quandoSituacaoInvalida() {
        subprocesso.setSituacaoId("OUTRA_SITUACAO");
        when(subprocessoRepository.findById(1L)).thenReturn(Optional.of(subprocesso));
        assertThrows(IllegalStateException.class, () -> servicoSubprocesso.aceitarCadastro(1L, "Observações", usuarioTitulo));
    }

    @Test
    void aceitarCadastro_deveLancarExcecao_quandoUnidadeSuperiorNaoEncontrada() {
        unidadeSubordinada.setUnidadeSuperior(null);
        subprocesso.setSituacaoId("CADASTRO_DISPONIBILIZADO");
        when(subprocessoRepository.findById(1L)).thenReturn(Optional.of(subprocesso));
        assertThrows(IllegalStateException.class, () -> servicoSubprocesso.aceitarCadastro(1L, "Observações", usuarioTitulo));
    }

    @Test
    void homologarCadastro_deveMudarSituacaoESalvarMovimentacao_quandoValido() {
        subprocesso.setSituacaoId("CADASTRO_DISPONIBILIZADO");
        when(subprocessoRepository.findById(1L)).thenReturn(Optional.of(subprocesso));
        when(subprocessoRepository.save(any(Subprocesso.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(inv -> inv.getArgument(0));

        SubprocessoDTO result = servicoSubprocesso.homologarCadastro(1L, "Observações", usuarioTitulo);

        assertNotNull(result);
        assertEquals("CADASTRO_HOMOLOGADO", result.getSituacaoId());

        ArgumentCaptor<Movimentacao> movCaptor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoRepository, times(1)).save(movCaptor.capture());
        assertEquals("Cadastro de atividades e conhecimentos homologado", movCaptor.getValue().getDescricao());
        assertEquals(unidadeSuperior, movCaptor.getValue().getUnidadeOrigem());
        assertEquals(unidadeSuperior, movCaptor.getValue().getUnidadeDestino());

        verify(subprocessoRepository, times(1)).save(subprocesso);
    }

    @Test
    void homologarCadastro_deveLancarExcecao_quandoSubprocessoNaoEncontrado() {
        when(subprocessoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ErroDominioNaoEncontrado.class, () -> servicoSubprocesso.homologarCadastro(1L, "Observações", usuarioTitulo));
    }

    @Test
    void homologarCadastro_deveLancarExcecao_quandoSituacaoInvalida() {
        subprocesso.setSituacaoId("OUTRA_SITUACAO");
        when(subprocessoRepository.findById(1L)).thenReturn(Optional.of(subprocesso));
        assertThrows(IllegalStateException.class, () -> servicoSubprocesso.homologarCadastro(1L, "Observações", usuarioTitulo));
    }

    @Test
    void aceitarRevisaoCadastro_deveSalvarAnaliseEMovimentacao_quandoValido() {
        subprocesso.setSituacaoId("REVISAO_CADASTRO_DISPONIBILIZADA");
        when(subprocessoRepository.findById(1L)).thenReturn(Optional.of(subprocesso));
        when(analiseCadastroRepository.save(any(AnaliseCadastro.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(inv -> inv.getArgument(0));

        SubprocessoDTO result = servicoSubprocesso.aceitarRevisaoCadastro(1L, "Observações", usuarioTitulo);

        assertNotNull(result);
        assertEquals("REVISAO_CADASTRO_DISPONIBILIZADA", result.getSituacaoId());

        ArgumentCaptor<AnaliseCadastro> analiseCaptor = ArgumentCaptor.forClass(AnaliseCadastro.class);
        verify(analiseCadastroRepository, times(1)).save(analiseCaptor.capture());
        assertEquals("ACEITE_REVISAO", analiseCaptor.getValue().getAcao());
        assertEquals("Observações", analiseCaptor.getValue().getObservacoes());
        assertEquals(usuarioTitulo, analiseCaptor.getValue().getAnalistaUsuarioTitulo());

        ArgumentCaptor<Movimentacao> movCaptor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoRepository, times(1)).save(movCaptor.capture());
        assertEquals("Revisão do cadastro de atividades e conhecimentos aceita", movCaptor.getValue().getDescricao());
        assertEquals(unidadeSubordinada, movCaptor.getValue().getUnidadeOrigem());
        assertEquals(unidadeSuperior, movCaptor.getValue().getUnidadeDestino());

        verifyNoInteractions(servicoNotificacao);
        verifyNoInteractions(alertaRepository);
    }

    @Test
    void aceitarRevisaoCadastro_deveLancarExcecao_quandoSubprocessoNaoEncontrado() {
        when(subprocessoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ErroDominioNaoEncontrado.class, () -> servicoSubprocesso.aceitarRevisaoCadastro(1L, "Observações", usuarioTitulo));
    }

    @Test
    void aceitarRevisaoCadastro_deveLancarExcecao_quandoSituacaoInvalida() {
        subprocesso.setSituacaoId("OUTRA_SITUACAO");
        when(subprocessoRepository.findById(1L)).thenReturn(Optional.of(subprocesso));
        assertThrows(IllegalStateException.class, () -> servicoSubprocesso.aceitarRevisaoCadastro(1L, "Observações", usuarioTitulo));
    }

    @Test
    void homologarRevisaoCadastro_deveMudarSituacaoESalvarMovimentacao_quandoValido() {
        subprocesso.setSituacaoId("REVISAO_CADASTRO_DISPONIBILIZADA");
        when(subprocessoRepository.findById(1L)).thenReturn(Optional.of(subprocesso));
        when(subprocessoRepository.save(any(Subprocesso.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(inv -> inv.getArgument(0));

        SubprocessoDTO result = servicoSubprocesso.homologarRevisaoCadastro(1L, "Observações", usuarioTitulo);

        assertNotNull(result);
        assertEquals("REVISAO_CADASTRO_HOMOLOGADA", result.getSituacaoId());

        ArgumentCaptor<Movimentacao> movCaptor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoRepository, times(1)).save(movCaptor.capture());
        assertEquals("Revisão do cadastro de atividades e conhecimentos homologada", movCaptor.getValue().getDescricao());
        assertEquals(unidadeSuperior, movCaptor.getValue().getUnidadeOrigem());
        assertEquals(unidadeSuperior, movCaptor.getValue().getUnidadeDestino());

        verify(subprocessoRepository, times(1)).save(subprocesso);
    }

    @Test
    void homologarRevisaoCadastro_deveLancarExcecao_quandoSubprocessoNaoEncontrado() {
        when(subprocessoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ErroDominioNaoEncontrado.class, () -> servicoSubprocesso.homologarRevisaoCadastro(1L, "Observações", usuarioTitulo));
    }

    @Test
    void homologarRevisaoCadastro_deveLancarExcecao_quandoSituacaoInvalida() {
        subprocesso.setSituacaoId("OUTRA_SITUACAO");
        when(subprocessoRepository.findById(1L)).thenReturn(Optional.of(subprocesso));
        assertThrows(IllegalStateException.class, () -> servicoSubprocesso.homologarRevisaoCadastro(1L, "Observações", usuarioTitulo));
    }

    @Test
    void devolverCadastro_deveMudarSituacaoESalvarAnaliseEMovimentacao_quandoValido() {
        subprocesso.setSituacaoId("CADASTRO_DISPONIBILIZADO");
        when(subprocessoRepository.findById(1L)).thenReturn(Optional.of(subprocesso));
        when(analiseCadastroRepository.save(any(AnaliseCadastro.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(inv -> inv.getArgument(0));
        when(subprocessoRepository.save(any(Subprocesso.class))).thenAnswer(inv -> inv.getArgument(0));

        SubprocessoDTO result = servicoSubprocesso.devolverCadastro(1L, "Motivo Teste", "Observações", usuarioTitulo);

        assertNotNull(result);
        assertEquals("CADASTRO_EM_ELABORACAO", result.getSituacaoId());
        assertNull(subprocesso.getDataFimEtapa1());

        ArgumentCaptor<AnaliseCadastro> analiseCaptor = ArgumentCaptor.forClass(AnaliseCadastro.class);
        verify(analiseCadastroRepository, times(1)).save(analiseCaptor.capture());
        assertTrue(analiseCaptor.getValue().getObservacoes().contains("Motivo Teste"));
        assertTrue(analiseCaptor.getValue().getObservacoes().contains("Observações"));

        ArgumentCaptor<Movimentacao> movCaptor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoRepository, times(1)).save(movCaptor.capture());
        assertTrue(movCaptor.getValue().getDescricao().contains("Devolução do cadastro de atividades"));
        assertEquals(unidadeSubordinada, movCaptor.getValue().getUnidadeDestino());

        verify(servicoNotificacao, times(1)).enviarEmail(eq(unidadeSubordinada.getSigla()), anyString(), anyString());
        verify(alertaRepository, times(1)).save(any(sgc.alerta.Alerta.class));
    }

    @Test
    void devolverValidacao_deveMudarSituacaoESalvarAnalise_quandoValido() {
        subprocesso.setSituacaoId("MAPA_VALIDADO");
        unidadeSuperior.setUnidadeSuperior(new Unidade());
        when(subprocessoRepository.findById(1L)).thenReturn(Optional.of(subprocesso));
        when(analiseValidacaoRepository.save(any(AnaliseValidacao.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(inv -> inv.getArgument(0));
        when(subprocessoRepository.save(any(Subprocesso.class))).thenAnswer(inv -> inv.getArgument(0));

        SubprocessoDTO result = servicoSubprocesso.devolverValidacao(1L, "Justificativa Teste", usuarioTitulo);

        assertNotNull(result);
        assertEquals("MAPA_DISPONIBILIZADO", result.getSituacaoId());
        assertNull(subprocesso.getDataFimEtapa2());

        ArgumentCaptor<AnaliseValidacao> analiseCaptor = ArgumentCaptor.forClass(AnaliseValidacao.class);
        verify(analiseValidacaoRepository, times(1)).save(analiseCaptor.capture());
        assertEquals("Justificativa Teste", analiseCaptor.getValue().getObservacoes());

        ArgumentCaptor<Movimentacao> movCaptor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoRepository, times(1)).save(movCaptor.capture());
        assertEquals("Devolução da validação do mapa de competências para ajustes", movCaptor.getValue().getDescricao());
        assertEquals(unidadeSubordinada, movCaptor.getValue().getUnidadeDestino());

        verify(servicoNotificacao, times(1)).enviarEmail(eq(unidadeSubordinada.getSigla()), anyString(), anyString());
        verify(alertaRepository, times(1)).save(any(sgc.alerta.Alerta.class));
    }
}