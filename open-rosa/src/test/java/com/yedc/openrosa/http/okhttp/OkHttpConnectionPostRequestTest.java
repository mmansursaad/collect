package com.yedc.openrosa.http.okhttp;

import com.yedc.openrosa.http.OpenRosaHttpInterface;
import com.yedc.openrosa.http.OpenRosaPostRequestTest;

public class OkHttpConnectionPostRequestTest extends OpenRosaPostRequestTest {

    @Override
    protected OpenRosaHttpInterface buildSubject(OpenRosaHttpInterface.FileToContentTypeMapper mapper) {
        return new OkHttpConnection(
                null,
                mapper,
                "Test Agent"
        );
    }
}
