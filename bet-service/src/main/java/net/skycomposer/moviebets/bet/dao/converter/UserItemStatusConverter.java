package net.skycomposer.moviebets.bet.dao.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import net.skycomposer.moviebets.common.dto.item.UserItemStatus;

@Converter(autoApply = false)
public class UserItemStatusConverter implements AttributeConverter<UserItemStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(UserItemStatus result) {
        return result != null ? result.getValue() : null;
    }

    @Override
    public UserItemStatus convertToEntityAttribute(Integer dbValue) {
        return dbValue != null ? UserItemStatus.fromValue(dbValue) : null;
    }
}

