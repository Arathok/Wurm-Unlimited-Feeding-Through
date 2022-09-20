package org.arathok.wurmunlimited.mods.FeedingThrough;

import com.wurmonline.server.items.*;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.shared.constants.IconConstants;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import java.io.IOException;


public class FeedingThroughItem {

    static ItemTemplate feedingThrough;
    static String radiusString;
    static int feedingThroughId;
    public static void registerFeedingThrough() throws IOException {
        if (Config.feedOnDeed)
        {
            radiusString = "on your Deed over time.";
        }
        else radiusString = "in a radius of: "+String.valueOf(Config.radiusPerTenQL)+" tiles";
        feedingThrough = new ItemTemplateBuilder("arathok.feedingThrough.feedingThrough.").name("Feeding Through",
                        "throughs",
                        "A wooden tub, made for animals to feed from it. Will feed any animal "+radiusString)

                .modelName("model.arathok.feedingThrough.")
                .imageNumber((short) IconConstants.ICON_LARGE_CRATE)
                .itemTypes(new short[] {

                        ItemTypes.ITEM_TYPE_BULKCONTAINER,
                        ItemTypes.ITEM_TYPE_HOLLOW,
                        ItemTypes.ITEM_TYPE_TOOL,
                        ItemTypes.ITEM_TYPE_PLANTABLE,
                        ItemTypes.ITEM_TYPE_DECORATION,
                        ItemTypes.ITEM_TYPE_TURNABLE,
                        ItemTypes.ITEM_TYPE_NO_IMPROVE,

                }).decayTime(14400L).dimensions(300, 50, 80).weightGrams(10000).material(Materials.MATERIAL_WOOD_BIRCH)
                .behaviourType((short) 1).primarySkill(SkillList.SMITHING_BLACKSMITHING).difficulty(30.0F) // no hard lock
                .build();

        feedingThroughId = feedingThrough.getTemplateId();

        CreationEntryCreator
                .createAdvancedEntry(SkillList.CARPENTRY, ItemList.nailsIronLarge, ItemList.plank,feedingThroughId, false, false, 0f, false, false,0,30, CreationCategories.ANIMAL_EQUIPMENT)
                .addRequirement(new CreationRequirement(1, ItemList.nailsIronSmall, 7, true))
                .addRequirement(new CreationRequirement(2, ItemList.plank, 5, true));


    }

}
