package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import sgc.organizacao.dto.DiagnosticoOrganizacionalDto;
import sgc.organizacao.model.UsuarioRepo;
import sgc.organizacao.model.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.service.CacheViewsOrganizacaoService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static sgc.organizacao.model.SituacaoUnidade.ATIVA;
import static sgc.organizacao.model.TipoUnidade.INTERMEDIARIA;
import static sgc.organizacao.model.Perfil.GESTOR;

@ExtendWith(MockitoExtension.class)
class ValidadorDadosOrganizacionaisTest {

    @Mock
    private CacheViewsOrganizacaoService cacheViewsOrganizacaoService;

    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @InjectMocks
    private ValidadorDadosOrganizacionais validador;

    @Test
    @DisplayName("diagnosticar - deve retornar sem violações quando tudo estiver limpo")
    void diagnosticarSemViolacoes() {
        when(cacheViewsOrganizacaoService.listarTodasUnidades()).thenReturn(List.of());

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertFalse(diagnostico.possuiViolacoes());
        assertEquals("", diagnostico.resumo());
    }

    @Test
    @DisplayName("construirResumo - deve pluralizar corretamente")
    void construirResumoPluralizacao() {
        // Acessando via reflexão ou forçando um diagnóstico com violações
        Map<String, List<String>> violacoes = new LinkedHashMap<>();
        violacoes.put("Tipo A", List.of("Ocorrência 1"));
        
        // 1 tipo, 1 ocorrência
        String resumo1 = org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "construirResumo", 1, 1);
        assertTrue(resumo1.contains("1 tipo de inconsistencia"));
        assertTrue(resumo1.contains("1 ocorrencia"));

        // 2 tipos, 2 ocorrências
        String resumo2 = org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "construirResumo", 2, 2);
        assertTrue(resumo2.contains("2 tipos de inconsistencias"));
        assertTrue(resumo2.contains("2 ocorrencias"));
    }

    @Test
    @DisplayName("extrairSigla - deve extrair sigla corretamente de diferentes formatos")
    void extrairSiglaFormatos() {
        String sigla1 = org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "extrairSigla", "erro em sigla=XYZ, tipo=U");
        assertEquals("XYZ", sigla1);

        String sigla2 = org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "extrairSigla", "sigla=ABC");
        assertEquals("ABC", sigla2);

        String sigla3 = org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "extrairSigla", "sem o prefixo");
        assertNull(sigla3);

        String sigla4 = org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "extrairSigla", "sigla=  ");
        assertNull(sigla4);
    }

    @Test
    @DisplayName("processarLinhaPerfil - deve identificar dados nulos na view")
    void processarLinhaPerfilDadosNulos() throws Exception {
        List<Object> perfisInvalidos = new ArrayList<>();
        Map<Long, Set<Object>> perfisPorUnidade = new HashMap<>();
        Map<Object, Integer> contagemPorChave = new HashMap<>();
        
        // Usando reflexão padrão para instanciar o record privado ContextoCargaPerfis
        Class<?> contextoClass = Arrays.stream(ValidadorDadosOrganizacionais.class.getDeclaredClasses())
                .filter(c -> c.getSimpleName().equals("ContextoCargaPerfis"))
                .findFirst()
                .orElseThrow();
        
        java.lang.reflect.Constructor<?> constructor = contextoClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object contexto = constructor.newInstance(perfisInvalidos, perfisPorUnidade, contagemPorChave);

        // Caso 1: usuario_titulo nulo
        Map<String, Object> linha1 = Map.of("perfil", "GESTOR", "unidade_codigo", 1L);
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "processarLinhaPerfil", linha1, contexto);
        
        // Caso 2: perfil nulo
        Map<String, Object> linha2 = Map.of("usuario_titulo", "123", "unidade_codigo", 1L);
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "processarLinhaPerfil", linha2, contexto);

        // Caso 3: unidade_codigo nulo
        Map<String, Object> linha3 = Map.of("usuario_titulo", "123", "perfil", "ADMIN");
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "processarLinhaPerfil", linha3, contexto);

        // Caso 4: perfil inválido
        Map<String, Object> linha4 = Map.of("usuario_titulo", "123", "perfil", "INVALIDO", "unidade_codigo", 1L);
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "processarLinhaPerfil", linha4, contexto);

        assertEquals(4, perfisInvalidos.size());
    }

    @Test
    @DisplayName("diagnosticarTitulosDuplicados - deve detectar títulos duplicados")
    void diagnosticarTitulosDuplicados() {
        // Simular um título duplicado na view
        when(namedParameterJdbcTemplate.queryForList(anyString(), anyMap())).thenReturn(List.of(
                Map.of("titulo", "111", "quantidade", 2L)
        ));

        // Invoca diretamente o método privado com o parâmetro necessário
        Set<String> titulosDuplicados = org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                validador, "diagnosticarTitulosDuplicados", List.of("111"));
        
        assertNotNull(titulosDuplicados);
        assertEquals(1, titulosDuplicados.size());
        assertTrue(titulosDuplicados.contains("111"));
    }

    @Test
    @DisplayName("validarUnidadeIntermediaria - deve detectar inconsistências em unidades intermediárias")
    void validarUnidadeIntermediaria_Inconsistencias() {
        UnidadeHierarquiaLeitura u = new UnidadeHierarquiaLeitura(1L, "U1", "U", "111", INTERMEDIARIA, ATIVA, null);
        Map<String, List<String>> violacoes = new HashMap<>();
        
        // 1. Sem filhas
        Object contexto = instanciarContextoValidacao(Set.of(), Map.of(), Map.of(), violacoes);
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "validarUnidadeIntermediaria", u, contexto);
        assertTrue(violacoes.containsKey("Unidade intermediaria sem filhas ativas participantes"));

        // 2. Sem gestor
        violacoes.clear();
        contexto = instanciarContextoValidacao(Set.of(1L), Map.of(), Map.of(), violacoes);
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "validarUnidadeIntermediaria", u, contexto);
        assertTrue(violacoes.containsKey("Unidade intermediaria sem perfil GESTOR"));

        // 3. Responsável sem perfil gestor
        violacoes.clear();
        ResponsabilidadeLeitura resp = new ResponsabilidadeLeitura(1L, "111");
        Object perfil = instanciarPerfilUsuarioUnidade("222", GESTOR); // Outro usuário
        contexto = instanciarContextoValidacao(Set.of(1L), Map.of(1L, resp), Map.of(1L, Set.of(perfil)), violacoes);
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "validarUnidadeIntermediaria", u, contexto);
        assertTrue(violacoes.containsKey("Responsavel de unidade intermediaria sem perfil GESTOR correspondente"));
    }

    @Test
    @DisplayName("lerLong - deve converter diferentes formatos de número")
    void lerLong_Formatos() {
        // String
        Map<String, Object> l1 = Map.of("unidade_codigo", "123");
        Long v1 = org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "lerLong", l1);
        assertEquals(123L, v1);

        // Integer
        Map<String, Object> l2 = Map.of("UNIDADE_CODIGO", 456);
        Long v2 = org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "lerLong", l2);
        assertEquals(456L, v2);

        // Null
        Map<String, Object> l3 = new HashMap<>();
        l3.put("unidade_codigo", null);
        Long v3 = org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "lerLong", l3);
        assertNull(v3);
        
        // Double (Number)
        Map<String, Object> l4 = Map.of("unidade_codigo", 789.0);
        Long v4 = org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "lerLong", l4);
        assertEquals(789L, v4);
    }

    @Test
    @DisplayName("lerString - deve ler colunas em caixa baixa e em caixa alta")
    void lerString_DeveLerColunasEmDiferentesCaixas() {
        Map<String, Object> m1 = Map.of("test", "val");
        assertEquals("val", org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "lerString", m1, "test"));
        
        Map<String, Object> m2 = Map.of("TEST", "VAL");
        assertEquals("VAL", org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "lerString", m2, "test"));
        assertEquals("VAL", org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "lerString", m2, "TEST"));
        
        assertNull(org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "lerString", Map.of(), "missing"));
    }

    @Test
    @DisplayName("tentarMapearPerfil - deve capturar erro de enum inválido")
    void tentarMapearPerfil_EnumInvalido() {
        List<Object> invalidos = new ArrayList<>();
        Map<Long, Set<Object>> perfis = new HashMap<>();
        Map<Object, Integer> contagem = new HashMap<>();
        Object contexto = instanciarContextoCargaPerfis(invalidos, perfis, contagem);
        
        // Dados com perfil que não existe no Enum Perfil
        Object dados = instanciarDadosLinhaPerfil("111", "NAO_EXISTE", 1L);
        
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "tentarMapearPerfil", dados, contexto);
        
        assertEquals(1, invalidos.size());
        Object erro = invalidos.get(0);
        assertEquals("VW_USUARIO_PERFIL_UNIDADE com perfil invalido", org.springframework.test.util.ReflectionTestUtils.invokeMethod(erro, "tipo"));
        assertEquals(
                "usuario_titulo=111, perfil=NAO_EXISTE, unidade_codigo=1",
                org.springframework.test.util.ReflectionTestUtils.invokeMethod(erro, "detalhe")
        );
    }

    @Test
    @DisplayName("validarIntegridadePerfis - deve filtrar perfis derivados de unidade sem responsável")
    void validarIntegridadePerfis_Filtro() {
        Map<String, List<String>> violacoes = new HashMap<>();
        Set<Long> unidadesSemResp = Set.of(1L);
        
        // Perfil inválido por título nulo em unidade sem responsável -> deve ser filtrado
        Object p1 = instanciarPerfilInvalido("VW_USUARIO_PERFIL_UNIDADE com usuario_titulo nulo", "detalhe", 1L);
        // Perfil inválido por título nulo em unidade COM responsável -> NÃO deve ser filtrado
        Object p2 = instanciarPerfilInvalido("VW_USUARIO_PERFIL_UNIDADE com usuario_titulo nulo", "detalhe", 2L);
        
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "validarIntegridadePerfis", List.of(p1, p2), unidadesSemResp, violacoes);
        
        assertEquals(1, violacoes.size());
        assertTrue(violacoes.containsKey("VW_USUARIO_PERFIL_UNIDADE com usuario_titulo nulo"));
    }

    @Test
    @DisplayName("diagnosticarPerfisInvalidos - deve identificar diferentes tipos de invalidade")
    void diagnosticarPerfisInvalidos_Tipos() {
        List<Object> invalidos = new ArrayList<>();
        Map<Long, Set<Object>> perfisPorUnidade = new HashMap<>();
        Map<Object, Integer> contagem = new HashMap<>();
        
        Object contexto = instanciarContextoCargaPerfis(invalidos, perfisPorUnidade, contagem);
        
        // 1. Título vazio
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "processarLinhaPerfil", Map.of("perfil", "GESTOR", "unidade_codigo", 1L), contexto);
        // 2. Perfil vazio
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "processarLinhaPerfil", Map.of("usuario_titulo", "111", "unidade_codigo", 1L), contexto);
        // 3. Unidade nula
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "processarLinhaPerfil", Map.of("usuario_titulo", "111", "perfil", "GESTOR"), contexto);
        // 4. Perfil inexistente
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(validador, "processarLinhaPerfil", Map.of("usuario_titulo", "111", "perfil", "X", "unidade_codigo", 1L), contexto);
        
        assertEquals(4, invalidos.size());
    }

    @Test
    @DisplayName("diagnosticar - fluxo completo com sucesso")
    void diagnosticar_Sucesso() {
        UnidadeHierarquiaLeitura u = new UnidadeHierarquiaLeitura(1L, "U1", "U1", "111", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, null);
        when(cacheViewsOrganizacaoService.listarTodasUnidades()).thenReturn(List.of(u));
        
        ResponsabilidadeLeitura resp = new ResponsabilidadeLeitura(1L, "111");
        when(cacheViewsOrganizacaoService.listarTodasResponsabilidades()).thenReturn(List.of(resp));
        
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("111");
        when(usuarioRepo.findAllById(anyList())).thenReturn(List.of(usuario));
        when(namedParameterJdbcTemplate.queryForList(anyString(), anyMap())).thenReturn(List.of());
        
        DiagnosticoOrganizacionalDto dto = validador.diagnosticar();
        assertFalse(dto.possuiViolacoes(), "Nao deveria possuir violacoes: " + dto.resumo());
    }

    private Object instanciarContextoCargaPerfis(List<Object> invalidos, Map<Long, Set<Object>> perfis, Map<Object, Integer> contagem) {
        Class<?> clazz = Arrays.stream(ValidadorDadosOrganizacionais.class.getDeclaredClasses())
                .filter(c -> c.getSimpleName().equals("ContextoCargaPerfis"))
                .findFirst().orElseThrow();
        try {
            java.lang.reflect.Constructor<?> cons = clazz.getDeclaredConstructors()[0];
            cons.setAccessible(true);
            return cons.newInstance(invalidos, perfis, contagem);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object instanciarDadosLinhaPerfil(String titulo, String perfilBruto, Long cod) {
        Class<?> clazz = Arrays.stream(ValidadorDadosOrganizacionais.class.getDeclaredClasses())
                .filter(c -> c.getSimpleName().equals("DadosLinhaPerfil"))
                .findFirst().orElseThrow();
        try {
            java.lang.reflect.Constructor<?> cons = clazz.getDeclaredConstructors()[0];
            cons.setAccessible(true);
            return cons.newInstance(titulo, perfilBruto, cod);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object instanciarContextoValidacao(Set<Long> filhas, Map<Long, ResponsabilidadeLeitura> resps, Map<Long, Set<Object>> perfis, Map<String, List<String>> violacoes) {
        Class<?> clazz = Arrays.stream(ValidadorDadosOrganizacionais.class.getDeclaredClasses())
                .filter(c -> c.getSimpleName().equals("ContextoValidacaoIntermediaria"))
                .findFirst().orElseThrow();
        try {
            java.lang.reflect.Constructor<?> cons = clazz.getDeclaredConstructors()[0];
            cons.setAccessible(true);
            return cons.newInstance(filhas, resps, perfis, violacoes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object instanciarPerfilUsuarioUnidade(String titulo, sgc.organizacao.model.Perfil p) {
        Class<?> clazz = Arrays.stream(ValidadorDadosOrganizacionais.class.getDeclaredClasses())
                .filter(c -> c.getSimpleName().equals("PerfilUsuarioUnidade"))
                .findFirst().orElseThrow();
        try {
            java.lang.reflect.Constructor<?> cons = clazz.getDeclaredConstructors()[0];
            cons.setAccessible(true);
            return cons.newInstance(titulo, p);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object instanciarPerfilInvalido(String tipo, String detalhe, Long cod) {
        Class<?> clazz = Arrays.stream(ValidadorDadosOrganizacionais.class.getDeclaredClasses())
                .filter(c -> c.getSimpleName().equals("PerfilInvalido"))
                .findFirst().orElseThrow();
        try {
            java.lang.reflect.Constructor<?> cons = clazz.getDeclaredConstructors()[0];
            cons.setAccessible(true);
            return cons.newInstance(tipo, detalhe, cod);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
