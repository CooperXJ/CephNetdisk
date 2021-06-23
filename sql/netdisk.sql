/*
 Navicat Premium Data Transfer

 Source Server         : netdisk
 Source Server Type    : MySQL
 Source Server Version : 50651
 Source Host           : 172.23.27.118:3306
 Source Schema         : netdisk

 Target Server Type    : MySQL
 Target Server Version : 50651
 File Encoding         : 65001

 Date: 23/06/2021 09:36:10
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chat_records
-- ----------------------------
DROP TABLE IF EXISTS `chat_records`;
CREATE TABLE `chat_records` (
  `sender_id` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `receiver_id` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `ctime` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `content` text COLLATE utf8mb4_bin,
  `is_read` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`receiver_id`,`sender_id`,`ctime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of chat_records
-- ----------------------------
BEGIN;
INSERT INTO `chat_records` VALUES ('sunqq', 'administrator', '2021-06-19 21:50:39', 'nijiaosmmingzi', 1);
INSERT INTO `chat_records` VALUES ('sunqq', 'administrator', '2021-06-22 14:30:49', '<img id=\"2.jpeg\" class=\"showImg\" src=\"http://172.23.27.119:7480/PublicPicture/2.jpeg\" style=\"max-width: 200px;max-height: 150px\">', 1);
INSERT INTO `chat_records` VALUES ('sunqq', 'administrator', '2021-06-22 14:33:10', '<img id=\"2.jpeg\" class=\"showImg\" src=\"http://172.23.27.119:7480/PublicPicture/2.jpeg\" style=\"max-width: 200px;max-height: 150px\">', 1);
INSERT INTO `chat_records` VALUES ('sunqq', 'administrator', '2021-06-22 17:03:53', '<img id=\"logo.jpg\" class=\"showImg\" src=\"http://172.23.27.119:7480/PublicPicture/logo.jpg\" style=\"max-width: 200px;max-height: 150px\">', 1);
INSERT INTO `chat_records` VALUES ('zt', 'administrator', '2021-06-19 21:55:58', '11', 1);
INSERT INTO `chat_records` VALUES ('administrator', 'sunqq', '2021-06-19 21:50:32', 'nihao', 1);
INSERT INTO `chat_records` VALUES ('administrator', 'sunqq', '2021-06-22 17:04:42', '<br><img id=\"1.jpeg\" class=\"showImg\" src=\"http://172.23.27.119:7480/PublicPicture/1.jpeg\" style=\"max-width: 200px;max-height: 150px\">', 1);
INSERT INTO `chat_records` VALUES ('xuejin', 'sunqq', '2021-06-23 09:05:51', 'hello<div><br></div>', 1);
INSERT INTO `chat_records` VALUES ('xuejin', 'sunqq', '2021-06-23 09:10:27', '你好', 1);
INSERT INTO `chat_records` VALUES ('sunqq', 'xuejin', '2021-06-23 09:05:16', '???', 1);
INSERT INTO `chat_records` VALUES ('sunqq', 'xuejin', '2021-06-23 09:06:06', '??', 1);
COMMIT;

-- ----------------------------
-- Table structure for file_recover
-- ----------------------------
DROP TABLE IF EXISTS `file_recover`;
CREATE TABLE `file_recover` (
  `username` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `file_name` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `ctime` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `bucket_name` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`username`,`file_name`,`bucket_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of file_recover
-- ----------------------------
BEGIN;
INSERT INTO `file_recover` VALUES ('zt', '10121865___.mp4', '2021-06-20 20:14:57', 'ztt-f483211fe16a40cf94c6f0f8ab949b29');
INSERT INTO `file_recover` VALUES ('zt', '10121868___.mp4', '2021-06-20 17:51:24', 'ztt-f483211fe16a40cf94c6f0f8ab949b29');
INSERT INTO `file_recover` VALUES ('zt', '402.jpg', '2021-06-20 17:51:31', 'ztt-f483211fe16a40cf94c6f0f8ab949b29');
INSERT INTO `file_recover` VALUES ('zt', '403.png', '2021-06-19 21:32:55', 'ztt-f483211fe16a40cf94c6f0f8ab949b29');
INSERT INTO `file_recover` VALUES ('zt', '404.jpg', '2021-06-20 20:14:49', 'ztt-f483211fe16a40cf94c6f0f8ab949b29');
INSERT INTO `file_recover` VALUES ('zt', '411.jpg', '2021-06-20 20:15:05', 'ztt-f483211fe16a40cf94c6f0f8ab949b29');
INSERT INTO `file_recover` VALUES ('zt', '412.jpg', '2021-06-20 20:15:09', 'ztt-f483211fe16a40cf94c6f0f8ab949b29');
INSERT INTO `file_recover` VALUES ('zt', '413.jpg', '2021-06-20 20:15:18', 'ztt-f483211fe16a40cf94c6f0f8ab949b29');
INSERT INTO `file_recover` VALUES ('zt', '414.jpg', '2021-06-20 20:15:15', 'ztt-f483211fe16a40cf94c6f0f8ab949b29');
INSERT INTO `file_recover` VALUES ('zt', '420.jpg', '2021-06-20 20:14:46', 'ztt-f483211fe16a40cf94c6f0f8ab949b29');
INSERT INTO `file_recover` VALUES ('zt', '421.jpg', '2021-06-20 11:49:30', 'ztt-f483211fe16a40cf94c6f0f8ab949b29');
INSERT INTO `file_recover` VALUES ('zt', '422.jpg', '2021-06-20 01:17:34', 'zt-6598401ac2054073b1ff19caf1d2d9f0');
INSERT INTO `file_recover` VALUES ('zt', '422.jpg', '2021-06-20 11:49:23', 'ztt-f483211fe16a40cf94c6f0f8ab949b29');
INSERT INTO `file_recover` VALUES ('zt', '426.jpg', '2021-06-20 11:49:26', 'ztt-f483211fe16a40cf94c6f0f8ab949b29');
INSERT INTO `file_recover` VALUES ('zt', '429.jpg', '2021-06-20 16:43:14', 'zt-6598401ac2054073b1ff19caf1d2d9f0');
INSERT INTO `file_recover` VALUES ('zt', '429.jpg', '2021-06-20 11:49:18', 'ztt-f483211fe16a40cf94c6f0f8ab949b29');
INSERT INTO `file_recover` VALUES ('zt', '??4.jpg', '2021-06-20 17:51:48', 'ztt-f483211fe16a40cf94c6f0f8ab949b29');
INSERT INTO `file_recover` VALUES ('zt', '??6.jpg', '2021-06-20 01:17:29', 'zt-6598401ac2054073b1ff19caf1d2d9f0');
INSERT INTO `file_recover` VALUES ('zt', 'google-access-helper-2.3.0.zip', '2021-06-20 21:55:28', 'gr-7ba1f4cc44c947e483b25de5003ee4df');
INSERT INTO `file_recover` VALUES ('zt', 'jquery-easyui-1.8.6.zip', '2021-06-20 21:55:25', 'gr-7ba1f4cc44c947e483b25de5003ee4df');
COMMIT;

-- ----------------------------
-- Table structure for friends
-- ----------------------------
DROP TABLE IF EXISTS `friends`;
CREATE TABLE `friends` (
  `user_id` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `friend_id` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `ctime` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `is_accept` int(11) NOT NULL DEFAULT '0',
  `reason` varchar(500) COLLATE utf8mb4_bin DEFAULT NULL,
  `type` int(11) NOT NULL,
  PRIMARY KEY (`user_id`,`friend_id`,`ctime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of friends
-- ----------------------------
BEGIN;
INSERT INTO `friends` VALUES ('administrator', 'sunqq', '2021-06-19 21:50:19', 1, '', 1);
INSERT INTO `friends` VALUES ('administrator', 'zt', '2021-06-19 21:55:37', 1, '', 1);
INSERT INTO `friends` VALUES ('sunqq', 'administrator', '2021-06-19 21:50:13', 1, '', 0);
INSERT INTO `friends` VALUES ('sunqq', 'xuejin', '2021-06-23 09:05:04', 1, '', 1);
INSERT INTO `friends` VALUES ('xuejin', 'sunqq', '2021-06-23 09:04:50', 1, '', 0);
INSERT INTO `friends` VALUES ('zt', 'administrator', '2021-06-19 21:55:27', 1, '1', 0);
COMMIT;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `nick_name` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `header_img` varchar(100) COLLATE utf8mb4_bin DEFAULT 'https://img2.baidu.com/it/u=44947549,676932814&fm=26&fmt=auto&gp=0.jpg',
  `password` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `active_code` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL,
  `active_status` int(11) NOT NULL,
  `security_code` int(11) DEFAULT NULL,
  `access_key` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `secret_key` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `max_storage_number` int(11) NOT NULL,
  `storage_space` int(11) NOT NULL,
  `recover_bucket` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Records of user
-- ----------------------------
BEGIN;
INSERT INTO `user` VALUES (1, 'Xiaoming', 'Xiaoming', 'https://img2.baidu.com/it/u=44947549,676932814&fm=26&fmt=auto&gp=0.jpg', '123456', '1789023580@qq.com', NULL, 1, NULL, 'ZKGKRV3NRHIJQAAHIXHY', 'GCgeuAKuBbTCGHfCzYCWWzx15w7cCJPlpvmKZ7Qf', 1000, 6291456, 'recover-cee938b15f7b40778f2f138daa3be1a3');
INSERT INTO `user` VALUES (6, 'sunqq', '孙庆庆', 'https://img2.baidu.com/it/u=44947549,676932814&fm=26&fmt=auto&gp=0.jpg', '123456', '1789023580@qq.com', NULL, 1, NULL, 'GNQWW31D7MGXXN9TEOXT', 'Gl93Dzq5UiLzYLrUMqAZN5GdsTLNIS4TRcpDQH2E', 1000, 6291456, 'recover-f32436678852425ba787930ff61ed668');
INSERT INTO `user` VALUES (8, 'administrator', 'administrator', 'https://img2.baidu.com/it/u=44947549,676932814&fm=26&fmt=auto&gp=0.jpg', '123456', '1789023580@qq.com', NULL, 1, NULL, 'EI1X4TCMLQ29YYWHMXNP', 'zm3zkyPwYnfBpCQwBQN6xA2YRuFPFxQif5e41zsj', 1000, 6291456, 'recover-5d9d1af5d0e240c8a5371ab0beb73491');
INSERT INTO `user` VALUES (10, 'zt', 'zt', 'https://img2.baidu.com/it/u=44947549,676932814&fm=26&fmt=auto&gp=0.jpg', '1234567', 'zhoudandashu@qq.com', NULL, 1, NULL, 'UAP72NDGTDB1XVW85KL7', '5eK5lTq4sRAHvYVGLo71IyME9NyR7MhhwDaXe4jN', 1000, 6291456, 'recover-ca5e0625d5ac4f6cb3512fb2053ea088');
INSERT INTO `user` VALUES (14, 'xuejin', 'Cooper', 'https://img2.baidu.com/it/u=44947549,676932814&fm=26&fmt=auto&gp=0.jpg', '123456', '17890235890@qq.com', '7093efe29db147b78318496880265143', 0, NULL, '8RSB6SN2OXGAMC2H0CCT', 'rCSgAvmIvlR6f63XsN4Ud7LjfF97L0Noc8i13Nar', 1000, 6291456, 'recover-e6ba1a4e767045db9af6c1cae51d66e9');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
