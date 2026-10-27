package org.example.supplyradar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动变更记录 VO。
 */
@Data
public class ChangeVO {

    private String changeType;
    private String fieldName;
    private String oldValue;
    private String newValue;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime changedAt;
}
