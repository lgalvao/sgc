package sgc.alerta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.modelo.Alerta;
import sgc.alerta.modelo.AlertaRepo;
import sgc.alerta.modelo.AlertaUsuario;
import sgc.alerta.modelo.AlertaUsuarioRepo;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.processo.modelo.Processo;
import sgc.sgrh.SgrhService;
import sgc.sgrh.dto.UnidadeDto;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sgc.alerta.modelo.TipoAlerta.CADASTRO_DISPONIBILIZADO;

@Nested
@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {
    @Mock
    private AlertaRepo repositorioAlerta;

    @Mock
    private AlertaUsuarioRepo repositorioAlertaUsuario;

    @Mock
    private UnidadeRepo repositorioUnidade;

    @Mock
    private SgrhService servicoSgrh;

    @InjectMocks
    private AlertaService alertaService;

    private Processo processo;
    private Subprocesso subprocesso;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo de Teste");

        unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("UNID-TESTE");

        subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        subprocesso.setDataLimiteEtapa1(LocalDate.of(2025, 12, 31));
    }

    @Test
    @DisplayName("Deve criar alerta para unidade OPERACIONAL ao iniciar processo")
    void criarAlertasProcessoIniciado_deveCriarAlertaParaUnidadeOperacional() {
        UnidadeDto unidadeDto = new UnidadeDto(unidade.getCodigo(), "Unidade de Teste", "UNID-TESTE", 1L, "OPERACIONAL");
        when(servicoSgrh.buscarUnidadePorCodigo(unidade.getCodigo())).thenReturn(Optional.of(unidadeDto));
        when(repositorioUnidade.findById(unidade.getCodigo())).thenReturn(Optional.of(unidade));
        when(repositorioAlerta.save(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        alertaService.criarAlertasProcessoIniciado(processo, List.of(subprocesso.getUnidade().getCodigo()), List.of(subprocesso));

        ArgumentCaptor<Alerta> alertaCaptor = ArgumentCaptor.forClass(Alerta.class);
        verify(repositorioAlerta, times(1)).save(alertaCaptor.capture());

        Alerta alertaSalvo = alertaCaptor.getValue();
        String descricaoEsperada = "Início do processo 'Processo de Teste'. Preencha as atividades e conhecimentos até 31/12/2025.";
        assertEquals(descricaoEsperada, alertaSalvo.getDescricao());
    }

    @Test
    @DisplayName("Deve criar dois alertas para unidade INTEROPERACIONAL ao iniciar processo")
    void criarAlertasProcessoIniciado_deveCriarDoisAlertasParaUnidadeInteroperacional() {
        UnidadeDto unidadeDto = new UnidadeDto(unidade.getCodigo(), "Unidade de Teste", "UNID-TESTE", 1L, "INTEROPERACIONAL");
        when(servicoSgrh.buscarUnidadePorCodigo(unidade.getCodigo())).thenReturn(Optional.of(unidadeDto));
        when(repositorioUnidade.findById(unidade.getCodigo())).thenReturn(Optional.of(unidade));
        when(repositorioAlerta.save(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        alertaService.criarAlertasProcessoIniciado(processo, List.of(subprocesso.getUnidade().getCodigo()), List.of(subprocesso));

        ArgumentCaptor<Alerta> alertaCaptor = ArgumentCaptor.forClass(Alerta.class);
        verify(repositorioAlerta, times(2)).save(alertaCaptor.capture());

        List<Alerta> alertasSalvos = alertaCaptor.getAllValues();
        String descOperacional = "Início do processo 'Processo de Teste'. Preencha as atividades e conhecimentos até 31/12/2025.";
        String descIntermediaria = "Início do processo 'Processo de Teste' em unidade(s) subordinada(s). Aguarde a disponibilização dos mapas para validação até 31/12/2025.";

        assertTrue(alertasSalvos.stream().anyMatch(a -> a.getDescricao().equals(descOperacional)));
        assertTrue(alertasSalvos.stream().anyMatch(a -> a.getDescricao().equals(descIntermediaria)));
    }

    @Test
    @DisplayName("Não deve criar alerta para tipo de unidade desconhecido")
    void criarAlertasProcessoIniciado_naoDeveCriarAlertaParaTipoDesconhecido() {
        alertaService.criarAlertasProcessoIniciado(processo, List.of(subprocesso.getUnidade().getCodigo()), List.of(subprocesso));
        verify(repositorioAlerta, never()).save(any(Alerta.class));
    }

    @Test
    @DisplayName("Deve marcar alerta como lido com sucesso")
    void marcarComoLido_deveMarcarComoLido() {
        Long alertaId = 1L;
        String usuarioTituloStr = "123456789012";
        Long usuarioTitulo = Long.parseLong(usuarioTituloStr);
        AlertaUsuario.Chave id = new AlertaUsuario.Chave(alertaId, usuarioTitulo);
        AlertaUsuario alertaUsuario = new AlertaUsuario();
        alertaUsuario.setId(id);
        alertaUsuario.setDataHoraLeitura(null);

        when(repositorioAlertaUsuario.findById(id)).thenReturn(Optional.of(alertaUsuario));

        alertaService.marcarComoLido(usuarioTituloStr, alertaId);

        ArgumentCaptor<AlertaUsuario> captor = ArgumentCaptor.forClass(AlertaUsuario.class);
        verify(repositorioAlertaUsuario).save(captor.capture());
        AlertaUsuario alertaUsuarioSalvo = captor.getValue();
        assertNotNull(alertaUsuarioSalvo.getDataHoraLeitura());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar marcar como lido alerta inexistente")
    void marcarComoLido_deveLancarExcecaoSeAlertaNaoEncontrado() {
        Long alertaId = 1L;
        String usuarioTituloStr = "123456789012";
        Long usuarioTitulo = Long.parseLong(usuarioTituloStr);
        AlertaUsuario.Chave id = new AlertaUsuario.Chave(alertaId, usuarioTitulo);
        when(repositorioAlertaUsuario.findById(id)).thenReturn(Optional.empty());

        assertThrows(ErroDominioNaoEncontrado.class, () -> alertaService.marcarComoLido(usuarioTituloStr, alertaId));
    }

    @Test
    @DisplayName("Deve criar alerta de cadastro disponibilizado com data nula")
    void criarAlertaCadastroDisponibilizado_deveFormatarDataNulaCorretamente() {
        Long unidadeOrigemCodigo = 20L;
        Long unidadeDestinoCodigo = 30L;
        Unidade unidadeOrigem = new Unidade();
        unidadeOrigem.setCodigo(unidadeOrigemCodigo);
        unidadeOrigem.setSigla("ORIGEM");

        when(repositorioUnidade.findById(unidadeOrigemCodigo)).thenReturn(Optional.of(unidadeOrigem));
        when(repositorioUnidade.findById(unidadeDestinoCodigo)).thenReturn(Optional.of(new Unidade()));
        when(repositorioAlerta.save(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        alertaService.criarAlertaCadastroDisponibilizado(processo, unidadeOrigemCodigo, unidadeDestinoCodigo);

        ArgumentCaptor<Alerta> alertaCaptor = ArgumentCaptor.forClass(Alerta.class);
        verify(repositorioAlerta).save(alertaCaptor.capture());

        // A asserção principal aqui é que o metodo não lança exceção com data nula.
        // A formatação da data em si é privada, mas seu efeito na descrição pode ser verificado.
        String descricaoEsperada = "Cadastro disponibilizado pela unidade ORIGEM no processo 'Processo de Teste'. Realize a análise do cadastro.";
        Alerta alertaSalvo = alertaCaptor.getValue();
        assertEquals(descricaoEsperada, alertaSalvo.getDescricao());
    }

    @Test
    @DisplayName("Deve criar alerta de cadastro devolvido com o motivo correto")
    void criarAlertaCadastroDevolvido_deveCriarAlertaComMotivo() {
        Long unidadeDestinoCodigo = 30L;
        String motivo = "Informações incompletas";

        when(repositorioUnidade.findById(unidadeDestinoCodigo)).thenReturn(Optional.of(new Unidade()));
        when(repositorioAlerta.save(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        alertaService.criarAlertaCadastroDevolvido(processo, unidadeDestinoCodigo, motivo);

        ArgumentCaptor<Alerta> alertaCaptor = ArgumentCaptor.forClass(Alerta.class);
        verify(repositorioAlerta).save(alertaCaptor.capture());

        String descricaoEsperada = "Cadastro devolvido no processo 'Processo de Teste'. Motivo: Informações incompletas. Realize os ajustes necessários e disponibilize novamente.";
        Alerta alertaSalvo = alertaCaptor.getValue();
        assertEquals(descricaoEsperada, alertaSalvo.getDescricao());
    }

    @Test
    @DisplayName("Deve ignorar subprocesso sem unidade ao criar alertas")
    void criarAlertasProcessoIniciado_deveIgnorarSubprocessoSemUnidade() {
        Subprocesso subprocessoSemUnidade = new Subprocesso();
        subprocessoSemUnidade.setCodigo(101L);

        alertaService.criarAlertasProcessoIniciado(processo, List.of(), List.of(subprocessoSemUnidade));

        verify(servicoSgrh, never()).buscarUnidadePorCodigo(anyLong());
        verify(repositorioAlerta, never()).save(any(Alerta.class));
    }

    @Test
    @DisplayName("Deve ignorar unidade não encontrada no SGRH ao criar alertas")
    void criarAlertasProcessoIniciado_deveIgnorarUnidadeNaoEncontradaNoSGRH() {
        when(servicoSgrh.buscarUnidadePorCodigo(unidade.getCodigo())).thenReturn(Optional.empty());

        alertaService.criarAlertasProcessoIniciado(processo, List.of(subprocesso.getUnidade().getCodigo()), List.of(subprocesso));

        verify(repositorioAlerta, never()).save(any(Alerta.class));
    }

    @Test
    @DisplayName("Deve criar alerta mesmo se SGRH falhar ao buscar responsável")
    void criarAlerta_deveContinuarSeSGRHFalhar() {
        Long codUnidadeDestino = 30L;
        when(repositorioUnidade.findById(codUnidadeDestino)).thenReturn(Optional.of(new Unidade()));
        when(servicoSgrh.buscarResponsavelUnidade(codUnidadeDestino)).thenThrow(new RuntimeException("Erro de comunicação com SGRH"));
        when(repositorioAlerta.save(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Apenas chama o metodo. O teste passa se nenhuma exceção for lançada.
        assertDoesNotThrow(() -> {
            alertaService.criarAlerta(processo, CADASTRO_DISPONIBILIZADO, codUnidadeDestino, "Descrição", null);
        });

        // Verifica que o alerta principal foi salvo, mas nenhum AlertaUsuario foi criado
        verify(repositorioAlerta, times(1)).save(any(Alerta.class));
        verify(repositorioAlertaUsuario, never()).save(any(AlertaUsuario.class));
    }
}