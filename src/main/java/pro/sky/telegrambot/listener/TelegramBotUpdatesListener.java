package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
//@RequiredArgsConstructor
public class TelegramBotUpdatesListener implements UpdatesListener {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private final TelegramBot telegramBot;

    @Autowired
    private final NotificationTaskRepository notificationTaskRepository;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskRepository notificationTaskRepository) {
        this.telegramBot = telegramBot;
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @Override
    public int process(List<Update> updates) {
                for (Update update : updates) {
                    Message message = update.message();
                    if (message != null && message.text() != null) {
                        processUserMessage(message);
                    }
                }
            }
            return CONFIRMED_UPDATES_ALL;
        }

    public void processUserMessage(Message message) {
        String text = message.text();
        long chatId = message.chat().id();

        // Если пришло стартовое сообщение "/start", отправляем приветствие
        if (text.equals("/start")) {
            SendMessage response = new SendMessage(chatId, "Привет, хозяин!");
            telegramBot.execute(response);
            return;
        }

        // Анализируем сообщения на предмет формата напоминания
        Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");
        Matcher matcher = pattern.matcher(text);

        if (matcher.matches()) {
            String datetimeStr = matcher.group(1); // Дата и время
            String reminderText = matcher.group(3); // Сам текст напоминания

            try {
                // Парсим дату и время
                LocalDateTime scheduleTime = LocalDateTime.parse(datetimeStr,
                        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

                // Перед сохранением усекаем до минут
                scheduleTime = scheduleTime.truncatedTo(ChronoUnit.MINUTES);

                // Создаем объект NotificationTask
                NotificationTask task = new NotificationTask(chatId, reminderText, scheduleTime);

                // Сохраняем задачу в БД
                notificationTaskRepository.save(task);

                // Сообщаем пользователю, что напоминание создано
                SendMessage response = new SendMessage(chatId, "Напоминание сохранено: "
                        + datetimeStr + ", \"" + reminderText + "\"");
                telegramBot.execute(response);

            } catch (Exception ex) {
                log.error("Ошибка при обработке напоминания:", ex);
                SendMessage errorResponse = new SendMessage(chatId, "Что-то пошло не так при сохранении напоминания.");
                telegramBot.execute(errorResponse);
            }
        } else {
            // Игнорируем неподходящие сообщения или выводим какое-то другое поведение
            log.info("Неподдерживаемое сообщение: {}", text);
        }
    }
