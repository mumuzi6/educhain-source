-- 首先插入父标签(分类)
INSERT IGNORE INTO tag (tagName, isParent) VALUES 
('性别', 1),
('年级', 1),
('编程语言', 1),
('兴趣', 1),
('运动',1),
('队伍角色',1);

-- 获取父标签ID
SET @gender_id := (SELECT id FROM tag WHERE tagName = '性别' AND isParent = 1);
SET @grade_id := (SELECT id FROM tag WHERE tagName = '年级' AND isParent = 1);
SET @major_id := (SELECT id FROM tag WHERE tagName = '编程语言' AND isParent = 1);
SET @interest_id := (SELECT id FROM tag WHERE tagName = '兴趣' AND isParent = 1);
SET @sport_id := (SELECT id FROM tag WHERE tagName = '运动' AND isParent = 1);
SET @role_id := (SELECT id FROM tag WHERE tagName = '队伍角色' AND isParent = 1);

-- 然后插入子标签，并关联到父标签
INSERT IGNORE INTO tag (tagName, parentId, isParent) VALUES 
('男', @gender_id, 0),
('女', @gender_id, 0),
('大一', @grade_id, 0),
('大二', @grade_id, 0),
('大三', @grade_id, 0),
('大四', @grade_id, 0),
('研一', @grade_id, 0),
('研二', @grade_id, 0),
('研三', @grade_id, 0),
('Java', @major_id, 0),
('Python', @major_id, 0),
('C++', @major_id, 0),
('JavaScript', @major_id, 0),
('Go', @major_id, 0),
('游戏', @interest_id, 0),
('音乐', @interest_id, 0),
('动漫', @interest_id, 0),
('唱歌', @interest_id, 0),
('作曲', @interest_id, 0),
('开发', @interest_id, 0),
('写作', @interest_id, 0),
('篮球', @sport_id, 0),
('足球', @sport_id, 0),
('羽毛球', @sport_id, 0),
('乒乓球', @sport_id, 0),
('游泳', @sport_id, 0),
('主唱', @role_id, 0),
('键盘手', @role_id, 0),
('吉他手', @role_id, 0),
('贝斯手', @role_id, 0),
('鼓手', @role_id, 0);