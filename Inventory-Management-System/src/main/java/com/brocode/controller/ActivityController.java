package com.brocode.controller;

import com.brocode.entity.ActivityLog;
import com.brocode.service.ActivityLogService;
import com.brocode.service.dto.ActivityCreateDto;
import com.brocode.utils.Activity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/pms/api/activity")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityLogService service;

    @GetMapping
    public ResponseEntity<?> getLogs(
            @RequestParam(required = false) Activity activity,
            @RequestParam(defaultValue = "") String createdBy,
            @RequestParam(required = false) LocalDate dateCreated,
            @RequestParam(required = false) Long id,
            Authentication authentication
    ){
        String currentUsername = authentication.getName();

        if (id != null){
            Optional<ActivityLog> log = service.getLog(id);
            // Filter out logs created by brocode unless current user is brocode
            if (log.isPresent() && "brocode".equals(log.get().getCreatedBy()) && !"brocode".equals(currentUsername)) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.of(log);
        }

        List<ActivityLog> logs = service.getLogs().reversed();

        if (!"brocode".equals(currentUsername)) {
            logs = logs.stream()
                    .filter(log -> !"brocode".equals(log.getCreatedBy()))
                    .toList();
        }

        if (activity != null){
            logs = logs.stream()
                    .filter(log -> log.getActivity().equals(activity))
                    .toList();
        }
        if (!createdBy.isEmpty()){
            logs = logs.stream()
                    .filter(log -> log.getCreatedBy().equalsIgnoreCase(createdBy))
                    .toList();
        }
        if (dateCreated != null){
            logs = logs.stream()
                    .filter(log -> log.getCreatedAt().toLocalDate().isEqual(dateCreated))
                    .toList();
        }

        return ResponseEntity.ok(logs);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> deleteActivity(
            @RequestParam Long id
    ){
        if (service.deleteLog(id)) return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> editActivity(
            @RequestParam Long id,
            @RequestBody ActivityCreateDto dto
    ){
        if (service.editLog(dto,id)) return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
    }

    @DeleteMapping("/all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll(){
        service.deleteAll();
    }

}
