package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.config.SiteProperties;
import com.blog.dto.SiteUpdateRequest;
import com.blog.entity.Setting;
import com.blog.mapper.SettingMapper;
import com.blog.vo.SiteInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 站点信息服务：settings 表优先，application.yml 的 blog.site 作为默认值兜底
 */
@Service
@RequiredArgsConstructor
public class SiteService {

    public static final String KEY_TITLE = "site_title";
    public static final String KEY_SUBTITLE = "site_subtitle";
    public static final String KEY_AUTHOR = "site_author";
    public static final String KEY_ICP = "site_icp";
    public static final String KEY_FOOTER = "site_footer";
    public static final String KEY_ANNOUNCEMENT = "site_announcement";
    public static final String KEY_ABOUT_MD = "site_about_md";
    public static final String KEY_ALLOW_COMMENTS = "site_allow_comments";

    private final SettingMapper settingMapper;
    private final MarkdownService markdownService;
    private final SiteProperties siteProperties;

    private Map<String, String> tableMap() {
        return settingMapper.selectList(null).stream()
                .collect(Collectors.toMap(Setting::getSkey, s -> s.getSvalue() == null ? "" : s.getSvalue()));
    }

    private String get(Map<String, String> map, String key, String def) {
        String v = map.get(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    public SiteInfoVO info() {
        Map<String, String> m = tableMap();
        SiteInfoVO vo = new SiteInfoVO();
        vo.setTitle(get(m, KEY_TITLE, siteProperties.getTitle()));
        vo.setSubtitle(get(m, KEY_SUBTITLE, siteProperties.getSubtitle()));
        vo.setAuthor(get(m, KEY_AUTHOR, siteProperties.getAuthor()));
        vo.setIcp(get(m, KEY_ICP, ""));
        vo.setFooter(get(m, KEY_FOOTER, ""));
        vo.setAnnouncement(get(m, KEY_ANNOUNCEMENT, ""));
        vo.setAboutMd(get(m, KEY_ABOUT_MD, ""));
        vo.setAboutHtml(markdownService.render(vo.getAboutMd()));
        try {
            vo.setAllowComments(Integer.parseInt(get(m, KEY_ALLOW_COMMENTS, "1")));
        } catch (NumberFormatException e) {
            vo.setAllowComments(1);
        }
        return vo;
    }

    @Transactional
    public void update(SiteUpdateRequest request) {
        if (request.getTitle() != null) {
            saveKV(KEY_TITLE, request.getTitle(), "站点标题");
        }
        if (request.getSubtitle() != null) {
            saveKV(KEY_SUBTITLE, request.getSubtitle(), "站点副标题");
        }
        if (request.getAuthor() != null) {
            saveKV(KEY_AUTHOR, request.getAuthor(), "站长昵称");
        }
        if (request.getIcp() != null) {
            saveKV(KEY_ICP, request.getIcp(), "备案号");
        }
        if (request.getFooter() != null) {
            saveKV(KEY_FOOTER, request.getFooter(), "页脚文案");
        }
        if (request.getAnnouncement() != null) {
            saveKV(KEY_ANNOUNCEMENT, request.getAnnouncement(), "站点公告");
        }
        if (request.getAboutMd() != null) {
            saveKV(KEY_ABOUT_MD, request.getAboutMd(), "关于页 Markdown 内容");
        }
        if (request.getAllowComments() != null) {
            saveKV(KEY_ALLOW_COMMENTS, String.valueOf(request.getAllowComments()), "是否允许评论");
        }
    }

    private void saveKV(String key, String value, String remark) {
        Setting setting = settingMapper.selectOne(new QueryWrapper<Setting>().eq("skey", key));
        if (setting == null) {
            setting = new Setting();
            setting.setSkey(key);
            setting.setSvalue(value);
            setting.setRemark(remark);
            settingMapper.insert(setting);
        } else {
            setting.setSvalue(value);
            settingMapper.updateById(setting);
        }
    }
}
