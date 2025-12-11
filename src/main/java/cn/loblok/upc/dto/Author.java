package cn.loblok.upc.dto;

import lombok.Data;

/**
 * 作者信息
 */
@Data
public class Author {

    private Long id;

    private String name;

    private String avatar;


    private String handle;

    private int followers;
}