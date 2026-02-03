package com.fuint.common.service;

import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.repository.model.MtMessage;
import java.util.List;

/**
 * 消息业务接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MessageService {

    /**
     * 添加消息
     *
     * @param reqMsgDto
     * @return
     */
    void addMessage(MtMessage reqMsgDto);

    /**
     * 置为已读
     *
     * @param msgId
     * @throws BusinessCheckException
     * @return
     */
    void readMessage(Integer msgId) throws BusinessCheckException;

    /**
     * 置为发送
     *
     * @param  msgId
     * @return
     */
    void sendMessage(Integer msgId, boolean isRead) throws BusinessCheckException;

    /**
     * 获取最新一条未读消息
     *
     * @param userId 会员ID
     * @return
     */
    MtMessage getOne(Integer userId);

    /**
     * 获取需要发送的消息
     *
     * @return
     * */
    List<MtMessage> getNeedSendList();
}
