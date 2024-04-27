package com.fullstackprojectbackend.securecapita.listener;

import com.fullstackprojectbackend.securecapita.event.NewUserEvent;
import com.fullstackprojectbackend.securecapita.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static com.fullstackprojectbackend.securecapita.utils.RequestUtils.getDevice;
import static com.fullstackprojectbackend.securecapita.utils.RequestUtils.getIpAddress;

@Component
@RequiredArgsConstructor
public class NewEventListener {

    private final EventService eventService;
    private final HttpServletRequest request;

    @EventListener
    private void onNewUserEvent(NewUserEvent event){
        eventService.addUserEvent(event.getEmail(), event.getType(), getDevice(request), getIpAddress(request));
    }


}
