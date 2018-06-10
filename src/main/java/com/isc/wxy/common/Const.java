package com.isc.wxy.common;

import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by XY W on 2018/5/19.
 */
public class Const {
    public static final String USERNAME="username";
    public static final String EMAIL="email";

    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }
 }
