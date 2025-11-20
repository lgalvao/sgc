package sgc.processo;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.service.CopiaMapaService;
import sgc.processo.dto.AtualizarProcessoReq;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.dto.mappers.ProcessoDetalheMapper;
import sgc.processo.dto.mappers.ProcessoMapper;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.eventos.EventoProcessoCriado;
import sgc.processo.eventos.EventoProcessoFinalizado;
import sgc.processo.eventos.EventoProcessoIniciado;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoNotificacaoService;
import sgc.processo.service.ProcessoService;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.model.*;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProcessoServiceTest {
    @Mock private ProcessoRepo processoRepo;
    @Mock private UnidadeRepo unidadeRepo;
    @Mock private SubprocessoRepo subprocessoRepo;
    @Mock private ApplicationEventPublisher publicadorEventos;
    @Mock private ProcessoMapper processoMapper;
    @Mock private ProcessoDetalheMapper processoDetalheMapper;
    @Mock private MapaRepo mapaRepo;
    @Mock private SubprocessoMovimentacaoRepo movimentacaoRepo;
    @Mock private CopiaMapaService servicoDeCopiaDeMapa;
    @Mock private ProcessoNotificacaoService processoNotificacaoService;
    @Mock private SgrhService sgrhService;

    @InjectMocks private ProcessoService processoService;

    @Test
    @DisplayName("Criar processo deve persistir e publicar evento")
    void criar() {
        CriarProcessoReq req = new CriarProcessoReq("Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(1L));
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);

        when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidade));
        when(processoRepo.save(any())).thenAnswer(i -> {
            Processo p = i.getArgument(0);
            p.setCodigo(100L);
            return p;
        });
        when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

        processoService.criar(req);

        verify(processoRepo).save(any());
        verify(publicadorEventos).publishEvent(any(EventoProcessoCriado.class));
    }

    @Test
    @DisplayName("Criar deve lançar exceção se descrição vazia")
    void criarDescricaoVazia() {
        CriarProcessoReq req = new CriarProcessoReq("", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(1L));
        assertThatThrownBy(() -> processoService.criar(req))
            .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Atualizar deve modificar processo se estiver CRIADO")
    void atualizar() {
        Long id = 100L;
        Processo processo = new Processo();
        processo.setCodigo(id);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        
        AtualizarProcessoReq req = AtualizarProcessoReq.builder()
            .codigo(id)
            .descricao("Nova Desc")
            .tipo(TipoProcesso.MAPEAMENTO)
            .dataLimiteEtapa1(LocalDateTime.now())
            .unidades(List.of(1L))
            .build();

        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
        when(unidadeRepo.findById(1L)).thenReturn(Optional.of(new Unidade()));
        when(processoRepo.save(any())).thenReturn(processo);
        when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

        processoService.atualizar(id, req);

        assertThat(processo.getDescricao()).isEqualTo("Nova Desc");
        verify(processoRepo).save(processo);
    }

    @Test
    @DisplayName("Atualizar deve falhar se não estiver CRIADO")
    void atualizarInvalido() {
        Long id = 100L;
        Processo processo = new Processo();
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));

        AtualizarProcessoReq req = AtualizarProcessoReq.builder()
            .descricao("Desc")
            .tipo(TipoProcesso.MAPEAMENTO)
            .unidades(List.of())
            .build();

        assertThatThrownBy(() -> processoService.atualizar(id, req))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Apagar deve remover se estiver CRIADO")
    void apagar() {
        Long id = 100L;
        Processo processo = new Processo();
        processo.setSituacao(SituacaoProcesso.CRIADO);
        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));

        processoService.apagar(id);

        verify(processoRepo).deleteById(id);
    }

    @Test
    @DisplayName("ObterDetalhes deve retornar DTO")
    void obterDetalhes() {
        Long id = 100L;
        Processo processo = new Processo();
        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
        when(processoDetalheMapper.toDetailDTO(processo)).thenReturn(new ProcessoDetalheDto());

        var res = processoService.obterDetalhes(id);

        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("IniciarMapeamento deve criar subprocessos e mudar estado")
    void iniciarProcessoMapeamento() {
        Long id = 100L;
        Processo processo = new Processo();
        processo.setCodigo(id);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);

        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        u1.setTipo(TipoUnidade.OPERACIONAL);
        processo.setParticipantes(Set.of(u1));

        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
        when(mapaRepo.save(any())).thenReturn(new Mapa());
        when(subprocessoRepo.save(any())).thenReturn(new Subprocesso());

        processoService.iniciarProcessoMapeamento(id, List.of(1L));

        assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
        verify(publicadorEventos).publishEvent(any(EventoProcessoIniciado.class));
    }

    @Test
    @DisplayName("Finalizar deve falhar se houver subprocessos não homologados")
    void finalizarFalha() {
        Long id = 100L;
        Processo processo = new Processo();
        processo.setCodigo(id);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);

        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(id)).thenReturn(List.of(sp));

        assertThatThrownBy(() -> processoService.finalizar(id))
            .isInstanceOf(ErroProcesso.class);
    }

    @Test
    @DisplayName("Finalizar deve completar se tudo homologado")
    void finalizarSucesso() {
        Long id = 100L;
        Processo processo = new Processo();
        processo.setCodigo(id);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        Unidade u = new Unidade();
        u.setCodigo(1L);

        Mapa m = new Mapa();

        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPA_HOMOLOGADO);
        sp.setUnidade(u);
        sp.setMapa(m);

        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(id)).thenReturn(List.of(sp));

        processoService.finalizar(id);

        assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.FINALIZADO);
        verify(unidadeRepo).save(u); // Mapa vigente set
        verify(publicadorEventos).publishEvent(any(EventoProcessoFinalizado.class));
    }

    @Test
    @DisplayName("listarFinalizados e listarAtivos devem chamar repo")
    void listagens() {
        when(processoRepo.findBySituacao(any())).thenReturn(List.of(new Processo()));
        when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

        assertThat(processoService.listarFinalizados()).hasSize(1);
        assertThat(processoService.listarAtivos()).hasSize(1);
    }

    @Test
    @DisplayName("Checar acesso deve retornar false se nao autenticado")
    void checarAcesso() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
    }
}
