package sgc.subprocesso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("SubprocessoCrudController")
class SubprocessoCrudControllerTest {

    @Mock
    private SubprocessoFacade subprocessoFacade;

    @Mock
    private OrganizacaoFacade organizacaoFacade;

    @InjectMocks
    private SubprocessoCrudController controller;

    @Test
    @DisplayName("obterPermissoes - Sucesso")
    void obterPermissoes() {
        when(subprocessoFacade.obterPermissoes(1L)).thenReturn(SubprocessoPermissoesDto.builder().build());
        ResponseEntity<SubprocessoPermissoesDto> response = controller.obterPermissoes(1L);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(subprocessoFacade).obterPermissoes(1L);
    }

    @Test
    @DisplayName("validarCadastro - Sucesso")
    void validarCadastro() {
        when(subprocessoFacade.validarCadastro(1L)).thenReturn(ValidacaoCadastroDto.builder().valido(true).build());
        ResponseEntity<ValidacaoCadastroDto> response = controller.validarCadastro(1L);
        assertThat(response.getBody().valido()).isTrue();
    }

    @Test
    @DisplayName("obterStatus - Sucesso")
    void obterStatus() {
        when(subprocessoFacade.obterSituacao(1L)).thenReturn(SubprocessoSituacaoDto.builder().build());
        ResponseEntity<SubprocessoSituacaoDto> response = controller.obterStatus(1L);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    @DisplayName("listar - Sucesso")
    void listar() {
        when(subprocessoFacade.listar()).thenReturn(List.of(Subprocesso.builder().codigo(1L).build()));
        List<Subprocesso> result = controller.listar();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("obterPorCodigo - Sucesso")
    void obterPorCodigo() {
        when(subprocessoFacade.obterDetalhes(1L)).thenReturn(new SubprocessoDetalheResponse(null, null, null, null, null));
        SubprocessoDetalheResponse result = controller.obterPorCodigo(1L);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("buscarPorProcessoEUnidade - Sucesso")
    void buscarPorProcessoEUnidade() {
        UnidadeDto unidade = UnidadeDto.builder().codigo(10L).build();
        when(organizacaoFacade.buscarUnidadePorSigla("U1")).thenReturn(unidade);
        when(subprocessoFacade.obterEntidadePorProcessoEUnidade(1L, 10L)).thenReturn(Subprocesso.builder().codigo(100L).build());

        ResponseEntity<Subprocesso> response = controller.buscarPorProcessoEUnidade(1L, "U1");
        assertThat(response.getBody().getCodigo()).isEqualTo(100L);
    }

    @Test
    @DisplayName("criar - Sucesso")
    void criar() {
        CriarSubprocessoRequest req = CriarSubprocessoRequest.builder().codProcesso(1L).codUnidade(10L).build();
        when(subprocessoFacade.criar(any())).thenReturn(Subprocesso.builder().codigo(100L).build());

        ResponseEntity<Subprocesso> response = controller.criar(req);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getHeaders().getLocation().toString()).contains("100");
    }

    @Test
    @DisplayName("atualizar - Sucesso")
    void atualizar() {
        AtualizarSubprocessoRequest req = AtualizarSubprocessoRequest.builder().build();
        when(subprocessoFacade.atualizar(eq(1L), any())).thenReturn(Subprocesso.builder().codigo(1L).build());

        ResponseEntity<Subprocesso> response = controller.atualizar(1L, req);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    @DisplayName("excluir - Sucesso")
    void excluir() {
        ResponseEntity<Void> response = controller.excluir(1L);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(subprocessoFacade).excluir(1L);
    }

    @Test
    @DisplayName("alterarDataLimite - Sucesso")
    void alterarDataLimite() {
        AlterarDataLimiteRequest req = new AlterarDataLimiteRequest(LocalDate.now());
        ResponseEntity<Void> response = controller.alterarDataLimite(1L, req);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(subprocessoFacade).alterarDataLimite(eq(1L), any());
    }

    @Test
    @DisplayName("reabrirCadastro - Sucesso")
    void reabrirCadastro() {
        ReabrirProcessoRequest req = new ReabrirProcessoRequest("J");
        ResponseEntity<Void> response = controller.reabrirCadastro(1L, req);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(subprocessoFacade).reabrirCadastro(1L, "J");
    }

    @Test
    @DisplayName("reabrirRevisaoCadastro - Sucesso")
    void reabrirRevisaoCadastro() {
        ReabrirProcessoRequest req = new ReabrirProcessoRequest("J");
        ResponseEntity<Void> response = controller.reabrirRevisaoCadastro(1L, req);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(subprocessoFacade).reabrirRevisaoCadastro(1L, "J");
    }
}
