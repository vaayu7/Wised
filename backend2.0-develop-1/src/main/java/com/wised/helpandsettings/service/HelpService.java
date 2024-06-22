package com.wised.helpandsettings.service;

import com.wised.auth.model.UserProfile;
import com.wised.auth.service.S3FileService;
import com.wised.helpandsettings.dtos.HelpRequest;
import com.wised.helpandsettings.dtos.HelpResponse;
import com.wised.helpandsettings.dtos.HelpResponseDTO;
import com.wised.helpandsettings.enums.HelpStatus;
import com.wised.helpandsettings.exception.HelpNotFoundException;
import com.wised.helpandsettings.model.Help;
import com.wised.helpandsettings.repository.HelpRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class HelpService {

    private final HelpRepository helpRepository;
    private final S3FileService s3FileService;

    public HelpResponse createHelpRequest(UserProfile userProfile, HelpRequest helpRequest) {
        List<String> awsUrls = new ArrayList<>();
        System.out.println("entered calling help request");
        // Upload each file to S3 and collect the AWS URLs
        for (MultipartFile file : helpRequest.getFiles()) {
            try {
                String key = generateS3Key(file.getOriginalFilename()); // Generate a unique key for the file
                s3FileService.uploadFile(key, file);
                String awsUrl = s3FileService.generatePresignedUrl(key, 3600); // Presigned URL with 1-hour expiration
                awsUrls.add(awsUrl);
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception accordingly
            }
        }

        // Create a new Help object with AWS URLs and other details
        Help help = Help.builder()
                .name(helpRequest.getName())
                .email(helpRequest.getEmail())
                .subject(helpRequest.getSubject())
                .issue(helpRequest.getIssue())
                .description(helpRequest.getDescription())
                .awsUrl(awsUrls)
                .helpStatus(HelpStatus.UNDER_REVIEW)
                .userProfile(userProfile)// Assuming initial status is 'under review'
                .build();
        System.out.println("calling help request built");

        // Save the help request to the database
        Help savedHelp = helpRepository.save(help);
        List<HelpResponseDTO> helpResponseDTOList =new ArrayList<>();

        HelpResponseDTO helpResponseDTO = mapToDTO(savedHelp);

        helpResponseDTOList.add(helpResponseDTO);
        System.out.println("returning the response");
        return HelpResponse.builder()
                .success(true)
                .message("Help requests retrieved successfully")
                .data(helpResponseDTOList)
                .build();
    }

    public boolean updateHelpStatus(Integer helpId, HelpStatus helpStatus){

        Optional<Help> optionalHelp = helpRepository.findById(helpId);
        if(optionalHelp.isPresent()){
            Help help = optionalHelp.get();
            if(help.getHelpStatus() != HelpStatus.RESOLVED){
                help.setHelpStatus(helpStatus.RESOLVED);
                helpRepository.save(help);
                return true;
            }
        }
        else{
            try {
                throw new HelpNotFoundException("no help found");
            } catch (HelpNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }



    private String generateS3Key(String originalFilename) {
        // Implement your logic to generate a unique key (e.g., using UUID)
        return "prefix/" + originalFilename; // Example: "prefix/filename.jpg"
    }


    public List<Help> getAllHelpRequests() {
        return helpRepository.findAll();
    }

    // Method to retrieve help request by ID
    public Optional<Help> getHelpRequestById(Integer id) {
        return helpRepository.findById(id);
    }

    // Method to retrieve help requests by status
    public List<Help> getHelpRequestsByStatus(HelpStatus status) {
        return helpRepository.findByHelpStatus(status);
    }

    // Method to retrieve help requests by user
    public HelpResponse getHelpRequestsByUser(UserProfile userProfile) {
        List<Help> helpRequests = helpRepository.findByUserProfile(userProfile);

        List<HelpResponseDTO> helpResponseDTOs = helpRequests.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return HelpResponse.builder()
                .success(true)
                .message("Help requests retrieved successfully")
                .data(helpResponseDTOs)
                .build();
    }

    // Method to map Help entity to HelpResponseDTO
    private HelpResponseDTO mapToDTO(Help help) {
        return HelpResponseDTO.builder()
                .id(help.getId())
                .name(help.getName())
                .email(help.getEmail())
                .subject(help.getSubject())
                .issue(help.getIssue())
                .description(help.getDescription())
                .awsUrl(help.getAwsUrl())
                .helpStatus(help.getHelpStatus())
                .build();
    }

    // Method to retrieve help requests by user email
    public List<Help> getHelpRequestsByUserEmail(String email) {
        return helpRepository.findByEmail(email);
    }

    // Method to retrieve help requests by subject
    public List<Help> getHelpRequestsBySubject(String subject) {
        return helpRepository.findBySubject(subject);
    }

    // Method to retrieve help requests by issue
    public List<Help> getHelpRequestsByIssue(String issue) {
        return helpRepository.findByIssue(issue);
    }

    // Method to retrieve help requests by description
    public List<Help> getHelpRequestsByDescription(String description) {
        return helpRepository.findByDescription(description);
    }




}
