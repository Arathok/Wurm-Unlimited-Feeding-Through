package org.arathok.wurmunlimited.mods.FeedingThrough;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.zones.Zones;
import org.gotti.wurmunlimited.modsupport.actions.ActionEntryBuilder;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.ActionPropagation;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class FeedingThroughShowRadiusPerformer implements ActionPerformer {

    public ActionEntry actionEntry;


    public FeedingThroughShowRadiusPerformer() {
        actionEntry = new ActionEntryBuilder((short) ModActions.getNextActionId(), "Show radius", "planning",
                new int[]{
                        6 /* ACTION_TYPE_NOMOVE */,
                        48 /* ACTION_TYPE_ENEMY_ALWAYS */,
                        35 /* DONT CARE WHETHER SOURCE OR TARGET */,

                }).range(4).build();

        ModActions.registerAction(actionEntry);

    }

    @Override
    public boolean action(Action action, Creature performer, Item source, Item target, short num, float counter) {
        return action(action, performer, target, num, counter);
    } // NEEDED OR THE ITEM WILL ONLY ACTIVATE IF YOU HAVE NO ITEM ACTIVE

    @Override
    public short getActionId() {
        return actionEntry.getNumber();
    }

    public static boolean canUse(Creature performer, Item target) {
        return performer.isPlayer() && !target.isTraded();
    }

    @Override
    public boolean action(Action action, Creature performer, Item target, short num, float counter) {


        if (!canUse(performer, target)) {
            performer.getCommunicator().sendAlertServerMessage("You are not allowed to do that");
            return propagate(action,
                    ActionPropagation.FINISH_ACTION,
                    ActionPropagation.NO_SERVER_PROPAGATION,
                    ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION);

        }

        try {
            List<Item> borderItems = new LinkedList<>();
            float ql = target.getCurrentQualityLevel();
            int targetRadius= (int) ((ql/10)*Config.radiusPerTenQL);
            //CORNERS//
            int maxX= target.getTileX()+targetRadius;
            int minX= target.getTileX()-targetRadius;
            int maxY= target.getTileY()+targetRadius;
            int minY= target.getTileY()-targetRadius;
            /// Draw four sides
            // Right side
            for (int y= minY; y<=maxY;y++)
            {
                Item marker = ItemFactory.createItem(ItemList.buildMarker,99.0F, (float) maxX,(float)y,90.0F,true,(byte)3,0,null);
                borderItems.add(marker);

            }

            // Left side
            for (int y= minY; y<=maxY;y++)
            {
                Item marker = ItemFactory.createItem(ItemList.buildMarker,99.0F, (float) minX,(float)y,90.0F,true,(byte)3,0,null);
                borderItems.add(marker);

            }

            // Top side
            for (int x= minX; x<=maxX;x++)
            {

                Item marker = ItemFactory.createItem(ItemList.buildMarker,99.0F, (float)x,(float)maxY,90.0F,true,(byte)3,0,null);
                borderItems.add(marker);

            }

            // Bottom side
            for (int x= minX; x<=maxX;x++)
            {

                Item marker = ItemFactory.createItem(ItemList.buildMarker,99.0F, (float)x,(float)minY,90.0F,true,(byte)3,0,null);
                borderItems.add(marker);

            }
            performer.getCommunicator().sendSafeServerMessage("you estimate the area animals would be enticed to use your feeding through.");
            if (target.getQualityLevel()<80.0F)
            performer.getCommunicator().sendSafeServerMessage("you think if you enhance its quality more, it might be more attractive to animals.");
            long time = System.currentTimeMillis();
            while (time+30000>System.currentTimeMillis() )
            {
                //do nothing
            }
            for (Item aMarker : borderItems)
                Items.destroyItem(aMarker.getWurmId());
                borderItems.clear();
        } catch (NoSuchTemplateException e) {
            throw new RuntimeException(e);
        } catch (FailedException e) {
            throw new RuntimeException(e);
        }


        return propagate(action,

                ActionPropagation.FINISH_ACTION,
                ActionPropagation.NO_SERVER_PROPAGATION,
                ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION);
    }


}
