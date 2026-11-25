package test.java.pro.sky.telegrambot;

import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.TelegramBot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import pro.sky.telegrambot.sender.NotificationSenderService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationSenderServiceTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private NotificationTaskRepository notificationTaskRepository;

    @InjectMocks
    private NotificationSenderService service;

    private List<NotificationTask> sampleTasks;

    @BeforeEach
    void setUp() {
        sampleTasks = new ArrayList<>();
        sampleTasks.add(createSampleTask("Написать курсовую", 123456L));
        sampleTasks.add(createSampleTask("Купить продукты", 654321L));
    }

    private NotificationTask createSampleTask(String message, long chatId) {
        NotificationTask task = new NotificationTask();
        task.setChatId(chatId);
        task.setMessageText(message);
        task.setScheduledTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        return task;
    }

    @Test
    void shouldSendNotificationsIfAnyExist() {
        // Arrange
        given(notificationTaskRepository.findByScheduledTime(any())).willReturn(sampleTasks);

        // Act
        service.sendNotifications();

        // Assert
        verify(telegramBot, times(2)).execute(any(SendMessage.class));
    }

    @Test
    void shouldDoNothingIfNoNotifications() {
        // Arrange
        given(notificationTaskRepository.findByScheduledTime(any())).willReturn(new ArrayList<>());

        // Act
        service.sendNotifications();

        // Assert
        verify(telegramBot, times(0)).execute(any(SendMessage.class));
    }

    @Test
    void shouldHandleSendingErrorsGracefully() {
        // Arrange
        given(notificationTaskRepository.findByScheduledTime(any())).willReturn(sampleTasks);
        given(telegramBot.execute(any(SendMessage.class))).willThrow(RuntimeException.class);

        // Act
        service.sendNotifications();

        // Assert
        verify(telegramBot, times(2)).execute(any(SendMessage.class));
    }
}
