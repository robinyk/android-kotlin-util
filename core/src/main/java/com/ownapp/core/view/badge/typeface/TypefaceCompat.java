/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ownapp.core.view.badge.typeface;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;

import com.ownapp.core.view.badge.resource.ResourcesCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.collection.LruCache;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

/**
 * Helper for accessing features in {@link Typeface}.
 */
@SuppressLint("NewApi")  // TODO: Remove this suppression once Q SDK is released.
public class TypefaceCompat {
    private static final TypefaceCompatBaseImpl sTypefaceCompatImpl;
    static {
        if (Build.VERSION.SDK_INT >= 29) {
            sTypefaceCompatImpl = new TypefaceCompatApi29Impl();
        } else if (Build.VERSION.SDK_INT >= 28) {
            sTypefaceCompatImpl = new TypefaceCompatApi28Impl();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sTypefaceCompatImpl = new TypefaceCompatApi26Impl();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && TypefaceCompatApi24Impl.isUsable()) {
            sTypefaceCompatImpl = new TypefaceCompatApi24Impl();
        } else {
            sTypefaceCompatImpl = new TypefaceCompatApi21Impl();
        }
    }

    /**
     * Cache for Typeface objects dynamically loaded from assets.
     */
    private static final LruCache<String, Typeface> sTypefaceCache = new LruCache<>(16);

    private TypefaceCompat() {}

    /**
     * Find from internal cache.
     *
     * @return null if not found.
     * @hide
     */
    @Nullable
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public static Typeface findFromCache(@NonNull Resources resources, int id, int style) {
        return sTypefaceCache.get(createResourceUid(resources, id, style));
    }

    /**
     * Create a unique id for a given Resource and id.
     *
     * @param resources Resources instance
     * @param id a resource id
     * @param style style to be used for this resource, -1 if not available.
     * @return Unique id for a given resource and id.
     */
    private static String createResourceUid(final Resources resources, int id, int style) {
        return resources.getResourcePackageName(id) + "-" + id + "-" + style;
    }

    /**
     * Create Typeface from XML resource which root node is font-family.
     *
     * @return null if failed to create.
     * @hide
     */
    @Nullable
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public static Typeface createFromResourcesFamilyXml(
            @NonNull Context context, @NonNull FontResourcesParserCompat.FamilyResourceEntry entry,
            @NonNull Resources resources, int id, int style,
            @Nullable ResourcesCompat.FontCallback fontCallback, @Nullable Handler handler,
            boolean isRequestFromLayoutInflator) {
        Typeface typeface;
        if (entry instanceof FontResourcesParserCompat.ProviderResourceEntry) {
            FontResourcesParserCompat.ProviderResourceEntry providerEntry = (FontResourcesParserCompat.ProviderResourceEntry) entry;
            final boolean isBlocking = isRequestFromLayoutInflator
                    ? providerEntry.getFetchStrategy()
                    == FontResourcesParserCompat.FETCH_STRATEGY_BLOCKING
                    : fontCallback == null;
            final int timeout = isRequestFromLayoutInflator ? providerEntry.getTimeout()
                    : FontResourcesParserCompat.INFINITE_TIMEOUT_VALUE;
            typeface = FontsContractCompat.getFontSync(context, providerEntry.getRequest(),
                    fontCallback, handler, isBlocking, timeout, style);
        } else {
            typeface = sTypefaceCompatImpl.createFromFontFamilyFilesResourceEntry(
                    context, (FontResourcesParserCompat.FontFamilyFilesResourceEntry) entry, resources, style);
            if (fontCallback != null) {
                if (typeface != null) {
                    fontCallback.callbackSuccessAsync(typeface, handler);
                } else {
                    fontCallback.callbackFailAsync(
                            FontsContractCompat.FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR,
                            handler);
                }
            }
        }
        if (typeface != null) {
            sTypefaceCache.put(createResourceUid(resources, id, style), typeface);
        }
        return typeface;
    }

    /**
     * Used by Resources to load a font resource of type font file.
     * @hide
     */
    @Nullable
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public static Typeface createFromResourcesFontFile(
            @NonNull Context context, @NonNull Resources resources, int id, String path,
            int style) {
        Typeface typeface = sTypefaceCompatImpl.createFromResourcesFontFile(
                context, resources, id, path, style);
        if (typeface != null) {
            final String resourceUid = createResourceUid(resources, id, style);
            sTypefaceCache.put(resourceUid, typeface);
        }
        return typeface;
    }

    /**
     * Create a Typeface from a given FontInfo list and a map that matches them to ByteBuffers.
     * @hide
     */
    @Nullable
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public static Typeface createFromFontInfo(@NonNull Context context,
                                              @Nullable CancellationSignal cancellationSignal, @NonNull FontsContractCompat.FontInfo[] fonts, int style) {
        return sTypefaceCompatImpl.createFromFontInfo(context, cancellationSignal, fonts, style);
    }

    /**
     * Retrieves the best matching font from the family specified by the {@link Typeface} object
     */
    @Nullable
    private static Typeface getBestFontFromFamily(final Context context, final Typeface typeface,
            final int style) {
        final FontResourcesParserCompat.FontFamilyFilesResourceEntry families = sTypefaceCompatImpl.getFontFamily(typeface);
        if (families == null) {
            return null;
        }

        return sTypefaceCompatImpl.createFromFontFamilyFilesResourceEntry(context, families,
                context.getResources(), style);
    }

    /**
     * Retrieves the best matching typeface given the family, style and context.
     * If null is passed for the family, then the "default" font will be chosen.
     *
     * @param family The font family. May be null.
     * @param style  The style of the typeface. e.g. NORMAL, BOLD, ITALIC, BOLD_ITALIC
     * @param context The context used to retrieve the font.
     * @return The best matching typeface.
     */
    @NonNull
    public static Typeface create(@NonNull final Context context, @Nullable final Typeface family,
            final int style) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        Typeface typefaceFromFamily = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            typefaceFromFamily = TypefaceCompat.getBestFontFromFamily(context, family, style);
            if (typefaceFromFamily != null) {
                return typefaceFromFamily;
            }
        }

        return Typeface.create(family, style);
    }
}
