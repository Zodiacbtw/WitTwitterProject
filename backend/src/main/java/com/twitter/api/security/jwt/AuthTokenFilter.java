package com.twitter.api.security.jwt;

import com.twitter.api.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // YENİ LOG: Filtrenin çağrılıp çağrılmadığını ve hangi URI için olduğunu kontrol et
        logger.info("AuthTokenFilter: doFilterInternal CALLED for URI: {}", request.getRequestURI());

        // YENİ KONTROL: Bağımlılıkların null olup olmadığını kontrol et
        if (jwtUtils == null) {
            logger.error("AuthTokenFilter: jwtUtils IS NULL! Dependency injection might have failed.");
        }
        if (userDetailsService == null) {
            logger.error("AuthTokenFilter: userDetailsService IS NULL! Dependency injection might have failed.");
        }

        try {
            // YENİ LOG: JWT parse etme girişimini logla
            logger.info("AuthTokenFilter: Attempting to parse JWT.");
            String jwt = parseJwt(request);
            // YENİ LOG: Parse edilen JWT'nin varlığını logla (token'ın kendisini değil)
            logger.info("AuthTokenFilter: Parsed JWT is: {}", (jwt != null ? "Present (length: " + jwt.length() + ")" : "NULL or Empty"));

            if (jwt != null && jwtUtils != null && jwtUtils.validateJwtToken(jwt)) { // jwtUtils null kontrolü eklendi
                // YENİ LOG: JWT'nin geçerli olduğunu logla
                logger.info("AuthTokenFilter: JWT validation successful.");
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                // YENİ LOG: JWT'den çıkarılan kullanıcı adını logla
                logger.info("AuthTokenFilter: Username from JWT: {}", username);

                if (userDetailsService != null) { // userDetailsService null kontrolü eklendi
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    // YENİ LOG: UserDetails'in yüklendiğini logla
                    logger.info("AuthTokenFilter: UserDetails loaded for username: {}", username);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null, // Credentials genellikle null olur token tabanlı auth'ta
                                    userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // YENİ LOG: Authentication objesini SecurityContextHolder'a set etme girişimini logla
                    logger.info("AuthTokenFilter: Setting Authentication in SecurityContextHolder for user: {}", username);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    // YENİ LOG: Authentication'ın başarıyla set edildiğini logla
                    logger.info("AuthTokenFilter: Authentication SET successfully in SecurityContextHolder for user: {}. Current Authentication: {}", username, SecurityContextHolder.getContext().getAuthentication());
                } else {
                    logger.error("AuthTokenFilter: userDetailsService is NULL, cannot load user details.");
                }
            } else {
                if (jwt == null) {
                    logger.info("AuthTokenFilter: JWT is NULL or Empty, skipping JWT authentication.");
                } else if (jwtUtils == null) {
                    logger.error("AuthTokenFilter: jwtUtils is NULL, cannot validate JWT.");
                } else {
                    // validateJwtToken false döndüğünde JwtUtils içinde zaten loglama olmalı
                    logger.warn("AuthTokenFilter: JWT validation FAILED (jwtUtils.validateJwtToken returned false).");
                }
            }
        } catch (Exception e) {
            // YENİ LOG: Exception durumunda stack trace'i de logla
            logger.error("AuthTokenFilter: Exception occurred while setting user authentication: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
        // YENİ LOG: Filtrenin tamamlandığını logla
        logger.info("AuthTokenFilter: doFilterInternal COMPLETED for URI: {}", request.getRequestURI());
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        // YENİ LOG (Opsiyonel, debug için): Gelen Authorization header'ını logla (production'da dikkatli olun)
        // logger.debug("AuthTokenFilter (parseJwt): Authorization Header: {}", headerAuth);

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            // YENİ LOG (Opsiyonel, debug için): Çıkarılan token'ın ilk birkaç karakterini logla
            String token = headerAuth.substring(7);
            // logger.debug("AuthTokenFilter (parseJwt): Extracted Token (first 10 chars): {}", (token.length() > 10 ? token.substring(0, 10) : token));
            return token;
        }
        // logger.debug("AuthTokenFilter (parseJwt): JWT String is null or does not start with Bearer, returning null.");
        return null;
    }
}