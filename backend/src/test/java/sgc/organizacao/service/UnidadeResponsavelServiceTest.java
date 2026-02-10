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
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.repo.ComumRepo;
import sgc.organizacao.dto.*;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("UnidadeResponsavelService")
class UnidadeResponsavelServiceTest {

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Mock
    private AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;

    @Mock
    private ResponsabilidadeRepo responsabilidadeRepo;

    @Mock
    private UsuarioMapper usuarioMapper;

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
            AtribuicaoTemporaria atribuicao1 = new AtribuicaoTemporaria();
            AtribuicaoTemporaria atribuicao2 = new AtribuicaoTemporaria();
            List<AtribuicaoTemporaria> atribuicoes = List.of(atribuicao1, atribuicao2);

            UnidadeDto unidade1 = UnidadeDto.builder().codigo(1L).nome("Unidade 1").build();
            UnidadeDto unidade2 = UnidadeDto.builder().codigo(2L).nome("Unidade 2").build();
            UsuarioDto usuario1 = UsuarioDto.builder().tituloEleitoral("111111111111").nome("User1").build();
            UsuarioDto usuario2 = UsuarioDto.builder().tituloEleitoral("222222222222").nome("User2").build();

            AtribuicaoTemporariaDto dto1 = AtribuicaoTemporariaDto.builder()
                    .codigo(1L)
                    .unidade(unidade1)
                    .usuario(usuario1)
                    .justificativa("Justificativa 1")
                    .build();
            AtribuicaoTemporariaDto dto2 = AtribuicaoTemporariaDto.builder()
                    .codigo(2L)
                    .unidade(unidade2)
                    .usuario(usuario2)
                    .justificativa("Justificativa 2")
                    .build();

            when(atribuicaoTemporariaRepo.findAll()).thenReturn(atribuicoes);
            when(usuarioMapper.toAtribuicaoTemporariaDto(atribuicao1)).thenReturn(dto1);
            when(usuarioMapper.toAtribuicaoTemporariaDto(atribuicao2)).thenReturn(dto2);

            // When
            List<AtribuicaoTemporariaDto> resultado = service.buscarTodasAtribuicoes();

            // Then
            assertThat(resultado).hasSize(2).containsExactly(dto1, dto2);
            verify(atribuicaoTemporariaRepo).findAll();
            verify(usuarioMapper, times(2)).toAtribuicaoTemporariaDto(any());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há atribuições")
        void deveRetornarListaVaziaQuandoNaoHaAtribuicoes() {
            // Given
            when(atribuicaoTemporariaRepo.findAll()).thenReturn(Collections.emptyList());

            // When
            List<AtribuicaoTemporariaDto> resultado = service.buscarTodasAtribuicoes();

            // Then
            assertThat(resultado).isEmpty();
            verify(atribuicaoTemporariaRepo).findAll();
            verify(usuarioMapper, never()).toAtribuicaoTemporariaDto(any());
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

        @Test
        @DisplayName("Deve criar atribuição com dataInicio null usando LocalDate.now()")
        void deveCriarAtribuicaoComDataInicioNullUsandoDataAtual() {
            // Given
            Long codUnidade = 1L;
            LocalDate dataTermino = LocalDate.now().plusDays(30);
            
            CriarAtribuicaoTemporariaRequest request = new CriarAtribuicaoTemporariaRequest(
                    "123456789012",
                    null,  // dataInicio null
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
            assertThat(atribuicao.getDataInicio()).isNotNull();
            // Verifica que a data de início está próxima ao momento atual (tolerância de 1 minuto)
            assertThat(atribuicao.getDataInicio()).isBetween(
                    LocalDateTime.now().minusMinutes(1),
                    LocalDateTime.now().plusMinutes(1)
            );
            assertThat(atribuicao.getDataTermino()).isEqualTo(dataTermino.atTime(23, 59, 59));
        }

        @Test
        @DisplayName("Deve lançar exceção quando dataTermino anterior a dataInicio explícita")
        void deveLancarExcecaoQuandoDataTerminoAnteriorADataInicioExplicita() {
            // Given
            Long codUnidade = 1L;
            LocalDate dataInicio = LocalDate.of(2024, 2, 15);
            LocalDate dataTermino = LocalDate.of(2024, 1, 15); // anterior
            
            CriarAtribuicaoTemporariaRequest request = new CriarAtribuicaoTemporariaRequest(
                    "123456789012",
                    dataInicio,
                    dataTermino,
                    "Cobertura de férias"
            );

            Unidade unidade = new Unidade();
            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral("123456789012");

            when(repo.buscar(Unidade.class, codUnidade)).thenReturn(unidade);
            when(repo.buscar(Usuario.class, "123456789012")).thenReturn(usuario);

            // When / Then
            assertThatThrownBy(() -> service.criarAtribuicaoTemporaria(codUnidade, request))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessage("A data de término deve ser posterior à data de início.");

            verify(atribuicaoTemporariaRepo, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando dataTermino anterior a dataInicio null (now)")
        void deveLancarExcecaoQuandoDataTerminoAnteriorADataInicioNull() {
            // Given
            Long codUnidade = 1L;
            LocalDate dataTermino = LocalDate.now().minusDays(1); // ontem
            
            CriarAtribuicaoTemporariaRequest request = new CriarAtribuicaoTemporariaRequest(
                    "123456789012",
                    null,  // usa LocalDate.now()
                    dataTermino,
                    "Cobertura de férias"
            );

            Unidade unidade = new Unidade();
            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral("123456789012");

            when(repo.buscar(Unidade.class, codUnidade)).thenReturn(unidade);
            when(repo.buscar(Usuario.class, "123456789012")).thenReturn(usuario);

            // When / Then
            assertThatThrownBy(() -> service.criarAtribuicaoTemporaria(codUnidade, request))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessage("A data de término deve ser posterior à data de início.");

            verify(atribuicaoTemporariaRepo, never()).save(any());
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
            when(usuarioPerfilRepo.findByUsuarioTitulo("123456789012")).thenReturn(Collections.emptyList());

            // When
            Usuario resultado = service.buscarResponsavelAtual(siglaUnidade);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getTituloEleitoral()).isEqualTo("123456789012");
            verify(repo).buscarPorSigla(Unidade.class, siglaUnidade);
            verify(repo).buscar(Responsabilidade.class, 1L);
            verify(repo).buscar(Usuario.class, "123456789012");
        }

        @Test
        @DisplayName("Deve lançar exceção quando unidade não encontrada")
        void deveLancarExcecaoQuandoUnidadeNaoEncontrada() {
            // Given
            String siglaUnidade = "INEXISTENTE";
            when(repo.buscarPorSigla(Unidade.class, siglaUnidade))
                    .thenThrow(new ErroEntidadeNaoEncontrada("Unidade", siglaUnidade));

            // When / Then
            assertThatThrownBy(() -> service.buscarResponsavelAtual(siglaUnidade))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Unidade")
                    .hasMessageContaining(siglaUnidade);

            verify(repo).buscarPorSigla(Unidade.class, siglaUnidade);
            verify(repo, never()).buscar(eq(Responsabilidade.class), any());
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
            responsabilidade.setUsuarioTitulo("222222222222"); // Substituto é o responsável atual
            responsabilidade.setUnidade(unidade);

            when(repo.buscar(Responsabilidade.class, unidadeCodigo)).thenReturn(responsabilidade);
            when(repo.buscar(Usuario.class, "222222222222")).thenReturn(substituto);
            when(repo.buscar(Usuario.class, "111111111111")).thenReturn(titularOficial);

            // When
            UnidadeResponsavelDto resultado = service.buscarResponsavelUnidade(unidadeCodigo);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.unidadeCodigo()).isEqualTo(unidadeCodigo);
            assertThat(resultado.titularTitulo()).isEqualTo("111111111111");
            assertThat(resultado.titularNome()).isEqualTo("João Silva");
            assertThat(resultado.substitutoTitulo()).isEqualTo("222222222222");
            assertThat(resultado.substitutoNome()).isEqualTo("Maria Santos");
        }

        @Test
        @DisplayName("Deve buscar responsável com apenas titular (sem substituto)")
        void deveBuscarResponsavelComApenasTitular() {
            // Given
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

            when(repo.buscar(Responsabilidade.class, unidadeCodigo)).thenReturn(responsabilidade);
            when(repo.buscar(Usuario.class, "111111111111")).thenReturn(titular);

            // When
            UnidadeResponsavelDto resultado = service.buscarResponsavelUnidade(unidadeCodigo);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.unidadeCodigo()).isEqualTo(unidadeCodigo);
            assertThat(resultado.titularTitulo()).isEqualTo("111111111111");
            assertThat(resultado.titularNome()).isEqualTo("João Silva");
            assertThat(resultado.substitutoTitulo()).isNull();
            assertThat(resultado.substitutoNome()).isNull();
        }
    }

    @Nested
    @DisplayName("Buscar Responsáveis em Lote")
    class BuscarResponsaveisUnidadesTests {

        @Test
        @DisplayName("Deve retornar mapa vazio quando lista de códigos está vazia")
        void deveRetornarMapaVazioQuandoListaVazia() {
            // Given
            List<Long> unidadesCodigos = Collections.emptyList();

            // When
            Map<Long, UnidadeResponsavelDto> resultado = service.buscarResponsaveisUnidades(unidadesCodigos);

            // Then
            assertThat(resultado).isEmpty();
            verify(responsabilidadeRepo, never()).findByUnidadeCodigoIn(anyList());
        }

        @Test
        @DisplayName("Deve retornar mapa vazio quando não há responsabilidades")
        void deveRetornarMapaVazioQuandoNaoHaResponsabilidades() {
            // Given
            List<Long> unidadesCodigos = List.of(1L, 2L);
            when(responsabilidadeRepo.findByUnidadeCodigoIn(unidadesCodigos)).thenReturn(Collections.emptyList());

            // When
            Map<Long, UnidadeResponsavelDto> resultado = service.buscarResponsaveisUnidades(unidadesCodigos);

            // Then
            assertThat(resultado).isEmpty();
            verify(responsabilidadeRepo).findByUnidadeCodigoIn(unidadesCodigos);
        }

        @Test
        @DisplayName("Deve buscar responsáveis de múltiplas unidades")
        void deveBuscarResponsaveisMultiplasUnidades() {
            // Given
            List<Long> unidadesCodigos = List.of(1L, 2L);

            Unidade u1 = new Unidade(); u1.setCodigo(1L); u1.setTituloTitular("111111111111");
            Unidade u2 = new Unidade(); u2.setCodigo(2L); u2.setTituloTitular("222222222222");

            Responsabilidade r1 = new Responsabilidade(); r1.setUnidadeCodigo(1L); r1.setUsuarioTitulo("111111111111"); r1.setUnidade(u1);
            Responsabilidade r2 = new Responsabilidade(); r2.setUnidadeCodigo(2L); r2.setUsuarioTitulo("333333333333"); r2.setUnidade(u2); // Substituto

            Usuario t1 = criarUsuarioComAtribuicoes("111111111111", "Titular 1");
            Usuario t2 = criarUsuarioComAtribuicoes("222222222222", "Titular 2");
            Usuario s2 = criarUsuarioComAtribuicoes("333333333333", "Substituto 2");

            when(responsabilidadeRepo.findByUnidadeCodigoIn(unidadesCodigos)).thenReturn(List.of(r1, r2));
            when(usuarioRepo.findByIdInWithAtribuicoes(anyList())).thenReturn(List.of(t1, t2, s2));

            // When
            Map<Long, UnidadeResponsavelDto> resultado = service.buscarResponsaveisUnidades(unidadesCodigos);

            // Then
            assertThat(resultado).hasSize(2);
            
            UnidadeResponsavelDto resp1 = resultado.get(1L);
            assertThat(resp1.titularTitulo()).isEqualTo("111111111111");
            assertThat(resp1.substitutoTitulo()).isNull();

            UnidadeResponsavelDto resp2 = resultado.get(2L);
            assertThat(resp2.titularTitulo()).isEqualTo("222222222222");
            assertThat(resp2.substitutoTitulo()).isEqualTo("333333333333");
        }
    }

    // Métodos auxiliares para criar objetos de teste
    
    private Usuario criarUsuarioComAtribuicoes(String titulo, String nome) {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(titulo);
        usuario.setNome(nome);
        return usuario;
    }


}
