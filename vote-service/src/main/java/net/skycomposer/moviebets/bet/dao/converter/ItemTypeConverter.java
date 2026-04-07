package net.skycomposer.moviebets.bet.dao.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import net.skycomposer.moviebets.common.dto.item.ItemType;

@Converter(autoApply = false)
public class ItemTypeConverter implements AttributeConverter<ItemType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ItemType result) {
        return result != null ? result.getValue() : null;
    }

    @Override
    public ItemType convertToEntityAttribute(Integer dbValue) {
        return dbValue != null ? ItemType.fromValue(dbValue) : null;
    }
}

