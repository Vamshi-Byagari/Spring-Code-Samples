package com.vbolide.repository;

import com.vbolide.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Map;

@Repository
public class UserRepository {

	private static final Logger LOG = LoggerFactory.getLogger(UserRepository.class);

	private final JdbcTemplate jdbcTemplate;
	private final ApplicationContext context;

	public UserRepository(JdbcTemplate jdbcTemplate, ApplicationContext context) {
		this.jdbcTemplate = jdbcTemplate;
		this.context = context;
	}


	private static final String CHECK_USER_NAME_SQL = "SELECT COUNT(ID) FROM USER WHERE EMAIL = :email";
	public boolean isUsernameAvailable(String email){
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		return namedParameterJdbcTemplate.queryForObject(CHECK_USER_NAME_SQL, Collections.singletonMap("email", email), Integer.class) < 1;
	}

	private static final String INSERT_USER_SQL = "INSERT INTO USER (ENCRYPTED_ID, FIRST_NAME, LAST_NAME, EMAIL, PASSWORD, MOBILE, GENDER, ROLES) VALUES(:encId, :firstName, :lastName, :email, :password, :mobile, :gender, :roles)";
	public long insertUser(User user) throws DataAccessException {
		BeanPropertySqlParameterSource beanPropertySqlParameterSource = new BeanPropertySqlParameterSource(user);
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		int update = new NamedParameterJdbcTemplate(jdbcTemplate).update(INSERT_USER_SQL, beanPropertySqlParameterSource, keyHolder);
		return update > 0 ? keyHolder.getKey().longValue() : 0;
	}

	public User getUserWithEmail(String email){
		try {
			return getUser("SELECT * FROM USER WHERE EMAIL = :email", Collections.singletonMap("email", email));
		}catch (Exception e) {
			LOG.error("Exception@{}:getUserWithEmail with email: {} => {}", this.getClass().getSimpleName(), email, e.getLocalizedMessage());
		}
		return null;
	}

	public User getUserWithEncryptedId(String encId){
		try {
			return getUser("SELECT * FROM USER WHERE ENCRYPTED_ID = :encId", Collections.singletonMap("encId", encId));
		}catch (Exception e) {
			LOG.error("Exception@{}:getUserWithEncryptedId with encId: {} => {}", this.getClass().getSimpleName(), encId, e.getLocalizedMessage());
		}
		return null;
	}

	private User getUser(String query, Map<String, Object> sqlParameters) {
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		return namedParameterJdbcTemplate.queryForObject(query, sqlParameters, (rs, rowNum) -> {
			User user = context.getBean(User.class);
			user.setId(rs.getInt("ID"));
			user.setEncId(rs.getString("ENCRYPTED_ID"));
			user.setFirstName(rs.getString("FIRST_NAME"));
			user.setLastName(rs.getString("LAST_NAME"));
			user.setEmail(rs.getString("EMAIL"));
			user.setPassword(rs.getString("PASSWORD"));
			user.setGender(rs.getString("GENDER"));
			user.setMobile(rs.getString("MOBILE"));
			user.setRoles(rs.getString("ROLES"));
			return user;
		});
	}
	
}