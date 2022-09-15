package org.arathok.wurmunlimited.mods.FeedingThrough;


import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
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
    public static void poller()
    {
        long time=System.currentTimeMillis();
        if (nextpoll<time) {
            feedAnimals();
            nextpoll = time + 600000;
        }

    }

    public static void feedAnimals()
    {
            for (FeedingThroughObject oneFeedingThrough : feedingThroughs)
            {

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









