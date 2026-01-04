package sgc.subprocesso.service.decomposed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.model.Atividade;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.model.Mapa;
import sgc.subprocesso.dto.ValidacaoCadastroDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.mapa.model.Competencia;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para SubprocessoValidacaoService")
class SubprocessoValidacaoServiceTest {

    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private AtividadeService atividadeService;
    @Mock
    private CompetenciaService competenciaService;
    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private SubprocessoValidacaoService service;

    @Test
    @DisplayName("Deve validar existência de atividades - Sucesso")
    void deveValidarExistenciaAtividadesSucesso() {
        Subprocesso sp = new Subprocesso();
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        sp.setMapa(mapa);

        Atividade atividade = new Atividade();
        atividade.setConhecimentos(List.of(new sgc.mapa.model.Conhecimento()));

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(10L)).thenReturn(List.of(atividade));

        service.validarExistenciaAtividades(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção se mapa não tiver atividades")
    void deveLancarExcecaoSemAtividades() {
        Subprocesso sp = new Subprocesso();
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        sp.setMapa(mapa);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(10L)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> service.validarExistenciaAtividades(1L))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("ao menos uma atividade");
    }

    @Test
    @DisplayName("Deve validar permissão de edição do mapa")
    void deveValidarPermissaoEdicaoMapa() {
        Subprocesso sp = new Subprocesso();
        Unidade u = new Unidade();
        u.setTituloTitular("123");
        sp.setUnidade(u);

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");

        when(crudService.obterEntidadePorCodigoMapa(100L)).thenReturn(sp);
        when(usuarioService.buscarUsuarioPorLogin("user")).thenReturn(usuario);

        service.validarPermissaoEdicaoMapa(100L, "user");
    }

    @Test
    @DisplayName("validarCadastro sucesso")
    void validarCadastroSucesso() {
        Subprocesso sp = new Subprocesso();
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        sp.setMapa(mapa);

        Atividade ativ = new Atividade();
        ativ.setConhecimentos(List.of(new sgc.mapa.model.Conhecimento()));

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(10L)).thenReturn(List.of(ativ));

        ValidacaoCadastroDto result = service.validarCadastro(1L);
        assertThat(result.getValido()).isTrue();
    }

    @Test
    @DisplayName("Deve lançar exceção se competência não estiver associada")
    void deveLancarExcecaoSeCompetenciaNaoEstiverAssociada() {
        Competencia competencia = new Competencia();
        competencia.setDescricao("C1");

        when(competenciaService.buscarPorMapa(1L)).thenReturn(List.of(competencia));

        assertThatThrownBy(() -> service.validarAssociacoesMapa(1L))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("competências que não foram associadas");
    }
}
