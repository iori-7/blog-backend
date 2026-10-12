package com.itheima.pojo;



import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
//lombok  在编译阶段，为实体类自动生成getter setter toString HashCode 方法
//pom文件引入依赖 实体类添加注解
@Data
public class User {

    @NotNull
    private Integer id;//主键ID
    private String username;//用户名
    @JsonIgnore //Jackson 的注解：序列化时忽略 password，接口返回的 JSON 里不会带密码
    private String password;//密码

    @NotEmpty
    @Pattern(regexp = "^\\S{1,10}$")
    private String nickname;//昵称

    @NotEmpty
    @Email
    private String email;//邮箱
    private String userPic;//用户头像地址
    private LocalDateTime createTime;//创建时间
    private LocalDateTime updateTime;//更新时间
}
