package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.Responsabilidade;
import sgc.organizacao.model.ResponsabilidadeRepo;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.HierarquiaService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do HierarquiaService")
class HierarquiaServiceTest {

    @Mock
    private ResponsabilidadeRepo responsabilidadeRepo;

    @InjectMocks
    private HierarquiaService hierarquiaService;


    @Test
    @DisplayName("Deve retornar true se usuario é responsavel")
    void deveRetornarTrueSeUsuarioResponsavel() {
        Unidade unidade = Unidade.builder().codigo(1L).build();
        Usuario usuario = Usuario.builder().tituloEleitoral("123456789012").build();
        Responsabilidade responsabilidade = Responsabilidade.builder()
                .unidadeCodigo(1L)
                .usuarioTitulo("123456789012")
                .build();

        when(responsabilidadeRepo.findById(1L)).thenReturn(Optional.of(responsabilidade));

        assertThat(hierarquiaService.isResponsavel(unidade, usuario)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false se usuario não é responsavel")
    void deveRetornarFalseSeUsuarioNaoResponsavel() {
        Unidade unidade = Unidade.builder().codigo(1L).build();
        Usuario usuario = Usuario.builder().tituloEleitoral("123456789012").build();
        Responsabilidade responsabilidade = Responsabilidade.builder()
                .unidadeCodigo(1L)
                .usuarioTitulo("000000000000") // Outro usuário
                .build();

        when(responsabilidadeRepo.findById(1L)).thenReturn(Optional.of(responsabilidade));

        assertThat(hierarquiaService.isResponsavel(unidade, usuario)).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false se unidade não tem responsavel")
    void deveRetornarFalseSeUnidadeSemResponsavel() {
        Unidade unidade = Unidade.builder().codigo(1L).build();
        Usuario usuario = Usuario.builder().tituloEleitoral("123456789012").build();

        when(responsabilidadeRepo.findById(1L)).thenReturn(Optional.empty());

        assertThat(hierarquiaService.isResponsavel(unidade, usuario)).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando unidade alvo não tem superior")
    void deveRetornarFalseQuandoUnidadeAlvoNaoTemSuperior() {
        Unidade alvo = criarUnidade(1L, null);
        Unidade superior = criarUnidade(2L, null);

        assertThat(hierarquiaService.isSubordinada(alvo, superior)).isFalse();
    }

    @Test
    @DisplayName("Deve retornar true quando unidade é diretamente subordinada")
    void deveRetornarTrueQuandoUnidadeDiretamenteSubordinada() {
        Unidade superior = criarUnidade(1L, null);
        Unidade alvo = criarUnidade(2L, superior);

        assertThat(hierarquiaService.isSubordinada(alvo, superior)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar true quando unidade é indiretamente subordinada")
    void deveRetornarTrueQuandoUnidadeIndiretamenteSubordinada() {
        Unidade raiz = criarUnidade(1L, null);
        Unidade intermediaria = criarUnidade(2L, raiz);
        Unidade alvo = criarUnidade(3L, intermediaria);

        assertThat(hierarquiaService.isSubordinada(alvo, raiz)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando unidade não é subordinada")
    void deveRetornarFalseQuandoUnidadeNaoSubordinada() {
        Unidade raiz1 = criarUnidade(1L, null);
        Unidade raiz2 = criarUnidade(2L, null);
        Unidade alvo = criarUnidade(3L, raiz1);

        assertThat(hierarquiaService.isSubordinada(alvo, raiz2)).isFalse();
    }

    @Test
    @DisplayName("Deve retornar true para mesma unidade em isMesmaOuSubordinada")
    void deveRetornarTrueParaMesmaUnidade() {
        Unidade unidade = criarUnidade(1L, null);

        assertThat(hierarquiaService.isMesmaOuSubordinada(unidade, unidade)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar true para unidade subordinada em isMesmaOuSubordinada")
    void deveRetornarTrueParaUnidadeSubordinadaEmIsMesmaOuSubordinada() {
        Unidade superior = criarUnidade(1L, null);
        Unidade alvo = criarUnidade(2L, superior);

        assertThat(hierarquiaService.isMesmaOuSubordinada(alvo, superior)).isTrue();
    }


    @Test
    @DisplayName("Deve retornar true quando é superior imediata")
    void deveRetornarTrueQuandoSuperiorImediata() {
        Unidade superior = criarUnidade(1L, null);
        Unidade alvo = criarUnidade(2L, superior);

        assertThat(hierarquiaService.isSuperiorImediata(alvo, superior)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando não é superior imediata")
    void deveRetornarFalseQuandoNaoSuperiorImediata() {
        Unidade raiz = criarUnidade(1L, null);
        Unidade intermediaria = criarUnidade(2L, raiz);
        Unidade alvo = criarUnidade(3L, intermediaria);

        assertThat(hierarquiaService.isSuperiorImediata(alvo, raiz)).isFalse();
    }

    private Unidade criarUnidade(Long codigo, Unidade superior) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigo);
        unidade.setUnidadeSuperior(superior);
        return unidade;
    }

    @Test
    @DisplayName("Deve retornar false se alvo não tem superior ao verificar superior imediata")
    void deveRetornarFalsoSeAlvoNaoTemSuperiorAoVerificarImediata() {
        Unidade alvo = criarUnidade(1L, null);
        Unidade superior = criarUnidade(2L, null);

        assertThat(hierarquiaService.isSuperiorImediata(alvo, superior)).isFalse();
    }
}
