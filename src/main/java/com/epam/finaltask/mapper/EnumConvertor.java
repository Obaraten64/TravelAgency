package com.epam.finaltask.mapper;

import com.epam.finaltask.exception.exceptions.VoucherException;

import java.util.Arrays;
import java.util.Optional;

public class EnumConvertor {
    //ensure that given class is enum
    public static <T extends Enum<T>> T getCorrectEnumType(Class<? extends T> enumType, String type) {
        //check if given enum exist, if so return it
        return Arrays.stream(enumType.getEnumConstants())
                .filter(t -> t.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() ->
                        new VoucherException("Wrong " + enumType.getSimpleName() + " provided"));
    }

    public static <T extends Enum<T>> String getStringValueOfEnumType(T type) {
        return Optional.ofNullable(type).map(Enum::name).orElse("empty");
    }
}
