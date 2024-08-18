package com.box_chat.identity_service.mapper;

import com.box_chat.identity_service.dto.request.AccountCreationRequest;
import com.box_chat.identity_service.entity.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    Account toAccount(AccountCreationRequest accountCreationRequest);
}
