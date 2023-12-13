package org.sunbird.halloffame.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mahesh.vakkund
 */
@Service
public class HallOfFameServiceImpl implements HallOfFameService {

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public Map<String, Object> fetchHallOfFameData() {
        Map<String, Object> resultMap = new HashMap<>();
        String jsonString = "{\n" +
                "    \"Title\": \"october 2023\",\n" +
                "    \"mdoList\": [\n" +
                "        {\n" +
                "            \"rank\": \"1\",\n" +
                "            \"karmaPoints\": \"100\",\n" +
                "            \"deptName\": \"Finance\",\n" +
                "            \"deptId\": \"1234\",\n" +
                "            \"deptLogo\": \"Finance Logo\",\n" +
                "            \"progress\": \"5\",\n" +
                "            \"negtiveOrPositive\": \"positive\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"rank\": \"2\",\n" +
                "            \"karmaPoints\": \"90\",\n" +
                "            \"deptName\": \"Finance\",\n" +
                "            \"deptId\": \"1234\",\n" +
                "            \"deptLogo\": \"Finance Logo\",\n" +
                "            \"progress\": \"4\",\n" +
                "            \"negtiveOrPositive\": \"positive\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"rank\": \"3\",\n" +
                "            \"karmaPoints\": \"80\",\n" +
                "            \"deptName\": \"Finance\",\n" +
                "            \"deptId\": \"1234\",\n" +
                "            \"deptLogo\": \"Finance Logo\",\n" +
                "            \"progress\": \"3\",\n" +
                "            \"negtiveOrPositive\": \"positive\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"rank\": \"4\",\n" +
                "            \"karmaPoints\": \"70\",\n" +
                "            \"deptName\": \"Railways\",\n" +
                "            \"deptId\": \"1235\",\n" +
                "            \"deptLogo\": \"Railways Logo\",\n" +
                "            \"progress\": \"2\",\n" +
                "            \"negtiveOrPositive\": \"negative\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"rank\": \"5\",\n" +
                "            \"karmaPoints\": \"60\",\n" +
                "            \"deptName\": \"Railways\",\n" +
                "            \"deptId\": \"1235\",\n" +
                "            \"deptLogo\": \"Railways Logo\",\n" +
                "            \"progress\": \"1\",\n" +
                "            \"negtiveOrPositive\": \"negative\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"rank\": \"6\",\n" +
                "            \"karmaPoints\": \"50\",\n" +
                "            \"deptName\": \"Railways\",\n" +
                "            \"deptId\": \"1235\",\n" +
                "            \"deptLogo\": \"Railways Logo\",\n" +
                "            \"progress\": \"5\",\n" +
                "            \"negtiveOrPositive\": \"positive\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"rank\": \"7\",\n" +
                "            \"karmaPoints\": \"40\",\n" +
                "            \"deptName\": \"Telecom\",\n" +
                "            \"deptId\": \"1236\",\n" +
                "            \"deptLogo\": \"Telecom Logo\",\n" +
                "            \"progress\": \"4\",\n" +
                "            \"negtiveOrPositive\": \"positive\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"rank\": \"8\",\n" +
                "            \"karmaPoints\": \"30\",\n" +
                "            \"deptName\": \"Telecom\",\n" +
                "            \"deptId\": \"1236\",\n" +
                "            \"deptLogo\": \"Telecom Logo\",\n" +
                "            \"progress\": \"3\",\n" +
                "            \"negtiveOrPositive\": \"positive\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"rank\": \"9\",\n" +
                "            \"karmaPoints\": \"20\",\n" +
                "            \"deptName\": \"Telecom\",\n" +
                "            \"deptId\": \"1236\",\n" +
                "            \"deptLogo\": \"Telecom Logo\",\n" +
                "            \"progress\": \"2\",\n" +
                "            \"negtiveOrPositive\": \"negative\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"rank\": \"10\",\n" +
                "            \"karmaPoints\": \"10\",\n" +
                "            \"deptName\": \"Postal\",\n" +
                "            \"deptId\": \"1237\",\n" +
                "            \"deptLogo\": \"Finance Logo\",\n" +
                "            \"progress\": \"1\",\n" +
                "            \"negtiveOrPositive\": \"negative\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, Object.class);
            resultMap = objectMapper.readValue(jsonString, mapType);
        } catch (IOException e) {
            logger.info("Error occured while fetching the data for Hall of fame: " + e.getMessage());
        }
        return resultMap;
    }
}
