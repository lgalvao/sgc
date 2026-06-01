package sgc.subprocesso;

import org.springframework.stereotype.*;
import sgc.mapa.dto.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

@Component
public class SubprocessoDtoMapper {
    private final OrganizacaoDtoMapper organizacaoDtoMapper;

    public SubprocessoDtoMapper(OrganizacaoDtoMapper organizacaoDtoMapper) {
        this.organizacaoDtoMapper = organizacaoDtoMapper;
    }

    public SubprocessoResumoDto paraResumo(Subprocesso subprocesso) {
        Processo processo = subprocesso.getProcesso();
        if (processo == null || subprocesso.getUnidade() == null) {
            throw new IllegalStateException("Subprocesso deve possuir processo e unidade associados");
        }

        LocalDateTime dataLimiteEtapa1 = subprocesso.getDataLimiteEtapa1();
        LocalDateTime dataLimiteEtapa2 = subprocesso.getDataLimiteEtapa2();

        return SubprocessoResumoDto.builder()
                .codigo(subprocesso.getCodigo())
                .unidade(organizacaoDtoMapper.paraUnidadeResumoObrigatoria(subprocesso.getUnidade()))
                .situacao(subprocesso.getSituacao().name())
                .dataLimiteEtapa1(dataLimiteEtapa1)
                .dataFimEtapa1(subprocesso.getDataFimEtapa1())
                .dataLimiteEtapa2(dataLimiteEtapa2)
                .dataFimEtapa2(subprocesso.getDataFimEtapa2())
                .ultimaDataLimite(calcularUltimaDataLimite(dataLimiteEtapa1, dataLimiteEtapa2))
                .codProcesso(processo.getCodigo())
                .codUnidade(subprocesso.getUnidade().getCodigo())
                .codMapa(subprocesso.getCodMapa())
                .processoDescricao(processo.getDescricao())
                .dataCriacaoProcesso(processo.getDataCriacao())
                .tipoProcesso(processo.getTipo() != null ? processo.getTipo().name() : null)
                .isEmAndamento(subprocesso.isEmAndamento())
                .etapaAtual(subprocesso.getEtapaAtual())
                .build();
    }

    public SubprocessoListagemDto paraListagem(Subprocesso subprocesso) {
        return paraListagem(paraResumo(subprocesso));
    }

    public SubprocessoListagemDto paraListagem(SubprocessoResumoDto resumo) {
        return SubprocessoListagemDto.builder()
                .codigo(resumo.codigo())
                .unidade(resumo.unidade())
                .situacao(resumo.situacao())
                .dataLimiteEtapa1(resumo.dataLimiteEtapa1())
                .dataFimEtapa1(resumo.dataFimEtapa1())
                .dataLimiteEtapa2(resumo.dataLimiteEtapa2())
                .dataFimEtapa2(resumo.dataFimEtapa2())
                .codProcesso(resumo.codProcesso())
                .codUnidade(resumo.codUnidade())
                .codMapa(resumo.codMapa())
                .processoDescricao(resumo.processoDescricao())
                .dataCriacaoProcesso(resumo.dataCriacaoProcesso())
                .tipoProcesso(resumo.tipoProcesso())
                .isEmAndamento(resumo.isEmAndamento())
                .etapaAtual(resumo.etapaAtual())
                .build();
    }

    public SubprocessoCadastroDto paraCadastro(Subprocesso subprocesso, List<AtividadeDto> atividades) {
        if (subprocesso.getUnidade() == null) {
            throw new IllegalStateException("Subprocesso deve possuir unidade associada");
        }

        return SubprocessoCadastroDto.builder()
                .codigo(subprocesso.getCodigo())
                .unidade(organizacaoDtoMapper.paraUnidadeResumoObrigatoria(subprocesso.getUnidade()))
                .atividades(atividades)
                .build();
    }

    private LocalDateTime calcularUltimaDataLimite(LocalDateTime dataLimiteEtapa1, LocalDateTime dataLimiteEtapa2) {
        if (dataLimiteEtapa2 == null) {
            return dataLimiteEtapa1;
        }
        return dataLimiteEtapa1.isAfter(dataLimiteEtapa2) ? dataLimiteEtapa1 : dataLimiteEtapa2;
    }
}
