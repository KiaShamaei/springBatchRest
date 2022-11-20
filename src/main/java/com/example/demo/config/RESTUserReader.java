package com.example.demo.config;

import com.example.demo.domain.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;


public class RESTUserReader implements ItemReader<Users> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RESTUserReader.class);

    private final String apiUrl;
    private final RestTemplate restTemplate;

    private int nextUserIndex;
    private List<Users> userData;

    RESTUserReader(String apiUrl, RestTemplate restTemplate) {
        this.apiUrl = apiUrl;
        this.restTemplate = restTemplate;
        nextUserIndex = 0;
    }
    @Override
    public Users read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        LOGGER.info("Reading the information of the next student");

        if (userDataIsNotInitialized()) {
            userData = fetchDataFromAPI();
        }

        Users nextUser = null;
        if (nextUserIndex < userData.size()) {
            nextUser = userData.get(nextUserIndex);
            nextUserIndex++;
        }
        else {
            nextUserIndex = 0;
            userData = null;
        }
        LOGGER.info("Found student: {}", nextUser);
        return nextUser;
    }
    private boolean userDataIsNotInitialized() {
        return this.userData == null;
    }
    private List<Users> fetchDataFromAPI(){
        LOGGER.debug("Fetching student data from an external API by using the url: {}", apiUrl);

        ResponseEntity<Users[]> response = restTemplate.getForEntity(apiUrl, Users[].class);
        Users[] userData = response.getBody();
        LOGGER.debug("Found {} students", userData.length);

        return Arrays.asList(userData);
    }
}
