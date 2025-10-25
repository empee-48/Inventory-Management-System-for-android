package com.brocode.service;

import com.brocode.entity.ActivityLog;
import com.brocode.entity.Category;
import com.brocode.repo.ActivityLogRepo;
import com.brocode.repo.CategoryRepo;
import com.brocode.service.dto.CategoryCreateDto;
import com.brocode.service.dto.CategoryResponseDto;
import com.brocode.utils.Activity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final MyCategoryMapper mapper;
    private final CategoryRepo repo;
    private final ActivityLogRepo logRepo;

    public Category getCategoryOrThrowError(Long id){
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Category Not Found"));
    }

    public List<CategoryResponseDto> getAll(){
        return repo.findAll().stream().map(mapper::categoryToResponse).toList();
    }

    public CategoryResponseDto getCategory(Long id){
        return repo.findById(id).map(mapper::categoryToResponse).orElseThrow(() -> new NoSuchElementException("Category Not Found"));
    }

    @Transactional
    public CategoryResponseDto createCategory(CategoryCreateDto dto){
        Category category = repo.save(mapper.createToCategory(dto));
        createLog(category, Activity.CREATE);
        return mapper.categoryToResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id){
        Category category = getCategoryOrThrowError(id);
        createLog(category, Activity.DELETE);
        repo.delete(category);
    }

    @Transactional
    public CategoryResponseDto editCategory(Long id, CategoryCreateDto dto){
        Category category = getCategoryOrThrowError(id);

        category.setName(dto.name());
        createLog(category, Activity.MODIFY);
        return mapper.categoryToResponse(repo.save(category));
    }

    private void createLog(Category category, Activity activity){
        ActivityLog activityLog = ActivityLog.builder()
                .activity(activity)
                .description(String.format("Category ID %d Name %s",
                        category.getId(),
                        category.getName())
                )
                .build();

        logRepo.save(activityLog);
    }

    public void deleteAll() {
        repo.deleteAll();
    }
}
