package sgc.seguranca.acesso;

import org.springframework.stereotype.Component;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfilRepo;
import sgc.organizacao.service.HierarquiaService;
import sgc.processo.model.Processo;

import java.util.EnumSet;
import java.util.Map;

import static sgc.organizacao.model.Perfil.*;
import static sgc.seguranca.acesso.Acao.*;

/**
 * Política de acesso para operações em processos.
 * Processos são gerenciados exclusivamente por ADMINs, com exceção de
 * visualização e operações de consulta que podem ser executadas por outros perfis.
 */
@Component
public class ProcessoAccessPolicy extends AbstractAccessPolicy<Processo> {

    public ProcessoAccessPolicy(UsuarioPerfilRepo usuarioPerfilRepo, HierarquiaService hierarquiaService) {
        super(usuarioPerfilRepo, hierarquiaService);
    }

    /**
     * Mapeamento de ações para regras de acesso.
     * Processos têm regras simples: apenas ADMIN pode criar/editar/excluir/iniciar/finalizar.
     * Operações em bloco (homologação) podem ser executadas por GESTOR e CHEFE.
     */
    private static final Map<Acao, RegrasAcaoProcesso> REGRAS = Map.ofEntries(
            Map.entry(CRIAR_PROCESSO, new RegrasAcaoProcesso(EnumSet.of(ADMIN))),
            Map.entry(VISUALIZAR_PROCESSO, new RegrasAcaoProcesso(EnumSet.of(ADMIN, GESTOR, CHEFE))),
            Map.entry(EDITAR_PROCESSO, new RegrasAcaoProcesso(EnumSet.of(ADMIN))),
            Map.entry(EXCLUIR_PROCESSO, new RegrasAcaoProcesso(EnumSet.of(ADMIN))),
            Map.entry(INICIAR_PROCESSO, new RegrasAcaoProcesso(EnumSet.of(ADMIN))),
            Map.entry(FINALIZAR_PROCESSO, new RegrasAcaoProcesso(EnumSet.of(ADMIN))),
            Map.entry(ENVIAR_LEMBRETE_PROCESSO, new RegrasAcaoProcesso(EnumSet.of(ADMIN))),
            Map.entry(HOMOLOGAR_CADASTRO_EM_BLOCO, new RegrasAcaoProcesso(EnumSet.of(ADMIN, GESTOR, CHEFE))),
            Map.entry(HOMOLOGAR_MAPA_EM_BLOCO, new RegrasAcaoProcesso(EnumSet.of(ADMIN, GESTOR, CHEFE))),
            Map.entry(ACEITAR_CADASTRO_EM_BLOCO, new RegrasAcaoProcesso(EnumSet.of(ADMIN, GESTOR, CHEFE))),
            Map.entry(DISPONIBILIZAR_MAPA_EM_BLOCO, new RegrasAcaoProcesso(EnumSet.of(ADMIN, GESTOR, CHEFE)))
    );

    @Override
    public boolean canExecute(Usuario usuario, Acao acao, Processo processo) {
        RegrasAcaoProcesso regras = REGRAS.get(acao);
        if (regras == null) {
            definirMotivoNegacao("Ação não reconhecida: " + acao);
            return false;
        }

        // Verifica se o usuário tem um dos perfis permitidos
        if (!temPerfilPermitido(usuario, regras.perfisPermitidos)) {
            definirMotivoNegacao(usuario, regras.perfisPermitidos, acao);
            return false;
        }

        return true;
    }

    /**
     * Record para regras de ação de processo.
     * Processos não têm verificações de situação ou hierarquia - apenas perfil.
     */
    private record RegrasAcaoProcesso(EnumSet<Perfil> perfisPermitidos) {
    }
}
