package com.ownapp.core.util.permission;

import androidx.annotation.NonNull;

/**
 * Updated by Robin on 2020/12/4
 */

public class Permission
{
    public String name;
    public int code;
    
    public Permission(@NonNull String name, int code)
    {
        this.name = name;
        this.code = code;
    }
    
    public String getName()
    {
        return name;
    }
    
    public int getCode()
    {
        return code;
    }
}
