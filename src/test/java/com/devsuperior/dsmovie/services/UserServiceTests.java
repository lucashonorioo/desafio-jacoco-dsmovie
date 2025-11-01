package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.entities.RoleEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.projections.UserDetailsProjection;
import com.devsuperior.dsmovie.repositories.UserRepository;
import com.devsuperior.dsmovie.tests.UserDetailsFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import com.devsuperior.dsmovie.utils.CustomUserUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class UserServiceTests {

	@InjectMocks
	private UserService service;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomUserUtil userUtil;

    private UserEntity userEntity;
    private String existingUsername;
    private String nonExistingUsername;
    private List<UserDetailsProjection> userDetailsProjections;


    @BeforeEach
    void setUp(){

        existingUsername = "maria@gmail.com";
        nonExistingUsername = "naoexiste@gmail.com";

        userEntity = UserFactory.createUserEntity();
        userEntity.setUsername(existingUsername);
        userEntity.setId(1L);
        userEntity.setPassword("123456");

        userDetailsProjections = UserDetailsFactory.createCustomAdminClientUser(existingUsername);



    }

	@Test
	public void authenticatedShouldReturnUserEntityWhenUserExists() {

        Mockito.when(userUtil.getLoggedUsername()).thenReturn(existingUsername);

        Mockito.when(userRepository.findByUsername(existingUsername)).thenReturn(Optional.of(userEntity));

        UserEntity result = service.authenticated();

        Assertions.assertNotNull(result);

        Assertions.assertEquals(userEntity.getUsername(), result.getUsername());
        Assertions.assertEquals(userEntity.getId(), result.getId());
        Assertions.assertEquals(userEntity.getPassword(), result.getPassword());

        Mockito.verify(userUtil, Mockito.times(1)).getLoggedUsername();
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(existingUsername);

	}

	@Test
	public void authenticatedShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {

        Mockito.when(userUtil.getLoggedUsername()).thenReturn(nonExistingUsername);
        Mockito.when(userRepository.findByUsername(nonExistingUsername)).thenReturn(Optional.empty());

        Assertions.assertThrows(UsernameNotFoundException.class, ()-> {
           service.authenticated();
        });

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(nonExistingUsername);
	}

	@Test
	public void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {

        Mockito.when(userRepository.searchUserAndRolesByUsername(existingUsername)).thenReturn(userDetailsProjections);

        UserDetails result = service.loadUserByUsername(existingUsername);

        Assertions.assertNotNull(result);

        Assertions.assertEquals(existingUsername, result.getUsername());

        UserEntity user = (UserEntity) result;

        Assertions.assertEquals(2, user.getRoles().size());

        var authorities = user.getRoles().stream().map(RoleEntity::getAuthority).toList();

        Assertions.assertTrue(authorities.contains("ROLE_CLIENT"));
        Assertions.assertTrue(authorities.contains("ROLE_ADMIN"));
	}

	@Test
	public void loadUserByUsernameShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {

        Mockito.when(userRepository.searchUserAndRolesByUsername(nonExistingUsername)).thenReturn(List.of());

        Assertions.assertThrows(UsernameNotFoundException.class, ()->{
           service.loadUserByUsername(nonExistingUsername);
        });

        Mockito.verify(userRepository, Mockito.times(1)).searchUserAndRolesByUsername(nonExistingUsername);
	}
}
