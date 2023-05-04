package com.yeyou.yeyoucommon.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yeyou.yeyoucommon.model.domain.Post;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 文章用户视图
 */
@Data
public class PostVO implements Serializable {

    private static final long serialVersionUID = -8724867714106027444L;

    private final static Gson GSON=new Gson();
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 标签 json 列表(数组)
     */
    private List<String> tagList;

    /**
     * 作者信息
     */
    private UserVo author;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否已点赞
     */
    private Boolean hasThumb;

    /**
     * 是否已收藏
     */
    private Boolean hasFavour;

    /**
     * 包装类转对象
     * @param postVO
     * @return
     */
    public static Post voToObj(PostVO postVO){
        if(postVO==null){
            return null;
        }
        Post post = new Post();
        BeanUtils.copyProperties(postVO, post);
        List<String> tagList=postVO.getTagList();
        if(!CollectionUtils.isEmpty(tagList)){
            post.setTags(GSON.toJson(tagList));
        }
        return post;
    }

    /**
     * 对象转包装类
     * @param post
     * @return
     */
    public static PostVO objToVo(Post post){
        if(post==null){
            return null;
        }
        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post,postVO);

        String tags = post.getTags();
        if(StringUtils.isNotBlank(tags)){
            postVO.setTagList(GSON.fromJson(tags,new TypeToken<List<String>>(){}.getType()));
        }
        return postVO;
    }
}
