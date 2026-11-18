
package cat.copernic.nothinganpri.service;

import cat.copernic.nothinganpri.entity.UsuariJpaEntity;
import cat.copernic.nothinganpri.repository.UsuariJpaRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de recuperar los datos de un usuario para Spring Security.
 *
 * Permite autenticar a los usuarios utilizando su dirección de correo
 * electrónico como nombre de usuario.
 *
 * También carga la contraseña cifrada, los roles y el estado de activación
 * de la cuenta.
 *
 * @author lopez
 */
@Service
public class UsuariDetailsService implements UserDetailsService {

    /**
     * Repositorio utilizado para consultar los usuarios registrados.
     */
    private final UsuariJpaRepository usuariRepository;

    /**
     * Constructor del servicio.
     *
     * @param usuariRepository repositorio de usuarios
     */
    public UsuariDetailsService(UsuariJpaRepository usuariRepository) {
        this.usuariRepository = usuariRepository;
    }

    /**
     * Busca un usuario mediante su correo electrónico para realizar
     * el proceso de autenticación de Spring Security.
     *
     * Recupera el correo electrónico, la contraseña cifrada, los roles
     * y el estado de activación de la cuenta.
     *
     * @param email correo electrónico utilizado para iniciar sesión
     * @return información del usuario para Spring Security
     * @throws UsernameNotFoundException si no existe ningún usuario
     *         con el correo electrónico indicado
     */
    @Override
    public UserDetails loadUserByUsername(String email) {

        UsuariJpaEntity usuari = usuariRepository.findByEmail(email.trim())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuari no trobat"));

        return User.withUsername(usuari.getEmail())
                .password(usuari.getPassword())
                .roles(usuari.getRol().trim())
                .disabled(!usuari.isActivo())
                .build();
    }
}
