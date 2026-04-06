package net.skycomposer.moviebets.common.dto.item;

public enum ItemType {
    MOVIE(0),
    MOVIE_ACTOR(1),
    MOVIE_DIRECTOR(2),
    MOVIE_CHARACTER(3),
    MUSIC(4),
    MUSIC_ARTIST(5),
    MUSIC_ALBUM(6),
    BOOK(7),
    BOOK_AUTHOR(8);


    private int value;

    private ItemType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ItemType fromValue(int value) {
        for (ItemType itemType: values()) {
            if (itemType.getValue() == value) {
                return itemType;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown value for ItemType enum: %d", value));
    }
}
