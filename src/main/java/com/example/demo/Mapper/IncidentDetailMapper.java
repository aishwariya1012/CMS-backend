package com.example.demo.Mapper;

import com.example.demo.dto.IncidentRequest;
import com.example.demo.Model.Incident;
import com.example.demo.Model.IncidentDetail;
import org.springframework.stereotype.Component;


@Component
public class IncidentDetailMapper {

   
    public IncidentDetail toEntity(IncidentRequest req, Incident savedIncident) {
        return IncidentDetail.builder()
                .incident(savedIncident)
                .propertyDescription(req.getPropertyDescription())
                .valueEstimate(req.getValueEstimate())
                .location(req.getLocation())
                .lostLocation(req.getLostLocation())
                .lostDate(req.getLostDate())
                .damagedPropertyType(req.getDamagedPropertyType())
                .damageEstimate(req.getDamageEstimate())
                .imagePath(req.getImagePath())
                .build();
    }

    
    public IncidentDetail updateEntity(IncidentDetail existing, IncidentRequest req) {
        if (req.getPropertyDescription() != null)
            existing.setPropertyDescription(req.getPropertyDescription());
        if (req.getValueEstimate() != null)
            existing.setValueEstimate(req.getValueEstimate());
        if (req.getLocation() != null)
            existing.setLocation(req.getLocation());
        if (req.getLostLocation() != null)
            existing.setLostLocation(req.getLostLocation());
        if (req.getLostDate() != null)
            existing.setLostDate(req.getLostDate());
        if (req.getDamagedPropertyType() != null)
            existing.setDamagedPropertyType(req.getDamagedPropertyType());
        if (req.getDamageEstimate() != null)
            existing.setDamageEstimate(req.getDamageEstimate());
        return existing;
    }
}
