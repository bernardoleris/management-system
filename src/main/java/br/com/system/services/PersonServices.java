package br.com.system.services;

import br.com.system.data.dto.PersonDTO;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.mapper.ObjectMapper;
import br.com.system.model.Person;
import br.com.system.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

@Service
public class PersonServices {
    private final AtomicLong counter = new AtomicLong();
    private final Logger logger = Logger.getLogger(PersonServices.class.getName());

    @Autowired
    PersonRepository repository;

    public List<PersonDTO> findAll() {
        logger.info("Finding people");

        return ObjectMapper.parseListObjects(repository.findAll(), PersonDTO.class) ;
    }

    public PersonDTO findById(Long id) {
        Person entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        return ObjectMapper.parseObject(entity, PersonDTO.class);
    }

    public PersonDTO createPerson(PersonDTO person) {
        logger.info("Creating one person!");

        Person entity = ObjectMapper.parseObject(person, Person.class);
        Person savedEntity = repository.save(entity);

        return ObjectMapper.parseObject(savedEntity, PersonDTO.class);
    }

    public void deletePerson(Long id) {
        logger.info("Deleting one person!");

        Person entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        repository.delete(entity);
    }

    public PersonDTO updatePerson(PersonDTO person) {
        logger.info("Updating one person!");

        Person entity = repository.findById(person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        entity.setFirstName(person.getFirstName());
        entity.setLastName(person.getLastName());
        entity.setAddress(person.getAddress());
        entity.setGender(person.getGender());

        return ObjectMapper.parseObject(repository.save(entity), PersonDTO.class);
    }
}
