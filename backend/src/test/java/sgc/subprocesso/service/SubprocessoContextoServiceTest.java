package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaFacade;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.ContextoEdicaoDto;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.SubprocessoDetalheService;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class SubprocessoContextoServiceTest {

    @Mock
    private UsuarioService usuarioService;
    @Mock
    private MapaFacade mapaFacade;
    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private SubprocessoDetalheService detalheService;

    @InjectMocks
    private SubprocessoContextoService service;

    private static final String SIGLA_TESTE = "TESTE";

    @Test
    @DisplayName("obterContextoEdicao deve retornar contexto completo quando tudo ok")
    void obterContextoEdicaoSucesso() {
        Long codSubprocesso = 1L;
        Long codUnidade = 10L;

        // Mock usuário autenticado
        Usuario usuario = new Usuario();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

        // Mock 1: Detalhes (agora via DetalheService)
        SubprocessoDetalheDto detalheDto = SubprocessoDetalheDto.builder()
                .unidade(SubprocessoDetalheDto.UnidadeDto.builder().codigo(codUnidade).sigla(SIGLA_TESTE).build())
                .build();
        when(detalheService.obterDetalhes(codSubprocesso, usuario))
                .thenReturn(detalheDto);

        // Mock 2: Unidade
        UnidadeDto unidadeDto = UnidadeDto.builder().codigo(codUnidade).sigla(SIGLA_TESTE).build();
        when(usuarioService.buscarUnidadePorSigla(SIGLA_TESTE)).thenReturn(Optional.of(unidadeDto));

        // Mock 3: Mapa (agora via CrudService)
        Subprocesso subprocesso = new Subprocesso();
        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);
        subprocesso.setMapa(mapa);
        when(crudService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);

        MapaCompletoDto mapaDto = MapaCompletoDto.builder().codigo(100L).build();
        when(mapaFacade.obterMapaCompleto(100L, codSubprocesso)).thenReturn(mapaDto);

        // Mock 4: Atividades
        when(detalheService.listarAtividadesSubprocesso(codSubprocesso))
                .thenReturn(Collections.emptyList());

        ContextoEdicaoDto resultado = service.obterContextoEdicao(codSubprocesso);

        assertThat(resultado).isNotNull();
        assertThat(resultado.unidade()).isEqualTo(unidadeDto);
        assertThat(resultado.subprocesso()).isEqualTo(detalheDto);
        assertThat(resultado.mapa()).isEqualTo(mapaDto);
        assertThat(resultado.atividadesDisponiveis()).isEmpty();
    }

    @Test
    @DisplayName("obterContextoEdicao deve lançar erro se unidade não encontrada")
    void obterContextoEdicaoErroUnidade() {
        Long codSubprocesso = 1L;

        Usuario usuario = new Usuario();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

        SubprocessoDetalheDto detalheDto = SubprocessoDetalheDto.builder()
                .unidade(SubprocessoDetalheDto.UnidadeDto.builder().sigla(SIGLA_TESTE).build())
                .build();
        when(detalheService.obterDetalhes(codSubprocesso, usuario))
                .thenReturn(detalheDto);

        when(usuarioService.buscarUnidadePorSigla(SIGLA_TESTE)).thenReturn(Optional.empty());

        assertThrows(ErroEntidadeNaoEncontrada.class, () ->
            service.obterContextoEdicao(codSubprocesso)
        );
    }

    @Test
    @DisplayName("obterContextoEdicao deve retornar mapa nulo se subprocesso não tem mapa")
    void obterContextoEdicaoSemMapa() {
        Long codSubprocesso = 1L;
        Long codUnidade = 10L;

        Usuario usuario = new Usuario();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

        SubprocessoDetalheDto detalheDto = SubprocessoDetalheDto.builder()
                .unidade(SubprocessoDetalheDto.UnidadeDto.builder().codigo(codUnidade).sigla(SIGLA_TESTE).build())
                .build();
        when(detalheService.obterDetalhes(codSubprocesso, usuario))
                .thenReturn(detalheDto);

        when(usuarioService.buscarUnidadePorSigla(SIGLA_TESTE)).thenReturn(Optional.of(UnidadeDto.builder().build()));

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setMapa(null);
        when(crudService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);

        when(detalheService.listarAtividadesSubprocesso(codSubprocesso))
                .thenReturn(Collections.emptyList());

        ContextoEdicaoDto resultado = service.obterContextoEdicao(codSubprocesso);

        assertThat(resultado.mapa()).isNull();
    }
}
