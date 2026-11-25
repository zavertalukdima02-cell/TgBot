package test.java.pro.sky.telegrambot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TelegramBotUpdatesListenerTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private NotificationTaskRepository notificationTaskRepository;

    @InjectMocks
    private TelegramBotUpdatesListener listener;

    private Message validMessage;

    @BeforeEach
    void setUp() {
        validMessage = Mockito.mock(Message.class);
        given(validMessage.text()).willReturn("01.01.2022 20:00 Написать курсовую");
        given(validMessage.chat().id()).willReturn(123456L);

        Update validUpdate = Mockito.mock(Update.class);
        given(validUpdate.message()).willReturn(validMessage);
    }

    @Test
    void shouldProcessStartCommand() {
        // Arrange
        given(validMessage.text()).willReturn("/start");

        // Act
        listener.processUserMessage(validMessage);

        // Assert
        verify(telegramBot).execute(any(SendMessage.class));
    }

    @Test
    void shouldProcessValidReminder() {
        // Arrange
        given(notificationTaskRepository.save(any())).willAnswer(invocation -> invocation.getArguments()[0]);

        // Act
        listener.processUserMessage(validMessage);

        // Assert
        verify(notificationTaskRepository).save(any(NotificationTask.class));
        verify(telegramBot).execute(any(SendMessage.class));
    }

    @Test
    void shouldIgnoreInvalidMessage() {
        // Arrange
        given(validMessage.text()).willReturn("Некорректное сообщение");

        // Act
        listener.processUserMessage(validMessage);

        // Assert
        verify(notificationTaskRepository, Mockito.never()).save(any(NotificationTask.class));
        verify(telegramBot, Mockito.never()).execute(any(SendMessage.class));
    }

    @Test
    void shouldHandleDatabaseFailure() {
        // Arrange
        given(notificationTaskRepository.save(any()))
                .willThrow(RuntimeException.class);

        // Act
        listener.processUserMessage(validMessage);

        // Assert
        verify(telegramBot).execute(any(SendMessage.class)); // Ожидаем отправку сообщения об ошибке
    }

    @Test
    void shouldCreateNotificationTaskCorrectly() {
        // Arrange
        LocalDateTime expectedTime = LocalDateTime.parse("01.01.2022 20:00", DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        expectedTime = expectedTime.truncatedTo(ChronoUnit.MINUTES);

        // Act
        listener.processUserMessage(validMessage);

        // Assert
        LocalDateTime finalExpectedTime = expectedTime;
        verify(notificationTaskRepository).save(Mockito.argThat(task ->
                task.getChatId() == 123456 &&
                        task.getMessageText().equals("Написать курсовую") &&
                        task.getScheduledTime().equals(finalExpectedTime)));
    }
}