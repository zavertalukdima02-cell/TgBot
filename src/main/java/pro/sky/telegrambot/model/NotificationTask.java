package main.java.pro.sky.telegrambot.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity // Объявляем класс сущностью Hibernate/JPA
@Table(name = "notification_task") // Имя таблицы в БД
public class NotificationTask {

    @Id // Аннотация, обозначающая первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Автоинкрементирование ключа
    private Long id;

    @Column(name = "chat_id", nullable = false) // Обязательное поле
    private Long chatId;

    @Column(name = "message_text", columnDefinition = "TEXT")
    private String messageText;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    public NotificationTask() {}

    public NotificationTask(Long chatId, String messageText, LocalDateTime scheduledTime) {
        this.chatId = chatId;
        this.messageText = messageText;
        this.scheduledTime = scheduledTime;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NotificationTask)) return false;
        NotificationTask that = (NotificationTask) o;
        return Objects.equals(id, that.id) && Objects.equals(chatId, that.chatId) && Objects.equals(messageText, that.messageText) && Objects.equals(scheduledTime, that.scheduledTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatId, messageText, scheduledTime);
    }

    @Override
    public String toString() {
        return "NotificationTask{" +
                "id=" + id +
                ", chatId='" + chatId + '\'' +
                ", messageText='" + messageText + '\'' +
                ", scheduledTime=" + scheduledTime +
                '}';
    }
}
