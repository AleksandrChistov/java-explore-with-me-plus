package ru.practicum.explorewithme.category.service;

import ru.practicum.explorewithme.category.dto.ResponseCategoryDto;
import ru.practicum.explorewithme.category.dto.RequestCategoryDto;

public interface AdminCategoryService {

    ResponseCategoryDto save(RequestCategoryDto categoryDto);

    ResponseCategoryDto update(long catId, RequestCategoryDto categoryDto);

    void delete(long catId);

}
