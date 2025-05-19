/**
 * 通用API响应格式
 */
interface BaseResponse<T = any> {
  /**
   * 状态码 0-成功，非0-失败
   */
  code: number;
  
  /**
   * 返回数据
   */
  data: T;
  
  /**
   * 状态码信息
   */
  message: string;
  
  /**
   * 详细描述
   */
  description: string;
}

export default BaseResponse; 