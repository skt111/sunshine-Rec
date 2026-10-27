package org.example.crawler.clean;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

/**
 * HTML 清洗器：去除脚本/样式，压缩空白，输出纯文本。
 */
@Component
public class HtmlCleaner {

    /**
     * 清洗 HTML 片段为纯文本（保留换行结构）。
     */
    public String toText(String html) {
        if (html == null || html.isBlank()) {
            return null;
        }
        Document doc = Jsoup.parse(html);
        doc.select("script,style,noscript,iframe").remove();
        // 保留基本格式标签，其余剥离
        String cleaned = Jsoup.clean(doc.body().html(), Safelist.basic());
        Document again = Jsoup.parse(cleaned);
        again.select("br").append("\\n");
        again.select("p").prepend("\\n\\n");
        String text = again.text().replace("\\n", "\n");
        text = text.replaceAll("[ \\t\\u00A0]+", " ").replaceAll("\\n{3,}", "\n\n").trim();
        return text.isEmpty() ? null : text;
    }

    /**
     * 提取纯文本并限制长度（入库保护）。
     */
    public String toText(String html, int maxLength) {
        String text = toText(html);
        if (text == null) {
            return null;
        }
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }

    /**
     * 从元素取文本（null 安全）。
     */
    public String textOf(Element element) {
        if (element == null) {
            return null;
        }
        String t = element.text();
        return t == null || t.isBlank() ? null : t.trim();
    }
}
