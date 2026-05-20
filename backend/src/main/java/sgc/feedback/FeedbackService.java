package sgc.feedback;

import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.springframework.web.multipart.*;
import sgc.comum.erros.*;
import sgc.feedback.dto.*;
import sgc.organizacao.*;
import tools.jackson.databind.*;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

/**
 * Serviço responsável por validar e persistir registros de feedback UAT.
 *
 * <p>O {@code usuarioId} e {@code usuarioNome} armazenados na entidade são sempre
 * extraídos do contexto de segurança — os metadados do cliente são apenas informativos.
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepo repo;
    private final FeedbackPropriedades propriedades;
    private final UsuarioFacade usuarioFacade;
    private final ObjectMapper objectMapper;

    /**
     * Registra um novo feedback, opcionalmente com screenshot.
     *
     * @param payload    dados submetidos pelo usuário
     * @param screenshot arquivo de captura de tela (pode ser nulo)
     * @return DTO com o id e a data/hora do registro criado
     */
    public FeedbackRespostaDto registrar(FeedbackPayloadDto payload, @Nullable MultipartFile screenshot) {
        validarNota(payload.nota());
        validarScreenshot(screenshot);

        var usuario = usuarioFacade.usuarioAutenticado();
        String metadataJson = serializarMetadados(payload.metadados());
        String caminhoScreenshot = salvarScreenshot(screenshot);

        String rota = extrairRota(payload.metadados());

        FeedbackRegistro registro = FeedbackRegistro.builder()
                .tipo(payload.tipo())
                .nota(payload.nota())
                .metadataJson(metadataJson)
                .caminhoScreenshot(caminhoScreenshot)
                .usuarioCodigo(usuario.getTituloEleitoral())
                .usuarioNome(usuario.getNome())
                .enviadoEm(OffsetDateTime.now())
                .rota(rota)
                .status(FeedbackStatus.NOVO)
                .build();

        FeedbackRegistro salvo = repo.save(registro);
        log.info("Feedback registrado: id={} tipo={} usuario={}", salvo.getCodigo(), salvo.getTipo(), salvo.getUsuarioCodigo());
        return new FeedbackRespostaDto(salvo.getCodigo(), salvo.getEnviadoEm());
    }

    @Transactional(readOnly = true)
    public List<FeedbackListagemDto> listarRecentes(int limite) {
        int limiteNormalizado = Math.clamp(limite, 1, 200);
        var page = PageRequest.of(0, limiteNormalizado, Sort.by(Sort.Direction.DESC, "enviadoEm"));
        return repo.findAll(page).stream()
                .map(registro -> {
                    boolean disponivel = false;
                    String pathStr = registro.getCaminhoScreenshot();
                    if (pathStr != null && !pathStr.isBlank()) {
                        disponivel = Files.exists(resolverCaminho(pathStr));
                    }
                    return FeedbackListagemDto.from(registro, disponivel);
                })
                .toList();
    }

    /**
     * Recupera os bytes da screenshot de um feedback.
     *
     * @param codigo identificador do feedback
     * @return bytes da imagem
     * @throws ErroEntidadeNaoEncontrada se o feedback ou a imagem não existirem
     */
    @Transactional(readOnly = true)
    public byte[] obterScreenshot(UUID codigo) {
        FeedbackRegistro registro = repo.findById(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Feedback", codigo));

        String nomeOuCaminho = registro.getCaminhoScreenshot();
        if (nomeOuCaminho == null || nomeOuCaminho.isBlank()) {
            throw new ErroEntidadeNaoEncontrada("Screenshot não disponível para este feedback");
        }

        try {
            Path path = resolverCaminho(nomeOuCaminho);
            if (!Files.exists(path)) {
                log.error("Arquivo de screenshot não encontrado: {}", path);
                throw new ErroEntidadeNaoEncontrada("Arquivo de screenshot não encontrado no servidor");
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("Erro ao ler screenshot: {}", e.getMessage());
            throw new ErroInconsistenciaInterna("Erro ao ler arquivo de screenshot");
        }
    }

    private Path resolverCaminho(String nomeOuCaminho) {
        // Se for um caminho absoluto antigo (contém separadores de diretório), tenta usá-lo diretamente
        if (nomeOuCaminho.contains("/") || nomeOuCaminho.contains("\\")) {
            return Path.of(nomeOuCaminho);
        }

        // Caso contrário, resolve contra o diretório configurado (comportamento novo e portátil)
        String dir = propriedades.screenshotDir();
        if (dir == null || dir.isBlank()) {
            dir = "./feedbacks/screenshots";
        }
        return Path.of(dir).resolve(nomeOuCaminho).normalize();
    }

    private void validarNota(@Nullable String nota) {
        if (nota == null || nota.isBlank()) {
            throw new ErroValidacao("nota não pode estar em branco");
        }
        if (nota.length() > 2000) {
            throw new ErroValidacao("nota não pode exceder 2000 caracteres");
        }
    }

    private void validarScreenshot(@Nullable MultipartFile screenshot) {
        if (screenshot == null || screenshot.isEmpty()) {
            return;
        }
        if (screenshot.getSize() > propriedades.maxScreenshotSizeBytes()) {
            throw new ErroValidacao("screenshot excede o tamanho máximo permitido de %d bytes".formatted(propriedades.maxScreenshotSizeBytes()));
        }
    }

    private @Nullable String salvarScreenshot(@Nullable MultipartFile screenshot) {
        if (screenshot == null || screenshot.isEmpty()) {
            return null;
        }

        String dir = propriedades.screenshotDir();
        if (dir == null || dir.isBlank()) {
            log.warn("sgc.feedback.screenshot-dir não configurado; screenshot descartado.");
            return null;
        }

        Path diretorioBase = Path.of(dir).toAbsolutePath().normalize();
        String nomeArquivo = UUID.randomUUID() + "_" + System.currentTimeMillis() + ".webp";
        Path destino = diretorioBase.resolve(nomeArquivo).normalize();

        // Prevenção de path traversal
        if (!destino.startsWith(diretorioBase)) {
            throw new ErroValidacao("caminho de destino inválido para screenshot");
        }

        try {
            Files.createDirectories(diretorioBase);
            Files.write(destino, screenshot.getBytes());
            log.info("Screenshot salva com sucesso em: {}", destino);
            return nomeArquivo; // Salva apenas o nome do arquivo para garantir portabilidade
        } catch (IOException e) {
            log.error("Falha ao salvar screenshot em {}: {}", destino, e.getMessage());
            return null;
        }
    }

    private @Nullable String serializarMetadados(@Nullable Object metadados) {
        if (metadados == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadados);
        } catch (Exception e) {
            log.warn("Falha ao serializar metadados de feedback: {}", e.getMessage());
            return null;
        }
    }

    private String extrairRota(@Nullable Object metadados) {
        if (metadados instanceof JsonNode node) {
            JsonNode rotaNode = node.get("rotaCaminho");
            if (rotaNode != null && !rotaNode.isNull()) {
                return rotaNode.asString();
            }
        }
        return "/desconhecido";
    }
}
