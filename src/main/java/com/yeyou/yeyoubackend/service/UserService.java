package com.yeyou.yeyoubackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoucommon.model.vo.UserVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author lhy
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2023-03-13 12:41:58
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     * @param tagList 标签列表
     */
    List<User> searchUsersByTags(List<String> tagList);

    /**
     * 获取当前登录用户信息
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 更新用户数据
     * @param user
     * @param currentUser
     * @return
     */
    int updateUserBySelf(User user, User currentUser);


    /**
     * 通过User直接判断是否为管理员
     */
    boolean isAdmin(User currentUser);

    /**
     * 通过HttpServletRequest判断是否为管理员
     * (系统内部调用)
     */
     boolean isAdmin(HttpServletRequest request);

    /**
     * 匹配相似用户
     * @param num
     * @param loginUser
     * @return
     */
    List<User> mathUsers(long num, User loginUser);
    /**
     * 缓存匹配相似用户
     * @param num
     * @param loginUser
     * @return
     */
    List<User> cacheMathUsers(long num, User loginUser);

    /**
     * 随机获取用户
     */
    List<User> getRandomUser(int num);


    /**
     * 获取登录用户的所有标签
     * @param loginUser
     * @return
     */
    List<String> getMyTags(User loginUser);

    /**
     * 更新登录用户的标签
     */
    Boolean updMyTags(List<String> tags,User loginUser);

    /**
     * 随机初始头像
     * @return
     */
    String randomUserIcon();

    /**
     * 获取脱敏的用户信息
     * @param user
     * @return
     */
    UserVo getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVo> getUserVO(List<User> userList);

}
