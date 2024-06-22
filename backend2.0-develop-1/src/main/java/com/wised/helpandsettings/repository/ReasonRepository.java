package com.wised.helpandsettings.repository;

import com.wised.helpandsettings.enums.ReasonEnum;
import com.wised.helpandsettings.model.Reason;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ReasonRepository extends JpaRepository<Reason, Integer> {


}
