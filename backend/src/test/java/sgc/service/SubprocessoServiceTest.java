package sgc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.alerta.modelo.AlertaRepo;
import sgc.analise.modelo.AnaliseCadastroRepo;
import sgc.analise.modelo.AnaliseValidacaoRepo;
import sgc.atividade.dto.AtividadeMapper;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.comum.modelo.Usuario;
import sgc.conhecimento.dto.ConhecimentoMapper;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.mapa.modelo.Mapa;
import sgc.notificacao.NotificacaoService;
import sgc.notificacao.modelo.NotificacaoRepo;
import sgc.subprocesso.SubprocessoService;
import sgc.subprocesso.dto.MovimentacaoDto;
import sgc.subprocesso.dto.MovimentacaoMapper;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.dto.SubprocessoMapper;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubprocessoServiceTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private ConhecimentoRepo conhecimentoRepo;
    @Mock
    private AnaliseCadastroRepo repositorioAnaliseCadastro;
    @Mock
    private AnaliseValidacaoRepo repositorioAnaliseValidacao;
    @Mock
    private NotificacaoRepo repositorioNotificacao;
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
    void casoFeliz_retornaDetalhesComMovimentacoesEElementos() {
        Long spId = 1L;
        Unidade unidade = mock(Unidade.class);
        when(unidade.getCodigo()).thenReturn(10L);
        when(unidade.getNome()).thenReturn("Unidade X");
        when(unidade.getSigla()).thenReturn("UX");
        Usuario titular = new Usuario();
        titular.setTitulo("0001");
        titular.setNome("Titular X");
        titular.setEmail("titular@exemplo");
        titular.setRamal("1234");
        when(unidade.getTitular()).thenReturn(titular);

        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(spId);
        sp.setUnidade(unidade);
        sp.setMapa(mapa);
        sp.setSituacaoId("EM_ANDAMENTO");
        sp.setDataLimiteEtapa1(LocalDate.of(2025, 12, 31));

        Movimentacao mov = mock(Movimentacao.class);
        MovimentacaoDto movDto = new MovimentacaoDto(
                500L,
                LocalDateTime.now(),
                null, null, null,
                unidade.getCodigo(), unidade.getSigla(), unidade.getNome(),
                "Mov desc"
        );

        Atividade at = new Atividade();
        at.setCodigo(200L);
        at.setMapa(mapa);
        at.setDescricao("Atividade A");

        Conhecimento kc = new Conhecimento();
        kc.setCodigo(300L);
        kc.setAtividade(at);
        kc.setDescricao("Conhecimento 1");

        when(subprocessoRepo.findById(spId)).thenReturn(Optional.of(sp));
        when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(spId)).thenReturn(List.of(mov));
        when(movimentacaoMapper.toDTO(any(Movimentacao.class))).thenReturn(movDto);
        when(atividadeRepo.findByMapaCodigo(mapa.getCodigo())).thenReturn(List.of(at));
        when(conhecimentoRepo.findAll()).thenReturn(List.of(kc));

        SubprocessoDetalheDto dto = subprocessoService.obterDetalhes(spId, "ADMIN", null);

        assertNotNull(dto);
        assertNotNull(dto.getUnidade());
        assertEquals(unidade.getCodigo(), dto.getUnidade().getCodigo());
        assertEquals("EM_ANDAMENTO", dto.getSituacao());
        assertNotNull(dto.getMovimentacoes());
        assertEquals(1, dto.getMovimentacoes().size());
        assertEquals(movDto.getCodigo(), dto.getMovimentacoes().getFirst().getCodigo());
        assertNotNull(dto.getElementosDoProcesso());
        boolean temAtividade = dto.getElementosDoProcesso().stream().anyMatch(e -> "ATIVIDADE".equals(e.getTipo()));
        boolean temConhecimento = dto.getElementosDoProcesso().stream().anyMatch(e -> "CONHECIMENTO".equals(e.getTipo()));
        assertTrue(temAtividade, "Esperado elemento do tipo ATIVIDADE");
        assertTrue(temConhecimento, "Esperado elemento do tipo CONHECIMENTO");
    }

    @Test
    void casoNaoEncontrado_lancaDomainNotFoundException() {
        Long id = 99L;
        when(subprocessoRepo.findById(id)).thenReturn(Optional.empty());
        assertThrows(ErroDominioNaoEncontrado.class, () -> subprocessoService.obterDetalhes(id, "ADMIN", null));
    }

    @Test
    void casoSemPermissao_lancaDomainAccessDeniedExceptionParaGestorDeOutraUnidade() {
        Long spId = 2L;
        Unidade unidade = new Unidade();
        unidade.setCodigo(50L);
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(spId);
        sp.setUnidade(unidade);

        when(subprocessoRepo.findById(spId)).thenReturn(Optional.of(sp));

        Long unidadeUsuario = 99L;
        assertThrows(ErroDominioAccessoNegado.class, () -> subprocessoService.obterDetalhes(spId, "GESTOR", unidadeUsuario));
    }
}