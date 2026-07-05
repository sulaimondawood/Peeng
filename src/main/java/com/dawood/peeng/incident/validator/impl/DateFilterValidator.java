package com.dawood.peeng.incident.validator.impl;

import com.dawood.peeng.incident.dto.request.IncidentFilterRequest;
import com.dawood.peeng.incident.validator.constraints.ValidDateFilter;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateFilterValidator implements ConstraintValidator<ValidDateFilter, IncidentFilterRequest> {
    @Override
    public boolean isValid(IncidentFilterRequest value, ConstraintValidatorContext context) {

        if (value == null) return true;

        int selectedStrategy = 0;

        boolean hasDate = value.date() != null;
        boolean hasDateRange = value.endDate() != null || value.startDate() != null;
        boolean hasDateBucket = value.dateBucket() != null;

       if(hasDate) selectedStrategy++;

       if(hasDateBucket) selectedStrategy++;

        if(hasDateRange){
            if(value.startDate() ==null || value.endDate()==null){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Both startDate and endDate must be provided.")
                        .addConstraintViolation();
                return false;
            }

            if(value.startDate().isAfter(value.endDate())){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Start Date cannot be after End Date.")
                        .addConstraintViolation();
                return false;
            }

            selectedStrategy++;
        }

        if (selectedStrategy > 1) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Provide only one of dateBucket, date, or startDate/endDate.")
                    .addConstraintViolation();

            return false;
        }

        return true;

    }
}
