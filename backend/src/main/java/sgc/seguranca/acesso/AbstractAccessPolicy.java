package sgc.seguranca.acesso;

import lombok.RequiredArgsConstructor;
import sgc.organizacao.model.*;
import sgc.organizacao.service.HierarquiaService;

import java.util.EnumSet;
import java.util.Objects;

/**
 * Classe base abstrata para políticas de acesso, centralizando lógica comum
 * de verificação de perfis, hierarquia e mensagens de erro.
 *
 * @param <T> O tipo do recurso protegido.
 */
@RequiredArgsConstructor
public abstract class AbstractAccessPolicy<T> implements AccessPolicy<T> {

    protected final UsuarioPerfilRepo usuarioPerfilRepo;
    protected final HierarquiaService hierarquiaService;
    protected final UnidadeRepo unidadeRepo;
    protected String ultimoMotivoNegacao = "";

    @Override
    public String getMotivoNegacao() {
        return ultimoMotivoNegacao;
    }

    /**
     * Define o motivo da negação de forma padronizada.
     */
    protected void definirMotivoNegacao(Usuario usuario, EnumSet<Perfil> perfisPermitidos, Acao acao) {
        this.ultimoMotivoNegacao = String.format(
                "Usuário '%s' não possui um dos perfis necessários: %s. Ação: %s",
                usuario.getTituloEleitoral(),
                formatarPerfis(perfisPermitidos),
                acao.getDescricao()
        );
    }

    /**
     * Define um motivo de negação customizado.
     */
    protected void definirMotivoNegacao(String motivo) {
        this.ultimoMotivoNegacao = motivo;
    }

    protected boolean temPerfilPermitido(Usuario usuario, EnumSet<Perfil> perfisPermitidos) {
        return perfisPermitidos.contains(usuario.getPerfilAtivo());
    }

    protected String formatarPerfis(EnumSet<Perfil> perfis) {
        return perfis.stream()
                .map(Perfil::name)
                .reduce((a, b) -> a + ", " + b)
                .orElse("nenhum");
    }

    /**
     * Verifica se o usuário possui o perfil especificado.
     */
    protected boolean temPerfil(Usuario usuario, Perfil perfil) {
        return usuario.getPerfilAtivo() == perfil;
    }

    /**
     * Verifica se o usuário atende ao requisito de hierarquia em relação à unidade.
     *
     * @param usuario  O usuário a ser verificado
     * @param unidade  A unidade do recurso
     * @param requisito O tipo de requisito de hierarquia
     * @return true se o requisito é atendido
     */
    protected boolean verificaHierarquia(Usuario usuario, Unidade unidade, RequisitoHierarquia requisito) {
        final Long codUnidadeRecurso = unidade.getCodigo();
        final Long codUnidadeUsuario = usuario.getUnidadeAtivaCodigo();

        return switch (requisito) {
            case NENHUM -> true;
            case MESMA_UNIDADE -> Objects.equals(codUnidadeUsuario, codUnidadeRecurso);

            case MESMA_OU_SUBORDINADA -> {
                if (Objects.equals(codUnidadeUsuario, codUnidadeRecurso)) {
                    yield true;
                }
                // Buscar unidade completa do usuário para verificar hierarquia
                Unidade unidadeUsuario = unidadeRepo.findById(codUnidadeUsuario)
                        .orElse(null);
                if (unidadeUsuario == null) {
                    yield false;
                }
                yield hierarquiaService.isSubordinada(unidade, unidadeUsuario);
            }

            case SUPERIOR_IMEDIATA -> {
                // Buscar unidade completa do usuário para verificar hierarquia
                Unidade unidadeUsuario = unidadeRepo.findById(codUnidadeUsuario)
                        .orElse(null);
                if (unidadeUsuario == null) {
                    yield false;
                }
                yield hierarquiaService.isSuperiorImediata(unidade, unidadeUsuario);
            }

            case TITULAR_UNIDADE -> {
                String tituloTitular = unidade.getTituloTitular();
                yield tituloTitular.equals(usuario.getTituloEleitoral());
            }
        };
    }

    /**
     * Obtém a mensagem de erro apropriada para negação de acesso por hierarquia.
     *
     * @param usuario  O usuário negado
     * @param unidade  A unidade do recurso
     * @param requisito O tipo de requisito que não foi atendido
     * @return Mensagem descritiva do motivo da negação
     */
    protected String obterMotivoNegacaoHierarquia(
            Usuario usuario, Unidade unidade, RequisitoHierarquia requisito) {

        String siglaUnidade = unidade.getSigla();

        return switch (requisito) {
            case NENHUM -> "Erro inesperado na verificação de hierarquia";

            case MESMA_UNIDADE -> String.format(
                    "Usuário '%s' não pertence à unidade '%s' do recurso",
                    usuario.getTituloEleitoral(), siglaUnidade);

            case MESMA_OU_SUBORDINADA -> String.format(
                    "Usuário '%s' não pertence à unidade '%s' nem a uma unidade superior na hierarquia",
                    usuario.getTituloEleitoral(), siglaUnidade);

            case SUPERIOR_IMEDIATA -> String.format(
                    "Usuário '%s' não pertence à unidade superior imediata da unidade '%s'",
                    usuario.getTituloEleitoral(), siglaUnidade);

            case TITULAR_UNIDADE -> {
                String tituloTitular = unidade.getTituloTitular();
                yield String.format(
                        "Usuário '%s' não é o titular da unidade '%s'. Titular: %s",
                        usuario.getTituloEleitoral(), siglaUnidade, tituloTitular);
            }
        };
    }

    /**
     * Enum para requisitos de hierarquia de unidades
     */
    protected enum RequisitoHierarquia {
        NENHUM,             // Sem verificação de hierarquia
        MESMA_UNIDADE,      // Usuário deve estar na mesma unidade
        MESMA_OU_SUBORDINADA, // Usuário deve estar na mesma unidade ou em unidade superior
        SUPERIOR_IMEDIATA,  // Usuário deve estar na unidade superior imediata
        TITULAR_UNIDADE     // Usuário deve ser o titular da unidade
    }
}
