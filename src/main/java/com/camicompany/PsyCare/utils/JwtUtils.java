package com.camicompany.PsyCare.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    private final String privateKey;
    private final String userGenerator;

    public JwtUtils(@Value("${jwt.private.key}") String privateKey,
                    @Value("${jwt.user.generator}") String userGenerator) {
        this.privateKey = privateKey;
        this.userGenerator = userGenerator;
    }

    public String generateToken(Authentication authentication) {
        //algorith to use
        Algorithm algorithm = Algorithm.HMAC256(privateKey);

        //retrieve user roles and permissions
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String username = user.getUsername();

        String authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        //create the token
        String jwtToken = JWT.create()
                .withIssuer(userGenerator)
                .withSubject(username)
                .withClaim("authorities", authorities)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + (24* 60 * 60 * 1000))) //The token lasts 24 horas
                .withNotBefore(new Date(System.currentTimeMillis()))
                .sign(algorithm);
        return jwtToken;
    }
    public DecodedJWT validateToken (String token){
        try {
            //get the algorithm used to encrypt and create the verifier
            Algorithm algorithm = Algorithm.HMAC256(privateKey);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(userGenerator)
                    .build();


            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT;
        }
        catch (JWTVerificationException exception){
            throw new JWTVerificationException("Invalid token. Not authenticated");
        }
    }

    //method to get the username from the token
    public String extractUsername (DecodedJWT decodedJWT){
        return decodedJWT.getSubject();
    }

    //method to get a specific claim from the token
    public Claim getSpecificClaim (DecodedJWT decodedJWT, String claimName){

        return decodedJWT.getClaim(claimName);
    }

    //method to get all claims from the token
    public Map<String, Claim> returnAllClaims (DecodedJWT decodedJWT){
        return decodedJWT.getClaims();
    }

}
