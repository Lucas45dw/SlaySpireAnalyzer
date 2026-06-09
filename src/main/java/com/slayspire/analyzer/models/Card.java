package com.slayspire.analyzer.models;

import java.util.HashMap;
import java.util.Map;

public class Card {
    private String id;
    private String name;
    private String normalizedName;
    private String color;
    private String rarity;
    private String type;
    private String typeKey;
    private int cost;
    private String description;
    private String descriptionRaw;
    private String upgrade;
    private String upgradeDescription;
    private String keywords;
    private String keywordsKey;
    private String compendiumOrder;
    private String imageUrl;
    private String betaImageUrl;
    private String damage;
    private String block;
    private String cardsDraw;
    private String energyGain;
    private String hitCount;
    private String hpLoss;
    private String starCost;
    private String tags;
    private String target;
    private String vars;
    private boolean isXCost;
    private boolean isXStarCost;
    private Map<String, Object> effects = new HashMap<>();

    public Card() {}

    public Card(String id, String name, String color, String rarity, String type,
                int cost, String description, String upgrade, String upgradeDescription) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.rarity = rarity;
        this.type = type;
        this.cost = cost;
        this.description = description;
        this.upgrade = upgrade;
        this.upgradeDescription = upgradeDescription;
        this.normalizedName = normalizeName(name);
    }

    private String normalizeName(String name) {
        if (name == null || name.isEmpty()) return "";
        return name.toLowerCase().replaceAll("[\\s\\-_]+", "");
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getNormalizedName() { return normalizedName; }
    public String getColor() { return color; }
    public String getRarity() { return rarity; }
    public String getType() { return type; }
    public String getTypeKey() { return typeKey; }
    public int getCost() { return cost; }
    public String getDescription() { return description; }
    public String getDescriptionRaw() { return descriptionRaw; }
    public String getUpgrade() { return upgrade; }
    public String getUpgradeDescription() { return upgradeDescription; }
    public String getKeywords() { return keywords; }
    public String getKeywordsKey() { return keywordsKey; }
    public String getCompendiumOrder() { return compendiumOrder; }
    public String getImageUrl() { return imageUrl; }
    public String getDamage() { return damage; }
    public String getBlock() { return block; }
    public String getCardsDraw() { return cardsDraw; }
    public String getEnergyGain() { return energyGain; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; this.normalizedName = normalizeName(name); }
    public void setColor(String color) { this.color = color; }
    public void setRarity(String rarity) { this.rarity = rarity; }
    public void setType(String type) { this.type = type; }
    public void setTypeKey(String typeKey) { this.typeKey = typeKey; }
    public void setCost(int cost) { this.cost = cost; }
    public void setDescription(String description) { this.description = description; }
    public void setDescriptionRaw(String descriptionRaw) { this.descriptionRaw = descriptionRaw; }
    public void setUpgrade(String upgrade) { this.upgrade = upgrade; }
    public void setUpgradeDescription(String upgradeDescription) { this.upgradeDescription = upgradeDescription; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    public void setKeywordsKey(String keywordsKey) { this.keywordsKey = keywordsKey; }
    public void setCompendiumOrder(String compendiumOrder) { this.compendiumOrder = compendiumOrder; }
    public void setDamage(String damage) { this.damage = damage; }
    public void setBlock(String block) { this.block = block; }
    public void setCardsDraw(String cardsDraw) { this.cardsDraw = cardsDraw; }
    public void setEnergyGain(String energyGain) { this.energyGain = energyGain; }
    public void setXCost(boolean isXCost) { this.isXCost = isXCost; }
    public void setXStarCost(boolean isXStarCost) { this.isXStarCost = isXStarCost; }

    public String getFormattedCost() {
        if (isXCost) return "X";
        if (isXStarCost) return "X*";
        if (starCost != null && !starCost.isEmpty()) return starCost + "*";
        return String.valueOf(cost);
    }

    public boolean hasUpgrade() {
        return upgrade != null && !upgrade.isEmpty() && !upgrade.equals("{}");
    }

    @Override
    public String toString() {
        return String.format("%s (%s, %s, %d cost)", name, color, rarity, cost);
    }
}
