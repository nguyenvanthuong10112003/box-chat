package com.box_chat.notification.mapper;

import com.box_chat.notification.dto.request.EmailRequest;
import com.box_chat.notification.dto.request.SendEmailRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmailMapper {
    @Mapping(target = "to", expression = "java(java.util.Collections.singletonList(emailRequest.getReceiver()))")
    SendEmailRequest toSendEmailRequest(EmailRequest emailRequest);
}
