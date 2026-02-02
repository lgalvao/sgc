package sgc.processo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.util.FormatadorData;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.mapper.ProcessoDetalheMapper;
import sgc.processo.model.Processo;
import sgc.seguranca.acesso.Acao;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProcessoDetalheBuilder {

    private final SubprocessoRepo subprocessoRepo;
    private final ProcessoDetalheMapper processoDetalheMapper;
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
                .dataCriacaoFormatada(FormatadorData.formatarData(processo.getDataCriacao()))
                .dataFinalizacaoFormatada(FormatadorData.formatarData(processo.getDataFinalizacao()))
                .dataLimiteFormatada(FormatadorData.formatarData(processo.getDataLimite()))
                .situacaoLabel(processo.getSituacao().getLabel())
                .tipoLabel(processo.getTipo().getLabel())
                .unidades(new ArrayList<>())
                .build();

        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigoWithUnidade(processo.getCodigo());
        montarHierarquiaUnidades(dto, processo, subprocessos);

        return dto;
    }



    private void montarHierarquiaUnidades(
            ProcessoDetalheDto dto, Processo processo, List<Subprocesso> subprocessos) {
        Map<Long, ProcessoDetalheDto.UnidadeParticipanteDto> mapaUnidades = new HashMap<>();
        Map<Long, Subprocesso> mapaSubprocessos = new HashMap<>();

        // Criar índice de subprocessos por unidade para lookup O(1)
        for (Subprocesso sp : subprocessos) {
            mapaSubprocessos.put(sp.getUnidade().getCodigo(), sp);
        }

        // Loop 1 consolidado: Mapear participantes E preencher dados dos subprocessos
        for (Unidade participante : processo.getParticipantes()) {
            ProcessoDetalheDto.UnidadeParticipanteDto unidadeDto =
                    processoDetalheMapper.toUnidadeParticipanteDto(participante);
            
            // Preencher dados do subprocesso se existir
            Subprocesso sp = mapaSubprocessos.get(participante.getCodigo());
            if (sp != null) {
                unidadeDto.setSituacaoSubprocesso(sp.getSituacao());
                unidadeDto.setDataLimite(sp.getDataLimiteEtapa1());
                unidadeDto.setCodSubprocesso(sp.getCodigo());
                if (sp.getMapa() != null) {
                    unidadeDto.setMapaCodigo(sp.getMapa().getCodigo());
                }
                unidadeDto.setDataLimiteFormatada(
                        FormatadorData.formatarData(sp.getDataLimiteEtapa1()));
                unidadeDto.setSituacaoLabel(sp.getSituacao().getDescricao());
            }
            
            mapaUnidades.put(participante.getCodigo(), unidadeDto);
        }

        // Loop 2 consolidado: Montar hierarquia E adicionar raízes
        for (ProcessoDetalheDto.UnidadeParticipanteDto unidadeDto : mapaUnidades.values()) {
            Long codUnidadeSuperior = unidadeDto.getCodUnidadeSuperior();
            ProcessoDetalheDto.UnidadeParticipanteDto pai = mapaUnidades.get(codUnidadeSuperior);
            
            if (pai != null) {
                // Tem pai participando do processo: adicionar como filho
                pai.getFilhos().add(unidadeDto);
            } else {
                // Não tem pai ou pai não participa: é raiz
                dto.getUnidades().add(unidadeDto);
            }
        }

        // Ordenação
        Comparator<ProcessoDetalheDto.UnidadeParticipanteDto> comparator =
                Comparator.comparing(ProcessoDetalheDto.UnidadeParticipanteDto::getSigla);

        dto.getUnidades().sort(comparator);

        for (ProcessoDetalheDto.UnidadeParticipanteDto unidadeDto : mapaUnidades.values()) {
            unidadeDto.getFilhos().sort(comparator);
        }
    }
}
