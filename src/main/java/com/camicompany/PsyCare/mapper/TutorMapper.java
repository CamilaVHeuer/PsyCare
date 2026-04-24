package com.camicompany.PsyCare.mapper;

import com.camicompany.PsyCare.dto.tutorDTO.TutorCreateRequest;
import com.camicompany.PsyCare.dto.tutorDTO.TutorResponse;

import com.camicompany.PsyCare.model.Tutor;
import org.springframework.stereotype.Component;

@Component
public class TutorMapper {

    public TutorResponse toResponse(Tutor tutor ){
        if ( tutor == null ) {
            return null;
        }

        return new TutorResponse(
                tutor.getId(),
                tutor.getFirstname(),
                tutor.getLastname(),
                tutor.getPhone(),
                formatCuil(tutor.getCuil()),
                tutor.getRelation()
        );
    }

    public Tutor toEntity(TutorCreateRequest tutorRequest){
        if ( tutorRequest == null ) {
            return null;
        }

        Tutor tutor = new Tutor();

        tutor.setFirstname(tutorRequest.firstname() );
        tutor.setLastname( tutorRequest.lastname() );
        tutor.setPhone( tutorRequest.phone() );
        tutor.setCuil( tutorRequest.cuil() );
        tutor.setRelation( tutorRequest.relation() );

        return tutor;
    }

    private String formatCuil(String cuil) {
        if (cuil == null || cuil.length() != 11) return cuil;

        return cuil.substring(0,2) + "-" +
                cuil.substring(2,10) + "-" +
                cuil.substring(10);
    }
}
