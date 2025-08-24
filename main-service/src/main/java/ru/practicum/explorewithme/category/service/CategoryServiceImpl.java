package ru.practicum.explorewithme.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.category.dao.CategoryRepository;
import ru.practicum.explorewithme.category.dto.ResponseCategoryDto;
import ru.practicum.explorewithme.category.mapper.CategoryMapper;
import ru.practicum.explorewithme.error.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    @Override
    public List<ResponseCategoryDto> getCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository
                .findAll(pageable)
                .map(categoryMapper::toCategoryDto)
                .toList();
    }

    @Override
    public ResponseCategoryDto getCategory(long catId) {
        return categoryRepository.findById(catId)
                .map(categoryMapper::toCategoryDto)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
    }
}
