package com.yeyou.yeyoubackend.controller;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;

import com.google.gson.reflect.TypeToken;
import com.yeyou.yeyoubackend.common.BaseResponse;
import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.common.ResultUtils;
import com.yeyou.yeyoubackend.contant.RedisConstant;
import com.yeyou.yeyoubackend.exception.BusinessException;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.model.request.UserLoginRequest;
import com.yeyou.yeyoubackend.model.request.UserRegisterRequest;
import com.yeyou.yeyoubackend.model.vo.MyPage;
import com.yeyou.yeyoubackend.model.vo.UserVo;
import com.yeyou.yeyoubackend.service.UserService;
import com.yeyou.yeyoubackend.utils.StringRedisCacheUtils;
import com.yeyou.yeyoubackend.utils.UserHold;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yeyou.yeyoubackend.contant.RedisConstant.USER_RECOMMEND_KEY;
import static com.yeyou.yeyoubackend.contant.RedisConstant.USER_RECOMMEND_LOCK;
import static com.yeyou.yeyoubackend.contant.UserConstant.ADMIN_ROLE;
import static com.yeyou.yeyoubackend.contant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
//@CrossOrigin(origins = {"http://localhost:3000"})
@Slf4j
public class UserController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserService userService;
    @Resource
    private StringRedisCacheUtils redisCacheUtils;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        if(user==null) return ResultUtils.error(ErrorCode.NOT_LOGIN, "账号或密码错误");
        String token = UUID.randomUUID().toString();
        Gson gson = new Gson();
        stringRedisTemplate.opsForValue().set(RedisConstant.USER_TOKEN_KEY+token,gson.toJson(user));
        return ResultUtils.loginSuccess(user,token);
    }


    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
//        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
//        User currentUser = (User) userObj;
        User currentUser = UserHold.get();
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();

        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUerByTags(@RequestParam(required = false) List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> users = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(users);
    }


    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize,long pageNum,HttpServletRequest request){
        MyPage<User> queryPage = new MyPage<>(pageNum, pageSize);
        Type retType = new TypeToken<MyPage<User>>(){}.getType();

        Page<User> page =redisCacheUtils.queryWithLock(USER_RECOMMEND_KEY,queryPage,retType,USER_RECOMMEND_LOCK,
                (userPage -> userService.page(userPage)),1,TimeUnit.MINUTES);
        //无缓存，重新获取数据
//        Page<User> page = userService.page(new Page<>(pageNum, pageSize));
        return ResultUtils.success(page);
    }

    @GetMapping("/randomUser")
    public BaseResponse<List<User>> randomUser(int num,HttpServletRequest request){
        List<User> randomUser = userService.getRandomUser(num);
        randomUser = randomUser.stream().map(userService::getSafetyUser).collect(Collectors.toList());

        return ResultUtils.success(randomUser);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user,HttpServletRequest request){
        if(user==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User curUser = userService.getLoginUser(request);
        int result = userService.updateUserBySelf(user, curUser);
        return ResultUtils.success(result);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    private boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 获取最匹配的前n个用户
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/matchUsersByTags")
    public BaseResponse<List<User>> matchUsers(long num,HttpServletRequest request){
        if(num<0 ||num>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.mathUsers(num, loginUser));
    }

    @GetMapping("/getMyTags")
    public BaseResponse<List<String>> getMyTags(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        List<String> tags=userService.getMyTags(loginUser);
        return ResultUtils.success(tags);
    }

    @GetMapping("/updateMyTags")
    public BaseResponse<Boolean> updateMyTags(@RequestParam(required = false) List<String> tagNameList, HttpServletRequest request){
        //1.校验参数信息
        if(tagNameList==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Boolean success = userService.updMyTags(tagNameList, loginUser);
        return ResultUtils.success(success);
    }

}
