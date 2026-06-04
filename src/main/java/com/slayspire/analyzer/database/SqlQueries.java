package com.slayspire.analyzer.database;

public class SqlQueries {
    public static final String ALL_CARDS =
        "SELECT * FROM cards ORDER BY compendium_order";

    public static final String SEARCH_CARDS =
        "SELECT * FROM cards WHERE LOWER(name) LIKE ? ORDER BY compendium_order";

    public static final String FILTER_CARDS_BASE =
        "SELECT * FROM cards WHERE 1=1";

    public static final String CARD_BY_ID =
        "SELECT * FROM cards WHERE id = ?";

    public static final String DISTINCT_CARD_COLORS =
        "SELECT DISTINCT color FROM cards ORDER BY color";

    public static final String DISTINCT_CARD_RARITIES =
        "SELECT DISTINCT rarity FROM cards ORDER BY rarity";

    public static final String DISTINCT_CARD_TYPES =
        "SELECT DISTINCT type FROM cards ORDER BY type";

    public static final String CARD_COUNT_BY_COLOR_RARITY =
        "SELECT COUNT(*) FROM cards WHERE color = ? AND rarity = ?";

    public static final String TOTAL_CARD_COUNT =
        "SELECT COUNT(*) FROM cards";

    public static final String ALL_RELICS =
        "SELECT * FROM relics ORDER BY compendium_order";

    public static final String SEARCH_RELICS =
        "SELECT * FROM relics WHERE LOWER(name) LIKE ? ORDER BY compendium_order";

    public static final String FILTER_RELICS_BASE =
        "SELECT * FROM relics WHERE 1=1";

    public static final String DISTINCT_RELIC_POOLS =
        "SELECT DISTINCT pool FROM relics ORDER BY pool";

    public static final String DISTINCT_RELIC_RARITIES =
        "SELECT DISTINCT rarity FROM relics ORDER BY rarity";

    public static final String CARD_POOL_GROUP =
        "SELECT color, rarity, COUNT(*) as cnt FROM cards GROUP BY color, rarity";

    public static final String DISTINCT_COLORS =
        "SELECT DISTINCT color FROM cards ORDER BY color";
}
