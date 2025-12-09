package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.comum.util.FormatadorData;
import sgc.diagnostico.dto.AvaliacaoServidorDto;
import sgc.diagnostico.dto.DiagnosticoDto;
import sgc.diagnostico.dto.OcupacaoCriticaDto;
import sgc.diagnostico.dto.ServidorDiagnosticoDto;
import sgc.diagnostico.model.AvaliacaoServidor;
import sgc.diagnostico.model.Diagnostico;
import sgc.diagnostico.model.OcupacaoCritica;
import sgc.diagnostico.model.SituacaoServidorDiagnostico;
import sgc.sgrh.model.Usuario;

import java.util.List;

/**
 * Serviço responsável pela conversão de entidades do módulo de diagnóstico para DTOs.
 */
@Service
@RequiredArgsConstructor
public class DiagnosticoDtoService {

    public DiagnosticoDto toDto(
            Diagnostico diagnostico,
            List<ServidorDiagnosticoDto> servidores,
            boolean podeSerConcluido,
            String motivoNaoPodeConcluir) {
        
        return DiagnosticoDto.builder()
                .codigo(diagnostico.getCodigo())
                .subprocessoCodigo(diagnostico.getSubprocesso().getCodigo())
                .situacao(diagnostico.getSituacao().name())
                .situacaoLabel(diagnostico.getSituacao().getLabel())
                .dataConclusao(diagnostico.getDataConclusao())
                .dataConclusaoFormatada(FormatadorData.formatarDataHora(diagnostico.getDataConclusao()))
                .justificativaConclusao(diagnostico.getJustificativaConclusao())
                .servidores(servidores)
                .podeSerConcluido(podeSerConcluido)
                .motivoNaoPodeConcluir(motivoNaoPodeConcluir)
                .build();
    }

    public ServidorDiagnosticoDto toDto(
            Usuario servidor,
            List<AvaliacaoServidor> avaliacoes,
            List<OcupacaoCritica> ocupacoes,
            int totalCompetencias) {

        List<AvaliacaoServidorDto> avaliacoesDto = avaliacoes.stream()
                .map(this::toDto)
                .toList();

        List<OcupacaoCriticaDto> ocupacoesDto = ocupacoes.stream()
                .map(this::toDto)
                .toList();

        // Determina situação geral do servidor baseado nas avaliações
        // Simplificação: pega a situação da primeira avaliação ou NAO_REALIZADA
        String situacao = SituacaoServidorDiagnostico.AUTOAVALIACAO_NAO_REALIZADA.name();
        String situacaoLabel = SituacaoServidorDiagnostico.AUTOAVALIACAO_NAO_REALIZADA.getLabel();

        if (!avaliacoes.isEmpty()) {
            // Em tese todas as avaliações do servidor deveriam ter a mesma situação de processo
            // Mas vamos pegar a mais avançada encontrada
            SituacaoServidorDiagnostico sit = avaliacoes.get(0).getSituacao();
            situacao = sit.name();
            situacaoLabel = sit.getLabel();
        } else if (totalCompetencias == 0) {
            // Se não tem competências para avaliar, considera concluída? 
            // Não, vamos manter como não realizada ou tratar caso especial
            // Mas por enquanto segue padrão
        }

        int competenciasAvaliadas = (int) avaliacoes.stream()
                .filter(a -> a.getImportancia() != null && a.getDominio() != null)
                .count();

        return ServidorDiagnosticoDto.builder()
                .tituloEleitoral(servidor.getTituloEleitoral())
                .nome(servidor.getNome())
                .situacao(situacao)
                .situacaoLabel(situacaoLabel)
                .avaliacoes(avaliacoesDto)
                .ocupacoes(ocupacoesDto)
                .totalCompetencias(totalCompetencias)
                .competenciasAvaliadas(competenciasAvaliadas)
                .ocupacoesPreenchidas(ocupacoes.size())
                .build();
    }

    public AvaliacaoServidorDto toDto(AvaliacaoServidor avaliacao) {
        return AvaliacaoServidorDto.builder()
                .codigo(avaliacao.getCodigo())
                .competenciaCodigo(avaliacao.getCompetencia().getCodigo())
                .competenciaDescricao(avaliacao.getCompetencia().getDescricao())
                .importancia(avaliacao.getImportancia() != null ? avaliacao.getImportancia().name() : null)
                .importanciaLabel(avaliacao.getImportancia() != null ? avaliacao.getImportancia().getLabel() : null)
                .dominio(avaliacao.getDominio() != null ? avaliacao.getDominio().name() : null)
                .dominioLabel(avaliacao.getDominio() != null ? avaliacao.getDominio().getLabel() : null)
                .gap(avaliacao.getGap())
                .observacoes(avaliacao.getObservacoes())
                .build();
    }

    public OcupacaoCriticaDto toDto(OcupacaoCritica ocupacao) {
        return OcupacaoCriticaDto.builder()
                .codigo(ocupacao.getCodigo())
                .competenciaCodigo(ocupacao.getCompetencia().getCodigo())
                .competenciaDescricao(ocupacao.getCompetencia().getDescricao())
                .situacao(ocupacao.getSituacao().name())
                .situacaoLabel(ocupacao.getSituacao().getLabel())
                .build();
    }
}
