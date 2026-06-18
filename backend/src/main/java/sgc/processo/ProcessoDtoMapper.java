package sgc.processo;

import org.springframework.stereotype.*;
import sgc.processo.dto.*;
import sgc.processo.dto.ProcessoDetalheDto.*;
import sgc.processo.model.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

@Component
public class ProcessoDtoMapper {

    public ProcessoResumoDto paraResumo(Processo processo) {
        return ProcessoResumoDto.builder()
                .codigo(processo.getCodigo())
                .descricao(processo.getDescricao())
                .situacao(processo.getSituacao() != null ? processo.getSituacao().name() : null)
                .tipo(processo.getTipo() != null ? processo.getTipo().name() : null)
                .dataLimite(processo.getDataLimite())
                .dataCriacao(processo.getDataCriacao())
                .dataFinalizacao(processo.getDataFinalizacao())
                .unidadesParticipantes(processo.getSiglasParticipantes())
                .build();
    }

    public ProcessoDetalheDto criarDetalheBase(
            Processo processo,
            boolean podeFinalizar,
            boolean podeHomologarCadastro,
            boolean podeHomologarMapa,
            boolean podeAceitarCadastroBloco,
            boolean podeAceitarMapaBloco,
            boolean podeDisponibilizarMapaBloco
    ) {
        return ProcessoDetalheDto.builder()
                .codigo(processo.getCodigo())
                .descricao(processo.getDescricao())
                .situacao(processo.getSituacao() != null ? processo.getSituacao().name() : null)
                .tipo(processo.getTipo() != null ? processo.getTipo().name() : null)
                .dataCriacao(processo.getDataCriacao())
                .dataFinalizacao(processo.getDataFinalizacao())
                .dataLimite(processo.getDataLimite())
                .podeFinalizar(podeFinalizar)
                .podeHomologarCadastro(podeHomologarCadastro)
                .podeHomologarMapa(podeHomologarMapa)
                .podeAceitarCadastroBloco(podeAceitarCadastroBloco)
                .podeAceitarMapaBloco(podeAceitarMapaBloco)
                .podeDisponibilizarMapaBloco(podeDisponibilizarMapaBloco)
                .unidades(new ArrayList<>())
                .build();
    }

    public UnidadeParticipanteDto paraUnidadeParticipante(Unidade unidade) {
        Unidade superior = unidade.getUnidadeSuperior();
        return criarUnidadeParticipanteBase(
                unidade.getNome(),
                unidade.getSigla(),
                unidade.getCodigo(),
                superior != null ? superior.getCodigo() : null
        );
    }

    public UnidadeParticipanteDto paraUnidadeParticipante(UnidadeProcesso snapshot) {
        return criarUnidadeParticipanteBase(
                snapshot.getNome(),
                snapshot.getSigla(),
                snapshot.getUnidadeCodigoPersistido(),
                snapshot.getUnidadeSuperiorCodigo()
        );
    }

    public void preencherParticipanteComSubprocesso(
            UnidadeParticipanteDto dto,
            Subprocesso subprocesso,
            Unidade localizacaoAtual
    ) {
        dto.setSituacaoSubprocesso(subprocesso.getSituacao() != null ? subprocesso.getSituacao().name() : null);
        dto.setDataLimite(subprocesso.getDataLimiteEtapa1());
        dto.setCodSubprocesso(subprocesso.getCodigo());
        dto.setMapaCodigo(subprocesso.getMapa() != null ? subprocesso.getMapa().getCodigo() : null);
        dto.setLocalizacaoAtualCodigo(localizacaoAtual.getCodigo());
    }

    public AcaoBlocoDto criarAcaoBloco(
            String codigo,
            AcaoProcesso acao,
            boolean mostrar,
            boolean habilitar,
            boolean requerDataLimite,
            boolean redirecionarPainel,
            String rotulo,
            String titulo,
            String texto,
            String rotuloBotao,
            String mensagemSucesso,
            List<SubprocessoElegivelDto> unidades
    ) {
        return AcaoBlocoDto.builder()
                .codigo(codigo)
                .acao(acao.name())
                .mostrar(mostrar)
                .habilitar(habilitar)
                .requerDataLimite(requerDataLimite)
                .redirecionarPainel(redirecionarPainel)
                .rotulo(rotulo)
                .titulo(titulo)
                .texto(texto)
                .rotuloBotao(rotuloBotao)
                .mensagemSucesso(mensagemSucesso)
                .unidades(new ArrayList<>(unidades))
                .build();
    }

    private UnidadeParticipanteDto criarUnidadeParticipanteBase(
            String nome,
            String sigla,
            Long codUnidade,
            Long codUnidadeSuperior
    ) {
        return UnidadeParticipanteDto.builder()
                .nome(nome)
                .sigla(sigla)
                .codUnidade(Objects.requireNonNull(codUnidade, "Codigo da unidade participante obrigatorio"))
                .codUnidadeSuperior(codUnidadeSuperior)
                .filhos(new ArrayList<>())
                .build();
    }
}
