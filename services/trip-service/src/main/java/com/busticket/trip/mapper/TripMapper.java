package com.xekhach.tripservice.mapper;

import com.xekhach.tripservice.dto.request.TripCreateRequest;
import com.xekhach.tripservice.dto.response.TripResponse;
import com.xekhach.tripservice.entity.Trip;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TripMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Trip toEntity(TripCreateRequest request);

    TripResponse toResponse(Trip trip);
}