package com.angga.schedulingtasks;

import com.angga.birthdayscheduler.libraries.TimezoneMapper;
import com.angga.birthdayscheduler.model.User;
import com.angga.birthdayscheduler.model.UserBirthdayLog;
import com.angga.birthdayscheduler.services.UserBirthdayLogService;
import com.angga.birthdayscheduler.services.UserService;
import org.jetbrains.annotations.Nullable;
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
    private final String timeToSent = "01";

    @Scheduled(fixedRate = 1800)
    public void reportCurrentTime() {

        Date dateNow = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<User> users = userService.findByBirthdayDate(dateFormat.format(dateNow));

        Observable<User> observable = Observable.from(users);
        observable
                .doOnError(s -> {
                    try {
                        throw new Exception("Error occured " + s.getCause());
                    } catch (Exception e) {
                        this.doObservableProcess(users);
                        e.printStackTrace();
                    }
                })
                .doOnCompleted(() -> System.out.println("Process completed!"))
                .subscribe(user -> this.processBirthdayScheduler(user));
    }

    private void doObservableProcess(List<User> users) {
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
                .subscribe(user -> this.processBirthdayScheduler(user));
    }

    /**
     * Main process for birthday scheduler
     *
     * @param user
     *
     * @return void
     */
    private void processBirthdayScheduler(User user) {
        String timezone = getTimezone(user) != null ? getTimezone(user) : "Asia/Jakarta";
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("HH");
        df.setTimeZone(TimeZone.getTimeZone(timezone));
        String time = df.format(date);
        System.out.println("Time : " + df.format(date));

        //When the localtime is 09.00 ~ AM --> so sending the gift!
        if (time.equalsIgnoreCase(this.timeToSent)) {
            if (this.isCongratulationSent(user)) {
                System.out.println("Birthday gift for this year already sent");
            } else {

                System.out.println("Sending the gift...");

                if (this.handleBirthday(user)) {
                    try {
                        this.handleHookbinData();
                    } catch (IOException e) {
                        this.buildHookbinProcess();
                    }
                } else {
                    //If failed --> return once again for sending the
                    this.handleBirthday(user);
                }
            }
        }
    }

    private void buildHookbinProcess() {
        try {
            this.handleHookbinData();
        } catch (IOException e) {
            buildHookbinProcess();
        }
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
        String l;
        while ((l = br.readLine()) != null) {
            System.out.println(l);
        }

        br.close();
    }

    private boolean handleBirthday(User user) {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        df.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));

        UserBirthdayLog userBirthdayLog = this.buildUserBirthdayLog(user, date);

        return userBirthdayLogService.store(userBirthdayLog);
    }

    private UserBirthdayLog buildUserBirthdayLog(User user, Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        df.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));

        UserBirthdayLog userBirthdayLog = new UserBirthdayLog();
        userBirthdayLog.setUserId(user.getUserId());
        userBirthdayLog.setCurrentYear(2022); //we use current year
        userBirthdayLog.setCreatedAt(df.format(date));
        userBirthdayLog.setUpdatedAt(df.format(date));

        return userBirthdayLog;
    }

    @Nullable
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
