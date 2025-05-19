/**
 * 用户性别枚举
 */
export const genderEnum: {[key: number]: string} = {
    0: '男',
    1: '女',
}

/**
 * 用户性别映射，根据性别数字获取对应的文本显示
 * @param gender 性别值
 */
export const getGenderText = (gender: number) => {
    return genderEnum[gender] || '未知';
} 