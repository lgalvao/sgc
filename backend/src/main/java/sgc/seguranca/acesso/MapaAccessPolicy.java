package sgc.seguranca.acesso;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;

import java.util.EnumSet;
import java.util.Map;

import static sgc.organizacao.model.Perfil.*;
import static sgc.seguranca.acesso.Acao.*;

/**
 * Política de acesso para operações diretas em mapas.
 * Na prática, a maioria das operações em mapas é feita através de subprocessos.
 * Este policy cobre operações CRUD diretas que podem ser necessárias em
 * casos administrativos ou de manutenção.
 */
@Component
@RequiredArgsConstructor
public class MapaAccessPolicy implements AccessPolicy<Mapa> {

    private String ultimoMotivoNegacao = "";

    /**
     * Mapeamento de ações para regras de acesso.
     * Mapas são gerenciados principalmente por ADMINs para operações diretas.
     */
    private static final Map<Acao, RegrasAcaoMapa> REGRAS = Map.ofEntries(
            Map.entry(LISTAR_MAPAS, new RegrasAcaoMapa(EnumSet.of(ADMIN, GESTOR, CHEFE))),
            Map.entry(VISUALIZAR_MAPA_DETALHES, new RegrasAcaoMapa(EnumSet.of(ADMIN, GESTOR, CHEFE))),
            Map.entry(CRIAR_MAPA, new RegrasAcaoMapa(EnumSet.of(ADMIN))),
            Map.entry(EDITAR_MAPA_DIRETO, new RegrasAcaoMapa(EnumSet.of(ADMIN))),
            Map.entry(EXCLUIR_MAPA, new RegrasAcaoMapa(EnumSet.of(ADMIN)))
    );

    @Override
    public boolean canExecute(Usuario usuario, Acao acao, Mapa mapa) {
        RegrasAcaoMapa regras = REGRAS.get(acao);
        if (regras == null) {
            ultimoMotivoNegacao = "Ação não reconhecida: " + acao;
            return false;
        }

        // Verifica se o usuário tem um dos perfis permitidos
        if (!temPerfilPermitido(usuario, regras.perfisPermitidos)) {
            ultimoMotivoNegacao = String.format(
                    "Usuário '%s' não possui um dos perfis necessários: %s. Ação: %s",
                    usuario.getTituloEleitoral(),
                    formatarPerfis(regras.perfisPermitidos),
                    acao.getDescricao()
            );
            return false;
        }

        return true;
    }

    @Override
    public String getMotivoNegacao() {
        return ultimoMotivoNegacao;
    }

    private boolean temPerfilPermitido(Usuario usuario, EnumSet<Perfil> perfisPermitidos) {
        return usuario.getTodasAtribuicoes().stream()
                .anyMatch(a -> perfisPermitidos.contains(a.getPerfil()));
    }

    private String formatarPerfis(EnumSet<Perfil> perfis) {
        return perfis.stream()
                .map(Perfil::name)
                .reduce((a, b) -> a + ", " + b)
                .orElse("nenhum");
    }

    /**
     * Record para regras de ação de mapa.
     * Mapas têm regras simples baseadas apenas em perfil.
     */
    private record RegrasAcaoMapa(EnumSet<Perfil> perfisPermitidos) {}
}
