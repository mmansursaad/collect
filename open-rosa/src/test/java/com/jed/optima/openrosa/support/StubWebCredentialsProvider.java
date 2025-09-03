package com.jed.optima.openrosa.support;

import androidx.annotation.NonNull;

import com.jed.optima.openrosa.forms.OpenRosaXmlFetcher;
import com.jed.optima.openrosa.http.HttpCredentials;
import com.jed.optima.openrosa.http.HttpCredentialsInterface;

import java.net.URI;

public class StubWebCredentialsProvider implements OpenRosaXmlFetcher.WebCredentialsProvider {

    @Override
    public HttpCredentialsInterface getCredentials(@NonNull URI url) {
        return new HttpCredentials(null, null);
    }
}
