package sgc.processo.service;

import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.model.*;
import sgc.processo.dto.*;
import sgc.processo.dto.ProcessoDetalheDto.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import static sgc.seguranca.AcaoPermissao.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.util.*;
import java.util.stream.*;

@Service
@RequiredArgsConstructor
public class ProcessoDetalheBuilder {
    private final SubprocessoRepo subprocessoRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final SgcPermissionEvaluator permissionEvaluator;
    private final SubprocessoValidacaoService subprocessoValidacaoService;
    private final UnidadeRepo unidadeRepo;
    private final SubprocessoService subprocessoService;
    @Transactional(readOnly = true)
    public ProcessoDetalheDto build(Processo processo, Usuario usuario) {
        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigoWithUnidade(processo.getCodigo());
        Set<Long> unidadesAcesso = obterUnidadesAcesso(processo, usuario);

        ProcessoDetalheDto dto = ProcessoDetalheDto.builder()
                .codigo(processo.getCodigo())
                .descricao(processo.getDescricao())
                .situacao(processo.getSituacao())
                .tipo(processo.getTipo().name())
                .dataCriacao(processo.getDataCriacao())
                .dataFinalizacao(processo.getDataFinalizacao())
                .dataLimite(processo.getDataLimite())
                .podeFinalizar(permissionEvaluator.verificarPermissao(usuario, processo, FINALIZAR_PROCESSO)
                        && subprocessoValidacaoService.validarSubprocessosParaFinalizacao(processo.getCodigo()).valido())
                .podeHomologarCadastro(permissionEvaluator.verificarPermissao(usuario, processo, HOMOLOGAR_CADASTRO_EM_BLOCO))
                .podeHomologarMapa(permissionEvaluator.verificarPermissao(usuario, processo, HOMOLOGAR_MAPA_EM_BLOCO))
                .podeAceitarCadastroBloco(permissionEvaluator.verificarPermissao(usuario, processo, ACEITAR_CADASTRO_EM_BLOCO))
                .podeDisponibilizarMapaBloco(permissionEvaluator.verificarPermissao(usuario, processo, DISPONIBILIZAR_MAPA_EM_BLOCO))
                .unidades(new ArrayList<>())
                .build();

        montarHierarquiaUnidades(dto, processo, subprocessos, unidadesAcesso);

        return dto;
    }

    private void montarHierarquiaUnidades(
            ProcessoDetalheDto dto, Processo processo, List<Subprocesso> subprocessos, Set<Long> unidadesAcesso) {
        Map<Long, UnidadeParticipanteDto> mapaUnidades = new HashMap<>();
        Map<Long, Subprocesso> mapaSubprocessos = new HashMap<>();

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

    private Set<Long> obterUnidadesAcesso(Processo processo, Usuario usuario) {
        if (usuario.getPerfilAtivo() == Perfil.ADMIN) {
            return null;
        }

        Long codUnidadeAtiva = usuario.getUnidadeAtivaCodigo();
        if (codUnidadeAtiva == null) {
            return Set.of();
        }

        if (usuario.getPerfilAtivo() != Perfil.GESTOR) {
            return Set.of(codUnidadeAtiva);
        }

        return buscarDescendentesNaHierarquia(processo.getParticipantes(), codUnidadeAtiva);
    }

    private Set<Long> buscarDescendentesNaHierarquia(List<UnidadeProcesso> participantes, Long codRaiz) {
        Map<Long, List<Long>> filhosPorPai = unidadeRepo.findAllWithHierarquia().stream()
                .filter(unidade -> unidade.getUnidadeSuperior() != null)
                .collect(Collectors.groupingBy(
                        unidade -> unidade.getUnidadeSuperior().getCodigo(),
                        Collectors.mapping(Unidade::getCodigo, Collectors.toList())
                ));

        Set<Long> codigosSubarvore = new HashSet<>();
        Queue<Long> fila = new ArrayDeque<>();
        fila.add(codRaiz);

        while (!fila.isEmpty()) {
            Long atual = fila.poll();
            if (!codigosSubarvore.add(atual)) {
                continue;
            }
            fila.addAll(filhosPorPai.getOrDefault(atual, List.of()));
        }

        return participantes.stream()
                .map(UnidadeProcesso::getUnidadeCodigo)
                .filter(codigosSubarvore::contains)
                .collect(Collectors.toSet());
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
