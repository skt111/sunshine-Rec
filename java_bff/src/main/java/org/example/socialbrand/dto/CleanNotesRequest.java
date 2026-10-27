package org.example.socialbrand.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 清洗请求：按 id 列表或按企业清洗；默认只处理未清洗过的记录。
 */
@Data
public class CleanNotesRequest {

    private List<Long> ids = new ArrayList<>();
    private Long companyId;
    /** 本次最多清洗条数（上限由配置 max-batch 控制） */
    private Integer limit;
    /** 是否包含已清洗过的记录（默认 false） */
    private Boolean includeCleaned;
}
