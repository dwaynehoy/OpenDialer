package com.strone.stronedialer.data;

import android.net.Uri;

/**
 * A base class containing build type specific provider constants
 */
public class CacheContractBase {
    public static final String AUTHORITY = "co.strone.stronedialer.test.cache";
    public static final Uri AUTHORITY_URI =  Uri.parse("content://" + AUTHORITY);
}
