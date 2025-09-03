package com.jed.optima.openrosa.http.okhttp;

import androidx.annotation.NonNull;

import com.jed.optima.openrosa.http.HttpCredentialsInterface;

public interface OpenRosaServerClientProvider {

    OpenRosaServerClient get(String schema, String userAgent, @NonNull HttpCredentialsInterface credentialsInterface);
}
