package com.maijia.mq.domain.test;

/**
 * User
 *
 * @author panjn
 * @date 2016/12/16
 */
public class User {
    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "{name:" + name + ", age:" + age + "}";
    }
}
