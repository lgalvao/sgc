package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.comum.util.FormatadorData;
import sgc.diagnostico.dto.*;
import sgc.diagnostico.model.*;
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
        
        return new DiagnosticoDto(
                diagnostico.getCodigo(),
                diagnostico.getSubprocesso().getCodigo(),
                diagnostico.getSituacao().name(),
                diagnostico.getSituacao().getLabel(),
                diagnostico.getDataConclusao(),
                FormatadorData.formatarDataHora(diagnostico.getDataConclusao()),
                diagnostico.getJustificativaConclusao(),
                servidores,
                podeSerConcluido,
                motivoNaoPodeConcluir
        );
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

        return new ServidorDiagnosticoDto(
                servidor.getTituloEleitoral(),
                servidor.getNome(),
                situacao,
                situacaoLabel,
                avaliacoesDto,
                ocupacoesDto,
                totalCompetencias,
                competenciasAvaliadas,
                ocupacoes.size()
        );
    }

    public AvaliacaoServidorDto toDto(AvaliacaoServidor avaliacao) {
        return new AvaliacaoServidorDto(
                avaliacao.getCodigo(),
                avaliacao.getCompetencia().getCodigo(),
                avaliacao.getCompetencia().getDescricao(),
                avaliacao.getImportancia() != null ? avaliacao.getImportancia().name() : null,
                avaliacao.getImportancia() != null ? avaliacao.getImportancia().getLabel() : null,
                avaliacao.getDominio() != null ? avaliacao.getDominio().name() : null,
                avaliacao.getDominio() != null ? avaliacao.getDominio().getLabel() : null,
                avaliacao.getGap(),
                avaliacao.getObservacoes()
        );
    }

    public OcupacaoCriticaDto toDto(OcupacaoCritica ocupacao) {
        return new OcupacaoCriticaDto(
                ocupacao.getCodigo(),
                ocupacao.getCompetencia().getCodigo(),
                ocupacao.getCompetencia().getDescricao(),
                ocupacao.getSituacao().name(),
                ocupacao.getSituacao().getLabel()
        );
    }
}
