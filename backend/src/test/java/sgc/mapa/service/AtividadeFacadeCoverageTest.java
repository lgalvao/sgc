package sgc.mapa.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.mapa.dto.AtividadeResponse;
import sgc.mapa.dto.AtualizarAtividadeRequest;
import sgc.mapa.dto.CriarAtividadeRequest;
import sgc.mapa.evento.EventoAtividadeAtualizada;
import sgc.mapa.evento.EventoAtividadeCriada;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AtividadeFacadeCoverageTest {

    @InjectMocks
    private AtividadeFacade facade;

    @Mock private AtividadeService atividadeService;
    @Mock private ConhecimentoService conhecimentoService;
    @Mock private SubprocessoFacade subprocessoFacade;
    @Mock private AccessControlService accessControlService;
    @Mock private UsuarioFacade usuarioService;
    @Mock private MapaFacade mapaFacade;
    @Mock private ApplicationEventPublisher eventPublisher;

    @Test
    void devePublicarEventoAoCriarAtividade() {
        // Cobre linhas 63 e 74
        CriarAtividadeRequest request = CriarAtividadeRequest.builder()
                .mapaCodigo(1L)
                .descricao("Nova Ativ")
                .build();

        Usuario usuario = new Usuario();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        mapa.setSubprocesso(subprocesso);
        when(mapaFacade.obterPorCodigo(1L)).thenReturn(mapa);

        AtividadeResponse createdResponse = new AtividadeResponse(100L, 1L, "Nova Ativ");
        when(atividadeService.criar(request)).thenReturn(createdResponse);

        Atividade atividadeCriada = new Atividade();
        atividadeCriada.setCodigo(100L);
        atividadeCriada.setDescricao("Nova Ativ");
        when(atividadeService.obterPorCodigo(100L)).thenReturn(atividadeCriada);

        when(subprocessoFacade.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);
        when(subprocessoFacade.obterSituacao(10L)).thenReturn(SubprocessoSituacaoDto.builder().build());

        facade.criarAtividade(request);

        verify(eventPublisher).publishEvent(any(EventoAtividadeCriada.class));
    }

    @Test
    void devePublicarEventoAoAtualizarAtividadeComMudanca() {
        // Cobre branches de mudança de descrição
        Long codAtiv = 100L;
        AtualizarAtividadeRequest request = AtualizarAtividadeRequest.builder().descricao("Descricao Alterada").build();

        Usuario usuario = new Usuario();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

        Atividade atividade = new Atividade();
        atividade.setCodigo(codAtiv);
        atividade.setDescricao("Descricao Original");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        mapa.setSubprocesso(subprocesso);
        atividade.setMapa(mapa);

        when(atividadeService.obterPorCodigo(codAtiv)).thenReturn(atividade);

        // Mock subprocesso facade for response creation
        when(subprocessoFacade.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);
        when(subprocessoFacade.obterSituacao(10L)).thenReturn(SubprocessoSituacaoDto.builder().build());

        facade.atualizarAtividade(codAtiv, request);

        verify(atividadeService).atualizar(codAtiv, request);
        verify(eventPublisher).publishEvent(any(EventoAtividadeAtualizada.class));
    }

    @Test
    void naoDevePublicarEventoAoAtualizarAtividadeSemMudanca() {
        // Cobre branch onde não há mudanças
        Long codAtiv = 100L;
        AtualizarAtividadeRequest request = AtualizarAtividadeRequest.builder().descricao("Descricao Original").build();

        Usuario usuario = new Usuario();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

        Atividade atividade = new Atividade();
        atividade.setCodigo(codAtiv);
        atividade.setDescricao("Descricao Original"); // Mesma descrição

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        mapa.setSubprocesso(subprocesso);
        atividade.setMapa(mapa);

        when(atividadeService.obterPorCodigo(codAtiv)).thenReturn(atividade);

        // Mock subprocesso facade for response creation
        when(subprocessoFacade.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);
        when(subprocessoFacade.obterSituacao(10L)).thenReturn(SubprocessoSituacaoDto.builder().build());

        facade.atualizarAtividade(codAtiv, request);

        verify(atividadeService).atualizar(codAtiv, request);
        verify(eventPublisher, never()).publishEvent(any(EventoAtividadeAtualizada.class));
    }
}
