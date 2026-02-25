package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.model.ComumRepo;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.service.CopiaMapaService;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

/**
 * Responsável por:
 * <ul>
 *   <li>Importar atividades entre subprocessos</li>
 *   <li>Listar atividades de um subprocesso para visualização</li>
 *   <li>Transformar atividades em DTOs para visualização</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
class SubprocessoAtividadeService {
    private final SubprocessoRepo subprocessoRepo;
    private final ComumRepo repo;
    private final MapaManutencaoService mapaManutencaoService;
    private final CopiaMapaService copiaMapaService;
    private final MovimentacaoRepo movimentacaoRepo;
    private final UsuarioFacade usuarioService;
    private final SgcPermissionEvaluator permissionEvaluator;

    private static final Set<SituacaoSubprocesso> SITUACOES_PERMITIDAS_IMPORTACAO = Set.of(
            NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO);

    @Transactional
    public void importarAtividades(Long codSubprocessoDestino, Long codSubprocessoOrigem) {
        final Subprocesso spDestino = repo.buscar(Subprocesso.class, codSubprocessoDestino);
        Usuario usuario = usuarioService.usuarioAutenticado();

        if (!permissionEvaluator.checkPermission(usuario, spDestino, "EDITAR_CADASTRO")) {
             throw new ErroAcessoNegado("Usuário não tem permissão para importar atividades.");
        }
        validarSituacaoParaImportacao(spDestino);

        Subprocesso spOrigem = repo.buscar(Subprocesso.class, codSubprocessoOrigem);
        if (!permissionEvaluator.checkPermission(usuario, spOrigem, "CONSULTAR_PARA_IMPORTACAO")) {
            throw new ErroAcessoNegado("Usuário não tem permissão para consultar o subprocesso de origem.");
        }

        copiaMapaService.importarAtividadesDeOutroMapa(
                spOrigem.getMapa().getCodigo(),
                spDestino.getMapa().getCodigo());

        if (spDestino.getSituacao() == SituacaoSubprocesso.NAO_INICIADO) {
            var tipoProcesso = spDestino.getProcesso().getTipo();

            switch (tipoProcesso) {
                case MAPEAMENTO -> spDestino.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                case REVISAO -> spDestino.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
                default -> log.debug("Tipo de processo {} não requer atualização automática de situação.", tipoProcesso);
            }
            subprocessoRepo.save(spDestino);
        }

        final Unidade unidadeOrigem = spOrigem.getUnidade();
        String descMovimentacao = String.format("Importação de atividades do subprocesso #%d (Unidade: %s)",
                spOrigem.getCodigo(),
                unidadeOrigem.getSigla());

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(spDestino)
                .unidadeOrigem(unidadeOrigem)
                .unidadeDestino(spDestino.getUnidade())
                .descricao(descMovimentacao)
                .usuario(usuario)
                .build());
        spDestino.setLocalizacaoAtualCache(spDestino.getUnidade());
    }

    @Transactional(readOnly = true)
    public List<AtividadeDto> listarAtividadesSubprocesso(Long codSubprocesso) {
        Subprocesso subprocesso = subprocessoRepo.findByIdWithMapaAndAtividades(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));
        List<Atividade> todasAtividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(
                subprocesso.getMapa().getCodigo());

        return todasAtividades.stream().map(this::mapAtividadeToDto).toList();
    }

    private void validarSituacaoParaImportacao(Subprocesso sp) {
        if (!SITUACOES_PERMITIDAS_IMPORTACAO.contains(sp.getSituacao())) {
            throw new ErroValidacao(
                "Situação do subprocesso não permite importação. Situação atual: %s"
                    .formatted(sp.getSituacao()));
        }
    }

    private AtividadeDto mapAtividadeToDto(Atividade atividade) {
        return AtividadeDto.builder()
                .codigo(atividade.getCodigo())
                .descricao(atividade.getDescricao())
                .conhecimentos(new ArrayList<>(atividade.getConhecimentos()))
                .build();
    }
}
