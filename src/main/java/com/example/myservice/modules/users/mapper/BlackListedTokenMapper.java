package com.example.myservice.modules.users.mapper;

import com.example.myservice.modules.users.entities.BlacklistedToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface BlackListedTokenMapper {
    @Mapping(target = "id", ignore = true)  // DB tự gen
    @Mapping(target = "token", source = "token")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "expiryDate", source = "expiryDate")
    BlacklistedToken toEntity(String token, Long userId, LocalDateTime expiryDate);
}
