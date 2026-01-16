package com.yedc.openrosa.http.okhttp;

import android.webkit.MimeTypeMap;

import com.yedc.openrosa.http.CollectThenSystemContentTypeMapper;
import com.yedc.openrosa.http.OpenRosaGetRequestTest;
import com.yedc.openrosa.http.OpenRosaHttpInterface;

public class OkHttpConnectionGetRequestTest extends OpenRosaGetRequestTest {

    @Override
    protected OpenRosaHttpInterface buildSubject() {
        return new OkHttpConnection(
                null,
                new CollectThenSystemContentTypeMapper(MimeTypeMap.getSingleton()),
                USER_AGENT
        );
    }
}
