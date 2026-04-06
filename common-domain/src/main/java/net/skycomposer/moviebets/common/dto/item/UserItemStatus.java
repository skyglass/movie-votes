package net.skycomposer.moviebets.common.dto.item;

public enum UserItemStatus {
    VOTED(0),
    BET_PLACED(1);

    private int value;

    private UserItemStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static UserItemStatus fromValue(int value) {
        for (UserItemStatus userItemStatus: values()) {
            if (userItemStatus.getValue() == value) {
                return userItemStatus;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown value for UserItemStatus enum: %d", value));
    }
}
