package org.example.supplyradar.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动变更记录表 {@code employment_event_history}。
 */
@Data
@TableName("employment_event_history")
public class EmploymentEventHistory {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long eventId;
    private Long crawlTaskId;
    private String changeType;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private LocalDateTime changedAt;
}
