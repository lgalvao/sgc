package sgc.processo.service;

import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.model.*;
import sgc.processo.dto.*;
import sgc.processo.dto.ProcessoDetalheDto.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProcessoDetalheBuilder {
    private final SubprocessoRepo subprocessoRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final SgcPermissionEvaluator permissionEvaluator;
    private final SubprocessoValidacaoService subprocessoValidacaoService;
    private final ProcessoValidacaoService processoValidacaoService;

    @Transactional(readOnly = true)
    public ProcessoDetalheDto build(Processo processo, Usuario usuario) {
        ProcessoDetalheDto dto = ProcessoDetalheDto.builder()
                .codigo(processo.getCodigo())
                .descricao(processo.getDescricao())
                .situacao(processo.getSituacao())
                .tipo(processo.getTipo().name())
                .dataCriacao(processo.getDataCriacao())
                .dataFinalizacao(processo.getDataFinalizacao())
                .dataLimite(processo.getDataLimite())
                .podeFinalizar(permissionEvaluator.checkPermission(usuario, processo, "FINALIZAR_PROCESSO")
                        && subprocessoValidacaoService.validarSubprocessosParaFinalizacao(processo.getCodigo()).valido())
                .podeHomologarCadastro(permissionEvaluator.checkPermission(usuario, processo, "HOMOLOGAR_CADASTRO_EM_BLOCO"))
                .podeHomologarMapa(permissionEvaluator.checkPermission(usuario, processo, "HOMOLOGAR_MAPA_EM_BLOCO"))
                .podeAceitarCadastroBloco(permissionEvaluator.checkPermission(usuario, processo, "ACEITAR_CADASTRO_EM_BLOCO"))
                .podeDisponibilizarMapaBloco(permissionEvaluator.checkPermission(usuario, processo, "DISPONIBILIZAR_MAPA_EM_BLOCO")
                        && subprocessoRepo.countByProcessoCodigoAndSituacaoIn(processo.getCodigo(), List.of(
                        SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO,
                        SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO
                )) > 0)
                .unidades(new ArrayList<>())
                .build();

        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigoWithUnidade(processo.getCodigo());
        montarHierarquiaUnidades(dto, processo, subprocessos, usuario);

        return dto;
    }

    private void montarHierarquiaUnidades(
            ProcessoDetalheDto dto, Processo processo, List<Subprocesso> subprocessos, Usuario usuario) {
        Map<Long, UnidadeParticipanteDto> mapaUnidades = new HashMap<>();
        Map<Long, Subprocesso> mapaSubprocessos = new HashMap<>();

        Set<Long> unidadesAcesso = (usuario.getPerfilAtivo() == Perfil.ADMIN)
                ? null
                : new HashSet<>(processoValidacaoService.buscarCodigosDescendentes(usuario.getUnidadeAtivaCodigo()));

        for (Subprocesso sp : subprocessos) {
            mapaSubprocessos.put(sp.getUnidade().getCodigo(), sp);
        }

        for (UnidadeProcesso participante : processo.getParticipantes()) {
            if (unidadesAcesso != null && !unidadesAcesso.contains(participante.getUnidadeCodigo())) {
                continue;
            }

            UnidadeParticipanteDto unidadeDto = UnidadeParticipanteDto.fromSnapshot(participante);
            Subprocesso sp = mapaSubprocessos.get(participante.getUnidadeCodigo());
            if (sp != null) {
                unidadeDto.setSituacaoSubprocesso(sp.getSituacao());
                unidadeDto.setDataLimite(sp.getDataLimiteEtapa1());
                unidadeDto.setCodSubprocesso(sp.getCodigo());
                if (sp.getMapa() != null) unidadeDto.setMapaCodigo(sp.getMapa().getCodigo());
                unidadeDto.setLocalizacaoAtualCodigo(obterUnidadeLocalizacao(sp).getCodigo());
            }
            mapaUnidades.put(participante.getUnidadeCodigo(), unidadeDto);
        }

        for (UnidadeParticipanteDto unidadeDto : mapaUnidades.values()) {
            Long codUnidadeSuperior = unidadeDto.getCodUnidadeSuperior();
            UnidadeParticipanteDto pai = mapaUnidades.get(codUnidadeSuperior);

            if (pai != null) pai.getFilhos().add(unidadeDto);
            else dto.getUnidades().add(unidadeDto);
        }

        Comparator<UnidadeParticipanteDto> comparator = Comparator.comparing(UnidadeParticipanteDto::getSigla);
        dto.getUnidades().sort(comparator);
        mapaUnidades.values().forEach(unidadeDto -> unidadeDto.getFilhos().sort(comparator));
    }
    private Unidade obterUnidadeLocalizacao(Subprocesso sp) {
        if (sp.getLocalizacaoAtual() != null) return sp.getLocalizacaoAtual();

        Unidade localizacao = movimentacaoRepo.findFirstBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo())
                .filter(m -> m.getUnidadeDestino() != null)
                .map(Movimentacao::getUnidadeDestino)
                .orElse(sp.getUnidade());

        sp.setLocalizacaoAtual(localizacao);
        return localizacao;
    }
}
