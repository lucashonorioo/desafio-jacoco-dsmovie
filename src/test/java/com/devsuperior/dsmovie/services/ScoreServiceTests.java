package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.tests.ScoreFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class ScoreServiceTests {
	
	@InjectMocks
	private ScoreService service;

    @Mock
    private UserService userService;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ScoreRepository scoreRepository;

    private UserEntity user;
    private MovieEntity movieEntity;
    private List<ScoreEntity> scoreEntities;
    private ScoreDTO scoreDTO;
    private  Long existingMovieId, nonExistingMovieId;


    @BeforeEach
    void setUp(){

        existingMovieId = 1L;

        user = UserFactory.createUserEntity();
        movieEntity = MovieFactory.createMovieEntity();

        ScoreEntity score = ScoreFactory.createScoreEntity();

        scoreEntities = new ArrayList<>(List.of(score));

        movieEntity.getScores().addAll(scoreEntities);

        scoreDTO = new ScoreDTO(existingMovieId, 5.0);

        nonExistingMovieId = 2L;

    }
	
	@Test
	public void saveScoreShouldReturnMovieDTO() {

        Mockito.when(userService.authenticated()).thenReturn(user);
        Mockito.when(movieRepository.findById(existingMovieId)).thenReturn(Optional.of(movieEntity));

        Mockito.when(scoreRepository.saveAndFlush(Mockito.any())).thenAnswer( invocation -> {
           ScoreEntity newScore =invocation.getArgument(0);
           movieEntity.getScores().add(newScore);
           return newScore;
        });

        Mockito.when(movieRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

        MovieDTO result = service.saveScore(scoreDTO);

        double expectedAvg = (4.5 + 5.0) / 2;
        int expectedCount = 2;

        Assertions.assertNotNull(result);

        Assertions.assertEquals(movieEntity.getId(), result.getId());

        Assertions.assertEquals(expectedCount, result.getCount());


    }
	
	@Test
	public void saveScoreShouldThrowResourceNotFoundExceptionWhenNonExistingMovieId() {

        ScoreDTO dtoWhitIdNotExistent = new ScoreDTO(nonExistingMovieId, 5.0);

        Mockito.when(userService.authenticated()).thenReturn(user);
        Mockito.when(movieRepository.findById(nonExistingMovieId)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () ->{
           service.saveScore(dtoWhitIdNotExistent);
        });

        Mockito.verify(scoreRepository, Mockito.never()).saveAndFlush(Mockito.any());
        Mockito.verify(movieRepository, Mockito.never()).save(Mockito.any());

	}
}
