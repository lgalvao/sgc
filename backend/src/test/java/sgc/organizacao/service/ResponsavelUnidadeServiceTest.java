package sgc.organizacao.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResponsavelUnidadeService")
class ResponsavelUnidadeServiceTest {
    @Mock
    private AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;

    @Mock
    private ComumRepo repo;

    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private ResponsabilidadeRepo responsabilidadeRepo;

    @InjectMocks
    private ResponsavelUnidadeService service;

    @Nested
    @DisplayName("Buscar atribuições")
    class BuscarAtribuicoesTests {

        @Test
        @DisplayName("Deve buscar todas as atribuições temporárias")
        void deveBuscarTodasAtribuicoes() {

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

            List<AtribuicaoDto> resultado = service.buscarTodasAtribuicoes();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.getFirst().unidadeCodigo()).isEqualTo(10L);
            assertThat(resultado.getFirst().usuario().tituloEleitoral()).isEqualTo(usuario.getTituloEleitoral());
        }
    }

    @Nested
    @DisplayName("Criar atribuição temporária")
    class CriarAtribuicaoTemporariaTests {

        @Test
        @DisplayName("Deve criar atribuição com dataInicio explícita")
        void deveCriarAtribuicaoComDataInicioExplicita() {

            Long codUnidade = 1L;
            LocalDate dataInicio = LocalDate.of(2024, 1, 15);
            LocalDate dataTermino = LocalDate.of(2024, 2, 15);

            CriarAtribuicaoRequest request = new CriarAtribuicaoRequest(
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

            service.criarAtribuicaoTemporaria(codUnidade, request);

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

        @Test
        @DisplayName("Deve criar atribuição com dataInicio nula (assume hoje)")
        void deveCriarAtribuicaoComDataInicioNula() {
            Long codUnidade = 1L;
            LocalDate dataTermino = LocalDate.now().plusDays(10);
            CriarAtribuicaoRequest request = new CriarAtribuicaoRequest("123", null, dataTermino, "Justificativa");

            Unidade unidade = new Unidade();
            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral("123");

            when(repo.buscar(Unidade.class, codUnidade)).thenReturn(unidade);
            when(repo.buscar(Usuario.class, "123")).thenReturn(usuario);

            service.criarAtribuicaoTemporaria(codUnidade, request);

            ArgumentCaptor<AtribuicaoTemporaria> captor = ArgumentCaptor.forClass(AtribuicaoTemporaria.class);
            verify(atribuicaoTemporariaRepo).save(captor.capture());
            assertThat(captor.getValue().getDataInicio()).isNotNull();
        }

        @Test
        @DisplayName("Deve rejeitar atribuição quando dataTermino for anterior ao inicio")
        void deveRejeitarAtribuicaoComDataTerminoAnteriorAoInicio() {
            Long codUnidade = 1L;
            LocalDate dataInicio = LocalDate.of(2024, 2, 10);
            LocalDate dataTermino = LocalDate.of(2024, 2, 9);
            CriarAtribuicaoRequest request = new CriarAtribuicaoRequest("123", dataInicio, dataTermino, "Justificativa");

            when(repo.buscar(Unidade.class, codUnidade)).thenReturn(new Unidade());
            when(repo.buscar(Usuario.class, "123")).thenReturn(new Usuario().setTituloEleitoral("123"));

            assertThatThrownBy(() -> service.criarAtribuicaoTemporaria(codUnidade, request))
                    .isInstanceOf(ErroValidacao.class);

            verify(atribuicaoTemporariaRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Buscar responsável atual")
    class BuscarResponsavelAtualTests {

        @Test
        @DisplayName("Deve buscar responsável atual com sucesso")
        void deveBuscarResponsavelAtualComSucesso() {

            String siglaUnidade = "ABC";

            Unidade unidade = new Unidade();
            unidade.setCodigo(1L);
            unidade.setSigla(siglaUnidade);

            Responsabilidade resp = new Responsabilidade();
            resp.setUnidadeCodigo(1L);
            resp.setUsuarioTitulo("123456789012");

            Usuario usuarioCompleto = new Usuario();
            usuarioCompleto.setTituloEleitoral("123456789012");
            usuarioCompleto.setNome("João silva");

            when(unidadeRepo.buscarCodigoAtivoPorSigla(siglaUnidade)).thenReturn(Optional.of(1L));
            when(responsabilidadeRepo.buscarLeituraDetalhadaPorCodigoUnidade(1L))
                    .thenReturn(Optional.of(new ResponsabilidadeUnidadeLeitura(1L, "123456789012", null, null, null, null)));
            when(repo.buscar(Usuario.class, "123456789012")).thenReturn(usuarioCompleto);

            Usuario resultado = service.buscarResponsavelAtual(siglaUnidade);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getTituloEleitoral()).isEqualTo("123456789012");
        }
    }

    @Nested
    @DisplayName("Buscar responsável de Unidade")
    class BuscarResponsavelUnidadeTests {

        @Test
        @DisplayName("Deve buscar responsável com titular e substituto")
        void deveBuscarResponsavelComTitularESubstituto() {

            Long unidadeCodigo = 1L;

            Usuario titularOficial = new Usuario();
            titularOficial.setTituloEleitoral("111111111111");
            titularOficial.setNome("João silva");

            Usuario substituto = new Usuario();
            substituto.setTituloEleitoral("222222222222");
            substituto.setNome("Maria santos");

            Unidade unidade = new Unidade();
            unidade.setCodigo(unidadeCodigo);
            unidade.setTituloTitular("111111111111");

            Responsabilidade responsabilidade = new Responsabilidade();
            responsabilidade.setUnidadeCodigo(unidadeCodigo);
            responsabilidade.setUsuarioTitulo("222222222222");
            responsabilidade.setUnidade(unidade);

            when(responsabilidadeRepo.buscarLeituraDetalhadaPorCodigoUnidade(unidadeCodigo))
                    .thenReturn(Optional.of(new ResponsabilidadeUnidadeLeitura(unidadeCodigo, "222222222222", "111111111111", null, null, null)));
            when(repo.buscar(Usuario.class, "222222222222")).thenReturn(substituto);
            when(repo.buscar(Usuario.class, "111111111111")).thenReturn(titularOficial);

            UnidadeResponsavelDto resultado = service.buscarResponsavelUnidade(unidadeCodigo);

            assertThat(resultado).isNotNull();
            assertThat(resultado.titularTitulo()).isEqualTo("111111111111");
            assertThat(resultado.substitutoTitulo()).isEqualTo("222222222222");
        }

        @Test
        @DisplayName("Deve buscar responsável quando titular oficial é o próprio responsável")
        void deveBuscarResponsavelQuandoTitularEhOProprioResponsavel() {
            Long unidadeCodigo = 1L;

            Usuario titular = new Usuario();
            titular.setTituloEleitoral("111111111111");
            titular.setNome("João Silva");

            Unidade unidade = new Unidade();
            unidade.setCodigo(unidadeCodigo);
            unidade.setTituloTitular("111111111111");

            Responsabilidade responsabilidade = new Responsabilidade();
            responsabilidade.setUnidadeCodigo(unidadeCodigo);
            responsabilidade.setUsuarioTitulo("111111111111");
            responsabilidade.setUnidade(unidade);

            when(responsabilidadeRepo.buscarLeituraDetalhadaPorCodigoUnidade(unidadeCodigo))
                    .thenReturn(Optional.of(new ResponsabilidadeUnidadeLeitura(unidadeCodigo, "111111111111", "111111111111", null, null, null)));
            when(repo.buscar(Usuario.class, "111111111111")).thenReturn(titular);

            UnidadeResponsavelDto resultado = service.buscarResponsavelUnidade(unidadeCodigo);

            assertThat(resultado.titularTitulo()).isEqualTo("111111111111");
            assertThat(resultado.substitutoTitulo()).isNull();
            assertThat(resultado.substitutoNome()).isNull();
        }
    }

    @Nested
    @DisplayName("Buscar responsáveis em lote")
    class BuscarResponsaveisEmLoteTests {

        @Test
        @DisplayName("Deve retornar mapa vazio quando lista de unidades vazia")
        void deveRetornarVazioQuandoListaVazia() {
            var result = service.buscarResponsaveisUnidades(List.of());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar mapa vazio quando nenhuma responsabilidade encontrada")
        void deveRetornarVazioQuandoNaoEncontrado() {
            when(responsabilidadeRepo.listarLeiturasDetalhadasPorCodigosUnidade(anyList())).thenReturn(List.of());
            var result = service.buscarResponsaveisUnidades(List.of(1L));
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Deve lançar exceção quando responsável não é encontrado no repositório de usuários")
        void deveLancarExcecaoQuandoUsuarioAusente() {
            Long codUnidade = 1L;
            when(responsabilidadeRepo.listarLeiturasDetalhadasPorCodigosUnidade(anyList()))
                    .thenReturn(List.of(new ResponsabilidadeUnidadeLeitura(codUnidade, "RESP", "TITULAR", null, null, null)));
            // Simular que o usuário "RESP" ou "TITULAR" não foi carregado
            when(usuarioRepo.listarPorTitulosComUnidadeLotacao(anyList())).thenReturn(List.of());

            assertThatThrownBy(() -> service.buscarResponsaveisUnidades(List.of(codUnidade)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Responsável ou titular oficial ausente");
        }

        @Test
        @DisplayName("Deve lançar exceção quando apenas o titular oficial estiver ausente")
        void deveLancarExcecaoQuandoTitularOficialAusente() {
            Long codUnidade = 1L;
            Usuario responsavel = new Usuario();
            responsavel.setTituloEleitoral("RESP");

            when(responsabilidadeRepo.listarLeiturasDetalhadasPorCodigosUnidade(anyList()))
                    .thenReturn(List.of(new ResponsabilidadeUnidadeLeitura(codUnidade, "RESP", "TITULAR", null, null, null)));
            when(usuarioRepo.listarPorTitulosComUnidadeLotacao(anyList())).thenReturn(List.of(responsavel));

            assertThatThrownBy(() -> service.buscarResponsaveisUnidades(List.of(codUnidade)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Responsável ou titular oficial ausente");
        }

        @Test
        @DisplayName("Deve retornar mapa com responsáveis quando tudo ok")
        void deveRetornarResponsaveisComSucesso() {
            Long codUnidade = 1L;
            Usuario uTit = new Usuario(); uTit.setTituloEleitoral("TIT"); uTit.setNome("Titular");
            Usuario uRes = new Usuario(); uRes.setTituloEleitoral("RES"); uRes.setNome("Responsavel");

            when(responsabilidadeRepo.listarLeiturasDetalhadasPorCodigosUnidade(anyList()))
                    .thenReturn(List.of(new ResponsabilidadeUnidadeLeitura(codUnidade, "RES", "TIT", null, null, null)));
            when(usuarioRepo.listarPorTitulosComUnidadeLotacao(anyList())).thenReturn(List.of(uTit, uRes));

            var result = service.buscarResponsaveisUnidades(List.of(codUnidade));

            assertThat(result).hasSize(1);
            assertThat(result.get(codUnidade).titularTitulo()).isEqualTo("TIT");
            assertThat(result.get(codUnidade).substitutoTitulo()).isEqualTo("RES");
        }

        @Test
        @DisplayName("Deve lançar exceção quando titular oficial estiver nulo")
        void deveLancarExcecaoQuandoTitularOficialNulo() {
            Long codUnidade = 2L;
            when(responsabilidadeRepo.listarLeiturasDetalhadasPorCodigosUnidade(anyList()))
                    .thenReturn(List.of(new ResponsabilidadeUnidadeLeitura(codUnidade, "RESP", null, null, null, null)));

            assertThatThrownBy(() -> service.buscarResponsaveisUnidades(List.of(codUnidade)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Titular oficial ausente");
        }

        @Test
        @DisplayName("Deve lançar exceção quando titular oficial estiver em branco")
        void deveLancarExcecaoQuandoTitularOficialEmBranco() {
            Long codUnidade = 3L;
            when(responsabilidadeRepo.listarLeiturasDetalhadasPorCodigosUnidade(anyList()))
                    .thenReturn(List.of(new ResponsabilidadeUnidadeLeitura(codUnidade, "RESP", "   ", null, null, null)));

            assertThatThrownBy(() -> service.buscarResponsaveisUnidades(List.of(codUnidade)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Titular oficial ausente");
        }
    }

    @Test
    @DisplayName("todasPossuemResponsavelEfetivo deve retornar false para responsável com título em branco")
    void todasPossuemResponsavelEfetivoDeveRetornarFalseComTituloEmBranco() {
        when(responsabilidadeRepo.listarLeiturasPorCodigosUnidade(List.of(10L)))
                .thenReturn(List.of(new ResponsabilidadeLeitura(10L, " ")));

        boolean resultado = service.todasPossuemResponsavelEfetivo(List.of(10L));

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("todasPossuemResponsavelEfetivo deve retornar false quando faltar unidade na resposta")
    void todasPossuemResponsavelEfetivoDeveRetornarFalseQuandoFaltarUnidade() {
        when(responsabilidadeRepo.listarLeiturasPorCodigosUnidade(List.of(10L, 11L)))
                .thenReturn(List.of(new ResponsabilidadeLeitura(10L, "RESP")));

        boolean resultado = service.todasPossuemResponsavelEfetivo(List.of(10L, 11L));

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("todasPossuemResponsavelEfetivo deve retornar true quando lista estiver vazia")
    void todasPossuemResponsavelEfetivoDeveRetornarTrueQuandoListaVazia() {
        boolean resultado = service.todasPossuemResponsavelEfetivo(List.of());
        assertThat(resultado).isTrue();
    }
}
