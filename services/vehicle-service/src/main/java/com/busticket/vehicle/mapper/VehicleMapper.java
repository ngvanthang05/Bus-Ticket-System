package com.xekhach.vehicleservice.mapper;

import com.xekhach.vehicleservice.dto.VehicleRequest;
import com.xekhach.vehicleservice.dto.VehicleResponse;
import com.xekhach.vehicleservice.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.BeanMapping;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    Vehicle toEntity(VehicleRequest request);

    VehicleResponse toResponse(Vehicle vehicle);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(VehicleRequest request, @MappingTarget Vehicle vehicle);
}