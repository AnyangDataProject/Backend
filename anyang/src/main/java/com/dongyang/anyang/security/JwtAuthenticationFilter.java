package com.dongyang.anyang.security;

import com.dongyang.anyang.auth.JwtProvider;
import com.dongyang.anyang.domain.user.User;
import com.dongyang.anyang.domain.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);


        if(token != null && jwtProvider.validateToken(token)) {
            Long userId = jwtProvider.getUserId(token);
            Optional<User> userOptional = userRepository.findById(userId);
            if(userOptional.isPresent()) {
                CustomUserDetails userDetails = new CustomUserDetails(userOptional.get());

                System.out.println("========== JWT AUTH ==========");
                System.out.println("USER ID = " + userDetails.getId());
                System.out.println("USER EMAIL = " + userDetails.getUsername());
                System.out.println("USER ROLE = " + userOptional.get().getRole());
                System.out.println("AUTHORITIES = " + userDetails.getAuthorities());
                System.out.println("==============================");

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            if(userOptional.isPresent()) {
                CustomUserDetails userDetails = new CustomUserDetails(userOptional.get());

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
