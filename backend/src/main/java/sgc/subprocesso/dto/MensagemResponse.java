package sgc.subprocesso.dto;

import lombok.Builder;

@Builder
public record MensagemResponse(
        String mensagem) {
}
