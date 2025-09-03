package com.jed.optima.openrosa.http.okhttp;

import android.webkit.MimeTypeMap;

import com.jed.optima.openrosa.http.CollectThenSystemContentTypeMapper;
import com.jed.optima.openrosa.http.OpenRosaGetRequestTest;
import com.jed.optima.openrosa.http.OpenRosaHttpInterface;

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
