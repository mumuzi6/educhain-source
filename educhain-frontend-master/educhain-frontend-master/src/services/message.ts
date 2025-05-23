import myAxios from "../plugins/myAxios";
import type BaseResponse from "../models/baseResponse";

/**
 * 发送消息
 * @param toId 接收者id
 * @param content 消息内容
 */
export const sendMessage = async (toId: number, content: string): Promise<number | null> => {
  try {
    const res = await myAxios.post('/message/send', {
      toId,
      content
    }) as BaseResponse<number>;
    
    if (res.code === 0) {
      return res.data;
    }
    return null;
  } catch (error) {
    console.error('发送消息失败', error);
    return null;
  }
};

/**
 * 获取与指定用户的聊天记录
 * @param userId 对方用户id
 */
export const getMessagesByUser = async (userId: number) => {
  try {
    const res = await myAxios.get('/message/list', {
      params: {
        userId
      }
    }) as BaseResponse<any>;
    
    if (res.code === 0) {
      return res.data;
    }
    return [];
  } catch (error) {
    console.error('获取聊天记录失败', error);
    return [];
  }
};

/**
 * 获取所有聊天会话
 */
export const getConversations = async () => {
  try {
    const res = await myAxios.get('/message/conversations') as BaseResponse<any>;
    
    if (res.code === 0) {
      return res.data;
    }
    return [];
  } catch (error) {
    console.error('获取会话列表失败', error);
    return [];
  }
};

/**
 * 标记消息为已读
 * @param messageId 消息id
 */
export const markMessageAsRead = async (messageId: number): Promise<boolean> => {
  try {
    const res = await myAxios.post('/message/read', null, {
      params: {
        messageId
      }
    }) as BaseResponse<boolean>;
    
    return res.code === 0 && res.data;
  } catch (error) {
    console.error('标记已读失败', error);
    return false;
  }
};

/**
 * 获取未读消息数量
 */
export const getUnreadCount = async (): Promise<number> => {
  try {
    const res = await myAxios.get('/message/unread/count') as BaseResponse<number>;
    
    if (res.code === 0) {
      return res.data;
    }
    return 0;
  } catch (error) {
    console.error('获取未读消息数量失败', error);
    return 0;
  }
}; 