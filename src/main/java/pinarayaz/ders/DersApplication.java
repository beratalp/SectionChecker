package pinarayaz.ders;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Application to check section availability from Offerings
 * Created by PINAR on 24.1.2018.
 */
public class DersApplication {
    private static final List<String> VALID_COURSES = Arrays.asList("CS 202", "CS 224", "HUM 112", "MATH 230", "PHYS 102");
    private static final List<SectionMapUpdater> UPDATERS = Arrays.asList(new SectionMapUpdater("CS"),
            new SectionMapUpdater("HUM"), new SectionMapUpdater("MATH"), new SectionMapUpdater("PHYS"));

    public static void main(String args[]) throws InterruptedException {

        // initialize and register Telegram bot
        ApiContextInitializer.init();
        try {
            new TelegramBotsApi().registerBot(NotificationSender.init(args[0]));

        } catch (TelegramApiRequestException e) {
            // Failed to register Telegram bot - stop execution
            e.printStackTrace();
            System.err.println("Telegram bot registration failed");
            System.exit(1);
        }
        // end initialize and register Telegram bot

        Map<String, Section> sectionMap = new HashMap<>();

        System.out.println("10 sec pause for bot registrations");
        Thread.sleep(10000L);

        while (true) {
            for (SectionMapUpdater updater : UPDATERS) {
                updater.update(sectionMap);
            }

            for (Map.Entry<String, Section> entry : sectionMap.entrySet()) {
                for (String course : VALID_COURSES) {
                    Section s = entry.getValue();
                    if (s.getCourseId().startsWith(course)) {
                        //System.out.println(s.toString());
                        if (s.getQuota() != 0 && s.getQuotaPrev() == 0) {
                            NotificationSender.get().sendNotification("quota for " + s.getCourseId() + " is now " + s.getQuota());
                        }
                        s.setQuotaPrev(s.getQuota());
                    }
                }
            }
            Thread.sleep(20000L);
        }
    }
}
