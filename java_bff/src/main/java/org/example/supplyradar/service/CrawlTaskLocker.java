package org.example.supplyradar.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 采集任务全局锁（MySQL GET_LOCK）。
 *
 * <p>GET_LOCK 为 session 级锁：由专用连接持有，释放时必须 RELEASE_LOCK + close，
 * 任务运行期间不归还连接池。</p>
 */
@Component
public class CrawlTaskLocker {

    private static final Logger log = LoggerFactory.getLogger(CrawlTaskLocker.class);
    private static final String LOCK_NAME = "supply_radar_crawl";

    private final DataSource dataSource;

    public CrawlTaskLocker(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 尝试获取全局锁。
     *
     * @return 持锁连接；失败返回 null
     */
    public Connection acquire() {
        try {
            Connection conn = dataSource.getConnection();
            boolean ok = false;
            try (PreparedStatement ps = conn.prepareStatement("SELECT GET_LOCK(?, 0)")) {
                ps.setString(1, LOCK_NAME);
                try (ResultSet rs = ps.executeQuery()) {
                    ok = rs.next() && rs.getInt(1) == 1;
                }
            }
            if (ok) {
                return conn;
            }
            conn.close();
            return null;
        } catch (Exception e) {
            log.warn("获取采集锁失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 释放全局锁（必须与 {@link #acquire()} 配对，finally 中调用）。
     */
    public void release(Connection conn) {
        if (conn == null) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement("SELECT RELEASE_LOCK(?)")) {
            ps.setString(1, LOCK_NAME);
            ps.executeQuery();
        } catch (Exception e) {
            log.warn("释放采集锁失败: {}", e.getMessage());
        } finally {
            try {
                conn.close();
            } catch (Exception ignore) {
                // ignore
            }
        }
    }
}
