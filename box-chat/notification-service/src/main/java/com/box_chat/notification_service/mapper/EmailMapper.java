package com.box_chat.notification_service.mapper;

import com.box_chat.event.dto.NotificationEvent;
import com.box_chat.notification_service.dto.request.EmailRequest;
import com.box_chat.notification_service.dto.request.SendEmailRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmailMapper {
    @Mapping(target = "to", expression = "java(java.util.Collections.singletonList(" +
        "new com.box_chat.notification_service.dto.request.UserRequest(" +
            "notificationEvent.getReceiverName(), " +
            "notificationEvent.getReceiverEmail()))" +
        ")")
    SendEmailRequest toSendEmailRequest(NotificationEvent notificationEvent);
}
