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
import sgc.alerta.modelo.Alerta;
import sgc.alerta.modelo.AlertaRepo;
import sgc.analise.modelo.AnaliseCadastro;
import sgc.analise.modelo.AnaliseCadastroRepo;
import sgc.analise.modelo.AnaliseValidacao;
import sgc.analise.modelo.AnaliseValidacaoRepo;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.notificacao.NotificacaoService;
import sgc.processo.modelo.Processo;
import sgc.subprocesso.SubprocessoService;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.dto.SubprocessoMapper;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SubprocessoServiceActionsTest {
    @Mock
    private SubprocessoRepo subprocessoRepo;

    @Mock
    private MovimentacaoRepo movimentacaoRepo;

    @Mock
    private AnaliseCadastroRepo analiseCadastroRepo;

    @Mock
    private AnaliseValidacaoRepo analiseValidacaoRepo;

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private AlertaRepo alertaRepo;

    @Mock
    private SubprocessoMapper subprocessoMapper;

    @InjectMocks
    private SubprocessoService subprocessoService;

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
        subprocesso.setProcesso(new Processo());
        subprocesso.getProcesso().setDescricao("Processo Teste");

        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenAnswer(inv -> {
            Subprocesso sp = inv.getArgument(0);
            SubprocessoDto dto = new SubprocessoDto();
            dto.setCodigo(sp.getCodigo());
            dto.setSituacaoId(sp.getSituacaoId());
            return dto;
        });
    }

    @Test
    void aceitarCadastro_deveSalvarAnaliseEMovimentacao_quandoValido() {
        subprocesso.setSituacaoId("CADASTRO_DISPONIBILIZADO");
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
        when(analiseCadastroRepo.save(any(AnaliseCadastro.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimentacaoRepo.save(any(Movimentacao.class))).thenAnswer(inv -> inv.getArgument(0));

        SubprocessoDto result = subprocessoService.aceitarCadastro(1L, "Observações", usuarioTitulo);

        assertNotNull(result);
        assertEquals("CADASTRO_DISPONIBILIZADO", result.getSituacaoId());

        ArgumentCaptor<AnaliseCadastro> analiseCaptor = ArgumentCaptor.forClass(AnaliseCadastro.class);
        verify(analiseCadastroRepo, times(1)).save(analiseCaptor.capture());
        assertEquals("ACEITE", analiseCaptor.getValue().getAcao());
        assertEquals("Observações", analiseCaptor.getValue().getObservacoes());
        assertEquals(usuarioTitulo, analiseCaptor.getValue().getAnalistaUsuarioTitulo());

        ArgumentCaptor<Movimentacao> movCaptor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoRepo, times(1)).save(movCaptor.capture());
        assertEquals("Cadastro de atividades e conhecimentos aceito", movCaptor.getValue().getDescricao());
        assertEquals(unidadeSubordinada, movCaptor.getValue().getUnidadeOrigem());
        assertEquals(unidadeSuperior, movCaptor.getValue().getUnidadeDestino());

        verify(notificacaoService, times(1)).enviarEmail(eq(unidadeSuperior.getSigla()), anyString(), anyString());
        verify(alertaRepo, times(1)).save(any(Alerta.class));
    }

    @Test
    void aceitarCadastro_deveLancarExcecao_quandoSubprocessoNaoEncontrado() {
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ErroDominioNaoEncontrado.class, () -> subprocessoService.aceitarCadastro(1L, "Observações", usuarioTitulo));
    }

    @Test
    void aceitarCadastro_deveLancarExcecao_quandoSituacaoInvalida() {
        subprocesso.setSituacaoId("OUTRA_SITUACAO");
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
        assertThrows(IllegalStateException.class, () -> subprocessoService.aceitarCadastro(1L, "Observações", usuarioTitulo));
    }

    @Test
    void aceitarCadastro_deveLancarExcecao_quandoUnidadeSuperiorNaoEncontrada() {
        unidadeSubordinada.setUnidadeSuperior(null);
        subprocesso.setSituacaoId("CADASTRO_DISPONIBILIZADO");
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
        assertThrows(IllegalStateException.class, () -> subprocessoService.aceitarCadastro(1L, "Observações", usuarioTitulo));
    }

    @Test
    void homologarCadastro_deveMudarSituacaoESalvarMovimentacao_quandoValido() {
        subprocesso.setSituacaoId("CADASTRO_DISPONIBILIZADO");
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
        when(subprocessoRepo.save(any(Subprocesso.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimentacaoRepo.save(any(Movimentacao.class))).thenAnswer(inv -> inv.getArgument(0));

        SubprocessoDto result = subprocessoService.homologarCadastro(1L, "Observações", usuarioTitulo);

        assertNotNull(result);
        assertEquals("CADASTRO_HOMOLOGADO", result.getSituacaoId());

        ArgumentCaptor<Movimentacao> movCaptor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoRepo, times(1)).save(movCaptor.capture());
        assertEquals("Cadastro de atividades e conhecimentos homologado", movCaptor.getValue().getDescricao());
        assertEquals(unidadeSuperior, movCaptor.getValue().getUnidadeOrigem());
        assertEquals(unidadeSuperior, movCaptor.getValue().getUnidadeDestino());

        verify(subprocessoRepo, times(1)).save(subprocesso);
    }

    @Test
    void homologarCadastro_deveLancarExcecao_quandoSubprocessoNaoEncontrado() {
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ErroDominioNaoEncontrado.class, () -> subprocessoService.homologarCadastro(1L, "Observações", usuarioTitulo));
    }

    @Test
    void homologarCadastro_deveLancarExcecao_quandoSituacaoInvalida() {
        subprocesso.setSituacaoId("OUTRA_SITUACAO");
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
        assertThrows(IllegalStateException.class, () -> subprocessoService.homologarCadastro(1L, "Observações", usuarioTitulo));
    }

    @Test
    void aceitarRevisaoCadastro_deveSalvarAnaliseEMovimentacao_quandoValido() {
        subprocesso.setSituacaoId("REVISAO_CADASTRO_DISPONIBILIZADA");
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
        when(analiseCadastroRepo.save(any(AnaliseCadastro.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimentacaoRepo.save(any(Movimentacao.class))).thenAnswer(inv -> inv.getArgument(0));

        SubprocessoDto result = subprocessoService.aceitarRevisaoCadastro(1L, "Observações", usuarioTitulo);

        assertNotNull(result);
        assertEquals("REVISAO_CADASTRO_DISPONIBILIZADA", result.getSituacaoId());

        ArgumentCaptor<AnaliseCadastro> analiseCaptor = ArgumentCaptor.forClass(AnaliseCadastro.class);
        verify(analiseCadastroRepo, times(1)).save(analiseCaptor.capture());
        assertEquals("ACEITE_REVISAO", analiseCaptor.getValue().getAcao());
        assertEquals("Observações", analiseCaptor.getValue().getObservacoes());
        assertEquals(usuarioTitulo, analiseCaptor.getValue().getAnalistaUsuarioTitulo());

        ArgumentCaptor<Movimentacao> movCaptor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoRepo, times(1)).save(movCaptor.capture());
        assertEquals("Revisão do cadastro de atividades e conhecimentos aceita", movCaptor.getValue().getDescricao());
        assertEquals(unidadeSubordinada, movCaptor.getValue().getUnidadeOrigem());
        assertEquals(unidadeSuperior, movCaptor.getValue().getUnidadeDestino());

        verify(notificacaoService, times(1)).enviarEmail(eq(unidadeSuperior.getSigla()), anyString(), anyString());
        verify(alertaRepo, times(1)).save(any(Alerta.class));
    }

    @Test
    void aceitarRevisaoCadastro_deveLancarExcecao_quandoSubprocessoNaoEncontrado() {
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ErroDominioNaoEncontrado.class, () -> subprocessoService.aceitarRevisaoCadastro(1L, "Observações", usuarioTitulo));
    }

    @Test
    void aceitarRevisaoCadastro_deveLancarExcecao_quandoSituacaoInvalida() {
        subprocesso.setSituacaoId("OUTRA_SITUACAO");
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
        assertThrows(IllegalStateException.class, () -> subprocessoService.aceitarRevisaoCadastro(1L, "Observações", usuarioTitulo));
    }

    @Test
    void homologarRevisaoCadastro_deveMudarSituacaoESalvarMovimentacao_quandoValido() {
        subprocesso.setSituacaoId("REVISAO_CADASTRO_DISPONIBILIZADA");
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
        when(subprocessoRepo.save(any(Subprocesso.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimentacaoRepo.save(any(Movimentacao.class))).thenAnswer(inv -> inv.getArgument(0));

        SubprocessoDto result = subprocessoService.homologarRevisaoCadastro(1L, "Observações", usuarioTitulo);

        assertNotNull(result);
        assertEquals("REVISAO_CADASTRO_HOMOLOGADA", result.getSituacaoId());

        ArgumentCaptor<Movimentacao> movCaptor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoRepo, times(1)).save(movCaptor.capture());
        assertEquals("Revisão do cadastro de atividades e conhecimentos homologada", movCaptor.getValue().getDescricao());
        assertEquals(unidadeSuperior, movCaptor.getValue().getUnidadeOrigem());
        assertEquals(unidadeSuperior, movCaptor.getValue().getUnidadeDestino());

        verify(subprocessoRepo, times(1)).save(subprocesso);
    }

    @Test
    void homologarRevisaoCadastro_deveLancarExcecao_quandoSubprocessoNaoEncontrado() {
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ErroDominioNaoEncontrado.class, () -> subprocessoService.homologarRevisaoCadastro(1L, "Observações", usuarioTitulo));
    }

    @Test
    void homologarRevisaoCadastro_deveLancarExcecao_quandoSituacaoInvalida() {
        subprocesso.setSituacaoId("OUTRA_SITUACAO");
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
        assertThrows(IllegalStateException.class, () -> subprocessoService.homologarRevisaoCadastro(1L, "Observações", usuarioTitulo));
    }

    @Test
    void devolverCadastro_deveMudarSituacaoESalvarAnaliseEMovimentacao_quandoValido() {
        subprocesso.setSituacaoId("CADASTRO_DISPONIBILIZADO");
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
        when(analiseCadastroRepo.save(any(AnaliseCadastro.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimentacaoRepo.save(any(Movimentacao.class))).thenAnswer(inv -> inv.getArgument(0));
        when(subprocessoRepo.save(any(Subprocesso.class))).thenAnswer(inv -> inv.getArgument(0));

        SubprocessoDto result = subprocessoService.devolverCadastro(1L, "Motivo Teste", "Observações", usuarioTitulo);

        assertNotNull(result);
        assertEquals("CADASTRO_EM_ELABORACAO", result.getSituacaoId());
        assertNull(subprocesso.getDataFimEtapa1());

        ArgumentCaptor<AnaliseCadastro> analiseCaptor = ArgumentCaptor.forClass(AnaliseCadastro.class);
        verify(analiseCadastroRepo, times(1)).save(analiseCaptor.capture());
        assertTrue(analiseCaptor.getValue().getObservacoes().contains("Motivo Teste"));
        assertTrue(analiseCaptor.getValue().getObservacoes().contains("Observações"));

        ArgumentCaptor<Movimentacao> movCaptor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoRepo, times(1)).save(movCaptor.capture());
        assertTrue(movCaptor.getValue().getDescricao().contains("Devolução do cadastro de atividades"));
        assertEquals(unidadeSubordinada, movCaptor.getValue().getUnidadeDestino());

        verify(notificacaoService, times(1)).enviarEmail(eq(unidadeSubordinada.getSigla()), anyString(), anyString());
        verify(alertaRepo, times(1)).save(any(Alerta.class));
    }

    @Test
    void devolverValidacao_deveMudarSituacaoESalvarAnalise_quandoValido() {
        subprocesso.setSituacaoId("MAPA_VALIDADO");
        unidadeSuperior.setUnidadeSuperior(new Unidade());
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
        when(analiseValidacaoRepo.save(any(AnaliseValidacao.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimentacaoRepo.save(any(Movimentacao.class))).thenAnswer(inv -> inv.getArgument(0));
        when(subprocessoRepo.save(any(Subprocesso.class))).thenAnswer(inv -> inv.getArgument(0));

        SubprocessoDto result = subprocessoService.devolverValidacao(1L, "Justificativa Teste", usuarioTitulo);

        assertNotNull(result);
        assertEquals("MAPA_DISPONIBILIZADO", result.getSituacaoId());
        assertNull(subprocesso.getDataFimEtapa2());

        ArgumentCaptor<AnaliseValidacao> analiseCaptor = ArgumentCaptor.forClass(AnaliseValidacao.class);
        verify(analiseValidacaoRepo, times(1)).save(analiseCaptor.capture());
        assertEquals("Justificativa Teste", analiseCaptor.getValue().getObservacoes());

        ArgumentCaptor<Movimentacao> movCaptor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoRepo, times(1)).save(movCaptor.capture());
        assertEquals("Devolução da validação do mapa de competências para ajustes", movCaptor.getValue().getDescricao());
        assertEquals(unidadeSubordinada, movCaptor.getValue().getUnidadeDestino());

        verify(notificacaoService, times(1)).enviarEmail(eq(unidadeSubordinada.getSigla()), anyString(), anyString());
        verify(alertaRepo, times(1)).save(any(Alerta.class));
    }
}