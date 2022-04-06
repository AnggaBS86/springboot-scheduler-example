package com.angga.schedulingtasks;

import com.angga.birthdayscheduler.libraries.TimezoneMapper;
import com.angga.birthdayscheduler.model.User;
import com.angga.birthdayscheduler.model.UserBirthdayLog;
import com.angga.birthdayscheduler.services.UserBirthdayLogService;
import com.angga.birthdayscheduler.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rx.Observable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Component
public class ScheduledTasks {

    @Autowired
    private UserService userService;

    @Autowired
    private UserBirthdayLogService userBirthdayLogService;

    private final String hookbinUrl = "https://hookb.in/jePWmNGJqdi9dlMMmJL6";

    //@Scheduled(fixedRate = 5000) --> for debugging only

    //Every day at 9am
    @Scheduled(cron = "0 0 9 * * *")
    public void reportCurrentTime() {
        List<User> users = userService.findByBirthdayDate("2022-04-06");

        Observable<User> observable = Observable.from(users);
        observable
                .doOnError(s -> {
                    try {
                        throw new Exception("Error occured " + s.getCause());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .doOnCompleted(() -> System.out.println("Process completed!"))
                .subscribe(user -> {
                    String timezone = getTimezone(user) != null ? getTimezone(user) : "Asia/Jakarta";
                    Date date = new Date();
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    df.setTimeZone(TimeZone.getTimeZone(timezone));

                    System.out.println("Date and time : " + df.format(date));

                    if (this.isCongratulationSent(user)) {
                        System.out.println("Birthday gift for this year already sent");
                    } else {
                        System.out.println("Sending the gift...");

                        try {
                            this.handleHookbinData();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        this.handleBirthday(user, df.format(date));
                    }
                });
    }

    private boolean isCongratulationSent(User user) {
        try {
            List<UserBirthdayLog> userBirthdayLogs = userBirthdayLogService.findByUserId(user.getUserId());
            System.out.println("Data user " + userBirthdayLogs.get(0));
            if (userBirthdayLogs.get(0) != null) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    private void handleHookbinData() throws IOException {
        URL url = new URL(this.hookbinUrl);
        String query = "";
        URLConnection urlc = url.openConnection();


        urlc.setDoOutput(true);
        urlc.setAllowUserInteraction(false);
        PrintStream ps = new PrintStream(urlc.getOutputStream());
        ps.print(query);
        ps.close();

        BufferedReader br = new BufferedReader(new InputStreamReader(urlc
                .getInputStream()));
        String l = null;
        while ((l = br.readLine()) != null) {
            System.out.println(l);
        }

        br.close();
    }

    private void handleBirthday(User user, String currentDate) {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        df.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));

        UserBirthdayLog userBirthdayLog = new UserBirthdayLog();
        userBirthdayLog.setUserId(user.getUserId());
        userBirthdayLog.setCurrentYear(2022); //we use current year
        userBirthdayLog.setCreatedAt(df.format(date));
        userBirthdayLog.setUpdatedAt(df.format(date));

        boolean store = userBirthdayLogService.store(userBirthdayLog);
        if (store) {
            System.out.println("Store to users_birthday_log success!");
        } else {
            System.err.println("Store to users_birthday_log failed!");
        }
    }

    private String getTimezone(User user) {
        String[] location = user.getLocation().split(",");

        if (location[0] != null && location[1] != null) {
            String longitude = location[0];
            String latitude = location[1];
            System.out.println("Long : " + longitude + ", Lat : " + latitude);

            return TimezoneMapper.latLngToTimezoneString(
                    Double.parseDouble(longitude),
                    Double.parseDouble(latitude)
            );
        }

        return null;
    }
}
