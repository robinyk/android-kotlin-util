package com.ownapp.core.util.permission;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import timber.log.Timber;

/**
 * Updated by Robin on 2020/12/4
 */

public final class PermissionUtil
{
    //**--------------------------------------------------------------------------------------------------
    //*      Enum
    //---------------------------------------------------------------------------------------------------*/
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            RequestCode.MULTIPLE
            , RequestCode.CAMERA
            , RequestCode.CALL_PHONE
            , RequestCode.READ_EXTERNAL_STORAGE
    })
    public @interface RequestCode {
        int MULTIPLE = 0;
        int CAMERA = 1;
        int CALL_PHONE = 2;
        int READ_EXTERNAL_STORAGE = 100;
        int ACCESS_COARSE_LOCATION = 400;
        int ACCESS_FINE_LOCATION = 401;
    }
    
    
    //**--------------------------------------------------------------------------------------------------
    //*      Constant
    //---------------------------------------------------------------------------------------------------*/
    public static final Permission CAMERA = new Permission(Manifest.permission.CAMERA, RequestCode.CAMERA);
    public static final Permission CALL_PHONE = new Permission(Manifest.permission.CALL_PHONE, RequestCode.CALL_PHONE);
    
    public static final Permission READ_EXTERNAL_STORAGE = new Permission(Manifest.permission.READ_EXTERNAL_STORAGE, RequestCode.READ_EXTERNAL_STORAGE);
    public static final Permission WRITE_EXTERNAL_STORAGE = new Permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101);
    
    public static final Permission ACCESS_COARSE_LOCATION = new Permission(Manifest.permission.ACCESS_COARSE_LOCATION, RequestCode.ACCESS_COARSE_LOCATION);
    public static final Permission ACCESS_FINE_LOCATION = new Permission(Manifest.permission.ACCESS_FINE_LOCATION, RequestCode.ACCESS_FINE_LOCATION);
    
    @SuppressLint("InlinedApi")
    public static final Permission USE_FINGERPRINT = new Permission(Manifest.permission.USE_FINGERPRINT, 500);
    public static final Permission USE_BIOMETRIC = new Permission(Manifest.permission.USE_BIOMETRIC, 501);
    
    
    //**--------------------------------------------------------------------------------------------------
    //*      Static
    //---------------------------------------------------------------------------------------------------*/
    public static boolean isAllowed(@NonNull final Context context, @NonNull final String permission)
    {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
    
    public static boolean check(@NonNull final Activity activity, @NonNull final Permission permission)
    {
        return check(activity, permission, null);
    }
    
    public static boolean check(@NonNull final Activity activity, @NonNull final Permission permission, @Nullable final Runnable action)
    {
        if(!isAllowed(activity, permission.getName()))
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission.getName()) && action != null)
                action.run();
            
            ActivityCompat.requestPermissions(activity, new String[]{ permission.getName() }, permission.getCode());
            return false;
        }
    
        return true;
    }
    
    public static boolean check(@NonNull final Activity activity, @NonNull final Permission[] permissions)
    {
        final String[] permissionArray = new String[permissions.length];
        
        boolean isAllowed = true;
        int i = 0;
    
        for(final Permission permission : permissions)
        {
            permissionArray[i++] = permission.getName();
            
            if (!isAllowed(activity, permission.getName()))
                isAllowed = false;
        }
    
        if(!isAllowed)
        {
            ActivityCompat.requestPermissions(activity, permissionArray, RequestCode.MULTIPLE);
        }
        
        return isAllowed;
    }
    
    public static void request(@NonNull final Activity activity, @NonNull final Permission permission)
    {
        request(activity, permission, null);
    }
    
    public static void request(@NonNull final Activity activity, @NonNull final Permission permission, @Nullable final Runnable action)
    {
        if (isAllowed(activity, permission.getName()))
        {
            Timber.d("%s is allowed", permission.getName());
            return;
        }
    
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission.getName()) && action != null)
            action.run();
        
        ActivityCompat.requestPermissions(activity, new String[]{ permission.getName() }, permission.getCode());
    }
}
