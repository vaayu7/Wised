package com.wised.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wised.auth.dtos.*;
import com.wised.auth.dtos.EducationRequest;
import com.wised.auth.exception.EducationAdditionException;
import com.wised.auth.mappers.EducationMapper;
import com.wised.auth.model.*;
import com.wised.auth.repository.EducationRepository;
import com.wised.auth.repository.UserProfileRepository;
import com.wised.auth.service.EducationService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/user/profile/education")
@AllArgsConstructor
public class EducationController {

    private final EducationService educationService;

    private final UserProfileRepository userProfileRepository;

    private final EducationRepository educationRepository;

    @GetMapping("")
    public ResponseEntity<String> getEducationList() {
        try {
            ClassPathResource resource = new ClassPathResource("education.json");
            String jsonContent = new String(resource.getInputStream().readAllBytes());
            return ResponseEntity.ok(jsonContent);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/add-education")
    public ResponseEntity<EducationResponse> addEducation(@RequestBody EducationRequest educationRequest) {
        try {

            // Fetch current user profile
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();

            // Retrieve the user's profile from the repository
            Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if (!userProfileOptional.isPresent()) {
                return ResponseEntity.badRequest().body(EducationResponse.builder()
                        .error("Invalid user")
                        .build());
            }

            UserProfile userProfile = userProfileOptional.get();
            Education education  = educationService.addEducation(userProfile, educationRequest);
            College collegeData = educationService.getCollege(education);
            School schoolData = educationService.getSchool(education);

            List<EducationDTO> modifiedEducationList = new ArrayList<>();
            EducationDTO educationDTO = EducationMapper.mapEducationToEducationDTO(education, collegeData, schoolData);
            modifiedEducationList.add(educationDTO);

            return ResponseEntity.ok().body(EducationResponse.builder()
                    .message("your changes have been successfully saved")
                    .data(modifiedEducationList)
                    .build());

        } catch (EducationAdditionException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(EducationResponse.builder()
                    .message("please check the details and try again.")
                    .error("failed to add educational details!")
                    .build());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/get-education")
    public ResponseEntity<EducationResponse> getEducation() {
        try {
            // Fetch current user profile
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();

            // Retrieve the user's profile from the repository
            Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if (userProfileOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(EducationResponse.builder()
                        .error("Invalid user")
                        .build());
            }

            UserProfile userProfile = userProfileOptional.get();
            List<EducationDTO> educationData = educationService.getEducation(userProfile);

            return ResponseEntity.ok(EducationResponse.builder()
                    .success(true)
                    .message("education details fetched successfully")
                    .data(educationData)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/edit-education")
    public ResponseEntity<EducationResponse> editEducation(@RequestBody EducationRequest educationRequest) {
        try {

            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();

            Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if (userProfileOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(EducationResponse.builder()
                        .error("Invalid user")
                        .build());
            }

            UserProfile userProfile = userProfileOptional.get();

            Optional<Education> educationOptional = educationRepository.findById(educationRequest.getId());
            if (educationOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(EducationResponse.builder()
                        .error("Education with ID " + educationRequest.getId() + " not found")
                        .build());
            }

            Education education = educationOptional.get();
            if (!education.getUser().equals(userProfile)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(EducationResponse.builder()
                        .error("Access denied. You do not have permission to edit this education")
                        .build());
            }

            Education modifiedEducation = educationService.addEducation(userProfile, educationRequest);
            College collegeData = educationService.getCollege(modifiedEducation);
            School schoolData = educationService.getSchool(modifiedEducation);

            List<EducationDTO> modifiedEducationList = new ArrayList<>();
            EducationDTO educationDTO = EducationMapper.mapEducationToEducationDTO(modifiedEducation, collegeData, schoolData);
            modifiedEducationList.add(educationDTO);

            return ResponseEntity.ok().body(EducationResponse.builder()
                    .message("your changes have been successfully saved")
                    .data(modifiedEducationList)
                    .build());
        } catch (EducationAdditionException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(EducationResponse.builder()
                    .message("please check the details and try again.")
                    .error("failed to modify the educational details!")
                    .build());
        }catch (Exception e) {
            // Return internal server error response in case of any exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @DeleteMapping("/remove-education")
    public ResponseEntity<EducationResponse> removeEducation(@RequestParam("id") Integer educationId) {
        try {
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();

            // Retrieve the user's profile from the repository
            Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if (userProfileOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(EducationResponse.builder()
                        .error("Invalid user")
                        .build());
            }

            UserProfile userProfile = userProfileOptional.get();
            Optional<Education> educationOptional = educationRepository.findById(educationId);
            if (educationOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(EducationResponse.builder()
                        .error("Education with ID " + educationId + " not found")
                        .build());
            }

            Education education = educationOptional.get();
            if (!education.getUser().equals(userProfile)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(EducationResponse.builder()
                        .error("Access denied. You do not have permission to edit this education")
                        .build());
            }
            boolean educationRemoved = educationService.removeEducation(educationId);

            if (educationRemoved) {
                return ResponseEntity.ok(EducationResponse.builder()
                        .message("Education deleted successfully!")
                        .build());
            } else {
                return ResponseEntity.ok(EducationResponse.builder()
                        .error("Failed to delete education. Please check the details and try again.")
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search/{keyword}")
    public ResponseEntity<SearchResponse> search(@PathVariable String keyword) {
        try {
            // Load data from the JSON file
            Map<String, List<String>> searchData = loadDataFromJson();

            // Filter data based on the keyword
            Map<String, List<String>> filteredData = searchData.entrySet().stream()
                    .filter(entry -> {
                        String key = entry.getKey().toLowerCase();
                        List<String> values = entry.getValue();
                        return key.contains(keyword.toLowerCase()) || values.stream().anyMatch(value -> value.toLowerCase().contains(keyword.toLowerCase()));
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            if (filteredData.isEmpty()) {
                return ResponseEntity.ok().body(SearchResponse.builder()
                        .message("No matches found for '" + keyword + "'")
                        .data(Collections.emptyMap())
                        .build());
            } else {
                return ResponseEntity.ok().body(SearchResponse.builder()
                        .message("Data filtered successfully")
                        .data(filteredData)
                        .build());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(SearchResponse.builder()
                    .error("Failed to load data from file")
                    .build());
        }
    }


    private Map<String, List<String>> loadDataFromJson() throws IOException {
        ClassPathResource resource = new ClassPathResource("education.json");
        String jsonContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonContent);

        Map<String, List<String>> dataMap = new LinkedHashMap<>();

        // Process universities
        if (rootNode.has("universities")) {
            JsonNode universitiesNode = rootNode.get("universities");
            dataMap.putAll(parseData(universitiesNode));
        }

        // Process boards
        if (rootNode.has("boards")) {
            JsonNode boardsNode = rootNode.get("boards");
            dataMap.putAll(parseData(boardsNode));
        }

        return dataMap;
    }

    private Map<String, List<String>> parseData(JsonNode parentNode) {
        Map<String, List<String>> dataMap = new LinkedHashMap<>();

        for (JsonNode node : parentNode) {
            String name = node.has("name") ? node.get("name").asText() : "";
            String city = node.has("city") ? node.get("city").asText() : "";
            String parentKey = name + " - " + city;

            List<String> childNodes = new ArrayList<>();

            if (node.has("institutes")) {
                JsonNode institutesNode = node.get("institutes");
                childNodes.addAll(parseChildData(institutesNode));
            } else if (node.has("schools")) {
                JsonNode schoolsNode = node.get("schools");
                childNodes.addAll(parseChildData(schoolsNode));
            }

            dataMap.put(parentKey, childNodes);
        }

        return dataMap;
    }

    private List<String> parseChildData(JsonNode childNode) {
        List<String> childData = new ArrayList<>();

        for (JsonNode node : childNode) {
            String name = node.has("name") ? node.get("name").asText() : "";
            childData.add("  - " + name);

            JsonNode specializationsNode = node.has("specializations") ? node.get("specializations") : null;
            if (specializationsNode != null) {
                for (JsonNode specializationNode : specializationsNode) {
                    childData.add("    * " + specializationNode.asText());
                }
            }
        }

        return childData;
    }
}