package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaService;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Perfil;
import sgc.subprocesso.dto.ContextoEdicaoDto;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.model.Subprocesso;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubprocessoContextoServiceTest {

    @Mock
    private SubprocessoService subprocessoService;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private MapaService mapaService;

    @InjectMocks
    private SubprocessoContextoService service;

    @Test
    @DisplayName("obterContextoEdicao deve retornar contexto completo quando tudo ok")
    void obterContextoEdicao_Sucesso() {
        Long codSubprocesso = 1L;
        Long codUnidade = 10L;
        Perfil perfil = Perfil.CHEFE;
        Long codUnidadeUsuario = 10L;

        // Mock 1: Detalhes (agora via SubprocessoService)
        SubprocessoDetalheDto detalheDto = SubprocessoDetalheDto.builder()
                .unidade(SubprocessoDetalheDto.UnidadeDto.builder().codigo(codUnidade).sigla("TESTE").build())
                .build();
        when(subprocessoService.obterDetalhes(codSubprocesso, perfil, codUnidadeUsuario))
                .thenReturn(detalheDto);

        // Mock 2: Unidade
        UnidadeDto unidadeDto = UnidadeDto.builder().codigo(codUnidade).sigla("TESTE").build();
        when(usuarioService.buscarUnidadePorSigla("TESTE")).thenReturn(Optional.of(unidadeDto));

        // Mock 3: Mapa (agora via SubprocessoService)
        Subprocesso subprocesso = new Subprocesso();
        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);
        subprocesso.setMapa(mapa);
        when(subprocessoService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);

        MapaCompletoDto mapaDto = MapaCompletoDto.builder().codigo(100L).build();
        when(mapaService.obterMapaCompleto(100L, codSubprocesso)).thenReturn(mapaDto);

        // Mock 4: Atividades
        when(subprocessoService.listarAtividadesSubprocesso(codSubprocesso))
                .thenReturn(Collections.emptyList());

        ContextoEdicaoDto resultado = service.obterContextoEdicao(codSubprocesso, perfil, codUnidadeUsuario);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getUnidade()).isEqualTo(unidadeDto);
        assertThat(resultado.getSubprocesso()).isEqualTo(detalheDto);
        assertThat(resultado.getMapa()).isEqualTo(mapaDto);
        assertThat(resultado.getAtividadesDisponiveis()).isEmpty();
    }

    @Test
    @DisplayName("obterContextoEdicao deve lançar erro se unidade não encontrada")
    void obterContextoEdicao_ErroUnidade() {
        Long codSubprocesso = 1L;
        Perfil perfil = Perfil.CHEFE;
        Long codUnidadeUsuario = 10L;

        SubprocessoDetalheDto detalheDto = SubprocessoDetalheDto.builder()
                .unidade(SubprocessoDetalheDto.UnidadeDto.builder().sigla("TESTE").build())
                .build();
        when(subprocessoService.obterDetalhes(codSubprocesso, perfil, codUnidadeUsuario))
                .thenReturn(detalheDto);

        when(usuarioService.buscarUnidadePorSigla("TESTE")).thenReturn(Optional.empty());

        assertThrows(ErroEntidadeNaoEncontrada.class, () ->
            service.obterContextoEdicao(codSubprocesso, perfil, codUnidadeUsuario)
        );
    }

    @Test
    @DisplayName("obterContextoEdicao deve retornar mapa nulo se subprocesso não tem mapa")
    void obterContextoEdicao_SemMapa() {
        Long codSubprocesso = 1L;
        Long codUnidade = 10L;
        Perfil perfil = Perfil.CHEFE;
        Long codUnidadeUsuario = 10L;

        SubprocessoDetalheDto detalheDto = SubprocessoDetalheDto.builder()
                .unidade(SubprocessoDetalheDto.UnidadeDto.builder().codigo(codUnidade).sigla("TESTE").build())
                .build();
        when(subprocessoService.obterDetalhes(codSubprocesso, perfil, codUnidadeUsuario))
                .thenReturn(detalheDto);

        when(usuarioService.buscarUnidadePorSigla("TESTE")).thenReturn(Optional.of(UnidadeDto.builder().build()));

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setMapa(null);
        when(subprocessoService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);

        when(subprocessoService.listarAtividadesSubprocesso(codSubprocesso))
                .thenReturn(Collections.emptyList());

        ContextoEdicaoDto resultado = service.obterContextoEdicao(codSubprocesso, perfil, codUnidadeUsuario);

        assertThat(resultado.getMapa()).isNull();
    }
}
