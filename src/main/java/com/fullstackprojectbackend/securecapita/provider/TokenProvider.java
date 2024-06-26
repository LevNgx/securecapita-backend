package com.fullstackprojectbackend.securecapita.provider;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fullstackprojectbackend.securecapita.domain.UserPrincipal;
import com.fullstackprojectbackend.securecapita.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@Component
@Slf4j
@RequiredArgsConstructor
public class TokenProvider {
    private static final String LEVIATHAN_X_LLC = "LEVIATHAN_X_LLC";
    private static final String CUSTOMER_MANAGEMENT_SERVICE = "CUSTOMER_MANAGEMENT_SERVICE";
    private static final String AUTHORITIES = "Authorities" ;
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 1_800_0000;
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 432_000_000;
    private final UserService userService;
    @Value("${jwt.secret}")
    private String secret;

    public String createAccessToken(UserPrincipal userPrincipal){
        String[] claims = getClaimsFromUser(userPrincipal);
        return JWT.create().withIssuer(LEVIATHAN_X_LLC).withAudience(CUSTOMER_MANAGEMENT_SERVICE)
                .withIssuedAt(new Date()).withSubject(userPrincipal.getUsername()).withArrayClaim(AUTHORITIES,claims)
                .withExpiresAt(new Date(currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .sign(HMAC512(secret.getBytes()));
    }

    public String createRefreshToken(UserPrincipal userPrincipal){

        return JWT.create().withIssuer(LEVIATHAN_X_LLC).withAudience(CUSTOMER_MANAGEMENT_SERVICE)
                .withIssuedAt(new Date()).withSubject(userPrincipal.getUsername()).
                withExpiresAt(new Date(currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
                .sign(HMAC512(secret.getBytes()));
    }

    public String getSubject(String token, HttpServletRequest request){

        try{
            JWTVerifier verifier = getJWTVerifier();
            return  verifier.verify(token).getSubject();
        }
        catch (TokenExpiredException exception){
            log.error(exception.getMessage());
            request.setAttribute("expiredMessage",exception.getMessage());
            throw exception;

        }
        catch (InvalidClaimException exception){
            log.error(exception.getMessage());
            request.setAttribute("inValidClaim",exception.getMessage());
            throw exception;
        }
        catch (Exception e){
            throw e;
        }

    }



    public List<GrantedAuthority> getAuthorities(String token){
        JWTVerifier verifier = getJWTVerifier();
//        log.info(verifier.verify(token).getClaims().toString());
//        System.out.println(verifier.verify(token).getClaim(AUTHORITIES).isMissing());
        if(verifier.verify(token).getClaim(AUTHORITIES).isMissing())
            return new ArrayList<>();
        String[] claims = getClaimsFromToken(token);
        return stream(claims).map(SimpleGrantedAuthority::new).collect(toList());
    }

    private  String[] getClaimsFromUser(UserPrincipal userPrincipal){
        return userPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new);
    }

    private  String[] getClaimsFromToken(String token){
        JWTVerifier verifier = getJWTVerifier();
        return verifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);
    }

    public boolean isTokenValid(String email, String token){
        JWTVerifier verifier = getJWTVerifier();
        return StringUtils.isNotEmpty(email) && !isTokenExpired(verifier, token);
    }

    private boolean isTokenExpired(JWTVerifier verifier, String token) {
        Date expiration = verifier.verify(token).getExpiresAt();
        return expiration.before(new Date());
    }

    public Authentication getAuthentication(String email, List<GrantedAuthority> authorities, HttpServletRequest request){
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userService.getUserByEmail(email), null, authorities);
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return usernamePasswordAuthenticationToken;

    }


    private JWTVerifier getJWTVerifier() {
        JWTVerifier verifier;
        try{
            Algorithm algorithm = HMAC512(secret);
            verifier = JWT.require(algorithm).withIssuer(LEVIATHAN_X_LLC).build();
            return verifier;
        }
        catch (JWTVerificationException e){
            log.error(e.getMessage());
            throw new JWTVerificationException("token is not valid/verified");
        }
    }

}
