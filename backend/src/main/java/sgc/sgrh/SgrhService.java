package sgc.sgrh;

import sgc.sgrh.dto.PerfilDto;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.UsuarioDto;

import java.util.List;
import java.util.Optional;

/**
 * Interface para serviço de integração com SGRH.
 * <p>
 * Responsável por consultar dados de usuários, unidades, responsabilidades
 * e perfis nas views do Oracle SGRH.
 */
public interface SgrhService {
    // ========== USUÁRIOS ==========
    /**
     * Busca usuário por título (CPF).
     * 
     * @param titulo CPF/título do servidor
     * @return Optional com dados do usuário se encontrado
     */
    Optional<UsuarioDto> buscarUsuarioPorTitulo(String titulo);
    
    /**
     * Busca usuário por email.
     * 
     * @param email Email do servidor
     * @return Optional com dados do usuário se encontrado
     */
    Optional<UsuarioDto> buscarUsuarioPorEmail(String email);
    
    /**
     * Lista todos os usuários ativos.
     * 
     * @return Lista de usuários ativos
     */
    List<UsuarioDto> buscarUsuariosAtivos();
    
    // ========== UNIDADES ==========
    
    /**
     * Busca unidade por código.
     * 
     * @param codigo Código da unidade
     * @return Optional com dados da unidade se encontrada
     */
    Optional<UnidadeDto> buscarUnidadePorCodigo(Long codigo);
    
    /**
     * Lista todas as unidades ativas.
     * 
     * @return Lista de unidades ativas
     */
    List<UnidadeDto> buscarUnidadesAtivas();
    
    /**
     * Busca subunidades de uma unidade pai.
     * 
     * @param codigoPai Código da unidade pai
     * @return Lista de subunidades
     */
    List<UnidadeDto> buscarSubunidades(Long codigoPai);
    
    /**
     * Constrói árvore hierárquica completa de unidades.
     * Retorna as unidades raiz com suas subunidades aninhadas.
     * 
     * @return Lista de unidades raiz com hierarquia completa
     */
    List<UnidadeDto> construirArvoreHierarquica();
    
    // ========== RESPONSABILIDADES ==========
    
    /**
     * Busca responsável (titular e substituto) de uma unidade.
     * 
     * @param unidadeCodigo Código da unidade
     * @return Optional com dados do responsável se encontrado
     */
    Optional<ResponsavelDto> buscarResponsavelUnidade(Long unidadeCodigo);
    
    /**
     * Busca unidades onde o servidor é responsável (titular ou substituto).
     * 
     * @param titulo CPF/título do servidor
     * @return Lista de códigos de unidades onde é responsável
     */
    List<Long> buscarUnidadesOndeEhResponsavel(String titulo);
    
    // ========== PERFIS ==========
    
    /**
     * Busca todos os perfis de um usuário.
     * 
     * @param titulo CPF/título do servidor
     * @return Lista de perfis com unidades associadas
     */
    List<PerfilDto> buscarPerfisUsuario(String titulo);
    
    /**
     * Verifica se usuário tem perfil específico em uma unidade.
     * 
     * @param titulo CPF/título do servidor
     * @param perfil Nome do perfil (ADMIN, GESTOR, CHEFE, SERVIDOR)
     * @param unidadeCodigo Código da unidade
     * @return true se usuário tem o perfil na unidade
     */
    boolean usuarioTemPerfil(String titulo, String perfil, Long unidadeCodigo);
    
    /**
     * Busca unidades onde usuário tem perfil específico.
     * 
     * @param titulo CPF/título do servidor
     * @param perfil Nome do perfil
     * @return Lista de códigos de unidades
     */
    List<Long> buscarUnidadesPorPerfil(String titulo, String perfil);
}