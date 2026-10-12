package com.itheima.anno;

import com.itheima.validation.StateValidation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

//元注解
@Documented //标识此注解可以抽取到dox文档
@Target({ FIELD })//标识将来此注解用在哪些地方：属性、类、方法等
@Retention(RUNTIME)//标识此注解在哪个阶段被保留：编译阶段，运行阶段等

@Constraint(validatedBy = {StateValidation.class }) //指定提供校验规则的类

public @interface State {
    //提供校验失败后的提示信息
    String message() default "State参数的值只能是已发布或者草稿";
    //指定分组
    Class<?>[] groups() default { };
    //负载 获取到State注解的附加信息
    Class<? extends Payload>[] payload() default { };
}