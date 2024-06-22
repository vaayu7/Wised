package com.wised.auth.service;

import com.wised.auth.dtos.ProfessionalDetailRequest;
import com.wised.auth.dtos.ProfessionalDetailResponse;
import com.wised.auth.dtos.ProfessionalDetailsDTO;
import com.wised.auth.model.ProfessionalDetails;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.ProfessionalDetailsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProfessionalDetailService {

    private final ProfessionalDetailsRepository professionalDetailsRepository;

    public ProfessionalDetailResponse createProfessionalDetail(UserProfile userProfile, ProfessionalDetailRequest request) {
        try {
            // Implement logic to create a new professional detail
            ProfessionalDetails professionalDetails = ProfessionalDetails.builder()
                    .userProfile(userProfile)
                    .companyName(request.getCompanyName())
                    .industry(request.getIndustry())
                    .isCurrentlyEmployed(request.isCurrentlyEmployed())
                    .designation(request.getDesignation())
                    .fromDate(request.getFromDate())
                    .toDate(request.getToDate())
                    .location(request.getLocation())
                    .build();

            ProfessionalDetails savedDetail = professionalDetailsRepository.save(professionalDetails);

            return ProfessionalDetailResponse.builder()
                    .success(true)
                    .message("Professional detail created successfully")
                    .data(List.of(mapToDTO(savedDetail)))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return ProfessionalDetailResponse.builder()
                    .success(false)
                    .error("Failed to create professional detail")
                    .build();
        }
    }

    public ProfessionalDetailResponse getProfessionalDetailById(UserProfile userProfile, int id) {
        try {
            // Implement logic to get professional detail by ID
            Optional<ProfessionalDetails> optionalDetail = professionalDetailsRepository.findById(id);
            if (optionalDetail.isPresent()) {
                ProfessionalDetails detail = optionalDetail.get();
                if (detail.getUserProfile().equals(userProfile)) {
                    return ProfessionalDetailResponse.builder()
                            .success(true)
                            .message("Professional detail retrieved successfully")
                            .data(List.of(mapToDTO(detail)))
                            .build();
                } else {
                    return ProfessionalDetailResponse.builder()
                            .success(false)
                            .error("User does not have access to this professional detail")
                            .build();
                }
            } else {
                return ProfessionalDetailResponse.builder()
                        .success(false)
                        .error("Professional detail not found for the given ID")
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ProfessionalDetailResponse.builder()
                    .success(false)
                    .error("Failed to retrieve professional detail")
                    .build();
        }
    }

    public ProfessionalDetailResponse updateProfessionalDetail(UserProfile userProfile, int id, ProfessionalDetailRequest request) {
        try {
            // Implement logic to update professional detail
            Optional<ProfessionalDetails> optionalDetail = professionalDetailsRepository.findById(id);
            if (optionalDetail.isPresent()) {
                ProfessionalDetails detailToUpdate = optionalDetail.get();
                if (!detailToUpdate.getUserProfile().equals(userProfile)) {
                    return ProfessionalDetailResponse.builder()
                            .success(false)
                            .error("User does not have permission to update this professional detail")
                            .build();
                }
                detailToUpdate.setCompanyName(request.getCompanyName());
                detailToUpdate.setIndustry(request.getIndustry());
                detailToUpdate.setCurrentlyEmployed(request.isCurrentlyEmployed());
                detailToUpdate.setDesignation(request.getDesignation());
                detailToUpdate.setFromDate(request.getFromDate());
                detailToUpdate.setToDate(request.getToDate());
                detailToUpdate.setLocation(request.getLocation());
                ProfessionalDetails updatedDetail = professionalDetailsRepository.save(detailToUpdate);
                return ProfessionalDetailResponse.builder()
                        .success(true)
                        .message("Professional detail updated successfully")
                        .data(List.of(mapToDTO(updatedDetail)))
                        .build();
            } else {
                return ProfessionalDetailResponse.builder()
                        .success(false)
                        .error("Professional detail not found for the given ID")
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ProfessionalDetailResponse.builder()
                    .success(false)
                    .error("Failed to update professional detail")
                    .build();
        }
    }

    public ProfessionalDetailResponse deleteProfessionalDetail(UserProfile userProfile, int id) {
        try {
            // Implement logic to delete professional detail
            Optional<ProfessionalDetails> optionalDetail = professionalDetailsRepository.findById(id);
            if (optionalDetail.isPresent()) {
                ProfessionalDetails detail = optionalDetail.get();
                if (detail.getUserProfile().equals(userProfile)) {
                    professionalDetailsRepository.delete(detail);
                    return ProfessionalDetailResponse.builder()
                            .success(true)
                            .message("Professional detail deleted successfully")
                            .build();
                } else {
                    return ProfessionalDetailResponse.builder()
                            .success(false)
                            .error("User does not have access to delete this professional detail")
                            .build();
                }
            } else {
                return ProfessionalDetailResponse.builder()
                        .success(false)
                        .error("Professional detail not found for the given ID")
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ProfessionalDetailResponse.builder()
                    .success(false)
                    .error("Failed to delete professional detail")
                    .build();
        }
    }

    // Helper method to convert ProfessionalDetail entity to DTO
    private ProfessionalDetailsDTO mapToDTO(ProfessionalDetails professionalDetail) {
        return ProfessionalDetailsDTO.builder()
                .id(professionalDetail.getId())
                .companyName(professionalDetail.getCompanyName())
                .industry(professionalDetail.getIndustry())
                .isCurrentlyEmployed(professionalDetail.isCurrentlyEmployed())
                .designation(professionalDetail.getDesignation())
                .fromDate(professionalDetail.getFromDate())
                .toDate(professionalDetail.getToDate())
                .location(professionalDetail.getLocation())
                .build();
    }
}
