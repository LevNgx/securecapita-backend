package com.fullstackprojectbackend.securecapita.filter;

import com.fullstackprojectbackend.securecapita.provider.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.fullstackprojectbackend.securecapita.utils.ExceptionUtil.processError;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.data.mapping.Alias.ofNullable;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;


@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    private static final String[] PUBLIC_ROUTES = {"/user/login", "user/register", "user/verify/code", "user/refresh/token"};
    private final TokenProvider tokenProvider;

    private static final String TOKEN_KEY = "token";
    private static final String EMAIL_KEY = "email";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            Map<String, String> values = getRequestValues(request);
            log.info("came here" + "1");
            String token = getToken(request);
            log.info("came here" + "2");
            if(tokenProvider.isTokenValid(values.get(EMAIL_KEY), token)){
                log.info("came here" + "3" + values + "gap " );
                List<GrantedAuthority> authorities = tokenProvider.getAuthorities(values.get(TOKEN_KEY));
                log.info(authorities.toString() + "came here" + "4");
                Authentication authentication = tokenProvider.getAuthentication(values.get(EMAIL_KEY), authorities, request);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            else {
                SecurityContextHolder.clearContext();
            }
            filterChain.doFilter(request, response);

        }
        catch (Exception e){
            log.error(e.getMessage() + "coming from here ?");
            processError(request, response, e);
        }
    }

    private String getToken(HttpServletRequest request) {
        try{
            return request.getHeader(AUTHORIZATION)
                    .startsWith(TOKEN_PREFIX) ? request.getHeader(AUTHORIZATION).replace(TOKEN_PREFIX, EMPTY) : "";
        }
        catch (Exception e){
            log.error(e.getMessage());
            return "";
        }

    }

    private Map<String, String> getRequestValues(HttpServletRequest request) {
        return Map.of(EMAIL_KEY, tokenProvider.getSubject(getToken(request), request), TOKEN_KEY, getToken(request));

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
       return request.getHeader(AUTHORIZATION) == null || !request.getHeader(AUTHORIZATION).startsWith(TOKEN_PREFIX)
               || request.getMethod().equalsIgnoreCase("OPTIONS") || asList(PUBLIC_ROUTES).contains(request.getRequestURI());
    }

}
