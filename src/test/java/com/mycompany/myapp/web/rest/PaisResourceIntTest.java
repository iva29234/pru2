package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.Pru1App;

import com.mycompany.myapp.domain.Pais;
import com.mycompany.myapp.repository.PaisRepository;

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
 * Test class for the PaisResource REST controller.
 *
 * @see PaisResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Pru1App.class)
public class PaisResourceIntTest {

    private static final Long DEFAULT_PAIS_ID = 1L;
    private static final Long UPDATED_PAIS_ID = 2L;

    private static final String DEFAULT_PAIS_NOMBRE = "AAAAA";
    private static final String UPDATED_PAIS_NOMBRE = "BBBBB";

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
    private PaisRepository paisRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restPaisMockMvc;

    private Pais pais;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PaisResource paisResource = new PaisResource();
        ReflectionTestUtils.setField(paisResource, "paisRepository", paisRepository);
        this.restPaisMockMvc = MockMvcBuilders.standaloneSetup(paisResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Pais createEntity(EntityManager em) {
        Pais pais = new Pais();
        pais.setPaisId(DEFAULT_PAIS_ID);
        pais.setPaisNombre(DEFAULT_PAIS_NOMBRE);
        pais.setCreatedBy(DEFAULT_CREATED_BY);
        pais.setCreatedDate(DEFAULT_CREATED_DATE);
        pais.setModifiedBy(DEFAULT_MODIFIED_BY);
        pais.setModifiedDate(DEFAULT_MODIFIED_DATE);
        return pais;
    }

    @Before
    public void initTest() {
        pais = createEntity(em);
    }

    @Test
    @Transactional
    public void createPais() throws Exception {
        int databaseSizeBeforeCreate = paisRepository.findAll().size();

        // Create the Pais

        restPaisMockMvc.perform(post("/api/pais")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(pais)))
                .andExpect(status().isCreated());

        // Validate the Pais in the database
        List<Pais> pais = paisRepository.findAll();
        assertThat(pais).hasSize(databaseSizeBeforeCreate + 1);
        Pais testPais = pais.get(pais.size() - 1);
        assertThat(testPais.getPaisId()).isEqualTo(DEFAULT_PAIS_ID);
        assertThat(testPais.getPaisNombre()).isEqualTo(DEFAULT_PAIS_NOMBRE);
        assertThat(testPais.getCreatedBy()).isEqualTo(DEFAULT_CREATED_BY);
        assertThat(testPais.getCreatedDate()).isEqualTo(DEFAULT_CREATED_DATE);
        assertThat(testPais.getModifiedBy()).isEqualTo(DEFAULT_MODIFIED_BY);
        assertThat(testPais.getModifiedDate()).isEqualTo(DEFAULT_MODIFIED_DATE);
    }

    @Test
    @Transactional
    public void checkPaisNombreIsRequired() throws Exception {
        int databaseSizeBeforeTest = paisRepository.findAll().size();
        // set the field null
        pais.setPaisNombre(null);

        // Create the Pais, which fails.

        restPaisMockMvc.perform(post("/api/pais")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(pais)))
                .andExpect(status().isBadRequest());

        List<Pais> pais = paisRepository.findAll();
        assertThat(pais).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllPais() throws Exception {
        // Initialize the database
        paisRepository.saveAndFlush(pais);

        // Get all the pais
        restPaisMockMvc.perform(get("/api/pais?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(pais.getId().intValue())))
                .andExpect(jsonPath("$.[*].paisId").value(hasItem(DEFAULT_PAIS_ID.intValue())))
                .andExpect(jsonPath("$.[*].paisNombre").value(hasItem(DEFAULT_PAIS_NOMBRE.toString())))
                .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY.toString())))
                .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE_STR)))
                .andExpect(jsonPath("$.[*].modifiedBy").value(hasItem(DEFAULT_MODIFIED_BY.toString())))
                .andExpect(jsonPath("$.[*].modifiedDate").value(hasItem(DEFAULT_MODIFIED_DATE_STR)));
    }

    @Test
    @Transactional
    public void getPais() throws Exception {
        // Initialize the database
        paisRepository.saveAndFlush(pais);

        // Get the pais
        restPaisMockMvc.perform(get("/api/pais/{id}", pais.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(pais.getId().intValue()))
            .andExpect(jsonPath("$.paisId").value(DEFAULT_PAIS_ID.intValue()))
            .andExpect(jsonPath("$.paisNombre").value(DEFAULT_PAIS_NOMBRE.toString()))
            .andExpect(jsonPath("$.createdBy").value(DEFAULT_CREATED_BY.toString()))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE_STR))
            .andExpect(jsonPath("$.modifiedBy").value(DEFAULT_MODIFIED_BY.toString()))
            .andExpect(jsonPath("$.modifiedDate").value(DEFAULT_MODIFIED_DATE_STR));
    }

    @Test
    @Transactional
    public void getNonExistingPais() throws Exception {
        // Get the pais
        restPaisMockMvc.perform(get("/api/pais/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePais() throws Exception {
        // Initialize the database
        paisRepository.saveAndFlush(pais);
        int databaseSizeBeforeUpdate = paisRepository.findAll().size();

        // Update the pais
        Pais updatedPais = paisRepository.findOne(pais.getId());
        updatedPais.setPaisId(UPDATED_PAIS_ID);
        updatedPais.setPaisNombre(UPDATED_PAIS_NOMBRE);
        updatedPais.setCreatedBy(UPDATED_CREATED_BY);
        updatedPais.setCreatedDate(UPDATED_CREATED_DATE);
        updatedPais.setModifiedBy(UPDATED_MODIFIED_BY);
        updatedPais.setModifiedDate(UPDATED_MODIFIED_DATE);

        restPaisMockMvc.perform(put("/api/pais")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedPais)))
                .andExpect(status().isOk());

        // Validate the Pais in the database
        List<Pais> pais = paisRepository.findAll();
        assertThat(pais).hasSize(databaseSizeBeforeUpdate);
        Pais testPais = pais.get(pais.size() - 1);
        assertThat(testPais.getPaisId()).isEqualTo(UPDATED_PAIS_ID);
        assertThat(testPais.getPaisNombre()).isEqualTo(UPDATED_PAIS_NOMBRE);
        assertThat(testPais.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testPais.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
        assertThat(testPais.getModifiedBy()).isEqualTo(UPDATED_MODIFIED_BY);
        assertThat(testPais.getModifiedDate()).isEqualTo(UPDATED_MODIFIED_DATE);
    }

    @Test
    @Transactional
    public void deletePais() throws Exception {
        // Initialize the database
        paisRepository.saveAndFlush(pais);
        int databaseSizeBeforeDelete = paisRepository.findAll().size();

        // Get the pais
        restPaisMockMvc.perform(delete("/api/pais/{id}", pais.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Pais> pais = paisRepository.findAll();
        assertThat(pais).hasSize(databaseSizeBeforeDelete - 1);
    }
}
