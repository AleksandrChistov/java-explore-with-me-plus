package ru.practicum.explorewithme.category.service;

import ru.practicum.explorewithme.category.dto.ResponseCategoryDto;

import java.util.List;

public interface CategoryService {

    List<ResponseCategoryDto> getCategories(int from, int size);

    ResponseCategoryDto getCategory(Long catId);

}
