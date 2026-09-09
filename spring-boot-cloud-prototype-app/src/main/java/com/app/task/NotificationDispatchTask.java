package com.app.task;

import com.app.enums.NotificationStatusEnum;
import com.app.mapper.NotificationMapper;
import com.app.pojo.entity.NotificationEntity;
import com.app.support.notification.NotificationSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 待发送通知处理任务。
 *
 * @author yanlei
 * @since 2026-09-09
 */
@Component
public class NotificationDispatchTask {
    @Autowired
    private NotificationMapper notificationMapper;
    @Autowired
    private NotificationSender notificationSender;

    /**
     * 定时发送待处理通知，并在发送失败时安排退避重试。
     */
    @Scheduled(fixedDelayString = "${notification.dispatch.fixed-delay:30000}")
    public void dispatch() {
        List<NotificationEntity> pending = notificationMapper.selectPending(LocalDateTime.now());
        for (NotificationEntity notification : pending) {
            dispatchOne(notification);
        }
    }

    /**
     * 发送单条通知并更新发送状态。
     *
     * @param notification 待发送通知
     */
    protected void dispatchOne(NotificationEntity notification) {
        try {
            notificationMapper.updateStatusById(notification.getId(), NotificationStatusEnum.SENDING.getValue(), notification.getStatus());
            notificationSender.send(notification.getRecipient(), notification.getSubject(), notification.getContent());
            notificationMapper.markSent(notification.getId(), LocalDateTime.now());
        } catch (RuntimeException exception) {
            notificationMapper.markFailed(notification.getId(), exception.getMessage(), LocalDateTime.now().plusMinutes(5));
        }
    }
}
