package org.arathok.wurmunlimited.mods.FeedingThrough;

import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeedingThroughBehaviour  implements BehaviourProvider {

        private final List<ActionEntry> openThrough;
        private final List<ActionEntry> closeThrough;
        private final FeedingThroughOpenPerformer openPerformer;
        private final FeedingThroughClosePerformer closePerformer;

        public FeedingThroughBehaviour() {
            this.openPerformer = new FeedingThroughOpenPerformer();
            this.openThrough = Collections.singletonList(openPerformer.actionEntry);
            this.closePerformer = new FeedingThroughClosePerformer();
            this.closeThrough = Collections.singletonList(closePerformer.actionEntry);
            ModActions.registerActionPerformer(openPerformer);
            ModActions.registerActionPerformer(closePerformer);

        }
        @Override
        public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
            FeedingThroughObject feedingThroughToDelete = new FeedingThroughObject();
            for (FeedingThroughObject oneFeedingThrough: PollAnimals.feedingThroughs) {
                if (oneFeedingThrough.itemId==target.getWurmId()) {
                    feedingThroughToDelete = oneFeedingThrough;
                    break;
                }
            }

            if (target.getTemplateId() == FeedingThroughItem.feedingThroughId) {
                if (FeedingThroughOpenPerformer.canUse(performer, target) && (!PollAnimals.feedingThroughs.contains(feedingThroughToDelete)|| !feedingThroughToDelete.isActive)) {
                    return new ArrayList<>(openThrough);

                } else if (target.getTemplateId() == FeedingThroughItem.feedingThroughId && PollAnimals.feedingThroughs.contains(feedingThroughToDelete)&& feedingThroughToDelete.isActive) {
                    if (FeedingThroughClosePerformer.canUse(performer, target))
                        return new ArrayList<>(closeThrough);


                }

            } else
                return null;
            return null;

        }
        @Override
        public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
            return getBehavioursFor(performer, target);
        }



}
