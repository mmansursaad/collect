package com.jed.optima.openrosa.http.okhttp;

import com.jed.optima.openrosa.http.OpenRosaHttpInterface;
import com.jed.optima.openrosa.http.OpenRosaPostRequestTest;

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
