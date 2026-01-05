package sgc.subprocesso.service.decomposed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.ValidacaoCadastroDto;
import sgc.subprocesso.model.Subprocesso;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoValidacaoService")
class SubprocessoValidacaoServiceTest {

    @Mock private AtividadeService atividadeService;
    @Mock private CompetenciaService competenciaService;
    @Mock private UsuarioService usuarioService;
    @Mock private SubprocessoCrudService crudService;

    @InjectMocks private SubprocessoValidacaoService service;

    @Test
    @DisplayName("validarPermissaoEdicaoMapa: sucesso")
    void validarPermissaoEdicaoMapaSucesso() {
        Subprocesso sp = new Subprocesso();
        Unidade u = new Unidade();
        u.setTituloTitular("123");
        sp.setUnidade(u);
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");

        when(crudService.obterEntidadePorCodigoMapa(1L)).thenReturn(sp);
        when(usuarioService.buscarUsuarioPorLogin("123")).thenReturn(usuario);

        service.validarPermissaoEdicaoMapa(1L, "123");
    }

    @Test
    @DisplayName("validarPermissaoEdicaoMapa: erro sem unidade")
    void validarPermissaoEdicaoMapaSemUnidade() {
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(null);
        when(crudService.obterEntidadePorCodigoMapa(1L)).thenReturn(sp);

        assertThatThrownBy(() -> service.validarPermissaoEdicaoMapa(1L, "123"))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("validarPermissaoEdicaoMapa: erro acesso negado")
    void validarPermissaoEdicaoMapaAcessoNegado() {
        Subprocesso sp = new Subprocesso();
        Unidade u = new Unidade();
        u.setTituloTitular("456");
        sp.setUnidade(u);
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");

        when(crudService.obterEntidadePorCodigoMapa(1L)).thenReturn(sp);
        when(usuarioService.buscarUsuarioPorLogin("123")).thenReturn(usuario);

        assertThatThrownBy(() -> service.validarPermissaoEdicaoMapa(1L, "123"))
                .isInstanceOf(ErroAccessoNegado.class);
    }

    @Test
    @DisplayName("obterAtividadesSemConhecimento: retorna lista vazia se mapa null")
    void obterAtividadesSemConhecimentoMapaNull() {
        Subprocesso sp = new Subprocesso();
        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        assertThat(service.obterAtividadesSemConhecimento(1L)).isEmpty();
    }

    @Test
    @DisplayName("obterAtividadesSemConhecimento: retorna atividades")
    void obterAtividadesSemConhecimentoRetornaLista() {
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        Atividade a1 = new Atividade(); a1.setConhecimentos(List.of(new Conhecimento()));
        Atividade a2 = new Atividade(); a2.setConhecimentos(List.of()); // Sem
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a1, a2));

        assertThat(service.obterAtividadesSemConhecimento(mapa)).containsExactly(a2);
    }

    @Test
    @DisplayName("validarExistenciaAtividades: erro sem mapa")
    void validarExistenciaAtividadesErroSemMapa() {
        Subprocesso sp = new Subprocesso();
        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        assertThatThrownBy(() -> service.validarExistenciaAtividades(1L))
                .isInstanceOf(ErroValidacao.class);
    }

    @Test
    @DisplayName("validarExistenciaAtividades: erro sem atividades")
    void validarExistenciaAtividadesErroSemAtividades() {
        Subprocesso sp = new Subprocesso();
        Mapa m = new Mapa(); m.setCodigo(1L);
        sp.setMapa(m);
        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.validarExistenciaAtividades(1L))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("ao menos uma atividade");
    }

    @Test
    @DisplayName("validarExistenciaAtividades: erro atividade sem conhecimento")
    void validarExistenciaAtividadesErroSemConhecimento() {
        Subprocesso sp = new Subprocesso();
        Mapa m = new Mapa(); m.setCodigo(1L);
        sp.setMapa(m);
        Atividade a = new Atividade();
        a.setConhecimentos(List.of());

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a));

        assertThatThrownBy(() -> service.validarExistenciaAtividades(1L))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("atividades devem possuir conhecimentos");
    }

    @Test
    @DisplayName("validarAssociacoesMapa: erro competencias sem associacao")
    void validarAssociacoesMapaErroCompetencias() {
        Competencia c = new Competencia();
        c.setAtividades(Collections.emptySet());
        when(competenciaService.buscarPorMapa(1L)).thenReturn(List.of(c));

        assertThatThrownBy(() -> service.validarAssociacoesMapa(1L))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("competências que não foram associadas");
    }

    @Test
    @DisplayName("validarAssociacoesMapa: erro atividades sem associacao")
    void validarAssociacoesMapaErroAtividades() {
        Competencia c = new Competencia();
        c.setAtividades(Set.of(new Atividade()));
        when(competenciaService.buscarPorMapa(1L)).thenReturn(List.of(c));

        Atividade a = new Atividade();
        a.setCompetencias(Collections.emptySet());
        when(atividadeService.buscarPorMapaCodigo(1L)).thenReturn(List.of(a));

        assertThatThrownBy(() -> service.validarAssociacoesMapa(1L))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("atividades que não foram associadas");
    }

    @Test
    @DisplayName("validarCadastro: mapa inexistente")
    void validarCadastroMapaInexistente() {
        Subprocesso sp = new Subprocesso();
        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        ValidacaoCadastroDto res = service.validarCadastro(1L);
        assertThat(res.getValido()).isFalse();
        assertThat(res.getErros().getFirst().getTipo()).isEqualTo("MAPA_INEXISTENTE");
    }

    @Test
    @DisplayName("validarCadastro: sem atividades")
    void validarCadastroSemAtividades() {
        Subprocesso sp = new Subprocesso();
        Mapa m = new Mapa(); m.setCodigo(1L); sp.setMapa(m);
        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(1L)).thenReturn(List.of());

        ValidacaoCadastroDto res = service.validarCadastro(1L);
        assertThat(res.getValido()).isFalse();
        assertThat(res.getErros().getFirst().getTipo()).isEqualTo("SEM_ATIVIDADES");
    }

    @Test
    @DisplayName("validarCadastro: atividade sem conhecimento")
    void validarCadastroAtividadeSemConhecimento() {
        Subprocesso sp = new Subprocesso();
        Mapa m = new Mapa(); m.setCodigo(1L); sp.setMapa(m);
        Atividade a = new Atividade(); a.setCodigo(10L); a.setConhecimentos(List.of());

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a));

        ValidacaoCadastroDto res = service.validarCadastro(1L);
        assertThat(res.getValido()).isFalse();
        assertThat(res.getErros().getFirst().getTipo()).isEqualTo("ATIVIDADE_SEM_CONHECIMENTO");
    }

    @Test
    @DisplayName("validarCadastro: sucesso")
    void validarCadastroSucesso() {
        Subprocesso sp = new Subprocesso();
        Mapa m = new Mapa(); m.setCodigo(1L); sp.setMapa(m);
        Atividade a = new Atividade(); a.setConhecimentos(List.of(new Conhecimento()));

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a));

        ValidacaoCadastroDto res = service.validarCadastro(1L);
        assertThat(res.getValido()).isTrue();
    }
}
