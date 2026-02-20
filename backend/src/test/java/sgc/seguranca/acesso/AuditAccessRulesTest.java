package sgc.seguranca.acesso;

import org.junit.jupiter.api.Test;
import sgc.organizacao.model.Perfil;
import sgc.subprocesso.model.SituacaoSubprocesso;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

class AuditAccessRulesTest {

    @Test
    void auditSubprocessoAccessPolicy() throws Exception {
        // Path relative to module root (backend/)
        Path outputPath = Paths.get("etc/scripts/access-audit-output.md");
        // Ensure directory exists
        Path parent = outputPath.getParent();
        if (parent != null) {
            java.io.File parentDir = parent.toFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw new RuntimeException("Falha ao criar diretório para relatório de auditoria: " + parentDir);
            }
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(outputPath.toFile(), java.nio.charset.StandardCharsets.UTF_8))) {
            out.println("# Relatório de Regras de Acesso (Gerado Automaticamente)");
            out.println("\n## SubprocessoAccessPolicy\n");
            out.println("| Ação | Perfis Permitidos | Situações Permitidas | Requisito Hierarquia |");
            out.println("|---|---|---|---|");

            Field regrasField = SubprocessoAccessPolicy.class.getDeclaredField("REGRAS");
            regrasField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<Acao, Object> regras = (Map<Acao, Object>) regrasField.get(null);

            regras.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> printRule(entry.getKey(), entry.getValue(), out));

            System.out.println("Relatório gerado em: " + outputPath.toAbsolutePath());
        }
    }

    private void printRule(Acao acao, Object regra, PrintWriter out) {
        try {
            Class<?> regraClass = regra.getClass();

            Field perfisField = regraClass.getDeclaredField("perfisPermitidos");
            perfisField.setAccessible(true);
            @SuppressWarnings("unchecked")
            EnumSet<Perfil> perfis = (EnumSet<Perfil>) perfisField.get(regra);

            Field situacoesField = regraClass.getDeclaredField("situacoesPermitidas");
            situacoesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            EnumSet<SituacaoSubprocesso> situacoes = (EnumSet<SituacaoSubprocesso>) situacoesField.get(regra);

            Field hierarquiaField = regraClass.getDeclaredField("requisitoHierarquia");
            hierarquiaField.setAccessible(true);
            Object hierarquia = hierarquiaField.get(regra);

            String perfisStr = perfis.stream().map(Enum::name).collect(Collectors.joining(", "));
            String situacoesStr = situacoes.size() == SituacaoSubprocesso.values().length ? "TODAS" :
                    situacoes.stream().map(Enum::name).collect(Collectors.joining(", "));

            // Format for Markdown table
            out.printf("| %s | %s | %s | %s |%n",
                    acao, perfisStr, situacoesStr, hierarquia);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
