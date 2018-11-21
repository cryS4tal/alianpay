package com.ylli.api.auth.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ylli.api.auth.Config;
import com.ylli.api.auth.mapper.NoticeMapper;
import com.ylli.api.auth.model.Notice;
import com.ylli.api.auth.model.emun.NoticeStatus;
import com.ylli.api.base.exception.AwesomeException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by yqpeng on 2017/3/17.
 */
@Service
public class NoticeService {
    @Autowired
    private NoticeMapper noticeMapper;

    public Page<Notice> query(Long ownerId, Integer state, String type, int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        return (Page<Notice>) noticeMapper.getList(ownerId, state, type);
    }

    public long getUnreadCount(Long ownerId) {
        Notice notice = new Notice();
        notice.ownerId = ownerId;
        notice.state = NoticeStatus.UNREAD;
        return (long) noticeMapper.selectCount(notice);
    }

    public Notice add(String type, Long outId, Long ownerId, String title, String description,
                      String extras) {
        Notice notice = new Notice();
        notice.type = type;
        notice.outId = outId;
        notice.ownerId = ownerId;
        notice.title = title;
        notice.description = description;
        notice.extras = extras;
        notice.state = NoticeStatus.UNREAD;
        noticeMapper.insertSelective(notice);
        return notice;
    }

    @Transactional
    public void markRead(List<Long> idList, Long ownerId) {
        if (idList == null) {
            return;
        }
        for (Long id : idList) {
            Notice selectNotice = new Notice();
            selectNotice.id = id;
            selectNotice.ownerId = ownerId;
            Notice notice = noticeMapper.selectOne(selectNotice);
            if (notice == null) {
                throw new AwesomeException(Config.ERROR_DATA_NOT_FOUND);
            }
            if (notice.state == NoticeStatus.READ) {
                continue;
            }
            noticeMapper.markRead(id);
        }
    }
}
