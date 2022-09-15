package org.arathok.wurmunlimited.mods.FeedingThrough;

import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.creatures.Communicator;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class FeedingThrough implements WurmServerMod, Initable, PreInitable, Configurable, ItemTemplatesCreatedListener, ServerStartedListener, ServerPollListener, PlayerMessageListener{

    public static Connection dbconn;
    public static Logger logger = Logger.getLogger("FeedingThrough");;
    @Override
    public void configure(Properties properties) {

    }

    @Override
    public void onItemTemplatesCreated() {
        try {
            FeedingThroughItem.registerFeedingThrough();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onPlayerMessage(Communicator communicator, String s) {
        return false;
    }

    @Override
    public void onServerPoll() {
            PollAnimals.poller();
    }

    @Override
    public void onServerStarted() {

        try {
            dbconn = ModSupportDb.getModSupportDb();

            // check if the ModSupportDb table exists
            // if not, create the table and update it with the server's last crop poll time
            if (!ModSupportDb.hasTable(dbconn, "FeedingThroughs")) {
                // table create
                try (PreparedStatement ps = dbconn.prepareStatement("CREATE TABLE FeedingThroughs (itemId LONG PRIMARY KEY NOT NULL DEFAULT 0, isActive BOOLEAN NOT NULL DEFAULT false")) {
                    ps.execute();

                }

            }

            PollAnimals pollAnimals = new PollAnimals();
            PollAnimals.readFromSQL(dbconn, PollAnimals.feedingThroughs);
            ModActions.registerBehaviourProvider(new FeedingThroughBehaviour());

        } catch (SQLException e) {
            logger.severe("something went wrong with the database!" + e);
            e.printStackTrace();
        } catch (NoSuchItemException e) {
            logger.severe("no item for that id!" + e);
            e.printStackTrace();
        }

    }

    @Override
    public void init() {
        WurmServerMod.super.init();
    }

    @Override
    public void preInit() {
        WurmServerMod.super.preInit();
    }

}
