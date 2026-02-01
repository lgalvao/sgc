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
import sgc.organizacao.dto.AtribuicaoTemporariaDto;
import sgc.organizacao.dto.CriarAtribuicaoTemporariaRequest;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
            assertThat(resultado).hasSize(2);
            assertThat(resultado).containsExactly(dto1, dto2);
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

            Usuario usuarioSimples = new Usuario();
            usuarioSimples.setTituloEleitoral("123456789012");

            Usuario usuarioCompleto = new Usuario();
            usuarioCompleto.setTituloEleitoral("123456789012");
            usuarioCompleto.setNome("João Silva");

            when(unidadeRepo.findBySigla(siglaUnidade)).thenReturn(Optional.of(unidade));
            when(usuarioRepo.chefePorCodUnidade(1L)).thenReturn(Optional.of(usuarioSimples));
            when(usuarioRepo.findByIdWithAtribuicoes("123456789012")).thenReturn(Optional.of(usuarioCompleto));
            when(usuarioPerfilRepo.findByUsuarioTitulo("123456789012")).thenReturn(Collections.emptyList());

            // When
            Usuario resultado = service.buscarResponsavelAtual(siglaUnidade);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getTituloEleitoral()).isEqualTo("123456789012");
            verify(unidadeRepo).findBySigla(siglaUnidade);
            verify(usuarioRepo).chefePorCodUnidade(1L);
            verify(usuarioRepo).findByIdWithAtribuicoes("123456789012");
            verify(usuarioPerfilRepo).findByUsuarioTitulo("123456789012");
        }

        @Test
        @DisplayName("Deve lançar exceção quando unidade não encontrada")
        void deveLancarExcecaoQuandoUnidadeNaoEncontrada() {
            // Given
            String siglaUnidade = "INEXISTENTE";
            when(unidadeRepo.findBySigla(siglaUnidade)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> service.buscarResponsavelAtual(siglaUnidade))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Unidade")
                    .hasMessageContaining(siglaUnidade);

            verify(unidadeRepo).findBySigla(siglaUnidade);
            verify(usuarioRepo, never()).chefePorCodUnidade(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando responsável não encontrado")
        void deveLancarExcecaoQuandoResponsavelNaoEncontrado() {
            // Given
            String siglaUnidade = "ABC";
            
            Unidade unidade = new Unidade();
            unidade.setCodigo(1L);
            unidade.setSigla(siglaUnidade);

            when(unidadeRepo.findBySigla(siglaUnidade)).thenReturn(Optional.of(unidade));
            when(usuarioRepo.chefePorCodUnidade(1L)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> service.buscarResponsavelAtual(siglaUnidade))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Responsável da unidade")
                    .hasMessageContaining(siglaUnidade);

            verify(unidadeRepo).findBySigla(siglaUnidade);
            verify(usuarioRepo).chefePorCodUnidade(1L);
            verify(usuarioRepo, never()).findByIdWithAtribuicoes(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário completo não encontrado")
        void deveLancarExcecaoQuandoUsuarioCompletoNaoEncontrado() {
            // Given
            String siglaUnidade = "ABC";
            
            Unidade unidade = new Unidade();
            unidade.setCodigo(1L);
            unidade.setSigla(siglaUnidade);

            Usuario usuarioSimples = new Usuario();
            usuarioSimples.setTituloEleitoral("123456789012");

            when(unidadeRepo.findBySigla(siglaUnidade)).thenReturn(Optional.of(unidade));
            when(usuarioRepo.chefePorCodUnidade(1L)).thenReturn(Optional.of(usuarioSimples));
            when(usuarioRepo.findByIdWithAtribuicoes("123456789012")).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> service.buscarResponsavelAtual(siglaUnidade))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Usuário")
                    .hasMessageContaining("123456789012");

            verify(unidadeRepo).findBySigla(siglaUnidade);
            verify(usuarioRepo).chefePorCodUnidade(1L);
            verify(usuarioRepo).findByIdWithAtribuicoes("123456789012");
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

            Usuario titular = new Usuario();
            titular.setTituloEleitoral("111111111111");
            titular.setNome("João Silva");

            Usuario substituto = new Usuario();
            substituto.setTituloEleitoral("222222222222");
            substituto.setNome("Maria Santos");

            List<Usuario> chefes = List.of(titular, substituto);

            when(usuarioRepo.findChefesByUnidadesCodigos(List.of(unidadeCodigo))).thenReturn(chefes);

            // When
            UnidadeResponsavelDto resultado = service.buscarResponsavelUnidade(unidadeCodigo);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.unidadeCodigo()).isEqualTo(unidadeCodigo);
            assertThat(resultado.titularTitulo()).isEqualTo("111111111111");
            assertThat(resultado.titularNome()).isEqualTo("João Silva");
            assertThat(resultado.substitutoTitulo()).isEqualTo("222222222222");
            assertThat(resultado.substitutoNome()).isEqualTo("Maria Santos");
            verify(usuarioRepo).findChefesByUnidadesCodigos(List.of(unidadeCodigo));
        }

        @Test
        @DisplayName("Deve buscar responsável com apenas titular (sem substituto)")
        void deveBuscarResponsavelComApenasTitular() {
            // Given
            Long unidadeCodigo = 1L;

            Usuario titular = new Usuario();
            titular.setTituloEleitoral("111111111111");
            titular.setNome("João Silva");

            List<Usuario> chefes = List.of(titular);

            when(usuarioRepo.findChefesByUnidadesCodigos(List.of(unidadeCodigo))).thenReturn(chefes);

            // When
            UnidadeResponsavelDto resultado = service.buscarResponsavelUnidade(unidadeCodigo);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.unidadeCodigo()).isEqualTo(unidadeCodigo);
            assertThat(resultado.titularTitulo()).isEqualTo("111111111111");
            assertThat(resultado.titularNome()).isEqualTo("João Silva");
            assertThat(resultado.substitutoTitulo()).isNull();
            assertThat(resultado.substitutoNome()).isNull();
            verify(usuarioRepo).findChefesByUnidadesCodigos(List.of(unidadeCodigo));
        }

        @Test
        @DisplayName("Deve lançar exceção quando lista de chefes está vazia")
        void deveLancarExcecaoQuandoListaChefesVazia() {
            // Given
            Long unidadeCodigo = 1L;
            when(usuarioRepo.findChefesByUnidadesCodigos(List.of(unidadeCodigo))).thenReturn(Collections.emptyList());

            // When / Then
            assertThatThrownBy(() -> service.buscarResponsavelUnidade(unidadeCodigo))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Responsável da unidade")
                    .hasMessageContaining("1");

            verify(usuarioRepo).findChefesByUnidadesCodigos(List.of(unidadeCodigo));
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
            verify(usuarioRepo, never()).findChefesByUnidadesCodigos(anyList());
        }

        @Test
        @DisplayName("Deve retornar mapa vazio quando não há chefes")
        void deveRetornarMapaVazioQuandoNaoHaChefes() {
            // Given
            List<Long> unidadesCodigos = List.of(1L, 2L);
            when(usuarioRepo.findChefesByUnidadesCodigos(unidadesCodigos)).thenReturn(Collections.emptyList());

            // When
            Map<Long, UnidadeResponsavelDto> resultado = service.buscarResponsaveisUnidades(unidadesCodigos);

            // Then
            assertThat(resultado).isEmpty();
            verify(usuarioRepo).findChefesByUnidadesCodigos(unidadesCodigos);
            verify(usuarioRepo, never()).findByIdInWithAtribuicoes(anyList());
        }

        @Test
        @DisplayName("Deve buscar responsáveis de múltiplas unidades com titular apenas")
        void deveBuscarResponsaveisMultiplasUnidadesComApenasTitular() {
            // Given
            List<Long> unidadesCodigos = List.of(1L, 2L);

            Usuario titular1 = criarUsuarioComAtribuicoes("111111111111", "João Silva", 1L);
            Usuario titular2 = criarUsuarioComAtribuicoes("222222222222", "Maria Santos", 2L);

            List<Usuario> todosChefes = List.of(titular1, titular2);
            List<Usuario> chefesCompletos = List.of(titular1, titular2);

            when(usuarioRepo.findChefesByUnidadesCodigos(unidadesCodigos)).thenReturn(todosChefes);
            when(usuarioRepo.findByIdInWithAtribuicoes(List.of("111111111111", "222222222222")))
                    .thenReturn(chefesCompletos);
            when(usuarioPerfilRepo.findByUsuarioTitulo("111111111111"))
                    .thenReturn(criarPerfisChefeUnidade(titular1, 1L));
            when(usuarioPerfilRepo.findByUsuarioTitulo("222222222222"))
                    .thenReturn(criarPerfisChefeUnidade(titular2, 2L));

            // When
            Map<Long, UnidadeResponsavelDto> resultado = service.buscarResponsaveisUnidades(unidadesCodigos);

            // Then
            assertThat(resultado).hasSize(2);
            
            UnidadeResponsavelDto resp1 = resultado.get(1L);
            assertThat(resp1).isNotNull();
            assertThat(resp1.unidadeCodigo()).isEqualTo(1L);
            assertThat(resp1.titularTitulo()).isEqualTo("111111111111");
            assertThat(resp1.titularNome()).isEqualTo("João Silva");
            assertThat(resp1.substitutoTitulo()).isNull();
            assertThat(resp1.substitutoNome()).isNull();

            UnidadeResponsavelDto resp2 = resultado.get(2L);
            assertThat(resp2).isNotNull();
            assertThat(resp2.unidadeCodigo()).isEqualTo(2L);
            assertThat(resp2.titularTitulo()).isEqualTo("222222222222");
            assertThat(resp2.titularNome()).isEqualTo("Maria Santos");
            assertThat(resp2.substitutoTitulo()).isNull();
            assertThat(resp2.substitutoNome()).isNull();
        }

        @Test
        @DisplayName("Deve buscar responsáveis com titular e substituto")
        void deveBuscarResponsaveisComTitularESubstituto() {
            // Given
            List<Long> unidadesCodigos = List.of(1L);

            Usuario titular = criarUsuarioComAtribuicoes("111111111111", "João Silva", 1L);
            Usuario substituto = criarUsuarioComAtribuicoes("222222222222", "Maria Santos", 1L);

            List<Usuario> todosChefes = List.of(titular, substituto);
            List<Usuario> chefesCompletos = List.of(titular, substituto);

            when(usuarioRepo.findChefesByUnidadesCodigos(unidadesCodigos)).thenReturn(todosChefes);
            when(usuarioRepo.findByIdInWithAtribuicoes(List.of("111111111111", "222222222222")))
                    .thenReturn(chefesCompletos);
            when(usuarioPerfilRepo.findByUsuarioTitulo("111111111111"))
                    .thenReturn(criarPerfisChefeUnidade(titular, 1L));
            when(usuarioPerfilRepo.findByUsuarioTitulo("222222222222"))
                    .thenReturn(criarPerfisChefeUnidade(substituto, 1L));

            // When
            Map<Long, UnidadeResponsavelDto> resultado = service.buscarResponsaveisUnidades(unidadesCodigos);

            // Then
            assertThat(resultado).hasSize(1);
            
            UnidadeResponsavelDto resp = resultado.get(1L);
            assertThat(resp).isNotNull();
            assertThat(resp.unidadeCodigo()).isEqualTo(1L);
            assertThat(resp.titularTitulo()).isEqualTo("111111111111");
            assertThat(resp.titularNome()).isEqualTo("João Silva");
            assertThat(resp.substitutoTitulo()).isEqualTo("222222222222");
            assertThat(resp.substitutoNome()).isEqualTo("Maria Santos");
        }

        @Test
        @DisplayName("Deve filtrar apenas chefes das unidades solicitadas")
        void deveFiltrarApenasChefesDasUnidadesSolicitadas() {
            // Given
            List<Long> unidadesCodigos = List.of(1L, 2L);

            // Usuário com perfil de chefe em múltiplas unidades, mas só queremos 1L e 2L
            Usuario usuario1 = criarUsuarioComAtribuicoes("111111111111", "João Silva", 1L);
            Usuario usuario2 = criarUsuarioComAtribuicoes("222222222222", "Maria Santos", 2L);

            List<Usuario> todosChefes = List.of(usuario1, usuario2);

            when(usuarioRepo.findChefesByUnidadesCodigos(unidadesCodigos)).thenReturn(todosChefes);
            when(usuarioRepo.findByIdInWithAtribuicoes(anyList())).thenReturn(todosChefes);
            
            // Mock para retornar perfis incluindo unidades não solicitadas
            List<UsuarioPerfil> perfis1 = new ArrayList<>();
            perfis1.add(criarUsuarioPerfil(usuario1, 1L, Perfil.CHEFE));
            perfis1.add(criarUsuarioPerfil(usuario1, 99L, Perfil.CHEFE)); // unidade não solicitada
            
            List<UsuarioPerfil> perfis2 = new ArrayList<>();
            perfis2.add(criarUsuarioPerfil(usuario2, 2L, Perfil.CHEFE));
            perfis2.add(criarUsuarioPerfil(usuario2, 88L, Perfil.CHEFE)); // unidade não solicitada

            when(usuarioPerfilRepo.findByUsuarioTitulo("111111111111")).thenReturn(perfis1);
            when(usuarioPerfilRepo.findByUsuarioTitulo("222222222222")).thenReturn(perfis2);

            // When
            Map<Long, UnidadeResponsavelDto> resultado = service.buscarResponsaveisUnidades(unidadesCodigos);

            // Then
            assertThat(resultado).hasSize(2);
            assertThat(resultado).containsOnlyKeys(1L, 2L);
            assertThat(resultado).doesNotContainKeys(88L, 99L);
        }

        @Test
        @DisplayName("Deve filtrar perfis não-CHEFE")
        void deveFiltrarPerfisNaoChefeNaBuscaEmLote() {
            // Given
            List<Long> unidadesCodigos = List.of(1L);

            Usuario usuario1 = criarUsuarioComAtribuicoes("111111111111", "João Silva", 1L);

            List<Usuario> todosChefes = List.of(usuario1);

            when(usuarioRepo.findChefesByUnidadesCodigos(unidadesCodigos)).thenReturn(todosChefes);
            when(usuarioRepo.findByIdInWithAtribuicoes(anyList())).thenReturn(todosChefes);
            
            // Mock com perfis CHEFE e outros perfis (GESTOR, SERVIDOR)
            List<UsuarioPerfil> perfis1 = new ArrayList<>();
            perfis1.add(criarUsuarioPerfil(usuario1, 1L, Perfil.CHEFE)); // deve incluir
            perfis1.add(criarUsuarioPerfil(usuario1, 1L, Perfil.GESTOR)); // deve filtrar
            perfis1.add(criarUsuarioPerfil(usuario1, 1L, Perfil.SERVIDOR)); // deve filtrar

            when(usuarioPerfilRepo.findByUsuarioTitulo("111111111111")).thenReturn(perfis1);

            // When
            Map<Long, UnidadeResponsavelDto> resultado = service.buscarResponsaveisUnidades(unidadesCodigos);

            // Then
            assertThat(resultado).hasSize(1);
            assertThat(resultado).containsKey(1L);
            
            UnidadeResponsavelDto resp = resultado.get(1L);
            assertThat(resp).isNotNull();
            assertThat(resp.titularTitulo()).isEqualTo("111111111111");
        }
    }

    // Métodos auxiliares para criar objetos de teste
    
    private Usuario criarUsuarioComAtribuicoes(String titulo, String nome, Long unidadeCodigo) {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(titulo);
        usuario.setNome(nome);
        return usuario;
    }

    private List<UsuarioPerfil> criarPerfisChefeUnidade(Usuario usuario, Long unidadeCodigo) {
        UsuarioPerfil perfil = criarUsuarioPerfil(usuario, unidadeCodigo, Perfil.CHEFE);
        return List.of(perfil);
    }

    private UsuarioPerfil criarUsuarioPerfil(Usuario usuario, Long unidadeCodigo, Perfil perfil) {
        UsuarioPerfil up = new UsuarioPerfil();
        up.setUsuario(usuario);
        up.setUnidadeCodigo(unidadeCodigo);
        up.setPerfil(perfil);
        return up;
    }
}
