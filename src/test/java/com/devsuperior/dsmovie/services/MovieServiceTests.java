package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class MovieServiceTests {
	
	@InjectMocks
	private MovieService service;

    @Mock
    private MovieRepository movieRepository;

    private String title ;
    private Pageable pageable;
    private MovieEntity movieEntity;
    private Page<MovieEntity> movieEntityPage;
    private Long existingId, nonExistingId, dependentId;
    private MovieDTO movieDTO;


    @BeforeEach
    void setUp(){

        title = "O poderoso chefão";
        pageable = PageRequest.of(0, 12);

        movieEntity = MovieFactory.createMovieEntity();

        movieEntityPage = new PageImpl<>(List.of(movieEntity), pageable, 1);

        existingId = 1L;
        nonExistingId = 100L;

        movieDTO = new MovieDTO(movieEntity);

        dependentId = 2L;

    }
	
	@Test
	public void findAllShouldReturnPagedMovieDTO() {

        Mockito.when(movieRepository.searchByTitle(title, pageable)).thenReturn(movieEntityPage);

        Page<MovieDTO> result = service.findAll(title, pageable);

        MovieDTO resultDTO = result.getContent().get(0);

        Assertions.assertNotNull(result);

        Assertions.assertEquals(1, result.getNumberOfElements());

        Assertions.assertEquals(movieEntity.getId(), resultDTO.getId());

        Assertions.assertEquals(movieEntity.getTitle(), resultDTO.getTitle());

	}
	
	@Test
	public void findByIdShouldReturnMovieDTOWhenIdExists() {

        Mockito.when(movieRepository.findById(existingId)).thenReturn(Optional.of(movieEntity));

        MovieDTO result = service.findById(existingId);

        Assertions.assertNotNull(result);

        Assertions.assertEquals(result.getId(), existingId);
        Assertions.assertEquals(result.getTitle(), movieEntity.getTitle());


	}
	
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

        Mockito.when(movieRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () ->{
            service.findById(nonExistingId);
        });
	}
	
	@Test
	public void insertShouldReturnMovieDTO() {

        Mockito.when(movieRepository.save(Mockito.any(MovieEntity.class))).thenReturn(movieEntity);

        MovieDTO result = service.insert(movieDTO);

        Assertions.assertNotNull(result);

        Assertions.assertEquals(movieEntity.getId(), result.getId());

        Assertions.assertEquals(movieEntity.getTitle(), result.getTitle());


	}
	
	@Test
	public void updateShouldReturnMovieDTOWhenIdExists() {

        Mockito.when(movieRepository.getReferenceById(existingId)).thenReturn(movieEntity);

        Mockito.when(movieRepository.save(movieEntity)).thenReturn(movieEntity);

        MovieDTO result = service.update(existingId, movieDTO);

        Assertions.assertNotNull(result);

        Assertions.assertEquals(existingId, result.getId());
        Assertions.assertEquals(movieEntity.getTitle(), result.getTitle());

	}
	
	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

        Mockito.when(movieRepository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
           service.update(nonExistingId, movieDTO);
        });

	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {

        Mockito.when(movieRepository.existsById(existingId)).thenReturn(true);

        Mockito.doNothing().when(movieRepository).deleteById(existingId);

        Assertions.assertDoesNotThrow(() ->{
            service.delete(existingId);
        });

	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

        Mockito.when(movieRepository.existsById(nonExistingId)).thenReturn(false);

        Assertions.assertThrows(ResourceNotFoundException.class, ()-> {
           service.delete(nonExistingId);
        });

	}
	
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {

        Mockito.when(movieRepository.existsById(dependentId)).thenReturn(true);

        Mockito.doThrow(DataIntegrityViolationException.class).when(movieRepository).deleteById(dependentId);

        Assertions.assertThrows(DatabaseException.class, () ->{
           service.delete(dependentId);
        });

	}
}
