package org.example.socialbrand.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 公开笔记详情：列表字段 + 全部历史快照（按观察时间倒序）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SocialNoteDetailVO extends SocialNoteVO {

    private List<SocialMetricSnapshotVO> snapshots = new ArrayList<>();
}
