package sgc.organizacao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.repo.ComumRepo;
import sgc.organizacao.dto.AtribuicaoTemporariaDto;
import sgc.organizacao.dto.CriarAtribuicaoTemporariaRequest;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("UnidadeResponsavelService")
class UnidadeResponsavelServiceTest {

    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;

    @Mock
    private ResponsabilidadeRepo responsabilidadeRepo;

    @Mock
    private ComumRepo repo;

    @InjectMocks
    private UnidadeResponsavelService service;

    @Nested
    @DisplayName("Buscar Atribuições")
    class BuscarAtribuicoesTests {

        @Test
        @DisplayName("Deve buscar todas as atribuições temporárias")
        void deveBuscarTodasAtribuicoes() {
            // Given
            Unidade unidade = new Unidade();
            unidade.setCodigo(10L);
            unidade.setSigla("UNIT");

            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral("123456789012");

            AtribuicaoTemporaria atribuicao = new AtribuicaoTemporaria();
            atribuicao.setCodigo(1L);
            atribuicao.setUnidade(unidade);
            atribuicao.setUsuarioTitulo("123456789012");
            atribuicao.setDataInicio(LocalDateTime.now());
            atribuicao.setDataTermino(LocalDateTime.now().plusDays(1));
            atribuicao.setJustificativa("Teste");

            when(atribuicaoTemporariaRepo.findAll()).thenReturn(List.of(atribuicao));
            when(repo.buscar(Usuario.class, "123456789012")).thenReturn(usuario);

            // When
            List<AtribuicaoTemporariaDto> resultado = service.buscarTodasAtribuicoes();

            // Then
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).unidadeCodigo()).isEqualTo(10L);
            assertThat(resultado.get(0).usuario()).isEqualTo(usuario);
        }
    }

    @Nested
    @DisplayName("Criar Atribuição Temporária")
    class CriarAtribuicaoTemporariaTests {

        @Test
        @DisplayName("Deve criar atribuição com dataInicio explícita")
        void deveCriarAtribuicaoComDataInicioExplicita() {
            // Given
            Long codUnidade = 1L;
            LocalDate dataInicio = LocalDate.of(2024, 1, 15);
            LocalDate dataTermino = LocalDate.of(2024, 2, 15);
            
            CriarAtribuicaoTemporariaRequest request = new CriarAtribuicaoTemporariaRequest(
                    "123456789012",
                    dataInicio,
                    dataTermino,
                    "Cobertura de férias"
            );

            Unidade unidade = new Unidade();
            unidade.setCodigo(codUnidade);

            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral("123456789012");
            usuario.setMatricula("12345678");

            when(repo.buscar(Unidade.class, codUnidade)).thenReturn(unidade);
            when(repo.buscar(Usuario.class, "123456789012")).thenReturn(usuario);

            // When
            service.criarAtribuicaoTemporaria(codUnidade, request);

            // Then
            ArgumentCaptor<AtribuicaoTemporaria> captor = ArgumentCaptor.forClass(AtribuicaoTemporaria.class);
            verify(atribuicaoTemporariaRepo).save(captor.capture());

            AtribuicaoTemporaria atribuicao = captor.getValue();
            assertThat(atribuicao.getUnidade()).isEqualTo(unidade);
            assertThat(atribuicao.getUsuarioTitulo()).isEqualTo("123456789012");
            assertThat(atribuicao.getUsuarioMatricula()).isEqualTo("12345678");
            assertThat(atribuicao.getDataInicio()).isEqualTo(dataInicio.atStartOfDay());
            assertThat(atribuicao.getDataTermino()).isEqualTo(dataTermino.atTime(23, 59, 59));
            assertThat(atribuicao.getJustificativa()).isEqualTo("Cobertura de férias");
        }
    }

    @Nested
    @DisplayName("Buscar Responsável Atual")
    class BuscarResponsavelAtualTests {

        @Test
        @DisplayName("Deve buscar responsável atual com sucesso")
        void deveBuscarResponsavelAtualComSucesso() {
            // Given
            String siglaUnidade = "ABC";
            
            Unidade unidade = new Unidade();
            unidade.setCodigo(1L);
            unidade.setSigla(siglaUnidade);

            Responsabilidade resp = new Responsabilidade();
            resp.setUnidadeCodigo(1L);
            resp.setUsuarioTitulo("123456789012");

            Usuario usuarioCompleto = new Usuario();
            usuarioCompleto.setTituloEleitoral("123456789012");
            usuarioCompleto.setNome("João Silva");

            when(repo.buscarPorSigla(Unidade.class, siglaUnidade)).thenReturn(unidade);
            when(repo.buscar(Responsabilidade.class, 1L)).thenReturn(resp);
            when(repo.buscar(Usuario.class, "123456789012")).thenReturn(usuarioCompleto);

            // When
            Usuario resultado = service.buscarResponsavelAtual(siglaUnidade);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getTituloEleitoral()).isEqualTo("123456789012");
        }
    }

    @Nested
    @DisplayName("Buscar Responsável de Unidade")
    class BuscarResponsavelUnidadeTests {

        @Test
        @DisplayName("Deve buscar responsável com titular e substituto")
        void deveBuscarResponsavelComTitularESubstituto() {
            // Given
            Long unidadeCodigo = 1L;

            Usuario titularOficial = new Usuario();
            titularOficial.setTituloEleitoral("111111111111");
            titularOficial.setNome("João Silva");

            Usuario substituto = new Usuario();
            substituto.setTituloEleitoral("222222222222");
            substituto.setNome("Maria Santos");

            Unidade unidade = new Unidade();
            unidade.setCodigo(unidadeCodigo);
            unidade.setTituloTitular("111111111111");

            Responsabilidade responsabilidade = new Responsabilidade();
            responsabilidade.setUnidadeCodigo(unidadeCodigo);
            responsabilidade.setUsuarioTitulo("222222222222");
            responsabilidade.setUnidade(unidade);

            when(repo.buscar(Responsabilidade.class, unidadeCodigo)).thenReturn(responsabilidade);
            when(repo.buscar(Usuario.class, "222222222222")).thenReturn(substituto);
            when(repo.buscar(Usuario.class, "111111111111")).thenReturn(titularOficial);

            // When
            UnidadeResponsavelDto resultado = service.buscarResponsavelUnidade(unidadeCodigo);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.titularTitulo()).isEqualTo("111111111111");
            assertThat(resultado.substitutoTitulo()).isEqualTo("222222222222");
        }
    }
}
