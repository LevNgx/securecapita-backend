package com.fullstackprojectbackend.securecapita.service.implementation;

import com.fullstackprojectbackend.securecapita.domain.UserEvent;
import com.fullstackprojectbackend.securecapita.enumeration.EventType;
import com.fullstackprojectbackend.securecapita.repository.EventRepository;
import com.fullstackprojectbackend.securecapita.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    @Override
    public Collection<UserEvent> getEventsByUserId(Long userId) {
        return eventRepository.getEventsByUserId(userId);
    }

    @Override
    public void addUserEvent(String email, EventType eventType, String device, String ipAddress) {
        eventRepository.addUserEvent(email, eventType, device, ipAddress);
    }

    @Override
    public void addUserEvent(Long userId, EventType eventType, String device, String ipAddress) {
        eventRepository.addUserEvent(userId, eventType, device, ipAddress);
    }
}
