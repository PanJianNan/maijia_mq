package com.maijia.mq.domain.test;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Movie
 *
 * @author panjn
 * @date 2016/12/16
 */
public class Movie implements Serializable {



    private static final long serialVersionUID = -7314568433232287721L;
    private String name;
    private BigDecimal saleCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getSaleCount() {
        return saleCount;
    }

    public void setSaleCount(BigDecimal saleCount) {
        this.saleCount = saleCount;
    }

    @Override
    public String toString() {
        return "{name:" + name + ", saleCount:" + saleCount + "}";
    }

    @Override
    public boolean equals(Object obj) {
        return true;
    }

    public static void main(String[] args) {
        Movie movie = new Movie();
        User user = new User();
        System.out.println(movie.equals(user));
        System.out.println(movie.hashCode());
        System.out.println(user.hashCode());

        Integer integer = new Integer(1024);
        System.out.println(integer);
        add(integer);
        System.out.println(integer);
        String s = new String("123");
        add(s);
        System.out.println(s);
    }

    static void add(Integer integer) {
        integer ++;
        System.out.println(integer);
    }

    static void add(String integer) {
        integer += "hhaah";
        System.out.println(integer);
    }
}
