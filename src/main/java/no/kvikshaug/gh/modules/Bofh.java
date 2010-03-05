package no.kvikshaug.gh.modules;

import no.kvikshaug.gh.Grouphug;
import no.kvikshaug.gh.ModuleHandler;
import no.kvikshaug.gh.exceptions.SQLUnavailableException;
import no.kvikshaug.gh.listeners.TriggerListener;
import no.kvikshaug.gh.util.SQLHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Need a quick excuse to shut a luser up? Look no further, the BOFH module will assist you.
 *
 * User: sunn/oyvindio
 * Date: 27.nov.2008
 * Time: 23:34:16
 */
public class Bofh implements TriggerListener {

    private static final String RANDOM_TRIGGER = "bofh";
    private static final String SPECIFIC_TRIGGER = "\\d+";
    public static final String HELP_TRIGGER = RANDOM_TRIGGER;

    private Random random;
    private List<String> excuses;

    /**
     * Initializes the excuses arraylist by fetching all rows from the database and filling the arraylist with their
     * contents
     * @param moduleHandler the handler for this module
     */
    public Bofh(ModuleHandler moduleHandler) {
        try {
            random = new Random(System.nanoTime());
            SQLHandler sqlHandler = SQLHandler.getSQLHandler();
            excuses = new ArrayList<String>(500); // there's just short of 500 rows in the db at the moment.

            List<Object[]> rows = sqlHandler.select("SELECT `excuse` FROM bofh;");

            if(rows.size() == 0) {
                throw new SQLException("Unable to find any rows in the excuse table.");
            }

            int i = 1;
            for (Object[] row : rows) {
                excuses.add("BOFH excuse #" + i + ": " + row[0]);
                i++;
            }
            // Don't trim to size as this is a List and not an ArrayList
            //excuses.trimToSize();

            moduleHandler.addTriggerListener(RANDOM_TRIGGER, this);
            moduleHandler.registerHelp(HELP_TRIGGER, "BOFH - Fend off lusers with Bastard Operator From Hell excuses for their system \"problems\".\n" +
                    "Usage:\n" +
                    Grouphug.MAIN_TRIGGER + RANDOM_TRIGGER + " returns a random excuse.\n" +
                    Grouphug.MAIN_TRIGGER + RANDOM_TRIGGER + " " + SPECIFIC_TRIGGER + " returns an excuse by number. (\\d+ means any digit, 1-n times)");
            System.out.println("BOFH module loaded.");
        } catch (SQLUnavailableException ex) {
            System.err.println("BOFH failed to start: SQL is unavailable!");
        } catch (SQLException se) {
            System.err.println("BOFH failed to start: SQL exception while fetching initial excuses!\n" +
                    "SQL said: " + se);
            se.printStackTrace();
        }
    }

    public void onTrigger(String channel, String sender, String login, String hostname, String message, String trigger) {
        String reply;
        if(message.equals("")) {
            reply = excuses.get(random.nextInt(excuses.size()));
        } else {
            try {
                int number = Integer.parseInt(message);

                if (number < 1 || number > excuses.size())
                    reply = "Invalid number. Valid numbers are 1-" + excuses.size() + ".";
                else
                    reply = excuses.get(number - 1); // 0-indexed, hence the -1.
            } catch (NumberFormatException nfe) {
                reply = "That's not a number, is it now?";
            }
        }
        if (reply != null) {
            Grouphug.getInstance().sendMessage(reply);
        }
    }
}