package com.main.netbankingapp.service;
import java.util.Date;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;


@Service
public class JwtService {
	private final String SECRET= "4SewcVYDfO8Ekh/WYT1stR0u9O7U6Jsz67jZ8Mm/bP4=";
	
	public String generateToken(Map<String, Object> claims, String subject) {
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(subject)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 3600000))
				.signWith(SignatureAlgorithm.HS256, SECRET.getBytes())
				.compact();
	}
	
	
	@Bean
	public JwtDecoder jwtDecoder() {

	    SecretKeySpec secretKey = new SecretKeySpec(SECRET.getBytes(), "HmacSHA256");
	    return NimbusJwtDecoder.withSecretKey(secretKey).build();
	}

}
