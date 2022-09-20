package org.arathok.wurmunlimited.mods.FeedingThrough;


import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.Traits;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import org.gotti.wurmunlimited.modsupport.actions.ActionEntryBuilder;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.ActionPropagation;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class PollAnimals  {

    static long nextpoll = 0;
    public static List<FeedingThroughObject> feedingThroughs = new LinkedList<>();

    public static void poller() throws NoSuchItemException {
        long time=System.currentTimeMillis();
        if (nextpoll<time) {
            feedAnimals();
            nextpoll = time + 600000;
        }

    }

    public static Item findFood(Item aFoodStorage, Creature aCreature)
    {

        for (Item afoodItem : aFoodStorage.getItems())
        {
            if (afoodItem.getTemplateId() != ItemList.bulkItem) continue;
            ItemTemplate realtTemplate = afoodItem.getRealTemplate();
            if (realtTemplate == null) continue;
            if (aCreature.getTemplate().getTemplateId() == CreatureTemplateIds.SEAL_CID) {
                if (afoodItem.isFish())
                    return afoodItem;
                else
                    continue;
            }
            if (afoodItem.isMeat() || afoodItem.isFish()) {
                if (aCreature.isCarnivore() || aCreature.isOmnivore())
                    return afoodItem;
                else
                    continue;
            }
            if (afoodItem.isSeed() || afoodItem.isVegetable() || afoodItem.isHerb()) {
                if (aCreature.isHerbivore() || aCreature.isOmnivore())
                    return afoodItem;
                else
                    continue;
            }
            if (afoodItem.isFood() && aCreature.isOmnivore())
                return afoodItem;
        }
        return null;
        }




    public static void feedAnimals() throws NoSuchItemException {

            for (FeedingThroughObject oneFeedingThrough : feedingThroughs)
            {
                float ql = Items.getItem(oneFeedingThrough.itemId).getCurrentQualityLevel();
                int targetRadius= (int) ((ql/10)*Config.radiusPerTenQL);
                Tiles.Tile test;
                VolaTile[] toCheck = Zones.getTilesSurrounding(Items.getItem(oneFeedingThrough.itemId).getTileX(),Items.getItem(oneFeedingThrough.itemId).getTileY(),true,targetRadius);
                for (VolaTile oneTile : toCheck)
                {
                    Creature[] creaturesOnTile = oneTile.getCreatures();
                    if (creaturesOnTile.length>0)
                    {
                        for (Creature aCreature : creaturesOnTile)
                        {
                            if (aCreature.hasTrait(Traits.TRAIT_CORRUPT))
                                continue;
                            Item aFeedingThrough = Items.getItem(oneFeedingThrough.itemId);
                            Item food=findFood (aFeedingThrough,aCreature);

                            if (food == null) continue;

                            ItemTemplate foodTemplate = food.getRealTemplate();

                            int baseVolume = foodTemplate.getVolume();
                            int volumeToRemove = food.getWeightGrams();
                            float partial = 1.0f;
                            float fishMod = 1;

                            if (foodTemplate.isFish() && foodTemplate.getTemplateId() != 369)
                                fishMod = food.getCurrentQualityLevel() / 100.0f;

                            if (volumeToRemove < baseVolume)
                                partial = volumeToRemove / baseVolume;
                            else
                                volumeToRemove = baseVolume;

                            food.setWeight(food.getWeightGrams() - volumeToRemove, true);

                            float hungerStilled = (float) foodTemplate.getWeightGrams() * partial * fishMod * food.getCurrentQualityLevel();

                            if (aCreature.getSize() == 5) {
                                hungerStilled *= 0.5F;
                            } else if (aCreature.getSize() == 4) {
                                hungerStilled *= 0.7F;
                            } else if (aCreature.getSize() == 2) {
                                hungerStilled *= 5.0F;
                            } else if (aCreature.getSize() == 1) {
                                hungerStilled *= 10.0F;
                            }

                            float nutrition;

                            if (foodTemplate.isHighNutrition()) {
                                nutrition = 0.56F + food.getQualityLevel() / 300.0F;
                            } else if (foodTemplate.isGoodNutrition()) {
                                nutrition = 0.4F + food.getQualityLevel() / 500.0F;
                            } else if (foodTemplate.isMediumNutrition()) {
                                nutrition = 0.3F + food.getQualityLevel() / 1000.0F;
                            } else {
                                nutrition = 0.1F + food.getQualityLevel() / 1000.0F;
                            }

                            aCreature.getStatus().modifyHunger((int) -hungerStilled, nutrition);


                        }
                    }
                }
            }
    }

    public static void readFromSQL(Connection dbconn, List<FeedingThroughObject> feedingThroughs) throws SQLException, NoSuchItemException {
        FeedingThrough.logger.log(Level.INFO,"reading all previously made feeding throughs from the DB");

        try {
            PreparedStatement ps = dbconn.prepareStatement("SELECT * FROM FeedingThroughs");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {


                FeedingThroughObject feedingThrough = new FeedingThroughObject();
                feedingThrough.itemId = rs.getLong("itemId"); // liest quasi den Wert von der Spalte
                feedingThrough.isActive = rs.getBoolean("isActive"); // liest quasi den Wert von der Spalte

                FeedingThrough.logger.log(Level.INFO, "adding: " + feedingThrough);
                feedingThroughs.add(feedingThrough);
            }
            rs.close();
        } catch (SQLException throwables) {
            FeedingThrough.logger.log(Level.SEVERE,"something went wrong reading from the DB!",throwables);
            throwables.printStackTrace();
        }

    }

    public static void insert(Connection dbconn, FeedingThroughObject aFeedingThrough) throws SQLException {
        try {
            PreparedStatement ps = dbconn.prepareStatement("upsert into FeedingThroughs (itemID,isActive) values (?,?)");
            ps.setLong(1, aFeedingThrough.itemId);
            ps.setBoolean(2, aFeedingThrough.isActive);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException throwables) {
            FeedingThrough.logger.log(Level.SEVERE,"something went wrong writing to the DB!",throwables);
            throwables.printStackTrace();
        }


    }


    public static void remove(Connection dbconn, FeedingThroughObject aFeedingThrough ) {
        try {
            PreparedStatement ps = dbconn.prepareStatement("DELETE FROM FeedingThroughs WHERE itemId = ?");
            ps.setLong(1, aFeedingThrough.itemId);
            ps.execute();
            ps.close();
        } catch (SQLException throwables) {
            FeedingThrough.logger.log(Level.SEVERE, "something went wrong deleting a feeding through from the DB!", throwables);
            throwables.printStackTrace();
        }
    }

}









