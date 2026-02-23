package sgc.processo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.model.Usuario;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.dto.ProcessoDetalheDto.UnidadeParticipanteDto;
import sgc.processo.model.Processo;
import sgc.processo.model.UnidadeProcesso;
import sgc.seguranca.Acao;
import sgc.seguranca.AccessControlService;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProcessoDetalheBuilder {
    private final SubprocessoRepo subprocessoRepo;
    private final AccessControlService accessControlService;

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
                .podeFinalizar(accessControlService.podeExecutar(usuario, Acao.FINALIZAR_PROCESSO, processo))
                .podeHomologarCadastro(accessControlService.podeExecutar(usuario, Acao.HOMOLOGAR_CADASTRO_EM_BLOCO, processo))
                .podeHomologarMapa(accessControlService.podeExecutar(usuario, Acao.HOMOLOGAR_MAPA_EM_BLOCO, processo))
                .podeAceitarCadastroBloco(accessControlService.podeExecutar(usuario, Acao.ACEITAR_CADASTRO_EM_BLOCO, processo))
                .podeDisponibilizarMapaBloco(accessControlService.podeExecutar(usuario, Acao.DISPONIBILIZAR_MAPA_EM_BLOCO, processo))

                .unidades(new ArrayList<>())
                .build();

        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigoWithUnidade(processo.getCodigo());
        montarHierarquiaUnidades(dto, processo, subprocessos);

        return dto;
    }

    private void montarHierarquiaUnidades(
            ProcessoDetalheDto dto, Processo processo, List<Subprocesso> subprocessos) {
        Map<Long, UnidadeParticipanteDto> mapaUnidades = new HashMap<>();
        Map<Long, Subprocesso> mapaSubprocessos = new HashMap<>();

        // Criar índice de subprocessos por unidade para lookup O(1)
        for (Subprocesso sp : subprocessos) {
            mapaSubprocessos.put(sp.getUnidade().getCodigo(), sp);
        }

        for (UnidadeProcesso participante : processo.getParticipantes()) {
            UnidadeParticipanteDto unidadeDto = UnidadeParticipanteDto.fromSnapshot(participante);
            Subprocesso sp = mapaSubprocessos.get(participante.getUnidadeCodigo());
            if (sp != null) {
                unidadeDto.setSituacaoSubprocesso(sp.getSituacao());
                unidadeDto.setDataLimite(sp.getDataLimiteEtapa1());
                unidadeDto.setCodSubprocesso(sp.getCodigo());
                if (sp.getMapa() != null) unidadeDto.setMapaCodigo(sp.getMapa().getCodigo());
            }
            mapaUnidades.put(participante.getUnidadeCodigo(), unidadeDto);
        }

        for (UnidadeParticipanteDto unidadeDto : mapaUnidades.values()) {
            Long codUnidadeSuperior = unidadeDto.getCodUnidadeSuperior();
            UnidadeParticipanteDto pai = mapaUnidades.get(codUnidadeSuperior);

            if (pai != null) pai.getFilhos().add(unidadeDto);
            else dto.getUnidades().add(unidadeDto);
        }

        // Ordenação
        Comparator<UnidadeParticipanteDto> comparator = Comparator.comparing(UnidadeParticipanteDto::getSigla);
        dto.getUnidades().sort(comparator);
        mapaUnidades.values().forEach(unidadeDto -> unidadeDto.getFilhos().sort(comparator));
    }
}
