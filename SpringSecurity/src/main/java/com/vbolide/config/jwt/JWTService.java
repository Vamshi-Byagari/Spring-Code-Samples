package com.vbolide.config.jwt;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
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

	private final ApplicationContext context;


	@Value("${iss}")
	private String jwtIssuer;

	@Value("${exp}")
	private int jwtExpiryInMinutes;
	
	@Value("${secret}")
	private String encodedSecret;


	public JWTService(ApplicationContext context) {
		this.context = context;
	}



	@PostConstruct
	public void checkingValues() {
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
		map.put(JWTClaim.SUBJECT, user.getEncId());

		LocalDateTime now = LocalDateTime.now();
		map.put(JWTClaim.ISSUED_AT, now.atZone(ZoneId.systemDefault()).toEpochSecond());
		map.put(JWTClaim.EXPIRATION, now.plusMinutes(jwtExpiryInMinutes).atZone(ZoneId.systemDefault()).toEpochSecond());
		return map;
	}

	private Map<String, Object> privateClaims(User user){
		HashMap<String, Object> map = new HashMap<>();
		map.put(JWTClaim.FIRST_NAME, user.getFirstName());
		map.put(JWTClaim.LAST_NAME, user.getLastName());
		map.put(JWTClaim.EMAIL, user.getEmail());
		map.put(JWTClaim.ROLES, user.getRoles());
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

	public User getUser(String jwt) {
		Claims claims = getClaims(jwt);
		User user = this.context.getBean(User.class);
		user.setEncId(claims.getSubject());
		user.setFirstName(claims.get(JWTClaim.FIRST_NAME, String.class));
		user.setLastName(claims.get(JWTClaim.LAST_NAME, String.class));
		user.setEmail(claims.get(JWTClaim.EMAIL, String.class));
		user.setRoles(claims.get(JWTClaim.ROLES, String.class));
		return user;
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


	    //JWT should contain only necessary claims (exclude any sensitive information from JWT)

	    //Private Claims
	    public static final String FIRST_NAME = "fname";
	    public static final String LAST_NAME = "lname";
	    public static final String EMAIL = "email";
	    public static final String ROLES = "roles";

	}

}