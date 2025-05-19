import myAxios from "../plugins/myAxios";
import { setCurrentUserState } from "../states/user";

// 定义后端响应类型
interface BaseResponse<T> {
  code: number;
  data: T;
  message: string;
  description: string;
}

export const getCurrentUser = async () => {
    // const currentUser = getCurrentUserState();
    // if (currentUser) {
    //     return currentUser;
    // }
    // 不存在则从远程获取
    try {
        const res = await myAxios.get('/user/current') as BaseResponse<any>;
        if (res.code === 0) {
            setCurrentUserState(res.data);
            return res.data;
        }
    } catch (error) {
        console.error('获取当前用户信息失败', error);
    }
    return null;
}

