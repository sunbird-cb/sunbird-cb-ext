package org.sunbird.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.sunbird.core.logger.CbExtLogger;

import java.util.Map;

@Service
public class OutboundReqService {

    @Autowired
    private RestTemplate restTemplate;

    private CbExtLogger logger = new CbExtLogger(getClass().getName());

    public Map fetchResultUsingPost(String uri, Object request) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Map response = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(request, headers);
            StringBuilder str = new StringBuilder(this.getClass().getCanonicalName())
                    .append(".fetchResult").append(System.lineSeparator());
            str.append("URI: ").append(uri).append(System.lineSeparator());
            str.append("Request: ").append(mapper.writeValueAsString(request))
                    .append(System.lineSeparator());
            logger.info(str.toString());
            response = restTemplate.postForObject(uri, entity, Map.class);
            str.append("Response: ").append(mapper.writeValueAsString(response))
                    .append(System.lineSeparator());
            logger.debug(str.toString());
        } catch (HttpClientErrorException e) {
            logger.error(e);
        } catch (Exception e) {
            logger.error(e);
        }
        return response;
    }
}
