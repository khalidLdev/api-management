package com.humanup.matrix.ui.apimanagement.graphql.query.impl;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.humanup.matrix.ui.apimanagement.dto.PersonDTO;
import com.humanup.matrix.ui.apimanagement.dto.ProfileDTO;
import com.humanup.matrix.ui.apimanagement.graphql.builder.ObjectBuilder;
import com.humanup.matrix.ui.apimanagement.graphql.query.IQueryPerson;
import com.humanup.matrix.ui.apimanagement.proxy.PersonProxy;
import com.humanup.matrix.ui.apimanagement.proxy.ProfileProxy;
import com.humanup.matrix.ui.apimanagement.vo.PersonVO;
import graphql.schema.DataFetchingEnvironment;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PersonQuery implements GraphQLQueryResolver, IQueryPerson {
  private static final Logger LOGGER = LoggerFactory.getLogger(PersonQuery.class);
  @Autowired PersonProxy personProxy;

  @Autowired ProfileProxy profileProxy;



  @Override
  public List<PersonVO> getListPerson(DataFetchingEnvironment env) {
    String token = ObjectBuilder.getTokenFromGraphQL(env);
    List<PersonDTO> personListDTO = null;
    try {
      personListDTO =
          ObjectBuilder.mapper.readValue(
              personProxy.findAllPerson(token), new TypeReference<List<PersonDTO>>() {});

    } catch (JsonProcessingException e) {
      LOGGER.error("Exception Parsing List<PersonVO> ", e);
    }
    return personListDTO.stream()
        .map(
            p -> {
              ProfileDTO profile = null;
              try {
                profile =
                    ObjectBuilder.mapper.readValue(
                        profileProxy.findProfileByTitle(p.getProfile()), ProfileDTO.class);
              } catch (JsonProcessingException e) {
                LOGGER.error("Exception Parsing Profile {}", p.getProfile(), e);
              }
              return PersonVO.builder()
                  .birthDate(p.getBirthDate())
                  .firstName(p.getFirstName())
                  .lastName(p.getLastName())
                  .mailAdresses(p.getMailAdresses())
                  .profile(ObjectBuilder.buildProfile(profile))
                  .skillVOList(ObjectBuilder.buildCollectionSkills(p))
                  .build();
            })
        .collect(Collectors.toList());
  }



  @Override
  public PersonVO getPersonByEmail(@NotNull final String email, DataFetchingEnvironment env ) {
    String token = ObjectBuilder.getTokenFromGraphQL(env);

    PersonDTO personDTO = null;
    ProfileDTO profileDTO = null;
    try {
      personDTO =
          ObjectBuilder.mapper.readValue(
                      personProxy.findPersonByEmail(email,token), PersonDTO.class);
      profileDTO =
              ObjectBuilder.mapper.readValue(
                      profileProxy.findProfileByTitle(personDTO.getProfile()), ProfileDTO.class);
    } catch (JsonProcessingException e) {
      LOGGER.error("Exception Parsing  Person {}", email, e);
    }
    return PersonVO.builder()
        .birthDate(personDTO.getBirthDate())
        .firstName(personDTO.getFirstName())
        .lastName(personDTO.getLastName())
        .mailAdresses(personDTO.getMailAdresses())
        .profile(ObjectBuilder.buildProfile(profileDTO))
        .skillVOList(ObjectBuilder.buildCollectionSkills(personDTO))
        .build();
  }

}
