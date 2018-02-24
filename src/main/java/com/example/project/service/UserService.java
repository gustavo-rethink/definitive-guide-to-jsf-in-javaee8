package com.example.project.service;

import java.util.Optional;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import com.example.project.model.Credentials;
import com.example.project.model.User;
import com.example.project.service.exception.DuplicateEntityException;
import com.example.project.service.exception.EntityNotFoundException;
import com.example.project.service.exception.InvalidPasswordException;
import com.example.project.service.exception.InvalidUsernameException;

@Stateless
public class UserService {

	@PersistenceContext
	private EntityManager entityManager;

	public User getById(Long id) {
	    try {
	        return entityManager
	            .createQuery("FROM User u WHERE u.id = :id", User.class)
	            .setParameter("id", id)
	            .getSingleResult();
	    }
	    catch (NoResultException e) {
	        throw new EntityNotFoundException(e);
	    }
	}

	public Optional<User> findByEmail(String email) {
	    try {
	        return Optional.of(entityManager
	            .createQuery("FROM User u WHERE u.email = :email", User.class)
	            .setParameter("email", email)
	            .getSingleResult());
	    }
	    catch (NoResultException e) {
	        return Optional.empty();
	    }
	}

	public User getByEmailAndPassword(String email, String password) {
	    User user = findByEmail(email).orElseThrow(InvalidUsernameException::new);

	    if (!user.getCredentials().isValid(password)) {
	        throw new InvalidPasswordException();
	    }

	    return user;
	}

	public User register(String email, String password) {
	    if (findByEmail(email).isPresent()) {
	        throw new DuplicateEntityException();
	    }

	    User user = new User();
	    user.setEmail(email);
	    entityManager.persist(user);
	    setPassword(user, password);
	    return user;
	}

	public void setPassword(User user, String password) {
		User managedUser = entityManager.merge(user);
		Credentials credentials = managedUser.getCredentials();

		if (credentials == null) {
			credentials = new Credentials();
			credentials.setUser(managedUser);
			entityManager.persist(credentials);
		}

		credentials.setPassword(password);
	}

}
