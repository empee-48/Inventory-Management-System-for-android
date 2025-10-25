package com.brocode.service;

import com.brocode.entity.ActivityLog;
import com.brocode.repo.ActivityLogRepo;
import com.brocode.service.dto.ActivityCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActivityLogService {
    private final ActivityLogRepo repo;

    public List<ActivityLog> getLogs(){
        return repo.findAll();
    }

    public Optional<ActivityLog> getLog(Long id){
        return repo.findById(id);
    }

    public boolean deleteLog(Long id){
        Optional<ActivityLog> log = repo.findById(id);

        log.ifPresent(repo::delete);

        return log.isPresent();
    }

    public void deleteAll(){
        repo.deleteAll();
    }

    public boolean editLog(ActivityCreateDto dto, Long id){
        Optional<ActivityLog> logOptional = repo.findById(id);

        if (logOptional.isEmpty()) return false;

        ActivityLog log = logOptional.get();
        log.setActivity(dto.activity());
        log.setCreatedAt(dto.timeStamp());
        log.setDescription(dto.description());
        log.setCreatedBy(dto.username());

        repo.save(log);
        return true;
    }

    public void changeUsernames(){
        repo.findAll().forEach(activityLog -> {
            activityLog.setCreatedBy("monicare@admin");
            repo.save(activityLog);
        });
    }
}
