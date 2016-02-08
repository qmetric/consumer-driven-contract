package com.qmetric.consumerdrivencontract

import com.github.dreamhead.moco.HttpServer
import com.github.dreamhead.moco.Runner

import static com.github.dreamhead.moco.Moco.by
import static com.github.dreamhead.moco.Moco.httpServer
import static com.github.dreamhead.moco.Moco.text
import static com.github.dreamhead.moco.Moco.uri
import static com.github.dreamhead.moco.Runner.runner

class DummyService {

    private HttpServer server
    private Runner runner

    static DummyService createExampleFooProviderServiceListeningOnPort(final int port)
    {
        new DummyService(port)
    }

    private DummyService(final int port) {
        server = httpServer(port)
    }

    void configureBarToBeFive()
    {
        this.server.request(by(uri("/bar"))).response(text("5"));
        runner = runner(this.server);
        runner.start();
    }

    void shutdown()
    {
        runner?.stop()
    }
}
