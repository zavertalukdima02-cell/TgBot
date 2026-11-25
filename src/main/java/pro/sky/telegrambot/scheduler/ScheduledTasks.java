package main.java.pro.sky.telegrambot.scheduler;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import pro.sky.telegrambot.sender.NotificationSenderService;

@Component
public class ScheduledTasks {

    private final NotificationSenderService senderService;

    @Autowired
    public ScheduledTasks(NotificationSenderService senderService, NotificationTaskRepository notificationTaskRepository, TelegramBot telegramBot) {
        this.senderService = senderService;
    }

    // Планируется выполнение задачи каждую минуту
    @Scheduled(cron = "0 */1 * * * ?")
    public void runNotificationJob() {
        senderService.sendNotifications();
    }
}


