package com.slayspire.analyzer.models;

import java.util.HashMap;
import java.util.Map;

public class Relic {
    private String id;
    private String name;
    private String normalizedName;
    private String pool;
    private String rarity;
    private String description;
    private String descriptionRaw;
    private String flavor;
    private String compendiumOrder;
    private String imageUrl;
    private String merchantPrice;
    private Map<String, Object> effects = new HashMap<>();

    public Relic() {}

    public Relic(String id, String name, String pool, String rarity, String description) {
        this.id = id;
        this.name = name;
        this.pool = pool;
        this.rarity = rarity;
        this.description = description;
        this.normalizedName = normalizeName(name);
    }

    private String normalizeName(String name) {
        if (name == null || name.isEmpty()) return "";
        return name.toLowerCase().replaceAll("[\\s\\-_]+", "");
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getNormalizedName() { return normalizedName; }
    public String getPool() { return pool; }
    public String getRarity() { return rarity; }
    public String getDescription() { return description; }
    public String getDescriptionRaw() { return descriptionRaw; }
    public String getFlavor() { return flavor; }
    public String getCompendiumOrder() { return compendiumOrder; }
    public String getImageUrl() { return imageUrl; }
    public String getMerchantPrice() { return merchantPrice; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; this.normalizedName = normalizeName(name); }
    public void setPool(String pool) { this.pool = pool; }
    public void setRarity(String rarity) { this.rarity = rarity; }
    public void setDescription(String description) { this.description = description; }
    public void setDescriptionRaw(String descriptionRaw) { this.descriptionRaw = descriptionRaw; }
    public void setFlavor(String flavor) { this.flavor = flavor; }
    public void setCompendiumOrder(String compendiumOrder) { this.compendiumOrder = compendiumOrder; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setMerchantPrice(String merchantPrice) { this.merchantPrice = merchantPrice; }

    @Override
    public String toString() {
        return String.format("%s (%s, %s)", name, pool, rarity);
    }
}
