package com.vbolide.config.iwt;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import com.vbolide.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
@PropertySource("classpath:jwt.properties")
public class JWTService {


	@Value("${iss}")
	private String jwtIssuer;

	@Value("${exp}")
	private int jwtExpiryInMinutes;
	
	@Value("${secret}")
	private String encodedSecret;


	@PostConstruct
	public void values() {
		System.out.printf("JWT PROPERTIES :: jwtIssuer: %s, jwtExpiryInMinutes: %s, secretToSignJWT: %s%n", jwtIssuer, jwtExpiryInMinutes, encodedSecret);
	}



	/** START:: CODE TO CREATE JWT **/

	public String createJWT(User user) {
		Map<String, Object> jwtClaims = jwtClaims(user);
		jwtClaims.putAll(privateClaims(user));
		return buildJWT(jwtClaims);
	}

	public String createJWT(Map<String, String> claims, User user) {
		Map<String, Object> jwtClaims = jwtClaims(user);
		jwtClaims.putAll(privateClaims(user));
		if(claims != null && !claims.isEmpty()) {
			jwtClaims.putAll(claims);
		}
		return buildJWT(jwtClaims);
	}

	private Map<String, Object> jwtClaims(User user){
		HashMap<String, Object> map = new HashMap<>();
		map.put(JWTClaim.ISSUER, jwtIssuer);
		map.put(JWTClaim.SUBJECT, user.getEmail());

		LocalDateTime now = LocalDateTime.now();
		map.put(JWTClaim.ISSUED_AT, now.atZone(ZoneId.systemDefault()).toEpochSecond());
		map.put(JWTClaim.EXPIRATION, now.plusMinutes(jwtExpiryInMinutes).atZone(ZoneId.systemDefault()).toEpochSecond());
		return map;
	}

	private Map<String, Object> privateClaims(User user){
		HashMap<String, Object> map = new HashMap<>();
		map.put(JWTClaim.FULL_NAME, user.getFirstName());
		map.put(JWTClaim.LAST_NAME, user.getLastName());
		map.put(JWTClaim.EMAIL, user.getEmail());
		map.put(JWTClaim.GENDER, user.getGender());
		return map;
	}

	private SecretKey signinKey() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedSecret));
	}

	private String buildJWT(Map<String, Object> jwtClaims) {
		return Jwts.builder().setClaims(jwtClaims).signWith(signinKey()).compact();
	}

	/** END:: CODE TO CREATE JWT **/



	/** START:: VALIDATING AND RETRIEVING CLAIM FROM JWT **/

	private Claims getClaims(String jwt){
		return Jwts.parserBuilder().setSigningKey(signinKey()).build().parseClaimsJws(jwt).getBody();
	}

	public <T> T getClaimFromJWT(String jwt, Function<Claims, T> claimsResolver) {
		return claimsResolver.apply(getClaims(jwt));
	}

	public String getUsernameFromJWT(String jwt){
		return getClaimFromJWT(jwt, Claims::getSubject);
	}

	public boolean isJWTExpired(String jwt){
		return getClaimFromJWT(jwt, Claims::getExpiration).before(new Date());
	}
	
	/** END:: VALIDATING AND RETRIEVING CLAIM FROM JWT **/



	public interface JWTClaim {

		//PreDefined Claims
	    public static final String ISSUER = "iss";
	    public static final String SUBJECT = "sub";
	    public static final String AUDIENCE = "aud";
	    public static final String EXPIRATION = "exp";
	    public static final String NOT_BEFORE = "nbf";
	    public static final String ISSUED_AT = "iat";
	    public static final String ID = "jti";

	    //Private Claims
	    public static final String FULL_NAME = "fname";
	    public static final String LAST_NAME = "lname";
	    public static final String EMAIL = "email";
	    public static final String MOBILE = "mobile"; //it is not recommended to include sensitive information in a JWT
	    public static final String GENDER = "gender";	    

	}

}