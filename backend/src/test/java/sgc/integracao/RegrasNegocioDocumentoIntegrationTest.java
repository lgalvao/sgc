package sgc.integracao;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

import static org.assertj.core.api.Assertions.*;

@Tag("integration")
@DisplayName("Integração do documento de regras de negócio")
class RegrasNegocioDocumentoIntegrationTest {
    private static final Pattern PADRAO_REGRA = Pattern.compile("\\*\\*(RN-\\d{2}\\.\\d{2})\\*\\*\\s*—\\s*(.+?)\\n> Fonte:\\s*(.+)", Pattern.DOTALL);
    private static final Pattern PADRAO_ARQUIVO_FONTE = Pattern.compile("`([^`]+\\.md)`");
    private static final Pattern PADRAO_CDU = Pattern.compile("cdu-(\\d{2})\\.md");

    private static Path raizRepositorio;
    private static String conteudoRegrasNegocio;

    @BeforeAll
    static void carregarDocumento() throws IOException {
        raizRepositorio = localizarRaizRepositorio();
        Path arquivoRegras = raizRepositorio.resolve("etc/reqs/regras-negocio.md");
        assertThat(arquivoRegras)
                .as("Arquivo de regras de negócio deve existir")
                .exists();
        conteudoRegrasNegocio = Files.readString(arquivoRegras);
    }

    @Test
    @DisplayName("Deve ter regras RN únicas e com texto não vazio")
    void deveTerRegrasUnicasComTextoPreenchido() {
        Matcher matcher = PADRAO_REGRA.matcher(conteudoRegrasNegocio);
        Set<String> codigos = new LinkedHashSet<>();
        List<String> duplicados = new ArrayList<>();

        while (matcher.find()) {
            String codigo = matcher.group(1).trim();
            String descricao = matcher.group(2).trim();
            String fonte = matcher.group(3).trim();

            if (!codigos.add(codigo)) {
                duplicados.add(codigo);
            }

            assertThat(descricao)
                    .as("Descrição da regra %s deve estar preenchida", codigo)
                    .isNotBlank();
            assertThat(fonte)
                    .as("Fonte da regra %s deve estar preenchida", codigo)
                    .isNotBlank();
        }

        assertThat(codigos)
                .as("Documento deve conter regras RN")
                .isNotEmpty();
        assertThat(duplicados)
                .as("Documento não deve repetir códigos RN")
                .isEmpty();
    }

    @Test
    @DisplayName("Cada regra deve referenciar arquivos de requisito existentes")
    void deveReferenciarArquivosDeRequisitosExistentes() {
        Matcher regrasMatcher = PADRAO_REGRA.matcher(conteudoRegrasNegocio);
        int regrasValidadas = 0;

        while (regrasMatcher.find()) {
            String codigo = regrasMatcher.group(1).trim();
            String blocoFonte = regrasMatcher.group(3);
            Matcher fontesMatcher = PADRAO_ARQUIVO_FONTE.matcher(blocoFonte);
            List<String> fontes = new ArrayList<>();
            while (fontesMatcher.find()) {
                fontes.add(fontesMatcher.group(1));
            }

            assertThat(fontes)
                    .as("Regra %s deve apontar ao menos um arquivo de fonte", codigo)
                    .isNotEmpty();

            for (String fonte : fontes) {
                Path caminhoFonte = resolverFonteMarkdown(fonte);
                assertThat(caminhoFonte)
                        .as("Regra %s deve referenciar arquivo existente: %s", codigo, fonte)
                        .exists();
            }

            regrasValidadas++;
        }

        assertThat(regrasValidadas)
                .as("Quantidade de regras validadas")
                .isGreaterThan(0);
    }

    @Test
    @DisplayName("Toda referência de CDU em regra deve ter teste de integração dedicado")
    void deveTerTesteIntegracaoParaCadaCduReferenciadoNasRegras() throws IOException {
        Set<String> cdusReferenciados = extrairCdusReferenciadosNoDocumento();
        Set<String> classesIntegracao = listarClassesIntegracaoCdu();

        List<String> ausentes = cdusReferenciados.stream()
                .filter(codigo -> !classesIntegracao.contains(codigo))
                .sorted()
                .map(codigo -> "CDU" + codigo + "IntegrationTest")
                .toList();

        assertThat(cdusReferenciados)
                .as("Regras de negócio devem referenciar CDUs")
                .isNotEmpty();
        assertThat(ausentes)
                .as("Cada CDU citado nas regras precisa de teste de integração dedicado")
                .isEmpty();
    }

    private static Set<String> extrairCdusReferenciadosNoDocumento() {
        Matcher matcher = PADRAO_CDU.matcher(conteudoRegrasNegocio);
        Set<String> codigos = new TreeSet<>();
        while (matcher.find()) {
            codigos.add(matcher.group(1));
        }
        return codigos;
    }

    private static Set<String> listarClassesIntegracaoCdu() throws IOException {
        Path pastaIntegracao = raizRepositorio.resolve("backend/src/test/java/sgc/integracao");
        try (Stream<Path> caminhos = Files.list(pastaIntegracao)) {
            return caminhos
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(PADRAO_NOME_CLASSE_INTEGRACAO::matcher)
                    .filter(Matcher::matches)
                    .map(matcher -> matcher.group(1))
                    .collect(Collectors.toCollection(TreeSet::new));
        }
    }

    private static final Pattern PADRAO_NOME_CLASSE_INTEGRACAO = Pattern.compile("CDU(\\d{2})IntegrationTest\\.java");

    private static Path localizarRaizRepositorio() {
        Path atual = Paths.get("").toAbsolutePath().normalize();
        while (atual != null) {
            if (Files.exists(atual.resolve("etc/reqs/regras-negocio.md"))) {
                return atual;
            }
            atual = atual.getParent();
        }
        throw new IllegalStateException("Não foi possível localizar a raiz do repositório do SGC.");
    }

    private static Path resolverFonteMarkdown(String fonte) {
        if (fonte.startsWith("etc/")) {
            return raizRepositorio.resolve(fonte);
        }
        if (fonte.startsWith("views/") || fonte.startsWith("design/") || fonte.startsWith("cdu-") || fonte.startsWith("_")) {
            return raizRepositorio.resolve("etc/reqs").resolve(fonte);
        }
        return raizRepositorio.resolve(fonte);
    }
}
