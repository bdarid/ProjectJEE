package ma.ensam.clubfootball.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Ressources publiques
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/error").permitAll()

                        // Administration pure (BF-08, BF-27)
                        .requestMatchers("/entraineurs/**", "/utilisateurs/**").hasRole("ADMIN")

                        // Joueurs et Équipes : lecture pour ADMIN et ENTRAINEUR, écriture uniquement ADMIN (BF-01 à BF-10)
                        .requestMatchers(HttpMethod.GET, "/joueurs/**", "/equipes/**").hasAnyRole("ADMIN", "ENTRAINEUR")
                        .requestMatchers("/joueurs/**", "/equipes/**").hasRole("ADMIN")

                        // Entraînements et Matchs : lecture pour ADMIN et ENTRAINEUR, gestion pour ENTRAINEUR (BF-11 à BF-19)
                        .requestMatchers(HttpMethod.GET, "/entrainements/**", "/matchs/**").hasAnyRole("ADMIN", "ENTRAINEUR")
                        .requestMatchers("/entrainements/**", "/matchs/**").hasRole("ENTRAINEUR")

                        // Dashboard et espace personnel accessibles à tout utilisateur connecté (BF-21, BF-22, BF-24)
                        .requestMatchers("/", "/dashboard/**").authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }
}