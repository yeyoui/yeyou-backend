package com.yeyou.yeyoubackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.contant.UserConstant;
import com.yeyou.yeyoubackend.exception.BusinessException;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.mapper.UserMapper;
import com.yeyou.yeyoubackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.ognl.CollectionElementsAccessor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yeyou.yeyoubackend.contant.UserConstant.USER_LOGIN_STATE;

/**
* @author lhy
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2023-03-13 12:41:58
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值
     */
    private static final String SALT = "yeyoui";
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String userCode) {
        //1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, userCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (userCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        //星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userCode",userCode);
        count = userMapper.selectCount(queryWrapper);
        if(count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");
        }
//        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        String encryptPassword=userPassword;//暂时不加密
        //新增用户
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
//        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
//        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        String encryptPassword = userPassword;
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword",encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }
    /**
     * 根据标签搜索用户
     * @param tagList 标签列表
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagList) {
        if(CollectionUtils.isEmpty(tagList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //通过sql查询
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        //拼接and查询
//        //like '%Java%' and like ...
//        for (String tagName : tagList) {
//            queryWrapper=queryWrapper.like("tags",tagName);
//        }
//        List<User> users = userMapper.selectList(queryWrapper);
//        return users.stream().map(this::getSafetyUser).collect(Collectors.toList());
        //内存查询(更灵活)
        Gson gson = new Gson();
        //先查询所有用户
        List<User> users = this.list();
        return users.stream().filter((user -> {
            String tags = user.getTags();
            Set<String> tempTagsSet = gson.fromJson(tags, new TypeToken<Set<String>>() {}.getType());
            tempTagsSet=Optional.of(tempTagsSet).orElse(new HashSet<>());
            for (String tag : tagList) {
                if(!tempTagsSet.contains(tag)) return false;
            }
            return true;
        })).map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 获取当前登录的用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Object curUser = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(curUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return (User) curUser;
    }

    /**
     * 更新用户信息
     */
    @Override
    public int updateUserBySelf(User user, User currentUser) {
        Long id = user.getId();
        if(id<0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //如果不是管理员且修改的不是自己的账号，抛出异常
        if(!Objects.equals(currentUser.getId(), id) && !isAdmin(currentUser)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //要更新的用户不存在
        if(this.getById(id)==null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return this.updateById(user) ?1:0;
    }

    /**
     * 通过User直接判断是否为管理员
     */
    @Override
    public boolean isAdmin(User currentUser) {
        return currentUser!=null && currentUser.getUserRole()== UserConstant.ADMIN_ROLE;
    }

    /**
     * 通过HttpServletRequest判断是否为管理员
     * (系统内部调用)
     */
    @Override
    public boolean isAdmin(HttpServletRequest request){
        if(request==null) return false;
        Object user = request.getSession().getAttribute(USER_LOGIN_STATE);
        return user==null && isAdmin((User) user);
    }
}




