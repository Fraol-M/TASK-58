package com.campusfit.auth.mapper;

import com.campusfit.auth.dto.UserDto;
import com.campusfit.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "status", expression = "java(user.getStatus().name())")
    UserDto toUserDto(User user);

    default UserDto toUserDto(User user, List<String> roles) {
        UserDto dto = toUserDto(user);
        dto.setRoles(roles);
        return dto;
    }
}
