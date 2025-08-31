package ru.practicum.explorewithme.consts;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
public class ConstantUtil {

    public static final LocalDateTime EPOCH_LOCAL_DATE_TIME = LocalDateTime.of(1970, 1, 1, 0, 0, 0);

}
