package main.java.pro.sky.telegrambot.sender;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationSenderService {

    private static final Logger log = LoggerFactory.getLogger(NotificationSenderService.class);

    private final TelegramBot telegramBot;

    private final NotificationTaskRepository notificationTaskRepository;

    @Autowired
    public NotificationSenderService(TelegramBot telegramBot, NotificationTaskRepository notificationTaskRepository) {
        this.telegramBot = telegramBot;
        this.notificationTaskRepository = notificationTaskRepository;
    }

    // Метод для отправки уведомлений
    public void sendNotifications() {
        LocalDateTime currentMinute = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> notifications = notificationTaskRepository.findByScheduledTime(currentMinute);

        for (NotificationTask task : notifications) {
            try {
                // Формирование запроса на отправку сообщения
                SendMessage request = new SendMessage(task.getChatId(), task.getMessageText());
                telegramBot.execute(request);

                // Логирование успеха
                log.info("Отправлено уведомление в чат {}: {}", task.getChatId(), task.getMessageText());

            } catch (Exception e) {
                // Логирование ошибки
                log.error("Ошибка при отправке уведомления в чат {}: {}", task.getChatId(), e.getMessage());
            }
        }
    }
}