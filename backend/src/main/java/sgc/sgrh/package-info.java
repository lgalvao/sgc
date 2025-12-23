/**
 * Módulo de Integração com Sistema de RH (Integration).
 * 
 * <p>Responsável pela integração com o sistema de RH externo (SGRH),
 * incluindo autenticação, autorização e sincronização de dados de usuários e unidades.</p>
 * 
 * <h2>API Pública</h2>
 * <ul>
 *   <li>{@link sgc.sgrh.SgrhService} - Facade para integração com SGRH</li>
 *   <li>{@link sgc.sgrh.api.UsuarioDto} - DTO de usuário</li>
 *   <li>{@link sgc.sgrh.api.UnidadeDto} - DTO de unidade</li>
 *   <li>{@link sgc.sgrh.api.PerfilDto} - DTO de perfil</li>
 *   <li>{@link sgc.sgrh.api.SgrhMapper} - Mapper público para conversões</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>comum - Componentes compartilhados</li>
 * </ul>
 * 
 * <h2>Eventos</h2>
 * <p>Este módulo não publica eventos atualmente.</p>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Integração com Sistema de RH",
    allowedDependencies = {"comum"}
)
package sgc.sgrh;
