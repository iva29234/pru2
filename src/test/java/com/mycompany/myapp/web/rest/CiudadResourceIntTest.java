package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.Pru1App;

import com.mycompany.myapp.domain.Ciudad;
import com.mycompany.myapp.repository.CiudadRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the CiudadResource REST controller.
 *
 * @see CiudadResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Pru1App.class)
public class CiudadResourceIntTest {

    private static final String DEFAULT_NOMBRE = "AAA";
    private static final String UPDATED_NOMBRE = "BBB";

    private static final Long DEFAULT_CANTIDAD = 1L;
    private static final Long UPDATED_CANTIDAD = 2L;

    private static final Integer DEFAULT_ANO = 1;
    private static final Integer UPDATED_ANO = 2;

    private static final String DEFAULT_CREATED_BY = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String UPDATED_CREATED_BY = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATED_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
    private static final ZonedDateTime UPDATED_CREATED_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final String DEFAULT_CREATED_DATE_STR = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(DEFAULT_CREATED_DATE);

    private static final String DEFAULT_MODIFIED_BY = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String UPDATED_MODIFIED_BY = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_MODIFIED_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
    private static final ZonedDateTime UPDATED_MODIFIED_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final String DEFAULT_MODIFIED_DATE_STR = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(DEFAULT_MODIFIED_DATE);

    @Inject
    private CiudadRepository ciudadRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restCiudadMockMvc;

    private Ciudad ciudad;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        CiudadResource ciudadResource = new CiudadResource();
        ReflectionTestUtils.setField(ciudadResource, "ciudadRepository", ciudadRepository);
        this.restCiudadMockMvc = MockMvcBuilders.standaloneSetup(ciudadResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Ciudad createEntity(EntityManager em) {
        Ciudad ciudad = new Ciudad();
        ciudad.setNombre(DEFAULT_NOMBRE);
        ciudad.setCantidad(DEFAULT_CANTIDAD);
        ciudad.setAno(DEFAULT_ANO);
        ciudad.setCreatedBy(DEFAULT_CREATED_BY);
        ciudad.setCreatedDate(DEFAULT_CREATED_DATE);
        ciudad.setModifiedBy(DEFAULT_MODIFIED_BY);
        ciudad.setModifiedDate(DEFAULT_MODIFIED_DATE);
        return ciudad;
    }

    @Before
    public void initTest() {
        ciudad = createEntity(em);
    }

    @Test
    @Transactional
    public void createCiudad() throws Exception {
        int databaseSizeBeforeCreate = ciudadRepository.findAll().size();

        // Create the Ciudad

        restCiudadMockMvc.perform(post("/api/ciudads")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(ciudad)))
                .andExpect(status().isCreated());

        // Validate the Ciudad in the database
        List<Ciudad> ciudads = ciudadRepository.findAll();
        assertThat(ciudads).hasSize(databaseSizeBeforeCreate + 1);
        Ciudad testCiudad = ciudads.get(ciudads.size() - 1);
        assertThat(testCiudad.getNombre()).isEqualTo(DEFAULT_NOMBRE);
        assertThat(testCiudad.getCantidad()).isEqualTo(DEFAULT_CANTIDAD);
        assertThat(testCiudad.getAno()).isEqualTo(DEFAULT_ANO);
        assertThat(testCiudad.getCreatedBy()).isEqualTo(DEFAULT_CREATED_BY);
        assertThat(testCiudad.getCreatedDate()).isEqualTo(DEFAULT_CREATED_DATE);
        assertThat(testCiudad.getModifiedBy()).isEqualTo(DEFAULT_MODIFIED_BY);
        assertThat(testCiudad.getModifiedDate()).isEqualTo(DEFAULT_MODIFIED_DATE);
    }

    @Test
    @Transactional
    public void checkNombreIsRequired() throws Exception {
        int databaseSizeBeforeTest = ciudadRepository.findAll().size();
        // set the field null
        ciudad.setNombre(null);

        // Create the Ciudad, which fails.

        restCiudadMockMvc.perform(post("/api/ciudads")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(ciudad)))
                .andExpect(status().isBadRequest());

        List<Ciudad> ciudads = ciudadRepository.findAll();
        assertThat(ciudads).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllCiudads() throws Exception {
        // Initialize the database
        ciudadRepository.saveAndFlush(ciudad);

        // Get all the ciudads
        restCiudadMockMvc.perform(get("/api/ciudads?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(ciudad.getId().intValue())))
                .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE.toString())))
                .andExpect(jsonPath("$.[*].cantidad").value(hasItem(DEFAULT_CANTIDAD.intValue())))
                .andExpect(jsonPath("$.[*].ano").value(hasItem(DEFAULT_ANO)))
                .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY.toString())))
                .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE_STR)))
                .andExpect(jsonPath("$.[*].modifiedBy").value(hasItem(DEFAULT_MODIFIED_BY.toString())))
                .andExpect(jsonPath("$.[*].modifiedDate").value(hasItem(DEFAULT_MODIFIED_DATE_STR)));
    }

    @Test
    @Transactional
    public void getCiudad() throws Exception {
        // Initialize the database
        ciudadRepository.saveAndFlush(ciudad);

        // Get the ciudad
        restCiudadMockMvc.perform(get("/api/ciudads/{id}", ciudad.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(ciudad.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE.toString()))
            .andExpect(jsonPath("$.cantidad").value(DEFAULT_CANTIDAD.intValue()))
            .andExpect(jsonPath("$.ano").value(DEFAULT_ANO))
            .andExpect(jsonPath("$.createdBy").value(DEFAULT_CREATED_BY.toString()))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE_STR))
            .andExpect(jsonPath("$.modifiedBy").value(DEFAULT_MODIFIED_BY.toString()))
            .andExpect(jsonPath("$.modifiedDate").value(DEFAULT_MODIFIED_DATE_STR));
    }

    @Test
    @Transactional
    public void getNonExistingCiudad() throws Exception {
        // Get the ciudad
        restCiudadMockMvc.perform(get("/api/ciudads/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCiudad() throws Exception {
        // Initialize the database
        ciudadRepository.saveAndFlush(ciudad);
        int databaseSizeBeforeUpdate = ciudadRepository.findAll().size();

        // Update the ciudad
        Ciudad updatedCiudad = ciudadRepository.findOne(ciudad.getId());
        updatedCiudad.setNombre(UPDATED_NOMBRE);
        updatedCiudad.setCantidad(UPDATED_CANTIDAD);
        updatedCiudad.setAno(UPDATED_ANO);
        updatedCiudad.setCreatedBy(UPDATED_CREATED_BY);
        updatedCiudad.setCreatedDate(UPDATED_CREATED_DATE);
        updatedCiudad.setModifiedBy(UPDATED_MODIFIED_BY);
        updatedCiudad.setModifiedDate(UPDATED_MODIFIED_DATE);

        restCiudadMockMvc.perform(put("/api/ciudads")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedCiudad)))
                .andExpect(status().isOk());

        // Validate the Ciudad in the database
        List<Ciudad> ciudads = ciudadRepository.findAll();
        assertThat(ciudads).hasSize(databaseSizeBeforeUpdate);
        Ciudad testCiudad = ciudads.get(ciudads.size() - 1);
        assertThat(testCiudad.getNombre()).isEqualTo(UPDATED_NOMBRE);
        assertThat(testCiudad.getCantidad()).isEqualTo(UPDATED_CANTIDAD);
        assertThat(testCiudad.getAno()).isEqualTo(UPDATED_ANO);
        assertThat(testCiudad.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testCiudad.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
        assertThat(testCiudad.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
        assertThat(testCiudad.getModifiedDate()).isEqualTo(UPDATED_MODIFIED_DATE);
    }

    @Test
    @Transactional
    public void deleteCiudad() throws Exception {
        // Initialize the database
        ciudadRepository.saveAndFlush(ciudad);
        int databaseSizeBeforeDelete = ciudadRepository.findAll().size();

        // Get the ciudad
        restCiudadMockMvc.perform(delete("/api/ciudads/{id}", ciudad.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Ciudad> ciudads = ciudadRepository.findAll();
        assertThat(ciudads).hasSize(databaseSizeBeforeDelete - 1);
    }
}
