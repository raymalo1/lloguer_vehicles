
package cat.copernic.nothinganpri.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuració de seguretat de l'aplicació (Spring Security).
 * <p>
 * Defineix les regles d'accés per rols (ADMIN, ADMINISTRADOR, AGENT, CLIENT),
 * configuració de login/logout, CSRF i codificació de contrasenyes amb BCrypt.
 * </p>
 *
 * @author Nothingan Pri Development Team
 * @since 1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configura la cadena de filtres de seguretat HTTP.
     *
     * @param http objecte HttpSecurity per configurar la seguretat
     * @param loginFailureHandler gestor d'errors de login personalitzat
     * @return SecurityFilterChain configurada
     * @throws Exception si hi ha error en la configuració
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            LoginFailureHandler loginFailureHandler)
            throws Exception {

        http
                // CSRF
                .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/chatbot/**")
                )
                // AUTORITZACIONS
                .authorizeHttpRequests(auth -> auth
                // RUTES PUBLIQUES
                .requestMatchers(
                        "/",
                        "/index",
                        "/login",
                        "/registre",
                        "/privacitat",
                        "/documentacio",
                        "/revisio",
                        "/confirmacio",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/uploads/**",
                        "/vehicles/*/foto",
                        "/api/chatbot/**",
                        "/corsa2005.png",
                        "/favicon.ico",
                        "/error",
                        "/recuperar-contrasenya",
                        "/reset-password",
                        "/verificar-codi",
                        "/nova-contrasenya"
                ).permitAll()
                // ADMINISTRACIO
                .requestMatchers("/admin/**")
                    .hasAnyRole("ADMIN", "ADMINISTRADOR")

                // Informe històric de reserves (RF91) — només admin
                .requestMatchers("/reserva/informe", "/reserva/informe/**",
                                 "/reserva/informe-client/**")
                    .hasAnyRole("ADMIN", "ADMINISTRADOR")

                // AGENTS I GESTIO DE SOL·LICITUDS
                .requestMatchers(
                        "/agent/**"
                ).hasAnyRole(
                        "ADMIN",
                        "ADMINISTRADOR",
                        "AGENT"
                )

                // SOL·LICITUDS D'ANUL·LACIÓ DE RESERVES
                .requestMatchers(
                        "/reserva/anullar/**",
                        "/reserva/rebutjar-anulacio/**"
                ).hasAnyRole(
                        "AGENT",
                        "ADMIN",
                        "ADMINISTRADOR"
                )
                .requestMatchers(
                        "/reserva/solicitar-anulacio/**"
                ).hasRole("CLIENT")

                // GESTIO DE CLIENTS I AGENTS
                .requestMatchers(
                        "/clients",
                        "/clients/**",
                        "/agents",
                        "/agents/**",
                        "/reserva/lliurar/**",
                        "/reserva/retornar/**",
                        "/vehicles/nou",
                        "/vehicles/*/editar",
                        "/vehicles/desa",
                        "/vehicles/*/baixa",
                        "/vehicles/*/activar"
                ).hasAnyRole(
                        "AGENT",
                        "ADMIN",
                        "ADMINISTRADOR"
                )
                // CATÀLEG DE VEHICLES
                .requestMatchers("/vehicles")
                .authenticated()
                // CLIENTS
                .requestMatchers("/client/**")
                .hasRole("CLIENT")
                // PERFIL
                .requestMatchers(
                        "/perfil/**",
                        "/perfil"
                ).authenticated()
                // RESTA DE RUTES
                .anyRequest().authenticated()
                )
                // LOGIN
                .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/", false)
                .failureHandler(loginFailureHandler)
                .permitAll()
                )
                // LOGOUT
                .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
                );

        return http.build();
    }

    /**
     * Proveïdor de codificador de contrasenyes (BCrypt).
     *
     * @return PasswordEncoder amb BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}
