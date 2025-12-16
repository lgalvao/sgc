package sgc.comum.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

/**
 * Utilitário para assinatura e validação de tokens simulados.
 *
 * <p>Utiliza HMAC-SHA256 com uma chave gerada aleatoriamente na inicialização da aplicação.
 * Isso garante que tokens não possam ser forjados externamente, embora sejam invalidados
 * a cada reinicialização do servidor.
 */
public class TokenSimuladoUtil {
    private static final String ALGORITMO = "HmacSHA256";
    private static final String SEGREDO = UUID.randomUUID().toString();

    private TokenSimuladoUtil() {
    }

    /**
     * Gera uma assinatura HMAC-SHA256 para o conteúdo fornecido.
     *
     * @param conteudo O conteúdo (geralmente Base64) a ser assinado.
     * @return A assinatura em Base64.
     */
    public static String assinar(String conteudo) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            SecretKeySpec secretKeySpec = new SecretKeySpec(SEGREDO.getBytes(StandardCharsets.UTF_8), ALGORITMO);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(conteudo.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Erro ao assinar token: algoritmo ou chave inválidos", e);
        }
    }

    /**
     * Valida se a assinatura corresponde ao conteúdo.
     *
     * @param conteudo O conteúdo original.
     * @param assinatura A assinatura a ser verificada.
     * @return true se a assinatura for válida, false caso contrário.
     */
    public static boolean validar(String conteudo, String assinatura) {
        if (conteudo == null || assinatura == null) {
            return false;
        }
        String assinaturaEsperada = assinar(conteudo);
        return assinaturaEsperada.equals(assinatura);
    }
}
