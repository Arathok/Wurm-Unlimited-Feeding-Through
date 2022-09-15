package org.arathok.wurmunlimited.mods.FeedingThrough;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import org.gotti.wurmunlimited.modsupport.actions.ActionEntryBuilder;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.ActionPropagation;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.logging.Level;

public class FeedingThroughOpenPerformer implements ActionPerformer {

    public ActionEntry actionEntry;


    public FeedingThroughOpenPerformer() {
        actionEntry = new ActionEntryBuilder((short) ModActions.getNextActionId(), "Open feeder", "opening",
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
                    FeedingThroughObject feedingThroughToAdd = new FeedingThroughObject();
                    feedingThroughToAdd.itemId=target.getWurmId();
                    feedingThroughToAdd.isActive=true;

                    PollAnimals.insert(FeedingThrough.dbconn,feedingThroughToAdd);
                    PollAnimals.feedingThroughs.add(feedingThroughToAdd);
                    FeedingThrough.logger.log(Level.INFO, performer.getName() + " opened their feedingThrough at " + target.getTileX() + " " + target.getTileY() + ", thus adding it to the AutoFeed list");
                    performer.getCommunicator().sendSafeServerMessage("You open the feeder of the feeding through. Animals will come to eat from it, whatever you put in.");
                    target.setName(target.getTemplate().getName());
                    target.setName(target.getName() + " (feeder opened)");

                } catch (Exception e) {
            throw new RuntimeException(e);
        }  return propagate(action,

                ActionPropagation.FINISH_ACTION,
                ActionPropagation.NO_SERVER_PROPAGATION,
                ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION);
    }


}
