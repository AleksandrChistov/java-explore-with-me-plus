package ru.practicum.explorewithme.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.category.dao.CategoryRepository;
import ru.practicum.explorewithme.category.dto.ResponseCategoryDto;
import ru.practicum.explorewithme.category.dto.RequestCategoryDto;
import ru.practicum.explorewithme.category.mapper.CategoryMapper;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.error.exception.NotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    @Override
    public ResponseCategoryDto save(RequestCategoryDto categoryDto) {
        Category newCategory = categoryMapper.toCategory(categoryDto);

        Category saved = categoryRepository.save(newCategory);

        return categoryMapper.toCategoryDto(saved);
    }

    @Override
    public ResponseCategoryDto update(long catId, RequestCategoryDto categoryDto) {
        Category fromDb = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        categoryMapper.updateCategoryFromDto(categoryDto, fromDb);

        Category updated = categoryRepository.save(fromDb);

        return categoryMapper.toCategoryDto(updated);
    }

    @Override
    public void delete(long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Category with id=" + catId + " was not found");
        }

        // todo: с категорией не должно быть связано ни одного события
        // /admin/events?categories=1&size=1
        // throw 409 with The category is not empty

        categoryRepository.deleteById(catId);
    }
}
