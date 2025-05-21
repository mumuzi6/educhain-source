/**
 * 用户类型
 */
export interface User {
    id: number;
    username: string;
    userAccount: string;
    avatarUrl?: string;
    gender: number;
    phone: string;
    email: string;
    userStatus: number;
    createTime: Date;
    updateTime: Date;
    userRole: number;
    planetCode: string;
    tags?: string;
}
